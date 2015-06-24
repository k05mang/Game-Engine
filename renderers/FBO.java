package renderers;
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

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

public class FBO {

	private int fboId, width, height, renderBuffer;
	
	/**
	 * Default constructor for a framebuffer object
	 * This constructor generates the FBO id, sets the framebuffer dimension
	 * to 0, and drawBuffers to GL_NONE
	 */
	public FBO(){
		fboId = glGenFramebuffers();
		width = 0;
		height = 0;
		renderBuffer = 0;
		glBindFramebuffer(GL_FRAMEBUFFER, fboId);
		glDrawBuffers(GL_NONE);
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	/**
	 * Constructs framebuffer object using the given textures for the corresponding 
	 * attachment points. The colorBuffers array is iterated over and each texture is 
	 * attached to color attachment points starting from GL_COLOR_ATTACHMENT0 up to 
	 * GL_COLOR_ATTACHMENT0+colorBuffers.length. 
	 * 
	 * @param colorBuffers Color renderable textures to be used for the framebuffers color attachments
	 * @param depthBuffer
	 * @param stencilBuffer
	 * @param drawBuffers
	 */
	protected FBO(Texture[] colorBuffers, Texture depthBuffer, Texture stencilBuffer, IntBuffer drawBuffers){
		fboId = glGenFramebuffers();
		width = depthBuffer.getWidth();
		height = depthBuffer.getHeight();
		renderBuffer = 0;
		glBindFramebuffer(GL_FRAMEBUFFER, fboId);
		
		for (int attach = 0; attach < colorBuffers.length; attach++) {
			glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0+attach,
					colorBuffers[attach].getId(), 0);
		}
		glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, depthBuffer.getId(), 0);
		glFramebufferTexture(GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT, stencilBuffer.getId(), 0);
		
		glDrawBuffers(drawBuffers);
		
