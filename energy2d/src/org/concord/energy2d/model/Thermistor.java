package org.concord.energy2d.model;

/**
 * A thermistor controls the power source by its temperature.
 * 
 * @author Charles Xie
 * 
 */
public class Thermistor {

	private Part powerSource;
	private float temperatureCoefficient = 0; // http://en.wikipedia.org/wiki/Temperature_coefficient

	public Thermistor(Part powerSource) {
		this.powerSource = powerSource;
	}

	public void setTemperatureCoefficient(float temperatureCoefficient) {
		this.temperatureCoefficient = temperatureCoefficient;
	}

	public float getTemperatureCoefficient() {
		return temperatureCoefficient;
	}

	public String toXml() {
		String xml = "<thermistor";
		xml += " temperature_coefficient=\"" + temperatureCoefficient + "\"";
		xml += " power_source=\"" + powerSource.getUid() + "\"/>";
		return xml;
	}

}
