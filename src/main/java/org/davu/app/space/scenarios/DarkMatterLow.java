// Copyright (c) 2022 David Uselmann
package org.davu.app.space.scenarios;

import java.nio.FloatBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.davu.app.space.display.VaoVboManager;
import org.joml.Math;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;


public class DarkMatterLow extends Galaxy {
	private static final Logger log = LogManager.getLogger(DarkMatterLow.class);

	private final float MAX_RADIUS = 400;
	private final float CORE_MASS_BASE = 5e4f;

	public DarkMatterLow() {
		super();
	}
    @Override
	public void initDarkMater() {
		log.info("Scenario LOW Dark Matter << visible");
        // init properties - Dark Matter
		dmVolume = /* MAX_RADIUS*2; */  (float) java.lang.Math.pow(MAX_RADIUS * 1.5, 3);
        dmMass   = CORE_MASS_BASE * 2f;
        dmCenter = new Vector3f();
	}


}
