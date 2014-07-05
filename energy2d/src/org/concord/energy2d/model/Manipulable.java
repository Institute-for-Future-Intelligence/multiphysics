/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.model;

import java.awt.Shape;
import java.awt.geom.Point2D;

import org.concord.energy2d.math.TransformableShape;

/**
 * @author Charles Xie
 * 
 */
public abstract class Manipulable {

	private boolean selected;
	private Shape shape;
	private boolean draggable = true;
	private boolean visible = true;
	private String label;
	private String uid;

	public Manipulable(Shape shape) {
		setShape(shape);
	}

	public abstract Manipulable duplicate(float x, float y);

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getUid() {
		return uid;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public boolean contains(float x, float y) {
		return shape.contains(x, y);
	}

	public void setShape(Shape shape) {
		this.shape = shape;
	}

	public Shape getShape() {
		return shape;
	}

	public Point2D.Float getCenter() {
		if (shape instanceof TransformableShape)
			return ((TransformableShape) shape).getCenter();
		return new Point2D.Float((float) shape.getBounds2D().getCenterX(), (float) shape.getBounds2D().getCenterY());
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setDraggable(boolean draggable) {
		this.draggable = draggable;
	}

	public boolean isDraggable() {
		return draggable;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isVisible() {
		return visible;
	}

}
