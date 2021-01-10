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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.energy2d.event.ManipulationEvent;
import org.energy2d.undo.UndoResizeManipulable;
import org.energy2d.undo.UndoTranslateManipulable;
import org.energy2d.util.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class PictureDialog extends JDialog {

	private final static DecimalFormat FORMAT = new DecimalFormat("####.######");

	private JTextField xField;
	private JTextField yField;
	private JTextField widthField;
	private JTextField heightField;
	private JTextField uidField;
	private JTextField labelField;
	private JTextField fileNameField;
	private JCheckBox draggableCheckBox;
	private Window owner;
	private ActionListener okListener;

	PictureDialog(final View2D view, final Picture picture, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "Image (#" + view.getPictures().indexOf(picture) + ") Properties", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		owner = getOwner();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				view.notifyManipulationListeners(picture, ManipulationEvent.PROPERTY_CHANGE);
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
				float width = parse(widthField.getText());
				if (Float.isNaN(width))
					return;
				float height = parse(heightField.getText());
				if (Float.isNaN(height))
					return;
				String uid = uidField.getText();
				if (uid != null) {
					uid = uid.trim();
					if (!uid.equals("") && !uid.equals(picture.getUid())) {
						if (view.isUidUsed(uid)) {
							JOptionPane.showMessageDialog(owner, "UID: " + uid + " has been taken.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
				}

				// undo
				float dx = 0.000001f * view.model.getLx();
				float dy = 0.000001f * view.model.getLy();
				boolean moved = Math.abs(x - picture.getX()) > dx || Math.abs(y - picture.getY()) > dy;
				if (moved)
					view.getUndoManager().addEdit(new UndoTranslateManipulable(view));

				boolean resized = Math.abs(width - picture.getWidth()) > dx || Math.abs(height - picture.getHeight()) > dy;
				if (resized)
					view.getUndoManager().addEdit(new UndoResizeManipulable(view));

				if (uid != null && !uid.equals(""))
					picture.setUid(uid);
				String label = labelField.getText();
				if (label != null && !label.equals(""))
					picture.setLabel(label);
				picture.setX(x);
				picture.setY(y);
				picture.setWidth(width);
				picture.setHeight(height);
				picture.setDraggable(draggableCheckBox.isSelected());
				view.notifyManipulationListeners(picture, ManipulationEvent.PROPERTY_CHANGE);
				view.setSelectedManipulable(view.getSelectedManipulable());
				view.repaint();
				dispose();
			}
		};

		JPanel panel = new JPanel(new BorderLayout());
		setContentPane(panel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(buttonPanel, BorderLayout.SOUTH);

		draggableCheckBox = new JCheckBox("Draggable by user", picture.isDraggable());
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
		uidField = new JTextField(picture.getUid(), 10);
		uidField.addActionListener(okListener);
		p.add(uidField);

		p.add(new JLabel("File Name:"));
		fileNameField = new JTextField(picture.getFileName(), 10);
		fileNameField.setEnabled(false);
		fileNameField.setEditable(false);
		p.add(fileNameField);

		p.add(new JLabel("Label:"));
		labelField = new JTextField(picture.getLabel(), 10);
		labelField.addActionListener(okListener);
		p.add(labelField);

		p.add(new JLabel("X (m):"));
		xField = new JTextField(FORMAT.format(picture.getX()), 10);
		xField.addActionListener(okListener);
		p.add(xField);

		p.add(new JLabel("Y (m):"));
		yField = new JTextField(FORMAT.format(picture.getY()), 10);
		yField.addActionListener(okListener);
		p.add(yField);

		p.add(new JLabel("Width (m):"));
		widthField = new JTextField(FORMAT.format(picture.getWidth()), 10);
		widthField.addActionListener(okListener);
		p.add(widthField);

		p.add(new JLabel("Height (m):"));
		heightField = new JTextField(FORMAT.format(picture.getHeight()), 10);
		heightField.addActionListener(okListener);
		p.add(heightField);

		pack();
		setLocationRelativeTo(view);

	}

	private float parse(String s) {
		return MiscUtil.parse(owner, s);
	}

}