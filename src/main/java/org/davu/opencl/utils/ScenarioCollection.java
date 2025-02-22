package org.davu.opencl.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.davu.app.space.display.Particles;

/**
 * Manages all scenario class names by registration
 * WHIP: This will only work if the class is instantiated.
 * TODO: Need to discover all subclasses in classpath
 */
public class ScenarioCollection {

	static Map<String, String> scenarios = new HashMap<>();
	static List<String> priors = new LinkedList<>(); // conflicting registered classes

	/**
	 * Registers a new scenario by Class instance.
	 * All classes must extend Particles class and the short name must be unique
	 *
	 * @param clase Particle subclass to register by short name
	 * @return true if successful, false is collision with prior registration
	 */
	public static boolean registerScenario(Class<? extends Particles> clase) {
		return registerScenario(clase.getSimpleName(), clase.getCanonicalName());
	}

	/**
	 * Registers a new scenario by Class instance.
	 * internal use version of the Class instance version
	 *
	 * @param simpleName class name without package
	 * @param canonicalName fully qualified class name
	 * @return true if successful, false is collision with prior registration
	 */
	static boolean registerScenario(String simpleName, String canonicalName) {
		String prior = scenarios.put(simpleName, canonicalName);
//		System.err.println(canonicalName);

		if (prior != null) {
			priors.add(prior);
			System.err.println();
			System.err.println("Warning:");
			printPriorMessage(canonicalName, prior);
			if (canonicalName.equals(prior)) {
				System.err.print("Special notice: Twice registered same class. Must be called twice!");
			}
			System.err.println();
		}
		return prior == null;
	}

	/**
	 * Return the class name for registered scenario lookup.
	 * @param name short name of the class
	 * @return canonical name of the class
	 */
	public static String getScenario(String name) {
		return scenarios.get(name);
	}

	/**
	 * Prints a warning message using the current class and the prior class.
	 * Internally used by the class to displace a consistent message
	 * @param current newly registered class that will replace another
	 * @param prior previously registered class that was replaced
	 */
	static void printPriorMessage(String current, String prior) {
		System.err.println("Registered class, " + current
			+ ", conflicts with and replaces prior registered class, " + prior);
	}


	/**
	 * Displays a list of all the scenario registration collisions and exists.
	 */
	public static void existIfCollisions() {
		existIfCollisions(true);
	}
	/**
	 * Internal method: unit testing does not exit
	 * @param exit true to exit if priors exist
	 */
	static void existIfCollisions(boolean exit) {
		// do nothing if no collision
		if (priors.isEmpty()) {
			return;
		}
		// print all the collisions
		System.err.println("Summary of all Scenarios that have short name collisions");
		for (String prior:priors) {
			String shortName = prior.substring( prior.lastIndexOf('.')+1 );
			String current = scenarios.get(shortName);
			printPriorMessage(current, prior);
		}
		// exit after printing collisions
		if (exit) {
			System.exit(1);
		}
	}

	static void initCollection() {
		scenarios = new HashMap<>();
		priors = new LinkedList<>();
	}
}
