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

class GridRenderer {

	final static byte X_LINE = 0;
	final static byte Y_LINE = 1;
	final static int MIN_GRID_SIZE = 2;
	final static int MAX_GRID_SIZE = 25;

	private Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[] { 1.5f }, 0);
	private Color color = new Color(128, 128, 225, 128);
	private int nx;
	private int ny;
	private int gridSize = 10;

	GridRenderer(int nx, int ny) {
		this.nx = nx;
		this.ny = ny;
	}

	void setGridSize(int gridSize) {
		this.gridSize = gridSize;
	}

	int getGridSize() {
		return gridSize;
	}

	byte onGridLine(JComponent c, int x, int y) {
		float dx = (float) c.getWidth() / (float) nx;
		float dy = (float) c.getHeight() / (float) ny;
		int k;
		for (int i = 0; i < nx; i += gridSize) {
			k = Math.round(i * dx);
			if (Math.abs(x - k) < 5)
				return Y_LINE;
		}
		for (int i = 0; i < ny; i += gridSize) {
			k = Math.round(i * dy);
			if (Math.abs(y - k) < 5)
				return X_LINE;
		}
		return -1;
	}

	void render(JComponent c, Graphics2D g) {

		int w = c.getWidth();
		int h = c.getHeight();
		float dx = (float) w / (float) nx;
		float dy = (float) h / (float) ny;

		Color oldColor = g.getColor();
		Stroke oldStroke = g.getStroke();
		g.setColor(color);
		g.setStroke(stroke);

		int k;
		for (int i = 0; i < nx; i += gridSize) {
			k = Math.round(i * dx);
			g.drawLine(k, 0, k, h);
		}
		for (int i = 0; i < ny; i += gridSize) {
			k = Math.round(i * dy);
			g.drawLine(0, k, w, k);
		}

		g.setColor(oldColor);
		g.setStroke(oldStroke);

	}

}
