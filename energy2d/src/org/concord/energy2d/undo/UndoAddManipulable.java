package org.concord.energy2d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.model.Anemometer;
import org.concord.energy2d.model.HeatFluxSensor;
import org.concord.energy2d.model.Manipulable;
import org.concord.energy2d.model.Model2D;
import org.concord.energy2d.model.Part;
import org.concord.energy2d.model.Particle;
import org.concord.energy2d.model.ParticleFeeder;
import org.concord.energy2d.model.Thermometer;
import org.concord.energy2d.view.View2D;

public class UndoAddManipulable extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private Manipulable addedManipulable;
	private View2D view;
	private Model2D model;
	private String name;

	public UndoAddManipulable(Manipulable addedManipulable, View2D view) {
		this.view = view;
		model = view.getModel();
		this.addedManipulable = addedManipulable;
		if (addedManipulable instanceof Part) {
			name = "Part";
		} else if (addedManipulable instanceof Thermometer) {
			name = "Thermometer";
		} else if (addedManipulable instanceof HeatFluxSensor) {
			name = "Heat Flux Sensor";
		} else if (addedManipulable instanceof Anemometer) {
			name = "Anemometer";
		} else if (addedManipulable instanceof Particle) {
			name = "Particle";
		} else if (addedManipulable instanceof ParticleFeeder) {
			name = "Particle Feeder";
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (addedManipulable != null)
			view.notifyManipulationListeners(addedManipulable, ManipulationEvent.DELETE);
		view.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (addedManipulable instanceof Part) {
			model.addPart((Part) addedManipulable);
			model.refreshPowerArray();
			model.refreshTemperatureBoundaryArray();
			model.refreshMaterialPropertyArrays();
			model.setInitialTemperature();
		} else if (addedManipulable instanceof Thermometer) {
			model.addThermometer((Thermometer) addedManipulable);
		} else if (addedManipulable instanceof HeatFluxSensor) {
			model.addHeatFluxSensor((HeatFluxSensor) addedManipulable);
		} else if (addedManipulable instanceof Anemometer) {
			model.addAnemometer((Anemometer) addedManipulable);
		} else if (addedManipulable instanceof Particle) {
			model.addParticle((Particle) addedManipulable);
		} else if (addedManipulable instanceof ParticleFeeder) {
			model.addParticleFeeder((ParticleFeeder) addedManipulable);
		}
		view.repaint();
	}

	@Override
	public String getPresentationName() {
		return "Add " + (name == null ? "Manipulable" : name);
	}

}
