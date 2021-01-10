package org.energy2d.math;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

/**
 * @author Charles Xie
 * 
 */
public class Annulus extends Area {

	private float x, y, outerDiameter, innerDiameter;

	public Annulus(float x, float y, float innerDiameter, float outerDiameter) {
		super();
		setShape(x, y, innerDiameter, outerDiameter);
	}

	public Annulus(Annulus a) {
		this(a.x, a.y, a.innerDiameter, a.outerDiameter);
	}

	public void translateBy(float dx, float dy) {
		transform(AffineTransform.getTranslateInstance(dx, dy));
		x += dx;
		y += dy;
	}

	public void translateTo(float x, float y) {
		translateBy(x - this.x, y - this.y);
	}

	public void setShape(float x, float y, float innerDiameter, float outerDiameter) {
		reset();
		add(new Area(new Ellipse2D.Float(x - 0.5f * outerDiameter, y - 0.5f * outerDiameter, outerDiameter, outerDiameter)));
		subtract(new Area(new Ellipse2D.Float(x - 0.5f * innerDiameter, y - 0.5f * innerDiameter, innerDiameter, innerDiameter)));
		this.x = x;
		this.y = y;
		this.innerDiameter = innerDiameter;
		this.outerDiameter = outerDiameter;
		if (outerDiameter < innerDiameter)
			throw new IllegalArgumentException("Outer diameter cannot be smaller than inner diameter");
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
