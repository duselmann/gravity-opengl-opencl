// Copyright (c) 2022 David Uselmann
package org.davu.app.space.scenarios;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Galaxies2 extends Galaxies {
	private static final Logger log = LogManager.getLogger(Galaxies2.class);

	public Galaxies2() {
		log.info("Scenario Initialization");

		setAlpha(.33f);
		coreMass = new float[] {coreMassBase*10f, coreMassBase/10f};
		maxRadius = new float[] {400, 300};
		ratio = 0.8f;
	}

}
