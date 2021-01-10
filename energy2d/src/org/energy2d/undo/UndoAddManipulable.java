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
		} else if (addedManipulable instanceof Fan) {
			name = "Fan";
		} else if (addedManipulable instanceof Heliostat) {
			name = "Heliostat";
		} else if (addedManipulable instanceof Cloud) {
			name = "Cloud";
		} else if (addedManipulable instanceof Tree) {
			name = "Tree";
		} else if (addedManipulable instanceof TextBox) {
			name = "Text Box";
		} else if (addedManipulable instanceof Picture) {
			name = "Image";
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
			if (view.isViewFactorLinesOn())
				model.generateViewFactorMesh();
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
		} else if (addedManipulable instanceof Fan) {
			model.addFan((Fan) addedManipulable);
			model.refreshMaterialPropertyArrays();
		} else if (addedManipulable instanceof Heliostat) {
			model.addHeliostat((Heliostat) addedManipulable);
		} else if (addedManipulable instanceof Cloud) {
			model.addCloud((Cloud) addedManipulable);
		} else if (addedManipulable instanceof Tree) {
			model.addTree((Tree) addedManipulable);
		} else if (addedManipulable instanceof TextBox) {
			view.addTextBox((TextBox) addedManipulable);
		} else if (addedManipulable instanceof Picture) {
			view.addPicture((Picture) addedManipulable);
		}
		view.setSelectedManipulable(addedManipulable);
		view.repaint();
	}

	@Override
	public String getPresentationName() {
		return "Add " + (name == null ? "Manipulable" : name);
	}

}
