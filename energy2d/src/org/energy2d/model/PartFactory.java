package org.energy2d.model;

/**
 * Make some custom shapes
 * 
 * @author Charles Xie
 * 
 */
public class PartFactory {

	private Model2D model;

	public PartFactory(Model2D model) {
		this.model = model;
	}

	public void addParabola(float a, float b) {
		int nx = model.getNx();
		float lx = model.getLx();
		float ly = model.getLy();
		float[] x = new float[nx * 2];
		float[] y = new float[nx * 2];
		for (int i = 0; i < nx; i++) {
			x[i] = lx / nx * i;
			y[i] = ly - a * (x[i] - lx * 0.5f) * (x[i] - lx * 0.5f);
			x[2 * nx - 1 - i] = x[i];
			y[2 * nx - 1 - i] = y[i] + b;
		}
		model.addPolygonPart(x, y);
	}

	public void addStair(float x, float y, float w, float h, int n) {
		float dx = w / n;
		float dy = h / n;
		float[] sx = new float[n * 2 + 3];
		float[] sy = new float[n * 2 + 3];
		for (int i = 0; i < n; i++) {
			sx[i * 2] = x + i * dx;
			sy[i * 2] = y + i * dy;
			sx[i * 2 + 1] = x + (i + 1) * dx;
			sy[i * 2 + 1] = y + i * dy;
		}
		sx[n * 2] = x + w;
		sy[n * 2] = y + h;
		sx[n * 2 + 1] = x + w - dx;
		sy[n * 2 + 1] = y + h;
		sx[n * 2 + 2] = x;
		sy[n * 2 + 2] = y + dy;
		model.addPolygonPart(sx, sy);
	}

}
