package org.energy2d.system;

import org.energy2d.util.XmlCharacterEncoder;

/**
 * A task contains system/user-defined code/script that runs within the system's main thread with a given frequency and period.
 * 
 * @author Charles Xie
 */

public abstract class Task {

	public static final int PERMANENT = Integer.MAX_VALUE;

	private int lifetime = PERMANENT;
	private int interval = 10;
	private String uid;
	private String description;
	private boolean enabled = true;
	private boolean completed;
	private boolean systemTask = true;
	private String script;
	private int priority = 1; // 5 = highest priority; 1 = lowest priority

	public Task() {
		uid = Long.toHexString(System.currentTimeMillis());
	}

	public Task(int interval) {
		this();
		setInterval(interval);
	}

	public Task(int interval, int lifetime) {
		this(interval);
		setLifetime(lifetime);
	}

	public Task(String uid, int interval, int lifetime) {
		this(interval, lifetime);
		setUid(uid);
	}

	public abstract void execute();

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean b) {
		enabled = b;
	}

	public void setSystemTask(boolean b) {
		systemTask = b;
	}

	public boolean isSystemTask() {
		return systemTask;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public String getScript() {
		return script;
	}

	public void setCompleted(boolean b) {
		completed = b;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}

	public void setLifetime(int lifetime) {
		this.lifetime = lifetime;
	}

	public int getLifetime() {
		return lifetime;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public int getInterval() {
		return interval;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getUid() {
		return uid;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public boolean equals(Object o) {
		if (!(o instanceof Task))
			return false;
		return ((Task) o).getUid().equals(uid);
	}

	public int hashCode() {
		return uid.hashCode();
	}

	public String toString() {
		return uid;
	}

	public String toXml() {
		String xml = "<task";
		xml += " uid=\"" + uid + "\"";
		if (!enabled)
			xml += " enabled=\"" + enabled + "\"";
		if (lifetime != PERMANENT)
			xml += " lifetime=\"" + lifetime + "\"";
		if (priority != 1)
			xml += " priority=\"" + priority + "\"";
		if (script != null)
			xml += " script=\"" + new XmlCharacterEncoder().encode(script) + "\"";
		if (description != null)
			xml += " description=\"" + new XmlCharacterEncoder().encode(description) + "\"";
		xml += " interval=\"" + interval + "\"/>";
		return xml;
	}

}