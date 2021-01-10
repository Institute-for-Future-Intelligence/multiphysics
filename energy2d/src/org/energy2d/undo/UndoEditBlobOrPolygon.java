package org.energy2d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.energy2d.math.Blob2D;
import org.energy2d.model.Part;
import org.energy2d.view.View2D;

public class UndoEditBlobOrPolygon extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private View2D view;
	private Part oldPart, newPart;

	public UndoEditBlobOrPolygon(View2D view, Part oldPart, Part newPart) {
		this.view = view;
		this.oldPart = oldPart;
		this.newPart = newPart;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		view.getModel().removePart(newPart);
		view.getModel().addPart(oldPart);
		view.getModel().refreshPowerArray();
		view.getModel().refreshTemperatureBoundaryArray();
		view.getModel().refreshMaterialPropertyArrays();
		if (view.isViewFactorLinesOn())
			view.getModel().generateViewFactorMesh();
		view.setSelectedManipulable(oldPart);
		view.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		view.getModel().removePart(oldPart);
		view.getModel().addPart(newPart);
		view.getModel().refreshPowerArray();
		view.getModel().refreshTemperatureBoundaryArray();
		view.getModel().refreshMaterialPropertyArrays();
		if (view.isViewFactorLinesOn())
			view.getModel().generateViewFactorMesh();
		view.setSelectedManipulable(newPart);
		view.repaint();
	}

	@Override
	public String getPresentationName() {
		return oldPart.getShape() instanceof Blob2D ? "Edit Blob" : "Edit Polygon";
	}

}
