package org.energy2d.model;

import java.awt.geom.Rectangle2D;

/**
 * A thermostat switches heating or cooling devices on or off to maintain the temperature at a setpoint.
 * 
 * A thermostat can be controlled by a thermometer, or just by the temperature at the center of the power source.
 * 
 * @author Charles Xie
 * 
 */
public class Thermostat {

	private Thermometer thermometer;
	private Part powerSource;
	private float setpoint = 20;
	private float deadband = 1;

	public Thermostat(Part powerSource) {
		if (powerSource == null)
			throw new IllegalArgumentException("A thermostat must be connected to a power source.");
		this.powerSource = powerSource;
	}

	public Thermostat(Thermometer thermometer, Part powerSource) {
		this(powerSource);
		this.thermometer = thermometer;
	}

	/** on/off (bang-bang) control, return a boolean to indicate if it is on or off */
	public boolean onoff(Model2D model) {
		float power = powerSource.getPower();
		if (power == 0)
			return false;
		boolean refresh = false;
		float t = 0;
		if (thermometer != null) {
			t = thermometer.getCurrentData();
		} else {
			Rectangle2D bounds = powerSource.getShape().getBounds2D();
			t = model.getTemperatureAt((float) bounds.getCenterX(), (float) bounds.getCenterY());
		}
		if (power > 0) { // if it is a heater
			if (t > setpoint + deadband) {
				powerSource.setPowerSwitch(false);
				refresh = true;
			} else if (t < setpoint - deadband) {
				powerSource.setPowerSwitch(true);
				refresh = true;
			}
		} else { // if it is a cooler
			if (t < setpoint - deadband) {
				powerSource.setPowerSwitch(false);
				refresh = true;
			} else if (t > setpoint + deadband) {
				powerSource.setPowerSwitch(true);
				refresh = true;
			}
		}
		return refresh;
	}

	public Thermometer getThermometer() {
		return thermometer;
	}

	public Part getPowerSource() {
		return powerSource;
	}

	public void setDeadband(float deadband) {
		this.deadband = deadband;
	}

	public float getDeadband() {
		return deadband;
	}

	public void setSetPoint(float setpoint) {
		this.setpoint = setpoint;
	}

	public float getSetPoint() {
		return setpoint;
	}

	public String toXml() {
		String xml = "<thermostat";
		xml += " set_point=\"" + setpoint + "\"";
		xml += " deadband=\"" + deadband + "\"";
		if (thermometer != null)
			xml += " thermometer=\"" + thermometer.getUid() + "\"";
		xml += " power_source=\"" + powerSource.getUid() + "\"/>";
		return xml;
	}

}
