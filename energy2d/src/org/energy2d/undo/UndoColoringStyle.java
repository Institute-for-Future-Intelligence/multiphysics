package org.energy2d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.energy2d.view.View2D;

public class UndoColoringStyle extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private byte oldValue, newValue;
	private View2D view;

	public UndoColoringStyle(View2D view) {
		oldValue = view.getColorPaletteType();
		this.view = view;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = view.getColorPaletteType();
		view.setColorPaletteType(oldValue);
		view.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		view.setColorPaletteType(newValue);
		view.repaint();
	}

	@Override
	public String getPresentationName() {
		return "Coloring Style Change";
	}

}
