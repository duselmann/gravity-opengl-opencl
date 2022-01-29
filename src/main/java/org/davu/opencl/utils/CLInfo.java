// Copyright (c) 2022 David Uselmann
package org.davu.opencl.utils;

import static org.davu.opencl.utils.CLUtils.*;
import static org.lwjgl.opencl.CL10.*;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.davu.opencl.utils.CLUtils.PlatformDevice;
import org.lwjgl.PointerBuffer;


public class CLInfo {


    public enum DeviceName {
        UNKOWN, DEFAULT, CPU, GPU, ACCELERATOR;
    }
    public static String deviceTypeString(int i) {
        int log2 = (int)(Math.log10(CL_DEVICE_TYPE_ACCELERATOR) / Math.log10(2));
        DeviceName[] deviceNames = DeviceName.values();
        String name = "UNKNOWN";
        if (log2 >= deviceNames.length) {
            return name;
        }
        return deviceNames[log2].name();
    }

    public enum MemoryUnits {
        B, KB, MB, GB, TB;
    }
    public static String formatMemory(long size) {
        if (size <= 0) {
            return "0";
        }
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        MemoryUnits[] memoryUnits = MemoryUnits.values();
        String units = "UNKNOWN";
        if (digitGroups < memoryUnits.length) {
            units = MemoryUnits.values()[digitGroups].toString();
        }
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units;
    }

    public static String makeNumberString(final ByteBuffer buffer) {
        StringBuilder b = new StringBuilder();

        for ( int i = buffer.position(); i < buffer.limit(); i++ ) {
            b.append(buffer.get(i)).append(' ');
        }

        return b.toString();
    }





    static void printExtensions(String paramName, String extensions) {
        System.out.println("\t" + paramName + ": \n\t\t" + extensions.replaceAll(" ", "\n\t\t"));
    }
    public static void printPlatformExt(long platform) {
        printExtensions("CL_PLATFORM_EXTENSIONS", getPlatformInfoString(platform, CL_PLATFORM_EXTENSIONS));
    }
    public static void printDeviceExt(long platform) {
        printExtensions("CL_DEVICE_EXTENSIONS  ", getDeviceInfoString(platform, CL_DEVICE_EXTENSIONS));
    }

    public static String getPlatformName(long platform) {
        return getPlatformInfoString(platform, CL_PLATFORM_NAME);
    }
    public static String getDeviceName(long device) {
        return getDeviceInfoString(device, CL_DEVICE_NAME);
    }
    public static String getDeviceType(long device) {
        return deviceTypeString(getDeviceInfoNumber(device, CL_DEVICE_TYPE, SIZE_OF_LONG));
    }
    public static int getComputeUnits(long device) {
        return getDeviceInfoNumber(device, CL_DEVICE_MAX_COMPUTE_UNITS, SIZE_OF_INT);
    }
    public static int getMemoryLocal(long device) {
        return getDeviceInfoNumber(device, CL_DEVICE_LOCAL_MEM_SIZE, SIZE_OF_LONG);
    }
    public static int getMemoryGlobal(long device) {
        return getDeviceInfoNumber(device, CL_DEVICE_GLOBAL_MEM_SIZE, SIZE_OF_LONG);
    }


    public static List<String> platformName(PointerBuffer platforms) {
        System.out.println("GPU platform count: " + platforms.limit());
        List<String> names = new ArrayList<>();

        while (platforms.hasRemaining()) {
            System.out.println();
            long platform = platforms.get();

            String name = getPlatformName(platform);
            names.add(name);
            System.out.println("GPU platform name : " + name);
            printPlatformExt(platform);
            // NOTE: Creating a new pointer buffer with values from another does not work.
            //       The address of the value in the heap is also critical which is not preserved.
            //       response.put(BufferUtils.createPointerBuffer(2).put(platform).rewind(), devices);
        }
        platforms.rewind();

        return names;
    }


