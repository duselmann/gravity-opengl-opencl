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


public class AndromedaAdjoin extends Galaxies2 {
	private static final Logger log = LogManager.getLogger(AndromedaAdjoin.class);


	public AndromedaAdjoin() {
		log.info("Scenario Initialization");

		setParticleCount(NumParticles*16);
		setMassiveCount(NumParticles/2);
		massBase = 1.5f;

		setAlpha(.25f);
		ratio = 0.7f;
        Vector3f andromedaVel = new Vector3f(0,0,0);
//        Vector3f milkywayVel = new Vector3f(0,0,0);
        Vector3f milkywayVel = new Vector3f(19f,5,-55);
        coreVel = new Vector3f[] {andromedaVel, milkywayVel};

        float andromedaMass = coreMassBase*99*4f;
        float milkywayMass = coreMassBase*4f;
		coreMass = new float[] {andromedaMass, milkywayMass};

		maxRadius = new float[] {500, 100};
    	coreDist = new Vector3f(200,0,300);
		coreDist = new Vector3f(200,0,200);
        velBase  = 1f;

		initDarkMater();
	}

    @Override
	public void initDarkMater() {
        // TODO always have DM but default low influence
        // init properties - Dark Matter
//        dmVolume = 8e9f; //2^30
		dmVolume = (float) java.lang.Math.pow(400 * 1.5, 3);
        dmMass   = coreMassBase * 20f;
        dmCenter = new Vector3f();
	}

    @Override
	public void makeVertices(VaoVboManager manager) {
		log.info("init particle data");

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

        float starMass = getParticleCount()*massBase*2.1f; // estimated internal mass from dust/stars

        for(int b=2; b<getParticleCount(); b++) {
            int leftRight = Math.random()<ratio ?0 :1;
            float r,aa,a1,a2;
            r = maxRadius[leftRight] * (float)Math.random() + (50/(1+leftRight));
            float galaxyArea = maxRadius[leftRight]*maxRadius[leftRight];//*4;
            aa = (float)(Math.random()*Math.PI*2); // disk angle around
            a1 = Math.cos(aa);                   // y-ish angle part
            a2 = Math.sin(aa);                   // x-ish  angle part

            // normal to that positions galaxy
            float innerArea = r*r;                      // inner disc volume of star matter
            float innerVol  = innerArea*r;                      // volume of dark matter
            Vector3f velNormal = coreNormal[leftRight];
            float darkMass  = 0; // inner dark matter mass

            // for one galaxy place in y-z plane, the other in x-y plane
            if (leftRight==1) {
            	pos.set(((float)Math.random()*10f-5f)*2f,  // x, disk thickness
            			r*a2,   // y
            			r*a1);  // z
            } else {
            	pos.set(r*a1,  // x
            			r*a2,  // y
            			((float)Math.random()*10f-5f)*2f); // z, disk thickness
                darkMass  = dmMass * innerVol/dmVolume; // inner dark matter mass
            }



            // calculate the velocity with respect to the dark matter
            float vr = velBase; // * Math.sqrt(darkMass/pos.length()); // radial velocity magnitude

        	// calculate the velocity with respect to the galaxy center
//            r  = pos.length();           // distance from galaxy center
//            Vector3f velDark = new Vector3f();
//        	velNormal.cross(pos, velDark).normalize().mul(vr);     // radial velocity vector

//            float discArea  = maxRadius[leftRight]*maxRadius[leftRight]; // disc volume of star matter
//            float innerMass = massBase/7.5f * getMassiveCount() * innerArea/discArea; // inner dark matter mass
//            float innerVr   =  Math.sqrt(innerMass/pos.length()); // radial velocity magnitude
//            Vector3f velInner = new Vector3f();
//        	velNormal.cross(pos, velInner).normalize().mul(innerVr);     // radial velocity vector


        	// calculate the velocity with respect to the galaxy center
            float starsMass = starMass * innerArea/galaxyArea; // inner dark matter mass
//            float totalMass = darkMass + starsMass + coreMass[leftRight];
            float totalMass = coreMass[leftRight];
            vr = velBase * Math.sqrt(totalMass/r); // galactic radial velocity magnitude

//            vr = velBase * Math.sqrt(coreMass[leftRight]/r); // galactic radial velocity magnitude
            Vector3f velv = new Vector3f();
            pos.cross(velNormal, velv).normalize().mul(vr);  // galactic radial velocity vector from stars

//            if (leftRight==0) {
//            	velv.add(velDark); // add in the dark velocity
//            	System.err.println(velDark);
//            	System.err.println(velInner);
//            }
//        	velv.sub(velInner); // add in the dark velocity
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

	@Override
	public void setGlasses(Glasses3D glasses) {
		super.setGlasses(glasses);
//		glasses3D.setGlasses();
		glasses3D.setSeparation3D(40f);
	}
}
