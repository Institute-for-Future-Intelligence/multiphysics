package org.energy2d.system;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JOptionPane;

/**
 * @author Charles Xie
 * 
 */
final class UpdateAnnouncer {

	private final static String HOME = "http://energy.concord.org/energy2d/";

	static void showMessage(System2D box) {
		String s = getJarLocation();
		if (!s.endsWith(".jar"))
			return;
		if (new File(s).lastModified() >= checkTimeStamp(HOME + "energy2d.jar"))
			return;
		String msg = "An update is available. Do you want to download it now?";
		if (JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(box.view), msg, "Update Energy2D", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			Helper.openBrowser(HOME + "update.html");
			System.exit(0);
		}
	}

	private static long checkTimeStamp(String s) {
		URLConnection connection = null;
		try {
			connection = new URL(s).openConnection();
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(5000);
		if (!(connection instanceof HttpURLConnection))
			return -1;
		HttpURLConnection c = (HttpURLConnection) connection;
		try {
			c.setRequestMethod("HEAD");
		} catch (ProtocolException e) {
			e.printStackTrace();
			return -1;
		}
		long t = c.getLastModified();
		c.disconnect();
		return t;
	}

	private static String getJarLocation() {
		String jarLocation = System.getProperty("java.class.path");
		if (System.getProperty("os.name").startsWith("Mac"))
			jarLocation = validateJarLocationOnMacOSX(jarLocation);
		return jarLocation;
	}

	/*
	 * Mac OS X's Java 1.5.0_06 implementation returns things like : /Users/user/energy2d.jar:/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Classes/.compatibility/14compatibility.jar
	 */
	private static String validateJarLocationOnMacOSX(String jarLocation) {
		int i = jarLocation.indexOf(".jar:/");
		if (i != -1)
			jarLocation = jarLocation.substring(0, i) + ".jar";
		return jarLocation;
	}

}