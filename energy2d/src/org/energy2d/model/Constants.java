package org.energy2d.model;

/**
 * @author Charles Xie
 * 
 */
public final class Constants {

	// Air's thermal conductivity = 0.025 W/(m*K)
	public final static float AIR_THERMAL_CONDUCTIVITY = 0.025f;

	// Air's specific heat = 1012 J/(kg*K)
	public final static float AIR_SPECIFIC_HEAT = 1012;

	// Air's density = 1.204 kg/m^3 at 25 C
	public final static float AIR_DENSITY = 1.204f;

	/*
	 * By default, air's kinematic viscosity = 1.568 x 10^-5 m^2/s at 27 C is
	 * used. It can be set to zero for inviscid fluid.
	 */
	public final static float AIR_VISCOSITY = 0.00001568f;

}
