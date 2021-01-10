package org.energy2d.model;

import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import org.energy2d.util.XmlCharacterEncoder;

/**
 * A fan that increases the speed of fluid flowing through it.
 * 
 * @author Charles Xie
 * 
 */
public class Fan extends Manipulable {

	private float speed;
	private float angle;

	public Fan(Shape s) {
		super(s);
	}

	@Override
	public Fan duplicate(float x, float y) {
		Shape s = getShape();
		if (s instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) s;
			s = new Rectangle2D.Float(x - 0.5f * r.width, y - 0.5f * r.height, r.width, r.height);
		}
		Fan f = new Fan(s);
		f.angle = angle;
		f.speed = speed;
		f.setLabel(getLabel());
		return f;
	}

	@Override
	public Fan duplicate() {
		Shape s = getShape();
		if (s instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) s;
			s = new Rectangle2D.Float(r.x, r.y, r.width, r.height);
		}
		Fan f = new Fan(s);
		f.angle = angle;
		f.speed = speed;
		f.setLabel(getLabel());
		return f;
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

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public float getSpeed() {
		return speed;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

	public float getAngle() {
		return angle;
	}

	public static Area getShape(Rectangle2D.Float r, float speed, float angle, float delta) {
		if (r.height > r.width) {
			float d1 = 0.5f * r.height * delta;
			float d2 = d1 * 2;
			float deg = (float) (Math.toDegrees(0.5 * Math.asin(r.height / Math.hypot(r.width, r.height))));
			Area a = new Area(new Arc2D.Float(r.x + r.width / 4, r.y + d1, r.width / 2, r.height - d2, deg, 180 - 2 * deg, Arc2D.PIE));
			a.add(new Area(new Arc2D.Float(r.x + r.width / 4, r.y + d1, r.width / 2, r.height - d2, -deg, 2 * deg - 180, Arc2D.PIE)));
			a.add(new Area(new Rectangle2D.Float(speed * Math.cos(angle) >= 0 ? r.x : r.x + r.width * 0.5f, r.y + r.height * (0.5f - 0.025f), r.width * 0.5f, 0.05f * r.height)));
			return a;
		}
		float d1 = 0.5f * r.width * delta;
		float d2 = d1 * 2;
		float deg = (float) (Math.toDegrees(0.5 * Math.asin(r.width / Math.hypot(r.width, r.height))));
		Area a = new Area(new Arc2D.Float(r.x + d1, r.y + r.height / 4, r.width - d2, r.height / 2, deg, -2 * deg, Arc2D.PIE));
		a.add(new Area(new Arc2D.Float(r.x + d1, r.y + r.height / 4, r.width - d2, r.height / 2, 180 - deg, 2 * deg, Arc2D.PIE)));
		a.add(new Area(new Rectangle2D.Float(r.x + r.width * (0.5f - 0.025f), speed * Math.sin(angle) > 0 ? r.y : r.y + r.height * 0.5f, 0.05f * r.width, r.height * 0.5f)));
		return a;
	}

	public String toXml() {
		XmlCharacterEncoder xce = new XmlCharacterEncoder();
		String xml = "<fan";
		String uid = getUid();
		if (uid != null && !uid.trim().equals(""))
			xml += " uid=\"" + xce.encode(uid) + "\"";
		String label = getLabel();
		if (label != null && !label.trim().equals(""))
			xml += " label=\"" + xce.encode(label) + "\"";
		if (getShape() instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) getShape();
			xml += " x=\"" + r.x + "\"";
			xml += " y=\"" + r.y + "\"";
			xml += " width=\"" + r.width + "\"";
			xml += " height=\"" + r.height + "\"";
		}
		xml += " speed=\"" + speed + "\"";
		xml += " angle=\"" + angle + "\"/>";
		return xml;
	}

	@Override
	public String toString() {
		return getUid() == null ? super.toString() : getUid();
	}

}
