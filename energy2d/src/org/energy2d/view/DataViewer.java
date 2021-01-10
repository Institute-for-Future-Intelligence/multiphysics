package org.energy2d.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.energy2d.model.Anemometer;
import org.energy2d.model.HeatFluxSensor;
import org.energy2d.model.Sensor;
import org.energy2d.model.Thermometer;
import org.energy2d.model.TimedData;
import org.energy2d.system.System2D;

/**
 * @author Charles Xie
 * 
 */
class DataViewer {

	private View2D view;

	DataViewer(View2D view) {
		this.view = view;
	}

	void showDataOfType(byte type) {

		switch (type) {
		case 0:
			List<Thermometer> thermometers = view.model.getThermometers();
			int n = thermometers.size();
			if (n < 1) {
				JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(view), "No thermometer is found.", "No sensor", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			Sensor[] sensors = new Sensor[n];
			for (int i = 0; i < n; i++)
				sensors[i] = thermometers.get(i);
			showData("Thermometer" + (n > 1 ? "s" : "") + " (" + '\u2103' + ")", sensors);
			break;
		case 1:
			List<HeatFluxSensor> heatFluxSensors = view.model.getHeatFluxSensors();
			n = heatFluxSensors.size();
			if (n < 1) {
				JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(view), "No heat flux sensor is found.", "No sensor", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			sensors = new Sensor[n];
			for (int i = 0; i < n; i++)
				sensors[i] = heatFluxSensors.get(i);
			showData("Heat Flux Sensor" + (n > 1 ? "s" : "") + " (W/m" + '\u00B2' + ")", sensors);
			break;
		case 2:
			List<Anemometer> anemometers = view.model.getAnemometers();
			n = anemometers.size();
			if (n < 1) {
				JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(view), "No anemometer is found.", "No sensor", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			sensors = new Sensor[n];
			for (int i = 0; i < n; i++)
				sensors[i] = anemometers.get(i);
			showData("Anemometer" + (n > 1 ? "s" : "") + " (m/s)", sensors);
			break;
		}
	}

	private void showData(String title, Sensor[] sensors) {
		List<TimedData> data = sensors[0].getData();
		int n = 0;
		for (Sensor s : sensors) {
			int size = s.getData().size();
			if (size > n) {
				n = size;
				data = s.getData();
			}
		}
		if (n < 1) {
			JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(view), "No data has been collected.", "No data", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		String[] header = new String[sensors.length + 1];
		header[0] = "Time";
		Object[][] column = new Object[n][sensors.length + 1];
		for (int i = 0; i < n; i++)
			column[i][0] = data.get(i).getTime(); // use sensor 0's time slot
		for (int j = 0; j < sensors.length; j++) {
			header[j + 1] = sensors[j].getLabel() != null ? sensors[j].getLabel() : "Value";
			data = sensors[j].getData();
			if (data.size() > 0) {
				for (int i = 0; i < n; i++)
					column[i][j + 1] = data.get(i).getValue();
			}
		}
		showDataWindow(title, column, header);
	}

	void showData(Sensor sensor) {
		List<TimedData> data = sensor.getData();
		int n = data.size();
		if (n < 1) {
			JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(view), "No data has been collected.", "No data", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		String[] header = new String[] { "Time", sensor.getLabel() != null ? sensor.getLabel() : "Value" };
		Object[][] column = new Object[n][2];
		data = sensor.getData();
		for (int i = 0; i < n; i++) {
			TimedData d = data.get(i);
			column[i][0] = d.getTime();
			column[i][1] = d.getValue();
		}
		showDataWindow(sensor.getName(), column, header);
	}

	private void showDataWindow(String title, Object[][] column, String[] header) {
		final JDialog dataWindow = new JDialog(JOptionPane.getFrameForComponent(view), title, true);
		dataWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		final JTable table = new JTable(column, header);
		table.setModel(new DefaultTableModel(column, header) {
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		});
		dataWindow.getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
		JPanel p = new JPanel();
		dataWindow.getContentPane().add(p, BorderLayout.SOUTH);
		JButton button = new JButton("Copy Data");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				table.selectAll();
				ActionEvent ae = new ActionEvent(table, ActionEvent.ACTION_PERFORMED, "copy");
				if (ae != null) {
					table.getActionMap().get(ae.getActionCommand()).actionPerformed(ae);
					JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(view), "The data is now ready for pasting.", "Copy Data", JOptionPane.INFORMATION_MESSAGE);
					table.clearSelection();
				}
			}
		});
		button.setEnabled(!System2D.isApplet());
		button.setToolTipText(button.isEnabled() ? "Copy data to the system clipboard" : "Copying data is not allowed as this is an applet.");
		p.add(button);
		button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dataWindow.dispose();
			}
		});
		p.add(button);
		dataWindow.pack();
		dataWindow.setLocationRelativeTo(view);
		dataWindow.setVisible(true);
	}

	void showAllData() {

		final JDialog dataWindow = new JDialog(JOptionPane.getFrameForComponent(view), "Sensor Data", true);
		dataWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		final JTabbedPane tabbedPane = new JTabbedPane();
		dataWindow.getContentPane().add(tabbedPane, BorderLayout.CENTER);

		final List<JTable> tables = new ArrayList<JTable>();

		List<Thermometer> thermometers = view.model.getThermometers();
		int count = thermometers.size();
		if (count > 0) {
			int n = thermometers.get(0).getData().size();
			String[] header = new String[count + 1];
			header[0] = "Time";
			Object[][] column = new Object[n][count + 1];
			for (int i = 0; i < n; i++)
				column[i][0] = thermometers.get(0).getData().get(i).getTime();
			for (int j = 0; j < count; j++) {
				Thermometer t = thermometers.get(j);
				header[j + 1] = t.getLabel() != null ? t.getLabel() : "Value";
				for (int i = 0; i < n; i++)
					column[i][j + 1] = t.getData().get(i).getValue();
			}
			JTable table = new JTable(column, header);
			table.setModel(new DefaultTableModel(column, header) {
				public boolean isCellEditable(int row, int col) {
					return false;
				}
			});
			tables.add(table);
			tabbedPane.addTab("Thermometer" + (count > 1 ? "s" : "") + " (" + '\u2103' + ")", new JScrollPane(table));
		}

		List<HeatFluxSensor> heatFluxSensors = view.model.getHeatFluxSensors();
		count = heatFluxSensors.size();
		if (count > 0) {
			int n = heatFluxSensors.get(0).getData().size();
			String[] header = new String[count + 1];
			header[0] = "Time";
			Object[][] column = new Object[n][count + 1];
			for (int i = 0; i < n; i++)
				column[i][0] = heatFluxSensors.get(0).getData().get(i).getTime();
			for (int j = 0; j < count; j++) {
				HeatFluxSensor h = heatFluxSensors.get(j);
				header[j + 1] = h.getLabel() != null ? h.getLabel() : "Value";
				for (int i = 0; i < n; i++)
					column[i][j + 1] = h.getData().get(i).getValue();
			}
			JTable table = new JTable(column, header);
			table.setModel(new DefaultTableModel(column, header) {
				public boolean isCellEditable(int row, int col) {
					return false;
				}
			});
			tables.add(table);
			tabbedPane.addTab("Heat Flux Sensor" + (n > 1 ? "s" : "") + " (W/m" + '\u00B2' + ")", new JScrollPane(table));
		}

		List<Anemometer> anemometers = view.model.getAnemometers();
		count = anemometers.size();
		if (count > 0) {
			int n = anemometers.get(0).getData().size();
			String[] header = new String[count + 1];
			header[0] = "Time";
			Object[][] column = new Object[n][count + 1];
			for (int i = 0; i < n; i++)
				column[i][0] = anemometers.get(0).getData().get(i).getTime();
			for (int j = 0; j < count; j++) {
				Anemometer a = anemometers.get(j);
				header[j + 1] = a.getLabel() != null ? a.getLabel() : "Value";
				for (int i = 0; i < n; i++)
					column[i][j + 1] = a.getData().get(i).getValue();
			}
			JTable table = new JTable(column, header);
			table.setModel(new DefaultTableModel(column, header) {
				public boolean isCellEditable(int row, int col) {
					return false;
				}
			});
			tables.add(table);
			tabbedPane.addTab("Anemometer" + (count > 1 ? "s" : "") + " (m/s)", new JScrollPane(table));
		}

		if (tables.isEmpty()) {
			JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(view), "No sensor is found.", "No sensor", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		JPanel p = new JPanel();
		dataWindow.getContentPane().add(p, BorderLayout.SOUTH);
		JButton button = new JButton("Copy Data");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedIndex = tabbedPane.getSelectedIndex();
				JTable t = tables.get(selectedIndex);
				if (t != null) {
					t.selectAll();
					ActionEvent ae = new ActionEvent(t, ActionEvent.ACTION_PERFORMED, "copy");
					if (ae != null) {
						t.getActionMap().get(ae.getActionCommand()).actionPerformed(ae);
						JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(view), "The data is now ready for pasting.", "Copy Data", JOptionPane.INFORMATION_MESSAGE);
						t.clearSelection();
					}
				}
			}
		});
		button.setEnabled(!System2D.isApplet());
		button.setToolTipText(button.isEnabled() ? "Copy data to the system clipboard" : "Copying data is not allowed as this is an applet.");
		p.add(button);
		button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dataWindow.dispose();
			}
		});
		p.add(button);

		dataWindow.pack();
		dataWindow.setLocationRelativeTo(view);
		dataWindow.setVisible(true);

	}

}
