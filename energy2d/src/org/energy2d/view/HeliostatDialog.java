package org.energy2d.view;

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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.energy2d.event.ManipulationEvent;
import org.energy2d.model.Heliostat;
import org.energy2d.undo.UndoResizeManipulable;
import org.energy2d.undo.UndoTranslateManipulable;
import org.energy2d.util.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class HeliostatDialog extends JDialog {

	private final static DecimalFormat FORMAT = new DecimalFormat("####.######");

	private JTextField xField;
	private JTextField yField;
	private JTextField wField;
	private JTextField hField;
	private JTextField uidField;
	private JTextField labelField;
	private JTextField targetField;
	private JComboBox<String> typeComboBox;
	private JCheckBox draggableCheckBox;
	private Window owner;
	private ActionListener okListener;

	HeliostatDialog(final View2D view, final Heliostat heliostat, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "Heliostat (#" + view.model.getHeliostats().indexOf(heliostat) + ") Properties", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		owner = getOwner();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				view.notifyManipulationListeners(heliostat, ManipulationEvent.PROPERTY_CHANGE);
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
				Shape s = heliostat.getShape();
				if (s instanceof Rectangle2D.Float) {
					Rectangle2D.Float r = (Rectangle2D.Float) s;
					float dx = 0.000001f * view.model.getLx();
					float dy = 0.000001f * view.model.getLy();
					boolean moved = Math.abs(x - s.getBounds2D().getX()) > dx || Math.abs(y - s.getBounds2D().getY()) > dy;
					if (moved) {
						view.getUndoManager().addEdit(new UndoTranslateManipulable(view));
					}
					boolean resized = Math.abs(w - s.getBounds2D().getWidth()) > dx || Math.abs(h - s.getBounds2D().getHeight()) > dy;
					if (resized) {
						view.getUndoManager().addEdit(new UndoResizeManipulable(view));
					}
					r.setRect(x, view.model.getLy() - y, w, h);
				}
				String uid = uidField.getText();
				if (uid != null) {
					uid = uid.trim();
					if (!uid.equals("") && !uid.equals(heliostat.getUid())) {
						if (view.isUidUsed(uid)) {
							JOptionPane.showMessageDialog(owner, "UID: " + uid + " has been taken.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
				}
				String type = typeComboBox.getSelectedItem().toString();
				if ("Mirror".equalsIgnoreCase(type))
					heliostat.setType(Heliostat.MIRROR);
				else if ("Photovoltaic".equalsIgnoreCase(type))
					heliostat.setType(Heliostat.PHOTOVOLTAIC);
				String targetID = targetField.getText();
				if (targetID != null && !targetID.trim().equals("") && !view.isUidUsed(targetID)) {
					JOptionPane.showMessageDialog(owner, "Object " + targetID + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				heliostat.setTarget(view.model.getPart(targetID));
				heliostat.setUid(uid);
				heliostat.setLabel(labelField.getText());
				heliostat.setDraggable(draggableCheckBox.isSelected());
				heliostat.setAngle();
				view.notifyManipulationListeners(heliostat, ManipulationEvent.PROPERTY_CHANGE);
				view.setSelectedManipulable(view.getSelectedManipulable());
				view.repaint();
				dispose();
			}
		};

		JPanel panel = new JPanel(new BorderLayout());
		setContentPane(panel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(buttonPanel, BorderLayout.SOUTH);

		draggableCheckBox = new JCheckBox("Draggable by user", heliostat.isDraggable());
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

		JPanel p = new JPanel(new GridLayout(8, 2, 8, 8));
		p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(p, BorderLayout.CENTER);

		p.add(new JLabel("Unique ID:"));
		uidField = new JTextField(heliostat.getUid(), 10);
		uidField.addActionListener(okListener);
		p.add(uidField);

		p.add(new JLabel("Type:"));
		typeComboBox = new JComboBox<String>(new String[] { "Mirror", "Photovoltaic" });
		switch (heliostat.getType()) {
		case Heliostat.MIRROR:
			typeComboBox.setSelectedIndex(0);
			break;
		case Heliostat.PHOTOVOLTAIC:
			typeComboBox.setSelectedIndex(1);
			break;
		}
		p.add(typeComboBox);

		p.add(new JLabel("Label:"));
		labelField = new JTextField(heliostat.getLabel(), 10);
		labelField.addActionListener(okListener);
		p.add(labelField);

		p.add(new JLabel("X (m):"));
		xField = new JTextField(FORMAT.format(heliostat.getShape().getBounds2D().getX()), 10);
		xField.addActionListener(okListener);
		p.add(xField);

		p.add(new JLabel("Y (m):"));
		yField = new JTextField(FORMAT.format(view.model.getLy() - heliostat.getShape().getBounds2D().getY()), 10);
		yField.addActionListener(okListener);
		p.add(yField);

		p.add(new JLabel("Width (m):"));
		wField = new JTextField(FORMAT.format(heliostat.getShape().getBounds2D().getWidth()), 10);
		wField.addActionListener(okListener);
		p.add(wField);

		p.add(new JLabel("Height (m):"));
		hField = new JTextField(FORMAT.format(heliostat.getShape().getBounds2D().getHeight()), 10);
		hField.addActionListener(okListener);
		p.add(hField);

		p.add(new JLabel("Target ID:"));
		targetField = new JTextField(heliostat.getTarget() == null || heliostat.getTarget().getUid() == null ? "" : heliostat.getTarget().getUid());
		targetField.addActionListener(okListener);
		p.add(targetField);

		pack();
		setLocationRelativeTo(view);

	}

	private float parse(String s) {
		return MiscUtil.parse(owner, s);
	}

}