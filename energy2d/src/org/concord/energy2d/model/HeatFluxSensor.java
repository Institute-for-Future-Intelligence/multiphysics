package org.concord.energy2d.model;

import java.awt.geom.Rectangle2D;

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

	public HeatFluxSensor duplicate(float x, float y) {
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
		String xml = "<heat_flux_sensor";
		String uid = getUid();
		if (uid != null && !uid.trim().equals(""))
			xml += " uid=\"" + uid + "\"";
		if (attachID != null && !attachID.trim().equals(""))
			xml += " attach=\"" + attachID + "\"";
		String label = getLabel();
		if (label != null && !label.trim().equals(""))
			xml += " label=\"" + label + "\"";
		xml += " angle=\"" + angle + "\"";
		xml += " x=\"" + getX() + "\"";
		xml += " y=\"" + getY() + "\"/>";
		return xml;
	}

}
