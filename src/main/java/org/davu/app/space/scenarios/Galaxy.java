// Copyright (c) 2022 David Uselmann
package org.davu.app.space.scenarios;

import java.nio.FloatBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.davu.app.space.display.VaoVboManager;
import org.joml.Math;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;


public class Galaxy extends Galaxies {
	private static final Logger log = LogManager.getLogger(Galaxy.class);

	private final float MAX_RADIUS = 400;
	private final float CORE_MASS_BASE = 5e4f;
	protected float bulgeRatio = 0.15f;

	public Galaxy() {
		log.info("Scenario Initialization");

		coreMassBase  = CORE_MASS_BASE;
        coreMass = new float[] {CORE_MASS_BASE, 1f};
    	coreDist = new Vector3f(0,0,0);
	    NumParticles = 1_048_576/16;
		setParticleCount(NumParticles);
		setMassiveCount(NumParticles);
		setAlpha(.5f);
		ratio = 1f; // all in one galaxy
        Vector3f coreVel1 = new Vector3f(0,0,0);
        Vector3f coreVel2 = new Vector3f(0,0,0);
        coreVel = new Vector3f[] {coreVel1,coreVel2};
        maxRadius = new float[] {MAX_RADIUS, MAX_RADIUS};
        massBase = 1.5f;
	}
    @Override
	public void initDarkMater() {
        // TODO always have DM but default low influence
        // init properties - Dark Matter
		dmVolume = (float) java.lang.Math.pow(MAX_RADIUS * 1.5, 3);
        dmMass   = CORE_MASS_BASE * 20f;
        dmCenter = new Vector3f();
	}

    @Override
	public void makeVertices(VaoVboManager manager) {
		log.info("init particle data");
        velBase  = 1f;
        // set the core locations in the first two mass registers
        Vector3f pos = new Vector3f();
        manager.addVertex(pos);
        manager.addVertex(coreDist);

        FloatBuffer velBuffer = BufferUtils.createFloatBuffer(4*getParticleCount());
        Vector3f[] coreNormal = new Vector3f[] {new Vector3f(0,0,1), new Vector3f(1,0,0)};

        Vector3f coreVel1 = coreVel[0];
        Vector3f coreVel2 = coreVel[1];
        velBuffer.put(coreVel1.x).put(coreVel1.y).put(coreVel1.z);
        velBuffer.put(coreMass[0]); // mass
        velBuffer.put(coreVel2.x).put(coreVel2.y).put(coreVel2.z);
        velBuffer.put(coreMass[1]); // mass

        float starMass = getParticleCount()*massBase*2.1f; // estimated internal mass from dust/stars

        for(int b=2; b<getParticleCount(); b++) {
            int leftRight = Math.random()<ratio ?0 :1;
            float r,aa,a1,a2;
            float galaxyArea = maxRadius[leftRight]*maxRadius[leftRight]*4;
            aa = (float)(Math.random()*Math.PI*2); // disk angle around
            a1 = Math.cos(aa);                   // y-ish angle part
            a2 = Math.sin(aa);                   // x-ish  angle part

            if (Math.random()< bulgeRatio) { // core bulge
            	r = 45f * (float)Math.random() + 15; // bulge radius
                float ba = (float)(Math.random()*Math.PI*2); // bulge angle around
//                      x = r * sin(polar) * cos(alpha)
//                		y = r * sin(polar) * sin(alpha)
//                		z = r * cos(polar)
            	pos.set( r * a2 * Math.cos(ba),
            			 r * a2 * Math.sin(ba),
            			 r * a1);
            } else {
            	r = randomDistanceFromCore(leftRight);
        		// get position of particle

                // for one galaxy place in y-z plane, the other in x-y plane
                if (leftRight==1) { // y-z
                	pos.set(((float)Math.random()*10f-5f)*2f,  // x, disk thickness
                			r*a2,   // y
                			r*a1);  // z
                } else { // x-y
                	pos.set(r*a1,  // x
                			r*a2,  // y
                			((float)Math.random()*10f-5f)*2f); // z, disk thickness
                }
            }

            // normal to that positions galaxy
            Vector3f velNormal = coreNormal[leftRight];

            // calculate the velocity with respect to the dark matter
            float vr = velBase; // * Math.sqrt(darkMass/pos.length()); // radial velocity magnitude

        	// calculate the velocity with respect to the galaxy center
            r  = pos.length();           // distance from galaxy center
            float innerVol = r*r*r;                      // volume of dark matter
            float darkMass = dmMass * innerVol/dmVolume; // inner dark matter mass
            float innerArea = r*r;                      // volume of dark matter
            float starsMass = starMass * innerArea/galaxyArea; // inner dark matter mass
            float totalMass = darkMass + starsMass + coreMass[leftRight];
            vr = velBase * Math.sqrt(totalMass/r); // galactic radial velocity magnitude
            Vector3f velv = new Vector3f();
            pos.cross(velNormal, velv).normalize().mul(vr);  // galactic radial velocity vector from stars

        	velv.add(coreVel[leftRight]); // add in galactic velocity

            velBuffer.put(velv.x).put(velv.y).put(velv.z);  // register particle velocity
            velBuffer.put((float)(massBase*Math.random())); // mass of each particle

            leftRight = leftRight * 2 - 1;
            pos.x = leftRight*coreDist.x + pos.x;
            pos.y = leftRight*coreDist.y + pos.y;
            pos.z = leftRight*coreDist.z + pos.z;
            manager.addVertex(pos);
        }

        velBuffer.flip();
        velocities = velBuffer;
    }


}
