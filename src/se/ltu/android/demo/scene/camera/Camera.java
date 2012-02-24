/* SVN FILE: $Id: Camera.java 24 2009-08-17 15:32:24Z belse $ */
package se.ltu.android.demo.scene.camera;

import se.ltu.android.demo.scene.intersection.Ray;
import android.opengl.Matrix;

/**
 * A class representing a camera. It's an abstract representation of
 * anything needed to create an OpenGL view.
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision: 24 $
 * @lastmodified $Date: 2009-08-18 00:32:24 +0900 (2009-08-18, 화) $
 */
public class Camera {
	/**
	 * Constant for multiplying angular degrees to radians. 
	 */
	public static final float DEG_TO_RAD = 0.01745329238474369f;
	
	// Projection matrix.. keep static so all instances of camera
	// share the same projection
	private static float[] mProjectionMatrix = {
		1,0,0,0,
		0,1,0,0,
		0,0,1,0,
		0,0,0,1
	};
	
	// keep some variables that are good for calculating a picking ray
	private static float mNearHeight;
	private static float mZNear;
	private static float mAspect;
	private static float mHeight;
	private static float mHalfWidth;
	private static float mHalfHeight;
	
	// Model View matrix and instance variables 
	private float[] mViewMatrix = {
		1,0,0,0,
		0,1,0,0,
		0,0,1,0,
		0,0,0,1	
	};
	private float[] mInvModelMatrix = new float[16];
	private float[] mPosition = new float[3];
	
	public Camera() {
		
	}
	
	/**
	 * Set the projection matrix, similar to glmLocalTranslationerspective.
	 * @param fovy Field of view angle in y coordinate
	 * @param width Width of screen
	 * @param height Height of screen
	 * @param zNear Distance to near-plane
	 * @param zFar Distance to far-plane
	 */
	public static void setPerspective(float fovy, float width, float height, float zNear, float zFar) {		
		float tan_fovy_half = (float) Math.tan((fovy * DEG_TO_RAD) / 2);
		Camera.mNearHeight = zNear * tan_fovy_half;
		Camera.mZNear = zNear;
		Camera.mHeight = height;
		Camera.mHalfWidth = width / 2;
		Camera.mHalfHeight = height / 2;
		Camera.mAspect = width / height;
		mProjectionMatrix[5] = 1 / tan_fovy_half;  // = cot(fovy/2)

		// Remember, column major matrix
		mProjectionMatrix[0] = mProjectionMatrix[5] / mAspect;
		mProjectionMatrix[1] = 0.0f;
		mProjectionMatrix[2] = 0.0f;
		mProjectionMatrix[3] = 0.0f;

		mProjectionMatrix[4] = 0.0f;
		//project[5] = 1 / near_height;  // already set
		mProjectionMatrix[6] = 0.0f;
		mProjectionMatrix[7] = 0.0f;

		mProjectionMatrix[8] = 0.0f;
		mProjectionMatrix[9] = 0.0f;
		mProjectionMatrix[10] = (zFar + zNear) / (zNear - zFar);
		mProjectionMatrix[11] = -1.0f;

		mProjectionMatrix[12] = 0.0f;
		mProjectionMatrix[13] = 0.0f;
		mProjectionMatrix[14] = (2 * zFar * zNear) / (zNear - zFar);
		mProjectionMatrix[15] = 0.0f;
	}
	
	/**
	 * @return the projection matrix 
	 */
	public static float[] getProjectionMatrix() {
		return mProjectionMatrix;
	}
	
	/**
	 * @param m the model-view matrix to set
	 */
	public void setViewMatrix(float[] m) {
		synchronized(mViewMatrix) {
			for(int i = 0; i < 16; i++) {
				mViewMatrix[i] = m[i];
			}
		}
	}
	
