package org.energy2d.view;

import java.awt.Graphics2D;
import java.awt.Shape;

import org.energy2d.math.Blob2D;

/**
 * @author Charles Xie
 * 
 */
class MovingBlob implements MovingShape {

	private Blob2D blob;

	MovingBlob(Blob2D blob) {
		this.blob = blob;
	}

	public Shape getShape() {
		return blob;
	}

	public void render(Graphics2D g) {
		g.draw(blob.getPath());
	}

}
