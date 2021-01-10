package org.energy2d.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 * @author Charles Xie
 * 
 */

public class ColorMenu extends JMenu {

	public final static String FILLING = "Filling";

	protected JColorChooser colorChooser;
	protected TextureChooser textureChooser;

	private Component parent;
	private JMenuItem noFillMenuItem;
	private JMenuItem moreColorMenuItem;
	private JMenuItem hexColorMenuItem;
	private JMenuItem textureMenuItem;
	private ColorArrayPane cap;

	public ColorMenu(Component parent, String name, JColorChooser color, TextureChooser texture) {
		super(name);
		this.parent = parent;
		colorChooser = color;
		textureChooser = texture;
		init();
	}

	public ColorMenu(Component parent, String name, boolean filled, JColorChooser color, TextureChooser texture) {
		super(name);
		this.parent = parent;
		colorChooser = color;
		textureChooser = texture;
		noFillMenuItem = new JCheckBoxMenuItem("No Fill", !filled);
		add(noFillMenuItem);
		addSeparator();
		init();
	}

	private void init() {

		cap = new ColorArrayPane();
		cap.addColorArrayListener(new ColorArrayListener() {
			public void colorSelected(ColorArrayEvent e) {
				doSelection();
				ColorMenu.this.firePropertyChange(FILLING, null, new ColorFill(e.getSelectedColor()));
			}
		});
		add(cap);
		addSeparator();

		moreColorMenuItem = new JMenuItem("More Colors...");
		add(moreColorMenuItem);

		hexColorMenuItem = new JMenuItem("Hex Color...");
		add(hexColorMenuItem);

		if (textureChooser != null) {
			textureMenuItem = new JMenuItem("Texture...");
			add(textureMenuItem);
		}

	}

	public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		super.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void setParent(Component parent) {
		this.parent = parent;
	}

	public void setColorChooser(JColorChooser cc) {
		colorChooser = cc;
	}

	public JColorChooser getColorChooser() {
		return colorChooser;
	}

	public void setTextureChooser(TextureChooser fec) {
		textureChooser = fec;
	}

	public TextureChooser getTextureChooser() {
		return textureChooser;
	}

	public void addNoFillListener(ActionListener a) {
		if (noFillMenuItem != null)
			noFillMenuItem.addActionListener(a);
	}

	public void removeNoFillListener(ActionListener a) {
		if (noFillMenuItem != null)
			noFillMenuItem.removeActionListener(a);
	}

	public void setNoFillAction(Action a) {
		if (noFillMenuItem != null)
			noFillMenuItem.setAction(a);
	}

	public void addColorArrayListener(ActionListener a) {
		addActionListener(a);
	}

	public void removeColorArrayListener(ActionListener a) {
		removeActionListener(a);
	}

	public void setColorArrayAction(Action a) {
		setAction(a);
	}

	public Color getHexInputColor(Color oldColor) {
		String s = oldColor != null ? Integer.toHexString(oldColor.getRGB() & 0x00ffffff) : "";
		int m = 6 - s.length();
		if (m != 6 && m != 0) {
			for (int k = 0; k < m; k++)
				s = "0" + s;
		}
		String hex = JOptionPane.showInputDialog(parent, "Input a hex color number:", s);
		if (hex == null)
			return null;
		Color c = oldColor;
		try {
			c = MiscUtil.convertToColor(hex);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(parent, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
		return c;
	}

	public void addMoreColorListener(final ActionListener a) {
		moreColorMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JColorChooser.createDialog(parent, "Background Color", true, colorChooser, a, null).setVisible(true);
			}
		});
	}

	public void addHexColorListener(ActionListener a) {
		hexColorMenuItem.addActionListener(a);
	}

	public void setMoreColorAction(final ActionListener a) {
		moreColorMenuItem.setAction(new AbstractAction("More Colors") {
			public void actionPerformed(ActionEvent e) {
				JColorChooser.createDialog(parent, "Background Color", true, colorChooser, a, null).setVisible(true);
			}
		});
	}

	public void addTextureListeners(final ActionListener ok, final ActionListener cancel) {
		if (textureMenuItem != null)
			textureMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					TextureChooser.createDialog(parent, "Texture", true, textureChooser, ok, cancel).setVisible(true);
				}
			});
	}

	public void setTextureActions(final ActionListener ok, final ActionListener cancel) {
		textureMenuItem.setAction(new AbstractAction("Texture") {
			public void actionPerformed(ActionEvent e) {
				TextureChooser.createDialog(parent, "Texture", true, textureChooser, ok, cancel).setVisible(true);
			}
		});
	}

	public void setColor(Color c) {
		cap.setSelectedColor(c);
	}

	public Color getColor() {
		return cap.getSelectedColor();
	}

	public void doSelection() {
		fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getActionCommand()));
	}

}
