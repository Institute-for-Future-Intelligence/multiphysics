/*
 *   Copyright (C) 2013  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.math;

import java.awt.Shape;
import java.awt.geom.Point2D;

/**
 * @author Charles Xie
 * 
 */
public interface TransformableShape extends Shape {

	public Point2D.Float getCenter();
	
	public float getArea();
	
	public void translateBy(float dx, float dy);

	public void rotateBy(float degree);

	public void scale(float scale);

	public void scaleX(float scale);

	public void scaleY(float scale);

	public void shearX(float shear);

	public void shearY(float shear);

	public void flipX();

	public void flipY();

}
