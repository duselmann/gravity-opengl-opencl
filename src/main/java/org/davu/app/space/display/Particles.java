// Copyright (c) 2022 David Uselmann
package org.davu.app.space.display;

import static org.davu.app.space.display.ColorsGL.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;


public class Particles implements VaoVboClient {
	private static final Logger log = LogManager.getLogger(Particles.class);

	// Other particle counts
//  int NumParticles = 1_048_576; // (2^20) green-red 3D does not work well with this many points
//	int NumParticles = 65_536; // seems to be the limit of 3D red-green density but alpha must be 1
	int NumParticles = 4_096*2; // 2x the particles of my original
//	int NumParticles = 1024;
//	int NumParticles = 256;

    protected  float massBase = 5f;
    protected  float velBase  = 7f;

	private int numParticles;
	private int massiveCount;

	private int mvp16Uniform;
	private int colorUniform;
	private int alphaUniform;
	private int vertexBuffer;

	private final FloatBuffer matrixBuffer;
	protected FloatBuffer velocities;

	private float alpha;

    // init properties - Dark Matter
    protected  float    dmVolume;
    protected  float    dmMass;
    protected  Vector3f dmCenter;

	private int tooCloseCount;

	private Glasses3D glasses3D;

	protected int offset;



	public Particles(Glasses3D glasses3D) {
		log.info("Creating particles");
		this.glasses3D = glasses3D;

		setParticleCount(NumParticles);

		// help instances - reusable matrix and buffer
		matrixBuffer    = BufferUtils.createFloatBuffer(16);

		alpha = 0.5f;
		if (numParticles > 2_000_000) {
			alpha = 0.1f;
		}

		initDarkMater();
	}


	@Override
	public void draw(Matrix4f mvp) {
	    glEnable(GL_BLEND);
	    glEnable(GL_VERTEX_PROGRAM_POINT_SIZE);
	    glEnable(GL_POINT_SMOOTH);
	    glShadeModel(GL_SMOOTH);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE/* _MINUS_SRC_ALPHA */); // the minus requires depth sorting
	    glEnable(GL_POINT_SPRITE);
	    glasses3D.render(electricBlue(), mvp, this::particleRender);
	}

	protected void particleRender(FloatBuffer colorBuffer, Matrix4f mvpMatrix) {
		glUniformMatrix4fv(mvp16Uniform, false, mvpMatrix.get(matrixBuffer));
		glUniform4fv(colorUniform, colorBuffer);  // for 3D glasses need green render also
	    glUniform1f(alphaUniform, alpha);
	    glDrawArrays(GL_POINTS, offset, numParticles);
	}

	@Override
	public void cleanup() {
		log.info("dispose - particles");
	}

	public Particles init() {
		log.info("init - particles");


        return this;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}
	public float getAlpha() {
		return alpha;
	}

	public int getTooCloseCount() {
		return tooCloseCount;
	}

	public void setParticleCount(int count) {
		this.numParticles = count;
		setMassiveCount(count);
	}
	@Override
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

    @Override
	public void makeVertices(VaoVboManager manager) {
		log.info("init particle data");

    	float span = 300f;

    	for (int v=0; v<numParticles; v++) {
//    		int tries = 0;
//    		boolean generate = true;
//    		while (generate && tries++<10) {
    		Vector3f vertex = new Vector3f();
    		vertex.x = (float)(Math.random() * span)-span/2;
    		vertex.y = (float)(Math.random() * span)-span/2;
    		vertex.z = ((span/numParticles * v - span/2));
//	    		generate      = checkNearPoint(64, v, vertices);
//    		}
    		manager.addVertex(vertex);
//    		if (v % 100_000 == 0) {
//    			System.out.println(v);
//    		}
    	}
    	velocities = initVelocities(manager);
	}

    public FloatBuffer  initVelocities(VaoVboManager manager) {
    	FloatBuffer velBuffer   = BufferUtils.createFloatBuffer(numParticles * 4);
    	int v=0;
    	Vector3f dmCenter = new Vector3f(this.dmCenter);
    	Vector3f worker   = new Vector3f();
    	Vector3f yAxis    = new Vector3f(0,1f,0);
    	Vector3f cross    = new Vector3f();
    	for (; v<numParticles; v++) {
        	Vector3f particle = manager.getVertex(this, v);
    		//particle.set(vertices[v*3+0],vertices[v*3+1],vertices[v*3+2]);

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
    	return velBuffer;
	}
    public void initDarkMater() {
        dmVolume = 1_073_741_824; //2^30
        dmMass   = 16_000;
        dmCenter = new Vector3f();
	}

    @Override
    public void setOffsetIndex(int offsetIndex) {
    	this.offset = offsetIndex;
    }

    @Override
	public void setVertexBuffer(int vertexBuffer) {
		this.vertexBuffer = vertexBuffer;
	}
	public int getVertexBuffer() {
		return vertexBuffer;
	}
    @Override
	public void setMvp16Uniform(int mvp16Uniform) {
		this.mvp16Uniform = mvp16Uniform;
	}
    @Override
	public void setColorUniform(int colorUniform) {
		this.colorUniform = colorUniform;
	}
    @Override
	public void setAlphaUniform(int alphaUniform) {
		this.alphaUniform = alphaUniform;
	}

    public FloatBuffer getVelocities() {
		return velocities;
	}

    public Vector3f getDarmMatterCenter() {
		return dmCenter;
	}
    public float getDarkMatterMass() {
		return dmMass;
	}
    public float getDarkMatterVolume() {
		return dmVolume;
	}

	public void doneWithVelocityData() {
		velocities = null; // frees up memory
	}
}
