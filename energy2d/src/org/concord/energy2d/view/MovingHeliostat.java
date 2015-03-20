package org.concord.energy2d.view;

import java.awt.geom.Rectangle2D;

import org.concord.energy2d.model.Heliostat;

/**
 * @author Charles Xie
 * 
 */
class MovingHeliostat extends ComplexMovingShape {

	private Rectangle2D.Float boundingBox;

	MovingHeliostat(Rectangle2D.Float boundingBox, float speed, float angle, float delta) {
		area = Heliostat.getShape(boundingBox, speed, angle, delta);
		this.boundingBox = boundingBox;
	}

	Rectangle2D.Float getBoundingBox() {
		return boundingBox;
	}

}
