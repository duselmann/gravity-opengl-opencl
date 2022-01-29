// Copyright (c) 2022 David Uselmann
package org.dynamics.math;


import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class Vector {
    public static final double RADIANS_PER_DEGREE = Math.PI / 180.0;

    public double x;
    public double y;
    public double z;


    public Vector() {
    }
    public Vector(double x) {
        this(x,0,0);
    }
    public Vector(double x, double y) {
        this(x,y,0);
    }
    public Vector(double x, double y, double z) {
        setXYZ(x,y,z);
    }
    public Vector(Vector v) {
        this(v.x, v.y, v.z);
    }

    public Vector(final List<Double> values) {
        int diff = 3 - values.size();
        for (int r=0; r<diff; r++) {
        	values.add(0.0);
        }
        setXYZ(values.get(0), values.get(1), values.get(2));
    }

    public Vector setXYZ(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector copy() {
    	return new Vector(this);
    }

//    public double x() {
//        return x;
//    }
//    public void setX(double x) {
//        this.x = x;
//    }
//    public double y() {
//        return y;
//    }
//    public void setY(double y) {
//        this.y = y;
//    }
//    public double z() {
//        return z;
//    }
//    public void setZ(double z) {
//        this.z = z;
//    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = PRIME * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = PRIME * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = PRIME * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
			return true;
		}
        if (obj == null) {
			return false;
		}
        if (getClass() != obj.getClass()) {
			return false;
		}
        final Vector other = (Vector) obj;
        if (x != other.x) {
			return false;
		}
        if (y != other.y) {
			return false;
		}
        return z == other.z;
    }
    public boolean equals(Object obj, double tolerance) {
        if (this == obj) {
			return true;
		}
        if (obj == null) {
			return false;
		}
        if (getClass() != obj.getClass()) {
			return false;
		}
        final Vector other = (Vector) obj;
        if (!isWithinTolerance(x, other.x, tolerance)) {
        	return false;
        }
        if (!isWithinTolerance(y, other.y, tolerance)) {
        	return false;
        }
        return isWithinTolerance(z, other.z, tolerance);
    }
    public static boolean isWithinTolerance(double a, double b, double tolerance) {
        return (a > b - tolerance && a < b + tolerance);
    }

    @Override
    public String toString() {
        return "<" +
                x + ", " +
                y + ", " +
                z + ">";
    }

    /**
     * Returns the computed square of the magnitude (useful for gravity)
     * @return square of the magnitude
     */
    public double magnitudeSQRD() {
        return (x*x + y*y + z*z);
    }

    /**
     * returns the computed vector magnitude
     * @return vector magnitude
     */
    public double magnitude() {
        return Math.sqrt( magnitudeSQRD() );
    }

    /**
     * create a unit vector of the coordinates
     */
    public Vector normalize() {
        // trap the zero magnitude vector and do nothing
        if (x!=0 || y!=0 || z!=0) {
            double m = magnitude();
            x /= m;
            y /= m;
            z /= m;
        }
        return this;
    }

    /**
     * multiply the vector by a scalar
     * @param s scalar multiplier or numerator
     */
    public Vector scalarMultiply(double s) {
        x *= s;
        y *= s;
        z *= s;
        return this;
    }

    /**
     * division the vector by a scalar
     * @param s scalar denominator
     */
    public Vector scalarDivide(double s) {
        x /= s;
        y /= s;
        z /= s;
        return this;
    }

    /**
     * add a scalar to the vector
     * @param s scalar increment
     */
    public Vector scalarAddition(double s) {
        x += s;
        y += s;
        z += s;
        return this;
    }

    /**
     * vector subtraction
     * @param v vector to subtract from host vector
     */
    public Vector subtract(Vector v) {
        x -= v.x;
        y -= v.y;
        z -= v.z;
        return this;
    }

    /**
     * vector addition
     * @param v vector to add to host vector
     */
    public Vector add(Vector v) {
        x += v.x;
        y += v.y;
        z += v.z;
        return this;
    }

    /**
     * return the computed vector cross product
     * @param v vector to cross with the host vector
     * @return vector cross product
     */
    public Vector crossProduct(Vector v) {
        return new Vector(
                (y*v.z - z*v.y),
                (z*v.x - x*v.z),
                (x*v.y - y*v.x)
        );
    }

    /**
     * return the computed vector dot product
     * @param v vector to dot with the host vector
     * @return dot product
     */
    public double dotProduct(Vector v) {
        return x*v.x + y*v.y + z*v.z;
    }

    /**
     * return the cosine of the angle between two vectors
     * @param v Vector to compute the cosine with host
     * @return cosine of the angle between two vectors
     */
    public double cos(Vector v) {
        double magnitude = Math.sqrt(magnitudeSQRD() * v.magnitudeSQRD());
        if (magnitude == 0) {
            return 1.0;
        } else {
            return dotProduct(v) / magnitude;
        }
    }

    /**
     * return the sine of the angle between two vectors
     * @param v Vector to compute the sine with host
     * @return sine of the angle between two vectors
     */
    public double sin(Vector v) {

        Vector c = crossProduct(v);
        double m = c.magnitude();

        return m / ( magnitude() * v.magnitude() );

        // or sqrt(1-cos(v)^2)
    }

    /**
     * rotate the vector about the given vector by given angle in degrees
     * @param theta angle in degrees to rotate
     * @param xa x-axis component of vector to rotate about
     * @param ya y-axis component of vector to rotate about
     * @param za z-axis component of vector to rotate about
     */
    public Vector rotate(double theta, double xa,double ya,double za) {
        theta *= RADIANS_PER_DEGREE;
        double cosTheta = Math.cos(theta);
        double sinTheta = Math.sin(theta);
        double x1, y1, z1;

        x1  = (cosTheta + (1 - cosTheta) * xa * xa)      * x;
        x1 += ((1 - cosTheta) * xa * ya - za * sinTheta) * y;
        x1 += ((1 - cosTheta) * xa * za + ya * sinTheta) * z;

        y1  = ((1 - cosTheta) * xa * ya + za * sinTheta) * x;
        y1 += (cosTheta + (1 - cosTheta) * ya * ya)      * y;
        y1 += ((1 - cosTheta) * ya * za - xa * sinTheta) * z;

        z1  = ((1 - cosTheta) * xa * za - ya * sinTheta) * x;
        z1 += ((1 - cosTheta) * ya * za + xa * sinTheta) * y;
        z1 += (cosTheta + (1 - cosTheta) * za * za)      * z;

        // copy the new coordinates to the vector components
        // but use a -y for counter-clockwise motion
        x = x1;
        y = y1;
        z = z1;
        return this;
    }

    /**
     * rotate the vector about the given vector by given angle in degrees
     * @param theta angle in degrees to rotate
     * @param v vector to rotate about
     */
    public Vector rotate(double theta, Vector v) {
        return rotate(theta, v.x, v.y, v.z);
    }

    public boolean inside(Rectangle2D bounds) {
        boolean inside;

//        if (z == 0) {
            inside = bounds.contains(new Point2D.Double(x,y));
//        }

        return inside;
    }

    public boolean outside(Rectangle2D bounds) {
        return ! inside(bounds);
    }
}
