// Copyright (c) 2022 David Uselmann
package org.davu.app.space.scenarios;

import java.nio.FloatBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.davu.app.space.display.Glasses3D;
import org.davu.app.space.display.VaoVboManager;
import org.joml.Math;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;


public class AndromedaClose extends Galaxies2 {
	private static final Logger log = LogManager.getLogger(AndromedaClose.class);


	public AndromedaClose() {
		log.info("Scenario Initialization");

		coreMassBase  = 5000f;
	    NumParticles = 1_048_576/16;
	    setParticleCount(NumParticles);
		setMassiveCount(NumParticles);

		setAlpha(.5f);
		massBase = 0.1f;

        Vector3f andromedaVel = new Vector3f(-3,0,0);
        Vector3f milkywayVel = new Vector3f(14f,0,0);
        coreVel = new Vector3f[] {milkywayVel, andromedaVel};

        float andromedaMass = coreMassBase*199f;
        float milkywayMass = coreMassBase*40f;
		coreMass = new float[] { milkywayMass,andromedaMass};

		maxRadius = new float[] {200, 440};
		ratio = 0.3f;
	}

    @Override
	public void makeVertices(VaoVboManager manager) {
		log.info("init particle data");
        velBase  = 1f;
    	Vector3f coreDist = new Vector3f(200,0,500);

        // set the core locations in the first two mass registers
    	Vector3f pos = new Vector3f();
        pos.x = -coreDist.x;
        pos.y = -coreDist.y;
        pos.z = -coreDist.z;
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


        for(int b=2; b<getParticleCount(); b++) {
            int leftRight = Math.random()<ratio ?0 :1;
            float r,aa,a1,a2;

            float galaxyRadius = maxRadius[leftRight];

            r = (galaxyRadius * (float)Math.random() + (50/(1+leftRight)));
            aa = (float)(Math.random()*Math.PI*2);
            a1 = (Math.cos(aa));
            a2 = (Math.sin(aa));
    		// get position of particle

            if (leftRight==1) {
            	pos.set(((float)Math.random()*10f-5f)*2f, // x
            			r*a2,   // y
            			r*a1);  // z
            } else {
            	pos.set(r*a1,  // x
            			r*a2,  // y
            			((float)Math.random()*10f-5f)*2f); // z
            }

            // normal to that positions galaxy
            Vector3f velNormal = coreNormal[leftRight];

        	// calculate the velocity with respect to the galaxy center
            r  = pos.length();           // distance from galaxy center
            float innerVol = r*r*r;                      // volume of dark matter
            float darkMass = dmMass * innerVol/dmVolume; // inner dark matter mass
            float vr = velBase * Math.sqrt(darkMass/pos.length()); // radial velocity magnitude
            Vector3f velDark = new Vector3f();
        	velNormal.cross(pos, velDark).normalize().mul(vr);     // radial velocity vector

        	// calculate the velocity with respect to the galaxy center
    		r  = pos.length();           // distance from galaxy center
            vr = velBase * Math.sqrt(coreMass[leftRight]/r); // galactic radial velocity magnitude
            Vector3f velGalactic = new Vector3f();
            pos.cross(velNormal, velGalactic).normalize().mul(vr);  // galactic radial velocity vector from black hole

            float leftRightRatio = leftRight==1? ratio: 1f-ratio;
            vr = Math.sqrt(leftRightRatio*getParticleCount()*massBase*r/galaxyRadius/galaxyRadius/2f); // galactic radial velocity from other stars   r r / rm rm
            Vector3f velStars = new Vector3f();
            pos.cross(velNormal, velStars).normalize().mul(vr);  // galactic radial velocity vector from stars

        	velGalactic.add(coreVel[leftRight]); // add in galactic velocity

        	Vector3f velv = velGalactic;
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

    @Override
	public void setGlasses(Glasses3D glasses) {
		super.setGlasses(glasses);
		glasses3D.setGlasses();
		glasses3D.setSeparation3D(40f);
	}

    @Override
	public void initDarkMater() {
        dmVolume = 8e9f; //2^30
        dmVolume = 44000f;
        dmMass   = coreMassBase*104*1000000000;
        dmCenter = new Vector3f();
	}
}
