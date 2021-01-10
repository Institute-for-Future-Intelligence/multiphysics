package org.energy2d.system;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.energy2d.event.GraphEvent;
import org.energy2d.event.GraphListener;
import org.energy2d.event.ManipulationEvent;
import org.energy2d.event.ManipulationListener;
import org.energy2d.util.MiscUtil;
import org.energy2d.view.Symbol;
import org.energy2d.view.View2D;

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
		addMouseOverEffect(selectButton);

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
		addMouseOverEffect(x);

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
		addMouseOverEffect(x);

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
		addMouseOverEffect(x);

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
		addMouseOverEffect(x);

		x = new JToggleButton(new ImageIcon(ToolBar.class.getResource("resources/annulus.png")));
		x.setToolTipText("Draw an annulus");
		x.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				box.view.setActionMode(View2D.ANNULUS_MODE);
			}
		});
		x.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MiscUtil.setSelectedSilently(graphButton, false);
			}
		});
		add(x);
		bg.add(x);
		addMouseOverEffect(x);

		// create particle button and its associated popup menu
		ImageIcon particleIcon = new ImageIcon(ToolBar.class.getResource("resources/particle.png"));
		final JToggleButton miscButton = new JToggleButton(particleIcon);
		final JRadioButtonMenuItem rbmiParticle = new JRadioButtonMenuItem("Particle", particleIcon, true);
		// Symbol.ParticleFeederIcon particleFeederIcon = new Symbol.ParticleFeederIcon(Color.WHITE, Color.GRAY, true);
		// particleFeederIcon.setStroke(new BasicStroke(3));
		// particleFeederIcon.setIconWidth(19);
		// particleFeederIcon.setIconHeight(19);
		// particleFeederIcon.setMarginX(7);
		// particleFeederIcon.setMarginY(7);
		// particleFeederIcon.setOffsetX(7);
		// particleFeederIcon.setOffsetY(7);
		ImageIcon particleFeederIcon = new ImageIcon(ToolBar.class.getResource("resources/particlefeeder.png"));
		final JRadioButtonMenuItem rbmiParticleFeeder = new JRadioButtonMenuItem("Particle Feeder", particleFeederIcon, false);
		// Symbol.FanIcon fanIcon = new Symbol.FanIcon(Color.WHITE, Color.BLACK, true);
		// fanIcon.setIconWidth(32);
		// fanIcon.setIconHeight(32);
		// fanIcon.setSymbolWidth(32);
		// fanIcon.setSymbolHeight(32);
		// fanIcon.setOffsetX(1);
		// fanIcon.setOffsetY(1);
		ImageIcon fanIcon = new ImageIcon(ToolBar.class.getResource("resources/fan.png"));
		final JRadioButtonMenuItem rbmiFan = new JRadioButtonMenuItem("Fan", fanIcon, false);
		// Symbol.HeliostatIcon heliostatIcon = new Symbol.HeliostatIcon(Color.WHITE, Color.BLACK, true);
		// heliostatIcon.setAngle((float) Math.PI / 8);
		// heliostatIcon.setIconWidth(32);
		// heliostatIcon.setIconHeight(32);
		// heliostatIcon.setSymbolWidth(32);
		// heliostatIcon.setSymbolHeight(32);
		// heliostatIcon.setOffsetX(1);
		// heliostatIcon.setOffsetY(1);
		ImageIcon heliostatIcon = new ImageIcon(ToolBar.class.getResource("resources/heliostat.png"));
		final JRadioButtonMenuItem rbmiHeliostat = new JRadioButtonMenuItem("Heliostat", heliostatIcon, false);
		// Symbol.CloudIcon cloudIcon = new Symbol.CloudIcon(true);
		// cloudIcon.setBorderColor(Color.BLACK);
		// cloudIcon.setIconWidth(32);
		// cloudIcon.setIconHeight(32);
		// cloudIcon.setSymbolWidth(32);
		// cloudIcon.setSymbolHeight(16);
		// cloudIcon.setMarginY(8);
		// cloudIcon.setOffsetY(8);
		ImageIcon cloudIcon = new ImageIcon(ToolBar.class.getResource("resources/cloud.png"));
		final JRadioButtonMenuItem rbmiCloud = new JRadioButtonMenuItem("Cloud", cloudIcon, false);
		// Symbol.TreeIcon treeIcon = new Symbol.TreeIcon(Tree.PINE, true);
		// treeIcon.setBorderColor(Color.BLACK);
		// treeIcon.setIconWidth(32);
		// treeIcon.setIconHeight(32);
		// treeIcon.setSymbolWidth(20);
		// treeIcon.setSymbolHeight(32);
		// treeIcon.setMarginX(6);
		// treeIcon.setOffsetX(6);
		ImageIcon treeIcon = new ImageIcon(ToolBar.class.getResource("resources/tree.png"));
		final JRadioButtonMenuItem rbmiTree = new JRadioButtonMenuItem("Tree", treeIcon, false);
		ActionListener miscChoiceAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JRadioButtonMenuItem selected = (JRadioButtonMenuItem) e.getSource();
				miscButton.setIcon(selected.getIcon());
				if (selected == rbmiParticle) {
					box.view.setActionMode(View2D.PARTICLE_MODE);
					miscButton.setToolTipText("Drop a particle");
				} else if (selected == rbmiParticleFeeder) {
					box.view.setActionMode(View2D.PARTICLE_FEEDER_MODE);
					miscButton.setToolTipText("Drop a particle feeder");
				} else if (selected == rbmiFan) {
					box.view.setActionMode(View2D.FAN_MODE);
					miscButton.setToolTipText("Drop a fan");
				} else if (selected == rbmiHeliostat) {
					box.view.setActionMode(View2D.HELIOSTAT_MODE);
					miscButton.setToolTipText("Drop a heliostat");
				} else if (selected == rbmiCloud) {
					box.view.setActionMode(View2D.CLOUD_MODE);
					miscButton.setToolTipText("Drop a cloud");
				} else if (selected == rbmiTree) {
					box.view.setActionMode(View2D.TREE_MODE);
					miscButton.setToolTipText("Drop a tree");
				}
				miscButton.setSelected(true);
				s2d.view.requestFocusInWindow();
			}
		};
		rbmiParticle.addActionListener(miscChoiceAction);
		rbmiParticleFeeder.addActionListener(miscChoiceAction);
		rbmiFan.addActionListener(miscChoiceAction);
		rbmiHeliostat.addActionListener(miscChoiceAction);
		rbmiCloud.addActionListener(miscChoiceAction);
		rbmiTree.addActionListener(miscChoiceAction);
		final JPopupMenu miscMenu = new JPopupMenu();
		miscMenu.add(rbmiParticle);
		miscMenu.add(rbmiParticleFeeder);
		miscMenu.add(rbmiFan);
		miscMenu.add(rbmiHeliostat);
		miscMenu.add(rbmiCloud);
		miscMenu.add(rbmiTree);
		ButtonGroup bgMisc = new ButtonGroup();
		bgMisc.add(rbmiParticle);
		bgMisc.add(rbmiParticleFeeder);
		bgMisc.add(rbmiFan);
		bgMisc.add(rbmiHeliostat);
		bgMisc.add(rbmiCloud);
		bgMisc.add(rbmiTree);

		miscButton.setToolTipText("Drop a particle");
		miscButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (rbmiParticle.isSelected())
					box.view.setActionMode(View2D.PARTICLE_MODE);
				else if (rbmiParticleFeeder.isSelected())
					box.view.setActionMode(View2D.PARTICLE_FEEDER_MODE);
				else if (rbmiFan.isSelected())
					box.view.setActionMode(View2D.FAN_MODE);
				else if (rbmiHeliostat.isSelected())
					box.view.setActionMode(View2D.HELIOSTAT_MODE);
				else if (rbmiCloud.isSelected())
					box.view.setActionMode(View2D.CLOUD_MODE);
				else if (rbmiTree.isSelected())
					box.view.setActionMode(View2D.TREE_MODE);
			}
		});
		add(miscButton);
		bg.add(miscButton);
		addMouseOverEffect(miscButton);

		JButton arrowButton = new JButton();
		Dimension d = new Dimension(12, x.getMaximumSize().height);
		arrowButton.setMaximumSize(d);
		arrowButton.setIcon(new Symbol.ArrowHead(Color.BLACK, d.width, d.height));
		arrowButton.setToolTipText("Click to select the action using the button to the left");
		arrowButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				miscMenu.show(miscButton, 0, miscButton.getHeight());
			}
		});
		arrowButton.setBorder(BorderFactory.createEmptyBorder());
		arrowButton.setFocusPainted(false);
		add(arrowButton);

		// create sensor button and its associated popup menu
		// Symbol.Thermometer thermometerIcon = new Symbol.Thermometer(true);
		// thermometerIcon.setValue(5);
		// thermometerIcon.setIconWidth(4);
		// thermometerIcon.setIconHeight(24);
		// thermometerIcon.setMarginX(14);
		// thermometerIcon.setMarginY(4);
		// thermometerIcon.setOffsetX(14);
		// thermometerIcon.setOffsetY(4);
		ImageIcon thermometerIcon = new ImageIcon(ToolBar.class.getResource("resources/thermometer.png"));
		final JToggleButton sensorButton = new JToggleButton(thermometerIcon);
		final JRadioButtonMenuItem rbmiThermometer = new JRadioButtonMenuItem("Thermometer", thermometerIcon, true);
		// Symbol heatFluxSensorIcon = new Symbol.HeatFluxSensor(true);
		// heatFluxSensorIcon.setIconWidth(24);
		// heatFluxSensorIcon.setIconHeight(10);
		// heatFluxSensorIcon.setMarginX(4);
		// heatFluxSensorIcon.setMarginY(11);
		// heatFluxSensorIcon.setOffsetX(4);
		// heatFluxSensorIcon.setOffsetY(11);
		ImageIcon heatFluxSensorIcon = new ImageIcon(ToolBar.class.getResource("resources/heatfluxsensor.png"));
		final JRadioButtonMenuItem rbmiHeatFluxSensor = new JRadioButtonMenuItem("Heat Flux Sensor", heatFluxSensorIcon, false);
		// Symbol anemometerIcon = new Symbol.Anemometer(true);
		// anemometerIcon.setIconWidth(24);
		// anemometerIcon.setIconHeight(24);
		// anemometerIcon.setMarginX(4);
		// anemometerIcon.setMarginY(4);
		// anemometerIcon.setOffsetX(4);
		// anemometerIcon.setOffsetY(4);
		ImageIcon anemometerIcon = new ImageIcon(ToolBar.class.getResource("resources/anemometer.png"));
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
		addMouseOverEffect(sensorButton);

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
		addMouseOverEffect(graphButton);

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
		addMouseOverEffect(heatingButton);

		JButton button = new JButton(new ImageIcon(ToolBar.class.getResource("resources/zoomin.png")));
		button.setBorderPainted(false);
		button.setToolTipText("Halve the size of the simulation box");
		button.addActionListener(box.view.getActionMap().get("Zoom In"));
		add(button);
		addMouseOverEffect(button);

		button = new JButton(new ImageIcon(ToolBar.class.getResource("resources/zoomout.png")));
		button.setBorderPainted(false);
		button.setToolTipText("Double the size of the simulation box");
		button.addActionListener(box.view.getActionMap().get("Zoom Out"));
		add(button);
		addMouseOverEffect(button);

	}

	private static void addMouseOverEffect(final AbstractButton button) {
		if (System.getProperty("os.name").startsWith("Mac")) { // Mac OS X does not have the same behavior as Windows 10, so we mimic it for Mac
			final Color defaultColor = button.getBackground();
			button.setOpaque(true);
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseExited(final MouseEvent e) {
					button.setBackground(defaultColor);
				}

				@Override
				public void mouseEntered(final MouseEvent e) {
					button.setBackground(SystemColor.controlLtHighlight);
				}
			});
		}
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
