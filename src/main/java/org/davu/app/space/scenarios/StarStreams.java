// Copyright (c) 2022 David Uselmann
package org.davu.app.space.scenarios;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.davu.app.space.display.Glasses3D;
import org.joml.Vector3f;


public class StarStreams extends Galaxies2 {
	private static final Logger log = LogManager.getLogger(StarStreams.class);

	public StarStreams() {
		log.info("Scenario Initialization");

		setAlpha(.1f);
		ratio = 0.7f;
        Vector3f andromedaVel = new Vector3f(0,0,0);
        Vector3f milkywayVel = new Vector3f(4,0,-6);
        coreVel = new Vector3f[] {andromedaVel, milkywayVel};

        float andromedaMass = coreMassBase*24f;
        float milkywayMass = coreMassBase*.1f;
		coreMass = new float[] {andromedaMass, milkywayMass};

		maxRadius = new float[] {400, 250};
		ratio = 0.7f;
	}
	@Override
	public void setGlasses(Glasses3D glasses) {
		super.setGlasses(glasses);
		glasses3D.setGlasses();
		glasses3D.setSeparation3D(40f);
	}

}
