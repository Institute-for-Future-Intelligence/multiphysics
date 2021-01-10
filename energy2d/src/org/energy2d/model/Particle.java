package org.energy2d.model;

import java.awt.Color;
import java.awt.geom.Ellipse2D;

import org.energy2d.util.ColorFill;
import org.energy2d.util.FillPattern;
import org.energy2d.util.Texture;
import org.energy2d.util.XmlCharacterEncoder;

/**
 * A particle in the Lagrangian frame.
 * 
 * @author Charles Xie
 * 
 */
public class Particle extends Manipulable implements Discrete {

	float mass = 0.1f;
	float radius = 0.04f;
	float rx, ry;
	float vx, vy;
	float ax, ay;
	float fx, fy;
	float theta;
	float omega;
	float alpha;
	float temperature = Float.NaN;
	boolean movable = true;

	private float rx0 = Float.NaN, ry0 = Float.NaN;
	private float vx0 = Float.NaN, vy0 = Float.NaN;
	private float theta0 = Float.NaN, omega0 = Float.NaN;
	private FillPattern fillPattern;
	private Color velocityColor = Color.BLACK;

	public Particle() {
		super(new Ellipse2D.Float());
		fillPattern = new ColorFill(Color.WHITE);
	}

	public Particle(float rx, float ry) {
		this();
		setLocation(rx, ry);
	}

	private void updateShape() {
		Ellipse2D.Float e = (Ellipse2D.Float) getShape();
		e.x = rx - radius;
		e.y = ry - radius;
		e.width = e.height = radius * 2;
	}

	@Override
	public Particle duplicate(float x, float y) {
		Particle p = new Particle(x, y);
		p.fillPattern = fillPattern;
		p.velocityColor = velocityColor;
		p.mass = mass;
		p.radius = radius;
		p.vx = vx;
		p.vy = vy;
		p.temperature = temperature;
		p.updateShape();
		return p;
	}

	@Override
	public Particle duplicate() {
		Particle p = new Particle(rx, ry);
		p.fillPattern = fillPattern;
		p.velocityColor = velocityColor;
		p.mass = mass;
		p.radius = radius;
		p.vx = vx;
		p.vy = vy;
		p.temperature = temperature;
		p.updateShape();
		return p;
	}

	public float distanceSq(float x, float y) {
		float dx = rx - x;
		float dy = ry - y;
		return dx * dx + dy * dy;
	}

	public void setLocation(float rx, float ry) {
		this.rx = rx;
		this.ry = ry;
		updateShape();
	}

	public void storeState() {
		rx0 = rx;
		ry0 = ry;
		vx0 = vx;
		vy0 = vy;
		theta0 = theta;
		omega0 = omega;
	}

	public boolean restoreState() {
		if (Float.isNaN(rx0)) {
			return false;
		} else {
			rx = rx0;
		}
		if (Float.isNaN(ry0)) {
			return false;
		} else {
			ry = ry0;
		}
		if (Float.isNaN(vx0)) {
			return false;
		} else {
			vx = vx0;
		}
		if (Float.isNaN(vy0)) {
			return false;
		} else {
			vy = vy0;
		}
		if (Float.isNaN(theta0)) {
			return false;
		} else {
			theta = theta0;
		}
		if (Float.isNaN(omega0)) {
			return false;
		} else {
			omega = omega0;
		}
		ax = 0;
		ay = 0;
		fx = 0;
		fy = 0;
		updateShape();
		return true;
	}

	@Override
	public void translateBy(float deltaX, float deltaY) {
		rx += deltaX;
		ry += deltaY;
		updateShape();
	}

	// predict this particle's new position using second order Taylor expansion.
	void predict(float dt) {
		if (!movable)
			return;
		float dt2 = 0.5f * dt * dt;
		rx += vx * dt + ax * dt2;
		ry += vy * dt + ay * dt2;
		vx += ax * dt;
		vy += ay * dt;
		theta += omega * dt + alpha * dt2;
		omega += alpha * dt;
		theta %= Math.PI * 2;
	}

	// correct this particle's position predicted by the predict method.
	// fx and fy were used in the force calculation routine to store the new acceleration data.
	// ax and ay were used to hold the old acceleration data before calling this method.
	// After calling this method, new acceleration data will be assigned to ax and ay, whereas the forces to fx and fy.
	void correct(float dt) {
		if (!movable)
			return;
		vx += 0.5f * dt * (fx - ax);
		vy += 0.5f * dt * (fy - ay);
		ax = fx;
		ay = fy;
		fx *= mass;
		fy *= mass;
		updateShape();
		// TODO: theta and omega
	}

