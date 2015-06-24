package collision;

import glMath.Mat3;
import glMath.Mat4;
import glMath.MatrixUtil;
import glMath.Vec3;
import glMath.Quaternion;

public class CollisionPlane implements CollisionMesh{
	
	private Mat4 modelMat, origModel;
	private Quaternion orientation, origOrient;
	private Vec3[] verts;
	private float side1, side2;
	
	public CollisionPlane(float side1, float side2){
		verts = new Vec3[]{
			new Vec3(side1, 0,side2),
			new Vec3(side1, 0,-side2),
			new Vec3(-side1, 0,side2),
			new Vec3(-side1, 0,-side2),
		};
		modelMat = new Mat4(1);
		orientation = new Quaternion();
		origModel = new Mat4(1);
		origOrient = new Quaternion();
		this.side1 = side1;
		this.side2 = side2;
	}
	
	public CollisionPlane(float sideLength){
		this(sideLength, sideLength);
	}
	
	public CollisionPlane(CollisionPlane copy) {
		this.modelMat = new Mat4(copy.modelMat);
		this.orientation = new Quaternion(copy.orientation);
		origModel = new Mat4(copy.modelMat);
		origOrient = new Quaternion(copy.orientation);
		this.verts = copy.verts;
		this.side1 = copy.side1;
		this.side2 = copy.side2;
	}
	
	public void translate(float x, float y, float z){
		modelMat.leftMult(MatrixUtil.translate(x, y, z));
	}
	
	public void translate(Vec3 translation){
		modelMat.leftMult(MatrixUtil.translate(translation));
	}
	
	public void scale(float factor){
		modelMat.leftMult(MatrixUtil.scale(factor, factor, factor));
	}
	
	public void scale(float x, float y, float z){
		modelMat.leftMult(MatrixUtil.scale(x, y, z));
	}
	
	public void scale(Vec3 scalars){
		modelMat.leftMult(MatrixUtil.scale(scalars));
	}
	
	public void rotate(float x, float y, float z, float theta){
		modelMat.leftMult(Quaternion.fromAxisAngle(x, y, z, theta).asMatrix());
	}
	
	public void rotate(Vec3 axis, float theta){
		modelMat.leftMult(Quaternion.fromAxisAngle(axis, theta).asMatrix());
	}
	
	public void orient(float x, float y, float z, float theta){
		orientation.set(Quaternion.multiply(Quaternion.fromAxisAngle(x, y, z, theta), orientation));
	}
	
	public void orient(Vec3 axis, float theta){
		orientation.set(Quaternion.multiply(Quaternion.fromAxisAngle(axis, theta), orientation));
	}
	
	public void orient(Vec3 angles){
		orientation.set(Quaternion.multiply(new Quaternion(angles), orientation));
	}
	
	public void orient(float roll, float pitch, float yaw){
		orientation.set(Quaternion.multiply(new Quaternion(roll, pitch, yaw), orientation));
	}
	
	public void setOrientation(Quaternion orient){
		orientation.set(orient);
	}
	
	public Quaternion getOrientation(){
		return orientation;
	}
	
	public Mat4 getModelMatrix(){
		return modelMat;
	}
	
	public Mat3 getNormalMatrix(){
		return this.getModelMatrix().getNormalMatrix();
	}
	
	public Vec3 getCenter(){
		return (Vec3)getModelMatrix().multVec(center).swizzle("xyz");
	}
	
	@Override
	public void setData(Mat4 modelMat, Quaternion orient){
		this.modelMat.setMatrix(modelMat);
		orientation.set(orient);
	}
	
	@Override
	public void setData(Mat4 modelMat){
		this.modelMat.setMatrix(modelMat);
		orientation.set(0,0,0);
	}

	@Override
	public Vec3 support(Vec3 direction){
		Mat4 transformMat = (Mat4)MatrixUtil.multiply(modelMat, orientation.asMatrix());
		
		Vec3 vert1 = (Vec3)transformMat.multVec(verts[0]).swizzle("xyz");
		float maxDot = vert1.dot(direction);
		Vec3 finalVert = new Vec3(vert1);
		
		Vec3 vert2 = (Vec3)transformMat.multVec(verts[1]).swizzle("xyz");
		if(vert2.dot(direction) > maxDot){
			finalVert.set(vert2);
			maxDot = vert2.dot(direction);
		}
		
		Vec3 vert3 = (Vec3)transformMat.multVec(verts[2]).swizzle("xyz");
		if(vert3.dot(direction) > maxDot){
			finalVert.set(vert3);
			maxDot = vert3.dot(direction);
		}
		
		Vec3 vert4 = (Vec3)transformMat.multVec(verts[3]).swizzle("xyz");
		if(vert4.dot(direction) > maxDot){
			finalVert.set(vert4);
			maxDot = vert4.dot(direction);
		}
		
		return finalVert;
	}

	@Override
	public CollisionMesh copy() {
		return new CollisionPlane(this);
	}

	@Override
	public void resetModel() {
		modelMat.setMatrix(origModel);
	}

	@Override
	public void resetOrientation() {
		orientation.set(origOrient);
	}

	@Override
	public void reset() {
		modelMat.setMatrix(origModel);
		orientation.set(origOrient);
	}
}
