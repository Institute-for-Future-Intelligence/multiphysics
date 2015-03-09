package org.concord.energy2d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.model.Manipulable;
import org.concord.energy2d.model.Model2D;
import org.concord.energy2d.model.Part;
import org.concord.energy2d.model.Particle;
import org.concord.energy2d.view.View2D;

public class UndoRemoveManipulable extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private Manipulable selectedManipulable;
	private int index = 0;
	private View2D view;
	private Model2D model;

	public UndoRemoveManipulable(View2D view) {
		this.view = view;
		model = view.getModel();
		selectedManipulable = view.getSelectedManipulable();
		if (selectedManipulable instanceof Part) {
			index = model.getParts().indexOf(selectedManipulable);
		} else if (selectedManipulable instanceof Particle) {
			index = model.getParticles().indexOf(selectedManipulable);
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (selectedManipulable instanceof Part) {
			model.addPart((Part) selectedManipulable, index);
			model.refreshPowerArray();
			model.refreshTemperatureBoundaryArray();
			model.refreshMaterialPropertyArrays();
		} else if (selectedManipulable instanceof Particle) {
			model.addParticle((Particle) selectedManipulable, index);
		}
		view.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		view.notifyManipulationListeners(selectedManipulable, ManipulationEvent.DELETE);
		view.repaint();
	}

	@Override
	public String getPresentationName() {
		return "Remove Manipulable";
	}

}
