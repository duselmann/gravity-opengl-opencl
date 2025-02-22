// Copyright (c) 2022 David Uselmann
package org.davu.app.space.scenarios;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Triplets2 extends Triplets {
	private static final Logger log = LogManager.getLogger(Triplets2.class);

	public Triplets2() {
		super();
		log.info("Scenario Initialization");

//        Vector3f coreVel0 = new Vector3f( 0f, 0f,0f);
//        Vector3f coreVel1 = new Vector3f( 1f,-1f,0f);
//        Vector3f coreVel2 = new Vector3f( 2f, 4f,0f);
        coreVel[2].x = 2f;
	}
}
