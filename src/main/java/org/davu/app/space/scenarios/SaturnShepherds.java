// Copyright (c) 2022 David Uselmann
package org.davu.app.space.scenarios;

import java.nio.FloatBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.davu.app.space.display.VaoVboManager;
import org.joml.Math;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;


public class SaturnShepherds extends Galaxies {
	private static final Logger log = LogManager.getLogger(SaturnShepherds.class);

	// 740 to 1370

	protected final float MAX_RADIUS = 1370;     // Rings extent (maybe 1800, and 5272 is interesting)
	protected final float CORE_MASS_BASE = 6e6f; // Saturn mass
	protected float saturnRadius = 840;          // Saturn bing 600 and first visible ring 840
	protected float moonMass = 1;

	// 1336   1 Pan
	// 1365   1 Daphnis
	// 1377   1 Atlas
	// 1394   1 Prometheus
	// 1417   1 Pandora
	// 1515   1 Janus & Epimetheus
	// 1675   1 Aegaeon
	// 1855 100 Mimas
	// 2380 100 Encheladus
	// 2946 200 Tethys
	// 3774 200 Dione
	// 5271 300 Rhea
	//12219 999 Titan


	public SaturnShepherds() {
		log.info("Scenario Initialization");

		coreMassBase  = CORE_MASS_BASE;
        coreMass = new float[] {CORE_MASS_BASE, moonMass/100}; // Saturn and Daphanis
	    NumParticles = 1_048_576*4;
		setParticleCount(NumParticles);                    // Ring dust particles
		setMassiveCount(6);                                // Shepherd moons + Saturn
		setAlpha(.75f);                                    // Ring base particle brightness
		ratio = 1f; 									   // all particles around Saturn
        Vector3f coreVel1 = new Vector3f(0,0,0);           // Saturn has no movement

        //Vector3f coreVel2 = new Vector3f(110,0,0); // good at 500 test particle

        // Daphanis settings
    	coreDist = new Vector3f(0,1365,-10);
        Vector3f coreVel2 = new Vector3f(67.33f,0,0);


        coreVel = new Vector3f[] {coreVel1,coreVel2};
        maxRadius = new float[] {MAX_RADIUS, 1};
        massBase = 1.5f;
	}

    @Override
	public void initDarkMater() {
        // TODO always have DM but default low influence
        // init properties - Dark Matter
		dmVolume = (float) java.lang.Math.pow(MAX_RADIUS * 1.5, 3);
        dmMass   = 1;
        dmCenter = new Vector3f();
	}

