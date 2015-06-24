package core;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.GL_COMPARE_REF_TO_TEXTURE;

import java.nio.FloatBuffer;

import glMath.*;
import renderers.Texture;

public class DirectionalLight implements Light {
	
	private Vec3 direction, color;
	private int renderW, renderH;
	private Texture shadowMap;
	private Camera lightView;
	private Mat4 perspective;
	
	public DirectionalLight(float dirX, float dirY, float dirZ, float r, float g, float b, int renderW, int renderH){
		direction = new Vec3(dirX, dirY, dirZ);
		color = new Vec3(r, g, b);
		this.renderH = renderH;
		this.renderW = renderW;
		shadowMap = null;
		lightView = null;
	}
	
	public DirectionalLight(Vec3 dir, Vec3 color, int renderW, int renderH){
		direction = new Vec3(dir);
		color = new Vec3(color);
		this.renderH = renderH;
		this.renderW = renderW;
		shadowMap = null;
		lightView = null;
	}
	
	public DirectionalLight(float dirX, float dirY, float dirZ, Vec3 color, int renderW, int renderH){
		direction = new Vec3(dirX, dirY, dirZ);
		color = new Vec3(color);
		this.renderH = renderH;
		this.renderW = renderW;
		shadowMap = null;
		lightView = null;
	}
	
	public DirectionalLight(Vec3 dir, float r, float g, float b, int renderW, int renderH){
		direction = new Vec3(dir);
		color = new Vec3(r, g, b);
		this.renderH = renderH;
		this.renderW = renderW;
		shadowMap = null;
		lightView = null;
	}
	
	public void setDir(float x, float y, float z) {
		direction.set(x, y, z);
	}
	
	public void setDir(Vec3 dir) {
		direction.set(dir);
	}
	
	public void storeDirction(FloatBuffer buffer){
		direction.store(buffer);
	}
	
	public FloatBuffer getDirAsBuffer(){
		return direction.asBuffer();
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
	public void storeColor(FloatBuffer buffer) {
		color.store(buffer);
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
	public void genShadowMap(){
		
		shadowMap = Texture.genEmpty(Texture._2D, renderW, renderH, GL_DEPTH_COMPONENT);
		
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
		
	}

	@Override
	public void cleanUp() {
		if(shadowMap != null){
			shadowMap.delete();
		}
	}
}
