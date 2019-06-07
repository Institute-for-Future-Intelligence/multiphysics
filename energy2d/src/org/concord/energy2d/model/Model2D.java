package org.concord.energy2d.model;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.event.ManipulationListener;
import org.concord.energy2d.math.Annulus;
import org.concord.energy2d.math.Blob2D;
import org.concord.energy2d.math.EllipticalAnnulus;
import org.concord.energy2d.math.Polygon2D;

/**
 * Units:
 * <p>
 * Temperature: centigrade; Length: meter; Time: second; Thermal diffusivity: m^2/s; Power: centigrade/second.
 * <p>
 * Using a 1D array and then a convenience function I(i, j) =i + j x ny to find t(i, j) is about 12% faster than using a 2D array directly (Java 6). Hence, using 1D array for 2D functions doesn't result in significant performance improvements.
 *
 * @author Charles Xie
 */
public class Model2D {

    // Stefan's constant unit J/(s*m^2*K^-4)
    final static float STEFAN_CONSTANT = 5.67E-08f;

    public final static byte BUOYANCY_AVERAGE_ALL = 0;
    public final static byte BUOYANCY_AVERAGE_COLUMN = 1;
    public final static byte GRAVITY_UNIFORM = 0;
    final static byte GRAVITY_CENTRIC = 1;

    private int indexOfStep;

    private float backgroundConductivity = Constants.AIR_THERMAL_CONDUCTIVITY;
    private float backgroundSpecificHeat = Constants.AIR_SPECIFIC_HEAT;
    private float backgroundDensity = Constants.AIR_DENSITY;
    private float backgroundTemperature;
    private float maximumHeatCapacity = -1;

    // temperature array
    private float[][] t;

    // velocity x-component array (m/s)
    private float[][] u;

    // velocity y-component array (m/s)
    private float[][] v;

    // internal temperature boundary array
    private float[][] tb;

    // internal heat generation array
    private float[][] q;

    // wind speed
    private float[][] uWind, vWind;

    // conductivity array
    private float[][] conductivity;

    // specific heat array
    private float[][] specificHeat;

    // density array
    private float[][] density;

    // fluid cell array
    private boolean[][] fluidity;

    private final List<HeatFluxSensor> heatFluxSensors;
    private final List<Anemometer> anemometers;
    private final List<Thermometer> thermometers;
    private final List<Thermostat> thermostats;
    private final List<Part> parts;
    private final List<Particle> particles;
    private final List<Photon> photons;
    private final List<Cloud> clouds;
    private final List<Tree> trees;
    private final List<Fan> fans;
    private final List<Heliostat> heliostats;
    private final List<ParticleFeeder> particleFeeders;

    private PhotonSolver2D photonSolver;
    private RadiositySolver2D radiositySolver;
    private FluidSolver2D fluidSolver;
    private HeatSolver2D heatSolver;
    private ParticleSolver2D particleSolver;

    private boolean sunny;
    private int photonEmissionInterval = 20;
    private int radiosityInterval = 20;

    private int nx = 100;
    private int ny = 100;

    // length in x direction (unit: meter)
    private float lx = 10;

    // length in y direction (unit: meter)
    private float ly = 10;

    private float deltaX = lx / nx;
    private float deltaY = ly / ny;

    private boolean running;
    private boolean notifyReset;

    // optimization flags
    private boolean hasPartPower;
    private boolean radiative;

    // condition flags
    private boolean convective = true;

    private List<PropertyChangeListener> propertyChangeListeners;
    private List<ManipulationListener> manipulationListeners;
    private Runnable tasks;

    public Model2D() {

        t = new float[nx][ny];
        u = new float[nx][ny];
        v = new float[nx][ny];
        q = new float[nx][ny];
        tb = new float[nx][ny];
        uWind = new float[nx][ny];
        vWind = new float[nx][ny];
        conductivity = new float[nx][ny];
        specificHeat = new float[nx][ny];
        density = new float[nx][ny];
        fluidity = new boolean[nx][ny];

        parts = Collections.synchronizedList(new ArrayList<>());
        particles = Collections.synchronizedList(new ArrayList<>());
        heatFluxSensors = Collections.synchronizedList(new ArrayList<>());
        anemometers = Collections.synchronizedList(new ArrayList<>());
        thermometers = Collections.synchronizedList(new ArrayList<>());
        thermostats = Collections.synchronizedList(new ArrayList<>());
        photons = Collections.synchronizedList(new ArrayList<>());
        clouds = Collections.synchronizedList(new ArrayList<>());
        trees = Collections.synchronizedList(new ArrayList<>());
        fans = Collections.synchronizedList(new ArrayList<>());
        heliostats = Collections.synchronizedList(new ArrayList<>());
        particleFeeders = Collections.synchronizedList(new ArrayList<>());

        init();

        heatSolver = new HeatSolver2DImpl(nx, ny);
        heatSolver.setSpecificHeat(specificHeat);
        heatSolver.setConductivity(conductivity);
        heatSolver.setDensity(density);
        heatSolver.setPower(q);
        heatSolver.setVelocity(u, v);
        heatSolver.setTemperatureBoundary(tb);
        heatSolver.setFluidity(fluidity);

        fluidSolver = new FluidSolver2DImpl(nx, ny);
        fluidSolver.setFluidity(fluidity);
        fluidSolver.setTemperature(t);
        fluidSolver.setWindSpeed(uWind, vWind);

        photonSolver = new PhotonSolver2D(lx, ly);
        photonSolver.setPower(q);
        radiositySolver = new RadiositySolver2D(this);

        particleSolver = new ParticleSolver2D(this);

        setGridCellSize();

        propertyChangeListeners = new ArrayList<>();
        manipulationListeners = new ArrayList<>();

    }

    public int getNx() {
        return nx;
    }

    public int getNy() {
        return ny;
    }

    public void setTasks(Runnable r) {
        tasks = r;
    }

    public void setConvective(boolean convective) {
        this.convective = convective;
    }

    public boolean isConvective() {
        return convective;
    }

    /**
     * Imagine that the 2D plane is thermally coupled with a thin layer that has the background temperature
     */
    public void setZHeatDiffusivity(float zHeatDiffusivity) {
        heatSolver.zHeatDiffusivity = zHeatDiffusivity;
    }

    public float getZHeatDiffusivity() {
        return heatSolver.zHeatDiffusivity;
    }

    public void setZHeatDiffusivityOnlyForFluid(boolean zHeatDiffusivityOnlyForFluid) {
        heatSolver.zHeatDiffusivityOnlyForFluid = zHeatDiffusivityOnlyForFluid;
    }

    public boolean getZHeatDiffusivityOnlyForFluid() {
        return heatSolver.zHeatDiffusivityOnlyForFluid;
    }

    public void setThermophoreticCoefficient(float thermophoreticCoefficient) {
        particleSolver.thermophoreticCoefficient = thermophoreticCoefficient;
    }

    public float getThermophoreticCoefficient() {
        return particleSolver.thermophoreticCoefficient;
    }

    public void setGravitationalAcceleration(float g) {
        particleSolver.g = g;
    }

    public float getGravitationalAcceleration() {
        return particleSolver.g;
    }

    public void setParticleDrag(float drag) {
        particleSolver.drag = drag;
    }

    public float getParticleDrag() {
        return particleSolver.drag;
    }

    public void setParticleHardness(float epsilon) {
        particleSolver.epsilon = epsilon;
    }

    public float getParticleHardness() {
        return particleSolver.epsilon;
    }

    public void setGravityType(byte gravityType) {
        fluidSolver.setGravityType(gravityType);
    }

    public byte getGravityType() {
        return fluidSolver.getGravityType();
    }

    public void setThermalExpansionCoefficient(float thermalExpansionCoefficient) {
        fluidSolver.setThermalExpansionCoefficient(thermalExpansionCoefficient);
    }

    public float getThermalExpansionCoefficient() {
        return fluidSolver.getThermalExpansionCoefficient();
    }

    public void setBuoyancyApproximation(byte buoyancyApproximation) {
        fluidSolver.setBuoyancyApproximation(buoyancyApproximation);
    }

