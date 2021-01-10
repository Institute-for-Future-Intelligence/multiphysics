package org.energy2d.view;

/**
 * Cache properties that were set previously using the interfaces such as dialog windows.
 * 
 * @author Charles Xie
 *
 */
final class PreviousProperties {

	// NaN indicates that no previous value has been set for a property

	float temperature = Float.NaN;
	float thermalConductivity = Float.NaN;
	float density = Float.NaN;
	float specificHeat = Float.NaN;
	float absorptivity = Float.NaN;
	float reflectivity = Float.NaN;
	float transmissivity = Float.NaN;
	float emissivity = Float.NaN;
	float elasticity = Float.NaN;

	PreviousProperties() {
	}

}
