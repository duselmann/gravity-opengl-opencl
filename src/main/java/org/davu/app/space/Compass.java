// Copyright (c) 2022 David Uselmann
package org.davu.app.space;

import static org.davu.app.space.ColorsGL.*;
import static org.davu.app.space.Utils.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector4d;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;


public class Compass {
	private static final Logger log = LogManager.getLogger(Compass.class);

	// program and arguments
	private int program;
	private int mvp16Uniform;
	private int colorUniform;
	private int alphaUniform;
	private int vertexShader;
	private int fragmentShader;

    private int vertexBuffer;

	private final FloatBuffer matrixBuffer;
	private final Matrix4f compass;

	private float alpha;

	private Glasses3D glasses3D;

	protected float[] vertices;


	public Compass(Glasses3D glasses3D) {
		log.info("Creating particles");

		this.glasses3D = glasses3D;

		// help instances - reusable matrix and buffer
		matrixBuffer    = BufferUtils.createFloatBuffer(16);
		compass         = new Matrix4f();

		alpha = 1f;
	}
	public void createProgram() throws IOException {
		program = Particles.program;
	}

//	public void createProgram() throws IOException {
//		log.info("Creating particles program");
//
//        vertexShader = Shader.createShader("gl/space-points.vs", GL_VERTEX_SHADER);
//        fragmentShader = Shader.createShader("gl/space-points.fs", GL_FRAGMENT_SHADER);
//        program = Shader.createProgram(vertexShader, fragmentShader);
//        glUseProgram(program);
//
//        mvp16Uniform = glGetUniformLocation(program, "mvp");
//        colorUniform = glGetUniformLocation(program, "color3D");
//        alphaUniform = glGetUniformLocation(program, "alpha");
//        glUseProgram(0);
//    }

	public void draw(Matrix4f proj, Matrix4f view) {
	    glUseProgram(program);
	    glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE/* _MINUS_SRC_ALPHA */); // the minus requires depth sorting
	    glEnable(GL_VERTEX_PROGRAM_POINT_SIZE);
	    glEnable(GL_LINE_WIDTH);
//	    glLineWidth(10f);
//	    glBindVertexArray(vertexArray[0]);
//	    glEnableVertexAttribArray(0);
//	    glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer[0]);
	    glVertexAttribPointer(0, 0, GL_FLOAT, false, 0, 0); // 3x4 is 4 xyz points

//        glVertexAttribPointer(0,2,true,0,vertexBuffer[0]);
//            0,                  // attribute 0. No particular reason for 0, but must match the layout in the shader.
//            2,                  // size
//            GL_FLOAT,           // type
//            GL_FALSE,           // normalized?
//            0,                  // stride
//            (void*)0            // array buffer offset
//        );
////	    Matrix4f mvpMatrix = compass.set(view)
////	        	.m30(0).m31(-2).m32(-3);
	    Matrix4f mvpMatrix = compass.set(view)
	        	.mul(proj);
////	        	.m30(0).m31(-2).m32(-3);

	    glasses3D.render(red(), mvpMatrix, (c,m)->{
	    	System.err.println("foo");
	    	particleRender(0,c,m);
	    });
//	    glasses3D.render(green(), mvpMatrix, (c,m)->{particleRender(2,c,m);});
//	    glasses3D.render(blue(), mvpMatrix, (c,m)->{particleRender(4,c,m);});
	}

	protected void particleRender(int idx, FloatBuffer colorBuffer, Matrix4f mvpMatrix) {
		System.err.println("bar");
	    glUniformMatrix4fv(mvp16Uniform, false, mvpMatrix.get(matrixBuffer));
		glUniform4fv(colorUniform, colorBuffer);  // for 3D glasses need green render also
	    glUniform1f(alphaUniform, alpha);  // for 3D glasses need green render also
	    glDrawArrays(GL_LINE, 0, 6);
	}

    public void bind() {
		log.info("binding particle data GL");
		// all the vertex GL handles

		bind(vertices);

	    glFinish();
	    glFlush();
    }

	protected void bind(float[] vertices) {
		log.info("binding particle data GL");

	    // Bind the Vertex Array Object then bind and set vertex buffer(s) and attribute pointer(s).
//	    glGenVertexArrays(vertexArrayObj);    // get a GL array object handle
//	    glBindVertexArray(vertexArrayObj[0]); // set a GL array object handle active for data buffers

	    this.vertexBuffer = glGenBuffers();   // get a GL buffer object handle
	    glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer); // set the GL buffer object handle active for data
	    glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW); // load the vertex data into the GPU buffer
        glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	public void cleanup() {
		log.info("dispose - particles");
//        quiteFree("glDeleteVertexArrays VAO", ()->glDeleteVertexArrays(vertexArray[0]));
        quiteFree("glDeleteBuffers VBO", ()->glDeleteBuffers(vertexBuffer));
        quiteFree("glDeleteProgram", ()->glDeleteProgram(program));
        quiteFree("glDeleteShader vertex", ()->glDeleteShader(vertexShader));
        quiteFree("glDeleteShader fragment", ()->glDeleteShader(fragmentShader));
	}

	public Compass init() throws IOException {
		log.info("init - particles");
		// vertexes, once passed to GL, are no longer needed in Java
		vertices = makeVertices();
        bind();
        createProgram();
        return this;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}
	public float getAlpha() {
		return alpha;
	}

