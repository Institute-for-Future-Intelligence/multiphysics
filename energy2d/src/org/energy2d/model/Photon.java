package org.energy2d.model;

/**
 * This is NOT really a photon in strict physics sense. It is just a particle for modeling the traveling of light energy.
 * 
 * @author Charles Xie
 * 
 * */

public class Photon implements Discrete {

	private float rx;
	private float ry;
	private float vx;
	private float vy;
	private float energy;
	private float c;

	Photon(float rx, float ry, float energy, float c) {
		this.rx = rx;
		this.ry = ry;
		this.energy = energy;
		this.c = c;
	}

	public Photon(float rx, float ry, float energy, float angle, float c) {
		this(rx, ry, energy, c);
		setVelocityAngle(angle);
	}

	/** This is not really the speed of light, just a number that represents the speed of the light animation. */
	public float getSpeed() {
		return c;
	}

	public float getEnergy() {
		return energy;
	}

	public void setVelocityAngle(float angle) {
		vx = (float) (Math.cos(angle) * c);
		vy = (float) (Math.sin(angle) * c);
	}

	public float getVx() {
		return vx;
	}

	public void setVx(float vx) {
		this.vx = vx;
	}

	public float getVy() {
		return vy;
	}

	public void setVy(float vy) {
		this.vy = vy;
	}

	public boolean isContained(float xmin, float xmax, float ymin, float ymax) {
		return rx >= xmin && rx <= xmax && ry >= ymin && ry <= ymax;
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

	public void move(float dt) {
		rx += vx * dt;
		ry += vy * dt;
	}

}