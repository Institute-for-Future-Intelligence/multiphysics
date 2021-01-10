package org.energy2d.view;

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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.energy2d.event.ManipulationEvent;
import org.energy2d.model.Anemometer;
import org.energy2d.model.Particle;
import org.energy2d.undo.UndoTranslateManipulable;
import org.energy2d.util.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class AnemometerDialog extends JDialog {

	private Window owner;
	private ActionListener okListener;
	private JTextField xField;
	private JTextField yField;
	private JTextField labelField;
	private JTextField uidField;
	private JTextField attachField;

	AnemometerDialog(final View2D view, final Anemometer anemometer, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "Anemometer (#" + view.model.getAnemometers().indexOf(anemometer) + ") Options", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		owner = getOwner();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				view.notifyManipulationListeners(anemometer, ManipulationEvent.PROPERTY_CHANGE);
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

				float x = MiscUtil.parse(owner, xField.getText());
				if (Float.isNaN(x))
					return;
				float y = MiscUtil.parse(owner, yField.getText());
				if (Float.isNaN(y))
					return;
				y = view.model.getLy() - y;
				boolean moved = Math.abs(x - anemometer.getX()) > 0.000001 * view.model.getLx() || Math.abs(y - anemometer.getY()) > 0.000001 * view.model.getLy();
				if (moved)
					view.getUndoManager().addEdit(new UndoTranslateManipulable(view));
				anemometer.setX(x);
				anemometer.setY(y);

				anemometer.setLabel(labelField.getText());
				String uid = uidField.getText();
				if (uid != null) {
					uid = uid.trim();
					if (!uid.equals("") && !uid.equals(anemometer.getUid())) {
						if (view.isUidUsed(uid)) {
							JOptionPane.showMessageDialog(owner, "UID: " + uid + " has been taken.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						anemometer.setUid(uid);
					}
				}

				String attachID = attachField.getText();
				if (attachID != null) {
					attachID = attachID.trim();
					if (!attachID.equals("")) {
						if (attachID.equals(anemometer.getUid())) {
							JOptionPane.showMessageDialog(owner, "Anemometer " + attachID + " cannot be attached to itself.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						if (!view.isUidUsed(attachID)) {
							JOptionPane.showMessageDialog(owner, "Object " + attachID + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						anemometer.setAttachID(attachID);
						Particle particle = view.model.getParticle(attachID);
						if (particle != null) {
							anemometer.setX(particle.getRx());
							anemometer.setY(particle.getRy());
						}
					}
				}

				view.notifyManipulationListeners(anemometer, ManipulationEvent.PROPERTY_CHANGE);
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
		xField = new JTextField(View2D.COORDINATES_FORMAT.format(anemometer.getX()), 10);
		xField.addActionListener(okListener);
		p.add(xField);

		p.add(new JLabel("Y:"));
		yField = new JTextField(View2D.COORDINATES_FORMAT.format((view.model.getLy() - anemometer.getY())), 10);
		yField.addActionListener(okListener);
		p.add(yField);

		p.add(new JLabel("Unique ID:"));
		uidField = new JTextField(anemometer.getUid(), 10);
		uidField.addActionListener(okListener);
		p.add(uidField);

		p.add(new JLabel("Attached to:"));
		attachField = new JTextField(anemometer.getAttachID(), 10);
		attachField.addActionListener(okListener);
		p.add(attachField);

		p.add(new JLabel("Label:"));
		labelField = new JTextField(anemometer.getLabel(), 10);
		labelField.addActionListener(okListener);
		p.add(labelField);

		pack();
		setLocationRelativeTo(view);

	}

}