    public static void displayInfo(PlatformDevice platformDevice, boolean includeExtensions) {
        @SuppressWarnings("unused")
        int result = 0;

        long platform = platformDevice.platform;
        if (platform == 0) {
            return;
        }
        System.out.println();
        System.out.println("GPU platform name             : " + getPlatformName(platform));
        String version = getPlatformInfoString(platformDevice.platform, CL_PLATFORM_VERSION);
        System.out.println("Version (platform)            : " + version);

        if (includeExtensions) {
            printPlatformExt(platform);
        }

       PointerBuffer devices = platformDevice.device;
       while (devices.hasRemaining()) {
            long device = devices.get();

            System.out.println("GPU Device name               : " + getDeviceName(device));
            System.out.println("CL Device Type                : " + getDeviceType(device));
            version = getDeviceInfoString(device, CL_DRIVER_VERSION);
            System.out.println("Version (driver)              : " + version);

            System.out.println("GPU Compute Units             : " + getComputeUnits(device));
            System.out.println("MAX_WORK_ITEM_DIMENSIONS      : "
                    + getDeviceInfoNumber(device, CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS, SIZE_OF_INT));
            // NOTE: If the architecture is 64 bit then a pointer is 8 bytes.
            System.out.println("CL_DEVICE_MAX_WORK_GROUP_SIZE : "
                    + getDeviceInfoNumber(device, CL_DEVICE_MAX_WORK_GROUP_SIZE, SIZE_OF_LONG));
            System.out.println("CL_DEVICE_MAX_CLOCK_FREQUENCY : "
                    + getDeviceInfoNumber(device, CL_DEVICE_MAX_CLOCK_FREQUENCY, SIZE_OF_INT));
            System.out.println("CL_DEVICE_ADDRESS_BITS        : "
                    + getDeviceInfoNumber(device, CL_DEVICE_ADDRESS_BITS, SIZE_OF_INT));
            System.out.println("CL_DEVICE_AVAILABLE           : "
                    + (getDeviceInfoNumber(device, CL_DEVICE_AVAILABLE, SIZE_OF_INT)==0 ?"no" :"yes"));
            System.out.println("CL_DEVICE_COMPILER_AVAILABLE  : "
                    + (getDeviceInfoNumber(device, CL_DEVICE_COMPILER_AVAILABLE, SIZE_OF_INT)==0 ?"no" :"yes"));

            if (includeExtensions) {
                printDeviceExt(device);
            }
        }
        devices.rewind();
        System.out.println();
    }


    public static void showMemory(long device) {
        long bytes = getMemoryLocal(device);
        System.out.println("Device Local mem  : " + formatMemory(bytes));

        bytes = getMemoryGlobal(device);
        System.out.println("Device Global mem : " + formatMemory(Math.abs(bytes)));
    }

    public static String platformVersion(long platform) {
        String version = getPlatformInfoString(platform, CL_PLATFORM_VERSION);
        System.out.println("Platform version : " + version);
        return version;
    }


    /**
     * Convenience method to display/debug the values in a created buffer of any of BufferType types.
     * @param sb the string builder to place the values.
     * @param buffer data buffer to write as string
     * @return self for chaining
     */
    public static <T extends Buffer> StringBuilder append(StringBuilder sb, T buffer) {
        BufferWrapper<T> wrapper = new BufferWrapper<>(buffer);
        sb.append("\n");
        while (wrapper.hasRemaining()) {
            sb.append(wrapper.get()).append(" ");
        }
        return sb;
    }

    public static void displayBufferContent(String message, int showCount, Buffer ... buffers) {
        // displayBufferContent("show these buffers", -1, buffer1, buffer2);
        StringBuilder sb = new StringBuilder();
        sb.append('\n').append(message).append('\n');
        for (Buffer buffer : buffers) {
            buffer.rewind();
            BufferWrapper<?> wrapper = new BufferWrapper<>(buffer);
            int count = showCount;
            if (showCount<=0) {
                count=buffer.remaining();
            }
            for (int i=0; i<count; i++) {
                sb.append(wrapper.get()).append(", ");
            }
            sb.append('\n');
            buffer.rewind();
        }
        sb.append('\n');
        System.out.println(sb);
    }

// TODO use CLCodes?
//     CLEventCallback eventCallBack = CLEventCallback.create((event, eventStatus, userData) -> {
//         System.out.println("Event callback status : " + getEventStatusName(event_command_exec_status));
//     });
//     resultCode = clSetEventCallback(e, CL_COMPLETE, eventCallBack, NULL);
}
