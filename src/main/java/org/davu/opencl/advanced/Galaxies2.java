// Copyright (c) 2022 David Uselmann
package org.davu.opencl.advanced;

import org.dynamics.math.Vector;


public class Galaxies2 extends Galaxies {

    public static void main(String[] args) throws Exception {
        Galaxies gravity = new Galaxies2();
        gravity.createWindow();
        gravity.setConditions();
        gravity.compute();
    }

    @Override
    public void setConditions() {
        super.setConditions();
        displayCanavs._3D = true;
        iters = 99;
        dt = 0.1f;
        coreVel  = new Vector(0,1.2);
        angle2   = new Vector(0,70);
        speedBoost = 1.f;
    }


    @Override
    public void initBodies(float[] bodies, float[] velocity) {
        for(int b=0; b<numBodies; b++) {
            int leftRight = Math.random()<0.5 ?-1 :1;
            float r,aa,x1,y1;
            r=aa=x1=y1=1;
            boolean generate = true;
            while (generate) {
                r = (200 * (float)Math.random()) + 150;
                aa = (float)(Math.random()*Math.PI*2);
                x1 = (float)(Math.cos(aa));
                y1 = (float)(Math.sin(aa));

                bodies[b*4 + 0] = r*x1;
                bodies[b*4 + 1] = r*y1;
                bodies[b*4 + 2] =   0f   ;
                bodies[b*4 + 3] = 0.5f; //0.0001f; // mass
                generate = checkNearPoint(100, b, bodies);
            }
            float vr = speedBoost*(float)Math.sqrt(coreMass/r);
            float vxSign = (y1>=0) ?-1 : 1;
            float vySign = (x1>=0) ? 1 :-1;

            velocity[b*4+0] = vxSign * Math.abs(y1) * vr;
            velocity[b*4+1] = vySign * Math.abs(x1) * vr;
            velocity[b*4+2] = 0f;
            velocity[b*4+3] = 0f;


            Vector angleV = leftRight < 0 ?angle1 :angle2;
            Vector angles = new Vector();
            angles.x = angleV.x + 10*Math.random();
            angles.y = angleV.y + 10*Math.random();
            angles.z = angleV.z + 10*Math.random();

            // rotate the view by 70 degrees
            Vector pos = new Vector(bodies[b*4+0], bodies[b*4+1]);
            if (angleV.x != 0) {
                pos.rotate(angles.x, 1,0,0);
            }
            if (angleV.y != 0) {
                pos.rotate(angles.y, 0,1,0);
            }
            bodies[b*4+0] = (float)pos.x+leftRight*(float)coreDist.x;
            bodies[b*4+1] = (float)pos.y+leftRight*(float)coreDist.y;
            bodies[b*4+2] = (float)pos.z+leftRight*(float)coreDist.z;
            Vector vel = new Vector(velocity[b*4+0], velocity[b*4+1], velocity[b*4+2]);
            if (angleV.x != 0) {
                vel.rotate(angles.x, 1,0,0);
            }
            if (angleV.y != 0) {
                vel.rotate(angles.y, 0,1,0);
            }
            velocity[b*4+0] = -leftRight*(float)vel.x+leftRight*(float)coreVel.x;
            velocity[b*4+1] = -leftRight*(float)vel.y+leftRight*(float)coreVel.y;
            velocity[b*4+2] = -leftRight*(float)vel.z+leftRight*(float)coreVel.z;
        }
        int b =0;
        bodies[b*4+0] = -(float)coreDist.x;
        bodies[b*4+1] = -(float)coreDist.y;
        bodies[b*4+2] = -(float)coreDist.z;
        bodies[b*4+3] = coreMass; // mass
        velocity[b*4+0] = -(float)coreVel.x;
        velocity[b*4+1] = -(float)coreVel.y;
        velocity[b*4+2] = -(float)coreVel.z;
        b =1;
        bodies[b*4+0] = (float)coreDist.x;
        bodies[b*4+1] = (float)coreDist.y;
        bodies[b*4+2] = (float)coreDist.z;
        bodies[b*4+3] = coreMass; // mass
        velocity[b*4+0] = (float)coreVel.x;
        velocity[b*4+1] = (float)coreVel.y;
        velocity[b*4+2] = (float)coreVel.z;
    }
}
