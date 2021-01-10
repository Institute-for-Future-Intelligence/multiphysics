package org.energy2d.model;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.energy2d.util.ColorFill;
import org.energy2d.util.XmlCharacterEncoder;

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
	private float mass = 0.1f;
	private float radius = 0.04f;
	private Color color = Color.WHITE;
	private Color velocityColor = Color.BLACK;
	private float randomSpeed = 0.01f;

	public ParticleFeeder(float x, float y) {
		super(new Rectangle2D.Float());
		setCenter(x, y);
	}

	@Override
	public ParticleFeeder duplicate(float x, float y) {
		ParticleFeeder pf = new ParticleFeeder(x, y);
		pf.mass = mass;
		pf.radius = radius;
		pf.period = period;
		pf.maximum = maximum;
		pf.color = color;
		pf.velocityColor = velocityColor;
		return pf;
	}

	@Override
	public ParticleFeeder duplicate() {
		float x = 0;
		float y = 0;
		Shape s = getShape();
		if (s instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) s;
			x = r.x + 0.5f * r.width;
			y = r.y + 0.5f * r.height;
		}
		ParticleFeeder pf = new ParticleFeeder(x, y);
		pf.mass = mass;
		pf.radius = radius;
		pf.period = period;
		pf.maximum = maximum;
		pf.color = color;
		pf.velocityColor = velocityColor;
		return pf;
	}

	public void feed(Model2D model) {
		List<Particle> particles = model.getParticles();
		if (particles.size() >= maximum)
			return;
		float x = getX();
		float y = getY();
		synchronized (particles) {
			for (Particle p : particles) {
				if (p.distanceSq(x, y) <= 4 * p.radius * radius)
					return;
			}
		}
		Particle p = new Particle(x, y);
		p.setMass(mass);
		p.setRadius(radius);
		p.setVx((float) ((Math.random() - 0.5) * randomSpeed));
		p.setVy((float) ((Math.random() - 0.5) * randomSpeed));
		p.setFillPattern(new ColorFill(color));
		p.setVelocityColor(velocityColor);
		model.addParticle(p);
	}

	@Override
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

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public void setVelocityColor(Color color) {
		velocityColor = color;
	}

	public Color getVelocityColor() {
		return velocityColor;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	public float getRadius() {
		return radius;
	}

	public void setMass(float mass) {
		this.mass = mass;
	}

	public float getMass() {
		return mass;
	}

	public String toXml() {
		XmlCharacterEncoder xce = new XmlCharacterEncoder();
		String xml = "<particle_feeder";
		String uid = getUid();
		if (uid != null && !uid.trim().equals(""))
			xml += " uid=\"" + xce.encode(uid) + "\"";
		String label = getLabel();
		if (label != null && !label.trim().equals(""))
			xml += " label=\"" + xce.encode(label) + "\"";
		if (!Color.WHITE.equals(color))
			xml += " color=\"" + Integer.toHexString(0x00ffffff & color.getRGB()) + "\"";
		if (!Color.BLACK.equals(color))
			xml += " velocity_color=\"" + Integer.toHexString(0x00ffffff & velocityColor.getRGB()) + "\"";
		xml += " radius=\"" + radius + "\"";
		xml += " mass=\"" + mass + "\"";
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
