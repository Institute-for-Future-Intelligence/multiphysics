package org.concord.energy2d.undo;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy2d.math.Blob2D;
import org.concord.energy2d.math.Polygon2D;
import org.concord.energy2d.math.Ring2D;
import org.concord.energy2d.model.Cloud;
import org.concord.energy2d.model.Fan;
import org.concord.energy2d.model.Heliostat;
import org.concord.energy2d.model.Manipulable;
import org.concord.energy2d.model.Model2D;
import org.concord.energy2d.model.Part;
import org.concord.energy2d.model.Particle;
import org.concord.energy2d.model.Tree;
import org.concord.energy2d.view.View2D;

public class UndoResizeManipulable extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private Manipulable selectedManipulable;
	private View2D view;
	private Model2D model;
	private String name;
	private float oldX, oldY, newX, newY;
	private float oldW, oldH, newW, newH;

	public UndoResizeManipulable(View2D view) {
		this.view = view;
		model = view.getModel();
		selectedManipulable = view.getSelectedManipulable();
		if (selectedManipulable instanceof Part) {
			name = "Part";
			Shape shape = selectedManipulable.getShape();
			if (shape instanceof Rectangle2D.Float) {
				Rectangle2D.Float rect = (Rectangle2D.Float) shape;
				oldX = rect.x;
				oldY = rect.y;
				oldW = rect.width;
				oldH = rect.height;
			} else if (shape instanceof Ellipse2D.Float) {
				Ellipse2D.Float elli = (Ellipse2D.Float) shape;
				oldX = elli.x;
				oldY = elli.y;
				oldW = elli.width;
				oldH = elli.height;
			} else if (shape instanceof Polygon2D) {
			} else if (shape instanceof Blob2D) {
			} else if (shape instanceof Ring2D) {
			}
		} else if (selectedManipulable instanceof Fan) {
			name = "Fan";
			Shape shape = selectedManipulable.getShape();
			Rectangle2D.Float rect = (Rectangle2D.Float) shape;
			oldX = rect.x;
			oldY = rect.y;
			oldW = rect.width;
			oldH = rect.height;
		} else if (selectedManipulable instanceof Heliostat) {
			name = "Heliostat";
			Shape shape = selectedManipulable.getShape();
			Rectangle2D.Float rect = (Rectangle2D.Float) shape;
			oldX = rect.x;
			oldY = rect.y;
			oldW = rect.width;
			oldH = rect.height;
		} else if (selectedManipulable instanceof Cloud) {
			name = "Cloud";
			Cloud cloud = (Cloud) selectedManipulable;
			oldX = cloud.getX();
			oldY = cloud.getY();
			oldW = cloud.getWidth();
			oldH = cloud.getHeight();
		} else if (selectedManipulable instanceof Tree) {
			name = "Tree";
			Tree tree = (Tree) selectedManipulable;
			oldX = tree.getX();
			oldY = tree.getY();
			oldW = tree.getWidth();
			oldH = tree.getHeight();
		} else if (selectedManipulable instanceof Particle) {
			name = "Particle";
			Particle particle = (Particle) selectedManipulable;
			oldW = particle.getRadius();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (selectedManipulable instanceof Part) {
			Shape shape = selectedManipulable.getShape();
			if (shape instanceof Rectangle2D.Float) {
				Rectangle2D.Float rect = (Rectangle2D.Float) shape;
				newX = rect.x;
				newY = rect.y;
				newW = rect.width;
				newH = rect.height;
				rect.x = oldX;
				rect.y = oldY;
				rect.width = oldW;
				rect.height = oldH;
			} else if (shape instanceof Ellipse2D.Float) {
				Ellipse2D.Float elli = (Ellipse2D.Float) shape;
				newX = elli.x;
				newY = elli.y;
				newW = elli.width;
				newH = elli.height;
				elli.x = oldX;
				elli.y = oldY;
				elli.width = oldW;
				elli.height = oldH;
			} else if (shape instanceof Polygon2D) {
			} else if (shape instanceof Blob2D) {
			} else if (shape instanceof Ring2D) {
			}
			model.refreshPowerArray();
			model.refreshTemperatureBoundaryArray();
			model.refreshMaterialPropertyArrays();
			if (view.isViewFactorLinesOn())
				model.generateViewFactorMesh();
		} else if (selectedManipulable instanceof Fan) {
			Shape shape = selectedManipulable.getShape();
			Rectangle2D.Float rect = (Rectangle2D.Float) shape;
			newX = rect.x;
			newY = rect.y;
			newW = rect.width;
			newH = rect.height;
			rect.x = oldX;
			rect.y = oldY;
			rect.width = oldW;
			rect.height = oldH;
			model.refreshMaterialPropertyArrays();
		} else if (selectedManipulable instanceof Heliostat) {
			Shape shape = selectedManipulable.getShape();
			Rectangle2D.Float rect = (Rectangle2D.Float) shape;
			newX = rect.x;
			newY = rect.y;
			newW = rect.width;
			newH = rect.height;
			rect.x = oldX;
			rect.y = oldY;
			rect.width = oldW;
			rect.height = oldH;
		} else if (selectedManipulable instanceof Cloud) {
			Cloud cloud = (Cloud) selectedManipulable;
			newX = cloud.getX();
			newY = cloud.getY();
			newW = cloud.getWidth();
			newH = cloud.getHeight();
			cloud.setX(oldX);
			cloud.setY(oldY);
			cloud.setDimension(oldW, oldH);
		} else if (selectedManipulable instanceof Tree) {
			Tree tree = (Tree) selectedManipulable;
			newX = tree.getX();
			newY = tree.getY();
			newW = tree.getWidth();
			newH = tree.getHeight();
			tree.setX(oldX);
			tree.setY(oldY);
			tree.setDimension(oldW, oldH);
		} else if (selectedManipulable instanceof Particle) {
			Particle particle = (Particle) selectedManipulable;
			newW = particle.getRadius();
			particle.setRadius(oldW);
		}
		view.setSelectedManipulable(selectedManipulable);
		view.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (selectedManipulable instanceof Part) {
			Shape shape = selectedManipulable.getShape();
			if (shape instanceof Rectangle2D.Float) {
				Rectangle2D.Float rect = (Rectangle2D.Float) shape;
				rect.x = newX;
				rect.y = newY;
				rect.width = newW;
				rect.height = newH;
			} else if (shape instanceof Ellipse2D.Float) {
				Ellipse2D.Float elli = (Ellipse2D.Float) shape;
				elli.x = newX;
				elli.y = newY;
				elli.width = newW;
				elli.height = newH;
			} else if (shape instanceof Polygon2D) {
			} else if (shape instanceof Blob2D) {
			} else if (shape instanceof Ring2D) {
			}
			model.refreshPowerArray();
			model.refreshTemperatureBoundaryArray();
			model.refreshMaterialPropertyArrays();
			if (view.isViewFactorLinesOn())
				model.generateViewFactorMesh();
		} else if (selectedManipulable instanceof Fan) {
			Shape shape = selectedManipulable.getShape();
			Rectangle2D.Float rect = (Rectangle2D.Float) shape;
			rect.x = newX;
			rect.y = newY;
			rect.width = newW;
			rect.height = newH;
			model.refreshMaterialPropertyArrays();
		} else if (selectedManipulable instanceof Heliostat) {
			Shape shape = selectedManipulable.getShape();
			Rectangle2D.Float rect = (Rectangle2D.Float) shape;
			rect.x = newX;
			rect.y = newY;
			rect.width = newW;
			rect.height = newH;
		} else if (selectedManipulable instanceof Cloud) {
			Cloud cloud = (Cloud) selectedManipulable;
			cloud.setX(newX);
			cloud.setY(newY);
			cloud.setDimension(newW, newH);
		} else if (selectedManipulable instanceof Tree) {
			Tree tree = (Tree) selectedManipulable;
			tree.setX(newX);
			tree.setY(newY);
			tree.setDimension(newW, newH);
		} else if (selectedManipulable instanceof Particle) {
			Particle particle = (Particle) selectedManipulable;
			particle.setRadius(newW);
		}
		view.setSelectedManipulable(selectedManipulable);
		view.repaint();
	}

	@Override
	public String getPresentationName() {
		return "Resize " + (name == null ? "Manipulable" : name);
	}

}
