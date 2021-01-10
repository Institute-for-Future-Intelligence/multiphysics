package org.energy2d.model;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.energy2d.util.XmlCharacterEncoder;

/**
 * A heliostat is a device that includes a plane mirror that turns so as to keep reflecting sunlight toward a predetermined target.
 * 
 * @author Charles Xie
 * 
 */
public class Heliostat extends Manipulable {

	public final static byte MIRROR = 0;
	public final static byte PHOTOVOLTAIC = 1;

	private byte type = MIRROR;
	private Part target;
	private float angle;
	private Model2D model;

	public Heliostat(Shape s, Model2D model) {
		super(s);
		this.model = model;
	}

	@Override
	public Heliostat duplicate(float x, float y) {
		Shape s = getShape();
		if (s instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) s;
			s = new Rectangle2D.Float(x - 0.5f * r.width, y - 0.5f * r.height, r.width, r.height);
		}
		Heliostat h = new Heliostat(s, model);
		h.target = target;
		h.setAngle();
		h.setLabel(getLabel());
		return h;
	}

	@Override
	public Heliostat duplicate() {
		Shape s = getShape();
		if (s instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) s;
			s = new Rectangle2D.Float(r.x, r.y, r.width, r.height);
		}
		Heliostat h = new Heliostat(s, model);
		h.target = target;
		h.setAngle();
		h.setLabel(getLabel());
		return h;
	}

	@Override
	public void translateBy(float dx, float dy) {
		Shape s = getShape();
		if (s instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) s;
			r.x += dx;
			r.y += dy;
		}
		if (target != null)
			setAngle();
	}

	public void setType(byte type) {
		this.type = type;
	}

	public byte getType() {
		return type;
	}

	public void setTarget(Part target) {
		this.target = target;
	}

	public Part getTarget() {
		return target;
	}

	public void setAngle() {
		float theta;
		if (target != null) {
			Point2D.Float c1 = target.getCenter();
			Point2D.Float c2 = getCenter();
			float dx = c1.x - c2.x;
			float dy = c1.y - c2.y;
			theta = 0.5f * ((float) Math.acos(dx / Math.hypot(dx, dy)) + model.getSunAngle());
		} else {
			theta = model.getSunAngle();
		}
		angle = (float) Math.PI * 0.5f - theta;
	}

	public float getAngle() {
		return angle;
	}

	public static Area getShape(Rectangle2D.Float r, float angle) {
		// the positions and sizes of the circles must ensure that r is the bounding box
		Area a = new Area(new Rectangle2D.Float(r.x + r.width * 0.45f, r.y + r.height * 0.5f, r.width * 0.1f, r.height * 0.5f));
		Area mirror = new Area(new Rectangle2D.Float(r.x, r.y + r.height * 0.45f, r.width, r.height * 0.1f));
		mirror.add(new Area(new Rectangle2D.Float(r.x + 0.3f * r.width, r.y + r.height * 0.54f, r.width * 0.4f, r.height * 0.05f)));
		mirror.transform(AffineTransform.getRotateInstance(angle, r.x + r.width * 0.5, r.y + r.height * 0.5));
		a.add(mirror);
		return a;
	}

	public boolean reflect(Photon p) {
		Rectangle2D.Float shape = (Rectangle2D.Float) getShape();
		float lenx = (float) (0.5 * shape.width * Math.cos(angle));
		float leny = (float) (0.5 * shape.width * Math.sin(angle));
		float cenx = 0.5f * shape.width + shape.x;
		float ceny = 0.5f * shape.height + shape.y;
		Line2D.Float line = new Line2D.Float(cenx - lenx, ceny - leny, cenx + lenx, ceny + leny);
		float dt = model.getTimeStep();
		float predictedX = p.getRx() + p.getVx() * dt;
		float predictedY = p.getRy() + p.getVy() * dt;
		boolean hit = line.intersectsLine(p.getRx(), p.getRy(), predictedX, predictedY);
		if (hit) {
			float d12 = (float) (1.0 / Math.hypot(line.x1 - line.x2, line.y1 - line.y2));
			float sin = (line.y2 - line.y1) * d12;
			float cos = (line.x2 - line.x1) * d12;
			float u = p.getVx() * cos + p.getVy() * sin; // velocity component parallel to the line
			float w = p.getVy() * cos - p.getVx() * sin; // velocity component perpendicular to the line
			p.setVx(u * cos + w * sin);
			p.setVy(u * sin - w * cos);
			return true;
		}
		return false;
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
		if (type != MIRROR)
			xml += " type=\"" + type + "\"";
		if (getShape() instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) getShape();
			xml += " x=\"" + r.x + "\"";
			xml += " y=\"" + r.y + "\"";
			xml += " width=\"" + r.width + "\"";
			xml += " height=\"" + r.height + "\"";
		}
		if (target != null && target.getUid() != null && !target.getUid().trim().equals(""))
			xml += " target=\"" + target.getUid() + "\"";
		xml += "/>";
		return xml;
	}

	@Override
	public String toString() {
		return getUid() == null ? super.toString() : getUid();
	}

}
