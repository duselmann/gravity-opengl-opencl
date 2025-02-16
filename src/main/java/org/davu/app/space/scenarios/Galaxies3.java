// Copyright (c) 2022 David Uselmann
package org.davu.app.space.scenarios;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;


public class Galaxies3 extends Galaxies2 {
	private static final Logger log = LogManager.getLogger(Galaxies3.class);

	public Galaxies3() {
		log.info("Scenario Initialization");

		setAlpha(.1f);
		ratio = 0.7f;
        Vector3f andromedaVel = new Vector3f(0,0,0);
        Vector3f milkywayVel = new Vector3f(0,-9,0);
        coreVel = new Vector3f[] {andromedaVel, milkywayVel};

        float andromedaMass = coreMassBase*24f;
        float milkywayMass = coreMassBase;
		coreMass = new float[] {andromedaMass, milkywayMass};

		maxRadius = new float[] {400, 250};
		ratio = 0.7f;
	}

}
