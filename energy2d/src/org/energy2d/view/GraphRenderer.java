package org.energy2d.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.text.DecimalFormat;
import java.util.List;

import org.energy2d.model.TimedData;

/**
 * @author Charles Xie
 * 
 */
class GraphRenderer {

	final static byte CLOSE_BUTTON = 0;
	final static byte DATA_BUTTON = 1;
	final static byte X_EXPAND_BUTTON = 2;
	final static byte X_SHRINK_BUTTON = 3;
	final static byte Y_EXPAND_BUTTON = 4;
	final static byte Y_SHRINK_BUTTON = 5;
	final static byte Y_FIT_BUTTON = 6;
	final static byte Y_SELECTION_BUTTON_LEFT_ARROW = 7;
	final static byte Y_SELECTION_BUTTON_RIGHT_ARROW = 8;

	final static byte TIME_UNIT_HOUR = 0;
	final static byte TIME_UNIT_MINUTE = 1;
	final static byte TIME_UNIT_SECOND = 2;

	final static String[] DATA_TYPES = new String[] { "Temperature (" + '\u2103' + ")", "Heat flux (W/m" + '\u00B2' + ")", "Wind speed (m/s)" };

	private byte dataType = 0;
	private byte timeUnit = TIME_UNIT_HOUR;
	private String xLabel = "Time (hr)";
	private String yLabel = DATA_TYPES[0];
	private final static DecimalFormat FORMAT = new DecimalFormat("##.####");
	private Font smallFont = new Font(null, Font.PLAIN, 9);
	private Font labelFont = new Font(null, Font.PLAIN | Font.BOLD, 12);
	private Stroke frameStroke = new BasicStroke(2);
	private Stroke thinStroke = new BasicStroke(1);
	private Stroke curveStroke = new BasicStroke(1.5f);
	private Color bgColor = new Color(255, 255, 225, 128);
	private Color fgColor = Color.black;
	private Color frameColor = new Color(205, 205, 205, 128);
	private int x, y, w, h;
	private float xmax = 360000; // 100 hours
	private float ymin = 0;
	private float ymax = 50;
	private float yIncrement = 5;
	private Rectangle closeButton;
	private Rectangle dataButton;
	private Rectangle xExpandButton, xShrinkButton;
	private Rectangle yExpandButton, yShrinkButton;
	private Rectangle ySelectButton;
	private Rectangle yFitButton;
	private Polygon[] arrowButtons;
	private Point mouseMovedPoint;

	GraphRenderer(int x, int y, int w, int h) {
		closeButton = new Rectangle(0, 0, 20, 20);
		dataButton = new Rectangle(0, 0, 20, 20);
		xExpandButton = new Rectangle(0, 0, 20, 20);
		xShrinkButton = new Rectangle(0, 0, 20, 20);
		yExpandButton = new Rectangle(0, 0, 20, 20);
		yShrinkButton = new Rectangle(0, 0, 20, 20);
		ySelectButton = new Rectangle(0, 0, 20, 20);
		yFitButton = new Rectangle(0, 0, 20, 20);
		arrowButtons = new Polygon[] { new Polygon(), new Polygon() };
		setFrame(x, y, w, h);
	}

	void reset() {
		dataType = 0;
		yLabel = DATA_TYPES[0];
	}

	void setMouseMovedPoint(Point mouseMovedPoint) {
		this.mouseMovedPoint = mouseMovedPoint;
	}

	void setLabelX(String xLabel) {
		this.xLabel = xLabel;
	}

	String getLabelX() {
		return xLabel;
	}

	void setLabelY(String yLabel) {
		this.yLabel = yLabel;
	}

	String getLabelY() {
		return yLabel;
	}

	void setXmax(float xmax) {
		this.xmax = xmax;
	}

	float getXmax() {
		return xmax;
	}

	void doubleXmax() {
		xmax *= 2;
	}

	void halveXmax() {
		xmax *= 0.5f;
	}

	void setYmin(float ymin) {
		this.ymin = ymin;
		yIncrement = (ymax - ymin) * 0.1f;
	}

	float getYmin() {
		return ymin;
	}

