package org.energy2d.model;

import org.energy2d.util.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class FluidSolver2DImpl extends FluidSolver2D {

	FluidSolver2DImpl(int nx, int ny) {
		super(nx, ny);
	}

	void diffuse(int b, float[][] f0, float[][] f) {

		// Copying a two-dimensional array is very fast: it takes less than 1% compared with the time for the relaxation solver below. Considering this, I chose clarity instead of swapping the arrays.
		MiscUtil.copy(f0, f);

		float hx = timeStep * viscosity * idxsq;
		float hy = timeStep * viscosity * idysq;
		float dn = 1f / (1 + 2 * (hx + hy));

		for (int k = 0; k < relaxationSteps; k++) {
			for (int i = 1; i < nx1; i++) {
				for (int j = 1; j < ny1; j++) {
					if (fluidity[i][j]) {
						f[i][j] = (f0[i][j] + hx * (f[i - 1][j] + f[i + 1][j]) + hy * (f[i][j - 1] + f[i][j + 1])) * dn;
					}
				}
			}
			applyBoundary(b, f);
		}

	}

	void advect(int b, float[][] f0, float[][] f) {
		macCormack(b, f0, f);
	}

	// MacCormack
	private void macCormack(int b, float[][] f0, float[][] f) {

		float tx = 0.5f * timeStep / deltaX;
		float ty = 0.5f * timeStep / deltaY;
		for (int i = 1; i < nx1; i++) {
			for (int j = 1; j < ny1; j++) {
				if (fluidity[i][j]) {
					f[i][j] = f0[i][j] - tx * (u0[i + 1][j] * f0[i + 1][j] - u0[i - 1][j] * f0[i - 1][j]) - ty * (v0[i][j + 1] * f0[i][j + 1] - v0[i][j - 1] * f0[i][j - 1]);
				}
			}
		}

		applyBoundary(b, f);

		for (int i = 1; i < nx1; i++) {
			for (int j = 1; j < ny1; j++) {
				if (fluidity[i][j]) {
					f0[i][j] = 0.5f * (f0[i][j] + f[i][j]) - 0.5f * tx * u0[i][j] * (f[i + 1][j] - f[i - 1][j]) - 0.5f * ty * v0[i][j] * (f[i][j + 1] - f[i][j - 1]);
				}
			}
		}

		MiscUtil.copy(f, f0);

		applyBoundary(b, f);

	}

}
