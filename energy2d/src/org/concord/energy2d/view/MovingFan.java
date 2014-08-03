package org.concord.energy2d.view;

import java.awt.geom.Rectangle2D;

import org.concord.energy2d.model.Fan;

/**
 * @author Charles Xie
 * 
 */
class MovingFan extends ComplexMovingShape {

	MovingFan(Rectangle2D.Float boundingBox, float speed, float angle, float delta) {
		area = Fan.getShape(boundingBox, speed, angle, delta);
	}

}
