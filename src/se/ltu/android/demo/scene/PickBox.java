/* SVN FILE: $Id: PickBox.java 24 2009-08-17 15:32:24Z belse $ */
package se.ltu.android.demo.scene;

import se.ltu.android.demo.scene.intersection.AABBox;

/**
 * Represents a bounding box that can be placed anywhere in
 * a scene. As an example, it is good for creating one or more 
 * pickable areas inside a solid mesh.
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision: 24 $
 * @lastmodified $Date: 2009-08-18 00:32:24 +0900 (2009-08-18, 화) $
 */
public class PickBox extends MetaLeaf {
	AABBox mModelBound;
	
	/**
	 * Creates a new instance with the given name and the given bounding volume
	 * @param name name of the instance
	 * @param bound custom bounding volume
	 */
	public PickBox(String name, AABBox bound) {
		super(name);
		mModelBound = bound;
	}

	/**
	 * Sets the bounding volume.
	 * @param bound custom bounding volume
	 */
	public void setModelBound(AABBox bound) {
		mModelBound = bound;
	}

	/**
	 * Empty implementation. Does nothing on a PickBox. Use <code>setModelBound()</code>
	 * if you want to change the model bound.
	 * @see se.ltu.android.demo.scene.Spatial#updateModelBound()
	 */
	@Override
	public void updateModelBound() {
	}

	/**
	 * Updates the world bound based on the model bound set for this object.
	 */
	@Override
	public void updateWorldBound(boolean propagate) {
		mWorldBound.transform(mTransformMatrix, mModelBound);
		if(propagate && mParent != null) {
			mParent.updateWorldBound(this);
		}
	}
}
