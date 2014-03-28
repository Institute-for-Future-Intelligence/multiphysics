/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.model;

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
