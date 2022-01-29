// Copyright (c) 2022 David Uselmann
package org.davu.opencl.advanced;

public class Globular3D extends Gravity {

    public static void main(String[] args) throws Exception {
        Globular3D globular = new Globular3D();
        globular.createWindow();
        globular.displayCanavs._3D = true;
        globular.iters = 5;
        globular.numBodies = 128;
        globular.massBase = 100;

        globular.compute();
    }

}
