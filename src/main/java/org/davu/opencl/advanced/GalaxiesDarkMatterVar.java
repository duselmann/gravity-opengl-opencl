// Copyright (c) 2022 David Uselmann
package org.davu.opencl.advanced;

import org.dynamics.math.Vector;


public class GalaxiesDarkMatterVar extends GalaxiesDarkMatter {

    public static void main(String[] args) throws Exception {
        Galaxies gravity = new GalaxiesDarkMatterVar();
        gravity.createWindow();
        gravity.setConditions();
        gravity.compute();
    }


    @Override
    public void setConditions() {
        super.setConditions();
        setLotsDarkMatter();
//        setLittleDarkMatter();
//        setMediumDarkMatter();

//        setAngleDarkMatter();
//        setAngle30(); // no dark matter
    }
	
    public void setLotsDarkMatter() {
        super.setConditions();
        dmCenter = new float[] {0,0,0,coreMass*10};
        int dmRadius1 = 800;
        dmRadius3 = dmRadius1*dmRadius1*dmRadius1;

        iters = 25;
        dt = 0.1f;
        speedBoost = 1.15f;
        coreVel  = new Vector(0,2);
    }
	
    public void setLittleDarkMatter() {
        dmCenter = new float[] {0,0,0,coreMass};
        int dmRadius1 = 600;
        dmRadius3 = dmRadius1*dmRadius1*dmRadius1;

        displayCanavs._3D = true;
        iters = 30;
        dt = 0.1f;
        speedBoost = 1.1f;
        coreVel  = new Vector(0,1.25);
        angle2   = new Vector(0,70);
    }
	
    public void setMediumDarkMatter() {
        dmCenter = new float[] {0,0,0,coreMass*5};
        int dmRadius1 = 600;
        dmRadius3 = dmRadius1*dmRadius1*dmRadius1;

        displayCanavs._3D = true;
        iters = 30;
        dt = 0.1f;
        speedBoost = 1.1f;
        coreVel  = new Vector(0,2);
        angle2   = new Vector(0,70);
    }
	
    public void setAngleDarkMatter() {
        dmCenter = new float[] {0,0,0,coreMass*7};
        int dmRadius1 = 600;
        dmRadius3 = dmRadius1*dmRadius1*dmRadius1;

        displayCanavs._3D = true;
        iters = 30;
        dt = 0.1f;
        speedBoost = 1.05f;
        coreVel  = new Vector(0,3);
        angle1   = new Vector(30);
        angle2   = new Vector(0,30);
    }
	
    public void setAngle30() {
        dmCenter = new float[] {0,0,0,0};
        int dmRadius1 = 600;
        dmRadius3 = dmRadius1*dmRadius1*dmRadius1;

        displayCanavs._3D = true;
        iters = 30;
        dt = 0.1f;
        speedBoost = 1.05f;
        coreVel  = new Vector(0,1.25);
        angle1   = new Vector(30);
        angle2   = new Vector(0,30);
    }
}
