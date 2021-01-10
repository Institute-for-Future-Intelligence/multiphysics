package org.energy2d.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.SystemColor;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.ListCellRenderer;

/**
 * @author Charles Xie
 * 
 */

public abstract class ComboBoxRenderer {

	public static class IconRenderer extends JRadioButton implements ListCellRenderer<Object> {

		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			setBackground(isSelected ? SystemColor.textHighlight : Color.white);
			setForeground(isSelected ? SystemColor.textHighlightText : Color.black);
			setHorizontalAlignment(CENTER);
			if (value instanceof Icon) {
				setIcon((Icon) value);
				if (value instanceof ImageIcon) {
					setToolTipText(((ImageIcon) value).getDescription());
				}
			} else {
				setText(value == null ? "Unknown" : value.toString());
			}
			return this;
		}

	}

	public static class ColorCell extends ColorRectangle implements ListCellRenderer<Object> {

		public ColorCell() {
			super();
		}

		public ColorCell(Color moreColor) {
			this();
			if (!isDefaultColor(moreColor))
				setMoreColor(moreColor);
		}

		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			setBackground(isSelected ? SystemColor.textHighlight : Color.white);
			setForeground(isSelected ? SystemColor.textHighlightText : Color.black);
			setColorID((Integer) value);
			return this;
		}

	}

	public static class FontLabel extends JLabel implements ListCellRenderer<Object> {

		public FontLabel() {
			setOpaque(true);
		}

		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			String s = (String) value;
			setText(s);
			setFont(new Font(s, Font.PLAIN, 12));
			return this;
		}

	}

}