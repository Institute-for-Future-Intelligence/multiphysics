package org.energy2d.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;

import org.energy2d.model.Cloud;
import org.energy2d.model.Fan;
import org.energy2d.model.Heliostat;
import org.energy2d.model.Tree;

/**
 * @author Charles Xie
 * 
 */

public abstract class Symbol implements Icon {

	protected int xSymbol = 0, ySymbol = 0, wSymbol = 8, hSymbol = 8;
	protected Color color = Color.white;
	protected Stroke stroke = new BasicStroke(1);
	protected boolean paintBorder;
	protected boolean pressed;
	protected boolean disabled;
	protected int offsetX, offsetY;
	protected int marginX, marginY;

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setPressed(boolean pressed) {
		this.pressed = pressed;
	}

	public boolean isPressed() {
		return pressed;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
	}

	public Stroke getStroke() {
		return stroke;
	}

	public void setBorderPainted(boolean paintBorder) {
		this.paintBorder = paintBorder;
	}

	public boolean isBorderPainted() {
		return paintBorder;
	}

	public void setOffsetX(int offsetX) {
		this.offsetX = offsetX;
	}

	public int getOffsetX() {
		return offsetX;
	}

	public void setOffsetY(int offsetY) {
		this.offsetY = offsetY;
	}

	public int getOffsetY() {
		return offsetY;
	}

	public void setMarginX(int marginX) {
		this.marginX = marginX;
	}

	public int getMarginX() {
		return marginX;
	}

	public void setMarginY(int marginY) {
		this.marginY = marginY;
	}

	public int getMarginY() {
		return marginY;
	}

	public void setSymbolWidth(int wSymbol) {
		this.wSymbol = wSymbol;
	}

	public int getSymbolWidth() {
		return wSymbol;
	}

	public void setSymbolHeight(int hSymbol) {
		this.hSymbol = hSymbol;
	}

	public int getSymbolHeight() {
		return hSymbol;
	}

	public void setIconWidth(int width) {
		wSymbol = width - marginX * 2;
	}

	public int getIconWidth() {
		return wSymbol + marginX * 2;
	}

	public void setIconHeight(int height) {
		hSymbol = height - marginY * 2;
	}

	public int getIconHeight() {
		return hSymbol + marginY * 2;
	}

	public boolean contains(int rx, int ry) {
		return rx > xSymbol && rx < xSymbol + wSymbol && ry > ySymbol && ry < ySymbol + hSymbol;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		xSymbol = x + offsetX;
		ySymbol = y + offsetY;
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(color);
		if (paintBorder) {
			g.drawRoundRect(xSymbol, ySymbol, wSymbol, hSymbol, 10, 10);
		}
		g2.setStroke(stroke);
	}

