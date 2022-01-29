// Copyright (c) 2022 David Uselmann
package org.davu.app.space;

import static org.davu.app.space.Utils.*;

import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;

public class DebugUtils {
	private Callback debugProc;

    GLDebugMessageCallback errorsCallback;


	public DebugUtils init() {
        debugProc = GLUtil.setupDebugMessageCallback();

        errorsCallback = new GLDebugMessageCallback() {
    		@Override
    		public void invoke(int source, int type, int id, int severity, int length, long message, long userParam) {
    			System.err.println("\nThere was an error in OpenGL\n");
    		}
    	};
        return this;
	}

	public void cleanup() {
        if (debugProc != null) {
            debugProc.free();
        }
        quiteFree("errorsCallback", ()->errorsCallback.free());
	}
}
