package org.energy2d.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.energy2d.event.ManipulationEvent;
import org.energy2d.model.Tree;
import org.energy2d.util.BackgroundComboBox;
import org.energy2d.util.ColorFill;
import org.energy2d.util.ColorMenu;

/**
 * @author Charles Xie
 * 
 */
class TreeDialog extends JDialog {

	private JColorChooser colorChooser;
	private JCheckBox draggableCheckBox;
	private BackgroundComboBox bgComboBox;
	private JComboBox<String> typeComboBox;
	private ActionListener okListener;

	TreeDialog(final View2D view, final Tree tree, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "Tree (#" + view.model.getTrees().indexOf(tree) + ") Properties", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				view.notifyManipulationListeners(tree, ManipulationEvent.PROPERTY_CHANGE);
				view.repaint();
				dispose();
			}
		});

		okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = typeComboBox.getSelectedItem().toString();
				if ("Pine".equalsIgnoreCase(s))
					tree.setType(Tree.PINE);
				else if ("Regular".equalsIgnoreCase(s))
					tree.setType(Tree.REGULAR);
				tree.setDraggable(draggableCheckBox.isSelected());
				view.notifyManipulationListeners(tree, ManipulationEvent.PROPERTY_CHANGE);
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

		p.add(new JLabel("Type:"));
		typeComboBox = new JComboBox<String>(new String[] { "Regular", "Pine" });
		switch (tree.getType()) {
		case Tree.REGULAR:
			typeComboBox.setSelectedIndex(0);
			break;
		case Tree.PINE:
			typeComboBox.setSelectedIndex(1);
			break;
		}
		p.add(typeComboBox);

		p.add(new JLabel("Color:"));
		colorChooser = new JColorChooser();
		colorChooser.setColor(tree.getColor());
		bgComboBox = new BackgroundComboBox(this, colorChooser, null);
		bgComboBox.setToolTipText("Background color");
		bgComboBox.setFillPattern(new ColorFill(tree.getColor()));
		bgComboBox.getColorMenu().setColorArrayAction(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				Color c = bgComboBox.getColorMenu().getColor();
				tree.setColor(c);
				view.repaint();
				bgComboBox.getColorMenu().firePropertyChange(ColorMenu.FILLING, null, new ColorFill(c));
			}
		});
		bgComboBox.getColorMenu().setMoreColorAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color c = bgComboBox.getColorMenu().getColorChooser().getColor();
				tree.setColor(c);
				view.repaint();
				bgComboBox.getColorMenu().firePropertyChange(ColorMenu.FILLING, null, new ColorFill(c));
			}
		});
		bgComboBox.getColorMenu().addHexColorListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color c = bgComboBox.getColorMenu().getHexInputColor(tree.getColor());
				if (c == null)
					return;
				tree.setColor(c);
				view.repaint();
				bgComboBox.getColorMenu().firePropertyChange(ColorMenu.FILLING, null, new ColorFill(c));
			}
		});
		p.add(bgComboBox);

		draggableCheckBox = new JCheckBox("Draggable by user", tree.isDraggable());
		p.add(draggableCheckBox);

		pack();
		setLocationRelativeTo(view);

	}

}
