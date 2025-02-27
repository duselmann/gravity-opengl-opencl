package org.davu.app.space;

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
	Option top;
	Option left;
	Option scenario;
	Option list;
	Option delay;

	public CliManager() {
		intiOptions();
	}

	private void intiOptions() {
		Option option;
		option = help = new Option("?", "help",   false, "Display CLI help. This documentation.");
		options.addOption(option);
		option = list = new Option("l", "list",     false, "List all the scenario Class Names.");
		options.addOption(option);

		option = width = new Option("w", "width",  true, "Pixel width of the viewport and window.");
		option.setType(Integer.class);
		options.addOption(option);
		option = height = new Option("h", "height", true, "Pixel height of the viewport and window.");
		option.setType(Integer.class);
		options.addOption(option);
		option = top = new Option("t", "top",  true, "Pixel top for window location.");
		option.setType(Integer.class);
		options.addOption(option);
		option = left = new Option("l", "left", true, "Pixel left window location.");
		option.setType(Integer.class);
		options.addOption(option);

		option = delay = new Option("d", "delay", true, "Milliseconds to wait to start compute.");
		option.setType(Integer.class);
		options.addOption(option);

		option = scenario = new Option("s", "scenario", true, "Initial conditions scenario Class Name.");
		option.setType(String.class);
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
		return getInt(width);
	}
	public boolean hasHeight() {
		return params.hasOption(height);
	}
	public int getHeight() {
		return getInt(height);
	}
	public boolean hasTop() {
		return params.hasOption(top);
	}
	public int getTop() {
		return getInt(top);
	}
	public boolean hasLeft() {
		return params.hasOption(left);
	}
	public int getLeft() {
		return getInt(left);
	}
	public boolean hasDelay() {
		return params.hasOption(delay);
	}
	public int getDelay() {
		return getInt(delay);
	}

	protected int getInt(Option intOption) {
		int value = 100;
		String optionValue = params.getOptionValue(intOption);
		try {
			value = Integer.valueOf(optionValue).intValue();
			if (value < 1) {
				throw new ParseException("Value may not be negative.");
			}
		} catch (Exception e) {
			System.err.print(intOption.getArgName() + " must be a valid positive integer. `"
					+ optionValue + "` is not acceptable.");
		}
		return value;
	}

	public void list() {
		if ( ! params.hasOption(list)) {
			return;
		}
	}
}
