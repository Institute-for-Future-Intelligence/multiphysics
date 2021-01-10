package org.energy2d.view;

import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

/**
 * @author Charles Xie
 * 
 */
class MovingAnnulus extends ComplexMovingShape {

	private Ellipse2D.Float inner, outer;

	MovingAnnulus(Ellipse2D.Float outer, Ellipse2D.Float inner) {
		area = new Area(outer);
		area.subtract(new Area(inner));
		this.inner = inner;
		this.outer = outer;
	}

	Ellipse2D.Float getInnerEllipse() {
		return inner;
	}

	Ellipse2D.Float getOuterEllipse() {
		return outer;
	}

}