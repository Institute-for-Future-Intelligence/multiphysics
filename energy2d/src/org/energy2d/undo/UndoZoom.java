package org.energy2d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.energy2d.view.View2D;

public class UndoZoom extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private float extent = 1;
	private View2D view;

	public UndoZoom(View2D view, float extent) {
		this.extent = extent;
		this.view = view;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		view.zoom(1.0f / extent);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		view.zoom(extent);
	}

	@Override
	public String getPresentationName() {
		return "Zoom";
	}

}
