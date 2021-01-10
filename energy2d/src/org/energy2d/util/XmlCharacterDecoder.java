package org.energy2d.util;

import static org.energy2d.util.EscapeCharacters.*;

/**
 * restores entity references to characters and the line breaks.
 * 
 * @author Charles Xie
 * 
 */

public class XmlCharacterDecoder {

	private final static String LESS_THAN = "<";
	private final static String GREATER_THAN = ">";
	private final static String AMPERSAND = "&";
	private final static String APOSTROPHE = "\'";
	private final static String QUOTATION = "\"";
	private final static String LINE_BREAK = "\n";

	public XmlCharacterDecoder() {
	}

	public String decode(String text) {

		if (text == null)
			return null;

		return text.replaceAll(LESS_THAN_ER, LESS_THAN).replaceAll(GREATER_THAN_ER, GREATER_THAN).replaceAll(AMPERSAND_ER, AMPERSAND).replaceAll(APOSTROPHE_ER, APOSTROPHE).replaceAll(QUOTATION_ER, QUOTATION).replaceAll(LINE_BREAK_ER, LINE_BREAK);

	}

}
