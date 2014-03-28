/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.model;

/**
 * This is NOT really a photon in strict physics sense. It is just a particle for modeling the traveling of light energy.
 * 
 * @author Charles Xie
 * 
 * */

public class Photon {

	private float x;
	private float y;
	private float vx;
	private float vy;
	private float energy;
	private float c;

	Photon(float x, float y, float energy, float c) {
		this.x = x;
		this.y = y;
		this.energy = energy;
		this.c = c;
	}

	public Photon(float x, float y, float energy, float angle, float c) {
		this(x, y, energy, c);
		setAngle(angle);
	}

	/** This is not really the speed of light, just a number that represents the speed of the light animation. */
	public float getSpeed() {
		return c;
	}

	public float getEnergy() {
		return energy;
	}

	void setAngle(float angle) {
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
		return x >= xmin && x <= xmax && y >= ymin && y <= ymax;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getX() {
		return x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getY() {
		return y;
	}

	public void move(float dt) {
		x += vx * dt;
		y += vy * dt;
	}

}