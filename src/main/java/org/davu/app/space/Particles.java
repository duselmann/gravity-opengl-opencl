// Copyright (c) 2022 David Uselmann
package org.davu.app.space;

import static org.davu.app.space.ColorsGL.*;
import static org.davu.app.space.Utils.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.io.IOException;
import java.nio.FloatBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;


public class Particles {
	private static final Logger log = LogManager.getLogger(Particles.class);

	// Other particle counts
//  int NumParticles = 1_048_576; // (2^20) green-red 3D does not work well with this many points
//	int NumParticles = 65_536; // seems to be the limit of 3D red-green density but alpha must be 1
	int NumParticles = 4_096*2; // 2x the particles of my original
//	int NumParticles = 1024;
//	int NumParticles = 256;

    protected  float massBase = 5f;
    protected  float velBase  = 7f;

	// program and arguments
	public static int program;
	private int mvp16Uniform;
	private int colorUniform;
	private int alphaUniform;
	private int vertexShader;
	private int fragmentShader;

	private int numParticles;
	private int massiveCount;

    private int vertexArray;
    private int vertexBuffer;

	private final FloatBuffer matrixBuffer;

	private float alpha;

    // init properties - Dark Matter
    protected  float    dmVolume;
    protected  float    dmMass;
    protected  Vector3f dmCenter;

	private int tooCloseCount;

	private Glasses3D glasses3D;

	protected float[] vertices;


	public Particles(Glasses3D glasses3D) {
		log.info("Creating particles");

		this.glasses3D = glasses3D;

		numParticles    = NumParticles;

		// help instances - reusable matrix and buffer
		matrixBuffer    = BufferUtils.createFloatBuffer(16);

		alpha = 0.2f;
		if (numParticles > 2_000_000) {
			alpha = 0.1f;
		}

		initDarkMater();
	}

	public void createProgram() throws IOException {
		log.info("Creating particles program");

        vertexShader = Shader.createShader("gl/space-points.vs", GL_VERTEX_SHADER);
        fragmentShader = Shader.createShader("gl/space-points.fs", GL_FRAGMENT_SHADER);
        program = Shader.createProgram(vertexShader, fragmentShader);
        glUseProgram(program);

        mvp16Uniform = glGetUniformLocation(program, "mvp");
        colorUniform = glGetUniformLocation(program, "color3D");
        alphaUniform = glGetUniformLocation(program, "alpha");
        glUseProgram(0);
    }

	public void draw(Matrix4f mvpMatrix) {
	    glUseProgram(program);
//		glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer); // set the GL buffer object handle active for data
//        glVertexPointer(3, GL_FLOAT, 0, 0);
	    glBindVertexArray(vertexArray);

	    glasses3D.render(electricBlue(), mvpMatrix, this::particleRender);
//        glBindBuffer(GL_ARRAY_BUFFER, 0);
	    glBindVertexArray(0);
	}

	protected void particleRender(FloatBuffer colorBuffer, Matrix4f mvpMatrix) {
		glUniformMatrix4fv(mvp16Uniform, false, mvpMatrix.get(matrixBuffer));
		glUniform4fv(colorUniform, colorBuffer);  // for 3D glasses need green render also
	    glUniform1f(alphaUniform, alpha);  // for 3D glasses need green render also
	    glDrawArrays(GL_POINTS, 0, numParticles);
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
    }

	public void cleanup() {
		log.info("dispose - particles");
        quiteFree("glDeleteBuffers VAO", ()->glDeleteVertexArrays(vertexArray));
        quiteFree("glDeleteBuffers VBO", ()->glDeleteBuffers(vertexBuffer));
        quiteFree("glDeleteProgram", ()->glDeleteProgram(program));
        quiteFree("glDeleteShader vertex", ()->glDeleteShader(vertexShader));
        quiteFree("glDeleteShader fragment", ()->glDeleteShader(fragmentShader));
	}