    public byte getBuoyancyApproximation() {
        return fluidSolver.getBuoyancyApproximation();
    }

    public void setBackgroundViscosity(float viscosity) {
        fluidSolver.setBackgroundViscosity(viscosity);
    }

    public float getBackgroundViscosity() {
        return fluidSolver.getViscosity();
    }

    public void setSunny(boolean sunny) {
        this.sunny = sunny;
        if (sunny) {
            radiative = true;
        } else {
            photons.clear();
        }
    }

    public boolean isSunny() {
        return sunny;
    }

    /**
     * synchronize the sun's angle with the clock, assuming sunrise at 6:00 and sunset at 18:00.
     */
    public void moveSun(float sunrise, float sunset) {
        float hour = getTime() / 3600f;
        int i = (int) hour;
        hour += (i % 24) - i;
        photonSolver.setSunAngle((hour - sunrise) / (sunset - sunrise) * (float) Math.PI);
        refreshPowerArray();
        if (getSunAngle() >= 0 && getSunAngle() <= Math.PI)
            refreshHeliostats();
    }

    public void setSunAngle(float sunAngle) {
        photonSolver.setSunAngle(sunAngle);
        if (!heliostats.isEmpty()) {
            for (Heliostat h : heliostats)
                h.setAngle();
        }
        if (Math.abs(sunAngle - photonSolver.getSunAngle()) > 0.001f)
            photons.clear();
    }

    public float getSunAngle() {
        return photonSolver.getSunAngle();
    }

    public void setSolarPowerDensity(float solarPowerDensity) {
        photonSolver.setSolarPowerDensity(solarPowerDensity);
    }

    public float getSolarPowerDensity() {
        return photonSolver.getSolarPowerDensity();
    }

    public void setSolarRayCount(int solarRayCount) {
        if (solarRayCount == photonSolver.getSolarRayCount())
            return;
        photons.clear();
        photonSolver.setSolarRayCount(solarRayCount);
    }

    public int getSolarRayCount() {
        return photonSolver.getSolarRayCount();
    }

    public void setSolarRaySpeed(float raySpeed) {
        photonSolver.setSolarRaySpeed(raySpeed);
    }

    public float getSolarRaySpeed() {
        return photonSolver.getSolarRaySpeed();
    }

    public void setPhotonEmissionInterval(int photonEmissionInterval) {
        this.photonEmissionInterval = photonEmissionInterval;
    }

    public int getPhotonEmissionInterval() {
        return photonEmissionInterval;
    }

    public void addPhoton(Photon p) {
        if (p != null)
            photons.add(p);
    }

    public void removePhoton(Photon p) {
        photons.remove(p);
    }

    public List<Photon> getPhotons() {
        return photons;
    }

    private void setGridCellSize() {
        heatSolver.setGridCellSize(deltaX, deltaY);
        fluidSolver.setGridCellSize(deltaX, deltaY);
        photonSolver.setGridCellSize(deltaX, deltaY);
    }

    public void setLx(float lx) {
        this.lx = lx;
        deltaX = lx / nx;
        setGridCellSize();
        photonSolver.setLx(lx);
    }

    public float getLx() {
        return lx;
    }

    public void setLy(float ly) {
        this.ly = ly;
        deltaY = ly / ny;
        setGridCellSize();
        photonSolver.setLy(ly);
    }

    public float getLy() {
        return ly;
    }

    public void translateAllBy(float dx, float dy) {
        if (!thermometers.isEmpty())
            for (Thermometer t : thermometers)
                t.translateBy(dx, dy);
        if (!anemometers.isEmpty())
            for (Anemometer a : anemometers)
                a.translateBy(dx, dy);
        if (!heatFluxSensors.isEmpty())
            for (HeatFluxSensor h : heatFluxSensors)
                h.translateBy(dx, dy);
        if (!clouds.isEmpty())
            for (Cloud c : clouds)
                c.translateBy(dx, dy);
        if (!trees.isEmpty())
            for (Tree t : trees)
                t.translateBy(dx, dy);
        for (Part p : parts)
            p.translateBy(dx, dy);
        for (Particle p : particles)
            p.translateBy(dx, dy);
        for (ParticleFeeder p : particleFeeders)
            p.translateBy(dx, dy);
        for (Fan f : fans)
            f.translateBy(dx, dy);
        for (Heliostat h : heliostats)
            h.translateBy(dx, dy);
    }

    public boolean scaleAll(float scale) {
        Rectangle2D.Float bound = new Rectangle2D.Float(0, 0, lx, ly);
        boolean out = false;
        if (!thermometers.isEmpty())
            for (Thermometer t : thermometers) {
                t.setCenter(scale * t.getX(), ly - scale * (ly - t.getY()));
                if (!bound.intersects(t.getShape().getBounds2D()))
                    out = true;
            }
        if (!anemometers.isEmpty())
            for (Anemometer a : anemometers) {
                a.setCenter(scale * a.getX(), ly - scale * (ly - a.getY()));
                if (!bound.intersects(a.getShape().getBounds2D()))
                    out = true;
            }
        if (!heatFluxSensors.isEmpty())
            for (HeatFluxSensor h : heatFluxSensors) {
                h.setCenter(scale * h.getX(), ly - scale * (ly - h.getY()));
                if (!bound.intersects(h.getShape().getBounds2D()))
                    out = true;
            }
        if (!clouds.isEmpty())
            for (Cloud c : clouds) {
                c.setLocation(scale * c.getX(), ly - scale * (ly - c.getY()));
                c.setDimension(c.getWidth() * scale, c.getHeight() * scale);
                if (!bound.intersects(c.getShape().getBounds2D()))
                    out = true;
            }
        if (!trees.isEmpty())
            for (Tree t : trees) {
                t.setLocation(scale * t.getX(), ly - scale * (ly - t.getY()));
                t.setDimension(t.getWidth() * scale, t.getHeight() * scale);
                if (!bound.intersects(t.getShape().getBounds2D()))
                    out = true;
            }
        if (!particles.isEmpty())
            for (Particle p : particles) {
                p.setLocation(scale * p.getRx(), ly - scale * (ly - p.getRy()));
                p.setRadius(p.getRadius() * scale);
                if (!bound.intersects(p.getShape().getBounds2D()))
                    out = true;
            }
        if (!particleFeeders.isEmpty())
            for (ParticleFeeder p : particleFeeders) {
                p.setCenter(scale * p.getX(), ly - scale * (ly - p.getY()));
                if (!bound.intersects(p.getShape().getBounds2D()))
                    out = true;
            }
        for (Part p : parts) {
            Shape s = p.getShape();
            if (s instanceof Rectangle2D.Float) {
                Rectangle2D.Float r = (Rectangle2D.Float) s;
                r.x = scale * r.x;
                r.y = ly - scale * (ly - r.y);
                r.width *= scale;
                r.height *= scale;
                if (!bound.intersects(r))
                    out = true;
            } else if (s instanceof Ellipse2D.Float) {
                Ellipse2D.Float e = (Ellipse2D.Float) s;
                e.x = scale * e.x;
                e.y = ly - scale * (ly - e.y);
                e.width *= scale;
                e.height *= scale;
                if (!bound.intersects(e.getBounds2D()))
                    out = true;
            } else if (s instanceof Annulus) {
                Annulus a = (Annulus) s;
                float x = scale * a.getX();
                float y = ly - scale * (ly - a.getY());
                float innerDiameter = a.getInnerDiameter() * scale;
                float outerDiameter = a.getOuterDiameter() * scale;
                a.setShape(x, y, innerDiameter, outerDiameter);
                if (!bound.intersects(a.getBounds2D()))
                    out = true;
            } else if (s instanceof EllipticalAnnulus) {
                EllipticalAnnulus e = (EllipticalAnnulus) s;
                float x = scale * e.getX();
                float y = ly - scale * (ly - e.getY());
                float innerA = e.getInnerA() * scale;
                float innerB = e.getInnerB() * scale;
                float outerA = e.getOuterA() * scale;
                float outerB = e.getOuterB() * scale;
                e.setShape(x, y, innerA, innerB, outerA, outerB);
                if (!bound.intersects(e.getBounds2D()))
                    out = true;
            } else if (s instanceof Polygon2D) {
                Polygon2D g = (Polygon2D) s;
                int n = g.getVertexCount();
                for (int i = 0; i < n; i++) {
                    Point2D.Float h = g.getVertex(i);
                    h.x = scale * h.x;
                    h.y = ly - scale * (ly - h.y);
                }
                if (!bound.intersects(g.getBounds2D()))
                    out = true;
            } else if (s instanceof Blob2D) {
                Blob2D b = (Blob2D) s;
                int n = b.getPointCount();
                for (int i = 0; i < n; i++) {
                    Point2D.Float h = b.getPoint(i);
                    h.x = scale * h.x;
                    h.y = ly - scale * (ly - h.y);
                }
                if (!bound.intersects(b.getBounds2D()))
                    out = true;
            }
        }
        for (Fan f : fans) {
            Shape s = f.getShape();
            if (s instanceof Rectangle2D.Float) {
                Rectangle2D.Float r = (Rectangle2D.Float) s;
                r.x = scale * r.x;
                r.y = ly - scale * (ly - r.y);
                r.width *= scale;
                r.height *= scale;
                if (!bound.intersects(r))
                    out = true;
            }
        }
        for (Heliostat h : heliostats) {
            Shape s = h.getShape();
            if (s instanceof Rectangle2D.Float) {
                Rectangle2D.Float r = (Rectangle2D.Float) s;
                r.x = scale * r.x;
                r.y = ly - scale * (ly - r.y);
                r.width *= scale;
                r.height *= scale;
                if (!bound.intersects(r))
                    out = true;
            }
        }
        return out;
    }

