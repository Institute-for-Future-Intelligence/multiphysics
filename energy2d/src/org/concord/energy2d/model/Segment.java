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

	public Segment(float x1, float y1, float x2, float y2, float xc, float yc) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.xc = xc;
		this.yc = yc;
		if (x1 == x2 && y1 == y2)
			throw new RuntimeException("segment cannot be a point: " + this);
	}

	public float length() {
		return (float) Math.hypot(x2 - x1, y2 - y1);
	}

	// the dot product with (x2-x1, y2-y1) must be zero and this normal vector points outwards
	public Vector2D getNormalVector() {
		Vector2D v = new Vector2D(y1 - y2, x2 - x1);
		Point2D.Float c = getCenter();
		if (new Vector2D(c.x - xc, c.y - yc).dotProduct(v) < 0)
			v.set(y2 - y1, x1 - x2);
		return v;
	}

	public Point2D.Float getCenter() {
		return new Point2D.Float(0.5f * (x1 + x2), 0.5f * (y1 + y2));
	}

	public float getViewFactor(Segment s) {
		// calculate the center of this segment
		Point2D.Float p1 = getCenter();
		// calculate the center of the other segment
		Point2D.Float p2 = s.getCenter();
		Vector2D r = new Vector2D(p2.x - p1.x, p2.y - p1.y);
		float r2 = r.x * r.x + r.y * r.y;
		r.normalize();
		Vector2D n1 = getNormalVector();
		n1.normalize();
		Vector2D n2 = s.getNormalVector();
		n2.normalize();
		// System.out.println(r+","+n1+","+n2);
		return -r.dotProduct(n1) * r.dotProduct(n2) / ((float) Math.PI * r2);
	}

	@Override
	public String toString() {
		return "(" + x1 + ", " + y1 + ") - (" + x2 + ", " + y2 + ")";
	}

}
