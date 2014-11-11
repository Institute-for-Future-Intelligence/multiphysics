package org.concord.energy2d.model;

import java.awt.geom.Rectangle2D;

import org.concord.energy2d.util.XmlCharacterEncoder;

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
		XmlCharacterEncoder xce = new XmlCharacterEncoder();
		String xml = "<anemometer";
		if (stencil != ONE_POINT)
			xml += " stencil=\"" + stencil + "\"";
		String uid = getUid();
		if (uid != null && !uid.trim().equals(""))
			xml += " uid=\"" + xce.encode(uid) + "\"";
		if (attachID != null && !attachID.trim().equals(""))
			xml += " attach=\"" + xce.encode(attachID) + "\"";
		String label = getLabel();
		if (label != null && !label.trim().equals(""))
			xml += " label=\"" + xce.encode(label) + "\"";
		xml += " x=\"" + getX() + "\"";
		xml += " y=\"" + getY() + "\"/>";
		return xml;
	}

}
