package org.energy2d.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Arrays;

/**
 * @author Charles Xie
 * 
 */
public class FieldLines {

	private int arrowSpacing = 16; // in pixels
	private int fluxLineSpacing = 2 * arrowSpacing; // in pixels
	private int nx, ny;
	private float[][] func, funx, funy;
	private Dimension size;
	private float dx, dy;
	private float vx, vy;
	private int arrowDirection = 1;
	private int numColors = 16;
	private Color[] spectrum;
	private Color minColor = new Color(0.0f, 0.0f, 1.0f);
	private Color maxColor = new Color(1.0f, 1.0f, 1.0f);
	private Color color;
	private float minimumMagnitude = 0.0001f;
	private float arrowLength = .75f;

	// a 2D array of flags, each corresponding to a region of the back buffer
	private boolean[][] map;

	public FieldLines() {
		spectrum = new Color[numColors];
		for (int i = 0; i < numColors; i++) {
			float u = i / (float) (numColors - 1);
			spectrum[i] = new Color(Math.round((1 - u) * minColor.getRed() + u * maxColor.getRed()), Math.round((1 - u) * minColor.getGreen() + u * maxColor.getGreen()), Math.round((1 - u) * minColor.getBlue() + u * maxColor.getBlue()));
		}
	}

	public void setArrowSpacing(int arrowSpacing) {
		this.arrowSpacing = arrowSpacing;
	}

