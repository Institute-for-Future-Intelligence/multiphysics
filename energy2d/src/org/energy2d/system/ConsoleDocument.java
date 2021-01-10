package org.energy2d.system;

import java.awt.Color;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;


/**
 * @author Charles Xie
 * 
 */

class ConsoleDocument extends DefaultStyledDocument {

	private ConsoleTextPane consoleTextPane;

	private static SimpleAttributeSet attError;
	private static SimpleAttributeSet attEcho;
	private static SimpleAttributeSet attPrompt;
	private static SimpleAttributeSet attUserInput;
	private static SimpleAttributeSet attStatus;

	// starts at 0, so first time isn't tracked (at least on Mac OS X)
	private Position positionBeforePrompt;
	// immediately after $, so this will track
	private Position positionAfterPrompt;
	// only still needed for the insertString override and replaceCommand
	private int offsetAfterPrompt;

	ConsoleDocument() {
		super();
		if (attError == null) {
			attError = new SimpleAttributeSet();
			StyleConstants.setForeground(attError, Color.red);
		}
		if (attPrompt == null) {
			attPrompt = new SimpleAttributeSet();
			StyleConstants.setForeground(attPrompt, Color.magenta);
		}
		if (attUserInput == null) {
			attUserInput = new SimpleAttributeSet();
			StyleConstants.setForeground(attUserInput, Color.black);
		}
		if (attEcho == null) {
			attEcho = new SimpleAttributeSet();
			StyleConstants.setForeground(attEcho, Color.blue);
			StyleConstants.setBold(attEcho, true);
		}
		if (attStatus == null) {
			attStatus = new SimpleAttributeSet();
			StyleConstants.setForeground(attStatus, Color.black);
			StyleConstants.setItalic(attStatus, true);
		}
	}

	void setConsoleTextPane(ConsoleTextPane consoleTextPane) {
		this.consoleTextPane = consoleTextPane;
	}

	/* Removes all content of the script window, and add a new prompt. */
	void clearContent() {
		try {
			super.remove(0, getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		setPrompt();
	}

	void setPrompt() {
		try {
			super.insertString(getLength(), "$ ", attPrompt);
			offsetAfterPrompt = getLength();
			positionBeforePrompt = createPosition(offsetAfterPrompt - 2);
			// after prompt should be immediately after $ otherwise tracks the
			// end
			// of the line (and no command will be found) at least on Mac OS X
			// it did.
			positionAfterPrompt = createPosition(offsetAfterPrompt - 1);
			consoleTextPane.setCaretPosition(offsetAfterPrompt);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	/*
	 * it looks like the positionBeforePrompt does not track when it started out
	 * as 0 and a insertString at location 0 occurs. It may be better to track
	 * the position after the prompt in stead
	 */
	void outputBeforePrompt(String str, SimpleAttributeSet attribute) {
		try {
			Position caretPosition = createPosition(consoleTextPane.getCaretPosition());
			super.insertString(positionBeforePrompt.getOffset(), str + "\n", attribute);
			// keep the offsetAfterPrompt in sync
			offsetAfterPrompt = positionBeforePrompt.getOffset() + 2;
			consoleTextPane.setCaretPosition(caretPosition.getOffset());
		} catch (BadLocationException e) {
			e.printStackTrace(System.err);
		}
	}

	void outputError(String strError) {
		outputBeforePrompt(strError, attError);
	}

	void outputErrorForeground(String strError) {
		try {
			super.insertString(getLength(), strError + "\n", attError);
			consoleTextPane.setCaretPosition(getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	void outputEcho(String strEcho) {
		outputBeforePrompt(strEcho, attEcho);
	}

	void outputStatus(String strStatus) {
		outputBeforePrompt(strStatus, attStatus);
	}

	void appendNewline() {
		try {
			super.insertString(getLength(), "\n", attUserInput);
			consoleTextPane.setCaretPosition(getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * override the insertString to make sure everything typed ends up at the
	 * end or in the 'command line' using the proper font, and the newline is
	 * processed.
	 */
	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
		int ichNewline = str.indexOf('\n');
		if (ichNewline > 0)
			str = str.substring(0, ichNewline);
		if (ichNewline != 0) {
			if (offs < offsetAfterPrompt) {
				offs = getLength();
			}
			super.insertString(offs, str, attUserInput);
			consoleTextPane.setCaretPosition(offs + str.length());
		}
		if (ichNewline >= 0) {
			consoleTextPane.enterPressed();
		}
	}

	String getCommandString() {
		String strCommand = "";
		try {
			int cmdStart = positionAfterPrompt.getOffset();
			// skip unnecessary leading spaces in the command.
			strCommand = getText(cmdStart, getLength() - cmdStart).trim();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return strCommand;
	}

	public void remove(int offs, int len) throws BadLocationException {
		if (offs < offsetAfterPrompt) {
			len -= offsetAfterPrompt - offs;
			if (len <= 0)
				return;
			offs = offsetAfterPrompt;
		}
		super.remove(offs, len);
		// consoleTextPane.setCaretPosition(offs);
	}

	public void replace(int offs, int length, String str, AttributeSet attrs)
			throws BadLocationException {
		if (offs < offsetAfterPrompt) {
			if (offs + length < offsetAfterPrompt) {
				offs = getLength();
				length = 0;
			} else {
				length -= offsetAfterPrompt - offs;
				offs = offsetAfterPrompt;
			}
		}
		super.replace(offs, length, str, attUserInput);
		// consoleTextPane.setCaretPosition(offs + str.length());
	}

	/*
	 * Replaces current command on script.
	 * 
	 * @param newCommand new command value @throws BadLocationException
	 */
	void replaceCommand(String newCommand) throws BadLocationException {
		replace(offsetAfterPrompt, getLength() - offsetAfterPrompt, newCommand, attUserInput);
	}

}