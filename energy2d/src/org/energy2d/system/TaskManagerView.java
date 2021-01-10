package org.energy2d.system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

/**
 * @author Charles Xie
 * 
 */

class TaskManagerView {

	private JTable table;
	private Vector<Vector<Object>> rowData;
	private JTextArea note;
	private TaskCreator taskCreator;
	private JButton removeTaskButton, editTaskButton;
	private final int editableCol = 3;
	private final int uidCol = 1;
	private TaskManager taskManager;

	TaskManagerView(TaskManager taskManager) {
		this.taskManager = taskManager;
		rowData = new Vector<Vector<Object>>();
	}

	/* show the tasks in a window */
	void show(Window owner) {
		createTasks(owner).setVisible(true);
	}

	void insertRow(Task task) {
		if (task == null)
			return;
		Vector<Object> v = new Vector<Object>();
		v.add(task.isEnabled());
		v.add(task.getUid());
		v.add(Integer.toString(task.getPriority()));
		v.add(Integer.toString(task.getInterval()));
		v.add(task.getLifetime() == Task.PERMANENT ? "Permanent" : Integer.toString(task.getLifetime()));
		rowData.add(v);
		if (table != null) {
			table.revalidate();
			table.repaint();
		}
	}

	void removeRow(Task task) {
		if (task == null)
			return;
		for (Vector<Object> v : rowData) {
			if (v.elementAt(uidCol).equals(task.getUid())) {
				rowData.remove(v);
				task.setCompleted(false);
				if (table != null) {
					table.clearSelection();
					table.repaint();
					editTaskButton.setEnabled(false);
					removeTaskButton.setEnabled(false);
				}
				break;
			}
		}
	}

