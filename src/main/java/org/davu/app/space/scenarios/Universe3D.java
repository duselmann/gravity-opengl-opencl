// Copyright (c) 2022 David Uselmann
package org.davu.app.space.scenarios;

import java.nio.FloatBuffer;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.davu.app.space.display.Particles;
import org.davu.app.space.display.VaoVboManager;
import org.joml.Math;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;


public class Universe3D extends Particles {
	private static final Logger log = LogManager.getLogger(Universe3D.class);

	int NumParticles = 4_096*8; // 2x the particles of my original


	public Universe3D() {
		setParticleCount(NumParticles*2);
		setMassiveCount(NumParticles);
		setAlpha(0.33f);
	}

    @Override
	public void makeVertices(VaoVboManager manager) {
		log.info("init particle data");
    	Vector3f pos = new Vector3f();
        float maxRadius = 25;
        Random rand = new Random();
        for(int b=0; b<getParticleCount(); b++) {
            boolean generate = true;
            while (generate) {
                pos = new Vector3f(
                		(float)rand.nextGaussian(),
                		(float)rand.nextGaussian(),
                		(float)rand.nextGaussian())
                        .normalize()
                        .mul((float)(maxRadius*Math.random()+25f));
                pos.z+=500;
                manager.addVertex(pos);
                generate = false; //checkNearPoint(16, b, vertices);
            }
        }
        velocities = initVelocities(manager);
	}

    @Override
	public FloatBuffer  initVelocities(VaoVboManager manager) {
        massBase = .1f;
        velBase  = 1f;
        double maxMagnitude = 0;
        double minMagnitude = 100;

    	FloatBuffer velBuffer   = BufferUtils.createFloatBuffer(4*getParticleCount());

    	for (int v=0; v<getParticleCount(); v++) {
            Vector3f pos = manager.getVertex(this, v);
            Vector3f normal = pos.normalize(new Vector3f());
            Vector3f vel = new Vector3f((velBase*normal.x),(velBase*normal.y),(velBase*normal.z));
            double mag   = vel.length();
            maxMagnitude = Math.max(mag, maxMagnitude);
            minMagnitude = Math.min(mag, minMagnitude);
            velBuffer.put(vel.x).put(vel.y).put(vel.z);
            velBuffer.put((float)(massBase*Math.random())); // mass
        }
    	velBuffer.flip();
    	return velBuffer;
	}
}
