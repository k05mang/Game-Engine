package core;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL33.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import glMath.Mat4;
import glMath.MatrixUtil;
import glMath.Vec3;
import glMath.VecUtil;
import primitives.Sphere;
import renderers.Texture;
import static renderers.Texture.CUBE_MAP;


public class PointLight implements Light {

	private float lightRadius, intensity;
	private Vec3 pos, color;
	private Texture shadowMap;
	private Camera cameras[];
	private Sphere light, volume;
	private Mat4 perspective;
	private static final int 
	XPOS_CAM = 0, 
	XNEG_CAM = 1, 
	YPOS_CAM = 2, 
	YNEG_CAM = 3, 
	ZPOS_CAM = 4, 
	ZNEG_CAM = 5, 
	NUM_CAMS = 6, 
	VOLUME_FINENESS = 10,
	SHADOW_MAP_SIZE = 1024;
	
	public PointLight(float posX, float posY, float posZ, float r, float g, float b, 
			float lightRad, float volumeRad, float intensity){
		pos = new Vec3(posX, posY, posZ);
		color = new Vec3(r, g, b);
		
		lightRadius = lightRad;
		this.intensity = intensity;
		light = new Sphere(lightRad, 40,40, 0, false);
		volume = new Sphere(volumeRad, VOLUME_FINENESS, 0,1, false);//new Sphere(lightRad+intensity*lightRad, VOLUME_FINENESS, 0,1, false);
		volume.translate(pos);
		perspective = MatrixUtil.getPerspective(90, 1, .01f, volumeRad);
		
		cameras = new Camera[]{
			new Camera(pos, -90,0),
			new Camera(pos, 90,0),
			new Camera(pos, 0,90),
			new Camera(pos, 0,-90),
			new Camera(pos, 180,0),
			new Camera(pos, 0,0) };
	}
	
	public PointLight(Vec3 pos, Vec3 color, float lightRad, float volumeRad, float intensity){
		this(pos.x, pos.y, pos.z, color.x, color.y, color.z, lightRad, volumeRad, intensity);
	}
	public PointLight(Vec3 pos, float r, float g, float b, float lightRad, float volumeRad, float intensity){
		this(pos.x, pos.y, pos.z, r, g, b, lightRad, volumeRad, intensity);
	}
	public PointLight(float posX, float posY, float posZ, Vec3 color, float lightRad, float volumeRad, float intensity){
		this(posX, posY, posZ, color.x, color.y, color.z, lightRad, volumeRad, intensity);
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
	
	public int getTextureId(){
		return shadowMap.getId();
	}

	public void setPos(float x, float y, float z){
		volume.translate((Vec3)VecUtil.subtract(new Vec3(x, y, z), pos));
		pos.set(x,y,z);
		for(int moveCams = 0; moveCams < NUM_CAMS; moveCams++){
			cameras[moveCams].moveTo(x,y,z);
		}
	}
	
	public void setPos(Vec3 loc){
		volume.translate((Vec3)VecUtil.subtract(loc, pos));
		pos.set(loc);
		for(int moveCams = 0; moveCams < NUM_CAMS; moveCams++){
			cameras[moveCams].moveTo(loc);
		}
	}
	
	public Vec3 getPos(){
		return pos;
	}
	
	public FloatBuffer getPosBuffer(){
		return pos.asBuffer();
	}
	
	public FloatBuffer getModelBuffer(){
		return volume.getModelMatrix().asBuffer();
	}
	
	@Override
	public Mat4 getPerspective(){
		return perspective;
	}
	
	@Override
	public FloatBuffer getPerspectiveBuffer(){
		return perspective.asBuffer();
	}
	
	@Override
	public void setColor(float r, float g, float b) {
		color.set(r,g,b);
	}
	
	@Override
	public void setColor(Vec3 color) {
		this.color.set(color);
	}
	
	@Override
	public Vec3 getColor() {
		return color;
	}
	
	@Override
	public FloatBuffer getColorBuffer() {
		return color.asBuffer();
	}

	@Override
	public void storeColor(FloatBuffer buffer) {
		this.color.store(buffer);
	}
	
	@Override
	public void genShadowMap(){
		shadowMap = Texture.genEmpty(CUBE_MAP, SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, GL_DEPTH_COMPONENT);
		
		shadowMap.setParam(Texture.MAG_FILTER, GL_LINEAR);
		shadowMap.setParam(Texture.MIN_FILTER, GL_LINEAR);
		shadowMap.setParam(Texture.COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		shadowMap.setParam(Texture.COMPARE_FUNC, GL_LESS);
	}
	
	@Override
	public Texture getShadowMap(){
		return shadowMap;
	}
	
	@Override
	public void bindShadow(int texUnit) {
		shadowMap.bind(texUnit);
	}

	@Override
	public void render() {
		light.render();
	}
	
	public void renderVolume(){
		volume.render();
	}
	
	@Override
	public void cleanUp() {
		if(shadowMap != null){
			shadowMap.delete();
		}
		light.delete();
		volume.delete();
	}
}
