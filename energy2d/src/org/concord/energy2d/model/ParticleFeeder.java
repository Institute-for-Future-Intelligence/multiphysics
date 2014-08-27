package org.concord.energy2d.model;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Feed particles at a given rate, stop when the number of particles in the model reaches the specified maximum
 * 
 * @author Charles Xie
 * 
 */
public class ParticleFeeder extends Manipulable {

	public final static float RELATIVE_WIDTH = 0.02f;
	public final static float RELATIVE_HEIGHT = 0.02f;

	private float period = 100; // feed a particle every $period seconds
	private int maximum = 100;

	public ParticleFeeder(float x, float y) {
		super(new Rectangle2D.Float());
		setCenter(x, y);
	}

	@Override
	public ParticleFeeder duplicate(float x, float y) {
		ParticleFeeder pf = new ParticleFeeder(x, y);
		pf.period = period;
		pf.maximum = maximum;
		return pf;
	}

	public void feed(Model2D model) {
		List<Particle> particles = model.getParticles();
		if (particles.size() >= maximum)
			return;
		Particle p = new Particle(getX(), getY());
		p.setVx((float) ((Math.random() - 0.5) * 0.01));
		p.setVy((float) ((Math.random() - 0.5) * 0.01));
		model.addParticle(p);
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

	public void setPeriod(float period) {
		this.period = period;
	}

	public float getPeriod() {
		return period;
	}

	public void setMaximum(int maximum) {
		this.maximum = maximum;
	}

	public int getMaximum() {
		return maximum;
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
		xml += " maximum=\"" + maximum + "\"";
		xml += " period=\"" + period + "\"/>";
		return xml;
	}

	@Override
	public String toString() {
		return getUid() == null ? super.toString() : getUid();
	}

}
