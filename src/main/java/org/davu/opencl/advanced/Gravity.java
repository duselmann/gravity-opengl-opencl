// Copyright (c) 2022 David Uselmann
package org.davu.opencl.advanced;

import static org.davu.opencl.utils.CLInfo.*;
import static org.davu.opencl.utils.CLUtils.*;
import static org.davu.opencl.utils.CLUtils.Platforms.*;
import static org.lwjgl.opencl.CL10.*;

import java.awt.Frame;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.davu.opencl.utils.CLUtils;
import org.davu.opencl.utils.CLUtils.PlatformDevice;
import org.dynamics.math.Distance;
import org.dynamics.math.Vector;
import org.dynamics.screen.DisplayCanavs;
import org.dynamics.screen.Window;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.CLContextCallback;
import org.lwjgl.opencl.CLProgramCallback;


public class Gravity {
	static final Logger log = LogManager.getLogger(Gravity.class);

    public static long GPU_THREADS = 256;

    protected  DisplayCanavs displayCanavs;
    protected  Window window;

    protected  long kernel;
    protected  long program;
    protected  long oldPosMem;
    protected  long newPosMem;
    protected  long oldVelMem;
    protected  long newVelMem;
    protected  long queue;
    protected  long context;
    protected  boolean running = true;
    protected  int tooCloseCount = 0;
    protected  float dt = 0.1f;  // the time step - delta t
    protected  int iters = 10;    // iterations to perform between displays
    protected  int numBodies = 1024*4;
    protected  float massBase = 5f;
    protected  float velBase = 7;
    protected  String kernelFunc = "gravity";
    protected  boolean isDarkMatter = false;
    protected  float dmRadius3 = 0;
    protected  float[] dmCenter = new float[4];

    public static void main(String[] args) throws Exception {
        Gravity gravity = new Gravity();
        gravity.createWindow();
        gravity.displayCanavs._3D = true;
        gravity.iters = 10;
        gravity.compute();
    }

    public void setNumBodies(int numberOfBodies) {
        // 4 vector: x,y,z position and mass
        numBodies = numberOfBodies;
        if (numBodies % GPU_THREADS != 0) {
            System.out.println("Number of bodies should be an even multiple of GPU Workgroup size, aka threads.");
        }
    }

    public  void displayCallBack() {
        displayCanavs.setRadiusCallBack(v -> (float)((v.z+250)/333));
    }
    public  void createWindow() {
        window = new Window(()->stop());
        window.setExtendedState(window.getExtendedState() | Frame.MAXIMIZED_BOTH);
//        try {Thread.sleep(3000);} catch (Exception e) {}
        displayCanavs = window.canvas;
        displayCanavs.setTranslation(1000,500);
        displayCallBack();
    }
    public  void display(FloatBuffer bodies) {
        displayCanavs.bodies = bodies;
        displayCanavs.repaint();
    }


