package org.energy2d.undo;

import java.awt.Shape;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.energy2d.math.Blob2D;
import org.energy2d.math.TransformableShape;
import org.energy2d.model.Fan;
import org.energy2d.model.HeatFluxSensor;
import org.energy2d.model.Manipulable;
import org.energy2d.model.Model2D;
import org.energy2d.model.Part;
import org.energy2d.model.Particle;
import org.energy2d.view.View2D;

public class UndoRotateManipulable extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private Manipulable selectedManipulable;
	private View2D view;
	private Model2D model;
	private String name;
	private float rotationAngle;
	private float oldAngle, newAngle;

	public UndoRotateManipulable(View2D view, float rotationAngle) {
		this.view = view;
		this.rotationAngle = rotationAngle;
		model = view.getModel();
		selectedManipulable = view.getSelectedManipulable();
		if (selectedManipulable instanceof Part) {
			name = "Part";
		} else if (selectedManipulable instanceof Particle) {
			name = "Particle";
		} else if (selectedManipulable instanceof Fan) {
			name = "Fan";
		} else if (selectedManipulable instanceof HeatFluxSensor) {
			name = "Heat Flux Sensor";
			HeatFluxSensor heatFluxSensor = (HeatFluxSensor) selectedManipulable;
			oldAngle = heatFluxSensor.getAngle();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (selectedManipulable instanceof Part) {
			Shape shape = selectedManipulable.getShape();
			if (shape instanceof TransformableShape) {
				TransformableShape s = (TransformableShape) shape;
				s.rotateBy(-rotationAngle);
				if (s instanceof Blob2D) {
					((Blob2D) s).update();
				}
				model.refreshPowerArray();
				model.refreshTemperatureBoundaryArray();
				model.refreshMaterialPropertyArrays();
				if (view.isViewFactorLinesOn())
					model.generateViewFactorMesh();
			}
		} else if (selectedManipulable instanceof Particle) {
		} else if (selectedManipulable instanceof Fan) {
		} else if (selectedManipulable instanceof HeatFluxSensor) {
			HeatFluxSensor heatFluxSensor = (HeatFluxSensor) selectedManipulable;
			newAngle = heatFluxSensor.getAngle();
			heatFluxSensor.setAngle(oldAngle);
		}
		view.setSelectedManipulable(selectedManipulable);
		view.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (selectedManipulable instanceof Part) {
			Shape shape = selectedManipulable.getShape();
			if (shape instanceof TransformableShape) {
				TransformableShape s = (TransformableShape) shape;
				s.rotateBy(rotationAngle);
				if (s instanceof Blob2D) {
					((Blob2D) s).update();
				}
				model.refreshPowerArray();
				model.refreshTemperatureBoundaryArray();
				model.refreshMaterialPropertyArrays();
				if (view.isViewFactorLinesOn())
					model.generateViewFactorMesh();
			}
		} else if (selectedManipulable instanceof Particle) {
		} else if (selectedManipulable instanceof Fan) {
		} else if (selectedManipulable instanceof HeatFluxSensor) {
			HeatFluxSensor heatFluxSensor = (HeatFluxSensor) selectedManipulable;
			heatFluxSensor.setAngle(newAngle);
		}
		view.setSelectedManipulable(selectedManipulable);
		view.repaint();
	}

	@Override
	public String getPresentationName() {
		return "Rotate " + (name == null ? "Manipulable" : name);
	}

}
