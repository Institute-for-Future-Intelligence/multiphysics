package org.energy2d.system;

import java.util.LinkedList;

/**
 * Implements a queue for a bash-like command history.
 * 
 * @author Charles Xie
 * 
 */

final class CommandHistory {

	private LinkedList<String> commandList = new LinkedList<String>();
	private int maxSize;
	private int pos = 0;

	/*
	 * Creates a new instance.
	 * 
	 * @param maxSize maximum size for the command queue
	 */
	CommandHistory(int maxSize) {
		this.maxSize = maxSize;
	}

	/*
	 * Retrieves the following command from the bottom of the list, updates list
	 * position.
	 * 
	 * @return the String value of a command
	 */
	String getCommandUp() {
		if (commandList.size() > 0)
			pos--;
		return getCommand();
	}

	/*
	 * Retrieves the following command from the top of the list, updates list
	 * position.
	 * 
	 * @return the String value of a command
	 */
	String getCommandDown() {
		if (commandList.size() > 0)
			pos++;
		return getCommand();
	}

	/*
	 * Calculates the command to return.
	 * 
	 * @return the String value of a command
	 */
	private String getCommand() {
		if (pos == 0)
			return "";
		int size = commandList.size();
		if (size > 0) {
			if (pos == (size + 1)) {
				return ""; // just beyond last one: ""
			} else if (pos > size) {
				pos = 1; // roll around to first command
			} else if (pos < 0) {
				pos = size; // roll around to last command
			}
			return commandList.get(pos - 1);
		}
		return "";
	}

	/*
	 * Adds a new command to the bottom of the list, resets list position.
	 * 
	 * @param command the String value of a command
	 */
	void addCommand(String command) {
		if (commandList.contains(command))
			commandList.remove(command);
		pos = 0;
		commandList.addLast(command);
		if (commandList.size() > maxSize) {
			commandList.removeFirst();
		}
	}

	/*
	 * Resets maximum size of command queue. Cuts off extra commands.
	 * 
	 * @param maxSize maximum size for the command queue
	 */
	void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
		while (maxSize < commandList.size()) {
			commandList.removeFirst();
		}
	}

	/*
	 * Resets instance.
	 * 
	 * @param maxSize maximum size for the command queue
	 */
	void reset(int maxSize) {
		this.maxSize = maxSize;
		commandList = new LinkedList<String>();
	}

}