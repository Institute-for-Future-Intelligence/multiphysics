/*
 *   Copyright (C) 2011  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.util;

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