	public void setFluxLineSpacing(int fluxLineSpacing) {
		this.fluxLineSpacing = fluxLineSpacing;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setMinimumColor(Color c) {
		minColor = c;
	}

	public void setMaximumColor(Color c) {
		maxColor = c;
	}

	// draw field lines for a 2D vector function
	public void render(Graphics2D g, Dimension size, float[][] funx, float[][] funy) {

		this.funx = funx;
		this.funy = funy;
		this.nx = funx.length;
		this.ny = funx[0].length;
		this.size = size;
		dx = (float) size.width / (float) nx;
		dy = (float) size.height / (float) ny;
		int mx = size.width / fluxLineSpacing + 1;
		int my = size.height / fluxLineSpacing + 1;
		if (map == null || map.length != mx || map[0].length != my)
			map = new boolean[mx][my];
		for (int i = 0; i < map.length; i++)
			Arrays.fill(map[i], false);
		int maxLength = Math.max(size.width, size.height);

		float x = 0, y = 0;
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				if (!map[i][j]) {
					// place a seed point in the center of the region
					x = (i + 0.5f) * fluxLineSpacing;
					y = (j + 0.5f) * fluxLineSpacing;
					// draw flux lines forward and backward through the seed point
					drawFluxLineForVector(g, x, y, 1, maxLength);
					drawFluxLineForVector(g, x, y, -1, maxLength);
				}
			}
		}

	}

	/*
	 * @x, @y pixel location of point to start at
	 * 
	 * @sign +1 to travel with the field, -1 to travel against it
	 */
	private void drawFluxLineForVector(Graphics2D g, float x, float y, float sign, int maxLength) {

		int i, j;
		double magnitude = 0;
		float newX = 0, newY = 0;
		float arrowScale = sign * arrowLength * arrowSpacing;

		for (int k = 0; k < maxLength; k++) {

			i = Math.round(x / dx);
			j = Math.round(y / dy);
			if (i <= 0 || i >= nx - 1 || j <= 0 || j >= ny - 1)
				continue;

			vx = funx[i][j];
			vy = funy[i][j];
			magnitude = Math.hypot(vx, vy);
			if (magnitude < minimumMagnitude)
				break;
			vx *= sign;
			vy *= sign;
			vx /= magnitude;
			vy /= magnitude;

			newX = x + vx;
			newY = y + vy;
			g.setColor(color != null ? color : getColor(magnitude));
			g.drawLine(Math.round(x), Math.round(y), Math.round(newX), Math.round(newY));
			// every few pixels, draw an arrow
			if (k > 0 && (k % (5 * arrowSpacing) == 0)) {
				drawArrow(g, x, y, x + arrowScale * vx, y + arrowScale * vy);
			}

			x = newX;
			y = newY;
			if (x < 0 || x >= size.width || y < 0 || y >= size.height)
				// we're outside the image's boundaries
				break;

			// mark this part of the image as occupied by a flux line
			map[Math.round(x) / fluxLineSpacing][Math.round(y) / fluxLineSpacing] = true;
		}

	}

	// draw field lines for the gradient of a scalar function
	public void render(Graphics2D g, Dimension size, float[][] func, int arrowDirection) {

		this.func = func;
		this.nx = func.length;
		this.ny = func[0].length;
		this.size = size;
		this.arrowDirection = arrowDirection;
		dx = (float) size.width / (float) nx;
		dy = (float) size.height / (float) ny;
		int mx = size.width / fluxLineSpacing + 1;
		int my = size.height / fluxLineSpacing + 1;
		if (map == null || map.length != mx || map[0].length != my)
			map = new boolean[mx][my];
		for (int i = 0; i < map.length; i++)
			Arrays.fill(map[i], false);
		int maxLength = Math.max(size.width, size.height);

		float x = 0, y = 0;
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				if (!map[i][j]) {
					// place a seed point at the center of the region
					x = (i + 0.5f) * fluxLineSpacing;
					y = (j + 0.5f) * fluxLineSpacing;
					// draw flux lines forward and backward through the seed point
					drawFluxLineForScalar(g, x, y, 1, maxLength);
					drawFluxLineForScalar(g, x, y, -1, maxLength);
				}
			}
		}

	}

	/*
	 * @x, @y pixel location of point to start at
	 * 
	 * @sign +1 to travel with the field, -1 to travel against it
	 */
	private void drawFluxLineForScalar(Graphics2D g, float x, float y, float sign, int maxLength) {

		int i, j;
		double magnitude = 0;
		float newX = 0, newY = 0;
		float arrowScale = sign * arrowLength * arrowDirection * arrowSpacing;

		for (int k = 0; k < maxLength; k++) {

			i = Math.round(x / dx);
			j = Math.round(y / dy);
			if (i <= 0 || i >= nx - 1 || j <= 0 || j >= ny - 1)
				continue;

			vx = (func[i + 1][j] - func[i - 1][j]) / 2;
			vy = (func[i][j + 1] - func[i][j - 1]) / 2;
			vx *= sign;
			vy *= sign;
			magnitude = Math.hypot(vx, vy);
			if (magnitude < minimumMagnitude)
				break;
			vx /= magnitude;
			vy /= magnitude;

			newX = x + vx;
			newY = y + vy;
			g.setColor(color == null ? getColor(magnitude) : color);
			g.drawLine(Math.round(x), Math.round(y), Math.round(newX), Math.round(newY));
			// every few pixels, draw an arrow
			if (k > 0 && (k % (5 * arrowSpacing) == 0)) {
				drawArrow(g, x, y, x + arrowScale * vx, y + arrowScale * vy);
			}

			x = newX;
			y = newY;
			if (x < 0 || x >= size.width || y < 0 || y >= size.height)
				// we're outside the image's boundaries
				break;

			// mark this part of the image as occupied by a flux line
			map[Math.round(x) / fluxLineSpacing][Math.round(y) / fluxLineSpacing] = true;
		}

	}

	private Color getColor(double magnitude) {
		int colorIndex = (int) ((Math.log10(magnitude) + 3) / 3.6 * numColors);
		// clamp the result
		if (colorIndex < 0)
			colorIndex = 0;
		else if (colorIndex >= numColors)
			colorIndex = numColors - 1;
		return spectrum[colorIndex];
	}

	// (x1,y1) is the origin of the arrow; (x2,y2) is the tip of the arrow
	private static void drawArrow(Graphics2D g, float x1, float y1, float x2, float y2) {
		float dx = x2 - x1;
		float dy = y2 - y1;
		float f = 1 / 3.0f; // length of arrow head over length of arrow stem
		float f2 = 1 / 6.0f; // half-width of arrow head over length of arrow stem
		float x3 = x2 - f * dx - f2 * dy;
		float y3 = y2 - f * dy + f2 * dx;
		float x4 = x2 - f * dx + f2 * dy;
		float y4 = y2 - f * dy - f2 * dx;
		g.drawLine(Math.round(x1), Math.round(y1), Math.round(x2), Math.round(y2));
		g.drawLine(Math.round(x3), Math.round(y3), Math.round(x2), Math.round(y2));
		g.drawLine(Math.round(x4), Math.round(y4), Math.round(x2), Math.round(y2));
	}

}
