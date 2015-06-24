package core;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

import glMath.Mat4;
import glMath.MatrixUtil;
import glMath.Quaternion;
import glMath.Vec3;
import glMath.VecUtil;

import java.nio.FloatBuffer;

import renderers.Texture;
import primitives.Cone;

public class SpotLight implements Light {
	
	private Vec3 color, direction, position;
	private Texture shadowMap;
	private Cone volume;
	private Camera lightView;
	private Mat4 perspective;
	private float angle, length, cutOff, intensity, attenuation, camTheta, camPhi;
	private static final int VOLUME_FINENESS = 20, SHADOW_MAP_SIZE = 1024;
	
	public SpotLight(float posX, float posY, float posZ, 
			float dirX, float dirY, float dirZ, 
			float r, float g, float b, 
			float angle, float length, float intensity, float atten){
		position = new Vec3(posX, posY, posZ);
		direction = new Vec3(dirX, dirY, dirZ);
		direction.normalize();
		color = new Vec3(r,g,b);
		this.intensity = intensity;
		attenuation = atten;
		if(angle <= 0){
			float absValue = Math.abs(angle);
			this.angle = absValue > 170 ? 170 : absValue;
		}else{
			this.angle = angle > 170 ? 170 : angle;
		}
		
		cutOff = (float)Math.cos((this.angle/2.0f)*((float)Math.PI/180.0f));
		this.length = length == 0 ? .0001f : length;
		volume = new Cone(this.length*(float)Math.tan((this.angle/2.0f)*(float)Math.PI/180.0f), this.length, VOLUME_FINENESS, 0, false);
		volume.translate(0,-this.length,0);
		perspective = MatrixUtil.getPerspective(this.angle, 1, .01f, this.length);
		
		Vec3 yAxis = new Vec3(0, -1, 0);
		float cosTheta = yAxis.dot(direction);
		if(cosTheta == -1 || cosTheta == 1){
			volume.rotate(1,0,0, cosTheta == -1 ? 180 : 0);
			lightView = new Camera(position,0, cosTheta == -1 ? 90 : -90);
			camTheta = 0;
			camPhi = cosTheta == -1 ? 90 : -90;
		}else{
			yAxis.add(direction).normalize();
			volume.rotate(yAxis, 180);
			float theta = 0, phi = 0;
			
			if(direction.z < 0){
				Vec3 projX = direction.proj(new Vec3(1,0,0));//to get phi
				Vec3 projY = direction.proj(new Vec3(0,1,0));//to get theta
				Vec3 dirShift = (Vec3)VecUtil.subtract(direction, projX);
				float cosPhi = dirShift.dot(new Vec3(0,0,-1));
				phi = (float)Math.acos(cosPhi)*180.0f/(float)Math.PI;
				phi = dirShift.y < 0 ? -phi : phi;
				
				dirShift.set((Vec3)VecUtil.subtract(direction, projY));
				float cosAngle = dirShift.dot(new Vec3(0,0,-1));
				theta = (float)Math.acos(cosAngle)*180.0f/(float)Math.PI;
				theta = dirShift.x < 0 ? theta : -theta;
			}else if(direction.z > 0){
				Vec3 projX = direction.proj(new Vec3(1,0,0));//to get phi
				Vec3 projY = direction.proj(new Vec3(0,1,0));//to get theta
				Vec3 dirShift = (Vec3)VecUtil.subtract(direction, projX);
				float cosPhi = dirShift.dot(new Vec3(0,0,1));
				phi = (float)Math.acos(cosPhi)*180.0f/(float)Math.PI;
				phi = dirShift.y < 0 ? -phi : phi;
				
				dirShift.set((Vec3)VecUtil.subtract(direction, projY));
				float cosAngle = dirShift.dot(new Vec3(0,0,-1));
				theta = (float)Math.acos(cosAngle)*180.0f/(float)Math.PI;
				theta = dirShift.x < 0 ? theta : -theta;
			}else{
				if(direction.x < 0){
					theta = 90;
					float cosPhi = direction.dot(new Vec3(-1,0,0));
					phi = (float)Math.acos(cosPhi)*180.0f/(float)Math.PI;
					phi = direction.y < 0 ? -phi : phi;
				}else if(direction.x > 0){
					theta = -90;
					float cosPhi = direction.dot(new Vec3(1,0,0));
					phi = (float)Math.acos(cosPhi)*180.0f/(float)Math.PI;
					phi = direction.y < 0 ? -phi : phi;
				}
				
			}
			lightView = new Camera(position,theta, phi);
			camTheta = theta;
			camPhi = phi;
		}
		volume.translate(position);
	}
	
