package org.energy2d.model;

/**
 * @author Charles Xie
 * 
 */
public class SimpleMassBoundary implements MassBoundary {

	private byte[] flowType;

	public SimpleMassBoundary() {
		flowType = new byte[4];
		// by default all sides are reflective
		setFlowTypeAtBorder(UPPER, REFLECTIVE);
		setFlowTypeAtBorder(LOWER, REFLECTIVE);
		setFlowTypeAtBorder(LEFT, REFLECTIVE);
		setFlowTypeAtBorder(RIGHT, REFLECTIVE);
	}

	public void setFlowTypeAtBorder(byte side, byte value) {
		if (side < UPPER || side > LEFT)
			throw new IllegalArgumentException("side parameter illegal");
		flowType[side] = value;
	}

	public byte getFlowTypeAtBorder(byte side) {
		if (side < UPPER || side > LEFT)
			throw new IllegalArgumentException("side parameter illegal");
		return flowType[side];
	}

	public String toXml() {
		String s = "<mass_flow_at_border upper=\"" + flowType[UPPER] + "\"";
		s += " lower=\"" + flowType[LOWER] + "\"";
		s += " left=\"" + flowType[LEFT] + "\"";
		s += " right=\"" + flowType[RIGHT] + "\"";
		s += "/>\n";
		return s;
	}

}
