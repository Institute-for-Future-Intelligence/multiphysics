package org.energy2d.util;

import static java.util.regex.Pattern.*;

import java.util.regex.Pattern;

/**
 * @author Charles Xie
 * 
 */
public abstract class Scripter {

	protected final static String REGEX_SEPARATOR = "[,\\s&&[^\\r\\n]]";
	protected final static String REGEX_WHITESPACE = "[\\s&&[^\\r\\n]]";
	protected final static String REGEX_NONNEGATIVE_DECIMAL = "((\\d*\\.\\d+)|(\\d+\\.\\d*)|(\\d+))";

	protected final static Pattern COMMAND_BREAK = compile("(;|\\r?\\n|\\r)+");
	protected final static Pattern COMMENT = compile("^(//|/\\*)");
	protected final static Pattern BEEP = compile("(^(?i)beep\\b){1}");
	protected final static Pattern RESET = compile("(^(?i)reset\\b){1}");
	protected final static Pattern RELOAD = compile("(^(?i)reload\\b){1}");
	protected final static Pattern RUN = compile("(^(?i)run\\b){1}");
	protected final static Pattern STOP = compile("(^(?i)stop\\b){1}");
	protected final static Pattern INIT = compile("(^(?i)init\\b){1}");
	protected final static Pattern LOAD = compile("(^(?i)load\\b){1}");
	protected final static Pattern ADD = compile("(^(?i)add\\b){1}");
	protected final static Pattern REMOVE = compile("(^(?i)remove\\b){1}");
	protected final static Pattern DELAY = compile("(^(?i)delay\\b){1}");
	protected final static Pattern SET = compile("(^(?i)set\\b){1}");

	public void executeScript(String script) {
		String[] command = COMMAND_BREAK.split(script);
		if (command.length < 1)
			return;
		for (String ci : command) {
			ci = ci.trim();
			if (ci.equals(""))
				continue;
			if (COMMENT.matcher(ci).find())
				continue; // comments
			evalCommand(ci);
		}
	}

	protected abstract void evalCommand(String ci);

	public static float[] parseArray(final int n, String str) {
		str = str.trim();
		if (str.startsWith("("))
			str = str.substring(1);
		if (str.endsWith(")"))
			str = str.substring(0, str.length() - 1);
		String[] s = str.split(",");
		if (s.length != n)
			return null;
		return parseArray(n, s);
	}

	public static float[] parseArray(int n, String[] s) {
		if (n > s.length)
			return null;
		float[] x = new float[n];
		float z = 0;
		for (int i = 0; i < n; i++) {
			try {
				z = Float.parseFloat(s[i]);
			} catch (NumberFormatException e) {
				return null;
			}
			x[i] = z;
		}
		return x;
	}

}