	public SpotLight(Vec3 pos, Vec3 direction, Vec3 color, 
			float angle, float length, float intensity, float atten){
		this(pos.x,pos.y,pos.z, direction.x,direction.y,direction.z, color.x,color.y,color.z,  angle,  length,  intensity,  atten);
	}
	
	/*float posX, float posY, float posZ, 
	float dirX, float dirY, float dirZ, 
	float r, float g, float b,*/
	public SpotLight(Vec3 pos, Vec3 direction, float r, float g, float b, 
			float angle, float length, float intensity, float atten){
		this(pos.x,pos.y,pos.z, direction.x,direction.y,direction.z, r, g, b,  angle,  length,  intensity,  atten);
	}
	
	public SpotLight(Vec3 pos, float dirX, float dirY, float dirZ, Vec3 color, 
			float angle, float length, float intensity, float atten){
		this(pos.x,pos.y,pos.z, dirX, dirY, dirZ, color.x,color.y,color.z,  angle,  length,  intensity,  atten);
	}
	
	public SpotLight(Vec3 pos, float dirX, float dirY, float dirZ, 
			float r, float g, float b, 
			float angle, float length, float intensity, float atten){
		this(pos.x,pos.y,pos.z, dirX, dirY, dirZ,  r, g, b,  angle,  length,  intensity,  atten);
	}
	
	public SpotLight(float posX, float posY, float posZ, Vec3 direction, Vec3 color, 
			float angle, float length, float intensity, float atten){
		this(posX,posY,posZ, direction.x,direction.y,direction.z, color.x,color.y,color.z,  angle,  length,  intensity,  atten);
	}
	
	public SpotLight(float posX, float posY, float posZ, Vec3 direction, float r, float g, float b,
			float angle, float length, float intensity, float atten){
		this(posX,posY,posZ, direction.x,direction.y,direction.z,  r, g, b,  angle,  length,  intensity,  atten);
	}
	
	public SpotLight(float posX, float posY, float posZ, 
			float dirX, float dirY, float dirZ, Vec3 color, 
			float angle, float length, float intensity, float atten){
		this(posX,posY,posZ, dirX, dirY, dirZ,  color.x,color.y,color.z,  angle,  length,  intensity,  atten);
	}
	
	public float getCutOff(){
		return cutOff;
	}
	
	public Vec3 getPosition(){
		return position;
	}
	
	public float getAttenuation(){
		return attenuation;
	}
	
	public float getIntensity(){
		return intensity;
	}
	
	public void setIntensity(float intensity) {
		this.intensity = intensity;
	}

	public void setAttenuation(float attenuation) {
		this.attenuation = attenuation;
	}

	public void setAngle(float angle){
		if(angle <= 0){
			float absValue = Math.abs(angle);
			this.angle = absValue > 170 ? 170 : absValue;
		}else{
			this.angle = angle > 170 ? 170 : angle;
		}
		cutOff = (float)Math.cos((this.angle/2.0f)*((float)Math.PI/180.0f));
//		volume.setRadius(this.length*(float)Math.tan((this.angle/2.0f)*(float)Math.PI/180.0f));
		perspective = MatrixUtil.getPerspective(this.angle, 1, .01f, this.length);
	}
	
	public void setLength(float length){
		length = length == 0 ? .0001f : length;
		volume.translate(direction.x*(length-this.length), direction.y*(length-this.length), direction.z*(length-this.length));
		this.length = length;
//		volume.setLength(this.length);
//		volume.setRadius(this.length*(float)Math.tan((this.angle/2.0f)*(float)Math.PI/180.0f));
		perspective = MatrixUtil.getPerspective(this.angle, 1, .01f, this.length);
	}
	
	public float getLength(){
		return length;
	}
	
	public float getAngle(){
		return angle;
	}
	
	public FloatBuffer getPosBuffer(){
		return position.asBuffer();
	}
	
