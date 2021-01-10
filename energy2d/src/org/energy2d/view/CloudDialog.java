package org.energy2d.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.energy2d.event.ManipulationEvent;
import org.energy2d.model.Cloud;
import org.energy2d.util.BackgroundComboBox;
import org.energy2d.util.ColorFill;
import org.energy2d.util.ColorMenu;
import org.energy2d.util.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class CloudDialog extends JDialog {

	private Window owner;
	private JColorChooser colorChooser;
	private JCheckBox draggableCheckBox;
	private BackgroundComboBox bgComboBox;
	private JTextField speedField;
	private ActionListener okListener;

	CloudDialog(final View2D view, final Cloud cloud, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "Cloud (#" + view.model.getClouds().indexOf(cloud) + ") Properties", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		owner = getOwner();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				view.notifyManipulationListeners(cloud, ManipulationEvent.PROPERTY_CHANGE);
				view.repaint();
				dispose();
			}
		});

		okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				float x = MiscUtil.parse(owner, speedField.getText());
				if (Float.isNaN(x))
					return;
				cloud.setSpeed(x);
				cloud.setDraggable(draggableCheckBox.isSelected());
				view.notifyManipulationListeners(cloud, ManipulationEvent.PROPERTY_CHANGE);
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

		Box miscBox = Box.createVerticalBox();
		miscBox.setBorder(BorderFactory.createTitledBorder("General"));
		box.add(miscBox);

		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		miscBox.add(p);

		p.add(new JLabel("Speed:"));
		speedField = new JTextField(cloud.getSpeed() + "", 10);
		speedField.addActionListener(okListener);
		p.add(speedField);

		p.add(new JLabel("Color:"));
		colorChooser = new JColorChooser();
		colorChooser.setColor(cloud.getColor());
		bgComboBox = new BackgroundComboBox(this, colorChooser, null);
		bgComboBox.setToolTipText("Background color");
		bgComboBox.setFillPattern(new ColorFill(cloud.getColor()));
		bgComboBox.getColorMenu().setColorArrayAction(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				Color c = bgComboBox.getColorMenu().getColor();
				cloud.setColor(c);
				view.repaint();
				bgComboBox.getColorMenu().firePropertyChange(ColorMenu.FILLING, null, new ColorFill(c));
			}
		});
		bgComboBox.getColorMenu().setMoreColorAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color c = bgComboBox.getColorMenu().getColorChooser().getColor();
				cloud.setColor(c);
				view.repaint();
				bgComboBox.getColorMenu().firePropertyChange(ColorMenu.FILLING, null, new ColorFill(c));
			}
		});
		bgComboBox.getColorMenu().addHexColorListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color c = bgComboBox.getColorMenu().getHexInputColor(cloud.getColor());
				if (c == null)
					return;
				cloud.setColor(c);
				view.repaint();
				bgComboBox.getColorMenu().firePropertyChange(ColorMenu.FILLING, null, new ColorFill(c));
			}
		});
		p.add(bgComboBox);

		draggableCheckBox = new JCheckBox("Draggable by user", cloud.isDraggable());
		p.add(draggableCheckBox);

		pack();
		setLocationRelativeTo(view);

	}

}
