package core;
import glMath.*;

import static glMath.VecUtil.*;

public class Camera {
	
	private float theta, phi;
	private Vec3 eye, forward, right, up;
	private Mat4 projection;
	/*
	 * theta: Horizontal rotation in degrees
	 * phi: Vertical rotation in degrees
	 * eye: The x,y,z, coordinates of the camera
	 * forward: The forward vector for the camera look at matrix
	 * right: The right vector for the camera look at matrix
	 * up: The up vector for the camera look at matrix
	 * projection: The projection matrix for this camera 
	 */
	
	/**
	 * Constructs a camera object without any projection matrix, the camera is centered
	 * at the origin, defined as (0,0,0) with 0 horizontal and vertical rotation.
	 */
	public Camera(){
		this(0,0,0,0,0);
	}
	
	/**
	 * Creates a camera with the given x, y, z eye position, and a theta horizontal rotation
	 * and a phi vertical rotation.
	 * 
	 * @param x the x component of the camera's location
	 * @param y the y component of the camera's location
	 * @param z the z component of the camera's location
	 * @param theta the camera's horizontal rotation (in degrees)
	 * @param phi the camera's vertical rotation (in degrees)
	 */
	public Camera( float x, float y, float z,
			float theta , float phi) {
		this.theta = theta;
		this.phi = phi;
		eye = new Vec3(x, y, z);
		forward = new Vec3(0,0,-1);
		right = new Vec3(1,0,0);
		up = new Vec3(0,1,0);
		projection = null;
	}
	
	/**
	 * Creates a camera with the given vector eye position, and a theta horizontal rotation
	 * and a phi vertical rotation.
	 * 
	 * @param eye Vector that will be used for this cameras starting position
	 * @param theta the camera's horizontal rotation (in degrees)
	 * @param phi the camera's vertical rotation (in degrees)
	 */
	public Camera( Vec3 eye, float theta , float phi) {
		this.theta = theta;
		this.phi = phi;
		this.eye = new Vec3(eye);
		forward = new Vec3(0,0,-1);
		right = new Vec3(1,0,0);
		up = new Vec3(0,1,0);
		projection = null;
	}
	
	/**
	 * Creates a camera with the given x, y, z eye position, and a theta horizontal rotation
	 * and a phi vertical rotation, additionall this creates and stores a perspective projection
	 * matrix formed using the given fovy, aspect, zNear, and zFar.
	 * 
	 * @param x the x component of the camera's location
	 * @param y the y component of the camera's location
	 * @param z the z component of the camera's location
	 * @param theta the camera's horizontal rotation (in degrees)
	 * @param phi the camera's vertical rotation (in degrees)
	 * @param fovy Field of View angle for the y direction 
	 * @param aspect Aspect ratio of the area being rendered to
	 * @param zNear The nearest z value to the view before being clipped
	 * @param zFar The farthest z value from the view before being clipped
	 */
	public Camera( float x, float y, float z,
			float theta , float phi,
			float fovy, float aspect, float zNear, float zFar) {
		this.theta = theta;
		this.phi = phi;
		eye = new Vec3(x, y, z);
		forward = new Vec3(0,0,-1);
		right = new Vec3(1,0,0);
		up = new Vec3(0,1,0);
		projection = MatrixUtil.getPerspective(fovy, aspect, zNear, zFar);
	}

	/**
	 * Creates a camera with the given vector eye position, and a theta horizontal rotation
	 * and a phi vertical rotation, additionall this creates and stores a perspective projection
	 * matrix formed using the given fovy, aspect, zNear, and zFar.
	 * 
	 * @param eye Vector that will be used for this cameras starting position
	 * @param theta the camera's horizontal rotation (in degrees)
	 * @param phi the camera's vertical rotation (in degrees)
	 * @param fovy Field of View angle for the y direction 
	 * @param aspect Aspect ratio of the area being rendered to
	 * @param zNear The nearest z value to the view before being clipped
	 * @param zFar The farthest z value from the view before being clipped
	 */
	public Camera( Vec3 eye, float theta , float phi,
			float fovy, float aspect, float zNear, float zFar) {
		this.theta = theta;
		this.phi = phi;
		this.eye = new Vec3(eye);
		forward = new Vec3(0,0,-1);
		right = new Vec3(1,0,0);
		up = new Vec3(0,1,0);
		projection = MatrixUtil.getPerspective(fovy, aspect, zNear, zFar);
	}

