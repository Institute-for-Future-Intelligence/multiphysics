/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.Graphics2D;
import java.awt.Shape;

/**
 * @author Charles Xie
 * 
 */
public interface MovingShape {

	public Shape getShape();

	public void render(Graphics2D g);

}