    public ThermalBoundary getThermalBoundary() {
        return heatSolver.getBoundary();
    }

    public void setThermalBoundary(ThermalBoundary b) {
        heatSolver.setBoundary(b);
    }

    public MassBoundary getMassBoundary() {
        return fluidSolver.getBoundary();
    }

    public void setMassBoundary(MassBoundary b) {
        fluidSolver.setBoundary(b);
    }

    public void setBackgroundTemperature(float backgroundTemperature) {
        this.backgroundTemperature = backgroundTemperature;
        heatSolver.backgroundTemperature = backgroundTemperature;
    }

    public float getBackgroundTemperature() {
        return backgroundTemperature;
    }

    public void setBackgroundConductivity(float backgroundConductivity) {
        this.backgroundConductivity = backgroundConductivity;
    }

    public float getBackgroundConductivity() {
        return backgroundConductivity;
    }

    public void setBackgroundSpecificHeat(float backgroundSpecificHeat) {
        this.backgroundSpecificHeat = backgroundSpecificHeat;
    }

    public float getBackgroundSpecificHeat() {
        return backgroundSpecificHeat;
    }

    public void setBackgroundDensity(float backgroundDensity) {
        this.backgroundDensity = backgroundDensity;
    }

    public float getBackgroundDensity() {
        return backgroundDensity;
    }

    /**
     * return the Prandtl Number of the background fluid
     */
    public float getPrandtlNumber() {
        return getBackgroundViscosity() * backgroundDensity * backgroundSpecificHeat / backgroundConductivity;
    }

    // thermostats

    /**
     * only one thermostat is needed for a power source or to connect a thermometer and a power source
     */
    public Thermostat addThermostat(Thermometer t, Part p) {
        Iterator<Thermostat> i = thermostats.iterator();
        synchronized (thermostats) {
            while (i.hasNext()) {
                Thermostat x = i.next();
                if (x.getThermometer() == t && x.getPowerSource() == p)
                    return x;
            }
        }
        Thermostat x = new Thermostat(t, p);
        thermostats.add(x);
        return x;
    }

    public void removeThermostat(Thermometer t, Part p) {
        if (thermostats.isEmpty())
            return;
        synchronized (thermostats) {
            thermostats.removeIf(x -> x.getThermometer() == t && x.getPowerSource() == p);
        }
    }

    public boolean isConnected(Thermometer t, Part p) {
        Iterator<Thermostat> i = thermostats.iterator();
        synchronized (thermostats) {
            while (i.hasNext()) {
                Thermostat x = i.next();
                if (x.getThermometer() == t && x.getPowerSource() == p)
                    return true;
            }
        }
        return false;
    }

    public Thermostat getThermostat(Object o) {
        Iterator<Thermostat> i = thermostats.iterator();
        synchronized (thermostats) {
            while (i.hasNext()) {
                Thermostat x = i.next();
                if (x.getThermometer() == o || x.getPowerSource() == o)
                    return x;
            }
        }
        return null;
    }

    public Thermostat getThermostat(Thermometer t, Part p) {
        Iterator<Thermostat> i = thermostats.iterator();
        synchronized (thermostats) {
            while (i.hasNext()) {
                Thermostat x = i.next();
                if (x.getThermometer() == t && x.getPowerSource() == p)
                    return x;
            }
        }
        return null;
    }

    public List<Thermostat> getThermostats() {
        return thermostats;
    }

    // thermometers

    public void addThermometer(Thermometer t) {
        thermometers.add(t);
    }

    public void addThermometer(Thermometer t, int index) {
        thermometers.add(index, t);
    }

    public void addThermometer(float x, float y) {
        thermometers.add(new Thermometer(x, y));
    }

    public Thermometer addThermometer(float x, float y, String uid, String label, byte stencil) {
        Thermometer t = new Thermometer(x, y);
        t.setUid(uid);
        t.setLabel(label);
        t.setStencil(stencil);
        thermometers.add(t);
        return t;
    }

    public void removeThermometer(Thermometer t) {
        thermometers.remove(t);
        if (!thermostats.isEmpty()) {
            thermostats.removeIf(x -> x.getThermometer() == t);
        }
    }

    public List<Thermometer> getThermometers() {
        return thermometers;
    }

    public Thermometer getThermometer(String uid) {
        if (uid == null)
            return null;
        synchronized (thermometers) {
            for (Thermometer t : thermometers) {
                if (uid.equals(t.getUid()))
                    return t;
            }
        }
        return null;
    }

    public Thermometer getThermometer(int i) {
        if (i < 0 || i >= thermometers.size())
            return null;
        return thermometers.get(i);
    }

    // anemometers

    public void addAnemometer(Anemometer a) {
        anemometers.add(a);
    }

    public void addAnemometer(Anemometer a, int index) {
        anemometers.add(index, a);
    }

    public void addAnemometer(float x, float y) {
        anemometers.add(new Anemometer(x, y));
    }

    public Anemometer addAnemometer(float x, float y, String uid, String label, byte stencil) {
        Anemometer a = new Anemometer(x, y);
        a.setUid(uid);
        a.setLabel(label);
        a.setStencil(stencil);
        anemometers.add(a);
        return a;
    }

    public void removeAnemometer(Anemometer a) {
        anemometers.remove(a);
    }

    public List<Anemometer> getAnemometers() {
        return anemometers;
    }

    public Anemometer getAnemometer(String uid) {
        if (uid == null)
            return null;
        synchronized (anemometers) {
            for (Anemometer a : anemometers) {
                if (uid.equals(a.getUid()))
                    return a;
            }
        }
        return null;
    }

    public Anemometer getAnemometer(int i) {
        if (i < 0 || i >= anemometers.size())
            return null;
        return anemometers.get(i);
    }

    // heat flux sensors

    public void addHeatFluxSensor(HeatFluxSensor h) {
        heatFluxSensors.add(h);
        measure(h);
    }

    public void addHeatFluxSensor(HeatFluxSensor h, int index) {
        heatFluxSensors.add(index, h);
        measure(h);
    }

    public void addHeatFluxSensor(float x, float y) {
        HeatFluxSensor h = new HeatFluxSensor(x, y);
        heatFluxSensors.add(h);
        measure(h);
    }

