package org.energy2d.system;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import org.energy2d.model.Anemometer;
import org.energy2d.model.Boundary;
import org.energy2d.model.Cloud;
import org.energy2d.model.Constants;
import org.energy2d.model.DirichletThermalBoundary;
import org.energy2d.model.Fan;
import org.energy2d.model.HeatFluxSensor;
import org.energy2d.model.Heliostat;
import org.energy2d.model.MassBoundary;
import org.energy2d.model.Particle;
import org.energy2d.model.ParticleFeeder;
import org.energy2d.model.SimpleMassBoundary;
import org.energy2d.model.ThermalBoundary;
import org.energy2d.model.Model2D;
import org.energy2d.model.NeumannThermalBoundary;
import org.energy2d.model.Part;
import org.energy2d.model.Thermometer;
import org.energy2d.model.Thermostat;
import org.energy2d.model.Tree;
import org.energy2d.util.ColorFill;
import org.energy2d.util.Scripter;
import org.energy2d.util.Texture;
import org.energy2d.util.XmlCharacterDecoder;
import org.energy2d.view.Picture;
import org.energy2d.view.TextBox;
import org.energy2d.view.View2D;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Charles Xie
 * 
 */
class XmlDecoder extends DefaultHandler {

	private System2D box;
	private String str;

	// model properties
	private float modelWidth = 10;
	private float modelHeight = 10;
	private float timeStep = 0.1f;
	private int measurementInterval = 100;
	private int controlInterval = 100;
	private int viewUpdateInterval = 20;
	private boolean sunny;
	private float sunAngle = (float) (Math.PI * 0.5);
	private float solarPowerDensity = 2000;
	private int solarRayCount = 24;
	private float solarRaySpeed = 0.1f;
	private int photonEmissionInterval = 20;
	private float perimeterStepSize = 0.05f;
	private boolean convective = true;
	private float zHeatDiffusivity;
	private boolean zHeatDiffusivityOnlyForFluid;
	private float gravitationalAcceleration = -1;
	private float thermophoreticCoefficient = 0;
	private float particleDrag = -1;
	private float particleHardness = -1;
	private float backgroundConductivity = Constants.AIR_THERMAL_CONDUCTIVITY;
	private float backgroundDensity = Constants.AIR_DENSITY;
	private float backgroundSpecificHeat = Constants.AIR_SPECIFIC_HEAT;
	private float backgroundViscosity = Constants.AIR_VISCOSITY;
	private float backgroundTemperature;
	private float thermalExpansionCoefficient = 0.00025f;
	private byte buoyancyApproximation = Model2D.BUOYANCY_AVERAGE_COLUMN;
	private byte gravityType = Model2D.GRAVITY_UNIFORM;
	private String nextSim, prevSim;

	// view properties
	private byte graphDataType, graphTimeUnit;
	private boolean fahrenheitUsed;
	private boolean viewFactorLines;
	private boolean borderTickmarks;
	private boolean grid;
	private boolean snapToGrid = true;
	private boolean isotherm;
	private boolean streamline;
	private boolean colorPalette;
	private byte colorPaletteType = View2D.RAINBOW;
	private boolean showLogo = true;
	private boolean controlPanel;
	private byte controlPanelPosition = 0;
	private byte heatMapType = View2D.HEATMAP_TEMPERATURE;
	private float colorPaletteX, colorPaletteY, colorPaletteW, colorPaletteH;
	private int gridSize = 10;
	private boolean velocity;
	private boolean heatFluxArrows;
	private boolean heatFluxLines;
	private boolean graphOn;
	private boolean clock = true;
	private boolean smooth = true;
	private float minimumTemperature;
	private float maximumTemperature = 50;
	private float fanRotationSpeedScaleFactor = 1;
	private String graphXLabel, graphYLabel;
	private float graphYmin = 0, graphYmax = 50;

	// discrete properties
	private String uid;
	private String label;

	// part properties
	private float partThermalConductivity = Float.NaN;
	private float partSpecificHeat = Float.NaN;
	private float partDensity = Float.NaN;
	private float partEmissivity = Float.NaN;
	private float partAbsorption = Float.NaN;
	private float partReflection = Float.NaN;
	private float partElasticity = 1;
	private boolean partScattering;
	private boolean partScatteringVisible = true;
	private float partTransmission = Float.NaN;
	private float temperature = Float.NaN;
	private float fanSpeed;
	private float fanAngle;
	private boolean partConstantTemperature = false;
	private float partPower = Float.NaN;
	private float partTemperatureCoefficient = 0;
	private float partReferenceTemperature = 0;
	private boolean partVisible = true;
	private boolean draggable = true;
	private boolean movable = true;
	private Color color;
	private Color velocityColor;
	private byte textureStyle;
	private int textureWidth = 10;
	private int textureHeight = 10;
	private Color textureForeground = Color.BLACK;
	private Color textureBackground = Color.WHITE;
	private boolean partFilled = true;
	private Part part;

	// particle properties
	private float particleRx = Float.NaN;
	private float particleRy = Float.NaN;
	private float particleVx = Float.NaN;
	private float particleVy = Float.NaN;
	private float particleTheta = Float.NaN;
	private float particleOmega = Float.NaN;
	private float particleRadius = Float.NaN;
	private float particleMass = Float.NaN;
	private Particle particle;

	XmlDecoder(System2D box) {
		this.box = box;
	}

	public void startDocument() {
		box.taskManager.clearCustomTasks();
		// reset for elements added later before XML was saved
		MassBoundary b = box.model.getMassBoundary();
		if (b instanceof SimpleMassBoundary) {
			SimpleMassBoundary smb = (SimpleMassBoundary) b;
			smb.setFlowTypeAtBorder(Boundary.LEFT, MassBoundary.REFLECTIVE);
			smb.setFlowTypeAtBorder(Boundary.RIGHT, MassBoundary.REFLECTIVE);
			smb.setFlowTypeAtBorder(Boundary.LOWER, MassBoundary.REFLECTIVE);
			smb.setFlowTypeAtBorder(Boundary.UPPER, MassBoundary.REFLECTIVE);
		}
	}

