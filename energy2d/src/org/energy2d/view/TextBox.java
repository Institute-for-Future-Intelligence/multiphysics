package org.energy2d.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;

import org.energy2d.model.Manipulable;
import org.energy2d.util.XmlCharacterEncoder;

/**
 * @author Charles Xie
 * 
 */
public class TextBox extends Manipulable {

	private float x, y;
	private String face = "Arial";
	private int style = Font.PLAIN;
	private int size = 14;
	private boolean border;
	private Color color = Color.white;

	public TextBox(Rectangle2D.Float rect) {
		super(rect);
	}

	public TextBox(Rectangle2D.Float rect, String text, float x, float y) {
		this(rect);
		setLabel(text);
		setLocation(x, y);
	}

	@Override
	public TextBox duplicate(float newX, float newY) {
		Rectangle2D.Float r = (Rectangle2D.Float) getShape();
		TextBox t = new TextBox(new Rectangle2D.Float(r.x, r.y, r.width, r.height));
		t.set(this);
		t.x = newX;
		t.y = newY;
		return t;
	}

	public TextBox duplicate() {
		return duplicate(x, y);
	}

	public void set(TextBox t) {
		setLabel(t.getLabel());
		face = t.face;
		style = t.style;
		size = t.size;
		color = t.color;
		border = t.border;
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

	public void translateBy(float dx, float dy) {
		x += dx;
		y += dy;
	}

	public void setLocation(float x, float y) {
		setX(x);
		setY(y);
	}

	public void setFace(String face) {
		this.face = face;
	}

	public String getFace() {
		return face;
	}

	public void setStyle(int style) {
		this.style = style;
	}

	public int getStyle() {
		return style;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public void setFont(Font font) {
		face = font.getFamily();
		style = font.getStyle();
		size = font.getSize();
	}

	public Font getFont() {
		return new Font(face, style, size);
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public void setBorder(boolean b) {
		border = b;
	}

	public boolean hasBorder() {
		return border;
	}

	public String toXml() {
		String xml = "<text";
		if (getUid() != null)
			xml += " uid=\"" + getUid() + "\"";
		xml += " string=\"" + new XmlCharacterEncoder().encode(getLabel()) + "\"";
		xml += " face=\"" + face + "\"";
		xml += " size=\"" + size + "\"";
		xml += " style=\"" + style + "\"";
		xml += " color=\"" + Integer.toHexString(0x00ffffff & getColor().getRGB()) + "\"";
		if (border)
			xml += " border=\"true\"";
		if (!isVisible())
			xml += " visible=\"false\"";
		if (!isDraggable())
			xml += " draggable=\"false\"";
		xml += " x=\"" + getX() + "\"";
		xml += " y=\"" + getY() + "\"/>\n";
		return xml;
	}

}
