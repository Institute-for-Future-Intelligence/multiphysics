package org.energy2d.model;

import java.util.Arrays;

import org.energy2d.math.MathUtil;
import org.energy2d.util.MiscUtil;

/**
 * For incompressible fluid. Need to introduce a mechanism for energy dissipation.
 * 
 * @author Charles Xie
 * 
 */
abstract class FluidSolver2D {

	// five relaxation steps are probably enough for most transient problems because there are numerous previous steps that can be considered as pre-relaxation steps,
	// especially when changes are slow or small.
	static byte relaxationSteps = 5;

	private float thermalExpansionCoefficient = 0.00025f;
	private float gravity = 0; // not set
	private byte buoyancyApproximation = Model2D.BUOYANCY_AVERAGE_ALL;
	private byte gravityType = Model2D.GRAVITY_UNIFORM;

	float viscosity = Constants.AIR_VISCOSITY;

	int nx, ny, nx1, ny1, nx2, ny2;
	float[][] u0, v0;
	float timeStep = .1f;
	float deltaX, deltaY;
	boolean[][] fluidity;
	MassBoundary boundary;
	float[][] t;
	float[][] uWind, vWind;
	private float[][] vorticity, stream;

	private float i2dx, i2dy;
	float idxsq, idysq;

	FluidSolver2D(int nx, int ny) {
		this.nx = nx;
		this.ny = ny;
		nx1 = nx - 1;
		ny1 = ny - 1;
		nx2 = nx - 2;
		ny2 = ny - 2;
		u0 = new float[nx][ny];
		v0 = new float[nx][ny];
		boundary = new SimpleMassBoundary();
	}

	void reset() {
		for (int i = 0; i < nx; i++) {
			Arrays.fill(u0[i], 0);
			Arrays.fill(v0[i], 0);
		}
		if (vorticity != null) {
			for (int i = 0; i < nx; i++) {
				Arrays.fill(vorticity[i], 0);
			}
		}
		if (stream != null) {
			for (int i = 0; i < nx; i++) {
				Arrays.fill(stream[i], 0);
			}
		}
	}

	void setBoundary(MassBoundary boundary) {
		this.boundary = boundary;
	}

	MassBoundary getBoundary() {
		return boundary;
	}

	void setGravityType(byte gravityType) {
		this.gravityType = gravityType;
	}

	byte getGravityType() {
		return gravityType;
	}

	void setBuoyancyApproximation(byte buoyancyApproximation) {
		this.buoyancyApproximation = buoyancyApproximation;
	}

	byte getBuoyancyApproximation() {
		return buoyancyApproximation;
	}

	void setThermalExpansionCoefficient(float thermalExpansionCoefficient) {
		this.thermalExpansionCoefficient = thermalExpansionCoefficient;
	}

	float getThermalExpansionCoefficient() {
		return thermalExpansionCoefficient;
	}

	void setWindSpeed(float[][] uWind, float[][] vWind) {
		this.uWind = uWind;
		this.vWind = vWind;
	}

	void setBackgroundViscosity(float viscosity) {
		this.viscosity = viscosity;
	}

	float getViscosity() {
		return viscosity;
	}

	void setTemperature(float[][] t) {
		this.t = t;
	}

	void setFluidity(boolean[][] fluidity) {
		this.fluidity = fluidity;
	}

	void setGridCellSize(float deltaX, float deltaY) {
		this.deltaX = deltaX;
		this.deltaY = deltaY;
		i2dx = 0.5f / deltaX;
		i2dy = 0.5f / deltaY;
		idxsq = 1f / (deltaX * deltaX);
		idysq = 1f / (deltaY * deltaY);
	}

	void setTimeStep(float timeStep) {
		this.timeStep = timeStep;
	}

	float getTimeStep() {
		return timeStep;
	}

	private void setObstacleVelocity(float[][] u, float[][] v) {
		int count = 0;
		float uw, vw;
		for (int i = 1; i < nx1; i++) {
			for (int j = 1; j < ny1; j++) {
				if (!fluidity[i][j]) {
					uw = uWind[i][j];
					vw = vWind[i][j];
					count = 0;
					if (fluidity[i - 1][j]) {
						count++;
						u[i][j] = uw - u[i - 1][j];
						v[i][j] = vw + v[i - 1][j];
					} else if (fluidity[i + 1][j]) {
						count++;
						u[i][j] = uw - u[i + 1][j];
						v[i][j] = vw + v[i + 1][j];
					}
					if (fluidity[i][j - 1]) {
						count++;
						u[i][j] = uw + u[i][j - 1];
						v[i][j] = vw - v[i][j - 1];
					} else if (fluidity[i][j + 1]) {
						count++;
						u[i][j] = uw + u[i][j + 1];
						v[i][j] = vw - v[i][j + 1];
					}
					if (count == 0) {
						u[i][j] = uw;
						v[i][j] = vw;
					}
				}
			}
		}
	}

