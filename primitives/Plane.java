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
import glMath.Vec3;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.BufferUtils;

import collision.ConvexHull;
import collision.CollisionPlane;
import renderers.Renderable;

public class Plane extends Renderable{
	private ArrayList<Vertex> vertices;
	private Triangle face1, face2;
	private int numIndices;
	private static final Vec3 center = new Vec3(0,0,0);
	float width, length;
	
	public Plane(float sideLength1, float sideLength2, int vAttrib, boolean adjBuffered){
		super(vAttrib, vAttrib+1, adjBuffered);
		this.width = sideLength1;
		this.length = sideLength2;
		vertices = new ArrayList<Vertex>(6);
		numIndices = (isAdjBuffered ? Triangle.INDEX_ADJ : Triangle.INDEX_NOADJ) << 1;
		
		ByteBuffer verts = BufferUtils.createByteBuffer(6*Vertex.SIZE_IN_BYTES);
		IntBuffer indices = BufferUtils.createIntBuffer((adjBuffered ? Triangle.INDEX_ADJ : Triangle.INDEX_NOADJ)*6);
		HashMap<Triangle.Edge, Triangle.HalfEdge> edgesMap = new HashMap<Triangle.Edge, Triangle.HalfEdge>();
		
		Vertex topLeft = new Vertex(-sideLength1,0,-sideLength2, 0,1,0, 0,1);
		Vertex bottomLeft = new Vertex(-sideLength1,0,sideLength2, 0,1,0, 0,0);
		Vertex topRight = new Vertex(sideLength1,0,-sideLength2, 0,1,0, 1,1);
		Vertex bottomRight = new Vertex(sideLength1,0,sideLength2, 0,1,0, 1,0);
		
		topLeft.store(verts);
		bottomLeft.store(verts);
		topRight.store(verts);
		bottomRight.store(verts);
		
		vertices.add(topLeft);
		vertices.add(bottomLeft);
		vertices.add(topRight);
		vertices.add(bottomRight);
		
		face1 = new Triangle(
				Integer.valueOf(0),
				Integer.valueOf(1),
				Integer.valueOf(2)
				);
		face2 = new Triangle(
				Integer.valueOf(2),
				Integer.valueOf(1),
				Integer.valueOf(3)
				);
		
		super.setUpTriangle(face1, edgesMap);
		super.setUpTriangle(face2, edgesMap);
		
		if(adjBuffered){
			face1.storeAllIndices(indices);
			face2.storeAllIndices(indices);
		}else{
			face1.storePrimitiveIndices(indices);
			face2.storePrimitiveIndices(indices);
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
		
		this.collider = new CollisionPlane(sideLength1, sideLength2);
	}
	
	public Plane(float sideLength, int vAttrib, boolean adjBuffered){
		this(sideLength, sideLength, vAttrib, adjBuffered);
	}
	
	public Plane(Plane copy){
		super(copy);
		face1 = copy.face1;
		face2 = copy.face2;
		this.vertices = copy.vertices;
		numIndices = copy.numIndices;
	}
	
	@Override
	public Plane copy(){
		return new Plane(this);
	}
	
	public Vec3 getCenter(){
		return (Vec3)getModelMatrix().multVec(center).swizzle("xyz");
	}
	
	@Override
	public int getNumIndices() {
		return numIndices;
	}

	public ArrayList<Vertex> getVertices() {
		return vertices;
	}
	
	public ArrayList<Triangle> getFaces(){
		ArrayList<Triangle> temp = new ArrayList<Triangle>(2);
		temp.add(face1);
		temp.add(face2);
		return temp;
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
		Mat3 tensor = new Mat3((mass*(width*width+length*length))/12);
		if(mass > 0){
			tensor.invert();
		}
		return tensor;
	}
}
