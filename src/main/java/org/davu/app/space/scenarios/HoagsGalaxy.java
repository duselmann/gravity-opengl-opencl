// Copyright (c) 2022 David Uselmann
package org.davu.app.space.scenarios;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class HoagsGalaxy extends Galaxy {
	private static final Logger log = LogManager.getLogger(HoagsGalaxy.class);

	private final float CORE_MASS_BASE = 5e4f;

	public HoagsGalaxy() {
		super();
		log.info("Scenario Initialization");

		coreMassBase  = CORE_MASS_BASE;
        coreMass = new float[] {CORE_MASS_BASE, CORE_MASS_BASE};
	}

}
