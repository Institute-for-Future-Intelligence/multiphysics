package org.energy2d.system;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.energy2d.event.ScriptEvent;
import org.energy2d.event.ScriptListener;

/**
 * @author Charles Xie
 * 
 */

class ScriptDialog extends JDialog implements EnterListener, ScriptListener {

	private ConsoleTextPane console;
	private System2D box;

	ScriptDialog(System2D s2d) {

		super(JOptionPane.getFrameForComponent(s2d.view), "Script Console", false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		box = s2d;

		console = new ConsoleTextPane(this);
		console.setPreferredSize(new Dimension(500, 400));
		console.setBorder(BorderFactory.createLoweredBevelBorder());
		console.setPrompt();
		console.appendNewline();
		console.setPrompt();
		add(new JScrollPane(console), BorderLayout.CENTER);

		JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		add(p, BorderLayout.SOUTH);

		JButton button = new JButton("Clear");
		button.setToolTipText("Clear the console");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clear();
			}
		});
		p.add(button);

		button = new JButton("Close");
		button.setToolTipText("Close the console");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				box.getScripter().removeScriptListener(ScriptDialog.this);
				dispose();
			}
		});
		p.add(button);

		box.getScripter().addScriptListener(this);

	}

	public void scriptEcho(String strEcho) {
		if (strEcho != null)
			console.outputEcho(strEcho);
	}

	public void scriptStatus(String strStatus) {
		if (strStatus != null)
			console.outputStatus(strStatus);
	}

	public void notifyScriptTermination(String strMsg, int msWalltime) {
		if (strMsg != null)
			console.outputError(strMsg);
	}

	private void clear() {
		console.clearContent();
		console.appendNewline();
		console.setPrompt();
	}

	private void executeCommand() {
		String strCommand = console.getCommandString().trim();
		if (strCommand.length() == 0)
			return;
		console.requestFocusInWindow();
		if (strCommand.equalsIgnoreCase("clear") || strCommand.equalsIgnoreCase("cls")) {
			clear();
			return;
		}
		console.appendNewline();
		console.setPrompt();
		String strErrorMessage = box.runNativeScript(strCommand);
		if (strErrorMessage != null)
			console.outputError(strErrorMessage);
	}

	public void enterPressed() {
		executeCommand();
	}

	public void outputScriptResult(final ScriptEvent e) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				switch (e.getStatus()) {
				case ScriptEvent.FAILED:
				case ScriptEvent.HARMLESS:
					console.outputError(e.getDescription());
					break;
				case ScriptEvent.SUCCEEDED:
					console.outputEcho(e.getDescription());
					break;
				}
			}
		});
	}

}