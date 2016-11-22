package org.concord.energy2d.model;

import java.util.Arrays;

/**
 * @author Charles Xie
 * @author Mark Henning
 * 
 */
abstract class HeatSolver2D {

	int nx, ny, nx1, ny1, nx2, ny2;
	ThermalBoundary boundary;
	float[][] conductivity;
	float[][] specificHeat;
	float[][] density;
	float[][] q;
	float[][] u, v;
	float[][] tb;
	boolean[] tbHasValues;
	double[][] t0; // array that stores the previous temperature results
	boolean[][] fluidity;
	float deltaX, deltaY;
	float timeStep = 0.1f;
	float backgroundTemperature;
	float zHeatDiffusivity;
	boolean zHeatDiffusivityOnlyForFluid;
	// arrays for precalculated values to speed up simulation
	float[][] ax, bx, ay, by; // conductivity averaged over neighboring cells
	float[][] invSumSAB;
	float[][] s;

	HeatSolver2D(int nx, int ny) {
		this.nx = nx;
		this.ny = ny;
		nx1 = nx - 1;
		ny1 = ny - 1;
		nx2 = nx - 2;
		ny2 = ny - 2;
		t0 = new double[nx][ny];
		boundary = new DirichletThermalBoundary();
		ax = new float[nx][ny];
		bx = new float[nx][ny];
		ay = new float[nx][ny];
		by = new float[nx][ny];
		invSumSAB = new float[nx][ny];
		s = new float[nx][ny];
	}

	void reset() {
		for (int i = 0; i < nx; i++) {
			Arrays.fill(t0[i], 0);
		}
	}
	
	void refreshMatPropDerivedPrecalculations() {
		float hx = 0.5f / (deltaX * deltaX);
		float hy = 0.5f / (deltaY * deltaY);
		float invTimeStep = 1.0f / timeStep;
		float rij;

		for (int i = 1; i < nx1; i++) {
			for (int j = 1; j < ny1; j++) {
				// Calculation of these values is required once only
				// This speeds up the simulation by about 40%
				rij = conductivity[i][j];
				ax[i][j] = hx * (rij + conductivity[i - 1][j]);
				bx[i][j] = hx * (rij + conductivity[i + 1][j]);
				ay[i][j] = hy * (rij + conductivity[i][j - 1]);
				by[i][j] = hy * (rij + conductivity[i][j + 1]);
				s[i][j] = specificHeat[i][j] * density[i][j] * invTimeStep;
				invSumSAB[i][j] = 1f / (s[i][j] + ax[i][j] + bx[i][j] + ay[i][j] + by[i][j]);
			}
		}
	}

	void setTimeStep(float timeStep) {
		this.timeStep = timeStep;
	}

	float getTimeStep() {
		return timeStep;
	}

	void setFluidity(boolean[][] fluidity) {
		this.fluidity = fluidity;
	}

	void setGridCellSize(float deltaX, float deltaY) {
		this.deltaX = deltaX;
		this.deltaY = deltaY;
	}

	void setBoundary(ThermalBoundary boundary) {
		this.boundary = boundary;
	}

	ThermalBoundary getBoundary() {
		return boundary;
	}

	void setVelocity(float[][] u, float[][] v) {
		this.u = u;
		this.v = v;
	}

	void setConductivity(float[][] conductivity) {
		this.conductivity = conductivity;
	}

	void setSpecificHeat(float[][] specificHeat) {
		this.specificHeat = specificHeat;
	}

	void setDensity(float[][] density) {
		this.density = density;
	}

	void setPower(float[][] q) {
		this.q = q;
	}

	void setTemperatureBoundary(float[][] tb, boolean[] tbHasValues) {
		this.tb = tb;
		this.tbHasValues = tbHasValues;
	}

	abstract float calcMaxStableTimeStep();

	abstract void solvePrepare(boolean convective, double[][] t);
	abstract void solvePerformMT(boolean convective, double[][] t, int part, int numOfParts); // Intended to run multithreaded
	abstract void solvePostprocess(boolean convective, double[][] t);

	void applyBoundary(double[][] t) {

		if (boundary instanceof DirichletThermalBoundary) {
			DirichletThermalBoundary b = (DirichletThermalBoundary) boundary;
			float tUpper = b.getTemperatureAtBorder(Boundary.UPPER);
			float tLower = b.getTemperatureAtBorder(Boundary.LOWER);
			float tLeft = b.getTemperatureAtBorder(Boundary.LEFT);
			float tRight = b.getTemperatureAtBorder(Boundary.RIGHT);
			for (int i = 0; i < nx; i++) {
				t[i][0] = tUpper;
				t[i][ny1] = tLower;
			}
			for (int j = 0; j < ny; j++) {
				t[0][j] = tLeft;
				t[nx1][j] = tRight;
			}
		} else if (boundary instanceof ComplexDirichletThermalBoundary) {
			ComplexDirichletThermalBoundary b = (ComplexDirichletThermalBoundary) boundary;
			float[] tUpper = b.getTemperaturesAtBorder(Boundary.UPPER);
			float[] tLower = b.getTemperaturesAtBorder(Boundary.LOWER);
			float[] tLeft = b.getTemperaturesAtBorder(Boundary.LEFT);
			float[] tRight = b.getTemperaturesAtBorder(Boundary.RIGHT);
			for (int i = 0; i < nx; i++) {
				t[i][0] = tUpper[i];
				t[i][ny1] = tLower[i];
			}
			for (int j = 0; j < ny; j++) {
				t[0][j] = tLeft[j];
				t[nx1][j] = tRight[j];
			}
		} else if (boundary instanceof NeumannThermalBoundary) {
			NeumannThermalBoundary b = (NeumannThermalBoundary) boundary;
			float fN = b.getFluxAtBorder(Boundary.UPPER);
			float fS = b.getFluxAtBorder(Boundary.LOWER);
			float fW = b.getFluxAtBorder(Boundary.LEFT);
			float fE = b.getFluxAtBorder(Boundary.RIGHT);
			// very small conductivity at the border could cause strange behaviors (e.g., huge number of isotherm lines), so impose a minimum
			float minConductivity = 0.001f;
			for (int i = 0; i < nx; i++) {
				t[i][0] = t[i][1] + fN * deltaY / Math.max(conductivity[i][0], minConductivity);
				t[i][ny1] = t[i][ny2] - fS * deltaY / Math.max(conductivity[i][ny1], minConductivity);
			}
			for (int j = 0; j < ny; j++) {
				t[0][j] = t[1][j] - fW * deltaX / Math.max(conductivity[0][j], minConductivity);
				t[nx1][j] = t[nx2][j] + fE * deltaX / Math.max(conductivity[nx1][j], minConductivity);
			}
		}

	}

}
