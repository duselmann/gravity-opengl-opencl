// Copyright (c) 2022 David Uselmann
package org.davu.app.space.display;

import static org.davu.app.space.display.ColorsGL.*;
import static org.lwjgl.opengl.GL30.*;

import java.io.IOException;
import java.nio.FloatBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

/**
 * Displays a three axis guide to assist with spatial orientation.
 * While initially included as a debug tool, I keep it around
 * because I found it interesting for typical use.
 * @author davu
 *
 */
public class Compass implements VaoVboClient {
	private static final Logger log = LogManager.getLogger(Compass.class);

	// program and arguments
	private int mvp16Uniform;
	private int colorUniform;
	private int alphaUniform;

    private int vertexBuffer;

	private final FloatBuffer matrixBuffer;
	private final Matrix4f compass;

	private float alpha;

	private Glasses3D glasses3D;

	protected int offset;


	public Compass(Glasses3D glasses3D) {
		log.info("Creating particles");

		this.glasses3D = glasses3D;

		// help instances - reusable matrix and buffer
		matrixBuffer    = BufferUtils.createFloatBuffer(16);
		compass         = new Matrix4f();

		alpha = 1f;
	}

	@Override
	public void draw(Matrix4f mvp) {
		glEnable(GL_LINE_SMOOTH);

	    Matrix4f mvpMatrix = compass.set(mvp)
	        	.setTranslation(0, -500, -300).m33(700);

	    glasses3D.render(red(), mvpMatrix, (c,m)->{particleRender(0,c,m);});
	    glasses3D.render(green(), mvpMatrix, (c,m)->{particleRender(2,c,m);});
	    glasses3D.render(blue(), mvpMatrix, (c,m)->{particleRender(4,c,m);});
	}

	protected void particleRender(int idx, FloatBuffer colorBuffer, Matrix4f mvpMatrix) {
	    glUniformMatrix4fv(mvp16Uniform, false, mvpMatrix.get(matrixBuffer));
		glUniform4fv(colorUniform, colorBuffer);  // for 3D glasses need green render also
	    glUniform1f(alphaUniform, alpha);  // for 3D glasses need green render also
	    glDrawArrays(GL_LINE_STRIP, offset+idx, 2);
	}


	@Override
	public void cleanup() {
		log.info("dispose - particles");
	}

	public Compass init() throws IOException {
		log.info("init - particles");
		// vertexes, once passed to GL, are no longer needed in Java
        return this;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}
	public float getAlpha() {
		return alpha;
	}

	@Override
    public void makeVertices(VaoVboManager manager) {
		log.info("init vertices");

		int s = 50;

		manager.addVertex(new Vector3f(0,0,0));
		manager.addVertex(new Vector3f(s,0,0));

		manager.addVertex(new Vector3f(0,0,0));
		manager.addVertex(new Vector3f(0,s,0));

		manager.addVertex(new Vector3f(0,0,0));
		manager.addVertex(new Vector3f(0,0,s));

	}

    @Override
    public void setOffsetIndex(int offsetIndex) {
    	this.offset = offsetIndex;
    }

    @Override
	public void setVertexBuffer(int vertexBuffer) {
		this.vertexBuffer = vertexBuffer;
	}
	public int getVertexBuffer() {
		return vertexBuffer;
	}
    @Override
	public void setMvp16Uniform(int mvp16Uniform) {
		this.mvp16Uniform = mvp16Uniform;
	}
    @Override
	public void setColorUniform(int colorUniform) {
		this.colorUniform = colorUniform;
	}
    @Override
	public void setAlphaUniform(int alphaUniform) {
		this.alphaUniform = alphaUniform;
	}
	@Override
	public int getParticleCount() {
		return 6;
	}
}
