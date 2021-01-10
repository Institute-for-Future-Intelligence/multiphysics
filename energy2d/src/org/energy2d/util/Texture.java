package org.energy2d.util;

import java.awt.Color;
import java.awt.Paint;

/**
 * @author Charles Xie
 */

public class Texture implements FillPattern {

	private int fgColor = 0xff000000;
	private int bgColor = 0xffffffff;
	private int alpha = 255;
	private byte type = TextureFactory.DIAGONAL_UP_STRIPE;
	private int cellWidth = 10;
	private int cellHeight = 10;
	private transient Paint texturePaint;

	public Texture() {
	}

	public Texture(int fgColor, int bgColor, byte type, int cellWidth, int cellHeight) {
		this.fgColor = fgColor;
		this.bgColor = bgColor;
		this.type = type;
		this.cellWidth = cellWidth;
		this.cellHeight = cellHeight;
		texturePaint = TextureFactory.createPattern(type, cellWidth, cellHeight, new Color(fgColor), new Color(bgColor));
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	public int getAlpha() {
		return alpha;
	}

	public Paint getPaint() {
		return texturePaint;
	}

	public byte getStyle() {
		return type;
	}

	public void setStyle(byte i) {
		type = i;
		if (cellWidth <= 0 || cellHeight <= 0)
			return;
		texturePaint = TextureFactory.createPattern(type, cellWidth, cellHeight, new Color(fgColor), new Color(bgColor));
	}

	public int getCellWidth() {
		return cellWidth;
	}

	public void setCellWidth(int w) {
		cellWidth = w;
		if (cellWidth <= 0 || cellHeight <= 0)
			return;
		texturePaint = TextureFactory.createPattern(type, cellWidth, cellHeight, new Color(fgColor), new Color(bgColor));
	}

	public int getCellHeight() {
		return cellHeight;
	}

	public void setCellHeight(int h) {
		cellHeight = h;
		if (cellWidth <= 0 || cellHeight <= 0)
			return;
		texturePaint = TextureFactory.createPattern(type, cellWidth, cellHeight, new Color(fgColor), new Color(bgColor));
	}

	public int getBackground() {
		return bgColor;
	}

	public void setBackground(int c) {
		bgColor = c;
		if (cellWidth <= 0 || cellHeight <= 0)
			return;
		texturePaint = TextureFactory.createPattern(type, cellWidth, cellHeight, new Color(fgColor), new Color(bgColor));
	}

	public int getForeground() {
		return fgColor;
	}

	public void setForeground(int c) {
		fgColor = c;
		if (cellWidth <= 0 || cellHeight <= 0)
			return;
		texturePaint = TextureFactory.createPattern(type, cellWidth, cellHeight, new Color(fgColor), new Color(bgColor));
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Texture))
			return false;
		int i = ((Texture) obj).getForeground();
		if (i != fgColor)
			return false;
		i = ((Texture) obj).getBackground();
		if (i != bgColor)
			return false;
		i = ((Texture) obj).getStyle();
		if (i != type)
			return false;
		i = ((Texture) obj).getCellWidth();
		if (i != cellWidth)
			return false;
		i = ((Texture) obj).getCellHeight();
		return i == cellHeight;
	}

	public int hashCode() {
		int result = 23;
		result = MiscUtil.hash(result, fgColor);
		result = MiscUtil.hash(result, bgColor);
		result = MiscUtil.hash(result, type);
		result = MiscUtil.hash(result, cellWidth);
		result = MiscUtil.hash(result, cellHeight);
		return result;
	}

	public String toString() {
		return "Texture " + type;
	}

}