package org.energy2d.undo;

import java.util.ArrayList;
import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.energy2d.model.Anemometer;
import org.energy2d.model.Cloud;
import org.energy2d.model.Fan;
import org.energy2d.model.HeatFluxSensor;
import org.energy2d.model.Heliostat;
import org.energy2d.model.Part;
import org.energy2d.model.Particle;
import org.energy2d.model.ParticleFeeder;
import org.energy2d.model.Photon;
import org.energy2d.model.Thermometer;
import org.energy2d.model.Thermostat;
import org.energy2d.model.Tree;
import org.energy2d.view.Picture;
import org.energy2d.view.TextBox;
import org.energy2d.view.View2D;

public class UndoClearAll extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private List<Part> parts;
	private List<Particle> particles;
	private List<ParticleFeeder> particleFeeders;
	private List<Fan> fans;
	private List<Heliostat> heliostats;
	private List<Photon> photons;
	private List<Anemometer> anemometers;
	private List<Thermometer> thermometers;
	private List<HeatFluxSensor> heatFluxSensors;
	private List<Thermostat> thermostats;
	private List<Cloud> clouds;
	private List<Tree> trees;
	private List<TextBox> textBoxes;
	private List<Picture> pictures;
	private View2D view;

	public UndoClearAll(View2D view) {
		parts = new ArrayList<Part>();
		parts.addAll(view.getModel().getParts());
		particles = new ArrayList<Particle>();
		particles.addAll(view.getModel().getParticles());
		particleFeeders = new ArrayList<ParticleFeeder>();
		particleFeeders.addAll(view.getModel().getParticleFeeders());
		fans = new ArrayList<Fan>();
		fans.addAll(view.getModel().getFans());
		heliostats = new ArrayList<Heliostat>();
		heliostats.addAll(view.getModel().getHeliostats());
		photons = new ArrayList<Photon>();
		photons.addAll(view.getModel().getPhotons());
		anemometers = new ArrayList<Anemometer>();
		anemometers.addAll(view.getModel().getAnemometers());
		thermometers = new ArrayList<Thermometer>();
		thermometers.addAll(view.getModel().getThermometers());
		heatFluxSensors = new ArrayList<HeatFluxSensor>();
		heatFluxSensors.addAll(view.getModel().getHeatFluxSensors());
		thermostats = new ArrayList<Thermostat>();
		thermostats.addAll(view.getModel().getThermostats());
		clouds = new ArrayList<Cloud>();
		clouds.addAll(view.getModel().getClouds());
		trees = new ArrayList<Tree>();
		trees.addAll(view.getModel().getTrees());
		textBoxes = new ArrayList<TextBox>();
		textBoxes.addAll(view.getTextBoxes());
		pictures = new ArrayList<Picture>();
		pictures.addAll(view.getPictures());
		this.view = view;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (!parts.isEmpty()) {
			view.getModel().getParts().addAll(parts);
		}
		if (!particles.isEmpty()) {
			view.getModel().getParticles().addAll(particles);
		}
		if (!particleFeeders.isEmpty()) {
			view.getModel().getParticleFeeders().addAll(particleFeeders);
		}
		if (!fans.isEmpty()) {
			view.getModel().getFans().addAll(fans);
		}
		if (!heliostats.isEmpty()) {
			view.getModel().getHeliostats().addAll(heliostats);
		}
		if (!photons.isEmpty()) {
			view.getModel().getPhotons().addAll(photons);
		}
		if (!anemometers.isEmpty()) {
			view.getModel().getAnemometers().addAll(anemometers);
		}
		if (!thermometers.isEmpty()) {
			view.getModel().getThermometers().addAll(thermometers);
		}
		if (!heatFluxSensors.isEmpty()) {
			view.getModel().getHeatFluxSensors().addAll(heatFluxSensors);
		}
		if (!thermostats.isEmpty()) {
			view.getModel().getThermostats().addAll(thermostats);
		}
		if (!clouds.isEmpty()) {
			view.getModel().getClouds().addAll(clouds);
		}
		if (!trees.isEmpty()) {
			view.getModel().getTrees().addAll(trees);
		}
		if (!textBoxes.isEmpty()) {
			view.getTextBoxes().addAll(textBoxes);
		}
		if (!pictures.isEmpty()) {
			view.getPictures().addAll(pictures);
		}
		view.getModel().refreshMaterialPropertyArrays();
		view.getModel().refreshPowerArray();
		view.getModel().refreshTemperatureBoundaryArray();
		view.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		view.getModel().clear();
		view.getModel().refreshMaterialPropertyArrays();
		view.getModel().refreshPowerArray();
		view.getModel().refreshTemperatureBoundaryArray();
		view.clear();
		view.repaint();
	}

	@Override
	public String getPresentationName() {
		return "Clear All";
	}

}
