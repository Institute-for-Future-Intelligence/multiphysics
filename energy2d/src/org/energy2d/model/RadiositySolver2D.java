package org.energy2d.model;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.energy2d.math.Blob2D;
import org.energy2d.math.EllipticalAnnulus;
import org.energy2d.math.Polygon2D;
import org.energy2d.math.Annulus;

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
	private float patchSizePercentage = 0.05f;
	private float[][] reflection, absorption;
	private int relaxationSteps = 2; // relaxation may not be needed much as we are already solving a time-dependent problem

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

	float measure(HeatFluxSensor sensor) {
		float measurement = 0f;
		float dx = patchSize * 0.5f * (float) Math.cos(-sensor.getAngle());
		float dy = patchSize * 0.5f * (float) Math.sin(-sensor.getAngle());
		float x1 = sensor.getX() - dx;
		float x2 = sensor.getX() + dx;
		float y1 = sensor.getY() - dy;
		float y2 = sensor.getY() + dy;
		Segment ss = new Segment(x1, y1, x2, y2, null);
		for (Segment s : segments) {
			if (isVisible(s, ss)) {
				float vf = s.getViewFactor(ss);
				if (vf > 1) // FIXME: Why is our view factor larger than 1 when two patches are very close?
					vf = 1;
				measurement += s.radiation * vf;
			}
		}
		return measurement;
	}

	void solve() {

		int n = segments.size();
		if (n <= 0)
			return;

		Segment s;
		synchronized (segments) {

			// compute emission of each segment using Stefan's Law (offset by the background radiation)
			for (int i = 0; i < n; i++) {
				s = segments.get(i);
				if (s.getPart().getEmissivity() > 0) {
					Point2D.Float c = s.getCenter();
					float temp;
					if (s.getPart().getConstantTemperature()) {
						temp = s.getPart().getTemperature() + 273;
					} else {
						temp = model.getTemperatureAt(c.x, c.y, Sensor.NINE_POINT) + 273; // FIXME: This needs to take the stencil points inwardly
					}
					temp *= temp;
					s.emission = s.getPart().getEmissivity() * Model2D.STEFAN_CONSTANT * temp * temp;
					temp = model.getBackgroundTemperature() + 273;
					temp *= temp;
					s.emission -= s.getPart().getEmissivity() * Model2D.STEFAN_CONSTANT * temp * temp;
				}
			}

			// apply Gauss-Seidel relaxation to get outgoing radiation for each segment (solving the radiosity matrix equation)
			for (int k = 0; k < relaxationSteps; k++) {
				for (int i = 0; i < n; i++) {
					s = segments.get(i);
					s.radiation = s.emission;
					for (int j = 0; j < n; j++) {
						if (j != i)
							s.radiation -= reflection[i][j] * segments.get(j).radiation;
					}
					s.radiation /= reflection[i][i];
				}
			}

			// get the radiation from other segments that ends up being absorbed by each segment
			for (int i = 0; i < n; i++) {
				s = segments.get(i);
				s.absorption = 0;
				for (int j = 0; j < n; j++) {
					if (j != i)
						s.absorption += absorption[i][j] * segments.get(j).radiation;
				}
			}

			float gx = model.getNx() / model.getLx();
			float gy = model.getNy() / model.getLy();
			float power;
			float length;
			float dx, dy;
			for (int i = 0; i < n; i++) {
				s = segments.get(i);
				length = s.length();
				int m = (int) (length * Math.max(gx, gy));
				if (m > 1) {
					power = (s.absorption - s.emission) / (m - 1);
					// equally divide and add energy to the power density array (the last round of radiation energy has been stored as thermal energy by the heat solver)
					dx = (s.x2 - s.x1) / m;
					dy = (s.y2 - s.y1) / m;
					// somehow we have to bypass the end points to avoid duplicating energy around a corner
					for (int a = 1; a < m; a++)
						model.changePowerAt(s.x1 + dx * a, s.y1 + dy * a, power);
				}
			}

		}

	}

	// populate the reflection matrix and the absorption matrix using visibility and view factors
	private void computeReflectionAndAbsorptionMatrices() {
		int n = segments.size();
		Segment s1, s2;
		float vf;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				reflection[i][j] = i == j ? 1 : 0; // the diagonal elements must be one because a segment is a line (hence the view factor must be zero)
				absorption[i][j] = 0; // the diagonal elements must be zero as a segment cannot absorb its own radiation
			}
		}
		for (int i = 0; i < n - 1; i++) {
			s1 = segments.get(i);
			for (int j = i + 1; j < n; j++) {
				s2 = segments.get(j);
				if (isVisible(s1, s2)) {
					vf = s1.getViewFactor(s2);
					if (vf > 1) // FIXME: Why is our view factor larger than 1 when two patches are very close?
						vf = 1;
					// the order of s1 and s2 is important below
					float lengthRatio = s1.length() / s2.length(); // apply the reciprocity rule
					reflection[i][j] = -s1.getPart().getReflectivity() * vf;
					reflection[j][i] = -s2.getPart().getReflectivity() * vf * lengthRatio;
					absorption[i][j] = s1.getPart().getAbsorptivity() * vf;
					absorption[j][i] = s2.getPart().getAbsorptivity() * vf * lengthRatio;
				}
			}
		}
	}

	void segmentizePerimeters() {
		segments.clear();
		patchSize = model.getLx() * patchSizePercentage;
		for (Part part : model.getParts()) {
			if (part.getTransmissivity() > 0.9999)
				continue;
			segmentizePerimeter(part);
		}
		int n = segments.size();
		reflection = new float[n][n];
		absorption = new float[n][n];
		computeReflectionAndAbsorptionMatrices();
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
			// follow the clockwise direction in setting lines (this is important for calculating outward-pointing normal vectors
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
			if (n <= 0)
				return;
			float[] vx = new float[n];
			float[] vy = new float[n];
			float theta;
			float delta = (float) (2 * Math.PI / n);
			// follow the clockwise direction in setting lines
			for (int i = 0; i < n; i++) {
				theta = delta * i;
				vx[i] = (float) (x + a * Math.cos(theta));
				vy[i] = (float) (y + b * Math.sin(theta));
			}
			for (int i = 0; i < n - 1; i++)
				segments.add(new Segment(vx[i], vy[i], vx[i + 1], vy[i + 1], part));
			if (vx[n - 1] != vx[0] || vy[n - 1] != vy[0])
				segments.add(new Segment(vx[n - 1], vy[n - 1], vx[0], vy[0], part));
		}

		else if (shape instanceof Polygon2D) {
			Polygon2D r = (Polygon2D) shape;
			int n = r.getVertexCount();
			// follow the clockwise direction in segmentization
			Point2D.Float v1, v2 = null;
			Line2D.Float line = new Line2D.Float();
			for (int i = 0; i < n - 1; i++) {
				v1 = r.getVertex(i);
				v2 = r.getVertex(i + 1);
				line.setLine(v1, v2);
				segmentize(line, part);
			}
			if (v2 != null) {
				v1 = r.getVertex(0);
				line.setLine(v1, v2);
				segmentize(line, part);
			}
		}

		else if (shape instanceof Blob2D) {
			Blob2D b = (Blob2D) shape;
			int n = b.getPathPointCount();
			int m = (int) (n / b.getPerimeter() * patchSize);
			Point2D.Float v1, v2 = null;
			// follow the clockwise direction in setting lines
			for (int i = 0; i < n - m; i++) {
				if (i % m == 0) {
					v1 = b.getPathPoint(i);
					v2 = b.getPathPoint(i + m);
					if (v1.x != v2.x || v1.y != v2.y)
						segments.add(new Segment(v1.x, v1.y, v2.x, v2.y, part));
				}
			}
			if (v2 != null) {
				v1 = b.getPathPoint(0);
				if (v1.x != v2.x || v1.y != v2.y)
					segments.add(new Segment(v1.x, v1.y, v2.x, v2.y, part));
			}
		}

		else if (shape instanceof Annulus) {
			Annulus r = (Annulus) shape;
			double perimeter = Math.PI * r.getInnerDiameter();
			int n = (int) (perimeter / patchSize);
			if (n <= 0)
				return;
			float[] vx = new float[n];
			float[] vy = new float[n];
			float theta;
			float delta = (float) (2 * Math.PI / n);
			float radius = 0.5f * r.getInnerDiameter();
			// follow the clockwise direction in setting lines
			for (int i = 0; i < n; i++) {
				theta = delta * i;
				vx[i] = (float) (r.getX() + radius * Math.cos(theta));
				vy[i] = (float) (r.getY() + radius * Math.sin(theta));
			}
			for (int i = 0; i < n - 1; i++)
				segments.add(new Segment(vx[i], vy[i], vx[i + 1], vy[i + 1], part));
			if (vx[n - 1] != vx[0] || vy[n - 1] != vy[0])
				segments.add(new Segment(vx[n - 1], vy[n - 1], vx[0], vy[0], part));
			perimeter = Math.PI * r.getOuterDiameter();
			n = (int) (perimeter / patchSize);
			vx = new float[n];
			vy = new float[n];
			delta = (float) (2 * Math.PI / n);
			radius = 0.5f * r.getOuterDiameter();
			// follow the clockwise direction in setting lines
			for (int i = 0; i < n; i++) {
				theta = delta * i;
				vx[i] = (float) (r.getX() + radius * Math.cos(theta));
				vy[i] = (float) (r.getY() + radius * Math.sin(theta));
			}
			for (int i = 0; i < n - 1; i++)
				segments.add(new Segment(vx[i], vy[i], vx[i + 1], vy[i + 1], part));
			if (vx[n - 1] != vx[0] || vy[n - 1] != vy[0])
				segments.add(new Segment(vx[n - 1], vy[n - 1], vx[0], vy[0], part));
		}

		else if (shape instanceof EllipticalAnnulus) {
			EllipticalAnnulus r = (EllipticalAnnulus) shape;
			float innerA = r.getInnerA();
			float innerB = r.getInnerB();
			float h = (innerA - innerB) / (innerA + innerB);
			h *= h;
			double innerPerimeter = Math.PI * (innerA + innerB) * (1 + 3 * h / (10 + Math.sqrt(4 - 3 * h)));
			int n = (int) (innerPerimeter / patchSize);
			if (n <= 0)
				return;
			float[] vx = new float[n];
			float[] vy = new float[n];
			float theta;
			float delta = (float) (2 * Math.PI / n);
			// follow the clockwise direction in setting lines
			for (int i = 0; i < n; i++) {
				theta = delta * i;
				vx[i] = (float) (r.getX() + innerA * Math.cos(theta));
				vy[i] = (float) (r.getY() + innerB * Math.sin(theta));
			}
			for (int i = 0; i < n - 1; i++)
				segments.add(new Segment(vx[i], vy[i], vx[i + 1], vy[i + 1], part));
			if (vx[n - 1] != vx[0] || vy[n - 1] != vy[0])
				segments.add(new Segment(vx[n - 1], vy[n - 1], vx[0], vy[0], part));
			float outerA = r.getOuterA();
			float outerB = r.getOuterB();
			h = (outerA - outerB) / (outerA + outerB);
			h *= h;
			double outerPerimeter = Math.PI * (outerA + outerB) * (1 + 3 * h / (10 + Math.sqrt(4 - 3 * h)));
			n = (int) (outerPerimeter / patchSize);
			vx = new float[n];
			vy = new float[n];
			delta = (float) (2 * Math.PI / n);
			// follow the clockwise direction in setting lines
			for (int i = 0; i < n; i++) {
				theta = delta * i;
				vx[i] = (float) (r.getX() + outerA * Math.cos(theta));
				vy[i] = (float) (r.getY() + outerB * Math.sin(theta));
			}
			for (int i = 0; i < n - 1; i++)
				segments.add(new Segment(vx[i], vy[i], vx[i + 1], vy[i + 1], part));
			if (vx[n - 1] != vx[0] || vy[n - 1] != vy[0])
				segments.add(new Segment(vx[n - 1], vy[n - 1], vx[0], vy[0], part));
		}

	}

	private void segmentize(Line2D.Float line, Part part) {
		float length = (float) Math.hypot(line.x1 - line.x2, line.y1 - line.y2);
		if (length <= patchSize) {
			if (line.x1 != line.x2 || line.y1 != line.y2)
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
			if (xj != line.x2 || yj != line.y2)
				segments.add(new Segment(xj, yj, line.x2, line.y2, part));
		}
	}

	// can the two segments see each other?
	boolean isVisible(Segment s1, Segment s2) {
		for (Part part : model.getParts()) {
			if (part.getTransmissivity() > 0.9999) // TODO: We just handle the complete transparent case here
				continue;
			if (part.intersectsLine(s1, s2))
				return false;
		}
		return true;
	}

}
