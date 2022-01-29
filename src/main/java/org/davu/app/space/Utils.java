// Copyright (c) 2022 David Uselmann
package org.davu.app.space;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

import org.davu.opencl.utils.CLUtils;
import org.lwjgl.BufferUtils;

public class Utils {

	public static <T> void quiteFree(String what, Runnable free) {
		quiteFree(what, free, false);
	}

	public static <T> void quiteFree(String what, Runnable free, boolean debug) {
		try {
			free.run();
			if (debug) {
				System.out.println(what +" freed");
			}
		} catch (Exception e) {
			System.err.println(what +" free error: "+ e.getMessage());
		}
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
     * Reads the specified resource and returns the raw data as a ByteBuffer.
     *
     * @param filePath   the resource to read
     *
     * @return the resource data
     *
     * @throws IOException if an IO error occurs
     */
    public static ByteBuffer ioResourceToByteBuffer(String filePath) throws IOException {
    	String source = loadResourceString(filePath);
    	byte[] src = source.getBytes();
    	ByteBuffer srcBuffer = BufferUtils.createByteBuffer(src.length);
    	srcBuffer.put(src);
    	srcBuffer.flip();
    	return srcBuffer;
    }
}
