package org.energy2d.undo;

import java.awt.event.KeyEvent;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.energy2d.view.View2D;

public class UndoTranslateAll extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private View2D view;
	private int keyCode;
	private float delta;

	public UndoTranslateAll(View2D view, int keyCode, float delta) {
		this.view = view;
		this.keyCode = keyCode;
		this.delta = delta;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		switch (keyCode) {
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_RIGHT:
			view.translateAllBy(-delta, 0);
			break;
		case KeyEvent.VK_UP:
		case KeyEvent.VK_DOWN:
			view.translateAllBy(0, -delta);
			break;
		}
		view.getModel().refreshMaterialPropertyArrays();
		view.getModel().refreshPowerArray();
		view.getModel().refreshTemperatureBoundaryArray();
		view.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		switch (keyCode) {
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_RIGHT:
			view.translateAllBy(delta, 0);
			break;
		case KeyEvent.VK_UP:
		case KeyEvent.VK_DOWN:
			view.translateAllBy(0, delta);
			break;
		}
		view.getModel().refreshMaterialPropertyArrays();
		view.getModel().refreshPowerArray();
		view.getModel().refreshTemperatureBoundaryArray();
		view.repaint();
	}

	@Override
	public String getPresentationName() {
		return "Translate All";
	}

}
