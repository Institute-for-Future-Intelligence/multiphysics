package org.concord.energy2d.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import org.concord.energy2d.model.Thermostat;

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

			int x1 = v.convertPointToPixelX(t.getThermometer().getX());
			int y1 = v.convertPointToPixelY(t.getThermometer().getY());
			int x2 = v.convertPointToPixelX(t.getPowerSource().getCenter().x);
			int y2 = v.convertPointToPixelY(t.getPowerSource().getCenter().y);
			g.drawLine(x1, y1, x1, y2);
			g.drawLine(x1, y2, x2, y2);

			g.setStroke(stroke2);
			g.drawOval(x1 - 3, y1 - 3, 6, 6);
			g.drawOval(x2 - 3, y2 - 3, 6, 6);

			g.setColor(Color.white);
			g.drawLine(x1, y1, x1, y2);
			g.drawLine(x1, y2, x2, y2);
			g.fillOval(x1 - 2, y1 - 2, 4, 4);
			g.fillOval(x2 - 2, y2 - 2, 4, 4);

			g.setStroke(oldStroke);
			g.setColor(oldColor);

		} else {

		}

	}

}
