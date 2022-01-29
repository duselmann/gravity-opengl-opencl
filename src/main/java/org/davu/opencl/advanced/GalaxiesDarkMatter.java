// Copyright (c) 2022 David Uselmann
package org.davu.opencl.advanced;

import org.dynamics.math.Vector;


public class GalaxiesDarkMatter extends Galaxies2 {

    public static void main(String[] args) throws Exception {
        Galaxies gravity = new GalaxiesDarkMatter();
        gravity.createWindow();
        gravity.setConditions();
        gravity.compute();
    }


    @Override
    public void setConditions() {
        super.setConditions();
        kernelFunc = "gravityDarkMatter";
        isDarkMatter = true;
        dmCenter = new float[] {0,0,0,coreMass*2*5};
        int dmRadius1 = 700;
        dmRadius3 = dmRadius1*dmRadius1*dmRadius1;

        speedBoost = 1f;
        coreMass  = 5e3f;
        coreDist = new Vector(500);
        coreVel  = new Vector(0,2);
        angle1   = new Vector(70);
        angle2   = new Vector(0,70);
    }

}
