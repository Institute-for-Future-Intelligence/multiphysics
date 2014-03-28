/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.model;

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
