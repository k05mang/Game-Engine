package renderers;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL33.*;

public class Texture {

	private int id, width, height, depth, mipLvls, maxMips, type;
	private boolean hasMipMaps, hasAlpha;
	private ArrayList<byte[]> data;
	public static final int 
		_1D = GL_TEXTURE_1D,
		_1D_ARRAY = GL_TEXTURE_1D_ARRAY,
		_2D = GL_TEXTURE_2D,
		_2D_ARRAY = GL_TEXTURE_2D_ARRAY,
		_2D_MULTISAMPLE = GL_TEXTURE_2D_MULTISAMPLE,
		_2D_MULTISAMPLE_ARRAY = GL_TEXTURE_2D_MULTISAMPLE_ARRAY,
		RECTANGLE = GL_TEXTURE_RECTANGLE,
		CUBE_MAP = GL_TEXTURE_CUBE_MAP,
		CUBE_POS_X = GL_TEXTURE_CUBE_MAP_POSITIVE_X,
		CUBE_NEG_X = GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
		CUBE_POS_Y = GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
		CUBE_NEG_Y = GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
		CUBE_POS_Z = GL_TEXTURE_CUBE_MAP_POSITIVE_Z,
		CUBE_NEG_Z = GL_TEXTURE_CUBE_MAP_NEGATIVE_Z,
		_3D = GL_TEXTURE_3D,
		BASE_LEVEL = GL_TEXTURE_BASE_LEVEL, 
		BORDER_COLOR = GL_TEXTURE_BORDER_COLOR,
		COMPARE_FUNC = GL_TEXTURE_COMPARE_FUNC, 
		COMPARE_MODE = GL_TEXTURE_COMPARE_MODE, 
		LOD_BIAS = GL_TEXTURE_LOD_BIAS, 
		MIN_FILTER = GL_TEXTURE_MIN_FILTER, 
		MAG_FILTER = GL_TEXTURE_MAG_FILTER, 
		MIN_LOD = GL_TEXTURE_MIN_LOD,
		MAX_LOD = GL_TEXTURE_MAX_LOD,
		MAX_LEVEL = GL_TEXTURE_MAX_LEVEL, 
		SWIZZLE_R = GL_TEXTURE_SWIZZLE_R, 
		SWIZZLE_G = GL_TEXTURE_SWIZZLE_G, 
		SWIZZLE_B = GL_TEXTURE_SWIZZLE_B, 
		SWIZZLE_A = GL_TEXTURE_SWIZZLE_A, 
		SWIZZLE_RGBA = GL_TEXTURE_SWIZZLE_RGBA,
		WRAP_S = GL_TEXTURE_WRAP_S, 
		WRAP_T = GL_TEXTURE_WRAP_T, 
		WRAP_R = GL_TEXTURE_WRAP_R
	;
	
