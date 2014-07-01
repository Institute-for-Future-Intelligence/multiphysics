/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.model;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;

import org.concord.energy2d.math.Blob2D;
import org.concord.energy2d.math.Polygon2D;
import org.concord.energy2d.math.Ring2D;
import org.concord.energy2d.math.TransformableShape;
import org.concord.energy2d.util.ColorFill;
import org.concord.energy2d.util.FillPattern;
import org.concord.energy2d.util.Texture;

/**
 * Default properties set to be that of polystyrene. See http://en.wikipedia.org/wiki/Polystyrene
 * 
 * @author Charles Xie
 * 
 */
public class Part extends Manipulable {

	// constant power input/output: positive = source, negative = sink, zero = off. Unit: W/m^3
	private float power;

	// this turns the power on and off: it should not be saved in the XML, or copied to another part
	private boolean powerSwitch = true;

	// http://en.wikipedia.org/wiki/Temperature_coefficient
	private float thermistorTemperatureCoefficient = 0;

	// a fixed or initial temperature for this part
	private float temperature;

	// when this flag is true, temperature is maintained at the set value. Otherwise, it will be just the initial value that defines the heat energy this part initially possesses.
	private boolean constantTemperature;

	/*
	 * the thermal conductivity: Fourier's Law, the flow of heat energy
	 * 
	 * q = - k dT/dx
	 * 
	 * Unit: W/(mK). Water's is 0.08.
	 */
	private float thermalConductivity = 1f;

	// the specific heat capacity: J/(kgK).
	private float specificHeat = 1300f;

	// density kg/m^3. The default value is foam's.
	private float density = 25f;

	// optical properties
	private float absorptivity = 1;
	private float transmissivity;
	private float reflectivity;
	private float emissivity;
	private boolean scattering;
	private boolean scatteringVisible = true;

	private float windSpeed;
	private float windAngle;

	private static int polygonize = 50;
	private final static DecimalFormat LABEL_FORMAT = new DecimalFormat("####.######");
	private final static DecimalFormat SHORT_LABEL_FORMAT = new DecimalFormat("###.##");

	private FillPattern fillPattern;
	private boolean filled = true;

	private Model2D model;

	public Part(Shape shape, Model2D model) {
		super(shape);
		this.model = model;
		fillPattern = new ColorFill(Color.gray);
	}

	public void setFilled(boolean filled) {
		this.filled = filled;
	}

	public boolean isFilled() {
		return filled;
	}

	public void setFillPattern(FillPattern fillPattern) {
		this.fillPattern = fillPattern;
	}

	public FillPattern getFillPattern() {
		return fillPattern;
	}

