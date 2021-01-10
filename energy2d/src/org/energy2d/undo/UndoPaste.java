package org.energy2d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.energy2d.event.ManipulationEvent;
import org.energy2d.model.Anemometer;
import org.energy2d.model.Cloud;
import org.energy2d.model.Fan;
import org.energy2d.model.HeatFluxSensor;
import org.energy2d.model.Manipulable;
import org.energy2d.model.Model2D;
import org.energy2d.model.Part;
import org.energy2d.model.Particle;
import org.energy2d.model.ParticleFeeder;
import org.energy2d.model.Thermometer;
import org.energy2d.model.Tree;
import org.energy2d.view.Picture;
import org.energy2d.view.TextBox;
import org.energy2d.view.View2D;

public class UndoPaste extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private Manipulable pastedManipulable;
	private View2D view;
	private Model2D model;
	private String name;

	public UndoPaste(Manipulable pastedManipulable, View2D view) {
		this.view = view;
		model = view.getModel();
		this.pastedManipulable = pastedManipulable;
		if (pastedManipulable instanceof Part) {
			name = "Part";
		} else if (pastedManipulable instanceof Thermometer) {
			name = "Thermometer";
		} else if (pastedManipulable instanceof HeatFluxSensor) {
			name = "Heat Flux Sensor";
		} else if (pastedManipulable instanceof Anemometer) {
			name = "Anemometer";
		} else if (pastedManipulable instanceof Particle) {
			name = "Particle";
		} else if (pastedManipulable instanceof ParticleFeeder) {
			name = "Particle Feeder";
		} else if (pastedManipulable instanceof Fan) {
			name = "Fan";
		} else if (pastedManipulable instanceof Cloud) {
			name = "Cloud";
		} else if (pastedManipulable instanceof Tree) {
			name = "Tree";
		} else if (pastedManipulable instanceof TextBox) {
			name = "Text Box";
		} else if (pastedManipulable instanceof Picture) {
			name = "Image";
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (pastedManipulable != null)
			view.notifyManipulationListeners(pastedManipulable, ManipulationEvent.DELETE);
		view.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (pastedManipulable instanceof Part) {
			model.addPart((Part) pastedManipulable);
			model.refreshPowerArray();
			model.refreshTemperatureBoundaryArray();
			model.refreshMaterialPropertyArrays();
			model.setInitialTemperature();
		} else if (pastedManipulable instanceof Thermometer) {
			model.addThermometer((Thermometer) pastedManipulable);
		} else if (pastedManipulable instanceof HeatFluxSensor) {
			model.addHeatFluxSensor((HeatFluxSensor) pastedManipulable);
		} else if (pastedManipulable instanceof Anemometer) {
			model.addAnemometer((Anemometer) pastedManipulable);
		} else if (pastedManipulable instanceof Particle) {
			model.addParticle((Particle) pastedManipulable);
		} else if (pastedManipulable instanceof ParticleFeeder) {
			model.addParticleFeeder((ParticleFeeder) pastedManipulable);
		} else if (pastedManipulable instanceof Fan) {
			model.addFan((Fan) pastedManipulable);
			model.refreshMaterialPropertyArrays();
		} else if (pastedManipulable instanceof Cloud) {
			model.addCloud((Cloud) pastedManipulable);
		} else if (pastedManipulable instanceof Tree) {
			model.addTree((Tree) pastedManipulable);
		} else if (pastedManipulable instanceof TextBox) {
			view.addTextBox((TextBox) pastedManipulable);
		} else if (pastedManipulable instanceof Picture) {
			view.addPicture((Picture) pastedManipulable);
		}
		view.setSelectedManipulable(pastedManipulable);
		view.repaint();
	}

	@Override
	public String getPresentationName() {
		return "Paste " + (name == null ? "Manipulable" : name);
	}

}
