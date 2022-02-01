// Copyright (c) 2022 David Uselmann
package org.davu.app.space;

import static org.lwjgl.opengl.GL30.*;
import static org.davu.app.space.Utils.*;
import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.opengl.GLCapabilities;
import org.davu.app.space.scenarios.*;
import org.lwjgl.opengl.GL;

import java.io.IOException;


public class Space implements Runnable {
	private long lastTime = System.nanoTime();

	private Guides guides;
	private Window window;
	private Controls controls;
	private Particles particles;
	private ViewMatrix view;
	private DebugUtils debug;
	private Glasses3D glasses3d;

    GravityCL gravity;

	public void initGL() throws IOException  {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        window = new Window().init();
        view = new ViewMatrix(window.getWidth(), window.getHeight());
        glasses3d = new Glasses3D(view);
        guides = new Guides(glasses3d);

        GLCapabilities caps = GL.createCapabilities(); // connects lwjgl to the native libraries
        if (!caps.OpenGL30) {
            throw new AssertionError("This app requires OpenGL 3.0.");
        }

        // TODO factory pattern like mechanism to select the initial conditions
        particles = new Galaxies2b(glasses3d).init();
        controls = new Controls(window, view, particles, glasses3d);

        debug = new DebugUtils().init();

	    glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
	    glEnableVertexAttribArray(0);
	    // Note that this is allowed, the call to glVertexAttribPointer registered VBO
	    // as the currently bound vertex buffer object so afterwards we can safely unbind
	    glBindBuffer(GL_ARRAY_BUFFER, 0);
	    // Unbind VAO (it's always a good thing to unbind any buffer/array to prevent strange bugs),
	    // remember: do NOT unbind the EBO, keep it bound to this VAO
	    glBindVertexArray(0);
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
	    glClearColor(0f, 0f, 0f, 1.0f);
	    glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
	    particles.draw(view.getViewProjection());
        guides.draw(view.getProjection(), view.getViewMatrix());
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
	        particles.cleanup();
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
	    new Space().run();
	}

}
