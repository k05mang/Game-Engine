package collision;

import glMath.Mat4;
import glMath.MatrixUtil;
import glMath.Quaternion;
import glMath.Vec3;

public interface CollisionMesh {
	public static Vec3 center = new Vec3(0,0,0);
	
	public Vec3 getCenter();
	
	public void setData(Mat4 modelMat, Quaternion orient);
	
	public void setData(Mat4 modelMat);
	
	public void resetModel();
	
	public void resetOrientation();
	
	public void reset();
	
	public Vec3 support(Vec3 direction);
	
	public void translate(float x, float y, float z);
	 
	public void translate(Vec3 translation);
	
	public void scale(float factor);
	
	public void scale(float x, float y, float z);
	
	public void scale(Vec3 scalars);
	
	public void orient(float x, float y, float z, float theta);
	
	public void orient(Vec3 axis, float theta);
	
	public void orient(Vec3 angles);
	
	public void orient(float roll, float pitch, float yaw);
	
	public void setOrientation(Quaternion orient);
	
	public CollisionMesh copy();
}
