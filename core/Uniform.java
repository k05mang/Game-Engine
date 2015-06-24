package core;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

public class Uniform {
	private String name;
	private int location, type, size;
	private final int 
		BLOCK = 0,
		FLOAT = 1,
		INT = 2,
		BOOL = 3,
		MAT2 = 4,
		MAT2X3 = 5,
		MAT2X4 = 6,
		MAT3 = 7,
		MAT3X2 = 8,
		MAT3X4 = 9,
		MAT4 = 10,
		MAT4X2 = 11,
		MAT4X3 = 12
		;
	
	/**
	 * Constructs a uniform object with a name and a type given as strings, if the type given is null
	 * then the uniform will be considered a block uniform
	 * 
	 * @param name Name of the uniform in the shader program
	 * @param type String identifying what type of uniform variable this will represent
	 */
	public Uniform(String name, String type){
		this.name = name;
		if(type != null){
			if(type.toLowerCase().contains("sampler")){
				this.type = INT;
				size = 1;
			}else{
				switch(type.toLowerCase()){
					case "float":
						this.type = FLOAT;
						size = 1;
						break;
					case "int":
						this.type = INT;
						size = 1;
						break;
					case "uint":
						this.type = INT;
						size = 1;
						break;
					case "bool":
						this.type = BOOL;
						size = 1;
						break;
						
					case "vec2":
						this.type = FLOAT;
						size = 2;
						break;
					case "ivec2":
						this.type = INT;
						size = 2;
						break;
					case "uvec2":
						this.type = INT;
						size = 2;
						break;
					case "bvec2":
						this.type = BOOL;
						size = 2;
						break;
						
					case "vec3":
						this.type = FLOAT;
						size = 3;
						break;
					case "ivec3":
						this.type = INT;
						size = 3;
						break;
					case "uvec3":
						this.type = INT;
						size = 3;
						break;
					case "bvec3":
						this.type = BOOL;
						size = 3;
						break;
						
					case "vec4":
						this.type = FLOAT;
						size = 4;
						break;
					case "ivec4":
						this.type = INT;
						size = 4;
						break;
					case "uvec4":
						this.type = INT;
						size = 4;
						break;
					case "bvec4":
						this.type = BOOL;
						size = 4;
						break;
						
					case "mat2":
						this.type = MAT2;
						size = 4;
						break;
					case "mat2x2":
						this.type = MAT2;
						size = 4;
						break;
					case "mat2x3":
						this.type = MAT2X3;
						size = 6;
						break;
					case "mat2x4":
						this.type = MAT2X4;
						size = 8;
						break;
						
					case "mat3":
						this.type = MAT3;
						size = 9;
						break;
					case "mat3x3":
						this.type = MAT3;
						size = 9;
						break;
					case "mat3x2":
						this.type = MAT3X2;
						size = 6;
						break;
					case "mat3x4":
						this.type = MAT3X4;
						size = 12;
						break;
						
					case "mat4":
						this.type = MAT4;
						size = 16;
						break;
					case "mat4x4":
						this.type = MAT4;
						size = 16;
						break;
					case "mat4x2":
						this.type = MAT4X2;
						size = 8;
						break;
					case "mat4x3":
						this.type = MAT4X3;
						size = 12;
						break;
				}
			}
		}else{
			this.type = BLOCK;
		}
		location = -1;
	}
	
	/**
	 * Sets the location of this uniform object as it relates to the GPU location index
	 * 
	 * @param loc New location index of this uniform on the GPU
	 */
	public void setLoc(int loc){
		location = loc;
	}
	
	/**
	 * Gets the location of this uniform object
	 * 
	 * @return Location on the GPU of this uniform object if it can be found
	 */
	public int getLoc(){
		return location;
	}
	
