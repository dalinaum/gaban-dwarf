/* SVN FILE: $Id: Object3D.java 26 2009-08-18 11:44:23Z belse $ */
package se.ltu.android.demo.scene;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import se.ltu.android.demo.scene.intersection.AABBox;
import se.ltu.android.demo.scene.state.Material;
import se.ltu.android.demo.util.BufferUtils;

import android.opengl.Matrix;
import android.util.Log;

/**
 * A basic geometrical object that is drawn with triangles.
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision: 26 $
 * @lastmodified $Date: 2009-08-18 20:44:23 +0900 (2009-08-18, 화) $
 */
public class Object3D extends Spatial {
	private static final String TAG = "Object3D";
	public static final int MODE_TRIANGLES = GL10.GL_TRIANGLES;
	public static final int MODE_TRIANGLE_STRIP = GL10.GL_TRIANGLE_STRIP;
	public static final int MODE_TRIANGLE_FAN = GL10.GL_TRIANGLE_FAN;
	
	protected AABBox mModelBound;
	protected int mDrawMode = MODE_TRIANGLES;
	protected int mVertexCount = 0;
	/**
	 * Local center point of this geometry in x,y,z.
	 */
	protected float[] mCenter = new float[3];
	/**
	 * Direct access to this Object3D's vertices. If you modify this data
	 * you must set the hasDirtyModelBound to true or the model bound
	 * will not be updated.
	 */
	protected FloatBuffer mVertices;
	// char instead of short, since char is unsigned
	protected CharBuffer mIndices;
	protected FloatBuffer mNormals;
	protected ByteBuffer mColors;
	protected FloatBuffer mTexcoords;
	protected boolean mHasDirtyModelBound = true;
	protected Object3D mCloneTarget = null;
	
	// VBO buffer pointers
	private int mVertBufferIndex;
	private int mIndexBufferIndex;
	private int mColorBufferIndex;
	private int mNormalBufferIndex;
	private int mTexCoordsBufferIndex;
	private int mIndexCount;
	private Material mMaterial;

        /**
         * Creates a new empty instance
         * @param name name of the object, for identifying purposes
         */
	public Object3D(String name) {
		super(name);
		mModelBound = new AABBox();
	}

        /**
         * Creates a new instance with the supplied vertices and indices
         * @param name name of the object, for identifying purposes
         * @param vertices vertices to set
         * @param indices indices to set
         */
	public Object3D(String name, FloatBuffer vertices, CharBuffer indices) {
		super(name);
		if(vertices.limit() % 3 != 0) {
			Log.e(TAG, "Invalid vertex array length (Found: "
					+vertices.limit()+", not divisable by 3) in "+name);
			return;
		}
		mVertexCount = vertices.limit() / 3;
		this.mVertices = vertices;
		this.mIndices = indices;
	}

	/**
	 * Creates a clone of this mesh. 
	 * The clone shares vertices, indices etc
	 * with the original but the buffers are write protected.
	 * If you change the original Object3D's buffers, those changes
	 * will be visible in the clone.
	 * @return the cloned Object3D
	 */
	public Object3D cloneMesh() {
		return cloneMesh(mName);
	}
	
	/**
	 * Creates a clone of this mesh. The clone shares vertices, indices etc
	 * with the original but the buffers are write protected.
	 * If you change the <i>elements</i> of the original Object3D's buffers, those changes
	 * will be visible in the clone. The result is <i>undefined</i> if you create new
         * buffers on the original mesh.
	 * @param name name of the clone
	 * @return the cloned Object3D
	 */
	public Object3D cloneMesh(String name) {
		if(mVertices == null || mIndices == null) {
			Log.e(TAG, "Can not clone a Object3D with no vertices or indices");
			return null;
		}
		
		Object3D clone = new Object3D(name);
		clone.mCloneTarget = this;
		clone.mModelBound = mModelBound;
		clone.mDrawMode = mDrawMode;
		clone.mVertexCount = mVertexCount;
		clone.mVertices = mVertices.asReadOnlyBuffer();
		clone.mIndices = mIndices.asReadOnlyBuffer();
		if(mNormals != null) {
			clone.mNormals = mNormals.asReadOnlyBuffer();
		}
		if(mColors != null) {
			clone.mColors = mColors.asReadOnlyBuffer();
		}
		if(mTexcoords != null) {
			clone.mTexcoords = mTexcoords.asReadOnlyBuffer();
		}
		
		clone.mMaterial = mMaterial;
		
		clone.setLocalTranslation(mLocalTranslation);
		clone.setLocalRotation(mLocalRotation);
		clone.setLocalScale(mLocalScale);
		return clone;
	}
	
