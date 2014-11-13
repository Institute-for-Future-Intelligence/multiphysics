package org.concord.energy2d.system;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.concord.energy2d.event.GraphEvent;
import org.concord.energy2d.event.GraphListener;
import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.event.ManipulationListener;
import org.concord.energy2d.util.MiscUtil;
import org.concord.energy2d.view.Symbol;
import org.concord.energy2d.view.View2D;

/**
 * @author Charles Xie
 * 
 */
class ToolBar extends JToolBar implements GraphListener, ToolBarListener, ManipulationListener {

	private JToggleButton graphButton;
	private JToggleButton selectButton;
	private JToggleButton heatingButton;

	private JPopupMenu sensorMenu;
	private JToggleButton sensorButton;

	private System2D box;

	ToolBar(final System2D s2d) {

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

		// create sensor button and its associated popup menu
		Symbol thermometerIcon = Symbol.get("Thermometer");
		thermometerIcon.setIconWidth(5);
		thermometerIcon.setIconHeight(25);
		thermometerIcon.setMarginX(13);
		thermometerIcon.setMarginY(3);
		thermometerIcon.setOffsetX(13);
		thermometerIcon.setOffsetY(2);
		sensorButton = new JToggleButton(thermometerIcon);
		final JRadioButtonMenuItem rbmiThermometer = new JRadioButtonMenuItem("Thermometer", thermometerIcon, true);
		Symbol heatFluxSensorIcon = Symbol.get("Heat Flux Sensor");
		heatFluxSensorIcon.setIconWidth(24);
		heatFluxSensorIcon.setIconHeight(10);
		heatFluxSensorIcon.setMarginX(4);
		heatFluxSensorIcon.setMarginY(11);
		heatFluxSensorIcon.setOffsetX(4);
		heatFluxSensorIcon.setOffsetY(11);
		final JRadioButtonMenuItem rbmiHeatFluxSensor = new JRadioButtonMenuItem("Heat Flux Sensor", heatFluxSensorIcon, false);
		Symbol anemometerIcon = Symbol.get("Anemometer");
		anemometerIcon.setIconWidth(24);
		anemometerIcon.setIconHeight(24);
		anemometerIcon.setMarginX(4);
		anemometerIcon.setMarginY(4);
		anemometerIcon.setOffsetX(4);
		anemometerIcon.setOffsetY(4);
		final JRadioButtonMenuItem rbmiAnemometer = new JRadioButtonMenuItem("Anemometer", anemometerIcon, false);
		ActionListener sensorChoiceAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JRadioButtonMenuItem selected = (JRadioButtonMenuItem) e.getSource();
				sensorButton.setIcon(selected.getIcon());
				if (selected == rbmiThermometer) {
					box.view.setActionMode(View2D.THERMOMETER_MODE);
					sensorButton.setToolTipText("Drop a thermometer");
				} else if (selected == rbmiHeatFluxSensor) {
					box.view.setActionMode(View2D.HEAT_FLUX_SENSOR_MODE);
					sensorButton.setToolTipText("Drop a heat flux sensor");
				} else if (selected == rbmiAnemometer) {
					box.view.setActionMode(View2D.ANEMOMETER_MODE);
					sensorButton.setToolTipText("Drop an anemometer");
				}
				sensorButton.setSelected(true);
				s2d.view.requestFocusInWindow();
			}
		};
		rbmiThermometer.addActionListener(sensorChoiceAction);
		rbmiHeatFluxSensor.addActionListener(sensorChoiceAction);
		rbmiAnemometer.addActionListener(sensorChoiceAction);
		sensorMenu = new JPopupMenu();
		sensorMenu.add(rbmiThermometer);
		sensorMenu.add(rbmiHeatFluxSensor);
		sensorMenu.add(rbmiAnemometer);
		ButtonGroup bgSensor = new ButtonGroup();
		bgSensor.add(rbmiThermometer);
		bgSensor.add(rbmiHeatFluxSensor);
		bgSensor.add(rbmiAnemometer);

		sensorButton.setToolTipText("Drop a thermometer");
		sensorButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (rbmiThermometer.isSelected())
					box.view.setActionMode(View2D.THERMOMETER_MODE);
				else if (rbmiHeatFluxSensor.isSelected())
					box.view.setActionMode(View2D.HEAT_FLUX_SENSOR_MODE);
				else if (rbmiAnemometer.isSelected())
					box.view.setActionMode(View2D.ANEMOMETER_MODE);
				graphButton.setEnabled(true);
			}
		});
		sensorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MiscUtil.setSelectedSilently(graphButton, false);
			}
		});
		add(sensorButton);
		bg.add(sensorButton);

		JButton arrowButton = new JButton();
		Dimension d = new Dimension(12, x.getMaximumSize().height);
		arrowButton.setMaximumSize(d);
		arrowButton.setIcon(new Symbol.ArrowHead(Color.BLACK, d.width, d.height));
		arrowButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				sensorMenu.show(sensorButton, 0, sensorButton.getHeight());
			}
		});
		arrowButton.setBorder(BorderFactory.createEmptyBorder());
		arrowButton.setFocusPainted(false);
		add(arrowButton);

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
