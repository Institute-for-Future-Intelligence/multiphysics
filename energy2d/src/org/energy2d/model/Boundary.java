package org.energy2d.model;

/**
 * @author Charles Xie
 * 
 */
public interface Boundary {

	public static final byte UPPER = 0;
	public static final byte RIGHT = 1;
	public static final byte LOWER = 2;
	public static final byte LEFT = 3;

	public String toXml();

}
