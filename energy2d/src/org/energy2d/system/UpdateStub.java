package org.energy2d.system;

import com.threerings.getdown.launcher.GetdownApp;

public class UpdateStub {

	public static boolean receivedCall = false;

	public static void main(final String[] args) {
		receivedCall = true;
	}

	static void update() {
		try {
			Class.forName("com.threerings.getdown.launcher.GetdownApp");
		} catch (ClassNotFoundException e) {
			return;
		}
		new Thread() {
			@Override
			public void run() {
				System.setProperty("direct", "true");
				GetdownApp.main(new String[] { "." });
				while (!UpdateStub.receivedCall)
					Thread.yield();
				UpdateStub.receivedCall = false;
				System.exit(0);
			};
		}.start();
	}

}
