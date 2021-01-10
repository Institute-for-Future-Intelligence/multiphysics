package org.energy2d.model;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.energy2d.util.XmlCharacterEncoder;

/**
 * Trees are expensive to calculate. So we use additional variables (x, y) for setting locations and avoiding recalculation of shapes when moving them.
 * 
 * @author Charles Xie
 * 
 */
public class Tree extends Manipulable {

	public final static byte REGULAR = 0;
	public final static byte PINE = 1;

	private byte type = PINE;
	private float x; // the x coordinate of the upper-left corner
	private float y; // the y coordinate of the upper-left corner
	private Color color = Color.GREEN.darker();
	private Rectangle2D.Float boundingBox;

	/** Construct a tree based on the specified bounding box, which must start from (0, 0). If not, the (x, y) will be ignored. */
	public Tree(Shape bb, byte type) {
		super(bb);
		if (!(bb instanceof Rectangle2D.Float))
			throw new IllegalArgumentException("Shape must be a Rectangle2D.Float");
		setType(type);
		Rectangle2D.Float r = (Rectangle2D.Float) bb;
		setDimension(r.width, r.height);
	}

	public static Area getShape(Rectangle2D.Float r, byte type) {
		// the positions and sizes of the circles must ensure that r is the bounding box
		Area a = new Area(new Rectangle2D.Float(r.x + r.width * 0.45f, r.y + r.height * 0.5f, r.width * 0.1f, r.height * 0.5f));
		switch (type) {
		case REGULAR:
			float p = Math.min(r.width, r.height) * 0.6f;
			float q = p * 0.8f;
			a.add(new Area(new Ellipse2D.Float(r.x + (r.width - p) * 0.5f, r.y, p, p)));
			a.add(new Area(new Ellipse2D.Float(r.x, r.y + p * 0.8f, q, q)));
			a.add(new Area(new Ellipse2D.Float(r.x + r.width - q, r.y + p * 0.8f, q, q)));
			break;
		case PINE:
			GeneralPath path = new GeneralPath();
			path.moveTo(r.x + r.width * 0.5f, r.y);
			path.lineTo(r.x + r.width * 0.3f, r.y + r.height * 0.3f);
			path.lineTo(r.x + r.width * 0.7f, r.y + r.height * 0.3f);
			a.add(new Area(path));
			path.reset();
			path.moveTo(r.x + r.width * 0.5f, r.y + r.height * 0.2f);
			path.lineTo(r.x + r.width * 0.2f, r.y + r.height * 0.5f);
			path.lineTo(r.x + r.width * 0.8f, r.y + r.height * 0.5f);
			a.add(new Area(path));
			path.reset();
			path.moveTo(r.x + r.width * 0.5f, r.y + r.height * 0.3f);
			path.lineTo(r.x, r.y + r.height * 0.8f);
			path.lineTo(r.x + r.width, r.y + r.height * 0.8f);
			a.add(new Area(path));
			break;
		}
		return a;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
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

	public void setType(byte type) {
		this.type = type;
	}

	public byte getType() {
		return type;
	}

	public void setDimension(float w, float h) {
		boundingBox = new Rectangle2D.Float(0, 0, w, h);
		setShape(getShape(boundingBox, type));
	}

	public float getWidth() {
		return boundingBox.width;
	}

	public float getHeight() {
		return boundingBox.height;
	}

	@Override
	public Tree duplicate(float x, float y) {
		Tree t = new Tree(new Rectangle2D.Float(0, 0, boundingBox.width, boundingBox.height), type);
		t.setLabel(getLabel());
		t.color = color;
		t.setX(x - boundingBox.width / 2); // offset to the center, since this method is called to paste.
		t.setY(y - boundingBox.height / 2);
		return t;
	}

	@Override
	public Tree duplicate() {
		Tree t = new Tree(new Rectangle2D.Float(0, 0, boundingBox.width, boundingBox.height), type);
		t.setLabel(getLabel());
		t.color = color;
		t.x = x;
		t.y = y;
		return t;
	}

	public String toXml() {
		XmlCharacterEncoder xce = new XmlCharacterEncoder();
		String xml = "<tree";
		String uid = getUid();
		if (uid != null && !uid.trim().equals(""))
			xml += " uid=\"" + xce.encode(uid) + "\"";
		String label = getLabel();
		if (label != null && !label.trim().equals(""))
			xml += " label=\"" + xce.encode(label) + "\"";
		if (!Color.GREEN.darker().equals(color))
			xml += " color=\"" + Integer.toHexString(0x00ffffff & color.getRGB()) + "\"";
		xml += " x=\"" + x + "\"";
		xml += " y=\"" + y + "\"";
		xml += " width=\"" + boundingBox.width + "\"";
		xml += " height=\"" + boundingBox.height + "\"";
		xml += " type=\"" + type + "\"/>";
		return xml;
	}

}
