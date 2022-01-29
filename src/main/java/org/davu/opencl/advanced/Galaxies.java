// Copyright (c) 2022 David Uselmann
package org.davu.opencl.advanced;

import org.dynamics.math.Vector;


public class Galaxies extends Gravity {

    float speedBoost = .9f; // orbit core mass speed boost
    float coreMass  = 5e3f;
    Vector coreDist = new Vector(500);
    Vector coreVel  = new Vector(0,1);
    Vector angle1   = new Vector(70);
    Vector angle2   = new Vector(70);


    public static void main(String[] args) throws Exception {
    	System.out.println();
    	System.out.println("Left mouse drag moves simulation on screen.");
    	System.out.println("Roller mouse zooms in and out.");
        System.out.println("Close Window to exit application.");
    	System.out.println();
        Galaxies gravity = new Galaxies();
        gravity.createWindow();
        gravity.setConditions();
        gravity.compute();
    }


    public void setConditions() {
        displayCanavs._3D = true;
        iters = 30;
        dt = 0.1f;
        coreVel  = new Vector(0,1.15);
    }


    @Override
    public  void displayCallBack() {
        displayCanavs.setRadiusCallBack(v -> (v.x>100) ?10 :(float)((v.z+500)/400));
    }

    @Override
    public void initBodies(float[] bodies, float[] velocity) {

        for(int b=0; b<numBodies; b++) {
            int leftRight = Math.random()<0.5 ?-1 :1;
            float r,aa,x1,y1;
            r=aa=x1=y1=1;
            boolean generate = true;
            while (generate) {
                r = (300 * (float)Math.random()) + 100;
                aa = (float)(Math.random()*Math.PI*2);
                x1 = (float)(Math.cos(aa));
                y1 = (float)(Math.sin(aa));

                bodies[b*4 + 0] = r*x1+leftRight*(float)coreDist.x;
                bodies[b*4 + 1] = r*y1+leftRight*(float)coreDist.y;
                bodies[b*4 + 2] =      leftRight*(float)coreDist.z;
                bodies[b*4 + 3] = 0.0001f; // mass
                generate = checkNearPoint(100, b, bodies);
            }
            float vr = (float)Math.sqrt(coreMass/r);
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
            bodies[b*4+0] = (float)pos.x;
            bodies[b*4+1] = (float)pos.y;
            bodies[b*4+2] = (float)pos.z;
            Vector vel = new Vector(velocity[b*4+0], velocity[b*4+1], velocity[b*4+2]);
            if (angleV.x != 0) {
                vel.rotate(angles.x, 1,0,0);
            }
            if (angleV.y != 0) {
                vel.rotate(angles.y, 0,1,0);
            }
            velocity[b*4+0] = (float)vel.x+leftRight*(float)coreVel.x;
            velocity[b*4+1] = (float)vel.y+leftRight*(float)coreVel.y;
            velocity[b*4+2] = (float)vel.z+leftRight*(float)coreVel.z;
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

    protected void assignBody(Vector[] body, int b, float[] bodies, float[] velocity) {
        Vector pos = body[0];
        Vector vel = body[1];
        bodies[b*4 + 0] = (float)pos.x;
        bodies[b*4 + 1] = (float)pos.y;
        bodies[b*4 + 2] = (float)pos.z;
        bodies[b*4 + 3] = 1;
        velocity[b*4 + 0] = (float)vel.x;
        velocity[b*4 + 1] = (float)vel.y;
        velocity[b*4 + 2] = (float)vel.z;
    }

    protected Vector[] bodyOrbitPlacement(float parent, float r) {
        float aa = (float)(Math.random()*Math.PI*2);
        float x1 = (float)(Math.cos(aa));
        float y1 = (float)(Math.sin(aa));
        Vector body = new Vector(r*x1, r*y1, 0);
        // rotate the view by 70 degrees
        double angle = 70+ 10*Math.random();
        body.rotate(angle, 1,0,0);

        float vr = -(float)Math.sqrt(parent/r);
        float vxSign = (y1>=0) ?-1 : 1;
        float vySign = (x1>=0) ? 1 :-1;
        Vector vel = new Vector(vxSign * Math.abs(y1) * vr, vySign * Math.abs(x1) * vr);
        vel.rotate(angle, 1,0,0);
        return new Vector[] {body,vel};
    }
}
