package org.energy2d.model;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.energy2d.event.MeasurementEvent;
import org.energy2d.event.MeasurementListener;

/**
 * @author Charles Xie
 * 
 */
public abstract class Sensor extends Manipulable {

	public final static byte ONE_POINT = 1;
	public final static byte FIVE_POINT = 5;
	public final static byte NINE_POINT = 9;

	byte stencil = ONE_POINT;

	private static int maximumDataPoints = 1000;
	List<TimedData> data;
	private List<MeasurementListener> listeners;

	String attachID;
	private float sensingSpotX, sensingSpotY;

	public Sensor(Shape shape) {
		super(shape);
		data = Collections.synchronizedList(new ArrayList<TimedData>());
		listeners = new ArrayList<MeasurementListener>();
	}

	public static void setMaximumDataPoints(int n) {
		maximumDataPoints = n;
	}

	public static int getMaximumDataPoints() {
		return maximumDataPoints;
	}

	public void setCenter(float x, float y) {
		if (getShape() instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) getShape();
			r.x = x - 0.5f * r.width;
			r.y = y - 0.5f * r.height;
		} else {
			// TODO: none-rectangular shape
		}
	}

	@Override
	public void translateBy(float dx, float dy) {
		if (getShape() instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) getShape();
			r.x += dx;
			r.y += dy;
		} else {
			// TODO: none-rectangular shape
		}
	}

	public void setX(float x) {
		if (getShape() instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) getShape();
			r.x = x - 0.5f * r.width;
		} else {
			// TODO: none-rectangular shape
		}
	}

	public void setY(float y) {
		if (getShape() instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) getShape();
			r.y = y - 0.5f * r.height;
		} else {
			// TODO: none-rectangular shape
		}
	}

	/** returns the x coordinate of the center */
	public float getX() {
		if (getShape() instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) getShape();
			return r.x + 0.5f * r.width;
		}
		return (float) getShape().getBounds2D().getCenterX();
	}

	/** returns the y coordinate of the center */
	public float getY() {
		if (getShape() instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) getShape();
			return r.y + 0.5f * r.height;
		}
		return (float) getShape().getBounds2D().getCenterY();
	}

	public void setSensingSpotX(float sensingSpotX) {
		this.sensingSpotX = sensingSpotX;
	}

	public float getSensingSpotX() {
		return sensingSpotX;
	}

	public void setSensingSpotY(float sensingSpotY) {
		this.sensingSpotY = sensingSpotY;
	}

	public float getSensingSpotY() {
		return sensingSpotY;
	}

	public void setAttachID(String attachID) {
		this.attachID = attachID;
	}

	public String getAttachID() {
		return attachID;
	}

	public void setStencil(byte stencil) {
		this.stencil = stencil;
	}

	public byte getStencil() {
		return stencil;
	}

	public void addMeasurementListener(MeasurementListener l) {
		if (!listeners.contains(l))
			listeners.add(l);
	}

	public void removeMeasurementListener(MeasurementListener l) {
		listeners.remove(l);
	}

	private void notifyMeasurementListeners() {
		if (listeners.isEmpty())
			return;
		MeasurementEvent e = new MeasurementEvent(this);
		for (MeasurementListener x : listeners)
			x.measurementTaken(e);
	}

	public void clear() {
		data.clear();
		notifyMeasurementListeners();
	}

	public List<TimedData> getData() {
		return data;
	}

	public float getCurrentData() {
		if (data.isEmpty())
			return Float.NaN;
		return data.get(data.size() - 1).getValue();
	}

	public float getDataMinimum() {
		if (data.isEmpty())
			return Float.NaN;
		float min = Float.MAX_VALUE;
		synchronized (data) {
			for (TimedData d : data) {
				if (min > d.getValue())
					min = d.getValue();
			}
		}
		return min;
	}

	public float getDataMaximum() {
		if (data.isEmpty())
			return Float.NaN;
		float max = -Float.MAX_VALUE;
		synchronized (data) {
			for (TimedData d : data) {
				if (max < d.getValue())
					max = d.getValue();
			}
		}
		return max;
	}

	public void addData(float time, float x) {
		data.add(new TimedData(time, x));
		notifyMeasurementListeners();
		if (data.size() > maximumDataPoints)
			data.remove(0);
	}

	public abstract String toXml();

	public abstract String getName();

}
