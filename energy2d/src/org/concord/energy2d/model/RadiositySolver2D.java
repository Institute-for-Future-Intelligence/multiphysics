package org.concord.energy2d.model;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

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

	RadiositySolver2D(Model2D model) {
		this.model = model;
	}

	void solve() {
		for (Part part : model.getParts()) {
			radiate(part);
		}
	}

	void radiate(Part part) {

		Shape shape = part.getShape();
		Line2D.Float line = new Line2D.Float();

		if (shape instanceof Rectangle2D.Float) {
			// must follow the clockwise direction in setting lines
			Rectangle2D.Float r = (Rectangle2D.Float) shape;
			// north
			line.setLine(r.x, r.y, r.x + r.width, r.y);
			radiate(line);
			// east
			line.setLine(r.x + r.width, r.y, r.x + r.width, r.y + r.height);
			radiate(line);
			// south
			line.setLine(r.x + r.width, r.y + r.height, r.x, r.y + r.height);
			radiate(line);
			// west
			line.setLine(r.x, r.y + r.height, r.x, r.y);
			radiate(line);
		}

		else if (shape instanceof Polygon2D) {
			Polygon2D r = (Polygon2D) shape;
			int n = r.getVertexCount();
			// must follow the clockwise direction in setting lines
			Point2D.Float v1, v2;
			for (int i = 0; i < n - 1; i++) {
				v1 = r.getVertex(i);
				v2 = r.getVertex(i + 1);
				line.setLine(v1, v2);
				radiate(line);
			}
			v1 = r.getVertex(n - 1);
			v2 = r.getVertex(0);
			line.setLine(v1, v2);
			radiate(line);
		}

		else if (shape instanceof Blob2D) {
			Blob2D r = (Blob2D) shape;
			int n = r.getPointCount();
			// must follow the clockwise direction in setting lines
			Point2D.Float v1, v2;
			for (int i = 0; i < n - 1; i++) {
				v1 = r.getPoint(i);
				v2 = r.getPoint(i + 1);
				line.setLine(v1, v2);
				radiate(line);
			}
			v1 = r.getPoint(n - 1);
			v2 = r.getPoint(0);
			line.setLine(v1, v2);
			radiate(line);
		}

		else if (shape instanceof Ellipse2D.Float) {
			Ellipse2D.Float e = (Ellipse2D.Float) shape;
			float a = e.width * 0.5f;
			float b = e.height * 0.5f;
			float x = e.x + a;
			float y = e.y + b;
			int polygonize = 20;
			float[] vx = new float[polygonize];
			float[] vy = new float[polygonize];
			float theta;
			float delta = (float) (2 * Math.PI / polygonize);
			for (int i = 0; i < polygonize; i++) {
				theta = delta * i;
				vx[i] = (float) (x + a * Math.cos(theta));
				vy[i] = (float) (y + b * Math.sin(theta));
			}
			for (int i = 0; i < polygonize - 1; i++) {
				line.setLine(vx[i], vy[i], vx[i + 1], vy[i + 1]);
				radiate(line);
			}
			line.setLine(vx[polygonize - 1], vy[polygonize - 1], vx[0], vy[0]);
			radiate(line);
		}

	}

	private void radiate(Line2D.Float line) {
	}

	// can the two segments see each other?
	boolean visible(Segment s1, Segment s2) {
		Point2D.Float p1 = s1.getCenter();
		Point2D.Float p2 = s2.getCenter();
		for (Part part : model.getParts()) {
			if (part.intersectsLine(p1, p2))
				return false;
		}
		return true;
	}

}
