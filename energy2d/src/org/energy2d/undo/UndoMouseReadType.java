package org.energy2d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.energy2d.view.View2D;

public class UndoMouseReadType extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private byte oldValue, newValue;
	private View2D view;

	public UndoMouseReadType(View2D view) {
		oldValue = view.getMouseReadType();
		this.view = view;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = view.getMouseReadType();
		view.setMouseReadType(oldValue);
		view.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		view.setMouseReadType(newValue);
		view.repaint();
	}

	@Override
	public String getPresentationName() {
		return "Mouse Read Type Change";
	}

}
