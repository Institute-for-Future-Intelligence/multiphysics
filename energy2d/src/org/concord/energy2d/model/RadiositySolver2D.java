package org.concord.energy2d.model;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.concord.energy2d.math.Blob2D;
import org.concord.energy2d.math.Polygon2D;

/**
 * This solves the radiosity equation.
 * 
 * @author Charles Xie
 * 
 */
class RadiositySolver2D {

	private Model2D model;
	private List<Segment> segments = Collections.synchronizedList(new ArrayList<Segment>());
	private float patchSize;
	private float patchSizePercentage = 0.02f;
	private float[][] reflection, absorption;
	private int relaxationSteps = 5;

	RadiositySolver2D(Model2D model) {
		this.model = model;
	}

	void setPatchSizePercentage(float patchSizePercentage) {
		this.patchSizePercentage = patchSizePercentage;
	}

	float getPatchSizePercentage() {
		return patchSizePercentage;
	}

	void reset() {
	}

	void solve() {

		int n = segments.size();
		if (n <= 0)
			return;

		Segment s, s2;
		synchronized (segments) {

			// compute emission
			for (int i = 0; i < n; i++) {
				s = segments.get(i);
				Point2D.Float c = s.getCenter();
				// Stefan's Law (not exactly)
				float temp = model.getTemperatureAt(c.x, c.y, Sensor.FIVE_POINT) + 273;
				temp *= temp;
				s.emission = s.getPart().getEmissivity() * Model2D.STEFAN_CONSTANT * temp * temp;
				temp = model.getBackgroundTemperature() + 273;
				temp *= temp;
				s.emission -= Model2D.STEFAN_CONSTANT * temp * temp;
			}
			// apply relaxation
			for (int k = 0; k < relaxationSteps; k++) {
				for (int i = 0; i < n; i++) {
					s = segments.get(i);
					s.radiation = s.emission;
					s.absorption = 0;
					for (int j = 0; j < n; j++) {
						if (j != i) {
							s2 = segments.get(j);
							s.radiation -= reflection[i][j] * s2.radiation;
							s.absorption += absorption[i][j] * s2.radiation;
						}
					}
					s.radiation /= reflection[i][i];
				}
			}

			float gx = model.getNx() / model.getLx();
			float gy = model.getNy() / model.getLy();
			for (int i = 0; i < n; i++) {
				s = segments.get(i);
				float dx = Math.abs(s.x2 - s.x1);
				float dy = Math.abs(s.y2 - s.y1);
				int m = (int) Math.max(dx * gx, dy * gy);
				if (m > 1) {
					float r = (s.absorption - s.radiation) / (m - 1);
					dx = (s.x2 - s.x1) / m;
					dy = (s.y2 - s.y1) / m;
					for (int a = 1; a < m; a++)
						// somehow we have to bypass the first point to avoid duplicating energy around a corner
						model.changePowerAt(s.x1 + dx * a, s.y1 + dy * a, r);
				}
			}

		}

		// System.out.println(segments.get(1).radiation + "," + segments.get(37).radiation);

	}

