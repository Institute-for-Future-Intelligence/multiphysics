package org.concord.energy2d.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.model.Fan;
import org.concord.energy2d.util.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class FanDialog extends JDialog {

	private final static DecimalFormat FORMAT = new DecimalFormat("####.######");

	private JTextField speedField;
	private JTextField angleField;
	private JCheckBox draggableCheckBox;
	private Window owner;
	private ActionListener okListener;

	FanDialog(final View2D view, final Fan fan, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "Fan (#" + view.model.getFans().indexOf(fan) + ") Properties", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		owner = getOwner();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				view.notifyManipulationListeners(fan, ManipulationEvent.PROPERTY_CHANGE);
				view.repaint();
				dispose();
			}
		});

		okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				float speed = parse(speedField.getText());
				if (Float.isNaN(speed))
					return;
				float angle = parse(angleField.getText());
				if (Float.isNaN(angle))
					return;
				fan.setSpeed(speed);
				fan.setAngle(angle);
				fan.setDraggable(draggableCheckBox.isSelected());
				view.notifyManipulationListeners(fan, ManipulationEvent.PROPERTY_CHANGE);
				view.setSelectedManipulable(view.getSelectedManipulable());
				view.repaint();
				dispose();
			}
		};

		JPanel panel = new JPanel(new BorderLayout());
		setContentPane(panel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(buttonPanel, BorderLayout.SOUTH);

		draggableCheckBox = new JCheckBox("Draggable by user", fan.isDraggable());
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

		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(p, BorderLayout.CENTER);

		p.add(new JLabel("Speed:"));
		speedField = new JTextField(FORMAT.format(fan.getSpeed()), 6);
		speedField.addActionListener(okListener);
		p.add(speedField);
		p.add(new JLabel("<html><i>m/s</i></html>"));

		p.add(new JLabel("Angle:"));
		angleField = new JTextField(FORMAT.format(Math.toDegrees(fan.getAngle())), 6);
		angleField.addActionListener(okListener);
		p.add(angleField);
		p.add(new JLabel("Degrees"));

		pack();
		setLocationRelativeTo(view);

	}

	private float parse(String s) {
		return MiscUtil.parse(owner, s);
	}
}