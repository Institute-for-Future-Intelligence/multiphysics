package org.concord.energy2d.system;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.concord.energy2d.model.Anemometer;
import org.concord.energy2d.model.Boundary;
import org.concord.energy2d.model.Cloud;
import org.concord.energy2d.model.Constants;
import org.concord.energy2d.model.DirichletThermalBoundary;
import org.concord.energy2d.model.HeatFluxSensor;
import org.concord.energy2d.model.MassBoundary;
import org.concord.energy2d.model.Particle;
import org.concord.energy2d.model.SimpleMassBoundary;
import org.concord.energy2d.model.ThermalBoundary;
import org.concord.energy2d.model.Model2D;
import org.concord.energy2d.model.NeumannThermalBoundary;
import org.concord.energy2d.model.Part;
import org.concord.energy2d.model.Thermometer;
import org.concord.energy2d.model.Thermostat;
import org.concord.energy2d.model.Tree;
import org.concord.energy2d.util.ColorFill;
import org.concord.energy2d.util.Scripter;
import org.concord.energy2d.util.Texture;
import org.concord.energy2d.util.XmlCharacterDecoder;
import org.concord.energy2d.view.TextBox;
import org.concord.energy2d.view.View2D;
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
	private boolean convective = true;
	private float zHeatDiffusivity;
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
	private byte graphDataType;
	private boolean fahrenheitUsed;
	private boolean ruler;
	private boolean grid;
	private boolean snapToGrid = true;
	private boolean isotherm;
	private boolean streamline;
	private boolean colorPalette;
	private byte colorPaletteType = View2D.RAINBOW;
	private boolean brand = true;
	private boolean controlPanel;
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
	private boolean partScattering;
	private boolean partScatteringVisible = true;
	private float partTransmission = Float.NaN;
	private float partTemperature = Float.NaN;
	private float partWindSpeed;
	private float partWindAngle;
	private boolean partConstantTemperature = false;
	private float partPower = Float.NaN;
	private boolean partVisible = true;
	private boolean partDraggable = true;
	private Color partColor = Color.GRAY;
	private byte partTextureStyle;
	private int partTextureWidth = 10;
	private int partTextureHeight = 10;
	private Color partTextureForeground = Color.BLACK;
	private Color partTextureBackground = Color.WHITE;
	private boolean partFilled = true;
	private Part part;

	// particle properties
	private float particleRx = Float.NaN;
	private float particleRy = Float.NaN;
	private float particleVx = Float.NaN;
	private float particleVy = Float.NaN;
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
		box.model.setConvective(convective);
		box.model.setZHeatDiffusivity(zHeatDiffusivity);
		box.model.setBackgroundConductivity(backgroundConductivity);
		box.model.setBackgroundDensity(backgroundDensity);
		box.model.setBackgroundSpecificHeat(backgroundSpecificHeat);
		box.model.setBackgroundTemperature(backgroundTemperature);
		box.model.setBackgroundViscosity(backgroundViscosity);
		box.model.setThermalExpansionCoefficient(thermalExpansionCoefficient);
		box.model.setBuoyancyApproximation(buoyancyApproximation);
		box.model.setGravityType(gravityType);

		box.view.setGraphDataType(graphDataType);
		box.view.setFahrenheitUsed(fahrenheitUsed);
		box.view.setRulerOn(ruler);
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
		box.view.setFrankOn(brand);
		box.view.setHeatMapType(heatMapType);
		float xColorPalette = colorPaletteX > 1 ? colorPaletteX / box.view.getWidth() : colorPaletteX;
		float yColorPalette = colorPaletteY > 1 ? colorPaletteY / box.view.getHeight() : colorPaletteY;
		float wColorPalette = colorPaletteW > 1 ? colorPaletteW / box.view.getWidth() : colorPaletteW;
		float hColorPalette = colorPaletteH > 1 ? colorPaletteH / box.view.getHeight() : colorPaletteH;
		box.view.setColorPaletteRectangle(xColorPalette, yColorPalette, wColorPalette, hColorPalette);
		box.view.setMinimumTemperature(minimumTemperature);
		box.view.setMaximumTemperature(maximumTemperature);
		box.view.setClockOn(clock);
		box.view.setSmooth(smooth);
		box.view.setGraphOn(graphOn);
		if (graphXLabel != null)
			box.view.setGraphXLabel(graphXLabel);
		if (graphYLabel != null)
			box.view.setGraphYLabel(graphYLabel);
		if (graphYmin != 0)
			box.view.setGraphYmin(graphYmin);
		if (graphYmax != 0)
			box.view.setGraphYmax(graphYmax);

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

		box.model.refreshPowerArray();
		box.model.refreshTemperatureBoundaryArray();
		box.model.refreshMaterialPropertyArrays();
		box.model.setInitialTemperature();
		box.view.repaint();

		resetGlobalVariables();

	}

	public void startElement(String uri, String localName, String qName, Attributes attrib) {

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
				String label = null, uid = null;
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
					} else if (attribName == "label") {
						label = attribValue;
					}
				}
				if (!Float.isNaN(x) && !Float.isNaN(y))
					box.model.addThermometer(x, y, uid, label, stencil);
			}
		} else if (qName == "heat_flux_sensor") {
			if (attrib != null) {
				float x = Float.NaN, y = Float.NaN;
				String label = null, uid = null;
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
					} else if (attribName == "label") {
						label = attribValue;
					}
				}
				if (!Float.isNaN(x) && !Float.isNaN(y))
					box.model.addHeatFluxSensor(x, y, uid, label, angle);
			}
		} else if (qName == "anemometer") {
			if (attrib != null) {
				float x = Float.NaN, y = Float.NaN;
				String label = null, uid = null;
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
					} else if (attribName == "label") {
						label = attribValue;
					}
				}
				if (!Float.isNaN(x) && !Float.isNaN(y))
					box.model.addAnemometer(x, y, uid, label, stencil);
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
				Color color = Color.white;
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
		} else if (qName == "z_heat_diffusivity") {
			zHeatDiffusivity = Float.parseFloat(str);
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
		} else if (qName == "graph_data_type") {
			graphDataType = Byte.parseByte(str);
		} else if (qName == "ruler") {
			ruler = Boolean.parseBoolean(str);
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
			brand = Boolean.parseBoolean(str);
		} else if (qName == "control_panel") {
			controlPanel = Boolean.parseBoolean(str);
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
		} else if (qName == "temperature") {
			partTemperature = Float.parseFloat(str);
		} else if (qName == "constant_temperature") {
			partConstantTemperature = Boolean.parseBoolean(str);
		} else if (qName == "power") {
			partPower = Float.parseFloat(str);
		} else if (qName == "wind_speed") {
			partWindSpeed = Float.parseFloat(str);
		} else if (qName == "wind_angle") {
			partWindAngle = Float.parseFloat(str);
		} else if (qName == "texture_style") {
			partTextureStyle = Byte.parseByte(str);
		} else if (qName == "texture_width") {
			partTextureWidth = Integer.parseInt(str);
		} else if (qName == "texture_height") {
			partTextureHeight = Integer.parseInt(str);
		} else if (qName == "texture_fg") {
			partTextureForeground = new Color(Integer.parseInt(str, 16));
		} else if (qName == "texture_bg") {
			partTextureBackground = new Color(Integer.parseInt(str, 16));
		} else if (qName == "filled") {
			partFilled = Boolean.parseBoolean(str);
		} else if (qName == "visible") {
			partVisible = Boolean.parseBoolean(str);
		} else if (qName == "draggable") {
			partDraggable = Boolean.parseBoolean(str);
		} else if (qName == "color") {
			partColor = new Color(Integer.parseInt(str, 16));
		} else if (qName == "rx") {
			particleRx = Float.parseFloat(str);
		} else if (qName == "ry") {
			particleRy = Float.parseFloat(str);
		} else if (qName == "vx") {
			particleVx = Float.parseFloat(str);
		} else if (qName == "vy") {
			particleVy = Float.parseFloat(str);
		} else if (qName == "radius") {
			particleRadius = Float.parseFloat(str);
		} else if (qName == "mass") {
			particleMass = Float.parseFloat(str);
		} else if (qName == "uid") {
			uid = str;
		} else if (qName == "label") {
			label = str;
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
				if (!Float.isNaN(partTemperature))
					part.setTemperature(partTemperature);
				if (!Float.isNaN(partPower))
					part.setPower(partPower);
				if (!Float.isNaN(partEmissivity))
					part.setEmissivity(partEmissivity);
				if (!Float.isNaN(partAbsorption))
					part.setAbsorption(partAbsorption);
				if (!Float.isNaN(partReflection))
					part.setReflection(partReflection);
				if (!Float.isNaN(partTransmission))
					part.setTransmission(partTransmission);
				part.setScattering(partScattering);
				part.setScatteringVisible(partScatteringVisible);
				part.setWindAngle(partWindAngle);
				part.setWindSpeed(partWindSpeed);
				part.setConstantTemperature(partConstantTemperature);
				part.setDraggable(partDraggable);
				part.setVisible(partVisible);
				if (partColor != null)
					part.setFillPattern(new ColorFill(partColor));
				if (partTextureStyle != 0)
					part.setFillPattern(new Texture(partTextureForeground.getRGB(), partTextureBackground.getRGB(), partTextureStyle, partTextureWidth, partTextureHeight));
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
				if (!Float.isNaN(particleRadius))
					particle.setRadius(particleRadius);
				if (!Float.isNaN(particleMass))
					particle.setMass(particleMass);
			}
		}

	}

	private void resetPartVariables() {
		partThermalConductivity = Float.NaN;
		partSpecificHeat = Float.NaN;
		partDensity = Float.NaN;
		partTemperature = Float.NaN;
		partConstantTemperature = false;
		partPower = Float.NaN;
		partEmissivity = Float.NaN;
		partAbsorption = Float.NaN;
		partReflection = Float.NaN;
		partTransmission = Float.NaN;
		partScattering = false;
		partScatteringVisible = true;
		partWindSpeed = 0;
		partWindAngle = 0;
		partVisible = true;
		partDraggable = true;
		partColor = Color.GRAY;
		partFilled = true;
		partTextureStyle = 0;
		partTextureWidth = 10;
		partTextureHeight = 10;
		partTextureForeground = Color.BLACK;
		partTextureBackground = Color.WHITE;
		particleRx = Float.NaN;
		particleRy = Float.NaN;
		particleVx = Float.NaN;
		particleVy = Float.NaN;
		particleRadius = Float.NaN;
		particleMass = Float.NaN;
		uid = null;
		label = null;
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
		zHeatDiffusivity = 0;
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
		fahrenheitUsed = false;
		ruler = false;
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
		brand = true;
		controlPanel = false;
		smooth = true;
		minimumTemperature = 0;
		maximumTemperature = 50;
		graphXLabel = null;
		graphYLabel = null;
		graphYmin = 0;
		graphYmax = 50;
		heatMapType = View2D.HEATMAP_TEMPERATURE;

	}

	public void characters(char[] ch, int start, int length) {
		str = new String(ch, start, length);
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