    public void compute() throws IOException {
        try {
            IntBuffer numBodiesBuff = BufferUtils.createIntBuffer(1);
            numBodiesBuff.put(numBodies);
            numBodiesBuff.rewind();

            log.info("initialize - bodies intial conditions");
            float[] bodies    = new float[numBodies*4];
            float[] velocity  = new float[numBodies*4];
            initBodies(bodies, velocity);

            log.info("initialize - bodies zero conditions");
            float[] newBodies = new float[numBodies*4];
            float[] newVel    = new float[numBodies*4];
            initNewBodies(newBodies, newVel);

            FloatBuffer bodiesBuffer = createFloatBuffer(bodies);
            FloatBuffer velBuffer    = createFloatBuffer(velocity);
            FloatBuffer newBodBuffer = createFloatBuffer(newBodies);
            FloatBuffer newVelBuffer = createFloatBuffer(newVel);

            PlatformDevice platformDevice = getGPU(NVIDIA);
            displayInfo(platformDevice, false);
            PointerBuffer device = platformDevice.device;
            long deviceId = device.get();
            device.rewind();
            try {
                // Expect max threads to be 256 (Intel) or 1024 (Nvidia)
                GPU_THREADS = getDeviceInfoNumber(deviceId, CL_DEVICE_MAX_WORK_GROUP_SIZE, SIZE_OF_LONG);
            } catch (Exception e) {
            	log.error("Attempting to use default thread count");
            }
            log.info("GPU Thread Count: {}", GPU_THREADS);

            // NOTE: Cannot reallocate a new buffer with the pointer values - it does not work.
            //       The original buffer must have more than just a pointer to the device.
            //       PointerBuffer buff = BufferUtils.createPointerBuffer(1).put(device.rewind().get()).rewind();
            //       long context = clCreateContext(null, buff, null, 0L, returnCode);

            // NOTE: This context callback is never called. It is unclear why. Other demos fail to call it too.
            CLContextCallback contextCallback = CLContextCallback.create(
                    (errInfo, privateInfo, cb, userData) -> System.out.println("Creating Context"));
            // NOTE: Platform is optional here in the first parameter which is a properties PointerBuffer
            //       The device address pointer is already pointing to the GPU.
            context = CLUtils.createContext(device, contextCallback);
            // NOTE: Must free the callback because it has a pointer injected into the GPU to trigger.
            contextCallback.free();
            // NOTE: Throughout this call chain, if any fails then then next could cause a JVM core dump.
            queue  = createCommandQ(context, device.get());

            // NOTE: Local memory structures can (should?) be created on the GPU using a size parameter
            //       Sending an allocated memory structure is unnecessary
            // FloatBuffer localPositions  = BufferUtils.createFloatBuffer(numBodies*4);
            // long locPosMem = createMemoryBuffer( context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, localPositions);


            // NOTE: Memory Buffers must be assigned to the context. Options are read, write, and read/write.
            //       It is recommended to use the minimum requirement. The memory copy host pointer was required.
            //       Fail code CL_INVALID_HOST_PTR, -37, is returned during enqueue without the pointer flag.
            // NOTE: Enqueue of the read of the results occurs after the kernel program runs.
            // NOTE: C source files can be loaded or hard coded strings.
            //       The extension is arbitrary. If your IDE accepts *.c or *.cpp then that is fine also.
            String source = loadResourceString("cl/gravity-cl.txt");

            // NOTE: This just waits for the write buffers to finish
            waitForQueue(queue);

            // NOTE: This callback was called regularly, unlike the context callback.
            CLProgramCallback buildCallback = CLProgramCallback.create(
                    (programPtr, userData) -> log.info("CL Program - Compile"));
            // NOTE: Load the program string into the GPU context and initiate a compile.
            //       The first arg is the devices id for looking up compile errors only.
            //       The second arg is the context id for the kernel.
            //       The third arg is the source code string.
            //       The fourth arg string is a compiler option(s) string similar to shell gcc.
            //       The last arg is a build callback - not sure how useful this is
            // NOTE: not sure if -cl-fast-relaxed-math would help performance enough
            log.info("CL Program - build");
            program = buildProgram(deviceId, context, source, buildCallback);
            // NOTE: Must free the callback because it has a pointer injected into the GPU to trigger.
            buildCallback.free();


            log.info("initialize - GPU memory structures");

            IntBuffer returnCode = BufferUtils.createIntBuffer(1);
            kernel = clCreateKernel(program, kernelFunc, returnCode);
            isSuccess(returnCode);

            oldPosMem = writeBuffer(queue,  context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, bodiesBuffer);
            newPosMem = createMemoryBuffer( context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, newBodBuffer);

            oldVelMem  = writeBuffer(queue, context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, velBuffer);
            newVelMem  = createMemoryBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, newVelBuffer);

            long[] positions  = new long[] { oldPosMem, newPosMem };
            long[] velocities = new long[] { oldVelMem, newVelMem };

            int current_old = 1;
            int current_new = 0;

            log.info("iteration - commence");
            while (running) {
                for(int iter=0; iter<iters; iter++) {
                    // toggle old and new memory
                    current_old = current_old == 1 ?0 :1;
                    current_new = current_new == 1 ?0 :1;
                    int result = 0;
                    int arg=0;
                    pushKernelStackPointer(kernel, arg++, positions[current_new]);
                    pushKernelStackPointer(kernel, arg++, velocities[current_new]);
                    pushKernelStackPointer(kernel, arg++, positions[current_old]);
                    pushKernelStackPointer(kernel, arg++, velocities[current_old]);
                    result = clSetKernelArg1f(kernel, arg++, dt);
                    isSuccess(result);
                    if (isDarkMatter) {
                        result = clSetKernelArg4f(kernel, arg++, dmCenter[0],dmCenter[1],dmCenter[2],dmCenter[3]);
                        isSuccess(result);
                        result = clSetKernelArg1f(kernel, arg++, dmRadius3);
                        isSuccess(result);
                    }

                    // NOTE: Send a signal to execute the kernel with return buffer size
                    //       One dimensional array.
                    int globalCols = numBodies;
                    int globalRows = 1;
                    int localCols = Math.max(1, (int) (numBodies / GPU_THREADS));
                    int localRows = 1;
                    pushKernelToQueue(queue, kernel, globalCols, globalRows, localCols, localRows);

                    waitForQueue(queue);
                    // NOTE: If successful then read the results memory, rMem, back into the results buffer, rBuffer.
                    popQueueResultBuffer(queue, newVelMem, velBuffer);
                    popQueueResultBuffer(queue, newPosMem, bodiesBuffer);

                    // LOGGING: displayBufferContent("updated value pos and vel", 4, bodiesBuffer, velBuffer);

                    // NOTE: This waits for the buffer transfer to finish
                    waitForQueue(queue);
                }

                display(bodiesBuffer);

                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            log.info("iteration - EXIT");
            release();
        }
    }

