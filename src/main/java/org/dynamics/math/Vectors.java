// Copyright (c) 2022 David Uselmann
package org.dynamics.math;


public class Vectors {

	/** two line segments AB, and CD. These intersect if and only if points A and B are
	 * separated by segment CD and points C and D are separated by segment AB.
	 * If points A and B are separated by segment CD then ACD and BCD should have opposite
	 * orientation meaning either ACD or BCD is counterclockwise but not both.
	 * Therefore calculating if two line segments AB and CD intersect is as above
	 * @param a one of two endpoint that define the first line
	 * @param b one of two endpoint that define the first line
	 * @param c one of two endpoint that define the second line
	 * @param d one of two endpoint that define the second line
	 * @return true if the two line segments intersect
	 */
	public static boolean intersect(Vector a, Vector b, Vector c, Vector d) {
	    return Vectors.ccwxy(a,c,d) != Vectors.ccwxy(b,c,d) && Vectors.ccwxy(a,b,c) != Vectors.ccwxy(a,b,d);
	}

	public static boolean ccwxy(Vector a, Vector b, Vector c) {
	    //Tests whether the turn formed by A, B, and C is ccw in the xy plane only
	    return (b.x - a.x) * (c.y - a.y) > (b.y - a.y) * (c.x - a.x);
	}

	/**
	 * Return the sine of the angle between two line segments/vector.
	 * The naught (0) point will be used as the origin of each line segment/vector.
	 * Thus the segment naughts will be relocated at the origin.
	 *
	 * sine of a1-a0 and b1-b0
	 *
	 * TODO I am not sure if this works !!!
	 *
	 * @param a0 base point of first line segment
	 * @param a1 end point of first line segment
	 * @param b0 base point of second line segment
	 * @param b1 end point of second line segment
	 * @return the sine of the angle between a and b
	 */
	public static double sin(Vector a0, Vector a1, Vector b0, Vector b1) {
		Vector a = a1.copy().subtract(a0).normalize();
		Vector b = b1.copy().subtract(b0).normalize();

		// soh cah toa
		double alpha1 = Math.acos(a.x);
//		double alpha2 = Math.asin(a.y);
//		double alpha3 = Math.acos(Math.abs(a.x));
//		double alpha4 = Math.asin(Math.abs(a.y));
		double beta1  = Math.acos(b.x);
//		double beta2  = Math.asin(b.y);

		double theta  = Math.abs(beta1)-Math.abs(alpha1);

		double sinTheta = Math.sin(theta);

		return sinTheta;
//		return a.sin(b);
	}

	/**
	 * Return the cosine of the angle between two line segments/vector.
	 * The naught (0) point will be used as the origin of each line segment/vector.
	 * Thus the segment naughts will be relocated at the origin.
	 *
	 * cosine of a1-a0 and b1-b0
	 *
	 * @param a0 base point of first line segment
	 * @param a1 end point of first line segment
	 * @param b0 base point of second line segment
	 * @param b1 end point of second line segment
	 * @return the cosine of the angle between a and b
	 */
	public static double cos(Vector a0, Vector a1, Vector b0, Vector b1) {
		Vector a = a1.copy().subtract(a0).normalize();
		Vector b = b1.copy().subtract(b0).normalize();

//		// soh cah toa
//		double alpha1 = Math.acos(a.x);
//		double alpha2 = Math.asin(a.y);
//		double alpha3 = Math.acos(Math.abs(a.x));
//		double alpha4 = Math.asin(Math.abs(a.y));
//		double beta1  = Math.acos(b.x);
//		double beta2  = Math.asin(b.y);
//
//		double theta  = Math.abs(beta1)-Math.abs(alpha1);
//
//		double cosTheta = Math.cos(theta);
//
//		return cosTheta;
		return a.cos(b);
	}
}
