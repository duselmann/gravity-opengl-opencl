// Copyright (c) 2022 David Uselmann
package org.davu.opencl.advanced;

public class Universe extends Gravity {


    public static void main(String[] args) throws Exception {
        Universe universe = new Universe();
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
            float r = (600 * (float)Math.random()) + 100;
            float aa = (float)(Math.random()*Math.PI*2);
            bodies[b*4 + 0] = r*(float)(Math.cos(aa))+400;
            bodies[b*4 + 1] = r*(float)(Math.sin(aa))+250;
            bodies[b*4 + 2] = 0;
            bodies[b*4 + 3] = r/2500; // mass

            float velBase = 0;
            velocity[b*4 + 0] =  (float)Math.random()*velBase-velBase/2;
            velocity[b*4 + 1] =  (float)Math.random()*velBase-velBase/2;
            velocity[b*4 + 2] =  (float)Math.random()*velBase-velBase/2;
            velocity[b*4 + 3] = 0f;
        }
    }
}
