// Copyright (c) 2022 David Uselmann
package org.davu.app.space.compute;

import static org.davu.app.space.Utils.*;
import static org.davu.opencl.utils.CLInfo.*;
import static org.davu.opencl.utils.CLUtils.*;
import static org.davu.opencl.utils.CLUtils.Platforms.*;
import static org.lwjgl.glfw.GLFWNativeWGL.*;
import static org.lwjgl.opencl.CL10.*;
import static org.lwjgl.opencl.KHRGLSharing.*;
import static org.lwjgl.opengl.WGL.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.io.IOException;
import java.nio.IntBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.davu.opencl.utils.CLUtils;
import org.davu.opencl.utils.CLUtils.PlatformDevice;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.CLContextCallback;
import org.lwjgl.opencl.CLProgramCallback;

public class OpenCL implements AutoCloseable {
	private static final Logger log = LogManager.getLogger(OpenCL.class);

	private long gpuThreads;

    // CL program handles
	private  long platformId;
	private  long deviceId;
	private  long context;
	private  long queue;
	private  long program;
    private  long kernel;

    // CL kernel function name
    private  final String kernelPath;
    private  final String kernelFunction;


    public OpenCL(String clPath, String kernel) {
    	kernelPath = clPath;
    	kernelFunction = kernel;
    	gpuThreads = 256;
    }


    public void init(long glWindow) throws IOException {
        try {
        	// TODO push to NVIDIA but GL does not have the same mechanism.
            log.info("CL GPU Device Acquisition: {}", NVIDIA);
        	// Currently assigned to INTEL because GL pushes there
            PlatformDevice platformDevice = getGPU(NVIDIA);
            displayInfo(platformDevice, false);
            platformId = platformDevice.platform;
            PointerBuffer device = platformDevice.device;
            deviceId = device.get();
            device.rewind();
            try {
                // Expect max threads to be 256 (Intel) or 1024 (Nvidia)
                gpuThreads = getDeviceInfoNumber(deviceId, CL_DEVICE_MAX_WORK_GROUP_SIZE, SIZE_OF_LONG);
            } catch (Exception e) {
            	gpuThreads = 256;
            	log.error("Attempting to use default thread count");
            }
            log.info("CL GPU Thread Count: {}", gpuThreads);

            // NOTE: Cannot reallocate a new buffer with the pointer values - it does not work.
            //       The original buffer must have more than just a pointer to the device.
            //       PointerBuffer buff = BufferUtils.createPointerBuffer(1).put(device.rewind().get()).rewind();
            //       long context = clCreateContext(null, buff, null, 0L, returnCode);

            log.info("CL GPU Context acquisition");
            // NOTE: This context callback is never called. It is unclear why. Other demos fail to call it too.
            CLContextCallback contextCallback = CLContextCallback.create(
                    (errInfo, privateInfo, cb, userData) -> System.out.println("Creating Context"));
            // NOTE: Platform is optional here in the first parameter which is a properties PointerBuffer
            //       The device address pointer is already pointing to the GPU.

            PointerBuffer ctxProps = null;
            if (glWindow > 0) {
	            ctxProps = BufferUtils.createPointerBuffer(7);
	            ctxProps
	                .put(CL_GL_CONTEXT_KHR)
	                .put(glfwGetWGLContext(glWindow))
	                .put(CL_WGL_HDC_KHR)
	                .put(wglGetCurrentDC())
	                .put(CL_CONTEXT_PLATFORM)
	                .put(platformId)
	                .put(NULL)
	                .flip();
            }
            context = CLUtils.createContext(ctxProps, device, contextCallback);

            // NOTE: Must free the callback because it has a pointer injected into the GPU to trigger.
            contextCallback.free();
            // NOTE: Throughout this call chain, if any fails then then next could cause a JVM core dump.
            log.info("CL GPU Queue acquisition");
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
            log.info("CL Program - load");
            String source = org.davu.app.space.Utils.loadResourceString(kernelPath); // third version

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

            log.info("CL Kernel acquisition");
            IntBuffer returnCode = BufferUtils.createIntBuffer(1);
            kernel = clCreateKernel(program, kernelFunction, returnCode);
            isSuccess(returnCode);

        } catch (Exception e) {
        	log.error("failed to init CL", e);
        	cleanup();
        	throw e;
        }
    }

    /**
     * Clean up all OpenCL allocations: CL_MEM, Queue, Context, Kernels, and Programs.
     */
    public void cleanup() {
        log.info("cleanup - GPU CL");
        quiteFree("cleanup - wait for queue", ()->waitForQueue(queue));

        // NOTE: OpenCL is in the C domain and resource cleanup is required to prevent memory leaks.
        //       Memory leaks can manifest as incorrect results return from the GPU poll
        //       Release in the opposite order of allocation
        quiteFree("cleanup - kernel", ()->clReleaseKernel(kernel));
        quiteFree("cleanup - program", ()->clReleaseProgram(program));
        quiteFree("cleanup - queue", ()->clReleaseCommandQueue(queue));
        quiteFree("cleanup - context", ()->clReleaseContext(context));
        // NOTE: CL destroy is only required when everything is completed.
        quiteFree("cleanup - CL destroy", ()->CL.destroy());
    }

    @Override
    public void close() throws Exception {
    	cleanup();
    }

    public long getGpuThreads() {
		return gpuThreads;
	}
    public long getKernel() {
		return kernel;
	}
    public long getQueue() {
    	return queue;
    }
    public long getContext() {
		return context;
	}

    public String getExtensions() {
    	return getPlatformInfoString(platformId, CL_PLATFORM_EXTENSIONS);
    }

}
