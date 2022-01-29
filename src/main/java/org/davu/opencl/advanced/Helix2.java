// Copyright (c) 2022 David Uselmann
package org.davu.opencl.advanced;

import org.dynamics.math.Vector;


public class Helix2 extends Gravity {

    public static void main(String[] args) throws Exception {
        Helix2 gravity = new Helix2();
        gravity.createWindow();
        gravity.compute();
    }


    @Override
    public  void displayCallBack() {
        displayCanavs.setRadiusCallBack(v -> (v.x>100) ?50 :(float)((v.z+500)/400));
    }

    @Override
    public void initBodies(float[] bodies, float[] velocity) {
        displayCanavs._3D = true;
        dt = 1f;

        int[][] rings = {{40,150},{40,200},{40,250},{40,300},{40,350},  {40,500},{40,550},{40,600},{40,650},{40,700},{40,750},{40,800}, {40,950},};

        float saturnMass = 5e3f;

        int b = 0;
        for (int[] ring : rings) {
            for (int i=0; i<numBodies/rings.length; b++,i++) {
                float r,aa,x1,y1;
                r=aa=x1=y1=1;
                boolean generate = true;
                while (generate) {
                    r = (35 * (float)Math.random()) + ring[1];
                    aa = (float)(Math.random()*Math.PI*2);
                    x1 = (float)(Math.cos(aa));
                    y1 = (float)(Math.sin(aa));

                    bodies[b*4 + 0] = r*x1 +100;
                    bodies[b*4 + 1] = r*y1 +100;
                    bodies[b*4 + 2] = 0f;
                    bodies[b*4 + 3] = 0.0001f; //0.0001f; // mass
                    generate = checkNearPoint(100, b, bodies);
                }
                float vr = (float)Math.sqrt(saturnMass/r);
                float vxSign = (y1>=0) ?-1 : 1;
                float vySign = (x1>=0) ? 1 :-1;

                velocity[b*4 + 0] = vxSign * Math.abs(y1) * vr;
                velocity[b*4 + 1] = vySign * Math.abs(x1) * vr;
                velocity[b*4 + 2] = 0f;
                velocity[b*4 + 3] = 0f;

                // rotate the view by 70 degrees
                Vector pos = new Vector(bodies[b*4+0], bodies[b*4+1]);
                double angle = 70+ 10*Math.random();
                pos.rotate(angle, 1,0,0);
                bodies[b*4 + 0] = (float)pos.x;
                bodies[b*4 + 1] = (float)pos.y;
                bodies[b*4 + 2] = (float)pos.z;

                Vector vel = new Vector(velocity[b*4+0], velocity[b*4+1], velocity[b*4+2]);
                vel.rotate(angle, 1,0,0);
                velocity[b*4 + 0] = (float)vel.x;
                velocity[b*4 + 1] = (float)vel.y;
                velocity[b*4 + 2] = (float)vel.z;
            }
        }
        b =0;
        bodies[b*4 +0] = 100;
        bodies[b*4 +1] = 100;
        bodies[b*4 +2] = 0;
        bodies[b*4 +3] = saturnMass; // mass

        velocity[b*4 +0] =  0;
        velocity[b*4 +1] =  0;
        velocity[b*4 +2] =  0;
    }
}
