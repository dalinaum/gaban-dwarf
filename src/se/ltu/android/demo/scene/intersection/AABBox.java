/* SVN FILE: $Id: AABBox.java 24 2009-08-17 15:32:24Z belse $ */
package se.ltu.android.demo.scene.intersection;

import android.util.Log;

/**
 * An Axis-Aligned Bounding Box that is defined by a minimum and a maximum
 * point.
 * 
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision: 24 $
 * @lastmodified $Date: 2009-08-18 00:32:24 +0900 (2009-08-18, 화) $
 */
public class AABBox {
	private final static String TAG = "AABBox";
	public float mMinX, mMinY, mMinZ, mMaxX, mMaxY, mMaxZ;
	
	/**
	 * Constructs the box with points at zero
	 */
	public AABBox() {
		mMinX = mMaxX = 0;
		mMinY = mMaxY = 0;
		mMinZ = mMaxZ = 0;
	}
	
	/**
	 * Constructs the box by giving it two points. This constructor will
	 * compare the point components to ensure that any component c0 is 
	 * smaller than c1.
	 * @param x0 first x-coordinate
	 * @param y0 first y-coordinate
	 * @param z0 first z-coordinate
	 * @param x1 second x-coordinate
	 * @param y1 second y-coordinate
	 * @param z1 second z-coordinate
	 */
	public AABBox(float x0, float y0, float z0, float x1, float y1, float z1) {
		if(x0 > x1) {
			mMinX = x1;
			mMaxX = x0;
		} else {
			mMinX = x0;
			mMaxX = x1;
		}
		if(y0 > y1) {
			mMinY = y1;
			mMaxY = y0;
		} else {
			mMinY = y0;
			mMaxY = y1;
		}
		if(z0 > z1) {
			mMinZ = z1;
			mMaxZ = z0;
		} else {
			mMinZ = z0;
			mMaxZ = z1;
		}
	}
	
	/**
	 * Apply a transformation matrix on another box and set the result
	 * on this box. This box current values will be overwritten while the
	 * other box will not be touched.
	 * @param matrix column-major transformation matrix to apply
         * @param other other bounding box to transform values from
	 */
	public void transform(float[] matrix, AABBox other) {
		if(matrix.length != 16) {
			Log.e(TAG, "The matrix size is wrong");
			return;
		}
		float av, bv;
		int col, row;
		float[] oldMin = {other.mMinX, other.mMinY, other.mMinZ};
		float[] oldMax = {other.mMaxX, other.mMaxY, other.mMaxZ};
		float[] newMin = {matrix[12], matrix[13], matrix[14]};
		float[] newMax = {matrix[12], matrix[13], matrix[14]};
		for (col = 0; col < 3; col++) {
			for (row = 0; row < 3; row++)
			{
				av = matrix[row+col*4] * oldMin[row];
				bv = matrix[row+col*4] * oldMax[row];
				if (av < bv)
				{
					newMin[col] += av;
					newMax[col] += bv;
				} else {
					newMin[col] += bv;
					newMax[col] += av;
				}
			}
		}
		// set the values to this box
		mMinX = newMin[0];
		mMinY = newMin[1];
		mMinZ = newMin[2];
		mMaxX = newMax[0];
		mMaxY = newMax[1];
		mMaxZ = newMax[2];
	}
	
	/**
	 * @return true if this bounding box is set
	 */
	public boolean isSet() {
		return (mMinX != mMaxX || mMinY != mMaxY || mMinZ != mMaxZ);
	}
}


