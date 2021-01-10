package org.energy2d.math;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A blob made of splines (by default, Catmull-Rom) going through a set of points. For efficiency, the splines will not be automatically recalculated when the points are modified.
 * 
 * The update() method must be called before doing anything. It is up to the developer to determine when is the optimal time to call the method.
 * 
 * @author Charles Xie
 * 
 */
public class Blob2D implements TransformableShape {

	private Point2D.Float[] points;
	private GeneralPath path;
	private int steps = 20;
	private float invStep = 1f / steps;
	private float[] px = new float[4];
	private float[] py = new float[4];
	private List<Point2D.Float> pathPoints;

	/** the coordinates of the points */
	public Blob2D(float[] x, float[] y) {
		if (x.length != y.length)
			throw new IllegalArgumentException("the number of x coodinates must be equal to that of the y coordinates.");
		if (x.length < 3)
			throw new IllegalArgumentException("the number of points must be no less than 3.");
		points = new Point2D.Float[x.length];
		for (int i = 0; i < x.length; i++)
			setPoint(i, x[i], y[i]);
		path = new GeneralPath();
		pathPoints = new ArrayList<Point2D.Float>();
		update();
	}

	/** the coordinates of the points */
	public Blob2D(int[] x, int[] y) {
		if (x.length != y.length)
			throw new IllegalArgumentException("the number of x coodinates must be equal to that of the y coordinates.");
		if (x.length < 3)
			throw new IllegalArgumentException("the number of points must be no less than 3.");
		points = new Point2D.Float[x.length];
		for (int i = 0; i < x.length; i++)
			setPoint(i, x[i], y[i]);
		path = new GeneralPath();
		pathPoints = new ArrayList<Point2D.Float>();
		update();
	}

	/** converted from a polygon */
	public Blob2D(Polygon p) {
		points = new Point2D.Float[p.npoints];
		for (int i = 0; i < points.length; i++)
			setPoint(i, p.xpoints[i], p.ypoints[i]);
		path = new GeneralPath();
		pathPoints = new ArrayList<Point2D.Float>();
		update();
	}

	public boolean isClockwise() {
		float sum = 0;
		int n = points.length;
		for (int i = 0; i < n - 1; i++)
			sum += (points[i + 1].x - points[i].x) * (points[i + 1].y + points[i].y);
		sum += (points[0].x - points[n - 1].x) * (points[0].y + points[n - 1].y);
		return sum > 0;
	}

	public Blob2D duplicate() {
		int n = points.length;
		float[] x = new float[n];
		float[] y = new float[n];
		for (int i = 0; i < n; i++) {
			x[i] = points[i].x;
			y[i] = points[i].y;
		}
		return new Blob2D(x, y);
	}

	public Blob2D insertPointBefore(int k) {
		int n = points.length;
		float[] x = new float[n + 1];
		float[] y = new float[n + 1];
		if (k > 0 && k < n) {
			for (int i = 0; i < k; i++) {
				x[i] = points[i].x;
				y[i] = points[i].y;
			}
			x[k] = 0.5f * (points[k].x + points[k - 1].x);
			y[k] = 0.5f * (points[k].y + points[k - 1].y);
			for (int i = k + 1; i < n + 1; i++) {
				x[i] = points[i - 1].x;
				y[i] = points[i - 1].y;
			}
		} else if (k == 0) {
			x[0] = 0.5f * (points[0].x + points[n - 1].x);
			y[0] = 0.5f * (points[0].y + points[n - 1].y);
			for (int i = 1; i < n + 1; i++) {
				x[i] = points[i - 1].x;
				y[i] = points[i - 1].y;
			}
		} else {
			return this;
		}
		return new Blob2D(x, y);
	}

	public Blob2D deletePointBefore(int k) {
		int n = points.length;
		if (n < 4)
			return this;
		float[] x = new float[n - 1];
		float[] y = new float[n - 1];
		if (k > 0 && k < n) {
			for (int i = 0; i < k; i++) {
				x[i] = points[i].x;
				y[i] = points[i].y;
			}
			for (int i = k + 1; i < n; i++) {
				x[i - 1] = points[i].x;
				y[i - 1] = points[i].y;
			}
		} else if (k == 0) {
			for (int i = 1; i < n; i++) {
				x[i - 1] = points[i - 1].x;
				y[i - 1] = points[i - 1].y;
			}
		} else {
			return this;
		}
		return new Blob2D(x, y);
	}

	public GeneralPath getPath() {
		return path;
	}

	public float getPerimeter() {
		float perimeter = 0;
		int n = pathPoints.size();
		float dx, dy;
		Point2D.Float p1, p2 = null;
		for (int i = 0; i < n - 1; i++) {
			p1 = pathPoints.get(i);
			p2 = pathPoints.get(i + 1);
			dx = p2.x - p1.x;
			dy = p2.y - p1.y;
			perimeter += Math.hypot(dx, dy);
		}
		if (p2 != null) {
			p1 = pathPoints.get(0);
			dx = p2.x - p1.x;
			dy = p2.y - p1.y;
			perimeter += Math.hypot(dx, dy);
		}
		return perimeter;
	}

