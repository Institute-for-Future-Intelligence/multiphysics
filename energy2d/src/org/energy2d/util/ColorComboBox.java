package org.energy2d.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

/**
 * This is a combo box color chooser in which the <tt>JColorChooser</tt> is combined.
 * 
 * @author Charles Xie
 */

public class ColorComboBox extends JComboBox<Integer> {

	public final static int INDEX_MORE_COLOR = ColorRectangle.COLORS.length;
	public final static int INDEX_COLOR_CHOOSER = 100;
	public final static int INDEX_HEX_INPUTTER = 200;

	private Color color6 = Color.white;
	private Color previousColor;
	private Runnable runnable;
	private static JColorChooser colorChooser;
	private Component parent;

	public ColorComboBox(Component parent0) {

		setParent(parent0);

		if (colorChooser == null)
			colorChooser = new JColorChooser();

		setRenderer(new ComboBoxRenderer.ColorCell());
		for (int i = 0; i <= INDEX_MORE_COLOR; i++)
			addItem(i);
		addItem(INDEX_COLOR_CHOOSER);
		addItem(INDEX_HEX_INPUTTER);
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int id = ((Integer) getSelectedItem()).intValue();
				if (id == INDEX_COLOR_CHOOSER) {
					JColorChooser.createDialog(parent, "More Colors", true, colorChooser, new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							color6 = colorChooser.getColor();
							setSelectedIndex(INDEX_MORE_COLOR);
							ColorRectangle cr = (ColorRectangle) getRenderer();
							cr.setMoreColor(color6);
							if (runnable != null)
								EventQueue.invokeLater(runnable);
						}
					}, null).setVisible(true);
				} else if (id == INDEX_HEX_INPUTTER) {
					String s = previousColor != null ? Integer.toHexString(previousColor.getRGB() & 0x00ffffff) : "";
					int m = 6 - s.length();
					if (m != 6 && m != 0) {
						for (int k = 0; k < m; k++)
							s = "0" + s;
					}
					String hex = JOptionPane.showInputDialog(parent, "Input a hex color number:", s);
					if (hex == null)
						return;
					try {
						color6 = MiscUtil.convertToColor(hex);
					} catch (NumberFormatException ex) {
						JOptionPane.showMessageDialog(parent, "Input color (hex) is not valid.");
						setSelectedIndex(INDEX_MORE_COLOR);
						return;
					}
					setSelectedIndex(INDEX_MORE_COLOR);
					ColorRectangle cr = (ColorRectangle) getRenderer();
					cr.setMoreColor(color6);
					if (runnable != null)
						EventQueue.invokeLater(runnable);
				}
				previousColor = getSelectedColor();
			}
		});

	}

	public void setParent(Component parent) {
		this.parent = parent;
	}

	public static void setColorChooser(JColorChooser cc) {
		colorChooser = cc;
	}

	public static JColorChooser getColorChooser() {
		return colorChooser;
	}

	/** @return the latest color selected from the <tt>JColorChooser</tt> */
	public Color getMoreColor() {
		return color6;
	}

	/**
	 * when the user chooses a color from the <tt>JColorChooser</tt>, do the job of updating the color of the target object if there is one. The job has to be passed through by a <tt>Runnable</tt> wrapper.
	 */
	public void updateColor(Runnable r) {
		runnable = r;
	}

	public void setColor(Color c) {
		if (c == null)
			return;
		boolean b = false;
		for (int i = 0; i < INDEX_MORE_COLOR; i++) {
			if (c.equals(ColorRectangle.COLORS[i])) {
				setSelectedIndex(i);
				b = true;
			}
		}
		if (!b) {
			((ColorRectangle) getRenderer()).setMoreColor(c);
			setSelectedIndex(INDEX_MORE_COLOR);
			color6 = c;
		}
	}

	public Color getSelectedColor() {
		int i = getSelectedIndex();
		if (i < INDEX_MORE_COLOR)
			return ColorRectangle.COLORS[i];
		return color6;
	}

}