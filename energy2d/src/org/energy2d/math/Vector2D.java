package org.energy2d.math;

/**
 * A 2D vector
 * 
 * @author Charles Xie
 * 
 */
public class Vector2D {

	public float x = 1, y = 0;

	public Vector2D(float x, float y) {
		set(x, y);
	}

	public void set(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public float length() {
		return (float) Math.hypot(x, y);
	}

	public void normalize() {
		float r = length();
		if (r == 0)
			throw new RuntimeException("vector cannot have zero length: " + this);
		x /= r;
		y /= r;
	}

	public float dotProduct(Vector2D v) {
		return x * v.x + y * v.y;
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}

}
