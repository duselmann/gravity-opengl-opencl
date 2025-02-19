// Copyright (c) 2022 David Uselmann
package org.davu.app.space.scenarios;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;


public class DarkMatterIdeal extends Galaxy {
	private static final Logger log = LogManager.getLogger(DarkMatterIdeal.class);

	private final float MAX_RADIUS = 400;
	private final float CORE_MASS_BASE = 5e4f;

	public DarkMatterIdeal() {
		super();
	}
    @Override
	public void initDarkMater() {
		log.info("Scenario Ideal Dark Matter 10-20 x visible");
        // init properties - Dark Matter
		dmVolume = (float) java.lang.Math.pow(MAX_RADIUS * 1.5, 3);
        dmMass   = CORE_MASS_BASE * 20f;
        dmCenter = new Vector3f();
	}


}
