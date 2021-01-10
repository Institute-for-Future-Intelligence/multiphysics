package org.energy2d.util;

import java.util.LinkedHashMap;
import static org.energy2d.util.EscapeCharacters.*;

/**
 * replace illegal characters in the text by entity references and preserve the link breaks.
 * 
 * @author Charles Xie
 * 
 */
public class XmlCharacterEncoder {

	private final static char LESS_THAN = '<';
	private final static char GREATER_THAN = '>';
	private final static char AMPERSAND = '&';
	private final static char APOSTROPHE = '\'';
	private final static char QUOTATION = '"';
	private final static char LINE_BREAK = '\n';

	private LinkedHashMap<Integer, Character> store;

	public XmlCharacterEncoder() {
		store = new LinkedHashMap<Integer, Character>();
	}

	public String encode(String text) {

		if (text == null)
			return null;

		for (int i = 0; i < text.length(); i++) {
			switch (text.charAt(i)) {
			case LESS_THAN:
				store.put(i, LESS_THAN);
				break;
			case GREATER_THAN:
				store.put(i, GREATER_THAN);
				break;
			case AMPERSAND:
				store.put(i, AMPERSAND);
				break;
			case APOSTROPHE:
				store.put(i, APOSTROPHE);
				break;
			case QUOTATION:
				store.put(i, QUOTATION);
				break;
			case LINE_BREAK:
				store.put(i, LINE_BREAK);
			}
		}

		if (!store.isEmpty()) {
			StringBuffer sb = new StringBuffer(text);
			int cumu = 0, del = 0;
			for (Integer index : store.keySet()) {
				Character character = store.get(index);
				switch (character.charValue()) {
				case LESS_THAN:
					del = index + cumu;
					sb.deleteCharAt(del);
					sb.insert(del, LESS_THAN_ER);
					cumu += LESS_THAN_ER.length() - 1;
					break;
				case GREATER_THAN:
					del = index + cumu;
					sb.deleteCharAt(del);
					sb.insert(del, GREATER_THAN_ER);
					cumu += GREATER_THAN_ER.length() - 1;
					break;
				case AMPERSAND:
					del = index + cumu;
					sb.deleteCharAt(del);
					sb.insert(del, AMPERSAND_ER);
					cumu += AMPERSAND_ER.length() - 1;
					break;
				case APOSTROPHE:
					del = index + cumu;
					sb.deleteCharAt(del);
					sb.insert(del, APOSTROPHE_ER);
					cumu += APOSTROPHE_ER.length() - 1;
					break;
				case QUOTATION:
					del = index + cumu;
					sb.deleteCharAt(del);
					sb.insert(del, QUOTATION_ER);
					cumu += QUOTATION_ER.length() - 1;
					break;
				case LINE_BREAK:
					del = index + cumu;
					sb.deleteCharAt(del);
					sb.insert(del, LINE_BREAK_ER);
					cumu += LINE_BREAK_ER.length() - 1;
					break;
				}
			}
			text = sb.toString();
		}

		return text;

	}

}