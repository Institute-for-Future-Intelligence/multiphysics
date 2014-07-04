package org.concord.energy2d.model;

import java.util.Iterator;
import java.util.List;

/**
 * This implements the discrete phase modeling (http://www.bakker.org/dartmouth06/engs150/15-dpm.pdf) which couples Lagrangian flow with Eulerian flow.
 * 
 * @author Charles Xie
 * 
 */
class ParticleSolver2D {

	private final static float INTERNAL_GRAVITY_UNIT = 0.0001f;

	float epsilon = 0.000001f;
	float rCutOffSquare = 2f;
	float g = 9.8f;
	float drag = 0.01f;
	float thermophoreticCoefficient = 0f;
	private float particleFluidTransfer = 0.05f; // temporary parameter
	private float attractive = 0f;

	private float timeStep = 0.1f;
	private boolean convective;
	private List<Particle> particles;
	private float[][] u, v, t;
	private List<Part> parts;
	private float lx, ly;
	private int nx, ny;

	private float sigma;
	private float fxi, fyi;
	private float rxij, ryij, rijsq;
	private float sr2, sr6, sr12, fij;
	private float fxij, fyij;

	public ParticleSolver2D(Model2D model) {
		particles = model.getParticles();
		parts = model.getParts();
		u = model.getXVelocity();
		v = model.getYVelocity();
		t = model.getTemperature();
		nx = model.getNx();
		ny = model.getNy();
	}

	void move(Model2D model) {
		lx = model.getLx();
		ly = model.getLy();
		timeStep = model.getTimeStep();
		convective = model.isConvective();
		float fluidDensity = model.getBackgroundDensity();
		float fluidConductivity = model.getBackgroundConductivity();
		synchronized (particles) {
			for (Iterator<Particle> it = particles.iterator(); it.hasNext();) {
				Particle p = it.next();
				if (interactWithBoundary(p, model.getMassBoundary()))
					it.remove();
			}
			for (Particle p : particles) {
				p.fx = p.fy = 0.0f;
				p.predict(timeStep);
				interactWithFluid(p, fluidDensity);
				interactWithParts(p);
			}
			computeParticleCollisions();
			for (Particle p : particles) {
				p.correct(timeStep);
				p.fx /= p.mass;
				p.fy /= p.mass;
				interactWithParts(p);
				if (!Float.isNaN(p.temperature)) {
					float txy = particleFluidTransfer * fluidConductivity * (p.temperature - model.getTemperatureAt(p.rx, p.ry));
					int n = Math.max(1, (int) (8 * nx * p.radius / lx)); // discretize contact surface into n slices
					for (int i = 0; i < n; i++) {
						float theta = 2 * (float) Math.PI / n * i;
						model.changeTemperatureAt((float) (p.rx + p.radius * Math.cos(theta)), (float) (p.ry + p.radius * Math.sin(theta)), txy);
					}
				}
			}
		}
	}

	private void interactWithFluid(Particle p, float fluidDensity) {
		float volume = (float) Math.PI * p.radius * p.radius;
		float buoyantForce = INTERNAL_GRAVITY_UNIT * g * (p.mass - fluidDensity * volume);
		if (convective) {
			int i = (int) (p.rx / lx * nx);
			int j = (int) (p.ry / ly * ny);
			if (i < 0)
				i = 0;
			else if (i >= nx)
				i = nx - 1;
			if (j < 0)
				j = 0;
			else if (j >= ny)
				j = ny - 1;
			p.fx += drag * (u[i][j] - p.vx);
			p.fy += drag * (v[i][j] - p.vy) + buoyantForce;
			// Newton's Third Law: Add the buoyant force back to the fluid
			v[i][j] -= buoyantForce * timeStep;
		} else {
			p.fx += -drag * p.vx;
			p.fy += -drag * p.vy + buoyantForce;
		}
		if (thermophoreticCoefficient > 0) {
			int i = (int) (p.rx / lx * nx);
			int j = (int) (p.ry / ly * ny);
			if (i < 0)
				i = 0;
			else if (i >= nx)
				i = nx - 1;
			if (j < 0)
				j = 0;
			else if (j >= ny)
				j = ny - 1;
			if (Math.abs(t[i][j]) > 0.1f) {
				int i1 = (int) ((p.rx - p.radius) / lx * nx);
				int i2 = (int) ((p.rx + p.radius) / lx * nx);
				int j1 = (int) ((p.ry - p.radius) / ly * ny);
				int j2 = (int) ((p.ry + p.radius) / ly * ny);
				if (i1 < 0)
					i1 = 0;
				else if (i1 >= nx)
					i1 = nx - 1;
				if (i2 < 0)
					i2 = 0;
				else if (i2 >= nx)
					i2 = nx - 1;
				if (j1 < 0)
					j1 = 0;
				else if (j1 >= ny)
					j1 = ny - 1;
				if (j2 < 0)
					j2 = 0;
				else if (j2 >= ny)
					j2 = ny - 1;
				p.fx -= thermophoreticCoefficient / p.mass * (t[i2][j] - t[i1][j]) / t[i][j];
				p.fy -= thermophoreticCoefficient / p.mass * (t[i][j2] - t[i][j1]) / t[i][j];
			}
		}
	}

