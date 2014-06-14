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

	public final static byte REFLECTIVE = 0;
	public final static byte OPEN = 1;

	private float epsilon = 0.001f;
	private float rCutOffSquare = 1.0f;
	private float g = 0.001f;
	private float timeStep = 0.1f;
	private float drag = 0.01f;
	private byte boundary = REFLECTIVE;

	private List<Particle> particles;
	private float[][] u, v;
	private List<Part> parts;
	private float lx, ly;
	private int nx, ny;

	private float sigmaij;
	private float fxi, fyi;
	private float rxij, ryij, rijsq;
	private float sr2, sr6, sr12, vij, wij, fij;
	private float fxij, fyij;
	private float sigab;

	public ParticleSolver2D(Model2D model) {
		particles = model.getParticles();
		parts = model.getParts();
		u = model.getXVelocity();
		v = model.getYVelocity();
		nx = model.getNx();
		ny = model.getNy();
	}

	void move(Model2D model) {
		lx = model.getLx();
		ly = model.getLy();
		timeStep = model.getTimeStep();
		synchronized (particles) {
			for (Iterator<Particle> it = particles.iterator(); it.hasNext();) {
				Particle p = it.next();
				if (interactWithBoundary(p))
					it.remove();
			}
			for (Particle p : particles) {
				p.fx = p.fy = 0.0f;
				p.predict(timeStep);
				interactWithFluid(p);
				interactWithParts(p);
			}
			computeParticleCollisions();
			for (Particle p : particles) {
				p.correct(timeStep);
				p.fx /= p.mass;
				p.fy /= p.mass;
			}
		}
	}

	private void interactWithFluid(Particle p) {
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
		p.fx = drag * (u[i][j] - p.vx);
		p.fy = drag * (v[i][j] - p.vy) + g;
	}

	private void interactWithParts(Particle p) {
		synchronized (parts) {
			for (Part part : parts) {
				if (part.reflect(p, timeStep, false))
					break;
			}
		}
	}

	private boolean interactWithBoundary(Particle p) {
		switch (boundary) {
		case REFLECTIVE:
			if (p.rx + p.radius > lx) {
				p.vx = -Math.abs(p.vx);
			} else if (p.rx - p.radius < 0) {
				p.vx = Math.abs(p.vx);
			}
			if (p.ry + p.radius > ly) {
				p.vy = -Math.abs(p.vy);
			} else if (p.ry - p.radius < 0) {
				p.vy = Math.abs(p.vy);
			}
			break;
		case OPEN:
			if (p.rx > lx || p.rx < 0 || p.ry > ly || p.ry < 0)
				return true;
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
				sigmaij = 4.0f * pi.radius * pj.radius;

				if (rijsq < rCutOffSquare * sigmaij) {

					sigab = pi.radius + pj.radius;
					sigab *= sigab;
					sr2 = sigab / rijsq;
					sr6 = sr2 * sr2 * sr2;
					sr12 = sr6 * sr6;
					vij = (sr12 - sr6) * epsilon;
					wij = vij + sr12 * epsilon;
					fij = wij / rijsq * 6f;
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
