package org.energy2d.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * @author Charles Xie
 * 
 */

public final class TextureFactory {

	private final static BasicStroke ultrathin = new BasicStroke(.5f);
	private final static BasicStroke thin = new BasicStroke(1);

	public final static byte SMALL = 101;
	public final static byte MEDIUM = 102;
	public final static byte LARGE = 103;
	public final static byte HUGE = 104;

	public final static byte POLKA = 1;
	public final static byte MOSIAC = 2;
	public final static byte POSITIVE = 3;
	public final static byte NEGATIVE = 4;
	public final static byte STARRY = 5;
	public final static byte CIRCULAR = 6;
	public final static byte HORIZONTAL_STRIPE = 7;
	public final static byte VERTICAL_STRIPE = 8;
	public final static byte DIAGONAL_UP_STRIPE = 9;
	public final static byte DIAGONAL_DOWN_STRIPE = 10;
	public final static byte GRID = 11;
	public final static byte HORIZONTAL_BRICK = 12;
	public final static byte INSULATION = 13;
	public final static byte FINE_SCREEN = 14;
	public final static byte CONCRETE = 15;
	public final static byte DOT_ARRAY = 16;
	public final static byte SINGLE_CIRCLE = 17;
	public final static byte DOUBLE_CIRCLES = 18;
	public final static byte HORIZONTAL_LATTICE = 19;
	public final static byte TRIANGLE_HALF = 20;
	public final static byte DICE = 21;
	public final static byte DIAGONAL_CROSS = 22;
	public final static byte STONE_WALL = 23;

	final static ArrayList<TextureCode> textureList = new ArrayList<TextureCode>();

	static {
		textureList.add(new TextureCode(HORIZONTAL_STRIPE, SMALL));
		textureList.add(new TextureCode(HORIZONTAL_STRIPE, MEDIUM));
		textureList.add(new TextureCode(VERTICAL_STRIPE, SMALL));
		textureList.add(new TextureCode(VERTICAL_STRIPE, MEDIUM));
		textureList.add(new TextureCode(DIAGONAL_UP_STRIPE, MEDIUM));
		textureList.add(new TextureCode(DIAGONAL_UP_STRIPE, LARGE));
		textureList.add(new TextureCode(DIAGONAL_DOWN_STRIPE, MEDIUM));
		textureList.add(new TextureCode(DIAGONAL_DOWN_STRIPE, LARGE));
		textureList.add(new TextureCode(GRID, SMALL));
		textureList.add(new TextureCode(GRID, MEDIUM));
		textureList.add(new TextureCode(HORIZONTAL_BRICK, MEDIUM));
		textureList.add(new TextureCode(HORIZONTAL_BRICK, LARGE));
		textureList.add(new TextureCode(CONCRETE, HUGE));
		textureList.add(new TextureCode(STONE_WALL, HUGE));
		textureList.add(new TextureCode(DOT_ARRAY, SMALL));
		textureList.add(new TextureCode(DOT_ARRAY, LARGE));
		textureList.add(new TextureCode(SINGLE_CIRCLE, MEDIUM));
		textureList.add(new TextureCode(SINGLE_CIRCLE, LARGE));
		textureList.add(new TextureCode(DOUBLE_CIRCLES, MEDIUM));
		textureList.add(new TextureCode(DOUBLE_CIRCLES, LARGE));
		textureList.add(new TextureCode(HORIZONTAL_LATTICE, MEDIUM));
		textureList.add(new TextureCode(HORIZONTAL_LATTICE, LARGE));
		textureList.add(new TextureCode(DICE, MEDIUM));
		textureList.add(new TextureCode(DIAGONAL_CROSS, LARGE));
		textureList.add(new TextureCode(DIAGONAL_CROSS, MEDIUM));
		textureList.add(new TextureCode(TRIANGLE_HALF, SMALL));
		textureList.add(new TextureCode(POLKA, SMALL));
		textureList.add(new TextureCode(POLKA, MEDIUM));
		textureList.add(new TextureCode(MOSIAC, SMALL));
		textureList.add(new TextureCode(MOSIAC, MEDIUM));
		textureList.add(new TextureCode(POSITIVE, MEDIUM));
		textureList.add(new TextureCode(NEGATIVE, MEDIUM));
		textureList.add(new TextureCode(STARRY, LARGE));
		textureList.add(new TextureCode(CIRCULAR, LARGE));
		textureList.add(new TextureCode(FINE_SCREEN, MEDIUM));
		textureList.add(new TextureCode(INSULATION, HUGE));
	}

	private static Rectangle r = new Rectangle();