//	public int getVertexBuffer() {
//		return vertexBuffer[0];
//	}



    public float[] makeVertices() {
		log.info("init vertices");

    	float[] vertices = new float[] {
    			0,0,0,
    			100,0,0,

    			0,0,0,
    			0,100,0,

    			0,0,0,
    			0,0,100
    	};


    	return vertices;
	}

/*

    private void createShip() throws IOException {
        WavefrontMeshLoader loader = new WavefrontMeshLoader();
        ship = loader.loadMesh("org/lwjgl/demo/game/ship.obj.zip");
        shipPositionVbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, shipPositionVbo);
        glBufferData(GL_ARRAY_BUFFER, ship.positions, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        shipNormalVbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, shipNormalVbo);
        glBufferData(GL_ARRAY_BUFFER, ship.normals, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    private void createAsteroid() throws IOException {
        WavefrontMeshLoader loader = new WavefrontMeshLoader();
        asteroid = loader.loadMesh("org/lwjgl/demo/game/asteroid.obj.zip");
        asteroidPositionVbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, asteroidPositionVbo);
        glBufferData(GL_ARRAY_BUFFER, asteroid.positions, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        asteroidNormalVbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, asteroidNormalVbo);
        glBufferData(GL_ARRAY_BUFFER, asteroid.normals, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    private static int createShader(String resource, int type) throws IOException {
        int shader = glCreateShader(type);
        ByteBuffer source = ioResourceToByteBuffer(resource, 1024);
        PointerBuffer strings = BufferUtils.createPointerBuffer(1);
        IntBuffer lengths = BufferUtils.createIntBuffer(1);
        strings.put(0, source);
        lengths.put(0, source.remaining());
        glShaderSource(shader, strings, lengths);
        glCompileShader(shader);
        int compiled = glGetShaderi(shader, GL_COMPILE_STATUS);
        String shaderLog = glGetShaderInfoLog(shader);
        if (shaderLog.trim().length() > 0) {
            System.err.println(shaderLog);
        }
        if (compiled == 0) {
            throw new AssertionError("Could not compile shader");
        }
        return shader;
    }

    private static int createProgram(int vshader, int fshader) {
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
            throw new AssertionError("Could not link program");
        }
        return program;
    }

    private void createCubemapProgram() throws IOException {
        int vshader = createShader("org/lwjgl/demo/game/cubemap.vs", GL_VERTEX_SHADER);
        int fshader = createShader("org/lwjgl/demo/game/cubemap.fs", GL_FRAGMENT_SHADER);
        int program = createProgram(vshader, fshader);
        glUseProgram(program);
        int texLocation = glGetUniformLocation(program, "tex");
        glUniform1i(texLocation, 0);
        cubemap_invViewProjUniform = glGetUniformLocation(program, "invViewProj");
        glUseProgram(0);
        cubemapProgram = program;
    }

    private void createShipProgram() throws IOException {
        int vshader = createShader("org/lwjgl/demo/game/ship.vs", GL_VERTEX_SHADER);
        int fshader = createShader("org/lwjgl/demo/game/ship.fs", GL_FRAGMENT_SHADER);
        int program = createProgram(vshader, fshader);
        glUseProgram(program);
        ship_viewUniform = glGetUniformLocation(program, "view");
        ship_projUniform = glGetUniformLocation(program, "proj");
        ship_modelUniform = glGetUniformLocation(program, "model");
        glUseProgram(0);
        shipProgram = program;
    }

    private void createParticleProgram() throws IOException {
        int vshader = createShader("org/lwjgl/demo/game/particle.vs", GL_VERTEX_SHADER);
        int fshader = createShader("org/lwjgl/demo/game/particle.fs", GL_FRAGMENT_SHADER);
        int program = createProgram(vshader, fshader);
        glUseProgram(program);
        particle_projUniform = glGetUniformLocation(program, "proj");
        glUseProgram(0);
        particleProgram = program;
    }

    private void createShotProgram() throws IOException {
        int vshader = createShader("org/lwjgl/demo/game/shot.vs", GL_VERTEX_SHADER);
        int fshader = createShader("org/lwjgl/demo/game/shot.fs", GL_FRAGMENT_SHADER);
        int program = createProgram(vshader, fshader);
        glUseProgram(program);
        shot_projUniform = glGetUniformLocation(program, "proj");
        glUseProgram(0);
        shotProgram = program;
    }

    private void update() {
        //* Update the background shader /
        glUseProgram(cubemapProgram);
        glUniformMatrix4fv(cubemap_invViewProjUniform, false, invViewProjMatrix.get(matrixBuffer));

        //* Update the ship shader /
        glUseProgram(shipProgram);
        glUniformMatrix4fv(ship_viewUniform, false, viewMatrix.get(matrixBuffer));
        glUniformMatrix4fv(ship_projUniform, false, projMatrix.get(matrixBuffer));

        //* Update the shot shader /
        glUseProgram(shotProgram);
        glUniformMatrix4fv(shot_projUniform, false, matrixBuffer);

        //* Update the particle shader /
        glUseProgram(particleProgram);
        glUniformMatrix4fv(particle_projUniform, false, matrixBuffer);

    }

    private void drawShips() {
        glUseProgram(shipProgram);
        glBindBuffer(GL_ARRAY_BUFFER, shipPositionVbo);
        glVertexPointer(3, GL_FLOAT, 0, 0);
        glEnableClientState(GL_NORMAL_ARRAY);
        glBindBuffer(GL_ARRAY_BUFFER, shipNormalVbo);
        glNormalPointer(GL_FLOAT, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for (int i = 0; i < ships.length; i++) {
            Ship ship = ships[i];
            if (ship == null)
                continue;
            float x = (float)(ship.x - cam.position.x);
            float y = (float)(ship.y - cam.position.y);
            float z = (float)(ship.z - cam.position.z);
            if (frustumIntersection.testSphere(x, y, z, shipRadius)) {
                modelMatrix.translation(x, y, z);
                modelMatrix.scale(shipRadius);
                glUniformMatrix4fv(ship_modelUniform, false, modelMatrix.get(matrixBuffer));
                glDrawArrays(GL_TRIANGLES, 0, this.ship.numVertices);
            }
        }
        glDisableClientState(GL_NORMAL_ARRAY);
    }

    private void drawAsteroids() {
        glUseProgram(shipProgram);
        glBindBuffer(GL_ARRAY_BUFFER, asteroidPositionVbo);
        glVertexPointer(3, GL_FLOAT, 0, 0);
        glEnableClientState(GL_NORMAL_ARRAY);
        glBindBuffer(GL_ARRAY_BUFFER, asteroidNormalVbo);
        glNormalPointer(GL_FLOAT, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for (int i = 0; i < asteroids.length; i++) {
            Asteroid asteroid = asteroids[i];
            if (asteroid == null)
                continue;
            float x = (float)(asteroid.x - cam.position.x);
            float y = (float)(asteroid.y - cam.position.y);
            float z = (float)(asteroid.z - cam.position.z);
            if (frustumIntersection.testSphere(x, y, z, asteroid.scale)) {
                modelMatrix.translation(x, y, z);
                modelMatrix.scale(asteroid.scale);
                glUniformMatrix4fv(ship_modelUniform, false, modelMatrix.get(matrixBuffer));
                glDrawArrays(GL_TRIANGLES, 0, this.asteroid.numVertices);
            }
        }
        glDisableClientState(GL_NORMAL_ARRAY);
    }

    private void drawParticles() {
            glUseProgram(particleProgram);
            glDepthMask(false);
            glEnable(GL_BLEND);
            glVertexPointer(4, GL_FLOAT, 6*4, particleVertices);
            particleVertices.position(4);
            glTexCoordPointer(2, GL_FLOAT, 6*4, particleVertices);
            particleVertices.position(0);
            glEnableClientState(GL_TEXTURE_COORD_ARRAY);
            glDrawArrays(GL_TRIANGLES, 0, num * 6);
            glDisableClientState(GL_TEXTURE_COORD_ARRAY);
            glDisable(GL_BLEND);
            glDepthMask(true);
    }

    private void drawShots() {
            glUseProgram(shotProgram);
            glDepthMask(false);
            glEnable(GL_BLEND);
            glVertexPointer(4, GL_FLOAT, 6*4, shotsVertices);
            shotsVertices.position(4);
            glTexCoordPointer(2, GL_FLOAT, 6*4, shotsVertices);
            shotsVertices.position(0);
            glEnableClientState(GL_TEXTURE_COORD_ARRAY);
            glDrawArrays(GL_TRIANGLES, 0, num * 6);
            glDisableClientState(GL_TEXTURE_COORD_ARRAY);
            glDisable(GL_BLEND);
            glDepthMask(true);
    }
    private void render() {
        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
        drawShips();
        drawAsteroids();
        drawShots();
        drawParticles();
    }
    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents();
            glViewport(0, 0, fbWidth, fbHeight);
            update();
            render();
            glfwSwapBuffers(window);
        }
    }
    */
}
