package org.energy2d.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;

/**
 * @author Charles Xie
 * 
 */

public class ScreenshotSaver extends AbstractAction {

	private JComponent component;
	private boolean borderless;
	private Border savedBorder;
	private ImagePreview imagePreview;
	private FileChooser fileChooser;

	private FileFilter pngFilter = new FileFilter() {

		public boolean accept(File file) {
			if (file == null)
				return false;
			if (file.isDirectory())
				return true;
			String filename = file.getName();
			int index = filename.lastIndexOf('.');
			if (index == -1)
				return false;
			String postfix = filename.substring(index + 1);
			if ("png".equalsIgnoreCase(postfix))
				return true;
			return false;
		}

		@Override
		public String getDescription() {
			return "PNG";
		}

	};

	/*
	 * @param c the component to be output @param noframe true if no frame is needed for the output image. If false, a black line frame will be added to the output image.
	 */
	public ScreenshotSaver(JComponent c, boolean noframe) {
		super();
		component = c;
		borderless = noframe;
		fileChooser = new FileChooser();
		imagePreview = new ImagePreview(fileChooser);
		putValue(NAME, "Save As Image...");
		putValue(SHORT_DESCRIPTION, "Save a screenshot of the current view");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_M));
		putValue(ACCELERATOR_KEY, System.getProperty("os.name").startsWith("Mac") ? KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.META_MASK, true) : KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_MASK, true));
	}

	public void destroy() {
		if (imagePreview != null && fileChooser != null)
			fileChooser.removePropertyChangeListener(imagePreview);
		component = null;
		fileChooser = null;
	}

	public void setCurrentDirectory(File file) {
		fileChooser.setCurrentDirectory(file);
	}

	public String getLatestPath() {
		return fileChooser.getLatestPath();
	}

	public void setFrame(boolean b) {
		borderless = !b;
	}

	public boolean getFrame() {
		return !borderless;
	}

	public void actionPerformed(ActionEvent e) {

		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(pngFilter);
		fileChooser.setFileFilter(pngFilter);
		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		fileChooser.setDialogTitle("Save image");
		fileChooser.setApproveButtonMnemonic('S');
		String latestPath = fileChooser.getLatestPath();
		if (latestPath != null)
			fileChooser.setCurrentDirectory(new File(latestPath));
		fileChooser.setAccessory(imagePreview);

		if (fileChooser.showSaveDialog(JOptionPane.getFrameForComponent(component)) == JFileChooser.APPROVE_OPTION) {
			final File file = fileChooser.getSelectedFile();
			String filename = MiscUtil.fileNameAutoExtend(fileChooser.getFileFilter(), file);
			final File temp = new File(filename);
			if (temp.exists()) {
				if (JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(component), "File " + temp.getName() + " exists, overwrite?", "File exists", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
					fileChooser.resetChoosableFileFilters();
					return;
				}
			}
			if (!borderless) {
				savedBorder = component.getBorder();
				component.setBorder(BorderFactory.createLineBorder(Color.black));
			}
			write(temp.getPath());
			if (savedBorder != null) {
				component.setBorder(savedBorder);
				savedBorder = null;
			}
			fileChooser.rememberFile(file.getPath());
		}
	}

	/**
	 * export the component to an image file.
	 * 
	 * @param name
	 *            the name of the output image file
	 */
	protected void write(String name) {
		Dimension size = component.getSize();
		BufferedImage bufferedImage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = bufferedImage.createGraphics();
		component.paint(g2);
		OutputStream out = null;
		try {
			out = new FileOutputStream(name);
			ImageIO.write(bufferedImage, MiscUtil.getSuffix(name), out);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException iox) {
				}
			}
		}
	}

}