	public Particles init() throws IOException {
		log.info("init - particles");
		// vertexes, once passed to GL, are no longer needed in Java
		vertices = makeVertices();
        bind();
        createProgram();

	    glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE/* _MINUS_SRC_ALPHA */); // the minus requires depth sorting
	    glEnable(GL_VERTEX_PROGRAM_POINT_SIZE);
	    glEnable(GL_POINT_SPRITE);

        return this;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}
	public float getAlpha() {
		return alpha;
	}

	public int getVertexBuffer() {
		return vertexBuffer;
	}
	public int getTooCloseCount() {
		return tooCloseCount;
	}

	public int getNumPartices() {
		return numParticles;
	}
	public void setParticleCount(int count) {
		this.numParticles = count;
		setMassiveCount(count);
	}
	public int getParticleCount() {
		return numParticles;
	}
	public void setMassiveCount(int count) {
		if (count > numParticles) {
			count = numParticles;
		}
		this.massiveCount = count;
	}
	public int getMassiveCount() {
		return massiveCount;
	}

    /**
     * TODO this worked for the Java rendering examples but not in the vertex impl
     * Used to determine if a current point is too close to the previous points.
     * If points are too close then there could be a floating point error.
     * @param sqrDist the tolerated square distance between points.
     * @param body    the point index within the array of points.
     * @param bodies  all the points array.
     * @return true if within the square distance
     */
    public  boolean checkNearPoint(int sqrDist, int body, float[] bodies) {
        boolean near = false;

        // the new point based on the body index
        Vector3f position = new Vector3f(bodies[body+0], bodies[body+1], bodies[body+2]);
        Vector3f other    = new Vector3f();

        // check all previous points to the checked point
        for (int b=0; b<body-1; b+=3) {
            other.set(bodies[b+0], bodies[b+1], bodies[b+2]);
            if (position.distanceSquared(other) < sqrDist) {
                tooCloseCount ++;
                near = true;
                break;
            }
        }
        return near;
    }

    public float[] makeVertices() {
		log.info("init particle data");

    	float[] vertices = new float[numParticles*3];

    	float span = 300f;

    	for (int v=0; v<vertices.length; v+=3) {
//    		int tries = 0;
//    		boolean generate = true;
//    		while (generate && tries++<10) {
	    		vertices[v+0] = (float)(Math.random() * span)-span/2;
	    		vertices[v+1] = (float)(Math.random() * span)-span/2;
	    		// TODO order particles in Z order but when flying this will not be correct
	    		vertices[v+2] = ((span/numParticles * v - span/2));
//	    		generate      = checkNearPoint(64, v, vertices);
//    		}
//    		if (v % 100_000 == 0) {
//    			System.out.println(v);
//    		}
    	}

    	return vertices;
	}

    public FloatBuffer  initVelocities() {
    	FloatBuffer velBuffer   = BufferUtils.createFloatBuffer(numParticles * 4);
    	int v=0;
    	Vector3f dmCenter = new Vector3f(this.dmCenter);
    	Vector3f particle = new Vector3f();
    	Vector3f worker   = new Vector3f();
    	Vector3f yAxis    = new Vector3f(0,1f,0);
    	Vector3f cross    = new Vector3f();
    	for (; v<numParticles; v++) {
    		particle.set(vertices[v*3+0],vertices[v*3+1],vertices[v*3+2]);
    		float dist = particle.distance(dmCenter);
    		float mass = dmMass * dist*dist*dist/dmVolume;
    		float vel = Math.sqrt(mass/dist); ///1000;
    		particle.sub(dmCenter, worker);
    		yAxis.cross(worker, cross);
    		cross.normalize();
    		cross.mul( 1f + (float)(0.66*Math.random()-0.33));

    		velBuffer.put(vel*cross.x).put(vel*cross.y).put(vel*cross.z);
    		velBuffer.put(0.1f * (1f + (float)(0.66*Math.random()-0.33)) ); // mass
    	}
    	velBuffer.flip();
    	this.vertices = null;
    	return velBuffer;
	}
    public void initDarkMater() {
        dmVolume = 1_073_741_824; //2^30
        dmMass   = 16_000;
        dmCenter = new Vector3f();
	}

}
