package org.energy2d.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

/**
 * @author Charles Xie
 * 
 */
class ScalarDistributionRenderer {

	private BufferedImage image;
	private int[] pixels;
	private int w, h;
	private float min = 0, max = 50;
	private float scale;
	private short[][] rgbScale;
	private boolean smooth = true;

	ScalarDistributionRenderer(short[][] rgbScale, float min, float max) {
		this.rgbScale = rgbScale;
		this.min = min;
		this.max = max;
		scale = rgbScale.length / (max - min);
	}

	void setSmooth(boolean smooth) {
		this.smooth = smooth;
	}

	boolean isSmooth() {
		return smooth;
	}

	void setMaximum(float max) {
		this.max = max;
		scale = rgbScale.length / (max - min);
	}

	float getMaximum() {
		return max;
	}

	void setMinimum(float min) {
		this.min = min;
		scale = rgbScale.length / (max - min);
	}

	float getMinimum() {
		return min;
	}

	int getRGB(int x, int y) {
		if (image == null)
			return Color.white.getRGB();
		if (x >= image.getWidth())
			x = image.getWidth() - 1;
		if (x < 0)
			x = 0;
		if (y >= image.getHeight())
			y = image.getHeight() - 1;
		if (y < 0)
			y = 0;
		return image.getRGB(x, y);
	}

	int getColor(float value) {
		float v = (value - min) * scale;
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

	void render(View2D view, Graphics2D g, float[][] distribution) {
		if (!view.isVisible())
			return;

		w = view.getWidth();
		h = view.getHeight();
		createImage(w, h, view);

		int m = distribution.length;
		int n = distribution[0].length;

		float dx = (float) m / (float) w;
		float dy = (float) n / (float) h;
		float x, y;

		int rc = 0, gc = 0, bc = 0, iv;
		float v;

		if (smooth) {
			int i0, j0, i1, j1;
			float s0, s1, t0, t1;
			for (int i = 0; i < w; i++) {
				x = i * dx;
				i0 = (int) x;
				i1 = i0 + 1;
				if (i1 > m - 1)
					i1 = m - 1;
				s1 = x - i0;
				s0 = 1 - s1;
				for (int j = 0; j < h; j++) {
					y = j * dy;
					j0 = (int) y;
					j1 = j0 + 1;
					if (j1 > n - 1)
						j1 = n - 1;
					t1 = y - j0;
					t0 = 1 - t1;
					v = (s0 * (t0 * distribution[i0][j0] + t1 * distribution[i0][j1]) + s1 * (t0 * distribution[i1][j0] + t1 * distribution[i1][j1]) - min) * scale;
					if (v > rgbScale.length - 2)
						v = rgbScale.length - 2;
					else if (v < 0)
						v = 0;
					iv = (int) v;
					v -= iv;
					rc = (int) (rgbScale[iv][0] * (1 - v) + rgbScale[iv + 1][0] * v);
					gc = (int) (rgbScale[iv][1] * (1 - v) + rgbScale[iv + 1][1] * v);
					bc = (int) (rgbScale[iv][2] * (1 - v) + rgbScale[iv + 1][2] * v);
					pixels[i + j * w] = (255 << 24) | (rc << 16) | (gc << 8) | bc;
				}
			}
		} else {
			int i0, j0;
			for (int i = 0; i < w; i++) {
				i0 = (int) (i * dx);
				for (int j = 0; j < h; j++) {
					j0 = (int) (j * dy);
					v = (distribution[i0][j0] - min) * scale;
					if (v > rgbScale.length - 2)
						v = rgbScale.length - 2;
					else if (v < 0)
						v = 0;
					iv = (int) v;
					v -= iv;
					rc = (int) (rgbScale[iv][0] * (1 - v) + rgbScale[iv + 1][0] * v);
					gc = (int) (rgbScale[iv][1] * (1 - v) + rgbScale[iv + 1][1] * v);
					bc = (int) (rgbScale[iv][2] * (1 - v) + rgbScale[iv + 1][2] * v);
					pixels[i + j * w] = (255 << 24) | (rc << 16) | (gc << 8) | bc;
				}
			}
		}

		image.setRGB(0, 0, w, h, pixels, 0, w);
		g.drawImage(image, 0, 0, view);

	}

	private void createImage(int w, int h, JComponent c) {
		if (image != null) {
			if (w != image.getWidth(c) || h != image.getHeight(c)) {
				image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
				pixels = new int[w * h];
			}
		} else {
			image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			pixels = new int[w * h];
		}
	}

}
