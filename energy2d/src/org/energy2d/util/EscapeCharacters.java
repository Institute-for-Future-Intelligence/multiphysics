package org.energy2d.util;

/**
 * Right now it does only the five entity references used by the XML syntax itself. No Unicode support is provided.
 * 
 * <pre>
 *  &lt; &lt; less than
 *  &gt; &gt; greater than
 *  &amp; &amp; ampersand 
 *  &amp;apos ' apostrophe 
 *  &quot; &quot; quotation mark
 * </pre>
 * 
 * @author Charles Xie
 * 
 */
class EscapeCharacters {

	final static String LESS_THAN_ER = "&lt;";
	final static String GREATER_THAN_ER = "&gt;";
	final static String AMPERSAND_ER = "&amp;";
	final static String APOSTROPHE_ER = "&apos;";
	final static String QUOTATION_ER = "&quot;";
	final static String LINE_BREAK_ER = "-linebreak-";

}
