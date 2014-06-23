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
		Segment s1, s2;
		for (int i = 0; i < n - 1; i++) {
			s1 = segments.get(i);
			for (int j = i + 1; j < n; j++) {
				s2 = segments.get(j);
				if (isVisible(s1, s2)) {

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
	}

	List<Segment> getSegments() {
		return segments;
	}

	private void segmentizePerimeter(Part part) {

		Shape shape = part.getShape();
		float xc = (float) shape.getBounds2D().getCenterX();
		float yc = (float) shape.getBounds2D().getCenterY();

		if (shape instanceof Rectangle2D.Float) { // special case, faster implementation (no trig)
			Rectangle2D.Float r = (Rectangle2D.Float) shape;
			float x0 = r.x;
			float y0 = r.y;
			float x1 = r.x + r.width;
			float y1 = r.y + r.height;
			// follow the clockwise direction in setting lines
			if (r.width <= patchSize) {
				segments.add(new Segment(x0, y0, x1, y0, xc, yc));
			} else {
				int n = (int) (r.width / patchSize);
				for (int i = 0; i < n; i++)
					segments.add(new Segment(x0 + i * patchSize, y0, x0 + (i + 1) * patchSize, y0, xc, yc));
				segments.add(new Segment(x0 + n * patchSize, y0, x1, y0, xc, yc));
			}
			if (r.height <= patchSize) {
				segments.add(new Segment(x1, y0, x1, y1, xc, yc));
			} else {
				int n = (int) (r.height / patchSize);
				for (int i = 0; i < n; i++)
					segments.add(new Segment(x1, y0 + i * patchSize, x1, y0 + (i + 1) * patchSize, xc, yc));
				segments.add(new Segment(x1, y0 + n * patchSize, x1, y1, xc, yc));
			}
			if (r.width <= patchSize) {
				segments.add(new Segment(x1, y1, x0, y1, xc, yc));
			} else {
				int n = (int) (r.width / patchSize);
				for (int i = 0; i < n; i++)
					segments.add(new Segment(x1 - i * patchSize, y1, x1 - (i + 1) * patchSize, y1, xc, yc));
				segments.add(new Segment(x1 - n * patchSize, y1, x0, y1, xc, yc));
			}
			if (r.height <= patchSize) {
				segments.add(new Segment(x0, y1, x0, y0, xc, yc));
			} else {
				int n = (int) (r.height / patchSize);
				for (int i = 0; i < n; i++)
					segments.add(new Segment(x0, y1 - i * patchSize, x0, y1 - (i + 1) * patchSize, xc, yc));
				segments.add(new Segment(x0, y1 - n * patchSize, x0, y0, xc, yc));
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
				segmentize(line, xc, yc);
			}
			v1 = r.getVertex(n - 1);
			v2 = r.getVertex(0);
			line.setLine(v1, v2);
			segmentize(line, xc, yc);
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
				segmentize(line, xc, yc);
			}
			v1 = r.getPoint(n - 1);
			v2 = r.getPoint(0);
			line.setLine(v1, v2);
			segmentize(line, xc, yc);
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
				segments.add(new Segment(vx[i], vy[i], vx[i + 1], vy[i + 1], xc, yc));
			}
			segments.add(new Segment(vx[n - 1], vy[n - 1], vx[0], vy[0], xc, yc));
		}

	}

	private void segmentize(Line2D.Float line, float xc, float yc) {
		float length = (float) Math.hypot(line.x1 - line.x2, line.y1 - line.y2);
		if (length <= patchSize) {
			segments.add(new Segment(line.x1, line.y1, line.x2, line.y2, xc, yc));
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
				segments.add(new Segment(xi, yi, xj, yj, xc, yc));
			}
			segments.add(new Segment(xj, yj, line.x2, line.y2, xc, yc));
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
