package org.concord.energy2d.model;

import java.awt.geom.Point2D;

import org.concord.energy2d.math.Vector2D;

/**
 * A line segment that is considered as a basic unit of radiation
 * 
 * @author Charles Xie
 * 
 */
public class Segment {

	public float x1, y1;
	public float x2, y2;
	private float xc, yc;

	Segment(float x1, float y1, float x2, float y2, float xc, float yc) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.xc = xc;
		this.yc = yc;
	}

	float length() {
		return (float) Math.hypot(x2 - x1, y2 - y1);
	}

	// the dot product with (x2-x1, y2-y1) must be zero and this normal vector points outwards
	Vector2D getNormalVector() {
		Point2D.Float c = getCenter();
		Vector2D v1 = new Vector2D(c.x - xc, c.y - yc);
		Vector2D v2 = new Vector2D(y1 - y2, x2 - x1);
		if (v1.dotProduct(v2) > 0)
			return v2;
		return new Vector2D(y2 - y1, x1 - x2);
	}

	Point2D.Float getCenter() {
		return new Point2D.Float(0.5f * (x1 + x2), 0.5f * (y1 + y2));
	}

	float getViewFactor(Segment s) {
		// calculate the center of this segment
		Point2D.Float p1 = getCenter();
		// calculate the center of the other segment
		Point2D.Float p2 = s.getCenter();
		Vector2D r = new Vector2D(p2.x - p1.x, p2.y - p1.y);
		Vector2D n1 = getNormalVector();
		Vector2D n2 = s.getNormalVector();
		return r.dotProduct(n1) * r.dotProduct(n2) / ((float) Math.PI * (r.x * r.x + r.y * r.y));
	}

}
