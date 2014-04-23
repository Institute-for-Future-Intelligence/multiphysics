/*
 *   Copyright (C) 2010  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.system;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileFilter;

import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.util.FileChooser;
import org.concord.energy2d.util.MiscUtil;
import org.concord.energy2d.util.ScreenshotSaver;

/**
 * @author Charles Xie
 * 
 */
class MenuBar extends JMenuBar {

	private final static boolean IS_MAC = System.getProperty("os.name").startsWith("Mac");

	FileChooser e2dFileChooser;
	private FileChooser htmFileChooser;

	private FileFilter e2dFilter = new FileFilter() {

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
			if ("e2d".equalsIgnoreCase(postfix))
				return true;
			return false;
		}

		@Override
		public String getDescription() {
			return "Energy2D";
		}

	};

	private FileFilter htmFilter = new FileFilter() {

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
			if ("htm".equalsIgnoreCase(postfix))
				return true;
			return false;
		}

		@Override
		public String getDescription() {
			return "HTML";
		}

	};

	private Action openAction;
	private Action saveAction;
	private Action saveAsAction;
	private Action saveAsAppletAction;
	private Action exitAction;
	private ScreenshotSaver screenshotSaver;
	private int fileMenuItemCount;
	private List<JComponent> recentFileMenuItems;

	MenuBar(final System2D box, final JFrame frame) {

		e2dFileChooser = new FileChooser();
		htmFileChooser = new FileChooser();
		recentFileMenuItems = new ArrayList<JComponent>();

		// file menu

		final JMenu fileMenu = new JMenu("File");
		fileMenu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent e) {
				if (!recentFileMenuItems.isEmpty()) {
					for (JComponent x : recentFileMenuItems)
						fileMenu.remove(x);
				}
				String[] recentFiles = getRecentFiles();
				if (recentFiles != null) {
					int n = recentFiles.length;
					if (n > 0) {
						for (int i = 0; i < n; i++) {
							JMenuItem x = new JMenuItem((i + 1) + "  " + MiscUtil.getFileName(recentFiles[i]));
							x.setToolTipText(recentFiles[i]);
							final File rf = new File(recentFiles[i]);
							x.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									box.stop();
									if (!box.askSaveBeforeLoading())
										return;
									box.loadFile(rf);
									e2dFileChooser.rememberFile(rf.getPath());
								}
							});
							fileMenu.insert(x, fileMenuItemCount + i);
							recentFileMenuItems.add(x);
						}
						JSeparator s = new JSeparator();
						fileMenu.add(s, fileMenuItemCount + n);
						recentFileMenuItems.add(s);
					}
				}
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});
		add(fileMenu);

		JMenuItem mi = new JMenuItem("New");
		mi.setToolTipText("Create a new simulation");
		KeyStroke ks = IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK);
		mi.setAccelerator(ks);
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!box.askSaveBeforeLoading())
					return;
				box.model.clear();
				box.model.refreshMaterialPropertyArrays();
				box.model.refreshPowerArray();
				box.model.refreshTemperatureBoundaryArray();
				box.view.clear();
				box.setCurrentFile(null);
				box.setCurrentModel(null);
				box.setFrameTitle();
				XmlDecoder x = new XmlDecoder(box);
				x.startDocument();
				x.endDocument();
				box.view.setGridOn(true);
				box.view.setRulerOn(true);
				box.view.repaint();
				box.notifyToolBarListener(new ToolBarEvent(ToolBarEvent.NEW_FILE, MenuBar.this));
				box.setSaved(true);
			}
		});
		fileMenu.add(mi);
		fileMenuItemCount++;

		openAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				box.stop();
				if (!box.askSaveBeforeLoading())
					return;
				e2dFileChooser.setAcceptAllFileFilterUsed(false);
				e2dFileChooser.addChoosableFileFilter(e2dFilter);
				e2dFileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
				e2dFileChooser.setDialogTitle("Open");
				e2dFileChooser.setApproveButtonMnemonic('O');
				e2dFileChooser.setAccessory(null);
				if (e2dFileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					File file = e2dFileChooser.getSelectedFile();
					if (!file.toString().endsWith(".e2d")) {
						file = new File(file.toString() + ".e2d");
					}
					if (file.exists()) {
						box.loadFile(file);
					} else {
						JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(box), "File " + file + " was not found.", "File not found", JOptionPane.ERROR_MESSAGE);
					}
					e2dFileChooser.rememberFile(file.getPath());
				}
				e2dFileChooser.resetChoosableFileFilters();
			}
		};
		openAction.putValue(Action.SHORT_DESCRIPTION, "Open a simulation");
		ks = IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK);
		box.view.getInputMap().put(ks, "Open");
		box.view.getActionMap().put("Open", openAction);
		mi = new JMenuItem("Open...");
		mi.setAccelerator(ks);
		mi.setToolTipText((String) openAction.getValue(Action.SHORT_DESCRIPTION));
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openAction.actionPerformed(e);
			}
		});
		fileMenu.add(mi);
		fileMenuItemCount++;

		saveAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (box.getCurrentFile() == null) {
					saveAs(box, frame);
				} else {
					save(box);
				}
			}
		};
		saveAction.putValue(Action.SHORT_DESCRIPTION, "Save a simulation");
		ks = IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK);
		box.view.getInputMap().put(ks, "Save");
		box.view.getActionMap().put("Save", saveAction);
		mi = new JMenuItem("Save");
		mi.setAccelerator(ks);
		mi.setToolTipText((String) saveAction.getValue(Action.SHORT_DESCRIPTION));
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAction.actionPerformed(e);
			}
		});
		fileMenu.add(mi);
		fileMenuItemCount++;

		saveAsAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				saveAs(box, frame);
			}
		};
		saveAsAction.putValue(Action.SHORT_DESCRIPTION, "Save a simulation as");
		ks = IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK);
		box.view.getInputMap().put(ks, "SaveAs");
		box.view.getActionMap().put("SaveAs", saveAsAction);
		mi = new JMenuItem("Save As...");
		mi.setAccelerator(ks);
		mi.setToolTipText((String) saveAsAction.getValue(Action.SHORT_DESCRIPTION));
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAsAction.actionPerformed(e);
			}
		});
		fileMenu.add(mi);
		fileMenuItemCount++;

		screenshotSaver = new ScreenshotSaver(box.view, true);
		fileMenu.add(screenshotSaver);
		fileMenuItemCount++;

		saveAsAppletAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (box.getCurrentFile() == null) {
					JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(box.view), "Sorry, you have to save the current model as a local file in order to create an applet for it.", "Applet not allowed", JOptionPane.ERROR_MESSAGE);
					return;
				}
				saveAsApplet(box, frame);
			}
		};
		saveAsAppletAction.putValue(Action.SHORT_DESCRIPTION, "Export this simulation as an applet");
		mi = new JMenuItem("Save As Applet...");
		mi.setToolTipText((String) saveAsAppletAction.getValue(Action.SHORT_DESCRIPTION));
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAsAppletAction.actionPerformed(e);
			}
		});
		fileMenu.add(mi);
		fileMenuItemCount++;

		fileMenu.addSeparator();
		fileMenuItemCount++;

		final Action propertyAction = box.view.getActionMap().get("Property");
		mi = new JMenuItem("Properties...");
		mi.setAccelerator((KeyStroke) propertyAction.getValue(Action.ACCELERATOR_KEY));
		mi.setToolTipText((String) propertyAction.getValue(Action.SHORT_DESCRIPTION));
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				propertyAction.actionPerformed(e);
			}
		});
		fileMenu.add(mi);
		fileMenuItemCount++;

		final Action scriptAction = box.view.getActionMap().get("Script");
		mi = new JMenuItem("Script Console...");
		mi.setAccelerator((KeyStroke) scriptAction.getValue(Action.ACCELERATOR_KEY));
		mi.setToolTipText((String) scriptAction.getValue(Action.SHORT_DESCRIPTION));
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scriptAction.actionPerformed(e);
			}
		});
		fileMenu.add(mi);
		fileMenuItemCount++;

		final Action taskAction = box.view.getActionMap().get("Task_Manager");
		mi = new JMenuItem("Task Manager...");
		mi.setAccelerator((KeyStroke) taskAction.getValue(Action.ACCELERATOR_KEY));
		mi.setToolTipText((String) taskAction.getValue(Action.SHORT_DESCRIPTION));
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				taskAction.actionPerformed(e);
			}
		});
		fileMenu.add(mi);
		fileMenuItemCount++;

		if (!IS_MAC) {
			fileMenu.addSeparator();
			fileMenuItemCount++;
		}

		exitAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				switch (box.askSaveOption()) {
				case JOptionPane.YES_OPTION:
					Action a = null;
					if (box.getCurrentFile() != null) {
						a = box.view.getActionMap().get("Save");
					} else {
						a = box.view.getActionMap().get("SaveAs");
					}
					if (a != null)
						a.actionPerformed(null);
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							box.shutdown();
							System.exit(0);
						}
					});
					break;
				case JOptionPane.NO_OPTION:
					box.shutdown();
					System.exit(0);
					break;
				case JOptionPane.CANCEL_OPTION:
					// do nothing
					break;
				}
			}
		};
		exitAction.putValue(Action.SHORT_DESCRIPTION, "Close " + System2D.BRAND_NAME);
		ks = IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK);
		box.view.getInputMap().put(ks, "Quit");
		box.view.getActionMap().put("Quit", exitAction);
		if (!IS_MAC) {
			mi = new JMenuItem("Exit");
			mi.setAccelerator(ks);
			mi.setToolTipText((String) exitAction.getValue(Action.SHORT_DESCRIPTION));
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					exitAction.actionPerformed(e);
				}
			});
			fileMenu.add(mi);
		}

		// insert menu

		JMenu menu = new JMenu("Insert");
		add(menu);

		menu.add(box.view.getActionMap().get("Insert Thermometer"));
		menu.add(box.view.getActionMap().get("Insert Heat Flux Sensor"));
		menu.add(box.view.getActionMap().get("Insert Anemometer"));
		menu.addSeparator();
		menu.add(box.view.getActionMap().get("Insert Cloud"));
		menu.add(box.view.getActionMap().get("Insert Tree"));
		menu.addSeparator();
		menu.add(box.view.getActionMap().get("Insert Text Box"));

		// edit menu

		menu = new JMenu("Edit");
		menu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent e) {
				Object src = e.getSource();
				if (src instanceof JPopupMenu) {
					JPopupMenu m = (JPopupMenu) src;
					int n = m.getComponentCount();
					Component c;
					for (int i = 0; i < n; i++) {
						c = m.getComponent(i);
						if (c instanceof JMenuItem) {
							JMenuItem mi = (JMenuItem) c;
							if (mi.getText().equals("Cut") || mi.getText().equals("Copy")) {
								mi.setEnabled(box.view.getSelectedManipulable() != null);
							} else if (mi.getText().equals("Paste")) {
								mi.setEnabled(box.view.getBufferedManipulable() != null);
							}
						}
					}
				}
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});
		add(menu);

		menu.add(box.view.getActionMap().get("Cut"));
		menu.add(box.view.getActionMap().get("Copy"));
		menu.add(box.view.getActionMap().get("Paste"));
		menu.addSeparator();

		mi = new JMenuItem("Clear All");
		mi.setToolTipText("Remove all the elements");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(box.view), "Are you sure you want to remove all objects?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					box.model.clear();
					box.model.refreshMaterialPropertyArrays();
					box.model.refreshPowerArray();
					box.model.refreshTemperatureBoundaryArray();
					box.view.clear();
					box.view.repaint();
				}
			}
		});
		menu.add(mi);

		mi = new JMenuItem("Translate All");
		mi.setToolTipText("Translate all the model elements");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(box.view), "<html>Hold down the ALT key and use the arrow keys to move around.<br>To move more slowly, also hold down the " + (IS_MAC ? "Command" : "CTRL") + " key.", "Translate all elements", JOptionPane.INFORMATION_MESSAGE);
				box.view.requestFocusInWindow();
			}
		});
		menu.add(mi);

		mi = new JMenuItem("Scale All");
		mi.setToolTipText("Scale all the model elements");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = JOptionPane.showInputDialog(JOptionPane.getFrameForComponent(box.view), "Type a scale factor (must be a positive number):", "Scale All", JOptionPane.QUESTION_MESSAGE);
				if (s == null || s.trim().equals("")) {
					JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(box.view), "Please type a positive number.", "Scale factor error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				float x = parse(s);
				if (Float.isNaN(x))
					return;
				if (x <= 0) {
					JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(box.view), "Scale factor must be a positive number.", "Scale factor error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (box.model.scaleAll(x)) {
					JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(box.view), "Your scale factor is too large -- some objects are out of the window.", "Warning", JOptionPane.WARNING_MESSAGE);
				}
				if (box.model.getPartCount() > 0) {
					box.model.refreshMaterialPropertyArrays();
					box.model.refreshPowerArray();
					box.model.refreshTemperatureBoundaryArray();
				}
				box.view.setSelectedManipulable(box.view.getSelectedManipulable());
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.RESIZE);
			}
		});
		menu.add(mi);
		menu.addSeparator();

		mi = new JMenuItem("Links");
		mi.setToolTipText("Set links to previous and next simulations");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LinksDialog d = new LinksDialog(box, true);
				d.setVisible(true);
			}
		});
		menu.add(mi);
		menu.addSeparator();

		mi = new JMenuItem("Preferences");
		mi.setToolTipText("Set preferences");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PreferencesDialog d = new PreferencesDialog(box, true);
				d.setVisible(true);
			}
		});
		menu.add(mi);

		// view menu

		final JCheckBoxMenuItem miSeeThrough = new JCheckBoxMenuItem("See-Through");
		final JCheckBoxMenuItem miIsotherm = new JCheckBoxMenuItem("Isotherm");
		final JCheckBoxMenuItem miVelocity = new JCheckBoxMenuItem("Velocity");
		final JCheckBoxMenuItem miStreamline = new JCheckBoxMenuItem("Streamlines");
		final JCheckBoxMenuItem miHeatFluxArrow = new JCheckBoxMenuItem("Heat Flux Arrows");
		final JCheckBoxMenuItem miHeatFluxLine = new JCheckBoxMenuItem("Heat Flux Lines");
		final JCheckBoxMenuItem miColorPalette = new JCheckBoxMenuItem("Color Palette");
		final JCheckBoxMenuItem miRuler = new JCheckBoxMenuItem("Ruler");
		final JCheckBoxMenuItem miGrid = new JCheckBoxMenuItem("Grid");
		final JMenuItem miIncrGrid = new JMenuItem("Increase Grid Lines");
		final JMenuItem miDecrGrid = new JMenuItem("Decrease Grid Lines");
		final JCheckBoxMenuItem miControlPanel = new JCheckBoxMenuItem("Control Panel");

		menu = new JMenu("View");
		menu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent e) {
				MiscUtil.setSelectedSilently(miSeeThrough, box.view.getSeeThrough());
				MiscUtil.setSelectedSilently(miIsotherm, box.view.isIsothermOn());
				MiscUtil.setSelectedSilently(miVelocity, box.view.isVelocityOn());
				MiscUtil.setSelectedSilently(miStreamline, box.view.isStreamlineOn());
				MiscUtil.setSelectedSilently(miHeatFluxArrow, box.view.isHeatFluxArrowsOn());
				MiscUtil.setSelectedSilently(miHeatFluxLine, box.view.isHeatFluxLinesOn());
				MiscUtil.setSelectedSilently(miColorPalette, box.view.isColorPaletteOn());
				MiscUtil.setSelectedSilently(miRuler, box.view.isRulerOn());
				MiscUtil.setSelectedSilently(miGrid, box.view.isGridOn());
				miIncrGrid.setEnabled(box.view.isGridOn());
				miDecrGrid.setEnabled(box.view.isGridOn());
				MiscUtil.setSelectedSilently(miControlPanel, box.view.isControlPanelVisible());
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});
		add(menu);

		miSeeThrough.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view.setSeeThrough(src.isSelected());
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		miSeeThrough.setToolTipText("Check if you wish to make all parts transparent to see heat flows inside them");
		menu.add(miSeeThrough);

		miIsotherm.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view.setIsothermOn(src.isSelected());
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		miIsotherm.setToolTipText("Check if you wish to show isotherm lines");
		menu.add(miIsotherm);

		miHeatFluxLine.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view.setHeatFluxLinesOn(src.isSelected());
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		miHeatFluxLine.setToolTipText("Check if you wish to show heat flux lines");
		menu.add(miHeatFluxLine);

		miHeatFluxArrow.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view.setHeatFluxArrowsOn(src.isSelected());
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		miHeatFluxArrow.setToolTipText("Check if you wish to show heat flux arrows");
		menu.add(miHeatFluxArrow);

		miVelocity.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view.setVelocityOn(src.isSelected());
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		miVelocity.setToolTipText("Check if you wish to show velocity vectors");
		menu.add(miVelocity);

		miStreamline.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view.setStreamlineOn(src.isSelected());
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		miStreamline.setToolTipText("Check if you wish to show streamlines");
		menu.add(miStreamline);

		final JRadioButtonMenuItem miMouseDefafult = new JRadioButtonMenuItem("Default");
		final JRadioButtonMenuItem miMouseTemperature = new JRadioButtonMenuItem("Temperature");
		final JRadioButtonMenuItem miMouseEnergy = new JRadioButtonMenuItem("Thermal Energy");
		final JRadioButtonMenuItem miMouseVelocity = new JRadioButtonMenuItem("Velocity");
		final JRadioButtonMenuItem miMouseHeatFlux = new JRadioButtonMenuItem("Heat Flux");
		final JRadioButtonMenuItem miMouseCoordinates = new JRadioButtonMenuItem("Coordinates");
		ButtonGroup bg = new ButtonGroup();
		bg.add(miMouseDefafult);
		bg.add(miMouseTemperature);
		bg.add(miMouseEnergy);
		bg.add(miMouseVelocity);
		bg.add(miMouseHeatFlux);
		bg.add(miMouseCoordinates);

		final JMenu mouseMenu = new JMenu("Mouse Reading");
		mouseMenu.setToolTipText("Select a property the value of which at a mouse position will be shown when it moves");
		mouseMenu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent e) {
				JRadioButtonMenuItem x = (JRadioButtonMenuItem) mouseMenu.getMenuComponent(box.view.getMouseReadType());
				MiscUtil.setSelectedSilently(x, true);
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});
		menu.add(mouseMenu);
		menu.addSeparator();

		miMouseDefafult.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					box.view.setMouseReadType((byte) 0);
				}
			}
		});
		mouseMenu.add(miMouseDefafult);

		miMouseTemperature.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					box.view.setMouseReadType((byte) 1);
				}
			}
		});
		mouseMenu.add(miMouseTemperature);

		miMouseEnergy.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					box.view.setMouseReadType((byte) 2);
				}
			}
		});
		mouseMenu.add(miMouseEnergy);

		miMouseVelocity.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					box.view.setMouseReadType((byte) 3);
				}
			}
		});
		mouseMenu.add(miMouseVelocity);

		miMouseHeatFlux.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					box.view.setMouseReadType((byte) 4);
				}
			}
		});
		mouseMenu.add(miMouseHeatFlux);

		miMouseCoordinates.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					box.view.setMouseReadType((byte) 5);
				}
			}
		});
		mouseMenu.add(miMouseCoordinates);

		miColorPalette.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view.setColorPaletteOn(src.isSelected());
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		miColorPalette.setToolTipText("Check if you wish to show the Color Palette");
		menu.add(miColorPalette);

		miControlPanel.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view.setControlPanelVisible(src.isSelected());
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		miControlPanel.setToolTipText("Check if you wish to show the built-in control panel");
		menu.add(miControlPanel);

		menu.addSeparator();

		miRuler.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view.setRulerOn(src.isSelected());
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		miRuler.setToolTipText("Check if you wish to show the Ruler");
		menu.add(miRuler);

		miGrid.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view.setGridOn(src.isSelected());
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		miGrid.setToolTipText("Check if you wish to show grid lines");
		menu.add(miGrid);

		ks = KeyStroke.getKeyStroke('[', KeyEvent.ALT_MASK);
		miIncrGrid.setAccelerator(ks);
		miIncrGrid.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int gridSize = box.view.getGridSize();
				if (gridSize > 2)
					box.view.setGridSize(--gridSize);
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		miIncrGrid.setToolTipText("Increase grid lines");
		menu.add(miIncrGrid);

		ks = KeyStroke.getKeyStroke(']', KeyEvent.ALT_MASK);
		miDecrGrid.setAccelerator(ks);
		miDecrGrid.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int gridSize = box.view.getGridSize();
				if (gridSize < 25)
					box.view.setGridSize(++gridSize);
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		miDecrGrid.setToolTipText("Decrease grid lines");
		menu.add(miDecrGrid);

		menu.addSeparator();

		mi = new JMenuItem("More...");
		mi.setToolTipText("Open the View Options");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				box.view.createDialog(box.view, false);
			}
		});
		menu.add(mi);

		// template menu

		menu = new JMenu("Examples");
		add(menu);

		JMenu subMenu = new JMenu("Heat and Temperature");
		menu.add(subMenu);

		LinkedHashMap<String, String> examples = new LinkedHashMap<String, String>();
		examples.put("Thermal Equilibrium Between Identical Objects", "models/identical-heat-capacity.e2d");
		examples.put("Thermal Equilibrium Between Objects with Different Specific Heats", "models/different-specific-heat1.e2d");
		examples.put("Thermal Equilibrium Between Objects with Different Densities", "models/different-density1.e2d");
		examples.put("The Effect of Thermal Conductivity on Equilibration Speed", "models/different-conductivity.e2d");
		examples.put("The Zeroth Law of Thermodynamics", "models/zeroth.e2d");
		examples.put("The First Law of Thermodynamics", "models/conservation-of-energy.e2d");
		createMenu(box, subMenu, examples);

		subMenu = new JMenu("Conduction");
		menu.add(subMenu);

		examples.put("Comparing Thermal Conductivities", "models/conduction1.e2d");
		examples.put("Comparing Conduction Areas", "models/conduction2.e2d");
		examples.put("Comparing Temperature Differences", "models/conduction3.e2d");
		examples.put("Comparing Conducting Distances", "models/conduction4.e2d");
		examples.put("Comparing Specific Heats", "models/conduction5.e2d");
		examples.put("The Series Circuit Analogy", "models/series-circuit-analogy.e2d");
		examples.put("The Parallel Circuit Analogy", "models/parallel-circuit-analogy.e2d");
		examples.put("Why We Feel Hot or Cold When Touching Something", "models/hand.e2d");
		examples.put("Wood Spoon vs. Metal Spoon", "models/spoon.e2d");
		createMenu(box, subMenu, examples);

		subMenu = new JMenu("Convection");
		menu.add(subMenu);

		examples.put("Natural Convection", "models/natural-convection.e2d");
		examples.put("Natural Convection with Different Temperatures", "models/natural-convection-temperature.e2d");
		examples.put("Comparing Natural Convection and Conduction", "models/compare-convection-conduction.e2d");
		examples.put("Comparing Forced Convection and Conduction", "models/forced-convection.e2d");
		examples.put("Exploring Forced Convection", "models/forced-convection1.e2d");
		examples.put("Stack Effect", "models/stack-effect.e2d");
		createMenu(box, subMenu, examples);

		subMenu = new JMenu("Radiation");
		menu.add(subMenu);

		mi = new JMenuItem("Radiation Heat and Temperature");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				box.loadModel("models/temperature-radiation.e2d");
			}
		});
		subMenu.add(mi);

		subMenu = new JMenu("Fluid Dynamics");
		menu.add(subMenu);

		examples.put("Bénard Cell", "models/benard-cell.e2d");
		examples.put("Lid-Driven Cavity", "models/lid-driven-cavity.e2d");
		examples.put("Smoke in Wind", "models/smoke-in-wind.e2d");
		examples.put("Laminar/Turbulent Flow", "models/reynolds.e2d");
		examples.put("Von Kármán Vortex Street", "models/vortex-street.e2d");
		createMenu(box, subMenu, examples);

		subMenu = new JMenu("Building Energy Analysis");
		menu.add(subMenu);

		examples.put("Thermal Bridge", "models/thermal-bridge.e2d");
		examples.put("Thermal Ghosting", "models/ghosting.e2d");
		examples.put("Fireplace", "models/fireplace-on.e2d");
		examples.put("Internal Heating", "models/internal-heater.e2d");
		examples.put("Thermostats", "models/thermostat2.e2d");
		examples.put("Infiltration", "models/infiltration.e2d");
		examples.put("Wind Effect", "models/wind-effect.e2d");
		examples.put("Solar Heating: Gable Roof", "models/solar-heating-gable-roof.e2d");
		examples.put("Solar Heating: Shed Roof", "models/solar-heating-skillion-roof.e2d");
		examples.put("Solar Heating: Two Stories", "models/solar-heating-two-story.e2d");
		examples.put("Solar Heating: Convection", "models/solar-heating-convection.e2d");
		examples.put("Solar Heating: Thermostat", "models/thermostat1.e2d");
		createMenu(box, subMenu, examples);

		subMenu = new JMenu("Industrial Applications");
		menu.add(subMenu);

		examples.put("Solar Oven", "models/solar-oven.e2d");
		examples.put("Solar Chimney", "models/solar-chimney.e2d");
		examples.put("Trombe Wall", "models/trombe-wall-closeup.e2d");
		examples.put("Solar Updraft Tower", "models/solar-updraft-tower.e2d");
		examples.put("Heat Fins", "models/fin1.e2d");
		examples.put("Coaxial Cable", "models/cable.e2d");
		createMenu(box, subMenu, examples);

		subMenu = new JMenu("Earth Science");
		menu.add(subMenu);

		examples.put("Solar Cycles", "models/solar-cycles-shadow.e2d");
		examples.put("Sun & Clouds", "models/solar-cycles.e2d");
		examples.put("USA Isotherm", "models/usa-isotherm.e2d");
		examples.put("Mantle Convection", "models/mantle.e2d");
		createMenu(box, subMenu, examples);

		subMenu = new JMenu("Boundary Conditions");
		menu.add(subMenu);

		examples.put("Fixed Temperature Boundary", "models/fixed-temperature-boundary.e2d");
		examples.put("Fixed Heat Flux Boundary", "models/fixed-flux-boundary.e2d");
		createMenu(box, subMenu, examples);

		subMenu = new JMenu("Miscellaneous");
		menu.add(subMenu);

		examples.put("Ray Optics", "models/ray-optics.e2d");
		examples.put("Natural Daylighting", "models/natural-daylighting.e2d");
		examples.put("Thermoregulation", "models/thermoregulation.e2d");
		createMenu(box, subMenu, examples);

		menu.addSeparator();
		mi = new JMenuItem("More...");
		mi.setToolTipText("Open the Online Model Repository");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Helper.openBrowser("http://energy.concord.org/energy2d/models.html");
			}
		});
		menu.add(mi);

		// help menu

		menu = new JMenu("Help");
		add(menu);

		mi = new JMenuItem("Keyboard Shortcuts...");
		mi.setToolTipText("See what keyboard shortcuts are supported");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Helper.showKeyboardShortcuts(frame);
			}
		});
		menu.add(mi);

		mi = new JMenuItem("Online Manual...");
		mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0, true));
		mi.setToolTipText("Open the Online Manual");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Helper.openBrowser("http://energy.concord.org/energy2d/manual");
			}
		});
		menu.add(mi);
		mi = new JMenuItem("Contact Us...");
		mi.setToolTipText("Contact us");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Helper.openBrowser("http://energy.concord.org/energy2d/contact.html");
			}
		});
		menu.add(mi);

		if (!System.getProperty("os.name").startsWith("Mac")) {
			menu.addSeparator();
			mi = new JMenuItem("About...");
			mi.setToolTipText("About " + System2D.BRAND_NAME);
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Helper.showAbout(frame);
				}
			});
			menu.add(mi);
		}

	}

	private void createMenu(final System2D box, JMenu menu, LinkedHashMap<String, String> templates) {
		JMenuItem mi;
		for (Map.Entry<String, String> x : templates.entrySet()) {
			mi = new JMenuItem(x.getKey());
			final String val = x.getValue();
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					box.loadModel(val);
				}
			});
			menu.add(mi);
		}
		templates.clear();
	}

	void setLatestPath(String latestPath, String type) {
		if (latestPath != null) {
			if ("htm".equalsIgnoreCase(type)) {
				htmFileChooser.setCurrentDirectory(new File(latestPath));
			} else if ("png".equalsIgnoreCase(type)) {
				screenshotSaver.setCurrentDirectory(new File(latestPath));
			} else if ("e2d".equalsIgnoreCase(type)) {
				e2dFileChooser.setCurrentDirectory(new File(latestPath));
			}
		}
	}

	String getLatestPath(String type) {
		if ("htm".equalsIgnoreCase(type))
			return htmFileChooser.getLatestPath();
		if ("png".equalsIgnoreCase(type))
			return screenshotSaver.getLatestPath();
		return e2dFileChooser.getLatestPath();
	}

	void addRecentFile(String path) {
		if (path != null)
			e2dFileChooser.addRecentFile(path);
	}

	String[] getRecentFiles() {
		return e2dFileChooser.getRecentFiles();
	}

	private void save(System2D box) {
		try {
			box.saveState(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(box.getCurrentFile()), "UTF-8")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveAs(System2D box, JFrame frame) {
		e2dFileChooser.setAcceptAllFileFilterUsed(false);
		e2dFileChooser.addChoosableFileFilter(e2dFilter);
		e2dFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		e2dFileChooser.setDialogTitle("Save");
		e2dFileChooser.setApproveButtonMnemonic('S');
		if (e2dFileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
			File file = e2dFileChooser.getSelectedFile();
			if (!file.toString().toLowerCase().endsWith(".e2d")) {
				file = new File(file.getParentFile(), MiscUtil.getFileName(file.toString()) + ".e2d");
			}
			boolean b = true;
			if (file.exists()) {
				if (JOptionPane.showConfirmDialog(frame, "File " + file.getName() + " exists, overwrite?", "File exists", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
					b = false;
				}
			}
			if (b) {
				box.setCurrentFile(file);
				try {
					box.saveState(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(box.getCurrentFile()), "UTF-8")));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			e2dFileChooser.rememberFile(file.getPath());
		}
	}

	private void saveAsApplet(System2D box, JFrame frame) {
		htmFileChooser.setAcceptAllFileFilterUsed(false);
		htmFileChooser.addChoosableFileFilter(htmFilter);
		htmFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		htmFileChooser.setDialogTitle("Save As Applet");
		htmFileChooser.setApproveButtonMnemonic('S');
		if (htmFileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
			File file = htmFileChooser.getSelectedFile();
			if (!file.toString().toLowerCase().endsWith(".htm")) {
				file = new File(file.getParentFile(), MiscUtil.getFileName(file.toString()) + ".htm");
			}
			boolean b = true;
			if (file.exists()) {
				if (JOptionPane.showConfirmDialog(frame, "File " + file.getName() + " exists, overwrite?", "File exists", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
					b = false;
				}
			}
			if (b) {
				box.saveApplet(file);
			}
			htmFileChooser.rememberFile(file.getPath());
		}
	}

	private float parse(String s) {
		float x = Float.NaN;
		try {
			x = Float.parseFloat(s);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(MenuBar.this), "Cannot parse: " + s, "Error", JOptionPane.ERROR_MESSAGE);
		}
		return x;
	}
}
