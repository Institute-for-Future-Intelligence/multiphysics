package org.energy2d.model;

/**
 * @author Charles Xie
 * 
 */
public interface Discrete {

	public float getRx();

	public float getRy();

	public void setVx(float vx);

	public float getVx();

	public void setVy(float vy);

	public float getVy();

	public float getSpeed();

	public void setVelocityAngle(float angle);

}