	public void update() {
		path.reset();
		pathPoints.clear();
		int n = points.length;
		path.moveTo(points[n - 1].x, points[n - 1].y);
		pathPoints.add(new Point2D.Float(points[n - 1].x, points[n - 1].y));
		float u;
		float sx, sy;
		int index;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < 4; j++) { // Initialize points m-2, m-1, m, m+1
				index = (i + j - 2 + n) % n;
				px[j] = points[index].x;
				py[j] = points[index].y;
			}
			for (int k = 0; k < steps; k++) {
				u = k * invStep;
				sx = catmullrom(-2, u) * px[0] + catmullrom(-1, u) * px[1] + catmullrom(0, u) * px[2] + catmullrom(1, u) * px[3];
				sy = catmullrom(-2, u) * py[0] + catmullrom(-1, u) * py[1] + catmullrom(0, u) * py[2] + catmullrom(1, u) * py[3];
				sx *= 0.5f;
				sy *= 0.5f;
				path.lineTo(sx, sy);
				pathPoints.add(new Point2D.Float(sx, sy));
			}
		}
		path.closePath();
	}

	public int getPathPointCount() {
		return pathPoints.size();
	}

	public Point2D.Float getPathPoint(int i) {
		if (i < 0 || i >= pathPoints.size())
			throw new IllegalArgumentException("index is out of bound.");
		return pathPoints.get(i);
	}

	public void setPoints(List<Point2D.Float> p) {
		if (p.size() < 3)
			throw new IllegalArgumentException("the number of points must be no less than 3.");
		if (points == null || p.size() != points.length)
			path = new GeneralPath();
		points = new Point2D.Float[p.size()];
		for (int i = 0; i < points.length; i++) {
			Point2D.Float pi = p.get(i);
			setPoint(i, pi.x, pi.y);
		}
		update();
	}

	public void setPoint(int i, float x, float y) {
		if (i < 0 || i >= points.length)
			throw new IllegalArgumentException("index is out of bound.");
		if (points[i] == null)
			points[i] = new Point2D.Float(x, y);
		else
			points[i].setLocation(x, y);
	}

	public Point2D.Float getPoint(int i) {
		if (i < 0 || i >= points.length)
			throw new IllegalArgumentException("index is out of bound.");
		return points[i];
	}

	public int getPointCount() {
		return points.length;
	}

	public void translateBy(float dx, float dy) {
		for (Point2D.Float p : points) {
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
		for (Point2D.Float v : points) {
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
		for (Point2D.Float v : points) {
			v.x = (float) ((v.x - cx) * scale + cx);
			v.y = (float) ((v.y - cy) * scale + cy);
		}
	}

	public void scaleX(float scale) {
		Rectangle2D r = path.getBounds2D();
		double cx = r.getCenterX();
		for (Point2D.Float v : points) {
			v.x = (float) ((v.x - cx) * scale + cx);
		}
	}

	public void scaleY(float scale) {
		Rectangle2D r = path.getBounds2D();
		double cy = r.getCenterY();
		for (Point2D.Float v : points) {
			v.y = (float) ((v.y - cy) * scale + cy);
		}
	}

	public void shearX(float shear) {
		Rectangle2D r = path.getBounds2D();
		double cy = r.getCenterY();
		for (Point2D.Float v : points) {
			v.x += (float) (v.y - cy) * shear;
		}
	}

	public void shearY(float shear) {
		Rectangle2D r = path.getBounds2D();
		double cx = r.getCenterX();
		for (Point2D.Float v : points) {
			v.y += (float) (v.x - cx) * shear;
		}
	}

	public void flipX() {
		float cx = (float) path.getBounds2D().getCenterX();
		float dx = 0;
		for (Point2D.Float v : points) {
			dx = v.x - cx;
			v.x = cx - dx;
		}
	}

	public void flipY() {
		float cy = (float) path.getBounds2D().getCenterY();
		float dy = 0;
		for (Point2D.Float v : points) {
			dy = v.y - cy;
			v.y = cy - dy;
		}
	}

	public boolean contains(Point2D p) {
		return contains(p.getX(), p.getY());
	}

	public boolean intersects(Rectangle r) {
		return path.intersects(r);
	}

	public boolean contains(double x, double y) {
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
		for (Point2D.Float v : points) {
			xc += v.x;
			yc += v.y;
		}
		return new Point2D.Float(xc / points.length, yc / points.length);
	}

	// TODO: Should use the smooth path
	public float getArea() {
		float area = 0;
		int n = points.length;
		Point2D.Float v1, v2;
		for (int i = 0; i < n - 1; i++) {
			v1 = points[i];
			v2 = points[i + 1];
			area += v1.getX() * v2.getY() - v2.getX() * v1.getY();
		}
		v1 = points[n - 1];
		v2 = points[0];
		area += v1.getX() * v2.getY() - v2.getX() * v1.getY();
		return area * 0.5f;
	}

	public Rectangle getBounds() {
		return path.getBounds();
	}

	public Rectangle2D getBounds2D() {
		return path.getBounds2D();
	}

	public boolean contains(Rectangle2D r) {
		return path.contains(r);
	}

	public boolean contains(double x, double y, double w, double h) {
		return path.contains(x, y, w, h);
	}

	public PathIterator getPathIterator(AffineTransform at) {
		return path.getPathIterator(at);
	}

	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return path.getPathIterator(at, flatness);
	}

	public boolean intersects(Rectangle2D r) {
		return path.intersects(r);
	}

	public boolean intersects(double x, double y, double w, double h) {
		return path.intersects(x, y, w, h);
	}

	// Catmull-Rom spline function
	private static float catmullrom(int i, float u) {
		switch (i) {
		case -2:
			return u * (u * (2 - u) - 1);
		case -1:
			return u * u * (3 * u - 5) + 2;
		case 0:
			return u * ((4 - 3 * u) * u + 1);
		case 1:
			return u * u * (u - 1);
		}
		return 0;
	}

}
