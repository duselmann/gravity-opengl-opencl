// Copyright (c) 2022 David Uselmann
package org.davu.app.space.scenarios;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;


public class DarkEnergyLow extends DarkEnergyIdeal {
	private static final Logger log = LogManager.getLogger(DarkEnergyLow.class);

	@Override
	protected void addDarkEnergy(Vector3f pos, Vector3f velv) {
		// none - for low configuration
		log.info("No dark energy");;
	}
}