    public HeatFluxSensor addHeatFluxSensor(float x, float y, String uid, String label, float angle) {
        HeatFluxSensor h = new HeatFluxSensor(x, y);
        h.setUid(uid);
        h.setLabel(label);
        h.setAngle(angle);
        heatFluxSensors.add(h);
        measure(h);
        return h;
    }

    public void removeHeatFluxSensor(HeatFluxSensor h) {
        heatFluxSensors.remove(h);
    }

    public List<HeatFluxSensor> getHeatFluxSensors() {
        return heatFluxSensors;
    }

    public HeatFluxSensor getHeatFluxSensor(String uid) {
        if (uid == null)
            return null;
        synchronized (heatFluxSensors) {
            for (HeatFluxSensor h : heatFluxSensors) {
                if (uid.equals(h.getUid()))
                    return h;
            }
        }
        return null;
    }

    public HeatFluxSensor getHeatFluxSensor(int i) {
        if (i < 0 || i >= heatFluxSensors.size())
            return null;
        return heatFluxSensors.get(i);
    }

    /**
     * Since the sensor data are erased, the index of step (and hence the clock) is also reset.
     */
    public void clearSensorData() {
        indexOfStep = 0;
        if (!thermometers.isEmpty()) {
            synchronized (thermometers) {
                for (Thermometer t : thermometers) {
                    t.clear();
                }
            }
        }
        if (!anemometers.isEmpty()) {
            synchronized (anemometers) {
                for (Anemometer a : anemometers) {
                    a.clear();
                }
            }
        }
        if (!heatFluxSensors.isEmpty()) {
            synchronized (heatFluxSensors) {
                for (HeatFluxSensor h : heatFluxSensors) {
                    h.clear();
                }
            }
        }
    }

    public float[] getSensorDataBounds(byte type) {
        switch (type) {
            case 0:
                if (!thermometers.isEmpty()) {
                    float[] bounds = new float[]{Float.MAX_VALUE, -Float.MAX_VALUE};
                    float min, max;
                    synchronized (thermometers) {
                        for (Thermometer t : thermometers) {
                            min = t.getDataMinimum();
                            if (Float.isNaN(min)) // no data has been collected
                                return null;
                            max = t.getDataMaximum();
                            if (bounds[0] > min)
                                bounds[0] = min;
                            if (bounds[1] < max)
                                bounds[1] = max;
                        }
                    }
                    return bounds;
                }
                break;
            case 1:
                if (!heatFluxSensors.isEmpty()) {
                    float[] bounds = new float[]{Float.MAX_VALUE, -Float.MAX_VALUE};
                    float min, max;
                    synchronized (heatFluxSensors) {
                        for (HeatFluxSensor f : heatFluxSensors) {
                            min = f.getDataMinimum();
                            if (Float.isNaN(min)) // no data has been collected
                                return null;
                            max = f.getDataMaximum();
                            if (bounds[0] > min)
                                bounds[0] = min;
                            if (bounds[1] < max)
                                bounds[1] = max;
                        }
                    }
                    return bounds;
                }
                break;
            case 2:
                if (!anemometers.isEmpty()) {
                    float[] bounds = new float[]{Float.MAX_VALUE, -Float.MAX_VALUE};
                    float min, max;
                    synchronized (anemometers) {
                        for (Anemometer a : anemometers) {
                            min = a.getDataMinimum();
                            if (Float.isNaN(min)) // no data has been collected
                                return null;
                            max = a.getDataMaximum();
                            if (bounds[0] > min)
                                bounds[0] = min;
                            if (bounds[1] < max)
                                bounds[1] = max;
                        }
                    }
                    return bounds;
                }
                break;
        }
        return null; // no sensor
    }

    /**
     * Every manipulable has a UID. To avoid confusion, two objects of different types cannot have the same UID.
     */
    public boolean isUidUsed(String uid) {
        if (uid == null || uid.trim().equals(""))
            throw new IllegalArgumentException("UID cannot be null or an empty string.");
        synchronized (parts) {
            for (Part p : parts) {
                if (uid.equals(p.getUid()))
                    return true;
            }
        }
        synchronized (particles) {
            for (Particle p : particles) {
                if (uid.equals(p.getUid()))
                    return true;
            }
        }
        synchronized (thermometers) {
            for (Thermometer t : thermometers) {
                if (uid.equals(t.getUid()))
                    return true;
            }
        }
        synchronized (anemometers) {
            for (Anemometer a : anemometers) {
                if (uid.equals(a.getUid()))
                    return true;
            }
        }
        synchronized (heatFluxSensors) {
            for (HeatFluxSensor h : heatFluxSensors) {
                if (uid.equals(h.getUid()))
                    return true;
            }
        }
        synchronized (clouds) {
            for (Cloud c : clouds) {
                if (uid.equals(c.getUid()))
                    return true;
            }
        }
        synchronized (trees) {
            for (Tree t : trees) {
                if (uid.equals(t.getUid()))
                    return true;
            }
        }
        synchronized (fans) {
            for (Fan f : fans) {
                if (uid.equals(f.getUid()))
                    return true;
            }
        }
        return false;
    }

    // parts

    public Part addRectangularPart(float x, float y, float w, float h) {
        Part p = new Part(new Rectangle2D.Float(x, y, w, h), this);
        addPart(p);
        return p;
    }

    public Part addRectangularPart(float x, float y, float w, float h, float temperature) {
        Part p = addRectangularPart(x, y, w, h);
        p.setTemperature(temperature);
        return p;
    }

    public Part addEllipticalPart(float x, float y, float a, float b) {
        Part p = new Part(new Ellipse2D.Float(x - 0.5f * a, y - 0.5f * b, a, b), this);
        addPart(p);
        return p;
    }

    public Part addEllipticalPart(float x, float y, float a, float b, float temperature) {
        Part p = addEllipticalPart(x, y, a, b);
        p.setTemperature(temperature);
        return p;
    }

    public Part addAnnulusPart(float x, float y, float innerA, float innerB, float outerA, float outerB) {
        Part p = new Part(new EllipticalAnnulus(x, y, innerA, innerB, outerA, outerB), this);
        addPart(p);
        return p;
    }

    public Part addAnnulusPart(float x, float y, float innerA, float innerB, float outerA, float outerB, float temperature) {
        Part p = addAnnulusPart(x, y, innerA, innerB, outerA, outerB);
        p.setTemperature(temperature);
        return p;
    }

    public Part addRingPart(float x, float y, float inner, float outer) {
        Part p = new Part(new Annulus(x, y, inner, outer), this);
        addPart(p);
        return p;
    }

    public Part addPolygonPart(Polygon2D polygon) {
        Part p = new Part(polygon, this);
        addPart(p);
        return p;
    }

    public Part addPolygonPart(float[] x, float[] y) {
        return addPolygonPart(new Polygon2D(x, y));
    }

    public Part addPolygonPart(float[] x, float[] y, float temperature) {
        Part p = addPolygonPart(x, y);
        p.setTemperature(temperature);
        return p;
    }

    public Part addBlobPart(Blob2D blob) {
        Part p = new Part(blob, this);
        addPart(p);
        return p;
    }

    public Part addBlobPart(float[] x, float[] y) {
        return addBlobPart(new Blob2D(x, y));
    }

    public Part addBlobPart(float[] x, float[] y, float temperature) {
        Part p = addBlobPart(x, y);
        p.setTemperature(temperature);
        return p;
    }

    public List<Part> getParts() {
        return parts;
    }

    public Part getPart(String uid) {
        if (uid == null)
            return null;
        synchronized (parts) {
            for (Part p : parts) {
                if (uid.equals(p.getUid()))
                    return p;
            }
        }
        return null;
    }

    public Part getPart(int i) {
        if (i < 0 || i >= parts.size())
            return null;
        return parts.get(i);
    }

    public int getPartCount() {
        return parts.size();
    }

    public void addPart(Part p) {
        if (!parts.contains(p)) {
            parts.add(p);
            if (p.getPower() != 0)
                hasPartPower = true;
            if (p.getEmissivity() > 0)
                radiative = true;
        }
    }