	void decreaseYmin() {
		ymin -= yIncrement;
	}

	void setYmax(float ymax) {
		this.ymax = ymax;
		yIncrement = (ymax - ymin) * 0.1f;
	}

	float getYmax() {
		return ymax;
	}

	void increaseYmax() {
		ymax += yIncrement;
	}

	void decreaseYmax() {
		ymax -= yIncrement;
	}

	void setFrame(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		int size = 24;
		int position = x + w - 20;
		closeButton.setLocation(position, y);
		position -= size;
		dataButton.setLocation(position, y);
		position -= size;
		xExpandButton.setLocation(position, y);
		position -= size;
		xShrinkButton.setLocation(position, y);
		position -= size;
		yExpandButton.setLocation(position, y);
		position -= size;
		yShrinkButton.setLocation(position, y);
		position -= size;
		yFitButton.setLocation(position, y);
	}

	boolean windowContains(int rx, int ry) {
		return rx > x && rx < x + w && ry > y && ry < y + h;
	}

	boolean buttonContains(int rx, int ry) {
		return closeButton.contains(rx, ry) || dataButton.contains(rx, ry) || xExpandButton.contains(rx, ry) || xShrinkButton.contains(rx, ry) || yExpandButton.contains(rx, ry) || yShrinkButton.contains(rx, ry) || yFitButton.contains(rx, ry) || ySelectButton.contains(rx, ry);
	}

	boolean buttonContains(byte button, int rx, int ry) {
		switch (button) {
		case CLOSE_BUTTON:
			return closeButton.contains(rx, ry);
		case DATA_BUTTON:
			return dataButton.contains(rx, ry);
		case X_EXPAND_BUTTON:
			return xExpandButton.contains(rx, ry);
		case X_SHRINK_BUTTON:
			return xShrinkButton.contains(rx, ry);
		case Y_EXPAND_BUTTON:
			return yExpandButton.contains(rx, ry);
		case Y_SHRINK_BUTTON:
			return yShrinkButton.contains(rx, ry);
		case Y_FIT_BUTTON:
			return yFitButton.contains(rx, ry);
		case Y_SELECTION_BUTTON_LEFT_ARROW:
			return new Rectangle(ySelectButton.x, ySelectButton.y, ySelectButton.width / 2, ySelectButton.height).contains(rx, ry);
		case Y_SELECTION_BUTTON_RIGHT_ARROW:
			return new Rectangle(ySelectButton.x + ySelectButton.width / 2, ySelectButton.y, ySelectButton.width / 2, ySelectButton.height).contains(rx, ry);
		default:
			return false;
		}
	}

	void next() {
		dataType = (byte) ((dataType + 1) % DATA_TYPES.length);
		yLabel = DATA_TYPES[dataType];
	}

	void previous() {
		dataType--;
		if (dataType < 0)
			dataType = (byte) (DATA_TYPES.length - 1);
		yLabel = DATA_TYPES[dataType];
	}

	void setDataType(byte dataType) {
		this.dataType = (byte) Math.min(DATA_TYPES.length - 1, dataType);
		yLabel = DATA_TYPES[this.dataType];
	}

	byte getDataType() {
		return dataType;
	}

	void setTimeUnit(byte timeUnit) {
		this.timeUnit = timeUnit;
		switch (timeUnit) {
		case TIME_UNIT_SECOND:
			xLabel = "Time (s)";
			break;
		case TIME_UNIT_MINUTE:
			xLabel = "Time (min)";
			break;
		case TIME_UNIT_HOUR:
			xLabel = "Time (hr)";
			break;
		default:
			xLabel = "Time";
			break;
		}
	}

	byte getTimeUnit() {
		return timeUnit;
	}

