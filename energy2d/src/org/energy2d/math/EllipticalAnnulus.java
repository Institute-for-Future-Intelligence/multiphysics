package org.energy2d.math;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

/**
 * @author Charles Xie
 * 
 */
public class EllipticalAnnulus extends Area {

	private float x, y, outerA, innerA, outerB, innerB;

	public EllipticalAnnulus() {
		super();
		add(new Area(new Ellipse2D.Float(x - outerA, y - outerB, 2 * outerA, 2 * outerB)));
		subtract(new Area(new Ellipse2D.Float(x - innerA, y - innerB, 2 * innerA, 2 * innerB)));
	}

	public EllipticalAnnulus(float x, float y, float innerA, float innerB, float outerA, float outerB) {
		super();
		setShape(x, y, innerA, innerB, outerA, outerB);
	}

	public EllipticalAnnulus(EllipticalAnnulus a) {
		this(a.x, a.y, a.innerA, a.innerB, a.outerA, a.outerB);
	}

	public void translateBy(float dx, float dy) {
		transform(AffineTransform.getTranslateInstance(dx, dy));
		x += dx;
		y += dy;
	}

	public void translateTo(float x, float y) {
		translateBy(x - this.x, y - this.y);
	}

	public void setShape(float x, float y, float innerA, float innerB, float outerA, float outerB) {
		reset();
		add(new Area(new Ellipse2D.Float(x - outerA, y - outerB, 2 * outerA, 2 * outerB)));
		subtract(new Area(new Ellipse2D.Float(x - innerA, y - innerB, 2 * innerA, 2 * innerB)));
		this.x = x;
		this.y = y;
		this.innerA = innerA;
		this.innerB = innerB;
		this.outerA = outerA;
		this.outerB = outerB;
		if (outerA < innerA || outerB < innerB)
			throw new IllegalArgumentException("Outer a or b cannot be smaller than inner a or b");
	}

	public void setShape() {
		reset();
		add(new Area(new Ellipse2D.Float(x - outerA, y - outerB, 2 * outerA, 2 * outerB)));
		subtract(new Area(new Ellipse2D.Float(x - innerA, y - innerB, 2 * innerA, 2 * innerB)));
		if (outerA < innerA || outerB < innerB)
			throw new IllegalArgumentException("Outer a or b cannot be smaller than inner a or b");
	}

	public void setX(float x) {
		this.x = x;
	}

	/** return the x-coordinate of the center */
	public float getX() {
		return x;
	}

	public void setY(float y) {
		this.y = y;
	}

	/** return the y-coordinate of the center */
	public float getY() {
		return y;
	}

	public void setInnerA(float innerA) {
		this.innerA = innerA;
	}

	public float getInnerA() {
		return innerA;
	}

	public void setInnerB(float innerB) {
		this.innerB = innerB;
	}

	public float getInnerB() {
		return innerB;
	}

	public void setOuterA(float outerA) {
		this.outerA = outerA;
	}

	public float getOuterA() {
		return outerA;
	}

	public void setOuterB(float outerB) {
		this.outerB = outerB;
	}

	public float getOuterB() {
		return outerB;
	}

	public float getArea() {
		return (float) (Math.PI * (outerA * outerB - innerA * innerB));
	}

}
