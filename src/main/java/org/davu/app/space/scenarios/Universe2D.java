// Copyright (c) 2022 David Uselmann
package org.davu.app.space.scenarios;

import java.nio.FloatBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.davu.app.space.display.Glasses3D;
import org.davu.app.space.display.Particles;
import org.davu.app.space.display.VaoVboManager;
import org.joml.Math;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;


public class Universe2D extends Particles {
	private static final Logger log = LogManager.getLogger(Universe2D.class);

	int NumParticles = 4_096*8; // 2x the particles of my original

	private FloatBuffer velBuffer;


	public Universe2D(Glasses3D glasses3D) {
		super(glasses3D);

		setParticleCount(NumParticles);
		setMassiveCount(1_024);
		setAlpha(0.8f);
	}

    @Override
	public void makeVertices(VaoVboManager manager) {
		log.info("init particle data");

        massBase = .1f;
        velBase  = 0f;
        float maxRadius = 600;
    	velBuffer   = BufferUtils.createFloatBuffer(4*getParticleCount());
    	Vector3f pos = new Vector3f();

        for(int b=0; b<getParticleCount(); b++) {
            float r = (maxRadius * (float)Math.random()) + 100;
            float aa = (float)(Math.random()*Math.PI*2);
            pos.x = r*(Math.cos(aa));
            pos.y = r*(Math.sin(aa));
            pos.z = 0;
            manager.addVertex(pos);

            velBuffer.put((float)Math.random()*velBase-velBase/2)
            .put((float)Math.random()*velBase-velBase/2)
            .put((float)Math.random()*velBase-velBase/2);
            velBuffer.put(r/2500); // mass
        }
    	velBuffer.flip();
    	velocities = velBuffer;
	}

    @Override
	public FloatBuffer  initVelocities(VaoVboManager manager) {
    	return velocities;
	}
}
