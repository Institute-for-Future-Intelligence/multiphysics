/*
 *   Copyright (C) 2012  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.geom.Rectangle2D;

import org.concord.energy2d.model.Tree;

/**
 * @author Charles Xie
 * 
 */
class MovingTree extends ComplexMovingShape {

	MovingTree(Rectangle2D.Float boundingBox, byte type) {
		area = Tree.getShape(boundingBox, type);
	}

}