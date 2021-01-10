package org.energy2d.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

/**
 * @author Charles Xie
 * 
 */
class ColorPalette {

	// the following color scales model after FLIR I-series IR cameras
	private final static short[][] RAINBOW_RGB = { { 0, 0, 128 }, { 20, 50, 120 }, { 20, 100, 200 }, { 10, 150, 150 }, { 120, 180, 50 }, { 220, 200, 10 }, { 240, 160, 36 }, { 225, 50, 50 }, { 230, 85, 110 }, { 250, 250, 250 }, { 255, 255, 255 } };
	private final static short[][] IRON_RGB = { { 40, 20, 100 }, { 80, 20, 150 }, { 150, 20, 150 }, { 200, 50, 120 }, { 220, 80, 80 }, { 230, 120, 30 }, { 240, 200, 20 }, { 240, 220, 80 }, { 255, 255, 125 }, { 250, 250, 250 }, { 255, 255, 255 } };
	private final static short[][] GRAY_RGB = { { 50, 50, 50 }, { 75, 75, 75 }, { 100, 100, 100 }, { 125, 125, 125 }, { 150, 150, 150 }, { 175, 175, 175 }, { 200, 200, 200 }, { 225, 225, 225 }, { 250, 250, 250 }, { 255, 255, 255 } };

	private short[][] rgbScale;
	private Font font = new Font(null, Font.PLAIN | Font.BOLD, 8);
	// relative to the width and height of the view
	private float rx, ry, rw, rh;
	private int labelCount = 5;
	private int w, h, x, y;
	private boolean fahrenheitUsed;

	ColorPalette(short[][] rgbScale) {
		this.rgbScale = rgbScale;
	}

	static short[][] getRgbArray(byte type) {
		switch (type) {
		case View2D.RAINBOW:
			return RAINBOW_RGB;
		case View2D.IRON:
			return IRON_RGB;
		case View2D.GRAY:
			return GRAY_RGB;
		default:
			return RAINBOW_RGB;
		}
	}

	void setUseFahrenheit(boolean b) {
		fahrenheitUsed = b;
	}

	void setRect(float rx, float ry, float rw, float rh) {
		this.rx = rx;
		this.ry = ry;
		this.rw = rw;
		this.rh = rh;
	}

	Rectangle2D.Float getRect() {
		return new Rectangle2D.Float(rx, ry, rw, rh);
	}

	private int getColor(int i) {
		float v = (float) i * (float) rgbScale.length / (float) Math.max(w, h);
		if (v > rgbScale.length - 2)
			v = rgbScale.length - 2;
		else if (v < 0)
			v = 0;
		int iv = (int) v;
		v -= iv;
		int rc = (int) (rgbScale[iv][0] * (1 - v) + rgbScale[iv + 1][0] * v);
		int gc = (int) (rgbScale[iv][1] * (1 - v) + rgbScale[iv + 1][1] * v);
		int bc = (int) (rgbScale[iv][2] * (1 - v) + rgbScale[iv + 1][2] * v);
		return (255 << 24) | (rc << 16) | (gc << 8) | bc;
	}

	void render(JComponent c, Graphics2D g, float max, float min) {
		x = (int) (rx * c.getWidth());
		y = (int) (ry * c.getHeight());
		w = (int) (rw * c.getWidth());
		h = (int) (rh * c.getHeight());
		if (h == 0) {
			h = 20;
		}
		if (w == 0) {
			w = c.getWidth() - 100;
		}
		if (x == 0) {
			x = 50;
		}
		if (y == 0) {
			y = 20;
		}
		Font oldFont = g.getFont();
		g.setFont(font);
		if (h > w) {
			for (int i = 0; i < h; i++) {
				g.setColor(new Color(getColor(i)));
				g.drawLine(x, y + i, x + w, y + i);
			}
			g.setColor(Color.white);
			g.draw3DRect(x, y, w, h, true);
			String s = null;
			for (int i = 0; i < labelCount + 1; i++) {
				if (fahrenheitUsed) {
					s = Math.round((min + i * (max - min) / labelCount) * 1.8 + 32) + "\u2109";
				} else {
					s = (int) (min + i * (max - min) / labelCount) + "\u2103";
				}
				g.drawString(s, x + w + 15, y + h * (float) i / (float) labelCount + 2.5f);
			}
		} else {
			for (int i = 0; i < w; i++) {
				g.setColor(new Color(getColor(i)));
				g.drawLine(x + i, y, x + i, y + h);
			}
			g.setColor(Color.white);
			g.draw3DRect(x, y, w, h, true);
			String s = null;
			FontMetrics fm = g.getFontMetrics();
			for (int i = 0; i < labelCount + 1; i++) {
				if (fahrenheitUsed) {
					s = Math.round((min + i * (max - min) / labelCount) * 1.8 + 32) + "\u2109";
				} else {
					s = (int) (min + i * (max - min) / labelCount) + "\u2103";
				}
				g.drawString(s, x + w * (float) i / (float) labelCount - fm.stringWidth(s) * 0.5f, y + h + 15);
			}
		}
		g.setFont(oldFont);
	}

}