    @Override
	public void makeVertices(VaoVboManager manager) {
		log.info("init particle data");
        velBase  = 1f;

        FloatBuffer velBuffer = BufferUtils.createFloatBuffer(4*getParticleCount());
        Vector3f[] coreNormal = new Vector3f[] {new Vector3f(0,0,1), new Vector3f(1,0,0)};

        // set the core locations in the first two mass registers
        Vector3f pos = new Vector3f();
        manager.addVertex(pos);
        manager.addVertex(coreDist);


        Vector3f coreVel1 = coreVel[0];
        Vector3f coreVel2 = coreVel[1];
        velBuffer.put(coreVel1.x).put(coreVel1.y).put(coreVel1.z);
        velBuffer.put(coreMass[0]); // mass
        velBuffer.put(coreVel2.x).put(coreVel2.y).put(coreVel2.z);
        velBuffer.put(coreMass[1]); // mass

//    	// 1336 Pan
        float moonMass = 1f;
        Vector3f moonR = new Vector3f(1336,0,0);
        Vector3f moonV = new Vector3f(0,67,0);
//        velBuffer.put(moonV.x).put(moonV.y).put(moonV.z);
//        velBuffer.put(moonMass); // mass
//        manager.addVertex(moonR);
//
//    	// 1365 Daphnis
//
//        // 1377 Atlas
//        moonMass = 1f;
//        moonR = new Vector3f(0,1377,0);
//        moonV = new Vector3f(66,0,0);
//        velBuffer.put(moonV.x).put(moonV.y).put(moonV.z);
//        velBuffer.put(moonMass); // mass
//        manager.addVertex(moonR);
//
//        // 1394 Prometheus
//        moonMass = 1f;
//        moonR = new Vector3f(-1394,0,0);
//        moonV = new Vector3f(0,-65.6f,0);
//        velBuffer.put(moonV.x).put(moonV.y).put(moonV.z);
//        velBuffer.put(moonMass); // mass
//        manager.addVertex(moonR);
//
//    	// 1417 Pandora
//        moonMass = 1f;
//        moonR = new Vector3f(0,-1417,0);
//        moonV = new Vector3f(-65.1f,0,0);
//        velBuffer.put(moonV.x).put(moonV.y).put(moonV.z);
//        velBuffer.put(moonMass); // mass
//        manager.addVertex(moonR);
//
//        // 1515 Janus & Epimetheus
//        moonMass = 1f;
//        moonR = new Vector3f(1515,0,0);
//        moonV = new Vector3f(0,62.9f,0);
//        velBuffer.put(moonV.x).put(moonV.y).put(moonV.z);
//        velBuffer.put(moonMass); // mass
//        manager.addVertex(moonR);
//        moonR = new Vector3f(-1515,0,0);
//        moonV = new Vector3f(0,-62.9f,0);
//        velBuffer.put(moonV.x).put(moonV.y).put(moonV.z);
//        velBuffer.put(moonMass); // mass
//        manager.addVertex(moonR);
//
//    	// 1675 Aegaeon
//        moonMass = 0.1f;
//        moonR = new Vector3f(0,1675,0);
//        moonV = new Vector3f(59.85f,0,0);
//        velBuffer.put(moonV.x).put(moonV.y).put(moonV.z);
//        velBuffer.put(moonMass); // mass
//        manager.addVertex(moonR);
//
//		test
      moonMass = 1f;
      moonR = new Vector3f(0,-740.0806f,0);
      moonV = new Vector3f(-90.04013f,0,0);
      velBuffer.put(moonV.x).put(moonV.y).put(moonV.z);
      velBuffer.put(moonMass); // mass
      manager.addVertex(moonR);
      moonR = new Vector3f(0,740.0806f,0);
      moonV = new Vector3f(90.04013f,0,0);
      velBuffer.put(moonV.x).put(moonV.y).put(moonV.z);
      velBuffer.put(moonMass); // mass
      manager.addVertex(moonR);
      moonR = new Vector3f(740.0806f,0,0);
      moonV = new Vector3f(0,-90.04013f,0);
      velBuffer.put(moonV.x).put(moonV.y).put(moonV.z);
      velBuffer.put(moonMass); // mass
      manager.addVertex(moonR);
      moonR = new Vector3f(-740.0806f,0,0);
      moonV = new Vector3f(0,90.04013f,0);
      velBuffer.put(moonV.x).put(moonV.y).put(moonV.z);
      velBuffer.put(moonMass); // mass
      manager.addVertex(moonR);

//    	// 1860 Mimas
//        moonMass = 500f;  // 100f
//        moonR = new Vector3f(0,-1860,0);
//        moonV = new Vector3f(-56.8f,0,0);
//        velBuffer.put(moonV.x).put(moonV.y).put(moonV.z);
//        velBuffer.put(moonMass); // mass
//        manager.addVertex(moonR);
//        moonR = new Vector3f(0,1860,0);
//        moonV = new Vector3f(56.8f,0,0);
//        velBuffer.put(moonV.x).put(moonV.y).put(moonV.z);
//        velBuffer.put(moonMass); // mass
//        manager.addVertex(moonR);
//        moonR = new Vector3f(1860,0,0);
//        moonV = new Vector3f(0,-56.8f,0);
//        velBuffer.put(moonV.x).put(moonV.y).put(moonV.z);
//        velBuffer.put(moonMass); // mass
//        manager.addVertex(moonR);
//        moonR = new Vector3f(-1860,0,0);
//        moonV = new Vector3f(0,56.8f,0);
//        velBuffer.put(moonV.x).put(moonV.y).put(moonV.z);
//        velBuffer.put(moonMass); // mass
//        manager.addVertex(moonR);
//
//    	// 2380 Encheladus
//        moonMass = 100f;
//        moonR = new Vector3f(2380,0,0);
//        moonV = new Vector3f(0,50.21f,0);
//        velBuffer.put(moonV.x).put(moonV.y).put(moonV.z);
//        velBuffer.put(moonMass); // mass
//        manager.addVertex(moonR);
//
//    	// 2946 200 Tethys
//        moonMass = 100f;
//        moonR = new Vector3f(-2946,0,0);
//        moonV = new Vector3f(0,-45.13f,0);
//        velBuffer.put(moonV.x).put(moonV.y).put(moonV.z);
//        velBuffer.put(moonMass); // mass
//        manager.addVertex(moonR);
//
//        // 3774 200 Dione
//        moonMass = 100f;
//        moonR = new Vector3f(0,3774,0);
//        moonV = new Vector3f(39.87f,0,0);
//        velBuffer.put(moonV.x).put(moonV.y).put(moonV.z);
//        velBuffer.put(moonMass); // mass
//        manager.addVertex(moonR);
//
//        // 5271 300 Rhea
//        moonMass = 100f;
//        moonR = new Vector3f(0,-5271,0);
//        moonV = new Vector3f(-33.74f,0,0);
//        velBuffer.put(moonV.x).put(moonV.y).put(moonV.z);
//        velBuffer.put(moonMass); // mass
//        manager.addVertex(moonR);


        for(int b=getMassiveCount(); b<getParticleCount(); b++) {
            int leftRight = Math.random()<ratio ?0 :1;
            float r,aa,a1,a2;
            aa = (float)(Math.random()*Math.PI*2); // disk angle around
            a1 = Math.cos(aa);                   // y-ish angle part
            a2 = Math.sin(aa);                   // x-ish  angle part

    		// first make a fuzzy edge
    		float maxRad = (maxRadius[leftRight]-saturnRadius) * (1f+(float)Math.random()/5f); // max * fuzzy factor
    		// compute a random distance from core
    		r = (maxRad * (float)Math.random());
    		// void near core, central core mass represents black hole and surrounding mass
    		// and, it is visually better
    		r += saturnRadius/(1+leftRight);
    		// get position of particle
            // for one galaxy place in y-z plane, the other in x-y plane
            if (leftRight==1) { // y-z
            	pos.set(0,  // x, disk thickness
            			r*a2,   // y
            			r*a1);  // z
            } else { // x-y
            	pos.set(r*a1,  // x
            			r*a2,  // y
            			0); // z, disk thickness
            }

            // normal to that positions galaxy
            Vector3f velNormal = coreNormal[leftRight];

            // calculate the velocity with respect to the dark matter
            float vr = velBase; // * Math.sqrt(darkMass/pos.length()); // radial velocity magnitude

        	// calculate the velocity with respect to the galaxy center
            r  = pos.length();           // distance from galaxy center
            float totalMass = coreMass[leftRight];
            vr = velBase * Math.sqrt(totalMass/r); // galactic radial velocity magnitude
            Vector3f velv = new Vector3f();
            pos.cross(velNormal, velv).normalize().mul(vr);  // galactic radial velocity vector from stars
//        	if (r > 740 && r < 740.5) {
//        		System.out.println(r + ": " + velv.length());
//        	}

        	velv.add(coreVel[leftRight]); // add in galactic velocity

            velBuffer.put(velv.x).put(velv.y).put(velv.z);  // register particle velocity
            velBuffer.put((float)(massBase*Math.random())); // mass of each particle

//            leftRight = leftRight * 2 - 1;
            pos.x = leftRight*coreDist.x + pos.x;
            pos.y = leftRight*coreDist.y + pos.y;
            pos.z = leftRight*coreDist.z + pos.z;
            manager.addVertex(pos);
        }

        velBuffer.flip();
        velocities = velBuffer;
    }

}
