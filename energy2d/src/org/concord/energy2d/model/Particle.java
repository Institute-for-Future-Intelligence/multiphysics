package org.concord.energy2d.model;

import java.awt.geom.Ellipse2D;

/**
 * A particle in the Lagrangian frame.
 * 
 * @author Charles Xie
 * 
 */
public class Particle extends Manipulable implements Discrete {

	float mass = 1.0f;
	float radius = 0.1f;
	float rx, ry;
	float vx, vy;
	float ax, ay;
	float fx, fy;
	private float dx, dy;

	public Particle(float rx, float ry) {
		super(new Ellipse2D.Float());
		this.rx = rx;
		this.ry = ry;
		updateShape();
	}

	private void updateShape() {
		Ellipse2D.Float e = (Ellipse2D.Float) getShape();
		e.x = rx;
		e.y = ry;
		e.width = e.height = radius * 2;
	}

	public Manipulable duplicate(float x, float y) {
		return new Particle(x, y);
	}

	void translateBy(float deltaX, float deltaY) {
		rx += deltaX;
		ry += deltaY;
		updateShape();
	}

	// predict this particle's new position using second order Taylor expansion.
	void predict(float dt) {
		float dt2 = 0.5f * dt * dt;
		dx = vx * dt + ax * dt2;
		dy = vy * dt + ay * dt2;
		rx += dx;
		ry += dy;
		vx += ax * dt;
		vy += ay * dt;
	}

	// correct this particle's position predicted by the predict method.
	// fx and fy were used in the force calculation routine to store the new acceleration data.
	// ax and ay were used to hold the old acceleration data before calling this method.
	// After calling this method, new acceleration data will be assigned to ax and ay, whereas the forces to fx and fy.
	void correct(float dt) {
		vx += 0.5f * dt * (fx - ax);
		vy += 0.5f * dt * (fy - ay);
		ax = fx;
		ay = fy;
		fx *= mass;
		fy *= mass;
	}

	public float getSpeed() {
		return (float) Math.hypot(vx, vy);
	}

	public void setAngle(float angle) {
		float c = getSpeed();
		vx = (float) (Math.cos(angle) * c);
		vy = (float) (Math.sin(angle) * c);
	}

	public void setRx(float rx) {
		this.rx = rx;
	}

	public float getRx() {
		return rx;
	}

	public void setRy(float ry) {
		this.ry = ry;
	}

	public float getRy() {
		return ry;
	}

	public void setVx(float vx) {
		this.vx = vx;
	}

	public float getVx() {
		return vx;
	}

	public void setVy(float vy) {
		this.vy = vy;
	}

	public float getVy() {
		return vy;
	}

	public float getAx() {
		return ax;
	}

	public float getAy() {
		return ay;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	public float getRadius() {
		return radius;
	}

	public void setMass(float mass) {
		this.mass = mass;
	}

	public float getMass() {
		return mass;
	}

}
