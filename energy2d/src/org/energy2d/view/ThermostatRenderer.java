package org.energy2d.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import org.energy2d.model.Thermostat;

/**
 * @author Charles Xie
 * 
 */
class ThermostatRenderer {

	private Stroke stroke1, stroke2;

	ThermostatRenderer() {
		stroke1 = new BasicStroke(3);
		stroke2 = new BasicStroke(1);
	}

	void render(Thermostat t, View2D v, Graphics2D g) {

		if (!v.isVisible())
			return;

		if (t.getThermometer() != null) {

			Stroke oldStroke = g.getStroke();
			Color oldColor = g.getColor();
			g.setStroke(stroke1);
			g.setColor(Color.black);

			float x1 = v.convertPointToPixelXf(t.getThermometer().getX());
			float y1 = v.convertPointToPixelYf(t.getThermometer().getY());
			float x2 = v.convertPointToPixelXf(t.getPowerSource().getCenter().x);
			float y2 = v.convertPointToPixelYf(t.getPowerSource().getCenter().y);
			g.draw(new Line2D.Float(x1, y1, x1, y2));
			g.draw(new Line2D.Float(x1, y2, x2, y2));

			g.setStroke(stroke2);
			g.draw(new Ellipse2D.Float(x1 - 3, y1 - 3, 6, 6));
			g.draw(new Ellipse2D.Float(x2 - 3, y2 - 3, 6, 6));

			g.setColor(Color.white);
			g.draw(new Line2D.Float(x1, y1, x1, y2));
			g.draw(new Line2D.Float(x1, y2, x2, y2));
			g.fill(new Ellipse2D.Float(x1 - 2, y1 - 2, 4, 4));
			g.fill(new Ellipse2D.Float(x2 - 2, y2 - 2, 4, 4));

			g.setStroke(oldStroke);
			g.setColor(oldColor);

		} else {

		}

	}

}
