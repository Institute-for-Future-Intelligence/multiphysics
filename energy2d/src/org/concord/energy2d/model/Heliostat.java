package org.concord.energy2d.model;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.concord.energy2d.util.XmlCharacterEncoder;

/**
 * A heliostat is a device that includes a plane mirror that turns so as to keep reflecting sunlight toward a predetermined target.
 * 
 * @author Charles Xie
 * 
 */
public class Heliostat extends Manipulable {

	private Part target;
	private float angle;

	public Heliostat(Shape s) {
		super(s);
	}

	@Override
	public Heliostat duplicate(float x, float y) {
		Shape s = getShape();
		if (s instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) s;
			s = new Rectangle2D.Float(x - 0.5f * r.width, y - 0.5f * r.height, r.width, r.height);
		}
		Heliostat h = new Heliostat(s);
		h.target = target;
		h.setLabel(getLabel());
		return h;
	}

	public void translateBy(float dx, float dy) {
		Shape s = getShape();
		if (s instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) s;
			r.x += dx;
			r.y += dy;
		}
	}

	public void setTarget(Part target) {
		this.target = target;
	}

	public Part getTarget() {
		return target;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

	public float getAngle() {
		return angle;
	}

	public static Area getShape(Rectangle2D.Float r, float angle) {
		// the positions and sizes of the circles must ensure that r is the bounding box
		Area a = new Area(new Rectangle2D.Float(r.x + r.width * 0.45f, r.y + r.height * 0.5f, r.width * 0.1f, r.height * 0.5f));
		Area bearing = new Area(new Ellipse2D.Float(r.x + r.width * 0.45f, r.y + r.height * 0.45f, r.width * 0.1f, r.height * 0.1f));
		a.add(bearing);
		Area mirror = new Area(new Rectangle2D.Float(r.x, r.y + r.height * 0.475f, r.width, r.height * 0.05f));
		mirror.add(new Area(new Rectangle2D.Float(r.x + 0.4f * r.width, r.y + r.height * 0.5f, r.width * 0.2f, r.height * 0.05f)));
		mirror.transform(AffineTransform.getRotateInstance(angle - Math.PI * 0.5, r.x + r.width * 0.5, r.y + r.height * 0.5));
		a.add(mirror);
		return a;
	}

	public String toXml() {
		XmlCharacterEncoder xce = new XmlCharacterEncoder();
		String xml = "<heliostat";
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
		if (target != null)
			xml += " target=\"" + target.getUid() + "\"";
		xml += "/>";
		return xml;
	}

	@Override
	public String toString() {
		return getUid() == null ? super.toString() : getUid();
	}

}
