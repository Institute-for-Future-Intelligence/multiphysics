package org.energy2d.system;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.energy2d.event.ManipulationEvent;
import org.energy2d.model.Sensor;
import org.energy2d.util.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class PreferencesDialog extends JDialog {

	private Window owner;
	private JCheckBox snapToGridCheckBox;
	private JTextField perimeterStepField;
	private JTextField maximumDataPointsField;
	private JComboBox<String> timeUnitComboBox;
	private JComboBox<String> controlPanelPositionComboBox;
	private ActionListener okListener;

	PreferencesDialog(final System2D s2d, boolean modal) {

		super(JOptionPane.getFrameForComponent(s2d.view), "Preferences", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		owner = getOwner();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				s2d.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
				s2d.view.repaint();
				dispose();
			}
		});

		okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				float x = parse(perimeterStepField.getText());
				if (Float.isNaN(x))
					return;
				if (x < 1 || x > 20) {
					JOptionPane.showMessageDialog(owner, "Perimeter step cannot be smaller than 1% or larger than 20%.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				s2d.model.setPerimeterStepSize(0.01f * x);
				if (s2d.view.isViewFactorLinesOn())
					s2d.model.generateViewFactorMesh();

				x = parse(maximumDataPointsField.getText());
				if (Float.isNaN(x))
					return;
				if (x < 100 || x > 100000) {
					JOptionPane.showMessageDialog(owner, "Maximum data points should be at least 100 and at most 100000.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				Sensor.setMaximumDataPoints((int) x);
				s2d.view.setGraphTimeUnit((byte) timeUnitComboBox.getSelectedIndex());
				s2d.view.setControlPanelPosition((byte) controlPanelPositionComboBox.getSelectedIndex());

				s2d.view.setSnapToGrid(snapToGridCheckBox.isSelected());
				if (s2d.snapToggleButton != null)
					s2d.snapToggleButton.setSelected(snapToGridCheckBox.isSelected());
				s2d.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
				s2d.view.repaint();
				dispose();

			}
		};

		JPanel panel = new JPanel(new BorderLayout());
		setContentPane(panel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(buttonPanel, BorderLayout.SOUTH);

		JButton button = new JButton("OK");
		button.addActionListener(okListener);
		buttonPanel.add(button);

		button = new JButton("Cancel");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttonPanel.add(button);

		Box box = Box.createVerticalBox();
		box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(box, BorderLayout.CENTER);

		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setBorder(BorderFactory.createTitledBorder("Edit Mode"));
		box.add(p);

		snapToGridCheckBox = new JCheckBox("Snap to computational grid (" + s2d.model.getNx() + " x " + s2d.model.getNy() + ")", s2d.view.isSnapToGrid());
		snapToGridCheckBox.setToolTipText("Should objects' shapes and coordinates be snapped to the computational grid?");
		p.add(snapToGridCheckBox);

		p = new JPanel(new GridLayout(2, 2, 10, 5));
		p.setBorder(BorderFactory.createTitledBorder("Graphs"));
		box.add(p);

		p.add(new JLabel(" Sensor Maximum Data Points:"));
		maximumDataPointsField = new JTextField("" + Sensor.getMaximumDataPoints(), 10);
		maximumDataPointsField.setToolTipText("Set the maximum number of data points sensors will collect");
		maximumDataPointsField.addActionListener(okListener);
		p.add(maximumDataPointsField);

		p.add(new JLabel(" Unit of Time Axis:"));
		timeUnitComboBox = new JComboBox<String>(new String[] { "Hour", "Minute", "Second" });
		timeUnitComboBox.setSelectedIndex(s2d.view.getGraphTimeUnit());
		timeUnitComboBox.setToolTipText("Select the unit for the time axis of the graph");
		timeUnitComboBox.addActionListener(okListener);
		p.add(timeUnitComboBox);

		p = new JPanel(new GridLayout(1, 2, 10, 5));
		p.setBorder(BorderFactory.createTitledBorder("Controls"));
		box.add(p);

		p.add(new JLabel(" Control Panel Position:"));
		controlPanelPositionComboBox = new JComboBox<String>(new String[] { "Bottom", "Top" });
		controlPanelPositionComboBox.setSelectedIndex(s2d.view.getControlPanelPosition());
		controlPanelPositionComboBox.setToolTipText("Set the position of the Control Panel");
		controlPanelPositionComboBox.addActionListener(okListener);
		p.add(controlPanelPositionComboBox);

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setBorder(BorderFactory.createTitledBorder("Radiation Perimeter Step"));
		box.add(p);

		p.add(new JLabel("Percentage of the box size:"));
		perimeterStepField = new JTextField("" + Math.round(100 * s2d.model.getPerimeterStepSize()), 10);
		perimeterStepField.setToolTipText("Set the step size for the perimeter of a radiation body");
		perimeterStepField.addActionListener(okListener);
		p.add(perimeterStepField);

		pack();
		setLocationRelativeTo(s2d.view);

	}

	private float parse(String s) {
		return MiscUtil.parse(owner, s);
	}

}
