// Copyright (c) 2022 David Uselmann
package org.davu.app.space;

// TODO implement a frustum for clipping what is displayed
//import org.joml.FrustumIntersection;
// I am not sure that a frustum in from JOML will help OpenGL
// in the manner that I am using GL so it is a later consideration

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Manages all the linear algebra necessary to "fly" around the arena.
 *
 * projection   is the projection matrix. This sets the view aspect ratio, angle, and depth.
 * viewMatrix   is the displacement matrix for objects to move as if the viewer is moving.
 *   In OpenGL, the objects in the field must be transformed as if the viewer moves.
 * viewProjection is the combination of all matrices, in this case view and projection.
 *
 * displacement is the vector offset of all objects in the viewport.
 *   This is opposite of camera location because OpenGL has no camera location mechanism.
 * orientation  is the angular offset of all objects in the viewport.
 *
 * angularAccel is the current increase in roll
 * angularDecel is the rate of roll retardation when no input is given; prevent loss of control
 * angularSpeed is the current roll speed
 *
 * impulseMag   is the acceleration value to use per input impulse
 * maxSpeed     is the cap on speed to prevent loosing the objects in view
 * acceleration is the current acceleration rate from user input
 * deceleration is the rate of deceleration when no input is given; prevents loss of control
 * velocity     is the current view speed
 * impulse      holds the additional acceleration, also called jerk in physics.
 *   (Aside: Jerk in physics is the change in acceleration just as acceleration is the change
 *    in velocity and velocity is the change in position. An uneven jerk is what makes it feel
 *    uncomfortable when pulsing the break in a car. I has been used to describe someone who
 *    make uncomfortable social inertactions. There is no term for a uncomfortable jerk,
 *    that would be a changing jerk, they're just jerks and non-jerks. I prefer impulse.)
 *
 * @author davu
 */
public class ViewMatrix {
	private Matrix4f viewProjection  = new Matrix4f();
	private Matrix4f projection      = new Matrix4f();
	private Matrix4f viewMatrix      = new Matrix4f().identity();

	private final float impulseMag   = 300f;
	private final float maxSpeed     = 3000.0f;
	private final float deceleration = 0.5f;
	private    Vector3f acceleration = new Vector3f();
	private    Vector3f velocity     = new Vector3f();
	private    Vector3f impulse      = new Vector3f();

	private final float angularMag   = 2f;
	private final float angularDecel = 1f;
	private    Vector3f angularAccel = new Vector3f();
	private    Vector3f angularSpeed = new Vector3f();

	private    Vector3f displacement = new Vector3f();
	private Quaternionf orientation  = new Quaternionf();

	public ViewMatrix(float width, float height) {
		updateAspect(width, height);
		displacement.set(0, 0, -1000);
	}

	public void beginAcceleration() {
        acceleration.zero();
	}
	public void endAcceleration() {
        float velocityMagnitude = velocity.length();
        if (velocityMagnitude > maxSpeed) {
            velocity.normalize().mul(maxSpeed);
        }
	}

	// when the screen changes size, this updates the GL aspect ratio
	public void updateAspect(float width, float height) {
		// set a 90 degree viewport with window aspect ratio, and Z extents
		projection.setPerspective(Math.toRadians(90.0f), (width / height), 0.1f, 20000.0f);
	}

	// updates any velocity vector with acceleration and deceleration, both linear and angular
	protected void updateVelocity(float dt, Vector3f acceleration, Vector3f velocity, float damp) {
		velocity.fma(dt, acceleration);
		velocity.mul(1.0f - damp * dt);
	}

	/**
	 * Smoothly update the view matrix using the elapsed time dt from the previous update.
	 * If the dt was omitted then the movement could feel stochastic.
	 * Rotation is decelerated and integrated into the orientation.
	 * Velocity is decelerated and added to the displacement.
	 * ViewMatrix is updated with the new orientation and displacement.
	 * View and Projection are combined into one matrix so that each point need not compute it.
	 * Frustum might be useful to clip display of points outside the projection.
	 *
	 * @param dt the milliseconds that elapsed since the previous update.
	 */
	public void updateView(float dt) {
		// ANGULAR
		// add acceleration angular velocity
		updateVelocity(dt, angularAccel, angularSpeed, angularDecel);
		// add rotation angular orientation
		if (angularSpeed.x + angularSpeed.y + angularSpeed.z != 0) {
			orientation.integrate(dt, angularSpeed.x, angularSpeed.y, angularSpeed.z);
		}

		// LINEAR
		// add acceleration linear velocity
		updateVelocity(dt, acceleration,  velocity,  deceleration);
		// add linear velocity to displacement
		displacement.fma(dt, velocity);

		// MATRIX
		// update view with position and orientation
		viewMatrix.set(orientation).translate(displacement);
		// update the MVP matrix once for all shader calls
	    viewProjection.set(projection).mul(viewMatrix);
	    // TODO impl display clipping if it improves performance
	    //frustumIntersection.set(viewProjection);
	}

	// extracts current side-to-side vector from the quaternion
	public Vector3f right(Vector3f dest) {
		return orientation.positiveX(dest);
	}
	// extracts current up-down vector from the quaternion
	public Vector3f up(Vector3f dest) {
		return orientation.positiveY(dest);
	}
	// extracts current facing vector from the quaternion
	public Vector3f forward(Vector3f dest) {
		return orientation.positiveZ(dest);
	}
	// add velocity along the facing direction. negative changes direction, >1 increases boost
	public void forward(float magnitude) {
        acceleration.fma(magnitude * impulseMag, forward(impulse));
	}
	// add velocity along the side direction. negative changes direction, >1 increases boost
	public void right(float magnitude) {
    	acceleration.fma(magnitude * impulseMag, right(impulse));
	}
	// add velocity along the up direction. negative changes direction, >1 increases boost
	public void up(float magnitude) {
    	acceleration.fma(magnitude * impulseMag, up(impulse));
	}
	// uses the cube of position to proportionally increase turning
	public void changeDirection(float mouseX, float mouseY, float rotation) {
        angularAccel.set(angularMag*mouseY*mouseY*mouseY, angularMag*mouseX*mouseX*mouseX, rotation);
	}


	public Matrix4f getProjection() {
		return projection;
	}
	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}
	public Matrix4f getViewProjection() {
		return viewProjection;
	}
}