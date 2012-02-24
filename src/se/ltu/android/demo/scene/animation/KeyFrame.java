/* SVN FILE: $Id: KeyFrame.java 24 2009-08-17 15:32:24Z belse $ */
package se.ltu.android.demo.scene.animation;

import java.security.InvalidParameterException;

import android.util.Log;

/**
 * A key frame consists of a transformation and a point in time.
 * It's the building stone of a key frame animation path.
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision: 24 $
 * @lastmodified $Date: 2009-08-18 00:32:24 +0900 (2009-08-18, 화) $
 */
public class KeyFrame implements Comparable<KeyFrame> {
	private final static String TAG = "Frame";
	protected float[] mRotation;
	protected float[] mTranslation;
	protected float[] mScale;
	protected long mTime;

	/**
	 * Creates a new frame with the given time stamp.
	 * The time is relative to another frame and not based on
	 * the current system time. The time can never be negative. If
	 * a negative time value is given, the value is set to zero
	 * and a warning is logged.
	 * 
	 * @param time time in milliseconds
	 */
	public KeyFrame(long time) {
		if(time < 0) {
			Log.w(TAG, "Got a negative time stamp, setting it to zero");
			time = 0;
		}
		this.mTime = time;
	}
	
	/**
	 * Compares this frame with another frame based on the
	 * time set for each frame. The transformations are not tested.
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(KeyFrame other) {
		if(this.mTime < other.mTime) {
			return -1;
		}
		if(this.mTime == other.mTime) {
			return 0;
		}
		return 1;
	}
	
	/**
	 * Compares this frame with another frame. They are
	 * considered equal if they occur at the same time.
	 * Transformations are <b>not</b> checked.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof KeyFrame))
			return false;
		KeyFrame other = (KeyFrame) obj;
		if (mTime != other.mTime)
			return false;
		return true;
	}

	/**
	 * @return the rotation
	 */
	public float[] getRotation() {
		return mRotation;
	}
	
	/**
	 * @return the scale
	 */
	public float[] getScale() {
		return mScale;
	}
	
	/**
	 * @return the time when this key frame should occur
	 */
	public long getTime() {
		return mTime;
	}
	
	/**
	 * @return the translation
	 */
	public float[] getTranslation() {
		return mTranslation;
	}
	
	/**
	 * Set the rotation specified by an angle and an axis of rotation
	 * @param angle angle in degrees
	 * @param x axis of rotation x coordinate
	 * @param y axis of rotation y coordinate
	 * @param z axis of rotation z coordinate
	 */
	public void setRotation(float angle, float x, float y, float z) {
		if(mRotation == null) {
			mRotation = new float[4];
		}
		mRotation[0] = angle;
		mRotation[1] = x;
		mRotation[2] = y;
		mRotation[3] = z;
	}
	
	/**
	 * Set the rotation specified by an angle and an axis of rotation
	 * @param rotation4f rotation to set
	 */
	public void setRotation(float[] rotation4f) {
		if(rotation4f == null) {
			mRotation = null;
			return;
		}
		if(rotation4f.length != 3) {
			throw new InvalidParameterException(
					"Invalid length of array, got "+rotation4f.length+", expected 3");
		}
		if(mRotation == null) {
			mRotation = new float[3];
		}
		mRotation[0] = rotation4f[0];
		mRotation[1] = rotation4f[1];
		mRotation[2] = rotation4f[2];
		mRotation[3] = rotation4f[3];
	}
	
	/**
	 * Set the scale
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setScale(float x, float y, float z) {
		if(mScale == null) {
			mScale = new float[3];
		}
		mScale[0] = x;
		mScale[1] = y;
		mScale[2] = z;
	}
	
	/**
	 * Set the scale
	 * @param scale3f scale to set
	 */
	public void setScale(float[] scale3f) {
		if(scale3f == null) {
			mScale = null;
			return;
		}
		if(scale3f.length != 3) {
			throw new InvalidParameterException(
					"Invalid length of array, got "+scale3f.length+", expected 3");
		}
		if(mScale == null) {
			mScale = new float[3];
		}
		mScale[0] = scale3f[0];
		mScale[1] = scale3f[1];
		mScale[2] = scale3f[2];
	}
	
	/**
	 * Set the translation
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param z z coordinate
	 */
	public void setTranslation(float x, float y, float z) {
		if(mTranslation == null) {
			mTranslation = new float[3];
		}
		mTranslation[0] = x;
		mTranslation[1] = y;
		mTranslation[2] = z;
	}
	
	/**
	 * Set the translation
	 * @param trans3f translation to set
	 */
	public void setTranslation(float[] trans3f) {
		if(trans3f == null) {
			mTranslation = null;
			return;
		}
		if(trans3f.length != 3) {
			throw new InvalidParameterException(
					"Invalid length of array, got "+trans3f.length+", expected 3");
		}
		if(mTranslation == null) {
			mTranslation = new float[3];
		}
		mTranslation[0] = trans3f[0];
		mTranslation[1] = trans3f[1];
		mTranslation[2] = trans3f[2];
	}
}