// Copyright (c) 2022 David Uselmann
package org.davu.opencl.advanced;

import org.dynamics.math.Vector;


public class SolarSystem extends Gravity {

    float[] bodies;

    public static void main(String[] args) throws Exception {
        SolarSystem gravity = new SolarSystem();
        gravity.createWindow();
        gravity.compute();
    }


    @Override
    public void displayCallBack() {
        displayCanavs.setRadiusCallBack(v -> (float)(v.x*(v.z+500)/400));
    }

    @Override
    public void initBodies(float[] bodies, float[] velocity) {
        this.bodies = bodies;
        displayCanavs._3D = true;
        dt = 0.00001f;
        iters = 10;

        float sunMass     = 1.9891e30f;
        float mercuryMass = 3.3022e23f;
        float venusMass   = 4.8685e24f;
        float earthMass   = 5.9736e24f;
        float marsMass    = 6.4185e23f;
        float jupiterMass = 1.8986e27f;
        float saturnMass  = 5.6846e26f;
        float uranusMass  = 8.6810e25f;
        float neptunMass  = 1.0243e26f;

//        float sunDist     = 5e5f;
        float mercuryDist = 5.791e7f;
        float venusDist   = 1.0821e8f;
        float earthDist   = 1.496e8f;
        float marsDist    = 2.2792e8f;
        float jupiterDist = 7.7857e8f;
        float saturnDist  = 1.43353e9f;
        float uranusDist  = 2.87246e9f;
        float neptunDist  = 4.49506e9f;

        float[][] system = new float[][] {
            {mercuryDist, mercuryMass},
            {venusDist,   venusMass},
            {earthDist,   earthMass},
            {marsDist,    marsMass},
            {jupiterDist, jupiterMass},
            {saturnDist,  saturnMass},
            {uranusDist,  uranusMass},
            {neptunDist,  neptunMass},
        };

        displayCanavs.scaleDistance=5000/neptunDist;
        displayCanavs.scaleRadius=2.5e-27f;

        for(int b=0; b<numBodies; b++) {
            float r = (float)((jupiterDist)*Math.random() + neptunDist*1.2);
            if (Math.random()<0.5) {
                r = (float)((jupiterDist-marsDist)*0.5*Math.random() + marsDist*1.2);
            }
            Vector[] body = bodyOrbitPlacement(sunMass, r);
            assignBody(body, b, bodies, velocity);
        }
        int b =0;
        bodies[b*4 +3] = sunMass; // mass
        bodies[b*4 +0] = 0;
        bodies[b*4 +1] = 0;
        bodies[b*4 +2] = 0;
        velocity[b*4 +0] =  0;
        velocity[b*4 +1] =  0;
        velocity[b*4 +2] =  0;

        Vector[] saturn = null;
        for (int p=0; p<system.length;p++) {
            Vector[] body = bodyOrbitPlacement(sunMass, system[p][0]);
            assignBody(body, ++b, bodies, velocity);
            bodies[b*4 + 3] = system[p][1];
            if (p==5) {
                saturn = body;
            }
        }

        for (int p=0; p<100;p++) {
            float r = (float)(2000*(80000-67000)*Math.random() + 67000*10);
            Vector[] body = bodyOrbitPlacement(saturnMass, r);
            body[0].rotate(-27, 1,0,0);
            body[1].rotate(-27, 1,0,0);
            body[0].add(saturn[0]);
            body[1].add(saturn[1]);
            assignBody(body, ++b, bodies, velocity);
            bodies[b*4 + 3] = 1;
        }
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
