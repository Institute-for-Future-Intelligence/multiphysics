package org.concord.energy2d.model;

import java.util.List;

/**
 * This implements the discrete phase modeling (http://www.bakker.org/dartmouth06/engs150/15-dpm.pdf) which couples Lagrangian flow with Eulerian flow.
 * 
 * @author Charles Xie
 * 
 */
class ParticleSolver2D {

	private float timeStep = 0.01f;
	private float drag = 0.1f;

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
		lx = model.getLx();
		ly = model.getLy();
		nx = model.getNx();
		ny = model.getNy();
	}

	void move() {
		synchronized (particles) {
			for (Particle p : particles) {
				p.predict(timeStep);
				calculate(p);
				p.correct(timeStep);
			}
		}
	}

	private void calculate(Particle p) {
		int i = (int) (p.rx / lx * nx);
		int j = (int) (p.ry / ly * ny);
		p.fx = drag * (u[i][j] - p.vx);
		p.fy = drag * (v[i][j] - p.vy);
	}

}
