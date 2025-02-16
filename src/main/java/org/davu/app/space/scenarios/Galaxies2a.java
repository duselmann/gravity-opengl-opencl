// Copyright (c) 2022 David Uselmann
package org.davu.app.space.scenarios;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;


public class Galaxies2a extends Galaxies2 {
	private static final Logger log = LogManager.getLogger(Galaxies2a.class);

	public Galaxies2a() {
		log.info("Scenario Initialization");

		setAlpha(.33f);
		ratio = 0.6f;
        Vector3f coreVel1 = new Vector3f(0,0,0);
        Vector3f coreVel2 = new Vector3f(0,5,0);
        coreVel = new Vector3f[] {coreVel1,coreVel2};
	}
}
