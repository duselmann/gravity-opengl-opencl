// Copyright (c) 2022 David Uselmann
package org.davu.app.space.scenarios;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;


public class StarStreams2 extends StarStreams {
	private static final Logger log = LogManager.getLogger(StarStreams2.class);

	public StarStreams2() {
		log.info("Scenario Initialization");

		setAlpha(.25f);
		ratio = 0.7f;
        Vector3f andromedaVel = new Vector3f(0,0,0);
        Vector3f milkywayVel = new Vector3f(0,0,-7);
        coreVel = new Vector3f[] {andromedaVel, milkywayVel};

        float andromedaMass = coreMassBase*24f;
        float milkywayMass = coreMassBase*.1f;
		coreMass = new float[] {andromedaMass, milkywayMass};

		maxRadius = new float[] {400, 250};
		ratio = 0.7f;
	}

}
