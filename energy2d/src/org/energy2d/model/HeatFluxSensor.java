package org.energy2d.model;

import java.awt.geom.Rectangle2D;

import org.energy2d.util.XmlCharacterEncoder;

/**
 * Measure both conductive and radiative heat fluxes.
 * 
 * @author Charles Xie
 * 
 */
public class HeatFluxSensor extends Sensor {

	public final static float RELATIVE_WIDTH = 0.036f;
	public final static float RELATIVE_HEIGHT = 0.012f;

	private float angle; // radians
	private float value;

	public HeatFluxSensor(float x, float y) {
		super(new Rectangle2D.Float());
		setCenter(x, y);
	}

	public HeatFluxSensor(float x, float y, String label, float angle) {
		this(x, y);
		this.angle = angle;
		setLabel(label);
	}

	@Override
	public HeatFluxSensor duplicate(float x, float y) {
		return new HeatFluxSensor(x, y, null, angle);
	}

	@Override
	public HeatFluxSensor duplicate() {
		float x = 0;
		float y = 0;
		if (getShape() instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) getShape();
			x = r.x + 0.5f * r.width;
			y = r.y + 0.5f * r.height;
		} else {
			// TODO: none-rectangular shape
		}
		return new HeatFluxSensor(x, y, null, angle);
	}

	public void setValue(float value) {
		this.value = value;
	}

	public float getValue() {
		return value;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

	public float getAngle() {
		return angle;
	}

	@Override
	public String getName() {
		return "Heat Flux Sensor (W/m" + '\u00B2' + ")";
	}

	@Override
	public String toXml() {
		XmlCharacterEncoder xce = new XmlCharacterEncoder();
		String xml = "<heat_flux_sensor";
		String uid = getUid();
		if (uid != null && !uid.trim().equals(""))
			xml += " uid=\"" + xce.encode(uid) + "\"";
		if (attachID != null && !attachID.trim().equals(""))
			xml += " attach=\"" + xce.encode(attachID) + "\"";
		String label = getLabel();
		if (label != null && !label.trim().equals(""))
			xml += " label=\"" + xce.encode(label) + "\"";
		xml += " angle=\"" + angle + "\"";
		xml += " x=\"" + getX() + "\"";
		xml += " y=\"" + getY() + "\"/>";
		return xml;
	}

}