	private JDialog createTasks(final Window owner) {

		final JDialog dialog = new JDialog(JOptionPane.getFrameForComponent(owner), "Task Manager", false);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				taskManager.notifyChange();
				dialog.dispose();
			}
		});

		JPanel panel = new JPanel(new BorderLayout(0, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		panel.setPreferredSize(new Dimension(600, 320));
		dialog.setContentPane(panel);

		Vector<String> columnNames = new Vector<String>();
		columnNames.add("Enable");
		columnNames.add("Task");
		columnNames.add("Priority");
		columnNames.add("Interval");
		columnNames.add("Lifetime");

		table = new JTable();
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setSelectionBackground(Color.lightGray);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setPreferredSize(new Dimension(200, 18));
		table.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				int nrow = table.getModel().getRowCount();
				int ncol = table.getModel().getColumnCount();
				Rectangle r;
				int i = 0, j = 0;
				search: for (i = 0; i < nrow; i++) {
					for (j = 0; j < ncol; j++) {
						r = table.getCellRect(i, j, true);
						if (r.contains(x, y)) {
							break search;
						}
					}
				}
				if (j != editableCol && j != 0 && e.getClickCount() >= 2) {
					editTaskButton.doClick();
				}
			}
		});

		rowData.clear();
		for (Task t : taskManager.taskPool) {
			insertRow(t);
		}

		DefaultTableModel tm = new DefaultTableModel(rowData, columnNames) {
			public Class<?> getColumnClass(int columnIndex) {
				return getValueAt(0, columnIndex).getClass();
			}

			public boolean isCellEditable(int row, int col) {
				Task t = taskManager.getTaskByUid((String) table.getValueAt(row, uidCol));
				if (t.isSystemTask() && col == 0)
					return false;
				return col == editableCol || col == 0;
			}
		};
		table.setModel(tm);
		tm.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				DefaultTableModel src = (DefaultTableModel) e.getSource();
				int col = e.getColumn();
				if (col == editableCol) {
					int row = e.getFirstRow();
					Task t = taskManager.getTaskByUid((String) src.getValueAt(row, uidCol));
					String s = (String) src.getValueAt(row, col);
					if (s != null)
						t.setInterval(Integer.parseInt(s));
				} else if (col == 0) {
					int row = e.getFirstRow();
					Task t = taskManager.getTaskByUid((String) src.getValueAt(row, uidCol));
					Boolean b = (Boolean) src.getValueAt(row, col);
					if (b != null)
						t.setEnabled(b.booleanValue());
				}
			}
		});

		ListSelectionModel lsm = table.getSelectionModel();
		lsm.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;
				ListSelectionModel sm = (ListSelectionModel) e.getSource();
				if (!sm.isSelectionEmpty()) {
					int row = sm.getMinSelectionIndex();
					Task t = taskManager.getTaskByUid((String) table.getValueAt(row, uidCol));
					note.setText(t.getDescription());
					boolean b = !t.isSystemTask();
					removeTaskButton.setEnabled(b);
					editTaskButton.setEnabled(b);
				}
			}
		});

		table.getColumnModel().getColumn(0).setMaxWidth(50);
		table.getColumnModel().getColumn(uidCol).setMinWidth(200);
		table.setShowGrid(false);
		table.setRowHeight(24);
		table.setRowMargin(2);
		table.setColumnSelectionAllowed(false);
		table.setBackground(Color.white);

		JScrollPane scroll = new JScrollPane(table);
		scroll.setPreferredSize(new Dimension(400, 150));
		scroll.getViewport().setBackground(Color.white);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setTopComponent(scroll);
		splitPane.setPreferredSize(new Dimension(400, 230));
		splitPane.setOneTouchExpandable(true);

		panel.add(splitPane, BorderLayout.NORTH);

		note = new JTextArea("Display description of a task.");
		note.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		note.setEditable(false);
		note.setFont(new Font("Verdana", Font.PLAIN, 9));
		scroll = new JScrollPane(note);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		splitPane.setBottomComponent(scroll);

		JPanel p = new JPanel(new BorderLayout(0, 10));

		JPanel p1 = new JPanel(new BorderLayout());
		p1.add(new JLabel("<html><font size=2>To change the interval of the selected system task:</font></html>"), BorderLayout.CENTER);
		p1.add(new JLabel("<html><font size=2>Double-click the <i>Interval</i> Cell, change the number, and press ENTER to verify it.</font></html>"), BorderLayout.SOUTH);
		p.add(p1, BorderLayout.CENTER);

		p1 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		p.add(p1, BorderLayout.SOUTH);

		JButton button = new JButton("Add Task");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (taskCreator == null)
					taskCreator = new TaskCreator(taskManager, owner);
				taskCreator.show(table, null, -1);
			}
		});
		p1.add(button);

		removeTaskButton = new JButton("Remove Task");
		removeTaskButton.setEnabled(false);
		removeTaskButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ListSelectionModel sm = table.getSelectionModel();
				if (!sm.isSelectionEmpty()) {
					int row = sm.getMinSelectionIndex();
					Task t = taskManager.getTaskByUid((String) table.getValueAt(row, uidCol));
					if (t != null) {
						taskManager.remove(t);
						taskManager.processPendingRequests();
					}
				}
			}
		});
		p1.add(removeTaskButton);

		editTaskButton = new JButton("Edit Task");
		editTaskButton.setEnabled(false);
		editTaskButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ListSelectionModel sm = table.getSelectionModel();
				if (!sm.isSelectionEmpty()) {
					int row = sm.getMinSelectionIndex();
					Task t = taskManager.getTaskByUid((String) table.getValueAt(row, uidCol));
					if (t != null && !t.isSystemTask()) {
						if (taskCreator == null)
							taskCreator = new TaskCreator(taskManager, owner);
						taskCreator.show(table, t, row);
						note.setText(t.getDescription());
						table.setValueAt(t.isEnabled(), row, 0);
						table.setValueAt(t.getUid(), row, 1);
						table.setValueAt(Integer.toString(t.getPriority()), row, 2);
						table.setValueAt(Integer.toString(t.getInterval()), row, 3);
						table.setValueAt(t.getLifetime() == Task.PERMANENT ? "Permanent" : Integer.toString(t.getLifetime()), row, 4);
						table.repaint();
					}
				}
			}
		});
		p1.add(editTaskButton);

		button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				taskManager.notifyChange();
				dialog.dispose();
			}
		});
		p1.add(button);

		panel.add(p, BorderLayout.SOUTH);

		dialog.pack();
		dialog.setLocationRelativeTo(owner);

		return dialog;

	}

}