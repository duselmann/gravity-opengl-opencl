package org.davu.app.space;

import org.davu.app.space.display.Glasses3D;
import org.davu.app.space.display.Particles;

public class ScenarioManager {

	protected final String packageName = "org.davu.app.space.scenarios.";


	public Particles build(String className, Glasses3D glasses) {
		Class<? extends Particles> scenario = getClass(className);

		if (scenario == null) {
			String packagedName = packageName + className;
			scenario = getClass(packagedName);
			if (scenario == null) {
				throw new RuntimeException("Scenario not found: " + packagedName);
			}
		}

		Particles particles;
		try {
			particles = scenario.newInstance();
			particles.setGlasses(glasses);
		} catch (Throwable t) {
			throw new RuntimeException("Failed to create scenario: " + className, t);
		}
		return particles;
	}

	@SuppressWarnings("unchecked")
	protected Class<? extends Particles> getClass(String className) {
		try {
			return (Class<? extends Particles>) Class.forName(className);
		} catch (Throwable t) {
			// try another means
		}
		return null;
	}
}
