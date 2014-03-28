package org.concord.energy2d.model;

import java.awt.geom.Rectangle2D;

/**
 * @author Charles Xie
 * 
 */
public class Thermometer extends Sensor {

	public final static float RELATIVE_WIDTH = 0.01f;
	public final static float RELATIVE_HEIGHT = 0.05f;

	public Thermometer(float x, float y) {
		super(new Rectangle2D.Float()); // should have used Point2D but it is not a Shape.
		setCenter(x, y);
	}

	public Thermometer(float x, float y, String label) {
		this(x, y);
		setLabel(label);
	}

	public Thermometer duplicate(float x, float y) {
		return new Thermometer(x, y);
	}

	public float getCurrentDataInFahrenheit() {
		return getCurrentData() * 1.8f + 32;
	}

	@Override
	public String getName() {
		return "Thermometer (" + '\u2103' + ")";
	}

	@Override
	public String toXml() {
		String xml = "<thermometer";
		if (stencil != ONE_POINT)
			xml += " stencil=\"" + stencil + "\"";
		String uid = getUid();
		if (uid != null && !uid.trim().equals(""))
			xml += " uid=\"" + uid + "\"";
		String label = getLabel();
		if (label != null && !label.trim().equals(""))
			xml += " label=\"" + label + "\"";
		xml += " x=\"" + getX() + "\"";
		xml += " y=\"" + getY() + "\"/>";
		return xml;
	}

}
