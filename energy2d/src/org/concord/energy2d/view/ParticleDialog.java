package org.concord.energy2d.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.model.Particle;
import org.concord.energy2d.util.BackgroundComboBox;
import org.concord.energy2d.util.ColorFill;
import org.concord.energy2d.util.ColorMenu;

/**
 * @author Charles Xie
 * 
 */
class ParticleDialog extends JDialog {

	private Window owner;
	private JColorChooser colorChooser;
	private BackgroundComboBox bgComboBox;
	private JTextField rxField, ryField, vxField, vyField;
	private JTextField massField, radiusField, temperatureField;
	private JComboBox<Boolean> draggableComboBox, movableComboBox;
	private ActionListener okListener;

	ParticleDialog(final View2D view, final Particle particle, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "Particle (#" + view.model.getParticles().indexOf(particle) + ") Properties", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		owner = getOwner();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				view.notifyManipulationListeners(particle, ManipulationEvent.PROPERTY_CHANGE);
				view.repaint();
				dispose();
			}
		});

		okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				float mass = parse(massField.getText());
				if (Float.isNaN(mass))
					return;
				float radius = parse(radiusField.getText());
				if (Float.isNaN(radius))
					return;
				float rx = parse(rxField.getText());
				if (Float.isNaN(rx))
					return;
				float ry = parse(ryField.getText());
				if (Float.isNaN(ry))
					return;
				float vx = parse(vxField.getText());
				if (Float.isNaN(vx))
					return;
				float vy = parse(vyField.getText());
				if (Float.isNaN(vy))
					return;
				float temperature = parse(temperatureField.getText());
				particle.setMass(mass);
				particle.setRadius(radius);
				particle.setTemperature(temperature);
				particle.setRx(rx);
				particle.setRy(ry);
				particle.setVx(vx);
				particle.setVy(vy);
				particle.setDraggable(draggableComboBox.getSelectedItem() == Boolean.TRUE);
				particle.setMovable(movableComboBox.getSelectedItem() == Boolean.TRUE);
				view.notifyManipulationListeners(particle, ManipulationEvent.PROPERTY_CHANGE);
				view.setSelectedManipulable(view.getSelectedManipulable());
				view.repaint();
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

		JPanel p = new JPanel(new GridLayout(10, 2, 5, 5));
		box.add(p);

		p.add(new JLabel("Mass (kg):"));
		massField = new JTextField(particle.getMass() + "", 10);
		massField.addActionListener(okListener);
		p.add(massField);

		p.add(new JLabel("Radius (m):"));
		radiusField = new JTextField(particle.getRadius() + "", 10);
		radiusField.addActionListener(okListener);
		p.add(radiusField);

		p.add(new JLabel("<html>Temperature (&deg;C):</html>"));
		temperatureField = new JTextField(particle.getTemperature() + "", 10);
		temperatureField.addActionListener(okListener);
		p.add(temperatureField);

		p.add(new JLabel("Rx (m):"));
		rxField = new JTextField(particle.getRx() + "", 10);
		rxField.addActionListener(okListener);
		p.add(rxField);

		p.add(new JLabel("Ry (m):"));
		ryField = new JTextField(particle.getRy() + "", 10);
		ryField.addActionListener(okListener);
		p.add(ryField);

		p.add(new JLabel("Vx (m/s):"));
		vxField = new JTextField(particle.getVx() + "", 10);
		vxField.addActionListener(okListener);
		p.add(vxField);

		p.add(new JLabel("Vy (m/s):"));
		vyField = new JTextField(particle.getVy() + "", 10);
		vyField.addActionListener(okListener);
		p.add(vyField);

		p.add(new JLabel("Color:"));
		colorChooser = new JColorChooser();
		colorChooser.setColor(particle.getColor());
		bgComboBox = new BackgroundComboBox(this, colorChooser, null);
		bgComboBox.setToolTipText("Particle color");
		bgComboBox.setFillPattern(new ColorFill(particle.getColor()));
		bgComboBox.getColorMenu().setColorArrayAction(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				Color c = bgComboBox.getColorMenu().getColor();
				particle.setColor(c);
				view.repaint();
				bgComboBox.getColorMenu().firePropertyChange(ColorMenu.FILLING, null, new ColorFill(c));
			}
		});
		bgComboBox.getColorMenu().setMoreColorAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color c = bgComboBox.getColorMenu().getColorChooser().getColor();
				particle.setColor(c);
				view.repaint();
				bgComboBox.getColorMenu().firePropertyChange(ColorMenu.FILLING, null, new ColorFill(c));
			}
		});
		bgComboBox.getColorMenu().addHexColorListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color c = bgComboBox.getColorMenu().getHexInputColor(particle.getColor());
				if (c == null)
					return;
				particle.setColor(c);
				view.repaint();
				bgComboBox.getColorMenu().firePropertyChange(ColorMenu.FILLING, null, new ColorFill(c));
			}
		});
		p.add(bgComboBox);

		p.add(new JLabel("Movable:"));
		movableComboBox = new JComboBox<Boolean>();
		movableComboBox.addItem(Boolean.TRUE);
		movableComboBox.addItem(Boolean.FALSE);
		movableComboBox.setSelectedIndex(particle.isMovable() ? 0 : 1);
		p.add(movableComboBox);

		p.add(new JLabel("Draggable by User:"));
		draggableComboBox = new JComboBox<Boolean>();
		draggableComboBox.addItem(Boolean.TRUE);
		draggableComboBox.addItem(Boolean.FALSE);
		draggableComboBox.setSelectedIndex(particle.isDraggable() ? 0 : 1);
		p.add(draggableComboBox);

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