    /**
     * Initialize all points position-mass and velocity.
     * @param bodies
     * @param velocity
     * @param newBodies
     * @param newVel
     */
    public void initBodies(float[] bodies, float[] velocity) {
        Random rand = new Random();
        for(int b=0; b<numBodies; b++) {
            boolean generate = true;
            while (generate) {
                Vector pos = new Vector(rand.nextGaussian(),rand.nextGaussian(),rand.nextGaussian())
                        .normalize()
                        .scalarMultiply(100*Math.random()+400);
                bodies[b*4 + 0] = (int)pos.x;
                bodies[b*4 + 1] = (int)pos.y;
                bodies[b*4 + 2] = (int)pos.z+500;
                generate = checkNearPoint(400, b, bodies);
            }
            bodies[b*4 + 3] = massBase*(float)Math.random(); // mass
            velocity[b*4 + 0] =  (float)Math.random()*velBase-velBase/2;
            velocity[b*4 + 1] =  (float)Math.random()*velBase-velBase/2;
            velocity[b*4 + 2] =  (float)Math.random()*velBase-velBase/2;
            velocity[b*4 + 3] = 0f;
        }

        // LOGGING: System.out.println("tooCloseCount = " + tooCloseCount);
    }


    /**
     * Since the typical initialization for all new arrays is zero,
     * this is a default method.
     * @param newBodies
     * @param newVel
     */
    public void initNewBodies(float[] newBodies, float[] newVel) {
        Arrays.fill(newBodies, 0);
        Arrays.fill(newVel, 0);
    }



    /**
     * Used to determine if a current point is too close to the previous points.
     * If points are too close then there could be a floating point error.
     * @param sqrDist the tolerated square distance between points.
     * @param body    the point index within the array of points.
     * @param bodies  all the points array.
     * @return true if within the square distance
     */
    public  boolean checkNearPoint(int sqrDist, int body, float[] bodies) {
        boolean near = false;

        // the new point based on the body index
        Vector position = new Vector(bodies[body*4+0], bodies[body*4+1], bodies[body*4+2]);

        // check all previous points to the checked point
        for (int b=0; b<body-1; b++) {
            Vector other = new Vector(bodies[b*4+0], bodies[b*4+1], bodies[b*4+2]);
            Distance distance = new Distance(position, other);
            if (distance.magnitudeSQRD() < sqrDist) {
                tooCloseCount ++;
                near = true;
                break;
            }
        }

        return near;

    }


    /**
     * Clean up all OpenCL allocations: CL_MEM, Queue, Context, Kernels, and Programs.
     */
    public  void release() {
    	System.out.println();
        log.info("cleanup - GPU");
        try {
            log.info("cleanup - wait for gueue");
            waitForQueue(queue);
        } catch (Exception e) {
            // try to free the reset
        }
        // NOTE: OpenCL is in the C domain and resource cleanup is required to prevent memory leaks.
        //       Memory leaks can manifest as incorrect results return from the GPU poll
        log.info("cleanup - memory");
        clReleaseMemObjects(oldPosMem, newPosMem, oldVelMem, newVelMem);
        try {
            log.info("cleanup - kernel");
            clReleaseKernel(kernel);
        } catch (Exception e) {
            // try to free the reset
        }
        try {
            log.info("cleanup - program");
            clReleaseProgram(program);
        } catch (Exception e) {
            // try to free the reset
        }
        try {
            log.info("cleanup - queue");
            clReleaseCommandQueue(queue);
        } catch (Exception e) {
            // try to free the reset
        }
        try {
            log.info("cleanup - context");
            clReleaseContext(context);
        } catch (Exception e) {
            // try to free the reset
        }
        // NOTE: This destroy is only required when everything is completed.
        log.info("cleanup - CL destroy");
        CL.destroy();
    }

    /**
     * Implementation of what this example needs to stop.
     * the running state controls a while loop to continue calculating and rendering.
     */
    public  void stop() {
        running = false;
    }

}
