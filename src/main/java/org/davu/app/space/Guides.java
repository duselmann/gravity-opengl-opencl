// Copyright (c) 2022 David Uselmann
package org.davu.app.space;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_NORMAL_ARRAY;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDisableClientState;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glLoadMatrixf;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glMultMatrixf;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glVertex3f;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

public class Guides {
	private final Matrix4f compass;
	private final FloatBuffer matrixBuffer;

	// does not use because this is rendered using gl functions calls rather than a shader.
	// TODO create shader program for the compass
	//	private Glasses3D glasses3D;


	public Guides(Glasses3D glasses3d) {
		compass = new Matrix4f();
		matrixBuffer = BufferUtils.createFloatBuffer(16);
	}

    public void draw(Matrix4f proj, Matrix4f view) { // asdf change perspective to ortho not projection
        glUseProgram(0);
        glEnable(GL_BLEND);
        glEnableClientState(GL_NORMAL_ARRAY);
        glMatrixMode(GL_PROJECTION);

        glPushMatrix();
          	glLoadMatrixf(proj.get(matrixBuffer));
	        glMatrixMode(GL_MODELVIEW);
	        glPushMatrix();

		        glLoadIdentity();
		        compass.set(view)
		        	.m30(0).m31(-2).m32(-3)
		        	.get(matrixBuffer);
		        glMultMatrixf(matrixBuffer);
		        glScalef(0.3f, 0.3f, 0.3f);

		        glBegin(GL_LINES);
			        glColor4f(1, 0, 0, 1); //red
			        glVertex3f(0, 0, 0);
			        glVertex3f(1, 0, 0);   //x

			        glColor4f(0, 1, 0, 1); //green
			        glVertex3f(0, 0, 0);
			        glVertex3f(0, 1, 0);   //y

			        glColor4f(0, 0, 1, 1); //blue
			        glVertex3f(0, 0, 0);
			        glVertex3f(0, 0, 1);   //z

//			        glColor4f(1, 1, 1, 1);
//			        glVertex3f(0, 0, 0);
		        glEnd();

	        glPopMatrix();
	        glMatrixMode(GL_PROJECTION);
        glPopMatrix();

        glMatrixMode(GL_MODELVIEW);
        glDisableClientState(GL_NORMAL_ARRAY);
        glDisable(GL_BLEND);
    }


    public void cleanup() {
    	//matrixBuffer disposal
	}
}
