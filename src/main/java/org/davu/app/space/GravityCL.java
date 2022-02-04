// Copyright (c) 2022 David Uselmann
package org.davu.app.space;

import static org.davu.app.space.Utils.*;
import static org.davu.opencl.utils.CLUtils.*;
import static org.lwjgl.opencl.CL10.*;
import static org.lwjgl.opencl.CL10GL.*;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;


public class GravityCL {
	private static final Logger log = LogManager.getLogger(GravityCL.class);

    // CL program handles
	protected  OpenCL openCl;
	private  long queue;
    private  long kernel;
    private  long glWindow;

    // CL data handles
    protected  long positionMem;
    protected  long velocityMem;

    // CL data handle arrays
    protected  long[] positions;
    protected  long[] velocities;

    protected  FloatBuffer velBuffer;

    // init properties - Particles
    protected  int numBodies;

    protected float dt = 0.1f; // gravity iterative time step

    // running status
    protected  boolean running = true;

	private Particles particles;
	private int glParticles;

	private IntBuffer clResult;

    public GravityCL(long glWindow, Particles particles) {
		String filePath = "cl/space-gravity.txt";
		String function = "gravityDarkMatter";
		openCl = new OpenCL(filePath, function);

		this.particles = particles;
    	setNumBodies(particles.getParticleCount());
    	this.glParticles = particles.getVertexBuffer();
    	this.glWindow = glWindow;

    	clResult = BufferUtils.createIntBuffer(1);
	}

    public void setNumBodies(int numberOfBodies) {
        numBodies = numberOfBodies;
    }

    public void initCL(Particles particles) throws IOException {

		try {
			openCl.init(glWindow);
	        checkGlSharing();
			kernel = openCl.getKernel();
			queue  = openCl.getQueue();

	        if (numBodies % openCl.getGpuThreads() != 0) {
	            log.warn(
	            	"Particle count, {}, is optimally a multiple of GPU Workgroup/Thread size, {}."
	            		,numBodies, openCl.getGpuThreads() );
	        }

	        bindCL(particles); // Particles manages the GL binding

        } catch (Exception e) {
        	log.error("failed to init CL", e);
        	cleanup();
        }
    }

	private void checkGlSharing() {
    	String extensions = openCl.getExtensions();
    	if (extensions == null || !extensions.contains("cl_khr_gl_sharing")) {
    		throw new RuntimeException("Missing CL-GL Sharing Extensions");
    	}
	}

	private void bindCL(Particles particles) {
    	log.info("CL Queue data structures");
    	positionMem = clCreateFromGLBuffer(openCl.getContext(), CL_MEM_READ_WRITE, glParticles, clResult);
    	isSuccess(clResult);

    	velBuffer = particles.getVelocities();
    	velocityMem = writeBuffer(queue, openCl.getContext(), CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, velBuffer);
	}

	public void compute() { // TODO start with passing in 0.1f, GL dt will be too small

        log.info("iteration - commence");
        if (running) {
            int result = 0;
//        	result = clEnqueueAcquireGLObjects(queue, positionMem, null, null);
//        	isSuccess(result);
            int arg = 0;
            pushKernelStackPointer(kernel, arg++, positionMem);  // positions GL handle arg
            pushKernelStackPointer(kernel, arg++, velocityMem);  // velocities CL handle arg
            result = clSetKernelArg1i(kernel, arg++, particles.getMassiveCount()); //
            isSuccess(result);
            result = clSetKernelArg1f(kernel, arg++, dt);        // delta time arg
            isSuccess(result);
            // darkMatter position and mass arg
            Particles p = particles;
            result = clSetKernelArg4f(kernel, arg++, p.dmCenter.x,p.dmCenter.y,p.dmCenter.z,p.dmVolume);
            isSuccess(result);
            result = clSetKernelArg1f(kernel, arg++, particles.dmMass);    // darkMatter radius arg
            isSuccess(result);

            // NOTE: Send a signal to execute the kernel with return buffer size
            //       One dimensional array of particles and velocities.
            int globalCols = numBodies;
            int globalRows = 1;
            int localCols = 256;//Math.max(1, (int) (numBodies / openCl.getGpuThreads()));
            int localRows = 1;
            pushKernelToQueue(queue, kernel, globalCols, globalRows, localCols, localRows);

//        	result = clEnqueueReleaseGLObjects(queue, positionMem, null, null);
//        	isSuccess(result);
            waitForQueue(queue); // NOTE: This waits for the computer and buffer transfer to finish

        }
    }

    /**
     * Clean up all OpenCL allocations: CL_MEM, Queue, Context, Kernels, and Programs.
     */
    public void cleanup() {
        log.info("cleanup - GravityCL");
    	stop();
        quiteFree("cleanup - wait for queue", ()->waitForQueue(queue));

        // TODO move velocities to Particles
        quiteFree("cleanup - memory", ()->clReleaseMemObjects(positionMem, velocityMem));

        openCl.cleanup();
    }

    /**
     * Implementation of what this example needs to stop.
     * the running state controls a while loop to continue calculating and rendering.
     */
    public  void stop() {
        running = false;
    }

    public float getDt() {
		return dt;
	}
    public void setDt(float dt) {
		this.dt = dt;
	}
}
