/*
 *   Copyright (C) 2013  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

/**
 * @author Charles Xie
 * 
 */
class Fan {

	static Area getShape(Rectangle2D.Float r, float speed, float angle, float delta) {
		if (r.height > r.width) {
			float d1 = 0.5f * r.height * delta;
			float d2 = d1 * 2;
			float deg = Math.round(Math.toDegrees(0.5 * Math.asin(r.height / Math.hypot(r.width, r.height))));
			Area a = new Area(new Arc2D.Float(r.x + r.width / 4, r.y + d1, r.width / 2, r.height - d2, deg, 180 - 2 * deg, Arc2D.PIE));
			a.add(new Area(new Arc2D.Float(r.x + r.width / 4, r.y + d1, r.width / 2, r.height - d2, -deg, 2 * deg - 180, Arc2D.PIE)));
			a.add(new Area(new Rectangle2D.Float(speed * Math.cos(angle) >= 0 ? r.x : r.x + r.width * 0.5f, r.y + r.height * (0.5f - 0.025f), r.width * 0.5f, 0.05f * r.height)));
			return a;
		}
		float d1 = 0.5f * r.width * delta;
		float d2 = d1 * 2;
		float deg = Math.round(Math.toDegrees(0.5 * Math.asin(r.width / Math.hypot(r.width, r.height))));
		Area a = new Area(new Arc2D.Float(r.x + d1, r.y + r.height / 4, r.width - d2, r.height / 2, deg, -2 * deg, Arc2D.PIE));
		a.add(new Area(new Arc2D.Float(r.x + d1, r.y + r.height / 4, r.width - d2, r.height / 2, 180 - deg, 2 * deg, Arc2D.PIE)));
		a.add(new Area(new Rectangle2D.Float(r.x + r.width * (0.5f - 0.025f), speed * Math.sin(angle) > 0 ? r.y : r.y + r.height * 0.5f, 0.05f * r.width, r.height * 0.5f)));
		return a;
	}

}