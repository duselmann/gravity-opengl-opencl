// Copyright (c) 2022 David Uselmann
package org.davu.opencl;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.davu.app.space.OpenCL;
import org.junit.jupiter.api.Test;

/**
 * Purpose: To compile OpenCL code before injecting
 * it into the runtime. I had some bugs and optimizations
 * that where convenient to test here.
 *
 * It can catch variable declaration missing, typoes, and
 * other syntax errors.
 *
 *
 * @author davu
 */
public class ComipleOpenCL {

	@Test
	void compile() {
		String filePath = "cl/space-gravity.txt";
		String function = "gravityDarkMatter";

		try (OpenCL openCl = new OpenCL(filePath, function)) {
			openCl.init(0);
		} catch (IOException e) {
			fail("Could not find CL file: "+ filePath);
		} catch (Exception e) {
			fail("CL Compile Failed: "+ filePath);
			e.printStackTrace();
		}
		assertTrue(true,"Must have compiled");
	}

}
