package primitives;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
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
import glMath.Vec4;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.BufferUtils;

import collision.ConvexHull;
import renderers.Renderable;

public class Cone extends Renderable {

	private ArrayList<Triangle> faces;
	private ArrayList<Vertex> vertices;
	private int subdiv, numIndices;
	private float length, radius;
	
	public Cone(float radius, float length, int subdivisions, int vAttrib, boolean bufferAdj){
		super(vAttrib, vAttrib+1, bufferAdj);
			
		subdiv = (subdivisions > 2 ? subdivisions : 3);
		this.radius = radius <= 0 ? .01f : radius;
		this.length = length == 0 ? .0001f : length;
		
		faces = new ArrayList<Triangle>();
		vertices = new ArrayList<Vertex>();
		numIndices = (subdiv+(subdiv-2))*(bufferAdj ? Triangle.INDEX_ADJ : Triangle.INDEX_NOADJ);
		
		HashMap<Triangle.Edge, Triangle.HalfEdge> edgesMap = new HashMap<Triangle.Edge, Triangle.HalfEdge>();
		
		ByteBuffer vertData = BufferUtils.createByteBuffer((subdiv+1)*Vertex.SIZE_IN_BYTES);
		IntBuffer indicesBuffer = BufferUtils.createIntBuffer(numIndices);
		
		Vertex tip = new Vertex(0,this.length/2.0f,0, 0,this.length/2.0f,0, 0,0);
		vertices.add(tip);
		tip.store(vertData);
		for(int segment = 1; segment < subdiv+1; segment++){
			double theta = 2*PI*(segment/(double)subdiv);
			
			float x = this.radius*(float)(cos(theta));
			float z = this.radius*(float)(sin(theta));
			
			Vertex vert1 = new Vertex(x, -this.length/2.0f, z,  x, -this.length/2.0f, z, 0,0);
			vertices.add(vert1);
			vert1.store(vertData);
			
			Triangle side = new Triangle(
					Integer.valueOf(0),	
					Integer.valueOf( (segment+1)%(subdiv+1) == 0 ? 1 : (segment+1)%(subdiv+1)),
					Integer.valueOf(segment)
					);
			super.setUpTriangle(side, edgesMap);
			faces.add(side);
			
			if(segment < subdiv-1){
			Triangle bottom = new Triangle(
					Integer.valueOf(1),						//the first bottom vertex
					Integer.valueOf( segment+1), 	//the vertex that is segment+1 of the bottom ring
					Integer.valueOf( segment+2)  //the vertex that is segment+2 of the bottom ring
					);
			super.setUpTriangle(bottom, edgesMap);
			faces.add(bottom);
			}
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
	
	public Cone(Cone copy){
		super(copy);
		this.faces = copy.faces;
		this.vertices = copy.vertices;
		numIndices = copy.numIndices;
		subdiv = copy.subdiv; 
		length = copy.length; 
		radius = copy.radius;
	}
	
	@Override
	public Cone copy(){
		return new Cone(this);
	}
	
	public float getRadius(){
		return radius;
	}
	
	public float getLength(){
		return length;
	}
	
	@Override
	public int getNumIndices(){
		return numIndices;
	}
	
	public ArrayList<Triangle> getFaces() {
		return faces;
	}

	public ArrayList<Vertex> getVertices() {
		return vertices;
	}

	public int getSubdivisions() {
		return subdiv;
	}

	@Override
	public void render() {
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
		Mat3 tensor = new Mat3(
				new Vec3((3*mass/5)*((radius*radius)/4+length*length), 0, 0),
				new Vec3(0, 3*mass/10*(radius*radius), 0),
				new Vec3(0, 0, (3*mass/5)*((radius*radius)/4+length*length))
				);
		if(mass > 0){
			tensor.invert();
		}
		return tensor;
	}

}
