/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

/**
 * @author Charles Xie
 * 
 */
class MovingEllipse implements MovingShape {

	private Ellipse2D.Float ellipse;

	MovingEllipse(Ellipse2D.Float ellipse) {
		this.ellipse = ellipse;
	}

	public Shape getShape() {
		return ellipse;
	}

	public void render(Graphics2D g) {
		g.draw(ellipse);
	}

}
