// Copyright (c) 2022 David Uselmann
package org.davu.opencl.advanced;

public class SolarSpeedy extends SolarSystem {
    public static void main(String[] args) throws Exception {
        SolarSpeedy gravity = new SolarSpeedy();
        gravity.createWindow();
        gravity.compute();
    }
    @Override
    public void initBodies(float[] bodies, float[] velocity) {
        super.initBodies(bodies, velocity);
        displayCanavs._3D = true;
        dt = 0.00001f;
        iters = 100;
    }
}