	private void centerString(String s, Graphics2D g, int x, int y, Shape[] shapes) {
		int stringWidth = g.getFontMetrics().stringWidth(s);
		if (shapes == arrowButtons) {
			Color oldColor = g.getColor();
			g.setColor(Color.lightGray);
			ySelectButton.setBounds(x - stringWidth / 2 - 20, y - 10, stringWidth + 40, 16);
			g.fill3DRect(ySelectButton.x, ySelectButton.y, ySelectButton.width, ySelectButton.height, true);
			g.setColor(oldColor);
			int x2 = x - stringWidth / 2 - 15;
			arrowButtons[0].reset();
			arrowButtons[0].addPoint(x2 + 10, y - 6);
			arrowButtons[0].addPoint(x2 + 10, y);
			arrowButtons[0].addPoint(x2 + 4, y - 3);
			g.fillPolygon(arrowButtons[0]);
			x2 = x + stringWidth / 2 + 5;
			arrowButtons[1].reset();
			arrowButtons[1].addPoint(x2 + 4, y - 6);
			arrowButtons[1].addPoint(x2 + 4, y);
			arrowButtons[1].addPoint(x2 + 10, y - 3);
			g.fillPolygon(arrowButtons[1]);
		}
		g.drawString(s, x - stringWidth / 2, y);
	}