    public void addPart(Part p, int index) {
        if (!parts.contains(p)) {
            parts.add(index, p);
            if (p.getPower() != 0)
                hasPartPower = true;
            if (p.getEmissivity() > 0)
                radiative = true;
        }
    }

    public void removePart(Part p) {
        parts.remove(p);
        if (!thermostats.isEmpty()) {
            thermostats.removeIf(x -> x.getPowerSource() == p);
        }
        checkPartPower();
        checkPartRadiation();
    }

    // particles

    public List<Particle> getParticles() {
        return particles;
    }

    public Particle getParticle(int i) {
        if (i < 0 || i >= particles.size())
            return null;
        return particles.get(i);
    }

    public Particle getParticle(String uid) {
        if (uid == null)
            return null;
        synchronized (particles) {
            for (Particle p : particles) {
                if (uid.equals(p.getUid()))
                    return p;
            }
        }
        return null;
    }

    public void addParticle(Particle p) {
        if (!particles.contains(p)) {
            particles.add(p);
        }
    }

    public void addParticle(Particle p, int index) {
        if (!particles.contains(p)) {
            particles.add(index, p);
        }
    }

    public void removeParticle(Particle p) {
        particles.remove(p);
    }

    // particle feeder

    public void addParticleFeeder(ParticleFeeder pf) {
        particleFeeders.add(pf);
    }

    public void addParticleFeeder(ParticleFeeder pf, int index) {
        particleFeeders.add(index, pf);
    }

    public void addParticleFeeder(float x, float y) {
        particleFeeders.add(new ParticleFeeder(x, y));
    }

    public void removeParticleFeeder(ParticleFeeder pf) {
        particleFeeders.remove(pf);
    }

    public List<ParticleFeeder> getParticleFeeders() {
        return particleFeeders;
    }

    public ParticleFeeder getParticleFeeder(String uid) {
        if (uid == null)
            return null;
        synchronized (particleFeeders) {
            for (ParticleFeeder pf : particleFeeders) {
                if (uid.equals(pf.getUid()))
                    return pf;
            }
        }
        return null;
    }

    public ParticleFeeder getParticleFeeder(int i) {
        if (i < 0 || i >= particleFeeders.size())
            return null;
        return particleFeeders.get(i);
    }

    // fans

    public void addFan(Fan f) {
        if (f != null && !fans.contains(f)) {
            fans.add(f);
        }
    }

    public void addFan(Fan f, int index) {
        if (f != null && !fans.contains(f)) {
            fans.add(index, f);
        }
    }

    public void removeFan(Fan f) {
        fans.remove(f);
    }

    public List<Fan> getFans() {
        return fans;
    }

    public Fan getFan(int i) {
        if (i < 0 || i >= fans.size())
            return null;
        return fans.get(i);
    }

    public Fan getFan(String uid) {
        if (uid == null)
            return null;
        synchronized (fans) {
            for (Fan f : fans) {
                if (uid.equals(f.getUid()))
                    return f;
            }
        }
        return null;
    }

    // heliostats

    public void refreshHeliostats() {
        if (heliostats.isEmpty())
            return;
        for (Heliostat h : heliostats) {
            h.setAngle();
        }
    }

    public void refreshHeliostatsAimedAt(Part target) {
        if (heliostats.isEmpty())
            return;
        for (Heliostat h : heliostats) {
            if (target == h.getTarget())
                h.setAngle();
        }
    }

    public void addHeliostat(Heliostat h) {
        if (h != null && !heliostats.contains(h)) {
            h.setAngle();
            heliostats.add(h);
        }
    }

    public void addHeliostat(Heliostat h, int index) {
        if (h != null && !heliostats.contains(h)) {
            heliostats.add(index, h);
        }
    }

    public void removeHeliostat(Heliostat h) {
        heliostats.remove(h);
    }

    public List<Heliostat> getHeliostats() {
        return heliostats;
    }

    public Heliostat getHeliostat(int i) {
        if (i < 0 || i >= heliostats.size())
            return null;
        return heliostats.get(i);
    }

    public Heliostat getHeliostat(String uid) {
        if (uid == null)
            return null;
        synchronized (heliostats) {
            for (Heliostat h : heliostats) {
                if (uid.equals(h.getUid()))
                    return h;
            }
        }
        return null;
    }

    // cloud

    public void addCloud(Cloud c) {
        if (c != null && !clouds.contains(c))
            clouds.add(c);
    }

    public void addCloud(Cloud c, int index) {
        if (c != null && !clouds.contains(c))
            clouds.add(index, c);
    }

    public void removeCloud(Cloud c) {
        clouds.remove(c);
    }

    public List<Cloud> getClouds() {
        return clouds;
    }

    // trees

    public void addTree(Tree t) {
        if (t != null && !trees.contains(t))
            trees.add(t);
    }

    public void addTree(Tree t, int index) {
        if (t != null && !trees.contains(t))
            trees.add(index, t);
    }

    public void removeTree(Tree t) {
        trees.remove(t);
    }

    public List<Tree> getTrees() {
        return trees;
    }

    public List<Segment> getPerimeterSegments() {
        return radiositySolver.getSegments();
    }

    List<Segment> getPerimeterSegments(Part part) {
        List<Segment> list = new ArrayList<>();
        for (Segment s : radiositySolver.getSegments()) {
            if (s.getPart() == part)
                list.add(s);
        }
        return list;
    }

    public void generateViewFactorMesh() {
        radiositySolver.segmentizePerimeters();
    }

    public void setPerimeterStepSize(float size) {
        radiositySolver.setPatchSizePercentage(size);
    }

    public float getPerimeterStepSize() {
        return radiositySolver.getPatchSizePercentage();
    }

    public boolean isVisible(Segment s1, Segment s2) {
        return radiositySolver.isVisible(s1, s2);
    }

    public float getMaximumHeatCapacity() {
        return maximumHeatCapacity;
    }

    /**
     * the part on the top sets the properties of a cell
     */
    public void refreshMaterialPropertyArrays() {
        float x, y, windSpeed;
        boolean initial = indexOfStep == 0;
        maximumHeatCapacity = backgroundDensity * backgroundSpecificHeat;
        float heatCapacity = 0;
        for (int i = 0; i < nx; i++) {
            x = i * deltaX;
            for (int j = 0; j < ny; j++) {
                y = j * deltaY;
                conductivity[i][j] = backgroundConductivity;
                specificHeat[i][j] = backgroundSpecificHeat;
                density[i][j] = backgroundDensity;
                fluidity[i][j] = true;
                uWind[i][j] = vWind[i][j] = 0;
                synchronized (parts) {
                    ListIterator<Part> li = parts.listIterator(parts.size());
                    while (li.hasPrevious()) {
                        Part p = li.previous();
                        // FIXME: for some reason, the fluid solver doesn't like the way we treat round-off error on the grid.
                        // So if the model is convective, revert to using the original contains(...) method for setting the properties on the grid.
                        if (contains(p.getShape(), x, y, convective)) {
                            conductivity[i][j] = p.getThermalConductivity();
                            specificHeat[i][j] = p.getSpecificHeat();
                            density[i][j] = p.getDensity();
                            fluidity[i][j] = false;
                            break;
                        }
                    }
                }
                // The rest properties of parts should use the new contains(...) method, as is in any other place
                synchronized (parts) {
                    ListIterator<Part> li = parts.listIterator(parts.size());
                    while (li.hasPrevious()) {
                        Part p = li.previous();
                        if (contains(p.getShape(), x, y, false)) {
                            if (!initial && p.getConstantTemperature())
                                t[i][j] = p.getTemperature();
                            if ((windSpeed = p.getWindSpeed()) != 0) { // parts used to support wind speed, we now prefer using fans
                                uWind[i][j] = (float) (windSpeed * Math.cos(p.getWindAngle()));
                                vWind[i][j] = (float) (windSpeed * Math.sin(p.getWindAngle()));
                            }
                            break;
                        }
                    }
                }
                synchronized (fans) {
                    for (Fan f : fans) {
                        if (contains(f.getShape(), x, y, false)) {
                            if ((windSpeed = f.getSpeed()) != 0) {
                                uWind[i][j] = (float) (windSpeed * Math.cos(f.getAngle()));
                                vWind[i][j] = (float) (windSpeed * Math.sin(f.getAngle()));
                            }
                            break;
                        }
                    }
                }
                heatCapacity = specificHeat[i][j] * density[i][j];
                if (maximumHeatCapacity < heatCapacity)
                    maximumHeatCapacity = heatCapacity;
            }
        }
        if (initial) {
            setInitialTemperature();
            setInitialVelocity();
        }
    }

