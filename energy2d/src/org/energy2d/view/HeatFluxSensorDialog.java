package org.energy2d.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.energy2d.event.ManipulationEvent;
import org.energy2d.model.HeatFluxSensor;
import org.energy2d.model.Particle;
import org.energy2d.undo.UndoRotateManipulable;
import org.energy2d.undo.UndoTranslateManipulable;
import org.energy2d.util.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class HeatFluxSensorDialog extends JDialog {

	private final static DecimalFormat ANGLE_FORMAT = new DecimalFormat("###.#");

	private Window owner;
	private ActionListener okListener;
	private JTextField xField;
	private JTextField yField;
	private JTextField angleField;
	private JTextField labelField;
	private JTextField uidField;
	private JTextField attachField;

	HeatFluxSensorDialog(final View2D view, final HeatFluxSensor heatFluxSensor, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "Heat Flux Sensor (#" + view.model.getHeatFluxSensors().indexOf(heatFluxSensor) + ") Options", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		owner = getOwner();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				view.notifyManipulationListeners(heatFluxSensor, ManipulationEvent.PROPERTY_CHANGE);
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

				float x = parse(xField.getText());
				if (Float.isNaN(x))
					return;
				float y = parse(yField.getText());
				if (Float.isNaN(y))
					return;
				y = view.model.getLy() - y;
				boolean moved = Math.abs(x - heatFluxSensor.getX()) > 0.000001 * view.model.getLx() || Math.abs(y - heatFluxSensor.getY()) > 0.000001 * view.model.getLy();
				if (moved) {
					view.getUndoManager().addEdit(new UndoTranslateManipulable(view));
					heatFluxSensor.setX(x);
					heatFluxSensor.setY(y);
				}

				float angle = parse(angleField.getText());
				if (Float.isNaN(angle))
					return;
				angle = (float) Math.toRadians(angle);
				boolean rotated = Math.abs(angle - heatFluxSensor.getAngle()) > 0.001;
				if (rotated) {
					view.getUndoManager().addEdit(new UndoRotateManipulable(view, 0));
					heatFluxSensor.setAngle(angle);
				}

				heatFluxSensor.setLabel(labelField.getText());
				String uid = uidField.getText();
				if (uid != null) {
					uid = uid.trim();
					if (!uid.equals("") && !uid.equals(heatFluxSensor.getUid())) {
						if (view.isUidUsed(uid)) {
							JOptionPane.showMessageDialog(owner, "UID: " + uid + " has been taken.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						heatFluxSensor.setUid(uid);
					}
				}

				String attachID = attachField.getText();
				if (attachID != null) {
					attachID = attachID.trim();
					if (!attachID.equals("")) {
						if (attachID.equals(heatFluxSensor.getUid())) {
							JOptionPane.showMessageDialog(owner, "Heat flux sensor " + attachID + " cannot be attached to itself.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						if (!view.isUidUsed(attachID)) {
							JOptionPane.showMessageDialog(owner, "Object " + attachID + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						heatFluxSensor.setAttachID(attachID);
						Particle particle = view.model.getParticle(attachID);
						if (particle != null) {
							heatFluxSensor.setX(particle.getRx());
							heatFluxSensor.setY(particle.getRy());
							heatFluxSensor.setAngle(particle.getTheta());
						}
					}
				}

				view.notifyManipulationListeners(heatFluxSensor, ManipulationEvent.PROPERTY_CHANGE);
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
		xField = new JTextField(View2D.COORDINATES_FORMAT.format(heatFluxSensor.getX()), 10);
		xField.addActionListener(okListener);
		p.add(xField);

		p.add(new JLabel("Y:"));
		yField = new JTextField(View2D.COORDINATES_FORMAT.format((view.model.getLy() - heatFluxSensor.getY())), 10);
		yField.addActionListener(okListener);
		p.add(yField);

		p.add(new JLabel("<html>Angle: (&deg;)</html>"));
		angleField = new JTextField(ANGLE_FORMAT.format(Math.toDegrees(heatFluxSensor.getAngle())), 10);
		angleField.addActionListener(okListener);
		p.add(angleField);

		p.add(new JLabel("Unique ID:"));
		uidField = new JTextField(heatFluxSensor.getUid(), 10);
		uidField.addActionListener(okListener);
		p.add(uidField);

		p.add(new JLabel("Label:"));
		labelField = new JTextField(heatFluxSensor.getLabel(), 10);
		labelField.addActionListener(okListener);
		p.add(labelField);

		p.add(new JLabel("Attach to:"));
		attachField = new JTextField(heatFluxSensor.getAttachID(), 10);
		attachField.addActionListener(okListener);
		p.add(attachField);

		pack();
		setLocationRelativeTo(view);

	}

	private float parse(String s) {
		return MiscUtil.parse(owner, s);
	}

}
