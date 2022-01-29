// Copyright (c) 2022 David Uselmann
package org.davu.app.space.scenarios;

import java.nio.FloatBuffer;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.davu.app.space.Glasses3D;
import org.davu.app.space.Particles;
import org.joml.Math;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;


public class Universe3D extends Particles {
	private static final Logger log = LogManager.getLogger(Universe3D.class);

	int NumParticles = 4_096*8; // 2x the particles of my original


	public Universe3D(Glasses3D glasses3D) {
		super(glasses3D);

		setParticleCount(NumParticles*2);
		setMassiveCount(NumParticles);
		setAlpha(0.33f);
	}

    @Override
	public float[] makeVertices() {
		log.info("init particle data");
    	float[] vertices = new float[3*getParticleCount()];

        float maxRadius = 25;
        Random rand = new Random();
        for(int b=0; b<getNumPartices(); b++) {
            boolean generate = true;
            Vector3f pos = new Vector3f();
            while (generate) {
                pos = new Vector3f(
                		(float)rand.nextGaussian(),
                		(float)rand.nextGaussian(),
                		(float)rand.nextGaussian())
                        .normalize()
                        .mul((float)(maxRadius*Math.random()+25f));
                vertices[b*3 + 0] = (int)pos.x;
                vertices[b*3 + 1] = (int)pos.y;
                vertices[b*3 + 2] = (int)pos.z+500;
                generate = false; //checkNearPoint(16, b, vertices);
            }
        }
    	return vertices;
	}

    @Override
	public FloatBuffer  initVelocities() {
        massBase = .1f;
        velBase  = 1f;
        double maxMagnitude = 0;
        double minMagnitude = 100;

    	FloatBuffer velBuffer   = BufferUtils.createFloatBuffer(4*getParticleCount());

    	for (int v=0; v<getParticleCount(); v++) {
            Vector3f pos = new Vector3f(vertices[v*3 + 0], vertices[v*3 + 1], vertices[v*3 + 2]);
            Vector3f normal = pos.normalize(new Vector3f());
            Vector3f vel = new Vector3f((velBase*normal.x),(velBase*normal.y),(velBase*normal.z));
            double mag   = vel.length();
            maxMagnitude = Math.max(mag, maxMagnitude);
            minMagnitude = Math.min(mag, minMagnitude);
            velBuffer.put(vel.x).put(vel.y).put(vel.z);
            velBuffer.put((float)(massBase*Math.random())); // mass
        }
    	System.out.println(maxMagnitude);
    	System.out.println(minMagnitude);
    	velBuffer.flip();
    	this.vertices = null;
    	return velBuffer;
	}
}