	void drawFrame(Graphics2D g) {

		// draw graph canvas
		g.setColor(bgColor);
		g.fillRoundRect(x - 10, y - 10, w + 20, h + 20, 20, 20);
		g.setStroke(frameStroke);
		g.setColor(frameColor);
		g.drawRoundRect(x - 10, y - 10, w + 20, h + 20, 20, 20);

		g.setStroke(thinStroke);

		// draw close button
		g.setColor(fgColor);
		g.fillRect(closeButton.x + 2, closeButton.y + 2, closeButton.width, closeButton.height);
		g.setColor(Color.lightGray);
		g.fill(closeButton);
		g.setColor(fgColor);
		g.draw(closeButton);
		g.drawLine(closeButton.x + 4, closeButton.y + 4, closeButton.x + closeButton.width - 4, closeButton.y + closeButton.height - 4);
		g.drawLine(closeButton.x + 4, closeButton.y + closeButton.height - 4, closeButton.x + closeButton.width - 4, closeButton.y + 4);

		// draw data button
		g.setColor(fgColor);
		g.fillRect(dataButton.x + 2, dataButton.y + 2, dataButton.width, dataButton.height);
		g.setColor(Color.lightGray);
		g.fill(dataButton);
		g.setColor(fgColor);
		g.draw(dataButton);
		int x2 = dataButton.x + 3;
		int y2 = dataButton.y + 3;
		int w2 = dataButton.width - 6;
		int h2 = dataButton.height - 6;
		g.drawRect(x2, y2, w2, h2);
		float cell = h2 / 3.0f;
		for (int i = 1; i < 4; i++) {
			int rn = Math.round(y2 + i * cell);
			g.drawLine(x2, rn, x2 + w2, rn);
			rn = Math.round(x2 + i * cell);
			g.drawLine(rn, y2, rn, y2 + h2);
		}

		// draw x scope control buttons
		g.setColor(fgColor);
		g.fillRect(xExpandButton.x + 2, xExpandButton.y + 2, xExpandButton.width, xExpandButton.height);
		g.setColor(Color.lightGray);
		g.fill(xExpandButton);
		g.setColor(fgColor);
		g.draw(xExpandButton);
		y2 = xExpandButton.y + xExpandButton.height / 2;
		x2 = xExpandButton.x + xExpandButton.width - 4;
		g.drawLine(xExpandButton.x + 3, y2, x2, y2);
		x2 = xExpandButton.x + 3;
		g.drawLine(x2, y2, x2 + 4, y2 - 4);
		g.drawLine(x2, y2, x2 + 4, y2 + 4);

		g.setColor(fgColor);
		g.fillRect(xShrinkButton.x + 2, xShrinkButton.y + 2, xShrinkButton.width, xShrinkButton.height);
		g.setColor(Color.lightGray);
		g.fill(xShrinkButton);
		g.setColor(fgColor);
		g.draw(xShrinkButton);
		x2 = xShrinkButton.x + xShrinkButton.width - 4;
		g.drawLine(xShrinkButton.x + 3, y2, x2, y2);
		g.drawLine(x2, y2, x2 - 4, y2 - 4);
		g.drawLine(x2, y2, x2 - 4, y2 + 4);

		// draw y scope control buttons
		g.setColor(fgColor);
		g.fillRect(yExpandButton.x + 2, yExpandButton.y + 2, yExpandButton.width, yExpandButton.height);
		g.setColor(Color.lightGray);
		g.fill(yExpandButton);
		g.setColor(fgColor);
		g.draw(yExpandButton);
		x2 = yExpandButton.x + yExpandButton.width / 2;
		y2 = yExpandButton.y + yExpandButton.height - 4;
		g.drawLine(x2, yExpandButton.y + 3, x2, y2);
		g.drawLine(x2, y2 + 1, x2 + 4, y2 - 3);
		g.drawLine(x2, y2 + 1, x2 - 4, y2 - 3);

		g.setColor(fgColor);
		g.fillRect(yShrinkButton.x + 2, yShrinkButton.y + 2, yShrinkButton.width, yShrinkButton.height);
		g.setColor(Color.lightGray);
		g.fill(yShrinkButton);
		g.setColor(fgColor);
		g.draw(yShrinkButton);
		x2 = yShrinkButton.x + yShrinkButton.width / 2;
		g.drawLine(x2, yShrinkButton.y + 3, x2, y2);
		g.drawLine(x2, yShrinkButton.y + 3, x2 + 4, yShrinkButton.y + 6);
		g.drawLine(x2, yShrinkButton.y + 3, x2 - 4, yShrinkButton.y + 6);

		g.setColor(fgColor);
		g.fillRect(yFitButton.x + 2, yFitButton.y + 2, yFitButton.width, yFitButton.height);
		g.setColor(Color.lightGray);
		g.fill(yFitButton);
		g.setColor(fgColor);
		g.draw(yFitButton);
		x2 = yFitButton.x + yFitButton.width / 2;
		g.drawLine(x2, yFitButton.y + 3, x2, y2);
		g.drawLine(x2, yFitButton.y + 3, x2 + 4, yFitButton.y + 6);
		g.drawLine(x2, yFitButton.y + 3, x2 - 4, yFitButton.y + 6);
		y2 = yFitButton.y + yFitButton.height - 4;
		g.drawLine(x2, y2 + 1, x2 + 4, y2 - 3);
		g.drawLine(x2, y2 + 1, x2 - 4, y2 - 3);

		// draw axes
		g.drawLine(x, y, x, y + h);
		g.drawLine(x, y + h, x + w, y + h);
		g.drawLine(x, y, x - 2, y + 4);
		g.drawLine(x, y, x + 2, y + 4);
		g.drawLine(x + w, y + h, x + w - 4, y + h - 2);
		g.drawLine(x + w, y + h, x + w - 4, y + h + 2);
		g.setFont(smallFont);
		int k;
		float unit;
		switch (timeUnit) {
		case TIME_UNIT_SECOND:
			unit = 0.1f;
			break;
		case TIME_UNIT_MINUTE:
			unit = 0.1f / 60f;
			break;
		default:
			unit = 0.1f / 3600f;
			break;
		}
		for (int i = 1; i < 10; i++) {
			k = x + Math.round(i * w * 0.1f);
			if (i % 2 == 0) {
				g.drawLine(k, y + h, k, y + h - 4);
				centerString(FORMAT.format(xmax * i * unit), g, k + 3, y + h - 8, null);
			} else {
				g.drawLine(k, y + h, k, y + h - 2);
			}
		}
		centerString(xLabel, g, x + w - 20, y + h - 4, null);

		int nTickmarksOfYAxis = 10;
		int tens = 1;
		if (ymax < ymin) {
			float ymaxOLD = ymax;
			ymax = ymin;
			ymin = ymaxOLD;
		} else if (ymax == ymin) {
			ymax = ymin + 1;
		}
		float dyMultiplied = (ymax - ymin) / nTickmarksOfYAxis;
		while (dyMultiplied < 1) {
			dyMultiplied *= 10;
			tens *= 10;
		}
		dyMultiplied = (float) Math.ceil(dyMultiplied);
		float yminMultiplied = (float) Math.floor(ymin * tens);
		float scaleY = h / (ymax - ymin);
		for (int i = 0; i < nTickmarksOfYAxis + 1; i++) {
			float yTickmark = (yminMultiplied + i * dyMultiplied) / tens;
			if (yTickmark > ymin && yTickmark < ymax) {
				k = (int) (y + h - (yTickmark - ymin) * scaleY);
				if (i % 2 == 0) {
					g.drawLine(x, k, x + 4, k);
					centerString(FORMAT.format(yTickmark), g, x + 18, k + 3, null);
				} else {
					g.drawLine(x, k, x + 2, k);
				}
			}
		}
		centerString(yLabel, g, x + 70, y + 10, arrowButtons);

		if (mouseMovedPoint != null)
			drawButtonInfo(g);

	}

