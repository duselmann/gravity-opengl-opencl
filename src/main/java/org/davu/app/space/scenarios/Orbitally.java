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


public class Orbitally extends Particles {
	private static final Logger log = LogManager.getLogger(Orbitally.class);

	int NumParticles = 1_048_576; // 2x the particles of my original
	float[] angles;


	public Orbitally(Glasses3D glasses3D) {
		super(glasses3D);

		setParticleCount(NumParticles*4);
		setMassiveCount(1_024);
		setAlpha(0.05f);
	}

    @Override
	public float[] makeVertices() {
		log.info("init particle data");
    	float[] vertices = new float[3*getParticleCount()];
    	angles = new float[getParticleCount()];

        float maxRadius = 900;
        for(int b=0; b<getNumPartices(); b++) {
            float r,aa,x1,y1;
            r=aa=x1=y1=1;
            r = (float) Math.sqrt(maxRadius *maxRadius * Math.random());
            aa = (float)(Math.random()*Math.PI*2);
            x1 = (Math.cos(aa));
            y1 = (Math.sin(aa));

            vertices[b*3 + 0] = r*x1;
            vertices[b*3 + 1] = r*y1;
            vertices[b*3 + 2] = (float)(100 * Math.random());
        }
    	return vertices;
	}

    @Override
	public FloatBuffer  initVelocities() {
        massBase = 5f;
        velBase  = .08f;
        float maxRadius = 400;
        float density = massBase*getParticleCount()/(maxRadius*maxRadius*maxRadius);

    	FloatBuffer velBuffer   = BufferUtils.createFloatBuffer(4*getParticleCount());

    	for (int v=0; v<getParticleCount(); v++) {
            Vector3f pos = new Vector3f(vertices[v*3 + 0], vertices[v*3 + 1], vertices[v*3 + 2]);
    		float r  = pos.length();
            float innerMass = density*r*r*r;
            float vr = velBase * Math.sqrt(innerMass/r);

            Vector3f velv = new Vector3f(0,0,1).cross(pos).normalize().mul(vr);

            velBuffer.put(velv.x).put(velv.y).put(velv.z);
            velBuffer.put((float)(600*massBase*Math.random())); // mass
        }
    	velBuffer.flip();
    	this.vertices = null;
    	this.angles = null;
    	return velBuffer;
	}
}
