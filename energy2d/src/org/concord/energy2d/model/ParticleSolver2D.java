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

	private float timeStep = 0.1f;
	private float drag = 0.1f;
	private byte boundary = REFLECTIVE;

	private List<Particle> particles;
	private float[][] u, v;
	private List<Part> parts;
	private float lx, ly;
	private int nx, ny;

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
			Particle p;
			for (Iterator<Particle> it = particles.iterator(); it.hasNext();) {
				p = it.next();
				p.predict(timeStep);
				calculate(p);
				p.correct(timeStep);
				interact(p);
				if (applyBoundary(p))
					it.remove();
			}
		}
	}

	private void calculate(Particle p) {
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
		p.fy = drag * (v[i][j] - p.vy) + 0.00001f;
		p.fx /= p.mass;
		p.fy /= p.mass;
	}

	private void interact(Particle p) {
		synchronized (parts) {
			for (Part part : parts) {
				if (part.reflect(p, timeStep, false))
					break;
			}
		}
	}

	private boolean applyBoundary(Particle p) {
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

}
