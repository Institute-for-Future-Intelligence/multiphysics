package org.concord.energy2d.model;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/**
 * Feed particles at a given rate
 * 
 * @author Charles Xie
 * 
 */
public class ParticleFeeder extends Manipulable {

	public final static float RELATIVE_WIDTH = 0.05f;
	public final static float RELATIVE_HEIGHT = 0.05f;

	private float rate;

	public ParticleFeeder(float x, float y) {
		super(new Rectangle2D.Float());
		setCenter(x, y);
	}

	@Override
	public ParticleFeeder duplicate(float x, float y) {
		return new ParticleFeeder(x, y);
	}

	public void feed(Model2D model) {

	}

	public void translateBy(float dx, float dy) {
		Shape s = getShape();
		if (s instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) s;
			r.x += dx;
			r.y += dy;
		}
	}

	public void setCenter(float x, float y) {
		Shape s = getShape();
		if (s instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) s;
			r.x = x - 0.5f * r.width;
			r.y = y - 0.5f * r.height;
		}
	}

	public void setX(float x) {
		Shape s = getShape();
		if (s instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) s;
			r.x = x - 0.5f * r.width;
		}
	}

	public void setY(float y) {
		Shape s = getShape();
		if (s instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) s;
			r.y = y - 0.5f * r.height;
		}
	}

	/** returns the x coordinate of the center */
	public float getX() {
		Shape s = getShape();
		if (s instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) s;
			return r.x + 0.5f * r.width;
		}
		return (float) s.getBounds2D().getCenterX();
	}

	/** returns the y coordinate of the center */
	public float getY() {
		Shape s = getShape();
		if (s instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) s;
			return r.y + 0.5f * r.height;
		}
		return (float) s.getBounds2D().getCenterY();
	}

	public void setRate(float rate) {
		this.rate = rate;
	}

	public float getRate() {
		return rate;
	}

	public String toXml() {
		String xml = "<particle_feeder";
		String uid = getUid();
		if (uid != null && !uid.trim().equals(""))
			xml += " uid=\"" + uid + "\"";
		String label = getLabel();
		if (label != null && !label.trim().equals(""))
			xml += " label=\"" + label + "\"";
		xml += " x=\"" + getX() + "\"";
		xml += " y=\"" + getY() + "\"";
		xml += " rate=\"" + rate + "\"/>";
		return xml;
	}

	@Override
	public String toString() {
		return getUid() == null ? super.toString() : getUid();
	}

}
