package org.energy2d.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import org.energy2d.event.ManipulationEvent;
import org.energy2d.undo.UndoTranslateManipulable;
import org.energy2d.util.ColorComboBox;
import org.energy2d.util.ColorRectangle;
import org.energy2d.util.ComboBoxRenderer;
import org.energy2d.util.MiscUtil;

/**
 * @author Charles Xie
 * 
 */

class TextBoxPanel extends JPanel {

	private final static String[] FONT_FAMILY_NAMES = new String[] { "Arial", "Arial Black", "Book Antiqua", "Comic Sans MS", "Courier New", "Default", "Dialog", "DialogInput", "Monospaced", "SansSerif", "Serif", "Times New Roman", "Verdana" };
	private final static Integer[] FONT_SIZE = new Integer[] { 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 36, 48 };
	private final static DecimalFormat FORMAT = new DecimalFormat("####.####");

	private View2D view;
	private TextBox textBox;
	private JDialog dialog;
	private static Point offset;
	private boolean cancelled;

	private JTextField uidField;
	private JCheckBox borderCheckBox;
	private JCheckBox draggableCheckBox;
	private JTextField xField, yField;
	private JComboBox<String> fontNameComboBox;
	private JComboBox<Integer> fontSizeComboBox;
	private ColorComboBox fontColorComboBox;
	private JToggleButton boldButton, italicButton;
	private JTextArea textArea;
	private TextBox copy;

