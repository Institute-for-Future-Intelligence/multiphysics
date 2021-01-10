package org.energy2d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.energy2d.view.View2D;

public class UndoControlPanel extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private boolean oldValue, newValue;
	private View2D view;

	public UndoControlPanel(View2D view) {
		oldValue = view.isControlPanelVisible();
		this.view = view;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = view.isControlPanelVisible();
		view.setControlPanelVisible(oldValue);
		view.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		view.setControlPanelVisible(newValue);
		view.repaint();
	}

	@Override
	public String getPresentationName() {
		return "Control Panel";
	}

}
