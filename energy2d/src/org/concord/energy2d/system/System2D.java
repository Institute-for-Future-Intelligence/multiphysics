package org.concord.energy2d.system;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.event.ManipulationListener;
import org.concord.energy2d.model.Anemometer;
import org.concord.energy2d.model.Cloud;
import org.concord.energy2d.model.Fan;
import org.concord.energy2d.model.HeatFluxSensor;
import org.concord.energy2d.model.Model2D;
import org.concord.energy2d.model.Part;
import org.concord.energy2d.model.Particle;
import org.concord.energy2d.model.ParticleFeeder;
import org.concord.energy2d.model.Sensor;
import org.concord.energy2d.model.Thermometer;
import org.concord.energy2d.model.Tree;
import org.concord.energy2d.util.MiscUtil;
import org.concord.energy2d.view.TextBox;
import org.concord.energy2d.view.View2D;
import org.concord.modeler.MwService;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

/**
 * Deploy as an app (energy2d.jar) or an applet (energy2d-applet.jar). The applet has no menu bar and tool bar and doesn't include /models and /resources.
 * 
 * @author Charles Xie
 * 
 */
public class System2D extends JApplet implements MwService, ManipulationListener {

	final static String BRAND_NAME = "Energy2D V2.2";

	Model2D model;
	View2D view;
	TaskManager taskManager;
	Task repaint, measure, control;
	private Scripter2D scripter;
	private ExecutorService threadService;
	private static boolean isApplet = true;

	private SAXParser saxParser;
	private DefaultHandler saxHandler;
	private XmlEncoder encoder;
	private File currentFile;
	private URL currentURL;
	private String currentModel;
	private boolean saved = true;
	private String nextSim, prevSim;

	Runnable clickRun, clickStop, clickReset, clickReload;
	private JButton buttonRun, buttonStop, buttonReset, buttonReload;
	private JLabel statusLabel;
	private ToolBarListener toolBarListener;
	private List<PropertyChangeListener> propertyChangeListeners;
	private JFrame owner;
	private static Preferences preferences;

