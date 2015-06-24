package renderers;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL33.*;
import glMath.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import collision.AABB;
import collision.CollisionMesh;
import primitives.Triangle;
import primitives.Vertex;


public abstract class Renderable {
	protected int vbo, vao, ibo, vAttrib, nAttrib;
	protected Mat4 modelMat;
	protected Quaternion orientation;
	protected boolean isAdjBuffered;
	protected CollisionMesh collider;
	protected AABB boundingVolume;
	protected Vec3 maxX, maxY, maxZ, minX, minY, minZ;
	
	public Renderable(int vAttrib, int nAttrib, boolean bufferAdj){
		
		modelMat = new Mat4(1);
		this.vAttrib = vAttrib;
		this.nAttrib = nAttrib;
		isAdjBuffered = bufferAdj;
		
		//these values should be initialized by the subclass
		collider = null;
		boundingVolume = new AABB(0);
		maxX = maxY = maxZ = minX = minY = minZ = new Vec3();
		
		vao = glGenVertexArrays();
		vbo = glGenBuffers();
		ibo = glGenBuffers();
		orientation = new Quaternion();
	}
	
	public Renderable(Renderable copy){
		vbo = copy.vbo; 
		vao = copy.vao; 
		ibo = copy.ibo; 
		vAttrib = copy.vAttrib; 
		nAttrib = copy.nAttrib;
		modelMat = new Mat4(1);
		orientation = new Quaternion();
		isAdjBuffered = copy.isAdjBuffered;
		//copies are needed of the collision meshes and bounding volumes to makes sure that multiple
		//meshes don't modify each others collision meshes which can cause problems for collisions
		this.collider = copy.collider.copy();
		this.boundingVolume = copy.boundingVolume.copy();
		maxX = new Vec3(copy.maxX);
		maxY = new Vec3(copy.maxY);
		maxZ = new Vec3(copy.maxZ);
		
		minX = new Vec3(copy.minX);
		minY = new Vec3(copy.minY);
		minZ = new Vec3(copy.minZ);
	}
	
	public abstract Renderable copy();
	
	public void translate(float x, float y, float z){
		modelMat.leftMult(MatrixUtil.translate(x, y, z));
		collider.translate(x, y, z);
		boundingVolume.translate(x, y, z);
	}
	
	public void translate(Vec3 translation){
		modelMat.leftMult(MatrixUtil.translate(translation));
		collider.translate(translation);
		boundingVolume.translate(translation);
	}
	
	public void scale(float factor){
		modelMat.leftMult(MatrixUtil.scale(factor, factor, factor));
		collider.scale(factor);
		boundingVolume.scale(factor);
	}
	
	public void scale(float x, float y, float z){
		modelMat.leftMult(MatrixUtil.scale(x, y, z));
		collider.scale(x, y, z);
		boundingVolume.scale(x, y, z);
	}
	
	public void scale(Vec3 scalars){
		modelMat.leftMult(MatrixUtil.scale(scalars));
		collider.scale(scalars);
		boundingVolume.scale(scalars);
	}
	
	public void rotate(float x, float y, float z, float theta){
		modelMat.leftMult(Quaternion.fromAxisAngle(x, y, z, theta).asMatrix());
	}
	
	public void rotate(Vec3 axis, float theta){
		modelMat.leftMult(Quaternion.fromAxisAngle(axis, theta).asMatrix());
	}
	
	public void orient(float x, float y, float z, float theta){
		orientation.set(Quaternion.multiply(Quaternion.fromAxisAngle(x, y, z, theta), orientation));
		collider.orient(x, y, z, theta);
		//additional code needs to be written for changing the bounding volume
	}
	
	public void orient(Vec3 axis, float theta){
		orientation.set(Quaternion.multiply(Quaternion.fromAxisAngle(axis, theta), orientation));
		collider.orient(axis, theta);
	}
	