	public static TexturePaint createPattern(int type, int w, int h, Color c1, Color c2) {

		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setStroke(thin);
		switch (type) {
		case POLKA:
			int x = w / 4;
			int y = h / 4;
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.fillOval(x, y, x + x, y + y);
			r.setBounds(x, y, w, h);
			return new TexturePaint(bi, r);
		case MOSIAC:
			x = w / 2;
			y = h / 2;
			g.setColor(c1);
			g.fillRect(0, 0, x, y);
			g.fillRect(x, y, x, y);
			g.setColor(c2);
			g.fillRect(x, 0, x, y);
			g.fillRect(0, y, x, y);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case POSITIVE:
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			x = w / 2 + 2;
			y = h / 2 + 2;
			g.drawLine(1, y, 3, y);
			g.drawLine(2, y - 1, 2, y + 1);
			g.drawLine(x, 1, x, 3);
			g.drawLine(x - 1, 2, x + 1, 2);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case NEGATIVE:
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			x = w / 2 + 2;
			y = h / 2 + 2;
			g.drawLine(1, y, 3, y);
			g.drawLine(x - 1, 2, x + 1, 2);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case CIRCULAR:
			for (int i = 0; i < w / 2; i++) {
				for (int j = 0; j < h / 2; j++) {
					g.setColor(c1);
					g.fillRect(2 * i, 2 * j, 1, 1);
					g.fillRect(2 * i + 1, 2 * j + 1, 1, 1);
					g.setColor(c2);
					g.fillRect(2 * i + 1, 2 * j, 1, 1);
					g.fillRect(2 * i, 2 * j + 1, 1, 1);
				}
			}
			g.setColor(c2);
			g.drawLine(1, h / 2, 3, h / 2);
			g.drawLine(2, h / 2 - 1, 2, h / 2 + 1);
			g.drawLine(w / 2, 1, w / 2, 3);
			g.drawLine(w / 2 - 1, 2, w / 2 + 1, 2);
			r.setBounds(0, 0, w - 2, h - 2);
			return new TexturePaint(bi, r);
		case FINE_SCREEN:
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			for (int i = 0; i < w / 2; i++) {
				for (int j = 0; j < h / 2; j++) {
					g.fillRect(2 * i, 2 * j, 1, 1);
				}
			}
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case CONCRETE:
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.drawOval(5, 8, 6, 5);
			g.drawOval(15, 20, 5, 6);
			g.drawOval(25, 10, 4, 4);
			g.drawOval(27, 29, 4, 3);
			g.drawOval(3, 26, 3, 5);
			g.drawRect(4, 14, 1, 1);
			g.drawRect(17, 32, 1, 1);
			g.drawRect(5, 26, 1, 1);
			g.drawRect(13, 27, 1, 1);
			g.drawRect(24, 24, 1, 1);
			g.drawRect(21, 2, 1, 1);
			g.drawRect(17, 5, 1, 1);
			g.drawRect(22, 15, 1, 1);
			g.drawRect(9, 27, 1, 1);
			g.drawRect(31, 8, 1, 1);
			g.drawRect(11, 15, 1, 1);
			g.drawRect(18, 11, 1, 1);
			g.drawRect(23, 8, 1, 1);
			g.drawRect(3, 5, 1, 1);
			g.drawRect(8, 17, 1, 1);
			g.drawRect(31, 19, 1, 1);
			g.drawRect(11, 31, 1, 1);
			g.drawRect(8, 4, 1, 1);
			g.drawRect(3, 19, 1, 1);
			g.drawRect(22, 27, 1, 1);
			g.drawRect(3, 33, 1, 1);
			g.drawRect(23, 17, 1, 1);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case DOT_ARRAY:
			x = w / 2 + 1;
			y = h / 2 + 1;
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.fillRect(1, y, 1, 1);
			g.fillRect(x, 1, 1, 1);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case STARRY:
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			for (int i = 0; i < w / 2; i++) {
				for (int j = 0; j < h / 2; j++) {
					g.fillRect(2 * i + 1, 2 * j, 1, 1);
					g.fillRect(2 * i, 2 * j + 1, 1, 1);
				}
			}
			x = w / 2 + 2;
			y = h / 2 + 2;
			g.drawLine(1, y, 3, y);
			g.drawLine(2, y - 1, 2, y + 1);
			g.drawLine(x, 1, x, 3);
			g.drawLine(x - 1, 2, x + 1, 2);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case HORIZONTAL_STRIPE:
			y = h / 2;
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.drawLine(0, y, w, y);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case VERTICAL_STRIPE:
			x = w / 2;
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.drawLine(x, 0, x, h);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case DIAGONAL_UP_STRIPE:
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.setStroke(ultrathin);
			g.drawLine(0, h - 1, w - 1, 0);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case DIAGONAL_DOWN_STRIPE:
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.setStroke(ultrathin);
			g.drawLine(0, 0, w, h);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case GRID:
			x = w / 2;
			y = h / 2;
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.drawLine(0, h / 2, w, h / 2);
			g.drawLine(w / 2, 0, w / 2, h);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case HORIZONTAL_BRICK:
			x = w / 2;
			y = h / 2;
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.drawLine(0, 0, w, 0);
			g.drawLine(0, y, w, y);
			g.drawLine(0, 0, 0, y);
			g.drawLine(x, y, x, h);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case SINGLE_CIRCLE:
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.drawOval(0, 0, w, h);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case DOUBLE_CIRCLES:
			x = w / 4;
			y = h / 4;
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.drawOval(0, 0, w, h);
			g.drawOval(x, y, x + x < w / 2 ? x + x + 2 : w / 2, y + y < h / 2 ? y + y + 2 : h / 2);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case HORIZONTAL_LATTICE:
			x = w / 2;
			y = h / 2;
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.drawOval(0, 0, x, y);
			g.drawLine(x, y / 2, w, y / 2);
			g.drawLine(x / 2, y, x / 2, h);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case DICE:
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.drawRect(0, 0, w, h);
			g.fillOval(w / 2 - 2, h / 2 - 2, 5, 5);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case TRIANGLE_HALF:
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			Polygon triangle = new Polygon();
			triangle.addPoint(0, 0);
			triangle.addPoint(w, 0);
			triangle.addPoint(0, h);
			g.fillPolygon(triangle);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case DIAGONAL_CROSS:
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.drawLine(0, 0, w, h);
			g.drawLine(w, 0, 0, h);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case STONE_WALL:
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.drawLine(5, 0, 6, 7);
			g.drawLine(6, 7, 0, 10);
			g.drawLine(6, 7, 16, 9);
			g.drawLine(16, 9, 23, 0);
			g.drawLine(16, 9, 15, 18);
			g.drawLine(15, 18, 20, 22);
			g.drawLine(15, 18, 4, 25);
			g.drawLine(4, 25, 0, 20);
			g.drawLine(4, 25, 8, 30);
			g.drawLine(9, 30, 4, 35);
			g.drawLine(4, 35, 0, 33);
			g.drawLine(9, 30, 19, 31);
			g.drawLine(19, 31, 23, 35);
			g.drawLine(19, 31, 20, 22);
			g.drawLine(20, 22, 29, 18);
			g.drawLine(29, 18, 31, 11);
			g.drawLine(31, 11, 19, 6);
			g.drawLine(31, 11, 35, 9);
			g.drawLine(35, 20, 29, 27);
			g.drawLine(29, 27, 35, 33);
			g.drawLine(35, 9, 29, 0);
			g.drawLine(29, 0, 23, 0);
			g.drawLine(35, 33, 29, 35);
			g.drawLine(35, 19, 29, 18);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case INSULATION:
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			int a = w / 3;
			int b = h / 3;
			int u = 0;
			for (int i = 0; i < 3; i++) {
				g.drawArc(u, 0, a, b, 0, 180);
				g.drawLine(u, b / 2, u + a / 2, h - b / 2);
				g.drawLine(u + a / 2, h - b / 2, u + a, b / 2);
				g.drawArc(u - a / 2, h - b, a, b, 0, -91);
				g.drawArc(u + a / 2, h - b, a, b, 180, 91);
				u += a;
			}
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		}
		return null;
	}