	public void setPosition(float x, float y, float z){
		volume.translate((Vec3)VecUtil.subtract(new Vec3(x, y, z), position));
		position.set(x, y, z);
		lightView.moveTo(position);
	}
	
	public void setPosition(Vec3 loc){
		volume.translate((Vec3)VecUtil.subtract(loc, position));
		position.set(loc);
		lightView.moveTo(position);
	}
	
	public void translate(float x, float y, float z){
		volume.translate(x,y,z);
		position.add(new Vec3(x,y,z));
		lightView.moveTo(position);
	}
	
	public void translate(Vec3 movement){
		volume.translate(movement);
		position.add(movement);
		lightView.moveTo(position);
	}
	
	public void storePos(FloatBuffer buffer){
		position.store(buffer);
	}
	
	public Vec3 getDirection(){
		return direction;
	}
	
	public FloatBuffer getDirBuffer(){
		return direction.asBuffer();
	}
	//----------------fix this--------------------------------
//	public void setDirection(float x, float y, float z){
//		volume.resetModel();
//		volume.translate(0,-this.length,0);
//		
//		Vec3 dir = (new Vec3(x, y, z)).normalize();
//		float cosTheta = dir.dot(direction);
//		if(cosTheta == -1 || cosTheta == 1){
//			/*Vec3 ranAxis = new Vec3((float)Math.random(), (float)Math.random(), (float)Math.random());
//			Vec3 zeroVec = new Vec3(0,0,0);
//			while(ranAxis.equals(dir) || ranAxis.equals(direction) || ranAxis.equals(zeroVec)){
//				ranAxis.set((float)Math.random(), (float)Math.random(), (float)Math.random());
//			}
//			ranAxis.normalize();
//			Vec3 proj = ranAxis.proj(direction);
//			ranAxis.subtract(proj).normalize();
//			System.out.println("x: "+proj.x+" y: "+proj.y+" z: "+proj.z);
//			volume.rotate(ranAxis, cosTheta == -1 ? 180 : 0);*/
//		}else{
//			volume.rotate((Vec3)VecUtil.add(dir, direction).normalize(), 180);
//			float theta = 0, phi = 0;
//			
//			if(dir.z < 0){
//				Vec3 projX = dir.proj(new Vec3(1,0,0));//to get phi
//				Vec3 projY = dir.proj(new Vec3(0,1,0));//to get theta
//				Vec3 dirShift = (Vec3)VecUtil.subtract(dir, projX);
//				float cosPhi = dirShift.dot(new Vec3(0,0,-1));
//				phi = (float)Math.acos(cosPhi)*180.0f/(float)Math.PI;
//				phi = dirShift.y < 0 ? -phi : phi;
//				
//				dirShift.set((Vec3)VecUtil.subtract(dir, projY));
//				float cosAngle = dirShift.dot(new Vec3(0,0,-1));
//				theta = (float)Math.acos(cosAngle)*180.0f/(float)Math.PI;
//				theta = dirShift.x < 0 ? theta : -theta;
//			}else if(dir.z > 0){
//				Vec3 projX = dir.proj(new Vec3(1,0,0));//to get phi
//				Vec3 projY = dir.proj(new Vec3(0,1,0));//to get theta
//				Vec3 dirShift = (Vec3)VecUtil.subtract(dir, projX);
//				float cosPhi = dirShift.dot(new Vec3(0,0,1));
//				phi = (float)Math.acos(cosPhi)*180.0f/(float)Math.PI;
//				phi = dirShift.y < 0 ? -phi : phi;
//				
//				dirShift.set((Vec3)VecUtil.subtract(dir, projY));
//				float cosAngle = dirShift.dot(new Vec3(0,0,-1));
//				theta = (float)Math.acos(cosAngle)*180.0f/(float)Math.PI;
//				theta = dirShift.x < 0 ? theta : -theta;
//			}else{
//				if(dir.x < 0){
//					theta = 90;
//					float cosPhi = dir.dot(new Vec3(-1,0,0));
//					phi = (float)Math.acos(cosPhi)*180.0f/(float)Math.PI;
//					phi = dir.y < 0 ? -phi : phi;
//				}else if(dir.x > 0){
//					theta = -90;
//					float cosPhi = dir.dot(new Vec3(1,0,0));
//					phi = (float)Math.acos(cosPhi)*180.0f/(float)Math.PI;
//					phi = dir.y < 0 ? -phi : phi;
//				}
//			}
//			lightView.lookY(phi-camPhi, false);
//			lightView.rotate(theta-camTheta);
//			camTheta = theta;
//			camPhi = phi;
//		}
//		direction.set(dir);
//		volume.translate(position);
//	}
//	
//	public void setDirection(Vec3 dir){
//		volume.resetModel();
//		volume.translate(0,-this.length,0);
//		
//		Vec3 direct = (new Vec3(dir)).normalize();
//		float cosTheta = dir.dot(direction);
//		if(cosTheta == -1 || cosTheta == 1){
//			//volume.rotate(1,0,0, theta);
//		}else{
//			volume.rotate((Vec3)VecUtil.add(direct, direction).normalize(), 180);
//			float theta = 0, phi = 0;
//			
//			if(direct.z < 0){
//				Vec3 projX = direct.proj(new Vec3(1,0,0));//to get phi
//				Vec3 projY = direct.proj(new Vec3(0,1,0));//to get theta
//				Vec3 directShift = (Vec3)VecUtil.subtract(direct, projX);
//				float cosPhi = directShift.dot(new Vec3(0,0,-1));
//				phi = (float)Math.acos(cosPhi)*180.0f/(float)Math.PI;
//				phi = directShift.y < 0 ? -phi : phi;
//				
//				directShift.set((Vec3)VecUtil.subtract(direct, projY));
//				float cosAngle = directShift.dot(new Vec3(0,0,-1));
//				theta = (float)Math.acos(cosAngle)*180.0f/(float)Math.PI;
//				theta = directShift.x < 0 ? theta : -theta;
//			}else if(direct.z > 0){
//				Vec3 projX = direct.proj(new Vec3(1,0,0));//to get phi
//				Vec3 projY = direct.proj(new Vec3(0,1,0));//to get theta
//				Vec3 directShift = (Vec3)VecUtil.subtract(direct, projX);
//				float cosPhi = directShift.dot(new Vec3(0,0,1));
//				phi = (float)Math.acos(cosPhi)*180.0f/(float)Math.PI;
//				phi = directShift.y < 0 ? -phi : phi;
//				
//				directShift.set((Vec3)VecUtil.subtract(direct, projY));
//				float cosAngle = directShift.dot(new Vec3(0,0,-1));
//				theta = (float)Math.acos(cosAngle)*180.0f/(float)Math.PI;
//				theta = directShift.x < 0 ? theta : -theta;
//			}else{
//				if(direct.x < 0){
//					theta = 90;
//					float cosPhi = direct.dot(new Vec3(-1,0,0));
//					phi = (float)Math.acos(cosPhi)*180.0f/(float)Math.PI;
//					phi = direct.y < 0 ? -phi : phi;
//				}else if(direct.x > 0){
//					theta = -90;
//					float cosPhi = direct.dot(new Vec3(1,0,0));
//					phi = (float)Math.acos(cosPhi)*180.0f/(float)Math.PI;
//					phi = direct.y < 0 ? -phi : phi;
//				}
//			}
//			lightView.lookY(phi-camPhi, false);
//			lightView.rotate(theta-camTheta);
//			camTheta = theta;
//			camPhi = phi;
//		}
//		direction.set(direct);
//		volume.translate(position);
//	}
	
	public void storeDir(FloatBuffer buffer){
		direction.store(buffer);
	}
	
	public FloatBuffer getModelBuffer(){
		return volume.getModelMatrix().asBuffer();
	}
	
	public Camera getView(){
		return lightView;
	}
	
	public FloatBuffer getViewBuffer(){
		return lightView.getLookAt().asBuffer();
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
	public FloatBuffer getColorBuffer() {
		return color.asBuffer();
	}

	@Override
	public Vec3 getColor() {
		return color;
	}

	@Override
	public void setColor(Vec3 color) {
		this.color.set(color);
	}

	@Override
	public void storeColor(FloatBuffer buffer) {
		color.store(buffer);
	}

	@Override
	public void genShadowMap(){
		shadowMap = Texture.genEmpty(Texture._2D, 
				SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, GL_DEPTH_COMPONENT);
		
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
		volume.render();
	}

	@Override
	public void cleanUp() {
		if(shadowMap != null){
			shadowMap.delete();
		}
		volume.delete();
	}

}
