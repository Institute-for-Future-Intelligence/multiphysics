package org.concord.energy2d.view;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/**
 * @author Charles Xie
 * 
 */
class MovingParticleFeeder implements MovingShape {

	private Rectangle2D.Float rectangle;

	MovingParticleFeeder(Rectangle2D.Float rectangle) {
		this.rectangle = rectangle;
	}

	public Shape getShape() {
		return rectangle;
	}

	public void render(Graphics2D g) {
		g.draw(rectangle);
	}

}
