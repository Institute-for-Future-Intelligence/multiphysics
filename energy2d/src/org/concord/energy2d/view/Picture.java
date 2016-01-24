package org.concord.energy2d.view;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import org.concord.energy2d.model.Manipulable;
import org.concord.energy2d.util.XmlCharacterEncoder;

/**
 * @author Charles Xie
 * 
 */
public class Picture extends Manipulable {

	private BufferedImage image;
	private String format = "png";

	public Picture(BufferedImage image, String formatName, float x, float y) {
		super(new Rectangle2D.Float()); // dummy rectangle, don't use
		setImage(image);
		setFormat(formatName);
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

	public float getWidth() {
		Rectangle2D.Float rect = (Rectangle2D.Float) shape;
		return rect.width;
	}

	public float getHeight() {
		Rectangle2D.Float rect = (Rectangle2D.Float) shape;
		return rect.height;
	}

	@Override
	public Manipulable duplicate() {
		Rectangle2D.Float rect = (Rectangle2D.Float) shape;
		return new Picture(image, format, rect.x, rect.y);
	}

	@Override
	public Manipulable duplicate(float x, float y) {
		return new Picture(image, format, x, y);
	}

	@Override
	public void translateBy(float dx, float dy) {
		Rectangle2D.Float rect = (Rectangle2D.Float) shape;
		rect.x += dx;
		rect.y += dy;
	}

	public String toXml() {
		String xml = "<image";
		if (getUid() != null)
			xml += " uid=\"" + getUid() + "\"";
		xml += " label=\"" + new XmlCharacterEncoder().encode(getLabel()) + "\"";
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
		if (!isVisible())
			xml += " visible=\"false\"";
		if (!isDraggable())
			xml += " draggable=\"false\"";
		xml += " format=\"" + format + "\"";
		xml += " x=\"" + getX() + "\"";
		xml += " y=\"" + getY() + "\"/>\n";
		return xml;
	}

}