	// ensure dx/dn = 0 at the boundary (the Neumann boundary condition)
	private void setObstacleBoundary(float[][] x) {
		for (int i = 1; i < nx1; i++) {
			for (int j = 1; j < ny1; j++) {
				if (!fluidity[i][j]) {
					if (fluidity[i - 1][j]) {
						x[i][j] = x[i - 1][j];
					} else if (fluidity[i + 1][j]) {
						x[i][j] = x[i + 1][j];
					}
					if (fluidity[i][j - 1]) {
						x[i][j] = x[i][j - 1];
					} else if (fluidity[i][j + 1]) {
						x[i][j] = x[i][j + 1];
					}
				}
			}
		}
	}

	private float getMeanTemperature(int i, int j) {
		int lowerBound = 0;
		// search for the upper bound
		for (int k = j - 1; k > 0; k--) {
			if (!fluidity[i][k]) {
				lowerBound = k;
				break;
			}
		}
		int upperBound = ny;
		for (int k = j + 1; k < ny; k++) {
			if (!fluidity[i][k]) {
				upperBound = k;
				break;
			}
		}
		float t0 = 0;
		for (int k = lowerBound; k < upperBound; k++) {
			t0 += t[i][k];
		}
		return t0 / (upperBound - lowerBound);
	}

	// Boussinesq approximation: density differences are sufficiently small to be neglected, except where they appear in terms multiplied by g, the acceleration due to gravity.
	private void applyBuoyancy(float[][] f) {
		float g = gravity * timeStep;
		float b = thermalExpansionCoefficient * timeStep;
		float t0;
		switch (buoyancyApproximation) {
		case Model2D.BUOYANCY_AVERAGE_ALL:
			// TODO: Should we include solid state cells or should we not?
			// The logic of including all cells is to provide a more stable reference temperature. The results tend to look more normal with this choice.
			// We can tell this from the column average below, which tends to produce less normal-looking results.
			// However, why should solid cells have anything to do with fluid cells? And why should fluid cells isolated from the current one have anything to do with it?
			t0 = MathUtil.getAverage(t);
			for (int i = 1; i < nx1; i++) {
				for (int j = 1; j < ny1; j++) {
					if (fluidity[i][j]) {
						f[i][j] += (g - b) * t[i][j] + b * t0;
					}
				}
			}
			break;
		case Model2D.BUOYANCY_AVERAGE_COLUMN:
			for (int i = 1; i < nx1; i++) {
				for (int j = 1; j < ny1; j++) {
					if (fluidity[i][j]) {
						t0 = getMeanTemperature(i, j);
						f[i][j] += (g - b) * t[i][j] + b * t0;
					}
				}
			}
			break;
		}
	}

	// for simulating mantle convection of planets
	private void applySphericalBuoyancy(float[][] u, float[][] v) {
		float g = gravity * timeStep;
		float b = thermalExpansionCoefficient * timeStep;
		float t0 = MathUtil.getAverage(t);
		float dx = 0, dy = 0, dr = 0;
		float cx = nx / 2, cy = ny / 2;
		for (int i = 1; i < nx1; i++) {
			for (int j = 1; j < ny1; j++) {
				if (fluidity[i][j]) {
					dx = (i - cx) * deltaX;
					dy = (j - cy) * deltaY;
					dr = (float) (1.0 / Math.hypot(dx, dy));
					dx *= dr;
					dy *= dr;
					dr = (g - b) * t[i][j] + b * t0;
					u[i][j] -= dr * dx;
					v[i][j] -= dr * dy;
				}
			}
		}
	}

	abstract void diffuse(int b, float[][] f0, float[][] f);

	abstract void advect(int b, float[][] f0, float[][] f);

	// Copying a two-dimensional array is very fast. Considering this, I chose clarity instead of swapping the arrays.
	void solve(float[][] u, float[][] v) {
		if (thermalExpansionCoefficient != 0) {
			switch (gravityType) {
			case Model2D.GRAVITY_UNIFORM:
				applyBuoyancy(v);
				break;
			case Model2D.GRAVITY_CENTRIC:
				applySphericalBuoyancy(u, v);
				break;
			}
		}
		setObstacleVelocity(u, v);
		if (viscosity > 0) { // viscid
			diffuse(1, u0, u);
			diffuse(2, v0, v);
			conserve(u, v, u0, v0);
			setObstacleVelocity(u, v);
		}
		MiscUtil.copy(u0, u);
		MiscUtil.copy(v0, v);
		advect(1, u0, u);
		advect(2, v0, v);
		conserve(u, v, u0, v0);
		setObstacleVelocity(u, v);
	}

