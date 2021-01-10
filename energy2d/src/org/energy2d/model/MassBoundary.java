package org.energy2d.model;

/**
 * @author Charles Xie
 * 
 */
public interface MassBoundary extends Boundary {

	public final static byte REFLECTIVE = 0;
	public final static byte THROUGH = 1;
	public final static byte STOP = 2;
	public final static byte PERIODIC = 3;

}