	TextBoxPanel(TextBox t, View2D v) {

		super(new BorderLayout(5, 5));

		if (t == null)
			throw new IllegalArgumentException("input cannot be null");
		textBox = t;
		view = v;
		storeSettings();

		ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				float x = parse(xField.getText());
				if (Float.isNaN(x))
					return;
				float y = parse(yField.getText());
				if (Float.isNaN(y))
					return;

				// undo
				float dx = 0.000001f * view.model.getLx();
				float dy = 0.000001f * view.model.getLy();
				boolean moved = Math.abs(x - textBox.getX()) > dx || Math.abs(y - textBox.getY()) > dy;
				if (moved)
					view.getUndoManager().addEdit(new UndoTranslateManipulable(view));

				String uid = uidField.getText();
				if (uid != null && !uid.trim().equals("")) {
					textBox.setUid(uid.trim());
				} else {
					textBox.setUid(null);
				}
				textBox.setLabel(textArea.getText());
				textBox.setX(x);
				textBox.setY(y);
				if (dialog != null) {
					offset = dialog.getLocationOnScreen();
					dialog.dispose();
				}
				view.repaint();
				view.notifyManipulationListeners(textBox, ManipulationEvent.OBJECT_ADDED);

			}
		};

		textArea = new JTextArea(textBox.getLabel(), 5, 10);
		textArea.setForeground(textBox.getColor());
		textArea.setFont(textBox.getFont());
		textArea.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				textBox.setLabel(textArea.getText());
				view.repaint();
			}
		});

		add(new JScrollPane(textArea), BorderLayout.CENTER);

		JPanel p = new JPanel(new BorderLayout(5, 5));
		p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		add(p, BorderLayout.NORTH);

		JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		p.add(p2, BorderLayout.NORTH);

		JLabel label = new JLabel("Unique ID:");
		p2.add(label);
		uidField = new JTextField(textBox.getUid(), 8);
		uidField.addActionListener(okListener);
		p2.add(uidField);

		label = new JLabel("X:");
		p2.add(label);
		xField = new JTextField(FORMAT.format(t.getX()), 10);
		xField.addActionListener(okListener);
		p2.add(xField);

		label = new JLabel("Y:");
		p2.add(label);
		yField = new JTextField(FORMAT.format(t.getY()), 10);
		yField.addActionListener(okListener);
		p2.add(yField);

		ActionListener styleListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int style = (boldButton.isSelected() ? Font.BOLD : Font.PLAIN) | (italicButton.isSelected() ? Font.ITALIC : Font.PLAIN);
				textBox.setStyle(style);
				textArea.setFont(textBox.getFont());
				view.repaint();
			}
		};

		boldButton = new JToggleButton("<html><b>B</b></html>");
		boldButton.setSelected((textBox.getFont().getStyle() & Font.BOLD) == Font.BOLD);
		boldButton.addActionListener(styleListener);
		p2.add(boldButton);

		italicButton = new JToggleButton("<html><b><i>I</i></b></html>");
		italicButton.setSelected((textBox.getFont().getStyle() & Font.ITALIC) == Font.ITALIC);
		italicButton.addActionListener(styleListener);
		p2.add(italicButton);

		p2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		p.add(p2, BorderLayout.CENTER);

		borderCheckBox = new JCheckBox("Border");
		borderCheckBox.setSelected(textBox.hasBorder());
		borderCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				textBox.setBorder(e.getStateChange() == ItemEvent.SELECTED);
				view.repaint();
			}
		});
		p2.add(borderCheckBox);

		draggableCheckBox = new JCheckBox("Draggable");
		draggableCheckBox.setSelected(textBox.isDraggable());
		draggableCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				textBox.setDraggable(e.getStateChange() == ItemEvent.SELECTED);
				view.repaint();
			}
		});
		p2.add(draggableCheckBox);

		p2.add(new JLabel("Font face:"));
		fontNameComboBox = createFontNameComboBox();
		fontNameComboBox.setSelectedItem(textBox.getFont().getFamily());
		fontNameComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textBox.setFace((String) fontNameComboBox.getSelectedItem());
				textArea.setFont(textBox.getFont());
				view.repaint();
			}
		});
		p2.add(fontNameComboBox);

		p2.add(new JLabel("Size:"));
		fontSizeComboBox = createFontSizeComboBox();
		fontSizeComboBox.setSelectedItem(new Integer(textBox.getFont().getSize()));
		fontSizeComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textBox.setSize(((Integer) fontSizeComboBox.getSelectedItem()));
				textArea.setFont(textBox.getFont());
				view.repaint();
			}
		});
		p2.add(fontSizeComboBox);

		p2.add(new JLabel("Color:"));
		fontColorComboBox = new ColorComboBox(this);
		fontColorComboBox.setColor(textBox.getColor());
		fontColorComboBox.setRenderer(new ComboBoxRenderer.ColorCell(textBox.getColor()));
		fontColorComboBox.setToolTipText("Font color");
		fontColorComboBox.setPreferredSize(new Dimension(80, fontSizeComboBox.getPreferredSize().height));
		fontColorComboBox.setAction(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (fontColorComboBox.getSelectedIndex() >= ColorRectangle.COLORS.length + 1) {
					fontColorComboBox.updateColor(new Runnable() {
						public void run() {
							textBox.setColor(fontColorComboBox.getMoreColor());
							textArea.setForeground(textBox.getColor());
						}
					});
				} else {
					textBox.setColor(fontColorComboBox.getSelectedColor());
					textArea.setForeground(textBox.getColor());
				}
				view.repaint();
			}
		});
		p2.add(fontColorComboBox);

		// bottom panel

		p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		add(p, BorderLayout.SOUTH);

		p.add(new JLabel("Note: HTML is not supported."));

		JButton button = new JButton("OK");
		button.addActionListener(okListener);
		p.add(button);

		button = new JButton("Cancel");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				restoreSettings();
				cancelled = true;
				if (dialog != null) {
					offset = dialog.getLocationOnScreen();
					dialog.dispose();
				}
			}
		});
		p.add(button);

	}

	boolean isCancelled() {
		return cancelled;
	}

	static void setOffset(Point p) {
		offset = p;
	}

	static Point getOffset() {
		return offset;
	}

	void setDialog(JDialog dialog) {
		this.dialog = dialog;
	}

	void storeSettings() {
		if (copy == null)
			copy = new TextBox(new Rectangle2D.Float());
		copy.set(textBox);
	}

	void restoreSettings() {
		if (copy == null)
			return;
		textBox.set(copy);
	}

	void handleWindowActivation(Color c) {
		textArea.setBackground(c);
		textArea.setCaretColor(new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue()));
		textArea.selectAll();
		textArea.requestFocusInWindow();
	}

	JDialog createDialog(boolean modal) {
		final JDialog dialog = new JDialog(JOptionPane.getFrameForComponent(view), "Text Box Properties", modal);
		dialog.setContentPane(this);
		setDialog(dialog);
		dialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dialog.dispose();
			}

			public void windowActivated(WindowEvent e) {
				handleWindowActivation(view.getTemperatureColor(view.getBackgroundTemperature()));
			}
		});
		if (TextBoxPanel.getOffset() == null)
			dialog.setLocationRelativeTo(view);
		else
			dialog.setLocation(TextBoxPanel.getOffset());
		dialog.pack();
		return dialog;
	}

	private static JComboBox<String> createFontNameComboBox() {
		JComboBox<String> c = new JComboBox<String>(FONT_FAMILY_NAMES);
		c.setRenderer(new ComboBoxRenderer.FontLabel());
		c.setToolTipText("Font type");
		FontMetrics fm = c.getFontMetrics(c.getFont());
		int max = 0, n = 0;
		for (int i = 0; i < FONT_FAMILY_NAMES.length; i++) {
			n = fm.stringWidth(FONT_FAMILY_NAMES[i]);
			if (max < n)
				max = n;
		}
		int w = max + 50;
		int h = fm.getHeight() + 8;
		c.setPreferredSize(new Dimension(w, h));
		c.setEditable(false);
		c.setRequestFocusEnabled(false);
		return c;
	}

	private static JComboBox<Integer> createFontSizeComboBox() {
		JComboBox<Integer> c = new JComboBox<Integer>(FONT_SIZE);
		c.setToolTipText("Font size");
		FontMetrics fm = c.getFontMetrics(c.getFont());
		int w = fm.stringWidth(FONT_SIZE[FONT_SIZE.length - 1].toString()) + 40;
		int h = fm.getHeight() + 8;
		c.setPreferredSize(new Dimension(w, h));
		c.setEditable(false);
		c.setRequestFocusEnabled(false);
		return c;
	}

	private float parse(String s) {
		return MiscUtil.parse(JOptionPane.getFrameForComponent(view), s);
	}

}
