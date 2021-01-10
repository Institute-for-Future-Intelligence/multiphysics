package org.energy2d.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Array;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import javax.swing.AbstractButton;
import javax.swing.JOptionPane;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.filechooser.FileFilter;

/**
 * @author Charles Xie
 * 
 */
public final class MiscUtil {

	private final static String FILE_SEPARATOR = System.getProperty("file.separator");

	private static final int fODD_PRIME_NUMBER = 37;

	public static int hash(int aSeed, boolean aBoolean) {
		return firstTerm(aSeed) + (aBoolean ? 1 : 0);
	}

	public static int hash(int aSeed, char aChar) {
		return firstTerm(aSeed) + aChar;
	}

	// Note that byte and short are handled by this method, through implicit conversion.
	public static int hash(int aSeed, int aInt) {
		return firstTerm(aSeed) + aInt;
	}

	public static int hash(int aSeed, long aLong) {
		return firstTerm(aSeed) + (int) (aLong ^ (aLong >>> 32));
	}

	public static int hash(int aSeed, float aFloat) {
		return hash(aSeed, Float.floatToIntBits(aFloat));
	}

	public static int hash(int aSeed, double aDouble) {
		return hash(aSeed, Double.doubleToLongBits(aDouble));
	}

	/**
	 * <code>aObject</code> is a possibly-null object field, and possibly an array. If <code>aObject</code> is an array, then each element may be a primitive or a possibly-null object.
	 */
	public static int hash(int aSeed, Object aObject) {
		int result = aSeed;
		if (aObject == null) {
			result = hash(result, 0);
		} else if (!isArray(aObject)) {
			result = hash(result, aObject.hashCode());
		} else {
			int length = Array.getLength(aObject);
			for (int idx = 0; idx < length; ++idx) {
				Object item = Array.get(aObject, idx);
				// recursive call!
				result = hash(result, item);
			}
		}
		return result;
	}

	private static int firstTerm(int aSeed) {
		return fODD_PRIME_NUMBER * aSeed;
	}

	private static boolean isArray(Object aObject) {
		return aObject.getClass().isArray();
	}

	/** return the file name of this path */
	public static String getFileName(String path) {
		if (path == null)
			return null;
		int i = path.lastIndexOf("/");
		if (i == -1)
			i = path.lastIndexOf("\\");
		if (i == -1)
			i = path.lastIndexOf(FILE_SEPARATOR);
		if (i == -1)
			return path;
		return path.substring(i + 1, path.length());
	}

	/** @return the extension of a file name in lower case */
	public static String getExtensionInLowerCase(File file) {
		if (file == null || file.isDirectory())
			return null;
		String extension = getSuffix(file.getName());
		if (extension != null)
			return extension.toLowerCase();
		return null;
	}

	/** @return the extension of a file name */
	public static String getSuffix(String filename) {
		String extension = null;
		int index = filename.lastIndexOf('.');
		if (index >= 1 && index < filename.length() - 1) {
			extension = filename.substring(index + 1);
		}
		return extension;
	}

	/**
	 * If the user does not input the extension specified by the file filter, automatically augment the file name with the specified extension.
	 */
	public static String fileNameAutoExtend(FileFilter filter, File file) {
		if (filter == null)
			return file.getAbsolutePath();
		String description = filter.getDescription().toLowerCase();
		String extension = getExtensionInLowerCase(file);
		String filename = file.getAbsolutePath();
		if (extension != null) {
			if (!filter.accept(file)) {
				filename = file.getAbsolutePath().concat(".").concat(description);
			}
		} else {
			filename = file.getAbsolutePath().concat(".").concat(description);
		}
		return filename;
	}

	/** return the parent directory of this file path. */
	public static String getParentDirectory(String path) {
		if (path == null)
			return null;
		if (path.toLowerCase().indexOf("http://") != -1 || path.toLowerCase().indexOf("https://") != -1) {
			int i = path.lastIndexOf('/');
			if (i == -1)
				return null;
			return path.substring(0, i + 1);
		}
		int i = path.lastIndexOf(FILE_SEPARATOR);
		if (i == -1)
			i = path.lastIndexOf("/");
		if (i == -1)
			return null;
		return path.substring(0, i + 1);
	}

	public static void setSelectedSilently(AbstractButton x, boolean b) {
		ActionListener[] al = x.getActionListeners();
		if (al != null && al.length > 0) {
			for (ActionListener a : al)
				x.removeActionListener(a);
		}
		ItemListener[] il = x.getItemListeners();
		if (il != null && il.length > 0) {
			for (ItemListener a : il)
				x.removeItemListener(a);
		}
		x.setSelected(b);
		if (al != null && al.length > 0) {
			for (ActionListener a : al)
				x.addActionListener(a);
		}
		if (il != null && il.length > 0) {
			for (ItemListener a : il)
				x.addItemListener(a);
		}
	}

	/** copy two-dimension arrays */
	public static void copy(float[][] dst, float[][] src) {
		for (int i = 0; i < src.length; i++)
			System.arraycopy(src[i], 0, dst[i], 0, src[i].length);
	}

	public static String formatTime(int time) {
		int seconds = time % 60;
		time /= 60;
		int minutes = time % 60;
		time /= 60;
		int hours = time % 24;
		time /= 24;
		int days = time;
		return String.format("%d:%02d:%02d:%02d", days, hours, minutes, seconds);
	}

