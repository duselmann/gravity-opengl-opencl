// Copyright (c) 2022 David Uselmann
package org.davu.opencl.advanced;

public class Universe2 extends Gravity {


    public static void main(String[] args) throws Exception {
        Universe2 universe = new Universe2();
        universe.createWindow();
        universe.compute();
    }


    @Override
    public void displayCallBack() {
        displayCanavs.setRadiusCallBack(v -> (float)v.x);
    }


    @Override
    public void initBodies(float[] bodies, float[] velocity) {
        iters = 10;
        dt = 0.1f;
        displayCanavs._3D = false;

        for(int b=0; b<numBodies; b++) {
            float r = (400 * (float)Math.random()) + 100;
            float aa = (float)(Math.random()*Math.PI*2);
            bodies[b*4 + 0] = r*(float)(Math.cos(aa));
            bodies[b*4 + 1] = r*(float)(Math.sin(aa));
            bodies[b*4 + 2] = 0;
            bodies[b*4 + 3] = 1; // mass

            float velBase = 0;
            velocity[b*4 + 0] =  (float)Math.random()*velBase-velBase/2;
            velocity[b*4 + 1] =  (float)Math.random()*velBase-velBase/2;
            velocity[b*4 + 2] =  (float)Math.random()*velBase-velBase/2;
            velocity[b*4 + 3] = 0f;
        }
        int b =0;
        bodies[b*4 +0] = 0;
        bodies[b*4 +1] = 0;
        bodies[b*4 +2] = 0;
        bodies[b*4 +3] = -1000; // mass

        velocity[b*4 +0] =  0;
        velocity[b*4 +1] =  0;
        velocity[b*4 +2] =  0;
    }
}
