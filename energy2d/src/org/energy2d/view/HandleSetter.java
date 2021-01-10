package org.energy2d.view;

import static org.energy2d.view.View2D.BOTTOM;
import static org.energy2d.view.View2D.LEFT;
import static org.energy2d.view.View2D.LOWER_LEFT;
import static org.energy2d.view.View2D.LOWER_RIGHT;
import static org.energy2d.view.View2D.RIGHT;
import static org.energy2d.view.View2D.TOP;
import static org.energy2d.view.View2D.UPPER_LEFT;
import static org.energy2d.view.View2D.UPPER_RIGHT;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import org.energy2d.math.Annulus;
import org.energy2d.math.Blob2D;
import org.energy2d.math.EllipticalAnnulus;
import org.energy2d.math.Polygon2D;
import org.energy2d.model.Cloud;
import org.energy2d.model.Manipulable;
import org.energy2d.model.Sensor;
import org.energy2d.model.Tree;

/**
 * @author Charles Xie
 * 
 */
class HandleSetter {

	static void setRects(View2D view, Manipulable m, Rectangle2D.Float[] handle) {

		if (m instanceof Sensor)
			return;

		float h = handle[0].width / 2;
		Shape s = m.getShape();

		if (s instanceof RectangularShape) {
			setRectHandles(view, s.getBounds2D(), handle, h);
		} else if (s instanceof Polygon2D) {
			Polygon2D p = (Polygon2D) s;
			int n = p.getVertexCount();
			Point2D.Float point;
			if (n <= handle.length) {
				for (int i = 0; i < handle.length; i++) {
					if (i < n) {
						point = p.getVertex(i);
						handle[i].x = view.convertPointToPixelXf(point.x) - h;
						handle[i].y = view.convertPointToPixelYf(point.y) - h;
					} else {
						handle[i].x = handle[i].y = -100;
					}
				}
			} else {
				float k = (float) n / (float) handle.length;
				for (int i = 0; i < handle.length; i++) {
					point = p.getVertex((int) (i * k));
					handle[i].x = view.convertPointToPixelXf(point.x) - h;
					handle[i].y = view.convertPointToPixelYf(point.y) - h;
				}
			}
		} else if (s instanceof Blob2D) {
			Blob2D b = (Blob2D) s;
			int n = b.getPointCount();
			Point2D.Float point;
			if (n <= handle.length) {
				for (int i = 0; i < handle.length; i++) {
					if (i < n) {
						point = b.getPoint(i);
						handle[i].x = view.convertPointToPixelXf(point.x) - h;
						handle[i].y = view.convertPointToPixelYf(point.y) - h;
					} else {
						handle[i].x = handle[i].y = -100;
					}
				}
			} else {
				float k = (float) n / (float) handle.length;
				for (int i = 0; i < handle.length; i++) {
					point = b.getPoint((int) (i * k));
					handle[i].x = view.convertPointToPixelXf(point.x) - h;
					handle[i].y = view.convertPointToPixelYf(point.y) - h;
				}
			}
		} else if (s instanceof Annulus) {
			setRectHandles(view, s.getBounds2D(), handle, h);
		} else if (s instanceof EllipticalAnnulus) {

			EllipticalAnnulus e = (EllipticalAnnulus) s;

			Rectangle2D.Float outerBound = new Rectangle2D.Float(e.getX() - e.getOuterA(), e.getY() - e.getOuterB(), 2 * e.getOuterA(), 2 * e.getOuterB());
			handle[UPPER_LEFT].x = view.convertPointToPixelXf((float) outerBound.getMinX()) - h;
			handle[UPPER_LEFT].y = view.convertPointToPixelYf((float) outerBound.getMinY()) - h;
			handle[LOWER_LEFT].x = view.convertPointToPixelXf((float) outerBound.getMinX()) - h;
			handle[LOWER_LEFT].y = view.convertPointToPixelYf((float) outerBound.getMaxY()) - h;
			handle[UPPER_RIGHT].x = view.convertPointToPixelXf((float) outerBound.getMaxX()) - h;
			handle[UPPER_RIGHT].y = view.convertPointToPixelYf((float) outerBound.getMinY()) - h;
			handle[LOWER_RIGHT].x = view.convertPointToPixelXf((float) outerBound.getMaxX()) - h;
			handle[LOWER_RIGHT].y = view.convertPointToPixelYf((float) outerBound.getMaxY()) - h;
			handle[TOP].x = view.convertPointToPixelXf((float) outerBound.getCenterX()) - h;
			handle[TOP].y = view.convertPointToPixelYf((float) outerBound.getMinY()) - h;
			handle[BOTTOM].x = view.convertPointToPixelXf((float) outerBound.getCenterX()) - h;
			handle[BOTTOM].y = view.convertPointToPixelYf((float) outerBound.getMaxY()) - h;
			handle[LEFT].x = view.convertPointToPixelXf((float) outerBound.getMinX()) - h;
			handle[LEFT].y = view.convertPointToPixelYf((float) outerBound.getCenterY()) - h;
			handle[RIGHT].x = view.convertPointToPixelXf((float) outerBound.getMaxX()) - h;
			handle[RIGHT].y = view.convertPointToPixelYf((float) outerBound.getCenterY()) - h;

			Rectangle2D.Float innerBound = new Rectangle2D.Float(e.getX() - e.getInnerA(), e.getY() - e.getInnerB(), 2 * e.getInnerA(), 2 * e.getInnerB());
			handle[UPPER_LEFT + 8].x = view.convertPointToPixelXf((float) innerBound.getMinX()) - h;
			handle[UPPER_LEFT + 8].y = view.convertPointToPixelYf((float) innerBound.getMinY()) - h;
			handle[LOWER_LEFT + 8].x = view.convertPointToPixelXf((float) innerBound.getMinX()) - h;
			handle[LOWER_LEFT + 8].y = view.convertPointToPixelYf((float) innerBound.getMaxY()) - h;
			handle[UPPER_RIGHT + 8].x = view.convertPointToPixelXf((float) innerBound.getMaxX()) - h;
			handle[UPPER_RIGHT + 8].y = view.convertPointToPixelYf((float) innerBound.getMinY()) - h;
			handle[LOWER_RIGHT + 8].x = view.convertPointToPixelXf((float) innerBound.getMaxX()) - h;
			handle[LOWER_RIGHT + 8].y = view.convertPointToPixelYf((float) innerBound.getMaxY()) - h;
			handle[TOP + 8].x = view.convertPointToPixelXf((float) innerBound.getCenterX()) - h;
			handle[TOP + 8].y = view.convertPointToPixelYf((float) innerBound.getMinY()) - h;
			handle[BOTTOM + 8].x = view.convertPointToPixelXf((float) innerBound.getCenterX()) - h;
			handle[BOTTOM + 8].y = view.convertPointToPixelYf((float) innerBound.getMaxY()) - h;
			handle[LEFT + 8].x = view.convertPointToPixelXf((float) innerBound.getMinX()) - h;
			handle[LEFT + 8].y = view.convertPointToPixelYf((float) innerBound.getCenterY()) - h;
			handle[RIGHT + 8].x = view.convertPointToPixelXf((float) innerBound.getMaxX()) - h;
			handle[RIGHT + 8].y = view.convertPointToPixelYf((float) innerBound.getCenterY()) - h;

			for (int i = RIGHT + 9; i < handle.length; i++) {
				handle[i].x = handle[i].y = -100;
			}

		} else if (s instanceof Area) {
			if (m instanceof Cloud) {
				Cloud c = (Cloud) m;
				Rectangle2D.Double bound = new Rectangle2D.Double();
				bound.x = s.getBounds2D().getX() + c.getX();
				bound.y = s.getBounds2D().getY() + c.getY();
				bound.width = s.getBounds2D().getWidth();
				bound.height = s.getBounds2D().getHeight();
				setRectHandles(view, bound, handle, h);
			} else if (m instanceof Tree) {
				Tree t = (Tree) m;
				Rectangle2D.Double bound = new Rectangle2D.Double();
				bound.x = s.getBounds2D().getX() + t.getX();
				bound.y = s.getBounds2D().getY() + t.getY();
				bound.width = s.getBounds2D().getWidth();
				bound.height = s.getBounds2D().getHeight();
				setRectHandles(view, bound, handle, h);
			}
		}

	}

