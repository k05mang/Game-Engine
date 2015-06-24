package collision;

import glMath.*;

public class BoundingSphere implements CollisionMesh{

	private float radius;
	private Vec3 center, origCenter;
	
	public BoundingSphere(float radius){
		this.radius = radius;
		center = new Vec3(0,0,0);
		origCenter = new Vec3(0,0,0);
	}
	
	public BoundingSphere(float radius, Vec3 center){
		this.radius = radius;
		this.center = new Vec3(center);
		origCenter = new Vec3(center);
	}
	
	public BoundingSphere(float radius, float cX, float cY, float cZ){
		this.radius = radius;
		this.center = new Vec3(cX, cY, cZ);
		origCenter = new Vec3(cX, cY, cZ);
	}
	
	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	@Override
	public Vec3 getCenter() {
		return center;
	}

	public void translate(Vec3 translation){
		center.add(translation);
	}
	
	public void translate(float x, float y, float z){
		center.add(x,y,z);
	}

	@Override
	public void setData(Mat4 modelMat, Quaternion orient) {
		center.set((Vec3)modelMat.multVec(CollisionMesh.center).swizzle("xyz"));
	}
	
	@Override
	public void setData(Mat4 modelMat){
		center.set((Vec3)modelMat.multVec(CollisionMesh.center).swizzle("xyz"));
	}

	@Override
	public Vec3 support(Vec3 direction) {
		Vec3 dirScaled = new Vec3(direction);
		dirScaled.normalize();
		dirScaled.scale(radius);
		return (Vec3)VecUtil.add(center, dirScaled);
	}

	@Override
	public CollisionMesh copy() {
		return new BoundingSphere(radius, center);
	}

	@Override
	public void orient(float x, float y, float z, float theta) {
	}

	@Override
	public void orient(Vec3 axis, float theta) {
	}
	
	public void orient(Vec3 angles){
	}
	
	public void orient(float roll, float pitch, float yaw){
	}
	
	public void setOrientation(Quaternion orient){
	}

	@Override
	public void resetModel() {
		center.set(origCenter);
	}

	@Override
	public void resetOrientation() {
		//there is no orientation to reset so do nothing
	}

	@Override
	public void reset() {
		center.set(origCenter);
	}

	@Override
	public void scale(float factor) {
		radius *= factor;
	}

	@Override
	public void scale(float x, float y, float z) {
		
	}

	@Override
	public void scale(Vec3 scalars) {
		//do nothing since this collision mesh can only be uniformly scaled
	}
}
