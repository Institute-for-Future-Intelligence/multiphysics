package org.energy2d.model;

/**
 * This is a simple Dirichlet thermal boundary that has the same temperature on each side.
 * 
 * If you need different temperatures within each side, use ComplexDirichletThermalBoundary.
 * 
 * @author Charles Xie
 * 
 */
public class DirichletThermalBoundary implements ThermalBoundary {

	// unit: centigrade
	private float[] temperatureAtBorder;

	public DirichletThermalBoundary() {
		temperatureAtBorder = new float[4];
		// by default all temperatures are zero
		setTemperatureAtBorder(UPPER, 0);
		setTemperatureAtBorder(LOWER, 0);
		setTemperatureAtBorder(LEFT, 0);
		setTemperatureAtBorder(RIGHT, 0);
	}

	public void setTemperatureAtBorder(byte side, float value) {
		if (side < UPPER || side > LEFT)
			throw new IllegalArgumentException("side parameter illegal");
		temperatureAtBorder[side] = value;
	}

	public float getTemperatureAtBorder(byte side) {
		if (side < UPPER || side > LEFT)
			throw new IllegalArgumentException("side parameter illegal");
		return temperatureAtBorder[side];
	}

	public String toXml() {
		String s = "<temperature_at_border upper=\"" + temperatureAtBorder[UPPER] + "\"";
		s += " lower=\"" + temperatureAtBorder[LOWER] + "\"";
		s += " left=\"" + temperatureAtBorder[LEFT] + "\"";
		s += " right=\"" + temperatureAtBorder[RIGHT] + "\"";
		s += "/>\n";
		return s;
	}

}
