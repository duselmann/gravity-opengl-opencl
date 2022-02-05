// Copyright (c) 2022 David Uselmann
package org.davu.app.space.display;

import org.joml.Matrix4f;

/**
 * Indicates that this implementation needs OpenGL
 * Vertex Arrays and Buffers to display.
 *
 * It will communicate its vertex needs and the manager
 * with return the necessary settings like offset and
 * Uniforms for the associated program.
 *
 * @author davu
 */
public interface VaoVboClient {
	public void cleanup();
	public void draw(Matrix4f mvp);
    public void makeVertices(VaoVboManager manager);
	public void setMvp16Uniform(int mvp16Uniform);
	public void setColorUniform(int colorUniform);
	public void setAlphaUniform(int alphaUniform);
	public void setVertexBuffer(int glVertexBuffer);
	public void setOffsetIndex(int offsetIndex);
	public int  getParticleCount();
}
