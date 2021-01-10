package org.energy2d.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.energy2d.event.ManipulationEvent;
import org.energy2d.model.ParticleFeeder;
import org.energy2d.undo.UndoTranslateManipulable;
import org.energy2d.util.BackgroundComboBox;
import org.energy2d.util.ColorFill;
import org.energy2d.util.ColorMenu;
import org.energy2d.util.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class ParticleFeederDialog extends JDialog {

	private final static DecimalFormat FORMAT = new DecimalFormat("####.######");

	private JColorChooser colorChooser;
	private BackgroundComboBox particleColorComboBox, velocityColorComboBox;
	private JTextField xField;
	private JTextField yField;
	private JTextField uidField;
	private JTextField labelField;
	private JTextField periodField;
	private JTextField maximumField;
	private JTextField massField;
	private JTextField radiusField;
	private JCheckBox draggableCheckBox;
	private Window owner;
	private ActionListener okListener;

	ParticleFeederDialog(final View2D view, final ParticleFeeder particleFeeder, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "Particle Feeder (#" + view.model.getParticleFeeders().indexOf(particleFeeder) + ") Properties", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		owner = getOwner();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				view.notifyManipulationListeners(particleFeeder, ManipulationEvent.PROPERTY_CHANGE);
				view.repaint();
				dispose();
			}
		});

		okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				float x = parse(xField.getText());
				if (Float.isNaN(x))
					return;
				float y = parse(yField.getText());
				if (Float.isNaN(y))
					return;
				float mass = parse(massField.getText());
				if (Float.isNaN(mass))
					return;
				float radius = parse(radiusField.getText());
				if (Float.isNaN(radius))
					return;
				float period = parse(periodField.getText());
				if (Float.isNaN(period))
					return;
				float maximum = parse(maximumField.getText());
				if (Float.isNaN(maximum))
					return;
				String uid = uidField.getText();
				if (uid != null) {
					uid = uid.trim();
					if (!uid.equals("") && !uid.equals(particleFeeder.getUid())) {
						if (view.isUidUsed(uid)) {
							JOptionPane.showMessageDialog(owner, "UID: " + uid + " has been taken.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
				}

				// undo
				float dx = 0.000001f * view.model.getLx();
				float dy = 0.000001f * view.model.getLy();
				boolean moved = Math.abs(x - particleFeeder.getX()) > dx || Math.abs(view.model.getLy() - y - particleFeeder.getY()) > dy;
				if (moved)
					view.getUndoManager().addEdit(new UndoTranslateManipulable(view));

				particleFeeder.setUid(uid);
				particleFeeder.setLabel(labelField.getText());
				particleFeeder.setMass(mass);
				particleFeeder.setRadius(radius);
				particleFeeder.setCenter(x, view.model.getLy() - y);
				particleFeeder.setPeriod(period);
				particleFeeder.setMaximum(Math.round(maximum));
				particleFeeder.setDraggable(draggableCheckBox.isSelected());
				view.notifyManipulationListeners(particleFeeder, ManipulationEvent.PROPERTY_CHANGE);
				view.setSelectedManipulable(view.getSelectedManipulable());
				view.repaint();
				dispose();
			}
		};

		JPanel panel = new JPanel(new BorderLayout());
		setContentPane(panel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(buttonPanel, BorderLayout.SOUTH);

		draggableCheckBox = new JCheckBox("Draggable by user", particleFeeder.isDraggable());
		buttonPanel.add(draggableCheckBox);

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

		JPanel p = new JPanel(new GridLayout(10, 2, 8, 8));
		p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(p, BorderLayout.CENTER);

		p.add(new JLabel("Unique ID:"));
		uidField = new JTextField(particleFeeder.getUid(), 10);
		uidField.addActionListener(okListener);
		p.add(uidField);

		p.add(new JLabel("Label:"));
		labelField = new JTextField(particleFeeder.getLabel(), 10);
		labelField.addActionListener(okListener);
		p.add(labelField);

		p.add(new JLabel("X (m):"));
		xField = new JTextField(FORMAT.format(particleFeeder.getX()), 10);
		xField.addActionListener(okListener);
		p.add(xField);

		p.add(new JLabel("Y (m):"));
		yField = new JTextField(FORMAT.format(view.model.getLy() - particleFeeder.getY()), 10);
		yField.addActionListener(okListener);
		p.add(yField);

		p.add(new JLabel("Mass (kg):"));
		massField = new JTextField(particleFeeder.getMass() + "", 10);
		massField.addActionListener(okListener);
		p.add(massField);

		p.add(new JLabel("Radius (m):"));
		radiusField = new JTextField(particleFeeder.getRadius() + "", 10);
		radiusField.addActionListener(okListener);
		p.add(radiusField);

		p.add(new JLabel("Feeding period (s):"));
		periodField = new JTextField(FORMAT.format(particleFeeder.getPeriod()), 10);
		periodField.addActionListener(okListener);
		p.add(periodField);

		p.add(new JLabel("Maximum particles:"));
		maximumField = new JTextField(particleFeeder.getMaximum() + "", 10);
		maximumField.addActionListener(okListener);
		p.add(maximumField);

		p.add(new JLabel("Color:"));
		colorChooser = new JColorChooser();
		colorChooser.setColor(particleFeeder.getColor());
		particleColorComboBox = new BackgroundComboBox(this, colorChooser, null);
		particleColorComboBox.setToolTipText("Particle color");
		particleColorComboBox.setFillPattern(new ColorFill(particleFeeder.getColor()));
		particleColorComboBox.getColorMenu().setColorArrayAction(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				Color c = particleColorComboBox.getColorMenu().getColor();
				particleFeeder.setColor(c);
				view.repaint();
				particleColorComboBox.getColorMenu().firePropertyChange(ColorMenu.FILLING, null, new ColorFill(c));
			}
		});
		particleColorComboBox.getColorMenu().setMoreColorAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color c = particleColorComboBox.getColorMenu().getColorChooser().getColor();
				particleFeeder.setColor(c);
				view.repaint();
				particleColorComboBox.getColorMenu().firePropertyChange(ColorMenu.FILLING, null, new ColorFill(c));
			}
		});
		particleColorComboBox.getColorMenu().addHexColorListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color c = particleColorComboBox.getColorMenu().getHexInputColor(particleFeeder.getColor());
				if (c == null)
					return;
				particleFeeder.setColor(c);
				view.repaint();
				particleColorComboBox.getColorMenu().firePropertyChange(ColorMenu.FILLING, null, new ColorFill(c));
			}
		});
		p.add(particleColorComboBox);

		p.add(new JLabel("Velocity Color:"));
		velocityColorComboBox = new BackgroundComboBox(this, colorChooser, null);
		velocityColorComboBox.setToolTipText("Velocity color");
		velocityColorComboBox.setFillPattern(new ColorFill(particleFeeder.getVelocityColor()));
		velocityColorComboBox.getColorMenu().setColorArrayAction(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				Color c = velocityColorComboBox.getColorMenu().getColor();
				particleFeeder.setVelocityColor(c);
				view.repaint();
				velocityColorComboBox.getColorMenu().firePropertyChange(ColorMenu.FILLING, null, new ColorFill(c));
			}
		});
		velocityColorComboBox.getColorMenu().setMoreColorAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color c = velocityColorComboBox.getColorMenu().getColorChooser().getColor();
				particleFeeder.setVelocityColor(c);
				view.repaint();
				velocityColorComboBox.getColorMenu().firePropertyChange(ColorMenu.FILLING, null, new ColorFill(c));
			}
		});
		velocityColorComboBox.getColorMenu().addHexColorListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color c = velocityColorComboBox.getColorMenu().getHexInputColor(particleFeeder.getColor());
				if (c == null)
					return;
				particleFeeder.setVelocityColor(c);
				view.repaint();
				velocityColorComboBox.getColorMenu().firePropertyChange(ColorMenu.FILLING, null, new ColorFill(c));
			}
		});
		p.add(velocityColorComboBox);

		pack();
		setLocationRelativeTo(view);

	}

	private float parse(String s) {
		return MiscUtil.parse(owner, s);
	}

}