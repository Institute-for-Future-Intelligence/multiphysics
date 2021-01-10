package org.energy2d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.energy2d.view.View2D;

public class UndoScaleAll extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private float scaleFactor = 1;
	private View2D view;

	public UndoScaleAll(View2D view, float scaleFactor) {
		this.scaleFactor = scaleFactor;
		this.view = view;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		view.scaleAll(1.0f / scaleFactor);
		view.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		view.scaleAll(scaleFactor);
		view.repaint();
	}

	@Override
	public String getPresentationName() {
		return "Scale All";
	}

}
