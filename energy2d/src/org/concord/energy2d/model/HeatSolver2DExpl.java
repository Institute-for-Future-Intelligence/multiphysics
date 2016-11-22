package org.concord.energy2d.model;

import org.concord.energy2d.util.MiscUtil;

/**
 * By default, we use an explicit solver.
 * It is accurate for static as well as for dynamic simulations and allows multithreading.
 * 
 * @author Mark Henning
 * 
 */
class HeatSolver2DExpl extends HeatSolver2D {

	HeatSolver2DExpl(int nx, int ny) {
		super(nx, ny);
	}

	float calcMaxStableTimeStep() {
		/*
		 * let  dT/dt = k T''
		 * then stability is given for time steps dt and grid spacings dx if
		 * 		0 < k dt / dx^2 < 0.5
		 * =>	0 < dt < 0.5 dx^2 / k
		 * For heat flow, k = heat conductivity / (specific heat capacity * density)
		 */
		float deltaMin;
		float kij;
		float kMax = 0;

		deltaMin = Float.min(deltaX, deltaY);
		
		for (int i = 1; i < nx1; i++) {
			for (int j = 1; j < ny1; j++) {
				kij = conductivity[i][j] / (specificHeat[i][j] * density[i][j]);
				if (kij > kMax) {
					kMax = kij;
				}
			}
		}
		
		return 0.5f * deltaMin * deltaMin / kMax;
	}

	void solvePrepare(boolean convective, double[][] t) {
		// Copying a two-dimensional array is very fast: it takes less than 1% compared with the time for the relaxation solver below. Considering this, I chose clarity instead of swapping the arrays.
		MiscUtil.copy(t0, t);
	}
 
	void solvePerformMT(boolean convective, double[][] t, int part, int numOfParts) {
		boolean solveZ = zHeatDiffusivity != 0;

		// To speed up referencing the values
		float tbi[];
		double ti[];
		double t0i[];
		double t0ip1[];
		double t0im1[];
		float si[];
		float qi[];
		float axi[];
		float ayi[];
		float bxi[];
		float byi[];
		float invSumSABi[];
		boolean fluidityi[];

		int start = 1 + (part * (nx1-1)) / numOfParts;
		int end = 1 + ((part+1) * (nx1-1)) / numOfParts;
		
		for (int i = start; i < end; i++) {
			tbi = tb[i];
			ti = t[i];
			t0i = t0[i];
			t0ip1 = t0[i+1];
			t0im1 = t0[i-1];
			si = s[i];
			qi = q[i];
			axi = ax[i];
			ayi = ay[i];
			bxi = bx[i];
			byi = by[i];
			invSumSABi = invSumSAB[i];
			fluidityi = fluidity[i];
			if (tbHasValues[i]) {
				for (int j = 1; j < ny1; j++) {
					if (Float.isNaN(tbi[j])) {
						// how do we deal with vacuum? if(density[i][j]==0 || density[i-1][j]==0||density[i+1][j]==0||density[i][j-1]==0||density[i][j+1]==0) continue;
						ti[j] = (t0i[j] * si[j] + qi[j] + axi[j] * t0im1[j] + bxi[j] * t0ip1[j] + ayi[j] * t0i[j - 1] + byi[j] * t0i[j + 1]) * invSumSABi[j];
						// use a simple proportional control only at the last step of relaxation if applicable
						if (solveZ) {
							if (!zHeatDiffusivityOnlyForFluid || (zHeatDiffusivityOnlyForFluid && fluidityi[j]))
								ti[j] -= zHeatDiffusivity * timeStep * (t0i[j] - backgroundTemperature);
						}
					} else {
						ti[j] = tbi[j];
					}
				}
			} else {
				for (int j = 1; j < ny1; j++) {
					// how do we deal with vacuum? if(density[i][j]==0 || density[i-1][j]==0||density[i+1][j]==0||density[i][j-1]==0||density[i][j+1]==0) continue;
					ti[j] = (t0i[j] * si[j] + qi[j] + axi[j] * t0im1[j] + bxi[j] * t0ip1[j] + ayi[j] * t0i[j - 1] + byi[j] * t0i[j + 1]) * invSumSABi[j];
					// use a simple proportional control only at the last step of relaxation if applicable
					if (solveZ) {
						if (!zHeatDiffusivityOnlyForFluid || (zHeatDiffusivityOnlyForFluid && fluidityi[j]))
							ti[j] -= zHeatDiffusivity * timeStep * (t0i[j] - backgroundTemperature);
					}
				}
			}
		}
	}

	void solvePostprocess(boolean convective, double[][] t) {
		applyBoundary(t);

		if (convective) {
			advect(t);
		}

	}

	private void advect(double[][] t) {
		macCormack(t);
	}

	// MacCormack
	private void macCormack(double[][] t) {

		float tx = 0.5f * timeStep / deltaX;
		float ty = 0.5f * timeStep / deltaY;
		
		for (int i = 1; i < nx1; i++) {
			final double[] ti = t[i];
			final double[] tim1 = t[i-1];
			final double[] tip1 = t[i+1];
			final double[] t0i = t0[i];
			final float[] vi = v[i];
			final float[] uim1 = u[i-1];
			final float[] uip1 = u[i+1];
			final boolean[] fluidityi = fluidity[i];
			for (int j = 1; j < ny1; j++) {
				if (fluidityi[j]) {
					t0i[j] = ti[j] - tx * (uip1[j] * tip1[j] - uim1[j] * tim1[j]) - ty * (vi[j + 1] * ti[j + 1] - vi[j - 1] * ti[j - 1]);
				}
			}
		}

		applyBoundary(t0);

		for (int i = 1; i < nx1; i++) {
			final double[] ti = t[i];
			final double[] t0im1 = t0[i-1];
			final double[] t0ip1 = t0[i+1];
			final double[] t0i = t0[i];
			final float[] vi = v[i];
			final float[] ui = u[i];
			final boolean[] fluidityi = fluidity[i];
			for (int j = 1; j < ny1; j++) {
				if (fluidityi[j]) {
					ti[j] = 0.5f * (ti[j] + t0i[j]) - 0.5f * tx * ui[j] * (t0ip1[j] - t0im1[j]) - 0.5f * ty * vi[j] * (t0i[j + 1] - t0i[j - 1]);
				}
			}
		}

		applyBoundary(t);

	}

}