package org.energy2d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.energy2d.view.View2D;

public class UndoViewFactorLines extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private boolean oldValue, newValue;
	private View2D view;

	public UndoViewFactorLines(View2D view) {
		oldValue = view.isViewFactorLinesOn();
		this.view = view;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = view.isViewFactorLinesOn();
		view.setViewFactorLinesOn(oldValue);
		view.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		view.setViewFactorLinesOn(newValue);
		view.repaint();
	}

	@Override
	public String getPresentationName() {
		return "View Factor Lines";
	}

}
