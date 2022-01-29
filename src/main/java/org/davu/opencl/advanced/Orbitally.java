// Copyright (c) 2022 David Uselmann
package org.davu.opencl.advanced;

import org.dynamics.math.Vector;


public class Orbitally extends Gravity {

    public static void main(String[] args) throws Exception {
        Orbitally gravity = new Orbitally();
        gravity.createWindow();
        gravity.compute();
    }


    @Override
    public  void displayCallBack() {
        displayCanavs.setRadiusCallBack(v -> (v.x>100) ?50 :(float)((v.z+500)/400));
    }

    @Override
    public void initBodies(float[] bodies, float[] velocity) {
        iters = 10;
        massBase = 1;
        displayCanavs._3D = true;
        float maxRadius = 400;
        float density = massBase*numBodies/(maxRadius*maxRadius*maxRadius);

        for(int b=0; b<numBodies; b++) {
            float r,aa,x1,y1;
            r=aa=x1=y1=1;
            boolean generate = true;
            while (generate) {
                r = (maxRadius * (float)Math.random() + 100);
                aa = (float)(Math.random()*Math.PI*2);
                x1 = (float)(Math.cos(aa));
                y1 = (float)(Math.sin(aa));

                bodies[b*4 + 0] = r*x1;
                bodies[b*4 + 1] = r*y1;
                bodies[b*4 + 2] = 0f;
                bodies[b*4 + 3] = massBase; //0.0001f; // mass
                generate = checkNearPoint(16, b, bodies);
            }
            float innerMass = density*r*r*r;
            float vr = 1.1f*(float)Math.sqrt(innerMass/r);
            float vxSign = (y1>=0) ?-1 : 1;
            float vySign = (x1>=0) ? 1 :-1;

            velocity[b*4 + 0] = vxSign * Math.abs(y1) * vr;
            velocity[b*4 + 1] = vySign * Math.abs(x1) * vr;
            velocity[b*4 + 2] = 0f;
            velocity[b*4 + 3] = 0f;

            // rotate the view by 70 degrees
            Vector pos = new Vector(bodies[b*4+0], bodies[b*4+1], bodies[b*4+2]);
            double angle = 50+ 20*Math.random();
            pos.rotate(angle, 1,0,0);
            bodies[b*4 + 0] = (float)pos.x;
            bodies[b*4 + 1] = (float)pos.y;
            bodies[b*4 + 2] = (float)pos.z;

            Vector vel = new Vector(velocity[b*4+0], velocity[b*4+1], velocity[b*4+2]);
            vel.rotate(angle, 1,0,0);
            vel.scalarMultiply(0.9);
            velocity[b*4 + 0] = (float)vel.x;
            velocity[b*4 + 1] = (float)vel.y;
            velocity[b*4 + 2] = (float)vel.z;
        }

    }
}