	/** draw special representations such as standard fill patterns for insulations */
	public static void renderSpecialCases(Shape s, Texture tex, Graphics2D g) {
		if (s instanceof Rectangle) {
			Rectangle r = (Rectangle) s;
			if (tex.getStyle() == TextureFactory.INSULATION) {
				g.setColor(new Color(tex.getForeground()));
				int m = 20; // default number of repeats
				int minLength = 20;
				Arc2D.Float arc = new Arc2D.Float();
				Line2D.Float line = new Line2D.Float();
				if (r.width > r.height) {
					float a = (float) r.width / (float) m;
					if (a < minLength) {
						m = Math.round((float) r.width / (float) minLength);
						a = (float) r.width / (float) m;
					}
					float b = (float) r.height / 3f;
					arc.width = a;
					arc.height = b;
					float u = r.x;
					for (int i = 0; i < m; i++) {
						arc.x = u;
						arc.y = r.y;
						arc.start = 0;
						arc.extent = 180;
						g.draw(arc);
						line.x1 = u;
						line.y1 = r.y + b * 0.5f;
						line.x2 = u + a * 0.5f;
						line.y2 = r.y + r.height - b * 0.5f;
						g.draw(line);
						line.x1 = u + a;
						g.draw(line);
						arc.x = u - a * 0.5f;
						arc.y = r.y + r.height - b;
						arc.extent = -91;
						g.draw(arc);
						arc.x = u + a * 0.5f;
						arc.start = 270;
						g.draw(arc);
						u += a;
					}
				} else {
					float a = (float) r.height / (float) m;
					if (a < minLength) {
						m = Math.round((float) r.height / (float) minLength);
						a = (float) r.height / (float) m;
					}
					float b = (float) r.width / 3f;
					arc.width = b;
					arc.height = a;
					float v = r.y;
					for (int i = 0; i < m; i++) {
						arc.y = v;
						arc.x = r.x;
						arc.start = 90;
						arc.extent = 180;
						g.draw(arc);
						line.y1 = v;
						line.x1 = r.x + b * 0.5f;
						line.y2 = v + a * 0.5f;
						line.x2 = r.x + r.width - b * 0.5f;
						g.draw(line);
						line.y1 = v + a;
						g.draw(line);
						arc.y = v - a * 0.5f;
						arc.x = r.x + r.width - b;
						arc.start = 0;
						arc.extent = -91;
						g.draw(arc);
						arc.y = v + a * 0.5f;
						arc.start = 90;
						g.draw(arc);
						v += a;
					}
				}
			}
		}
	}

}
