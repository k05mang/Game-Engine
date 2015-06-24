package core;
import glMath.Mat4;
import glMath.Vec3;
import renderers.Texture;

import java.nio.FloatBuffer;


public interface Light {
	
	public void setColor(float r, float g, float b);
	
	public void setColor(Vec3 color);
	
	public FloatBuffer getColorBuffer();
	
	public void storeColor(FloatBuffer buffer);
	
	public Vec3 getColor();
	
	public void genShadowMap();
	
	public Texture getShadowMap();
	
	public void bindShadow(int texUnit);
	
	public void render();
	
	public void cleanUp();
	
	public Mat4 getPerspective();
	
	public FloatBuffer getPerspectiveBuffer();
}
