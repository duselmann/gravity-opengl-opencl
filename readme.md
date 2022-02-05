# Universe Simulation on GPU
- A multi-million particle gravity simulation.
- The main program is org.davu.app.Space.main
- See each package.html for code details.
- Currently only builds with Windows natives. (see TODOs)
- Currently only works with Intel chipset. (see TODOs)
- davu is short for David Uselmann

#### Controls

##### Space.main
- ESC key    quit (also window x button)
- W/A/S/D    moves forward,left,back,right (i.e. FPS)
- R/F keys   (or shift+W/S) moves up,down 
- Q/E keys   rolls CCW/CC
- Ctrl+3     toggles 3D glasses mode.
- Ctrl+D+up/down arrows   adjusts 3D glasses separation.
- Ctrl+A+up/down arrows   adjusts particle alpha value.
- Ctrl+T+up/down arrows   adjusts time step.
- Left  mouse button   looks in that direction.
- Right mouse button   increase speed much faster.
- Letting go of all controls reduces speed.

##### opencl.advanced classes
- Click and drag to move the simulation in the window
- Mouse roller or touchpad two-finger scroll to zoom in/out

#### Purpose
- To make a million (or more) particle engine. Check.
- To learn how to run code on the GPU. Check.
- To learn how to link OpenCL to OpenGL. Check.
- To visualize gravity on a massively parallel scale. Check.
- To demonstrate the power of the the GPU in today's laptops. Check.
- To have fun. Check. Check. Check.

### Build Gravity jar

    mvn package

### Running Space main

    java -jar target/gravity.jar

### Running Other main

    java -cp target/gravity.jar org.davu.opencl.advanced.Saturn

### Override main in custom gravity.jar

    mvn package -Dclass=org.davu.opencl.advanced.Saturn
    java -jar target/gravity.jar


## Technologies
- LWJGL  - Java Gaming Library OpenGL/CL/AL (and others) link to native resources.
- OpenGL - Open Graphic Library to render simulations.
- OpenCL - Open Computing Library to compute Newtonian gravity.
- Java2D - Java AWT window and rendering is used for some demos.
- JOML   - Linear algebra math framework.

### TODOs

##### Architecture detecting pom.xml
- The current pom.xml is only configured for Windows natives.
- When I used this OS detecting pom, it pulled down all native jars.
- This cause conflicts where the wrong OS native was bound to the JVM.
- https://github.com/LWJGL/lwjgl3-demos/blob/main/pom.xml
- Looking at the pom and replace the OS native with yours
- <lwjgl.natives>natives-windows</lwjgl.natives>
- then it should work for your machine architecture.

##### OpenGL GPU Selection
- I could not find a GPU selector for OpenGL. It selects GPU 0.
- I matched the OpenGL GPU for OpenCL. 
- See org.davu.app.space.OpenCL.java line 57 for OpenCL GPU assignment.

##### Maybe change package.html
- Thinking I might rather they be readme.md files for github display.

##### Maybe add some sound
- The pom has openal jar dependency for when I get around to implementing some sound.

##### Special Note
- Newtonian gravity is normalizable. The distances and masses are relative magnitude numbers.
- This could be a natural range if Newton's Constant and actual kg, m, sec initial conditions are used.


### All Other main methods

    java -cp target/gravity.jar org.davu.opencl.simple.TestInfo
    java -cp target/gravity.jar org.davu.opencl.simple.Test1D
    java -cp target/gravity.jar org.davu.opencl.simple.Test1Dvector
    java -cp target/gravity.jar org.davu.opencl.simple.Test2D
    java -cp target/gravity.jar org.davu.opencl.simple.Test2D2D
    java -cp target/gravity.jar org.davu.opencl.simple.TestR2D2D
    
    java -cp target/gravity.jar org.davu.opencl.advanced.Explosive
    java -cp target/gravity.jar org.davu.opencl.advanced.Explosive2
    java -cp target/gravity.jar org.davu.opencl.advanced.Galaxies
    java -cp target/gravity.jar org.davu.opencl.advanced.Galaxies2
    java -cp target/gravity.jar org.davu.opencl.advanced.GalaxiesDarkMatter
    java -cp target/gravity.jar org.davu.opencl.advanced.GalaxiesDarkMatterVar
    java -cp target/gravity.jar org.davu.opencl.advanced.GalaxyArms
    java -cp target/gravity.jar org.davu.opencl.advanced.Globular3D
    java -cp target/gravity.jar org.davu.opencl.advanced.Gravity
    java -cp target/gravity.jar org.davu.opencl.advanced.Helix
    java -cp target/gravity.jar org.davu.opencl.advanced.Helix2
    java -cp target/gravity.jar org.davu.opencl.advanced.Orbitally
    java -cp target/gravity.jar org.davu.opencl.advanced.Saturn
    java -cp target/gravity.jar org.davu.opencl.advanced.SaturnClose
    java -cp target/gravity.jar org.davu.opencl.advanced.SaturnSpiral
    java -cp target/gravity.jar org.davu.opencl.advanced.SolarSystem
    java -cp target/gravity.jar org.davu.opencl.advanced.SolarSpeedy
    java -cp target/gravity.jar org.davu.opencl.advanced.Universe
    java -cp target/gravity.jar org.davu.opencl.advanced.Universe2