	public System2D() {

		// Locale.setDefault(Locale.US); for the applet, this is a security violation
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		propertyChangeListeners = new ArrayList<PropertyChangeListener>();

		model = new Model2D();
		model.addManipulationListener(this);
		view = new View2D();
		view.addManipulationListener(this);
		view.setModel(model);
		view.setPreferredSize(new Dimension(400, 400));
		view.setBorder(BorderFactory.createEtchedBorder());
		view.setArea(0, model.getLx(), 0, model.getLy());
		addPropertyChangeListener(view);
		model.addPropertyChangeListener(view);
		getContentPane().add(view, BorderLayout.CENTER);

		encoder = new XmlEncoder(this);
		saxHandler = new XmlDecoder(this);
		try {
			saxParser = SAXParserFactory.newInstance().newSAXParser();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		createActions();

		taskManager = new TaskManager() {

			@Override
			public void runScript(String script) {
				runNativeScript(script);
			}

			@Override
			public void notifyChange() {
				view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}

			@Override
			public int getIndexOfStep() {
				return model.getIndexOfStep();
			}
		};
		model.setTasks(new Runnable() {
			public void run() {
				taskManager.execute();
			}
		});
		createTasks();

	}

	public static boolean isApplet() {
		return isApplet;
	}

	public void addTask(Task t) {
		taskManager.add(t);
	}

	private void createTasks() {

		repaint = new Task(20) {
			@Override
			public void execute() {
				view.repaint();
				view.setTime(model.getTime());
			}
		};
		repaint.setUid("REPAINT");
		repaint.setDescription("Refresh the view.");
		addTask(repaint);

		measure = new Task(100) {
			@Override
			public void execute() {
				model.takeMeasurement();
			}
		};
		measure.setUid("MEASURE");
		measure.setDescription("Take the measurements from the sensors.");
		addTask(measure);

		control = new Task(100) {
			@Override
			public void execute() {
				model.control();
			}
		};
		control.setUid("CONTROL");
		control.setDescription("Invoke the controllers (e.g., thermostats).");
		addTask(control);

		taskManager.processPendingRequests();

	}

	public View2D getView() {
		return view;
	}

	public Model2D getModel() {
		return model;
	}

	public void setOwner(JFrame owner) {
		this.owner = owner;
	}

	private void createActions() {

		Action a = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				Helper.showScriptDialog(System2D.this);
			}
		};
		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0, true);
		a.putValue(Action.NAME, "Script");
		a.putValue(Action.ACCELERATOR_KEY, ks);
		a.putValue(Action.SHORT_DESCRIPTION, "Open the Script Console");
		view.getInputMap().put(ks, "Script");
		view.getActionMap().put("Script", a);

		a = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				view.createDialog(model, true);
			}
		};
		ks = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_MASK);
		a.putValue(Action.NAME, "Property");
		a.putValue(Action.ACCELERATOR_KEY, ks);
		a.putValue(Action.SHORT_DESCRIPTION, "Open the Properties Editor");
		view.getInputMap().put(ks, "Property");
		view.getActionMap().put("Property", a);

		a = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				taskManager.show(owner);
			}
		};
		ks = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0, true);
		a.putValue(Action.NAME, "Task_Manager");
		a.putValue(Action.ACCELERATOR_KEY, ks);
		a.putValue(Action.SHORT_DESCRIPTION, "Open the Task Manager");
		view.getInputMap().put(ks, "Task_Manager");
		view.getActionMap().put("Task_Manager", a);

		a = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				PreferencesDialog d = new PreferencesDialog(System2D.this, true);
				d.setVisible(true);
			}
		};
		a.putValue(Action.NAME, "Preferences");
		a.putValue(Action.SHORT_DESCRIPTION, "Open the Preferences Dialog");
		view.getActionMap().put("Preferences", a);

		a = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (nextSim != null) {
					try {
						loadSim(nextSim);
					} catch (IOException ioe) {
						JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(view), nextSim + " cannot be loaded: " + ioe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		};
		a.putValue(Action.NAME, "Next");
		a.putValue(Action.SHORT_DESCRIPTION, "Next Simulation");
		view.getActionMap().put("Next_Simulation", a);

		a = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (prevSim != null) {
					try {
						loadSim(prevSim);
					} catch (IOException ioe) {
						JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(view), prevSim + " cannot be loaded: " + ioe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		};
		a.putValue(Action.NAME, "Previous");
		a.putValue(Action.SHORT_DESCRIPTION, "Previous Simulation");
		view.getActionMap().put("Previous_Simulation", a);

	}

	@Override
	public void init() {
		String s = null;
		try {
			s = getParameter("script");
		} catch (Exception e) {
			s = null;
		}
		if (s != null) {
			runNativeScript(s);
		}
		view.repaint();
		System.out.println(BRAND_NAME + " initialized.");
	}

	void executeInThreadService(Runnable r) {
		if (threadService == null)
			threadService = Executors.newFixedThreadPool(1);
		threadService.execute(r);
	}

	public void run() {
		view.setRunToggle(true);
		executeInThreadService(new Runnable() {
			public void run() {
				model.run();
			}
		});
	}

	public void runSteps(final int n) {
		executeInThreadService(new Runnable() {
			public void run() {
				// TODO
			}
		});
	}

	public void stop() {
		model.stop();
		view.setRunToggle(false);
	}

	public void reset() {
		model.reset();
		view.reset();
		view.repaint();
	}

	public void initialize() {
		clear();
		init();
	}

	public void clear() {
		model.clear();
		view.clear();
		view.repaint();
	}

	public void destroy() {
		stop();
		try {
			if (threadService != null && !threadService.isShutdown()) {
				threadService.shutdownNow();
			}
		} catch (Throwable t) {
		}
	}

	public JPopupMenu getPopupMenu() {
		return view.getPopupMenu();
	}

	public Component getSnapshotComponent() {
		return view;
	}

	void setSaved(boolean b) {
		saved = b;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				setFrameTitle();
			}
		});
	}

	void saveApplet(File file) {
		new AppletConverter(this).write(file);
	}

	private void loadStateApp(Reader reader) throws IOException {
		// stop();
		reset();
		clear();
		if (reader == null)
			return;
		try {
			saxParser.parse(new InputSource(reader), saxHandler);
		} catch (SAXException e) {
			e.printStackTrace();
		} finally {
			reader.close();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				if (buttonStop != null)
					callAction(buttonStop);
			}
		});
		setSaved(true);
	}

	private void loadStateApp(InputStream is) throws IOException {
		// stop();
		reset();
		clear();
		loadState(is);
	}

	public void loadState(InputStream is) throws IOException {
		// stop();
		if (is == null)
			return;
		try {
			saxParser.parse(new InputSource(is), saxHandler);
		} catch (SAXException e) {
			e.printStackTrace();
		} finally {
			is.close();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				if (buttonStop != null)
					callAction(buttonStop);
			}
		});
		setSaved(true);
	}

	void saveState(Writer writer) throws IOException {
		updateStatus("Saving...");
		if (clickStop != null) {
			EventQueue.invokeLater(clickStop);
		} else {
			stop();
		}
		if (writer == null)
			return;
		try {
			writer.write(encoder.encode().toCharArray());
		} finally {
			writer.close();
		}
		setSaved(true);
	}

	public void saveState(OutputStream os) throws IOException {
		updateStatus("Saving...");
		if (clickStop != null) {
			EventQueue.invokeLater(clickStop);
		} else {
			stop();
		}
		if (os == null)
			return;
		try {
			os.write(encoder.encode().getBytes());
		} finally {
			os.close();
		}
		setSaved(true);
	}

	private void updateStatus(final String status) {
		if (statusLabel != null)
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					statusLabel.setText(status);
					Timer timer = new Timer(1000, new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							statusLabel.setText(null);
							if (buttonReload != null)
								buttonReload.setEnabled(true);
						}
					});
					timer.setRepeats(false);
					timer.start();
				}
			});
	}

	void loadFile(File file) {
		setReloadButtonEnabled(true);
		if (file == null)
			return;
		try {
			// loadStateApp(new FileInputStream(file)); this call doesn't work on some Mac
			loadStateApp(new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8")));
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(view), e.getLocalizedMessage(), "File error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		notifyToolBarListener(new ToolBarEvent(ToolBarEvent.FILE_INPUT, this));
		currentFile = file;
		currentModel = null;
		currentURL = null;
		setFrameTitle();
	}

	void loadModel(String name) {
		setReloadButtonEnabled(true);
		if (name == null)
			return;
		if (!askSaveBeforeLoading())
			return;
		try {
			loadStateApp(System2D.class.getResourceAsStream(name));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		notifyToolBarListener(new ToolBarEvent(ToolBarEvent.FILE_INPUT, this));
		currentModel = name;
		currentFile = null;
		currentURL = null;
		setFrameTitle();
	}

	public void loadURL(URL url) throws IOException {
		setReloadButtonEnabled(true);
		if (url == null)
			return;
		if (!askSaveBeforeLoading())
			return;
		loadStateApp(url.openConnection().getInputStream());
		notifyToolBarListener(new ToolBarEvent(ToolBarEvent.FILE_INPUT, this));
		currentURL = url;
		currentFile = null;
		currentModel = null;
		setFrameTitle();
	}

	public void reload() {
		if (currentFile != null) {
			loadFile(currentFile);
			return;
		}
		if (currentModel != null) {
			loadModel(currentModel);
			return;
		}
		if (currentURL != null) {
			try {
				loadURL(currentURL);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
	}

	public void setNextSimulation(String nextSim) {
		notifyPropertyChangeListeners("Next Simulation", this.nextSim, nextSim);
		this.nextSim = nextSim;
	}

	public String getNextSimulation() {
		return nextSim;
	}

	public void setPreviousSimulation(String prevSim) {
		notifyPropertyChangeListeners("Prev Simulation", this.prevSim, prevSim);
		this.prevSim = prevSim;
	}

	public String getPreviousSimulation() {
		return prevSim;
	}

	/** Load a simulation by its file name. Currently only load from the same directory as the current file or model. */
	public void loadSim(String fileName) throws IOException {
		if (currentFile != null) {
			String parentDirectory = MiscUtil.getParentDirectory(currentFile.toString());
			if (parentDirectory != null)
				loadFile(new File(parentDirectory, fileName));
		} else if (currentModel != null) {
			String parentDirectory = MiscUtil.getParentDirectory(currentModel);
			if (parentDirectory != null)
				loadModel(parentDirectory + fileName);
		} else if (currentURL != null) {
			String parentDirectory = MiscUtil.getParentDirectory(currentURL.toString());
			if (parentDirectory != null)
				loadURL(new URL(parentDirectory + fileName));
		} else {
			URL codeBase = null;
			try {
				codeBase = getCodeBase();
			} catch (Exception e) {
			}
			if (codeBase != null) {
				try {
					loadURL(new URL(codeBase, fileName));
				} catch (IOException e) {
					throw e;
				}
			}
		}
	}

	private void setReloadButtonEnabled(final boolean b) {
		if (buttonReload == null)
			return;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				buttonReload.setEnabled(b);
			}
		});
	}

	int askSaveOption() {
		if (saved || owner == null || currentModel != null || currentURL != null)
			return JOptionPane.NO_OPTION;
		return JOptionPane.showConfirmDialog(owner, "Do you want to save the changes?", "Save", JOptionPane.YES_NO_CANCEL_OPTION);
	}

	boolean askSaveBeforeLoading() {
		if (owner == null) // not an application
			return true;
		switch (askSaveOption()) {
		case JOptionPane.YES_OPTION:
			Action a = null;
			if (currentFile != null) {
				a = view.getActionMap().get("Save");
			} else {
				a = view.getActionMap().get("SaveAs");
			}
			if (a != null)
				a.actionPerformed(null);
			return true;
		case JOptionPane.NO_OPTION:
			return true;
		default:
			return false;
		}
	}

	void setCurrentModel(String name) {
		currentModel = name;
	}

	public String getCurrentModel() {
		return currentModel;
	}

	void setCurrentFile(File file) {
		currentFile = file;
		setFrameTitle();
	}

	File getCurrentFile() {
		return currentFile;
	}

	public URL getCurrentURL() {
		return currentURL;
	}

	public boolean needExecutorService() {
		return true;
	}

	public String runNativeScript(String script) {
		if (script == null)
			return null;
		if (scripter == null)
			scripter = new Scripter2D(this);
		scripter.executeScript(script);
		if (scripter.shouldNotifySaveReminder())
			setSaved(false);
		return null;
	}

	public Scripter2D getScripter() {
		if (scripter == null)
			scripter = new Scripter2D(this);
		return scripter;
	}

	public void setEditable(boolean b) {
	}

	public void setExecutorService(ExecutorService service) {
		threadService = service;
	}

	public void manipulationOccured(ManipulationEvent e) {
		Object target = e.getTarget();
		switch (e.getType()) {
		case ManipulationEvent.REPAINT:
			view.repaint();
			break;
		case ManipulationEvent.PROPERTY_CHANGE:
			setSaved(false);
			break;
		case ManipulationEvent.TRANSLATE:
			setSaved(false);
			break;
		case ManipulationEvent.RESIZE:
			setSaved(false);
			break;
		case ManipulationEvent.OBJECT_ADDED:
			setSaved(false);
			break;
		case ManipulationEvent.SENSOR_ADDED:
			setSaved(false);
			break;
		case ManipulationEvent.DELETE:
			if (target instanceof Part)
				model.removePart((Part) target);
			else if (target instanceof Particle)
				model.removeParticle((Particle) target);
			else if (target instanceof Anemometer)
				model.removeAnemometer((Anemometer) target);
			else if (target instanceof Thermometer)
				model.removeThermometer((Thermometer) target);
			else if (target instanceof HeatFluxSensor)
				model.removeHeatFluxSensor((HeatFluxSensor) target);
			else if (target instanceof TextBox)
				view.removeTextBox((TextBox) target);
			else if (target instanceof Cloud)
				view.removeCloud((Cloud) target);
			else if (target instanceof Tree)
				view.removeTree((Tree) target);
			else if (target instanceof Fan)
				view.removeFan((Fan) target);
			else if (target instanceof ParticleFeeder)
				view.removeParticleFeeder((ParticleFeeder) target);
			setSaved(false);
			break;
		case ManipulationEvent.RUN:
			if (clickRun != null) {
				EventQueue.invokeLater(clickRun);
			} else {
				run();
			}
			break;
		case ManipulationEvent.STOP:
			if (clickStop != null) {
				EventQueue.invokeLater(clickStop);
			} else {
				stop();
			}
			break;
		case ManipulationEvent.RESET:
			if (clickReset != null) {
				EventQueue.invokeLater(clickReset);
			} else {
				reset();
			}
			break;
		case ManipulationEvent.RELOAD:
			if (clickReload != null) {
				EventQueue.invokeLater(clickReload);
			} else {
				reload();
			}
			break;
		case ManipulationEvent.FATAL_ERROR_OCCURRED:
			view.repaint();
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(view), "<html>The current time steplength is " + model.getTimeStep() + " s.<br>Reduce it in the Properties Window and then reset the simulation.<br>(Usually it should be less than 1 s for convection simulations.)", "Fatal error", JOptionPane.INFORMATION_MESSAGE);
					Action propertyAction = view.getActionMap().get("Property");
					if (propertyAction != null)
						propertyAction.actionPerformed(null);
				}
			});
			break;
		case ManipulationEvent.SUN_SHINE:
			model.setSunny(!model.isSunny());
			model.refreshPowerArray();
			setSaved(false);
			break;
		case ManipulationEvent.SUN_ANGLE_INCREASE:
			float a = model.getSunAngle() + (float) Math.PI / 18;
			model.setSunAngle(Math.min(a, (float) Math.PI));
			model.refreshPowerArray();
			setSaved(false);
			break;
		case ManipulationEvent.SUN_ANGLE_DECREASE:
			a = model.getSunAngle() - (float) Math.PI / 18;
			model.setSunAngle(Math.max(a, 0));
			model.refreshPowerArray();
			setSaved(false);
			break;
		}
		if (target instanceof Part) {
			Part p = (Part) target;
			model.refreshMaterialPropertyArrays();
			model.refreshPowerArray();
			model.refreshTemperatureBoundaryArray();
			if (p.getEmissivity() > 0)
				model.getPhotons().clear();
			if (model.isRadiative())
				model.generateViewFactorMesh();
			setSaved(false);
		} else if (target instanceof Fan) {
			model.refreshMaterialPropertyArrays();
		}
		view.repaint();
	}

	private JPanel createButtonPanel() {
		statusLabel = new JLabel();
		JPanel p = new JPanel();
		buttonRun = new JButton("Run");
		buttonRun.setToolTipText("Run the simulation");
		buttonRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				run();
				buttonRun.setEnabled(false);
				buttonStop.setEnabled(true);
			}
		});
		p.add(buttonRun);
		buttonStop = new JButton("Stop");
		buttonStop.setEnabled(false);
		buttonStop.setToolTipText("Stop the simulation");
		buttonStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stop();
				buttonRun.setEnabled(true);
				buttonStop.setEnabled(false);
			}
		});
		p.add(buttonStop);
		buttonReset = new JButton("Reset");
		buttonReset.setToolTipText("Reset the simulation to time zero");
		buttonReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reset();
				buttonRun.setEnabled(true);
				buttonStop.setEnabled(false);
				notifyToolBarListener(new ToolBarEvent(ToolBarEvent.RESET, buttonReset));
			}
		});
		p.add(buttonReset);
		JPanel spacer = new JPanel();
		spacer.setPreferredSize(new Dimension(20, 10));
		p.add(spacer);
		buttonReload = new JButton("Reload");
		buttonReload.setEnabled(false);
		buttonReload.setToolTipText("Reload the initial configurations");
		buttonReload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!askSaveBeforeLoading())
					return;
				reload();
				buttonRun.setEnabled(true);
				buttonStop.setEnabled(false);
			}
		});
		p.add(buttonReload);
		clickRun = new Runnable() {
			public void run() {
				callAction(buttonRun);
			}
		};
		clickStop = new Runnable() {
			public void run() {
				callAction(buttonStop);
			}
		};
		clickReset = new Runnable() {
			public void run() {
				callAction(buttonReset);
			}
		};
		clickReload = new Runnable() {
			public void run() {
				callAction(buttonReload);
			}
		};
		return p;
	}

	// JButton.doClick() does not do anything if a button is disabled.
	private static void callAction(JButton button) {
		ActionListener[] a = button.getActionListeners();
		if (a == null || a.length == 0)
			return;
		for (ActionListener x : a)
			x.actionPerformed(null);
	}

	void setToolBarListener(ToolBarListener l) {
		toolBarListener = l;
	}

	void notifyToolBarListener(ToolBarEvent e) {
		setFrameTitle();
		if (toolBarListener != null)
			toolBarListener.tableBarShouldChange(e);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if (!propertyChangeListeners.contains(listener))
			propertyChangeListeners.add(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		if (listener != null)
			propertyChangeListeners.remove(listener);
	}

	private void notifyPropertyChangeListeners(String propertyName, Object oldValue, Object newValue) {
		if (propertyChangeListeners.isEmpty())
			return;
		PropertyChangeEvent e = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
		for (PropertyChangeListener x : propertyChangeListeners)
			x.propertyChange(e);
	}

	void setFrameTitle() {
		if (owner == null)
			return;
		if (currentFile != null) {
			owner.setTitle(BRAND_NAME + ": " + currentFile + (saved ? "" : " *"));
		} else if (currentModel != null) {
			owner.setTitle(BRAND_NAME + ": " + currentModel);
		} else if (currentURL != null) {
			owner.setTitle(BRAND_NAME + ": " + currentURL);
		} else {
			owner.setTitle(BRAND_NAME);
		}
	}

	void shutdown() {
		if (preferences != null && owner != null) {
			Rectangle bounds = owner.getBounds();
			preferences.putInt("Upper-left x", bounds.x);
			preferences.putInt("Upper-left y", bounds.y);
			MenuBar menuBar = (MenuBar) owner.getJMenuBar();
			preferences.put("Latest E2D Path", menuBar.getLatestPath("e2d"));
			preferences.put("Latest HTM Path", menuBar.getLatestPath("htm"));
			preferences.put("Latest PNG Path", menuBar.getLatestPath("png"));
			String[] recentFiles = menuBar.getRecentFiles();
			if (recentFiles != null) {
				int n = recentFiles.length;
				if (n > 0)
					for (int i = 0; i < n; i++)
						preferences.put("Recent File " + i, recentFiles[n - i - 1]);
			}
			preferences.putInt("Sensor Maximum Data Points", Sensor.getMaximumDataPoints());
		}
		MiscUtil.shutdown();
	}

	public static void main(final String[] args) {

		isApplet = false;

		Locale.setDefault(Locale.US);

		boolean launchedByJWS = false;

		// detect if the app is launched via webstart just checking its class loader: SystemClassLoader or JnlpClassLoader.
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (!cl.equals(ClassLoader.getSystemClassLoader()))
			launchedByJWS = true;

		if (System.getProperty("os.name").startsWith("Mac")) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", BRAND_NAME);
		}

		if (preferences == null)
			preferences = Preferences.userNodeForPackage(System2D.class);
		Sensor.setMaximumDataPoints(preferences.getInt("Sensor Maximum Data Points", 1000));

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int w = (int) (screen.height * 0.7);

		final System2D box = new System2D();
		box.view.setPreferredSize(new Dimension(w, w));
		box.view.setGridOn(true);
		box.view.setBorderTickmarksOn(true);
		// new org.concord.energy2d.model.PartFactory(box.model).addBlob();
		final JFrame frame = new JFrame();
		frame.setIconImage(new ImageIcon(System2D.class.getResource("resources/frame.png")).getImage());
		final MenuBar menuBar = new MenuBar(box, frame);
		menuBar.setLatestPath(preferences.get("Latest E2D Path", null), "e2d");
		menuBar.setLatestPath(preferences.get("Latest HTM Path", null), "htm");
		menuBar.setLatestPath(preferences.get("Latest PNG Path", null), "png");
		menuBar.addRecentFile(preferences.get("Recent File 0", null));
		menuBar.addRecentFile(preferences.get("Recent File 1", null));
		menuBar.addRecentFile(preferences.get("Recent File 2", null));
		menuBar.addRecentFile(preferences.get("Recent File 3", null));
		frame.setJMenuBar(menuBar);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.getContentPane().add(box.getContentPane(), BorderLayout.CENTER);
		ToolBar toolBar = new ToolBar(box);
		box.setToolBarListener(toolBar);
		box.view.addManipulationListener(toolBar);
		frame.getContentPane().add(toolBar, BorderLayout.NORTH);
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		bottomPanel.add(box.createButtonPanel(), BorderLayout.CENTER);
		box.statusLabel.setPreferredSize(new Dimension(100, 24));
		bottomPanel.add(box.statusLabel, BorderLayout.WEST);
		frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		int x = preferences.getInt("Upper-left x", (screen.height - w) / 8);
		int y = preferences.getInt("Upper-left y", (screen.height - w) / 8);
		frame.setLocation(x, y);
		frame.setTitle(BRAND_NAME);
		frame.pack();
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				Action a = box.view.getActionMap().get("Quit");
				if (a != null)
					a.actionPerformed(null);
			}

			public void windowOpened(WindowEvent e) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						if (args != null && args.length > 1) {
							String filePath = args[1];
							if (filePath.toLowerCase().trim().endsWith(".e2d")) {
								box.loadFile(new File(filePath));
								menuBar.e2dFileChooser.rememberFile(filePath);
							}

						}
					}
				});
			}
		});
		box.owner = frame;

		if (System.getProperty("os.name").startsWith("Mac")) {
			Application app = new Application();
			app.setEnabledPreferencesMenu(true);
			app.addApplicationListener(new ApplicationAdapter() {

				@Override
				public void handleQuit(ApplicationEvent e) {
					Action a = box.view.getActionMap().get("Quit");
					if (a != null)
						a.actionPerformed(null);
					e.setHandled(true);
				}

				@Override
				public void handlePreferences(ApplicationEvent e) {
					new PreferencesDialog(box, true).setVisible(true);
					e.setHandled(true);
				}

				@Override
				public void handleOpenFile(final ApplicationEvent e) {
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							String filePath = e.getFilename();
							if (filePath.toLowerCase().trim().endsWith(".e2d")) {
								box.loadFile(new File(filePath));
								menuBar.e2dFileChooser.rememberFile(filePath);
							}
						}
					});
					e.setHandled(true);
				}

				@Override
				public void handleAbout(ApplicationEvent e) {
					Helper.showAbout(frame);
					e.setHandled(true);
				}

			});
		}

		if (!launchedByJWS)
			UpdateAnnouncer.showMessage(box);

	}

}
