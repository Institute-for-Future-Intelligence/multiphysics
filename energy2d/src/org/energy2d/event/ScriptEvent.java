package org.energy2d.event;

/**
 * @author Charles Xie
 *
 */

import java.util.EventObject;

public class ScriptEvent extends EventObject {

	public final static byte SUCCEEDED = 0x00;
	public final static byte FAILED = 0x01;
	public final static byte HARMLESS = 0x02;

	private byte status;
	private String description;

	public ScriptEvent(Object source, byte status, String description) {
		super(source);
		this.status = status;
		this.description = description;
	}

	public byte getStatus() {
		return status;
	}

	public String getDescription() {
		return description;
	}

}