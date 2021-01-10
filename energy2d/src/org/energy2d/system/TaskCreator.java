package org.energy2d.system;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * @author Charles Xie
 * 
 */
class TaskCreator {

	private final static byte UID_OK = 0;
	private final static byte UID_ERROR = 1;
	private final static byte UID_EXISTS = 2;
	private final static byte INPUT_ERROR = 3;

	private TaskManager taskManager;
	private Task task;

	private JPanel contentPane;
	private JTextArea scriptArea;
	private JTextField uidField, descriptionField;
	private JTextField intervalField, lifetimeField;
	private JCheckBox permanentCheckBox;
	private JLabel lifetimeLabel;
	private JSpinner prioritySpinner;
	private JDialog dialog;
	private JTable table;
	private int row;
	private Window owner;

	TaskCreator(TaskManager t, Window w) {

		taskManager = t;
		owner = w;

		contentPane = new JPanel(new BorderLayout(5, 5));

		scriptArea = new JTextArea();
		scriptArea.setBorder(BorderFactory.createTitledBorder("Scripts:"));
		JScrollPane scroller = new JScrollPane(scriptArea);
		scroller.setPreferredSize(new Dimension(600, 400));
		contentPane.add(scroller, BorderLayout.CENTER);

		JPanel topPanel = new JPanel(new BorderLayout());
		contentPane.add(topPanel, BorderLayout.NORTH);

		JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		topPanel.add(p1, BorderLayout.CENTER);

		p1.add(new JLabel("UID: "));
		uidField = new JTextField("Untitled");
		uidField.setColumns(10);
		p1.add(uidField);

		p1.add(new JLabel("Description: "));
		descriptionField = new JTextField();
		descriptionField.setColumns(50);
		p1.add(descriptionField);

		final JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
		topPanel.add(p2, BorderLayout.SOUTH);

		p2.add(new JLabel("Priority: "));
		prioritySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
		p2.add(prioritySpinner);

		p2.add(new JLabel("Interval: "));
		intervalField = new JTextField(10);
		p2.add(intervalField);

		permanentCheckBox = new JCheckBox("Permanent");
		permanentCheckBox.setSelected(true);
		permanentCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					p2.remove(lifetimeLabel);
					p2.remove(lifetimeField);
				} else {
					p2.add(lifetimeLabel);
					p2.add(lifetimeField);
					lifetimeField.setText(Integer.toString(100000));
				}
				p2.validate();
				p2.repaint();
			}
		});
		p2.add(permanentCheckBox);

		lifetimeLabel = new JLabel("Lifetime: ");
		lifetimeField = new JTextField();

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		JButton button = new JButton("OK");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switch (check()) {
				case UID_OK:
					taskManager.notifyChange();
					dialog.dispose();
					break;
				case UID_EXISTS:
					JOptionPane.showMessageDialog(dialog, "A task with the UID \"" + uidField.getText() + "\" already exists.", "Duplicate Task UID", JOptionPane.ERROR_MESSAGE);
					break;
				case UID_ERROR:
					JOptionPane.showMessageDialog(dialog, "A task UID must contain at least four characters in [a-zA-Z_0-9] (no space allowed): \"" + uidField.getText() + "\".", "Task UID Error", JOptionPane.ERROR_MESSAGE);
					break;
				}
			}
		});
		buttonPanel.add(button);

		button = new JButton("Cancel");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
		buttonPanel.add(button);

	}

	private byte check() {

		String uid = uidField.getText();
		if (!uid.matches("\\w{4,}"))
			return UID_ERROR;

		if (task == null) {
			if (taskManager.getTaskByUid(uid) != null)
				return UID_EXISTS;
			float x = parse(intervalField.getText());
			if (Float.isNaN(x))
				return INPUT_ERROR;
			Task t = new Task((int) x) {
				public void execute() {
					taskManager.runScript(getScript());
					if (taskManager.getIndexOfStep() >= getLifetime()) {
						setCompleted(true);
					}
				}
			};
			t.setUid(uid);
			t.setSystemTask(false);
			t.setPriority((Integer) prioritySpinner.getValue());
			x = parse(lifetimeField.getText());
			if (Float.isNaN(x))
				return INPUT_ERROR;
			t.setLifetime(permanentCheckBox.isSelected() ? Task.PERMANENT : (int) x);
			t.setDescription(descriptionField.getText());
			t.setScript(scriptArea.getText());
			taskManager.add(t);
			taskManager.processPendingRequests();
		} else {
			task.setPriority((Integer) prioritySpinner.getValue());
			float x = parse(intervalField.getText());
			if (Float.isNaN(x))
				return INPUT_ERROR;
			task.setInterval((int) x);
			x = parse(lifetimeField.getText());
			if (Float.isNaN(x))
				return INPUT_ERROR;
			task.setLifetime(permanentCheckBox.isSelected() ? Task.PERMANENT : (int) x);
			task.setDescription(descriptionField.getText());
			task.setScript(scriptArea.getText());
			if (!task.getUid().equals(uid)) {
				task.setUid(uid);
				table.setValueAt(uid, row, 1);
			}
		}
		return UID_OK;

	}

	void show(JTable table, Task task, int row) {
		this.table = table;
		this.task = task;
		this.row = row;
		if (dialog == null) {
			dialog = new JDialog(JOptionPane.getFrameForComponent(table), "Creating a Task", true);
			dialog.setContentPane(contentPane);
			dialog.pack();
			dialog.setLocationRelativeTo(table);
		}
		if (task != null) {
			dialog.setTitle("Edit a Task");
			uidField.setText(task.getUid());
			descriptionField.setText(task.getDescription());
			scriptArea.setText(task.getScript());
			scriptArea.setCaretPosition(0);
			prioritySpinner.setValue(task.getPriority());
			permanentCheckBox.setSelected(task.getLifetime() == Task.PERMANENT);
			lifetimeField.setText(Integer.toString(task.getLifetime()));
			intervalField.setText(Integer.toString(task.getInterval()));
		} else {
			dialog.setTitle("Create a Task");
			uidField.setText("Untitled");
			descriptionField.setText(null);
			scriptArea.setText(null);
			prioritySpinner.setValue(1);
			lifetimeField.setText(Integer.toString(Task.PERMANENT));
			intervalField.setText(Integer.toString(10));
			permanentCheckBox.setSelected(true);
		}
		dialog.setVisible(true);
	}

	private float parse(String s) {
		float x = Float.NaN;
		try {
			x = Float.parseFloat(s);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(owner, "Cannot parse " + e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
		return x;
	}

}
