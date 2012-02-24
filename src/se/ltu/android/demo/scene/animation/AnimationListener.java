/* SVN FILE: $Id: AnimationListener.java 24 2009-08-17 15:32:24Z belse $ */
package se.ltu.android.demo.scene.animation;

import se.ltu.android.demo.scene.Spatial;

/**
 * Any class that want to able to listen to changes on an animation should implement
 * this interface and then register itself on the animations setListener() method.
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision: 24 $
 * @lastmodified $Date: 2009-08-18 00:32:24 +0900 (2009-08-18, 화) $
 */
public interface AnimationListener {
	
	/**
	 * Tells the listeners that the animation has ended.
	 * @param anim the animation that started the event
	 * @param spatial the animated spatial
	 */
	public void onAnimationEnd(KeyFrameAnimation anim, Spatial spatial);
}
