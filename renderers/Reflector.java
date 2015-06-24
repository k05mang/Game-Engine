package renderers;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import core.Camera;
import core.GameObject;
import static org.lwjgl.opengl.GL11.*;
import glMath.*;
import static renderers.Texture.*;

public class Reflector {
	
	private Vec3 center;
	private Texture color, depth;
	private Camera cameras[];
	private Mat4 perspective, modelMat;
	private static final int 
	XPOS_CAM = 0, 
	XNEG_CAM = 1, 
	YPOS_CAM = 2, 
	YNEG_CAM = 3, 
	ZPOS_CAM = 4, 
	ZNEG_CAM = 5, 
	NUM_CAMS = 6, 
	MAP_SIZE = 1024;
	
	public Reflector(float depth){
		this(depth, 0,0,0);
	}
	
	public Reflector(float depth, Vec3 center){
		this(depth, center.x, center.y, center.z);
	}
	
	public Reflector(float depth, float cX, float cY, float cZ){
		cameras = new Camera[]{
			new Camera(cX, cY, cZ, -90,0),
			new Camera(cX, cY, cZ, 90,0),
			new Camera(cX, cY, cZ, 0,-90),
			new Camera(cX, cY, cZ, 0,90),
			new Camera(cX, cY, cZ, 0,0),
			new Camera(cX, cY, cZ, 180,0) 
			};
		center = new Vec3(cX, cY, cZ);
		modelMat = new Mat4(1);
		perspective = MatrixUtil.getPerspective(90, 1, .01f, depth);
		color = genEmpty(CUBE_MAP, MAP_SIZE, MAP_SIZE, GL_RGB);
		this.depth = genEmpty(CUBE_MAP, MAP_SIZE, MAP_SIZE, GL_DEPTH_COMPONENT);
	}
	
	public FloatBuffer[] getCamBuffers(){
		return new FloatBuffer[]{
			cameras[XPOS_CAM].getLookAt().asBuffer(),
			cameras[XNEG_CAM].getLookAt().asBuffer(),
			cameras[YPOS_CAM].getLookAt().asBuffer(),
			cameras[YNEG_CAM].getLookAt().asBuffer(),
			cameras[ZPOS_CAM].getLookAt().asBuffer(),
			cameras[ZNEG_CAM].getLookAt().asBuffer()
		};
	}
	
	public FloatBuffer getCamBuffer(){
		FloatBuffer buffer = BufferUtils.createFloatBuffer(6*Mat4.SIZE_IN_FLOATS);
		
		for(int moveCams = 0; moveCams < NUM_CAMS; moveCams++){
			buffer.put(cameras[moveCams].getLookAt().asBuffer());
		}
		buffer.flip();
		
		return buffer;
	}
	
	public Camera[] getCameras(){
		return cameras;
	}

	public void setPos(float x, float y, float z){
		for(int moveCams = 0; moveCams < NUM_CAMS; moveCams++){
			cameras[moveCams].moveTo(x,y,z);
		}
	}
	
	public void setPos(Vec3 loc){
		for(int moveCams = 0; moveCams < NUM_CAMS; moveCams++){
			cameras[moveCams].moveTo(loc);
		}
	}
	
	public void translate(float x, float y, float z){
		modelMat.leftMult(MatrixUtil.translate(x, y, z));
		this.setPos((Vec3)modelMat.multVec(center).swizzle("xyz"));
	}
	
	public void translate(Vec3 translation){
		modelMat.leftMult(MatrixUtil.translate(translation));
		this.setPos((Vec3)modelMat.multVec(center).swizzle("xyz"));
	}
	
	public void rotate(float x, float y, float z, float theta){
		modelMat.leftMult(Quaternion.fromAxisAngle(x, y, z, theta).asMatrix());
		this.setPos((Vec3)modelMat.multVec(center).swizzle("xyz"));
	}
	
	public void rotate(Vec3 axis, float theta){
		modelMat.leftMult(Quaternion.fromAxisAngle(axis, theta).asMatrix());
		this.setPos((Vec3)modelMat.multVec(center).swizzle("xyz"));
	}
	
	public Vec3 getPos(){
		return (Vec3)modelMat.multVec(center).swizzle("xyz");
	}
	
	public void setPos(GameObject target){
		modelMat.loadIdentity();
		center.set(target.getPosition());
		for(int moveCams = 0; moveCams < NUM_CAMS; moveCams++){
			cameras[moveCams].moveTo(center);
		}
	}
	
	public FloatBuffer getPosBuffer(){
		return getPos().asBuffer();
	}

	public Mat4 getPerspective(){
		return perspective;
	}
	
	public FloatBuffer getPerspectiveBuffer(){
		return perspective.asBuffer();
	}
	
	public void bindRead(int texUnit){
		color.bind(texUnit);
	}
	
	public void bindWrite(FBO buffer){
		buffer.setColor(color, false, 0);
		buffer.setDepth(depth);
	}
	
	public void delete(){
		color.delete();
		depth.delete();
	}
}
