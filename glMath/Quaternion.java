package glMath;

public class Quaternion {
	private Vec4 data;
	
	public Quaternion(){
		data = new Vec4(0,0,0,1);
	}
	
	public Quaternion(float x, float y, float z, float w){
		data = new Vec4(x, y, z, w);
	}
	
	public Quaternion(Vec4 data){
		this.data = new Vec4(data);
	}
	
	public Quaternion(Vec3 angles){
		Vec3 radAngles = (new Vec3(angles)).scale((float)(Math.PI/180)/2.0f);
		
		float sinr = (float)Math.sin(radAngles.x);
		float sinp = (float)Math.sin(radAngles.y);
		float siny = (float)Math.sin(radAngles.z);
		
		float cosr = (float)Math.cos(radAngles.x);
		float cosp = (float)Math.cos(radAngles.y);
		float cosy = (float)Math.cos(radAngles.z);
		
	 
		data = new Vec4(sinr*cosp*cosy - cosr*sinp*siny,
				cosr*sinp*cosy + sinr*cosp*siny,
				cosr*cosp*siny - sinr*sinp*cosy,
				cosr*cosp*cosy + sinr*sinp*siny);
		
		this.normalize();
	}
	
	public Quaternion(float roll, float pitch, float yaw){
		Vec3 radAngles = (new Vec3(roll, pitch, yaw)).scale((float)(Math.PI/180)/2.0f);
		
		float sinr = (float)Math.sin(radAngles.x);
		float sinp = (float)Math.sin(radAngles.y);
		float siny = (float)Math.sin(radAngles.z);
		
		float cosr = (float)Math.cos(radAngles.x);
		float cosp = (float)Math.cos(radAngles.y);
		float cosy = (float)Math.cos(radAngles.z);
		
	 
		data = new Vec4(sinr*cosp*cosy - cosr*sinp*siny,
				cosr*sinp*cosy + sinr*cosp*siny,
				cosr*cosp*siny - sinr*sinp*cosy,
				cosr*cosp*cosy + sinr*sinp*siny);
		
		this.normalize();
	}
	
	public Quaternion(Quaternion copy){
		data = new Vec4(copy.getData());
	}
	
	public Quaternion conjugate(){
		data.normalize();
		return new Quaternion(-data.x,-data.y,-data.z,data.w);
	}
	
	public Quaternion mult(Quaternion rhs){
		Vec4 multData = rhs.getData();
		return new Quaternion(data.w*multData.x + data.x*multData.w + data.y*multData.z - data.z*multData.y,
				                data.w*multData.y + data.y*multData.w + data.z*multData.x - data.x*multData.z,
				                data.w*multData.z + data.z*multData.w + data.x*multData.y - data.y*multData.x,
				                data.w*multData.w - data.x*multData.x - data.y*multData.y - data.z*multData.z);
	}
	
	public Quaternion addVector(Vec3 vector){
		Quaternion newRotation = new Quaternion(vector.x, vector.y, vector.z, 0);
//		newRotation.set(newRotation.mult(this));
		newRotation.set(this.mult(newRotation));
		data.x += newRotation.data.x*.5;
		data.y += newRotation.data.y*.5;
		data.z += newRotation.data.z*.5;
		data.w += newRotation.data.w*.5;
//		this.normalize();
		return this;
	}
	
	public Quaternion addVector(float x, float y, float z){
		Quaternion newRotation = new Quaternion(x, y, z, 0);
		newRotation.set(newRotation.mult(this));
		data.x += newRotation.data.x*.5;
		data.y += newRotation.data.y*.5;
		data.z += newRotation.data.z*.5;
		data.w += newRotation.data.w*.5;
		return this;
	}
	
	public void normalize(){
		data.normalize();
	}
	
	public Vec3 multVec(Vec3 vector){
		Vec3 vecCopy = new Vec3(vector);
		vecCopy.normalize();
		Quaternion vec = new Quaternion(new Vec4(vecCopy, 0));
		return (Vec3)multiply(this, vec, conjugate()).getData().swizzle("xyz");
	}
	
	public void set(Quaternion dupe){
		data.set(dupe.getData());
	}
	
