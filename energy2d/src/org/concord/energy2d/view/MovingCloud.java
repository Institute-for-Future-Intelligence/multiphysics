/*
 *   Copyright (C) 2012  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.geom.Rectangle2D;

import org.concord.energy2d.model.Cloud;

/**
 * @author Charles Xie
 * 
 */
class MovingCloud extends ComplexMovingShape {

	MovingCloud(Rectangle2D.Float boundingBox) {
		area = Cloud.getShape(boundingBox);
	}

}