	public float getSpeed() {
		return (float) Math.hypot(vx, vy);
	}

	public void setVelocityAngle(float angle) {
		float c = getSpeed();
		vx = (float) (Math.cos(angle) * c);
		vy = (float) (Math.sin(angle) * c);
	}

	public void setRx(float rx) {
		this.rx = rx;
		updateShape();
	}

	public float getRx() {
		return rx;
	}

	public void setRy(float ry) {
		this.ry = ry;
		updateShape();
	}

	public float getRy() {
		return ry;
	}

	public void setVx(float vx) {
		this.vx = vx;
	}

	public float getVx() {
		return vx;
	}

	public void setVy(float vy) {
		this.vy = vy;
	}

	public float getVy() {
		return vy;
	}

	public float getAx() {
		return ax;
	}

	public float getAy() {
		return ay;
	}

	public void setTheta(float theta) {
		this.theta = theta;
	}

	public float getTheta() {
		return theta;
	}

	public void setOmega(float omega) {
		this.omega = omega;
	}

	public float getOmega() {
		return omega;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	public float getAlpha() {
		return alpha;
	}

	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}

	public float getTemperature() {
		return temperature;
	}

	public void setRadius(float radius) {
		this.radius = radius;
		updateShape();
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

	public void setMovable(boolean movable) {
		this.movable = movable;
	}

	public boolean isMovable() {
		return movable;
	}

	public void setFillPattern(FillPattern fillPattern) {
		this.fillPattern = fillPattern;
	}

	public FillPattern getFillPattern() {
		return fillPattern;
	}

	public void setVelocityColor(Color color) {
		velocityColor = color;
	}

	public Color getVelocityColor() {
		return velocityColor;
	}

	public String toXml() {
		XmlCharacterEncoder xce = new XmlCharacterEncoder();
		String xml = "<particle>\n";
		xml += "<rx>" + rx + "</rx>\n";
		xml += "<ry>" + ry + "</ry>\n";
		if (vx != 0)
			xml += "<vx>" + vx + "</vx>\n";
		if (vy != 0)
			xml += "<vy>" + vy + "</vy>\n";
		if (theta != 0)
			xml += "<theta>" + theta + "</theta>\n";
		if (omega != 0)
			xml += "<omega>" + omega + "</omega>\n";
		xml += "<radius>" + radius + "</radius>\n";
		xml += "<mass>" + mass + "</mass>\n";
		String uid = getUid();
		if (uid != null && !uid.trim().equals(""))
			xml += "<uid>" + xce.encode(uid) + "</uid>\n";
		String label = getLabel();
		if (label != null && !label.trim().equals(""))
			xml += "<label>" + xce.encode(label) + "</label>\n";
		if (fillPattern instanceof ColorFill) {
			Color color = ((ColorFill) fillPattern).getColor();
			if (!color.equals(Color.WHITE)) {
				xml += "<color>" + Integer.toHexString(0x00ffffff & color.getRGB()) + "</color>\n";
			}
		} else if (fillPattern instanceof Texture) {
			Texture pf = (Texture) fillPattern;
			xml += "<texture>";
			int i = pf.getForeground();
			xml += "<texture_fg>" + Integer.toString(i, 16) + "</texture_fg>\n";
			i = pf.getBackground();
			xml += "<texture_bg>" + Integer.toString(i, 16) + "</texture_bg>\n";
			i = ((Texture) fillPattern).getStyle();
			xml += "<texture_style>" + i + "</texture_style>\n";
			i = pf.getCellWidth();
			xml += "<texture_width>" + i + "</texture_width>\n";
			i = pf.getCellHeight();
			xml += "<texture_height>" + i + "</texture_height>\n";
			xml += "</texture>\n";
		}
		if (!Color.BLACK.equals(velocityColor))
			xml += "<velocity_color>" + Integer.toHexString(0x00ffffff & velocityColor.getRGB()) + "</velocity_color>\n";
		if (!Float.isNaN(temperature))
			xml += "<temperature>" + temperature + "</temperature>\n";
		if (!isDraggable())
			xml += "<draggable>false</draggable>\n";
		if (!isMovable())
			xml += "<movable>false</movable>\n";
		xml += "</particle>\n";
		return xml;
	}

}