	public Symbol getScaledInstance(float scale) {
		try {
			Symbol icon = getClass().newInstance();
			icon.setIconWidth((int) (scale * icon.getIconWidth()));
			icon.setIconHeight((int) (scale * icon.getIconHeight()));
			return icon;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Image createImage(Component c) {
		BufferedImage image = new BufferedImage(getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.createGraphics();
		try {
			paintIcon(c, g, 0, 0);
			return image;
		} finally {
			g.dispose();
		}
	}

	static class Moon extends Symbol {

		public Moon(Color color, int w, int h) {
			setColor(color);
			setIconWidth(w);
			setIconHeight(h);
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			Graphics2D g2 = (Graphics2D) g;
			Area a = new Area(new Ellipse2D.Float(xSymbol, ySymbol, wSymbol, hSymbol));
			a.subtract(new Area(new Ellipse2D.Float(xSymbol + wSymbol * 0.25f, ySymbol, wSymbol, hSymbol)));
			g2.fill(a);
		}

	}

	static class Sun extends Symbol {

		public Sun(Color color, int w, int h) {
			setColor(color);
			setIconWidth(w);
			setIconHeight(h);
			setStroke(new BasicStroke(2));
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			Graphics2D g2 = (Graphics2D) g;
			Ellipse2D.Float s = new Ellipse2D.Float(xSymbol, ySymbol, wSymbol * 0.75f, hSymbol * 0.75f);
			g2.fill(s);
			int x1, y1;
			double angle = 0;
			int n = 8;
			for (int i = 0; i < n; i++) {
				angle = i * Math.PI * 2 / n;
				x1 = (int) (s.getCenterX() + 10 * Math.cos(angle));
				y1 = (int) (s.getCenterY() + 10 * Math.sin(angle));
				g2.drawLine(x1, y1, (int) s.getCenterX(), (int) s.getCenterY());
			}
		}

	}

	public static class Thermometer extends Symbol {

		private int value;
		private int ballDiameter;
		private boolean isButtonIcon;

		public Thermometer(boolean isButtonIcon) {
			this.isButtonIcon = isButtonIcon;
		}

		public void setValue(int value) {
			this.value = value;
		}

		public int getBarHeight() {
			return hSymbol - Math.round(wSymbol * 1.5f);
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			Graphics2D g2 = (Graphics2D) g;
			if (isButtonIcon) {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			}
			g2.setColor(Color.white);
			g2.fillRect(xSymbol, ySymbol, wSymbol, hSymbol);
			ballDiameter = Math.round(wSymbol * 1.45f);
			int x2 = xSymbol + wSymbol / 2;
			int y2 = ySymbol + hSymbol - ballDiameter / 2;
			if (value != 0) {
				g2.setColor(Color.red);
				BasicStroke bs = new BasicStroke(wSymbol * 0.3f);
				g2.setStroke(bs);
				g2.drawLine(x2, y2 - value, x2, y2);
			}
			g2.setColor(Color.black);
			g2.setStroke(stroke);
			g2.drawRect(xSymbol, ySymbol, wSymbol, hSymbol);
			int n = hSymbol / 2;
			for (int i = 1; i < n; i++) {
				g2.drawLine(xSymbol, ySymbol + i * 2, Math.round(xSymbol + 0.2f * wSymbol), ySymbol + i * 2);
				g2.drawLine(xSymbol + wSymbol, ySymbol + i * 2, Math.round(xSymbol + wSymbol - 0.2f * wSymbol), ySymbol + i * 2);
			}
			x2 = Math.round(xSymbol - wSymbol * 0.25f);
			g2.setColor(Color.lightGray);
			g2.fillOval(x2, y2, ballDiameter, ballDiameter);
			g2.setColor(Color.black);
			g2.drawOval(x2, y2, ballDiameter, ballDiameter);
		}

	}

	public static class HeatFluxSensor extends Symbol {

		private Stroke stroke2;
		private boolean isButtonIcon;

		public HeatFluxSensor(boolean isButtonIcon) {
			stroke2 = new BasicStroke(3);
			this.isButtonIcon = isButtonIcon;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			int y2 = Math.round(ySymbol + hSymbol * 0.5f);
			Graphics2D g2 = (Graphics2D) g;
			if (isButtonIcon) {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			}
			g2.setColor(Color.white);
			g2.fillRect(xSymbol, ySymbol, wSymbol, hSymbol);
			g2.fillOval(xSymbol - 7, y2 - 3, 6, 6);
			g2.setColor(Color.black);
			g2.drawRect(xSymbol, ySymbol, wSymbol, hSymbol);
			for (int i = 4; i < wSymbol - 4; i += 4) {
				if (i % 8 == 0) {
					g2.drawLine(xSymbol + i, ySymbol, xSymbol + i + 4, ySymbol + hSymbol);
				} else {
					g2.drawLine(xSymbol + i, ySymbol + hSymbol, xSymbol + i + 4, ySymbol);
				}
			}
			g2.drawLine(xSymbol - 2, y2, xSymbol + 4, y2);
			g2.drawOval(xSymbol - 7, y2 - 3, 6, 6);
			g2.setStroke(stroke2);
			g2.drawLine(xSymbol, ySymbol, xSymbol + wSymbol, ySymbol);
			g2.drawLine(xSymbol, ySymbol + hSymbol, xSymbol + wSymbol, ySymbol + hSymbol);
		}

	}

	public static class Anemometer extends Symbol {

		private float angle;
		private boolean isButtonIcon;

		public Anemometer(boolean isButtonIcon) {
			this.isButtonIcon = isButtonIcon;
		}

		public void setAngle(float angle) {
			this.angle = angle;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {

			super.paintIcon(c, g, x, y);

			Graphics2D g2 = (Graphics2D) g;
			if (isButtonIcon) {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			}
			double xc = xSymbol + wSymbol * 0.5;
			double yc = ySymbol + hSymbol * 0.5;
			g2.setColor(Color.white);
			g.fillOval(Math.round(xSymbol + wSymbol * 0.4f), Math.round(ySymbol + hSymbol * 0.4f), Math.round(wSymbol * 0.2f), Math.round(hSymbol * 0.2f));
			g2.setColor(Color.black);
			g.drawOval(Math.round(xSymbol + wSymbol * 0.4f), Math.round(ySymbol + hSymbol * 0.4f), Math.round(wSymbol * 0.2f), Math.round(hSymbol * 0.2f));

			g2.rotate(angle, xc, yc);

			int[] xPoints = new int[] { (int) xc, Math.round(xSymbol + wSymbol * 0.4f), Math.round(xSymbol + wSymbol * 0.6f) };
			int[] yPoints = new int[] { ySymbol, Math.round(ySymbol + hSymbol * 0.4f), Math.round(ySymbol + hSymbol * 0.4f) };
			g2.setColor(Color.white);
			g.fillPolygon(xPoints, yPoints, 3);
			g2.setColor(Color.black);
			g.drawPolygon(xPoints, yPoints, 3);

			double theta = 2.0 * Math.PI / 3.0;
			g2.rotate(theta, xc, yc);
			g2.setColor(Color.white);
			g.fillPolygon(xPoints, yPoints, 3);
			g2.setColor(Color.black);
			g.drawPolygon(xPoints, yPoints, 3);

			g2.rotate(theta, xc, yc);
			g2.setColor(Color.white);
			g.fillPolygon(xPoints, yPoints, 3);
			g2.setColor(Color.black);
			g.drawPolygon(xPoints, yPoints, 3);

			g2.rotate(-angle - 2 * theta, xc, yc);

		}

	}

	public static class ParticleFeederIcon extends Symbol {

		private Color borderColor;
		private boolean isButtonIcon;

		public ParticleFeederIcon(Color color, Color borderColor, boolean isButtonIcon) {
			setColor(color);
			setBorderColor(borderColor);
			this.isButtonIcon = isButtonIcon;
		}

		public void setBorderColor(Color borderColor) {
			this.borderColor = borderColor;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			Graphics2D g2 = (Graphics2D) g;
			if (isButtonIcon) {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			}
			g2.setStroke(stroke);
			g2.setColor(color);
			g2.fillRoundRect(xSymbol, ySymbol, wSymbol, hSymbol, 8, 8);
			g2.setColor(borderColor);
			g2.drawRoundRect(xSymbol, ySymbol, wSymbol, hSymbol, 8, 8);
			g2.fillOval(Math.round(xSymbol + 0.5f * wSymbol - 2), Math.round(ySymbol + 0.5f * hSymbol - 2), 4, 4);
		}

	}

	public static class FanIcon extends Symbol {

		private Color borderColor;
		private float speed, angle, rotation;
		private boolean isButtonIcon;

		public FanIcon(Color color, Color borderColor, boolean isButtonIcon) {
			setColor(color);
			setBorderColor(borderColor);
			this.isButtonIcon = isButtonIcon;
		}

		public void setSpeed(float speed) {
			this.speed = speed;
		}

		public void setAngle(float angle) {
			this.angle = angle;
		}

		public void setRotation(float rotation) {
			this.rotation = rotation;
		}

		public void setBorderColor(Color borderColor) {
			this.borderColor = borderColor;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			Graphics2D g2 = (Graphics2D) g;
			if (isButtonIcon) {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			}
			Area a = Fan.getShape(new Rectangle2D.Float(xSymbol, ySymbol, wSymbol > 0 ? wSymbol : getIconWidth(), hSymbol > 0 ? hSymbol : getIconHeight()), speed, angle, (float) Math.abs(Math.sin(rotation)));
			g2.setColor(color);
			g2.fill(a);
			g2.setColor(borderColor);
			g2.setStroke(stroke);
			g2.draw(a);
		}
	}

	public static class HeliostatIcon extends Symbol {

		private Color borderColor;
		private float angle;
		private boolean isButtonIcon;

		public HeliostatIcon(Color color, Color borderColor, boolean isButtonIcon) {
			setColor(color);
			setBorderColor(borderColor);
			this.isButtonIcon = isButtonIcon;
		}

		public void setAngle(float angle) {
			this.angle = angle;
		}

		public void setBorderColor(Color borderColor) {
			this.borderColor = borderColor;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			Graphics2D g2 = (Graphics2D) g;
			if (isButtonIcon) {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			}
			Area a = Heliostat.getShape(new Rectangle2D.Float(xSymbol, ySymbol, wSymbol > 0 ? wSymbol : getIconWidth(), hSymbol > 0 ? hSymbol : getIconHeight()), angle);
			g2.setColor(color);
			g2.fill(a);
			g2.setColor(borderColor);
			g2.setStroke(stroke);
			g2.draw(a);
		}
	}

	public static class CloudIcon extends Symbol {

		private Color borderColor;
		private boolean isButtonIcon;

		public CloudIcon(boolean isButtonIcon) {
			this.isButtonIcon = isButtonIcon;
		}

		public void setBorderColor(Color borderColor) {
			this.borderColor = borderColor;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			Graphics2D g2 = (Graphics2D) g;
			if (isButtonIcon) {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			}
			Area a = Cloud.getShape(new Rectangle2D.Float(xSymbol, ySymbol, wSymbol > 0 ? wSymbol : getIconWidth(), hSymbol > 0 ? hSymbol : getIconHeight()));
			g2.setColor(color);
			g2.fill(a);
			g2.setColor(borderColor);
			g2.setStroke(stroke);
			g2.draw(a);
		}
	}

	public static class TreeIcon extends Symbol {

		private byte type;
		private Color borderColor;
		private boolean isButtonIcon;

		public TreeIcon(byte type, boolean isButtonIcon) {
			this.type = type;
			this.isButtonIcon = isButtonIcon;
		}

		public void setType(byte type) {
			this.type = type;
		}

		public void setBorderColor(Color borderColor) {
			this.borderColor = borderColor;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			Graphics2D g2 = (Graphics2D) g;
			if (isButtonIcon) {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			}
			Area a = Tree.getShape(new Rectangle2D.Float(xSymbol, ySymbol, wSymbol > 0 ? wSymbol : getIconWidth(), hSymbol > 0 ? hSymbol : getIconHeight()), type);
			g2.setColor(color);
			g2.fill(a);
			g2.setColor(borderColor);
			g2.setStroke(stroke);
			g2.draw(a);
		}
	}

	static class SwitchIcon extends Symbol {

		public SwitchIcon(Color color, int w, int h) {
			setColor(color);
			setIconWidth(w);
			setIconHeight(h);
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			Graphics2D g2 = (Graphics2D) g;
			float thickness = ((BasicStroke) stroke).getLineWidth();
			Rectangle2D.Float s = new Rectangle2D.Float(xSymbol + wSymbol * 0.2f, ySymbol + hSymbol * 0.2f, wSymbol * 0.6f, hSymbol * 0.6f);
			Arc2D.Float a = new Arc2D.Float(s, 80 - thickness * 10, thickness * 20 - 340, Arc2D.OPEN);
			g2.draw(a);
			g2.drawLine((int) (xSymbol + wSymbol * 0.5f), (int) (ySymbol + hSymbol * 0.1f), (int) (xSymbol + wSymbol * 0.5f), (int) (ySymbol + hSymbol * 0.4f));
		}

	}

	static class GraphIcon extends Symbol {

		private Stroke s2 = new BasicStroke(1);

		public GraphIcon(Color color, int w, int h) {
			setColor(color);
			setIconWidth(w);
			setIconHeight(h);
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			Graphics2D g2 = (Graphics2D) g;
			int x0 = (int) (xSymbol + wSymbol * 0.25f);
			int y0 = (int) (ySymbol + hSymbol * 0.25f);
			g2.drawRect(x0 - 1, y0 - 1, (int) (wSymbol * 0.5f) + 2, (int) (hSymbol * 0.5f) + 2);
			g2.setStroke(s2);
			if (pressed) {
				g2.drawLine(x0, y0, (int) (x0 + wSymbol * 0.5f), (int) (y0 + hSymbol * 0.5f));
				g2.drawLine(x0, (int) (y0 + hSymbol * 0.5f), (int) (x0 + wSymbol * 0.5f), y0);
			} else {
				g2.drawLine(x0, (int) (y0 + hSymbol * 0.25f), (int) (x0 + wSymbol * 0.25f), (int) (y0 + hSymbol * 0.5f));
				g2.drawLine((int) (x0 + wSymbol * 0.25f), (int) (y0 + hSymbol * 0.5f), (int) (x0 + wSymbol * 0.5f), y0);
			}
		}

	}

	static class ResetIcon extends Symbol {

		public ResetIcon(Color color, int w, int h) {
			setColor(color);
			setIconWidth(w);
			setIconHeight(h);
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			Graphics2D g2 = (Graphics2D) g;
			Rectangle2D.Float s = new Rectangle2D.Float(xSymbol + wSymbol * 0.4f, ySymbol + hSymbol * 0.3f, wSymbol * 0.4f, hSymbol * 0.4f);
			Arc2D.Float a = new Arc2D.Float(s, -90, 180, Arc2D.OPEN);
			g2.draw(a);
			int x0 = Math.round(xSymbol + wSymbol * 0.3f);
			int y0 = Math.round(ySymbol + hSymbol * 0.28f);
			g2.drawLine(Math.round(xSymbol + wSymbol * 0.55f), y0, x0, y0);
			g2.drawLine(x0, y0, x0 + 2, y0 - 2);
			g2.drawLine(x0, y0, x0 + 2, y0 + 2);
			y0 += Math.round(hSymbol * 0.4f);
			g2.drawLine(Math.round(xSymbol + wSymbol * 0.55f), y0, x0, y0);
		}

	}

	static class StartIcon extends Symbol {

		private int d = 4;
		private Stroke s2 = new BasicStroke(d);

		public StartIcon(Color color, int w, int h) {
			setColor(color);
			setIconWidth(w);
			setIconHeight(h);
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			if (pressed) {
				((Graphics2D) g).setStroke(s2);
				g.drawLine(xSymbol + wSymbol / 2 - d + 1, ySymbol + d * 2, xSymbol + wSymbol / 2 - d + 1, ySymbol + hSymbol - d * 2);
				g.drawLine(xSymbol + wSymbol / 2 + d - 1, ySymbol + d * 2, xSymbol + wSymbol / 2 + d - 1, ySymbol + hSymbol - d * 2);
			} else {
				int[] xpoints = new int[] { xSymbol + wSymbol / 2 - d, xSymbol + wSymbol - d, xSymbol + wSymbol / 2 - d };
				int[] ypoints = new int[] { ySymbol + d, ySymbol + hSymbol / 2, ySymbol + hSymbol - d };
				g.fillPolygon(new Polygon(xpoints, ypoints, 3));
			}
		}

	}

	static class NextIcon extends Symbol {

		public NextIcon(Color color, int w, int h) {
			setColor(color);
			setIconWidth(w);
			setIconHeight(h);
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			g.setColor(disabled ? Color.gray : color);
			int d = 4;
			int[] xpoints = new int[] { xSymbol + 3 * d, xSymbol + wSymbol - d, xSymbol + 3 * d };
			int[] ypoints = new int[] { ySymbol + d, ySymbol + hSymbol / 2, ySymbol + hSymbol - d };
			g.fillPolygon(new Polygon(xpoints, ypoints, 3));
			g.fillRect(xSymbol + d, ySymbol + hSymbol / 2 - d, 2 * d, 2 * d);
		}

	}

	static class PrevIcon extends Symbol {

		public PrevIcon(Color color, int w, int h) {
			setColor(color);
			setIconWidth(w);
			setIconHeight(h);
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			g.setColor(disabled ? Color.gray : color);
			int d = 4;
			int[] xpoints = new int[] { xSymbol + wSymbol - 3 * d, xSymbol + d, xSymbol + wSymbol - 3 * d };
			int[] ypoints = new int[] { ySymbol + d, ySymbol + hSymbol / 2, ySymbol + hSymbol - d };
			g.fillPolygon(new Polygon(xpoints, ypoints, 3));
			g.fillRect(xSymbol + wSymbol - 3 * d, ySymbol + hSymbol / 2 - d, 2 * d, 2 * d);
		}

	}

	static class ModeIcon extends Symbol {

		private Stroke stroke2 = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[] { 1 }, 0);

		public ModeIcon(Color color, int w, int h) {
			setColor(color);
			setIconWidth(w);
			setIconHeight(h);
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			Graphics2D g2 = (Graphics2D) g;
			if (pressed) {
				g2.setStroke(stroke2);
				g2.drawOval(xSymbol + 5, ySymbol + 5, wSymbol - 10, hSymbol - 10);
				g2.drawOval(xSymbol + 10, ySymbol + 10, wSymbol - 20, hSymbol - 20);
				g2.drawOval(xSymbol + 15, ySymbol + 15, wSymbol - 30, hSymbol - 30);
			} else {
				g2.rotate(Math.PI / 3, xSymbol + wSymbol / 2, ySymbol + hSymbol / 2);
				int d = 6;
				int[] xpoints = new int[] { xSymbol + wSymbol - 2 * d, xSymbol + d, xSymbol + wSymbol - 2 * d };
				int[] ypoints = new int[] { ySymbol + 3 * d / 2, ySymbol + hSymbol / 2, ySymbol + hSymbol - 3 * d / 2 };
				g.fillPolygon(new Polygon(xpoints, ypoints, 3));
				g.fillRect(xSymbol + wSymbol - 3 * d, ySymbol + hSymbol / 2 - d / 2, 2 * d, d);
				g2.rotate(-Math.PI / 3, xSymbol + wSymbol / 2, ySymbol + hSymbol / 2);
			}
		}

	}

	public static class ArrowHead extends Symbol {

		public ArrowHead(Color color, int w, int h) {
			setColor(color);
			setIconWidth(w);
			setIconHeight(h);
		}

		@Override
		public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
			g.setColor(color);
			final int x2 = wSymbol / 2;
			final int y2 = hSymbol / 2;
			final int[] vx = new int[] { 2, wSymbol - 2, x2 };
			final int[] vy = new int[] { y2 - 2, y2 - 2, y2 + 4 };
			g.fillPolygon(vx, vy, vx.length);
		}

	}

	static class LogoIcon extends Symbol {

		private Font font;
		private int cornerDiameter = 20;

		public LogoIcon() {
			font = new Font("Book Antiqua", Font.BOLD, 14);
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, xSymbol, ySymbol);
			Graphics2D g2 = (Graphics2D) g;
			String s = "Energy2D";
			g2.setFont(font);
			FontMetrics fm = g.getFontMetrics();
			wSymbol = fm.stringWidth(s) + 10;
			hSymbol = fm.getHeight() + fm.getDescent() + 3;
			xSymbol = x - 6;
			ySymbol = y - fm.getAscent() - 3;
			g2.setColor(Color.gray);
			g2.fillRoundRect(xSymbol, ySymbol, wSymbol, hSymbol, cornerDiameter, cornerDiameter);
			g2.setStroke(stroke);
			g2.setColor(color);
			g2.drawRoundRect(xSymbol, ySymbol, wSymbol, hSymbol, cornerDiameter, cornerDiameter);
			g2.setColor(Color.black);
			g2.drawString(s, x + 1, y - 1);
			g2.drawString(s, x + 1, y + 1);
			g2.drawString(s, x - 1, y - 1);
			g2.drawString(s, x - 1, y + 1);
			g2.setColor(Color.lightGray);
			g2.drawString(s, x, y);
		}

	}

}
