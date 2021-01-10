package org.energy2d.model;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.energy2d.util.XmlCharacterEncoder;

/**
 * Clouds (three circles intersected within a rectangle) are expensive to calculate. So we use additional variables (x, y) for setting locations and avoiding recalculation of shapes when moving them.
 * 
 * @author Charles Xie
 * 
 */
public class Cloud extends Manipulable {

	private float x; // the x coordinate of the upper-left corner
	private float y; // the y coordinate of the upper-left corner
	private float speed; // clouds only move in the horizontal direction
	private Color color = Color.WHITE;
	private Rectangle2D.Float boundingBox;

	/** Construct a cloud based on the specified bounding box, which must start from (0, 0). If not, the (x, y) will be ignored. */
	public Cloud(Shape bb) {
		super(bb);
		if (!(bb instanceof Rectangle2D.Float))
			throw new IllegalArgumentException("Shape must be a Rectangle2D.Float");
		Rectangle2D.Float r = (Rectangle2D.Float) bb;
		setDimension(r.width, r.height);
	}

	// the size and shape of a cloud are determined by its bounding box that cuts three circles
	public static Area getShape(Rectangle2D.Float r) {
		// the positions and sizes of the circles must ensure that r is the bounding box
		float max = Math.max(r.width, r.height);
		Area a = new Area(new Ellipse2D.Float(r.x + r.width / 6, r.y, max / 2, max / 2));
		a.add(new Area(new Ellipse2D.Float(r.x, r.y + r.height / 2, max / 3, max / 3)));
		a.add(new Area(new Ellipse2D.Float(r.x + r.width / 3, r.y + r.height / 3, max / 2, max / 2)));
		a.add(new Area(new Ellipse2D.Float(r.x + 2 * r.width / 3, r.y + 2 * r.height / 3, r.width / 3, r.width / 3)));
		a.intersect(new Area(r));
		return a;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public void move(float timeStep, float lx) {
		if (speed == 0)
			return;
		x += speed * timeStep;
		// apply periodic boundary condition
		if (x > lx)
			x -= lx + boundingBox.width;
		else if (x < -boundingBox.width)
			x += lx + boundingBox.width;
	}

	@Override
	public void translateBy(float dx, float dy) {
		x += dx;
		y += dy;
	}

	public void setLocation(float x, float y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public Point2D.Float getCenter() {
		Rectangle2D bound = getShape().getBounds2D();
		return new Point2D.Float((float) bound.getCenterX() + x, (float) bound.getCenterY() + y);
	}

	@Override
	public boolean contains(float rx, float ry) {
		return getShape().contains(rx - x, ry - y);
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getX() {
		return x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getY() {
		return y;
	}

	public void setDimension(float w, float h) {
		boundingBox = new Rectangle2D.Float(0, 0, w, h);
		setShape(getShape(boundingBox));
	}

	public float getWidth() {
		return boundingBox.width;
	}

	public float getHeight() {
		return boundingBox.height;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public float getSpeed() {
		return speed;
	}

	@Override
	public Cloud duplicate(float newX, float newY) {
		Cloud c = new Cloud(new Rectangle2D.Float(0, 0, boundingBox.width, boundingBox.height));
		c.speed = speed;
		c.setLabel(getLabel());
		c.color = color;
		c.setX(newX - boundingBox.width / 2); // offset to the center, since this method is called to paste.
		c.setY(newY - boundingBox.height / 2);
		return c;
	}

	@Override
	public Cloud duplicate() {
		Cloud c = new Cloud(new Rectangle2D.Float(0, 0, boundingBox.width, boundingBox.height));
		c.speed = speed;
		c.setLabel(getLabel());
		c.color = color;
		c.x = x;
		c.y = y;
		return c;
	}

	public String toXml() {
		XmlCharacterEncoder xce = new XmlCharacterEncoder();
		String xml = "<cloud";
		String uid = getUid();
		if (uid != null && !uid.trim().equals(""))
			xml += " uid=\"" + xce.encode(uid) + "\"";
		String label = getLabel();
		if (label != null && !label.trim().equals(""))
			xml += " label=\"" + xce.encode(label) + "\"";
		if (!Color.WHITE.equals(color))
			xml += " color=\"" + Integer.toHexString(0x00ffffff & color.getRGB()) + "\"";
		xml += " x=\"" + x + "\"";
		xml += " y=\"" + y + "\"";
		xml += " width=\"" + boundingBox.width + "\"";
		xml += " height=\"" + boundingBox.height + "\"";
		xml += " speed=\"" + speed + "\"/>";
		return xml;
	}

}
