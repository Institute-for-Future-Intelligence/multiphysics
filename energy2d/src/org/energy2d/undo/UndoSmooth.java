package org.energy2d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.energy2d.view.View2D;

public class UndoSmooth extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private boolean oldValue, newValue;
	private View2D view;

	public UndoSmooth(View2D view) {
		oldValue = view.isSmooth();
		this.view = view;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = view.isSmooth();
		view.setSmooth(oldValue);
		view.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		view.setSmooth(newValue);
		view.repaint();
	}

	@Override
	public String getPresentationName() {
		return "Smooth";
	}

}
