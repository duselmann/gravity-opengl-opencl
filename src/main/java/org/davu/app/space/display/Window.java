// Copyright (c) 2022 David Uselmann
package org.davu.app.space.display;

import static org.davu.app.space.Utils.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAddress;

import java.nio.IntBuffer;

import org.joml.Math;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

public class Window {
	private int width = 800*2;
	private int height = 500*2;
	private int fbWidth = 800*2;
	private int fbHeight = 500*2;
	private long window;
	private GLFWKeyCallback keyCallback;
	private GLFWCursorPosCallback cpCallback;
	private GLFWMouseButtonCallback mbCallback;
	private GLFWFramebufferSizeCallback fbCallback;
	private GLFWWindowSizeCallback wsCallback;
	private float mouseX;
	private float mouseY;
    private boolean[] keyDown;
    private boolean rightMouseDown;
    private boolean leftMouseDown;

	public Window init() {
	    keyDown = new boolean[GLFW.GLFW_KEY_LAST];

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // if true then need to resize viewport
        glfwWindowHint(GLFW_SAMPLES, 1);

        window = glfwCreateWindow(width, height, "Universe Symulation", 0L, NULL);
        glfwSetWindowPos(window, 100, 100);
        if (window == NULL) {
            throw new AssertionError("Failed to create the GLFW window");
        }
	    glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // vsync, could be zero, need to find out what it is for vsync
        glfwShowWindow(window);

        IntBuffer framebufferSize = BufferUtils.createIntBuffer(2);
        nglfwGetFramebufferSize(window, memAddress(framebufferSize), memAddress(framebufferSize) + 4);
        fbWidth = framebufferSize.get(0);
        fbHeight = framebufferSize.get(1);

        framebufferCallback();
        resizeCallback();
        keyCallback();
        mouseButtonCallback();
        mouseCursorCallback();

        return this;
	}

	public void clearGL() {
	    glClearColor(0f, 0f, 0f, 1.0f);
	    glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
	}


	public void cleanup() {
        quiteFree("keyCallback",()->keyCallback.free());
        quiteFree("cpCallback", ()->cpCallback.free());
        quiteFree("mbCallback", ()->mbCallback.free());
        quiteFree("fbCallback", ()->fbCallback.free());
        quiteFree("wsCallback", ()->wsCallback.free());
        quiteFree("glfwDestroyWindow", ()->glfwDestroyWindow(window));
	}

	public boolean isOpen() {
		return !glfwWindowShouldClose(window);
	}

	public void swapBuffer() {
        glfwSwapBuffers(window);
	}

	public void updateViewport() {
        glViewport(0, 0, fbWidth, fbHeight);
	}

	private void resizeCallback() {
        glfwSetWindowSizeCallback(window, wsCallback = new GLFWWindowSizeCallback() {
            @Override
			public void invoke(long window, int newWidth, int newHeight) {
                if (newWidth > 0 && newHeight > 0 && (Window.this.width != newWidth || Window.this.height != newHeight)) {
                	width = newWidth;
                	height = newHeight;
                }
            }
        });
	}

	private void mouseButtonCallback() {
        glfwSetMouseButtonCallback(window, mbCallback = new GLFWMouseButtonCallback() {
            @Override
			public void invoke(long window, int button, int action, int mods) {
            	if (button == GLFW_MOUSE_BUTTON_RIGHT) {
					if (action == GLFW_PRESS)
                        rightMouseDown = true;
                    else if (action == GLFW_RELEASE)
                        rightMouseDown = false;
                }
            	if (button == GLFW_MOUSE_BUTTON_LEFT) {
					if (action == GLFW_PRESS)
                        leftMouseDown = true;
                    else if (action == GLFW_RELEASE)
                        leftMouseDown = false;
                }
            }
        });
	}

	private void mouseCursorCallback() {
        glfwSetCursor(window, glfwCreateStandardCursor(GLFW_CROSSHAIR_CURSOR));
        glfwSetCursorPosCallback(window, cpCallback = new GLFWCursorPosCallback() {
            @Override
			public void invoke(long window, double xpos, double ypos) {
                float normX = (float) ((xpos - width/2.0) / width * 2.0);
                float normY = (float) ((ypos - height/2.0) / height * 2.0);
                mouseX = Math.max(-width/2.0f, Math.min(width/2.0f, normX));
                mouseY = Math.max(-height/2.0f, Math.min(height/2.0f, normY));
            }
        });
	}

	private void keyCallback() {
        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_UNKNOWN) {
                    return;
                }
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    glfwSetWindowShouldClose(window, true);
                }
                if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                    keyDown[key] = true;
                } else {
                    keyDown[key] = false;
                }
            }
        });
	}

	private void framebufferCallback() {
        glfwSetFramebufferSizeCallback(window, fbCallback = new GLFWFramebufferSizeCallback() {
            @Override
			public void invoke(long window, int width, int height) {
				if (width > 0 && height > 0 && (fbWidth != width || fbHeight != height)) {
                    fbWidth = width;
                    fbHeight = height;
                    //cam.updateAspect(width, height);
                }
            }
        });
	}

	public boolean isKeyDown(int keyNum) {
		return keyDown[keyNum];
	}
	public boolean isRightMouseDown() {
		return rightMouseDown;
	}
	public boolean isLeftMouseDown() {
		return leftMouseDown;
	}
	public float getMouseX() {
		return mouseX;
	}
	public float getMouseY() {
		return mouseY;
	}
	public int getFbHeight() {
		return fbHeight;
	}
	public int getFbWidth() {
		return fbWidth;
	}
	public int getHeight() {
		return height;
	}
	public int getWidth() {
		return width;
	}
	public long getWindow() {
		return window;
	}
}
