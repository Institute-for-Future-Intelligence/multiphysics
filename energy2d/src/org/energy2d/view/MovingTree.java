package org.energy2d.view;

import java.awt.geom.Rectangle2D;

import org.energy2d.model.Tree;

/**
 * @author Charles Xie
 * 
 */
class MovingTree extends ComplexMovingShape {

	MovingTree(Rectangle2D.Float boundingBox, byte type) {
		area = Tree.getShape(boundingBox, type);
	}

}