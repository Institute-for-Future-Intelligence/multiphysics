package org.energy2d.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.MenuSelectionManager;

/**
 * @author Charles Xie
 * 
 */

public class ColorArrayPane extends JComponent implements MouseListener, MouseMotionListener {

	private final static int[] VALUES = new int[] { 0, 128, 192, 255 };
	private final static Dimension CELL_SIZE = new Dimension(12, 12);
	private final static int HGAP = 5, VGAP = 5;
	private final static int HMARGIN = 10, VMARGIN = 5;
	private final static List<Rectangle> RECT_LIST = new ArrayList<Rectangle>();
	private final static List<Color> COLOR_LIST = new ArrayList<Color>();

	private static Rectangle selectedRectangle;
	private static Rectangle hoveredRectangle;
	private static int length;

	private List<ColorArrayListener> listenerList;

	private static void init() {

		if (!RECT_LIST.isEmpty())
			return;

		int n = VALUES.length;
		for (int r = 0; r < n; r++) {
			for (int g = 0; g < n; g++) {
				for (int b = 0; b < n; b++) {
					COLOR_LIST.add(new Color(VALUES[r] << 16 | VALUES[g] << 8 | VALUES[b]));
				}
			}
		}

		length = (int) Math.sqrt(n * n * n);

		for (int i = 0; i < length; i++) {
			for (int j = 0; j < length; j++) {
				RECT_LIST.add(new Rectangle(HMARGIN + i * (CELL_SIZE.width + HGAP), VMARGIN + j * (CELL_SIZE.height + VGAP), CELL_SIZE.width, CELL_SIZE.height));
			}
		}

	}

	public ColorArrayPane() {
		init();
		setPreferredSize(new Dimension(length * (CELL_SIZE.width + HGAP) - HGAP + 2 * HMARGIN, length * (CELL_SIZE.height + VGAP) - VGAP + 2 * VMARGIN));
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public JDialog createDialog(Component parent, String title, final JColorChooser colorChooser, final ActionListener a) {

		final JDialog d = new JDialog(JOptionPane.getFrameForComponent(parent), title, true);
		d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		d.getContentPane().add(this, BorderLayout.CENTER);

		JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		d.getContentPane().add(p, BorderLayout.SOUTH);

		JButton b = new JButton("More");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JColorChooser.createDialog(d, "More Colors", true, colorChooser, a, null).setVisible(true);
			}
		});
		p.add(b);

		b = new JButton("Close");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				d.dispose();
			}
		});
		p.add(b);

		d.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				d.dispose();
			}
		});

		d.pack();
		d.setLocationRelativeTo(parent);

		return d;

	}

	public void addColorArrayListener(ColorArrayListener listener) {
		if (listener == null)
			return;
		if (listenerList == null)
			listenerList = new ArrayList<ColorArrayListener>();
		listenerList.add(listener);
	}

	public void removeColorArrayListener(ColorArrayListener listener) {
		if (listener == null)
			return;
		listenerList.remove(listener);
	}

	protected void notifyColorArrayListeners(ColorArrayEvent e) {
		if (listenerList == null || listenerList.isEmpty())
			return;
		for (ColorArrayListener l : listenerList)
			l.colorSelected(e);
	}

	public void setSelectedColor(Color c) {
		selectedRectangle = null;
		for (Color x : COLOR_LIST) {
			if (c.equals(x)) {
				selectedRectangle = RECT_LIST.get(COLOR_LIST.indexOf(x));
				break;
			}
		}
	}

	public Color getSelectedColor() {
		if (selectedRectangle == null)
			return null;
		for (int i = 0, n = RECT_LIST.size(); i < n; i++) {
			if (selectedRectangle == RECT_LIST.get(i))
				return COLOR_LIST.get(i);
		}
		return null;
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		update(g);
	}

	/** this method being public is a side effect of interface implementation */
	public void update(Graphics g) {
		Rectangle rect;
		Color color;
		for (int i = 0, n = RECT_LIST.size(); i < n; i++) {
			color = COLOR_LIST.get(i);
			g.setColor(color);
			rect = RECT_LIST.get(i);
			g.fillRect(rect.x, rect.y, rect.width, rect.height);
			g.setColor(rect == hoveredRectangle ? Color.white : Color.black);
			g.drawRect(rect.x, rect.y, rect.width, rect.height);
			if (rect == selectedRectangle) {
				g.setColor(Color.black);
				g.drawRect(rect.x - 2, rect.y - 2, rect.width + 4, rect.height + 4);
			}
		}
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
		int m = (e.getX() - HMARGIN) / (HGAP + CELL_SIZE.width);
		int n = (e.getY() - VMARGIN) / (VGAP + CELL_SIZE.height);
		int k = m * length + n;
		if (k < 0 || k >= RECT_LIST.size())
			return;
		selectedRectangle = RECT_LIST.get(k);
		MenuSelectionManager.defaultManager().clearSelectedPath();
		notifyColorArrayListeners(new ColorArrayEvent(this, COLOR_LIST.get(k)));
		hoveredRectangle = null;
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
		hoveredRectangle = null;
	}

	public void mouseMoved(MouseEvent e) {
		int m = (e.getX() - HMARGIN) / (HGAP + CELL_SIZE.width);
		int n = (e.getY() - VMARGIN) / (VGAP + CELL_SIZE.height);
		int k = m * length + n;
		if (k < 0 || k >= RECT_LIST.size())
			return;
		hoveredRectangle = RECT_LIST.get(k);
		repaint();
	}

	public void mouseDragged(MouseEvent e) {
	}

}
