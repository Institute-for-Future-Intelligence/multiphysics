package org.energy2d.math;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Mutatable implementation of polygon (GeneralPath is immutatable).
 * 
 * @author Charles Xie
 * 
 */
public class Polygon2D implements TransformableShape {

	private Point2D.Float[] vertex;
	private GeneralPath path;

	/** the coordinates of the vertices of this polygon. */
	public Polygon2D(float[] x, float[] y) {
		if (x.length != y.length)
			throw new IllegalArgumentException("the number of x coodinates must be equal to that of the y coordinates.");
		if (x.length < 3)
			throw new IllegalArgumentException("the number of vertices must be no less than 3.");
		vertex = new Point2D.Float[x.length];
		for (int i = 0; i < x.length; i++)
			setVertex(i, x[i], y[i]);
		path = new GeneralPath();
	}

	public Polygon2D duplicate() {
		int n = vertex.length;
		float[] x = new float[n];
		float[] y = new float[n];
		for (int i = 0; i < n; i++) {
			x[i] = vertex[i].x;
			y[i] = vertex[i].y;
		}
		return new Polygon2D(x, y);
	}

	public Polygon2D insertVertexBefore(int k) {
		int n = vertex.length;
		float[] x = new float[n + 1];
		float[] y = new float[n + 1];
		if (k > 0 && k < n) {
			for (int i = 0; i < k; i++) {
				x[i] = vertex[i].x;
				y[i] = vertex[i].y;
			}
			x[k] = 0.5f * (vertex[k].x + vertex[k - 1].x);
			y[k] = 0.5f * (vertex[k].y + vertex[k - 1].y);
			for (int i = k + 1; i < n + 1; i++) {
				x[i] = vertex[i - 1].x;
				y[i] = vertex[i - 1].y;
			}
		} else if (k == 0) {
			x[0] = 0.5f * (vertex[0].x + vertex[n - 1].x);
			y[0] = 0.5f * (vertex[0].y + vertex[n - 1].y);
			for (int i = 1; i < n + 1; i++) {
				x[i] = vertex[i - 1].x;
				y[i] = vertex[i - 1].y;
			}
		} else {
			return this;
		}
		return new Polygon2D(x, y);
	}

	public Polygon2D deleteVertexBefore(int k) {
		int n = vertex.length;
		if (n < 4)
			return this;
		float[] x = new float[n - 1];
		float[] y = new float[n - 1];
		if (k > 0 && k < n) {
			for (int i = 0; i < k; i++) {
				x[i] = vertex[i].x;
				y[i] = vertex[i].y;
			}
			for (int i = k + 1; i < n; i++) {
				x[i - 1] = vertex[i].x;
				y[i - 1] = vertex[i].y;
			}
		} else if (k == 0) {
			for (int i = 1; i < n; i++) {
				x[i - 1] = vertex[i - 1].x;
				y[i - 1] = vertex[i - 1].y;
			}
		} else {
			return this;
		}
		return new Polygon2D(x, y);
	}

	public boolean isClockwise() {
		float sum = 0;
		int n = vertex.length;
		for (int i = 0; i < n - 1; i++)
			sum += (vertex[i + 1].x - vertex[i].x) * (vertex[i + 1].y + vertex[i].y);
		sum += (vertex[0].x - vertex[n - 1].x) * (vertex[0].y + vertex[n - 1].y);
		return sum > 0;
	}

	private void update() {
		synchronized (path) {
			path.reset();
			path.moveTo(vertex[0].x, vertex[0].y);
			for (int i = 1; i < vertex.length; i++)
				path.lineTo(vertex[i].x, vertex[i].y);
			path.closePath();
		}
	}

	public void setVertices(List<Point2D.Float> points) {
		if (points.size() < 3)
			throw new IllegalArgumentException("the number of vertices must be no less than 3.");
		if (vertex == null || points.size() != vertex.length)
			path = new GeneralPath();
		vertex = new Point2D.Float[points.size()];
		for (int i = 0; i < vertex.length; i++) {
			Point2D.Float pi = points.get(i);
			setVertex(i, pi.x, pi.y);
		}
	}

	public void setVertex(int i, float x, float y) {
		if (i < 0 || i >= vertex.length)
			throw new IllegalArgumentException("index of vertex is out of bound.");
		if (vertex[i] == null)
			vertex[i] = new Point2D.Float(x, y);
		else
			vertex[i].setLocation(x, y);
	}

	public Point2D.Float getVertex(int i) {
		if (i < 0 || i >= vertex.length)
			throw new IllegalArgumentException("index of vertex is out of bound.");
		return vertex[i];
	}

	public int getVertexCount() {
		return vertex.length;
	}

	public void translateBy(float dx, float dy) {
		for (Point2D.Float p : vertex) {
			p.x += dx;
			p.y += dy;
		}
	}

	public void rotateBy(float degree) {
		Rectangle2D r = path.getBounds2D();
		double cx = r.getCenterX();
		double cy = r.getCenterY();
		double a = Math.toRadians(degree);
		double sin = Math.sin(a);
		double cos = Math.cos(a);
		double dx = 0;
		double dy = 0;
		for (Point2D.Float v : vertex) {
			dx = v.x - cx;
			dy = v.y - cy;
			v.x = (float) (dx * cos - dy * sin + cx);
			v.y = (float) (dx * sin + dy * cos + cy);
		}
	}