	public void endDocument() {

		box.view.setControlPanelVisible(controlPanel);
		box.view.setControlPanelPosition(controlPanelPosition);
		box.setPreviousSimulation(prevSim);
		box.setNextSimulation(nextSim);
		box.model.setLx(modelWidth);
		box.model.setLy(modelHeight);
		box.view.setArea(0, modelWidth, 0, modelHeight);
		box.model.setTimeStep(timeStep);
		box.measure.setInterval(measurementInterval);
		box.control.setInterval(controlInterval);
		box.repaint.setInterval(viewUpdateInterval);
		box.model.setSunny(sunny);
		box.model.setSunAngle(sunAngle);
		box.model.setSolarPowerDensity(solarPowerDensity);
		box.model.setSolarRayCount(solarRayCount);
		box.model.setSolarRaySpeed(solarRaySpeed);
		box.model.setPhotonEmissionInterval(photonEmissionInterval);
		box.model.setPerimeterStepSize(perimeterStepSize);
		box.model.setConvective(convective);
		box.model.setZHeatDiffusivity(zHeatDiffusivity);
		box.model.setZHeatDiffusivityOnlyForFluid(zHeatDiffusivityOnlyForFluid);
		if (gravitationalAcceleration >= 0)
			box.model.setGravitationalAcceleration(gravitationalAcceleration);
		box.model.setThermophoreticCoefficient(thermophoreticCoefficient);
		if (particleDrag >= 0)
			box.model.setParticleDrag(particleDrag);
		if (particleHardness >= 0)
			box.model.setParticleHardness(particleHardness);
		box.model.setBackgroundConductivity(backgroundConductivity);
		box.model.setBackgroundDensity(backgroundDensity);
		box.model.setBackgroundSpecificHeat(backgroundSpecificHeat);
		box.model.setBackgroundTemperature(backgroundTemperature);
		box.model.setBackgroundViscosity(backgroundViscosity);
		box.model.setThermalExpansionCoefficient(thermalExpansionCoefficient);
		box.model.setBuoyancyApproximation(buoyancyApproximation);
		box.model.setGravityType(gravityType);

		box.view.setGraphDataType(graphDataType);
		box.view.setGraphTimeUnit(graphTimeUnit);
		box.view.setFahrenheitUsed(fahrenheitUsed);
		box.view.setViewFactorLinesOn(viewFactorLines);
		box.view.setBorderTickmarksOn(borderTickmarks);
		box.view.setGridOn(grid);
		box.view.setSnapToGrid(snapToGrid);
		box.view.setGridSize(gridSize);
		box.view.setIsothermOn(isotherm);
		box.view.setStreamlineOn(streamline);
		box.view.setVelocityOn(velocity);
		box.view.setHeatFluxArrowsOn(heatFluxArrows);
		box.view.setHeatFluxLinesOn(heatFluxLines);
		box.view.setColorPaletteOn(colorPalette);
		box.view.setColorPaletteType(colorPaletteType);
		box.view.setShowLogo(showLogo);
		box.view.setHeatMapType(heatMapType);
		float xColorPalette = colorPaletteX > 1 ? colorPaletteX / box.view.getWidth() : colorPaletteX;
		float yColorPalette = colorPaletteY > 1 ? colorPaletteY / box.view.getHeight() : colorPaletteY;
		float wColorPalette = colorPaletteW > 1 ? colorPaletteW / box.view.getWidth() : colorPaletteW;
		float hColorPalette = colorPaletteH > 1 ? colorPaletteH / box.view.getHeight() : colorPaletteH;
		box.view.setColorPaletteRectangle(xColorPalette, yColorPalette, wColorPalette, hColorPalette);
		box.view.setMinimumTemperature(minimumTemperature);
		box.view.setMaximumTemperature(maximumTemperature);
		box.view.setFanRotationSpeedScaleFactor(fanRotationSpeedScaleFactor);
		box.view.setClockOn(clock);
		box.view.setSmooth(smooth);
		box.view.setGraphOn(graphOn);
		if (graphXLabel != null)
			box.view.setGraphXLabel(graphXLabel);
		if (graphYLabel != null)
			box.view.setGraphYLabel(graphYLabel);
		if (graphYmin != 0)
			box.view.setGraphYmin(graphYmin);
		if (graphYmax != 50)
			box.view.setGraphYmax(graphYmax);

		box.model.refreshPowerArray();
		box.model.refreshTemperatureBoundaryArray();
		box.model.refreshMaterialPropertyArrays();
		box.model.setInitialTemperature();
		if (box.model.isRadiative())
			box.model.generateViewFactorMesh();

		// since we don't know the width and height of the model until now, we have to fix the locations and the sizes of
		// the sensors, since they are relative to the size of the model.
		List<Thermometer> thermometers = box.model.getThermometers();
		if (!thermometers.isEmpty()) {
			synchronized (thermometers) {
				for (Thermometer t : thermometers) {
					Rectangle2D.Float r = (Rectangle2D.Float) t.getShape();
					r.width = Thermometer.RELATIVE_WIDTH * modelWidth;
					r.height = Thermometer.RELATIVE_HEIGHT * modelHeight;
					r.x = r.x - 0.5f * r.width;
					r.y = r.y - 0.5f * r.height;
				}
			}
		}
		List<HeatFluxSensor> heatFluxSensors = box.model.getHeatFluxSensors();
		if (!heatFluxSensors.isEmpty()) {
			synchronized (heatFluxSensors) {
				for (HeatFluxSensor h : heatFluxSensors) {
					Rectangle2D.Float r = (Rectangle2D.Float) h.getShape();
					r.width = HeatFluxSensor.RELATIVE_WIDTH * modelWidth;
					r.height = HeatFluxSensor.RELATIVE_HEIGHT * modelHeight;
					r.x = r.x - 0.5f * r.width;
					r.y = r.y - 0.5f * r.height;
					box.model.measure(h);
				}
			}
		}
		List<Anemometer> anemometers = box.model.getAnemometers();
		if (!anemometers.isEmpty()) {
			synchronized (anemometers) {
				for (Anemometer a : anemometers) {
					Rectangle2D.Float r = (Rectangle2D.Float) a.getShape();
					r.width = Anemometer.RELATIVE_WIDTH * modelWidth;
					r.height = Anemometer.RELATIVE_HEIGHT * modelHeight;
					r.x = r.x - 0.5f * r.width;
					r.y = r.y - 0.5f * r.height;
				}
			}
		}

		box.view.repaint();

		resetGlobalVariables();

	}

