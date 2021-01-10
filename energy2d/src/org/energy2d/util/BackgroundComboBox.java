package org.energy2d.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicBorders;

/**
 * @author Charles Xie
 * 
 */

public class BackgroundComboBox extends JComponent implements FocusListener, PropertyChangeListener {

	private SelectionPanel selectionPanel;
	private JButton popButton;
	private ColorMenu colorMenu;

	public BackgroundComboBox(Component parent, boolean filled, JColorChooser colorChooser, TextureChooser textureChooser) {
		this(parent, new ColorMenu(parent, "Background", filled, colorChooser, textureChooser));
	}

	public BackgroundComboBox(Component parent, JColorChooser colorChooser, TextureChooser textureChooser) {
		this(parent, new ColorMenu(parent, "Background", colorChooser, textureChooser));
	}

	private BackgroundComboBox(Component parent, final ColorMenu colorMenu) {

		setLayout(new BorderLayout());
		setBorder(new BasicBorders.ButtonBorder(Color.lightGray, Color.white, Color.black, Color.gray));

		colorMenu.addPropertyChangeListener(this);
		this.colorMenu = colorMenu;

		selectionPanel = new SelectionPanel();
		selectionPanel.setPreferredSize(new Dimension(80, 18));
		selectionPanel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (selectionPanel.isEnabled()) {
					colorMenu.getPopupMenu().show(selectionPanel, 0, selectionPanel.getHeight());
				}
			}
		});
		add(selectionPanel, BorderLayout.CENTER);

		popButton = new JButton(new DownTriangleIcon());
		popButton.setBorder(new BasicBorders.ButtonBorder(Color.gray, Color.black, Color.white, Color.lightGray));
		popButton.setPreferredSize(new Dimension(20, 18));
		popButton.setFocusPainted(false);
		popButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				colorMenu.getPopupMenu().show(selectionPanel, 0, selectionPanel.getHeight());
			}
		});

		add(popButton, BorderLayout.EAST);

		selectionPanel.addFocusListener(this);
		popButton.addFocusListener(this);

	}

	public void setParent(Component parent) {
		colorMenu.setParent(parent);
	}

	public ColorMenu getColorMenu() {
		return colorMenu;
	}

	public void setFillPattern(FillPattern fp) {
		selectionPanel.setFillPattern(fp);
	}

	public FillPattern getFillPattern() {
		return selectionPanel.getFillPattern();
	}

	public void setEnabled(boolean b) {
		selectionPanel.setEnabled(b);
		popButton.setEnabled(b);
	}

	public void focusLost(FocusEvent e) {
		selectionPanel.setBorder(new LineBorder(selectionPanel.unselectedColor, 2));
		selectionPanel.repaint();
	}

	public void focusGained(FocusEvent e) {
		selectionPanel.setBorder(new LineBorder(selectionPanel.selectedColor, 2));
		selectionPanel.repaint();
	}

	public void propertyChange(PropertyChangeEvent e) {

		String name = e.getPropertyName();

		if (name.equals(ColorMenu.FILLING)) {
			Object obj = e.getNewValue();
			if (obj instanceof ColorFill) {
				selectionPanel.setFillPattern((ColorFill) obj);
			} else if (obj instanceof Texture) {
				selectionPanel.setFillPattern((Texture) obj);
			}
			selectionPanel.repaint();
		}

	}

	protected class SelectionPanel extends JComponent {

		private Color selectedColor = Color.black;
		private Color unselectedColor = Color.white;
		private FillPattern fillPattern;

		public SelectionPanel() {
			setBorder(new LineBorder(unselectedColor, 2));
		}

		public boolean getLockRatio() {
			return false;
		}

		public void setFillPattern(FillPattern fp) {
			fillPattern = fp;
		}

		public FillPattern getFillPattern() {
			return fillPattern;
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (fillPattern instanceof Texture) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setPaint(((Texture) fillPattern).getPaint());
				g2.fillRect(0, 0, getWidth(), getHeight());
			} else if (fillPattern instanceof ColorFill) {
				g.setColor(((ColorFill) fillPattern).getColor());
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		}

	}

	private class DownTriangleIcon implements Icon {

		public void paintIcon(Component c, Graphics g, int x, int y) {
			int w = getIconWidth();
			int h = getIconHeight();
			Polygon triangle = new Polygon();
			triangle.addPoint(x, y);
			triangle.addPoint(x + w, y);
			triangle.addPoint(x + w / 2, y + h);
			g.setColor(popButton.isEnabled() ? SystemColor.textText : SystemColor.textInactiveText);
			g.fillPolygon(triangle);
		}

		public int getIconWidth() {
			return 6;
		}

		public int getIconHeight() {
			return 4;
		}

	}

}