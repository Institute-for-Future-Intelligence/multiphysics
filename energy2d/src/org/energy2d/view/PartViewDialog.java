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
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.energy2d.event.ManipulationEvent;
import org.energy2d.model.Part;
import org.energy2d.util.BackgroundComboBox;
import org.energy2d.util.ColorFill;
import org.energy2d.util.ColorMenu;
import org.energy2d.util.TextureChooser;
import org.energy2d.util.FillPattern;
import org.energy2d.util.Texture;

/**
 * @author Charles Xie
 * 
 */
class PartViewDialog extends JDialog {

	private JColorChooser colorChooser;
	private TextureChooser textureChooser;
	private JCheckBox visibleCheckBox;
	private JCheckBox draggableCheckBox;
	private JCheckBox seeThroughCheckBox;
	private BackgroundComboBox bgComboBox;
	private ActionListener okListener;

	PartViewDialog(final View2D view, final Part part, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "Part (#" + view.model.getParts().indexOf(part) + ") View Options", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				view.notifyManipulationListeners(part, ManipulationEvent.PROPERTY_CHANGE);
				view.repaint();
				dispose();
			}
		});

		okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				part.setDraggable(draggableCheckBox.isSelected());
				part.setVisible(visibleCheckBox.isSelected());
				part.setFilled(!seeThroughCheckBox.isSelected());
				view.notifyManipulationListeners(part, ManipulationEvent.PROPERTY_CHANGE);
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

		p.add(new JLabel("Filling"));
		colorChooser = new JColorChooser();
		textureChooser = new TextureChooser();
		FillPattern fp = part.getFillPattern();
		if (fp instanceof ColorFill) {
			Color bgColor = ((ColorFill) fp).getColor();
			colorChooser.setColor(bgColor);
			textureChooser.setSelectedBackgroundColor(bgColor);
		} else if (fp instanceof Texture) {
			Texture texture = (Texture) fp;
			textureChooser.setSelectedBackgroundColor(new Color(texture.getBackground()));
			textureChooser.setSelectedForegroundColor(new Color(texture.getForeground()));
			textureChooser.setSelectedStyle(texture.getStyle(), texture.getCellWidth(), texture.getCellHeight());
		}

		bgComboBox = new BackgroundComboBox(this, part.isFilled(), colorChooser, textureChooser);
		bgComboBox.setToolTipText("Background filling");
		bgComboBox.setFillPattern(part.getFillPattern());
		bgComboBox.getColorMenu().setNoFillAction(new AbstractAction("No Fill") {
			public void actionPerformed(ActionEvent e) {
				Object src = e.getSource();
				if (src instanceof JCheckBoxMenuItem) {
					JCheckBoxMenuItem cbmi = (JCheckBoxMenuItem) src;
					part.setFilled(!cbmi.isSelected());
					view.repaint();
					bgComboBox.getColorMenu().firePropertyChange(ColorMenu.FILLING, null, null);
				}
			}
		});
		bgComboBox.getColorMenu().setColorArrayAction(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				FillPattern fp = new ColorFill(bgComboBox.getColorMenu().getColor());
				if (fp.equals(part.getFillPattern()))
					return;
				part.setFillPattern(fp);
				view.repaint();
				bgComboBox.getColorMenu().firePropertyChange(ColorMenu.FILLING, null, fp);
			}
		});
		bgComboBox.getColorMenu().setMoreColorAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FillPattern fp = new ColorFill(bgComboBox.getColorMenu().getColorChooser().getColor());
				if (fp.equals(part.getFillPattern()))
					return;
				part.setFillPattern(fp);
				view.repaint();
				bgComboBox.getColorMenu().firePropertyChange(ColorMenu.FILLING, null, fp);
			}
		});
		bgComboBox.getColorMenu().addHexColorListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color c = bgComboBox.getColorMenu().getHexInputColor(part.getFillPattern() instanceof ColorFill ? ((ColorFill) part.getFillPattern()).getColor() : null);
				if (c == null)
					return;
				FillPattern fp = new ColorFill(c);
				if (fp.equals(part.getFillPattern()))
					return;
				part.setFillPattern(fp);
				view.repaint();
				bgComboBox.getColorMenu().firePropertyChange(ColorMenu.FILLING, null, fp);
			}
		});
		bgComboBox.getColorMenu().setTextureActions(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FillPattern fp = bgComboBox.getColorMenu().getTextureChooser().getFillPattern();
				if (fp != null && fp.equals(part.getFillPattern()))
					return;
				part.setFillPattern(fp);
				view.repaint();
				bgComboBox.getColorMenu().firePropertyChange(ColorMenu.FILLING, null, fp);
			}
		}, null);
		p.add(bgComboBox);

		visibleCheckBox = new JCheckBox("Visible", part.isVisible());
		p.add(visibleCheckBox);

		seeThroughCheckBox = new JCheckBox("See through", !part.isFilled());
		p.add(seeThroughCheckBox);

		draggableCheckBox = new JCheckBox("Draggable by user", part.isDraggable());
		p.add(draggableCheckBox);

		pack();
		setLocationRelativeTo(view);

	}

}
