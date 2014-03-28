/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;

/**
 * @author Charles Xie
 * 
 */
class MovingPolygon implements MovingShape {

	private Polygon polygon;

	MovingPolygon(Polygon polygon) {
		this.polygon = polygon;
	}

	public Shape getShape() {
		return polygon;
	}

	public void render(Graphics2D g) {
		g.draw(polygon);
	}

}
