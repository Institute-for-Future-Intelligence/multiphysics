package org.energy2d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.energy2d.view.View2D;

public class UndoGridSize extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private int oldValue, newValue;
	private View2D view;

	public UndoGridSize(View2D view) {
		oldValue = view.getGridSize();
		this.view = view;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = view.getGridSize();
		view.setGridSize(oldValue);
		view.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		view.setGridSize(newValue);
		view.repaint();
	}

	@Override
	public String getPresentationName() {
		return "Grid Size";
	}

}
