// Copyright (c) 2022 David Uselmann
package org.davu.opencl.simple;

import static org.davu.opencl.utils.CLInfo.*;
import static org.davu.opencl.utils.CLUtils.*;
import static org.davu.opencl.utils.CLUtils.Platforms.*;
import static org.lwjgl.opencl.CL10.*;

import java.io.IOException;
import java.nio.FloatBuffer;

import org.davu.opencl.utils.CLUtils;
import org.davu.opencl.utils.CLUtils.PlatformDevice;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.CLContextCallback;
import org.lwjgl.opencl.CLProgramCallback;


public class Testing2D2D {


    public static void main(String[] args) throws Exception {
        compute();
    }


    static void compute() throws IOException {
        // The Intel on-board
        PlatformDevice platformDevice = getGPU(INTEL);
        displayInfo(platformDevice, false);
        PointerBuffer device = platformDevice.device;
        long deviceId = device.get(0); // NOTE: get() advances the pointer, get(int) does not
        showMemory(deviceId);
        // TODO need to work out how to discover and properly utilize the desired GPU

//      PointerBuffer device = getMaxComputeUnitDevicePlatform(platformsDevices);
        // NOTE: Cannot reallocate a new buffer with the pointer values - it does not work.
        //       The original buffer must have more than just a pointer to the device.
        //       PointerBuffer buff = BufferUtils.createPointerBuffer(1).put(device.rewind().get()).rewind();
        //       long context = clCreateContext(null, buff, null, 0L, returnCode);

        // NOTE: This context callback is never called. It is unclear why. Other demos fail to call it too.
        CLContextCallback contextCallback = CLContextCallback.create(
                (errInfo, privateInfo, cb, userData) -> System.out.println("Creating Context"));
        // NOTE: Platform is optional here in the first parameter which is a properties PointerBuffer
        //       The device address pointer is already pointing to the GPU.
        long context = CLUtils.createContext(device, contextCallback);
        // NOTE: Must free the callback because it has a pointer injected into the GPU to trigger.
        contextCallback.free();
        // NOTE: Throughout this call chain, if any fails then then next could cause a JVM core dump.
        long queue  = createCommandQ(context, deviceId);

        // NOTE: In C the two dimensional array is all in your mind. It is really a 1D array with offsets.
        //       This demonstrates the 2D array visually while allocating a 1D actual array.
        FloatBuffer aBuffer = createFloatBuffer(
                -2f, -1f,  0f,  1f,  2f,  3f,
                 4f,  5f,  6f,  7f,  8f,  9f,
                10f, 11f, 12f, 13f, 14f, 15f,
                16f, 17f, 18f, 19f, 20f, 21f);
        FloatBuffer bBuffer = createFloatBuffer(
                21f, 20f, 19f, 18f, 17f, 16f,
                15f, 14f, 13f, 12f, 11f, 10f,
                 9f,  8f,  7f,  6f,  5f,  4f,
                 3f,  2f,  1f,  0f, -1f, -2f);
        // NOTE: Obtaining output from the GPU requires passing a write buffer of the appropriate size.
        //       The output buffer can be larger than required. The kernel code is C, is it critical
        //       to properly manage memory and not write values outside the buffer allocations.
        FloatBuffer rBuffer = BufferUtils.createFloatBuffer(24);

        // NOTE: Memory Buffers must be assigned to the context. Options are read, write, and read/write.
        //       It is recommended to use the minimum requirement. The memory copy host pointer was required.
        //       Fail code CL_INVALID_HOST_PTR, -37, is returned during enqueue without the pointer flag.
        long aMem = writeBuffer(queue, context, CL_MEM_READ_ONLY  | CL_MEM_COPY_HOST_PTR, aBuffer);
        long bMem = writeBuffer(queue, context, CL_MEM_READ_ONLY  | CL_MEM_COPY_HOST_PTR, bBuffer);
        long rMem = createMemoryBuffer(context, CL_MEM_WRITE_ONLY | CL_MEM_COPY_HOST_PTR, rBuffer);
        // NOTE: Enqueue of the read of the results occurs after the kernal program runs.
        // NOTE: C source files can be loaded or hard coded strings.
        //       The extension is arbitrary. If your IDE accepts *.c or *.cpp then that is fine also.
        String source = loadResourceString("cl/sum2D2D.txt");

        // NOTE: This just waits for the write buffers to finish
        waitForQueue(queue);

        // NOTE: Load the program string into the GPU context
        // This is combined with the build utility call.
        // long program = pushProgram(context, source);

        // NOTE: This callback was called regularly, unlike the context callback.
        CLProgramCallback buildCallback = CLProgramCallback.create(
                (programPtr, userData) -> System.out.println("Building Program Kernel\n"));
        // NOTE: Initiate a compile. The third param empty string is for compiler options.
        //       The second parameter, null here, can be the devices PointerBuffer.
        //       If there is more than one GPU/CPU reference, then it is unclear what would happen
        //       If the devices buffer is included don't forget to rewind() it because of the get
        //       incurred for the clCreateCommandQueue
        long program = buildProgram(deviceId, context, source, buildCallback);
        // NOTE: Must free the callback because it has a pointer injected into the GPU to trigger.
        buildCallback.free();

        // NOTE: The kernel name parameter must match the a function in the OpenCL source.
        // Test1D calls the params on its own, while this one is an example of a combined call.
        long kernel = createKernel(program, "sum", aMem, bMem, rMem);

        // NOTE: Send a signal to execute the kernel with return buffer size
        //       One dimensional array.
        int globalCols = 6;
        int globalRows = 4;
        int localCols = 2;
        int localRows = 2;
        pushKernelToQueue(queue, kernel, globalCols, globalRows, localCols, localRows);

        // NOTE: If successful then read the results memory, rMem, back into the results buffer, rBuffer.
        popQueueResultBuffer(queue, rMem, rBuffer);

        // NOTE: This waits for the buffer transfer to finish
        waitForQueue(queue);

        // NOTE: If the transfer worked then rBuffer will contain the answer.
        StringBuilder sb = new StringBuilder();
        for (aBuffer.rewind(); aBuffer.hasRemaining();) {
            float val = aBuffer.get();
            if (val>=0 && val<10) {
                sb.append(" ");
            }
            sb.append(val).append(", ");
            if (aBuffer.remaining() % globalCols == 0) {
                sb.append("\n");
            }
        }
        sb.append("  + \n");
        for (bBuffer.rewind(); bBuffer.hasRemaining();) {
            float val = bBuffer.get();
            if (val>=0 && val<10) {
                sb.append(" ");
            }
            sb.append(val).append(", ");
            if (bBuffer.remaining() % globalCols == 0) {
                sb.append("\n");
            }
        }
        sb.append("  = \n");
        for (rBuffer.rewind(); rBuffer.hasRemaining();) {
            float val = rBuffer.get();
            sb.append(val).append(", ");
            if (rBuffer.remaining() % globalCols == 0) {
                sb.append("\n");
            }
        }
        System.out.println(sb);

        // NOTE: OpenCL is in the C domain and resource cleanup is required to prevent memory leaks.
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseMemObject(aMem);
        clReleaseMemObject(bMem);
        clReleaseMemObject(rMem);
        clReleaseCommandQueue(queue);
        clReleaseContext(context);

        // NOTE: This destroy is only required when everything is completed.
        CL.destroy();
     }

}
