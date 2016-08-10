package org.concord.energy2d.system;

import java.io.IOException;

import com.threerings.getdown.launcher.Getdown;
import com.threerings.getdown.launcher.GetdownApp;

public class Updater {

	public static void download() {
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

}
