package org.davu.opencl.utils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CliManager {

	Options options = new Options();
	CommandLineParser parser = new DefaultParser();
	CommandLine params;

	Option help;
	Option width;
	Option height;
	Option scenario;
	Option list;

	public CliManager() {
		intiOptions();
	}

	private void intiOptions() {
		Option option;
		option = help = new Option("?", "help",   false, "Display CLI help. This documentation.");
		options.addOption(option);

		option = width = new Option("w", "width",  true, "Pixel width of the viewport and window");
		options.addOption(option);
		option = height = new Option("h", "height", true, "Pixel height of the viewport and window");
		options.addOption(option);

		option = scenario = new Option("s", "scenario", true, "Initial conditions scenario Class Name");
		options.addOption(option);
		option = list = new Option("l", "list",     false, "List all the scenario Class Names");
		options.addOption(option);
	}

	public void displayCliOptions() {
		HelpFormatter formatter = new HelpFormatter();
		System.err.println();
		formatter.printHelp("Space", options);
		System.err.println();
	}
	public void displayCliOptionsExit() {
		displayCliOptions();
		System.exit(1);
	}

	public boolean helpRequested() {
		return params.hasOption(help);
	}

	public void parseArgs(String ... args) {
		try {
			params = parser.parse(options, args);
			if (helpRequested()) {
				displayCliOptionsExit();
			}
		} catch (ParseException e) {
			System.err.println();
			System.err.println("There is an invalid option in the command line arguments.");
			displayCliOptionsExit();
		}
	}

	public boolean hasScenario() {
		return params.hasOption(scenario);
	}

	public String getScenario() {
		return params.getOptionValue(scenario);
	}

	public boolean hasWidth() {
		return params.hasOption(width);
	}
	public int getWidth() {
		return Integer.valueOf( params.getOptionValue(width) );
	}
	public boolean hasHeight() {
		return params.hasOption(height);
	}
	public int getHeight() {
		return Integer.valueOf( params.getOptionValue(height) );
	}

	public void list() {
		if ( ! params.hasOption(list)) {
			return;
		}
	}
}
