package org.energy2d.view;

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

import org.energy2d.event.GraphEvent;
import org.energy2d.event.ManipulationEvent;
import org.energy2d.undo.UndoClock;
import org.energy2d.undo.UndoColoringProperty;
import org.energy2d.undo.UndoColoringStyle;
import org.energy2d.undo.UndoColorPalette;
import org.energy2d.undo.UndoControlPanel;
import org.energy2d.undo.UndoGridLines;
import org.energy2d.undo.UndoHeatFluxArrows;
import org.energy2d.undo.UndoHeatFluxLines;
import org.energy2d.undo.UndoIsotherm;
import org.energy2d.undo.UndoMaximumTemperature;
import org.energy2d.undo.UndoMinimumTemperature;
import org.energy2d.undo.UndoMouseReadType;
import org.energy2d.undo.UndoSeeThrough;
import org.energy2d.undo.UndoSmooth;
import org.energy2d.undo.UndoStreamlines;
import org.energy2d.undo.UndoTickmarks;
import org.energy2d.undo.UndoVelocity;
import org.energy2d.undo.UndoViewFactorLines;
import org.energy2d.util.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class ViewDialog extends JDialog {

	private Window owner;
	private JTextField lowerTempField, upperTempField, fanRotationSpeedScaleField;
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
				if (Math.abs(x - view.getMinimumTemperature()) > 0.001) {
					view.getUndoManager().addEdit(new UndoMinimumTemperature(view));
					view.setMinimumTemperature(x);
				}
				x = parse(upperTempField.getText());
				if (Float.isNaN(x))
					return;
				if (Math.abs(x - view.getMaximumTemperature()) > 0.001) {
					view.getUndoManager().addEdit(new UndoMaximumTemperature(view));
					view.setMaximumTemperature(x);
				}
				x = parse(fanRotationSpeedScaleField.getText());
				if (Float.isNaN(x))
					return;
				if (x <= 0) {
					JOptionPane.showMessageDialog(owner, "Fan rotation speed scale must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (Math.abs(x - view.getFanRotationSpeedScaleFactor()) > 0.001) {
					view.setFanRotationSpeedScaleFactor(x);
				}
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
				view.getUndoManager().addEdit(new UndoIsotherm(view));
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
				view.getUndoManager().addEdit(new UndoVelocity(view));
				JCheckBox src = (JCheckBox) e.getSource();
				view.setVelocityOn(src.isSelected());
				view.repaint();
			}
		});
		p.add(checkBox);

		checkBox = new JCheckBox("Streamlines");
		checkBox.setSelected(view.isStreamlineOn());
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				view.getUndoManager().addEdit(new UndoStreamlines(view));
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
				view.getUndoManager().addEdit(new UndoHeatFluxLines(view));
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
				view.getUndoManager().addEdit(new UndoHeatFluxArrows(view));
				JCheckBox src = (JCheckBox) e.getSource();
				view.setHeatFluxArrowsOn(src.isSelected());
				view.repaint();
			}
		});
		p.add(checkBox);

		checkBox = new JCheckBox("Tickmarks");
		checkBox.setSelected(view.isBorderTickmarksOn());
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				view.getUndoManager().addEdit(new UndoTickmarks(view));
				JCheckBox src = (JCheckBox) e.getSource();
				view.setBorderTickmarksOn(src.isSelected());
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
				view.getUndoManager().addEdit(new UndoSeeThrough(view));
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
				view.getUndoManager().addEdit(new UndoSmooth(view));
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
				view.getUndoManager().addEdit(new UndoClock(view));
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
				view.getUndoManager().addEdit(new UndoGridLines(view));
				JCheckBox src = (JCheckBox) e.getSource();
				view.setGridOn(src.isSelected());
				view.repaint();
			}
		});
		p.add(checkBox);

		checkBox = new JCheckBox("View Factor Lines");
		checkBox.setSelected(view.isViewFactorLinesOn());
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				view.getUndoManager().addEdit(new UndoViewFactorLines(view));
				JCheckBox src = (JCheckBox) e.getSource();
				view.setViewFactorLinesOn(src.isSelected());
				view.repaint();
			}
		});
		p.add(checkBox);

		checkBox = new JCheckBox("Color Palette");
		checkBox.setSelected(view.isColorPaletteOn());
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				view.getUndoManager().addEdit(new UndoColorPalette(view));
				JCheckBox src = (JCheckBox) e.getSource();
				view.setColorPaletteOn(src.isSelected());
				view.repaint();
			}
		});
		p.add(checkBox);
		count++;

		checkBox = new JCheckBox("Logo");
		checkBox.setSelected(view.getShowLogo());
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox src = (JCheckBox) e.getSource();
				view.setShowLogo(src.isSelected());
				view.repaint();
			}
		});
		p.add(checkBox);

		checkBox = new JCheckBox("Control Panel");
		checkBox.setSelected(view.isControlPanelVisible());
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				view.getUndoManager().addEdit(new UndoControlPanel(view));
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

		final JComboBox<String> coloringComboBox = new JComboBox<String>();
		coloringComboBox.addItem("None");
		coloringComboBox.addItem("Temperature");
		coloringComboBox.addItem("Thermal energy");
		coloringComboBox.setSelectedIndex(view.getHeatMapType() - View2D.HEATMAP_NONE);
		coloringComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					int i = coloringComboBox.getSelectedIndex();
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
					view.getUndoManager().addEdit(new UndoColoringProperty(view));
					view.setHeatMapType((byte) (i + View2D.HEATMAP_NONE));
					view.repaint();
				}
			}
		});
		p.add(coloringComboBox);

		p.add(new JPanel());
		count++;

		p.add(new JLabel("Coloring style"));

		final JComboBox<String> coloringStyleComboBox = new JComboBox<String>();
		coloringStyleComboBox.addItem("Rainbow");
		coloringStyleComboBox.addItem("Iron");
		coloringStyleComboBox.addItem("Gray");
		coloringStyleComboBox.setSelectedIndex(view.getColorPaletteType() - View2D.RAINBOW);
		coloringStyleComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					view.getUndoManager().addEdit(new UndoColoringStyle(view));
					int i = coloringStyleComboBox.getSelectedIndex();
					view.setColorPaletteType((byte) (i + View2D.RAINBOW));
					view.repaint();
				}
			}
		});
		p.add(coloringStyleComboBox);

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

		p.add(new JLabel("Fan rotation speed scale"));

		fanRotationSpeedScaleField = new JTextField(view.getFanRotationSpeedScaleFactor() + "", 8);
		fanRotationSpeedScaleField.addActionListener(okListener);
		p.add(fanRotationSpeedScaleField);
		p.add(new JLabel("Dimensionless"));
		count++;

		p.add(new JLabel("Mouse reading"));

		final JComboBox<String> mouseReadingComboBox = new JComboBox<String>();
		mouseReadingComboBox.addItem("Default");
		mouseReadingComboBox.addItem("Temperature");
		mouseReadingComboBox.addItem("Thermal energy");
		mouseReadingComboBox.addItem("Velocity");
		mouseReadingComboBox.addItem("Heat flux");
		mouseReadingComboBox.addItem("Coordinates");
		mouseReadingComboBox.setSelectedIndex(view.getMouseReadType());
		mouseReadingComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					view.getUndoManager().addEdit(new UndoMouseReadType(view));
					view.setMouseReadType((byte) mouseReadingComboBox.getSelectedIndex());
					view.repaint();
					view.notifyManipulationListeners(null, ManipulationEvent.MOUSE_READ_CHANGED);
				}
			}
		});
		p.add(mouseReadingComboBox);

		p.add(new JPanel());
		count++;

		MiscUtil.makeCompactGrid(p, count, 3, 5, 5, 10, 2);

		pack();
		setLocationRelativeTo(view);

	}

	private float parse(String s) {
		return MiscUtil.parse(owner, s);
	}

}
