package org.energy2d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.energy2d.view.View2D;

public class UndoHeatFluxLines extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private boolean oldValue, newValue;
	private View2D view;

	public UndoHeatFluxLines(View2D view) {
		oldValue = view.isHeatFluxLinesOn();
		this.view = view;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = view.isHeatFluxLinesOn();
		view.setHeatFluxLinesOn(oldValue);
		view.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		view.setHeatFluxLinesOn(newValue);
		view.repaint();
	}

	@Override
	public String getPresentationName() {
		return "Heat Flux Lines";
	}

}
