package org.energy2d.util;

import java.awt.Color;
import java.util.EventObject;

/**
 * @author Charles Xie
 * 
 */

class ColorArrayEvent extends EventObject {

	private Color selectedColor;

	public ColorArrayEvent(Object source, Color c) {
		super(source);
		selectedColor = c;
	}

	public Color getSelectedColor() {
		return selectedColor;
	}

}
