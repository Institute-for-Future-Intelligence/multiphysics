package org.energy2d.util;

import java.awt.Color;

/**
 * @author Charles Xie
 */

public class ColorFill implements FillPattern {

	private Color color;

	public ColorFill() {
	}

	public ColorFill(Color c) {
		setColor(c);
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color c) {
		color = c;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof ColorFill))
			return false;
		Color c = ((ColorFill) obj).getColor();
		return color.equals(c);
	}

	public int hashCode() {
		return color.hashCode();
	}

	public String toString() {
		return color.toString();
	}

}