// Copyright (c) 2022 David Uselmann
package org.davu.opencl.advanced;

import java.util.Random;

import org.dynamics.math.Vector;


public class Explosive extends Gravity {

    public static void main(String[] args) throws Exception {
        Explosive gravity = new Explosive();
        gravity.createWindow();
        gravity.compute();
    }


    @Override
    public  void displayCallBack() {
        displayCanavs.setRadiusCallBack(v -> (v.x>100) ?50 :(float)((v.z+500)/400));
    }

    @Override
    public void initBodies(float[] bodies, float[] velocity) {
        iters    = 20;
        massBase = .1f;
        velBase  = 1.525f;
        dt       = 1f;
        displayCanavs._3D = true;
        float maxRadius = 25;
        Random rand = new Random();

        double maxMagnitude = 0;
        double minMagnitude = 100;

        for(int b=0; b<numBodies; b++) {
            boolean generate = true;
            Vector pos = new Vector();
            while (generate) {
                pos = new Vector(rand.nextGaussian(),rand.nextGaussian(),rand.nextGaussian())
                        .normalize()
                        .scalarMultiply(maxRadius*Math.random()+25);
                bodies[b*4 + 0] = (int)pos.x;
                bodies[b*4 + 1] = (int)pos.y;
                bodies[b*4 + 2] = (int)pos.z+500;
                generate = checkNearPoint(16, b, bodies);
            }
            bodies[b*4 + 3] = massBase*(float)Math.random(); // mass

            Vector normal = pos.copy().normalize();
            Vector vel   = new Vector((velBase*normal.x),(velBase*normal.y),(velBase*normal.z));
            double mag   = vel.magnitude();
            maxMagnitude = Math.max(mag, maxMagnitude);
            minMagnitude = Math.min(mag, minMagnitude);
            velocity[b*4 + 0] =  (float)(vel.x);
            velocity[b*4 + 1] =  (float)(vel.y);
            velocity[b*4 + 2] =  (float)(vel.z);
            velocity[b*4 + 3] = 0f;
        }
        System.out.println(maxMagnitude);
        System.out.println(minMagnitude);
    }
}
