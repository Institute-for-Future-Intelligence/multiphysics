package org.concord.energy2d.system;

import java.awt.BasicStroke;
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

		// create particle button and its associated popup menu
		ImageIcon particleIcon = new ImageIcon(ToolBar.class.getResource("resources/particle.png"));
		final JToggleButton particleButton = new JToggleButton(particleIcon);
		final JRadioButtonMenuItem rbmiParticle = new JRadioButtonMenuItem("Particle", particleIcon, true);
		Symbol.ParticleFeederIcon particleFeederIcon = new Symbol.ParticleFeederIcon(Color.WHITE, Color.GRAY);
		particleFeederIcon.setStroke(new BasicStroke(3));
		particleFeederIcon.setIconWidth(19);
		particleFeederIcon.setIconHeight(19);
		particleFeederIcon.setMarginX(7);
		particleFeederIcon.setMarginY(7);
		particleFeederIcon.setOffsetX(7);
		particleFeederIcon.setOffsetY(7);
		final JRadioButtonMenuItem rbmiParticleFeeder = new JRadioButtonMenuItem("Particle Feeder", particleFeederIcon, false);
		ActionListener particleChoiceAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JRadioButtonMenuItem selected = (JRadioButtonMenuItem) e.getSource();
				particleButton.setIcon(selected.getIcon());
				if (selected == rbmiParticle) {
					box.view.setActionMode(View2D.PARTICLE_MODE);
					particleButton.setToolTipText("Drop a particle");
				} else if (selected == rbmiParticleFeeder) {
					box.view.setActionMode(View2D.PARTICLE_FEEDER_MODE);
					particleButton.setToolTipText("Drop a particle feeder");
				}
				particleButton.setSelected(true);
				s2d.view.requestFocusInWindow();
			}
		};
		rbmiParticle.addActionListener(particleChoiceAction);
		rbmiParticleFeeder.addActionListener(particleChoiceAction);
		final JPopupMenu particleMenu = new JPopupMenu();
		particleMenu.add(rbmiParticle);
		particleMenu.add(rbmiParticleFeeder);
		ButtonGroup bgParticle = new ButtonGroup();
		bgParticle.add(rbmiParticle);
		bgParticle.add(rbmiParticleFeeder);

		particleButton.setToolTipText("Drop a particle");
		particleButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (rbmiParticle.isSelected())
					box.view.setActionMode(View2D.PARTICLE_MODE);
				else if (rbmiParticleFeeder.isSelected())
					box.view.setActionMode(View2D.PARTICLE_FEEDER_MODE);
			}
		});
		add(particleButton);
		bg.add(particleButton);

		JButton arrowButton = new JButton();
		Dimension d = new Dimension(12, x.getMaximumSize().height);
		arrowButton.setMaximumSize(d);
		arrowButton.setIcon(new Symbol.ArrowHead(Color.BLACK, d.width, d.height));
		arrowButton.setToolTipText("Click to select the particle action using the button to the left");
		arrowButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				particleMenu.show(particleButton, 0, particleButton.getHeight());
			}
		});
		arrowButton.setBorder(BorderFactory.createEmptyBorder());
		arrowButton.setFocusPainted(false);
		add(arrowButton);

		// create sensor button and its associated popup menu
		Symbol.Thermometer thermometerIcon = new Symbol.Thermometer();
		thermometerIcon.setValue(5);
		thermometerIcon.setIconWidth(4);
		thermometerIcon.setIconHeight(24);
		thermometerIcon.setMarginX(14);
		thermometerIcon.setMarginY(4);
		thermometerIcon.setOffsetX(14);
		thermometerIcon.setOffsetY(4);
		final JToggleButton sensorButton = new JToggleButton(thermometerIcon);
		final JRadioButtonMenuItem rbmiThermometer = new JRadioButtonMenuItem("Thermometer", thermometerIcon, true);
		Symbol heatFluxSensorIcon = new Symbol.HeatFluxSensor();
		heatFluxSensorIcon.setIconWidth(24);
		heatFluxSensorIcon.setIconHeight(10);
		heatFluxSensorIcon.setMarginX(4);
		heatFluxSensorIcon.setMarginY(11);
		heatFluxSensorIcon.setOffsetX(4);
		heatFluxSensorIcon.setOffsetY(11);
		final JRadioButtonMenuItem rbmiHeatFluxSensor = new JRadioButtonMenuItem("Heat Flux Sensor", heatFluxSensorIcon, false);
		Symbol anemometerIcon = new Symbol.Anemometer();
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
		final JPopupMenu sensorMenu = new JPopupMenu();
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

		arrowButton = new JButton();
		arrowButton.setMaximumSize(d);
		arrowButton.setIcon(new Symbol.ArrowHead(Color.BLACK, d.width, d.height));
		arrowButton.setToolTipText("Click to select the sensor type to add using the button to the left");
		arrowButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
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

		JButton button = new JButton(new ImageIcon(ToolBar.class.getResource("resources/zoomin.png")));
		button.setBorderPainted(false);
		button.setToolTipText("Halve the size of the simulation box");
		button.addActionListener(box.view.getActionMap().get("Zoom In"));
		add(button);

		button = new JButton(new ImageIcon(ToolBar.class.getResource("resources/zoomout.png")));
		button.setBorderPainted(false);
		button.setToolTipText("Double the size of the simulation box");
		button.addActionListener(box.view.getActionMap().get("Zoom Out"));
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
