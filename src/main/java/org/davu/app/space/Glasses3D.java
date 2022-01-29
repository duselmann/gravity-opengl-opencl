// Copyright (c) 2022 David Uselmann
package org.davu.app.space;

import static org.davu.app.space.ColorsGL.*;

import java.nio.FloatBuffer;
import java.util.function.BiConsumer;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Glasses3D {
	private final ViewMatrix camera;

    private Vector3f side = new Vector3f();
    private float separation3D;
	private boolean glasses;

	public Glasses3D(ViewMatrix viewCamera) {
		this.camera = viewCamera;
		separation3D = 8f;
	}

	public void render(FloatBuffer defaultColor, Matrix4f mvpMatrix,
			BiConsumer<FloatBuffer, Matrix4f> renderer) {
		FloatBuffer firstColor = defaultColor;
	    if (is3D()) {
			camera.right(side).mul(separation3D);
			mvpMatrix.translate(side);
			firstColor = red();
	    }

	    renderer.accept(firstColor, mvpMatrix);

	    if (is3D()) {
			mvpMatrix.translate(side.mul(-1.0f));
			renderer.accept(cyan(), mvpMatrix);
	    }
	}

	public boolean is3D() {
		return glasses && camera != null;
	}
	public void setGlasses() {
		setGlasses(!glasses);
	}
	public Glasses3D setGlasses(boolean glasses) {
//		log.debug("enable 3D glasses mode");
		this.glasses = glasses;
		return this;
	}
	public void setSeparation3D(float separation3d) {
		separation3D = separation3d;
	}
	public float getSeparation3D() {
		return separation3D;
	}
}
