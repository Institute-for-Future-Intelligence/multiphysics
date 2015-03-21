package org.concord.energy2d.model;

import java.awt.Shape;
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

	private byte type;
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
		Heliostat f = new Heliostat(s);
		f.type = type;
		f.setLabel(getLabel());
		return f;
	}

	public void translateBy(float dx, float dy) {
		Shape s = getShape();
		if (s instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) s;
			r.x += dx;
			r.y += dy;
		}
	}

	public void setType(byte type) {
		this.type = type;
	}

	public byte getType() {
		return type;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

	public float getAngle() {
		return angle;
	}

	public static Area getShape(Rectangle2D.Float r, float angle) {
		// the positions and sizes of the circles must ensure that r is the bounding box
		Area a = new Area(new Rectangle2D.Float(r.x + r.width * 0.475f, r.y + r.height * 0.5f, r.width * 0.05f, r.height * 0.5f));
		a.add(new Area(new Ellipse2D.Float(r.x + r.width * 0.45f, r.y + r.height * 0.45f, r.width * 0.1f, r.height * 0.1f)));
		a.add(new Area(new Rectangle2D.Float(r.x, r.y + r.height * 0.475f, r.width, r.height * 0.05f)));
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
		xml += " type=\"" + type + "\"/>";
		return xml;
	}

	@Override
	public String toString() {
		return getUid() == null ? super.toString() : getUid();
	}

}