	public void set(float roll, float pitch, float yaw){
		Vec3 radAngles = (new Vec3(roll, pitch, yaw)).scale((float)(Math.PI/180)/2.0f);
		
		float sinr = (float)Math.sin(radAngles.x);
		float sinp = (float)Math.sin(radAngles.y);
		float siny = (float)Math.sin(radAngles.z);
		
		float cosr = (float)Math.cos(radAngles.x);
		float cosp = (float)Math.cos(radAngles.y);
		float cosy = (float)Math.cos(radAngles.z);
		
	 
		data.set(sinr*cosp*cosy - cosr*sinp*siny,
				cosr*sinp*cosy + sinr*cosp*siny,
				cosr*cosp*siny - sinr*sinp*cosy,
				cosr*cosp*cosy + sinr*sinp*siny);
		
		this.normalize();
	}
	
	public void set(float x, float y, float z, float w){
		data.set(x,y,z,w);
	}
	
	public Mat4 asMatrix(){
		normalize();
		
		float x2 = data.x*data.x;
		float y2 = data.y*data.y;
		float z2 = data.z*data.z;
		
		float xy = data.x*data.y;
		float xz = data.x*data.z;
		float yz = data.y*data.z;
		
		float wx = data.w*data.x;
		float wy = data.w*data.y;
		float wz = data.w*data.z;
		
		return new Mat4( new Vec4(1.0f - 2.0f * (y2 + z2), 2.0f * (xy - wz), 2.0f * (xz + wy), 0.0f),
						new Vec4(2.0f * (xy + wz), 1.0f - 2.0f * (x2 + z2), 2.0f * (yz - wx), 0.0f),
						new Vec4(2.0f * (xz - wy), 2.0f * (yz + wx), 1.0f - 2.0f * (x2 + y2), 0.0f),
						new Vec4(0.0f, 0.0f, 0.0f, 1.0f));
	}
	
	public Mat3 asRotMatrix(){
		normalize();
		
		float x2 = data.x*data.x;
		float y2 = data.y*data.y;
		float z2 = data.z*data.z;
		
		float xy = data.x*data.y;
		float xz = data.x*data.z;
		float yz = data.y*data.z;
		
		float wx = data.w*data.x;
		float wy = data.w*data.y;
		float wz = data.w*data.z;
		
		return new Mat3( new Vec3(1.0f - 2.0f * (y2 + z2), 2.0f * (xy - wz), 2.0f * (xz + wy)),
						new Vec3(2.0f * (xy + wz), 1.0f - 2.0f * (x2 + z2), 2.0f * (yz - wx)),
						new Vec3(2.0f * (xz - wy), 2.0f * (yz + wx), 1.0f - 2.0f * (x2 + y2)));
	}
	
	public Vec4 getData(){
		return data;
	}
	
	public Vec3 getAxis(){
		Vec3 axis = (Vec3)data.swizzle("xyz");
		axis.normalize();
		return axis;
	}
	
	public float getAngle(){
		return (float)Math.acos(data.w)*2*(float)(180/Math.PI);
	}
	
	public void print(){
		data.print();
	}
	
	public static Quaternion multiply(Quaternion... quats){
		if(quats.length > 1){
			Quaternion result = new Quaternion(quats[0]);
			for(int curQuat = 1; curQuat < quats.length; curQuat++){
				result.set(result.mult(quats[curQuat]));
			}
			return result;
		}else{
			return quats.length == 1 ? quats[0] : null;
		}
	}
	
	public static Quaternion fromAxisAngle(Vec3 axis, float angle){
		Vec3 nAxis = new Vec3(axis);
		nAxis.normalize();
		float sinAngle = (float)Math.sin((angle*Math.PI/180)/2.0f);
		nAxis.scale(sinAngle);
		
		return new Quaternion(new Vec4(nAxis, (float)Math.cos((angle*Math.PI/180)/2.0f)));
	}
	
	public static Quaternion fromAxisAngle(float x, float y, float z, float angle){
		Vec3 nAxis = new Vec3(x, y, z);
		nAxis.normalize();
		float sinAngle = (float)Math.sin((angle*Math.PI/180)/2.0f);
		nAxis.scale(sinAngle);
		
		return new Quaternion(new Vec4(nAxis, (float)Math.cos((angle*Math.PI/180)/2.0f)));
	}
}