	protected Texture(int width, int internalFormat, int format){
		id = glGenTextures();
		this.data = new ArrayList<byte[]>();
		type = _1D;
		int maxSize = glGetInteger(GL_MAX_TEXTURE_SIZE);
		if(width <= 0){
			System.err.println("Textures sizes cannot be 0 or negative, setting texture to default size: 128");
			this.width = 128;
		}else{
			this.width = (width < maxSize ? width : maxSize);
		}
		maxMips = 31 - Integer.numberOfLeadingZeros(this.width);
		height = 1;
		depth = 0;
		this.bind();
		glTexImage1D(type, 0, internalFormat, this.width, 0, format, GL_UNSIGNED_BYTE, (ByteBuffer)null);
		glTexParameteri(_1D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(_1D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(_1D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		this.unbind();
	}
	
	protected Texture(int type, int width, int height, int internalFormat, int format){
		id = glGenTextures();
		this.data = new ArrayList<byte[]>();
		this.type = type;
		depth = 1;
		//boolean isPower2 = ((this.height & (this.height-1)) == 0 && (this.width & (this.width-1)) == 0);
		int maxSize;
		this.bind();
		switch(type){
			case CUBE_MAP:
				maxSize = glGetInteger(GL_MAX_CUBE_MAP_TEXTURE_SIZE);
				if(width <= 0){
					System.err.println("Textures sizes cannot be 0 or negative, setting texture to default size: 128");
					this.width = 128;
				}else{
					this.width = (width < maxSize ? width : maxSize);
				}
				
				if(height <= 0){
					System.err.println("Textures sizes cannot be 0 or negative, setting texture to default size: 128");
					this.height = 128;
				}else{
					this.height = (height < maxSize ? height : maxSize);
				}
				maxMips = 31 - Integer.numberOfLeadingZeros((this.width < this.height ? this.height : this.width));
				
				for (int i = 0; i < 6; i++) {
					glTexImage2D(CUBE_POS_X + i, 0, internalFormat, this.width, this.height, 0, format, GL_UNSIGNED_BYTE, (ByteBuffer) null);
				}
				glTexParameteri(type, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
				break;
			case _1D_ARRAY:
				maxSize = glGetInteger(GL_MAX_TEXTURE_SIZE);
				int maxArraySize = glGetInteger(GL_MAX_ARRAY_TEXTURE_LAYERS); 
				if(width <= 0){
					System.err.println("Textures sizes cannot be 0 or negative, setting texture to default size: 128");
					this.width = 128;
				}else{
					this.width = (width < maxSize ? width : maxSize);
				}
				
				if(height <= 0){
					System.err.println("Textures sizes cannot be 0 or negative, setting texture to default size: 128");
					this.height = 128;
				}else{
					this.height = (height < maxArraySize ? height : maxArraySize);
				}
				maxMips = 31 - Integer.numberOfLeadingZeros(this.width);
				
				glTexImage2D(type, 0, internalFormat, this.width, this.height, 0, format, GL_UNSIGNED_BYTE, (ByteBuffer) null);
				break;
			default:
				maxSize = glGetInteger(type == RECTANGLE ? GL_MAX_RECTANGLE_TEXTURE_SIZE : GL_MAX_TEXTURE_SIZE);
				if(width <= 0){
					System.err.println("Textures sizes cannot be 0 or negative, setting texture to default size: 128");
					this.width = 128;
				}else{
					this.width = (width < maxSize ? width : maxSize);
				}
				
				if(height <= 0){
					System.err.println("Textures sizes cannot be 0 or negative, setting texture to default size: 128");
					this.height = 128;
				}else{
					this.height = (height < maxSize ? height : maxSize);
				}
				maxMips = type == RECTANGLE ? 1 : 31 - Integer.numberOfLeadingZeros((this.width < this.height ? this.height : this.width));

				glTexImage2D(type, 0, internalFormat, this.width, this.height, 0, format, GL_UNSIGNED_BYTE, (ByteBuffer) null);
				break;
		}
		glTexParameteri(type, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(type, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(type, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(type, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		this.unbind();
	}
	
	protected Texture(int type, int width, int height, int depth, int internalFormat, int format){
		id = glGenTextures();
		this.data = new ArrayList<byte[]>();
		this.type = type;
		this.bind();
		if(type == _2D_ARRAY){
			int maxSize = glGetInteger(GL_MAX_TEXTURE_SIZE);
			int maxArraySize = glGetInteger(GL_MAX_ARRAY_TEXTURE_LAYERS);
			if(width <= 0){
				System.err.println("Textures sizes cannot be 0 or negative, setting texture to default size: 128");
				this.width = 128;
			}else{
				this.width = (width < maxSize ? width : maxSize);
			}
			
			if(height <= 0){
				System.err.println("Textures sizes cannot be 0 or negative, setting texture to default size: 128");
				this.height = 128;
			}else{
				this.height = (height < maxSize ? height : maxSize);
			}
			
			if(depth <= 0){
				System.err.println("Textures sizes cannot be 0 or negative, setting texture to default size: 128");
				this.depth = 128;
			}else{
				this.depth = (depth < maxArraySize ? depth : maxArraySize);
			}
			maxMips = 31 - Integer.numberOfLeadingZeros((this.width < this.height ? this.height : this.width));
			
			glTexImage3D(type, 0, internalFormat, this.width, this.height, this.depth, 0, format, GL_UNSIGNED_BYTE, (ByteBuffer) null);
		}else{
			int maxSize = glGetInteger(GL_MAX_TEXTURE_SIZE);
			if(width <= 0){
				System.err.println("Textures sizes cannot be 0 or negative, setting texture to default size: 128");
				this.width = 128;
			}else{
				this.width = (width < maxSize ? width : maxSize);
			}
			
			if(height <= 0){
				System.err.println("Textures sizes cannot be 0 or negative, setting texture to default size: 128");
				this.height = 128;
			}else{
				this.height = (height < maxSize ? height : maxSize);
			}
			
			if(depth <= 0){
				System.err.println("Textures sizes cannot be 0 or negative, setting texture to default size: 128");
				this.depth = 128;
			}else{
				this.depth = (depth < maxSize ? depth : maxSize);
			}
			maxMips = 31 - Integer.numberOfLeadingZeros(Math.max(this.depth, Math.max(this.height, this.width)));
			
			glTexImage3D(type, 0, internalFormat, this.width, this.height, this.depth, 0, format, GL_UNSIGNED_BYTE, (ByteBuffer) null);	
		}
		glTexParameteri(type, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(type, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(type, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(type, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(type, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
		this.unbind();
	}
	
	protected Texture(int samples, boolean fixedSampleLoc, int internalFormat, int width, int height){
		id = glGenTextures();
		this.data = null;
		this.type = _2D_MULTISAMPLE;
		int maxSize = glGetInteger(GL_MAX_TEXTURE_SIZE);
		if(width <= 0){
			System.err.println("Textures sizes cannot be 0 or negative, setting texture to default size: 128");
			this.width = 128;
		}else{
			this.width = (width < maxSize ? width : maxSize);
		}
		
		if(height <= 0){
			System.err.println("Textures sizes cannot be 0 or negative, setting texture to default size: 128");
			this.height = 128;
		}else{
			this.height = (height < maxSize ? height : maxSize);
		}
		
		this.bind();
		glTexImage2DMultisample(_2D_MULTISAMPLE, samples, internalFormat, this.width, this.height, fixedSampleLoc);
		glTexParameteri(type, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(type, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(type, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(type, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		this.unbind();
	}
	
	protected Texture(int samples, boolean fixedSampleLoc, int internalFormat, int width, int height, int size){
		id = glGenTextures();
		this.data = null;
		this.type = _2D_MULTISAMPLE_ARRAY;
		int maxSize = glGetInteger(GL_MAX_TEXTURE_SIZE);
		int maxArraySize = glGetInteger(GL_MAX_ARRAY_TEXTURE_LAYERS);
		if(width <= 0){
			System.err.println("Textures sizes cannot be 0 or negative, setting texture to default size: 128");
			this.width = 128;
		}else{
			this.width = (width < maxSize ? width : maxSize);
		}
		
		if(height <= 0){
			System.err.println("Textures sizes cannot be 0 or negative, setting texture to default size: 128");
			this.height = 128;
		}else{
			this.height = (height < maxSize ? height : maxSize);
		}
		
		if(size <= 0){
			System.err.println("Textures sizes cannot be 0 or negative, setting texture to default size: 128");
			this.depth = 128;
		}else{
			this.depth = (depth < maxArraySize ? size : maxArraySize);
		}
		
		this.bind();
		glTexImage3DMultisample(_2D_MULTISAMPLE_ARRAY, samples, internalFormat, this.width, this.height, this.depth, fixedSampleLoc);
		glTexParameteri(type, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(type, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(type, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(type, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(type, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
		this.unbind();
	}
	
	protected Texture(int type, boolean mipMaps, boolean mipMapsProvided, int mipLvls, int width, int height, int internalFormat, int format, boolean hasAlpha, ArrayList<byte[]> data){
		id = glGenTextures();
		this.hasAlpha = hasAlpha;
		this.width = width;
		this.height = height;
		this.depth = 0;
		maxMips = mipLvls;
		this.mipLvls = mipLvls;
		this.data = data;
		this.type = type;
		
		this.bind();
		glTexParameteri(type, GL_TEXTURE_MAX_LEVEL, mipLvls);//set the limit for number of mipmaps to generate
		if(mipMapsProvided){
			
			hasMipMaps = true;
			if(type == _1D){//----1D texture----
				glTexImage1D(type, 0, internalFormat, width, 0, format, GL_UNSIGNED_BYTE, (ByteBuffer)null);//initialize base texture to be empty
				//allocate the space for the mip maps
				glGenerateMipmap(type);
				//go through each of the provided textures and buffe them for each mip map level
				for(int mip = 0; mip < mipLvls+1; mip++){
					ByteBuffer dataBuffer = BufferUtils.createByteBuffer(data.get(mip).length).put(data.get(mip));
					dataBuffer.flip();
					//buffer the texture and scale the dimensions to match 
					glTexSubImage1D(type, mip, 0, width >> mip, format, GL_UNSIGNED_BYTE, dataBuffer);
				}
			}else if(type == _1D_ARRAY){//----1D Array texture----
				//got through each of the indices of the texture array
				glTexImage2D(type, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, (ByteBuffer)null);
				for(int layer = 0; layer < height; layer++){
					//allocate the space for the mip maps
					glGenerateMipmap(type);
					//loop through each of the mip maps for the current array texture and buffer the texture to the VRAM
					for(int mip = 0; mip < mipLvls+1; mip++){
						ByteBuffer dataBuffer = BufferUtils.createByteBuffer(data.get(layer*(mipLvls+1)+mip).length)
								.put(data.get(layer*(mipLvls+1)+mip));
						dataBuffer.flip();
						//buffer mip texture and scale the dimensions
						glTexSubImage2D(type, mip, 0, 0, width >> mip, layer, format, GL_UNSIGNED_BYTE, dataBuffer);
					}
				}
			}else if(type == CUBE_MAP){//----Cube map texture----
				//loop through each of the faces of the cube map
				for(int face = 0; face < 6; face++){
					//create an empty texture for each face
					glTexImage2D(CUBE_POS_X+face, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, (ByteBuffer)null);
				}
				glGenerateMipmap(CUBE_MAP);
				//re-iterate each of the faces of the cube map again and initialize the mip maps
				//opengl requires type cube map for generating mip maps, making simultaneous buffering impossible
				for(int face = 0; face < 6; face++){
					for(int mip = 0; mip < mipLvls+1; mip++){
						ByteBuffer dataBuffer = BufferUtils.createByteBuffer(data.get(face*(mipLvls+1)+mip).length).put(data.get(face*(mipLvls+1)+mip));
						dataBuffer.flip();
						glTexSubImage2D(CUBE_POS_X+face, mip, 0, 0, width >> mip, height >> mip, format, GL_UNSIGNED_BYTE, dataBuffer);
					}
				}
				glTexParameteri(type, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
			}else{
				//complete
				glTexImage2D(type, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, (ByteBuffer)null);
				glGenerateMipmap(type);
				for(int mip = 0; mip < mipLvls+1; mip++){
					ByteBuffer dataBuffer = BufferUtils.createByteBuffer(data.get(mip).length).put(data.get(mip));
					dataBuffer.flip();
					glTexSubImage2D(type, mip, 0, 0, width >> mip, height >> mip, format, GL_UNSIGNED_BYTE, dataBuffer);
				}
			}
			glTexParameteri(type, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexParameteri(type, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
			
		}else if(mipMaps){
			
			hasMipMaps = true;
			if(type == _1D){//----1D texture----
				ByteBuffer dataBuffer = BufferUtils.createByteBuffer(data.get(0).length).put(data.get(0));
				dataBuffer.flip();
				glTexImage1D(type, 0, internalFormat, width, 0, format, GL_UNSIGNED_BYTE, dataBuffer);
			}else if(type == _1D_ARRAY){//----1D Array texture----
				ByteBuffer dataBuffer = BufferUtils.createByteBuffer(height*data.get(0).length);
				for(int layer = 0; layer < height; layer++){
					dataBuffer.put(data.get(layer));
				}
				dataBuffer.flip();
				glTexImage2D(type, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, dataBuffer);
			}else if(type == CUBE_MAP){//----Cube map texture----
				for(int face = 0; face < 6; face++){
					ByteBuffer dataBuffer = BufferUtils.createByteBuffer(data.get(face).length).put(data.get(face));
					dataBuffer.flip();
					glTexImage2D(CUBE_POS_X+face, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, dataBuffer);
				}
				glTexParameteri(type, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
			}else{
				ByteBuffer dataBuffer = BufferUtils.createByteBuffer(data.get(0).length).put(data.get(0));
				dataBuffer.flip();
				glTexImage2D(type, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, dataBuffer);
				//System.out.println(glGetString(glGetError()));
			}
			glGenerateMipmap(type);
			glTexParameteri(type, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexParameteri(type, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
			
		}else{
			
			hasMipMaps = false;
			if(type == _1D){//----1D texture----
				ByteBuffer dataBuffer = BufferUtils.createByteBuffer(data.get(0).length).put(data.get(0));
				dataBuffer.flip();
				glTexImage1D(type, 0, internalFormat, width, 0, format, GL_UNSIGNED_BYTE, dataBuffer);
			}else if(type == _1D_ARRAY){//----1D Array texture----
				ByteBuffer dataBuffer = BufferUtils.createByteBuffer(height*data.get(0).length);
				for(int layer = 0; layer < height; layer++){
					dataBuffer.put(data.get(layer));
				}
				dataBuffer.flip();
				glTexImage2D(type, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, dataBuffer);
			}else if(type == CUBE_MAP){//----Cube map texture----
				for(int face = 0; face < 6; face++){
					ByteBuffer dataBuffer = BufferUtils.createByteBuffer(data.get(face).length).put(data.get(face));
					dataBuffer.flip();
					glTexImage2D(CUBE_POS_X+face, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, dataBuffer);
				}
				glTexParameteri(type, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
			}else{
				ByteBuffer dataBuffer = BufferUtils.createByteBuffer(data.get(0).length).put(data.get(0));
				dataBuffer.flip();
				glTexImage2D(type, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, dataBuffer);
			}
			glTexParameteri(type, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexParameteri(type, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		}
		glTexParameteri(type, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(type, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		this.unbind();
	}
	
	protected Texture(int type, boolean mipMaps, boolean mipMapsProvided, int mipLvls, int width, int height, int depth, int internalFormat, int format, boolean hasAlpha, ArrayList<byte[]> data){
		id = glGenTextures();
		this.width = width;
		this.hasAlpha = hasAlpha;
		this.height = height;
		this.depth = depth;
		this.data = data;
		this.type = type;
		this.mipLvls = mipLvls;
		maxMips = mipLvls;
		
		this.bind();
		glTexParameteri(type, GL_TEXTURE_MAX_LEVEL, mipLvls);//set the limit for number of mipmaps to generate
		if(mipMapsProvided){
			
			hasMipMaps = true;
			if(type == _2D_ARRAY){
				glTexImage3D(type, 0, internalFormat, width, height, depth, 0, format, GL_UNSIGNED_BYTE, (ByteBuffer)null);
				glGenerateMipmap(type);
				for(int layer = 0; layer < depth; layer++){
					for(int mipmap = 0; mipmap < mipLvls+1; mipmap++){
						ByteBuffer dataBuffer = BufferUtils.createByteBuffer(data.get(layer*(mipLvls+1)+mipmap).length).put(data.get(layer*(mipLvls+1)+mipmap));
						dataBuffer.flip();
						glTexSubImage3D(type, mipmap, 0, 0, 0, width >> mipmap, height >> mipmap, layer, format, GL_UNSIGNED_BYTE, dataBuffer);
					}
				}
			}else{
				glTexImage3D(type, 0, internalFormat, width, height, depth, 0, format, GL_UNSIGNED_BYTE, (ByteBuffer)null);
				glGenerateMipmap(type);
				for(int mipmap = 0; mipmap < mipLvls+1; mipmap++){
					ByteBuffer dataBuffer = BufferUtils.createByteBuffer((depth >> mipmap)*(data.get(0).length >> (2*mipmap)));
					for(int layer = 0; layer < depth >> mipmap; layer++){
						dataBuffer.put(data.get(mipmap*(mipLvls+1)+layer));
					}
					dataBuffer.flip();
					glTexSubImage3D(type, mipmap, 0, 0, 0, width >> mipmap, height >> mipmap, depth >> mipmap, format, GL_UNSIGNED_BYTE, dataBuffer);
				}
			}
			glTexParameteri(type, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexParameteri(type, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
			
		}else if(mipMaps){
			
			hasMipMaps = true;
			if(type == _2D_ARRAY){
				ByteBuffer dataBuffer = BufferUtils.createByteBuffer(data.get(0).length*depth);
				for(int layer = 0; layer < depth; layer++){
					dataBuffer.put(data.get(layer));
				}
				dataBuffer.flip();
				glTexImage3D(type, 0, internalFormat, width, height, depth, 0, format, GL_UNSIGNED_BYTE, dataBuffer);
			}else{
				ByteBuffer dataBuffer = BufferUtils.createByteBuffer(data.get(0).length*depth);
				for(int layer = 0; layer < depth; layer++){
					dataBuffer.put(data.get(layer));
				}
				dataBuffer.flip();
				glTexImage3D(type, 0, internalFormat, width, height, depth, 0, format, GL_UNSIGNED_BYTE, dataBuffer);
			}
			glGenerateMipmap(type);
			glTexParameteri(type, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexParameteri(type, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
			
		}else{
			
			hasMipMaps = false;
			if(type == _2D_ARRAY){
				ByteBuffer dataBuffer = BufferUtils.createByteBuffer(data.get(0).length*depth);
				for(int layer = 0; layer < depth; layer++){
					dataBuffer.put(data.get(layer));
				}
				dataBuffer.flip();
				glTexImage3D(type, 0, internalFormat, width, height, depth, 0, format, GL_UNSIGNED_BYTE, dataBuffer);
			}else{
				ByteBuffer dataBuffer = BufferUtils.createByteBuffer(depth*data.get(0).length);
				for(int layer = 0; layer < depth; layer++){
					dataBuffer.put(data.get(layer));
				}
				dataBuffer.flip();
				glTexImage3D(type, 0, internalFormat, width, height, depth, 0, format, GL_UNSIGNED_BYTE, dataBuffer);
			}
			glTexParameteri(type, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexParameteri(type, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		}
		glTexParameteri(type, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(type, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(type, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
		this.unbind();
	}
	
	/**
	 * Generates an empty texture
	 * The texture created by this function will only use the information necessary to create the texture. If the texture does not
	 * use a value in its initialization it will be ignored, for instance a 2D texture will ignore the depth parameter. A 2D texture
	 * array will not.
	 * 
	 * Format and internal format will be checked for compatibility based on the OpenGL 3.3 specification, it is recommended that the 
	 * formats are compatible however in the even that they are not a default value will be chosen based on the format requested.
	 * If the format is not usable with textures null will be returned and the texture will have failed to be created. 
	 * 
	 * In the even the internal format does not match the given format but the format is usable a default value will be used instead, below 
	 * is a list of the default values used as the internal format for the given formats:
	 * 
	 * All color formats will have the same internal format as the given format
	 * Depth component - GL_DEPTH_COMPONENT24
	 * Depth and Stencil - GL_DEPTH24_STENCIL8
	 * 
	 * @param type Type of texture to be created by this function
	 * @param width Width of the texture
	 * @param height Height of the texture
	 * @param depth Depth of the texture
	 * @param internalFormat Internal format in which the texture will store and read its data
	 * @param format Format of the texture 
	 * @return An empty texture object with the given dimension and format data or null if the texture failed to be created
	 */
	public static Texture genEmpty(int type, int width, int height, int depth, int internalFormat, int format){
		//determine what type of texture is being created to call the appropriate constructor
		if(type == _1D){
			//check if the dimensions are usable
			if(width < 1){
				System.err.println("Failed to create texture size parameter was less than or equal to 0");
				return null;
			}
			
			//check the format, if the format doesn't match use a default value for the given type
			if(checkFormats(internalFormat, format)){
				//construct the texture object
				return new Texture(width, internalFormat, format);
			}else{
				//check what type of base format is used and use the chosen default value, or return null in case of failure
				if(	format == GL_RED  ||
					format == GL_RG   ||
					format == GL_RGB  ||
					format == GL_RGBA ||
					format == GL_BGRA)
				{
					return new Texture(width, format, format);
				}else if(format == GL_DEPTH_COMPONENT){
					return new Texture(width, GL_DEPTH_COMPONENT24, format);
				}else if(format == GL_DEPTH_STENCIL){
					return new Texture(width, GL_DEPTH24_STENCIL8, format);
				}else{
					System.err.println("Failed to create texture\nFormat given is not usable with textures");
					return null;
				}
			}
		}else if(type == _1D_ARRAY  ||
				 type == _2D 	    ||
				 type == RECTANGLE  ||
				 type == CUBE_POS_X ||
				 type == CUBE_NEG_X ||
				 type == CUBE_POS_Y ||
				 type == CUBE_NEG_Y ||
				 type == CUBE_POS_Z ||
				 type == CUBE_NEG_Z || 
				 type == CUBE_MAP)
		{
			if(width < 1 || height < 1 ){
				System.err.println("Failed to create texture size parameter was less than or equal to 0");
				return null;
			}
			
			if(checkFormats(internalFormat, format)){
				return new Texture(type, width, height, internalFormat, format);
			}else{
				if(	format == GL_RED  ||
					format == GL_RG   ||
					format == GL_RGB  ||
					format == GL_RGBA ||
					format == GL_BGRA)
				{
					return new Texture(type, width, height, format, format);
				}else if(format == GL_DEPTH_COMPONENT){
					return new Texture(type, width, height, GL_DEPTH_COMPONENT24, format);
				}else if(format == GL_DEPTH_STENCIL){
					return new Texture(type, width, height, GL_DEPTH24_STENCIL8, format);
				}else{
					System.err.println("Failed to create texture\nFormat given is not usable with textures");
					return null;
				}
			}
		}else if(type == _2D_ARRAY || type == _3D){
			if(width < 1 || height < 1 || depth < 1){
				System.err.println("Failed to create texture size parameter was less than or equal to 0");
				return null;
			}
			
			if(checkFormats(internalFormat, format)){
				return new Texture(type, width, height, depth, internalFormat, format);
			}else{
				if(	format == GL_RED  ||
					format == GL_RG   ||
					format == GL_RGB  ||
					format == GL_RGBA ||
					format == GL_BGRA)
				{
					return new Texture(type, width, height, depth, format, format);
				}else if(format == GL_DEPTH_COMPONENT){
					return new Texture(type, width, height, depth, GL_DEPTH_COMPONENT24, format);
				}else if(format == GL_DEPTH_STENCIL){
					return new Texture(type, width, height, depth, GL_DEPTH24_STENCIL8, format);
				}else{
					System.err.println("Failed to create texture\nFormat given is not usable with textures");
					return null;
				}
			}
		}else{
			System.err.println("Type given is not an applicable type or is not support");
			return null;
		}
	}

	/**
	 * Generates an empty texture
	 * The texture created by this function will only use the information necessary to create the texture. If the texture does not
	 * use a value in its initialization it will be ignored, for instance a 2D texture will ignore the depth parameter. A 2D texture
	 * array will not.
	 * 
	 * Format and internal format will be checked for compatibility based on the OpenGL 3.3 specification, it is recommended that the 
	 * formats are compatible however in the even that they are not a default value will be chosen based on the format requested.
	 * If the format is not usable with textures null will be returned and the texture will have failed to be created. 
	 * 
	 * In the even the internal format does not match the given format but the format is usable a default value will be used instead, below 
	 * is a list of the default values used as the internal format for the given formats:
	 * 
	 * All color formats will have the same internal format as the given format
	 * Depth component - GL_DEPTH_COMPONENT24
	 * Depth and Stencil - GL_DEPTH24_STENCIL8
	 * 
	 * @param type Type of texture to be created by this function
	 * @param width Width of the texture
	 * @param height Height of the texture
	 * @param depth Depth of the texture
	 * @param format Format of the texture 
	 * @return An empty texture object with the given dimension and format data or null if the texture failed to be created
	 */
	public static Texture genEmpty(int type, int width, int height, int depth, int format){
		if(	format == GL_RED  ||
			format == GL_RG   ||
			format == GL_RGB  ||
			format == GL_RGBA ||
			format == GL_BGRA)
		{
			return genEmpty(type, width, height, depth, format, format);
		}else if(format == GL_DEPTH_COMPONENT){
			return genEmpty(type, width, height, depth, GL_DEPTH_COMPONENT24, format);
		}else if(format == GL_DEPTH_STENCIL){
			return genEmpty(type, width, height, depth, GL_DEPTH24_STENCIL8, format);
		}else{
			System.err.println("Failed to create texture\nFormat given is not usable with textures");
			return null;
		}
	}

	/**
	 * Generates an empty texture
	 * The texture created by this function will only use the information necessary to create the texture. If the texture does not
	 * use a value in its initialization it will be ignored, for instance a 2D texture will ignore the depth parameter. A 2D texture
	 * array will not.
	 * 
	 * Format and internal format will be checked for compatibility based on the OpenGL 3.3 specification, it is recommended that the 
	 * formats are compatible however in the even that they are not a default value will be chosen based on the format requested.
	 * If the format is not usable with textures null will be returned and the texture will have failed to be created. 
	 * 
	 * In the even the internal format does not match the given format but the format is usable a default value will be used instead, below 
	 * is a list of the default values used as the internal format for the given formats:
	 * 
	 * All color formats will have the same internal format as the given format
	 * Depth component - GL_DEPTH_COMPONENT24
	 * Depth and Stencil - GL_DEPTH24_STENCIL8
	 * 
	 * @param type Type of texture to be created by this function
	 * @param width Width of the texture
	 * @param height Height of the texture
	 * @param format Format of the texture 
	 * @return An empty texture object with the given dimension and format data or null if the texture failed to be created
	 */
	public static Texture genEmpty(int type, int width, int height, int format){
		return genEmpty(type, width, height, 0, format);
	}
	
	/**
	 * Generates a multisample type texture
	 * The acceptable texture types are those that are multisample, however _2D and _2D_ARRAY are also acceptable as shortcuts
	 * for their multisample counter parts. 
	 * 
	 * Internal format will be tested for being a renderable format, in the event the internal format is not color, depth, or 
	 * stencil renderable the texture will fail to be loaded. Internal format will determine the textures rendering properties 
	 * and will change the sampling cap for the texture.
	 * 
	 * Any unused parameters will be ignored such as the size parameter for 2D multisample textures.
	 * 
	 * @param type Type of multisample texture to create
	 * @param samples Number of samples to use for this texture, this value is capped based on the rendering format
	 * @param fixedSampleLoc Determines whether the same sample location and number of samples will be used each time the
	 * texture is used
	 * @param internalFormat Internal format of the texture
	 * @param width Width of the texture
	 * @param height Height of the texture
	 * @param size Size of the array if the type is an array, this is equivalent to depth
	 * @return An empty multisample texture generated using the given arguments 
	 */
	public static Texture genMultisample(int type, int samples, boolean fixedSampleLoc, int internalFormat, int width, int height, int size){
		if(type != _2D_MULTISAMPLE || type != _2D_MULTISAMPLE_ARRAY || type != _2D || type != _2D_ARRAY){
			System.err.println("Failed to create multisample texture, type was not a valid texture type");
			return null;
		}
		boolean isColorRenderable = false, isDepthRenderable = false, isStencilRenderable = false;
		//check if the internalFormat given is a either color, depth, or stencil renderable
		if(	internalFormat == GL_RED  ||
			internalFormat == GL_RG	  ||
			internalFormat == GL_RGB  ||
			internalFormat == GL_RGBA ||
			internalFormat == GL_BGR  ||
			internalFormat == GL_BGRA)
		{
			isColorRenderable = true;
		}else if(internalFormat == GL_DEPTH_COMPONENT){
			isDepthRenderable = true;
		}else if(internalFormat == GL_DEPTH_STENCIL){
			isDepthRenderable = true;
			isStencilRenderable = true;
		}else if(internalFormat == GL_STENCIL_INDEX){
			isStencilRenderable = true;
		}else{
			//if the base formats failed, check to see if the internalFormat given is a sized format instead
			if(	checkFormats(internalFormat, GL_RED)  ||
				checkFormats(internalFormat, GL_RG)	  ||
				checkFormats(internalFormat, GL_RGB)  ||
				checkFormats(internalFormat, GL_RGBA) ||
				checkFormats(internalFormat, GL_BGR)  ||
				checkFormats(internalFormat, GL_BGRA) )
				{
					isColorRenderable = true;
				}else if(checkFormats(internalFormat, GL_DEPTH_COMPONENT)){
					isDepthRenderable = true;
				}else if(checkFormats(internalFormat, GL_DEPTH_STENCIL)){
					isDepthRenderable = true;
					isStencilRenderable = true;
				}else if(checkFormats(internalFormat, GL_STENCIL_INDEX)){
					isStencilRenderable = true;
				}else{
					System.err.println("Multisample format paramter is not a renderable format\n"
							+ "Please check the OpenGL 3.3 specification for color, depth, or stencil renderable formats");
					return null;
				}
		}
		int maxSamples = 0;
		
		if(isColorRenderable){
			maxSamples = glGetInteger(GL_MAX_COLOR_TEXTURE_SAMPLES);
		}else if(isDepthRenderable && isStencilRenderable){
			maxSamples = glGetInteger(GL_MAX_DEPTH_TEXTURE_SAMPLES);
		}else if(isDepthRenderable){
			maxSamples = glGetInteger(GL_MAX_DEPTH_TEXTURE_SAMPLES);
		}else if(isStencilRenderable){
			maxSamples = glGetInteger(GL_MAX_INTEGER_SAMPLES);
		}
		
		if(samples > 1){
			samples = (samples < maxSamples ? samples : maxSamples);
		}else{
			System.err.println("Unable to create multisample texture\n"
					+ "The number of samples for a multisample texture must not be negative, 0 or 1");
			return null;
		}
		
		switch(type){
			case _2D_MULTISAMPLE:
				return new Texture(samples, fixedSampleLoc, internalFormat, width, height);
			case _2D_MULTISAMPLE_ARRAY:
				return new Texture(samples, fixedSampleLoc, internalFormat, width, height, size);
			case _2D:
				return new Texture(samples, fixedSampleLoc, internalFormat, width, height);
			case _2D_ARRAY:
				return new Texture(samples, fixedSampleLoc, internalFormat, width, height, size);
			default:
				return null;
		}
	}
	
	/**
	 * Generates a multisample type texture
	 * The acceptable texture types are those that are multisample, however _2D and _2D_ARRAY are also acceptable as shortcuts
	 * for their multisample counter parts. 
	 * 
	 * Internal format will be tested for being a renderable format, in the event the internal format is not color, depth, or 
	 * stencil renderable the texture will fail to be loaded. Internal format will determine the textures rendering properties 
	 * and will change the sampling cap for the texture.
	 * 
	 * Any unused parameters will be ignored such as the size parameter for 2D multisample textures.
	 * 
	 * @param type Type of multisample texture to create
	 * @param samples Number of samples to use for this texture, this value is capped based on the rendering format
	 * @param internalFormat Internal format of the texture
	 * @param width Width of the texture
	 * @param height Height of the texture
	 * @param size Size of the array if the type is an array, this is equivalent to depth
	 * @return An empty multisample texture generated using the given arguments 
	 */
	public static Texture genMultisample(int type, int samples, int internalFormat, int width, int height, int size){
		return genMultisample(_2D_MULTISAMPLE, samples, true, internalFormat, width, height, 0);
	}

	/**
	 * Generates a 2D multisample type texture
	 * 
	 * Internal format will be tested for being a renderable format, in the event the internal format is not color, depth, or 
	 * stencil renderable the texture will fail to be loaded. Internal format will determine the textures rendering properties 
	 * and will change the sampling cap for the texture.
	 * 
	 * @param samples Number of samples to use for this texture, this value is capped based on the rendering format
	 * @param fixedSampleLoc Determines whether the same sample location and number of samples will be used each time the
	 * texture is used
	 * @param internalFormat Internal format of the texture
	 * @param width Width of the texture
	 * @param height Height of the texture
	 * @return An empty multisample texture generated using the given arguments 
	 */
	public static Texture genMultisample(int samples, boolean fixedSampleLoc, int internalFormat, int width, int height){
		return genMultisample(_2D_MULTISAMPLE, samples, fixedSampleLoc, internalFormat, width, height, 0);
	}

	/**
	 * Generates a 2D multisample type texture with an internal format of type GL_RGB
	 * 
	 * @param samples Number of samples to use for this texture, this value is capped based on the rendering format
	 * @param fixedSampleLoc Determines whether the same sample location and number of samples will be used each time the
	 * texture is used
	 * @param width Width of the texture
	 * @param height Height of the texture
	 * @return An empty multisample texture generated using the given arguments 
	 */
	public static Texture genMultisample(int samples, boolean fixedSampleLoc, int width, int height){
		return genMultisample(_2D_MULTISAMPLE, samples, fixedSampleLoc, GL_RGB, width, height, 0);
	}

	/**
	 * Generates a 2D multisample type texture with an internal format of type GL_RGB and fixed sample locations
	 * 
	 * @param samples Number of samples to use for this texture, this value is capped based on the rendering format
	 * @param width Width of the texture
	 * @param height Height of the texture
	 * @return An empty multisample texture generated using the given arguments 
	 */
	public static Texture genMultisample(int samples, int width, int height){
		return genMultisample(_2D_MULTISAMPLE, samples, true, GL_RGB, width, height, 0);
	}
	
	public static Texture load(int type, boolean mipMaps, boolean mipMapsProvided, int mipLvls, int internalFormat, int format, String... files){
		if(files == null || files.length < 1){
			System.err.println("Failed to generate texture, Files must be provided when using this method");
			return null;
		}else if(!checkFormats(internalFormat, format)){
			System.err.println("failed format check");
			return null;
		}else if(type == _2D_MULTISAMPLE || type == _2D_MULTISAMPLE_ARRAY){
			System.err.println("Type cannot be a multisample type, using perceived type instead");
			type = (type == _2D_MULTISAMPLE ? _2D : _2D_ARRAY);
		}
		
		switch(type){
			case CUBE_MAP:
				//test if there are the necessary number of files given for generating the requested number of mipMaps
				if(mipMapsProvided){
					if(files.length != (mipLvls+1)*6){
						System.err.println("Insufficient number of images provided for requested number of mipMaps");
						return null;
					}
				}
				else if(files.length < 6){
					System.err.println("Insufficient number of texture files to generate cube map");
					return null;
				}else{
					return genCubeMap(mipMaps, mipMapsProvided, mipLvls, internalFormat, format, files);
				}
			case _2D_ARRAY:
				if(mipMapsProvided){
					if(files.length%(mipLvls+1) != 0){
						System.err.println("Insufficient number of images provided for requested number of mipMaps");
						return null;
					}else{
						return gen2DArray(mipMaps, mipMapsProvided, mipLvls, internalFormat, format, files);
					}
				}else{
					return gen2DArray(mipMaps, mipMapsProvided, mipLvls, internalFormat, format, files);
				}
			case _3D:
				if(mipMapsProvided){
					if(files.length%(mipLvls+1) != 0){
						System.err.println("Insufficient number of images provided for requested number of mipMaps");
						return null;
					}else{
						return gen3D(mipMaps, mipMapsProvided, mipLvls, internalFormat, format, files);
					}
				}else{
					return gen3D(mipMaps, mipMapsProvided, mipLvls, internalFormat, format, files);
				}
			default:
				ArrayList<byte[]> data = new ArrayList<byte[]>(files.length);
				int finalH = 0, finalW = 0;
				int maxSize = glGetInteger(GL_MAX_TEXTURE_SIZE);
				boolean hasAlpha = false;
				org.newdawn.slick.opengl.Texture temp = null;
				if(mipMapsProvided && type != RECTANGLE){
					for(int subImage = 0; subImage < mipLvls+1; subImage++){
						temp = processFile(files[subImage]);
						if(temp == null){
							return null;
						}
						int textureH = temp.getImageHeight();
						int textureW = temp.getImageWidth();
						
						if (textureW > maxSize || textureH > maxSize) {
							System.err.println("Given textures exceed maximum cube map size for this system, failed to load textures");
							return null;
						}
						
						if(subImage == 0){
							finalH = textureH;
							finalW = textureW;
							hasAlpha = temp.hasAlpha();
							
							boolean isPower2 = (finalH & (finalH-1)) == 0 && (finalW & (finalW-1)) == 0;
							if(type != RECTANGLE && !isPower2){
								System.err.println("Images that are not powers of 2 must be of type RECTANGLE, failed to load image");
								return null;
							}
						}
						
						//doubles as a check for previous dimensions matching
						if(finalH >> subImage != textureH || finalW >> subImage != textureW){
							System.err.println("Sub images for provided mip maps are not the proper dimensions");
							return null;
						}
						
						data.add(flipImage(temp.getTextureData(), finalW, finalH, (hasAlpha ? 4 : 3)));
						//
						temp.release();
					}
				}else{
					temp = processFile(files[0]);
					if(temp == null){
						return null;
					}
					finalH = temp.getImageHeight();
					finalW = temp.getImageWidth();
					
					boolean isPower2 = (finalH & (finalH-1)) == 0 && (finalW & (finalW-1)) == 0;
					if(type != RECTANGLE && !isPower2){
						System.err.println("Images that are not powers of 2 must be of type RECTANGLE, failed to load image");
						return null;
					}
					
					hasAlpha = temp.hasAlpha();
					if (finalW > maxSize || finalH > maxSize) {
						System.err.println("Given textures exceed maximum cube map size for this system, failed to load textures");
						return null;
					}
					
					data.add(flipImage(temp.getTextureData(), finalW, finalH, (hasAlpha ? 4 : 3)));
					temp.release();
				}
				
				if(format == internalFormat){
					if(hasAlpha && format != GL_RGBA){
						format = internalFormat = GL_RGBA;
					}else if(!hasAlpha && format == GL_RGBA){
						format = internalFormat = GL_RGB;
					}
				}
				int mipsMax = 31 - Integer.numberOfLeadingZeros((finalW < finalH ? finalH : finalW));
				return new Texture(type, (type != RECTANGLE ? mipMaps : false), (type != RECTANGLE ? mipMapsProvided : false), (mipLvls < mipsMax ? mipLvls : mipsMax), 
						finalW, finalH, internalFormat, format, hasAlpha, data);
		}
	}
	
	public static Texture load(int type, boolean mipMaps, int mipLvls, int internalFormat, int format, String... files){
		return load(type, mipMaps, false, mipLvls, internalFormat, format, files);
	}
	
	public static Texture load(int type, boolean mipMaps, int internalFormat, int format, String... files){
		return load(type, mipMaps, false, 1000, internalFormat, format, files);
	}
	
	public static Texture load(int type, boolean mipMaps, int format, String... files){
		if(	format == GL_RED  ||
			format == GL_RG   ||
			format == GL_RGB  ||
			format == GL_RGBA ||
			format == GL_BGRA)
			{
				return load(type, mipMaps, false, 1000, format, format, files);
			}else if(format == GL_DEPTH_COMPONENT){
				return load(type, mipMaps, false, 1000, GL_DEPTH_COMPONENT16, format, files);
			}else if(format == GL_DEPTH_STENCIL){
				return load(type, mipMaps, false, 1000, GL_DEPTH24_STENCIL8, format, files);
			}else{
				System.err.println("Failed to create texture\nFormat given is not usable with textures");
				return null;
			}
	}
	
	public static Texture load(int type, boolean mipMaps, String... files){
		return load(type, mipMaps, false, 1000, GL_RGBA, GL_RGBA, files);
	}
	
	public static Texture load(boolean mipMaps, String... files){
		if(files == null){
			System.err.println("Files must be provided when calling the load function");
			return null;
		}
		if(files.length < 6){
			return load(_2D, mipMaps, false, 1000, GL_RGBA, GL_RGBA, files);
		}else{
			return load(CUBE_MAP, mipMaps, false, 1000, GL_RGBA, GL_RGBA, files);
		}
	}
	
	public static Texture load(int type, int mipLvls, int internalFormat, int format, String... files){
		return load(type, true, false, mipLvls, internalFormat, format, files);
	}
	
	public static Texture load(int type, int mipLvls, int format, String... files){
		if(	format == GL_RED  ||
			format == GL_RG   ||
			format == GL_RGB  ||
			format == GL_RGBA ||
			format == GL_BGRA)
			{
				return load(type, true, false, mipLvls, format, format, files);
			}else if(format == GL_DEPTH_COMPONENT){
				return load(type, true, false, mipLvls, GL_DEPTH_COMPONENT16, format, files);
			}else if(format == GL_DEPTH_STENCIL){
				return load(type, true, false, mipLvls, GL_DEPTH24_STENCIL8, format, files);
			}else{
				System.err.println("Failed to create texture\nFormat given is not usable with textures");
				return null;
			}
	}
	
	public static Texture load(int type, int mipLvls, String... files){
		return load(type, true, false, mipLvls, GL_RGBA, GL_RGBA, files);
	}
	
	public static Texture load(int type, String... files){
		return load(type, true, false, 1000, GL_RGBA, GL_RGBA, files);
	}
	
	public static Texture load(String... files){
		if(files == null){
			System.err.println("Files must be provided when calling the load function");
			return null;
		}
		if(files.length < 6){
			return load(_2D, true, false, 1000, GL_RGBA, GL_RGBA, files);
		}else{
			return load(CUBE_MAP, true, false, 1000, GL_RGBA, GL_RGBA, files);
		}
	}
	
	private static Texture genCubeMap(boolean mipMaps, boolean mipMapsProvided, int mipLvls, int internalFormat, int format, String... files){
		boolean hasSameDimensions = true, hasAlpha = false;
		ArrayList<byte[]> data = new ArrayList<byte[]>(files.length);
		int finalH = 0, finalW = 0;
		int maxSize = glGetInteger(GL_MAX_CUBE_MAP_TEXTURE_SIZE);
		for(int image = 0; image < 6; image++){
			org.newdawn.slick.opengl.Texture temp = null;
			if(mipMapsProvided){
				for(int subImage = 0; subImage < mipLvls+1; subImage++){
					temp = processFile(files[image*(mipLvls+1)+subImage]);
					if(temp == null){
						return null;
					}
					int textureH = temp.getImageHeight();
					int textureW = temp.getImageWidth();
					
					if (textureW > maxSize || textureH > maxSize) {
						System.err.println("Given textures exceed maximum cube map size for this system, failed to load textures");
						return null;
					}
					
					if(image == 0){
						finalH = textureH;
						finalW = textureW;
						hasAlpha = temp.hasAlpha();
						
						boolean isPower2 = (finalH & (finalH-1)) == 0 && (finalW & (finalW-1)) == 0;
						if(!isPower2){
							System.err.println("Images that are not powers of 2 must be of type RECTANGLE, failed to load image");
							return null;
						}else if(finalH != finalW){
							System.err.println("Failed to load cube map, images of a cube map must have the same dimensions to be square");
							return null;
						}
					}
					
					//doubles as a check for previous dimensions matching
					if(finalH >> subImage != textureH || finalW >> subImage != textureW){
						System.err.println("Sub images for provided mip maps are not the proper dimensions");
						return null;
					}
					
					data.add(flipImage(temp.getTextureData(), finalW, finalH, (hasAlpha ? 4 : 3)));
					temp.release();
				}
			}else{
				temp = processFile(files[image]);
				if(temp == null){
					return null;
				}
				int textureH = temp.getImageHeight();
				int textureW = temp.getImageWidth();
				
				if (textureW > maxSize || textureH > maxSize) {
					System.err.println("Given textures exceed maximum cube map size for this system, failed to load textures");
					return null;
				}
				
				finalH = textureH;
				finalW = textureW;
				
				boolean isPower2 = (finalH & (finalH-1)) == 0 && (finalW & (finalW-1)) == 0;
				if(!isPower2){
					System.err.println("Images that are not powers of 2 must be of type RECTANGLE, failed to load image");
					return null;
				}else if(finalH != finalW){
					System.err.println("Failed to load cube map, images of a cube map must have the same dimensions to be square");
					return null;
				}
				
				hasAlpha = temp.hasAlpha();
				
				data.add(flipImage(temp.getTextureData(), finalW, finalH, (hasAlpha ? 4 : 3)));
				if(image != 0){
					hasSameDimensions = hasSameDimensions && data.get(image-1).length == data.get(image).length;
				}
				temp.release();
				
				if(!hasSameDimensions){
					System.err.println("Failed to load cube map, supplied textures do not share the same dimensions");
					return null;
				}
			}
		}
		if(format == internalFormat){
			if(hasAlpha && format != GL_RGBA){
				format = internalFormat = GL_RGBA;
			}else if(!hasAlpha && format == GL_RGBA){
				format = internalFormat = GL_RGB;
			}
		}
		int mipsMax = 31 - Integer.numberOfLeadingZeros((finalW < finalH ? finalH : finalW));
		return new Texture(CUBE_MAP, mipMaps, mipMapsProvided, (mipLvls < mipsMax ? mipLvls : mipsMax), 
				finalW, finalH, internalFormat, format, hasAlpha, data);
	}
	
	private static Texture gen2DArray(boolean mipMaps, boolean mipMapsProvided, int mipLvls, int internalFormat, int format, String... files){
		ArrayList<byte[]> data = new ArrayList<byte[]>(files.length);
		int finalW = 0, finalH = 0;
		int maxSize = glGetInteger(GL_MAX_TEXTURE_SIZE);
		int maxArraySize = glGetInteger(GL_MAX_ARRAY_TEXTURE_LAYERS);
		int finalArraySize = 0;
		boolean hasSameDimensions = true, hasAlpha = false;
		
		int numElements = (mipMapsProvided ? files.length/(mipLvls+1) : files.length);
		if(mipMapsProvided){
			finalArraySize = (numElements > maxArraySize ? maxArraySize : numElements);
		}else{
			finalArraySize = (files.length > maxArraySize ? maxArraySize : files.length);
		}
		
		
		for(int image = 0; image < finalArraySize; image++){
			org.newdawn.slick.opengl.Texture temp = null;
			if(mipMapsProvided){
				for(int subImage = 0; subImage < mipLvls+1; subImage++){
					temp = processFile(files[image*(mipLvls+1)+subImage]);
					if(temp == null){
						return null;
					}
					int textureW = temp.getImageWidth();
					int textureH = temp.getImageHeight();
					
					if(image == 0){
						if (textureW > maxSize || textureH > maxSize) {
							System.err.println("Given textures exceed maximum size for this system, failed to load textures");
							return null;
						}
						finalW = textureW;
						finalH = textureH;
						hasAlpha = temp.hasAlpha();
						
						boolean isPower2 = (finalH & (finalH-1)) == 0 && (finalW & (finalW-1)) == 0;
						if(!isPower2){
							System.err.println("Images that are not powers of 2 must be of type RECTANGLE, failed to load image");
							return null;
						}
					}
					
					//doubles as a check for previous dimensions matching
					if(finalW >> subImage != textureW || finalW >> subImage != textureW){
						System.err.println("Sub images for provided mip maps are not the proper dimensions");
						return null;
					}
					
					data.add(flipImage(temp.getTextureData(), finalW, finalH, (hasAlpha ? 4 : 3)));
					temp.release();
				}
			}else{
				temp = processFile(files[image]);
				if(temp == null){
					return null;
				}
				int textureW = temp.getImageWidth();
				int textureH = temp.getImageHeight();
				
				if (textureW > maxSize || textureH > maxSize) {
					System.err.println("Given textures exceed maximum size for this system, failed to load textures");
					return null;
				}
				
				finalW = textureW;
				finalH = textureH;
				
				boolean isPower2 = (finalH & (finalH-1)) == 0 && (finalW & (finalW-1)) == 0;
				if(!isPower2){
					System.err.println("Images that are not powers of 2 must be of type RECTANGLE, failed to load image");
					return null;
				}
				
				hasAlpha = temp.hasAlpha();
				
				data.add(flipImage(temp.getTextureData(), finalW, finalH, (hasAlpha ? 4 : 3)));
				if(image != 0){
					hasSameDimensions = hasSameDimensions && data.get(image-1).length == data.get(image).length;
				}
				temp.release();
				
				if(!hasSameDimensions){
					System.err.println("Failed to load texture array, supplied textures do not share the same dimensions");
					return null;
				}
			}
		}
		if(format == internalFormat){
			if(hasAlpha && format != GL_RGBA){
				format = internalFormat = GL_RGBA;
			}else if(!hasAlpha && format == GL_RGBA){
				format = internalFormat = GL_RGB;
			}
		}
		int mipsMax = 31 - Integer.numberOfLeadingZeros(finalW > finalH ? finalW : finalH);
		return new Texture(_2D_ARRAY, mipMaps, mipMapsProvided, (mipLvls < mipsMax ? mipLvls : mipsMax), 
				finalW, finalH, numElements, internalFormat, format, hasAlpha, data);
	}
	
	private static Texture gen3D(boolean mipMaps, boolean mipMapsProvided, int mipLvls, int internalFormat, int format, String... files){
		ArrayList<byte[]> data = new ArrayList<byte[]>(files.length);
		int finalW = 0, finalH = 0, size3D = 0;
		int maxSize = glGetInteger(GL_MAX_3D_TEXTURE_SIZE);
		boolean hasSameDimensions = true, hasAlpha = false;
		
		if(mipMapsProvided){
			int baseSize = files.length/(mipLvls+1);
			size3D = (baseSize > maxSize ? maxSize : baseSize);
		}else{
			size3D = files.length > maxSize ? maxSize : files.length;
		}
		
		org.newdawn.slick.opengl.Texture temp = null;
		if(mipMapsProvided){
			for(int mipmap = 0; mipmap < mipLvls+1; mipmap++){
				for(int image = 0; image < size3D; image++){
					temp = processFile(files[mipmap*(mipLvls+1)+image]);
					if(temp == null){
						return null;
					}
					int textureW = temp.getImageWidth();
					int textureH = temp.getImageHeight();
					
					if(image == 0){
						if (textureW > maxSize || textureH > maxSize) {
							System.err.println("Given textures exceed maximum size for this system, failed to load textures");
							return null;
						}
						finalW = textureW;
						finalH = textureH;
						hasAlpha = temp.hasAlpha();
					}
					
					//doubles as a check for previous dimensions matching
					if(finalW >> mipmap != textureW || finalW >> mipmap != textureW){
						System.err.println("Sub images for provided mip maps are not the proper dimensions");
						return null;
					}
					
					data.add(flipImage(temp.getTextureData(), finalW, finalH, (hasAlpha ? 4 : 3)));
					temp.release();
				}
			}
		}else{
			for(int image = 0; image < size3D; image++){
				temp = processFile(files[image]);
				if(temp == null){
					return null;
				}
				int textureW = temp.getImageWidth();
				int textureH = temp.getImageHeight();
				
				if (textureW > maxSize || textureH > maxSize) {
					System.err.println("Given textures exceed maximum size for this system, failed to load textures");
					return null;
				}
				
				finalW = textureW;
				finalH = textureH;
				
				hasAlpha = temp.hasAlpha();
				
				data.add(flipImage(temp.getTextureData(), finalW, finalH, (hasAlpha ? 4 : 3)));
				if(image != 0){
					hasSameDimensions = hasSameDimensions && data.get(image-1).length == data.get(image).length;
				}
				temp.release();
				
				if(!hasSameDimensions){
					System.err.println("Failed to load 3D texture, supplied textures do not share the same dimensions");
					return null;
				}
			}
		}
		if(format == internalFormat){
			if(hasAlpha && format != GL_RGBA){
				format = internalFormat = GL_RGBA;
			}else if(!hasAlpha && format == GL_RGBA){
				format = internalFormat = GL_RGB;
			}
		}
		int mipsMax = 31 - Integer.numberOfLeadingZeros(Math.max(size3D,  Math.max(finalH, finalW)));
		return new Texture(_3D, mipMaps, mipMapsProvided, (mipLvls < mipsMax ? mipLvls : mipsMax), 
				finalW, finalH, size3D, internalFormat, format, hasAlpha, data);
	}
	
	private static org.newdawn.slick.opengl.Texture processFile(String file){
		org.newdawn.slick.opengl.Texture temp = null;
		String[] splitFile = file.split("\\.");
		String fileType = splitFile[splitFile.length-1].toLowerCase();
		//System.out.println(fileType);
		try {
			switch(fileType){
				case "png":
					temp = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream(file));
					break;
					
				case "jpeg":
					temp = TextureLoader.getTexture("JPEG", ResourceLoader.getResourceAsStream(file));
					break;
				case "jpg":
					temp = TextureLoader.getTexture("JPEG", ResourceLoader.getResourceAsStream(file));
					break;
				case "jpe":
					temp = TextureLoader.getTexture("JPEG", ResourceLoader.getResourceAsStream(file));
					break;
				case "jif":
					temp = TextureLoader.getTexture("JPEG", ResourceLoader.getResourceAsStream(file));
					break;
				case "jfif":
					temp = TextureLoader.getTexture("JPEG", ResourceLoader.getResourceAsStream(file));
					break;
				case "jfi":
					temp = TextureLoader.getTexture("JPEG", ResourceLoader.getResourceAsStream(file));
					break;
					
				case "gif":
					temp = TextureLoader.getTexture("GIF", ResourceLoader.getResourceAsStream(file));
					break;
					
				case "tga":
					temp = TextureLoader.getTexture("TGA", ResourceLoader.getResourceAsStream(file));
					break;	
				default:
					System.err.println("File type: "+ fileType+"");
					return null;
			}
		} catch (IOException e) {
			System.err.println("Failed to load texture: "+file);
			e.printStackTrace();
			return null;//in future make this use a default texture
		}
		return temp;
	}
	
	private static byte[] flipImage(byte[] image, int width, int height, int pixelbytes){
		byte[] flipped = new byte[image.length];
		int dataHeight = height*pixelbytes;
		int dataWidth = width*pixelbytes;
		for(int curLine = dataHeight-pixelbytes; curLine > 0; curLine -= pixelbytes){
			for(int curPixel = 0; curPixel < dataWidth-pixelbytes; curPixel++){
				if(pixelbytes == 4){
					flipped[curPixel+(dataHeight-pixelbytes-curLine)*width] = image[curPixel+curLine*width];
					flipped[curPixel+(dataHeight-pixelbytes-curLine)*width+1] = image[curPixel+curLine*width+1];
					flipped[curPixel+(dataHeight-pixelbytes-curLine)*width+2] = image[curPixel+curLine*width+2];
					flipped[curPixel+(dataHeight-pixelbytes-curLine)*width+3] = image[curPixel+curLine*width+3];
				}else{
					flipped[curPixel+(dataHeight-pixelbytes-curLine)*width] = image[curPixel+curLine*width];
					flipped[curPixel+(dataHeight-pixelbytes-curLine)*width+1] = image[curPixel+curLine*width+1];
					flipped[curPixel+(dataHeight-pixelbytes-curLine)*width+2] = image[curPixel+curLine*width+2];
				}
			}
		}
		return flipped;
	}
	
	/**
	 * Checks if two formats are compatible together for textures
	 * 
	 * @param internalFormat The sized internal format of the texture
	 * @param format The base internal format of the texture
	 * @return True if the formats match, false otherwise
	 */
	public static boolean checkFormats(int internalFormat, int format){
		switch(format){
			case GL_RED:
				if( internalFormat == GL_RED 	   				||
					internalFormat == GL_R8 	   				||
					internalFormat == GL_R8_SNORM  				|| 
					internalFormat == GL_R16 	   				|| 
					internalFormat == GL_R16_SNORM 				|| 
					internalFormat == GL_R16F 	   				|| 
					internalFormat == GL_R32F 	   				|| 
					internalFormat == GL_R8I 	   				|| 
					internalFormat == GL_R8UI 	   				|| 
					internalFormat == GL_R16I 	   				|| 
					internalFormat == GL_R16UI 	   				|| 
					internalFormat == GL_R32I 	   				|| 
					internalFormat == GL_R32UI	   				|| 
					internalFormat == GL_COMPRESSED_RED			|| 
					internalFormat == GL_COMPRESSED_RED_RGTC1   || 
					internalFormat == GL_COMPRESSED_SIGNED_RED_RGTC1)
				{
					return true;
				}else{
					//System.err.println("Internal format and the given format are not compatible types");
					return false;
				}
			case GL_RG:
				if( internalFormat == GL_RG 	   				||
					internalFormat == GL_RG8 	   				||		 
					internalFormat == GL_RG8_SNORM  			|| 
					internalFormat == GL_RG16 	   				|| 
					internalFormat == GL_RG16_SNORM 			|| 
					internalFormat == GL_RG16F 	   				|| 
					internalFormat == GL_RG32F 	   				|| 
					internalFormat == GL_RG8I 	   				|| 
					internalFormat == GL_RG8UI 	   				|| 
					internalFormat == GL_RG16I 	   				|| 
					internalFormat == GL_RG16UI 	   			|| 
					internalFormat == GL_RG32I 	   				|| 
					internalFormat == GL_RG32UI	   				|| 
					internalFormat == GL_COMPRESSED_RG			|| 
					internalFormat == GL_COMPRESSED_RG_RGTC2    || 
					internalFormat == GL_COMPRESSED_SIGNED_RG_RGTC2)
				{
					return true;
				}else{
					//System.err.println("Internal format and the given format are not compatible types");
					return false;
				}
			case GL_RGB:
				if(	internalFormat == GL_RGB 			||
					internalFormat == GL_R3_G3_B2 		||
					internalFormat == GL_RGB4 			||
					internalFormat == GL_RGB5 			||
					internalFormat == GL_RGB8 			||
					internalFormat == GL_RGB8_SNORM 	||
					internalFormat == GL_RGB10 			||
					internalFormat == GL_RGB12 			||
					internalFormat == GL_RGB16 			||
					internalFormat == GL_RGB16_SNORM 	||
					internalFormat == GL_SRGB8 			||
					internalFormat == GL_RGB16F 		||
					internalFormat == GL_RGB32F 		||
					internalFormat == GL_R11F_G11F_B10F ||
					internalFormat == GL_RGB9_E5 		||
					internalFormat == GL_RGB8I 			||
					internalFormat == GL_RGB8UI 		||
					internalFormat == GL_RGB16I 		||
					internalFormat == GL_RGB16UI 		||
					internalFormat == GL_RGB32I 		||
					internalFormat == GL_RGB32UI 		||
					internalFormat == GL_COMPRESSED_RGB || 
					internalFormat == GL_COMPRESSED_SRGB )
				{
					return true;
				}else{
					//System.err.println("Internal format and the given format are not compatible types");
					return false;
				}
			case GL_RGBA:
				if(	internalFormat == GL_RGBA 			 ||
					internalFormat == GL_RGBA2 			 ||
					internalFormat == GL_RGBA4 			 ||
					internalFormat == GL_RGB5_A1 		 ||
					internalFormat == GL_RGBA8 			 ||
					internalFormat == GL_RGBA8_SNORM 	 ||
					internalFormat == GL_RGB10_A2 		 ||
					internalFormat == GL_RGB10_A2UI 	 ||
					internalFormat == GL_RGBA12 		 ||
					internalFormat == GL_RGBA16 		 ||
					internalFormat == GL_RGBA16 		 ||
					internalFormat == GL_SRGB8_ALPHA8 	 ||
					internalFormat == GL_RGBA16F 		 ||
					internalFormat == GL_RGBA32F 		 ||
					internalFormat == GL_RGBA8I 		 ||
					internalFormat == GL_RGBA8UI 		 ||
					internalFormat == GL_RGBA16I 		 ||
					internalFormat == GL_RGBA16UI 		 ||
					internalFormat == GL_RGBA32I 		 ||
					internalFormat == GL_RGBA32UI 		 ||
					internalFormat == GL_COMPRESSED_RGBA || 
					internalFormat == GL_COMPRESSED_SRGB_ALPHA )
				{
					return true;
				}else{
					//System.err.println("Internal format and the given format are not compatible types");
					return false;
				}
			case GL_BGR:
				if(	internalFormat == GL_BGR 			||
					internalFormat == GL_R3_G3_B2 		||
					internalFormat == GL_RGB4 			||
					internalFormat == GL_RGB5 			||
					internalFormat == GL_RGB8 			||
					internalFormat == GL_RGB8_SNORM 	||
					internalFormat == GL_RGB10 			||
					internalFormat == GL_RGB12 			||
					internalFormat == GL_RGB16 			||
					internalFormat == GL_RGB16_SNORM 	||
					internalFormat == GL_SRGB8 			||
					internalFormat == GL_RGB16F 		||
					internalFormat == GL_RGB32F 		||
					internalFormat == GL_R11F_G11F_B10F ||
					internalFormat == GL_RGB9_E5 		||
					internalFormat == GL_RGB8I 			||
					internalFormat == GL_RGB8UI 		||
					internalFormat == GL_RGB16I 		||
					internalFormat == GL_RGB16UI 		||
					internalFormat == GL_RGB32I 		||
					internalFormat == GL_RGB32UI 		||
					internalFormat == GL_COMPRESSED_RGB || 
					internalFormat == GL_COMPRESSED_SRGB )
				{
					return true;
				}else{
					//System.err.println("Internal format and the given format are not compatible types");
					return false;
				}
			case GL_BGRA:
				if(	internalFormat == GL_BGRA 			 ||
					internalFormat == GL_RGBA2 			 ||
					internalFormat == GL_RGBA4 			 ||
					internalFormat == GL_RGB5_A1 		 ||
					internalFormat == GL_RGBA8 			 ||
					internalFormat == GL_RGBA8_SNORM 	 ||
					internalFormat == GL_RGB10_A2 		 ||
					internalFormat == GL_RGB10_A2UI 	 ||
					internalFormat == GL_RGBA12 		 ||
					internalFormat == GL_RGBA16 		 ||
					internalFormat == GL_RGBA16 		 ||
					internalFormat == GL_SRGB8_ALPHA8 	 ||
					internalFormat == GL_RGBA16F 		 ||
					internalFormat == GL_RGBA32F 		 ||
					internalFormat == GL_RGBA8I 		 ||
					internalFormat == GL_RGBA8UI 		 ||
					internalFormat == GL_RGBA16I 		 ||
					internalFormat == GL_RGBA16UI 		 ||
					internalFormat == GL_RGBA32I 		 ||
					internalFormat == GL_RGBA32UI 		 ||
					internalFormat == GL_COMPRESSED_RGBA || 
					internalFormat == GL_COMPRESSED_SRGB_ALPHA )
				{
					return true;
				}else{
					//System.err.println("Internal format and the given format are not compatible types");
					return false;
				}
			case GL_DEPTH_COMPONENT:
				if(	internalFormat == GL_DEPTH_COMPONENT16 ||
					internalFormat == GL_DEPTH_COMPONENT24 ||
					internalFormat == GL_DEPTH_COMPONENT32 ||
					internalFormat == GL_DEPTH_COMPONENT32F )
				{
					return true;
				}else{
					//System.err.println("Internal format and the given format are not compatible types");
					return false;
				}
			case GL_DEPTH_STENCIL:
				if(	internalFormat == GL_DEPTH24_STENCIL8 || internalFormat == GL_DEPTH32F_STENCIL8 )
				{
					return true;
				}else{
					//System.err.println("Internal format and the given format are not compatible types");
					return false;
				}
			case GL_STENCIL_INDEX:
				if(	internalFormat == GL_STENCIL_INDEX1 ||
					internalFormat == GL_STENCIL_INDEX4 ||
					internalFormat == GL_STENCIL_INDEX8 ||
					internalFormat == GL_STENCIL_INDEX16 )
				{
					return true;
				}else{
					//System.err.println("Internal format and the given format are not compatible types");
					return false;
				}
			default:
				//System.err.println("The format given is not a proper format for textures");
				return false;
		}
	}
	
	/**
	 * Sets the texParameter of this texture object
	 * 
	 * @param param The parameter to change 
	 * @param value The value to set the parameter to
	 */
	public void setParam(int param, int value){
		//test if the given parameter is usable with the texParam function based on the opengl 3.3 specification
		this.bind();
		if(param == BORDER_COLOR || param == SWIZZLE_RGBA ){
			System.err.println(param == SWIZZLE_RGBA ? "param SWIZZLE_RGBA requires 4 values to to be set" : "param BORDER_COLOR requires 4 values to to be set");
		}else if(param == LOD_BIAS || param == MIN_LOD || param == MAX_LOD){
			glTexParameterf(type, param, (float)value);
		}else{
			if(param == BASE_LEVEL || param == MAX_LEVEL){
				
				if(value < 0){
					System.err.println("Value must be positive for the parameters: MAX_LEVEL and BASE_LEVEL");
				}else if(this.type == RECTANGLE && value != 0){
					System.err.println("Rectangular textures can only have a base|max level value of 0");
				}else{
					glTexParameteri(type, param, value);
					if(param == MAX_LEVEL)
						mipLvls = value;
				}
				
			}else if(param == WRAP_S || param == WRAP_T || param == WRAP_R){
				
				if(value == GL_REPEAT || value == GL_MIRRORED_REPEAT){
					if(this.type == RECTANGLE){
						System.err.println("Rectangular textures cannot have REPEAT or MIRRORED_REPEAT as a wrapping value");
					}else{
						glTexParameteri(type, param, value);
					}
				}else if(value == GL_CLAMP_TO_EDGE || value == GL_CLAMP_TO_BORDER){
					glTexParameteri(type, param, value);
				}else{
					System.err.println("The value given is not applicable to WRAP_(S|T|R)");
				}
				
			}else if(param == SWIZZLE_R || param == SWIZZLE_G || param == SWIZZLE_B || param == SWIZZLE_A){
				
				if(value == GL_RED || value == GL_GREEN || value == GL_BLUE || value == GL_ALPHA || value == GL_ZERO || value == GL_ONE){
					glTexParameteri(type, param, value);
				}else{
					System.err.println("The value given is not applicable to SWIZZLE_(R|G|B|A)");
				}
				
			}else if(param == MIN_FILTER){
				
				if(value == GL_NEAREST || value == GL_LINEAR){
					glTexParameteri(type, param, value);
				}else if(value == GL_NEAREST_MIPMAP_NEAREST || value == GL_NEAREST_MIPMAP_LINEAR || value == GL_LINEAR_MIPMAP_NEAREST || value == GL_LINEAR_MIPMAP_LINEAR){
					//check if the texture has mipmap enabled to prevent errors with mipmap filters on non mipmap textures
					if(hasMipMaps){
						glTexParameteri(type, param, value);
					}else{
						System.err.println("Using mipmap filters on a non mipmap texture is prohibited");
					}
				}else{
					System.err.println("The value given is not applicable to MIN_FILTER");
				}
				
			}else if(param == MAG_FILTER){
				
				if(value == GL_NEAREST || value == GL_LINEAR){
					glTexParameteri(type, param, value);
				}else{
					System.err.println("The value given is not applicable to MAG_FILTER");
				}
				
			}else if(param == COMPARE_FUNC){
				
				if(value == GL_LEQUAL || value == GL_GEQUAL || value == GL_LESS || value == GL_GREATER || 
						value == GL_EQUAL || value == GL_NOTEQUAL || value == GL_ALWAYS || value == GL_NEVER){
					glTexParameteri(type, param, value);
				}else{
					System.err.println("The value given is not applicable to COMPARE_FUNC");
				}
				
			}else if(param == COMPARE_MODE){
				
				if(value == GL_NONE || value == GL_COMPARE_REF_TO_TEXTURE){
					glTexParameteri(type, param, value);
				}else{
					System.err.println("The value given is not applicable to COMPARE_MODE");
				}
				
			}else{
				System.err.println("The param given is not usable with texture parameters");
			}
		}
		this.unbind();
	}
	
	/**
	 * Sets the texParameter of this texture object
	 * 
	 * @param param The parameter to change 
	 * @param value The value to set the parameter to
	 */
	public void setParam(int param, float value){
		this.bind();
		if(param == LOD_BIAS || param == MIN_LOD || param == MAX_LOD){
			glTexParameterf(type, param, value);
		}else{
			System.err.println("The param given is isn't a type set with float values");
		}
		this.unbind();
	}
	
	/**
	 * Sets the texParamter of this texture, this function only works with parameters that take 
	 * 4 integer values. These values can be enumerated types or arbitrary values used for paramters
	 * like BORDER_COLOR
	 * 
	 * @param param Parameter to set, this must be of type GL_TEXTURE_BORDER_COLOR, GL_TEXTURE_SWIZZLE_RGBA, 
	 * Texture.SWIZZLE_RGBA, or Texture.BORDER_COLOR
	 * @param val1 First value passed to the context
	 * @param val2 Second value passed to the context
	 * @param val3 Third value passed to the context
	 * @param val4 Fourth value passed to the context
	 */
	public void setParam(int param, int val1, int val2, int val3, int val4){
		this.bind();
		if(param == BORDER_COLOR){
			IntBuffer buffer = BufferUtils.createIntBuffer(4);
			buffer.put(val1).put(val2).put(val3).put(val4);
			buffer.flip();
			glTexParameter(type, param, buffer);
		}else if(param == SWIZZLE_RGBA){
			boolean val1Ok = val1 == GL_RED || val1 == GL_GREEN || val1 == GL_BLUE || val1 == GL_ALPHA || val1 == GL_ZERO || val1 == GL_ONE;
			boolean val2Ok = val2 == GL_RED || val2 == GL_GREEN || val2 == GL_BLUE || val2 == GL_ALPHA || val2 == GL_ZERO || val2 == GL_ONE;
			boolean val3Ok = val3 == GL_RED || val3 == GL_GREEN || val3 == GL_BLUE || val3 == GL_ALPHA || val3 == GL_ZERO || val3 == GL_ONE;
			boolean val4Ok = val4 == GL_RED || val4 == GL_GREEN || val4 == GL_BLUE || val4 == GL_ALPHA || val4 == GL_ZERO || val4 == GL_ONE;
			if(val1Ok && val2Ok && val3Ok && val4Ok){
				IntBuffer buffer = BufferUtils.createIntBuffer(4);
				buffer.put(val1).put(val2).put(val3).put(val4);
				buffer.flip();
				glTexParameter(type, param, buffer);
			}else{
				System.err.println("The values given is not usable with the given parameter");
			}
		}else{
			System.err.println("The param given is not usable with texture parameters");
		}
		this.unbind();
	}
	
	/**
	 * Sets the texParamter of this texture, this function only works with parameters that take 
	 * 4 float values. These values can be enumerated types or arbitrary values used for paramters
	 * like BORDER_COLOR
	 * 
	 * @param param Parameter to set, this must be of type GL_TEXTURE_BORDER_COLOR or Texture.BORDER_COLOR
	 * @param val1 First value passed to the context
	 * @param val2 Second value passed to the context
	 * @param val3 Third value passed to the context
	 * @param val4 Fourth value passed to the context
	 */
	public void setParam(int param, float val1, float val2, float val3, float val4){
		this.bind();
		if(param == BORDER_COLOR){
			FloatBuffer buffer = BufferUtils.createFloatBuffer(4);
			buffer.put(val1).put(val2).put(val3).put(val4).flip();
			glTexParameter(type, param, buffer);
		}else{
			System.err.println("The param given is not usable with texture parameters");
		}
		this.unbind();
	}
	
	/**
	 * Gets the OpenGL texture id for this texture object
	 * 
	 * @return Integer representing the OpenGL texture id
	 */
	public int getId(){
		return id;
	}
	
	/**
	 * Gets the width of this texture
	 * 
	 * @return Width of the texture
	 */
	public int getWidth(){
		return width;
	}
	
	/**
	 * Gets the height of this texture
	 * 
	 * @return Height of the texture
	 */
	public int getHeight(){
		return height;
	}
	
	/**
	 * Gets the depth of this texture
	 * 
	 * @return Depth of the texture
	 */
	public int getDepth(){
		return depth;
	}
	/**
	 * Gets the type of this texture
	 * 
	 * @return The texture type
	 */
	public int getType(){
		return type;
	}
	
	/**
	 * Determines if this texture has mipMaps
	 * 
	 * @return True if the texture has mipMap either through OpenGL mipMap calls or through user provided mipMaps,
	 * false if it does not ahev mipMaps
	 */
	public boolean hasMipMaps(){
		return hasMipMaps;
	}
	
	public void genMipMaps(){
		if(type != _2D_MULTISAMPLE ||
			type != _2D_MULTISAMPLE_ARRAY ||
			type != RECTANGLE)
		{
			this.bind();
			glGenerateMipmap(type);
			this.unbind();
			hasMipMaps = true;
		}else{
			System.err.println("Unable to generate mip maps for this texture, type is not mip mappable");
		}
	}
	
	public boolean isArray(){
		if(type == _1D_ARRAY || type == _2D_ARRAY || type == _2D_MULTISAMPLE_ARRAY){
			return true;
		}else{
			return false;
		}
	}
	

	/**
	 * Determines whether this texture has an alpha component
	 * 
	 * @return True if this texture has an alpha, false otherwise
	 */
	public boolean hasAlpha(){
		return hasAlpha;
	}
	
	/**
	 * Binds this texture object to the context
	 */
	public void bind(){
		glBindTexture(type, id);
	}
	
	/**
	 * Binds this texture object to the context after setting the active texture unit to the value 
	 * provided.
	 * 
	 * @param activeTexture OpenGL texture unit to bind this texture to, this value must be in the range
	 * GL_TEXTUREi, where i ranges from 0 (GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS - 1)
	 * 
	 * @return True if this texture was bound to the texture unit, false if the provided value was not a 
	 * texture unit or was out of the range of the texture units
	 */
	public boolean bind(int activeTexture){
		int maxUnits = glGetInteger(GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS);
		if(activeTexture > GL_TEXTURE0 || activeTexture < GL_TEXTURE0+maxUnits){
			glActiveTexture(activeTexture);
			glBindTexture(type, id);
			return true;
		}else{
			return false;
		}
	}
	
	@Override
	public boolean equals(Object o){
		if(o!= null && o instanceof Texture){
			Texture equality = (Texture) o;
			return id == equality.getId();
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		return id;
	}
	
	/**
	 * Unbinds this texture object from the context
	 */
	public void unbind(){
		glBindTexture(type, 0);
	}
	
	/**
	 * Deletes this texture from the graphics card
	 */
	public void delete(){
		glBindTexture(type, 0);
		glDeleteTextures(id);
	}
}
