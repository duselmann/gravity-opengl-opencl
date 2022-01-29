// Copyright (c) 2022 David Uselmann
package org.davu.opencl.advanced;

public class SaturnSpiral extends Gravity {

    public static void main(String[] args) throws Exception {
        SaturnSpiral gravity = new SaturnSpiral();
        gravity.createWindow();
        gravity.compute();
    }


    @Override
    public  void displayCallBack() {
        displayCanavs.setRadiusCallBack(v -> (v.x>100) ?50f :1f);
    }


    @Override
    public void initBodies(float[] bodies, float[] velocity) {
        iters = 5;
        float saturnMass = 5e3f;

        for(int b=0; b<numBodies; b++) {
            float r,x1,y1;
            r=x1=y1=1;
            boolean generate = true;
            while (generate) {
                r = (1000 * (float)Math.random()) + 100;
                x1 = Math.random()<0.5 ?0 :(Math.random()<0.5 ?-1 :1);
                y1 = x1!=0             ?0 :(Math.random()<0.5 ?-1 :1);
                bodies[b*4 + 0] = r*x1;
                bodies[b*4 + 1] = r*y1;
                bodies[b*4 + 2] = 0f;
                bodies[b*4 + 3] = 0f; //0.0001f; // mass
                generate = checkNearPoint(1, b, bodies);
            }
            float vr = (float)Math.sqrt(saturnMass/r);
            float vxSign = (y1>=0) ?-1 : 1;
            float vySign = (x1>=0) ? 1 :-1;

            velocity[b*4 + 0] = vxSign * Math.abs(y1) * vr;
            velocity[b*4 + 1] = vySign * Math.abs(x1) * vr;
            velocity[b*4 + 2] =  0f;
            velocity[b*4 + 3] = 0f;
        }
        // set Saturn
        int b =0;
        bodies[b*4 +0] = 0;
        bodies[b*4 +1] = 0;
        bodies[b*4 +2] = 0;
        bodies[b*4 +3] = saturnMass; // mass

        velocity[b*4 +0] =  0;
        velocity[b*4 +1] =  0;
        velocity[b*4 +2] =  0;
    }
}
