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
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

public class GBuffers {
	private IntBuffer drawBuffers;
	public final int 
			NORMAL_BUFFER = 0,
			FINAL = 1,
			NUM_TEXTURES = 2;
	
	private int frameBuffer, renderBuffer, textures[], width, height;
	
	public GBuffers(int width, int height){
		this.width = width;
		this.height = height;
		textures = new int[NUM_TEXTURES];
		//generate frame buffer and render buffer
		frameBuffer = glGenFramebuffers();
		renderBuffer = glGenRenderbuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
		//generate the textures for rendering into
		textures[NORMAL_BUFFER] = glGenTextures();
		textures[FINAL] = glGenTextures();
		//loop through the textures and initialize the data for each
		for(int setData = 0; setData < NUM_TEXTURES; setData++){
			glBindTexture(GL_TEXTURE_RECTANGLE, textures[setData]);

			glTexImage2D(GL_TEXTURE_RECTANGLE, 0,
								GL_RGB, width, height, 0, GL_RGB,
								GL_UNSIGNED_BYTE, (ByteBuffer)null);

			glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

			glBindTexture(GL_TEXTURE_RECTANGLE, 0);
			glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0+setData, textures[setData], 0);
		}
		//initialize the render buffer for logical operations such as depth and stencil buffers
		glBindRenderbuffer(GL_RENDERBUFFER, renderBuffer);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_STENCIL, width, height);
		glBindRenderbuffer(GL_RENDERBUFFER, 0);
		//attach the render buffer to the appropriate attachment point
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, renderBuffer);

		//check for frame buffer completeness printing error messages in such a case as incompleteness
		if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE){
			System.err.println("Frame buffer incomplete");
			switch(glCheckFramebufferStatus(GL_FRAMEBUFFER)){
			
				case GL_FRAMEBUFFER_UNDEFINED :
					System.err.println("GL_FRAMEBUFFER_UNDEFINED");
					
				case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
					
				case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
					
				case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
					
				case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
					
				case GL_FRAMEBUFFER_UNSUPPORTED  :
					System.err.println("GL_FRAMEBUFFER_UNSUPPORTED");
					
				case GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE");
					
				case GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS   :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS");
			}
		}
		glBindFramebuffer(GL_FRAMEBUFFER,  0);
		drawBuffers = BufferUtils.createIntBuffer(3);
		drawBuffers.put(new int[]{GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1});
		drawBuffers.flip();
	}

	public void geomPass(){
		//bind the frame buffer
		glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
		//change the drawn buffers to the ones for storing data
		glDrawBuffers(drawBuffers);
		//allow depth buffer writing
		glDepthMask(true);
		//clear everything
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glEnable(GL_DEPTH_TEST);
	}
	
	public void shadowVolPass(){
		//disable writing into depth buffer, and make read only
		glDepthMask(false);
		//don't render into any color buffers
		glDrawBuffer(GL_NONE);
		//still compare depth values for lights
		glEnable(GL_DEPTH_TEST);
		glDisable(GL_CULL_FACE);
		//clear the stencil buffer
		glClear(GL_STENCIL_BUFFER_BIT);
		//set the stencil operation to always pass and to set the reference value to 0, with a mask of 0
		glStencilFunc(GL_ALWAYS, 0, 0);
		//params (face, stencilFail, depthFail, depthSuccess)
		//if the back face fails the depth test increment the value in the stencil buffer
		glStencilOpSeparate(GL_BACK, GL_KEEP, GL_INCR_WRAP, GL_KEEP);
		//if the front face fails the depth test decrement the value in the stencil buffer
		glStencilOpSeparate(GL_FRONT, GL_KEEP, GL_DECR_WRAP, GL_KEEP);
	}
	
	public void bindForAmbient(){
		glDrawBuffer(GL_COLOR_ATTACHMENT1);
		glStencilFunc(GL_EQUAL, 1, 0xFF);
		glEnable(GL_BLEND);
	}

	public void stencilPass(){
		//if the stencil buffer has a value less than 1 we pass the stencil test
		glStencilFunc(GL_LESS, 1, 0xFF);
		//params (face, stencilFail, depthFail, depthSuccess)
		//if the back face fails the stencil test increment the stencil buffer
		glStencilOpSeparate(GL_BACK, GL_INCR_WRAP, GL_INCR_WRAP, GL_KEEP);
		//if the front face fails the stencil test increment the stencil buffer
		glStencilOpSeparate(GL_FRONT, GL_INCR_WRAP, GL_DECR_WRAP, GL_KEEP);
		/*
		 * the reason is that after the shadow volume is rendered to the stencil buffer
		 * the buffer has a value of 1 if the pixel is in the shadow, thus when we do the lighting pass
		 * we want to exclude any portion of the light volume that is part of the shadow volume
		 * so if the stencil value is less than 1, which means it is 0, we perform the standard 
		 * depth fail increment/decrement for the light volume, how ever if the face fails the stencil
		 * test then the stencil value is incremented which means that after performing front face
		 * calculations the stencil will be 2, but since it failed the stencil test it never reaches the 
		 * depth test, which means that once we get to the back face it too will fail the stencil test
		 * and never go through depth testing to modify the stencil buffer. So after everything has happened
		 * the pixels in the light volume will have a value of 1 and anything part of the volume in the 
		 * shadow will be 3, we can then pass the stencil test when rendering the light volume only when the 
		 * buffer has a value of 1
		 *  */
	}
	
	public void bindForLight(){
		//set the final texture as the draw target
		glDrawBuffer(GL_COLOR_ATTACHMENT1);
		//bind the normal texture for reading
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_RECTANGLE, textures[NORMAL_BUFFER]);
	}

	public void lightPass(){
		//make the stencil operator check if the stencil value is equal to the ref = 0
		//bitmask tells it a value to AND the ref and buffer values with before comparison
		glStencilFunc(GL_EQUAL, 1, 0xFF);
		//disable depth testing as it is unnecessary since the stencil buffer will remove pixels
		glDisable(GL_DEPTH_TEST);
		//enable blending of the values for the lights
		glEnable(GL_BLEND);
		//blend the values by just adding them, this accumulates the lighting
		glBlendEquation(GL_FUNC_ADD);
		//scale factors to be applied to the added values
		glBlendFunc(GL_ONE, GL_ONE);
		//enable face culling
		glEnable(GL_CULL_FACE);
		//set it back to front face culling
		glCullFace(GL_FRONT);
	}

	public void finalPass(){
		glBindBuffer(GL_DRAW_BUFFER, 0);
		glBindBuffer(GL_READ_BUFFER, frameBuffer);
		glReadBuffer(GL_COLOR_ATTACHMENT1);
		glBlitFramebuffer(0, 0, width, height, 0, 0, width, height,  GL_COLOR_BUFFER_BIT,  GL_NEAREST);
	}
	
	public void cleanUpGPU(){
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glDeleteRenderbuffers(renderBuffer);
		glBindTexture(GL_TEXTURE_RECTANGLE, 0);
		IntBuffer deleteTextures = BufferUtils.createIntBuffer(NUM_TEXTURES);
		deleteTextures.put(textures);
		deleteTextures.flip();
		glDeleteTextures(deleteTextures);
		glDeleteFramebuffers(frameBuffer);
	}
}