		if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE){
			System.err.println("Frame buffer incomplete");
			switch(glCheckFramebufferStatus(GL_FRAMEBUFFER)){
			
				case GL_FRAMEBUFFER_UNDEFINED :
					System.err.println("GL_FRAMEBUFFER_UNDEFINED");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
					break;
				case GL_FRAMEBUFFER_UNSUPPORTED  :
					System.err.println("GL_FRAMEBUFFER_UNSUPPORTED");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS   :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS");
					break;
			}
		}
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	protected FBO(Texture[] colorBuffers, Texture depthStencil, IntBuffer drawBuffers){
		fboId = glGenFramebuffers();

		if(depthStencil != null){
			width = depthStencil.getWidth();
			height = depthStencil.getHeight();
		}else{
			width = colorBuffers[0].getWidth();
			height = colorBuffers[0].getHeight();
		}
		
		if(depthStencil != null){
			renderBuffer = 0;
		}else{
			renderBuffer = glGenRenderbuffers();
		}
		
		glBindFramebuffer(GL_FRAMEBUFFER, fboId);
		
		for (int attach = 0; attach < colorBuffers.length; attach++) {
			glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0+attach,
					colorBuffers[attach].getId(), 0);
		}
		
		if(depthStencil != null){
			glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, depthStencil.getId(), 0);
		}else{
			glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, renderBuffer);
		}
		
		glDrawBuffers(drawBuffers);
		
		if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE){
			System.err.println("Frame buffer incomplete");
			switch(glCheckFramebufferStatus(GL_FRAMEBUFFER)){
			
				case GL_FRAMEBUFFER_UNDEFINED :
					System.err.println("GL_FRAMEBUFFER_UNDEFINED");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
					break;
				case GL_FRAMEBUFFER_UNSUPPORTED  :
					System.err.println("GL_FRAMEBUFFER_UNSUPPORTED");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS   :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS");
					break;
			}
		}
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	protected FBO(Texture colorBuffer, Texture depthBuffer, Texture stencilBuffer, boolean bindArrayAttachments, IntBuffer drawBuffers){
		fboId = glGenFramebuffers();
		width = colorBuffer.getWidth();
		height = colorBuffer.getHeight();
		renderBuffer = 0;
		glBindFramebuffer(GL_FRAMEBUFFER, fboId);
		
		if(!bindArrayAttachments){
			glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, colorBuffer.getId(), 0);
		}else{
			for(int layer = 0; layer < colorBuffer.getDepth(); layer++){
				glFramebufferTextureLayer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0+layer, colorBuffer.getId(), 0, layer);
			}
		}
		glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, depthBuffer.getId(), 0);
		glFramebufferTexture(GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT, stencilBuffer.getId(), 0);
		
		glDrawBuffers(drawBuffers);
		
		if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE){
			System.err.println("Frame buffer incomplete");
			switch(glCheckFramebufferStatus(GL_FRAMEBUFFER)){
			
				case GL_FRAMEBUFFER_UNDEFINED :
					System.err.println("GL_FRAMEBUFFER_UNDEFINED");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
					break;
				case GL_FRAMEBUFFER_UNSUPPORTED  :
					System.err.println("GL_FRAMEBUFFER_UNSUPPORTED");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS   :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS");
					break;
			}
		}
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	protected FBO(Texture colorBuffer, Texture depthStencil, boolean bindArrayAttachments, IntBuffer drawBuffers){
		fboId = glGenFramebuffers();
		width = colorBuffer.getWidth();
		height = colorBuffer.getHeight();
		if(depthStencil != null){
			renderBuffer = 0;
		}else{
			renderBuffer = glGenRenderbuffers();
		}
		
		glBindFramebuffer(GL_FRAMEBUFFER, fboId);
		
		if(!bindArrayAttachments){
			glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, colorBuffer.getId(), 0);
		}else{
			for(int layer = 0; layer < colorBuffer.getDepth(); layer++){
				glFramebufferTextureLayer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0+layer, colorBuffer.getId(), 0, layer);
			}
		}
		
		if(depthStencil != null){
			glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, depthStencil.getId(), 0);
		}else{
			glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, renderBuffer);
		}
		
		glDrawBuffers(drawBuffers);
		
		if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE){
			System.err.println("Frame buffer incomplete");
			switch(glCheckFramebufferStatus(GL_FRAMEBUFFER)){
			
				case GL_FRAMEBUFFER_UNDEFINED :
					System.err.println("GL_FRAMEBUFFER_UNDEFINED");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
					break;
				case GL_FRAMEBUFFER_UNSUPPORTED  :
					System.err.println("GL_FRAMEBUFFER_UNSUPPORTED");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS   :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS");
					break;
			}
		}
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	protected FBO(Texture depthBuffer, Texture stencilBuffer, boolean genRenderBuffer){
		fboId = glGenFramebuffers();
		if(depthBuffer != null){
			width = depthBuffer.getWidth();
			height = depthBuffer.getHeight();
			if(stencilBuffer != null){
				renderBuffer = 0;
			}else if(genRenderBuffer){
				renderBuffer = glGenRenderbuffers();
			}
		}else{
			width = stencilBuffer.getWidth();
			height = stencilBuffer.getHeight();
			if(genRenderBuffer){
				renderBuffer = glGenRenderbuffers();
			}
		}
		
		glBindFramebuffer(GL_FRAMEBUFFER, fboId);
		if(depthBuffer != null){
			glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, depthBuffer.getId(), 0);
		}else if(genRenderBuffer){
			glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, renderBuffer);
		}
		
		if(stencilBuffer != null){
			glFramebufferTexture(GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT, stencilBuffer.getId(), 0);
		}else if(genRenderBuffer){
			glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT, GL_RENDERBUFFER, renderBuffer);
		}
		
		glDrawBuffers(GL_NONE);
		
		if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE){
			System.err.println("Frame buffer incomplete");
			switch(glCheckFramebufferStatus(GL_FRAMEBUFFER)){
			
				case GL_FRAMEBUFFER_UNDEFINED :
					System.err.println("GL_FRAMEBUFFER_UNDEFINED");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
					break;
				case GL_FRAMEBUFFER_UNSUPPORTED  :
					System.err.println("GL_FRAMEBUFFER_UNSUPPORTED");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS   :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS");
					break;
			}
		}
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	protected FBO(Texture depthStencil){
		fboId = glGenFramebuffers();
		width = depthStencil.getWidth();
		height = depthStencil.getHeight();
		renderBuffer = 0;
		
		glBindFramebuffer(GL_FRAMEBUFFER, fboId);
		
		glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, depthStencil.getId(), 0);
		
		glDrawBuffers(GL_NONE);
		
		if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE){
			System.err.println("Frame buffer incomplete");
			switch(glCheckFramebufferStatus(GL_FRAMEBUFFER)){
			
				case GL_FRAMEBUFFER_UNDEFINED :
					System.err.println("GL_FRAMEBUFFER_UNDEFINED");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
					break;
				case GL_FRAMEBUFFER_UNSUPPORTED  :
					System.err.println("GL_FRAMEBUFFER_UNSUPPORTED");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE");
					break;
				case GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS   :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS");
					break;
			}
		}
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	/**
	 * Creates a FBO object with the given arguments if they are compatible together with a framebuffer object
	 * 
	 * @param colorBuffers An array of color renderable textures to be used with the framebuffers color
	 * attachment points. At least 1 value must be in the array otherwise the method will fail to make
	 * an FBO and return null.
	 * 
	 * @param depthBuffer The texture used for the depth attachment of the fbo
	 * @param stencilBuffer The texture used for the stencil attachment of the fbo
	 * @param drawBuffers Constants that represent the draw buffers for this frame buffer
	 * @return An FBO object with the given textures bound to the respective attachment points and whose
	 * draw buffers are set to the values given
	 */
	public static FBO create(Texture[] colorBuffers, Texture depthBuffer, Texture stencilBuffer, int... drawBuffers){
		if(colorBuffers == null || depthBuffer == null ||  stencilBuffer == null){
			System.err.println("Textures provided contain a null object, failed to create FBO");
			return null;
		}else if(colorBuffers.length == 0){
			System.err.println("Color buffer must have at least 1 texture object for this function");
			return null;
		}
		
		int width = colorBuffers[0].getWidth(), height = colorBuffers[0].getHeight(), depth = colorBuffers[0].getDepth();
		for(Texture colorBuffer : colorBuffers){
			if(colorBuffer.getWidth() != width || colorBuffer.getHeight() != height || colorBuffer.getDepth() != depth){
				System.err.println("Textures must all have the same dimensions, failed to create FBO");
				return null;
			}
		}
		
		if(depthBuffer.getWidth() != width || depthBuffer.getHeight() != height || depthBuffer.getDepth() != depth ||
				stencilBuffer.getWidth() != width || stencilBuffer.getHeight() != height || stencilBuffer.getDepth() != depth)
		{
			System.err.println("Textures must all have the same dimensions, failed to create FBO");
			return null;
		}
		
		int maxDrawBuffers = glGetInteger(GL_MAX_DRAW_BUFFERS);
		int maxAttachments = glGetInteger(GL_MAX_COLOR_ATTACHMENTS);
		
		if(drawBuffers.length > maxDrawBuffers)
		{
			IntBuffer drawBuffer = (IntBuffer)BufferUtils.createIntBuffer(maxDrawBuffers);
			for(int curBuffer = 0; curBuffer < maxDrawBuffers; curBuffer++){
				drawBuffer.put(drawBuffers[curBuffer]);
			}
			drawBuffer.flip();
			return new FBO(colorBuffers, depthBuffer, stencilBuffer, drawBuffer);
		}
		else if(colorBuffers.length > maxAttachments)
		{
			Texture[] colorAttachments = new Texture[maxAttachments];
			for(int curAttach = 0; curAttach < maxAttachments; curAttach++){
				colorAttachments[curAttach] = colorBuffers[curAttach];
			}
			IntBuffer drawBuffer;
			if(drawBuffers.length > 0){
				drawBuffer = (IntBuffer) BufferUtils
						.createIntBuffer(drawBuffers.length).put(drawBuffers).flip();
			}else{
				drawBuffer = (IntBuffer) BufferUtils
						.createIntBuffer(1).put(GL_COLOR_ATTACHMENT0).flip();
			}
			return new FBO(colorAttachments, depthBuffer, stencilBuffer, drawBuffer);
		}
		else
		{
			IntBuffer drawBuffer;
			if(drawBuffers.length > 0){
				drawBuffer = (IntBuffer) BufferUtils
						.createIntBuffer(drawBuffers.length).put(drawBuffers).flip();
			}else{
				drawBuffer = (IntBuffer) BufferUtils
						.createIntBuffer(1).put(GL_COLOR_ATTACHMENT0).flip();
			}
			return new FBO(colorBuffers, depthBuffer, stencilBuffer, drawBuffer);
		}
		//possibly check for proper renderable types
		//int maxRenderBuffer = glGetInteger(GL_MAX_RENDERBUFFER_SIZE);
	}
	
	public static FBO create(Texture[] colorBuffers, Texture depthStencil, int... drawBuffers){
		if(colorBuffers == null){
			System.err.println("ColorBuffers is null, failed to create FBO");
			return null;
		}else if(colorBuffers.length == 0){
			System.err.println("Color buffer must have at least 1 texture object for this function");
			return null;
		}
		
		int width = colorBuffers[0].getWidth(), height = colorBuffers[0].getHeight();
		for(Texture colorBuffer : colorBuffers){
			if(colorBuffer.getWidth() != width || colorBuffer.getHeight() != height){
				System.err.println("Color buffer textures must all have the same dimensions, failed to create FBO");
				return null;
			}
		}
		
		if(depthStencil != null){
			if (depthStencil.getWidth() != width
					|| depthStencil.getHeight() != height) {
				System.err
						.println("Textures must all have the same dimensions, failed to create FBO");
				return null;
			}
		}
		
		int maxDrawBuffers = glGetInteger(GL_MAX_DRAW_BUFFERS);
		int maxAttachments = glGetInteger(GL_MAX_COLOR_ATTACHMENTS);
		
		if(drawBuffers.length > maxDrawBuffers)
		{
			IntBuffer drawBuffer = (IntBuffer)BufferUtils.createIntBuffer(maxDrawBuffers);
			for(int curBuffer = 0; curBuffer < maxDrawBuffers; curBuffer++){
				drawBuffer.put(drawBuffers[curBuffer]);
			}
			drawBuffer.flip();
			return new FBO(colorBuffers, depthStencil, drawBuffer);
		}
		else if(colorBuffers.length > maxAttachments)
		{
			Texture[] colorAttachments = new Texture[maxAttachments];
			for(int curAttach = 0; curAttach < maxAttachments; curAttach++){
				colorAttachments[curAttach] = colorBuffers[curAttach];
			}
			IntBuffer drawBuffer;
			if(drawBuffers.length > 0){
				drawBuffer = (IntBuffer) BufferUtils
						.createIntBuffer(drawBuffers.length).put(drawBuffers).flip();
			}else{
				drawBuffer = (IntBuffer) BufferUtils
						.createIntBuffer(1).put(GL_COLOR_ATTACHMENT0).flip();
			}
			return new FBO(colorAttachments, depthStencil, drawBuffer);
		}
		else
		{
			IntBuffer drawBuffer;
			if(drawBuffers.length > 0){
				drawBuffer = (IntBuffer) BufferUtils
						.createIntBuffer(drawBuffers.length).put(drawBuffers).flip();
			}else{
				drawBuffer = (IntBuffer) BufferUtils
						.createIntBuffer(1).put(GL_COLOR_ATTACHMENT0).flip();
			}
			return new FBO(colorBuffers, depthStencil, drawBuffer);
		}
	}
	
	public static FBO create(Texture[] colorBuffers, int... drawBuffers){
		return create(colorBuffers, drawBuffers);
	}
	
	public static FBO create(Texture colorBuffer, Texture depthBuffer, Texture stencilBuffer, boolean bindArrayAttachments, int... drawBuffers){
		if(colorBuffer == null || depthBuffer == null ||  stencilBuffer == null){
			System.err.println("Textures provided contain a null object, creating an empty FBO");
			return new FBO();
		}
		
		if(depthBuffer.getWidth() != colorBuffer.getWidth() || depthBuffer.getHeight() != colorBuffer.getHeight() ||
			stencilBuffer.getWidth() != colorBuffer.getWidth() || stencilBuffer.getHeight() != colorBuffer.getHeight()||
			stencilBuffer.getWidth() != colorBuffer.getWidth() || stencilBuffer.getDepth() != colorBuffer.getDepth())
		{
			System.err.println("Textures must all have the same dimensions, failed to create FBO");
			return null;
		}
		
		int maxDrawBuffers = glGetInteger(GL_MAX_DRAW_BUFFERS);
		
		if(drawBuffers.length > maxDrawBuffers)
		{
			IntBuffer drawBuffer = (IntBuffer)BufferUtils.createIntBuffer(maxDrawBuffers);
			for(int curBuffer = 0; curBuffer < maxDrawBuffers; curBuffer++){
				drawBuffer.put(drawBuffers[curBuffer]);
			}
			drawBuffer.flip();
			
			return new FBO(colorBuffer, depthBuffer, stencilBuffer, (colorBuffer.isArray() ? bindArrayAttachments : false), drawBuffer);
		}
		else
		{
			IntBuffer drawBuffer = (IntBuffer)BufferUtils.createIntBuffer(drawBuffers.length).put(drawBuffers).flip();
			return new FBO(colorBuffer, depthBuffer, stencilBuffer, (colorBuffer.isArray() ? bindArrayAttachments : false),drawBuffer);
		}
	}
	
	public static FBO create(Texture colorBuffer, Texture depthStencil, boolean bindArrayAttachments, int... drawBuffers){
		if(colorBuffer == null && depthStencil != null){
			return new FBO(depthStencil);
		}else if(colorBuffer == null && depthStencil == null){
			System.err.println("Both textures cannot be null, creating an empty FBO");
			return new FBO();
		}
		
		if(depthStencil != null){
			if (colorBuffer.getWidth() != depthStencil.getWidth() || 
					colorBuffer.getHeight() != depthStencil.getHeight() ||
							colorBuffer.getDepth() != depthStencil.getDepth()) {
				System.err.println("Textures must all have the same dimensions, failed to create FBO");
				return null;
			}
		}
		
		int maxDrawBuffers = glGetInteger(GL_MAX_DRAW_BUFFERS);
		
		if(drawBuffers.length > maxDrawBuffers)
		{
			IntBuffer drawBuffer = (IntBuffer)BufferUtils.createIntBuffer(maxDrawBuffers);
			for(int curBuffer = 0; curBuffer < maxDrawBuffers; curBuffer++){
				drawBuffer.put(drawBuffers[curBuffer]);
			}
			drawBuffer.flip();
			
			return new FBO(colorBuffer, depthStencil, (colorBuffer.isArray() ? bindArrayAttachments : false), drawBuffer);
		}
		else
		{
			IntBuffer drawBuffer;
			if(drawBuffers.length > 0){
				drawBuffer = (IntBuffer) BufferUtils
						.createIntBuffer(drawBuffers.length).put(drawBuffers).flip();
			}else{
				drawBuffer = (IntBuffer) BufferUtils
						.createIntBuffer(1).put(GL_COLOR_ATTACHMENT0).flip();
			}
			return new FBO(colorBuffer, depthStencil, (colorBuffer.isArray() ? bindArrayAttachments : false),drawBuffer);
		}
	}
	
	public static FBO create(Texture colorBuffer, boolean bindArrayAttachments, int... drawBuffers){
		return create(colorBuffer, null, bindArrayAttachments, drawBuffers);
	}
	
	public static FBO create(Texture depthBuffer, Texture stencilBuffer, boolean genRenderBuffer){
		if(depthBuffer == null && stencilBuffer == null){
			System.err.println("Both textures cannot be null, creating an empty FBO");
			return new FBO();
		}
		
		if(depthBuffer != null && stencilBuffer != null){
			if(depthBuffer.getWidth() != stencilBuffer.getWidth() || 
					depthBuffer.getHeight() != stencilBuffer.getHeight() ||
					depthBuffer.getDepth() != stencilBuffer.getDepth()){
				System.err.println("Textures must all have the same dimensions, failed to create FBO");
				return null;
			}
		}
		
		return new FBO(depthBuffer, stencilBuffer, genRenderBuffer);
	}
	
	public static FBO create(Texture depthStencil){
		if(depthStencil == null){
			System.err.println("Texture cannot be null, creating an empty FBO");
			return new FBO();
		}
		
		return new FBO(depthStencil);
	}
	
	public void setDrawBuffers(int... drawBuffers){
		int maxDrawBuffers = glGetInteger(GL_MAX_DRAW_BUFFERS);
		glBindFramebuffer(GL_FRAMEBUFFER, fboId);
		if(drawBuffers.length > maxDrawBuffers)
		{
			IntBuffer drawBuffer = (IntBuffer)BufferUtils.createIntBuffer(maxDrawBuffers);
			for(int curBuffer = 0; curBuffer < maxDrawBuffers; curBuffer++){
				drawBuffer.put(drawBuffers[curBuffer]);
			}
			drawBuffer.flip();
			glDrawBuffers(drawBuffer);
		}else{
			glDrawBuffers((IntBuffer)BufferUtils.createIntBuffer(drawBuffers.length).put(drawBuffers).flip());
		}
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	public boolean setColor(Texture color, boolean bindArrayAttachments, int attachPoint){
		if(width != 0 && height != 0){
			if (color.getWidth() != width || color.getHeight() != height) {
				System.err
						.println("Failed to attach color buffer, dimensions do not match for this frame buffer object");
				return false;
			}
		}
		glBindFramebuffer(GL_FRAMEBUFFER, fboId);
		if(!bindArrayAttachments){
			glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0+attachPoint, color.getId(), 0);
		}else{
			for(int layer = 0; layer < color.getDepth(); layer++){
				glFramebufferTextureLayer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0+attachPoint+layer, color.getId(), 0, layer);
			}
		}
		
		if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE){
			System.err.println("Frame buffer incomplete");
			switch(glCheckFramebufferStatus(GL_FRAMEBUFFER)){
			
				case GL_FRAMEBUFFER_UNDEFINED :
					System.err.println("GL_FRAMEBUFFER_UNDEFINED");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
					return false;
				case GL_FRAMEBUFFER_UNSUPPORTED  :
					System.err.println("GL_FRAMEBUFFER_UNSUPPORTED");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS   :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS");
					return false;	
			}
		}
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		return true;
	}
	
	public boolean setColors(Texture... color){
		glBindFramebuffer(GL_FRAMEBUFFER, fboId);

		for (int attach = 0; attach < color.length; attach++) {
			glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0+attach, color[attach].getId(), 0);
		}
		
		if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE){
			System.err.println("Frame buffer incomplete");
			switch(glCheckFramebufferStatus(GL_FRAMEBUFFER)){
			
				case GL_FRAMEBUFFER_UNDEFINED :
					System.err.println("GL_FRAMEBUFFER_UNDEFINED");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
					return false;
				case GL_FRAMEBUFFER_UNSUPPORTED  :
					System.err.println("GL_FRAMEBUFFER_UNSUPPORTED");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS   :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS");
					return false;	
			}
		}
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		return true;
	}
	
	public boolean setDepth(Texture depth){
		if(width != 0 && height != 0){
			if (depth.getWidth() != width || depth.getHeight() != height) {
				System.err.println("Failed to attach depth buffer, dimensions do not match for this frame buffer object");
				return false;
			}
		}else{
			width = depth.getWidth();
			height = depth.getHeight();
		}
		glBindFramebuffer(GL_FRAMEBUFFER, fboId);

		glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, depth.getId(), 0);
		
		if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE){
			System.err.println("Frame buffer incomplete");
			switch(glCheckFramebufferStatus(GL_FRAMEBUFFER)){
			
				case GL_FRAMEBUFFER_UNDEFINED :
					System.err.println("GL_FRAMEBUFFER_UNDEFINED");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
					return false;
				case GL_FRAMEBUFFER_UNSUPPORTED  :
					System.err.println("GL_FRAMEBUFFER_UNSUPPORTED");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS   :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS");
					return false;	
			}
		}
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		return true;
	}
	
	public boolean setStencil(Texture stencil){
		if(width != 0 && height != 0){
			if (stencil.getWidth() != width || stencil.getHeight() != height) {
				System.err
						.println("Failed to attach color buffer, dimensions do not match for this frame buffer object");
				return false;
			}
		}
		glBindFramebuffer(GL_FRAMEBUFFER, fboId);

		glFramebufferTexture(GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT, stencil.getId(), 0);
		
		if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE){
			System.err.println("Frame buffer incomplete");
			switch(glCheckFramebufferStatus(GL_FRAMEBUFFER)){
			
				case GL_FRAMEBUFFER_UNDEFINED :
					System.err.println("GL_FRAMEBUFFER_UNDEFINED");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
					return false;
				case GL_FRAMEBUFFER_UNSUPPORTED  :
					System.err.println("GL_FRAMEBUFFER_UNSUPPORTED");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS   :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS");
					return false;	
			}
		}
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		return true;
	}
	
	public boolean setDepthStencil(Texture depthStencil){
		if(width != 0 && height != 0){
			if (depthStencil.getWidth() != width || depthStencil.getHeight() != height) {
				System.err
						.println("Failed to attach color buffer, dimensions do not match for this frame buffer object");
				return false;
			}
		}
		glBindFramebuffer(GL_FRAMEBUFFER, fboId);
		if(renderBuffer != 0){
			glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, 0);
			glDeleteRenderbuffers(renderBuffer);
		}
		glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, depthStencil.getId(), 0);
		
		if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE){
			System.err.println("Frame buffer incomplete");
			switch(glCheckFramebufferStatus(GL_FRAMEBUFFER)){
			
				case GL_FRAMEBUFFER_UNDEFINED :
					System.err.println("GL_FRAMEBUFFER_UNDEFINED");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER  :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
					return false;
				case GL_FRAMEBUFFER_UNSUPPORTED  :
					System.err.println("GL_FRAMEBUFFER_UNSUPPORTED");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE");
					return false;
				case GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS   :
					System.err.println("GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS");
					return false;	
			}
		}
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		return true;
	}
	
	public void bind(){
		glBindFramebuffer(GL_FRAMEBUFFER, fboId);
	}
	
	public void bindForRead(){
		glBindFramebuffer(GL_READ_BUFFER, fboId);
	}
	
	public void bindForDraw(){
		glBindFramebuffer(GL_DRAW_BUFFER, fboId);
	}
	
	public void unbindForRead(){
		glBindFramebuffer(GL_READ_BUFFER, 0);
	}
	
	public void unbindForDraw(){
		glBindFramebuffer(GL_DRAW_BUFFER, 0);
	}
	
	public void unbind(){
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	public int getId() {
		return fboId;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getRenderBuffer() {
		return renderBuffer;
	}

	public static int getMaxDrawBuffers(){
		return glGetInteger(GL_MAX_DRAW_BUFFERS);
	}
	
	public static int getMaxAttachments(){
		return glGetInteger(GL_MAX_COLOR_ATTACHMENTS);
	}
	
	public void delete(){
		if(renderBuffer != 0){
			glBindFramebuffer(GL_FRAMEBUFFER, fboId);
			glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, 0);
			glDeleteRenderbuffers(renderBuffer);
		}
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glDeleteFramebuffers(fboId);
	}
}