	@Override
	public void draw(GL10 gl) {
		// test for null first so we can return without manipulating the stack
		if(mVertices == null) {
			Log.e(TAG, "Vertices are null in: "+mName);
			return;
		}
		if(mIndices == null) {
			Log.e(TAG, "Vertices are null in: "+mName);
			return;
		}
		gl.glPushMatrix();
		gl.glMultMatrixf(mTransformMatrix, 0);

		if(mMaterial != null) {
			mMaterial.applyState(gl);
		} else {
			Material.removeState(gl);
		}
		
		if (mVertBufferIndex == 0) {
			
			mVertices.rewind();
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertices);
			
			// enable non-mandatory arrays if found
			if(mColors != null) {
				mColors.rewind();
				gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
				gl.glColorPointer(4, GL10.GL_UNSIGNED_BYTE, 0, mColors);
			}
			if(mNormals != null) {
				mNormals.rewind();
				gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
				gl.glNormalPointer(GL10.GL_FLOAT, 0, mNormals);
			}
			
			// do the drawing
			mIndices.rewind();
	    	gl.glDrawElements(mDrawMode, mIndices.limit(), GL10.GL_UNSIGNED_SHORT, mIndices);
	    	
	    	// disable non-mandatory arrays
	    	if(mColors != null) {
	    		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
	    	}
	    	if(mNormals != null) {
	    		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
	    	}
    	
		} else { // use VBO's
            GL11 gl11 = (GL11)gl;
            
            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertBufferIndex);
            gl11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
            
