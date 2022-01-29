// Copyright (c) 2022 David Uselmann
package org.davu.app.space;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

/**
 * Shader color buffers for primary and secondary colors.
 *
 * @author davu
 */
public class ColorsGL {
	private static final FloatBuffer magBuffer;
	private static final FloatBuffer redBuffer;
	private static final FloatBuffer yloBuffer;
	private static final FloatBuffer grnBuffer;
	private static final FloatBuffer cynBuffer;
	private static final FloatBuffer elcBuffer;
	private static final FloatBuffer bluBuffer;
	private static final FloatBuffer purBuffer;

	static {
		magBuffer = make(1f,0f,1f);
		redBuffer = make(1f,0f,0f);
		yloBuffer = make(1f,1f,0f);
		grnBuffer = make(0f,1f,0f);
		cynBuffer = make(0f,1f,1f);
		elcBuffer = make(0f,0.68f,1f);
		bluBuffer = make(0f,0f,1f);
		purBuffer = make(0.33f,0f,1f);
	}

	protected static FloatBuffer make(float r, float g, float b) {
		FloatBuffer color = BufferUtils.createFloatBuffer(4);
		color.put(new float[]{r,g,b,1f}).flip();
		return color;

	}

	public static FloatBuffer magenta() {
		return magBuffer;
	}
	public static FloatBuffer red() {
		return redBuffer;
	}
	public static FloatBuffer yellow() {
		return yloBuffer;
	}
	public static FloatBuffer green() {
		return grnBuffer;
	}
	public static FloatBuffer cyan() {
		return cynBuffer;
	}
	public static FloatBuffer electricBlue() {
		return elcBuffer;
	}
	public static FloatBuffer blue() {
		return bluBuffer;
	}
	public static FloatBuffer purple() {
		return purBuffer;
	}
}
