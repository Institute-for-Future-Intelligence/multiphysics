package org.concord.energy2d.view;

import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

/**
 * @author Charles Xie
 * 
 */
class MovingAnnulus extends ComplexMovingShape {

	MovingAnnulus(Ellipse2D.Float outer, Ellipse2D.Float inner) {
		area = new Area(outer);
		area.subtract(new Area(inner));
	}

}