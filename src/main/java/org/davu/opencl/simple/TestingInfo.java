// Copyright (c) 2022 David Uselmann
package org.davu.opencl.simple;

import static org.davu.opencl.utils.CLInfo.*;
import static org.davu.opencl.utils.CLUtils.*;
import static org.davu.opencl.utils.CLUtils.Platforms.*;
import static org.lwjgl.opencl.CL10.*;


import java.io.IOException;

import org.davu.opencl.utils.CLUtils.PlatformDevice;
import org.lwjgl.opencl.CL;


public class TestingInfo {


    public static void main(String[] args) throws Exception {
        compute();
    }


    static void compute() throws IOException {
        // The Intel on-board
        // NOTE: CL actually allows discovery of the CPU as well but these demos focus on the GPU
        PlatformDevice platformDevice = getGPU(NVIDIA);
        displayInfo(platformDevice, true);

        // NOTE: get() advances the pointer, get(int) does not
        long deviceId = platformDevice.device.get(0);
        showMemory(deviceId);

        // NOTE: Of all the CLInfo utility functions, many are very useful to determine
        //       the OpenCL device capabilities. The CL version and extensions for features
        //       and memory for local workspace size.
        String version = getPlatformInfoString(platformDevice.platform, CL_PLATFORM_VERSION);
        System.out.println("version: " + version);
        String extensions = getPlatformInfoString(platformDevice.platform, CL_PLATFORM_EXTENSIONS);
        System.out.println("extensions: " + extensions);
        long bytes = getDeviceInfoNumber(deviceId, CL_DEVICE_LOCAL_MEM_SIZE, 8);
        System.out.println("memory size in bytes: " + bytes);

        // NOTE: This destroy is only required when everything is completed.
        CL.destroy();
     }


}
