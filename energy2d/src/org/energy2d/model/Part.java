package org.energy2d.model;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.List;

import org.energy2d.math.Annulus;
import org.energy2d.math.Blob2D;
import org.energy2d.math.EllipticalAnnulus;
import org.energy2d.math.Polygon2D;
import org.energy2d.math.TransformableShape;
import org.energy2d.util.ColorFill;
import org.energy2d.util.FillPattern;
import org.energy2d.util.Texture;
import org.energy2d.util.XmlCharacterEncoder;

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
	private float thermistorReferenceTemperature = 0;

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

	// mechanical properties
	private float elasticity = 1.0f;

	private float windSpeed;
	private float windAngle;

	private final static DecimalFormat LABEL_FORMAT = new DecimalFormat("####.######");
	private final static DecimalFormat TWO_DECIMALS_FORMAT = new DecimalFormat("####.##");
	private final static DecimalFormat SHORT_LABEL_FORMAT = new DecimalFormat("###.##");

	private FillPattern fillPattern;
	private boolean filled = true;

	private Model2D model;

	public Part(Shape shape, Model2D model) {
		super(shape);
		this.model = model;
		fillPattern = new ColorFill(Color.GRAY);
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

	@Override
	public Part duplicate(float x, float y) {
		Shape s = getShape();
		if (s instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) s;
			s = new Rectangle2D.Float(x - 0.5f * r.width, y - 0.5f * r.height, r.width, r.height);
		} else if (s instanceof Ellipse2D.Float) {
			Ellipse2D.Float e = (Ellipse2D.Float) s;
			s = new Ellipse2D.Float(x - 0.5f * e.width, y - 0.5f * e.height, e.width, e.height);
		} else if (s instanceof Annulus) {
			s = new Annulus((Annulus) s);
			Rectangle2D r = s.getBounds2D();
			((Annulus) s).translateBy(x - (float) r.getCenterX(), y - (float) r.getCenterY());
		} else if (s instanceof EllipticalAnnulus) {
			s = new EllipticalAnnulus((EllipticalAnnulus) s);
			Rectangle2D r = s.getBounds2D();
			((EllipticalAnnulus) s).translateBy(x - (float) r.getCenterX(), y - (float) r.getCenterY());
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
		copyPropertiesTo(p);
		return p;
	}

	@Override
	public Part duplicate() {
		Shape s = getShape();
		if (s instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) s;
			s = new Rectangle2D.Float(r.x, r.y, r.width, r.height);
		} else if (s instanceof Ellipse2D.Float) {
			Ellipse2D.Float e = (Ellipse2D.Float) s;
			s = new Ellipse2D.Float(e.x, e.y, e.width, e.height);
		} else if (s instanceof Annulus) {
			s = new Annulus((Annulus) s);
		} else if (s instanceof EllipticalAnnulus) {
			s = new EllipticalAnnulus((EllipticalAnnulus) s);
		} else if (s instanceof Polygon2D) {
			s = ((Polygon2D) s).duplicate();
		} else if (s instanceof Blob2D) {
			s = ((Blob2D) s).duplicate();
		}
		Part p = new Part(s, model);
		copyPropertiesTo(p);
		return p;
	}

	public void copyPropertiesTo(Part p) {
		p.filled = filled;
		p.fillPattern = fillPattern;
		p.power = power;
		p.elasticity = elasticity;
		p.thermistorTemperatureCoefficient = thermistorTemperatureCoefficient;
		p.thermistorReferenceTemperature = thermistorReferenceTemperature;
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
	}

	@Override
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
		} else if (s instanceof Annulus) {
			((Annulus) s).translateBy(dx, dy);
		} else if (s instanceof EllipticalAnnulus) {
			((EllipticalAnnulus) s).translateBy(dx, dy);
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

	public void setElasticity(float elasticity) {
		this.elasticity = elasticity;
	}

	public float getElasticity() {
		return elasticity;
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

	public void setThermistorReferenceTemperature(float referenceTemperature) {
		thermistorReferenceTemperature = referenceTemperature;
	}

	public float getThermistorReferenceTemperature() {
		return thermistorReferenceTemperature;
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

		} else if (shape instanceof Polygon2D || shape instanceof Blob2D) { // a polygon or blob may be concave or convex

			float delta = model.getLx() / model.getNx();
			float indent = 0.001f * delta;
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
			List<Segment> partSegments = model.getPerimeterSegments(this);
			int n = partSegments.size();
			if (n > 0) {
				boolean bothBelongToThisPart = s1.getPart() == this && s2.getPart() == this;
				Shape shape2 = shape;
				if (shape instanceof Blob2D) { // we will have to use the approximated polygon instead of the blob
					Path2D.Float path = new Path2D.Float();
					Segment s = partSegments.get(0);
					path.moveTo(s.x1, s.y1);
					path.lineTo(s.x2, s.y2);
					if (n > 1) {
						for (int i = 1; i < n; i++) {
							s = partSegments.get(i);
							path.lineTo(s.x2, s.y2);
						}
					}
					path.closePath();
					shape2 = path;
				}
				for (Segment s : partSegments) {
					if (bothBelongToThisPart && (shape2.contains(x3, y3) || shape2.contains(x4, y4)))
						return true;
					if (s.intersectsLine(x3, y3, x4, y4))
						return true;
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
			float patchSize = model.getLx() * model.getPerimeterStepSize();
			int n = (int) (perimeter / patchSize);
			if (n > 0) {
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
			}

		} else if (shape instanceof Annulus) {

			Annulus r0 = (Annulus) shape;
			// shrink it a bit to ensure that it intersects with this outer line
			float indent = 0.01f;
			float x = r0.getX();
			float y = r0.getY();
			float d = r0.getOuterDiameter() * (1 - indent);
			double perimeter = Math.PI * d;
			float patchSize = model.getLx() * model.getPerimeterStepSize();
			int n = (int) (perimeter / patchSize);
			if (n > 0) {
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

		} else if (shape instanceof EllipticalAnnulus) {

			EllipticalAnnulus r0 = (EllipticalAnnulus) shape;
			// shrink it a bit to ensure that it intersects with this outer line
			float indent = 0.01f;
			float x = r0.getX();
			float y = r0.getY();
			float outerA = r0.getOuterA() * (1 - indent);
			float outerB = r0.getOuterB() * (1 - indent);
			float h = (outerA - outerB) / (outerA + outerB);
			h *= h;
			double outerPerimeter = Math.PI * (outerA + outerB) * (1 + 3 * h / (10 + Math.sqrt(4 - 3 * h)));
			float patchSize = model.getLx() * model.getPerimeterStepSize();
			int n = (int) (outerPerimeter / patchSize);
			if (n > 0) {
				float[] vx = new float[n];
				float[] vy = new float[n];
				float theta;
				float delta = (float) (2 * Math.PI / n);
				for (int i = 0; i < n; i++) {
					theta = delta * i;
					vx[i] = (float) (x + outerA * Math.cos(theta));
					vy[i] = (float) (y + outerB * Math.sin(theta));
				}
				for (int i = 0; i < n - 1; i++)
					if (Line2D.linesIntersect(p1.x, p1.y, p2.x, p2.y, vx[i], vy[i], vx[i + 1], vy[i + 1]))
						return true;
				if (Line2D.linesIntersect(p1.x, p1.y, p2.x, p2.y, vx[n - 1], vy[n - 1], vx[0], vy[0]))
					return true;
				// expand it a bit to ensure that it intersects with this inner line
				float innerA = r0.getInnerA() * (1 + indent);
				float innerB = r0.getInnerB() * (1 + indent);
				h = (innerA - innerB) / (innerA + innerB);
				h *= h;
				double innerPerimeter = Math.PI * (innerA + innerB) * (1 + 3 * h / (10 + Math.sqrt(4 - 3 * h)));
				n = (int) (innerPerimeter / patchSize);
				vx = new float[n];
				vy = new float[n];
				delta = (float) (2 * Math.PI / n);
				for (int i = 0; i < n; i++) {
					theta = delta * i;
					vx[i] = (float) (x + innerA * Math.cos(theta));
					vy[i] = (float) (y + innerB * Math.sin(theta));
				}
				for (int i = 0; i < n - 1; i++)
					if (Line2D.linesIntersect(p1.x, p1.y, p2.x, p2.y, vx[i], vy[i], vx[i + 1], vy[i + 1]))
						return true;
				if (Line2D.linesIntersect(p1.x, p1.y, p2.x, p2.y, vx[n - 1], vy[n - 1], vx[0], vy[0]))
					return true;
			}

		}

		return false;

	}

	boolean reflect(Discrete p, boolean scatter) {
		float dt = model.getTimeStep();
		float predictedX = p.getRx() + p.getVx() * dt;
		float predictedY = p.getRy() + p.getVy() * dt;
		if (p instanceof Particle) {
			float dt2 = 0.5f * dt * dt;
			predictedX += ((Particle) p).ax * dt2;
			predictedY += ((Particle) p).ay * dt2;
		}
		Shape shape = getShape();
		boolean predictedToBeInShape = true; // optimization flag: if the predicted position is not within this part, skip the costly reflection calculation
		if (p instanceof Photon)
			predictedToBeInShape = shape.contains(predictedX, predictedY);
		if (shape instanceof Rectangle2D.Float) {
			if (predictedToBeInShape)
				return reflect((Rectangle2D.Float) shape, p, predictedX, predictedY, scatter);
		} else if (shape instanceof Polygon2D) {
			if (predictedToBeInShape)
				return reflect((Polygon2D) shape, p, predictedX, predictedY, scatter);
		} else if (shape instanceof Blob2D) {
			if (predictedToBeInShape)
				return reflect((Blob2D) shape, p, predictedX, predictedY, scatter);
		} else if (shape instanceof Ellipse2D.Float) {
			if (predictedToBeInShape)
				return reflect((Ellipse2D.Float) shape, p, predictedX, predictedY, scatter);
		} else if (shape instanceof Annulus) {
			if (predictedToBeInShape)
				return reflect((Annulus) shape, p, predictedX, predictedY, scatter);
		} else if (shape instanceof EllipticalAnnulus) {
			if (predictedToBeInShape)
				return reflect((EllipticalAnnulus) shape, p, predictedX, predictedY, scatter);
		}
		return false;
	}

	// simpler case, avoid trig for a faster implementation
	private boolean reflect(Rectangle2D.Float r, Discrete p, float predictedX, float predictedY, boolean scatter) {
		if (p instanceof Particle) {
			Particle particle = (Particle) p;
			float radius = particle.radius;
			float x0 = r.x;
			float y0 = r.y;
			float x1 = r.x + r.width;
			float y1 = r.y + r.height;
			boolean predictedToHit = predictedX - radius <= x1 && predictedX + radius >= x0 && predictedY - radius <= y1 && predictedY + radius >= y0;
			if (predictedToHit) {
				float impulse = 0;
				float hitX = predictedX, hitY = predictedY;
				if (particle.rx - radius <= x0) { // use the farthest point to decide if the particle is to the left
					impulse = Math.abs(particle.vx);
					particle.vx = -impulse * elasticity;
					hitX += radius + 0.5f * model.getLy() / model.getNy();
				} else if (particle.rx + radius >= x1) { // particle to the right
					impulse = Math.abs(particle.vx);
					particle.vx = impulse * elasticity;
					hitX -= radius + 0.5f * model.getLy() / model.getNy();
				}
				if (particle.ry - radius <= y0) { // particle above
					impulse = Math.abs(particle.vy);
					particle.vy = -impulse * elasticity;
					hitY += radius + 0.5f * model.getLy() / model.getNy();
				} else if (particle.ry + radius >= y1) { // particle below
					impulse = Math.abs(particle.vy);
					particle.vy = impulse * elasticity;
					hitY -= radius + 0.5f * model.getLy() / model.getNy();
				}
				if (elasticity < 1) {
					float energy = 0.5f * particle.mass * impulse * impulse * (1 - elasticity * elasticity);
					float volume = model.getLx() * model.getLy() / (model.getNx() * model.getNy());
					model.changeTemperatureAt(hitX, hitY, energy / (specificHeat * density * volume));
				}
				return true;
			}
		} else if (p instanceof Photon) {
			if (p.getRx() <= r.x) {
				if (scatter) {
					p.setVelocityAngle((float) (Math.PI * (0.5 + Math.random())));
				} else {
					p.setVx(-Math.abs(p.getVx()));
				}
			} else if (p.getRx() >= r.x + r.width) {
				if (scatter) {
					p.setVelocityAngle((float) (Math.PI * (0.5 - Math.random())));
				} else {
					p.setVx(Math.abs(p.getVx()));
				}
			}
			if (p.getRy() <= r.y) {
				if (scatter) {
					p.setVelocityAngle((float) (Math.PI * (1 + Math.random())));
				} else {
					p.setVy(-Math.abs(p.getVy()));
				}
			} else if (p.getRy() >= r.y + r.height) {
				if (scatter) {
					p.setVelocityAngle((float) (Math.PI * Math.random()));
				} else {
					p.setVy(Math.abs(p.getVy()));
				}
			}
			return true;
		}
		return false;
	}

	private boolean reflect(Blob2D b, Discrete p, float predictedX, float predictedY, boolean scatter) {
		boolean clockwise = b.isClockwise();
		int n = b.getPathPointCount();
		Point2D.Float v1, v2;
		Line2D.Float line = new Line2D.Float();
		for (int i = 0; i < n - 1; i++) {
			v1 = b.getPathPoint(i);
			v2 = b.getPathPoint(i + 1);
			line.setLine(v1, v2);
			if (reflectFromLine(p, line, predictedX, predictedY, scatter, clockwise))
				return true;
		}
		v1 = b.getPathPoint(n - 1);
		v2 = b.getPathPoint(0);
		line.setLine(v1, v2);
		if (reflectFromLine(p, line, predictedX, predictedY, scatter, clockwise))
			return true;
		return false;
	}

	private boolean reflect(Ellipse2D.Float e, Discrete p, float predictedX, float predictedY, boolean scatter) {
		float a = e.width * 0.5f;
		float b = e.height * 0.5f;
		float x = e.x + a;
		float y = e.y + b;
		int polygonize = 50;
		float[] vx = new float[polygonize];
		float[] vy = new float[polygonize];
		float theta;
		float delta = (float) (2 * Math.PI / polygonize);
		for (int i = 0; i < polygonize; i++) {
			theta = -delta * i;
			vx[i] = (float) (x + a * Math.cos(theta));
			vy[i] = (float) (y + b * Math.sin(theta));
		}
		Line2D.Float line = new Line2D.Float();
		for (int i = 0; i < polygonize - 1; i++) {
			line.setLine(vx[i], vy[i], vx[i + 1], vy[i + 1]);
			if (reflectFromLine(p, line, predictedX, predictedY, scatter, true))
				return true;
		}
		line.setLine(vx[polygonize - 1], vy[polygonize - 1], vx[0], vy[0]);
		if (reflectFromLine(p, line, predictedX, predictedY, scatter, true))
			return true;
		return false;
	}

	private boolean reflect(Annulus e, Discrete p, float predictedX, float predictedY, boolean scatter) {
		float rInner = e.getInnerDiameter() * 0.5f;
		float rOuter = e.getOuterDiameter() * 0.5f;
		float x = e.getX();
		float y = e.getY();
		boolean inside = new Ellipse2D.Float(x - rInner, y - rInner, 2 * rInner, 2 * rInner).contains(p.getRx(), p.getRy());
		float a = inside ? rInner : rOuter;
		int polygonize = 50;
		float[] vx = new float[polygonize];
		float[] vy = new float[polygonize];
		float theta;
		float delta = (float) (2 * Math.PI / polygonize);
		for (int i = 0; i < polygonize; i++) {
			theta = -delta * i;
			vx[i] = (float) (x + a * Math.cos(theta));
			vy[i] = (float) (y + a * Math.sin(theta));
		}
		Line2D.Float line = new Line2D.Float();
		for (int i = 0; i < polygonize - 1; i++) {
			line.setLine(vx[i], vy[i], vx[i + 1], vy[i + 1]);
			if (reflectFromLine(p, line, predictedX, predictedY, scatter, !inside))
				return true;
		}
		line.setLine(vx[polygonize - 1], vy[polygonize - 1], vx[0], vy[0]);
		if (reflectFromLine(p, line, predictedX, predictedY, scatter, !inside))
			return true;
		return false;
	}

	private boolean reflect(EllipticalAnnulus e, Discrete p, float predictedX, float predictedY, boolean scatter) {
		float innerA = e.getInnerA();
		float innerB = e.getInnerB();
		float outerA = e.getOuterA();
		float outerB = e.getOuterB();
		float x = e.getX();
		float y = e.getY();
		boolean inside = new Ellipse2D.Float(x - innerA, y - innerB, 2 * innerA, 2 * innerB).contains(p.getRx(), p.getRy());
		float a = inside ? innerA : outerA;
		float b = inside ? innerB : outerB;
		int polygonize = 50;
		float[] vx = new float[polygonize];
		float[] vy = new float[polygonize];
		float theta;
		float delta = (float) (2 * Math.PI / polygonize);
		for (int i = 0; i < polygonize; i++) {
			theta = -delta * i;
			vx[i] = (float) (x + a * Math.cos(theta));
			vy[i] = (float) (y + b * Math.sin(theta));
		}
		Line2D.Float line = new Line2D.Float();
		for (int i = 0; i < polygonize - 1; i++) {
			line.setLine(vx[i], vy[i], vx[i + 1], vy[i + 1]);
			if (reflectFromLine(p, line, predictedX, predictedY, scatter, !inside))
				return true;
		}
		line.setLine(vx[polygonize - 1], vy[polygonize - 1], vx[0], vy[0]);
		if (reflectFromLine(p, line, predictedX, predictedY, scatter, !inside))
			return true;
		return false;
	}

	private boolean reflect(Polygon2D r, Discrete p, float predictedX, float predictedY, boolean scatter) {
		boolean clockwise = r.isClockwise();
		int n = r.getVertexCount();
		Point2D.Float v1, v2;
		Line2D.Float line = new Line2D.Float();
		for (int i = 0; i < n - 1; i++) {
			v1 = r.getVertex(i);
			v2 = r.getVertex(i + 1);
			line.setLine(v1, v2);
			if (reflectFromLine(p, line, predictedX, predictedY, scatter, clockwise))
				return true;
		}
		v1 = r.getVertex(n - 1);
		v2 = r.getVertex(0);
		line.setLine(v1, v2);
		if (reflectFromLine(p, line, predictedX, predictedY, scatter, clockwise))
			return true;
		return false;
	}

	private boolean reflectFromLine(Discrete p, Line2D.Float line, float predictedX, float predictedY, boolean scatter, boolean clockwise) {
		if (line.x1 == line.x2 && line.y1 == line.y2)
			return false;
		boolean hit = false;
		if (p instanceof Photon) { // a photon doesn't have any size, use its center to detect collision
			hit = line.intersectsLine(p.getRx(), p.getRy(), predictedX, predictedY);
		} else if (p instanceof Particle) {
			Particle particle = (Particle) p;
			float r = particle.radius;
			hit = Line2D.ptSegDistSq(line.x1, line.y1, line.x2, line.y2, predictedX, predictedY) <= r * r;
		}
		if (hit) {
			float d12 = (float) Math.hypot(line.x1 - line.x2, line.y1 - line.y2);
			float sin = (clockwise ? line.y2 - line.y1 : line.y1 - line.y2) / d12;
			float cos = (clockwise ? line.x2 - line.x1 : line.x1 - line.x2) / d12;
			if (scatter) {
				double angle = -Math.PI * Math.random(); // remember internally the y-axis points downward
				double cos1 = Math.cos(angle);
				double sin1 = Math.sin(angle);
				double cos2 = cos1 * cos - sin1 * sin;
				double sin2 = sin1 * cos + cos1 * sin;
				p.setVx((float) (p.getSpeed() * cos2));
				p.setVy((float) (p.getSpeed() * sin2));
			} else {
				float u; // velocity component parallel to the line
				float w; // velocity component perpendicular to the line
				if (p instanceof Particle) {
					Particle particle = (Particle) p;
					u = particle.vx * cos + particle.vy * sin;
					w = particle.vy * cos - particle.vx * sin;
					w *= elasticity;
					if (Math.abs(w) < 0.01f)
						w = -Math.abs(w); // force the w component to point outwards
					p.setVx(u * cos + w * sin);
					p.setVy(u * sin - w * cos);
				} else {
					u = p.getVx() * cos + p.getVy() * sin;
					w = p.getVy() * cos - p.getVx() * sin;
					p.setVx(u * cos + w * sin);
					p.setVy(u * sin - w * cos);
				}
				if (p instanceof Particle && elasticity < 1) {
					Particle particle = (Particle) p;
					float hitX = predictedX + (particle.radius + 0.5f * model.getLx() / model.getNx()) * sin;
					float hitY = predictedY - (particle.radius + 0.5f * model.getLy() / model.getNy()) * cos;
					float energy = 0.5f * particle.mass * w * w * (1 - elasticity * elasticity);
					float volume = model.getLx() * model.getLy() / (model.getNx() * model.getNy());
					model.changeTemperatureAt(hitX, hitY, energy / (specificHeat * density * volume));
				}
			}
			return true;
		}
		return false;
	}

	public String toXml() {
		XmlCharacterEncoder xce = new XmlCharacterEncoder();
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
		} else if (getShape() instanceof Annulus) {
			Annulus ring = (Annulus) getShape();
			xml += "<ring";
			xml += " x=\"" + ring.getX() + "\"";
			xml += " y=\"" + ring.getY() + "\"";
			xml += " inner=\"" + ring.getInnerDiameter() + "\"";
			xml += " outer=\"" + ring.getOuterDiameter() + "\"/>";
		} else if (getShape() instanceof EllipticalAnnulus) {
			EllipticalAnnulus e = (EllipticalAnnulus) getShape();
			xml += "<annulus";
			xml += " x=\"" + e.getX() + "\"";
			xml += " y=\"" + e.getY() + "\"";
			xml += " innerA=\"" + e.getInnerA() + "\"";
			xml += " innerB=\"" + e.getInnerB() + "\"";
			xml += " outerA=\"" + e.getOuterA() + "\"";
			xml += " outerB=\"" + e.getOuterB() + "\"/>";
		}
		xml += "<elasticity>" + elasticity + "</elasticity>\n";
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
		if (thermistorReferenceTemperature != 0)
			xml += "<reference_temperature>" + thermistorReferenceTemperature + "</reference_temperature>\n";
		if (windSpeed != 0)
			xml += "<wind_speed>" + windSpeed + "</wind_speed>\n";
		if (windAngle != 0)
			xml += "<wind_angle>" + windAngle + "</wind_angle>\n";
		if (getUid() != null && !getUid().trim().equals(""))
			xml += "<uid>" + xce.encode(getUid()) + "</uid>\n";
		if (fillPattern instanceof ColorFill) {
			Color color = ((ColorFill) fillPattern).getColor();
			if (!color.equals(Color.GRAY)) {
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
			xml += "<label>" + xce.encode(label) + "</label>\n";
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
			s = useFahrenheit ? TWO_DECIMALS_FORMAT.format(temp * 1.8f + 32) + " \u00b0F" : TWO_DECIMALS_FORMAT.format(temp) + " \u00b0C";
		} else if (label.equalsIgnoreCase("%thermal_energy"))
			s = Math.round(model.getThermalEnergy(this)) + " J";
		else if (label.equalsIgnoreCase("%density"))
			s = (int) density + " kg/m\u00b3";
		else if (label.equalsIgnoreCase("%elasticity"))
			s = SHORT_LABEL_FORMAT.format(elasticity) + "";
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
			s = label.replace("%temperature", useFahrenheit ? TWO_DECIMALS_FORMAT.format(temperature * 1.8f + 32) + " \u00b0F" : TWO_DECIMALS_FORMAT.format(temperature) + " \u00b0C");
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
		} else if (getShape() instanceof Annulus) {
			area = ((Annulus) getShape()).getArea();
		} else if (getShape() instanceof EllipticalAnnulus) {
			area = ((EllipticalAnnulus) getShape()).getArea();
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
