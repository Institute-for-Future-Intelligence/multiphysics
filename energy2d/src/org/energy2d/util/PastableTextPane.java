package org.energy2d.util;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;

/**
 * @author Charles Xie
 * 
 */

public class PastableTextPane extends JTextPane {

	protected TextComponentPopupMenu popupMenu;

	protected MouseAdapter mouseAdapter = new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
			if (MiscUtil.isRightClick(e)) {
				PastableTextPane.this.requestFocusInWindow();
				if (popupMenu == null)
					popupMenu = new TextComponentPopupMenu(PastableTextPane.this);
				popupMenu.show(PastableTextPane.this, e.getX(), e.getY());
			}
		}
	};

	public PastableTextPane() {
		super();
		addMouseListener(mouseAdapter);
	}

	public PastableTextPane(StyledDocument doc) {
		super(doc);
		addMouseListener(mouseAdapter);
	}

}