	/*
	 * enforce the continuity condition div(V)=0 (velocity field must be divergence-free to conserve mass) using the relaxation method: http://en.wikipedia.org/wiki/Relaxation_method. This procedure solves the Poisson equation.
	 */
	void conserve(float[][] u, float[][] v, float[][] phi, float[][] div) {

		for (int i = 1; i < nx1; i++) {
			for (int j = 1; j < ny1; j++) {
				if (fluidity[i][j]) {
					div[i][j] = (u[i + 1][j] - u[i - 1][j]) * i2dx + (v[i][j + 1] - v[i][j - 1]) * i2dy;
					phi[i][j] = 0;
				}
			}
		}
		applyBoundary(0, div);
		applyBoundary(0, phi);
		setObstacleBoundary(div);
		setObstacleBoundary(phi);

		float s = 0.5f / (idxsq + idysq);

		for (int k = 0; k < relaxationSteps; k++) {
			for (int i = 1; i < nx1; i++) {
				for (int j = 1; j < ny1; j++) {
					if (fluidity[i][j]) {
						phi[i][j] = s * ((phi[i - 1][j] + phi[i + 1][j]) * idxsq + (phi[i][j - 1] + phi[i][j + 1]) * idysq - div[i][j]);
					}
				}
			}
		}

		for (int i = 1; i < nx1; i++) {
			for (int j = 1; j < ny1; j++) {
				if (fluidity[i][j]) {
					u[i][j] -= (phi[i + 1][j] - phi[i - 1][j]) * i2dx;
					v[i][j] -= (phi[i][j + 1] - phi[i][j - 1]) * i2dy;
				}
			}
		}
		applyBoundary(1, u);
		applyBoundary(2, v);

	}

	float[][] getStreamFunction(float[][] u, float[][] v) {
		if (vorticity == null)
			vorticity = new float[nx][ny];
		if (stream == null)
			stream = new float[nx][ny];
		calculateVorticity(u, v);
		calculateStreamFunction();
		return stream;
	}

	private void calculateStreamFunction() {
		float s = 0.5f / (idxsq + idysq);
		for (int i = 0; i < nx; i++) {
			Arrays.fill(stream[i], 0);
		}
		for (int k = 0; k < relaxationSteps; k++) {
			for (int i = 1; i < nx1; i++) {
				for (int j = 1; j < ny1; j++) {
					if (fluidity[i][j]) {
						stream[i][j] = s * ((stream[i - 1][j] + stream[i + 1][j]) * idxsq + (stream[i][j - 1] + stream[i][j + 1]) * idysq + vorticity[i][j]);
					}
				}
			}
			applyBoundary(0, stream);
			setObstacleBoundary(stream);
		}
	}

	private void calculateVorticity(float[][] u, float[][] v) {
		float du_dy, dv_dx;
		for (int i = 1; i < nx1; i++) {
			for (int j = 1; j < ny1; j++) {
				if (fluidity[i][j]) {
					du_dy = (u[i][j + 1] - u[i][j - 1]) / (2 * deltaY);
					dv_dx = (v[i + 1][j] - v[i - 1][j]) / (2 * deltaX);
					vorticity[i][j] = du_dy - dv_dx;
				}
			}
		}
		applyBoundary(0, vorticity);
		setObstacleBoundary(vorticity);
	}

	/* b=1 horizontal; b=2 vertical */
	void applyBoundary(int direction, float[][] f) {
		SimpleMassBoundary b = (SimpleMassBoundary) boundary;
		boolean horizontal = direction == 1;
		boolean vertical = direction == 2;
		for (int i = 1; i < nx1; i++) {
			// upper side
			if (vertical) {
				switch (b.getFlowTypeAtBorder(Boundary.UPPER)) {
				case MassBoundary.STOP:
					f[i][0] = 0;
					break;
				case MassBoundary.REFLECTIVE:
					f[i][0] = -f[i][1];
					break;
				}
			} else {
				f[i][0] = f[i][1];
			}
			// lower side
			if (vertical) {
				switch (b.getFlowTypeAtBorder(Boundary.LOWER)) {
				case MassBoundary.STOP:
					f[i][ny1] = 0;
					break;
				case MassBoundary.REFLECTIVE:
					f[i][ny1] = -f[i][ny2];
					break;
				}
			} else {
				f[i][ny1] = f[i][ny2];
			}
		}
		for (int j = 1; j < ny1; j++) {
			// left side
			if (horizontal) {
				switch (b.getFlowTypeAtBorder(Boundary.LEFT)) {
				case MassBoundary.STOP:
					f[0][j] = 0;
					break;
				case MassBoundary.REFLECTIVE:
					f[0][j] = -f[1][j];
					break;
				}
			} else {
				f[0][j] = f[1][j];
			}
			// right side
			if (horizontal) {
				switch (b.getFlowTypeAtBorder(Boundary.RIGHT)) {
				case MassBoundary.STOP:
					f[nx1][j] = 0;
					break;
				case MassBoundary.REFLECTIVE:
					f[nx1][j] = -f[nx2][j];
					break;
				}
			} else {
				f[nx1][j] = f[nx2][j];
			}
		}
		// upper-left corner
		f[0][0] = 0.5f * (f[1][0] + f[0][1]);
		// upper-right corner
		f[nx1][0] = 0.5f * (f[nx2][0] + f[nx1][1]);
		// lower-left corner
		f[0][ny1] = 0.5f * (f[1][ny1] + f[0][ny2]);
		// lower-right corner
		f[nx1][ny1] = 0.5f * (f[nx2][ny1] + f[nx1][ny2]);
	}

}
