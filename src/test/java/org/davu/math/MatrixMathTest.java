// Copyright (c) 2022 David Uselmann
package org.davu.math;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Purpose: To test my understanding of the JOML module and
 *  matrix manipulation. While most things might be wrong here,
 *  it was instrumental for the creation of the view transforms.
 *
 *  Lesson One: The view transform is how to move the object in
 *  the OpenGL space, not the camera. Many will call it a camera,
 *  but it is is not.
 *
 *  Lesson Two: Many of the explanations on line are incorrect at
 *  worst and incomplete at best. It is better to explore this topic
 *  in a test environment.
 *
 * @author davu
 */
class MatrixMathTest {

	Vector3f x = new Vector3f(1,0,0);
	Vector3f y = new Vector3f(0,1,0);
	Vector3f z = new Vector3f(0,0,1);

	@BeforeEach
	void setup() {
		x = new Vector3f(1,0,0);
		y = new Vector3f(0,1,0);
		z = new Vector3f(0,0,1);
	}

	Matrix4f lookAlongFromMatrix(Vector3f look, Vector3f from, Vector3f up, Matrix4f matrix) {
    	Vector3f side= new Vector3f();
    	look.normalize().cross(up, side).normalize();

    	matrix.set(side.x,  side.y,  side.z, 0,
    			   up.x,    up.y,    up.z,   0,
    			  -look.x, -look.y, -look.z, 0,
    			   0,       0,       0,      1);

    	// if look is away from the virtual camera then translate the objects the opposite direction
    	Vector3f forToObj = new Vector3f();
    	return matrix.translate(from.fma(-1, forToObj));
	}

	@Test
	void matrixLibViewMatrix_vs_lookAlong() {
		// in opengl
		// x is side to side
		// y is the up/down
		// z is into the screen

		Matrix4f expect = new Matrix4f()
				  .set(-1, 0,  0, -2,
		 			    0, 1,  0, -2,
		 			    0, 0, -1, -2,
		 			    0, 0,  0,  1);

		Vector3f from = new Vector3f(2,2,2);
		Matrix4f view = new Matrix4f();

		view.identity().lookAlong(z, y);
		view.translate(from);
//		assertEquals(expect, view);

		Matrix4f view2 = lookAlongFromMatrix(z, from, y, new Matrix4f());
		assertNotEquals(expect, view);

		assertNotEquals(view, view2);
	}


	// from glu lib cross product computation
	Vector3f computeNormalOfPlane(Vector3f side, Vector3f forward, Vector3f up) {
		side.x=(forward.y*up.z)-(forward.z*up.y);
		side.y=(forward.z*up.x)-(forward.x*up.z);
		side.z=(forward.x*up.y)-(forward.y*up.x);
		return side;
	}

	@Test
	void matrixLib_vs_gluCossProduct() {
		// given that the glu impl port and the joml lib cross product agree
		// there is confidence that that the cross product is working

		Vector3f crossProduct = new Vector3f();

		y.cross(z, crossProduct);
		assertNotEquals(y, crossProduct);
		assertEquals(x, crossProduct);

		z.cross(y, crossProduct);
		assertNotEquals(y, crossProduct);
		assertEquals(-x.x, crossProduct.x);

		computeNormalOfPlane(crossProduct, y, z);
		assertNotEquals(y, crossProduct);
		assertEquals(x, crossProduct);

		computeNormalOfPlane(crossProduct, z, y);
		assertNotEquals(y, crossProduct);
		assertEquals(-x.x, crossProduct.x);
	}

}
