package org.davu.opencl.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScenarioCollectionTests {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
		ScenarioCollection.initCollection();
	}

	@AfterEach
	void tearDown() throws Exception {
		ScenarioCollection.initCollection();
	}

	@Test
	void testThatRegisteredClassIsRegistered() {
		boolean result = ScenarioCollection.registerScenario("Test", "org.package.Test");

		assertTrue(result, "Expect first registration success as true result");
	}

	@Test
	@SuppressWarnings("unused")
	void testDoubleRegisteredClassShortNameIsDetected() {
		boolean result1 = ScenarioCollection.registerScenario("Test", "org.package.Test");
		boolean result2 = ScenarioCollection.registerScenario("Test", "org.other.Test");

		assertFalse(result2, "Expect double registration warning as false result");
	}

	@Test
	@SuppressWarnings("unused")
	void testSecondRegisteredClass() {
		boolean result1 = ScenarioCollection.registerScenario("Test", "org.package.Test");
		boolean result2 = ScenarioCollection.registerScenario("Test", "org.other.Test");
		boolean result3 = ScenarioCollection.registerScenario("Another", "org.other.Another");

		assertTrue(result3, "Expect first registration success as true result");
	}

	@Test
	void testDisplayClassNameCollisions() {
		ScenarioCollection.registerScenario("Test", "org.package.Test");
		ScenarioCollection.registerScenario("Test", "org.other.Test");
		ScenarioCollection.registerScenario("Another", "org.other.Another");
		ScenarioCollection.existIfCollisions(false); // do not exit during testing
	}

	@Test
	@SuppressWarnings("unused")
	void testExactSameReRegister() {
		boolean result1 = ScenarioCollection.registerScenario("Test", "org.package.Test");
		boolean result2 = ScenarioCollection.registerScenario("Test", "org.package.Test");

		assertFalse(result2, "Expect double registration warning as false result");
	}

	@Test
	void testDisplayPriorsShouldDisplayNothingWhenNoShortNameCollision() {
		ScenarioCollection.registerScenario("Test", "org.package.Test");
		ScenarioCollection.registerScenario("Another", "org.other.Another");
		ScenarioCollection.existIfCollisions();
	}

	@Test
	void testGetScenarioClassName() {
		String testClassName = "org.package.Test";
		ScenarioCollection.registerScenario("Test", testClassName);
		ScenarioCollection.registerScenario("Another", "org.other.Another");
		String scenarioClass = ScenarioCollection.getScenario("Test");

		assertEquals(scenarioClass, testClassName);
	}
}
