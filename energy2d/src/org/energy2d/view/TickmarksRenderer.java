package org.energy2d.view;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.text.DecimalFormat;

/**
 * @author Charles Xie
 */
class TickmarksRenderer {

    private final static DecimalFormat FORMAT_DECIMAL = new DecimalFormat("####.####");
    private final static DecimalFormat FORMAT_SCIENTIFIC_NOTATION = new DecimalFormat("0.#E0");
    private Stroke stroke = new BasicStroke(1);
    private Font smallFont = new Font(null, Font.PLAIN, 9);
    private int nx = 100, ny = 100;
    private float dx, dy;
    private float xmin, ymin;
    private String unit = "m";

    TickmarksRenderer() {
    }

    void setSize(float xmin, float xmax, float ymin, float ymax) {
        float ratio = (ymax - ymin) / (xmax - xmin);
        ny = (int) (nx * ratio);
        dx = (xmax - xmin) / nx;
        dy = (ymax - ymin) / ny;
        this.xmin = xmin;
        this.ymin = ymin;
    }

    private static void centerString(String s, Graphics2D g, int x, int y) {
        int stringWidth = g.getFontMetrics().stringWidth(s);
        g.drawString(s, x - stringWidth / 2, y);
    }

    void render(View2D c, Graphics2D g) {

        if (!c.isVisible())
            return;

        Font oldFont = g.getFont();
        Stroke oldStroke = g.getStroke();

        int w = c.getWidth();
        int h = c.getHeight();

        g.setStroke(stroke);
        g.setFont(smallFont);
        int k;
        for (int i = 1; i < nx; i++) {
            k = Math.round(i * w / nx);
            g.setColor(c.getContrastColor(k, h - 12));
            if (i % 10 == 0) {
                g.drawLine(k, h, k, h - 10);
                if (dx < 0.001) {
                    centerString(FORMAT_SCIENTIFIC_NOTATION.format(xmin + i * dx), g, k, h - 12);
                } else {
                    centerString(FORMAT_DECIMAL.format(xmin + i * dx), g, k, h - 12);
                }
            } else {
                g.drawLine(k, h, k, h - 5);
            }
        }
        g.setColor(c.getContrastColor(w - 10, h - 12));
        centerString(unit, g, w - 10, h - 12);
        for (int i = 1; i < ny; i++) {
            k = Math.round((1f - (float) i / (float) ny) * h);
            g.setColor(c.getContrastColor(15, k + 3));
            if (i % 10 == 0) {
                g.drawLine(0, k, 10, k);
                if (dy < 0.001) {
                    centerString(FORMAT_SCIENTIFIC_NOTATION.format(ymin + i * dy), g, 25, k + 3);
                } else {
                    centerString(FORMAT_DECIMAL.format(ymin + i * dy), g, 15, k + 3);
                }
            } else {
                g.drawLine(0, k, 5, k);
            }
        }
        g.setColor(c.getContrastColor(15, 10));
        centerString(unit, g, 15, 10);

        g.setStroke(oldStroke);
        g.setFont(oldFont);

    }

}