	/**
	 * Sets the cameras rotation matrix. This is similar
	 * to setting the model view matrix but it keeps the
	 * cameras current position.
	 * @param rotM rotation matrix to set
	 */
	public void setRotationViewMatrix(float[] rotM) {
		synchronized(mViewMatrix) {
			for(int i = 0; i < 16; i++) {
				mViewMatrix[i] = rotM[i];
			}
			Matrix.translateM(mViewMatrix, 0, -mPosition[0], -mPosition[1], -mPosition[2]);
		}
	}
	
	/**
	 * @return the model view matrix
	 */
	public float[] getViewMatrix() {
		synchronized(mViewMatrix) {
			return mViewMatrix;
		}
	}
	
	/**
	 * Define a viewing transformation in terms of an eye point, a center of view, and an mLocalTranslation vector.
	 * @param eyex eye x coordinate
	 * @param eyey eye y coordinate
	 * @param eyez eye z coordinate
	 * @param centerx view center x coordinate
	 * @param centery view center y coordinate
	 * @param centerz view center z coordinate
	 * @param mLocalTranslationx mLocalTranslation vector x coordinate
	 * @param mLocalTranslationy mLocalTranslation vector y coordinate
	 * @param mLocalTranslationz mLocalTranslation vector z coordinate
	 */
	public void lookAt(
    	float eyex, float eyey, float eyez,
    	float centerx, float centery, float centerz,
    	float mLocalTranslationx, float mLocalTranslationy, float mLocalTranslationz) {
    
    	float[] x = new float[3]; 
    	float[] y = new float[3];
    	float[] z = new float[3];
    	float mag;

    	// Make rotation matrix
    	  
    	// Z vector
    	z[0] = eyex - centerx;
    	z[1] = eyey - centery;
    	z[2] = eyez - centerz;
    	
    	mag = Matrix.length(z[0], z[1], z[2]);
    	if (mag > 0) {			// mpichler, 19950515
    		mag = 1/mag;
    		z[0] *= mag;
    		z[1] *= mag;
    		z[2] *= mag;
    	}
    	
    	// Y vector
    	y[0] = mLocalTranslationx;
    	y[1] = mLocalTranslationy;
    	y[2] = mLocalTranslationz;

    	// X vector = Y cross Z    	
    	x[0] = y[1] * z[2] - y[2] * z[1];
    	x[1] = -y[0] * z[2] + y[2] * z[0];
    	x[2] = y[0] * z[1] - y[1] * z[0];
    	
    	// Recompute Y = Z cross X    	
    	y[0] = z[1] * x[2] - z[2] * x[1];
    	y[1] = -z[0] * x[2] + z[2] * x[0];
    	y[2] = z[0] * x[1] - z[1] * x[0];
    	
    	// mpichler, 19950515
    	
    	// cross product gives area of parallelogram, which is < 1.0 for
    	// non-perpendicular unit-length vectors; so normalize x, y here

    	mag = Matrix.length(x[0], x[1], x[2]);
    	if (mag > 0) {
    		mag = 1/mag;
    		x[0] *= mag;
    		x[1] *= mag;
    		x[2] *= mag;
    	}

    	mag = Matrix.length(y[0], y[1], y[2]);
    	if (mag > 0) {
    		mag = 1/mag;
    		y[0] *= mag;
    		y[1] *= mag;
    		y[2] *= mag;
    	}

    	synchronized(mViewMatrix) {
	    	mViewMatrix[0] = x[0];
	    	mViewMatrix[4] = x[1];
	    	mViewMatrix[8] = x[2];
	    	mViewMatrix[12] = 0.0f;
	    	mViewMatrix[1] = y[0];
	    	mViewMatrix[5] = y[1];
	    	mViewMatrix[9] = y[2];
	    	mViewMatrix[13] = 0.0f;
	    	mViewMatrix[2] = z[0];
	    	mViewMatrix[6] = z[1];
	    	mViewMatrix[10] = z[2];
	    	mViewMatrix[14] = 0.0f;
	    	mViewMatrix[3] = 0.0f;
	    	mViewMatrix[7] = 0.0f;
	    	mViewMatrix[11] = 0.0f;
	    	mViewMatrix[15] = 1.0f;
	    	
	    	//Matrix.multiplyMM(model, 0, m, 0, model, 0);
	    	// Translate Eye to Origin 
	    	mPosition[0] = eyex;
	    	mPosition[1] = eyey;
	    	mPosition[2] = eyez;
	    	Matrix.translateM(mViewMatrix, 0, -mPosition[0], -mPosition[1], -mPosition[2]);
    	}
    }

