package org.energy2d.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import javax.swing.JComponent;

/**
 * @author Charles Xie
 * 
 */

class SpotlightRenderer {

	private Stroke stroke1 = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[] { 1.5f }, 0);
	private Stroke stroke2 = new BasicStroke(4);
	private Color color1 = Color.yellow;
	private Color color2 = new Color(20, 255, 20, 128);
	private int nx;
	private int ny;
	private int size = 3;

	public SpotlightRenderer(int nx, int ny) {
		this.nx = nx;
		this.ny = ny;
	}

	void render(JComponent c, Graphics2D g, int x, int y) {

		float dx = (float) c.getWidth() / (float) nx;
		float dy = (float) c.getHeight() / (float) ny;
		int ix = Math.round(x / dx);
		int iy = Math.round(y / dy);

		Color oldColor = g.getColor();
		Stroke oldStroke = g.getStroke();
		g.setColor(color1);
		g.setStroke(stroke1);

		int k;
		int rx = Math.round(size * dx);
		int ry = Math.round(size * dy);
		for (int i = 0; i < nx; i++) {
			if (i > ix - size && i < ix + size) {
				k = Math.round(i * dx);
				g.drawLine(k, y - ry, k, y + ry);
			}
		}
		for (int i = 0; i < ny; i++) {
			if (i > iy - size && i < iy + size) {
				k = Math.round(i * dy);
				g.drawLine(x - rx, k, x + rx, k);
			}
		}

		g.setColor(Color.white);
		g.fillOval(Math.round(ix * dx - 3), Math.round(iy * dy - 3), 6, 6);

		g.setColor(color2);
		g.setStroke(stroke2);
		g.drawRect(x - rx, y - ry, 2 * rx, 2 * ry);

		g.setColor(oldColor);
		g.setStroke(oldStroke);

	}

}
