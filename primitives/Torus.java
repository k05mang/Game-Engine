package primitives;

import glMath.Mat3;
import glMath.Vec3;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Math.PI;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
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

import org.lwjgl.BufferUtils;

import renderers.Renderable;

public class Torus extends Renderable {
	
	private ArrayList<Triangle> faces;
	private ArrayList<Vertex> vertices;
	private int rings, ringSegs, numIndices;
	private float tubeRadius, radius;

	public Torus(float radius, float tubeRadius, int rings, int ringSegs, 
			int vAttrib, boolean bufferAdj){
		super(vAttrib, vAttrib+1, bufferAdj);
		
		this.radius = radius;
		this.tubeRadius = tubeRadius;
		this.rings = rings < 3 ? 3 : rings;
		this.ringSegs = ringSegs < 3 ? 3 : ringSegs;
		faces = new ArrayList<Triangle>(2*this.rings*this.ringSegs);
		vertices = new ArrayList<Vertex>(this.rings*this.ringSegs);
		HashMap<Triangle.Edge, Triangle.HalfEdge> edgeMap = new HashMap<Triangle.Edge, Triangle.HalfEdge>();
		numIndices = 2*(bufferAdj ? Triangle.INDEX_ADJ : Triangle.INDEX_NOADJ)*this.rings*this.ringSegs;
		
		IntBuffer indices = BufferUtils.createIntBuffer(numIndices);
		ByteBuffer vertData = BufferUtils.createByteBuffer(this.rings*this.ringSegs*Vertex.SIZE_IN_BYTES);
		
		for(int curRing = 0; curRing < this.rings; curRing++){
			for(int ring = 0; ring < this.ringSegs; ring++){
				double phi = 2*PI*(ring/(double)this.ringSegs);
				double theta = 2*PI*(curRing/(double)this.rings);
				
				float x = (float)( (radius + tubeRadius*cos(phi))*cos(theta) );
				float y = (float)(tubeRadius*sin(phi));
				float z = (float)( (radius + tubeRadius*cos(phi))*sin(theta) );
				
				float normX = x-radius*(float)cos(theta);
				float normZ = z-radius*(float)sin(theta);
				
				Vertex vert = new Vertex(x, y, z,  normX, y, normZ,  0, 0);
				vert.store(vertData);
				vertices.add(vert);
				
				Triangle face1 = new Triangle(
						Integer.valueOf(ring+this.rings*curRing),
						Integer.valueOf((ring+1)%this.ringSegs+this.rings*( (curRing+1)%this.rings )),
						Integer.valueOf(ring+this.rings*( (curRing+1)%this.rings ))
						);
				
				Triangle face2 = new Triangle(
						Integer.valueOf(ring+this.rings*curRing),
						Integer.valueOf((ring+1)%this.ringSegs+this.rings*curRing),
						Integer.valueOf((ring+1)%this.ringSegs+this.rings*( (curRing+1)%this.rings ))
						);
				super.setUpTriangle(face1,  edgeMap);
				super.setUpTriangle(face2,  edgeMap);
				
				faces.add(face1);
				faces.add(face2);
			}
		}
		
		for(Triangle face : faces){
			face.initAdjacent();
			
			if(bufferAdj){
				face.storeAllIndices(indices);
			}else{
				face.storePrimitiveIndices(indices);
			}
			
		}
		
		vertData.flip();
		indices.flip();
		
		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, vertData, GL_STATIC_DRAW);
		glVertexAttribPointer(vAttrib, 3, GL_FLOAT, false, Vertex.SIZE_IN_BYTES, 0);
		glVertexAttribPointer(nAttrib, 3, GL_FLOAT, false, Vertex.SIZE_IN_BYTES, 12);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
		
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices , GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	public Torus(Torus copy){
		super(copy);
		this.faces = copy.faces;
		this.vertices = copy.vertices;
		rings = copy.rings;
		ringSegs = copy.ringSegs; 
		numIndices = copy.numIndices;
	}
	
	@Override
	public Torus copy(){
		return new Torus(this);
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

	public int getRings() {
		return rings;
	}

	public int getRingSegs() {
		return ringSegs;
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
		float tubeRadiusSq = tubeRadius*tubeRadius;
		float iXY = .125f*(4*tubeRadiusSq+5*radiusSq);
		Mat3 tensor = new Mat3(
				new Vec3(iXY, 0, 0),
				new Vec3(0, (tubeRadiusSq+.75f*radiusSq)*mass, 0),
				new Vec3(0, 0, iXY)
				);
		if(mass > 0){
			tensor.invert();
		}
		return tensor;
	}
}
