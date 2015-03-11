package org.concord.energy2d.math;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

/**
 * @author Charles Xie
 * 
 */
public class Ring2D extends Area {

	private float x, y, outerDiameter, innerDiameter;

	public Ring2D(float x, float y, float innerDiameter, float outerDiameter) {
		super(new Ellipse2D.Float(x - 0.5f * outerDiameter, y - 0.5f * outerDiameter, outerDiameter, outerDiameter));
		subtract(new Area(new Ellipse2D.Float(x - 0.5f * innerDiameter, y - 0.5f * innerDiameter, innerDiameter, innerDiameter)));
		this.x = x;
		this.y = y;
		this.innerDiameter = innerDiameter;
		this.outerDiameter = outerDiameter;
	}

	public Ring2D(Ring2D ring) {
		this(ring.x, ring.y, ring.innerDiameter, ring.outerDiameter);
	}

	public void translateBy(float dx, float dy) {
		transform(AffineTransform.getTranslateInstance(dx, dy));
		x += dx;
		y += dy;
	}

	public void translateTo(float x, float y) {
		translateBy(x - this.x, y - this.y);
	}

	public void setRing(float x, float y, float innerDiameter, float outerDiameter) {
		reset();
		add(new Area(new Ellipse2D.Float(x - 0.5f * outerDiameter, y - 0.5f * outerDiameter, outerDiameter, outerDiameter)));
		subtract(new Area(new Ellipse2D.Float(x - 0.5f * innerDiameter, y - 0.5f * innerDiameter, innerDiameter, innerDiameter)));
		this.x = x;
		this.y = y;
		this.innerDiameter = innerDiameter;
		this.outerDiameter = outerDiameter;
	}

	/** return the x-coordinate of the center */
	public float getX() {
		return x;
	}

	/** return the y-coordinate of the center */
	public float getY() {
		return y;
	}

	public float getInnerDiameter() {
		return innerDiameter;
	}

	public float getOuterDiameter() {
		return outerDiameter;
	}

	public float getArea() {
		return (float) (0.25 * Math.PI * (outerDiameter * outerDiameter - innerDiameter * innerDiameter));
	}

}
