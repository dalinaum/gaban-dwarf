/* SVN FILE: $Id: Quad.java 24 2009-08-17 15:32:24Z belse $ */
package se.ltu.android.demo.scene.shapes;

import se.ltu.android.demo.scene.Object3D;

/**
 * A quadrilateral.
 * 
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision: 24 $
 * @lastmodified $Date: 2009-08-18 00:32:24 +0900 (2009-08-18, 화) $
 */
public class Quad extends Object3D {
	private final static char[] INDICES = {
		0,1,2,3
	};
	private final static float[] NORMALS = {
			0,0,1,0,0,1,0,0,1,0,0,1
	};
	private final static float[] TEXCOORDS = {
			0,0,1,0,1,1,0,1
	};
	
	private float extX;
	private float extY;
	
	/**
	 * Constructs a quad with given dimensions
	 * @param name for identifying purposes
	 * @param width length in x-axis
	 * @param height length in y-axis
	 */
	public Quad(String name, float width, float height) {
		super(name);
		extX = width/2.0f;
		extY = height/2.0f;
		construct();
	}

	private void construct() {
		mDrawMode = MODE_TRIANGLE_FAN;
		float minX = mCenter[0] - extX;
		float maxX = mCenter[0] + extX;
		float minY = mCenter[1] - extY;
		float maxY = mCenter[1] + extY;
		
		float[] vertices = {
				minX, minY, 0,
				maxX, minY, 0,
				maxX, maxY, 0,
				minX, maxY, 0
		};
		
		setVertices(vertices);
		setIndices(INDICES);
		setNormals(NORMALS);
		setTexCoords(TEXCOORDS);
	}
}
