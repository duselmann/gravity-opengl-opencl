// Copyright (c) 2022 David Uselmann
package org.davu.app.space.display;

import static org.davu.app.space.Utils.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Manages Vertex Array and Buffer objects for multiple uses.
 * First, web chatter suggest it is more efficient if GL need
 * not swap buffers to display different entities.
 * Second, when I did configure separately, the last one won.
 * It is likely because I was not fully swapping buffers.
 *
 * This works by expecting all new display implementations to
 * register with this manager. It will communicate the index
 * offset in the vertex array that it should use when displaying.
 * The clients will submit their vertex counts.
 *
 * After vertex data is passed to OpenGL, it is not needed in Java
 * memory and is disposed the Java way... setting it to null.
 *
 * @author davu
 */
public class VaoVboManager {
	private static final Logger log = LogManager.getLogger(VaoVboManager.class);

	private class Client {
		VaoVboClient client;
		int vertices;
		int offset;
		public Client(VaoVboClient client, int vertices) {
			super();
			this.client = client;
			this.vertices = vertices;
		}

	}
	private ArrayList<Client> clients;
	protected float[] vertices;
	protected int currentIndex;

	// program and arguments
	public static int program;
	private int mvp16Uniform;
	private int colorUniform;
	private int alphaUniform;
	private int vertexShader;
	private int fragmentShader;
    private int vertexArray;
    private int vertexBuffer;

	public VaoVboManager() {
		clients = new ArrayList<Client>();
	}

	public void register(VaoVboClient client) {
		// TODO index arrays for more complex vertex structures
		if (vertices != null) {
			throw new RuntimeException("Must register all VAO VBO clients before manager initialization.");
		}
		clients.add(new Client(client, client.getParticleCount()));
	}

	public void init() {
		int total = clients.stream().map((c)->c.vertices).reduce (0, (x,y) -> x+y);
		vertices = new float[total * 3];

		for (Client client : clients) {
			client.offset = currentIndex;
			client.client.setOffsetIndex(currentIndex/3); // vertex offset not float
			client.client.makeVertices(this);
		}
		bind();
		// vertexes, once passed to GL, are no longer needed in Java
		vertices = null; // frees up memory
        createProgram();
		for (Client client : clients) {
			client.offset = currentIndex;
			client.client.setVertexBuffer(vertexBuffer);
		}
	}

	public void addVertex(Vector3f vertex) {
		vertices[currentIndex++] = vertex.x;
		vertices[currentIndex++] = vertex.y;
		vertices[currentIndex++] = vertex.z;
	}
	public Vector3f getVertex(VaoVboClient vvClient, int index) {
		Client client = clients.stream().filter((c)->{return c.client == vvClient;}).findFirst().get();
		if (index < 0 || index > client.vertices) {
			throw new IndexOutOfBoundsException("Vertex Client requested a vertex out of bounds: " + index);
		}
		int v = 3*index + client.offset;
		return new Vector3f(vertices[v],vertices[v+1],vertices[v+2]);
	}


	public void createProgram() {
		log.info("Creating particles program");

		try {
	        vertexShader = Shader.createShader("gl/space-points.vs", GL_VERTEX_SHADER);
	        fragmentShader = Shader.createShader("gl/space-points.fs", GL_FRAGMENT_SHADER);
	        program = Shader.createProgram(vertexShader, fragmentShader);
	        glUseProgram(program);

	        mvp16Uniform = glGetUniformLocation(program, "mvp");
	        colorUniform = glGetUniformLocation(program, "color3D");
	        alphaUniform = glGetUniformLocation(program, "alpha");
	        for (Client c : clients) {
	        	c.client.setAlphaUniform(alphaUniform);
	        	c.client.setColorUniform(colorUniform);
	        	c.client.setMvp16Uniform(mvp16Uniform);
	        }
	        glUseProgram(0);
		} catch (Exception e) {
			new RuntimeException("Failed to create shader program.", e);
		}
    }

    public void bind() {
		log.info("binding particle data GL");
		// all the vertex GL handles
		vertexArray = glGenVertexArrays();
		glBindVertexArray(vertexArray);

		vertexBuffer = glGenBuffers();        // get a GL buffer object handle
	    glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer); // set the GL buffer object handle active for data
	    glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW); // load the vertex data into the GPU buffer

	    int positionLocation = glGetAttribLocation(program, "position");
	    glEnableVertexAttribArray(positionLocation);
	    glVertexAttribPointer(positionLocation,3, GL_FLOAT, false, 0, 0);

        // TODO not fully sure about these 4 necessary lines
	    glEnableVertexAttribArray(0);
	    glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
	    // Note that this is allowed, the call to glVertexAttribPointer registered VBO
	    // as the currently bound vertex buffer object so afterwards we can safely unbind
	    glBindBuffer(GL_ARRAY_BUFFER, 0);
	    // Unbind VAO (it's always a good thing to unbind any buffer/array to prevent strange bugs),
	    // remember: do NOT unbind the EBO, keep it bound to this VAO
	    glBindVertexArray(0);
    }

	public void cleanup() {
		log.info("dispose - VAO VBO Manager");
		clients.stream().map(c->c.client).forEach(c->c.cleanup());
        quiteFree("glDeleteBuffers VAO", ()->glDeleteVertexArrays(vertexArray));
        quiteFree("glDeleteBuffers VBO", ()->glDeleteBuffers(vertexBuffer));
        quiteFree("glDeleteProgram", ()->glDeleteProgram(program));
        quiteFree("glDeleteShader vertex", ()->glDeleteShader(vertexShader));
        quiteFree("glDeleteShader fragment", ()->glDeleteShader(fragmentShader));
	}

	public void draw(Matrix4f mvp) {
	    glUseProgram(program);
	    glBindVertexArray(vertexArray);
	    for (Client client : clients) {
	    	client.client.draw(mvp);
	    }
	}
}