	public void scale(float scale) {
		Rectangle2D r = path.getBounds2D();
		double cx = r.getCenterX();
		double cy = r.getCenterY();
		for (Point2D.Float v : vertex) {
			v.x = (float) ((v.x - cx) * scale + cx);
			v.y = (float) ((v.y - cy) * scale + cy);
		}
	}

	public void scaleX(float scale) {
		Rectangle2D r = path.getBounds2D();
		double cx = r.getCenterX();
		for (Point2D.Float v : vertex) {
			v.x = (float) ((v.x - cx) * scale + cx);
		}
	}

	public void scaleY(float scale) {
		Rectangle2D r = path.getBounds2D();
		double cy = r.getCenterY();
		for (Point2D.Float v : vertex) {
			v.y = (float) ((v.y - cy) * scale + cy);
		}
	}

	public void shearX(float shear) {
		Rectangle2D r = path.getBounds2D();
		double cy = r.getCenterY();
		for (Point2D.Float v : vertex) {
			v.x += (float) (v.y - cy) * shear;
		}
	}

	public void shearY(float shear) {
		Rectangle2D r = path.getBounds2D();
		double cx = r.getCenterX();
		for (Point2D.Float v : vertex) {
			v.y += (float) (v.x - cx) * shear;
		}
	}

	public void flipX() {
		float cx = (float) path.getBounds2D().getCenterX();
		float dx = 0;
		for (Point2D.Float v : vertex) {
			dx = v.x - cx;
			v.x = cx - dx;
		}
	}

	public void flipY() {
		float cy = (float) path.getBounds2D().getCenterY();
		float dy = 0;
		for (Point2D.Float v : vertex) {
			dy = v.y - cy;
			v.y = cy - dy;
		}
	}

	public boolean contains(Point2D p) {
		return contains(p.getX(), p.getY());
	}

	public boolean intersects(Rectangle r) {
		update();
		return path.intersects(r);
	}

	public boolean contains(double x, double y) {
		update();
		return path.contains(x, y);
	}

	public Point2D.Float getBoundCenter() {
		Rectangle2D r = path.getBounds2D();
		return new Point2D.Float((float) r.getCenterX(), (float) r.getCenterY());
	}

	public void translateCenterTo(float x, float y) {
		Point2D.Float center = getCenter();
		translateBy(x - center.x, y - center.y);
	}

	public Point2D.Float getCenter() {
		float xc = 0;
		float yc = 0;
		for (Point2D.Float v : vertex) {
			xc += v.x;
			yc += v.y;
		}
		return new Point2D.Float(xc / vertex.length, yc / vertex.length);
	}

	public float getArea() {
		float area = 0;
		int n = vertex.length;
		Point2D.Float v1, v2;
		for (int i = 0; i < n - 1; i++) {
			v1 = vertex[i];
			v2 = vertex[i + 1];
			area += v1.getX() * v2.getY() - v2.getX() * v1.getY();
		}
		v1 = vertex[n - 1];
		v2 = vertex[0];
		area += v1.getX() * v2.getY() - v2.getX() * v1.getY();
		return area * 0.5f;
	}

	public Rectangle getBounds() {
		int xmin = Integer.MAX_VALUE;
		int ymin = xmin;
		int xmax = -xmin;
		int ymax = -xmin;
		for (Point2D.Float v : vertex) {
			if (xmin > v.x)
				xmin = Math.round(v.x);
			if (ymin > v.y)
				ymin = Math.round(v.y);
			if (xmax < v.x)
				xmax = Math.round(v.x);
			if (ymax < v.y)
				ymax = Math.round(v.y);
		}
		return new Rectangle(xmin, ymin, xmax - xmin, ymax - ymin);
	}

	public Rectangle2D getBounds2D() {
		float xmin = Float.MAX_VALUE;
		float ymin = xmin;
		float xmax = -xmin;
		float ymax = -xmin;
		for (Point2D.Float v : vertex) {
			if (xmin > v.x)
				xmin = v.x;
			if (ymin > v.y)
				ymin = v.y;
			if (xmax < v.x)
				xmax = v.x;
			if (ymax < v.y)
				ymax = v.y;
		}
		return new Rectangle2D.Float(xmin, ymin, xmax - xmin, ymax - ymin);
	}

	public boolean contains(Rectangle2D r) {
		update();
		return path.contains(r);
	}

	public boolean contains(double x, double y, double w, double h) {
		update();
		return path.contains(x, y, w, h);
	}

	public PathIterator getPathIterator(AffineTransform at) {
		update();
		return path.getPathIterator(at);
	}

	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		update();
		return path.getPathIterator(at, flatness);
	}

	public boolean intersects(Rectangle2D r) {
		update();
		return path.intersects(r);
	}

	public boolean intersects(double x, double y, double w, double h) {
		update();
		return path.intersects(x, y, w, h);
	}

}
