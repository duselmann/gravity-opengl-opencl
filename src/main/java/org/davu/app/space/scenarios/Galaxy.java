// Copyright (c) 2022 David Uselmann
package org.davu.app.space.scenarios;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;


public class Galaxy extends Galaxies {
	private static final Logger log = LogManager.getLogger(Galaxy.class);

	private final float MAX_RADIUS = 400;
	private final float CORE_MASS_BASE = 5e4f;

	public Galaxy() {
		log.info("Scenario Initialization");

		coreMassBase  = CORE_MASS_BASE;
        coreMass = new float[] {CORE_MASS_BASE, 0.1f};
    	coreDist = new Vector3f(0,0,0);
	    NumParticles = (int)(1_048_576/64.0);
		setParticleCount(NumParticles);
		setMassiveCount(NumParticles);
		setAlpha(.9f);
		ratio = 1f;
        Vector3f coreVel1 = new Vector3f(0,0,0);
        Vector3f coreVel2 = new Vector3f(0,0,0);
        coreVel = new Vector3f[] {coreVel1,coreVel2};
        maxRadius = new float[] {MAX_RADIUS, MAX_RADIUS};
        massBase = 1f;
	}
    @Override
	public void initDarkMater() {
        // TODO always have DM but default low influence
        // init properties - Dark Matter
		dmVolume = /* MAX_RADIUS*2; */  (float) Math.pow(MAX_RADIUS * 1.5, 3);
        dmMass   = CORE_MASS_BASE * 20f;
        dmCenter = new Vector3f();
	}
}
