package org.concord.energy2d.view;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import org.concord.energy2d.math.Blob2D;
import org.concord.energy2d.math.Polygon2D;
import org.concord.energy2d.math.Ring2D;
import org.concord.energy2d.model.Cloud;
import org.concord.energy2d.model.Manipulable;
import org.concord.energy2d.model.Tree;

import static org.concord.energy2d.view.View2D.BOTTOM;
import static org.concord.energy2d.view.View2D.LEFT;
import static org.concord.energy2d.view.View2D.LOWER_LEFT;
import static org.concord.energy2d.view.View2D.LOWER_RIGHT;
import static org.concord.energy2d.view.View2D.RIGHT;
import static org.concord.energy2d.view.View2D.TOP;
import static org.concord.energy2d.view.View2D.UPPER_LEFT;
import static org.concord.energy2d.view.View2D.UPPER_RIGHT;

/**
 * @author Charles Xie
 * 
 */
class HandleSetter {

	static void setRects(View2D view, Manipulable m, Rectangle[] handle) {

		int h = handle[0].width / 2;
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
						handle[i].x = view.convertPointToPixelX(point.x) - h;
						handle[i].y = view.convertPointToPixelY(point.y) - h;
					} else {
						handle[i].x = handle[i].y = -100;
					}
				}
			} else {
				float k = (float) n / (float) handle.length;
				for (int i = 0; i < handle.length; i++) {
					point = p.getVertex((int) (i * k));
					handle[i].x = view.convertPointToPixelX(point.x) - h;
					handle[i].y = view.convertPointToPixelY(point.y) - h;
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
						handle[i].x = view.convertPointToPixelX(point.x) - h;
						handle[i].y = view.convertPointToPixelY(point.y) - h;
					} else {
						handle[i].x = handle[i].y = -100;
					}
				}
			} else {
				float k = (float) n / (float) handle.length;
				for (int i = 0; i < handle.length; i++) {
					point = b.getPoint((int) (i * k));
					handle[i].x = view.convertPointToPixelX(point.x) - h;
					handle[i].y = view.convertPointToPixelY(point.y) - h;
				}
			}
		} else if (s instanceof Ring2D) {
			setRectHandles(view, s.getBounds2D(), handle, h);
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

	private static void setRectHandles(View2D view, Rectangle2D bound, Rectangle[] handle, int h) {
		handle[UPPER_LEFT].x = view.convertPointToPixelX((float) bound.getMinX()) - h;
		handle[UPPER_LEFT].y = view.convertPointToPixelY((float) bound.getMinY()) - h;
		handle[LOWER_LEFT].x = view.convertPointToPixelX((float) bound.getMinX()) - h;
		handle[LOWER_LEFT].y = view.convertPointToPixelY((float) bound.getMaxY()) - h;
		handle[UPPER_RIGHT].x = view.convertPointToPixelX((float) bound.getMaxX()) - h;
		handle[UPPER_RIGHT].y = view.convertPointToPixelY((float) bound.getMinY()) - h;
		handle[LOWER_RIGHT].x = view.convertPointToPixelX((float) bound.getMaxX()) - h;
		handle[LOWER_RIGHT].y = view.convertPointToPixelY((float) bound.getMaxY()) - h;
		handle[TOP].x = view.convertPointToPixelX((float) bound.getCenterX()) - h;
		handle[TOP].y = view.convertPointToPixelY((float) bound.getMinY()) - h;
		handle[BOTTOM].x = view.convertPointToPixelX((float) bound.getCenterX()) - h;
		handle[BOTTOM].y = view.convertPointToPixelY((float) bound.getMaxY()) - h;
		handle[LEFT].x = view.convertPointToPixelX((float) bound.getMinX()) - h;
		handle[LEFT].y = view.convertPointToPixelY((float) bound.getCenterY()) - h;
		handle[RIGHT].x = view.convertPointToPixelX((float) bound.getMaxX()) - h;
		handle[RIGHT].y = view.convertPointToPixelY((float) bound.getCenterY()) - h;
		for (int i = RIGHT + 1; i < handle.length; i++) {
			handle[i].x = handle[i].y = -100;
		}
	}

}
