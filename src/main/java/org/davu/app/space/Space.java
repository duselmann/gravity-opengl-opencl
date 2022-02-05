// Copyright (c) 2022 David Uselmann
package org.davu.app.space;

import static org.davu.app.space.Utils.*;
import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.opengl.GLCapabilities;
import org.davu.app.space.compute.GravityCL;
import org.davu.app.space.display.Compass;
import org.davu.app.space.display.DebugUtils;
import org.davu.app.space.display.Glasses3D;
import org.davu.app.space.display.Particles;
import org.davu.app.space.display.VaoVboManager;
import org.davu.app.space.display.ViewMatrix;
import org.davu.app.space.display.Window;
import org.lwjgl.opengl.GL;

import java.io.IOException;


public class Space implements Runnable {
	private long lastTime = System.nanoTime();

	private Window window;
	private Controls controls;
	private ViewMatrix view;
	private DebugUtils debug;
	private Glasses3D glasses3d;
	private VaoVboManager vertexManager;
    private GravityCL gravity;
	protected Particles particles;


	public Space(String scenario) {
        view = new ViewMatrix(800,600);
        glasses3d = new Glasses3D(view);
		particles = new ScenarioManager().build(scenario, glasses3d);
	}

	public void initGL() throws IOException  {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        window = new Window().init();
        view.updateAspect(window.getWidth(), window.getHeight());

        GLCapabilities caps = GL.createCapabilities(); // connects lwjgl to the native libraries
        if (!caps.OpenGL30) {
            throw new AssertionError("This app requires OpenGL 3.0.");
        }

        vertexManager = new VaoVboManager();
        vertexManager.register(particles);
        vertexManager.register(new Compass(glasses3d));
        vertexManager.init();
        controls  = new Controls(window, view, particles, glasses3d);

        debug     = new DebugUtils().init();
	}

	private void initCL() throws IOException {
		gravity = new GravityCL(window.getWindow(), particles);
		gravity.initCL(particles);
		controls.setGravity(gravity);
	}

	private void update() {
	    long thisTime = System.nanoTime();
	    float dt = (thisTime - lastTime) / 1E9f;
	    lastTime = thisTime;

	    view.updateView(dt);
	    controls.updateControls();
	}

	private void render() {
		window.clearGL();
		// order here does not matter for compass
		// init order matters
		// mvpm is same for particles either w or w/o compass init
		vertexManager.draw(view.getViewProjection());
	}

	private void loop() {
	    while (window.isOpen()) {
	        glfwPollEvents();
	        window.updateViewport();
	        update();
	        render();
	        window.swapBuffer();
	    }
	}

	@Override
	public void run() {
	    try {
	        initGL();
	        initCL();
	        initComputeThread();
	        loop();
	    } catch (Throwable t) {
	        t.printStackTrace();
	    } finally {
	    	gravity.cleanup();
	        debug.cleanup();
	    	vertexManager.cleanup();
	        window.cleanup();
	    	quiteFree("glfwTerminate", ()->glfwTerminate());
	    }
	}

	private void initComputeThread() {
		Thread compute = new Thread(() -> {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			while(window.isOpen()) {
				gravity.compute();
			}
		});
		compute.setDaemon(true);
		compute.start();
	}

	public static void main(String[] args) {
		String scenario = "Galaxies2b";
		if (args.length>0) {
			scenario = args[0];
		}
	    new Space(scenario).run();
	}

}