            // enable non-mandatory arrays if found
			if(mColorBufferIndex != 0) {
				gl11.glEnableClientState(GL11.GL_COLOR_ARRAY);
				gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mColorBufferIndex);
				gl11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 0, 0);
			}
			if(mNormalBufferIndex != 0) {
				gl11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
				gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mNormalBufferIndex);
				gl11.glNormalPointer(GL11.GL_FLOAT, 0, 0);
			}
			if(mTexCoordsBufferIndex != 0) {
				gl11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mTexCoordsBufferIndex);
				gl11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
			}
            
            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mIndexBufferIndex);
            gl11.glDrawElements(mDrawMode, mIndexCount,
                    GL11.GL_UNSIGNED_SHORT, 0);
            
            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
            
            if(mColorBufferIndex != 0) {
				gl11.glDisableClientState(GL11.GL_COLOR_ARRAY);
			}
			if(mNormalBufferIndex != 0) {
				gl11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
			}
			if(mTexCoordsBufferIndex != 0) {
				gl11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);				
			}
        }
		
		gl.glPopMatrix();
	}
	
	/**
	 * @param colorArray colors to set
	 */
	public void setColors(byte[] colorArray) {
		int size = colorArray.length;
		if(size != mVertexCount * 4) {
			Log.e(TAG, "Invalid array length (Expected: "
					+mVertexCount*4+", Found: "+size+") in "+mName);
			return;
		}
		if(mColors == null || mColors.capacity() != size) {
			mColors = BufferUtils.createByteBuffer(size);
		}
		mColors.clear();
		mColors.put(colorArray);
		return;
	}

        /**
         * Set the OpenGL draw mode of this mesh.
         * @param mode an integer defining the mode (see Object3D.MODE*).
         */
	public void setDrawMode(int mode) {
		if(mode != MODE_TRIANGLE_FAN ||
		   mode != MODE_TRIANGLE_STRIP ||
		   mode != MODE_TRIANGLES) {
			Log.e(TAG, "Unrecognized draw mode");
			return;
		}
		mDrawMode = mode;
	}

        /**
         * @param indexArray indices to set
         */
	public void setIndices(char[] indexArray) {
		int size = indexArray.length;
		if(mIndices == null || mIndices.capacity() != size) {
			mIndices = BufferUtils.createCharBuffer(size);
		}
		mIndices.clear();
		mIndices.put(indexArray);
		return;
	}
	
	/**
	 * Sets the bounding volume.
	 * @param bound custom bounding volume
	 */
	public void setModelBound(AABBox bound) {
		mModelBound = bound;
		mHasDirtyModelBound = false;
	}
	
	/**
	 * @param normalArray normals to set
	 */
	public void setNormals(float[] normalArray) {
		int size = normalArray.length;
		if(size != mVertexCount * 3) {
			Log.e(TAG, "Invalid normal array length (Expected: "
					+4+", Found: "+size+") in "+mName);
			return;
		}
		if(mNormals == null || mNormals.capacity() != size) {
			mNormals = BufferUtils.createFloatBuffer(size);
		}
		mNormals.clear();
		mNormals.put(normalArray);
		return;
	}

        /**
         * @param normals normals to set
         */
	public void setNormals(FloatBuffer normals) {
		this.mNormals = normals;
	}
	
	/**
	 * Set the color for each vertex to the supplied color
	 * @param color4f color to set as floats
	 */
	public void setSolidColor(float[] color4f) {
		int size = color4f.length;
		if(size != 4) {
			Log.e(TAG, "Invalid array length (Expected: "
					+4+", Found: "+size+") in "+mName);
			return;
		}
		byte[] color4b = new byte[4];
		color4b[0] = (byte) ((int)((color4f[0] * 255)) & 0xff);
		color4b[1] = (byte) ((int)((color4f[1] * 255)) & 0xff);
		color4b[2] = (byte) ((int)((color4f[2] * 255)) & 0xff);
		color4b[3] = (byte) ((int)((color4f[3] * 255)) & 0xff);
		setSolidColor(color4b);
		return;
	}
	
	/**
         * Set the color for each vertex to the supplied color
	 * @param color4b color to set as unsigned bytes
	 */
	public void setSolidColor(byte[] color4b) {
		int size = color4b.length;
		if(size != 4) {
			Log.e(TAG, "Invalid array length (Expected: "
					+4+", Found: "+size+") in "+mName);
			return;
		}
		if(mColors == null || mColors.capacity() != mVertexCount*4) {
			mColors = BufferUtils.createByteBuffer(4*mVertexCount);
		}
		mColors.clear();
		for(int i = 0; i < mVertexCount; i++) {
			mColors.put(color4b);
		}
		return;
	}

	/**
	 * @param texcoordsArray texture coordinates to set
	 */
	public void setTexCoords(float[] texcoordsArray) {
		int size = texcoordsArray.length;
		if(size != mVertexCount * 2) {
			Log.e(TAG, "Invalid texture coordinate array length (Expected: "
					+mVertexCount*2+", Found: "+size+") in "+mName);
			return;
		}
		if(mTexcoords == null || mTexcoords.capacity() != size) {
			mTexcoords = BufferUtils.createFloatBuffer(size);
		}
		mTexcoords.clear();
		mTexcoords.put(texcoordsArray);
	}

        /**
         * @param texcoords texture coordinates to set
         */
	public void setTexCoords(FloatBuffer texcoords) {
		this.mTexcoords = texcoords;
	}
	
	/**
	 * @param vertexArray vertices to set
	 */
	public void setVertices(float[] vertexArray) {
		int size = vertexArray.length;
		/*
		if(vertices != null) {
			Log.e(TAG, "Setting vertices twice is forbidden! In "+name);
			return;
		}
		*/
		if(size % 3 != 0) {
			Log.e(TAG, "Invalid vertex array length (Found: "
					+size+", not divisable by 3) in "+mName);
			return;
		}
		mVertexCount = size/3;
		mVertices = BufferUtils.createFloatBuffer(size);
		mVertices.clear();
		mVertices.put(vertexArray);
		mHasDirtyModelBound = true;
		return;
	}
	
	/**
	 * @return a read-only FloatBuffer with this Object3D's vertices
	 */
	public FloatBuffer getVertices() {
		if(mVertices != null) {
			return mVertices.asReadOnlyBuffer();
		}
		return null;
	}

        /**
         * @return indices
         */
	public CharBuffer getIndices() {
		if(mIndices != null) {
			return mIndices.asReadOnlyBuffer();
		}
		return null;
	}
	
	/**
	 * Updates the bounding volume for this mesh
	 * This method uses the mesh's world transformation matrix so
	 * ensure that the matrix is valid (or call updateTransform() on
	 * this mesh before calling this method).
	 */
	@Override
	public void updateModelBound() {
		// For a clone, we update the targets bound
		if(mCloneTarget != null) {
			mCloneTarget.updateModelBound();
			return;
		}
		
		// For an original Object3D, we only update
		// when our vertices has changed
		if(!mHasDirtyModelBound) {
			return;
		}
		
		float tmpX,tmpY,tmpZ;
		int limit = mVertices.limit();
		
		for(int pos = 0; pos < limit; pos += 3) {
			tmpX = mVertices.get(pos);
			tmpY = mVertices.get(pos+1);
			tmpZ = mVertices.get(pos+2);
			if(pos == 0) {
				mModelBound.mMinX = mModelBound.mMaxX = tmpX;
				mModelBound.mMinY = mModelBound.mMaxY = tmpY;
				mModelBound.mMinZ = mModelBound.mMaxZ = tmpZ;
			} else {
				if(tmpX < mModelBound.mMinX)
					mModelBound.mMinX = tmpX;
				if(tmpY < mModelBound.mMinY)
					mModelBound.mMinY = tmpY;
				if(tmpZ < mModelBound.mMinZ)
					mModelBound.mMinZ = tmpZ;
				if(tmpX > mModelBound.mMaxX)
					mModelBound.mMaxX = tmpX;
				if(tmpY > mModelBound.mMaxY)
					mModelBound.mMaxY = tmpY;
				if(tmpZ > mModelBound.mMaxZ)
					mModelBound.mMaxZ = tmpZ;
			}
		}
		
		mHasDirtyModelBound = false;
	}

	@Override
	public void updateWorldBound(boolean propagate) {
		if(mHasDirtyModelBound) {
			updateModelBound();
		}
		mWorldBound.transform(mTransformMatrix, mModelBound);
		if(propagate && mParent != null) {
			mParent.updateWorldBound(this);
		}
	}
	
	@Override
        public void forgetHardwareBuffers() {
		if(mCloneTarget != null) {
			mCloneTarget.forgetHardwareBuffers();
			return;
		}
        mVertBufferIndex = 0;
        mIndexBufferIndex = 0;
        mNormalBufferIndex = 0;
        mTexCoordsBufferIndex = 0;
        mColorBufferIndex = 0;
    }
    
    @Override
    public void freeHardwareBuffers(GL10 gl) {
    	if(mCloneTarget != null) {
    		mCloneTarget.freeHardwareBuffers(gl);
    		forgetHardwareBuffers();
    		return;
    	}
    	
        if (mVertBufferIndex != 0) {
            if (gl instanceof GL11) {
                GL11 gl11 = (GL11)gl;
                int[] buffer = new int[1];
                buffer[0] = mVertBufferIndex;
                gl11.glDeleteBuffers(1, buffer, 0);
                
                buffer[0] = mIndexBufferIndex;
                gl11.glDeleteBuffers(1, buffer, 0);
                
                if(mNormalBufferIndex != 0) {
                	buffer[0] = mNormalBufferIndex;
                	gl11.glDeleteBuffers(1, buffer, 0);
                }
                if(mTexCoordsBufferIndex != 0) {
                	buffer[0] = mTexCoordsBufferIndex;
                	gl11.glDeleteBuffers(1, buffer, 0);
                }
                if(mColorBufferIndex != 0) {
                	buffer[0] = mColorBufferIndex;
                	gl11.glDeleteBuffers(1, buffer, 0);
                }
            }
            forgetHardwareBuffers();
        }
    }
    
    @Override    
    public void generateHardwareBuffers(GL10 gl) {
    	if(mCloneTarget != null) {
    		if (mCloneTarget.mVertBufferIndex == 0) {
    			mCloneTarget.generateHardwareBuffers(gl);
    		}
    		mVertBufferIndex = mCloneTarget.mVertBufferIndex;
            mIndexBufferIndex = mCloneTarget.mIndexBufferIndex;
            mNormalBufferIndex = mCloneTarget.mNormalBufferIndex;
            mTexCoordsBufferIndex = mCloneTarget.mTexCoordsBufferIndex;
            mColorBufferIndex = mCloneTarget.mColorBufferIndex;
            mIndexCount = mCloneTarget.mIndexCount;
            return;
    	}
    	
        if (mVertBufferIndex == 0) {
            if (gl instanceof GL11) {
                GL11 gl11 = (GL11)gl;
                int[] buffer = new int[1];
                
                mVertices.rewind();
                mIndices.rewind();
                
                // Allocate and fill the vertex buffer.
                gl11.glGenBuffers(1, buffer, 0);
                mVertBufferIndex = buffer[0];
                gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertBufferIndex);
                final int vertexSize = mVertices.capacity() * 4;
                gl11.glBufferData(GL11.GL_ARRAY_BUFFER, vertexSize, 
                        mVertices, GL11.GL_STATIC_DRAW);
                
                if(mNormals != null) {
                	mNormals.rewind();
                	gl11.glGenBuffers(1, buffer, 0);
                    mNormalBufferIndex = buffer[0];
                    gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mNormalBufferIndex);
                    final int normalSize = mNormals.capacity() * 4;
                    gl11.glBufferData(GL11.GL_ARRAY_BUFFER, normalSize, 
                            mNormals, GL11.GL_STATIC_DRAW);
                }
                if(mColors != null) {
                	mColors.rewind();
                	gl11.glGenBuffers(1, buffer, 0);
                    mColorBufferIndex = buffer[0];
                    gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mColorBufferIndex);
                    final int colorSize = mColors.capacity();
                    gl11.glBufferData(GL11.GL_ARRAY_BUFFER, colorSize, 
                            mColors, GL11.GL_STATIC_DRAW);
                }
                if(mTexcoords != null) {
                	mTexcoords.rewind();
                	gl11.glGenBuffers(1, buffer, 0);
                    mTexCoordsBufferIndex = buffer[0];
                    gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mTexCoordsBufferIndex);
                    final int texcoordSize = mTexcoords.capacity() * 4;
                    gl11.glBufferData(GL11.GL_ARRAY_BUFFER, texcoordSize, 
                            mTexcoords, GL11.GL_STATIC_DRAW);
                }
                
                // Unbind the array buffer.
                gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
                
                // Allocate and fill the index buffer.
                gl11.glGenBuffers(1, buffer, 0);
                mIndexBufferIndex = buffer[0];
                gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 
                        mIndexBufferIndex);
                // A char is 2 bytes.
                final int indexSize = mIndices.capacity() * 2;
                gl11.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, indexSize, mIndices, GL11.GL_STATIC_DRAW);
                
                // Unbind the element array buffer.
                gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
                
                mIndexCount = mIndices.limit();
                
                // TODO are we safe so delete the java.nio.buffers now??
                // Probably not... since we can lose context every now and then.
            }
        }
    }

	/**
	 * Prior to this call, you should call updateTransform()
	 * if the transformation matrix has not already been calculated. 
	 * @return an array with the vertices
	 */
	public float[] getWorldVertices() {
		mVertices.clear();
		int len = mVertices.limit();
		float[] world_vectors = new float[len];
		// Homogeneous coordinates
		float[] world_v = new float[4];
		float[] local_v = {0,0,0,1}; // last digit should never change (an opengl point)
		for(int i = 0; i < len; i += 3) {
			mVertices.get(local_v, 0, 3);
			Matrix.multiplyMV(world_v, 0, mTransformMatrix, 0, local_v, 0);
			world_vectors[i] = world_v[0];
			world_vectors[i+1] = world_v[1];
			world_vectors[i+2] = world_v[2];
		}
		return world_vectors;
	}
	
    /**
     * Writes the Object3D's vertex data to a stream 
     * @param s stream to write to
     * @throws IOException
     */
    public void exportModel(DataOutputStream s) throws IOException {
    	int len;
    	// write information
    	s.writeInt(mDrawMode);
    	s.writeInt(mVertexCount);
    	if(mCenter != null) {
    		s.writeInt(0);
    	} else {
    		s.writeInt(1);
    		s.writeFloat(mCenter[0]);
    		s.writeFloat(mCenter[1]);
    		s.writeFloat(mCenter[2]);
    	}
    	if (mModelBound == null) {
        	s.writeInt(0);
        } else {
        	s.writeInt(1);
        	s.writeFloat(mModelBound.mMinX);
        	s.writeFloat(mModelBound.mMinY);
        	s.writeFloat(mModelBound.mMinZ);
        	s.writeFloat(mModelBound.mMaxX);
        	s.writeFloat(mModelBound.mMaxY);
        	s.writeFloat(mModelBound.mMaxZ);
        }
        if (mIndices == null)
            s.writeInt(0);
        else {
            s.writeInt(mIndices.limit());
            mIndices.rewind();
            len = mIndices.limit();
            for (int i = 0; i < len; i++) {
                s.writeChar(mIndices.get(i));
            }
        }
        if (mVertices == null)
            s.writeInt(0);
        else {
            s.writeInt(mVertices.limit());
            mVertices.rewind();
            len = mVertices.limit();
            for (int i = 0; i < len; i++) {
                s.writeFloat(mVertices.get(i));
            }
        }
        if (mTexcoords == null)
            s.writeInt(0);
        else {
            s.writeInt(mTexcoords.limit());
            mTexcoords.rewind();
            len = mTexcoords.limit();
            for (int i = 0; i < len; i++) {
                s.writeFloat(mTexcoords.get(i));
            }
        }
        
        if (mNormals == null)
            s.writeInt(0);
        else {
            s.writeInt(mNormals.limit());
            mNormals.rewind();
            len = mNormals.limit();
            for (int i = 0; i < len; i++) {
                s.writeFloat(mNormals.get(i));
            }
        }
    }

    /**
     * Reads the model content of the given stream and set
     * the read data on this Object3D.
     * @param s stream to read from
     * @throws IOException
     */
    public void importModel(DataInputStream s) throws IOException {
        int len;
        mDrawMode = s.readInt();
        mVertexCount = s.readInt();
        
        if(s.readInt() == 0) {
        	mCenter = new float[3];
        } else {
        	mCenter[0] = s.readFloat();
        	mCenter[1] = s.readFloat();
        	mCenter[2] = s.readFloat();
        }
        
        if(s.readInt() == 0) {
        	mModelBound = new AABBox();
        	mHasDirtyModelBound = true;
        } else {
        	mModelBound.mMinX = s.readFloat();
        	mModelBound.mMinY = s.readFloat();
        	mModelBound.mMinZ = s.readFloat();
        	mModelBound.mMaxX = s.readFloat();
        	mModelBound.mMaxY = s.readFloat();
        	mModelBound.mMaxZ = s.readFloat();
        	mHasDirtyModelBound = false;
        }
        
        if((len = s.readInt()) == 0) {
        	mIndices = null;
        } else {
        	CharBuffer buf = BufferUtils.createCharBuffer(len);
        	buf.clear();
            for (int x = 0; x < len; x++)
                buf.put(s.readChar());
            mIndices = buf;
        }
        
        if((len = s.readInt()) == 0) {
        	mVertices = null;
        } else {
        	FloatBuffer buf = BufferUtils.createFloatBuffer(len);
        	buf.clear();
            for (int x = 0; x < len; x++)
                buf.put(s.readFloat());
            mVertices = buf;
        }
        
        if((len = s.readInt()) == 0) {
        	mTexcoords = null;
        } else {
        	FloatBuffer buf = BufferUtils.createFloatBuffer(len);
        	buf.clear();
            for (int x = 0; x < len; x++)
                buf.put(s.readFloat());
            mTexcoords = buf;
        }
        
        if((len = s.readInt()) == 0) {
        	mNormals = null;
        } else {
        	FloatBuffer buf = BufferUtils.createFloatBuffer(len);
        	buf.clear();
            for (int x = 0; x < len; x++)
                buf.put(s.readFloat());
            mNormals = buf;
        }
    }

	@Override
	public void setMaterial(Material material) {
		this.mMaterial = material;
	}

        /**
         * @return the material set for this object
         */
	public Material getMaterial() {
		return mMaterial;
	}

}
