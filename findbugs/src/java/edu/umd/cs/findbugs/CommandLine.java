/*
 * FindBugs - Find bugs in Java programs
 * Copyright (C) 2004, University of Maryland
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.umd.cs.findbugs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper class for parsing command line arguments.
 */
public abstract class CommandLine {
	private List<String> optionList;
	private Set<String> requiresArgumentSet;
	private Map<String, String> optionDescriptionMap;
	private Map<String, String> argumentDescriptionMap;
	int maxWidth;

	public CommandLine() {
		this.optionList = new LinkedList<String>();
		this.requiresArgumentSet = new HashSet<String>();
		this.optionDescriptionMap = new HashMap<String, String>();
		this.argumentDescriptionMap = new HashMap<String, String>();
		this.maxWidth = 0;
	}

	/**
	 * Add an option.
	 * @param option the option, must start with "-"
	 * @param description single line description of the option
	 */
	public void addOption(String option, String description) {
		optionList.add(option);
		optionDescriptionMap.put(option, description);

		if (option.length() > maxWidth)
			maxWidth = option.length();
	}

	/**
	 * Add an option requiring an argument.
	 * @param option the option, must start with "-"
	 * @param argumentDesc brief (one or two word) description of the argument
	 * @param description single line description of the option
	 */
	public void addOption(String option, String argumentDesc, String description) {
		optionList.add(option);
		optionDescriptionMap.put(option, description);
		requiresArgumentSet.add(option);
		argumentDescriptionMap.put(option, argumentDesc);

		int width = option.length() + 3 + argumentDesc.length();
		if (width > maxWidth)
			maxWidth = width;
	}

	/**
	 * Parse a command line.
	 * Calls down to handleOption() and handleOptionWithArgument() methods.
	 * Stops parsing when it reaches the end of the command line,
	 * or when a command line argument not starting with "-" is seen.
	 *
	 * @param argv the arguments
	 * @return the number of arguments parsed; if equal to
	 *   argv.length, then the entire command line was parsed
	 */
	public int parse(String argv[]) throws IOException {
		int arg = 0;

		while (arg < argv.length) {
			String option = argv[arg];
			if (!option.startsWith("-"))
				break;

			if (optionDescriptionMap.get(option) == null)
				throw new IllegalArgumentException("Unknown option: " + option);

			if (requiresArgumentSet.contains(option)) {
				++arg;
				if (arg >= argv.length)
					throw new IllegalArgumentException("Option " + option + " requires an argument");
				String argument = argv[arg];
				handleOptionWithArgument(option, argument);
				++arg;
			} else {
				handleOption(option);
				++arg;
			}
		}

		return arg;
	}

	/**
	 * Callback method for handling an option.
	 * @param option the option
	 */
	protected abstract void handleOption(String option) throws IOException;

	/**
	 * Callback method for handling an option with an argument.
	 * @param option the option
	 * @param argument the argument
	 */
	protected abstract void handleOptionWithArgument(String option, String argument) throws IOException;

	/**
	 * Print command line usage information to given stream.
	 * @param os the output stream
	 */
	public void printUsage(OutputStream os) {
		PrintStream out = new PrintStream(os);
		for (Iterator<String> i = optionList.iterator(); i.hasNext(); ) {
			String option = i.next();
			out.print("  ");

			StringBuffer buf = new StringBuffer();
			buf.append(option);
			if (requiresArgumentSet.contains(option)) {
				buf.append(" <");
				buf.append(argumentDescriptionMap.get(option));
				buf.append(">");
			}
			printField(out, buf.toString(), maxWidth+1);

			out.println(optionDescriptionMap.get(option));
		}
	}

	private static final String SPACES = "                    ";

	private static void printField(PrintStream out, String s, int width) {
		if (s.length() > width) throw new IllegalArgumentException();
		int nSpaces = width - s.length();
		out.print(s);
		while (nSpaces > 0) {
			int n = Math.min(SPACES.length(), nSpaces);
			out.print(SPACES.substring(0, n));
			nSpaces -= n;
		}
	}
}

// vim:ts=3
