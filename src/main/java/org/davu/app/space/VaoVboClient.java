package org.davu.app.space;

import org.joml.Matrix4f;

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