	public void orient(Vec3 angles){
		orientation.set(Quaternion.multiply(new Quaternion(angles), orientation));
		collider.orient(angles);
	}
	
	public void orient(float roll, float pitch, float yaw){
		orientation.set(Quaternion.multiply(new Quaternion(roll, pitch, yaw), orientation));
		collider.orient(roll, pitch, yaw);
	}
	
	public Quaternion getOrientation(){
		return orientation;
	}
	
	public void setOrientation(Quaternion orient){
		orientation.set(orient);
		collider.setOrientation(orient);
	}
	
	public Mat4 getModelMatrix(){
		return (Mat4)MatrixUtil.multiply(modelMat, orientation.asMatrix());
	}
	
	public Mat3 getNormalMatrix(){
		return this.getModelMatrix().getNormalMatrix();
	}
	
	public FloatBuffer getMatrixBuffer(){
		return this.getModelMatrix().asBuffer();
	}
	
	public FloatBuffer getNMatrixBuffer(){
		return this.getNormalMatrix().asBuffer();
	}
	
	protected int getVertexBuffer(){
		return vbo;
	}
	
	protected int getVertexArray(){
		return vao;
	}
	
	protected int getIndexBuffer(){
		return ibo;
	}
	
	public int getVattrib(){
		return vAttrib;
	}
	
	public int getNattrib(){
		return nAttrib;
	}
	
	public abstract ArrayList<Triangle> getFaces();
	
	public abstract ArrayList<Vertex> getVertices();
	
	public abstract int getNumIndices();
	
	public boolean hasAdjData(){
		return isAdjBuffered;
	}
	
	public void resetModel(){
		modelMat.loadIdentity();
		collider.resetModel();
		boundingVolume.resetModel();
	}
	
	public void resetOrientation(){
		this.orientation.set(0, 0, 0);
		collider.resetOrientation();
		boundingVolume.resetOrientation();
	}
	
	public void reset(){
		modelMat.loadIdentity();
		this.orientation.set(0, 0, 0);
		collider.reset();
		boundingVolume.reset();
	}
	
	protected void setUpTriangle(Triangle face, HashMap<Triangle.Edge, Triangle.HalfEdge> edgesMap){
		//create the half edge map to the edges of the face
		for(Triangle.Edge edge : face.edges){
			Triangle.HalfEdge halfEdge = new Triangle.HalfEdge(edge.start);
			edgesMap.put(edge, halfEdge);
			halfEdge.parent = face;
			face.halfEdges.add(halfEdge);
		}
		/*create the linked half edges of the face
		and link the opposite half edges if the
		half edge exists in the map*/
		int numEdges = face.edges.length;
		for(int edgeGet = 0; edgeGet < numEdges; edgeGet++){
			Triangle.Edge edge = face.edges[edgeGet];
			//set the half edge that is next in the triangle after the current half edge
			face.halfEdges.get(edgeGet).next = face.halfEdges.get((edgeGet+1)%numEdges);
			//generate the key to get the opposite half edge
			Triangle.Edge oppositeEdge = new Triangle.Edge(edge.end,  edge.start);
			//if the map contains the opposite key set the two half edges opposite pointers
			if(edgesMap.containsKey(oppositeEdge)){
				Triangle.HalfEdge h1 = edgesMap.get(edge);
				Triangle.HalfEdge h2 = edgesMap.get(oppositeEdge);
				h1.opposite = h2;
				h2.opposite = h1;
			}
		}
	}
	
	public void delete(){
		glBindVertexArray(0);
		glDeleteVertexArrays(vao);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		glDeleteBuffers(vbo);
		glDeleteBuffers(ibo);
	}
	
	public CollisionMesh getCollisionMesh(){
		return collider;
	}
	
	public AABB getBoundingVolume(){
		return boundingVolume;
	}
	
	public abstract void render();
	
	public abstract Mat3 computeTensor(float mass);
}
