package org.energy2d.system;

import java.awt.EventQueue;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/* 
 * @author Charles Xie
 */

public abstract class TaskManager {

	List<Task> taskPool;
	private List<Task> tasksToRemove, tasksToAdd;
	private TaskManagerView view;

	public TaskManager() {
		taskPool = Collections.synchronizedList(new ArrayList<Task>());
		tasksToAdd = Collections.synchronizedList(new ArrayList<Task>());
		tasksToRemove = Collections.synchronizedList(new ArrayList<Task>());
	}

	/** get the running index of step from the system it manages */
	public abstract int getIndexOfStep();

	/** send the script to the system it manages for execution. */
	public abstract void runScript(String script);

	/** notify the system it manages that the tasks have changed. */
	public abstract void notifyChange();

	private void createView() {
		if (view == null)
			view = new TaskManagerView(this);
	}

	/** remove all custom tasks */
	public void clearCustomTasks() {
		synchronized (taskPool) {
			Iterator<Task> i = taskPool.iterator();
			while (i.hasNext()) {
				Task t = i.next();
				if (!t.isSystemTask())
					i.remove();
			}
		}
	}

	/** get all customer tasks */
	public List<Task> getCustomTasks() {
		List<Task> list = new ArrayList<Task>();
		synchronized (taskPool) {
			for (Task t : taskPool) {
				if (!t.isSystemTask())
					list.add(t);
			}
		}
		return list;
	}

	/** add a task to the task pool. */
	public void add(Task t) {
		if (t != null) {
			if (tasksToAdd.contains(t))
				return;
			tasksToAdd.add(t);
		}
	}

	/** remove a task from the task pool. */
	public void remove(Task t) {
		if (t != null) {
			if (tasksToRemove.contains(t))
				return;
			tasksToRemove.add(t);
		}
	}

	/** return true if the task pool contains the specified task */
	public boolean contains(Task t) {
		return taskPool.contains(t);
	}

	public boolean toBeAdded(Task t) {
		return tasksToAdd.contains(t);
	}

	public boolean hasTaskToAdd() {
		return !tasksToAdd.isEmpty();
	}

	public boolean toBeRemoved(Task t) {
		return tasksToRemove.contains(t);
	}

	public boolean hasTaskToRemove() {
		return !tasksToRemove.isEmpty();
	}

	/** return a task by UID. */
	public Task getTaskByUid(String uid) {
		synchronized (taskPool) {
			for (Task t : taskPool) {
				if (t.getUid().equals(uid))
					return t;
			}
		}
		return null;
	}

	/** execute the tasks in the pool (the order has been sorted according to the priorities). */
	public void execute() {
		processPendingRequests();
		try { // it probably won't hurt much not to synchronize this iterator
			for (Task task : taskPool) {
				if (task.isEnabled() && task.getInterval() > 0) {
					if (getIndexOfStep() > task.getLifetime())
						task.setCompleted(true);
					if (task.isCompleted()) {
						remove(task);
					}
					if (getIndexOfStep() % task.getInterval() == 0) {
						task.execute();
					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void processPendingRequests() {
		if (!tasksToAdd.isEmpty()) {
			synchronized (tasksToAdd) {
				for (Task t : tasksToAdd)
					addTask(t);
			}
			tasksToAdd.clear();
		}
		if (!tasksToRemove.isEmpty()) {
			synchronized (tasksToRemove) {
				for (Task t : tasksToRemove)
					removeTask(t);
			}
			tasksToRemove.clear();
		}
	}

	private void removeTask(final Task t) {
		if (!contains(t))
			return;
		taskPool.remove(t);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				createView();
				view.removeRow(t);
			}
		});
	}

	// add a task and place it in the pool according to the priority setting
	private void addTask(final Task t) {
		if (contains(t))
			return;
		if (taskPool.isEmpty()) {
			taskPool.add(t);
		} else {
			synchronized (taskPool) {
				int m = -1;
				int n = taskPool.size();
				for (int i = 0; i < n; i++) {
					if (t.getPriority() > taskPool.get(i).getPriority()) {
						m = i;
						break;
					}
				}
				if (m == -1) {
					taskPool.add(t);
				} else {
					taskPool.add(m, t);
				}
			}
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				createView();
				view.insertRow(t);
			}
		});
	}

	public void show(final Window owner) {
		if (EventQueue.isDispatchThread()) {
			createView();
			view.show(owner);
		} else {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					createView();
					view.show(owner);
				}
			});
		}
	}

	public String toString() {
		return taskPool.toString();
	}

}