	/**
	 * Creates a camera with the given x, y, z eye position, and a theta horizontal rotation
	 * and a phi vertical rotation, additionall this creates and stores an orthographic projection
	 * matrix formed using the given left, right, bottom, top, zNear, and zFar values.
	 * 
	 * @param x the x component of the camera's location
	 * @param y the y component of the camera's location
	 * @param z the z component of the camera's location
	 * @param theta the camera's horizontal rotation (in degrees)
	 * @param phi the camera's vertical rotation (in degrees)
	 * @param left The minimum x clipping value
	 * @param right The maximum x clipping value
	 * @param bottom The minimum y clipping value
	 * @param top The maximum y clipping value
	 * @param zNear The nearest z value to the view before being clipped
	 * @param zFar The farthest z value from the view before being clipped
	 */
	public Camera( float x, float y, float z,
			float theta , float phi,
			float left, float right, float bottom, float top, float zNear, float zFar) {
		this.theta = theta;
		this.phi = phi;
		eye = new Vec3(x, y, z);
		forward = new Vec3(0,0,-1);
		this.right = new Vec3(1,0,0);
		up = new Vec3(0,1,0);
		projection = MatrixUtil.getOrtho(left, right, bottom, top, zNear, zFar);
	}

	/**
	 * Creates a camera with the given vector eye position, and a theta horizontal rotation
	 * and a phi vertical rotation, additionall this creates and stores an orthographic projection
	 * matrix formed using the given left, right, bottom, top, zNear, and zFar values.
	 * 
	 * @param eye Vector that will be used for this cameras starting position
	 * @param theta the camera's horizontal rotation (in degrees)
	 * @param phi the camera's vertical rotation (in degrees)
	 * @param left The minimum x clipping value
	 * @param right The maximum x clipping value
	 * @param bottom The minimum y clipping value
	 * @param top The maximum y clipping value
	 * @param zNear The nearest z value to the view before being clipped
	 * @param zFar The farthest z value from the view before being clipped
	 */
	public Camera( Vec3 eye, float theta , float phi,
			float left, float right, float bottom, float top, float zNear, float zFar) {
		this.theta = theta;
		this.phi = phi;
		this.eye = new Vec3(eye);
		forward = new Vec3(0,0,-1);
		this.right = new Vec3(1,0,0);
		up = new Vec3(0,1,0);
		projection = MatrixUtil.getOrtho(left, right, bottom, top, zNear, zFar);
	}

	public Mat4 getLookAt() {
		Quaternion rot = new Quaternion(phi,theta,0);
		
		forward = rot.multVec(VecUtil.zAxis);
		up = rot.multVec(VecUtil.yAxis);
		right = up.cross(forward);
		
		return  new Mat4(
				new Vec4(right.x, up.x, forward.x, 0),
				new Vec4(right.y, up.y, forward.y, 0),
				new Vec4(right.z, up.z, forward.z, 0),
				new Vec4(-dot(right,eye),-dot(up,eye),-dot(forward,eye),1.0f)
				);
	}
	
	public Mat4 getProjection(){
		return projection;
	}
	
	/**
	 * Increases the camera's rotation angle by the indicated amount. 
	 * 
	 * @param amt amount (in degrees) by which to increase the camera's rotation
	 */
	public void rotate( float amt ) {
		theta = (theta+amt)%360;
	}
	 
	/**
	 * Rotates the camera vertically
	 * 
	 * @param amt amount (in degrees) by which to increase the camera's rotation
	 */
	public void lookY(float amt, boolean restrict){
		if (restrict) {
			phi += (!(phi + amt < -85 || phi + amt > 85) ? amt : 0);
		}else{
			phi += amt;
		}
	}
	
	public void moveTo(float locX, float locY, float locZ){
		eye.set(locX, locY, locZ);
	}
	
	public void moveTo(Vec3 position){
		eye.set(position);
	}
	
	public void strafe(float amt){
		eye.add(right.x*amt, right.y*amt, right.z*amt);
	}
	
	/**
	 * Moves the camera in a horizontal direction independent of where the camera is facing (walking)
	 * 
	 * @param amt how far to move 
	 */
	public void move(float amt){
		Quaternion rot = new Quaternion(0,theta,0);
		
		Vec3 dir = rot.multVec(VecUtil.zAxis);
		
		eye.add(dir.scale(amt));
	}
	
	public void moveUpDown(float amt){
		eye.set(1, eye.y+amt);
	}
	
	public void fly(float amt){
		eye.add(forward.x*amt, forward.y*amt, forward.z*amt);
	}
	
	public Vec3 getEye(){
		return eye;
	}
	
	public Vec3 getForwardVec(){
		return forward;
	}
}