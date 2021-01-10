package com.apple.eawt;

import java.awt.Point;

/** skeleton methods to be compatible to Mac OS X */

public class Application {

	private static Point zeroPoint = new Point();

	public void addApplicationListener(ApplicationListener listener) {
	}

	public boolean getEnabledPreferencesMenu() {
		return false;
	}

	public static Point getMouseLocationOnScreen() {
		return zeroPoint;
	}

	public void removeApplicationListener(ApplicationListener listener) {
	}

	public void setEnabledAboutMenu(boolean enable) {
	}

	public void setEnabledPreferencesMenu(boolean enable) {
	}

}