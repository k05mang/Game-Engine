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
import static java.lang.Math.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.BufferUtils;

import collision.ConvexHull;
import renderers.Renderable;
import glMath.*;

public class Cylinder extends Renderable{
	private float height, radius;
	private int segments;
	private ArrayList<Triangle> faces;
	private ArrayList<Vertex> vertices;
	private int numIndices;
	
	public Cylinder(float radius, float height, int segments, int vAttrib, boolean bufferAdj){
		super(vAttrib, vAttrib+1, bufferAdj);
		
		faces = new ArrayList<Triangle>();
		vertices = new ArrayList<Vertex>();
		this.segments = (segments < 3 ? 3 : segments);
		this.height = height == 0 ? .0001f : height;
		this.radius = radius <= 0 ? .01f : radius;
		numIndices = (this.segments*2+(this.segments-2)*2)
				*(bufferAdj ? Triangle.INDEX_ADJ : Triangle.INDEX_NOADJ);
		
		HashMap<Triangle.Edge, Triangle.HalfEdge> edgesMap = new HashMap<Triangle.Edge, Triangle.HalfEdge>();
		
		ByteBuffer vertData = BufferUtils.createByteBuffer(2*this.segments*Vertex.SIZE_IN_BYTES);
		IntBuffer indicesBuffer = BufferUtils.createIntBuffer(numIndices);
		
		for(int segment = 0; segment < this.segments; segment++){
			double theta = 2*PI*(segment/(double)this.segments);
			
			float x = this.radius*(float)(cos(theta));
			float y = this.height/2.0f;
			float z = this.radius*(float)(sin(theta));
			
			Vertex vert1 = new Vertex(x, y, z,  x, y, z, 0,0);
			Vertex vert2 = new Vertex(x, -y, z,  x, -y, z, 0,0);
			vertices.add(vert1);
			vertices.add(vert2);
			
			vert1.store(vertData);
			vert2.store(vertData);
			
			int nextSegment = (segment+1)%this.segments;
			
			//only compute a new top and bottom face when we are on an odd segment edge
			if(segment != 0 && segment != this.segments-1){
				Triangle top = new Triangle(
						Integer.valueOf(0),					
						Integer.valueOf( (segment+1) << 1),
						Integer.valueOf( segment << 1)
						);
				super.setUpTriangle(top, edgesMap);
				faces.add(top);
				
				Triangle bottom = new Triangle(
						Integer.valueOf(1),						
						Integer.valueOf( (segment << 1)+1), 	
						Integer.valueOf( ((segment+1) << 1)+1)  
						);
				super.setUpTriangle(bottom, edgesMap);
				faces.add(bottom);
			}
			
			Triangle sideLeft = new Triangle(
					Integer.valueOf( segment << 1 ),    
					Integer.valueOf( (nextSegment << 1)+1),
					Integer.valueOf( (segment << 1)+1)
					);
			super.setUpTriangle(sideLeft, edgesMap);
			faces.add(sideLeft);
			
			Triangle sideRight = new Triangle(
					Integer.valueOf( segment << 1 ),
					Integer.valueOf( nextSegment << 1 ),
					Integer.valueOf( (nextSegment << 1)+1 )
					);
			super.setUpTriangle(sideRight, edgesMap);
			faces.add(sideRight);
		}

		vertData.flip();
		
		for(Triangle face : faces){
			face.initAdjacent();
			
			if(bufferAdj){
				face.storeAllIndices(indicesBuffer);
			}else{
				face.storePrimitiveIndices(indicesBuffer);
			}
			
		}
		indicesBuffer.flip();
		
		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, vertData, GL_STATIC_DRAW);
		glVertexAttribPointer(vAttrib, 3, GL_FLOAT, false, Vertex.SIZE_IN_BYTES, 0);
		glVertexAttribPointer(nAttrib, 3, GL_FLOAT, false, Vertex.SIZE_IN_BYTES, 12);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
		
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer , GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		
		this.collider = new ConvexHull(vertices, faces.get(0));
	}
	
	public Cylinder(Cylinder copy){
		super(copy);
		this.faces = copy.faces;
		this.vertices = copy.vertices;
		numIndices = copy.numIndices;
		this.segments = copy.segments;
	}
	
	@Override
	public Cylinder copy(){
		return new Cylinder(this);
	}
	
	public float getRadius(){
		return radius;
	}
	
	public float getHeight(){
		return height;
	}
	
	@Override
	public int getNumIndices(){
		return numIndices;
	}

	public int getSegments() {
		return segments;
	}

	public ArrayList<Triangle> getFaces() {
		return faces;
	}

	public ArrayList<Vertex> getVertices() {
		return vertices;
	}

	@Override
	public void render(){
		glBindVertexArray(vao);
		glEnableVertexAttribArray(vAttrib);
		glEnableVertexAttribArray(nAttrib);
		
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
		glDrawElements((isAdjBuffered ? GL_TRIANGLES_ADJACENCY : GL_TRIANGLES), 
				numIndices, GL_UNSIGNED_INT, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		
		glDisableVertexAttribArray(vAttrib);
		glDisableVertexAttribArray(nAttrib);
		glBindVertexArray(0);
	}

	@Override
	public Mat3 computeTensor(float mass) {
		float radiusSq = radius*radius;
		float heightSq = height*height;
		Mat3 tensor = new Mat3(
				new Vec3((mass/12)*(3*radiusSq+heightSq), 0, 0),
				new Vec3(0, (mass*radiusSq)/2, 0),
				new Vec3(0, 0, (mass/12)*(3*radiusSq+heightSq))
				);
		if(mass > 0){
			tensor.invert();
		}
		return tensor;
	}
}
