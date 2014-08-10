package org.concord.energy2d.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
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

	private JTextField xField;
	private JTextField yField;
	private JTextField wField;
	private JTextField hField;
	private JTextField uidField;
	private JTextField labelField;
	private JTextField velocityField;
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
				float x = parse(xField.getText());
				if (Float.isNaN(x))
					return;
				float y = parse(yField.getText());
				if (Float.isNaN(y))
					return;
				float w = parse(wField.getText());
				if (Float.isNaN(w))
					return;
				float h = parse(hField.getText());
				if (Float.isNaN(h))
					return;
				float speed = parse(velocityField.getText());
				if (Float.isNaN(speed))
					return;
				float angle = 0;
				Shape s = fan.getShape();
				if (s instanceof Rectangle2D.Float) {
					Rectangle2D.Float r = (Rectangle2D.Float) s;
					r.setRect(x, y, w, h);
					if (w > h)
						angle = 90;
				}
				String uid = uidField.getText();
				if (uid != null) {
					uid = uid.trim();
					if (!uid.equals("") && !uid.equals(fan.getUid())) {
						if (view.isUidUsed(uid)) {
							JOptionPane.showMessageDialog(owner, "UID: " + uid + " has been taken.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
				}
				fan.setUid(uid);
				fan.setLabel(labelField.getText());
				fan.setSpeed(speed);
				fan.setAngle((float) Math.toRadians(angle));
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

		JPanel p = new JPanel(new GridLayout(7, 2, 8, 8));
		p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(p, BorderLayout.CENTER);

		p.add(new JLabel("Unique ID:"));
		uidField = new JTextField(fan.getUid(), 10);
		uidField.addActionListener(okListener);
		p.add(uidField);

		p.add(new JLabel("Label:"));
		labelField = new JTextField(fan.getLabel(), 10);
		labelField.addActionListener(okListener);
		p.add(labelField);

		p.add(new JLabel("X (m):"));
		xField = new JTextField(FORMAT.format(fan.getShape().getBounds2D().getX()), 10);
		xField.addActionListener(okListener);
		p.add(xField);

		p.add(new JLabel("Y (m):"));
		yField = new JTextField(FORMAT.format(fan.getShape().getBounds2D().getY()), 10);
		yField.addActionListener(okListener);
		p.add(yField);

		p.add(new JLabel("Width (m):"));
		wField = new JTextField(FORMAT.format(fan.getShape().getBounds2D().getWidth()), 10);
		wField.addActionListener(okListener);
		p.add(wField);

		p.add(new JLabel("Height (m):"));
		hField = new JTextField(FORMAT.format(fan.getShape().getBounds2D().getHeight()), 10);
		hField.addActionListener(okListener);
		p.add(hField);

		p.add(new JLabel("Velocity (m/s):"));
		velocityField = new JTextField(FORMAT.format(fan.getSpeed()));
		velocityField.addActionListener(okListener);
		p.add(velocityField);

		pack();
		setLocationRelativeTo(view);

	}

	private float parse(String s) {
		return MiscUtil.parse(owner, s);
	}

}