package org.energy2d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.energy2d.view.View2D;

public class UndoColorPalette extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private boolean oldValue, newValue;
	private View2D view;

	public UndoColorPalette(View2D view) {
		oldValue = view.isColorPaletteOn();
		this.view = view;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = view.isColorPaletteOn();
		view.setColorPaletteOn(oldValue);
		view.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		view.setColorPaletteOn(newValue);
		view.repaint();
	}

	@Override
	public String getPresentationName() {
		return "Color Palette";
	}

}
