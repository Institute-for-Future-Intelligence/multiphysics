package org.energy2d.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.energy2d.event.ManipulationEvent;
import org.energy2d.model.Part;
import org.energy2d.model.Particle;
import org.energy2d.model.Sensor;
import org.energy2d.model.Thermometer;
import org.energy2d.model.Thermostat;
import org.energy2d.undo.UndoTranslateManipulable;
import org.energy2d.util.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class ThermometerDialog extends JDialog {

	private Window owner;
	private ActionListener okListener;
	private JTextField xField;
	private JTextField yField;
	private JTextField labelField;
	private JTextField uidField;
	private JTextField attachField;
	private JTextField setpointField;
	private JTextField deadbandField;
	private JRadioButton onePointButton;
	private JRadioButton fivePointsButton;
	private JRadioButton ninePointsButton;
	private JCheckBox[] powerSourceCheckBoxes;
	private List<Part> powerSources;

	ThermometerDialog(final View2D view, final Thermometer thermometer, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "Thermometer (#" + view.model.getThermometers().indexOf(thermometer) + ") Options", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		owner = getOwner();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				view.notifyManipulationListeners(thermometer, ManipulationEvent.PROPERTY_CHANGE);
				view.repaint();
				dispose();
			}
		});

		JPanel panel = new JPanel(new BorderLayout());
		setContentPane(panel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(buttonPanel, BorderLayout.SOUTH);

		okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				boolean thermostatSet = false;
				for (int i = 0; i < powerSourceCheckBoxes.length; i++) {
					if (powerSourceCheckBoxes[i].isSelected()) {
						thermostatSet = true;
						break;
					}
				}
				if (thermostatSet) {
					if (checkUidField())
						return;
				}

				float x = parse(xField.getText());
				if (Float.isNaN(x))
					return;
				float y = parse(yField.getText());
				if (Float.isNaN(y))
					return;
				y = view.model.getLy() - y - thermometer.getSensingSpotY();
				boolean moved = Math.abs(x - thermometer.getX()) > 0.000001 * view.model.getLx() || Math.abs(y - thermometer.getY()) > 0.000001 * view.model.getLy();
				if (moved)
					view.getUndoManager().addEdit(new UndoTranslateManipulable(view));
				thermometer.setX(x);
				thermometer.setY(y);

				thermometer.setLabel(labelField.getText());
				String uid = uidField.getText();
				if (uid != null) {
					uid = uid.trim();
					if (!uid.equals("") && !uid.equals(thermometer.getUid())) {
						if (view.isUidUsed(uid)) {
							JOptionPane.showMessageDialog(owner, "UID: " + uid + " has been taken.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						thermometer.setUid(uid);
					}
				}

				String attachID = attachField.getText();
				if (attachID != null) {
					attachID = attachID.trim();
					if (!attachID.equals("")) {
						if (attachID.equals(thermometer.getUid())) {
							JOptionPane.showMessageDialog(owner, "Thermometer " + attachID + " cannot be attached to itself.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						if (!view.isUidUsed(attachID)) {
							JOptionPane.showMessageDialog(owner, "Object " + attachID + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						thermometer.setAttachID(attachID);
						Particle particle = view.model.getParticle(attachID);
						if (particle != null) {
							thermometer.setX(particle.getRx());
							thermometer.setY(particle.getRy() - thermometer.getSensingSpotY());
						}
					}
				}

				if (onePointButton.isSelected())
					thermometer.setStencil(Sensor.ONE_POINT);
				else if (fivePointsButton.isSelected())
					thermometer.setStencil(Sensor.FIVE_POINT);
				else if (ninePointsButton.isSelected())
					thermometer.setStencil(Sensor.NINE_POINT);

				float setpoint = parse(setpointField.getText());
				if (Float.isNaN(setpoint))
					return;
				float deadband = parse(deadbandField.getText());
				if (Float.isNaN(deadband))
					return;
				for (int i = 0; i < powerSourceCheckBoxes.length; i++) {
					Part ps = powerSources.get(i);
					if (powerSourceCheckBoxes[i].isSelected()) {
						Thermostat t = view.model.addThermostat(thermometer, ps);
						t.setSetPoint(setpoint);
						t.setDeadband(deadband);
					} else {
						view.model.removeThermostat(thermometer, ps);
					}
				}

				view.notifyManipulationListeners(thermometer, ManipulationEvent.PROPERTY_CHANGE);
				view.repaint();
				dispose();

			}

		};

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

		// general properties

		JPanel p = new JPanel(new GridLayout(3, 2, 8, 8));
		p.setBorder(BorderFactory.createTitledBorder("General properties"));
		box.add(p);

		p.add(new JLabel("X:"));
		xField = new JTextField(View2D.COORDINATES_FORMAT.format(thermometer.getX()), 10);
		xField.addActionListener(okListener);
		p.add(xField);

		p.add(new JLabel("Y:"));
		yField = new JTextField(View2D.COORDINATES_FORMAT.format((view.model.getLy() - thermometer.getY() - thermometer.getSensingSpotY())), 10);
		yField.addActionListener(okListener);
		p.add(yField);

		p.add(new JLabel("Unique ID:"));
		uidField = new JTextField(thermometer.getUid(), 10);
		uidField.addActionListener(okListener);
		p.add(uidField);

		p.add(new JLabel("Attached to:"));
		attachField = new JTextField(thermometer.getAttachID(), 10);
		attachField.addActionListener(okListener);
		p.add(attachField);

		p.add(new JLabel("Label:"));
		labelField = new JTextField(thermometer.getLabel(), 10);
		labelField.addActionListener(okListener);
		p.add(labelField);

		// thermometer calibration: how small it can be

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setBorder(BorderFactory.createTitledBorder("Sampled area (stencil)"));
		box.add(p);

		ButtonGroup bg = new ButtonGroup();

		onePointButton = new JRadioButton("One point");
		p.add(onePointButton);
		bg.add(onePointButton);

		fivePointsButton = new JRadioButton("Five points");
		p.add(fivePointsButton);
		bg.add(fivePointsButton);

		ninePointsButton = new JRadioButton("Nine points");
		p.add(ninePointsButton);
		bg.add(ninePointsButton);

		switch (thermometer.getStencil()) {
		case 5:
			fivePointsButton.setSelected(true);
			break;
		case 9:
			ninePointsButton.setSelected(true);
			break;
		default:
			onePointButton.setSelected(true);
		}

		// thermostat properties: a thermometer can control multiple power sources, but a power source can only be controlled by a thermometer

		Thermostat thermostat = view.model.getThermostat(thermometer);

		List<Part> parts = view.model.getParts();
		powerSources = new ArrayList<Part>();
		for (Part x : parts) {
			if (x.getPower() != 0) {
				Thermostat ts = view.model.getThermostat(x);
				if (ts == null || ts.getThermometer() == thermometer)
					powerSources.add(x);
			}
		}
		powerSourceCheckBoxes = new JCheckBox[powerSources.size()];

		JPanel thermostatPanel = new JPanel(new BorderLayout());
		thermostatPanel.setBorder(BorderFactory.createTitledBorder("Thermostat connection"));
		box.add(thermostatPanel);

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		thermostatPanel.add(p, BorderLayout.NORTH);
		p.add(new JLabel("Set point: "));
		setpointField = new JTextField(thermostat == null ? "20" : thermostat.getSetPoint() + "", 10);
		setpointField.addActionListener(okListener);
		p.add(setpointField);
		p.add(new JLabel("\u2103"));

		p.add(new JLabel("      Deadband: "));
		deadbandField = new JTextField(thermostat == null ? "1" : thermostat.getDeadband() + "", 10);
		deadbandField.addActionListener(okListener);
		p.add(deadbandField);
		p.add(new JLabel("\u2103"));

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		thermostatPanel.add(p, BorderLayout.CENTER);
		p.add(new JLabel(powerSources.isEmpty() ? "No power source." : "Power sources:"));

		p = new JPanel(new GridLayout(1 + powerSources.size() / 5, 5));
		thermostatPanel.add(p, BorderLayout.SOUTH);
		for (int i = 0; i < powerSources.size(); i++) {
			Part ps = powerSources.get(i);
			String uid = ps.getUid();
			if (uid == null) { // auto-generate a UID
				uid = Long.toHexString(System.currentTimeMillis());
				ps.setUid(uid);
			}
			powerSourceCheckBoxes[i] = new JCheckBox(ps.getUid());
			if (view.model.isConnected(thermometer, ps))
				powerSourceCheckBoxes[i].setSelected(true);
			powerSourceCheckBoxes[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					checkUidField();
				}
			});
			p.add(powerSourceCheckBoxes[i]);
		}

		pack();
		setLocationRelativeTo(view);

	}

	private boolean checkUidField() {
		String s = uidField.getText();
		if (s == null || s.trim().length() == 0) {
			JOptionPane.showMessageDialog(owner, "Please provide a unique ID for this thermometer.", "Reminder", JOptionPane.WARNING_MESSAGE);
			uidField.requestFocusInWindow();
			return true;
		}
		return false;
	}

	private float parse(String s) {
		return MiscUtil.parse(owner, s);
	}

}
