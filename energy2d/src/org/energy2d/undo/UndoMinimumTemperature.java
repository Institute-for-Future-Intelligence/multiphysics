package org.energy2d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.energy2d.view.View2D;

public class UndoMinimumTemperature extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private float oldValue, newValue;
	private View2D view;

	public UndoMinimumTemperature(View2D view) {
		oldValue = view.getMinimumTemperature();
		this.view = view;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = view.getMinimumTemperature();
		view.setMinimumTemperature(oldValue);
		view.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		view.setMinimumTemperature(newValue);
		view.repaint();
	}

	@Override
	public String getPresentationName() {
		return "Minimum Temperature";
	}

}
