package org.concord.energy2d.system;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.concord.energy2d.event.ManipulationEvent;

/**
 * @author Charles Xie
 * 
 */
class PreferencesDialog extends JDialog {

	private JCheckBox snapToGridCheckBox;
	private ActionListener okListener;

	PreferencesDialog(final System2D s2d, boolean modal) {

		super(JOptionPane.getFrameForComponent(s2d.view), "Preferences", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				s2d.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
				s2d.view.repaint();
				dispose();
			}
		});

		okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				s2d.view.setSnapToGrid(snapToGridCheckBox.isSelected());
				s2d.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
				s2d.view.repaint();
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

		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setBorder(BorderFactory.createTitledBorder("Edit Mode"));
		box.add(p);

		snapToGridCheckBox = new JCheckBox("Snap to computational grid (" + s2d.model.getNx() + " x " + s2d.model.getNy() + ")", s2d.view.isSnapToGrid());
		snapToGridCheckBox.setToolTipText("Should objects' shapes and coordinates be snapped to the computational grid?");
		p.add(snapToGridCheckBox);

		pack();
		setLocationRelativeTo(s2d.view);

	}
}
