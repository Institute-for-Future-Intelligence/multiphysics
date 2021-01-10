package org.energy2d.event;

import java.util.EventObject;

/**
 * @author Charles Xie
 * 
 */
public class ManipulationEvent extends EventObject {

	public static final byte DELETE = 0;
	public static final byte TRANSLATE = 1;
	public static final byte RESIZE = 2;
	public static final byte PROPERTY_CHANGE = 3;
	public static final byte SUN_SHINE = 4;
	public static final byte SUN_ANGLE_INCREASE = 5;
	public static final byte SUN_ANGLE_DECREASE = 6;
	public static final byte OBJECT_ADDED = 7;
	public static final byte SENSOR_ADDED = 8;
	public static final byte RUN = 11;
	public static final byte STOP = 12;
	public static final byte RESET = 13;
	public static final byte RELOAD = 14;
	public static final byte GRID = 15;
	public static final byte GRAPH = 16;
	public static final byte MOUSE_READ_CHANGED = 17;
	public static final byte REPAINT = 18;
	public static final byte SELECT_MODE_CHOSEN = 21;
	public static final byte HEATING_MODE_CHOSEN = 22;
	public static final byte FATAL_ERROR_OCCURRED = 99;

	private Object target;
	private byte type = -1;

	public ManipulationEvent(Object source, byte type) {
		super(source);
		this.type = type;
	}

	public ManipulationEvent(Object source, Object target, byte type) {
		super(source);
		this.target = target;
		this.type = type;
	}

	public byte getType() {
		return type;
	}

	public Object getTarget() {
		return target;
	}

}