    public void refreshPowerArray() {
        checkPartPower();
        float x, y, power;
        int count;
        for (int i = 0; i < nx; i++) {
            x = i * deltaX;
            for (int j = 0; j < ny; j++) {
                y = j * deltaY;
                q[i][j] = 0;
                if (hasPartPower) {
                    count = 0;
                    synchronized (parts) {
                        for (Part p : parts) {
                            if (p.getPower() != 0 && p.getPowerSwitch() && contains(p.getShape(), x, y, false)) {
                                power = p.getPower();
                                if (p.getThermistorTemperatureCoefficient() != 0) {
                                    power *= 1f + p.getThermistorTemperatureCoefficient() * (t[i][j] - p.getThermistorReferenceTemperature());
                                }
                                q[i][j] += power;
                                count++;
                            }
                        }
                    }
                    if (count > 0)
                        q[i][j] /= count;
                }
            }
        }
    }

    public void refreshTemperatureBoundaryArray() {
        float x, y;
        int count;
        for (int i = 0; i < nx; i++) {
            x = i * deltaX;
            for (int j = 0; j < ny; j++) {
                y = j * deltaY;
                tb[i][j] = 0;
                count = 0;
                synchronized (parts) {
                    for (Part p : parts) {
                        if (p.getConstantTemperature() && contains(p.getShape(), x, y, false)) {
                            tb[i][j] += p.getTemperature();
                            count++;
                        }
                    }
                }
                if (count > 0) {
                    tb[i][j] /= count;
                } else {
                    tb[i][j] = Float.NaN;
                }
            }
        }
    }

    // avoid round-off error in detecting if a point falls within a shape
    private boolean contains(Shape shape, float x, float y, boolean tolerateRoundOffError) {
        if (tolerateRoundOffError)
            return shape.contains(x, y);
        float tol = 0.001f;
        return shape.contains(x, y) || shape.contains(x - deltaX * tol, y) || shape.contains(x + deltaX * tol, y) || shape.contains(x, y - deltaY * tol) || shape.contains(x, y + deltaY * tol);
    }

    /**
     * get the total thermal energy of the system
     */
    public float getThermalEnergy() {
        float energy = 0;
        for (int i = 1; i < nx - 1; i++) { // excluding the border cells to ensure the conservation of energy
            for (int j = 1; j < ny - 1; j++) {
                energy += t[i][j] * density[i][j] * specificHeat[i][j];
            }
        }
        return energy * deltaX * deltaY;
    }

    /**
     * get the total thermal energy stored in this part
     */
    float getThermalEnergy(Part p) {
        if (p == null)
            return 0;
        float x, y;
        float energy = 0;
        for (int i = 0; i < nx; i++) {
            x = i * deltaX;
            for (int j = 0; j < ny; j++) {
                y = j * deltaY;
                if (contains(p.getShape(), x, y, false)) { // no overlap of parts will be allowed
                    energy += t[i][j] * density[i][j] * specificHeat[i][j];
                }
            }
        }
        return energy * deltaX * deltaY;
    }

    /**
     * get the thermal energy stored in the cell at the given point. If the point is out of bound, return -1 (any impossible value to indicate error)
     */
    public float getThermalEnergyAt(float x, float y) {
        int i = Math.round(x / deltaX);
        if (i < 0 || i >= nx)
            return -1;
        int j = Math.round(y / deltaY);
        if (j < 0 || j >= ny)
            return -1;
        return t[i][j] * density[i][j] * specificHeat[i][j] * deltaX * deltaY;
    }

    private void init() {
        for (int i = 0; i < nx; i++) {
            Arrays.fill(conductivity[i], backgroundConductivity);
            Arrays.fill(specificHeat[i], backgroundSpecificHeat);
            Arrays.fill(density[i], backgroundDensity);
        }
        setInitialTemperature();
    }

    public void clear() {
        parts.clear();
        particles.clear();
        particleFeeders.clear();
        photons.clear();
        anemometers.clear();
        thermometers.clear();
        heatFluxSensors.clear();
        thermostats.clear();
        clouds.clear();
        trees.clear();
        fans.clear();
        heliostats.clear();
        maximumHeatCapacity = -1;
    }

    public void lockAll(boolean b) {
        for (Part x : parts)
            x.setDraggable(!b);
        for (Particle x : particles)
            x.setDraggable(!b);
        for (ParticleFeeder x : particleFeeders)
            x.setDraggable(!b);
        for (Fan x : fans)
            x.setDraggable(!b);
        for (Heliostat x : heliostats)
            x.setDraggable(!b);
    }

    public void removeAllParticles() {
        particles.clear();
    }