	/**
	 * Sets the value of this uniform object on the GPU
	 * 
	 * @param data Buffer of data to use in changing the value of this uniform
	 */
	public void set(Buffer data){
		if(data instanceof FloatBuffer){
			FloatBuffer dataCast = (FloatBuffer)data;
			if(type == FLOAT){
				switch(size){
					case 1:
						glUniform1(location, dataCast);
						break;
					case 2:
						glUniform2(location, dataCast);
						break;
					case 3:
						glUniform3(location, dataCast);
						break;
					case 4:
						glUniform4(location, dataCast);
						break;
				}
			}else if(type >= MAT2){
				this.setMat(false, dataCast);
			}else{
				System.err.println("Types do not match, FloatBuffers can only be used with uniforms that are of type float or mat in GLSL");
			}
		}else if(data instanceof IntBuffer){
			IntBuffer dataCast = (IntBuffer)data;
			switch(type){
				case INT:
					switch(size){
						case 1:
							glUniform1(location, dataCast);
							break;
						case 2:
							glUniform2(location, dataCast);
							break;
						case 3:
							glUniform3(location, dataCast);
							break;
						case 4:
							glUniform4(location, dataCast);
						break;
					}
					break;
				case BOOL:
					switch(size){
						case 1:
							glUniform1(location, dataCast);
							break;
						case 2:
							glUniform2(location, dataCast);
							break;
						case 3:
							glUniform3(location, dataCast);
							break;
						case 4:
							glUniform4(location, dataCast);
						break;
					}
					break;
				default:
					System.err.println("Types do not match, IntBuffers can only be used with uniforms that are of type int, uint, bool, or sampler in GLSL");
					break;
			}
		}else{
			System.err.println("Failed to buffer data to uniform, buffer data is not of type FloatBuffer or IntBuffer");
		}
	}
	
	public void set(float... variables){
		if (type == FLOAT || type >= MAT2) {
			FloatBuffer data = BufferUtils.createFloatBuffer(size);
			if (variables.length >= size) {
				for (int insert = 0; insert < size; insert++) {
					data.put(variables[insert]);
				}
				data.flip();
			} else if (variables.length < size) {
				for (int insert = 0; insert < variables.length; insert++) {
					data.put(variables[insert]);
				}
				for (int remaining = size - variables.length; remaining < size; remaining++) {
					data.put(0.0f);
				}
				data.flip();
			}
			this.set(data);
		}else{
			System.err.println("Types do not match, floats can only be used with uniforms that are of type float in GLSL");
		}
	}
	
	public void set(int... variables){
		if (type == INT || type == BOOL) {
			IntBuffer data = BufferUtils.createIntBuffer(size);
			if (variables.length >= size) {
				for (int insert = 0; insert < size; insert++) {
					data.put(variables[insert]);
				}
				data.flip();
			} else if (variables.length < size) {
				for (int insert = 0; insert < variables.length; insert++) {
					data.put(variables[insert]);
				}
				for (int remaining = size - variables.length; remaining < size; remaining++) {
					data.put(0);
				}
				data.flip();
			}
			this.set(data);
		}else{
			System.err.println("Types do not match, this uniform is not of type int, uint, bool, or sampler");
		}
	}
	
	public void set(boolean... variables){
		if (type == BOOL) {
			IntBuffer data = BufferUtils.createIntBuffer(size);
			if (variables.length >= size) {
				for (int insert = 0; insert < size; insert++) {
					data.put((variables[insert] ? 1 : 0));
				}
				data.flip();
			} else if (variables.length < size) {
				for (int insert = 0; insert < variables.length; insert++) {
					data.put((variables[insert] ? 1 : 0));
				}
				for (int remaining = size - variables.length; remaining < size; remaining++) {
					data.put(0);
				}
				data.flip();
			}
			this.set(data);
		}else{
			System.err.println("Types do not match, this uniform is not of type bool");
		}
	}
	
	public void setMat(boolean transpose, FloatBuffer data){
		if(type >= MAT2){
			switch (type) {
				case MAT2:
					glUniformMatrix2(location, transpose, data);
					break;
				case MAT2X3:
					glUniformMatrix2x3(location, transpose, data);
					break;
				case MAT2X4:
					glUniformMatrix2x4(location, transpose, data);
					break;
				case MAT3:
					glUniformMatrix3(location, transpose, data);
					break;
				case MAT3X2:
					glUniformMatrix3x2(location, transpose, data);
					break;
				case MAT3X4:
					glUniformMatrix3x4(location, transpose, data);
					break;
				case MAT4:
					glUniformMatrix4(location, transpose, data);
					break;
				case MAT4X2:
					glUniformMatrix4x2(location, transpose, data);
					break;
				case MAT4X3:
					glUniformMatrix4x3(location, transpose, data);
					break;
			}
		}else{
			System.err.println("Types do not match, this uniform is not a matrix type");
		}
	}
}
