package org.energy2d.view;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

/**
 * @author Charles Xie
 * 
 */
class MovingParticleFeeder implements MovingShape {

	private RoundRectangle2D.Float rectangle;

	MovingParticleFeeder(RoundRectangle2D.Float rectangle) {
		this.rectangle = rectangle;
	}

	public Shape getShape() {
		return rectangle;
	}

	public void render(Graphics2D g) {
		g.draw(rectangle);
	}

}
