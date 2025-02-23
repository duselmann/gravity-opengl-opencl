// Copyright (c) 2022 David Uselmann
package org.davu.app.space.scenarios;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;


public class PinwheelGalaxy extends Galaxy {
	private static final Logger log = LogManager.getLogger(PinwheelGalaxy.class);

	private final float CORE_MASS_BASE = 5e4f;

	public PinwheelGalaxy() {
		super();
		log.info("Scenario Initialization");

		coreMassBase  = CORE_MASS_BASE;
        coreMass = new float[] {CORE_MASS_BASE, CORE_MASS_BASE};

        Vector3f coreVel1 = new Vector3f(0,0,0);
        Vector3f coreVel2 = new Vector3f(0,0,-10f);
        coreVel = new Vector3f[] {coreVel1,coreVel2};

        coreDist = new Vector3f(0f, 0f, 500f);
	}

}
