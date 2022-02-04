// Copyright (c) 2022 David Uselmann
package org.davu.app.space.scenarios;

import java.nio.FloatBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.davu.app.space.Glasses3D;
import org.davu.app.space.Particles;
import org.davu.app.space.VaoVboManager;
import org.joml.Math;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;


public class Explosion3D extends Particles {
	private static final Logger log = LogManager.getLogger(Explosion3D.class);

	int NumParticles = 4_096*6; // 2x the particles of my original


	public Explosion3D(Glasses3D glasses3D) {
		super(glasses3D);

		setParticleCount(NumParticles);
		setMassiveCount(1_024);
		setAlpha(0.8f);
		glasses3D.setSeparation3D(40f);
	}

    @Override
	public void makeVertices(VaoVboManager manager) {
		log.info("init particle data");

        massBase = .1f;
        velBase  = 0f;
        float maxRadius = 2000;
        float maxRadiusSqrd = maxRadius*maxRadius*.2f;
        FloatBuffer velBuffer   = BufferUtils.createFloatBuffer(4*getParticleCount());

    	Vector3f pos = new Vector3f();
        for(int b=0; b<getParticleCount(); b++) {
        	do {
        		float x = (maxRadius * (float)Math.random()) -maxRadius/2;
        		float y = (maxRadius * (float)Math.random()) -maxRadius/2;
        		float z = (maxRadius * (float)Math.random()) -maxRadius/2;
        		pos.set(x,y,z);
        	} while( pos.lengthSquared() > maxRadiusSqrd);

        	manager.addVertex(pos);

            velBuffer.put((float)Math.random()*velBase-velBase/2)
            .put((float)Math.random()*velBase-velBase/2)
            .put((float)Math.random()*velBase-velBase/2);
            velBuffer.put(pos.length()/250); // mass
        }
        velBuffer.flip();
        velocities = velBuffer;
	}

    @Override
	public FloatBuffer  initVelocities(VaoVboManager manager) {
    	return velocities;
	}

}
