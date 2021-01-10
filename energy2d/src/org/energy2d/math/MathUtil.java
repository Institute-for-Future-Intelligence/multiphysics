package org.energy2d.math;

/**
 * @author Charles Xie
 * 
 */
public class MathUtil {

	/** @return true if x is between a and b. */
	public final static boolean between(float a, float b, float x) {
		return x < Math.max(a, b) && x > Math.min(a, b);
	}

	public static float getMax(float[] array) {
		float max = -Float.MAX_VALUE;
		for (float x : array) {
			if (x > max)
				max = x;
		}
		return max;
	}

	public static float getMin(float[] array) {
		float min = Float.MAX_VALUE;
		for (float x : array) {
			if (x < min)
				min = x;
		}
		return min;
	}

	public static float getMax(float[][] array) {
		float max = -Float.MAX_VALUE;
		for (float[] a : array) {
			for (float x : a) {
				if (x > max)
					max = x;
			}
		}
		return max;
	}

	public static float getMin(float[][] array) {
		float min = Float.MAX_VALUE;
		for (float[] a : array) {
			for (float x : a) {
				if (x < min)
					min = x;
			}
		}
		return min;
	}

	public static float getAverage(float[][] array) {
		float ave = 0;
		for (float[] a : array) {
			for (float x : a) {
				ave += x;
			}
		}
		return ave / (array.length * array[0].length);
	}

}