    private void setInitialVelocity() {
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                if (fluidity[i][j]) {
                    u[i][j] = v[i][j] = 0;
                } else {
                    u[i][j] = uWind[i][j];
                    v[i][j] = vWind[i][j];
                }
            }
        }
    }

    public void setInitialTemperature() {
        if (parts == null || parts.isEmpty()) {
            for (int i = 0; i < nx; i++) {
                for (int j = 0; j < ny; j++) {
                    t[i][j] = backgroundTemperature;
                }
            }
        } else {
            float x, y;
            int count;
            for (int i = 0; i < nx; i++) {
                x = i * deltaX;
                for (int j = 0; j < ny; j++) {
                    y = j * deltaY;
                    count = 0;
                    t[i][j] = 0;
                    synchronized (parts) {
                        for (Part p : parts) { // a cell gets the average temperature from the overlapping parts
                            if (contains(p.getShape(), x, y, false)) {
                                count++;
                                t[i][j] += p.getTemperature();
                            }
                        }
                    }
                    if (count > 0) {
                        t[i][j] /= count;
                    } else {
                        t[i][j] = backgroundTemperature;
                    }
                }
            }
        }
        clearSensorData();
    }

    public void run() {
        checkPartPower();
        checkPartRadiation();
        refreshPowerArray();
        if (!running) {
            running = true;
            while (running) {
                nextStep();
                if (fatalErrorOccurred()) {
                    notifyManipulationListeners(ManipulationEvent.STOP);
                    notifyManipulationListeners(ManipulationEvent.FATAL_ERROR_OCCURRED);
                    break;
                }
                if (tasks != null)
                    tasks.run();
            }
            if (notifyReset) {
                indexOfStep = 0;
                reallyReset();
                notifyReset = false;
                // call view.repaint() to get rid of the residual pixels that are still calculated in nextStep()
                notifyManipulationListeners(ManipulationEvent.REPAINT);
            }
        }
    }

    public boolean fatalErrorOccurred() {
        return Float.isNaN(t[nx / 2][ny / 2]);
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public void reset() {
        if (running) {
            stop();
            notifyReset = true;
        } else {
            reallyReset();
        }
        running = false;
        indexOfStep = 0;
    }

    private void reallyReset() {
        setInitialTemperature();
        setInitialVelocity();
        synchronized (parts) {
            for (Part p : parts)
                p.setPowerSwitch(true);
        }
        if (!anemometers.isEmpty()) {
            synchronized (anemometers) {
                for (Anemometer a : anemometers)
                    a.setAngle(0);
            }
        }
        photons.clear();
        heatSolver.reset();
        fluidSolver.reset();
        particleSolver.reset();
        radiositySolver.reset();
        attachSensors();
        if (!heatFluxSensors.isEmpty()) {
            synchronized (heatFluxSensors) {
                for (HeatFluxSensor f : heatFluxSensors) {
                    measure(f);
                }
            }
        }
    }

    private void checkPartPower() {
        hasPartPower = false;
        synchronized (parts) {
            for (Part p : parts) {
                if (p.getPower() != 0) {
                    hasPartPower = true;
                    break;
                }
            }
        }
    }

    public boolean isRadiative() {
        checkPartRadiation();
        return radiative;
    }

    private void checkPartRadiation() {
        radiative = false;
        synchronized (parts) {
            for (Part p : parts) {
                if (p.getEmissivity() > 0) {
                    radiative = true;
                    break;
                }
            }
        }
    }

    private void nextStep() {

        // photon simulation of solar inputs
        if (sunny) {
            if (indexOfStep % photonEmissionInterval == 0) {
                photonSolver.sunShine(photons, parts);
                refreshPowerArray();
            }
        }
        photonSolver.solve(this);

        // radiation solver
        if (radiative) {
            if (indexOfStep % radiosityInterval == 0) {
                refreshPowerArray();
                radiositySolver.solve();
            }
        }

        // convection solver
        if (convective) {
            fluidSolver.solve(u, v);
            if (!fans.isEmpty())
                applyFans();
        }

        // conduction solver
        heatSolver.solve(convective, t);

        // particle solver
        if (!particles.isEmpty())
            particleSolver.move(this);
        if (!particleFeeders.isEmpty()) {
            synchronized (particleFeeders) {
                for (ParticleFeeder pf : particleFeeders) {
                    if (indexOfStep % Math.round(pf.getPeriod() / getTimeStep()) == 0) {
                        pf.feed(this);
                    }
                }
            }
        }

        // other animations
        if (!clouds.isEmpty()) {
            synchronized (clouds) {
                for (Cloud c : clouds)
                    c.move(heatSolver.getTimeStep(), lx);
            }
        }

        indexOfStep++;

    }

    private void applyFans() {
        float x, y;
        synchronized (fans) {
            for (int i = 0; i < nx; i++) {
                x = i * deltaX;
                for (int j = 0; j < ny; j++) {
                    y = j * deltaY;
                    for (Fan f : fans) {
                        if (contains(f.getShape(), x, y, false)) {
                            if (f.getSpeed() != 0) {
                                u[i][j] = uWind[i][j];
                                v[i][j] = vWind[i][j];
                            }
                        }
                    }
                }
            }
        }
    }

    public float getTime() {
        return indexOfStep * heatSolver.getTimeStep();
    }

    public int getIndexOfStep() {
        return indexOfStep;
    }

    public void setTimeStep(float timeStep) {
        notifyPropertyChangeListeners("Time step", getTimeStep(), timeStep);
        heatSolver.setTimeStep(timeStep);
        fluidSolver.setTimeStep(timeStep);
    }

    public float getTimeStep() {
        return heatSolver.getTimeStep();
    }

    void changePowerAt(float x, float y, float increment) {
        int i = Math.min(t.length - 1, Math.round(x / deltaX));
        if (i < 0)
            i = 0;
        int j = Math.min(t[0].length - 1, Math.round(y / deltaY));
        if (j < 0)
            j = 0;
        q[i][j] += increment;
    }

    public void setTemperature(float[][] t) {
        this.t = t;
    }

    public float getTemperatureAt(float x, float y) {
        int i = Math.min(t.length - 1, Math.round(x / deltaX));
        if (i < 0)
            i = 0;
        int j = Math.min(t[0].length - 1, Math.round(y / deltaY));
        if (j < 0)
            j = 0;
        return t[i][j];
    }

    public float getTemperatureAt(float x, float y, byte stencil) {
        int i = Math.min(t.length - 1, Math.round(x / deltaX));
        if (i < 0)
            i = 0;
        int j = Math.min(t[0].length - 1, Math.round(y / deltaY));
        if (j < 0)
            j = 0;
        return getTemperature(i, j, stencil);
    }

    public float getTemperature(int i, int j, byte stencil) {
        if (i < 0)
            i = 0;
        else if (i > nx - 1)
            i = nx - 1;
        if (j < 0)
            j = 0;
        else if (j > ny - 1)
            j = ny - 1;
        switch (stencil) {
            case Sensor.FIVE_POINT:
                float temp = t[i][j];
                int count = 1;
                if (i > 0) {
                    temp += t[i - 1][j];
                    count++;
                }
                if (i < nx - 1) {
                    temp += t[i + 1][j];
                    count++;
                }
                if (j > 0) {
                    temp += t[i][j - 1];
                    count++;
                }
                if (j < ny - 1) {
                    temp += t[i][j + 1];
                    count++;
                }
                return temp / count;
            case Sensor.NINE_POINT:
                temp = t[i][j];
                count = 1;
                if (i > 0) {
                    temp += t[i - 1][j];
                    count++;
                }
                if (i < nx - 1) {
                    temp += t[i + 1][j];
                    count++;
                }
                if (j > 0) {
                    temp += t[i][j - 1];
                    count++;
                }
                if (j < ny - 1) {
                    temp += t[i][j + 1];
                    count++;
                }
                if (i > 0 && j > 0) {
                    temp += t[i - 1][j - 1];
                    count++;
                }
                if (i > 0 && j < ny - 1) {
                    temp += t[i - 1][j + 1];
                    count++;
                }
                if (i < nx - 1 && j > 0) {
                    temp += t[i + 1][j - 1];
                    count++;
                }
                if (i < nx - 1 && j < ny - 1) {
                    temp += t[i + 1][j + 1];
                    count++;
                }
                return temp / count;
            default:
                return t[i][j];
        }
    }

    public void setTemperatureAt(float x, float y, float temperature) {
        int i = Math.min(t.length - 1, Math.round(x / deltaX));
        if (i < 0)
            return;
        int j = Math.min(t[0].length - 1, Math.round(y / deltaY));
        if (j < 0)
            return;
        t[i][j] = temperature;
    }

    public void changeTemperatureAt(float x, float y, float increment) {
        int i = Math.min(t.length - 1, Math.round(x / deltaX));
        if (i < 0)
            return;
        int j = Math.min(t[0].length - 1, Math.round(y / deltaY));
        if (j < 0)
            return;
        t[i][j] += increment;
    }

    float getAverageTemperatureAt(float x, float y) {
        float temp = 0;
        int i0 = Math.round(x / deltaX);
        int j0 = Math.round(y / deltaY);
        int i = Math.min(t.length - 1, i0);
        int j = Math.min(t[0].length - 1, j0);
        if (i < 0)
            i = 0;
        if (j < 0)
            j = 0;
        temp += t[i][j];
        i = Math.min(t.length - 1, i0 + 1);
        j = Math.min(t[0].length - 1, j0);
        if (i < 0)
            i = 0;
        if (j < 0)
            j = 0;
        temp += t[i][j];
        i = Math.min(t.length - 1, i0 - 1);
        j = Math.min(t[0].length - 1, j0);
        if (i < 0)
            i = 0;
        if (j < 0)
            j = 0;
        temp += t[i][j];
        i = Math.min(t.length - 1, i0);
        j = Math.min(t[0].length - 1, j0 + 1);
        if (i < 0)
            i = 0;
        if (j < 0)
            j = 0;
        temp += t[i][j];
        i = Math.min(t.length - 1, i0);
        j = Math.min(t[0].length - 1, j0 - 1);
        if (i < 0)
            i = 0;
        if (j < 0)
            j = 0;
        temp += t[i][j];
        return temp * 0.2f;
    }

    void changeAverageTemperatureAt(float x, float y, float increment) {
        increment *= 0.2f;
        int i0 = Math.round(x / deltaX);
        int j0 = Math.round(y / deltaY);
        int i = Math.min(t.length - 1, i0);
        int j = Math.min(t[0].length - 1, j0);
        if (i >= 0 && j >= 0)
            t[i][j] += increment;
        i = Math.min(t.length - 1, i0 + 1);
        j = Math.min(t[0].length - 1, j0);
        if (i >= 0 && j >= 0)
            t[i][j] += increment;
        i = Math.min(t.length - 1, i0 - 1);
        j = Math.min(t[0].length - 1, j0);
        if (i >= 0 && j >= 0)
            t[i][j] += increment;
        i = Math.min(t.length - 1, i0);
        j = Math.min(t[0].length - 1, j0 + 1);
        if (i >= 0 && j >= 0)
            t[i][j] += increment;
        i = Math.min(t.length - 1, i0);
        j = Math.min(t[0].length - 1, j0 - 1);
        if (i >= 0 && j >= 0)
            t[i][j] += increment;
    }

    public float[][] getTemperature() {
        return t;
    }

    private float[] getHeatFlux(int i, int j) {
        if (i < 1)
            i = 1;
        else if (i > nx - 2)
            i = nx - 2;
        if (j < 1)
            j = 1;
        else if (j > ny - 2)
            j = ny - 2;
        float fx = -conductivity[i][j] * (t[i + 1][j] - t[i - 1][j]) / (2 * deltaX);
        float fy = -conductivity[i][j] * (t[i][j + 1] - t[i][j - 1]) / (2 * deltaY);
        return new float[]{fx, fy};
    }

    public float[] getHeatFluxAt(float x, float y) {
        return getHeatFlux(Math.round(x / deltaX), Math.round(y / deltaY));
    }

    public float[][] getXVelocity() {
        return u;
    }

    public float[][] getYVelocity() {
        return v;
    }

    public float[] getVelocityAt(float x, float y) {
        int i = Math.min(t.length - 1, Math.round(x / deltaX));
        if (i < 0)
            i = 0;
        int j = Math.min(t[0].length - 1, Math.round(y / deltaY));
        if (j < 0)
            j = 0;
        return new float[]{u[i][j], v[i][j]};
    }

    public float getVorticityAt(float x, float y) {
        return getVorticity(Math.round(x / deltaX), Math.round(y / deltaY));
    }

    private float getVorticity(int i, int j) {
        if (i < 1)
            i = 1;
        else if (i > nx - 2)
            i = nx - 2;
        if (j < 1)
            j = 1;
        else if (j > ny - 2)
            j = ny - 2;
        if (!fluidity[i][j])
            return 0;
        float du_dy = (u[i][j + 1] - u[i][j - 1]) / deltaY;
        float dv_dx = (v[i + 1][j] - v[i - 1][j]) / deltaX;
        return 0.5f * (du_dy - dv_dx);
    }

    public float getVorticity(int i, int j, byte stencil) {
        switch (stencil) {
            case Sensor.FIVE_POINT:
                float vor = getVorticity(i, j);
                vor += getVorticity(i - 1, j);
                vor += getVorticity(i + 1, j);
                vor += getVorticity(i, j - 1);
                vor += getVorticity(i, j + 1);
                return vor / 5;
            case Sensor.NINE_POINT:
                vor = getVorticity(i, j);
                vor += getVorticity(i - 1, j);
                vor += getVorticity(i + 1, j);
                vor += getVorticity(i, j - 1);
                vor += getVorticity(i, j + 1);
                vor += getVorticity(i - 1, j - 1);
                vor += getVorticity(i - 1, j + 1);
                vor += getVorticity(i + 1, j - 1);
                vor += getVorticity(i + 1, j + 1);
                return vor / 9;
            default:
                return getVorticity(i, j);
        }
    }

    public float[][] getStreamFunction() {
        return fluidSolver.getStreamFunction(u, v);
    }

    public float[][] getSpecificHeat() {
        return specificHeat;
    }

    public float[][] getDensity() {
        return density;
    }

    public float[][] getConductivity() {
        return conductivity;
    }

    public boolean hasSensor() {
        return !thermometers.isEmpty() || !heatFluxSensors.isEmpty() || !anemometers.isEmpty();
    }

    public void attachSensors() {
        if (!thermometers.isEmpty()) {
            synchronized (thermometers) {
                for (Thermometer m : thermometers) {
                    if (m.getAttachID() != null) {
                        Particle host = getParticle(m.getAttachID());
                        if (host != null) {
                            m.setX(host.rx);
                            m.setY(host.ry - m.getSensingSpotY());
                        }
                    }
                }
            }
        }
        if (!heatFluxSensors.isEmpty()) {
            synchronized (heatFluxSensors) {
                for (HeatFluxSensor f : heatFluxSensors) {
                    if (f.getAttachID() != null) {
                        Particle host = getParticle(f.getAttachID());
                        if (host != null) {
                            f.setX(host.rx);
                            f.setY(host.ry);
                            f.setAngle(host.getTheta());
                        }
                    }
                }
            }
        }
        if (!anemometers.isEmpty()) {
            synchronized (anemometers) {
                for (Anemometer a : anemometers) {
                    if (a.getAttachID() != null) {
                        Particle host = getParticle(a.getAttachID());
                        if (host != null) {
                            a.setX(host.rx);
                            a.setY(host.ry);
                        }
                    }
                }
            }
        }
    }

    public void takeMeasurement() {
        attachSensors();
        if (!thermometers.isEmpty()) {
            int i, j;
            int offset = Math.round(thermometers.get(0).getSensingSpotY() / ly * ny);
            synchronized (thermometers) {
                for (Thermometer m : thermometers) {
                    i = Math.round(m.getX() / deltaX);
                    j = Math.round(m.getY() / deltaY);
                    if (i >= 0 && i < nx && j >= 0 && j < ny) {
                        m.addData(getTime(), getTemperature(i, j + offset, m.getStencil()));
                    }
                }
            }
        }
        if (!heatFluxSensors.isEmpty()) {
            synchronized (heatFluxSensors) {
                for (HeatFluxSensor f : heatFluxSensors) {
                    f.addData(getTime(), measure(f));
                }
            }
        }
        if (!anemometers.isEmpty()) {
            int i, j;
            synchronized (anemometers) {
                for (Anemometer a : anemometers) {
                    i = Math.round(a.getX() / deltaX);
                    j = Math.round(a.getY() / deltaY);
                    if (i >= 0 && i < nx && j >= 0 && j < ny) {
                        a.addData(getTime(), (float) Math.hypot(u[i][j], v[i][j]));
                    }
                }
            }
        }
    }

    public float measure(HeatFluxSensor f) {
        float flux = 0;
        int i = Math.round(f.getX() / deltaX);
        int j = Math.round(f.getY() / deltaY);
        if (i >= 0 && i < nx && j >= 0 && j < ny) {
            float[] h = getHeatFlux(i, j);
            flux = (float) (h[0] * Math.sin(f.getAngle()) + h[1] * Math.cos(f.getAngle()));
            if (radiative)
                flux += radiositySolver.measure(f);
        }
        f.setValue(flux);
        return flux;
    }

    // if controllers run every step, they could slow down significantly
    public void control() {
        boolean refresh = false;
        for (Thermostat x : thermostats) {
            if (x.onoff(this))
                refresh = true;
        }
        for (Part p : parts) {
            if (p.getThermistorTemperatureCoefficient() != 0) {
                refresh = true;
                break;
            }
        }
        if (refresh)
            refreshPowerArray();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (!propertyChangeListeners.contains(listener))
            propertyChangeListeners.add(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listener != null)
            propertyChangeListeners.remove(listener);
    }

    private void notifyPropertyChangeListeners(String propertyName, Object oldValue, Object newValue) {
        if (propertyChangeListeners.isEmpty())
            return;
        PropertyChangeEvent e = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
        for (PropertyChangeListener x : propertyChangeListeners)
            x.propertyChange(e);
    }

    public void addManipulationListener(ManipulationListener listener) {
        if (!manipulationListeners.contains(listener))
            manipulationListeners.add(listener);
    }

    public void removeManipulationListener(ManipulationListener listener) {
        if (listener != null)
            manipulationListeners.remove(listener);
    }

    private void notifyManipulationListeners(byte type) {
        if (manipulationListeners.isEmpty())
            return;
        ManipulationEvent e = new ManipulationEvent(this, type);
        for (ManipulationListener x : manipulationListeners)
            x.manipulationOccured(e);
    }

}
