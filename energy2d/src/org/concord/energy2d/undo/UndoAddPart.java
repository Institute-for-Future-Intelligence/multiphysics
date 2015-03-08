package org.concord.energy2d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.model.Model2D;
import org.concord.energy2d.model.Part;
import org.concord.energy2d.view.View2D;

public class UndoAddPart extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private Part addedPart;
	private View2D view;
	private Model2D model;

	public UndoAddPart(View2D view) {
		this.view = view;
		model = view.getModel();
		addedPart = model.getPart(model.getPartCount() - 1);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		view.notifyManipulationListeners(addedPart, ManipulationEvent.DELETE);
		view.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		model.addPart(addedPart);
		model.refreshPowerArray();
		model.refreshTemperatureBoundaryArray();
		model.refreshMaterialPropertyArrays();
		model.setInitialTemperature();
		view.repaint();
	}

	@Override
	public String getPresentationName() {
		return "Add Part";
	}

}
