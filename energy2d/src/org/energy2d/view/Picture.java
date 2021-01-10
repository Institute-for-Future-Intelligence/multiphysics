package org.energy2d.view;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import org.energy2d.model.Manipulable;
import org.energy2d.util.XmlCharacterEncoder;

/**
 * @author Charles Xie
 * 
 */
public class Picture extends Manipulable {

	private BufferedImage image;
	private String format = "png";
	private String fileName;
	private boolean border;

	public Picture(BufferedImage image, String format, String fileName, float x, float y) {
		super(new Rectangle2D.Float()); // dummy rectangle, don't use
		setImage(image);
		setFormat(format);
		setFileName(fileName);
		Rectangle2D.Float rect = (Rectangle2D.Float) shape;
		rect.x = x;
		rect.y = y;
		rect.width = image.getWidth();
		rect.height = image.getHeight();
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getFormat() {
		return format;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setX(float x) {
		Rectangle2D.Float rect = (Rectangle2D.Float) shape;
		rect.x = x;
	}

	public float getX() {
		Rectangle2D.Float rect = (Rectangle2D.Float) shape;
		return rect.x;
	}

	public void setY(float y) {
		Rectangle2D.Float rect = (Rectangle2D.Float) shape;
		rect.y = y;
	}

	public float getY() {
		Rectangle2D.Float rect = (Rectangle2D.Float) shape;
		return rect.y;
	}

	public void setWidth(float width) {
		Rectangle2D.Float rect = (Rectangle2D.Float) shape;
		rect.width = width;
	}

	public float getWidth() {
		Rectangle2D.Float rect = (Rectangle2D.Float) shape;
		return rect.width;
	}

	public void setHeight(float height) {
		Rectangle2D.Float rect = (Rectangle2D.Float) shape;
		rect.height = height;
	}

	public float getHeight() {
		Rectangle2D.Float rect = (Rectangle2D.Float) shape;
		return rect.height;
	}

	public void setBorder(boolean b) {
		border = b;
	}

	public boolean hasBorder() {
		return border;
	}

	@Override
	public Manipulable duplicate() {
		Rectangle2D.Float rect = (Rectangle2D.Float) shape;
		Picture p = new Picture(image, format, fileName, rect.x, rect.y);
		p.setWidth(getWidth());
		p.setHeight(getHeight());
		return p;
	}

	@Override
	public Manipulable duplicate(float x, float y) {
		Picture p = new Picture(image, format, fileName, x, y);
		p.setWidth(getWidth());
		p.setHeight(getHeight());
		return p;
	}

	@Override
	public void translateBy(float dx, float dy) {
		Rectangle2D.Float rect = (Rectangle2D.Float) shape;
		rect.x += dx;
		rect.y += dy;
	}

	public String toXml() {
		String xml = "<image";
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		boolean success = true;
		try {
			ImageIO.write(image, format, b);
		} catch (IOException e) {
			e.printStackTrace();
			success = false;
		} finally {
			try {
				b.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (success)
			xml += " data=\"" + DatatypeConverter.printBase64Binary(b.toByteArray()) + "\"";
		if (getUid() != null && !getUid().equals(""))
			xml += " uid=\"" + getUid() + "\"";
		if (border)
			xml += " border=\"true\"";
		if (label != null)
			xml += " label=\"" + new XmlCharacterEncoder().encode(label) + "\"";
		if (fileName != null)
			xml += " filename=\"" + new XmlCharacterEncoder().encode(fileName) + "\"";
		if (!isVisible())
			xml += " visible=\"false\"";
		if (!isDraggable())
			xml += " draggable=\"false\"";
		xml += " format=\"" + format + "\"";
		xml += " width=\"" + getWidth() + "\"";
		xml += " height=\"" + getHeight() + "\"";
		xml += " x=\"" + getX() + "\"";
		xml += " y=\"" + getY() + "\"/>\n";
		return xml;
	}

}