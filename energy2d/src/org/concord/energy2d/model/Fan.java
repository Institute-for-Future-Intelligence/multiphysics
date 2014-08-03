package org.concord.energy2d.model;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

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
	public Manipulable duplicate(float x, float y) {
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

	public String toXml() {
		String xml = "<part>\n";
		if (getShape() instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) getShape();
			xml += "<rectangle";
			xml += " x=\"" + r.x + "\"";
			xml += " y=\"" + r.y + "\"";
			xml += " width=\"" + r.width + "\"";
			xml += " height=\"" + r.height + "\"/>";
		}
		if (speed != 0) {
			xml += "<speed>" + speed + "</speed>\n";
		}
		if (angle != 0) {
			xml += "<angle>" + angle + "</angle>\n";
		}
		if (getUid() != null && !getUid().trim().equals(""))
			xml += "<uid>" + getUid() + "</uid>\n";
		String label = getLabel();
		if (label != null && !label.trim().equals(""))
			xml += "<label>" + label + "</label>\n";
		if (!isVisible())
			xml += "<visible>false</visible>\n";
		if (!isDraggable())
			xml += "<draggable>false</draggable>\n";
		xml += "</part>\n";
		return xml;
	}

	@Override
	public String toString() {
		return getUid() == null ? super.toString() : getUid();
	}

}