	public void startElement(String uri, String localName, String qName, Attributes attrib) {

		str = null;

		String attribName, attribValue;

		if (qName == "task") {
			if (attrib != null) {
				boolean enabled = true;
				int interval = -1, lifetime = Task.PERMANENT, priority = 1;
				String uid = null, description = null, script = null;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "interval") {
						interval = Integer.parseInt(attribValue);
					} else if (attribName == "lifetime") {
						lifetime = Integer.parseInt(attribValue);
					} else if (attribName == "priority") {
						priority = Integer.parseInt(attribValue);
					} else if (attribName == "uid") {
						uid = attribValue;
					} else if (attribName == "description") {
						description = attribValue;
					} else if (attribName == "script") {
						script = attribValue;
					} else if (attribName == "enabled") {
						enabled = Boolean.parseBoolean(attribValue);
					}
				}
				Task t = new Task(uid, interval, lifetime) {
					@Override
					public void execute() {
						if (getScript() != null)
							box.taskManager.runScript(getScript());
					}
				};
				t.setSystemTask(false);
				t.setEnabled(enabled);
				t.setScript(new XmlCharacterDecoder().decode(script));
				t.setDescription(new XmlCharacterDecoder().decode(description));
				t.setPriority(priority);
				box.taskManager.add(t);
				box.taskManager.processPendingRequests();
			}
		} else if (qName == "rectangle") {
			if (attrib != null) {
				float x = Float.NaN, y = Float.NaN, w = Float.NaN, h = Float.NaN;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "x") {
						x = Float.parseFloat(attribValue);
					} else if (attribName == "y") {
						y = Float.parseFloat(attribValue);
					} else if (attribName == "width") {
						w = Float.parseFloat(attribValue);
					} else if (attribName == "height") {
						h = Float.parseFloat(attribValue);
					}
				}
				if (!Float.isNaN(x) && !Float.isNaN(y) && !Float.isNaN(w) && !Float.isNaN(h))
					part = box.model.addRectangularPart(x, y, w, h);
			}
		} else if (qName == "ellipse") {
			if (attrib != null) {
				float x = Float.NaN, y = Float.NaN, a = Float.NaN, b = Float.NaN;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "x") {
						x = Float.parseFloat(attribValue);
					} else if (attribName == "y") {
						y = Float.parseFloat(attribValue);
					} else if (attribName == "a") {
						a = Float.parseFloat(attribValue);
					} else if (attribName == "b") {
						b = Float.parseFloat(attribValue);
					}
				}
				if (!Float.isNaN(x) && !Float.isNaN(y) && !Float.isNaN(a) && !Float.isNaN(b))
					part = box.model.addEllipticalPart(x, y, a, b);
			}
		} else if (qName == "ring") {
			if (attrib != null) {
				float x = Float.NaN, y = Float.NaN, inner = Float.NaN, outer = Float.NaN;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "x") {
						x = Float.parseFloat(attribValue);
					} else if (attribName == "y") {
						y = Float.parseFloat(attribValue);
					} else if (attribName == "inner") {
						inner = Float.parseFloat(attribValue);
					} else if (attribName == "outer") {
						outer = Float.parseFloat(attribValue);
					}
				}
				if (!Float.isNaN(x) && !Float.isNaN(y) && !Float.isNaN(inner) && !Float.isNaN(outer))
					part = box.model.addRingPart(x, y, inner, outer);
			}
		} else if (qName == "annulus") {
			if (attrib != null) {
				float x = Float.NaN, y = Float.NaN, innerA = Float.NaN, innerB = Float.NaN, outerA = Float.NaN, outerB = Float.NaN;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "x") {
						x = Float.parseFloat(attribValue);
					} else if (attribName == "y") {
						y = Float.parseFloat(attribValue);
					} else if (attribName == "innerA") {
						innerA = Float.parseFloat(attribValue);
					} else if (attribName == "innerB") {
						innerB = Float.parseFloat(attribValue);
					} else if (attribName == "outerA") {
						outerA = Float.parseFloat(attribValue);
					} else if (attribName == "outerB") {
						outerB = Float.parseFloat(attribValue);
					}
				}
				if (!Float.isNaN(x) && !Float.isNaN(y) && !Float.isNaN(innerA) && !Float.isNaN(innerB) && !Float.isNaN(outerA) && !Float.isNaN(outerB))
					part = box.model.addAnnulusPart(x, y, innerA, innerB, outerA, outerB);
			}
		} else if (qName == "polygon") {
			if (attrib != null) {
				int count = -1;
				String vertices = null;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "count") {
						count = Integer.parseInt(attribValue);
					} else if (attribName == "vertices") {
						vertices = attribValue;
					}
				}
				if (count > 0 && vertices != null) {
					float[] v = Scripter.parseArray(count * 2, vertices);
					float[] x = new float[count];
					float[] y = new float[count];
					for (int i = 0; i < count; i++) {
						x[i] = v[2 * i];
						y[i] = v[2 * i + 1];
					}
					part = box.model.addPolygonPart(x, y);
				}
			}
		} else if (qName == "blob") {
			if (attrib != null) {
				int count = -1;
				String points = null;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "count") {
						count = Integer.parseInt(attribValue);
					} else if (attribName == "points") {
						points = attribValue;
					}
				}
				if (count > 0 && points != null) {
					float[] v = Scripter.parseArray(count * 2, points);
					float[] x = new float[count];
					float[] y = new float[count];
					for (int i = 0; i < count; i++) {
						x[i] = v[2 * i];
						y[i] = v[2 * i + 1];
					}
					part = box.model.addBlobPart(x, y);
				}
			}
		} else if (qName == "particle") {
			particle = new Particle();
			box.model.addParticle(particle);
		} else if (qName == "temperature_at_border") {
			if (attrib != null) {
				float left = Float.NaN, right = Float.NaN, upper = Float.NaN, lower = Float.NaN;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "left") {
						left = Float.parseFloat(attribValue);
					} else if (attribName == "right") {
						right = Float.parseFloat(attribValue);
					} else if (attribName == "upper") {
						upper = Float.parseFloat(attribValue);
					} else if (attribName == "lower") {
						lower = Float.parseFloat(attribValue);
					}
				}
				if (!Float.isNaN(left) && !Float.isNaN(right) && !Float.isNaN(upper) && !Float.isNaN(lower)) {
					DirichletThermalBoundary b = null;
					ThermalBoundary boundary = box.model.getThermalBoundary();
					if (boundary instanceof DirichletThermalBoundary) {
						b = (DirichletThermalBoundary) boundary;
					} else {
						b = new DirichletThermalBoundary();
						box.model.setThermalBoundary(b);
					}
					b.setTemperatureAtBorder(Boundary.UPPER, upper);
					b.setTemperatureAtBorder(Boundary.RIGHT, right);
					b.setTemperatureAtBorder(Boundary.LOWER, lower);
					b.setTemperatureAtBorder(Boundary.LEFT, left);
				}
			}
		} else if (qName == "flux_at_border") { // heat flux
			if (attrib != null) {
				float left = Float.NaN, right = Float.NaN, upper = Float.NaN, lower = Float.NaN;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "left") {
						left = Float.parseFloat(attribValue);
					} else if (attribName == "right") {
						right = Float.parseFloat(attribValue);
					} else if (attribName == "upper") {
						upper = Float.parseFloat(attribValue);
					} else if (attribName == "lower") {
						lower = Float.parseFloat(attribValue);
					}
				}
				if (!Float.isNaN(left) && !Float.isNaN(right) && !Float.isNaN(upper) && !Float.isNaN(lower)) {
					NeumannThermalBoundary b = null;
					ThermalBoundary boundary = box.model.getThermalBoundary();
					if (boundary instanceof NeumannThermalBoundary) {
						b = (NeumannThermalBoundary) boundary;
					} else {
						b = new NeumannThermalBoundary();
						box.model.setThermalBoundary(b);
					}
					b.setFluxAtBorder(Boundary.UPPER, upper);
					b.setFluxAtBorder(Boundary.RIGHT, right);
					b.setFluxAtBorder(Boundary.LOWER, lower);
					b.setFluxAtBorder(Boundary.LEFT, left);
				}
			}
		} else if (qName == "mass_flow_at_border") {
			if (attrib != null) {
				byte left = MassBoundary.REFLECTIVE;
				byte right = MassBoundary.REFLECTIVE;
				byte upper = MassBoundary.REFLECTIVE;
				byte lower = MassBoundary.REFLECTIVE;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "left") {
						left = Byte.parseByte(attribValue);
					} else if (attribName == "right") {
						right = Byte.parseByte(attribValue);
					} else if (attribName == "upper") {
						upper = Byte.parseByte(attribValue);
					} else if (attribName == "lower") {
						lower = Byte.parseByte(attribValue);
					}
				}
				SimpleMassBoundary b = (SimpleMassBoundary) box.model.getMassBoundary();
				b.setFlowTypeAtBorder(Boundary.UPPER, upper);
				b.setFlowTypeAtBorder(Boundary.RIGHT, right);
				b.setFlowTypeAtBorder(Boundary.LOWER, lower);
				b.setFlowTypeAtBorder(Boundary.LEFT, left);
			}
		} else if (qName == "thermometer") {
			if (attrib != null) {
				float x = Float.NaN, y = Float.NaN;
				String label = null, uid = null, attachID = null;
				byte stencil = Thermometer.ONE_POINT;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "x") {
						x = Float.parseFloat(attribValue);
					} else if (attribName == "y") {
						y = Float.parseFloat(attribValue);
					} else if (attribName == "stencil") {
						stencil = Byte.parseByte(attribValue);
					} else if (attribName == "uid") {
						uid = attribValue;
					} else if (attribName == "attach") {
						attachID = attribValue;
					} else if (attribName == "label") {
						label = attribValue;
					}
				}
				if (!Float.isNaN(x) && !Float.isNaN(y)) {
					Thermometer t = box.model.addThermometer(x, y, uid, label, stencil);
					if (attachID != null)
						t.setAttachID(attachID);
				}
			}
		} else if (qName == "heat_flux_sensor") {
			if (attrib != null) {
				float x = Float.NaN, y = Float.NaN;
				String label = null, uid = null, attachID = null;
				float angle = 0;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "x") {
						x = Float.parseFloat(attribValue);
					} else if (attribName == "y") {
						y = Float.parseFloat(attribValue);
					} else if (attribName == "angle") {
						angle = Float.parseFloat(attribValue);
					} else if (attribName == "uid") {
						uid = attribValue;
					} else if (attribName == "attach") {
						attachID = attribValue;
					} else if (attribName == "label") {
						label = attribValue;
					}
				}
				if (!Float.isNaN(x) && !Float.isNaN(y)) {
					HeatFluxSensor h = box.model.addHeatFluxSensor(x, y, uid, label, angle);
					if (attachID != null)
						h.setAttachID(attachID);
				}
			}
		} else if (qName == "anemometer") {
			if (attrib != null) {
				float x = Float.NaN, y = Float.NaN;
				String label = null, uid = null, attachID = null;
				byte stencil = Anemometer.ONE_POINT;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "x") {
						x = Float.parseFloat(attribValue);
					} else if (attribName == "y") {
						y = Float.parseFloat(attribValue);
					} else if (attribName == "stencil") {
						stencil = Byte.parseByte(attribValue);
					} else if (attribName == "uid") {
						uid = attribValue;
					} else if (attribName == "attach") {
						attachID = attribValue;
					} else if (attribName == "label") {
						label = attribValue;
					}
				}
				if (!Float.isNaN(x) && !Float.isNaN(y)) {
					Anemometer a = box.model.addAnemometer(x, y, uid, label, stencil);
					if (attachID != null)
						a.setAttachID(attachID);
				}
			}
		} else if (qName == "thermostat") {
			if (attrib != null) {
				float setpoint = Float.NaN, deadband = Float.NaN;
				String thermometerUID = null, powerSourceUID = null;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "set_point") {
						setpoint = Float.parseFloat(attribValue);
					} else if (attribName == "deadband") {
						deadband = Float.parseFloat(attribValue);
					} else if (attribName == "thermometer") {
						thermometerUID = attribValue;
					} else if (attribName == "power_source") {
						powerSourceUID = attribValue;
					}
				}
				if (!Float.isNaN(setpoint) && !Float.isNaN(deadband) && powerSourceUID != null) {
					Part p = box.model.getPart(powerSourceUID);
					if (p != null) {
						Thermometer t = null;
						if (thermometerUID != null)
							t = box.model.getThermometer(thermometerUID);
						Thermostat ts = box.model.addThermostat(t, p);
						ts.setDeadband(deadband);
						ts.setSetPoint(setpoint);
					}
				}
			}
		} else if (qName == "cloud") {
			if (attrib != null) {
				float x = Float.NaN, y = Float.NaN, w = Float.NaN, h = Float.NaN;
				float speed = 0;
				String label = null, uid = null;
				Color color = Color.WHITE;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "x") {
						x = Float.parseFloat(attribValue);
					} else if (attribName == "y") {
						y = Float.parseFloat(attribValue);
					} else if (attribName == "width") {
						w = Float.parseFloat(attribValue);
					} else if (attribName == "height") {
						h = Float.parseFloat(attribValue);
					} else if (attribName == "speed") {
						speed = Float.parseFloat(attribValue);
					} else if (attribName == "uid") {
						uid = attribValue;
					} else if (attribName == "label") {
						label = attribValue;
					} else if (attribName == "color") {
						color = new Color(Integer.parseInt(attribValue, 16));
					}
				}
				if (!Float.isNaN(x) && !Float.isNaN(y) && !Float.isNaN(w) && !Float.isNaN(h)) {
					Cloud c = new Cloud(new Rectangle2D.Float(0, 0, w, h));
					c.setX(x);
					c.setY(y);
					c.setSpeed(speed);
					c.setUid(uid);
					c.setLabel(label);
					c.setColor(color);
					box.model.addCloud(c);
				}
			}
		} else if (qName == "tree") {
			if (attrib != null) {
				float x = Float.NaN, y = Float.NaN, w = Float.NaN, h = Float.NaN;
				byte type = Tree.REGULAR;
				String label = null, uid = null;
				Color color = Color.GREEN.darker();
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "x") {
						x = Float.parseFloat(attribValue);
					} else if (attribName == "y") {
						y = Float.parseFloat(attribValue);
					} else if (attribName == "width") {
						w = Float.parseFloat(attribValue);
					} else if (attribName == "height") {
						h = Float.parseFloat(attribValue);
					} else if (attribName == "type") {
						type = Byte.parseByte(attribValue);
					} else if (attribName == "uid") {
						uid = attribValue;
					} else if (attribName == "label") {
						label = attribValue;
					} else if (attribName == "color") {
						color = new Color(Integer.parseInt(attribValue, 16));
					}
				}
				if (!Float.isNaN(x) && !Float.isNaN(y) && !Float.isNaN(w) && !Float.isNaN(h)) {
					Tree t = new Tree(new Rectangle2D.Float(0, 0, w, h), type);
					t.setX(x);
					t.setY(y);
					t.setUid(uid);
					t.setLabel(label);
					t.setColor(color);
					box.model.addTree(t);
				}
			}
		} else if (qName == "text") {
			if (attrib != null) {
				float x = Float.NaN, y = Float.NaN;
				int size = 14, style = Font.PLAIN;
				String str = null, face = null, uid = null;
				Color color = null;
				boolean border = false;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "x") {
						x = Float.parseFloat(attribValue);
					} else if (attribName == "y") {
						y = Float.parseFloat(attribValue);
					} else if (attribName == "uid") {
						uid = attribValue;
					} else if (attribName == "string") {
						str = attribValue;
					} else if (attribName == "size") {
						size = Integer.parseInt(attribValue);
					} else if (attribName == "style") {
						style = Integer.parseInt(attribValue);
					} else if (attribName == "name") { // TODO: backward compatibility, to remove by 2013
						face = attribValue;
					} else if (attribName == "face") {
						face = attribValue;
					} else if (attribName == "color") {
						color = new Color(Integer.parseInt(attribValue, 16));
					} else if (attribName == "border") {
						border = Boolean.parseBoolean(attribValue);
					}
				}
				if (!Float.isNaN(x) && !Float.isNaN(y)) {
					TextBox t = box.view.addText(new XmlCharacterDecoder().decode(str), x, y);
					t.setUid(uid);
					t.setSize(size);
					t.setStyle(style);
					t.setFace(face);
					t.setColor(color);
					t.setBorder(border);
					box.view.repaint();
				}
			}
		} else if (qName == "image") {
			if (attrib != null) {
				float x = Float.NaN, y = Float.NaN, w = Float.NaN, h = Float.NaN;
				String filename = null, format = null, uid = null, data = null;
				boolean border = false, draggable = true;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "x") {
						x = Float.parseFloat(attribValue);
					} else if (attribName == "y") {
						y = Float.parseFloat(attribValue);
					} else if (attribName == "width") {
						w = Float.parseFloat(attribValue);
					} else if (attribName == "height") {
						h = Float.parseFloat(attribValue);
					} else if (attribName == "uid") {
						uid = attribValue;
					} else if (attribName == "data") {
						data = attribValue;
					} else if (attribName == "format") {
						format = attribValue;
					} else if (attribName == "filename") {
						filename = attribValue;
					} else if (attribName == "border") {
						border = Boolean.parseBoolean(attribValue);
					} else if (attribName == "draggable") {
						draggable = Boolean.parseBoolean(attribValue);
					}
				}
				if (!Float.isNaN(x) && !Float.isNaN(y) && data != null) {
					InputStream in = new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(data));
					Picture p = null;
					try {
						p = box.view.addPicture(ImageIO.read(in), format, filename, x, y);
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							in.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if (p != null) {
						p.setUid(uid);
						p.setBorder(border);
						p.setDraggable(draggable);
						p.setX(x);
						p.setY(y);
						if (!Float.isNaN(w))
							p.setWidth(w);
						if (!Float.isNaN(h))
							p.setHeight(h);
						box.view.repaint();
					}
				}
			}
		} else if (qName == "fan") {
			if (attrib != null) {
				float x = Float.NaN, y = Float.NaN, w = Float.NaN, h = Float.NaN;
				String label = null, uid = null;
				float speed = 0;
				float angle = 0;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "x") {
						x = Float.parseFloat(attribValue);
					} else if (attribName == "y") {
						y = Float.parseFloat(attribValue);
					} else if (attribName == "width") {
						w = Float.parseFloat(attribValue);
					} else if (attribName == "height") {
						h = Float.parseFloat(attribValue);
					} else if (attribName == "speed") {
						speed = Float.parseFloat(attribValue);
					} else if (attribName == "angle") {
						angle = Float.parseFloat(attribValue);
					} else if (attribName == "uid") {
						uid = attribValue;
					} else if (attribName == "label") {
						label = attribValue;
					}
				}
				if (!Float.isNaN(x) && !Float.isNaN(y) && !Float.isNaN(w) && !Float.isNaN(h)) {
					Fan f = new Fan(new Rectangle2D.Float(x, y, w, h));
					f.setUid(uid);
					f.setLabel(label);
					f.setSpeed(speed);
					f.setAngle(angle);
					box.model.addFan(f);
				}
			}
		} else if (qName == "heliostat") {
			if (attrib != null) {
				float x = Float.NaN, y = Float.NaN, w = Float.NaN, h = Float.NaN;
				String label = null, uid = null, targetID = null;
				byte type = Heliostat.MIRROR;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "x") {
						x = Float.parseFloat(attribValue);
					} else if (attribName == "y") {
						y = Float.parseFloat(attribValue);
					} else if (attribName == "type") {
						type = Byte.parseByte(attribValue);
					} else if (attribName == "width") {
						w = Float.parseFloat(attribValue);
					} else if (attribName == "height") {
						h = Float.parseFloat(attribValue);
					} else if (attribName == "uid") {
						uid = attribValue;
					} else if (attribName == "target") {
						targetID = attribValue;
					} else if (attribName == "label") {
						label = attribValue;
					}
				}
				if (!Float.isNaN(x) && !Float.isNaN(y) && !Float.isNaN(w) && !Float.isNaN(h)) {
					Heliostat hs = new Heliostat(new Rectangle2D.Float(x, y, w, h), box.model);
					hs.setUid(uid);
					hs.setType(type);
					hs.setLabel(label);
					if (targetID != null)
						hs.setTarget(box.model.getPart(targetID));
					box.model.addHeliostat(hs);
				}
			}
		} else if (qName == "particle_feeder") {
			if (attrib != null) {
				float x = Float.NaN, y = Float.NaN, mass = Float.NaN, radius = Float.NaN;
				String label = null, uid = null;
				float period = Float.NaN;
				int maximum = 0;
				Color color = Color.WHITE;
				Color velocityColor = Color.BLACK;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "x") {
						x = Float.parseFloat(attribValue);
					} else if (attribName == "y") {
						y = Float.parseFloat(attribValue);
					} else if (attribName == "mass") {
						mass = Float.parseFloat(attribValue);
					} else if (attribName == "radius") {
						radius = Float.parseFloat(attribValue);
					} else if (attribName == "period") {
						period = Float.parseFloat(attribValue);
					} else if (attribName == "maximum") {
						maximum = Integer.parseInt(attribValue);
					} else if (attribName == "uid") {
						uid = attribValue;
					} else if (attribName == "label") {
						label = attribValue;
					} else if (attribName == "color") {
						color = new Color(Integer.parseInt(attribValue, 16));
					} else if (attribName == "velocity_color") {
						velocityColor = new Color(Integer.parseInt(attribValue, 16));
					}
				}
				if (!Float.isNaN(x) && !Float.isNaN(y)) {
					ParticleFeeder pf = new ParticleFeeder(x, y);
					pf.setUid(uid);
					pf.setLabel(label);
					pf.setColor(color);
					pf.setVelocityColor(velocityColor);
					if (!Float.isNaN(mass))
						pf.setMass(mass);
					if (!Float.isNaN(radius))
						pf.setRadius(radius);
					if (!Float.isNaN(period))
						pf.setPeriod(period);
					if (maximum > 0)
						pf.setMaximum(maximum);
					box.model.addParticleFeeder(pf);
				}
			}
		}

	}

	public void endElement(String uri, String localName, String qName) {

		if (qName == "next_sim") {
			nextSim = str;
		} else if (qName == "prev_sim") {
			prevSim = str;
		} else if (qName == "model_width") {
			modelWidth = Float.parseFloat(str);
		} else if (qName == "model_height") {
			modelHeight = Float.parseFloat(str);
		} else if (qName == "timestep") {
			timeStep = Float.parseFloat(str);
		} else if (qName == "measurement_interval") {
			measurementInterval = Integer.parseInt(str);
		} else if (qName == "control_interval") {
			controlInterval = Integer.parseInt(str);
		} else if (qName == "viewupdate_interval") {
			viewUpdateInterval = Integer.parseInt(str);
		} else if (qName == "sunny") {
			sunny = Boolean.parseBoolean(str);
		} else if (qName == "sun_angle") {
			sunAngle = Float.parseFloat(str);
		} else if (qName == "solar_power_density") {
			solarPowerDensity = Float.parseFloat(str);
		} else if (qName == "solar_ray_count") {
			solarRayCount = Integer.parseInt(str);
		} else if (qName == "solar_ray_speed") {
			solarRaySpeed = Float.parseFloat(str);
		} else if (qName == "photon_emission_interval") {
			photonEmissionInterval = Integer.parseInt(str);
		} else if (qName == "perimeter_step_size") {
			perimeterStepSize = Float.parseFloat(str);
		} else if (qName == "z_heat_diffusivity") {
			zHeatDiffusivity = Float.parseFloat(str);
		} else if (qName == "z_heat_diffusivity_only_for_fluid") {
			zHeatDiffusivityOnlyForFluid = Boolean.parseBoolean(str);
		} else if (qName == "gravitational_acceleration") {
			gravitationalAcceleration = Float.parseFloat(str);
		} else if (qName == "thermophoretic_coefficient") {
			thermophoreticCoefficient = Float.parseFloat(str);
		} else if (qName == "particle_drag") {
			particleDrag = Float.parseFloat(str);
		} else if (qName == "particle_hardness") {
			particleHardness = Float.parseFloat(str);
		} else if (qName == "convective") {
			convective = Boolean.parseBoolean(str);
		} else if (qName == "background_conductivity") {
			backgroundConductivity = Float.parseFloat(str);
		} else if (qName == "background_density") {
			backgroundDensity = Float.parseFloat(str);
		} else if (qName == "background_specific_heat") {
			backgroundSpecificHeat = Float.parseFloat(str);
		} else if (qName == "background_temperature") {
			backgroundTemperature = Float.parseFloat(str);
		} else if (qName == "background_viscosity") {
			backgroundViscosity = Float.parseFloat(str);
		} else if (qName == "thermal_buoyancy" || qName == "thermal_expansion_coefficient") {
			thermalExpansionCoefficient = Float.parseFloat(str);
		} else if (qName == "buoyancy_approximation") {
			buoyancyApproximation = Byte.parseByte(str);
		} else if (qName == "gravity_type") {
			gravityType = Byte.parseByte(str);
		} else if (qName == "minimum_temperature") {
			minimumTemperature = Float.parseFloat(str);
		} else if (qName == "maximum_temperature") {
			maximumTemperature = Float.parseFloat(str);
		} else if (qName == "fan_rotation_speed_scale_factor") {
			fanRotationSpeedScaleFactor = Float.parseFloat(str);
		} else if (qName == "graph_data_type") {
			graphDataType = Byte.parseByte(str);
		} else if (qName == "graph_time_unit") {
			graphTimeUnit = Byte.parseByte(str);
		} else if (qName == "view_factor_lines") {
			viewFactorLines = Boolean.parseBoolean(str);
		} else if (qName == "ruler" || qName == "border_tickmarks") {
			borderTickmarks = Boolean.parseBoolean(str);
		} else if (qName == "fahrenheit_used") {
			fahrenheitUsed = Boolean.parseBoolean(str);
		} else if (qName == "isotherm") {
			isotherm = Boolean.parseBoolean(str);
		} else if (qName == "streamline") {
			streamline = Boolean.parseBoolean(str);
		} else if (qName == "velocity") {
			velocity = Boolean.parseBoolean(str);
		} else if (qName == "heat_flux_arrow") {
			heatFluxArrows = Boolean.parseBoolean(str);
		} else if (qName == "heat_flux_line") {
			heatFluxLines = Boolean.parseBoolean(str);
		} else if (qName == "grid") {
			grid = Boolean.parseBoolean(str);
		} else if (qName == "snap_to_grid") {
			snapToGrid = Boolean.parseBoolean(str);
		} else if (qName == "grid_size") {
			gridSize = Integer.parseInt(str);
		} else if (qName == "color_palette") {
			colorPalette = Boolean.parseBoolean(str);
		} else if (qName == "color_palette_type") {
			colorPaletteType = Byte.parseByte(str);
		} else if (qName == "brand") {
			showLogo = Boolean.parseBoolean(str);
		} else if (qName == "control_panel") {
			controlPanel = Boolean.parseBoolean(str);
		} else if (qName == "control_panel_position") {
			controlPanelPosition = Byte.parseByte(str);
		} else if (qName == "heat_map") {
			heatMapType = Byte.parseByte(str);
		} else if (qName == "color_palette_x") {
			colorPaletteX = Float.parseFloat(str);
		} else if (qName == "color_palette_y") {
			colorPaletteY = Float.parseFloat(str);
		} else if (qName == "color_palette_w") {
			colorPaletteW = Float.parseFloat(str);
		} else if (qName == "color_palette_h") {
			colorPaletteH = Float.parseFloat(str);
		} else if (qName == "clock") {
			clock = Boolean.parseBoolean(str);
		} else if (qName == "smooth") {
			smooth = Boolean.parseBoolean(str);
		} else if (qName == "graph") {
			graphOn = Boolean.parseBoolean(str);
		} else if (qName == "graph_xlabel") {
			graphXLabel = str;
		} else if (qName == "graph_ylabel") {
			graphYLabel = str;
		} else if (qName == "graph_ymin") {
			graphYmin = Float.parseFloat(str);
		} else if (qName == "graph_ymax") {
			graphYmax = Float.parseFloat(str);
		} else if (qName == "elasticity") {
			partElasticity = Float.parseFloat(str);
		} else if (qName == "thermal_conductivity") {
			partThermalConductivity = Float.parseFloat(str);
		} else if (qName == "specific_heat") {
			partSpecificHeat = Float.parseFloat(str);
		} else if (qName == "density") {
			partDensity = Float.parseFloat(str);
		} else if (qName == "emissivity") {
			partEmissivity = Float.parseFloat(str);
		} else if (qName == "absorption") {
			partAbsorption = Float.parseFloat(str);
		} else if (qName == "reflection") {
			partReflection = Float.parseFloat(str);
		} else if (qName == "scattering") {
			partScattering = Boolean.parseBoolean(str);
		} else if (qName == "scattering_visible") {
			partScatteringVisible = Boolean.parseBoolean(str);
		} else if (qName == "transmission") {
			partTransmission = Float.parseFloat(str);
		} else if (qName == "constant_temperature") {
			partConstantTemperature = Boolean.parseBoolean(str);
		} else if (qName == "power") {
			partPower = Float.parseFloat(str);
		} else if (qName == "temperature_coefficient") {
			partTemperatureCoefficient = Float.parseFloat(str);
		} else if (qName == "reference_temperature") {
			partReferenceTemperature = Float.parseFloat(str);
		} else if (qName == "wind_speed") {
			fanSpeed = Float.parseFloat(str);
		} else if (qName == "wind_angle") {
			fanAngle = Float.parseFloat(str);
		} else if (qName == "texture_style") {
			textureStyle = Byte.parseByte(str);
		} else if (qName == "texture_width") {
			textureWidth = Integer.parseInt(str);
		} else if (qName == "texture_height") {
			textureHeight = Integer.parseInt(str);
		} else if (qName == "texture_fg") {
			textureForeground = new Color(Integer.parseInt(str, 16));
		} else if (qName == "texture_bg") {
			textureBackground = new Color(Integer.parseInt(str, 16));
		} else if (qName == "filled") {
			partFilled = Boolean.parseBoolean(str);
		} else if (qName == "visible") {
			partVisible = Boolean.parseBoolean(str);
		} else if (qName == "draggable") {
			draggable = Boolean.parseBoolean(str);
		} else if (qName == "movable") {
			movable = Boolean.parseBoolean(str);
		} else if (qName == "color") {
			color = new Color(Integer.parseInt(str, 16));
		} else if (qName == "velocity_color") {
			velocityColor = new Color(Integer.parseInt(str, 16));
		} else if (qName == "rx") {
			particleRx = Float.parseFloat(str);
		} else if (qName == "ry") {
			particleRy = Float.parseFloat(str);
		} else if (qName == "vx") {
			particleVx = Float.parseFloat(str);
		} else if (qName == "vy") {
			particleVy = Float.parseFloat(str);
		} else if (qName == "theta") {
			particleTheta = Float.parseFloat(str);
		} else if (qName == "omega") {
			particleOmega = Float.parseFloat(str);
		} else if (qName == "radius") {
			particleRadius = Float.parseFloat(str);
		} else if (qName == "mass") {
			particleMass = Float.parseFloat(str);
		} else if (qName == "uid") {
			uid = str;
		} else if (qName == "label") {
			label = str;
		} else if (qName == "temperature") {
			temperature = Float.parseFloat(str);
		} else if (qName == "boundary") {
			// nothing to do at this point
		} else if (qName == "part") {
			if (part != null) {
				if (!Float.isNaN(partThermalConductivity))
					part.setThermalConductivity(partThermalConductivity);
				if (!Float.isNaN(partSpecificHeat))
					part.setSpecificHeat(partSpecificHeat);
				if (!Float.isNaN(partDensity))
					part.setDensity(partDensity);
				if (!Float.isNaN(temperature))
					part.setTemperature(temperature);
				if (!Float.isNaN(partPower))
					part.setPower(partPower);
				if (!Float.isNaN(partEmissivity))
					part.setEmissivity(partEmissivity);
				if (!Float.isNaN(partAbsorption))
					part.setAbsorptivity(partAbsorption);
				if (!Float.isNaN(partReflection))
					part.setReflectivity(partReflection);
				if (!Float.isNaN(partTransmission))
					part.setTransmissivity(partTransmission);
				part.setElasticity(partElasticity);
				part.setScattering(partScattering);
				part.setScatteringVisible(partScatteringVisible);
				part.setThermistorTemperatureCoefficient(partTemperatureCoefficient);
				part.setThermistorReferenceTemperature(partReferenceTemperature);
				part.setWindAngle(fanAngle);
				part.setWindSpeed(fanSpeed);
				part.setConstantTemperature(partConstantTemperature);
				part.setDraggable(draggable);
				part.setVisible(partVisible);
				if (color != null)
					part.setFillPattern(new ColorFill(color));
				if (textureStyle != 0)
					part.setFillPattern(new Texture(textureForeground.getRGB(), textureBackground.getRGB(), textureStyle, textureWidth, textureHeight));
				part.setFilled(partFilled);
				part.setUid(uid);
				part.setLabel(label);
				resetPartVariables();
			}
		} else if (qName == "particle") {
			if (particle != null) {
				if (!Float.isNaN(particleRx))
					particle.setRx(particleRx);
				if (!Float.isNaN(particleRy))
					particle.setRy(particleRy);
				if (!Float.isNaN(particleVx))
					particle.setVx(particleVx);
				if (!Float.isNaN(particleVy))
					particle.setVy(particleVy);
				if (!Float.isNaN(particleTheta))
					particle.setTheta(particleTheta);
				if (!Float.isNaN(particleOmega))
					particle.setOmega(particleOmega);
				if (!Float.isNaN(particleRadius))
					particle.setRadius(particleRadius);
				if (!Float.isNaN(particleMass))
					particle.setMass(particleMass);
				if (!Float.isNaN(temperature))
					particle.setTemperature(temperature);
				if (color != null)
					particle.setFillPattern(new ColorFill(color));
				if (textureStyle != 0)
					particle.setFillPattern(new Texture(textureForeground.getRGB(), textureBackground.getRGB(), textureStyle, textureWidth, textureHeight));
				if (velocityColor != null)
					particle.setVelocityColor(velocityColor);
				particle.setUid(uid);
				particle.setLabel(label);
				particle.setMovable(movable);
				particle.setDraggable(draggable);
				particle.storeState();
				resetParticleVariables();
			}
		}

	}

	private void resetPartVariables() {
		partThermalConductivity = Float.NaN;
		partSpecificHeat = Float.NaN;
		partDensity = Float.NaN;
		partElasticity = 1;
		partConstantTemperature = false;
		partPower = Float.NaN;
		partTemperatureCoefficient = 0;
		partReferenceTemperature = 0;
		partEmissivity = Float.NaN;
		partAbsorption = Float.NaN;
		partReflection = Float.NaN;
		partTransmission = Float.NaN;
		partScattering = false;
		partScatteringVisible = true;
		fanSpeed = 0;
		fanAngle = 0;
		partVisible = true;
		partFilled = true;
		textureStyle = 0;
		textureWidth = 10;
		textureHeight = 10;
		textureForeground = Color.BLACK;
		textureBackground = Color.WHITE;
		uid = null;
		label = null;
		color = null;
		temperature = Float.NaN;
		draggable = true;
	}

	private void resetParticleVariables() {
		particleRx = Float.NaN;
		particleRy = Float.NaN;
		particleVx = Float.NaN;
		particleVy = Float.NaN;
		particleTheta = Float.NaN;
		particleOmega = Float.NaN;
		particleRadius = Float.NaN;
		particleMass = Float.NaN;
		movable = true;
		textureStyle = 0;
		textureWidth = 10;
		textureHeight = 10;
		textureForeground = Color.BLACK;
		textureBackground = Color.WHITE;
		uid = null;
		label = null;
		color = null;
		velocityColor = null;
		temperature = Float.NaN;
		draggable = true;
	}

	private void resetGlobalVariables() {

		// model properties
		nextSim = null;
		prevSim = null;
		modelWidth = 10;
		modelHeight = 10;
		timeStep = 1;
		measurementInterval = 100;
		controlInterval = 100;
		viewUpdateInterval = 20;
		sunny = false;
		sunAngle = (float) Math.PI * 0.5f;
		solarPowerDensity = 2000;
		solarRayCount = 24;
		solarRaySpeed = 0.1f;
		photonEmissionInterval = 20;
		perimeterStepSize = 0.05f;
		zHeatDiffusivity = 0;
		zHeatDiffusivityOnlyForFluid = false;
		gravitationalAcceleration = -1;
		thermophoreticCoefficient = 0;
		particleDrag = -1;
		particleHardness = -1;
		convective = true;
		backgroundConductivity = Constants.AIR_THERMAL_CONDUCTIVITY;
		backgroundDensity = Constants.AIR_DENSITY;
		backgroundSpecificHeat = Constants.AIR_SPECIFIC_HEAT;
		backgroundViscosity = Constants.AIR_VISCOSITY;
		backgroundTemperature = 0;
		thermalExpansionCoefficient = 0;
		buoyancyApproximation = Model2D.BUOYANCY_AVERAGE_COLUMN;
		gravityType = Model2D.GRAVITY_UNIFORM;

		// view properties
		graphDataType = 0;
		graphTimeUnit = 0;
		fahrenheitUsed = false;
		viewFactorLines = false;
		borderTickmarks = false;
		grid = false;
		snapToGrid = true;
		gridSize = 10;
		isotherm = false;
		streamline = false;
		colorPalette = false;
		colorPaletteType = View2D.RAINBOW;
		velocity = false;
		heatFluxArrows = false;
		heatFluxLines = false;
		graphOn = false;
		clock = true;
		showLogo = true;
		controlPanel = false;
		controlPanelPosition = 0;
		smooth = true;
		minimumTemperature = 0;
		maximumTemperature = 50;
		fanRotationSpeedScaleFactor = 1;
		graphXLabel = null;
		graphYLabel = null;
		graphYmin = 0;
		graphYmax = 50;
		heatMapType = View2D.HEATMAP_TEMPERATURE;

	}

	public void characters(char[] ch, int start, int length) { // SAX parse breaks from entitiy characters
		if (str == null) {
			str = new String(ch, start, length);
		} else {
			str += new String(ch, start, length);
		}
	}

	public void warning(SAXParseException e) {
		e.printStackTrace();
	}

	public void error(SAXParseException e) {
		e.printStackTrace();
	}

	public void fatalError(SAXParseException e) {
		e.printStackTrace();
	}

}
