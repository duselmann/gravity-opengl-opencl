// Copyright (c) 2022 David Uselmann
package org.davu.app.space.scenarios;

import java.nio.FloatBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.davu.app.space.Glasses3D;
import org.davu.app.space.Particles;
import org.joml.Math;
import org.lwjgl.BufferUtils;


public class SprialRibbon extends Particles {
	private static final Logger log = LogManager.getLogger(SprialRibbon.class);

	int NumParticles = 4_096*6; // 2x the particles of my original

	private FloatBuffer velBuffer;


	public SprialRibbon(Glasses3D glasses3D) {
		super(glasses3D);

		setParticleCount(NumParticles);
		setMassiveCount(NumParticles/6);
		setAlpha(0.8f);
		glasses3D.setGlasses();
		glasses3D.setSeparation3D(40);
	}

    @Override
	public float[] makeVertices() {
		log.info("init particle data");

        massBase = 10f;
        velBase  = 0f;
        float maxRadius = 600;
    	float[] vertices = new float[3*getParticleCount()];
    	velBuffer   = BufferUtils.createFloatBuffer(4*getParticleCount());

        for(int b=0; b<getNumPartices(); b++) {
            float r = Math.sqrt(maxRadius *maxRadius * (float)Math.random()) + 100;

            float aa = (float)(Math.random()*Math.PI*2);
            vertices[b*3 + 0] = r*(Math.cos(aa));
            vertices[b*3 + 1] = r*(Math.sin(aa));
            vertices[b*3 + 2] = r*aa;

            velBuffer.put((float)Math.random()*velBase-velBase/2)
            .put((float)Math.random()*velBase-velBase/2)
            .put((float)Math.random()*velBase-velBase/2);
            velBuffer.put(r/2500); // mass
        }
    	return vertices;
	}

    @Override
	public FloatBuffer  initVelocities() {
    	velBuffer.flip();
    	this.vertices = null;
    	return velBuffer;
	}
}
