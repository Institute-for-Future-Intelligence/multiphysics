package org.energy2d.model;

/**
 * @author Charles Xie
 * 
 */
public class NeumannThermalBoundary implements ThermalBoundary {

	// heat flux: unit w/m^2
	private float[] fluxAtBorder;

	public NeumannThermalBoundary() {
		fluxAtBorder = new float[4];
		// by default all fluxes are zero, meaning that the borders are completely insulative
		setFluxAtBorder(UPPER, 0);
		setFluxAtBorder(LOWER, 0);
		setFluxAtBorder(LEFT, 0);
		setFluxAtBorder(RIGHT, 0);
	}

	public void setFluxAtBorder(byte side, float value) {
		if (side < UPPER || side > LEFT)
			throw new IllegalArgumentException("side parameter illegal");
		fluxAtBorder[side] = value;
	}

	public float getFluxAtBorder(byte side) {
		if (side < UPPER || side > LEFT)
			throw new IllegalArgumentException("side parameter illegal");
		return fluxAtBorder[side];
	}

	public String toXml() {
		String s = "<flux_at_border upper=\"" + fluxAtBorder[UPPER] + "\"";
		s += " lower=\"" + fluxAtBorder[LOWER] + "\"";
		s += " left=\"" + fluxAtBorder[LEFT] + "\"";
		s += " right=\"" + fluxAtBorder[RIGHT] + "\"";
		s += "/>\n";
		return s;
	}

}
