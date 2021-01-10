package org.energy2d.math;

/**
 * The tridiagonal matrix algorithm (TDMA), also known as the Thomas algorithm,
 * is a simplified form of Gaussian elimination that can be used to solve
 * tridiagonal systems of equations. A tridiagonal system for n unknowns may be
 * written as
 * 
 * a[i]*x[i-1]+b[i]*x[i]+c[i]*x[i+1]=d[i], where a[0]=0 and c[n-1]=0.
 * 
 * @author Charles Xie
 * 
 */
public class Tdma {

	private Tdma() {
	}

	/**
	 * Floating number version. c and d are modified during the operation. All
	 * input arrays must be of the same size.
	 * 
	 * @param a
	 *            the subdiagonal elements
	 * @param b
	 *            the diagonal elements
	 * @param c
	 *            the superdiagonal elements
	 * @param d
	 *            the right-hand-side vector
	 * @param x
	 *            the result vector
	 */
	public static void solve(float[] a, float[] b, float[] c, float[] d, float[] x) {
		int n = a.length;
		float temp;
		c[0] /= b[0];
		d[0] /= b[0];
		for (int i = 1; i < n; i++) {
			temp = 1.0f / (b[i] - c[i - 1] * a[i]);
			c[i] *= temp; // redundant at the last step as c[n-1]=0.
			d[i] = (d[i] - d[i - 1] * a[i]) * temp;
		}
		x[n - 1] = d[n - 1];
		for (int i = n - 2; i >= 0; i--) {
			x[i] = d[i] - c[i] * x[i + 1];
		}
	}

}
