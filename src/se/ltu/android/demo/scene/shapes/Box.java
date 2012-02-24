/* SVN FILE: $Id: Box.java 24 2009-08-17 15:32:24Z belse $ */
package se.ltu.android.demo.scene.shapes;

import se.ltu.android.demo.scene.Object3D;

/**
 * A box
 * 
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision: 24 $
 * @lastmodified $Date: 2009-08-18 00:32:24 +0900 (2009-08-18, 화) $
 */
public class Box extends Object3D {
	
	private final static char[] INDICES = {
			 0, 1, 2,	 2, 3, 0,	//front
			 5, 4, 7,	 7, 6, 5,	//back
			 8, 9,10,	10,11, 8,	//right
			12,13,14,	14,15,12,	//left
			16,17,18,	18,19,16,	//top
			20,21,22,	22,23,20	//bottom
	};
		
	private final static float[] NORMALS = {
			 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,	//front
			 0, 0,-1, 0, 0,-1, 0, 0,-1, 0, 0,-1,	//back
			 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,	//right
			-1, 0, 0,-1, 0, 0,-1, 0, 0,-1, 0, 0,	//left
			 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0,	//top
			 0,-1, 0, 0,-1, 0, 0,-1, 0, 0,-1, 0		//bottom
	};
		
	private final static float[] TEXCOORDS = {
			0,0,1,0,1,1,0,1,	//front
			0,0,1,0,1,1,0,1,	//back
			0,0,1,0,1,1,0,1,	//right
			0,0,1,0,1,1,0,1,	//left
			0,0,1,0,1,1,0,1,	//top
			0,0,1,0,1,1,0,1		//bottom
	};
	
	// extent from center point in each axis
	private float extX;
	private float extY;
	private float extZ;

	/**
	 * Creates a new box by defining two opposing end points.
	 * Each minimum coordinate must be less than the maximum coordinate in order to
	 * have all normals pointing outwards.
	 * @param name name of the spatial for identifying purposes
	 * @param minX minimum point in the x-axis 
	 * @param minY minimum point in the y-axis 
	 * @param minZ minimum point in the z-axis 
	 * @param maxX maximum point in the x-axis 
	 * @param maxY maximum point in the y-axis 
	 * @param maxZ maximum point in the z-axis 
	 */
	public Box(String name, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		super(name);
		
		extX = (maxX - minX) / 2.0f;
		extY = (maxX - minX) / 2.0f;
		extZ = (maxX - minX) / 2.0f;
		mCenter[0] = minX + extX;
		mCenter[1] = minY + extY;
		mCenter[2] = minZ + extZ;

		construct(minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	/**
	 * Creates a new box centered at the origin.
	 * @param name name of the spatial for identifying purposes
	 * @param widthX width of the box in the x-axis
	 * @param widthY width of the box in the y-axis
	 * @param widthZ width of the box in the z-axis
	 */
	public Box(String name, float widthX, float widthY, float widthZ) {
		super(name);
		
		extX = widthX / 2.0f;
		extY = widthY / 2.0f;
		extZ = widthZ / 2.0f;
		
		construct();
	}
	
	/**
	 * Creates a new box with the specified center point (array of length with x,y,z) and width.
	 * If the center point array is null or of the wrong length, (0,0,0) is used. 
	 * @param name name of the spatial for identifying purposes
	 * @param centerP center point of box as an array of length 3 containing the coordinates in x,y,z
	 * @param widthX width of the box in the x-axis
	 * @param widthY width of the box in the y-axis
	 * @param widthZ width of the box in the z-axis
	 */
	public Box(String name, float[] centerP, float widthX, float widthY, float widthZ) {
		super(name);
		if(centerP != null && centerP.length == 3) {
			mCenter[0] = centerP[0];
			mCenter[1] = centerP[1];
			mCenter[2] = centerP[2];
		}
		extX = widthX / 2.0f;
		extY = widthY / 2.0f;
		extZ = widthZ / 2.0f;
		
		construct();
	}
	
	private void construct() {
		float minX = mCenter[0] - extX;
		float minY = mCenter[1] - extY;
		float minZ = mCenter[2] - extZ;
		float maxX = mCenter[0] + extX;
		float maxY = mCenter[1] + extY;
		float maxZ = mCenter[2] + extZ;
		construct(minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	private void construct(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		float[] vertices = {
			minX,minY,maxZ,	maxX,minY,maxZ,	//0,1 = front
			maxX,maxY,maxZ,	minX,maxY,maxZ,	//2,3
			minX,minY,minZ,	maxX,minY,minZ,	//4,5 = back
			maxX,maxY,minZ,	minX,maxY,minZ, //6,7			(index below)
			maxX,minY,maxZ,	maxX,minY,minZ,	//1,5 = right	8,9
			maxX,maxY,minZ,	maxX,maxY,maxZ,	//6,2			10,11
			minX,minY,minZ,	minX,minY,maxZ,	//4,0 = left	12,13
			minX,maxY,maxZ,	minX,maxY,minZ,	//3,7			14,15
			minX,maxY,maxZ,	maxX,maxY,maxZ,	//3,2 = top		16,17
			maxX,maxY,minZ,	minX,maxY,minZ,	//6,7			18,19
			maxX,minY,maxZ,	minX,minY,maxZ,	//1,0 = bottom	20,21
			minX,minY,minZ,	maxX,minY,minZ	//4,5			22,23
		};
		
		setVertices(vertices);
		setIndices(INDICES);
		setNormals(NORMALS);
		setTexCoords(TEXCOORDS);
	}
	
	
}
