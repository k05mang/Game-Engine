package collision;

import glMath.Mat4;
import glMath.MatrixUtil;
import glMath.Quaternion;
import glMath.Vec3;

public class OBB implements CollisionMesh{

	private Mat4 modelMat, origModel;
	private Quaternion orientation, origOrient;
	private Vec3 dimensions, halfDimensions;
	
	public OBB(){
		this(1,1,1);
	}
	
	public OBB(float width, float height, float length){
		dimensions = new Vec3(width, height, length);
		halfDimensions = new Vec3(width/2.0f,height/2.0f,length/2.0f);
		modelMat = new Mat4(1);
		orientation = new Quaternion();
		origModel = new Mat4(1);
		origOrient = new Quaternion();
	}
	
	public OBB(Vec3 dimensions){
		this(dimensions.x, dimensions.y, dimensions.z);
	}
	
	public OBB(float scale){
		this(scale, scale, scale);
	}
	
	public OBB(OBB copy) {
		this.modelMat = new Mat4(copy.modelMat);
		this.orientation = new Quaternion(copy.orientation);
		origModel = new Mat4(copy.modelMat);
		origOrient = new Quaternion(copy.orientation);
		this.dimensions = new Vec3(copy.dimensions);
		this.halfDimensions = new Vec3(copy.halfDimensions);
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
	
	public Quaternion getOrientation(){
		return orientation;
	}
	
	public void setOrientation(Quaternion orient){
		orientation.set(orient);
	}
	
	public Mat4 getModelMatrix(){
		return modelMat;
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
	public Vec3 getCenter(){
		return (Vec3)modelMat.multVec(center).swizzle("xyz");
	}
	
	@Override
	public Vec3 support(Vec3 direction){
		Mat4 transformMat = (Mat4)MatrixUtil.multiply(modelMat, orientation.asMatrix());
		Vec3 relativeDir = this.orientation.multVec(direction);
		
		return (Vec3)transformMat.multVec(new Vec3(
				Math.copySign(halfDimensions.x, relativeDir.x),
				Math.copySign(halfDimensions.y, relativeDir.y),
				Math.copySign(halfDimensions.z, relativeDir.z)
				)).swizzle("xyz");
	}

	@Override
	public CollisionMesh copy() {
		return new OBB(this);
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
