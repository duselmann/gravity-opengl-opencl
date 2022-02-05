// Copyright (c) 2022 David Uselmann
package org.davu.app.space.display;

import static org.davu.app.space.Utils.*;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;

public class Shader {

    public static int createShader(String resource, int type) throws IOException {
        int shader = glCreateShader(type);
        ByteBuffer source = ioResourceToByteBuffer(resource);
        PointerBuffer strings = BufferUtils.createPointerBuffer(1);
        IntBuffer length = BufferUtils.createIntBuffer(1);
        strings.put(0, source);
        length.put(0, source.remaining());
        glShaderSource(shader, strings, length);
        glCompileShader(shader);
        int compiled = glGetShaderi(shader, GL_COMPILE_STATUS);
        String shaderLog = glGetShaderInfoLog(shader);
        if (shaderLog.trim().length() > 0) {
            System.err.println(shaderLog);
        }
        if (compiled == 0) {
            throw new AssertionError("Error compiling shader");
        }
        return shader;
    }

    public static int createProgram(int vshader, int fshader) {
        int program = glCreateProgram();
        glAttachShader(program, vshader);
        glAttachShader(program, fshader);
        glLinkProgram(program);
        int linked = glGetProgrami(program, GL_LINK_STATUS);
        String programLog = glGetProgramInfoLog(program);
        if (programLog.trim().length() > 0) {
            System.err.println(programLog);
        }
        if (linked == 0) {
            throw new AssertionError("Error linking program");
        }
        return program;
    }

}
