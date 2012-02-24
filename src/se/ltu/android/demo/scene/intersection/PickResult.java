/* SVN FILE: $Id: PickResult.java 24 2009-08-17 15:32:24Z belse $ */
package se.ltu.android.demo.scene.intersection;

import se.ltu.android.demo.scene.Spatial;

/**
 * Contains the result from testing ray intersections against a scenes elements.<br><br>
 * <bold>Note:</bold>Currently it only holds the closest intersecting spatial since there
 * was no need for more at the time.
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision: 24 $
 * @lastmodified $Date: 2009-08-18 00:32:24 +0900 (2009-08-18, 화) $
 */
public class PickResult {
	Spatial mSpatial;
	float mDistance;

	/**
	 * Add an intersecting spatial to the result
	 * @param spatial intersecting spatial
	 * @param distance distance to the intersection point
	 */
	public void add(Spatial spatial, float distance) {
		if(mSpatial == null || distance < this.mDistance) {
			this.mSpatial = spatial;
			this.mDistance = distance;
		}
	}
	
	/**
	 * @return the closest spatial
	 */
	public Spatial getClosest() {
		return mSpatial;
	}
	
	/**
	 * @return true if there is at least one result
	 */
	public boolean hasResult() {
		return mSpatial != null;
	}
	
	public String toString() {
		if(mSpatial != null) {
			return "s: "+mSpatial.toString() + ", d: "+mDistance;
		}
		return "empty";
	}
}
