package org.energy2d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.energy2d.view.View2D;

public class UndoTickmarks extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private boolean oldValue, newValue;
	private View2D view;

	public UndoTickmarks(View2D view) {
		oldValue = view.isBorderTickmarksOn();
		this.view = view;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = view.isBorderTickmarksOn();
		view.setBorderTickmarksOn(oldValue);
		view.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		view.setBorderTickmarksOn(newValue);
		view.repaint();
	}

	@Override
	public String getPresentationName() {
		return "Border Tickmarks";
	}

}