	private void drawButtonInfo(Graphics2D g) {
		String s = null;
		Rectangle r = null;
		if (closeButton.contains(mouseMovedPoint)) {
			s = "Close graph";
			r = closeButton;
		} else if (dataButton.contains(mouseMovedPoint)) {
			s = "View data";
			r = dataButton;
		} else if (xExpandButton.contains(mouseMovedPoint)) {
			s = "Expand x axis";
			r = xExpandButton;
		} else if (xShrinkButton.contains(mouseMovedPoint)) {
			s = "Shrink x axis";
			r = xShrinkButton;
		} else if (yExpandButton.contains(mouseMovedPoint)) {
			s = "Expand y axis";
			r = yExpandButton;
		} else if (yShrinkButton.contains(mouseMovedPoint)) {
			s = "Shrink y axis";
			r = yShrinkButton;
		} else if (yFitButton.contains(mouseMovedPoint)) {
			s = "Fit y axis to data";
			r = yFitButton;
		} else if (ySelectButton.contains(mouseMovedPoint)) {
			s = "Select data type";
			r = ySelectButton;
		}
		if (s == null)
			return;
		g.setFont(smallFont);
		int stringWidth = g.getFontMetrics().stringWidth(s);
		g.setStroke(thinStroke);
		g.setColor(Color.black);
		g.fillRoundRect(r.x + (r.width - stringWidth) / 2 - 5, r.y - 24, stringWidth + 10, 20, 8, 8);
		g.setColor(Color.white);
		g.drawString(s, r.x + (r.width - stringWidth) / 2, r.y - 12);
	}

	void drawData(Graphics2D g, List<TimedData> data, String label, boolean highlight) {

		g.setStroke(curveStroke);
		g.setColor(highlight ? Color.yellow : fgColor);

		int n = data.size();
		if (n > 0) {
			int m = Math.max(1, (int) (n / w));
			TimedData d = data.get(0);
			float t1 = d.getTime();
			float v1 = d.getValue();
			float t2, v2;
			int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
			float scaleX = w / xmax;
			float scaleY = h / (ymax - ymin);
			synchronized (data) {
				for (int i = m; i <= n - m; i += m) {
					x1 = (int) (x + t1 * scaleX);
					y1 = (int) (y + h - (v1 - ymin) * scaleY);
					if (x1 > x + w)
						break;
					d = data.get(i);
					t2 = d.getTime();
					v2 = d.getValue();
					x2 = (int) (x + t2 * scaleX);
					y2 = (int) (y + h - (v2 - ymin) * scaleY);
					g.drawLine(x1, y1, x2, y2);
					t1 = t2;
					v1 = v2;
				}
			}
			if (label != null) {
				g.setFont(labelFont);
				g.drawString(label, x2 + 5, y2);
			}
		}

	}

	float[] getData(List<TimedData> data, int rx, int ry) {
		if (!data.isEmpty()) {
			float t, v;
			int dx = 0, dy = 0;
			float scaleX = w / xmax;
			float scaleY = h / (ymax - ymin);
			synchronized (data) {
				for (TimedData d : data) {
					t = d.getTime();
					v = d.getValue();
					dx = (int) (x + t * scaleX) - rx;
					dy = (int) (y + h - (v - ymin) * scaleY) - ry;
					if (dx * dx + dy * dy < 100)
						return new float[] { t, v };
				}
			}
		}
		return null;
	}

}
