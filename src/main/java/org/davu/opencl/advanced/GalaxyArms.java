package org.davu.opencl.advanced;

public class GalaxyArms extends Galaxies {

    float pointMass =1f;


    public static void main(String[] args) throws Exception {
        GalaxyArms gravity = new GalaxyArms();
        gravity.createWindow();
        gravity.setConditions();
        gravity.compute();
    }


    @Override
    public  void displayCallBack() {
        displayCanavs.setRadiusCallBack(v -> (v.x>100) ?10 :(float)((v.z+500)/400));
    }

    @Override
    public void setConditions() {
        super.setConditions();
        setWithDarkMatter();
//        setNoDarkMatterSpeedDiff();
//        setNoDarkMatterUnstable();
    }
    public void setNoDarkMatterSpeedDiff() { // radial speed diff
        super.setConditions();
        isDarkMatter = false;
        pointMass = 0.1f;
        dmCenter = new float[] {1,0,0,(coreMass+numBodies*pointMass)*0};

        speedBoost = 1f;
        coreMass  = 5e3f;

        displayCanavs._3D = false;
        iters = 20;
    }
    public void setNoDarkMatterUnstable() { // radial speed diff
        super.setConditions();
        isDarkMatter = false;
        pointMass = 2f;
        dmCenter = new float[] {1,0,0,(coreMass+numBodies*pointMass)*0};

        speedBoost = 1f;
        coreMass  = 5e3f;

        displayCanavs._3D = false;
        iters = 20;
    }
    public void setWithDarkMatter() {
        super.setConditions();
        kernelFunc = "gravityDarkMatter";
        isDarkMatter = true;
        pointMass = 0.15f;
        dmCenter = new float[] {1,0,0,(coreMass+numBodies*pointMass)*10};
        int dmRadius1 = 600;
        dmRadius3 = dmRadius1*dmRadius1*dmRadius1;

        speedBoost = 1f;
        coreMass  = 5e3f;

        displayCanavs._3D = false;
        iters = 20;
    }

    @Override
    public void initBodies(float[] bodies, float[] velocity) {

//        displayCanavs._3D = true;

        for(int b=0; b<numBodies; b++) {
            float r,aa,x1,y1;
            r=aa=x1=y1=1;
            boolean generate = true;
            while (generate) {
                r = (500 * (float)Math.random()) + 100;
                aa = (float)(Math.random()*Math.PI*2);
                x1 = (float)(Math.cos(aa));
                y1 = (float)(Math.sin(aa));

                bodies[b*4 + 0] = r*x1;
                bodies[b*4 + 1] = r*y1*0.7f;
                bodies[b*4 + 2] = (float)(r*Math.random()/10);
                bodies[b*4 + 3] = pointMass; // mass
                generate = checkNearPoint(100, b, bodies);
            }

            double radius2 = bodies[b*4 + 0]*bodies[b*4 + 0]
                            + bodies[b*4 + 1]*bodies[b*4 + 1]
                            + bodies[b*4 + 2]*bodies[b*4 + 2];
            double radius = Math.sqrt(radius2);
            double radius3 = radius * radius2;
            // DARK MATTER
            double dmMass  = dmCenter[3];
            if (radius3<dmRadius3) {
                dmMass *= radius3/dmRadius3;
            }
            float vr = (float)Math.sqrt((coreMass+dmMass)/radius) * speedBoost;

            float vxSign = (y1>=0) ?-1 : 1;
            float vySign = (x1>=0) ? 1 :-1;

            velocity[b*4 + 0] = vxSign * Math.abs(y1) * vr;
            velocity[b*4 + 1] = vySign * Math.abs(x1) * vr;
            velocity[b*4 + 2] = 0f;
            velocity[b*4 + 3] = 0f;
        }
        int b =0;
        bodies[b*4 +0] = 0;
        bodies[b*4 +1] = 0;
        bodies[b*4 +2] = 0;
        bodies[b*4 +3] = coreMass; // mass

        velocity[b*4 +0] =  0;
        velocity[b*4 +1] =  0;
        velocity[b*4 +2] =  0;
    }
}
