package org.energy2d.util;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

/**
 * @author Charles Xie
 * 
 */

public class TextureChooser extends JTabbedPane {

	private TexturePanel[] pp;
	private FillPattern fillPattern;
	private ColorComboBox bgComboBox;
	private ColorComboBox fgComboBox;

	public TextureChooser() {
		super();
		addTab("Texture", createTexturePanel());
	}

	public void setSelectedForegroundColor(Color c) {
		fgComboBox.setColor(c);
		setTexturePanelForeground(c);
	}

	public void setSelectedBackgroundColor(Color c) {
		bgComboBox.setColor(c);
		setTexturePanelBackground(c);
	}

	public void setSelectedStyle(byte style, int cellWidth, int cellHeight) {
		setSelectedTexturePanel(style, cellWidth, cellHeight);
	}

	/**
	 * Creates and returns a new dialog containing the specified TextureChooser pane along with "OK", "Cancel", and "Reset" buttons. If the "OK" or "Cancel" buttons are pressed, the dialog is automatically hidden (but not disposed). If the "Reset" button is pressed, the chooser's selection will be reset to the option which was set the last time show was invoked on the dialog and the dialog will remain showing.
	 */
	public static JDialog createDialog(Component parent, String title, boolean modal, TextureChooser chooser, ActionListener okListener, ActionListener cancelListener) {

		final JDialog dialog = new JDialog(JOptionPane.getFrameForComponent(parent), title == null ? "Fill Effects" : title, modal);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		Container container = dialog.getContentPane();
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		container.add(panel, BorderLayout.CENTER);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 6;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(chooser, c);

		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(new JLabel(), c);

		final ActionListener okListener1 = okListener;
		final TextureChooser chooser1 = chooser;

		JButton button = new JButton("OK");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switch (chooser1.getSelectedIndex()) {
				case 0:
					TexturePanel pp = chooser1.getSelectedTexturePanel();
					if (pp != null) {
						chooser1.fillPattern = new Texture(pp.getForeground().getRGB(), pp.getBackground().getRGB(), pp.getStyle(), pp.getCellWidth(), pp.getCellHeight());
					}
					break;
				}
				okListener1.actionPerformed(e);
				dialog.dispose();
			}
		});
		c.gridx = 1;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(button, c);

		button = new JButton("Cancel");
		if (cancelListener != null) {
			button.addActionListener(cancelListener);
		} else {
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dialog.dispose();
				}
			});
		}
		c.gridy = 2;
		panel.add(button, c);

		dialog.pack();

		if (parent == null) {
			dialog.setLocation(200, 200);
		} else {
			dialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(parent));
		}

		return dialog;

	}

	private void setSelectedTexturePanel(byte style, int cellWidth, int cellHeight) {
		for (TexturePanel p : pp)
			p.setSelected(p.getStyle() == style && p.getCellWidth() == cellWidth && p.getCellHeight() == cellHeight);
	}

	private TexturePanel getSelectedTexturePanel() {
		for (TexturePanel p : pp) {
			if (p.isSelected())
				return p;
		}
		return null;
	}

	private void setTexturePanelForeground(Color c) {
		for (TexturePanel p : pp)
			p.setForeground(c);
	}

	private void setTexturePanelBackground(Color c) {
		for (TexturePanel p : pp)
			p.setBackground(c);
	}

	private JPanel createTexturePanel() {

		JPanel p = new JPanel(new BorderLayout(10, 10));

		final int size = TextureFactory.textureList.size();
		final int grid = (int) Math.sqrt(size + 0.0001);

		final JPanel texturePanel = new JPanel(new GridLayout(grid, grid * grid < size ? grid + 1 : grid, 2, 2));
		texturePanel.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				int iSelected = -1;
				for (int k = 0; k < pp.length; k++) {
					if (pp[k].isSelected())
						iSelected = k;
				}
				if (iSelected == -1)
					return;
				switch (e.getKeyCode()) {
				case KeyEvent.VK_UP:
					if (iSelected >= grid) {
						pp[iSelected].setSelected(false);
						iSelected -= grid;
						pp[iSelected].setSelected(true);
						texturePanel.repaint();
					}
					break;
				case KeyEvent.VK_DOWN:
					if (iSelected < size - grid) {
						pp[iSelected].setSelected(false);
						iSelected += grid;
						pp[iSelected].setSelected(true);
						texturePanel.repaint();
					}
					break;
				case KeyEvent.VK_LEFT:
					if (iSelected > 0) {
						pp[iSelected].setSelected(false);
						iSelected--;
						pp[iSelected].setSelected(true);
						texturePanel.repaint();
					}
					break;
				case KeyEvent.VK_RIGHT:
					if (iSelected < size - 1) {
						pp[iSelected].setSelected(false);
						iSelected++;
						pp[iSelected].setSelected(true);
						texturePanel.repaint();
					}
					break;
				}
			}
		});
		p.add(texturePanel, BorderLayout.CENTER);

		pp = new TexturePanel[size];
		int cell = 10;
		for (int i = 0; i < pp.length; i++) {
			TextureCode tc = TextureFactory.textureList.get(i);
			switch (tc.size) {
			case TextureFactory.SMALL:
				cell = 4;
				break;
			case TextureFactory.MEDIUM:
				cell = 8;
				break;
			case TextureFactory.LARGE:
				cell = 12;
				break;
			case TextureFactory.HUGE:
				cell = 36;
				break;
			}
			pp[i] = new TexturePanel(tc.style, cell, cell);
			texturePanel.add(pp[i]);
			final int ii = i;
			pp[i].addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					for (int k = 0; k < pp.length; k++) {
						pp[k].setSelected(k == ii);
						pp[k].repaint();
					}
					texturePanel.requestFocusInWindow();
				}
			});
		}
		pp[0].setSelected(true);

		JPanel p4 = new JPanel(new GridLayout(1, 2, 10, 10));
		p4.setBorder(new EmptyBorder(10, 10, 10, 10));
		p.add(p4, BorderLayout.SOUTH);

		JPanel p3 = new JPanel(new BorderLayout());
		p4.add(p3);

		JLabel label = new JLabel("Foreground Color:");
		p3.add(label, BorderLayout.NORTH);
		fgComboBox = new ColorComboBox(this);
		fgComboBox.setRenderer(new ComboBoxRenderer.ColorCell());
		fgComboBox.setToolTipText("Foreground color");
		fgComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final ColorComboBox cb = (ColorComboBox) e.getSource();
				int id = cb.getSelectedIndex();
				if (id >= ColorRectangle.COLORS.length + 1) {
					cb.updateColor(new Runnable() {
						public void run() {
							setTexturePanelForeground(cb.getMoreColor());
						}
					});
				} else if (id == ColorRectangle.COLORS.length) {
					setTexturePanelForeground(cb.getMoreColor());
				} else {
					setTexturePanelForeground(ColorRectangle.COLORS[id]);
				}
			}
		});
		p3.add(fgComboBox, BorderLayout.CENTER);
		label.setLabelFor(fgComboBox);

		p3 = new JPanel(new BorderLayout());
		p4.add(p3);

		label = new JLabel("Background Color:");
		p3.add(label, BorderLayout.NORTH);
		bgComboBox = new ColorComboBox(this);
		bgComboBox.setRenderer(new ComboBoxRenderer.ColorCell());
		bgComboBox.setToolTipText("Background color");
		bgComboBox.setSelectedIndex(ColorRectangle.COLORS.length);
		bgComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final ColorComboBox cb = (ColorComboBox) e.getSource();
				int id = cb.getSelectedIndex();
				if (id >= ColorRectangle.COLORS.length + 1) {
					cb.updateColor(new Runnable() {
						public void run() {
							setTexturePanelBackground(cb.getMoreColor());
						}
					});
				} else if (id == ColorRectangle.COLORS.length) {
					setTexturePanelBackground(cb.getMoreColor());
				} else {
					setTexturePanelBackground(ColorRectangle.COLORS[id]);
				}
			}
		});
		p3.add(bgComboBox, BorderLayout.CENTER);
		label.setLabelFor(bgComboBox);

		return p;

	}

	class TexturePanel extends JPanel {

		private final BasicStroke highlightOutside = new BasicStroke(4);
		private final BasicStroke highlightInside = new BasicStroke(2);
		private boolean selected;
		private byte style = TextureFactory.POLKA;
		private int cellWidth = 12;
		private int cellHeight = 12;

		public TexturePanel(byte style, int cellWidth, int cellHeight) {
			setBackground(Color.white);
			setForeground(Color.black);
			this.style = style;
			this.cellWidth = cellWidth;
			this.cellHeight = cellHeight;
			setPreferredSize(new Dimension(36, 36));
		}

		public void setStyle(byte i) {
			style = i;
		}

		public byte getStyle() {
			return style;
		}

		public void setCellWidth(int i) {
			cellWidth = i;
		}

		public int getCellWidth() {
			return cellWidth;
		}

		public void setCellHeight(int i) {
			cellHeight = i;
		}

		public int getCellHeight() {
			return cellHeight;
		}

		public void setSelected(boolean b) {
			selected = b;
		}

		public boolean isSelected() {
			return selected;
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			int w = getWidth();
			int h = getHeight();
			g2d.setPaint(TextureFactory.createPattern(style, cellWidth, cellHeight, getForeground(), getBackground()));
			g2d.fillRect(0, 0, w, h);
			if (selected) {
				g2d.setStroke(highlightOutside);
				g2d.setColor(Color.black);
				g2d.drawRect(2, 2, w - 5, h - 5);
				g2d.setStroke(highlightInside);
				g2d.setColor(Color.white);
				g2d.drawRect(2, 2, w - 5, h - 5);
			}
		}

	}

	public FillPattern getFillPattern() {
		return fillPattern;
	}

	public void setFillPattern(FillPattern fp) {
		fillPattern = fp;
	}

}
