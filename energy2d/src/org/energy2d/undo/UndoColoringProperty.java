package org.energy2d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.energy2d.view.View2D;

public class UndoColoringProperty extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private byte oldValue, newValue;
	private View2D view;

	public UndoColoringProperty(View2D view) {
		oldValue = view.getHeatMapType();
		this.view = view;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = view.getHeatMapType();
		view.setHeatMapType(oldValue);
		view.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		view.setHeatMapType(newValue);
		view.repaint();
	}

	@Override
	public String getPresentationName() {
		return "Coloring Property Change";
	}

}
