package org.concord.energy2d.view;

import javax.swing.Icon;

/**
 * @author Charles Xie
 * 
 */
public class Picture {

	private Icon image;
	private float x, y;

	public Picture(Icon image, float x, float y) {
		setImage(image);
		setLocation(x, y);
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getX() {
		return x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getY() {
		return y;
	}

	public void setLocation(float x, float y) {
		setX(x);
		setY(y);
	}

	public void setImage(Icon image) {
		this.image = image;
	}

	public Icon getImage() {
		return image;
	}

}
