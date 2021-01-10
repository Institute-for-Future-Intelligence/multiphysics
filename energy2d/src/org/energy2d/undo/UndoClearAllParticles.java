package org.energy2d.undo;

import java.util.ArrayList;
import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.energy2d.model.Particle;
import org.energy2d.view.View2D;

public class UndoClearAllParticles extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private List<Particle> particles;
	private View2D view;

	public UndoClearAllParticles(View2D view) {
		particles = new ArrayList<Particle>();
		particles.addAll(view.getModel().getParticles());
		this.view = view;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (!particles.isEmpty()) {
			view.getModel().getParticles().addAll(particles);
		}
		view.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		view.getModel().getParticles().clear();
		view.repaint();
	}

	@Override
	public String getPresentationName() {
		return "Clear All Particles";
	}

}
