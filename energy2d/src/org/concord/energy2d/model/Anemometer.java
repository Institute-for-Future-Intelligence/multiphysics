package org.concord.energy2d.model;

import java.awt.geom.Rectangle2D;

/**
 * @author Charles Xie
 * 
 */
public class Anemometer extends Sensor {

	public final static float RELATIVE_WIDTH = 0.05f;
	public final static float RELATIVE_HEIGHT = 0.05f;

	private float angle;

	public Anemometer(float x, float y) {
		super(new Rectangle2D.Float());
		setCenter(x, y);
	}

	public Anemometer(float x, float y, String label) {
		this(x, y);
		setLabel(label);
	}

	public Anemometer duplicate(float x, float y) {
		return new Anemometer(x, y);
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

	public float getAngle() {
		return angle;
	}

	@Override
	public String getName() {
		return "Anemometer (m/s)";
	}

	@Override
	public String toXml() {
		String xml = "<anemometer";
		if (stencil != ONE_POINT)
			xml += " stencil=\"" + stencil + "\"";
		String uid = getUid();
		if (uid != null && !uid.trim().equals(""))
			xml += " uid=\"" + uid + "\"";
		if (attachID != null && !attachID.trim().equals(""))
			xml += " attach=\"" + attachID + "\"";
		String label = getLabel();
		if (label != null && !label.trim().equals(""))
			xml += " label=\"" + label + "\"";
		xml += " x=\"" + getX() + "\"";
		xml += " y=\"" + getY() + "\"/>";
		return xml;
	}

}
