package org.energy2d.model;

import org.energy2d.util.MiscUtil;

/**
 * By default, we use an implicit solver, which is unconditionally stable. While it finds the equilibrium state more quickly, the numeric diffusion that stablizes it causes the transient state to be inaccurate.
 * 
 * A smaller time step will need to be used, compared with an explicit solver.
 * 
 * We should also implement an explicit solver as an option. It makes no sense to use an implicit method when we need transient accuracy.
 * 
 * @author Charles Xie
 * 
 */
class HeatSolver2DImpl extends HeatSolver2D {

	// five relaxation steps are probably enough for most transient problems because there are numerous previous steps that
	// can be considered as pre-relaxation steps, especially when changes are slow or small.
	private static byte relaxationSteps = 5;

	HeatSolver2DImpl(int nx, int ny) {
		super(nx, ny);
	}

	void solve(boolean convective, float[][] t) {

		// Copying a two-dimensional array is very fast: it takes less than 1% compared with the time for the relaxation solver below. Considering this, I chose clarity instead of swapping the arrays.
		MiscUtil.copy(t0, t);

		float hx = 0.5f / (deltaX * deltaX);
		float hy = 0.5f / (deltaY * deltaY);
		float rij, sij, axij, bxij, ayij, byij;
		float invTimeStep = 1f / timeStep;

		boolean solveZ = zHeatDiffusivity != 0;

		for (int k = 0; k < relaxationSteps; k++) {
			for (int i = 1; i < nx1; i++) {
				for (int j = 1; j < ny1; j++) {
					if (Float.isNaN(tb[i][j])) {
						// how do we deal with vacuum? if(density[i][j]==0 || density[i-1][j]==0||density[i+1][j]==0||density[i][j-1]==0||density[i][j+1]==0) continue;
						sij = specificHeat[i][j] * density[i][j] * invTimeStep;
						rij = conductivity[i][j];
						axij = hx * (rij + conductivity[i - 1][j]);
						bxij = hx * (rij + conductivity[i + 1][j]);
						ayij = hy * (rij + conductivity[i][j - 1]);
						byij = hy * (rij + conductivity[i][j + 1]);
						t[i][j] = (t0[i][j] * sij + q[i][j] + axij * t[i - 1][j] + bxij * t[i + 1][j] + ayij * t[i][j - 1] + byij * t[i][j + 1]) / (sij + axij + bxij + ayij + byij);
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

	private void advect(float[][] t) {
		macCormack(t);
	}

	// MacCormack
	private void macCormack(float[][] t) {

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