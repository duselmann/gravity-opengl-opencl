// Copyright (c) 2022 David Uselmann
package org.davu.app.space.scenarios;

import java.nio.FloatBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.davu.app.space.Glasses3D;
import org.davu.app.space.Particles;
import org.joml.Math;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;


public class Explosion3D extends Particles {
	private static final Logger log = LogManager.getLogger(Explosion3D.class);

	int NumParticles = 4_096*6; // 2x the particles of my original

	private FloatBuffer velBuffer;


	public Explosion3D(Glasses3D glasses3D) {
		super(glasses3D);

		setParticleCount(NumParticles);
		setMassiveCount(1_024);
		setAlpha(0.8f);
		glasses3D.setSeparation3D(40f);
	}

    @Override
	public float[] makeVertices() {
		log.info("init particle data");

        massBase = .1f;
        velBase  = 0f;
        float maxRadius = 2000;
        float maxRadiusSqrd = maxRadius*maxRadius*.2f;
    	float[] vertices = new float[3*getParticleCount()];
    	velBuffer   = BufferUtils.createFloatBuffer(4*getParticleCount());

    	Vector3f pos = new Vector3f();
        for(int b=0; b<getNumPartices(); b++) {
        	do {
        		float x = (maxRadius * (float)Math.random()) -maxRadius/2;
        		float y = (maxRadius * (float)Math.random()) -maxRadius/2;
        		float z = (maxRadius * (float)Math.random()) -maxRadius/2;
        		pos.set(x,y,z);
        	} while( pos.lengthSquared() > maxRadiusSqrd);

            vertices[b*3 + 0] = pos.x;
            vertices[b*3 + 1] = pos.y;
            vertices[b*3 + 2] = pos.z;

            velBuffer.put((float)Math.random()*velBase-velBase/2)
            .put((float)Math.random()*velBase-velBase/2)
            .put((float)Math.random()*velBase-velBase/2);
            velBuffer.put(pos.length()/250); // mass
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
