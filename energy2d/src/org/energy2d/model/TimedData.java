package org.energy2d.model;

/**
 * @author Charles Xie
 * 
 */
public final class TimedData {

	private final float time;
	private final float value;

	public TimedData(float time, float value) {
		this.time = time;
		this.value = value;
	}

	public float getTime() {
		return time;
	}

	public float getValue() {
		return value;
	}

}
