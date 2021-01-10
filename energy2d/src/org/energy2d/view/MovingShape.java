package org.energy2d.view;

import java.awt.Graphics2D;
import java.awt.Shape;

/**
 * @author Charles Xie
 * 
 */
public interface MovingShape {

	public Shape getShape();

	public void render(Graphics2D g);

}
