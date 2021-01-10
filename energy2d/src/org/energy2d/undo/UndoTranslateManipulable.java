package org.energy2d.undo;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.energy2d.math.Blob2D;
import org.energy2d.math.EllipticalAnnulus;
import org.energy2d.math.Polygon2D;
import org.energy2d.math.Annulus;
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
			saveShape();
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
			saveShape();
		} else if (selectedManipulable instanceof Heliostat) {
			name = "Heliostat";
			saveShape();
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
		} else if (selectedManipulable instanceof Cloud) {
			name = "Cloud";
			Cloud cloud = (Cloud) selectedManipulable;
			oldX = cloud.getX();
			oldY = cloud.getY();
		} else if (selectedManipulable instanceof Tree) {
			name = "Tree";
			Tree tree = (Tree) selectedManipulable;
			oldX = tree.getX();
			oldY = tree.getY();
		} else if (selectedManipulable instanceof TextBox) {
			name = "Text Box";
			TextBox textBox = (TextBox) selectedManipulable;
			oldX = textBox.getX();
			oldY = textBox.getY();
		} else if (selectedManipulable instanceof Picture) {
			name = "Image";
			Picture picture = (Picture) selectedManipulable;
			oldX = picture.getX();
			oldY = picture.getY();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (selectedManipulable instanceof Part) {
			undoShape();
			model.refreshPowerArray();
			model.refreshTemperatureBoundaryArray();
			model.refreshMaterialPropertyArrays();
			model.refreshHeliostatsAimedAt((Part) selectedManipulable);
			if (view.isViewFactorLinesOn())
				model.generateViewFactorMesh();
		} else if (selectedManipulable instanceof Particle) {
			Particle particle = (Particle) selectedManipulable;
			newX = particle.getRx();
			newY = particle.getRy();
			particle.setRx(oldX);
			particle.setRy(oldY);
			model.attachSensors();
		} else if (selectedManipulable instanceof ParticleFeeder) {
			ParticleFeeder particleFeeder = (ParticleFeeder) selectedManipulable;
			newX = particleFeeder.getX();
			newY = particleFeeder.getY();
			particleFeeder.setX(oldX);
			particleFeeder.setY(oldY);
		} else if (selectedManipulable instanceof Fan) {
			undoShape();
			model.refreshMaterialPropertyArrays();
		} else if (selectedManipulable instanceof Heliostat) {
			undoShape();
			((Heliostat) selectedManipulable).setAngle();
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
		} else if (selectedManipulable instanceof Cloud) {
			Cloud cloud = (Cloud) selectedManipulable;
			newX = cloud.getX();
			newY = cloud.getY();
			cloud.setX(oldX);
			cloud.setY(oldY);
		} else if (selectedManipulable instanceof Tree) {
			Tree tree = (Tree) selectedManipulable;
			newX = tree.getX();
			newY = tree.getY();
			tree.setX(oldX);
			tree.setY(oldY);
		} else if (selectedManipulable instanceof TextBox) {
			TextBox textBox = (TextBox) selectedManipulable;
			newX = textBox.getX();
			newY = textBox.getY();
			textBox.setX(oldX);
			textBox.setY(oldY);
		} else if (selectedManipulable instanceof Picture) {
			Picture picture = (Picture) selectedManipulable;
			newX = picture.getX();
			newY = picture.getY();
			picture.setX(oldX);
			picture.setY(oldY);
		}
		view.setSelectedManipulable(selectedManipulable);
		view.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (selectedManipulable instanceof Part) {
			redoShape();
			model.refreshPowerArray();
			model.refreshTemperatureBoundaryArray();
			model.refreshMaterialPropertyArrays();
			model.refreshHeliostatsAimedAt((Part) selectedManipulable);
			if (view.isViewFactorLinesOn())
				model.generateViewFactorMesh();
		} else if (selectedManipulable instanceof Particle) {
			Particle particle = (Particle) selectedManipulable;
			particle.setRx(newX);
			particle.setRy(newY);
			model.attachSensors();
		} else if (selectedManipulable instanceof ParticleFeeder) {
			ParticleFeeder particleFeeder = (ParticleFeeder) selectedManipulable;
			particleFeeder.setX(newX);
			particleFeeder.setY(newY);
		} else if (selectedManipulable instanceof Fan) {
			redoShape();
			model.refreshMaterialPropertyArrays();
		} else if (selectedManipulable instanceof Heliostat) {
			redoShape();
			((Heliostat) selectedManipulable).setAngle();
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
		} else if (selectedManipulable instanceof Cloud) {
			Cloud cloud = (Cloud) selectedManipulable;
			cloud.setX(newX);
			cloud.setY(newY);
		} else if (selectedManipulable instanceof Tree) {
			Tree tree = (Tree) selectedManipulable;
			tree.setX(newX);
			tree.setY(newY);
		} else if (selectedManipulable instanceof TextBox) {
			TextBox textBox = (TextBox) selectedManipulable;
			textBox.setX(newX);
			textBox.setY(newY);
		} else if (selectedManipulable instanceof Picture) {
			Picture picture = (Picture) selectedManipulable;
			picture.setX(newX);
			picture.setY(newY);
		}
		view.setSelectedManipulable(selectedManipulable);
		view.repaint();
	}

	private void saveShape() {
		Shape shape = selectedManipulable.getShape();
		if (shape instanceof Rectangle2D.Float) {
			Rectangle2D.Float rect = (Rectangle2D.Float) shape;
			oldX = rect.x;
			oldY = rect.y;
		} else if (shape instanceof Ellipse2D.Float) {
			Ellipse2D.Float elli = (Ellipse2D.Float) shape;
			oldX = elli.x;
			oldY = elli.y;
		} else if (shape instanceof Polygon2D) {
			Polygon2D poly = (Polygon2D) shape;
			Point2D.Float center = poly.getCenter();
			oldX = center.x;
			oldY = center.y;
		} else if (shape instanceof Blob2D) {
			Blob2D blob = (Blob2D) shape;
			Point2D.Float center = blob.getCenter();
			oldX = center.x;
			oldY = center.y;
		} else if (shape instanceof Annulus) {
			Annulus ring = (Annulus) shape;
			oldX = ring.getX();
			oldY = ring.getY();
		} else if (shape instanceof EllipticalAnnulus) {
			EllipticalAnnulus e = (EllipticalAnnulus) shape;
			oldX = e.getX();
			oldY = e.getY();
		}
	}

	private void undoShape() {
		Shape shape = selectedManipulable.getShape();
		if (shape instanceof Rectangle2D.Float) {
			Rectangle2D.Float rect = (Rectangle2D.Float) shape;
			newX = rect.x;
			newY = rect.y;
			rect.x = oldX;
			rect.y = oldY;
		} else if (shape instanceof Ellipse2D.Float) {
			Ellipse2D.Float elli = (Ellipse2D.Float) shape;
			newX = elli.x;
			newY = elli.y;
			elli.x = oldX;
			elli.y = oldY;
		} else if (shape instanceof Polygon2D) {
			Polygon2D poly = (Polygon2D) shape;
			Point2D.Float center = poly.getCenter();
			newX = center.x;
			newY = center.y;
			poly.translateCenterTo(oldX, oldY);
		} else if (shape instanceof Blob2D) {
			Blob2D blob = (Blob2D) shape;
			Point2D.Float center = blob.getCenter();
			newX = center.x;
			newY = center.y;
			blob.translateCenterTo(oldX, oldY);
			blob.update();
		} else if (shape instanceof Annulus) {
			Annulus ring = (Annulus) shape;
			newX = ring.getX();
			newY = ring.getY();
			ring.translateTo(oldX, oldY);
		} else if (shape instanceof EllipticalAnnulus) {
			EllipticalAnnulus e = (EllipticalAnnulus) shape;
			newX = e.getX();
			newY = e.getY();
			e.translateTo(oldX, oldY);
		}
	}

	private void redoShape() {
		Shape shape = selectedManipulable.getShape();
		if (shape instanceof Rectangle2D.Float) {
			Rectangle2D.Float rect = (Rectangle2D.Float) shape;
			rect.x = newX;
			rect.y = newY;
		} else if (shape instanceof Ellipse2D.Float) {
			Ellipse2D.Float elli = (Ellipse2D.Float) shape;
			elli.x = newX;
			elli.y = newY;
		} else if (shape instanceof Polygon2D) {
			Polygon2D poly = (Polygon2D) shape;
			poly.translateCenterTo(newX, newY);
		} else if (shape instanceof Blob2D) {
			Blob2D blob = (Blob2D) shape;
			blob.translateCenterTo(newX, newY);
			blob.update();
		} else if (shape instanceof Annulus) {
			Annulus ring = (Annulus) shape;
			ring.translateTo(newX, newY);
		} else if (shape instanceof EllipticalAnnulus) {
			EllipticalAnnulus e = (EllipticalAnnulus) shape;
			e.translateTo(newX, newY);
		}
	}

	@Override
	public String getPresentationName() {
		return "Move " + (name == null ? "Manipulable" : name);
	}

}
