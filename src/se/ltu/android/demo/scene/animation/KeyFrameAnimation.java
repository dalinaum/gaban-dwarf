/* SVN FILE: $Id: KeyFrameAnimation.java 24 2009-08-17 15:32:24Z belse $ */
package se.ltu.android.demo.scene.animation;

import java.util.ArrayList;

import se.ltu.android.demo.scene.Spatial;

import android.util.Log;
import android.view.animation.Interpolator;

/**
 * A key frame-based animation path. A key frame is nothing more than
 * a transformation and a point in time. Any spatial affected by this
 * animation will move between the set transformation at the specified
 * moments in time.<br><br>
 * 
 * <strong>Note:</strong> currently it only support translations. While
 * implementing scaling is easy, the scene should support quaternions
 * to effectively interpolate the rotation between two key frames.
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision: 24 $
 * @lastmodified $Date: 2009-08-18 00:32:24 +0900 (2009-08-18, 화) $
 */
public class KeyFrameAnimation {
	private ArrayList<KeyFrame> mFrames = new ArrayList<KeyFrame>();
	private long mCurTime;
	private int mCurIndex;
	private int mNextIndex;
	private KeyFrame mCurFrame;
	private KeyFrame mNextFrame;
	private long mLastFrameTime = -1;
	private boolean mIsRunning = false;
	private float[] mTmpTrans = new float[3];
	private boolean mIsPrepared;
	private Interpolator mInterpolator;
	private AnimationListener mListener;
	
	/**
	 * Creates a new empty instance
	 */
	public KeyFrameAnimation() {
	}
	
	/**
	 * Creates a new empty instance that notifies the given
	 * listener on changes.
	 * @param listener AnimationListener to be notified of any changes
	 */
	public KeyFrameAnimation(AnimationListener listener) {
		this.mListener = listener;
	}

	/**
	 * Set the animation listener
	 * 
	 * @param listener
	 *            an AnimationListener or null if this animation should not
	 *            notify any changes
	 */
	public void setListener(AnimationListener listener) {
		this.mListener = listener;
	}

	/**
	 * Adds a frame to the animation path. If there already exists a frame with
	 * the same time as the added frame; that frame is first removed before the
	 * given frame is added.
	 * 
	 * @param frame
	 */
	public void addFrame(KeyFrame frame) {
		int len = mFrames.size();
		if (mLastFrameTime < frame.mTime) {
			// add last instead of checking the whole array
			mFrames.add(frame);
			mLastFrameTime = frame.mTime;
			return;
		}
		for (int i = 0; i < len; i++) {
			if (mFrames.get(i).mTime == frame.mTime) {
				// replace duplicate frame at the same position
				mFrames.remove(i);
				mFrames.add(i - 1, frame);
				return;
			}
			if (mFrames.get(i).mTime > frame.mTime) {
				// insert just before the larger element
				mFrames.add(i, frame);
				return;
			}
		}
	}

	/**
	 * Prepares the animation to be run. Any missing information in each frame
	 * is filled in and creates a first frame.
	 * @param spatial spatial to fill in missing information from
	 */
	public void prepare(Spatial spatial) {
		if (mFrames.size() == 0) {
			return;
		}
		if (mFrames.size() == 1 && mFrames.get(0).mTime == 0) {
			Log.e("TAG", "Animation is incomplete");
			return;
		}
		// insert the initial frame, if we miss one
		if (mFrames.get(0).mTime != 0) {
			KeyFrame startFrame = new KeyFrame(0);
			// startFrame.setRotation(spatial.getLocalRotation());
			// startFrame.setScale(spatial.getLocalScale());
			if (spatial.getLocalTranslation() != null) {
				startFrame.setTranslation(spatial.getLocalTranslation());
			} else {
				startFrame.setTranslation(0, 0, 0);
			}
			mFrames.add(0, startFrame);
		}
		// TODO interpolate missing information
		int len = mFrames.size();
		KeyFrame frame;
		for (int i = 0; i < len; i++) {
			frame = mFrames.get(i);
			if (frame.mTranslation == null) {
				frame.setTranslation(0, 0, 0);
			}
		}

		mIsPrepared = true;
		reset();
	}

	/**
	 * Removes a frame from the animation path.
	 * 
	 * @param frame
	 *            frame to remove
	 * @return true if a frame was found and removed
	 */
	public boolean removeFrame(KeyFrame frame) {
		return mFrames.remove(frame);
	}

	/**
	 * Resets the animation back to the first frame and (re)starts the animation.
	 */
	public void reset() {
		mCurTime = 0;
		mCurIndex = -1;
		mNextIndex = 0;
		if (mIsPrepared) {
			frameChange(null);
			mIsRunning = true;
		}
	}

	/**
	 * Updates the animation based on the current time per frame This method is
	 * called from a spatial.
	 * 
	 * @param tpf current time per frame
	 * @param caller spatial that called the update
	 */
	public void update(long tpf, Spatial caller) {
		if (mIsRunning) {
			mCurTime += tpf;

			// handle frame change
			if (mCurTime > mNextFrame.mTime) {
				if (mCurTime > mLastFrameTime) {
					caller.setLocalTranslation(
							mFrames.get(mFrames.size() - 1).mTranslation);
				}
				frameChange(caller);
			}
			// ratio between frames
			float frameRatio = (mCurTime - mCurFrame.mTime)
					/ ((float) (mNextFrame.mTime - mCurFrame.mTime));
			if (mInterpolator != null) {
				frameRatio = mInterpolator.getInterpolation(frameRatio);
			}

			float[] nextTrans = mNextFrame.getTranslation();
			float[] curTrans = mCurFrame.getTranslation();
			mTmpTrans[0] = curTrans[0] + (nextTrans[0] - curTrans[0])
					* frameRatio;
			mTmpTrans[1] = curTrans[1] + (nextTrans[1] - curTrans[1])
					* frameRatio;
			mTmpTrans[2] = curTrans[2] + (nextTrans[2] - curTrans[2])
					* frameRatio;
			synchronized (caller) {
				caller.setLocalTranslation(mTmpTrans);
				caller.updateTransform();
				caller.updateWorldBound(false);
			}
		}
	}

	/**
	 * Set the interpolator that will change the behavior between
	 * any two key frames.
	 * @param ip interpolator to set
	 */
	public void setInterpolator(Interpolator ip) {
		this.mInterpolator = ip;
	}

	/**
	 * Handles a frame change
	 */
	// caller is only needed for notifying listener, could be null
	private void frameChange(Spatial caller) {
		if (mCurTime >= mLastFrameTime) {
			// end of animation
			// TODO implement some kind of wrapping mechanism
			mIsRunning = false;
			if(mListener != null) {
				mListener.onAnimationEnd(this, caller);
			}
			return;
		}
		mCurFrame = mFrames.get(++mCurIndex);
		mNextFrame = mFrames.get(++mNextIndex);
		while (mCurTime < mCurFrame.mTime || mCurTime > mNextFrame.mTime) {
			mCurFrame = mFrames.get(++mCurIndex);
			mNextFrame = mFrames.get(++mNextIndex);
		}
	}
}