	/**
	 * platform-independent check for Windows' equivalent of right click of mouse button. This can be used as an alternative as MouseEvent.isPopupTrigger(), which requires checking within both mousePressed() and mouseReleased() methods.
	 */
	public static boolean isRightClick(MouseEvent e) {
		if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0)
			return true;
		if (System.getProperty("os.name").startsWith("Mac") && e.isControlDown())
			return true;
		return false;
	}

	/**
	 * convert hexadecimal RGB to color --- Color.decode(String nm) does not seem to work. The passed string can start with an "#".
	 */
	public static Color convertToColor(String s) {
		if (s == null)
			throw new IllegalArgumentException("Did you mean to convert hexadecimal RGB to color?");
		if (s.length() == 7 && s.charAt(0) == '#') {
			int r = Integer.parseInt(s.substring(1, 3), 16);
			int g = Integer.parseInt(s.substring(3, 5), 16);
			int b = Integer.parseInt(s.substring(5, 7), 16);
			return new Color(r, g, b);
		} else if (s.length() == 6) {
			int r = Integer.parseInt(s.substring(0, 2), 16);
			int g = Integer.parseInt(s.substring(2, 4), 16);
			int b = Integer.parseInt(s.substring(4, 6), 16);
			return new Color(r, g, b);
		}
		throw new NumberFormatException("hex color code error");
	}

	/** get the contrast color with the same alpha value */
	public static Color getContrastColor(Color c) {
		return getContrastColor(c, c.getAlpha());
	}

	/** get the contrast color with the same alpha value */
	public static Color getContrastColor(Color c, int alpha) {
		return new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue(), alpha);
	}

	public static Color parseRGBColor(String str) {
		if (str.startsWith("0x")) {
			try {
				return new Color(Integer.valueOf(str.substring(2), 16));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		if (str.startsWith("#")) {
			try {
				return new Color(Integer.valueOf(str.substring(1), 16));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Aligns the first <code>rows</code> <code>cols</code> components of <code>parent</code> in a grid. Each component in a column is as wide as the maximum preferred width of the components in that column; height is similarly determined for each row. The parent is made just big enough to fit them all.
	 * 
	 * @param rows
	 *            number of rows
	 * @param cols
	 *            number of columns
	 * @param initialX
	 *            x location to start the grid at
	 * @param initialY
	 *            y location to start the grid at
	 * @param xPad
	 *            x padding between cells
	 * @param yPad
	 *            y padding between cells
	 */
	public static void makeCompactGrid(Container parent, int rows, int cols, int initialX, int initialY, int xPad, int yPad) {
		SpringLayout layout;
		try {
			layout = (SpringLayout) parent.getLayout();
		} catch (ClassCastException exc) {
			System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
			return;
		}

		// Align all cells in each column and make them the same width.
		Spring x = Spring.constant(initialX);
		for (int c = 0; c < cols; c++) {
			Spring width = Spring.constant(0);
			for (int r = 0; r < rows; r++) {
				width = Spring.max(width, getConstraintsForCell(r, c, parent, cols).getWidth());
			}
			for (int r = 0; r < rows; r++) {
				SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
				constraints.setX(x);
				constraints.setWidth(width);
			}
			x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
		}

		// Align all cells in each row and make them the same height.
		Spring y = Spring.constant(initialY);
		for (int r = 0; r < rows; r++) {
			Spring height = Spring.constant(0);
			for (int c = 0; c < cols; c++) {
				height = Spring.max(height, getConstraintsForCell(r, c, parent, cols).getHeight());
			}
			for (int c = 0; c < cols; c++) {
				SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
				constraints.setY(y);
				constraints.setHeight(height);
			}
			y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
		}

		// Set the parent's size.
		SpringLayout.Constraints pCons = layout.getConstraints(parent);
		pCons.setConstraint(SpringLayout.SOUTH, y);
		pCons.setConstraint(SpringLayout.EAST, x);

	}

	/* Used by makeCompactGrid. */
	private static SpringLayout.Constraints getConstraintsForCell(int r, int c, Container parent, int cols) {
		SpringLayout layout = (SpringLayout) parent.getLayout();
		Component component = parent.getComponent(r * cols + c);
		return layout.getConstraints(component);
	}

	private static Synthesizer synthesizer;

	public static void beep(int noteNumber, int velocity) {
		if (synthesizer == null) {
			try {
				synthesizer = MidiSystem.getSynthesizer();
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
				return;
			}
		}
		if (synthesizer != null) {
			try {
				synthesizer.open();
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
				return;
			}
			MidiChannel[] channels = synthesizer.getChannels();
			channels[0].noteOn(noteNumber, velocity);
		}
	}

	/** Release the system resources this class may have held. */
	public static void shutdown() {
		if (synthesizer != null) {
			synthesizer.close();
		}
	}

	/** Parse a number using the current locale */
	public static float parse(Window owner, String s) {
		float x = Float.NaN;
		try {
			x = NumberFormat.getInstance(Locale.getDefault()).parse(s).floatValue();
		} catch (ParseException e) {
			JOptionPane.showMessageDialog(owner, "Cannot parse " + e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
		return x;
	}

	/** Convert an Image into a BufferedImage */
	public static BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage)
			return (BufferedImage) img;
		BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return bi;
	}

	/** Resize a BufferedImage */
	public static BufferedImage resize(BufferedImage img, int w, int h) {
		BufferedImage newImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = newImage.createGraphics();
		g.drawImage(img.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
		g.dispose();
		return newImage;
	}

}
