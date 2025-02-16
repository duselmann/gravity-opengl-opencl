// Copyright (c) 2022 David Uselmann
package org.davu.opencl.utils;

import static org.lwjgl.opencl.CL10.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.davu.opencl.utils.BufferWrapper.BufferType;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CLContextCallbackI;
import org.lwjgl.opencl.CLProgramCallbackI;

/**
 * Some utilities for OpenCL
 */
public class CLUtils {
    private static final Logger logger = LogManager.getLogger(CLUtils.class);
    static {
    	logger.info("CL Utils loaded");
    }


    public static int SIZE_OF_BYTE = 1;
    public static int SIZE_OF_INT  = 4;
    public static int SIZE_OF_LONG = 8;

    public enum Option {
        DENORMS_ARE_ZERO,
        FAST_RELAXED_MATH,
        FINITE_MATH_ONLY,
        MAD_ENABLE,
        NO_SIGNED_ZEROS,
        OPT_DISABLE,
        SINGLE_PRECISION_CONSTANT,
        STRICT_ALIASING,
        UNSAFE_MATH_OPTIMIZATIONS,
        WARNINGS_ARE_ERRORS("-Werror"),
        WARNINGS_DISABLED("-w")
        ;

        String param;
        Option() {}
        Option(String string) {
            param=string;
        }

        @Override
        public String toString() {
            if (param != null && !param.isEmpty()) {
                return param;
            }
            return "-cl-"+super.toString().toLowerCase().replaceAll("_","-");
        }
        public static String toString(Option[] options) {
            if (options == null || options.length==0) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            for (Option option : options) {
                sb.append(option.toString()).append(" ");
            }
            return sb.toString();
        }
    }



    public enum Platforms {
        AMD, INTEL, NVIDIA;
    }

    /**
     * A structure to hold the devices on a platform.
     * @author davu
     */
    public static class PlatformDevice {
        public final long platform;
        public final PointerBuffer device;
        public PlatformDevice(long platform, PointerBuffer device) {
            this.platform = platform;
            this.device = device;
        }
    }


    /**
     * Convenient fetch platform count
     * @return number of platforms available
     */
    public static int getPlatformCount() {
        IntBuffer platformsCount = BufferUtils.createIntBuffer(1);
        int result = clGetPlatformIDs(null, platformsCount);
        isSuccess(result);
        int platformCount = platformsCount.get();
        return platformCount;
    }

    /**
     * Convenient fetch all platforms.
     * @return platform IDs pointer buffer up to the number on the computer.
     */
    public static PointerBuffer getPlatforms() {
        PointerBuffer platforms = BufferUtils.createPointerBuffer( getPlatformCount() );
        int result = clGetPlatformIDs(platforms, (IntBuffer)null);
        isSuccess(result);
        return platforms;
    }

    /**
     * Convenient fetch platforms given a count less than or equal to
     * the total platforms on a given computer.
     * NOTE: If the count is greater than then number of platforms, it will return the max on the computer.
     *       The platform IDs are required to access device IDs.
     * @return platform IDs pointer buffer on the computer.
     */
    public static PointerBuffer getPlatforms(int platformCount) {
        PointerBuffer platforms = BufferUtils.createPointerBuffer(platformCount);
        int result = clGetPlatformIDs(platforms, (IntBuffer)null);
        isSuccess(result);
        return platforms;
    }


    /**
     * Given a platform this discovers the GPU count and then the GPUs IDs
     * NOTE: Device IDs are CPUs and GPUs on a computer.
     * @param platform
     * @return a pointer buffer containing the ID for GPUs on a device.
     */
    public static PointerBuffer getDevices(long platform) {
        // NOTE: When fetching many values the first call is the count of the item.
        //       The same method is used to fetch the actual item with the a different null param.
        IntBuffer deviceCount = BufferUtils.createIntBuffer(1);
        int result = clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, null, deviceCount);
        isSuccess(result);
        int count = deviceCount.get();
        // TODO this needs to move to CLInfo
        //System.out.println("GPU Device Count  : " + count);

