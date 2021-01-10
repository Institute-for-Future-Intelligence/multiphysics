package org.energy2d.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import org.energy2d.math.MathUtil;

public class ContourMap {

	private float resolution = 1;
	private Color color = Color.black;
	private float[][] func;
	private int nx, ny;
	private Dimension size;
	private Point2D.Float pa, pb;
	private Line2D.Float line;
	private int step = 1;

	public ContourMap() {
		pa = new Point2D.Float();
		pb = new Point2D.Float();
		line = new Line2D.Float();
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public void setResolution(float resolution) {
		this.resolution = resolution;
	}

	public float getResolution() {
		return resolution;
	}

	public void render(Graphics2D g, Dimension size, float[][] func) {
		this.func = func;
		this.nx = func.length;
		this.ny = func[0].length;
		this.size = size;
		g.setColor(color);
		for (int x = 0; x < nx - step; x += step) {
			for (int y = 0; y < ny - step; y += step) {
				connect(g, x, y, x + step, y, x, y + step, x + step, y + step);
				connect(g, x, y, x + step, y, x, y, x, y + step);
				connect(g, x, y, x + step, y, x + step, y, x + step, y + step);
				connect(g, x, y, x, y + step, x + step, y, x + step, y + step);
				connect(g, x, y, x, y + step, x, y + step, x + step, y + step);
				connect(g, x + step, y, x + step, y + step, x, y + step, x + step, y + step);
			}
		}
	}

	// draw a contour line between (x1, y1) - (x2, y2) and (x3, y3) - (x4, y4) if applicable
	private void connect(Graphics2D g, int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
		float f1 = func[x1][y1];
		float f2 = func[x2][y2];
		float f3 = func[x3][y3];
		float f4 = func[x4][y4];
		float fmin = Math.min(Math.min(f1, f2), Math.min(f3, f4));
		float fmax = Math.max(Math.max(f1, f2), Math.max(f3, f4));
		int imin = (int) (fmin / resolution);
		int imax = (int) (fmax / resolution);
		if (imin != imax) {
			float v;
			for (int i = imin; i <= imax; i++) {
				v = i * resolution;
				if (MathUtil.between(f1, f2, v) && MathUtil.between(f3, f4, v)) {
					interpolate(f1, f2, x1, y1, x2, y2, v, pa);
					interpolate(f3, f4, x3, y3, x4, y4, v, pb);
					line.setLine(pa, pb);
					g.draw(line);
				}
			}
		}
	}

	private void interpolate(float f1, float f2, int x1, int y1, int x2, int y2, float v, Point2D.Float p) {
		float r2 = (v - f1) / (f2 - f1);
		float r1 = 1 - r2;
		float h = 0.5f * step;
		p.x = ((x1 + h) * r1 + (x2 + h) * r2) * size.width / (float) nx;
		p.y = ((y1 + h) * r1 + (y2 + h) * r2) * size.height / (float) ny;
	}

}