	private static void setRectHandles(View2D view, Rectangle2D bound, Rectangle2D.Float[] handle, float h) {
		handle[UPPER_LEFT].x = view.convertPointToPixelXf((float) bound.getMinX()) - h;
		handle[UPPER_LEFT].y = view.convertPointToPixelYf((float) bound.getMinY()) - h;
		handle[LOWER_LEFT].x = view.convertPointToPixelXf((float) bound.getMinX()) - h;
		handle[LOWER_LEFT].y = view.convertPointToPixelYf((float) bound.getMaxY()) - h;
		handle[UPPER_RIGHT].x = view.convertPointToPixelXf((float) bound.getMaxX()) - h;
		handle[UPPER_RIGHT].y = view.convertPointToPixelYf((float) bound.getMinY()) - h;
		handle[LOWER_RIGHT].x = view.convertPointToPixelXf((float) bound.getMaxX()) - h;
		handle[LOWER_RIGHT].y = view.convertPointToPixelYf((float) bound.getMaxY()) - h;
		handle[TOP].x = view.convertPointToPixelXf((float) bound.getCenterX()) - h;
		handle[TOP].y = view.convertPointToPixelYf((float) bound.getMinY()) - h;
		handle[BOTTOM].x = view.convertPointToPixelXf((float) bound.getCenterX()) - h;
		handle[BOTTOM].y = view.convertPointToPixelYf((float) bound.getMaxY()) - h;
		handle[LEFT].x = view.convertPointToPixelXf((float) bound.getMinX()) - h;
		handle[LEFT].y = view.convertPointToPixelYf((float) bound.getCenterY()) - h;
		handle[RIGHT].x = view.convertPointToPixelXf((float) bound.getMaxX()) - h;
		handle[RIGHT].y = view.convertPointToPixelYf((float) bound.getCenterY()) - h;
		for (int i = RIGHT + 1; i < handle.length; i++) {
			handle[i].x = handle[i].y = -100;
		}
	}

}
