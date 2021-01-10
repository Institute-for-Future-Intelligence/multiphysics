package org.energy2d.model;

import java.util.Arrays;

/**
 * @author Charles Xie
 * 
 */
public class ComplexDirichletThermalBoundary implements ThermalBoundary {

	// unit: centigrade
	private float[] temperatureLeft;
	private float[] temperatureRight;
	private float[] temperatureUpper;
	private float[] temperatureLower;

	public ComplexDirichletThermalBoundary(int nx, int ny) {
		temperatureLeft = new float[ny];
		temperatureRight = new float[ny];
		temperatureUpper = new float[nx];
		temperatureLower = new float[nx];
		// by default all temperatures are zero
		Arrays.fill(temperatureLeft, 0);
		Arrays.fill(temperatureRight, 0);
		Arrays.fill(temperatureUpper, 0);
		Arrays.fill(temperatureLower, 0);
	}

	public void setTemperaturesAtBorder(byte side, float[] value) {
		if (side < UPPER || side > LEFT)
			throw new IllegalArgumentException("side parameter illegal");
		switch (side) {
		case LEFT:
			if (value.length != temperatureLeft.length)
				throw new IllegalArgumentException("array lengths do not match: left boundary temperatures");
			for (int i = 0; i < temperatureLeft.length; i++)
				temperatureLeft[i] = value[i];
			break;
		case RIGHT:
			if (value.length != temperatureRight.length)
				throw new IllegalArgumentException("array lengths do not match: right boundary temperatures");
			for (int i = 0; i < temperatureRight.length; i++)
				temperatureRight[i] = value[i];
			break;
		case UPPER:
			if (value.length != temperatureUpper.length)
				throw new IllegalArgumentException("array lengths do not match: upper boundary temperatures");
			for (int i = 0; i < temperatureUpper.length; i++)
				temperatureUpper[i] = value[i];
			break;
		case LOWER:
			if (value.length != temperatureLower.length)
				throw new IllegalArgumentException("array lengths do not match: lower boundary temperatures");
			for (int i = 0; i < temperatureLower.length; i++)
				temperatureLower[i] = value[i];
			break;
		}
	}

	public float[] getTemperaturesAtBorder(byte side) {
		if (side < UPPER || side > LEFT)
			throw new IllegalArgumentException("side parameter illegal");
		switch (side) {
		case LEFT:
			return temperatureLeft;
		case RIGHT:
			return temperatureRight;
		case UPPER:
			return temperatureUpper;
		default:
			return temperatureLower;
		}
	}

	public String toXml() {
		String s = "<temperature_at_border upper=\"" + Arrays.toString(temperatureUpper) + "\"";
		s += " lower=\"" + Arrays.toString(temperatureLower) + "\"";
		s += " left=\"" + Arrays.toString(temperatureLeft) + "\"";
		s += " right=\"" + Arrays.toString(temperatureRight) + "\"";
		s += "/>\n";
		return s;
	}

}
