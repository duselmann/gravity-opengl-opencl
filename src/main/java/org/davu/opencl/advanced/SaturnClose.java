// Copyright (c) 2022 David Uselmann
package org.davu.opencl.advanced;

import org.dynamics.math.Vector;


public class SaturnClose extends Gravity {

    public static void main(String[] args) throws Exception {
        SaturnClose gravity = new SaturnClose();
        gravity.createWindow();
        gravity.compute();
    }


    @Override
    public  void displayCallBack() {
        displayCanavs.setRadiusCallBack(v -> {
            if (v.x>1000) {
                return 50f;
            } else if (v.x>1) {
                return (float)Math.min(15, Math.max(5, 10*((v.z+500)/500)));
            }
            return (float)((v.z+500)/500);
        });
    }


    @Override
    public void initBodies(float[] bodies, float[] velocity) {
        displayCanavs._3D = false;
        iters = 5;

        Vector saturnLocation = new Vector(-400, 0, 1000);
        float saturnMass = 5e4f;

        for(int b=0; b<numBodies; b++) {
            float r,aa,x1,y1;
            r=aa=x1=y1=1;
            boolean generate = true;
            while (generate) {
                r  = (2000 * (float)Math.random()) + 100;
                aa = (float)(Math.random()*Math.PI*2);
                x1 = (float)(Math.cos(aa));
                y1 = (float)(Math.sin(aa));
                bodies[b*4 + 0] = r*x1;
                bodies[b*4 + 1] = r*y1;
                bodies[b*4 + 2] = 0;
                bodies[b*4 + 3] = b==1?10f:0.0001f; // mass
                generate = checkNearPoint(900, b, bodies);
            }
            float vr = (float)Math.sqrt(saturnMass/r);
            float vxSign = (y1>=0) ?-1 : 1;
            float vySign = (x1>=0) ? 1 :-1;

            velocity[b*4 + 0] = vxSign * Math.abs(y1) * vr;
            velocity[b*4 + 1] = vySign * Math.abs(x1) * vr;
            velocity[b*4 + 2] = 0f;
            velocity[b*4 + 3] = 0f;

            // rotate the view by 80 degrees
            Vector pos = new Vector(bodies[b*4+0], bodies[b*4+1], bodies[b*4+2]);
            pos.rotate(80, 1,0,0);
            bodies[b*4 + 0] = (float)(pos.x+saturnLocation.x);
            bodies[b*4 + 1] = (float)(pos.y+saturnLocation.y);
            bodies[b*4 + 2] = (float)(pos.z+saturnLocation.z);

            Vector vel = new Vector(velocity[b*4+0], velocity[b*4+1], velocity[b*4+2]);
            vel.rotate(80, 1,0,0);
            velocity[b*4 + 0] = (float)vel.x;
            velocity[b*4 + 1] = (float)vel.y;
            velocity[b*4 + 2] = (float)vel.z;
        }
        // set Saturn
        int b =0;
        bodies[b*4 +0] = (float)saturnLocation.x;
        bodies[b*4 +1] = (float)saturnLocation.y;
        bodies[b*4 +2] = (float)saturnLocation.z;
        bodies[b*4 +3] = saturnMass; // mass

        velocity[b*4 +0] =  0;
        velocity[b*4 +1] =  0;
        velocity[b*4 +2] =  0;
    }

}
