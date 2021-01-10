package org.energy2d.system;

import java.util.EventObject;

/**
 * @author Charles Xie
 * 
 */
class ToolBarEvent extends EventObject {

	public final static byte FILE_INPUT = 0;
	public final static byte FILE_OUTPUT = 1;
	public final static byte RESET = 2;
	public final static byte NEW_FILE = 3;

	private byte type = FILE_INPUT;

	public ToolBarEvent(byte type, Object source) {
		super(source);
		this.type = type;
	}

	public byte getType() {
		return type;
	}

}
