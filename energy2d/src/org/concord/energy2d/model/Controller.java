/*
 *   Copyright (C) 2012  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.model;

/**
 * @author Charles Xie
 * 
 */
public interface Controller {

	/** on/off control, return a boolean to indicate if it is on or off */
	public boolean onoff(Model2D model);

}
