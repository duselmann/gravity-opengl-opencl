// Copyright (c) 2022 David Uselmann
package org.dynamics.math;

public class Distance {
    private final Vector vector;
    private Vector unitVector;
    private Vector modified;
    private Double magnitude;
    private Double magnitudeSQRD;
    private Double magnitudeMOD;

    public Distance(Vector target, Vector other) {
        vector = vector(target, other);
    }

    public Vector vector() {
        return new Vector(vector);
    }

    public Vector unitVector() {
        if (unitVector != null) {
            return new Vector(unitVector);
        }
        return normalize();
    }

    private Vector normalize() {
        Vector uv = vector();
        // trap the zero magnitude vector and do nothing
        if (uv.x!=0 || uv.y!=0 || uv.z!=0) {
            double m = magnitude();
            uv.x /= m;
            uv.y /= m;
            uv.z /= m;
        }
        return unitVector = uv;
    }

    public double magnitude() {
        if (magnitude != null) {
            return magnitude;
        }
        magnitudeSQRD = magnitudeSQRD();
        return magnitude = Math.sqrt(magnitudeSQRD);
    }

    public double magnitudeSQRD() {
        if (magnitudeSQRD != null) {
            return magnitudeSQRD;
        }
        return magnitudeSQRD = vector.magnitudeSQRD();
    }

    public Distance modified(Vector modified) {
        this.modified = modified;
        return this;
    }
    public Vector modified() {
        return modified;
    }

    public Distance magnitudeMOD(Double magnitudeMOD) {
        this.magnitudeMOD = magnitudeMOD;
        return this;
    }
    public Double magnitudeMOD() {
        return magnitudeMOD;
    }

    public static Vector vector(Vector target, Vector other) {
        Vector distance = new Vector(other);
        distance.subtract(target);
        return distance;
    }

    public static Vector unitVector(Vector target, Vector other) {
        return vector(target, other).normalize();
    }

    public static double magnitude(Vector target, Vector other) {
        return vector(target, other).magnitude();
    }

//    public static double magnitudeSQRD(Vector target, Vector other) {
//        return vector(target, other).magnitudeSQRD();
//    }
}
