// Copyright (c) 2022 David Uselmann
package org.davu.app.space;

import static org.lwjgl.glfw.GLFW.*;

import org.davu.app.space.compute.GravityCL;
import org.davu.app.space.display.Glasses3D;
import org.davu.app.space.display.Particles;
import org.davu.app.space.display.ViewMatrix;
import org.davu.app.space.display.Window;

public class Controls {

	private Window window;
	private ViewMatrix camera;
	private Particles particles;
	private GravityCL gravity;
	private Glasses3D glasses3d;
	private long wait3D;

	public Controls(Window window, ViewMatrix camera, Particles particles, Glasses3D glasses) {
		this.camera = camera;
		this.window = window;
		this.particles = particles;
		this.glasses3d = glasses;

        System.out.println("ESC key    quit (also window x button)");
		System.out.println("W/A/S/D    moves forward,left,back,right (i.e. FPS)");
        System.out.println("R/F keys   (or shift+W/S) moves up,down ");
        System.out.println("Q/E keys   rolls CCW/CC");
        System.out.println("Ctrl+3     toggles 3D glasses mode.");
        System.out.println("Ctrl+D+up/down arrows   adjusts 3D glasses separation.");
        System.out.println("Ctrl+A+up/down arrows   adjusts particle alpha value.");
        System.out.println("Ctrl+T+up/down arrows   adjusts time step.");
        System.out.println("Left  mouse button   looks in that direction.");
        System.out.println("Right mouse button   increase speed much faster.");
        System.out.println("Letting go of all controls reduces speed.");
	}

	public void setGravity(GravityCL gravity) {
		this.gravity = gravity;
	}

    public void updateControls() {
    	camera.beginAcceleration();
        float rotation = 0.0f;

        float multiplier = 1f;
    	if (window.isRightMouseDown()) {
        	multiplier = 4.0f;
    	}

    	if ( !window.isKeyDown(GLFW_KEY_LEFT_CONTROL) ) {

	    	if (window.isKeyDown(GLFW_KEY_LEFT_SHIFT)) {
	            if (window.isKeyDown(GLFW_KEY_W)) {
	            	camera.up(-1.0f*multiplier);
	            }
	            if (window.isKeyDown(GLFW_KEY_S)) {
	            	camera.up(+1.0f*multiplier);
	            }
	    	} else {
		        if (window.isKeyDown(GLFW_KEY_W)) {
		        	camera.forward(+1.0f*multiplier);
		        }
		        if (window.isKeyDown(GLFW_KEY_S)) {
		        	camera.forward(-1.0f*multiplier);
		        }
	    	}

	        if (window.isKeyDown(GLFW_KEY_D)) {
	        	camera.right(-1.0f*multiplier);
	        }
	        if (window.isKeyDown(GLFW_KEY_A)) {
	        	camera.right(+1.0f*multiplier);
	        }
	        if (window.isKeyDown(GLFW_KEY_R)) {
	        	camera.up(-1.0f*multiplier);
	        }
	        if (window.isKeyDown(GLFW_KEY_F)) {
	        	camera.up(+1.0f*multiplier);
	        }

	        if (window.isKeyDown(GLFW_KEY_Q)) {
	        	rotation = +1.0f;
	        }
	        if (window.isKeyDown(GLFW_KEY_E)) {
	        	rotation = -1.0f;
	        }
    	}

        if (window.isLeftMouseDown()) {
        	camera.changeDirection(window.getMouseX(), window.getMouseY(), rotation);
        } else {
        	camera.changeDirection(0, 0, rotation);
        }

        if (window.isKeyDown(GLFW_KEY_LEFT_CONTROL)) {
            if (window.isKeyDown(GLFW_KEY_3)) {
            	if (System.currentTimeMillis() > wait3D) {
            		glasses3d.setGlasses();
            		wait3D = System.currentTimeMillis() + 1000;
            	}
            }

            if (window.isKeyDown(GLFW_KEY_A)) {
            	if (window.isKeyDown(GLFW_KEY_UP)) {
            		float alpha = particles.getAlpha() * 1.005f;
            		particles.setAlpha( Math.min(alpha, 1.0f) );
            	} else if (window.isKeyDown(GLFW_KEY_DOWN)) {
            		float alpha = particles.getAlpha() * 0.995f;
            		particles.setAlpha( Math.max(alpha, 0.005f) );
            	}
            }
            if (window.isKeyDown(GLFW_KEY_D)) {
            	if (window.isKeyDown(GLFW_KEY_UP)) {
            		float sep = glasses3d.getSeparation3D() * 1.005f;
            		glasses3d.setSeparation3D( Math.min(sep, 100.0f) );
            	} else if (window.isKeyDown(GLFW_KEY_DOWN)) {
            		float sep = glasses3d.getSeparation3D() * 0.995f;
            		glasses3d.setSeparation3D( Math.max(sep, 1f) );
            	}
            }
            if (window.isKeyDown(GLFW_KEY_T)) {
            	if (window.isKeyDown(GLFW_KEY_UP)) {
            		float dt = gravity.getDt() * 1.005f;
            		gravity.setDt( Math.min(dt, 20.0f) );
            	} else if (window.isKeyDown(GLFW_KEY_DOWN)) {
            		float dt = gravity.getDt() * 0.995f;
            		gravity.setDt( Math.max(dt, 0.001f) );
            	}
            }

        }

    	camera.endAcceleration();
    }
}