	private void interactWithParts(Particle p) {
		synchronized (parts) {
			for (Part part : parts) {
				if (part.reflect(p, false))
					break;
			}
		}
	}

	private boolean interactWithBoundary(Particle p, MassBoundary boundary) {
		float dt2 = timeStep * timeStep * 0.5f;
		float predictedX = p.rx + p.vx * timeStep + p.ax * dt2;
		float predictedY = p.ry + p.vy * timeStep + p.ay * dt2;
		if (boundary instanceof SimpleMassBoundary) {
			SimpleMassBoundary b = (SimpleMassBoundary) boundary;
			if (predictedX + p.radius > lx) {
				byte flowType = b.getFlowTypeAtBorder(Boundary.RIGHT);
				if (flowType == MassBoundary.REFLECTIVE)
					p.vx = -Math.abs(p.vx);
				else if (flowType == MassBoundary.THROUGH)
					return true;
			} else if (predictedX - p.radius < 0) {
				byte flowType = b.getFlowTypeAtBorder(Boundary.LEFT);
				if (flowType == MassBoundary.REFLECTIVE)
					p.vx = Math.abs(p.vx);
				else if (flowType == MassBoundary.THROUGH)
					return true;
			}
			if (predictedY + p.radius > ly) {
				byte flowType = b.getFlowTypeAtBorder(Boundary.LOWER);
				if (flowType == MassBoundary.REFLECTIVE)
					p.vy = -Math.abs(p.vy);
				else if (flowType == MassBoundary.THROUGH)
					return true;
			} else if (predictedY - p.radius < 0) {
				byte flowType = b.getFlowTypeAtBorder(Boundary.UPPER);
				if (flowType == MassBoundary.REFLECTIVE)
					p.vy = Math.abs(p.vy);
				else if (flowType == MassBoundary.THROUGH)
					return true;
			}
		}
		return false;
	}

	// use Lennard-Jones potential to implement interactions of round particles (by default, a short cutoff is used to turn off the attraction)
	private void computeParticleCollisions() {

		int n = particles.size();
		if (n <= 0)
			return;

		for (int i = 0; i < n - 1; i++) {

			Particle pi = particles.get(i);
			fxi = pi.fx;
			fyi = pi.fy;

			for (int j = i + 1; j < n; j++) {

				Particle pj = particles.get(j);
				rxij = pi.rx - pj.rx;
				ryij = pi.ry - pj.ry;
				rijsq = rxij * rxij + ryij * ryij;

				if (rijsq < rCutOffSquare * 4.0f * pi.radius * pj.radius) {
					sigma = pi.radius + pj.radius;
					sigma *= sigma;
					sr2 = sigma / rijsq;
					/* check if this pair gets too close */
					if (sr2 > 5.0f) {
						sr2 = 5.0f;
						rijsq = sigma * sigma;
					}
					sr6 = sr2 * sr2 * sr2;
					sr12 = sr6 * sr6;
					fij = 6f * epsilon / rijsq * (2f * sr12 - attractive * sr6);
					fxij = fij * rxij;
					fyij = fij * ryij;
					fxi += fxij;
					fyi += fyij;
					pj.fx -= fxij;
					pj.fy -= fyij;
				}

			}

			pi.fx = fxi;
			pi.fy = fyi;

		}

	}

}
