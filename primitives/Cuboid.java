package primitives;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL32.GL_TRIANGLES_ADJACENCY;
import glMath.Mat3;
import glMath.Mat4;
import glMath.MatrixUtil;
import glMath.Quaternion;
import glMath.Vec3;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.BufferUtils;

import collision.ConvexHull;
import collision.OBB;
import renderers.Renderable;

public class Cuboid extends Renderable {
	private ArrayList<Vertex> vertices;
	private ArrayList<Triangle> faces;
	private int numIndices;
	private Vec3 dimensions, halfDimensions;
	
	public Cuboid(float width, float height, float length, boolean invertNormals, int vAttrib, boolean adjBuffered){
		super(vAttrib, vAttrib+1, adjBuffered);
		
		halfDimensions = new Vec3(width/2.0f,height/2.0f,length/2.0f);
		dimensions = new Vec3(width, height, length);
		
		HashMap<Triangle.Edge, Triangle.HalfEdge> edgesMap = new HashMap<Triangle.Edge, Triangle.HalfEdge>();
		faces = new ArrayList<Triangle>(12);
		vertices = new ArrayList<Vertex>(24);
		numIndices = 12 * (isAdjBuffered ? Triangle.INDEX_ADJ : Triangle.INDEX_NOADJ);
		
		IntBuffer indices = BufferUtils.createIntBuffer((adjBuffered ? Triangle.INDEX_ADJ : Triangle.INDEX_NOADJ) * 12);
		ByteBuffer verts = BufferUtils.createByteBuffer(24 * Vertex.SIZE_IN_BYTES);
		
		//-----zpos------
		vertices.add(new Vertex(-halfDimensions.x, halfDimensions.y, halfDimensions.z, 0, 0, (invertNormals ? -1
				: 1), 0, 1));
		vertices.add(new Vertex(-halfDimensions.x, -halfDimensions.y, halfDimensions.z, 0, 0, (invertNormals ? -1
				: 1), 0, 0));
		vertices.add(new Vertex(halfDimensions.x, halfDimensions.y, halfDimensions.z, 0, 0, (invertNormals ? -1
				: 1), 1, 1));
		vertices.add(new Vertex(halfDimensions.x, -halfDimensions.y, halfDimensions.z, 0, 0, (invertNormals ? -1
				: 1), 1, 0));
		//-----zneg------
		vertices.add(new Vertex(halfDimensions.x, halfDimensions.y, -halfDimensions.z, 0, 0, (invertNormals ? 1
				: -1), 0, 1));
		vertices.add(new Vertex(halfDimensions.x, -halfDimensions.y, -halfDimensions.z, 0, 0, (invertNormals ? 1
				: -1), 0, 0));
		vertices.add(new Vertex(-halfDimensions.x, halfDimensions.y, -halfDimensions.z, 0, 0, (invertNormals ? 1
				: -1), 1, 1));
		vertices.add(new Vertex(-halfDimensions.x, -halfDimensions.y, -halfDimensions.z, 0, 0, (invertNormals ? 1
				: -1), 1, 0));
		
		//-----xpos------
		vertices.add(new Vertex(halfDimensions.x, halfDimensions.y, halfDimensions.z, (invertNormals ? -1 : 1), 0,
				0, 0, 1));
		vertices.add(new Vertex(halfDimensions.x, -halfDimensions.y, halfDimensions.z, (invertNormals ? -1 : 1),
				0, 0, 0, 0));
		vertices.add(new Vertex(halfDimensions.x, halfDimensions.y, -halfDimensions.z, (invertNormals ? -1 : 1),
				0, 0, 1, 1));
		vertices.add(new Vertex(halfDimensions.x, -halfDimensions.y, -halfDimensions.z, (invertNormals ? -1 : 1),
				0, 0, 1, 0));
		//-----xneg------
		vertices.add(new Vertex(-halfDimensions.x, halfDimensions.y, -halfDimensions.z, (invertNormals ? 1 : -1),
				0, 0, 0, 1));
		vertices.add(new Vertex(-halfDimensions.x, -halfDimensions.y, -halfDimensions.z, (invertNormals ? 1 : -1),
				0, 0, 0, 0));
		vertices.add(new Vertex(-halfDimensions.x, halfDimensions.y, halfDimensions.z, (invertNormals ? 1 : -1),
				0, 0, 1, 1));
		vertices.add(new Vertex(-halfDimensions.x, -halfDimensions.y, halfDimensions.z, (invertNormals ? 1 : -1),
				0, 0, 1, 0));
		
		//-----ypos------
		vertices.add(new Vertex(-halfDimensions.x, halfDimensions.y, -halfDimensions.z, 0,
				(invertNormals ? -1 : 1), 0, 0, 1));
		vertices.add(new Vertex(-halfDimensions.x, halfDimensions.y, halfDimensions.z, 0,
				(invertNormals ? -1 : 1), 0, 0, 0));
		vertices.add(new Vertex(halfDimensions.x, halfDimensions.y, -halfDimensions.z, 0,
				(invertNormals ? -1 : 1), 0, 1, 1));
		vertices.add(new Vertex(halfDimensions.x, halfDimensions.y, halfDimensions.z, 0, (invertNormals ? -1 : 1),
				0, 1, 0));
		//-----yneg------
		vertices.add(new Vertex(-halfDimensions.x, -halfDimensions.y, halfDimensions.z, 0,
				(invertNormals ? 1 : -1), 0, 0, 1));
		vertices.add(new Vertex(-halfDimensions.x, -halfDimensions.y, -halfDimensions.z, 0, (invertNormals ? 1
				: -1), 0, 0, 0));
		vertices.add(new Vertex(halfDimensions.x, -halfDimensions.y, halfDimensions.z, 0,
				(invertNormals ? 1 : -1), 0, 1, 1));
		vertices.add(new Vertex(halfDimensions.x, -halfDimensions.y, -halfDimensions.z, 0,
				(invertNormals ? 1 : -1), 0, 1, 0));
		
		for (int face = 0; face < 6; face++) {
			vertices.get(0 + 4 * face).store(verts);
			vertices.get(1 + 4 * face).store(verts);
			vertices.get(2 + 4 * face).store(verts);
			vertices.get(3 + 4 * face).store(verts);

			Triangle face1 = new Triangle(Integer.valueOf(0 + 4 * face),
					Integer.valueOf(1 + 4 * face),
					Integer.valueOf(3 + 4 * face));
			Triangle face2 = new Triangle(Integer.valueOf(0 + 4 * face),
					Integer.valueOf(3 + 4 * face),
					Integer.valueOf(2 + 4 * face));
			super.setUpTriangle(face1, edgesMap);
			super.setUpTriangle(face2, edgesMap);
			faces.add(face1);
			faces.add(face2);
		}
		
		for(Triangle face : faces){
			//face.initAdjacent();
			
			if(adjBuffered){
				face.storeAllIndices(indices);
			}else{
				face.storePrimitiveIndices(indices);
			}
			
		}
		verts.flip();
		indices.flip();
		
		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, verts, GL_STATIC_DRAW);
		glVertexAttribPointer(vAttrib, 3, GL_FLOAT, false, Vertex.SIZE_IN_BYTES, 0);
		glVertexAttribPointer(nAttrib, 3, GL_FLOAT, false, Vertex.SIZE_IN_BYTES, 12);
		glVertexAttribPointer(nAttrib+1, 2, GL_FLOAT, false, Vertex.SIZE_IN_BYTES, 24);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
		
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices , GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		
		this.collider = new OBB(width, height, length);
	}
	
	public Cuboid(Vec3 dimensions, boolean invertNormals, int vAttrib, boolean adjBuffered){
		this(dimensions.x, dimensions.y, dimensions.z, invertNormals, vAttrib, adjBuffered);
	}
	
	public Cuboid(float scale, boolean invertNormals, int vAttrib, boolean adjBuffered){
		this(scale, scale, scale, invertNormals, vAttrib, adjBuffered);
	}
	
	public Cuboid(Cuboid copy){
		super(copy);
		this.faces = copy.faces;
		this.vertices = copy.vertices;
		numIndices = copy.numIndices;
		this.dimensions = copy.dimensions;
		this.halfDimensions = copy.halfDimensions;
	}
	
	@Override
	public Cuboid copy(){
		return new Cuboid(this);
	}
	
	@Override
	public int getNumIndices() {
		return numIndices;
	}

	public ArrayList<Triangle> getFaces() {
		return faces;
	}

	public ArrayList<Vertex> getVertices() {
		return vertices;
	}

	@Override
	public void render() {
		glBindVertexArray(vao);
		glEnableVertexAttribArray(vAttrib);
		glEnableVertexAttribArray(nAttrib);
		glEnableVertexAttribArray(nAttrib+1);
		
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
		glDrawElements((isAdjBuffered ? GL_TRIANGLES_ADJACENCY : GL_TRIANGLES), 
				numIndices, GL_UNSIGNED_INT, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		
		glDisableVertexAttribArray(vAttrib);
		glDisableVertexAttribArray(nAttrib);
		glEnableVertexAttribArray(nAttrib+1);
		glBindVertexArray(0);
	}

	@Override
	public Mat3 computeTensor(float mass) {
		float widthSq = dimensions.x*dimensions.x;
		float heightSq = dimensions.y*dimensions.y;
		float lengthSq = dimensions.z*dimensions.z;
		Mat3 tensor = new Mat3(
				new Vec3((mass/12)*(heightSq+lengthSq), 0, 0),
				new Vec3(0, (mass/12)*(widthSq+lengthSq), 0),
				new Vec3(0, 0, (mass/12)*(widthSq+heightSq))
				);
		if(mass > 0){
			tensor.invert();
		}
		return tensor;
	}

}