	public Part duplicate(float x, float y) {
		Shape s = getShape();
		if (s instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) s;
			s = new Rectangle2D.Float(x - 0.5f * r.width, y - 0.5f * r.height, r.width, r.height);
		} else if (s instanceof Ellipse2D.Float) {
			Ellipse2D.Float e = (Ellipse2D.Float) s;
			s = new Ellipse2D.Float(x - 0.5f * e.width, y - 0.5f * e.height, e.width, e.height);
		} else if (s instanceof Ring2D) {
			s = new Ring2D((Ring2D) s);
			Rectangle2D r = s.getBounds2D();
			((Ring2D) s).translateBy(x - (float) r.getCenterX(), y - (float) r.getCenterY());
		} else if (s instanceof Polygon2D) {
			s = ((Polygon2D) s).duplicate();
			Rectangle2D r = s.getBounds2D();
			float dx = x - (float) r.getCenterX();
			float dy = y - (float) r.getCenterY();
			((Polygon2D) s).translateBy(dx, dy);
		} else if (s instanceof Blob2D) {
			s = ((Blob2D) s).duplicate();
			Rectangle2D r = s.getBounds2D();
			float dx = x - (float) r.getCenterX();
			float dy = y - (float) r.getCenterY();
			((Blob2D) s).translateBy(dx, dy);
			((Blob2D) s).update();
		}
		Part p = new Part(s, model);
		p.filled = filled;
		p.fillPattern = fillPattern;
		p.power = power;
		p.thermistorTemperatureCoefficient = thermistorTemperatureCoefficient;
		p.temperature = temperature;
		p.constantTemperature = constantTemperature;
		p.thermalConductivity = thermalConductivity;
		p.specificHeat = specificHeat;
		p.density = density;
		p.absorptivity = absorptivity;
		p.reflectivity = reflectivity;
		p.scattering = scattering;
		p.scatteringVisible = scatteringVisible;
		p.transmissivity = transmissivity;
		p.emissivity = emissivity;
		p.windAngle = windAngle;
		p.windSpeed = windSpeed;
		p.setLabel(getLabel());
		return p;
	}

	public void translateBy(float dx, float dy) {
		Shape s = getShape();
		if (s instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) s;
			r.x += dx;
			r.y += dy;
		} else if (s instanceof Ellipse2D.Float) {
			Ellipse2D.Float e = (Ellipse2D.Float) s;
			e.x += dx;
			e.y += dy;
		} else if (s instanceof Ring2D) {
			((Ring2D) s).translateBy(dx, dy);
		} else if (s instanceof Polygon2D) {
			((Polygon2D) s).translateBy(dx, dy);
		} else if (s instanceof Blob2D) {
			((Blob2D) s).translateBy(dx, dy);
			((Blob2D) s).update();
		}
	}

	public void setWindSpeed(float windSpeed) {
		this.windSpeed = windSpeed;
	}

	public float getWindSpeed() {
		return windSpeed;
	}

	public void setWindAngle(float windAngle) {
		this.windAngle = windAngle;
	}

	public float getWindAngle() {
		return windAngle;
	}

	public void setEmissivity(float emissivity) {
		this.emissivity = emissivity;
	}

	public float getEmissivity() {
		return emissivity;
	}

	public void setTransmissivity(float transmission) {
		this.transmissivity = transmission;
	}

	public float getTransmissivity() {
		return transmissivity;
	}

	public void setAbsorptivity(float absorption) {
		this.absorptivity = absorption;
	}

	public float getAbsorptivity() {
		return absorptivity;
	}

	public void setReflectivity(float reflectivity) {
		this.reflectivity = reflectivity;
	}

	public float getReflectivity() {
		return reflectivity;
	}

	public void setScattering(boolean scattering) {
		this.scattering = scattering;
	}

	public boolean getScattering() {
		return scattering;
	}

	public void setScatteringVisible(boolean scatteringVisible) {
		this.scatteringVisible = scatteringVisible;
	}

	public boolean isScatteringVisible() {
		return scatteringVisible;
	}

	public void setConstantTemperature(boolean b) {
		constantTemperature = b;
	}

	public boolean getConstantTemperature() {
		return constantTemperature;
	}

	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}

	public float getTemperature() {
		return temperature;
	}

	public void setPower(float power) {
		this.power = power;
	}

	public float getPower() {
		return power;
	}

	public void setPowerSwitch(boolean b) {
		powerSwitch = b;
	}

	public boolean getPowerSwitch() {
		return powerSwitch;
	}

	public void setThermistorTemperatureCoefficient(float temperatureCoefficient) {
		thermistorTemperatureCoefficient = temperatureCoefficient;
	}

	public float getThermistorTemperatureCoefficient() {
		return thermistorTemperatureCoefficient;
	}

	public void setThermalConductivity(float thermalConductivity) {
		this.thermalConductivity = thermalConductivity;
	}

	public float getThermalConductivity() {
		return thermalConductivity;
	}

	public void setSpecificHeat(float specificHeat) {
		this.specificHeat = specificHeat;
	}

	public float getSpecificHeat() {
		return specificHeat;
	}

	public void setDensity(float density) {
		this.density = density;
	}

	public float getDensity() {
		return density;
	}

	boolean contains(Photon p) {
		return getShape().contains(p.getRx(), p.getRy());
	}

	/* return true if the line connecting the two specified segments intersects with this part. */
	boolean intersectsLine(Segment s1, Segment s2) {

		Point2D.Float p1 = s1.getCenter();
		Point2D.Float p2 = s2.getCenter();

		if (p1.distanceSq(p2) < 0.000001f * model.getLx())
			return true;

		Shape shape = getShape();

		if (shape instanceof Rectangle2D.Float) { // a rectangle is convex

			if (s1.getPart() == this && s2.getPart() == this)
				return true;
			// shrink it a bit to ensure that it intersects with this rectangular part
			Rectangle2D.Float r0 = (Rectangle2D.Float) shape;
			float indent = 0.001f;
			float x0 = r0.x + indent * r0.width;
			float y0 = r0.y + indent * r0.height;
			float x1 = r0.x + (1 - indent) * r0.width;
			float y1 = r0.y + (1 - indent) * r0.height;
			if (Line2D.linesIntersect(p1.x, p1.y, p2.x, p2.y, x0, y0, x1, y0))
				return true;
			if (Line2D.linesIntersect(p1.x, p1.y, p2.x, p2.y, x1, y0, x1, y1))
				return true;
			if (Line2D.linesIntersect(p1.x, p1.y, p2.x, p2.y, x1, y1, x0, y1))
				return true;
			if (Line2D.linesIntersect(p1.x, p1.y, p2.x, p2.y, x0, y1, x0, y0))
				return true;

		} else if (shape instanceof Polygon2D) { // a polygon may be concave or convex

			float indent = 0.001f * model.getLx() / model.getNx();
			float delta = 0;
			float x3 = p1.x, y3 = p1.y, x4 = p2.x, y4 = p2.y;
			if (Math.abs(p1.x - p2.x) < indent) {
				delta = Math.signum(p2.y - p1.y) * indent;
				y3 += delta;
				y4 -= delta;
			} else if (Math.abs(p1.y - p2.y) < indent) {
				delta = Math.signum(p2.x - p1.x) * indent;
				x3 += delta;
				x4 -= delta;
			} else {
				float k = (p2.y - p1.y) / (p2.x - p1.x);
				delta = Math.signum(p2.x - p1.x) * indent;
				x3 += delta;
				x4 -= delta;
				y3 = p1.y + k * (x3 - p1.x);
				y4 = p1.y + k * (x4 - p1.x);
			}
			if (s1.getPart() == this && s2.getPart() == this) {
				for (Segment s : model.getRadiationSegments()) {
					if (s.getPart() == this) {
						if (shape.contains(x3, y3) || shape.contains(x4, y4))
							return true;
						if (s.intersectsLine(x3, y3, x4, y4))
							return true;
					}
				}
			} else {
				for (Segment s : model.getRadiationSegments()) {
					if (s.getPart() == this) {
						if (s.intersectsLine(x3, y3, x4, y4))
							return true;
					}
				}
			}

		} else if (shape instanceof Blob2D) { // a blob may be concave or convex

			float indent = 0.001f * model.getLx() / model.getNx();
			float delta = 0;
			float x3 = p1.x, y3 = p1.y, x4 = p2.x, y4 = p2.y;
			if (Math.abs(p1.x - p2.x) < indent) {
				delta = Math.signum(p2.y - p1.y) * indent;
				y3 += delta;
				y4 -= delta;
			} else if (Math.abs(p1.y - p2.y) < indent) {
				delta = Math.signum(p2.x - p1.x) * indent;
				x3 += delta;
				x4 -= delta;
			} else {
				float k = (p2.y - p1.y) / (p2.x - p1.x);
				delta = Math.signum(p2.x - p1.x) * indent;
				x3 += delta;
				x4 -= delta;
				y3 = p1.y + k * (x3 - p1.x);
				y4 = p1.y + k * (x4 - p1.x);
			}
			if (s1.getPart() == this && s2.getPart() == this) {
				for (Segment s : model.getRadiationSegments()) {
					if (s.getPart() == this) {
						if (shape.contains(x3, y3) || shape.contains(x4, y4))
							return true;
						if (s.intersectsLine(x3, y3, x4, y4))
							return true;
					}
				}
			} else {
				for (Segment s : model.getRadiationSegments()) {
					if (s.getPart() == this) {
						if (s.intersectsLine(x3, y3, x4, y4))
							return true;
					}
				}
			}

		} else if (shape instanceof Ellipse2D.Float) { // an ellipse is convex

			if (s1.getPart() == this && s2.getPart() == this)
				return true;
			Ellipse2D.Float e0 = (Ellipse2D.Float) shape;
			// shrink it a bit to ensure that it intersects with this elliptical part
			float indent = 0.01f;
			float ex = e0.x + indent * e0.width;
			float ey = e0.y + indent * e0.height;
			float ew = (1 - 2 * indent) * e0.width;
			float eh = (1 - 2 * indent) * e0.height;
			float a = ew * 0.5f;
			float b = eh * 0.5f;
			float x = ex + a;
			float y = ey + b;
			float h = (a - b) / (a + b);
			h *= h;
			double perimeter = Math.PI * (a + b) * (1 + 3 * h / (10 + Math.sqrt(4 - 3 * h)));
			float patchSize = model.getLx() * model.getRadiationMeshSize();
			int n = (int) (perimeter / patchSize);
			float[] vx = new float[n];
			float[] vy = new float[n];
			float theta;
			float delta = (float) (2 * Math.PI / n);
			for (int i = 0; i < n; i++) {
				theta = delta * i;
				vx[i] = (float) (x + a * Math.cos(theta));
				vy[i] = (float) (y + b * Math.sin(theta));
			}
			for (int i = 0; i < n - 1; i++)
				if (Line2D.linesIntersect(p1.x, p1.y, p2.x, p2.y, vx[i], vy[i], vx[i + 1], vy[i + 1]))
					return true;
			if (Line2D.linesIntersect(p1.x, p1.y, p2.x, p2.y, vx[n - 1], vy[n - 1], vx[0], vy[0]))
				return true;

		} else if (shape instanceof Ring2D) {

			Ring2D r0 = (Ring2D) shape;
			// shrink it a bit to ensure that it intersects with this outer line
			float indent = 0.01f;
			float x = r0.getX();
			float y = r0.getY();
			float d = r0.getOuterDiameter() * (1 - indent);
			double perimeter = Math.PI * d;
			float patchSize = model.getLx() * model.getRadiationMeshSize();
			int n = (int) (perimeter / patchSize);
			float[] vx = new float[n];
			float[] vy = new float[n];
			float theta;
			float delta = (float) (2 * Math.PI / n);
			d /= 2;
			for (int i = 0; i < n; i++) {
				theta = delta * i;
				vx[i] = (float) (x + d * Math.cos(theta));
				vy[i] = (float) (y + d * Math.sin(theta));
			}
			for (int i = 0; i < n - 1; i++)
				if (Line2D.linesIntersect(p1.x, p1.y, p2.x, p2.y, vx[i], vy[i], vx[i + 1], vy[i + 1]))
					return true;
			if (Line2D.linesIntersect(p1.x, p1.y, p2.x, p2.y, vx[n - 1], vy[n - 1], vx[0], vy[0]))
				return true;
			// expand it a bit to ensure that it intersects with this inner line
			d = r0.getInnerDiameter() * (1 + indent);
			perimeter = Math.PI * d;
			n = (int) (perimeter / patchSize);
			vx = new float[n];
			vy = new float[n];
			delta = (float) (2 * Math.PI / n);
			d /= 2;
			for (int i = 0; i < n; i++) {
				theta = delta * i;
				vx[i] = (float) (x + d * Math.cos(theta));
				vy[i] = (float) (y + d * Math.sin(theta));
			}
			for (int i = 0; i < n - 1; i++)
				if (Line2D.linesIntersect(p1.x, p1.y, p2.x, p2.y, vx[i], vy[i], vx[i + 1], vy[i + 1]))
					return true;
			if (Line2D.linesIntersect(p1.x, p1.y, p2.x, p2.y, vx[n - 1], vy[n - 1], vx[0], vy[0]))
				return true;

		}

		return false;

	}

	boolean reflect(Discrete p, float timeStep, boolean scatter) {

		Shape shape = getShape();

		if (shape instanceof Rectangle2D.Float) { // simpler case, faster implementation

			float radius = 0;
			if (p instanceof Particle)
				radius = ((Particle) p).radius;

			Rectangle2D.Float r = (Rectangle2D.Float) shape;
			float x0 = r.x;
			float y0 = r.y;
			float x1 = r.x + r.width;
			float y1 = r.y + r.height;
			if (p.getRx() - radius <= x1 && p.getRx() + radius >= x0 && p.getRy() - radius <= y1 && p.getRy() + radius >= y0) { // overlap
				float dx = p.getVx() * timeStep;
				if (p.getRx() + radius - dx <= x0) {
					if (scatter) {
						p.setAngle((float) (Math.PI * (0.5 + Math.random())));
					} else {
						p.setVx(-Math.abs(p.getVx()));
					}
				} else if (p.getRx() - radius - dx >= x1) {
					if (scatter) {
						p.setAngle((float) (Math.PI * (0.5 - Math.random())));
					} else {
						p.setVx(Math.abs(p.getVx()));
					}
				}
				float dy = p.getVy() * timeStep;
				if (p.getRy() + radius - dy <= y0) {
					if (scatter) {
						p.setAngle((float) (Math.PI * (1 + Math.random())));
					} else {
						p.setVy(-Math.abs(p.getVy()));
					}
				} else if (p.getRy() - radius - dy >= y1) {
					if (scatter) {
						p.setAngle((float) (Math.PI * Math.random()));
					} else {
						p.setVy(Math.abs(p.getVy()));
					}
				}
				return true;
			}

		} else if (shape instanceof Polygon2D) {

			Polygon2D r = (Polygon2D) shape;
			if (r.contains(p.getRx(), p.getRy())) {
				reflect(r, p, timeStep, scatter);
				return true;
			}

		} else if (shape instanceof Blob2D) {

			Blob2D b = (Blob2D) shape;
			if (b.contains(p.getRx(), p.getRy())) {
				reflect(b, p, timeStep, scatter);
				return true;
			}

		} else if (shape instanceof Ellipse2D.Float) {

			Ellipse2D.Float e = (Ellipse2D.Float) shape;
			if (e.contains(p.getRx(), p.getRy())) {
				reflect(e, p, timeStep, scatter);
				return true;
			}

		}

		return false;

	}

	private static void reflect(Ellipse2D.Float e, Discrete p, float timeStep, boolean scatter) {
		float a = e.width * 0.5f;
		float b = e.height * 0.5f;
		float x = e.x + a;
		float y = e.y + b;
		float[] vx = new float[polygonize];
		float[] vy = new float[polygonize];
		float theta;
		float delta = (float) (2 * Math.PI / polygonize);
		for (int i = 0; i < polygonize; i++) {
			theta = delta * i;
			vx[i] = (float) (x + a * Math.cos(theta));
			vy[i] = (float) (y + b * Math.sin(theta));
		}
		Line2D.Float line = new Line2D.Float();
		for (int i = 0; i < polygonize - 1; i++) {
			line.setLine(vx[i], vy[i], vx[i + 1], vy[i + 1]);
			if (reflectFromLine(p, line, timeStep, scatter))
				return;
		}
		line.setLine(vx[polygonize - 1], vy[polygonize - 1], vx[0], vy[0]);
		reflectFromLine(p, line, timeStep, scatter);
	}

	private static void reflect(Polygon2D r, Discrete p, float timeStep, boolean scatter) {
		int n = r.getVertexCount();
		Point2D.Float v1, v2;
		Line2D.Float line = new Line2D.Float();
		for (int i = 0; i < n - 1; i++) {
			v1 = r.getVertex(i);
			v2 = r.getVertex(i + 1);
			line.setLine(v1, v2);
			if (reflectFromLine(p, line, timeStep, scatter))
				return;
		}
		v1 = r.getVertex(n - 1);
		v2 = r.getVertex(0);
		line.setLine(v1, v2);
		reflectFromLine(p, line, timeStep, scatter);
	}

	private static void reflect(Blob2D b, Discrete p, float timeStep, boolean scatter) {
		int n = b.getPathPointCount();
		Point2D.Float v1, v2;
		Line2D.Float line = new Line2D.Float();
		for (int i = 0; i < n - 1; i++) {
			v1 = b.getPathPoint(i);
			v2 = b.getPathPoint(i + 1);
			line.setLine(v1, v2);
			if (reflectFromLine(p, line, timeStep, scatter))
				return;
		}
		v1 = b.getPathPoint(n - 1);
		v2 = b.getPathPoint(0);
		line.setLine(v1, v2);
		reflectFromLine(p, line, timeStep, scatter);
	}

	private static boolean reflectFromLine(Discrete p, Line2D.Float line, float timeStep, boolean scatter) {
		float x1 = p.getRx();
		float y1 = p.getRy();
		float x2 = p.getRx() - p.getVx() * timeStep;
		float y2 = p.getRy() - p.getVy() * timeStep;
		if (line.intersectsLine(x1, y1, x2, y2)) {
			x1 = line.x1;
			y1 = line.y1;
			x2 = line.x2;
			y2 = line.y2;
			float r12 = 1.0f / (float) Math.hypot(x1 - x2, y1 - y2);
			float sin = (y2 - y1) * r12;
			float cos = (x2 - x1) * r12;
			if (scatter) {
				double angle = -Math.PI * Math.random(); // remember internally the y-axis points downward
				double cos1 = Math.cos(angle);
				double sin1 = Math.sin(angle);
				double cos2 = cos1 * cos - sin1 * sin;
				double sin2 = sin1 * cos + cos1 * sin;
				p.setVx((float) (p.getSpeed() * cos2));
				p.setVy((float) (p.getSpeed() * sin2));
			} else {
				// velocity component parallel to the line
				float u = p.getVx() * cos + p.getVy() * sin;
				// velocity component perpendicular to the line
				float w = p.getVy() * cos - p.getVx() * sin;
				p.setVx(u * cos + w * sin);
				p.setVy(u * sin - w * cos);
			}
			return true;
		}
		return false;
	}

	public String toXml() {
		String xml = "<part>\n";
		if (getShape() instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) getShape();
			xml += "<rectangle";
			xml += " x=\"" + r.x + "\"";
			xml += " y=\"" + r.y + "\"";
			xml += " width=\"" + r.width + "\"";
			xml += " height=\"" + r.height + "\"/>";
		} else if (getShape() instanceof Ellipse2D.Float) {
			Ellipse2D.Float e = (Ellipse2D.Float) getShape();
			xml += "<ellipse";
			xml += " x=\"" + e.getCenterX() + "\"";
			xml += " y=\"" + e.getCenterY() + "\"";
			xml += " a=\"" + e.width + "\"";
			xml += " b=\"" + e.height + "\"/>";
		} else if (getShape() instanceof Polygon2D) {
			Polygon2D p = (Polygon2D) getShape();
			xml += "<polygon count=\"" + p.getVertexCount() + "\" vertices=\"";
			int n = p.getVertexCount();
			Point2D.Float p2d;
			for (int i = 0; i < n - 1; i++) {
				p2d = p.getVertex(i);
				xml += p2d.x + ", " + p2d.y + ", ";
			}
			p2d = p.getVertex(n - 1);
			xml += p2d.x + ", " + p2d.y + "\"/>\n";
		} else if (getShape() instanceof Blob2D) {
			Blob2D b = (Blob2D) getShape();
			xml += "<blob count=\"" + b.getPointCount() + "\" points=\"";
			int n = b.getPointCount();
			Point2D.Float p2d;
			for (int i = 0; i < n - 1; i++) {
				p2d = b.getPoint(i);
				xml += p2d.x + ", " + p2d.y + ", ";
			}
			p2d = b.getPoint(n - 1);
			xml += p2d.x + ", " + p2d.y + "\"/>\n";
		} else if (getShape() instanceof Ring2D) {
			Ring2D ring = (Ring2D) getShape();
			xml += "<ring";
			xml += " x=\"" + ring.getX() + "\"";
			xml += " y=\"" + ring.getY() + "\"";
			xml += " inner=\"" + ring.getInnerDiameter() + "\"";
			xml += " outer=\"" + ring.getOuterDiameter() + "\"/>";
		}
		xml += "<thermal_conductivity>" + thermalConductivity + "</thermal_conductivity>\n";
		xml += "<specific_heat>" + specificHeat + "</specific_heat>\n";
		xml += "<density>" + density + "</density>\n";
		xml += "<transmission>" + transmissivity + "</transmission>\n";
		xml += "<reflection>" + reflectivity + "</reflection>\n";
		xml += "<scattering>" + scattering + "</scattering>\n";
		if (!scatteringVisible)
			xml += "<scattering_visible>false</scattering_visible>\n";
		xml += "<absorption>" + absorptivity + "</absorption>\n";
		xml += "<emissivity>" + emissivity + "</emissivity>\n";
		xml += "<temperature>" + temperature + "</temperature>\n";
		xml += "<constant_temperature>" + constantTemperature + "</constant_temperature>\n";
		if (power != 0)
			xml += "<power>" + power + "</power>\n";
		if (thermistorTemperatureCoefficient != 0)
			xml += "<temperature_coefficient>" + thermistorTemperatureCoefficient + "</temperature_coefficient>\n";
		if (windSpeed != 0) {
			xml += "<wind_speed>" + windSpeed + "</wind_speed>\n";
		}
		if (windAngle != 0) {
			xml += "<wind_angle>" + windAngle + "</wind_angle>\n";
		}
		if (getUid() != null && !getUid().trim().equals(""))
			xml += "<uid>" + getUid() + "</uid>\n";
		if (fillPattern instanceof ColorFill) {
			Color color = ((ColorFill) fillPattern).getColor();
			if (!color.equals(Color.gray)) {
				xml += "<color>" + Integer.toHexString(0x00ffffff & color.getRGB()) + "</color>\n";
			}
		} else if (fillPattern instanceof Texture) {
			Texture pf = (Texture) fillPattern;
			xml += "<texture>";
			int i = pf.getForeground();
			xml += "<texture_fg>" + Integer.toString(i, 16) + "</texture_fg>\n";
			i = pf.getBackground();
			xml += "<texture_bg>" + Integer.toString(i, 16) + "</texture_bg>\n";
			i = ((Texture) fillPattern).getStyle();
			xml += "<texture_style>" + i + "</texture_style>\n";
			i = pf.getCellWidth();
			xml += "<texture_width>" + i + "</texture_width>\n";
			i = pf.getCellHeight();
			xml += "<texture_height>" + i + "</texture_height>\n";
			xml += "</texture>\n";
		}
		if (!isFilled())
			xml += "<filled>false</filled>\n";
		String label = getLabel();
		if (label != null && !label.trim().equals(""))
			xml += "<label>" + label + "</label>\n";
		if (!isVisible())
			xml += "<visible>false</visible>\n";
		if (!isDraggable())
			xml += "<draggable>false</draggable>\n";
		xml += "</part>\n";
		return xml;
	}

	public String getLabel(String label, Model2D model, boolean useFahrenheit) {
		if (label == null)
			return null;
		if (label.indexOf('%') == -1)
			return label;
		String s = null;
		if (label.equalsIgnoreCase("%temperature")) {
			Rectangle2D bounds = getShape().getBounds2D();
			float temp = model.getTemperatureAt((float) bounds.getCenterX(), (float) bounds.getCenterY());
			s = useFahrenheit ? Math.round(temp * 1.8f + 32) + " \u00b0F" : Math.round(temp) + " \u00b0C";
		} else if (label.equalsIgnoreCase("%thermal_energy")) {
			s = Math.round(model.getThermalEnergy(this)) + " J";
		} else if (label.equalsIgnoreCase("%density"))
			s = (int) density + " kg/m\u00b3";
		else if (label.equalsIgnoreCase("%specific_heat"))
			s = (int) specificHeat + " J/(kg\u00d7\u00b0C)";
		else if (label.equalsIgnoreCase("%heat_capacity"))
			s = SHORT_LABEL_FORMAT.format(specificHeat * density * getArea()) + " J/\u00b0C";
		else if (label.equalsIgnoreCase("%volumetric_heat_capacity"))
			s = SHORT_LABEL_FORMAT.format(specificHeat * density) + " J/(m\u00b3\u00d7\u00b0C)";
		else if (label.equalsIgnoreCase("%thermal_diffusivity"))
			s = LABEL_FORMAT.format(thermalConductivity / (specificHeat * density)) + " m\u00b2/s";
		else if (label.equalsIgnoreCase("%thermal_conductivity"))
			s = (float) thermalConductivity + " W/(m\u00d7\u00b0C)";
		else if (label.equalsIgnoreCase("%power_density"))
			s = (int) power + " W/m\u00b3";
		else if (label.equalsIgnoreCase("%area"))
			s = getAreaString();
		else if (label.equalsIgnoreCase("%width"))
			s = getWidthString();
		else if (label.equalsIgnoreCase("%height"))
			s = getHeightString();
		else {
			s = label.replace("%temperature", useFahrenheit ? (int) (temperature * 1.8f + 32) + " \u00b0F" : (int) temperature + " \u00b0C");
			s = s.replace("%thermal_energy", Math.round(model.getThermalEnergy(this)) + " J");
			s = s.replace("%density", (int) density + " kg/m\u00b3");
			s = s.replace("%specific_heat", (int) specificHeat + " J/(kg\u00d7\u00b0C)");
			s = s.replace("%heat_capacity", SHORT_LABEL_FORMAT.format(specificHeat * density * getArea()) + " J/\u00b0C");
			s = s.replace("%volumetric_heat_capacity", SHORT_LABEL_FORMAT.format(specificHeat * density) + " J/(m\u00b3\u00d7\u00b0C)");
			s = s.replace("%thermal_diffusivity", LABEL_FORMAT.format(thermalConductivity / (specificHeat * density)) + " m\u00b2/s");
			s = s.replace("%thermal_conductivity", (float) thermalConductivity + " W/(m\u00d7\u00b0C)");
			s = s.replace("%power_density", (int) power + " W/m\u00b3");
			s = s.replace("%area", getAreaString());
			s = s.replace("%width", getWidthString());
			s = s.replace("%height", getHeightString());
		}
		return s;
	}

	private float getArea() {
		float area = -1;
		if (getShape() instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) getShape();
			area = r.width * r.height;
		} else if (getShape() instanceof Ellipse2D.Float) {
			Ellipse2D.Float e = (Ellipse2D.Float) getShape();
			area = (float) (e.width * e.height * 0.25 * Math.PI);
		} else if (getShape() instanceof TransformableShape) {
			area = ((TransformableShape) getShape()).getArea();
		} else if (getShape() instanceof Ring2D) {
			area = ((Ring2D) getShape()).getArea();
		}
		return area;
	}

	private String getAreaString() {
		float area = getArea();
		return area < 0 ? "Unknown" : LABEL_FORMAT.format(area) + " m\u00b2";
	}

	private String getWidthString() {
		if (getShape() instanceof Rectangle2D.Float)
			return LABEL_FORMAT.format(((Rectangle2D.Float) getShape()).width) + " m";
		if (getShape() instanceof Ellipse2D.Float)
			return LABEL_FORMAT.format(((Ellipse2D.Float) getShape()).width) + " m";
		return "Unknown";
	}

	private String getHeightString() {
		if (getShape() instanceof Rectangle2D.Float)
			return LABEL_FORMAT.format(((Rectangle2D.Float) getShape()).height) + " m";
		if (getShape() instanceof Ellipse2D.Float)
			return LABEL_FORMAT.format(((Ellipse2D.Float) getShape()).height) + " m";
		return "Unknown";
	}

	@Override
	public String toString() {
		return getUid() == null ? super.toString() : getUid();
	}

}
