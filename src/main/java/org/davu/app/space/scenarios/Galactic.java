// Copyright (c) 2022 David Uselmann
package org.davu.app.space.scenarios;

import java.nio.FloatBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.davu.app.space.display.Particles;
import org.davu.app.space.display.VaoVboManager;
import org.joml.Math;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;


public class Galactic extends Particles {
	private static final Logger log = LogManager.getLogger(Galactic.class);

	int NumParticles = 1_048_576; // 2x the particles of my original


	public Galactic() {
		setParticleCount(NumParticles);
		setMassiveCount(1_024);
		setAlpha(0.2f);
	}

    @Override
	public void makeVertices(VaoVboManager manager) {
		log.info("init particle data");


        float maxRadius = 900;
        Vector3f pos = new Vector3f();
        for(int b=0; b<getParticleCount(); b++) {
            float r,aa,x1,y1;
            r=aa=x1=y1=1;
            r = Math.sqrt(maxRadius *maxRadius * (float)Math.random() + 100);
            aa = (float)(Math.random()*Math.PI*2);
            x1 = (Math.cos(aa));
            y1 = (Math.sin(aa));

            pos.x = r*x1;
            pos.y = r*y1;
            pos.z  = (float)(100 * Math.random());
            manager.addVertex(pos);
            if (b < 6) System.out.println(pos);
        }
        velocities = initVelocities(manager);
	}

    @Override
	public FloatBuffer  initVelocities(VaoVboManager manager) {
        massBase = 5f;
        velBase  = 0.02f;
        float maxRadius = 400;
        float density = massBase*getParticleCount()/(maxRadius*maxRadius*maxRadius);
    	FloatBuffer velBuffer   = BufferUtils.createFloatBuffer(4*getParticleCount());


    	for (int v=0; v<getParticleCount(); v++) {
    		Vector3f pos = manager.getVertex(this, v);
            if (v < 6) System.out.println(pos);
    		float r  = pos.length();
            float innerMass = density*r*r*r;
            float vr = velBase * Math.sqrt(innerMass/r);

            Vector3f velv = new Vector3f(0,0,1).cross(pos).normalize().mul(vr);

            velBuffer.put(velv.x).put(velv.y).put(velv.z);
            velBuffer.put((float)(massBase*Math.random())); // mass
        }
    	velBuffer.flip();
    	return velBuffer;
	}

}
