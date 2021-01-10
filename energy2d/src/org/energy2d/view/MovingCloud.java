package org.energy2d.view;

import java.awt.geom.Rectangle2D;

import org.energy2d.model.Cloud;

/**
 * @author Charles Xie
 * 
 */
class MovingCloud extends ComplexMovingShape {

	MovingCloud(Rectangle2D.Float boundingBox) {
		area = Cloud.getShape(boundingBox);
	}

}