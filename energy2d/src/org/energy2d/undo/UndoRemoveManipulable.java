package org.energy2d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.energy2d.event.ManipulationEvent;
import org.energy2d.model.Anemometer;
import org.energy2d.model.Cloud;
import org.energy2d.model.Fan;
import org.energy2d.model.HeatFluxSensor;
import org.energy2d.model.Heliostat;
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

public class UndoRemoveManipulable extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private Manipulable selectedManipulable;
	private int index = 0;
	private View2D view;
	private Model2D model;
	private String name;

	public UndoRemoveManipulable(View2D view) {
		this.view = view;
		model = view.getModel();
		selectedManipulable = view.getSelectedManipulable();
		if (selectedManipulable instanceof Part) {
			index = model.getParts().indexOf(selectedManipulable);
			name = "Part";
		} else if (selectedManipulable instanceof Fan) {
			index = model.getFans().indexOf(selectedManipulable);
			name = "Fan";
		} else if (selectedManipulable instanceof Heliostat) {
			index = model.getHeliostats().indexOf(selectedManipulable);
			name = "Heliostat";
		} else if (selectedManipulable instanceof Particle) {
			index = model.getParticles().indexOf(selectedManipulable);
			name = "Particle";
		} else if (selectedManipulable instanceof ParticleFeeder) {
			index = model.getParticleFeeders().indexOf(selectedManipulable);
			name = "Particle Feedder";
		} else if (selectedManipulable instanceof Anemometer) {
			index = model.getAnemometers().indexOf(selectedManipulable);
			name = "Anemometer";
		} else if (selectedManipulable instanceof HeatFluxSensor) {
			index = model.getHeatFluxSensors().indexOf(selectedManipulable);
			name = "Heat Flux Sensor";
		} else if (selectedManipulable instanceof Thermometer) {
			index = model.getThermometers().indexOf(selectedManipulable);
			name = "Thermometer";
		} else if (selectedManipulable instanceof Cloud) {
			index = model.getClouds().indexOf(selectedManipulable);
			name = "Cloud";
		} else if (selectedManipulable instanceof Tree) {
			index = model.getTrees().indexOf(selectedManipulable);
			name = "Tree";
		} else if (selectedManipulable instanceof TextBox) {
			index = view.getTextBoxes().indexOf(selectedManipulable);
			name = "Text Box";
		} else if (selectedManipulable instanceof Picture) {
			index = view.getPictures().indexOf(selectedManipulable);
			name = "Image";
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
		} else if (selectedManipulable instanceof Fan) {
			model.addFan((Fan) selectedManipulable, index);
			model.refreshMaterialPropertyArrays();
		} else if (selectedManipulable instanceof Heliostat) {
			model.addHeliostat((Heliostat) selectedManipulable, index);
		} else if (selectedManipulable instanceof Particle) {
			model.addParticle((Particle) selectedManipulable, index);
		} else if (selectedManipulable instanceof ParticleFeeder) {
			model.addParticleFeeder((ParticleFeeder) selectedManipulable, index);
		} else if (selectedManipulable instanceof Anemometer) {
			model.addAnemometer((Anemometer) selectedManipulable, index);
		} else if (selectedManipulable instanceof HeatFluxSensor) {
			model.addHeatFluxSensor((HeatFluxSensor) selectedManipulable, index);
		} else if (selectedManipulable instanceof Thermometer) {
			model.addThermometer((Thermometer) selectedManipulable, index);
		} else if (selectedManipulable instanceof Cloud) {
			model.addCloud((Cloud) selectedManipulable, index);
		} else if (selectedManipulable instanceof Tree) {
			model.addTree((Tree) selectedManipulable, index);
		} else if (selectedManipulable instanceof TextBox) {
			view.addTextBox((TextBox) selectedManipulable, index);
		} else if (selectedManipulable instanceof Picture) {
			view.addPicture((Picture) selectedManipulable, index);
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
		return "Remove " + (name == null ? "Manipulable" : name);
	}

}
