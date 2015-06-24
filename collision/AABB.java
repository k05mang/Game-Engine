package collision;

import glMath.Mat4;
import glMath.MatrixUtil;
import glMath.Quaternion;
import glMath.Vec3;

public class AABB implements CollisionMesh{
	
	private Mat4 modelMat;
	private Vec3 halfDimensions;
	
	public AABB(){
		this(1,1,1);
	}
	
	public AABB(float width, float height, float length){
		halfDimensions = new Vec3(width/2.0f,height/2.0f,length/2.0f);
		modelMat = new Mat4(1);
	}
	
	public AABB(Vec3 dimensions){
		this(dimensions.x, dimensions.y, dimensions.z);
	}
	
	public AABB(float scale){
		this(scale, scale, scale);
	}
	
	public AABB(AABB copy) {
		this.modelMat = new Mat4(copy.modelMat);
		this.halfDimensions = new Vec3(copy.halfDimensions);
	}
	
	public boolean colliding(AABB collider){
		Vec3 thisCenter = (Vec3)modelMat.multVec(center).swizzle("xyz");
		Vec3 colliderCenter = collider.getCenter();
		//max x value of this < min x of collider or min x value of this < max x value of collider
		boolean x = thisCenter.x+halfDimensions.x > colliderCenter.x-collider.halfDimensions.x //case where this is left of collider
				&&
				thisCenter.x-halfDimensions.x < colliderCenter.x+collider.halfDimensions.x;//case where this is right of collider
				
		//max y value of this < min y of collider or min y value of this < max y value of collider
		boolean y = thisCenter.y+halfDimensions.y > colliderCenter.y-collider.halfDimensions.y //case where this is below of collider
				&&
				thisCenter.y-halfDimensions.y < colliderCenter.y+collider.halfDimensions.y;//case where this is above of collider
				
		//max z value of this < min z of collider or min z value of this < max z value of collider
		boolean z = thisCenter.z+halfDimensions.z > colliderCenter.z-collider.halfDimensions.z //case where this is behind of collider
				&&
				thisCenter.z-halfDimensions.z < colliderCenter.z+collider.halfDimensions.z;//case where this is front of collider
				
		//all three must be true for the boxes to be colliding
		return x && y && z;
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
	}
	
	public void rotate(Vec3 axis, float theta){
	}
	
	public void orient(float x, float y, float z, float theta){
	}
	
	public void orient(Vec3 axis, float theta){
	}
	
	public void orient(Vec3 angles){
	}
	
	public void orient(float roll, float pitch, float yaw){
	}
	
	public Quaternion getOrientation(){
		return null;
	}
	
	public void setOrientation(Quaternion orient){
	}
	
	public Mat4 getModelMatrix(){
		return modelMat;
	}
	
	public Vec3 getHalfDimensions(){
		return halfDimensions;
	}
	
	@Override
	public void setData(Mat4 modelMat, Quaternion orient){
		this.modelMat.setMatrix(modelMat);
	}
	
	@Override
	public void setData(Mat4 modelMat){
		this.modelMat.setMatrix(modelMat);
	}
	
	@Override
	public Vec3 getCenter(){
		return (Vec3)modelMat.multVec(center).swizzle("xyz");
	}
	
	@Override
	public Vec3 support(Vec3 direction){
		
		return (Vec3)modelMat.multVec(new Vec3(
				Math.copySign(halfDimensions.x, direction.x),
				Math.copySign(halfDimensions.y, direction.y),
				Math.copySign(halfDimensions.z, direction.z)
				)).swizzle("xyz");
	}

	@Override
	public AABB copy() {
		return new AABB(this);
	}

	@Override
	public void resetModel() {
		this.modelMat.loadIdentity();
	}

	@Override
	public void resetOrientation() {
		//there is no orientation to reset so do nothing
	}

	@Override
	public void reset() {
		this.modelMat.loadIdentity();
	}
}
