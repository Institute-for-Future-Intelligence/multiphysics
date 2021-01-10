package org.energy2d.system;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;

import javax.swing.text.BadLocationException;

import org.energy2d.util.PastableTextPane;


/**
 * @author Charles Xie
 * 
 */

class ConsoleTextPane extends PastableTextPane {

	private CommandHistory commandHistory = new CommandHistory(20);
	private ConsoleDocument consoleDoc;
	private EnterListener enterListener;

	ConsoleTextPane(EnterListener enterListener) {
		super(new ConsoleDocument());
		consoleDoc = (ConsoleDocument) getDocument();
		consoleDoc.setConsoleTextPane(this);
		this.enterListener = enterListener;
	}

	public String getCommandString() {
		String cmd = consoleDoc.getCommandString();
		commandHistory.addCommand(cmd);
		return cmd;
	}

	public void setPrompt() {
		consoleDoc.setPrompt();
	}

	public void appendNewline() {
		consoleDoc.appendNewline();
	}

	public void outputError(final String strError) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				consoleDoc.outputError(strError);
			}
		});
	}

	public void outputErrorForeground(final String strError) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				consoleDoc.outputErrorForeground(strError);
			}
		});
	}

	public void outputEcho(final String strEcho) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				consoleDoc.outputEcho(strEcho);
			}
		});
	}

	public void outputStatus(final String strStatus) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				consoleDoc.outputStatus(strStatus);
			}
		});
	}

	public void enterPressed() {
		if (enterListener != null)
			enterListener.enterPressed();
	}

	public void clearContent() {
		consoleDoc.clearContent();
	}

	/**
	 * Custom key event processing for command history implementation. Captures
	 * key up and key down * strokes to call command history and redefines the
	 * same events with control down to allow caret * vertical shift.
	 */
	protected void processKeyEvent(KeyEvent e) {
		// Id Control key is down, captures events does command history recall
		// and inhibits caret vertical shift.
		if (e.getKeyCode() == KeyEvent.VK_UP && e.getID() == KeyEvent.KEY_PRESSED
				&& !e.isControlDown()) {
			recallCommand(true);
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN && e.getID() == KeyEvent.KEY_PRESSED
				&& !e.isControlDown()) {
			recallCommand(false);
		}
		// If Control key is down, redefines the event as if it where a key up
		// or key down stroke without
		// modifiers. This allows to move the caret up and down with no command
		// history recall.
		else if ((e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP)
				&& e.getID() == KeyEvent.KEY_PRESSED && e.isControlDown()) {
			super.processKeyEvent(new KeyEvent((Component) e.getSource(), e.getID(), e.getWhen(),
					0, // No modifiers
					e.getKeyCode(), e.getKeyChar(), e.getKeyLocation()));
		}
		// Standard processing for other events.
		else {
			super.processKeyEvent(e);
		}
	}

	/**
	 * Recall command histoy.
	 * 
	 * @param up
	 *            - history up or down
	 */
	private final void recallCommand(boolean up) {
		String cmd = up ? commandHistory.getCommandUp() : commandHistory.getCommandDown();
		try {
			consoleDoc.replaceCommand(cmd);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

}