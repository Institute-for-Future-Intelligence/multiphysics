package org.energy2d.system;

import java.awt.EventQueue;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.JOptionPane;

import com.threerings.getdown.launcher.Getdown;
import com.threerings.getdown.launcher.GetdownApp;

public class Updater {

	private static boolean firstTime = true;
	private static boolean restartRequested;

	public static void download(final System2D box) {
		if (System2D.launchedByJWS)
			return;
		if ("true".equalsIgnoreCase(System.getProperty("NoUpdate")))
			return;
		new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
				System.setProperty("direct", "true");
				System.setProperty("no_install", "true");
				System.setProperty("silent", "true");
				GetdownApp.main(new String[] { "." });
				for (int i = 0; i < 60; i++) {
					if (!firstTime) {
						break;
					}
					try {
						Thread.sleep(1000);
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
					if (Getdown.isUpdateAvailable()) {
						if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(box.owner, "A new update is available. Would you like to install it and restart now?", "Update", JOptionPane.YES_NO_OPTION)) {
							restartRequested = true;
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									Action a = box.view.getActionMap().get("Quit");
									if (a != null)
										a.actionPerformed(null);
								}
							});
						} else {
							firstTime = false;
						}
					}
				}
			};
		}.start();
	}

	public static void install() {
		if (System2D.launchedByJWS)
			return;
		if ("true".equalsIgnoreCase(System.getProperty("NoUpdate")))
			return;
		try {
			Getdown.install();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static boolean isRestartRequested() {
		return restartRequested;
	}

}
