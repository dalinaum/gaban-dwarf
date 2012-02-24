/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.Matrix;
import android.util.Log;

import se.ltu.android.demo.scene.animation.KeyFrameAnimation;
import se.ltu.android.demo.scene.intersection.AABBox;
import se.ltu.android.demo.scene.intersection.PickResult;
import se.ltu.android.demo.scene.intersection.Ray;
import se.ltu.android.demo.scene.state.Material;

/**
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public abstract class Spatial {
	private final static String TAG = "Spatial";
	protected Container mParent;
	protected String mName = "unnamed node";
	protected AABBox mWorldBound;
	protected ArrayList<KeyFrameAnimation> mAnimations;
	protected Boolean mIsPickable = true; 
	
	/**
	 * Transformation matrix
	 */
	protected float[] mTransformMatrix = new float[16];
	protected float[] mLocalTranslation = null;
	protected float[] mLocalRotation = null;
	protected float[] mLocalScale = null;
	
	private Object mDataObject; // would be needed ??
		
	public Spatial(String name) {
		this.mName = name;
		mWorldBound = new AABBox();
		Matrix.setIdentityM(mTransformMatrix, 0);
	}
	
	/**
	 * Draw the geometry or go through the children if it's not a geometry
	 * @param gl
	 */
	public abstract void draw(GL10 gl);
	
	public String getName() {
		return mName;
	}

	public boolean hasParent() {
		return mParent != null;
	}
	
	public void detachFromParent() {
		mParent.detachChild(this);
	}
	
	public float[] getLocalTranslation() {
		return mLocalTranslation;
	}
	
	public float[] getLocalRotation() {
		return mLocalRotation;
	}
	
	public float[] getLocalScale() {
		return mLocalScale;
	}
	
	public float[] getTransform() {
		return mTransformMatrix;
	}
	
	public void setLocalTranslation(float x, float y, float z) {
		if(mLocalTranslation == null) {
			mLocalTranslation = new float[3];
		}
		mLocalTranslation[0] = x;
		mLocalTranslation[1] = y;
		mLocalTranslation[2] = z;
	}
	
	public void setLocalTranslation(float[] translation) {
		if(translation != null && translation.length == 3) {
			if(mLocalTranslation == null) {
				mLocalTranslation = new float[3];
			}
			mLocalTranslation[0] = translation[0];
			mLocalTranslation[1] = translation[1];
			mLocalTranslation[2] = translation[2];
		}
	}
	
	public void setLocalRotation(float angle, float x, float y, float z) {
		if(mLocalRotation == null) {
			mLocalRotation = new float[4];
		}
		mLocalRotation[0] = angle;
		mLocalRotation[1] = x;
		mLocalRotation[2] = y;
		mLocalRotation[3] = z;
	}
	
	public void setLocalRotation(float[] rotation) {
		if(rotation != null && rotation.length == 4) {
			if(mLocalRotation == null) {
				mLocalRotation = new float[4];
			}
			mLocalRotation[0] = rotation[0];
			mLocalRotation[1] = rotation[1];
			mLocalRotation[2] = rotation[2];
		}
	}
	
	public void setLocalScale(float[] scale) {
		if(scale != null && scale.length == 3) {
			if(mLocalScale == null) {
				mLocalScale = new float[3];
			}
			mLocalScale[0] = scale[0];
			mLocalScale[1] = scale[1];
			mLocalScale[2] = scale[2];
		}
	}
	
	public void setLocalScale(float x, float y, float z) {
		if(mLocalScale == null) {
			mLocalScale = new float[3];
		}
		mLocalScale[0] = x;
		mLocalScale[1] = y;
		mLocalScale[2] = z;
	}
	
	public void setTransform(float[] transM) {
		if(transM == null || transM.length != 16) {
			return;
		}
		for(int i = 0; i < 16; i++) {
			this.mTransformMatrix[i] = transM[i];
		}
	}

	/**
	 * Test this spatial for equality with another spatial.
	 * They are considered equal if the names are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Spatial))
			return false;
		Spatial other = (Spatial) obj;
		if (mName == null) {
			if (other.mName != null)
				return false;
		} else if (!mName.equals(other.mName))
			return false;
		if (mDataObject == null) {
			if (other.mDataObject != null)
				return false;
		} else if (!mDataObject.equals(other.mDataObject))
			return false;
		return true;
	}
	
	/**
	 * Updates the bounding volume for this spatial
	 */
	public abstract void updateModelBound();
	
	/**
	 * Updates the world bound for this spatial and
	 * propagate the changes up to the root if wanted.
	 * @param propagate set to true if we want to propagate the
	 * changes up to the root 
	 */
	public abstract void updateWorldBound(boolean propagate);
	
	/**
	 * Updates the world transformation matrix for this spatial
	 */
	public void updateTransform() {
		if(mParent != null) {
			for(int i = 0; i < 16; i++) {
				mTransformMatrix[i] = mParent.mTransformMatrix[i];
			}
		} else {
			Matrix.setIdentityM(mTransformMatrix, 0);
		}
		
		if (mLocalTranslation != null) {
			Matrix.translateM(mTransformMatrix, 0, mLocalTranslation[0], mLocalTranslation[1], mLocalTranslation[2]);
		}
		if (mLocalRotation != null) {
			Matrix.rotateM(mTransformMatrix, 0, mLocalRotation[0], mLocalRotation[1], mLocalRotation[2], mLocalRotation[3]);
		}
		if (mLocalScale != null) {
			Matrix.scaleM(mTransformMatrix, 0, mLocalScale[0], mLocalScale[1], mLocalScale[2]);
		}
	}
	
	/**
	 * @return the world bound
	 */
	public AABBox getWorldBound() {
		return mWorldBound;
	}
	
	/**
	 * Check for intersections between this spatial and
	 * a ray. It must be passed PickResult where the results
	 * will end up.
	 * @param ray ray to test against
	 * @param result contains the results when the method returns
	 */
	public void calculatePick(Ray ray, PickResult result) {
		if(result == null) {
			Log.w(TAG, "PickResult is null in "+mName);
			return;
		}
		
		if(mIsPickable) {
			float[] distance = new float[1];
			if(ray.intersects(mWorldBound, distance)) {
				result.add(this, distance[0]);
			}
		}
	}
	
	/**
	 * Add an animation controller to this spatial
	 * @param anim animation controller to add
	 */
	public void addController(KeyFrameAnimation anim) {
		if(mAnimations == null) {
			mAnimations = new ArrayList<KeyFrameAnimation>();
		}
		anim.prepare(this);
		mAnimations.add(anim);
	}
	
	/**
	 * Removes all animation controllers from this spatial
	 */
	public void clearControllers() {
		mAnimations.clear();
	}
	
	/**
	 * Remove a specific animation controller
	 * @param anim animation controller to remove
	 */
	public void removeController(KeyFrameAnimation anim) {
		mAnimations.remove(anim);
	}
	
	/**
	 * @return the parent node of this spatial
	 */
	public Container getParent() {
		return mParent;
	}
	
	/**
	 * Updates the animation controllers of this spatial
	 * @param tpf time in milliseconds since last update
	 */
	public void update(long tpf) {
		if(mAnimations != null) {
			int len = mAnimations.size();
			for(int i = 0; i < len; i++) {
				mAnimations.get(i).update(tpf, this);
			}
		}
	}
	
	/**
	 * Returns the name of this spatial
	 */
	public String toString() {
		return mName;
	}

	/**
     * Deletes the hardware buffers allocated by this object (if any).
     */
	public abstract void freeHardwareBuffers(GL10 gl);

	/** 
     * When the OpenGL ES device is lost, GL handles become invalidated.
     * In that case, we just want to "forget" the old handles (without
     * explicitly deleting them) and make new ones.
     */
	public abstract void forgetHardwareBuffers();

	/** 
     * Allocates hardware buffers on the graphics card and fills them with
     * data if a buffer has not already been previously allocated.  Note that
     * this function uses the GL_OES_vertex_buffer_object extension, which is
     * not guaranteed to be supported on every device.
     * @param gl  A pointer to the OpenGL ES context.
     */
	public abstract void generateHardwareBuffers(GL10 gl);
	
	/**
	 * Set if this object should be tested for intersections with a
	 * pick ray and end up in a PickResult.<br>
	 * <br>
	 * Default is true.
	 * @param pickable true if this object should be pickable
	 */
	public void setPickable(boolean pickable) {
		this.mIsPickable = pickable;
	}
	
	/**
	 * Tells whether or not this object is tested for intersections with
	 * a pick ray.
	 * @return true if this object is pickable
	 */
	public boolean isPickable() {
		return mIsPickable;
	}

	/**
	 * Set an object that contains application specific information
	 * about this spatial
	 * @param data the data object to set
	 */
	public void setData(Object data) {
		this.mDataObject = data;
	}

	/**
	 * @return the data object
	 */
	public Object getData() {
		return mDataObject;
	}
	
	/**
	 * @return true if this object has a data object
	 */
	public boolean hasData() {
		return (mDataObject != null);
	}
	
	/**
	 * @param material material to set or null to clear
	 */
	public abstract void setMaterial(Material material);
}