	/**
	 * Translate the cameras position with the given coordinates
	 * @param x translation x coordinate
	 * @param y translation y coordinate
	 * @param z translation z coordinate
	 */
	public void translate(float x, float y, float z) {
		synchronized(mViewMatrix) {
			mPosition[0] -= x;
			mPosition[1] -= y;
			mPosition[2] -= z;
			Matrix.translateM(mViewMatrix, 0, -x, -y, -z);
		}
	}
	
	/**
	 * Translate the cameras position with the given coordinates
	 * @param vector3f an array of size three, containing x,y and z coordinates
	 */
	public void translate(float[] vector3f) {
		if(vector3f == null || vector3f.length != 3) {
			return;
		}
		synchronized(mViewMatrix) {
			mPosition[0] -= vector3f[0];
			mPosition[1] -= vector3f[1];
			mPosition[2] -= vector3f[2];
			Matrix.translateM(mViewMatrix, 0, -vector3f[0], -vector3f[1], -vector3f[2]);
		}
	}
	
	/**
	 * @return the current position
	 */
	public float[] getPosition() {
		return mPosition;
	}
	
	/**
	 * Set the cameras model view matrix to the identity matrix
	 */
	public void setIdentity() {
		synchronized(mViewMatrix) {
			Matrix.setIdentityM(mViewMatrix, 0);
			mPosition[0] = 0;
			mPosition[1] = 0;
			mPosition[2] = 0;
		}
	}
	
	/**
	 * Set the absolute position of this camera
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param z z coordinate
	 */
	public void setPosition(float x, float y, float z) {
		synchronized(mViewMatrix) {
			// revert last position
			Matrix.translateM(mViewMatrix, 0, mPosition[0], mPosition[1], mPosition[2]);
			// set new position
			mPosition[0] = x;
	    	mPosition[1] = y;
	    	mPosition[2] = z;
	    	Matrix.translateM(mViewMatrix, 0, -mPosition[0], -mPosition[1], -mPosition[2]);
		}
	}
	
	/**
	 * Calculates a pick ray based on the given screen coordinates and
	 * the current projection matrix and model view matrix.
	 * 
	 * The screen coordinates are expected to have (0,0) at the mLocalTranslationper left 
	 * corner of the screen and the y-axis is reversed compared to the OpenGL y-axis. 
	 * @param pickX screen x coordinate
	 * @param pickY screen y coordinate
	 */
    public Ray calculatePickRay(float pickX, float pickY) {
    	// coordinates centered on the screen
    	// -1 <= x <= 1 and -1 <= y <= 1
    	float unit_x = (pickX - mHalfWidth)/mHalfWidth;
    	float unit_y = ((mHeight - pickY) - mHalfHeight)/mHalfHeight;
		
		float[] rayRawPos = {0.0f, 0.0f, 0.0f, 1.0f};
		float[] rayRawDir = {unit_x * mNearHeight * mAspect, unit_y * mNearHeight, -mZNear, 0.0f};
		float[] rayPos = new float[4];
		float[] rayDir = new float[4];
		
		// multiply the position and vector with the inverse model matrix
		// to get world coordinates
		synchronized(mViewMatrix) {
			Matrix.invertM(mInvModelMatrix, 0, mViewMatrix, 0);
		}
		Matrix.multiplyMV(rayPos, 0, mInvModelMatrix, 0, rayRawPos, 0);
		Matrix.multiplyMV(rayDir, 0, mInvModelMatrix, 0, rayRawDir, 0);

		return new Ray(rayPos[0], rayPos[1], rayPos[2], rayDir[0], rayDir[1], rayDir[2]);
	}
}
