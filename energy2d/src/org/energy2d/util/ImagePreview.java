package org.energy2d.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.border.LineBorder;

public class ImagePreview extends JComponent implements PropertyChangeListener {

	private ImageIcon thumbnail;
	private String path;
	private boolean lockRatio = true;

	public ImagePreview(JFileChooser fc) {
		setPreferredSize(new Dimension(200, 100));
		if (fc != null)
			fc.addPropertyChangeListener(this);
		setBorder(new LineBorder(Color.black));
	}

	public void setLockRatio(boolean b) {
		lockRatio = b;
	}

	public boolean getLockRatio() {
		return lockRatio;
	}

	public void setPath(String s) {
		path = s;
		if (path == null) {
			thumbnail = null;
		} else {
			loadImage();
		}
	}

	public void setFile(File file) {
		if (file != null) {
			path = file.getPath();
		} else {
			path = null;
		}
		if (path == null) {
			thumbnail = null;
		} else {
			loadImage();
		}
	}

	/** use the cached image if there is one */
	public void loadImage() {
		if (path == null)
			return;
		if (getWidth() <= 0 || getHeight() <= 0)
			return; // not ready yet
		ImageIcon tmpIcon = new ImageIcon(path);
		if (getLockRatio()) {
			float rx = (float) tmpIcon.getIconWidth() / (float) (getWidth() - 10);
			float ry = (float) tmpIcon.getIconHeight() / (float) (getHeight() - 10);
			if (rx > 1.f || ry > 1.f) {
				if (rx > ry) {
					thumbnail = new ImageIcon(tmpIcon.getImage().getScaledInstance(getWidth() - 10, -1, Image.SCALE_DEFAULT));
				} else {
					thumbnail = new ImageIcon(tmpIcon.getImage().getScaledInstance(-1, getHeight() - 10, Image.SCALE_DEFAULT));
				}
			} else {
				thumbnail = tmpIcon;
			}
		} else {
			thumbnail = new ImageIcon(tmpIcon.getImage().getScaledInstance(getWidth(), getHeight(), Image.SCALE_DEFAULT));
		}
	}

	public void propertyChange(PropertyChangeEvent e) {
		String name = e.getPropertyName();
		if (name.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
			File file = (File) e.getNewValue();
			if (file != null) {
				path = file.getPath();
				if (isShowing()) {
					loadImage();
					repaint();
				}
			}
		}
	}

	public void paintComponent(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		if (thumbnail == null) {
			loadImage();
		} else {
			int x = (getWidth() >> 1) - (thumbnail.getIconWidth() >> 1);
			int y = (getHeight() >> 1) - (thumbnail.getIconHeight() >> 1);
			if (y < 0) {
				y = 0;
			}
			if (x < 5) {
				x = 5;
			}
			thumbnail.paintIcon(this, g, x, y);
		}
	}

}