	private void computeViewFactors() {
		int n = segments.size();
		Segment s1, s2;
		float vf;
		// populate the reflection matrix (using visibility and view factors)
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				reflection[i][j] = i == j ? 1 : 0;
				absorption[i][j] = 0;
			}
		}
		for (int i = 0; i < n - 1; i++) {
			s1 = segments.get(i);
			for (int j = i + 1; j < n; j++) {
				s2 = segments.get(j);
				if (isVisible(s1, s2)) {
					vf = s1.getViewFactor(s2);
					if (vf > 1) // FIXME: Why is our view factor sometimes larger than 1?
						vf = 1;
					reflection[i][j] = s1.getPart().getReflectivity() * vf;
					reflection[j][i] = s2.getPart().getReflectivity() * vf;
					absorption[i][j] = s1.getPart().getAbsorptivity() * vf;
					absorption[j][i] = s2.getPart().getAbsorptivity() * vf;
				}
			}
		}
	}

	void segmentizePerimeters() {
		segments.clear();
		patchSize = model.getLx() * patchSizePercentage;
		for (Part part : model.getParts()) {
			segmentizePerimeter(part);
		}
		int n = segments.size();
		reflection = new float[n][n];
		absorption = new float[n][n];
		computeViewFactors();
	}

	List<Segment> getSegments() {
		return segments;
	}

	private void segmentizePerimeter(Part part) {

		Shape shape = part.getShape();

		if (shape instanceof Rectangle2D.Float) { // special case, faster implementation (no trig)
			Rectangle2D.Float r = (Rectangle2D.Float) shape;
			float x0 = r.x;
			float y0 = r.y;
			float x1 = r.x + r.width;
			float y1 = r.y + r.height;
			// follow the clockwise direction in setting lines
			if (r.width <= patchSize) {
				segments.add(new Segment(x0, y0, x1, y0, part));
			} else {
				int n = (int) (r.width / patchSize);
				for (int i = 0; i < n; i++)
					segments.add(new Segment(x0 + i * patchSize, y0, x0 + (i + 1) * patchSize, y0, part));
				if (Math.abs(x0 + n * patchSize - x1) > 0.05f * patchSize)
					segments.add(new Segment(x0 + n * patchSize, y0, x1, y0, part));
			}
			if (r.height <= patchSize) {
				segments.add(new Segment(x1, y0, x1, y1, part));
			} else {
				int n = (int) (r.height / patchSize);
				for (int i = 0; i < n; i++)
					segments.add(new Segment(x1, y0 + i * patchSize, x1, y0 + (i + 1) * patchSize, part));
				if (Math.abs(y0 + n * patchSize - y1) > 0.05f * patchSize)
					segments.add(new Segment(x1, y0 + n * patchSize, x1, y1, part));
			}
			if (r.width <= patchSize) {
				segments.add(new Segment(x1, y1, x0, y1, part));
			} else {
				int n = (int) (r.width / patchSize);
				for (int i = 0; i < n; i++)
					segments.add(new Segment(x1 - i * patchSize, y1, x1 - (i + 1) * patchSize, y1, part));
				if (Math.abs(x1 - n * patchSize - x0) > 0.05f * patchSize)
					segments.add(new Segment(x1 - n * patchSize, y1, x0, y1, part));
			}
			if (r.height <= patchSize) {
				segments.add(new Segment(x0, y1, x0, y0, part));
			} else {
				int n = (int) (r.height / patchSize);
				for (int i = 0; i < n; i++)
					segments.add(new Segment(x0, y1 - i * patchSize, x0, y1 - (i + 1) * patchSize, part));
				if (Math.abs(y1 - n * patchSize - y0) > 0.05f * patchSize)
					segments.add(new Segment(x0, y1 - n * patchSize, x0, y0, part));
			}
		}

		else if (shape instanceof Polygon2D) {
			Polygon2D r = (Polygon2D) shape;
			int n = r.getVertexCount();
			// follow the clockwise direction in segmentization
			Point2D.Float v1, v2;
			Line2D.Float line = new Line2D.Float();
			for (int i = 0; i < n - 1; i++) {
				v1 = r.getVertex(i);
				v2 = r.getVertex(i + 1);
				line.setLine(v1, v2);
				segmentize(line, part);
			}
			v1 = r.getVertex(n - 1);
			v2 = r.getVertex(0);
			line.setLine(v1, v2);
			segmentize(line, part);
		}

		else if (shape instanceof Blob2D) {
			Blob2D r = (Blob2D) shape;
			int n = r.getPointCount();
			// follow the clockwise direction in setting lines
			Point2D.Float v1, v2;
			Line2D.Float line = new Line2D.Float();
			for (int i = 0; i < n - 1; i++) {
				v1 = r.getPoint(i);
				v2 = r.getPoint(i + 1);
				line.setLine(v1, v2);
				segmentize(line, part);
			}
			v1 = r.getPoint(n - 1);
			v2 = r.getPoint(0);
			line.setLine(v1, v2);
			segmentize(line, part);
		}

		else if (shape instanceof Ellipse2D.Float) {
			Ellipse2D.Float e = (Ellipse2D.Float) shape;
			float a = e.width * 0.5f;
			float b = e.height * 0.5f;
			float x = e.x + a;
			float y = e.y + b;
			float h = (a - b) / (a + b);
			h *= h;
			double perimeter = Math.PI * (a + b) * (1 + 3 * h / (10 + Math.sqrt(4 - 3 * h)));
			int n = (int) (perimeter / patchSize);
			float[] vx = new float[n];
			float[] vy = new float[n];
			float theta;
			float delta = (float) (2 * Math.PI / n);
			for (int i = 0; i < n; i++) {
				theta = delta * i;
				vx[i] = (float) (x + a * Math.cos(theta));
				vy[i] = (float) (y + b * Math.sin(theta));
			}
			for (int i = 0; i < n - 1; i++) {
				segments.add(new Segment(vx[i], vy[i], vx[i + 1], vy[i + 1], part));
			}
			segments.add(new Segment(vx[n - 1], vy[n - 1], vx[0], vy[0], part));
		}

	}

	private void segmentize(Line2D.Float line, Part part) {
		float length = (float) Math.hypot(line.x1 - line.x2, line.y1 - line.y2);
		if (length <= patchSize) {
			segments.add(new Segment(line.x1, line.y1, line.x2, line.y2, part));
		} else {
			float cos = (line.x2 - line.x1) / length;
			float sin = (line.y2 - line.y1) / length;
			int n = (int) (length / patchSize);
			float xi, yi, xj = 0, yj = 0;
			for (int i = 0; i < n; i++) {
				xi = line.x1 + i * patchSize * cos;
				yi = line.y1 + i * patchSize * sin;
				xj = xi + patchSize * cos;
				yj = yi + patchSize * sin;
				segments.add(new Segment(xi, yi, xj, yj, part));
			}
			segments.add(new Segment(xj, yj, line.x2, line.y2, part));
		}
	}

	// can the two segments see each other?
	boolean isVisible(Segment s1, Segment s2) {
		Point2D.Float p1 = s1.getCenter();
		Point2D.Float p2 = s2.getCenter();
		for (Part part : model.getParts()) {
			if (part.intersectsLine(p1, p2))
				return false;
		}
		return true;
	}

}
