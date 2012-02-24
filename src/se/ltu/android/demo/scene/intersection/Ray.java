/* SVN FILE: $Id: Ray.java 24 2009-08-17 15:32:24Z belse $ */
package se.ltu.android.demo.scene.intersection;

/**
 * A basic ray with intersection test. 
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision: 24 $
 * @lastmodified $Date: 2009-08-18 00:32:24 +0900 (2009-08-18, 화) $
 */
public class Ray {	
	private float mX, mY, mZ;		// ray origin	
	private float mXDirection, mYDirection, mZDirection;		// ray direction	
	private float mInvXDirection, mInvYDirection, mInvZDirection;	// inverses of direction components
	private boolean mSignOfInvXDirection, mSignOfInvYDirection, mSignOfInvZDirection;
	
	/**
	 * Constructs a new ray
	 * @param x origin x-coordinate
	 * @param y origin y-coordinate
	 * @param z origin z-coordinate
	 * @param i direction x-coordinate
	 * @param j direction y-coordinate
	 * @param k direction z-coordinate
	 */
	public Ray(float x, float y, float z, float i, float j, float k) {
		this.mX = x;
		this.mY = y;
		this.mZ = z;
		this.mXDirection = i;
		this.mYDirection = j;
		this.mZDirection = k;
		
		// inverses of direction component
		this.mInvXDirection = 1.0f/this.mXDirection;
		this.mInvYDirection = 1.0f/this.mYDirection;
		this.mInvZDirection = 1.0f/this.mZDirection;
		this.mSignOfInvXDirection = (mInvXDirection >= 0);
		this.mSignOfInvYDirection = (mInvYDirection >= 0);
		this.mSignOfInvZDirection = (mInvZDirection >= 0);
	} // public Ray(float x, float y, float z, float i, float j, float k)
	
	/**
	 * Calculates whether or not the ray intersects an axis-aligned bounding box.
	 * Same as calling <code>intersects(box, null)</code>
	 * @param box an axis-aligned bounding box
	 * @return true if this ray intersects the box
	 */
	public boolean intersects(AABBox box){
		return intersects(box, null);
	}
	
	/**
	 * Calculates whether or not the ray intersects an axis-aligned bounding box.
	 * @param box an axis-aligned bounding box
	 * @param distance the resulting distance from the origin of this ray to the 
	 * intersection point of the box, only valid if this method returns true.
	 * @return true if this ray intersects the box
	 */
	public boolean intersects(AABBox box, float[] distance) {
		float tmin, tmax, tymin, tymax, tzmin, tzmax;
		float t0 = Float.NEGATIVE_INFINITY;
		float t1 = Float.POSITIVE_INFINITY;
		
		if (mSignOfInvXDirection) {
			tmin = (box.mMinX - mX) * mInvXDirection;
			tmax = (box.mMaxX - mX) * mInvXDirection;
		}
		else {
			tmin = (box.mMaxX - mX) * mInvXDirection;
			tmax = (box.mMinX - mX) * mInvXDirection;
		}
		if (mSignOfInvYDirection) {
			tymin = (box.mMinY - mY) * mInvYDirection;
			tymax = (box.mMaxY - mY) * mInvYDirection;
		}
		else {
			tymin = (box.mMaxY - mY) * mInvYDirection;
			tymax = (box.mMinY - mY) * mInvYDirection;
		}
		if ( (tmin > tymax) || (tymin > tmax) ) {
			return false;
		}
		if (tymin > tmin) {
			tmin = tymin;
		}
		if (tymax < tmax) {
			tmax = tymax;
		}
		
		if (mSignOfInvZDirection) {
			tzmin = (box.mMinZ - mZ) * mInvZDirection;
			tzmax = (box.mMaxZ - mZ) * mInvZDirection;
		}
		else {
			tzmin = (box.mMaxZ - mZ) * mInvZDirection;
			tzmax = (box.mMinZ - mZ) * mInvZDirection;
		}
	  if ( (tmin > tzmax) || (tzmin > tmax) ) {
	    return false;
	  }
	  if (tzmin > tmin) {
	    tmin = tzmin;
	  }
	  if (tzmax < tmax) {
	    tmax = tzmax;
	  }
	  
	  if(tmin < t1 && tmax > t0) {
		  if(distance != null && distance.length > 0) {
			  distance[0] = tmin;
		  }
		  return true;
	  }
	  return false;
	}
}
