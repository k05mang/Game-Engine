package primitives;
import glMath.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Math.PI;
import static java.lang.Math.sin;
import static java.lang.Math.cos;

import org.lwjgl.BufferUtils;

import collision.BoundingSphere;
import collision.ConvexHull;
import renderers.Renderable;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.GL_TRIANGLES_ADJACENCY;

public class Sphere extends Renderable{
	private ArrayList<Vertex> vertices;
	private ArrayList<Triangle> faces;
	private int slices, stacks, numIndices;
	private float radius;

	public Sphere(float radius, int slices, int stacks, int vAttrib, boolean bufferAdj){
		super(vAttrib, vAttrib+1, bufferAdj);
		
		vertices = new ArrayList<Vertex>();
		faces = new ArrayList<Triangle>();
		this.slices = slices < 3 ? 3 : slices;
		this.stacks = stacks < 1 ? 1 : stacks;
		this.radius = radius <= 0 ? .01f : radius;
		/*
		compute the number of indices needed for a sphere
		number of triangles for the "caps" of the sphere are equal to the number of slices, 2 caps = 2*slices
		when there is 1 stack there are only the caps, 2 stacks means 1 row of slices number of quads which are each
		2 triangles, so (stacks-1)*(2*slices) number of internal faces for the sphere
		*/
		numIndices = ((2*this.slices)*(this.stacks-1)+2*this.slices)*(bufferAdj ? Triangle.INDEX_ADJ : Triangle.INDEX_NOADJ);
		
		//hash map for the edges to the half edges look up
		HashMap<Triangle.Edge, Triangle.HalfEdge> edgesMap = new HashMap<Triangle.Edge, Triangle.HalfEdge>();
		int numVerts = this.slices*this.stacks+2;
		ByteBuffer vertData = BufferUtils.createByteBuffer(numVerts*Vertex.SIZE_IN_BYTES);
		IntBuffer indicesBuffer = BufferUtils.createIntBuffer(numIndices);
		
		//generate the vertices
		Vertex first = new Vertex(0,0,this.radius, 0,0,1, 0,0);
		vertices.add(first);
		first.store(vertData);
		for(int stack = 1; stack < this.stacks+1; stack++){
			for(int slice = 0; slice < this.slices; slice++){
				double phi = PI*(stack/(double)(this.stacks+1));
				double theta = 2*PI*(slice/(double)this.slices);
				
				float x = (float)( this.radius*cos(theta)*sin(phi) );
				float y = (float)( this.radius*sin(theta)*sin(phi) );
				float z = (float)( this.radius*cos(phi) );
				Vertex vert = new Vertex(x,y,z, x,y,z, 0,0);
				vertices.add(vert);
				vert.store(vertData);
				int cycle = (slice+1)%this.slices;
				//if we are on the first stack we are generating triangles for the cap
				if (stack == 1) {
					//create the face
					Triangle face = new Triangle(
							Integer.valueOf( 0),
							Integer.valueOf( slice+1),
							Integer.valueOf( cycle+1 ));
					
					super.setUpTriangle(face, edgesMap);
					faces.add(face);
				}
				//if we are on the last stack we are generating triangles for the bottom cap
				if(stack == this.stacks){
					//numVerts-this.slices-1 indicates the index of the starting vertex for the bottom stack
					Triangle face = new Triangle(
							Integer.valueOf( numVerts-1),
							Integer.valueOf( numVerts-1-this.slices+cycle),
							Integer.valueOf( numVerts-1-this.slices+slice ));
					
					super.setUpTriangle(face, edgesMap);
					faces.add(face);
				}
				//if we are in the mid section of the sphere then there are 2 triangles to generate per slice
				if(stack != 1){
					int prevStack = (stack-2)*this.slices;
					int curStack = (stack-1)*this.slices;
					
					Triangle face1 = new Triangle(
							Integer.valueOf( prevStack+slice+1 ),	//top left vertex in the left triangle of the square
							Integer.valueOf( curStack+slice+1), 	//bottom left vertex in the left triangle of the square
							Integer.valueOf( curStack+cycle+1)  	//bottom right vertex in the left triangle of the square
							);
					super.setUpTriangle(face1, edgesMap);
					
					Triangle face2 = new Triangle(
							Integer.valueOf( prevStack+slice+1 ),		 //top left vertex in the right triangle of the square
							Integer.valueOf( curStack+cycle+1)	,	 	 //bottom right vertex in the right triangle of the square
							Integer.valueOf( prevStack+cycle+1 )		 //top right vertex in the right triangle of the square
							);
					super.setUpTriangle(face2, edgesMap);
					faces.add(face1);
					faces.add(face2);
				}
			}
		}
		Vertex last = new Vertex(0,0,-this.radius, 0,0,-1, 0,0);
		vertices.add(last);
		last.store(vertData);
		
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
		
		this.collider = new BoundingSphere(radius);
	}
	
	public Sphere(float radius, int divisions, int vAttrib, boolean bufferAdj){
		this(radius, divisions, divisions, vAttrib, bufferAdj);
	}
	
	public Sphere(Sphere copy){
		super(copy);
		this.faces = copy.faces;
		this.vertices = copy.vertices;
		numIndices = copy.numIndices;
		radius = copy.radius;
		this.stacks = copy.stacks;
		this.slices = copy.slices;
	}
	
	@Override
	public Sphere copy(){
		return new Sphere(this);
	}
	
	//change functionality
	public void setRadius(float radius){
		this.radius = radius <= 0 ? .01f : radius;
//		base.setValueAt(0, 0, this.radius);
//		base.setValueAt(1, 1, this.radius);
//		base.setValueAt(2, 2, this.radius);
	}
	
	public float getRadius(){
		return radius;
	}
	
	public int numSlices(){
		return slices;
	}
	
	public int numStacks(){
		return stacks;
	}
	
	@Override
	public int getNumIndices(){
		return numIndices;
	}

	@Override
	public ArrayList<Vertex> getVertices() {
		return vertices;
	}

	@Override
	public ArrayList<Triangle> getFaces() {
		return faces;
	}

	@Override
	public boolean equals(Object o){
		if(o instanceof Sphere){
			Sphere casted = (Sphere)o;
			return casted.numSlices() == slices && casted.numStacks() == stacks;
		}else{
			return false;
		}
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
		Mat3 tensor = new Mat3((2*mass*radius*radius)/5);
		if(mass > 0){
			tensor.invert();
		}
		return tensor;
	}
}