        // NOTE: The count call above is used to allocate enough pointers.
        //       The devices buffer is populated by the CL call.
        PointerBuffer devices = BufferUtils.createPointerBuffer(count);
        result = clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, devices, (IntBuffer)null);
        isSuccess(result);

        return devices;
    }

    /**
     * If the order of platform and GPU discovery on a particular computer is known then
     * this can be used to fetch that platform and device by index number.
     * For example is NVIDA is returned first and Intel is returned second then use
     * platform number 0 for NVIDIA and 1 for Intel. Furthermore, if each platform
     * has more than one GPU, then the device number will return the desired device
     * as platform works. On the test computer, Intel was the second and each had only 1 GPU.
     *
     * @param platformNumber index of the desired platform - for a device discovery
     * @param deviceNumber   index of the desired GPU device
     * @return
     */
    public static PlatformDevice getDevice(int platformNumber, int deviceNumber) {  // NOTE: 0 based lists
        // NOTE: When fetching many values the first call is the count of the item.
        //       The same method is used to fetch the actual item with the a different null param.
        IntBuffer platformCount = BufferUtils.createIntBuffer(1);
        int result = clGetPlatformIDs(null, platformCount);
        isSuccess(result);

        // NOTE: The count call above is used to allocate enough pointers.
        PointerBuffer platforms = BufferUtils.createPointerBuffer(platformCount.get());
        result = clGetPlatformIDs(platforms, (IntBuffer)null);
        isSuccess(result);
        long platform = platforms.get(platformNumber); // Here is how the numbered platform is selected.


        // NOTE: When fetching many values the first call is the count of the item.
        //       The same method is used to fetch the actual item with the a different null param.
        IntBuffer deviceCount = BufferUtils.createIntBuffer(1);
        result = clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, (PointerBuffer)null, deviceCount);
        isSuccess(result);

        // NOTE: The count call above is used to allocate enough pointers.
        //       The devices buffer is populated by the CL call.
        // The dev computer has two platforms with one device each. currently the deviceNumber is not used.
        PointerBuffer device = BufferUtils.createPointerBuffer(deviceCount.get());
        result = clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, device, (IntBuffer)null);
        isSuccess(result);

        return new PlatformDevice(platform, device);
    }
    public static PlatformDevice getGPU(Platforms platform) {
        return getGPU(platform.toString());
    }
    public static PlatformDevice getGPU(String platformNameLike) {
        // NOTE: When fetching many values the first call is the count of the item.
        //       The same method is used to fetch the actual item with the a different null param.
        IntBuffer platformCount = BufferUtils.createIntBuffer(1);
        int result = clGetPlatformIDs(null, platformCount);
        isSuccess(result);

        // NOTE: The count call above is used to allocate enough pointers.
        PointerBuffer platforms = BufferUtils.createPointerBuffer(platformCount.get());
        result = clGetPlatformIDs(platforms, (IntBuffer)null);
        isSuccess(result);

        long platform = -1;
        while (platforms.hasRemaining()) {
            platform = platforms.get();
            String name = getPlatformInfoString(platform, CL_PLATFORM_NAME);
            if (name.contains(platformNameLike)) {
                break;
            }
            name = getPlatformInfoString(platform, CL_PLATFORM_VENDOR);
            if (name.contains(platformNameLike)) {
                break;
            }

        }
        isSuccess(platform>0 ?0 :CL_DEVICE_NOT_FOUND);

        // NOTE: When fetching many values the first call is the count of the item.
        //       The same method is used to fetch the actual item with the a different null param.
        IntBuffer deviceCount = BufferUtils.createIntBuffer(1);
        result = clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, (PointerBuffer)null, deviceCount);
        isSuccess(result);

        // NOTE: The count call above is used to allocate enough pointers.
        //       The devices buffer is populated by the CL call.
        // The dev computer has two platforms with one device each. currently the deviceNumber is not used.
        PointerBuffer device = BufferUtils.createPointerBuffer(deviceCount.get());
        result = clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, device, (IntBuffer)null);
        isSuccess(result);

        return new PlatformDevice(platform, device);
    }


    /**
     * This manages the IntBuffer form of success code.
     *
     * NOTE:
     * There are two forms of return codes from the CL methods.
     * For CL methods that return an important value instead of using the buffers,
     * it will have a final param that accepts a single value int buffer. The
     * method will return the result code in that buffer.
     *
     * @param result
     */
    public static void isSuccess(IntBuffer result) {
        isSuccess(result.get());
        // NOTE: rewinding the buffer allows it to be reused for the next call.
        result.rewind();
    }

    /**
     * This manages the int return form of success code.
     *
     * NOTE:
     * There are two forms of return codes from the CL methods.
     * For CL methods that return an important value instead of using the buffers,
     * it will have a final param that accepts a single value int buffer. The
     * method will return the result code in that buffer.
     *
     * @param result
     */
    public static void isSuccess(int result) {
        // NOTE: if not success then inform with an exception than can be caught if handled.
        boolean isSuccess = (result == CL_SUCCESS);
        if (!isSuccess) {
            System.out.println();
            System.out.println("success     = " + isSuccess);
            System.out.println("fail code   = " + result);
            System.out.println("fail string = " + CLCodes.getString(result));

            throw new RuntimeException(CLCodes.getString(result));
        }
    }


    /**
     * Constructs a context for the given device.
     * @param device   the device to run programs
     * @return the context address on the device
     */
    public static long createContext(PointerBuffer device) {
        return createContext(device, null);
    }
    /**
     * Constructs a context for the given device with a possible callback.
     * NOTE: The clCreateContext takes a pointer buffer retrieved in the device
     *       retrieval. Looking forward to the command queue, it takes a specific
     *       device address.
     * @param device   the device to run programs
     * @param callback the callback method -- TODO need to look into when this is expected to be called
     * @return the context address on the device
     */
    public static long createContext(PointerBuffer device, CLContextCallbackI callback) {
    	return createContext(null, device, callback);
    }
    public static long createContext(PointerBuffer properties, PointerBuffer device, CLContextCallbackI callback) {
        IntBuffer returnCode = BufferUtils.createIntBuffer(1);
        long context = clCreateContext(properties, device, callback, 0L, returnCode);
        isSuccess(returnCode);
        return context;
    }

    /**
     * Constructs a command queue for a given context on a device.
     * NOTE: The clCreateContext takes a pointer buffer retrieved in the device
     *       retrieval. Creating a command queue, it takes a specific device address.
     * @param context the context address created prior
     * @param device  one of the devices' address where the program will run
     * @return the address of the created command queue
     */
    public static long createCommandQ(long context, long device) {
        return createCommandQ(context, device, false);
    }
    public static long createCommandQ(long context, long device, boolean profile) {
        IntBuffer returnCode = BufferUtils.createIntBuffer(1);
        long queue  = clCreateCommandQueue(context, device, CL_QUEUE_PROFILING_ENABLE, returnCode);
        isSuccess(returnCode);
        return queue;
    }

    /**
     * Creates a data buffer on the context for floating point values
     * NOTE: I think that the buffer is used only for sizing the allocation.
     *       See writeFloatBuffer for data transfer.
     * @param context the context to create the data allocation
     * @param flags   CL flags to control the state of the buffer
     * @param buffer the values to place in the context allocation
     * @return a memory address for the new buffer
     */
    public static long createMemoryBuffer(long context, int flags, Buffer buffer) {
        IntBuffer returnCode = BufferUtils.createIntBuffer(1);
        try {
            switch(BufferType.to(buffer)) {
            case BYTE:
                return clCreateBuffer(context, flags, (ByteBuffer)buffer, returnCode);
            case CHAR:
                throw new RuntimeException("CharBuffer is unsupported");
            case DOUBLE:
                return clCreateBuffer(context, flags, (DoubleBuffer)buffer, returnCode);
            case FLOAT:
                return clCreateBuffer(context, flags, (FloatBuffer)buffer, returnCode);
            case INTEGER:
                return clCreateBuffer(context, flags, (IntBuffer)buffer, returnCode);
            case LONG:
                throw new RuntimeException("LongBuffer is unsupported");
            case SHORT:
                return clCreateBuffer(context, flags, (ShortBuffer)buffer, returnCode);
            default:
                return -1;
            }
        } finally {
            if (returnCode.position() != 0) {
                isSuccess(returnCode);
            }
        }
    }
    /**
     * Not only creates a data buffer on the context for floating point values,
     * this enqueues it on the given queue.
     * NOTE: Queues are where actions are place to be executed. The values written
     *       to the allocated memory address are scheduled but not written by this call.
     * @param queue   the queue to place the memory buffer for writing
     * @param context the context to create the data allocation
     * @param flags   CL flags to control the state of the buffer
     * @param buffer the values to place in the context allocation
     * @return a memory address for the new buffer
     */
    public static long writeBuffer(long queue, long context, int flags, Buffer buffer) {
        long mem = createMemoryBuffer(context, flags, buffer);
        int returnCode = CL_SUCCESS; // presume success for finally block not consume other exceptions
        try {
            switch(BufferType.to(buffer)) {
            case BYTE:
                returnCode = clEnqueueWriteBuffer(queue, mem, true, 0, (ByteBuffer)buffer, null, null);
                break;
            case CHAR:
                throw new RuntimeException("CharBuffer is unsupported");
            case DOUBLE:
                returnCode = clEnqueueWriteBuffer(queue, mem, true, 0, (DoubleBuffer)buffer, null, null);
                break;
            case FLOAT:
                returnCode = clEnqueueWriteBuffer(queue, mem, true, 0, (FloatBuffer)buffer, null, null);
                break;
            case INTEGER:
                returnCode = clEnqueueWriteBuffer(queue, mem, true, 0, (IntBuffer)buffer, null, null);
                break;
            case LONG:
                throw new RuntimeException("LongBuffer is unsupported");
            case SHORT:
                returnCode = clEnqueueWriteBuffer(queue, mem, true, 0, (ShortBuffer)buffer, null, null);
                break;
            default:
                return -1;
            }
        } finally {
            isSuccess(returnCode);
        }
        return mem;
    }

    /**
     * Waits for the device queue to finish before proceeding to the next action.
     * Allows the Java thread to join the device thread.
     * NOTE: It can be useful to wait for buffer writes to complete prior to queuing a kernel.
     * @param queue the queue to wait for completion of scheduled actions.
     */
    public static void waitForQueue(long queue) {
        int resultCode = clFinish(queue);
        isSuccess(resultCode);
    }


    /**
     * Writes a program source code to the device for compilation.
     * @param context the context address
     * @param source  the source code string
     * @return the program memory address
     */
    static long pushProgram(long context, String source) {
        IntBuffer returnCode = BufferUtils.createIntBuffer(1);
        long program = clCreateProgramWithSource(context, source, returnCode);
        isSuccess(returnCode);
        return program;
    }

    /**
     * Push program source and builds it in the given context memory address.
     * @param context context address to place the program
     * @param source  the source code string
     * @return program memory address
     */
    public static long buildProgram(long device, long context, String source) {
        return buildProgram(device, context, source, (CLProgramCallbackI)null);
    }
    /**
     * Push program source and builds it in the given context memory address.
     * @param context context address to place the program
     * @param source  the source code string
     * @param callback a callback function called when/after code is built
     * @return program memory address
     */
    public static long buildProgram(long deviceId, long context, String source,
            CLProgramCallbackI callback, Option ... options) {

        long program = pushProgram(context, source);

        int resultCode = clBuildProgram(program, null, Option.toString(options), callback, 0L);

        if (resultCode == CL_BUILD_PROGRAM_FAILURE) {
            PointerBuffer len = BufferUtils.createPointerBuffer(1);
            resultCode = clGetProgramBuildInfo(program, deviceId, CL_PROGRAM_BUILD_LOG, (ByteBuffer)null, len);
            int logLen = (int)len.get();
            ByteBuffer buildLogBuf = BufferUtils.createByteBuffer(logLen);
            resultCode = clGetProgramBuildInfo(program, deviceId, CL_PROGRAM_BUILD_LOG, buildLogBuf, null);
            buildLogBuf.rewind();
            byte[] stringBytes = new byte[logLen];
            buildLogBuf.get(stringBytes);
            System.out.println(new String(stringBytes));
        }
        isSuccess(resultCode);
        return program;
    }

    /**
     * Creates a program kernel with optional function arguments.
     * NOTE: the function name specified must match a function name in the submitted source.
     * @param program address of the built program
     * @param functionName name of the function this kernel will call
     * @param params the parameter the function requires
     * @return created kernel address
     */
    public static long createKernel(long program, String functionName, Long ... params) {
        IntBuffer returnCode = BufferUtils.createIntBuffer(1);
        long kernel = clCreateKernel(program, functionName, returnCode);
        isSuccess(returnCode);

        for (int i=0; i<params.length; i++) {
            pushKernelStackPointer(kernel, i, params[i]);
        }

        // NOTE: The enqueue writes only placed the memory in the GPU, this sets them as params for the method.
        //pushKernelStackPointer(kernel, 0, aMem);
        //pushKernelStackPointer(kernel, 1, bMem);
        //pushKernelStackPointer(kernel, 2, rMem);
        // NOTE: This is the largest difference between 2.x API. The kernel was an object to call setArgs.
        //       kernel.setArg(0, aMem);
        //       kernel.setArg(1, bMem);
        //       kernel.setArg(2, rMem);
        //       From 2.x to 3.x set args has many named method names decorated with an indication of the buffer type.
        //       This is because all the methods signatures take the long pointer reference. To distinguish one
        //       from another, the methods must have different names. This code is passing 1 pointer; hence "1p"

        return kernel;
    }

    /**
     * Pushes parameters for the function that the kernel has been built to run.
     * The parameter must match of the index it is submitted. It is more logical
     * to use the variable argument parameter call of createKernel than to call
     * for each argument separately.
     * @param kernel the kernel address
     * @param paramIndex zero base index of the parameter to load
     * @param clMemRef the memory address of the parameter data created by writeBuffer or createBuffer
     */
    public static void pushKernelStackPointer(long kernel, int paramIndex, long clMemRef) {
        int resultCode = clSetKernelArg1p(kernel, paramIndex, clMemRef);
        isSuccess(resultCode);
    }

    /**
     * Queues a single dimensional kernel to run on the GPU.
     * Yes, it takes until now to finally execute the kernel on the device.
     * @param queue  the queue address to load the created kernel
     * @param kernel the kernel address to enqueue
     * @param capacity the number of elements in the enqueued data
     */
    public static void pushKernelToQueue(long queue, long kernel, int capacity) {
        PointerBuffer kernel1DGlobalWorkSize = BufferUtils.createPointerBuffer(1);
        kernel1DGlobalWorkSize.put(0, capacity);
        int resultCode = clEnqueueNDRangeKernel(queue, kernel, 1, null, kernel1DGlobalWorkSize, null, null, null);
        isSuccess(resultCode);
    }
    /**
     * Queues a two dimensional kernel to run on the GPU.
     * Yes, it takes until now to finally execute the kernel on the device.
     * @param queue  the queue address to load the created kernel
     * @param kernel the kernel address to enqueue
     * @param cols the number of elements per column in the enqueued data
     * @param rows the number of rows in the enqueued data
     */
    public static void pushKernelToQueue(long queue, long kernel, int cols, int rows) {
        PointerBuffer kernel2DGlobalWorkSize = BufferUtils.createPointerBuffer(2);
        kernel2DGlobalWorkSize.put(0, cols);
        kernel2DGlobalWorkSize.put(1, rows);
        int resultCode = clEnqueueNDRangeKernel(queue, kernel, 2, null, kernel2DGlobalWorkSize, null, null, null);
        isSuccess(resultCode);
    }
    public static void pushKernelToQueue(long queue, long kernel, int globalCols, int globalRows, int localCols, int localRows) {
        PointerBuffer kernel2DGlobalWorkSize = BufferUtils.createPointerBuffer(2);
        kernel2DGlobalWorkSize.put(0, globalCols);
        kernel2DGlobalWorkSize.put(1, globalRows);
        kernel2DGlobalWorkSize.rewind();
        PointerBuffer kernel2DlocalWorkSize = BufferUtils.createPointerBuffer(2);
        kernel2DlocalWorkSize.put(0, localCols);
        kernel2DlocalWorkSize.put(1, localRows);
        kernel2DlocalWorkSize.rewind();
        int resultCode = clEnqueueNDRangeKernel(queue, kernel, 2, null, kernel2DGlobalWorkSize, kernel2DlocalWorkSize, null, null);
        isSuccess(resultCode);
    }
    public static PointerBuffer pushKernelToQueueProfile(long queue, long kernel, int globalCols, int globalRows, int localCols, int localRows) {
        PointerBuffer kernel2DGlobalWorkSize = BufferUtils.createPointerBuffer(2);
        kernel2DGlobalWorkSize.put(0, globalCols);
        kernel2DGlobalWorkSize.put(1, globalRows);
        kernel2DGlobalWorkSize.rewind();
        PointerBuffer kernel2DlocalWorkSize = BufferUtils.createPointerBuffer(2);
        kernel2DlocalWorkSize.put(0, localCols);
        kernel2DlocalWorkSize.put(1, localRows);
        kernel2DlocalWorkSize.rewind();
        PointerBuffer profiling = BufferUtils.createPointerBuffer(1);
        int resultCode = clEnqueueNDRangeKernel(queue, kernel, 2, null, kernel2DGlobalWorkSize, kernel2DlocalWorkSize, null, profiling);
        isSuccess(resultCode);
        return profiling;
    }

    /**
     * Fetches the results of the program run on the device.
     * @param queue the queue address the kernel was executed
     * @param returnMem the memory address the kernel was instructed to write results.
     * @param returnBuff a buffer to place the results
     */
    public static void popQueueResultBuffer(long queue, long returnMem, Buffer returnBuff) {
        int returnCode = CL_SUCCESS; // presume success for finally block not consume other exceptions
        try {
            switch(BufferType.to(returnBuff)) {
            case BYTE:
                returnCode = clEnqueueReadBuffer(queue, returnMem, true, 0, (ByteBuffer)returnBuff, null, null);
                break;
            case CHAR:
                throw new RuntimeException("CharBuffer is unsupported");
            case DOUBLE:
                returnCode = clEnqueueReadBuffer(queue, returnMem, true, 0, (DoubleBuffer)returnBuff, null, null);
                break;
            case FLOAT:
                returnCode = clEnqueueReadBuffer(queue, returnMem, true, 0, (FloatBuffer)returnBuff, null, null);
                break;
            case INTEGER:
                returnCode = clEnqueueReadBuffer(queue, returnMem, true, 0, (IntBuffer)returnBuff, null, null);
                break;
            case LONG:
                throw new RuntimeException("LongBuffer is unsupported");
            case SHORT:
                returnCode = clEnqueueReadBuffer(queue, returnMem, true, 0, (ShortBuffer)returnBuff, null, null);
                break;
            default:
                returnCode = -1;
            }
        } finally {
            isSuccess(returnCode);
        }
    }


    /**
     * Convenience method to construct a buffer from the provided values.
     * @param values variable arguments values to place in the buffer
     * @return the created buffer containing the variable argument values.
     */
    public static FloatBuffer createFloatBuffer(float ... values) {
        // NOTE: Arrays cannot be passed in directly. They must be placed in a memory buffer.
        //       If the buffer rewind() must be called after fill to place the pointer at the first.
        //       Otherwise a fail code CL_INVALID_BUFFER_SIZE,-61, is returned because it is at the end.
        //       A buffer can be advanced and rewound any number of times. To get the next/current entry,
        //       use get(), to get a specific entry, use get(index). To find out how many left, use remaining()
        //       and limit() will return the buffer capacity count - it is only the byte count for a ByteBuffer.
        //       Here float buffers are created and the BufferUtils knows to allocate sizeOf(float) * size bytes.
        FloatBuffer fBuffer = BufferUtils.createFloatBuffer(values.length);
        fBuffer.put(values).rewind();
        return fBuffer;
    }

    /**
     * Load a program source code into a String. It will actually load any text file
     * on the class path into a string but it is here specifically to load CL programs.
     * @param filePath the relative path to the resource on the class path
     * @return text contained in the given file resource
     * @throws IOException thrown when the file is not found or trouble loading it.
     */
    public static String loadResourceString(String filePath) throws IOException {
        try (InputStream is = CLUtils.class.getClassLoader().getResourceAsStream(filePath)) {
            if (is == null) {
                throw new IOException("File not found: " + filePath);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * This is here as something I tried to use to probe how OpenCl works. There is more
     * to memory address and device IDs than simply existing in a PointerBuffer.
     * Do not use this method, it seems there is more to the copy. See MemoryUtils?
     * @param pointers pointer values from another PointerBuffer
     * @return a new PointerBuffer instance containing the given values.
     */
    @Deprecated
    static PointerBuffer toPointerBuffer(long[] pointers) {
        PointerBuffer buf = BufferUtils.createPointerBuffer(pointers.length).put(pointers);
        buf.rewind();
        return buf;
    }





    interface TriFuntionInt<A,B,C> {
        int apply(A a, B b, C c);
    }

    private static int getInfoBufferSize(long pointer, int paramId, TriFuntionInt<Long, Integer, PointerBuffer> getInfoSize) {
        // NOTE: Like fetching platform count, fetching the extension string size first.
        PointerBuffer size = BufferUtils.createPointerBuffer(1);
        int result = getInfoSize.apply(pointer, paramId, size);
        isSuccess(result);
        return (int)size.get(0);
    }
    private static String getInfoString(long pointer, int paramId, int size, TriFuntionInt<Long, Integer, ByteBuffer> getInfoString) {
        // NOTE: Then allocate a byte buffer to hold the extension string and fetch
        ByteBuffer buffer = BufferUtils.createByteBuffer(size);
        int result = getInfoString.apply(pointer, paramId, buffer);
        isSuccess(result);
        // NOTE: The LWJGL library has many helpful tools; ByteBuffer to UTF8 String conversion.
        return makeString(buffer);
    }
    public static String makeString(ByteBuffer buffer) {
        int length = buffer.remaining();
        StringBuilder buff = new StringBuilder(length);

        for ( int i = buffer.position(); i < buffer.limit(); i++ ) {
            buff.append((char)buffer.get(i));
        }

        return buff.toString();
    }
    private static int getInfoNumber(long pointer, int paramId, int size, TriFuntionInt<Long, Integer, ByteBuffer> getInfoString) {
        // NOTE: Then allocate a byte buffer to hold the extension string and fetch
        ByteBuffer buffer = BufferUtils.createByteBuffer(size);
        int result = getInfoString.apply(pointer, paramId, buffer);
        isSuccess(result);
        // NOTE: The LWJGL library has many helpful tools; ByteBuffer to UTF8 String conversion.
        return buffer.getInt();
    }


    public static String getPlatformInfoString(long pointer, int paramId) {
        // NOTE: lookup the length of the info string in bytes
        int length = getInfoBufferSize(pointer, paramId,
                (ptr, pId, bytes)->clGetPlatformInfo(pointer, paramId, (ByteBuffer)null, bytes));
        // NOTE: fetch the actual info string in bytes
        return getInfoString(pointer, paramId, length,
                (ptr, pId, buff)->clGetPlatformInfo(pointer, paramId, buff, (PointerBuffer)null));
    }
    public static int getPlatformInfoNumber(long pointer, int paramId, int length) {
        // NOTE: fetch the actual info string in bytes
        return getInfoNumber(pointer, paramId, length,
                (ptr, pId, buff)->clGetPlatformInfo(pointer, paramId, buff, (PointerBuffer)null));
    }

    public static String getDeviceInfoString(long device, int paramId) {
        // NOTE: lookup the length of the info string in bytes
        int length = getInfoBufferSize(device, paramId,
                (ptr, pId, bytes)->clGetDeviceInfo(device, paramId, (ByteBuffer)null, bytes));
        // NOTE: fetch the actual info string in bytes
        return getInfoString(device, paramId, length,
                (ptr, pId, buff)->clGetDeviceInfo(device, paramId, buff, (PointerBuffer)null));
    }
    public static int getDeviceInfoNumber(long device, int paramId, int length) {
        // NOTE: fetch the actual info string in bytes
        return getInfoNumber(device, paramId, length,
                (ptr, pId, buff)->clGetDeviceInfo(device, paramId, buff, (PointerBuffer)null));
    }

    /**
     * use getDeviceInfoNumber instead
     */
    @Deprecated
    static int getDeviceInfoInt(long cl_device_id, int param_name) {
        IntBuffer ib = BufferUtils.createIntBuffer(1);
        clGetDeviceInfo(cl_device_id, param_name, ib, null);
        return ib.get(0);
    }
    /**
     * use getDeviceInfoNumber instead
     */
    @Deprecated
    static long getDeviceInfoLong(long cl_device_id, int param_name) {
        LongBuffer lb = BufferUtils.createLongBuffer(1);
        clGetDeviceInfo(cl_device_id, param_name, lb, null);
        return lb.get(0);
    }
    /**
     * use getDeviceInfoNumber instead
     */
    @Deprecated
    static long getDeviceInfoPointer(long cl_device_id, int param_name) {
        PointerBuffer pb = BufferUtils.createPointerBuffer(1);
        clGetDeviceInfo(cl_device_id, param_name, pb, null);
        return pb.get(0);
    }

    /**
     * Release a collection of CL_MEM objects from the device (GPU)
     * @param objects var args or an array of CL_MEM long values to be released
     */
    public static void clReleaseMemObjects(long ... objects) {
        for(long object : objects) {
            try {
                clReleaseMemObject(object);
            } catch (Exception e) {
                // try to free them all anyway
            }
        }
    }

}
