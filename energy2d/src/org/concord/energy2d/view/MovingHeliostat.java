package org.concord.energy2d.view;

import java.awt.geom.Rectangle2D;

import org.concord.energy2d.model.Heliostat;

/**
 * @author Charles Xie
 * 
 */
class MovingHeliostat extends ComplexMovingShape {

	private Rectangle2D.Float boundingBox;

	MovingHeliostat(Rectangle2D.Float boundingBox, float angle) {
		area = Heliostat.getShape(boundingBox, angle);
		this.boundingBox = boundingBox;
	}

	Rectangle2D.Float getBoundingBox() {
		return boundingBox;
	}

}
