package org.concord.energy2d.model;

import org.concord.energy2d.util.MiscUtil;

/**
 * An implicit solver is unconditionally stable. While it finds the equilibrium state more quickly, the numeric diffusion that stablizes it causes the transient state to be inaccurate.
 * 
 * However, the current implementation does not work properly, as it gives asymmetric results, so it was disabled.
 * The class is kept as it might serve as a basis for future implementations.
 * 
 * Besides that, although having optimized the solver for speed, it currently lacks multithreading capabilities.
 * 
 * @author Charles Xie
 * @author Mark Henning
 * 
 */
class HeatSolver2DImpl extends HeatSolver2D {

	// five relaxation steps are probably enough for most transient problems because there are numerous previous steps that
	// can be considered as pre-relaxation steps, especially when changes are slow or small.
	private static byte relaxationSteps = 5;

	HeatSolver2DImpl(int nx, int ny) {
		super(nx, ny);
	}

	float calcMaxStableTimeStep() {
		return Float.MAX_VALUE; // For the implicit solver, there is currently no max time step
	}

	void solvePrepare(boolean convective, double[][] t) {
		// Copying a two-dimensional array is very fast: it takes less than 1% compared with the time for the relaxation solver below. Considering this, I chose clarity instead of swapping the arrays.
		MiscUtil.copy(t0, t);

		boolean solveZ = zHeatDiffusivity != 0;

		for (int k = 0; k < relaxationSteps; k++) {
			for (int i = 1; i < nx1; i++) {
				for (int j = 1; j < ny1; j++) {
					if (Float.isNaN(tb[i][j])) {
						// how do we deal with vacuum? if(density[i][j]==0 || density[i-1][j]==0||density[i+1][j]==0||density[i][j-1]==0||density[i][j+1]==0) continue;
						t[i][j] = (t0[i][j] * s[i][j] + q[i][j] + ax[i][j] * t[i - 1][j] + bx[i][j] * t[i + 1][j] + ay[i][j] * t[i][j - 1] + by[i][j] * t[i][j + 1]) * invSumSAB[i][j];
						// use a simple proportional control only at the last step of relaxation if applicable
						if (solveZ && k == relaxationSteps - 1) {
							if (!zHeatDiffusivityOnlyForFluid || (zHeatDiffusivityOnlyForFluid && fluidity[i][j]))
								t[i][j] -= zHeatDiffusivity * timeStep * (t0[i][j] - backgroundTemperature);
						}
					} else {
						t[i][j] = tb[i][j];
					}
				}
			}
			applyBoundary(t);
		}

		if (convective) {
			advect(t);
		}
	}
	 
	void solvePerformMT(boolean convective, double[][] t, int part, int numOfParts) {
		// Currently, this solver does not support running in multithreaded mode
	}

	void solvePostprocess(boolean convective, double[][] t) {
	}
	
	private void advect(double[][] t) {
		macCormack(t);
	}

	// MacCormack
	private void macCormack(double[][] t) {

		float tx = 0.5f * timeStep / deltaX;
		float ty = 0.5f * timeStep / deltaY;
		for (int i = 1; i < nx1; i++) {
			for (int j = 1; j < ny1; j++) {
				if (fluidity[i][j]) {
					t0[i][j] = t[i][j] - tx * (u[i + 1][j] * t[i + 1][j] - u[i - 1][j] * t[i - 1][j]) - ty * (v[i][j + 1] * t[i][j + 1] - v[i][j - 1] * t[i][j - 1]);
				}
			}
		}

		applyBoundary(t0);

		for (int i = 1; i < nx1; i++) {
			for (int j = 1; j < ny1; j++) {
				if (fluidity[i][j]) {
					t[i][j] = 0.5f * (t[i][j] + t0[i][j]) - 0.5f * tx * u[i][j] * (t0[i + 1][j] - t0[i - 1][j]) - 0.5f * ty * v[i][j] * (t0[i][j + 1] - t0[i][j - 1]);
				}
			}
		}

		applyBoundary(t);

	}

}