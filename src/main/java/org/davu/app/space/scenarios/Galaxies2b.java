// Copyright (c) 2022 David Uselmann
package org.davu.app.space.scenarios;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.davu.app.space.Glasses3D;
import org.joml.Vector3f;


public class Galaxies2b extends Galaxies2 {
	private static final Logger log = LogManager.getLogger(Galaxies2b.class);

	public Galaxies2b(Glasses3D glasses3D) {
		super(glasses3D);
		log.info("Scenario Initialization");

		setAlpha(.2f);
		ratio = 0.8f;
        Vector3f coreVel1 = new Vector3f(0,0,0);
        Vector3f coreVel2 = new Vector3f(0,-4,0);
        coreVel = new Vector3f[] {coreVel1,coreVel2};
	}

}
