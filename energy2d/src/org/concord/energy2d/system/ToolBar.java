/*
 *   Copyright (C) 2010  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.system;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.concord.energy2d.event.GraphEvent;
import org.concord.energy2d.event.GraphListener;
import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.event.ManipulationListener;
import org.concord.energy2d.util.MiscUtil;
import org.concord.energy2d.view.View2D;

/**
 * @author Charles Xie
 * 
 */
class ToolBar extends JToolBar implements GraphListener, ToolBarListener, ManipulationListener {

	private JToggleButton graphButton;
	private JToggleButton selectButton;
	private JToggleButton heatingButton;

	private System2D box;

	ToolBar(System2D s2d) {

		super(HORIZONTAL);
		setFloatable(false);

		box = s2d;

		box.view.addGraphListener(this);

		ButtonGroup bg = new ButtonGroup();

		selectButton = new JToggleButton(new ImageIcon(ToolBar.class.getResource("resources/select.png")));
		selectButton.setToolTipText("Select and move an object");
		selectButton.setSelected(true);
		selectButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				box.view.setActionMode(View2D.SELECT_MODE);
			}
		});
		add(selectButton);
		bg.add(selectButton);

		JToggleButton x = new JToggleButton(new ImageIcon(ToolBar.class.getResource("resources/rectangle.png")));
		x.setToolTipText("Draw a rectangle");
		x.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				box.view.setActionMode(View2D.RECTANGLE_MODE);
			}
		});
		x.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MiscUtil.setSelectedSilently(graphButton, false);
			}
		});
		add(x);
		bg.add(x);

		x = new JToggleButton(new ImageIcon(ToolBar.class.getResource("resources/ellipse.png")));
		x.setToolTipText("Draw an ellipse");
		x.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				box.view.setActionMode(View2D.ELLIPSE_MODE);
			}
		});
		x.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MiscUtil.setSelectedSilently(graphButton, false);
			}
		});
		add(x);
		bg.add(x);

		x = new JToggleButton(new ImageIcon(ToolBar.class.getResource("resources/polygon.png")));
		x.setToolTipText("Draw a polygon");
		x.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				box.view.setActionMode(View2D.POLYGON_MODE);
			}
		});
		x.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MiscUtil.setSelectedSilently(graphButton, false);
			}
		});
		add(x);
		bg.add(x);

		x = new JToggleButton(new ImageIcon(ToolBar.class.getResource("resources/blob.png")));
		x.setToolTipText("Draw a blob");
		x.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				box.view.setActionMode(View2D.BLOB_MODE);
			}
		});
		x.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MiscUtil.setSelectedSilently(graphButton, false);
			}
		});
		add(x);
		bg.add(x);

		heatingButton = new JToggleButton(new ImageIcon(ToolBar.class.getResource("resources/heat.png")));
		heatingButton.setToolTipText("Click to heat, shift-click to cool");
		heatingButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				box.view.setActionMode(View2D.HEATING_MODE);
			}
		});
		heatingButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MiscUtil.setSelectedSilently(graphButton, false);
			}
		});
		add(heatingButton);
		bg.add(heatingButton);

		x = new JToggleButton(new ImageIcon(ToolBar.class.getResource("resources/thermometer.png")));
		x.setToolTipText("Add a thermometer");
		x.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				box.view.setActionMode(View2D.THERMOMETER_MODE);
				graphButton.setEnabled(true);
			}
		});
		x.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MiscUtil.setSelectedSilently(graphButton, false);
			}
		});
		add(x);
		bg.add(x);

		graphButton = new JToggleButton(new ImageIcon(ToolBar.class.getResource("resources/graph.png")));
		graphButton.setEnabled(false);
		graphButton.setToolTipText("Show or hide graphs");
		graphButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JToggleButton src = (JToggleButton) e.getSource();
				box.view.setGraphOn(src.isSelected());
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		add(graphButton);

		JButton button = new JButton(new ImageIcon(ToolBar.class.getResource("resources/zoomin.png")));
		button.setToolTipText("Halve the size of the simulation box");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (box.model.getTime() > 0) {
					JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(box.view), "Sorry, the simulation must be reset before this action can be taken.", "Cannot zoom now", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				float lx = box.model.getLx();
				float ly = box.model.getLy();
				box.model.setLx(lx * 0.5f);
				box.model.setLy(ly * 0.5f);
				box.model.translateAllBy(0, -ly * 0.5f); // fix the y-flip problem
				box.view.setArea(0, lx * 0.5f, 0, ly * 0.5f);
				box.model.refreshPowerArray();
				box.model.refreshTemperatureBoundaryArray();
				box.model.refreshMaterialPropertyArrays();
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		add(button);

		button = new JButton(new ImageIcon(ToolBar.class.getResource("resources/zoomout.png")));
		button.setToolTipText("Double the size of the simulation box");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (box.model.getTime() > 0) {
					JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(box.view), "Sorry, the simulation must be reset before this action can be taken.", "Cannot zoom now", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				float lx = box.model.getLx();
				float ly = box.model.getLy();
				box.model.setLx(2 * lx);
				box.model.setLy(2 * ly);
				box.model.translateAllBy(0, ly); // fix the y-flip problem
				box.view.setArea(0, 2 * lx, 0, 2 * ly);
				box.model.refreshPowerArray();
				box.model.refreshTemperatureBoundaryArray();
				box.model.refreshMaterialPropertyArrays();
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		add(button);

	}

	public void graphClosed(GraphEvent e) {
		MiscUtil.setSelectedSilently(graphButton, false);
	}

	public void graphOpened(GraphEvent e) {
		MiscUtil.setSelectedSilently(graphButton, true);
	}

	public void manipulationOccured(ManipulationEvent e) {
		switch (e.getType()) {
		case ManipulationEvent.GRAPH:
			MiscUtil.setSelectedSilently(graphButton, box.model.hasSensor());
			break;
		case ManipulationEvent.OBJECT_ADDED:
		case ManipulationEvent.SELECT_MODE_CHOSEN:
		case ManipulationEvent.RESET:
			selectButton.doClick();
			break;
		case ManipulationEvent.HEATING_MODE_CHOSEN:
			heatingButton.doClick();
			break;
		case ManipulationEvent.SENSOR_ADDED:
			graphButton.setEnabled(true);
			break;
		}
	}

	public void tableBarShouldChange(ToolBarEvent e) {
		switch (e.getType()) {
		case ToolBarEvent.FILE_INPUT:
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					selectButton.doClick();
					selectButton.requestFocusInWindow();
					MiscUtil.setSelectedSilently(graphButton, box.view.isGraphOn());
					graphButton.setEnabled(box.model.hasSensor());
				}
			});
			break;
		case ToolBarEvent.RESET:
			selectButton.doClick();
			break;
		case ToolBarEvent.NEW_FILE:
			selectButton.doClick();
			if (graphButton.isSelected())
				MiscUtil.setSelectedSilently(graphButton, false);
			graphButton.setEnabled(false);
			break;
		}
	}

}
