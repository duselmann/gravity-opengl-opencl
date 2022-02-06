// Copyright (c) 2022 David Uselmann
package org.davu.opencl.simple;

import static org.davu.opencl.utils.CLInfo.*;
import static org.davu.opencl.utils.CLUtils.*;
import static org.lwjgl.opencl.CL10.*;


import java.io.IOException;

import org.davu.opencl.utils.CLUtils.PlatformDevice;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL;

/**
 * No computation in this example,
 * it fetches data from from all GPUs
 * found on the machine.
 *
 * @author davu
 */
public class TestingInfo {


    public static void main(String[] args) throws Exception {
        compute();
    }


    static void compute() throws IOException {
    	int numPlatforms = getPlatformCount();
    	System.out.println("Found " + numPlatforms + " platforms.\n");
    	PointerBuffer platformPtrs = getPlatforms(numPlatforms);

    	for (int platformIndex=0; platformIndex<numPlatforms; platformIndex++) {
    		System.out.println();
    		System.out.println("----------------------------------------------------------------");
    		long platformPtr = platformPtrs.get();
    		String platformName = getPlatformName(platformPtr);
    		System.out.println("Platform " + platformIndex + " is " + platformName);

    		String platformVendor = getPlatformInfoString(platformPtr, CL_PLATFORM_VENDOR);
    		System.out.println("Platform " + platformIndex + " vendor is " + platformVendor);

    		displayPlatformInfo(platformVendor);
    	}


        // NOTE: This destroy is only required when everything is completed.
        CL.destroy();
     }

    static void displayPlatformInfo(String platformVendor) {
        // The Intel on-board
        // NOTE: CL actually allows discovery of the CPU as well but these demos focus on the GPU
        PlatformDevice platformDevice = getGPU(platformVendor);
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
    }

}
