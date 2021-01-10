package org.energy2d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.energy2d.view.View2D;

public class UndoMaximumTemperature extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private float oldValue, newValue;
	private View2D view;

	public UndoMaximumTemperature(View2D view) {
		oldValue = view.getMaximumTemperature();
		this.view = view;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = view.getMaximumTemperature();
		view.setMaximumTemperature(oldValue);
		view.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		view.setMaximumTemperature(newValue);
		view.repaint();
	}

	@Override
	public String getPresentationName() {
		return "Maximum Temperature";
	}

}
