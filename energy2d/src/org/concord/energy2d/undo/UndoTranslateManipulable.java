package org.concord.energy2d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy2d.model.Anemometer;
import org.concord.energy2d.model.Cloud;
import org.concord.energy2d.model.Fan;
import org.concord.energy2d.model.HeatFluxSensor;
import org.concord.energy2d.model.Manipulable;
import org.concord.energy2d.model.Model2D;
import org.concord.energy2d.model.Part;
import org.concord.energy2d.model.Particle;
import org.concord.energy2d.model.ParticleFeeder;
import org.concord.energy2d.model.Thermometer;
import org.concord.energy2d.model.Tree;
import org.concord.energy2d.view.TextBox;
import org.concord.energy2d.view.View2D;

public class UndoTranslateManipulable extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private Manipulable selectedManipulable;
	private View2D view;
	private Model2D model;
	private String name;
	private float oldX, oldY, newX, newY;

	public UndoTranslateManipulable(View2D view) {
		this.view = view;
		model = view.getModel();
		selectedManipulable = view.getSelectedManipulable();
		if (selectedManipulable instanceof Part) {
			name = "Part";
		} else if (selectedManipulable instanceof Thermometer) {
			name = "Thermometer";
			Thermometer thermometer = (Thermometer) selectedManipulable;
			oldX = thermometer.getX();
			oldY = thermometer.getY();
		} else if (selectedManipulable instanceof HeatFluxSensor) {
			name = "Heat Flux Sensor";
			HeatFluxSensor heatFluxSensor = (HeatFluxSensor) selectedManipulable;
			oldX = heatFluxSensor.getX();
			oldY = heatFluxSensor.getY();
		} else if (selectedManipulable instanceof Anemometer) {
			name = "Anemometer";
			Anemometer anemometer = (Anemometer) selectedManipulable;
			oldX = anemometer.getX();
			oldY = anemometer.getY();
		} else if (selectedManipulable instanceof Particle) {
			name = "Particle";
			Particle particle = (Particle) selectedManipulable;
			oldX = particle.getRx();
			oldY = particle.getRy();
		} else if (selectedManipulable instanceof ParticleFeeder) {
			name = "Particle Feeder";
			ParticleFeeder particleFeeder = (ParticleFeeder) selectedManipulable;
			oldX = particleFeeder.getX();
			oldY = particleFeeder.getY();
		} else if (selectedManipulable instanceof Fan) {
			name = "Fan";
		} else if (selectedManipulable instanceof Cloud) {
			name = "Cloud";
		} else if (selectedManipulable instanceof Tree) {
			name = "Tree";
		} else if (selectedManipulable instanceof TextBox) {
			name = "Text Box";
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (selectedManipulable instanceof Particle) {
			Particle particle = (Particle) selectedManipulable;
			newX = particle.getRx();
			newY = particle.getRy();
			particle.setRx(oldX);
			particle.setRy(oldY);
		} else if (selectedManipulable instanceof ParticleFeeder) {
			ParticleFeeder particleFeeder = (ParticleFeeder) selectedManipulable;
			newX = particleFeeder.getX();
			newY = particleFeeder.getY();
			particleFeeder.setX(oldX);
			particleFeeder.setY(oldY);
		} else if (selectedManipulable instanceof Thermometer) {
			Thermometer thermometer = (Thermometer) selectedManipulable;
			newX = thermometer.getX();
			newY = thermometer.getY();
			thermometer.setX(oldX);
			thermometer.setY(oldY);
		} else if (selectedManipulable instanceof HeatFluxSensor) {
			HeatFluxSensor heatFluxSensor = (HeatFluxSensor) selectedManipulable;
			newX = heatFluxSensor.getX();
			newY = heatFluxSensor.getY();
			heatFluxSensor.setX(oldX);
			heatFluxSensor.setY(oldY);
		} else if (selectedManipulable instanceof Anemometer) {
			Anemometer anemometer = (Anemometer) selectedManipulable;
			newX = anemometer.getX();
			newY = anemometer.getY();
			anemometer.setX(oldX);
			anemometer.setY(oldY);
		}
		view.setSelectedManipulable(selectedManipulable);
		view.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (selectedManipulable instanceof Part) {
			model.refreshPowerArray();
			model.refreshTemperatureBoundaryArray();
			model.refreshMaterialPropertyArrays();
			model.setInitialTemperature();
		} else if (selectedManipulable instanceof Thermometer) {
			Thermometer thermometer = (Thermometer) selectedManipulable;
			thermometer.setX(newX);
			thermometer.setY(newY);
		} else if (selectedManipulable instanceof HeatFluxSensor) {
			HeatFluxSensor heatFluxSensor = (HeatFluxSensor) selectedManipulable;
			heatFluxSensor.setX(newX);
			heatFluxSensor.setY(newY);
		} else if (selectedManipulable instanceof Anemometer) {
			Anemometer anemometer = (Anemometer) selectedManipulable;
			anemometer.setX(newX);
			anemometer.setY(newY);
		} else if (selectedManipulable instanceof Particle) {
			Particle particle = (Particle) selectedManipulable;
			particle.setRx(newX);
			particle.setRy(newY);
		} else if (selectedManipulable instanceof ParticleFeeder) {
			ParticleFeeder particleFeeder = (ParticleFeeder) selectedManipulable;
			particleFeeder.setX(newX);
			particleFeeder.setY(newY);
		} else if (selectedManipulable instanceof Fan) {
			model.refreshMaterialPropertyArrays();
		} else if (selectedManipulable instanceof Cloud) {
		} else if (selectedManipulable instanceof Tree) {
		} else if (selectedManipulable instanceof TextBox) {
		}
		view.setSelectedManipulable(selectedManipulable);
		view.repaint();
	}

	@Override
	public String getPresentationName() {
		return "Move " + (name == null ? "Manipulable" : name);
	}

}
