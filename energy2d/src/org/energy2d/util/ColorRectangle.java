package org.energy2d.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.SystemColor;

import javax.swing.JComponent;

/**
 * @author Charles Xie
 * 
 */

public class ColorRectangle extends JComponent {

	public final static Color[] COLORS = { Color.black, Color.gray, Color.blue, Color.red, Color.green, Color.magenta };
	private int colorID = 0;
	private Color moreColor = Color.white;

	ColorRectangle() {
		setPreferredSize(new Dimension(60, 20));
		setBackground(Color.white);
	}

	public ColorRectangle(int id, Color c) {
		setColorID(id);
		setMoreColor(c);
		setBackground(Color.white);
	}

	boolean isDefaultColor(Color c) {
		for (Color x : COLORS) {
			if (x.equals(c))
				return true;
		}
		return false;
	}

	public void setMoreColor(Color c) {
		moreColor = c;
	}

	public void setMoreColor(int r, int g, int b) {
		moreColor = new Color(r, g, b);
	}

	public Color getMoreColor() {
		return moreColor;
	}

	public void setColorID(int id) {
		colorID = id;
		if (colorID < 0)
			colorID = 0;
	}

	public int getColorID() {
		return colorID;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		update(g);
	}

	public void update(Graphics g) {

		int width = getWidth();
		int height = getHeight();

		g.setColor(getBackground());
		g.fillRect(0, 0, width, height);
		g.setColor(Color.gray);
		g.drawRect(3, 3, width - 6, height - 6);

		if (colorID < COLORS.length) {
			g.setColor(COLORS[colorID]);
			g.fillRect(4, 4, width - 7, height - 7);
		} else if (colorID == ColorComboBox.INDEX_MORE_COLOR) {
			g.setColor(moreColor);
			g.fillRect(4, 4, width - 7, height - 7);
		} else if (colorID == ColorComboBox.INDEX_COLOR_CHOOSER) {
			g.setColor(getBackground().equals(SystemColor.textHighlight) ? SystemColor.textHighlightText : SystemColor.textText);
			FontMetrics fm = g.getFontMetrics();
			String s = "More...";
			int w = fm.stringWidth(s);
			g.drawString(s, (width - w) >> 1, 14);
		} else if (colorID == ColorComboBox.INDEX_HEX_INPUTTER) {
			g.setColor(getBackground().equals(SystemColor.textHighlight) ? SystemColor.textHighlightText : SystemColor.textText);
			FontMetrics fm = g.getFontMetrics();
			String s = "Hex...";
			int w = fm.stringWidth(s);
			g.drawString(s, (width - w) >> 1, 14);
		}

	}

}
