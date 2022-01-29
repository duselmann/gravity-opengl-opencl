# Universe Simulation
- A multi-million particle gravity simulation.
- The main program is org.davu.app.Space.main
- See each package.html for code details.
- Currently only builds with Windows natives. (see TODOs)
- Currently only works with Intel chipset. (see TODOs)
- davu is short for David Uselmann

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
S
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

##### Factory for initial condition instances.
- org.davu.app.Space#44 has a new instance of the scenario in use.
- I need to construct a factory to control that scenario instantiated.
- For now, the instance name can be replaced with any subclass of Particles.

##### Maybe change package.html
- Thinking I might rather they be readme.md files.

##### Maybe add some sound
- The pom has openal jar dependency for when I get around to implementing some sound.

#### Special Note
- Newtonian gravity is normalizable. The distances and masses are relative.
- This could be a natural range if Newton's Constant and actual kg, m, sec initial conditions are used.
