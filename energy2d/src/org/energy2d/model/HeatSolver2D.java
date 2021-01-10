package org.energy2d.model;

import java.util.Arrays;

/**
 * @author Charles Xie
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
	float[][] t0; // array that stores the previous temperature results
	boolean[][] fluidity;
	float deltaX, deltaY;
	float timeStep = 0.1f;
	float backgroundTemperature;
	float zHeatDiffusivity;
	boolean zHeatDiffusivityOnlyForFluid;

	HeatSolver2D(int nx, int ny) {
		this.nx = nx;
		this.ny = ny;
		nx1 = nx - 1;
		ny1 = ny - 1;
		nx2 = nx - 2;
		ny2 = ny - 2;
		t0 = new float[nx][ny];
		boundary = new DirichletThermalBoundary();
	}

	void reset() {
		for (int i = 0; i < nx; i++) {
			Arrays.fill(t0[i], 0);
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

	void setTemperatureBoundary(float[][] tb) {
		this.tb = tb;
	}

	abstract void solve(boolean convective, float[][] t);

	void applyBoundary(float[][] t) {

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
