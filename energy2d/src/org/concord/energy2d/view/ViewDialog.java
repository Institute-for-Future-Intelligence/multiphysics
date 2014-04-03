/*
 *   Copyright (C) 2010  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.concord.energy2d.event.GraphEvent;
import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.util.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class ViewDialog extends JDialog {

	private Window owner;
	private JTextField lowerTempField, upperTempField;
	private JLabel nameLabel1, nameLabel2, unitLabel1, unitLabel2;

	ViewDialog(final View2D view, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "View Options", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		owner = getOwner();

		ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				float x = parse(lowerTempField.getText());
				if (Float.isNaN(x))
					return;
				view.setMinimumTemperature(x);
				x = parse(upperTempField.getText());
				if (Float.isNaN(x))
					return;
				view.setMaximumTemperature(x);
				view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
				view.repaint();
				dispose();
			}
		};

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
				view.repaint();
				dispose();
			}
		});

		JPanel panel = new JPanel(new BorderLayout());
		setContentPane(panel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(buttonPanel, BorderLayout.SOUTH);

		JButton button = new JButton("OK");
		button.addActionListener(okListener);
		buttonPanel.add(button);

		JTabbedPane tab = new JTabbedPane();
		panel.add(tab, BorderLayout.CENTER);
		tab.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel p2 = new JPanel(new BorderLayout());
		JPanel p = new JPanel(new SpringLayout());
		p2.add(p, BorderLayout.NORTH);
		tab.add(p2, "General");
		int count = 0;

		JCheckBox checkBox = new JCheckBox("Isotherm");
		checkBox.setSelected(view.isIsothermOn());
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox src = (JCheckBox) e.getSource();
				view.setIsothermOn(src.isSelected());
				view.repaint();
			}
		});
		p.add(checkBox);

		checkBox = new JCheckBox("Velocity");
		checkBox.setSelected(view.isVelocityOn());
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox src = (JCheckBox) e.getSource();
				view.setVelocityOn(src.isSelected());
				view.repaint();
			}
		});
		p.add(checkBox);

		checkBox = new JCheckBox("Streamline");
		checkBox.setSelected(view.isStreamlineOn());
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox src = (JCheckBox) e.getSource();
				view.setStreamlineOn(src.isSelected());
				view.repaint();
			}
		});
		p.add(checkBox);

		checkBox = new JCheckBox("Heat Flux Lines");
		checkBox.setSelected(view.isHeatFluxLinesOn());
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox src = (JCheckBox) e.getSource();
				view.setHeatFluxLinesOn(src.isSelected());
				view.repaint();
			}
		});
		p.add(checkBox);
		count++;

		checkBox = new JCheckBox("Heat Flux Arrows");
		checkBox.setSelected(view.isHeatFluxArrowsOn());
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox src = (JCheckBox) e.getSource();
				view.setHeatFluxArrowsOn(src.isSelected());
				view.repaint();
			}
		});
		p.add(checkBox);

		checkBox = new JCheckBox("Ruler");
		checkBox.setSelected(view.isRulerOn());
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox src = (JCheckBox) e.getSource();
				view.setRulerOn(src.isSelected());
				view.repaint();
			}
		});
		p.add(checkBox);

		checkBox = new JCheckBox("Graph");
		checkBox.setSelected(view.isGraphOn());
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox src = (JCheckBox) e.getSource();
				view.setGraphOn(src.isSelected());
				view.repaint();
				view.notifyGraphListeners(src.isSelected() ? GraphEvent.GRAPH_OPENED : GraphEvent.GRAPH_CLOSED);
			}
		});
		p.add(checkBox);

		checkBox = new JCheckBox("See-Through");
		checkBox.setSelected(view.getSeeThrough());
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox src = (JCheckBox) e.getSource();
				view.setSeeThrough(src.isSelected());
				view.repaint();
			}
		});
		p.add(checkBox);
		count++;

		checkBox = new JCheckBox("Smooth");
		checkBox.setSelected(view.isSmooth());
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox src = (JCheckBox) e.getSource();
				view.setSmooth(src.isSelected());
				view.repaint();
			}
		});
		p.add(checkBox);

		checkBox = new JCheckBox("Clock");
		checkBox.setSelected(view.isClockOn());
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox src = (JCheckBox) e.getSource();
				view.setClockOn(src.isSelected());
				view.repaint();
			}
		});
		p.add(checkBox);

		checkBox = new JCheckBox("Grid");
		checkBox.setSelected(view.isGridOn());
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox src = (JCheckBox) e.getSource();
				view.setGridOn(src.isSelected());
				view.repaint();
			}
		});
		p.add(checkBox);

		checkBox = new JCheckBox("Color Palette");
		checkBox.setSelected(view.isColorPaletteOn());
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox src = (JCheckBox) e.getSource();
				view.setColorPaletteOn(src.isSelected());
				view.repaint();
			}
		});
		p.add(checkBox);
		count++;

		checkBox = new JCheckBox("Brand");
		checkBox.setSelected(view.isFrankOn());
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox src = (JCheckBox) e.getSource();
				view.setFrankOn(src.isSelected());
				view.repaint();
			}
		});
		p.add(checkBox);

		checkBox = new JCheckBox("Control Panel");
		checkBox.setSelected(view.isControlPanelVisible());
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox src = (JCheckBox) e.getSource();
				view.setControlPanelVisible(src.isSelected());
				view.repaint();
			}
		});
		p.add(checkBox);

		p.add(new JPanel());
		p.add(new JPanel());
		count++;

		MiscUtil.makeCompactGrid(p, count, 4, 5, 5, 10, 2);

		p2 = new JPanel(new BorderLayout());
		p = new JPanel(new SpringLayout());
		p2.add(p, BorderLayout.NORTH);
		tab.add(p2, "Visualization");
		count = 0;

		p.add(new JLabel("Coloring property"));

		JComboBox<String> comboBox = new JComboBox<String>();
		comboBox.addItem("None");
		comboBox.addItem("Temperature");
		comboBox.addItem("Thermal energy");
		comboBox.setSelectedIndex(view.getHeatMapType() - View2D.HEATMAP_NONE);
		comboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				int i = ((JComboBox) e.getSource()).getSelectedIndex();
				nameLabel1.setEnabled(i > 0);
				nameLabel2.setEnabled(i > 0);
				unitLabel1.setEnabled(i > 0);
				unitLabel2.setEnabled(i > 0);
				switch (i) {
				case 0:
					lowerTempField.setEnabled(false);
					upperTempField.setEnabled(false);
					nameLabel1.setText(null);
					nameLabel2.setText(null);
					unitLabel1.setText(null);
					unitLabel2.setText(null);
					break;
				case 1:
					lowerTempField.setEnabled(true);
					upperTempField.setEnabled(true);
					nameLabel1.setText("Lowest temperature");
					nameLabel2.setText("Highest temperature");
					unitLabel1.setText("\u00B0C");
					unitLabel2.setText("\u00B0C");
					break;
				case 2:
					lowerTempField.setEnabled(true);
					upperTempField.setEnabled(true);
					nameLabel1.setText("Lowest energy");
					nameLabel2.setText("Highest energy");
					unitLabel1.setText("J");
					unitLabel2.setText("J");
					break;
				}
				view.setHeatMapType((byte) (i - View2D.HEATMAP_NONE));
				view.repaint();
			}
		});
		p.add(comboBox);

		p.add(new JPanel());
		count++;

		p.add(new JLabel("Color palette"));

		comboBox = new JComboBox<String>();
		comboBox.addItem("Rainbow");
		comboBox.addItem("Iron");
		comboBox.addItem("Gray");
		comboBox.setSelectedIndex(view.getColorPaletteType() - View2D.RAINBOW);
		comboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				int i = ((JComboBox) e.getSource()).getSelectedIndex();
				view.setColorPaletteType((byte) (i - View2D.RAINBOW));
				view.repaint();
			}
		});
		p.add(comboBox);

		p.add(new JPanel());
		count++;

		nameLabel1 = new JLabel("Lowest temperature");
		p.add(nameLabel1);

		lowerTempField = new JTextField(view.getMinimumTemperature() + "", 8);
		lowerTempField.addActionListener(okListener);
		p.add(lowerTempField);
		unitLabel1 = new JLabel("\u00B0C");
		p.add(unitLabel1);
		count++;

		nameLabel2 = new JLabel("Highest temperature");
		p.add(nameLabel2);

		upperTempField = new JTextField(view.getMaximumTemperature() + "", 8);
		upperTempField.addActionListener(okListener);
		p.add(upperTempField);
		unitLabel2 = new JLabel("\u00B0C");
		p.add(unitLabel2);
		count++;

		p.add(new JLabel("Mouse reading"));

		comboBox = new JComboBox<String>();
		comboBox.addItem("Default");
		comboBox.addItem("Temperature");
		comboBox.addItem("Thermal energy");
		comboBox.addItem("Velocity");
		comboBox.addItem("Heat flux");
		comboBox.addItem("Coordinates");
		comboBox.setSelectedIndex(view.getMouseReadType());
		comboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JComboBox src = (JComboBox) e.getSource();
				int i = src.getSelectedIndex();
				view.setMouseReadType((byte) i);
				view.repaint();
				view.notifyManipulationListeners(null, ManipulationEvent.MOUSE_READ_CHANGED);
			}
		});
		p.add(comboBox);

		p.add(new JPanel());
		count++;

		MiscUtil.makeCompactGrid(p, count, 3, 5, 5, 10, 2);

		pack();
		setLocationRelativeTo(view);

	}

	private float parse(String s) {
		float x = Float.NaN;
		try {
			x = Float.parseFloat(s);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(owner, "Cannot parse " + e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
		return x;
	}

}
