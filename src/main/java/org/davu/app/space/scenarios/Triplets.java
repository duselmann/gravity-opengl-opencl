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


public class Triplets extends Particles {
	private static final Logger log = LogManager.getLogger(Triplets.class);

	protected float coreMassBase  = 5e3f;
    protected int NumParticles = (int)(1_048_576/32.0);
	protected float[] coreMass;
	protected float[] maxRadius;
	protected float[] ratio;
	protected Vector3f[] coreVel;
	protected Vector3f[] corePos;
//	protected Vector3f coreDist = new Vector3f(500,0,250);
	Vector3f[] coreNormal;

	public Triplets() {
		log.info("Scenario Initialization");

		setParticleCount(NumParticles);
		setMassiveCount(NumParticles);

		setAlpha(.5f);

//      Vector3f[] coreNormal = new Vector3f[] {new Vector3f(0,0,1), new Vector3f(1,0,0), new Vector3f(0,1,0)};
        coreNormal = new Vector3f[] {new Vector3f(0,0,1f) /*middle*/, new Vector3f(0,1,0) /*left*/, new Vector3f(1,0,0) /*right*/};

        Vector3f coreVel0 = new Vector3f( 0f, 0f,0f);
        Vector3f coreVel1 = new Vector3f( 1f,-1f,0f);
        Vector3f coreVel2 = new Vector3f( 0f, 4f,0f);
        coreVel = new Vector3f[] {coreVel0,coreVel1,coreVel2};
//        coreVel = new Vector3f[] {new Vector3f(),new Vector3f(),new Vector3f()};
        coreMass = new float[] {coreMassBase, coreMassBase, coreMassBase};
		maxRadius = new float[] {350, 350, 350};
		ratio = new float[] {0.33f, 0.33f, 0.33f};

		// set the core locations in the first two mass registers
        Vector3f center = new Vector3f(0,0,0);
        Vector3f left   = new Vector3f(-1000,0,-555);
        Vector3f right  = new Vector3f(500,0,500);
        corePos = new Vector3f[] {center, left, right};

        massBase = 0.1f;
	}

    @Override
	public void makeVertices(VaoVboManager manager) {
		log.info("init particle data");

		glasses3D.setGlasses();

        velBase  = 1f;

        FloatBuffer velBuffer = BufferUtils.createFloatBuffer(4*getParticleCount());

        for (int core=0; core < corePos.length; core++) {
	        velBuffer.put(coreVel[core].x).put(coreVel[core].y).put(coreVel[core].z);
	        velBuffer.put(coreMass[core]); // mass
	        manager.addVertex(corePos[core]);
        }

        float starMass = getParticleCount()*massBase*0;

        Vector3f pos = new Vector3f();
        for(int b=corePos.length; b<getParticleCount(); b++) {

        	double rand = Math.random();
            int whichGalaxy = rand<ratio[0]?0 : rand<(ratio[0]+ratio[1])?1 : 2;

            float r,aa,a1,a2;
            float galaxyArea = maxRadius[whichGalaxy]*maxRadius[whichGalaxy]*4;
            r = (maxRadius[whichGalaxy] * (float)Math.random() + 50 /*min dist from core*/);
            aa = (float)(Math.random()*Math.PI*2);
            a1 = (Math.cos(aa));
            a2 = (Math.sin(aa));
    		// get position of particle

//            float velMult = 1f;
//            float multiplier = 0;
            if (whichGalaxy==2) {
//                multiplier = 1;
            	pos.set(((float)Math.random()*10f-5f)*2f, // x
            			r*a2,   // y
            			r*a1);  // z
            } else if (whichGalaxy==1) {
//              velMult = 0.5f;
//                multiplier = 1;
            	pos.set(r*a1, // x
            			((float)Math.random()*10f-5f)*2f, // y
            			r*a2   // z
            			);
            } else {
//                multiplier = 1;
            	pos.set(r*a1,  // x
            			r*a2,  // y
            			((float)Math.random()*10f-5f)*2f); // z
            }

            // normal to that positions galaxy
            Vector3f velNormal = coreNormal[whichGalaxy];

            // calculate the velocity with respect to the dark matter
            float vr = velBase; // * Math.sqrt(darkMass/pos.length()); // radial velocity magnitude

        	// calculate the velocity with respect to the galaxy center
    		r  = pos.length();                     // distance from dark center
            float innerVol = r*r*r;                      // volume of dark matter
            float darkMass = dmMass * innerVol/dmVolume; // inner dark matter mass
            float innerArea = r*r;                      // volume of dark matter
            float starsMass = starMass * innerArea/galaxyArea; // inner dark matter mass
            float totalMass = darkMass + starsMass + coreMass[whichGalaxy];
            vr = velBase * Math.sqrt(totalMass/r); // galactic radial velocity magnitude
            Vector3f velv = new Vector3f();
            pos.cross(velNormal, velv).normalize().mul(vr);  // galactic radial velocity vector from stars

        	velv.add(coreVel[whichGalaxy]); // add in galactic velocity

            velBuffer.put(velv.x).put(velv.y).put(velv.z);  // register particle velocity
            velBuffer.put((float)(massBase*Math.random())); // mass of each particle

            pos.x = corePos[whichGalaxy].x + pos.x;
            pos.y = corePos[whichGalaxy].y + pos.y;
            pos.z = corePos[whichGalaxy].z + pos.z;
            manager.addVertex(pos);
        }

        velBuffer.flip();
        velocities = velBuffer;
    }

    @Override
	public FloatBuffer  initVelocities(VaoVboManager manager) {
    	return velocities;
	}

    @Override
	public void initDarkMater() {
        // TODO always have DM but default low influence
        // init properties - Dark Matter
        dmVolume = 8e9f; //2^30
        dmMass   = coreMassBase*2*1000;
        dmCenter = new Vector3f();
	}
}
