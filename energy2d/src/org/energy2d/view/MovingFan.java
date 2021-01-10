package org.energy2d.view;

import java.awt.geom.Rectangle2D;

import org.energy2d.model.Fan;

/**
 * @author Charles Xie
 * 
 */
class MovingFan extends ComplexMovingShape {

	private Rectangle2D.Float boundingBox;

	MovingFan(Rectangle2D.Float boundingBox, float speed, float angle, float delta) {
		area = Fan.getShape(boundingBox, speed, angle, delta);
		this.boundingBox = boundingBox;
	}

	Rectangle2D.Float getBoundingBox() {
		return boundingBox;
	}

}
