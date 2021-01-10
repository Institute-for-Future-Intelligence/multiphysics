package org.energy2d.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

/**
 * This is a file chooser that remembers the latest path and files.
 * 
 * @author Charles Xie
 */

public class FileChooser extends JFileChooser {

	private final static int MAX = 4;
	private List<String> recentFiles;
	private String latestPath;

	public FileChooser() {
		super();
		init();
	}

	public FileChooser(File currentDirectory) {
		super(currentDirectory);
		init();
	}

	private void init() {
		setMultiSelectionEnabled(false);
		setFileHidingEnabled(true);
		recentFiles = new ArrayList<String>();
	}

	public String getLatestPath() {
		return latestPath;
	}

	@Override
	public void setCurrentDirectory(File dir) {
		super.setCurrentDirectory(dir);
		latestPath = getCurrentDirectory().toString();
	}

	public void rememberFile(String fileName) {
		if (fileName == null)
			return;
		latestPath = getCurrentDirectory().toString();
		if (recentFiles.contains(fileName)) {
			recentFiles.remove(fileName);
		} else {
			if (recentFiles.size() >= MAX)
				recentFiles.remove(0);
		}
		recentFiles.add(fileName);
	}

	public void addRecentFile(String fileName) {
		recentFiles.add(fileName);
	}

	public String[] getRecentFiles() {
		int n = recentFiles.size();
		if (n == 0)
			return new String[] {};
		String[] s = new String[n];
		for (int i = 0; i < n; i++) {
			s[n - 1 - i] = recentFiles.get(i);
		}
		return s;
	}

}
