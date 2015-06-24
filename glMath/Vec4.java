package glMath;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

public class Vec4 implements Vector {

	public float x, y, z, w;
	public static final int SIZE_IN_BYTES = 16;
	public static final int SIZE_IN_FLOATS = 4;
	
	/**
	 * Default construct which initializes all vector components to 0
	 */
	public Vec4(){
		this(0,0,0,0);
	}
	
	/**
	 * Constructs a Vec4 object with 4 floats 
	 * 
	 * @param x X component of this vector
	 * @param y Y component of this vector
	 * @param z Z component of this vector
	 * @param w W component of this vector
	 */
	public Vec4(float x, float y, float z, float w){
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	/**
	 * Constructs a Vec4 object with two Vec2 objects
	 * 
	 * @param vec1 Vec2 which initializes the x and y components of this vector
	 * @param vec2 Vec2 which initializes the z and w components of this vector
	 */
	public Vec4(Vec2 vec1, Vec2 vec2){
		this(vec1.x, vec1.y, vec2.x, vec2.y);
	}
	
	/**
	 * Constructs a Vec4 object with a Vec2 and 2 floats
	 * 
	 * @param vec Vec2 which initializes the x and y components of this vector
	 * @param z Float for the z component
	 * @param w Float for the w component
	 */
	public Vec4(Vec2 vec, float z, float w){
		this(vec.x, vec.y, z, w);
	}

	/**
	 * Constructs a Vec4 object with 2 floats for the first components and a Vec2 for the last two components
	 *  
	 * @param x Float for the x component
	 * @param y Float for the y component
	 * @param vec Vec2 which initializes the x and y components of this vector
	 */
	public Vec4(float x, float y, Vec2 vec){
		this(x, y, vec.x, vec.y);
	}
	
	/**
	 * Constructs a Vec4 object with 2 floats initializing the x and w components of the vector and
	 * a Vec2 initializing the y and z components 
	 * 
	 * @param x Float for the x component
	 * @param vec Vec2 which initializes the y and z components of this vector
	 * @param w Float for the w component
	 */
	public Vec4(float x, Vec2 vec, float w){
		this(x, vec.x, vec.y, w);
	}
	
	/**
	 * Constructs a Vec4 object with a float and a Vec3 for the last components
	 * 
	 * @param x Float to initialize the x component of this vector
	 * @param vec Vec3 to initialize the y, z, and w components of this vector
	 */
	public Vec4(float x, Vec3 vec){
		this(x, vec.x, vec.y, vec.z);
	}
	
	 /**
	  * Constructs a Vec4 object with a Vec3 for the first components and a float for the last component
	  * 
	  * @param vec Vec3 to initialize the x, y, and z components of this vector
	  * @param w Float to initialize the w component of this vector
	  */
	public Vec4( Vec3 vec, float w){
		this(vec.x, vec.y, vec.z, w);
	}
	
	/**
	 * Copy constructor for Vec4
	 * 
	 * @param copy Vector to copy from
	 */
	public Vec4(Vec4 copy){
		this(copy.x, copy.y, copy.z, copy.w);
	}
	
	/**
	 * Constructs a Vec4 object using a single value to initialize all components
	 * 
	 * @param initializer Value to initialize the vector with
	 */
	public Vec4(float initializer){
		this(initializer, initializer, initializer, initializer);
	}
	
	/**
	 * Constructs a Vec4 with the first 4 components of the array
	 * 
	 * @param init Float array to use in the initialization
	 */
	public Vec4(float[] init){
		if(init.length >= 4){
			x = init[0];
			y = init[1];
			z = init[2];
			w = init[3];
		}
	}
	
	@Override
	public float dot(Vector vector) {
		if(vector instanceof Vec4){
			Vec4 vect = (Vec4)vector;
			return x*vect.x+y*vect.y+z*vect.z+w*vect.w;
		}else{
			System.err.println("Type mismatch in dot product, vector is not of type Vec4");
			return 0;	
		}
	}

	@Override
	public float length() {
		return (float)Math.sqrt(x*x+y*y+z*z+w*w);
	}

	@Override
	public float normalize() {
		float len = (float)Math.sqrt(x*x+y*y+z*z+w*w);
		x /= len;
		y /= len;
		z /= len;
		w /= len;
		return len;
	}

	@Override
	public Vec4 add(Vector vector) {
		if(vector instanceof Vec4){
			Vec4 vect = (Vec4)vector;
			x += vect.x;
			y += vect.y;
			z += vect.z;
			w += vect.w;
		}else{
			System.err.println("Type mismatch in vector addition, vector is not of type Vec3");
		}
		return this;
	}
	
	public Vec4 add(float x, float y, float z, float w) {
		this.x += x;
		this.y += y;
		this.z += z;
		this.w += w;
		return this;
	}

	@Override
	public Vec4 subtract(Vector vector) {
		if(vector instanceof Vec4){
			Vec4 vect = (Vec4)vector;
			x -= vect.x;
			y -= vect.y;
			z -= vect.z;
			w -= vect.w;
		}else{
			System.err.println("Type mismatch in vector subtraction, vector is not of type Vec3");
		}
		return this;
	}
	
	public Vec4 subtract(float x, float y, float z, float w) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		this.w -= w;
		return this;
	}

	@Override
	public Vec4 scale(float factor) {
		x *= factor;
		y *= factor;
		z *= factor;
		w *= factor;
		return this;
	}
	
	@Override
	public Vec4 set(int index, float value){
		switch(index){
			case 0:
				x = value;
				break;
			case 1:
				y = value;
				break;
			case 2:
				z = value;
				break;
			case 3:
				w = value;
				break;
			default:
				System.err.println("Index out of bounds for this vector");
				break;
		}
		return this;
	}
	
	@Override
	public Vec4 set(Vector vec){
		if(vec instanceof Vec4){
			Vec4 vector = (Vec4)vec;
			x = vector.x;
			y = vector.y;
			z = vector.z;
			w = vector.w;
		}else{
			System.err.println("Vector given is not of type Vec4, failed to set values");
		}
		return this;
	}
	
	@Override
	public Vec4 set(float... values){
		switch(values.length){
			case 1:
				x = values[0];
				break;
			case 2:
				x = values[0];
				y = values[1];
				break;
			case 3:
				x = values[0];
				y = values[1];
				z = values[2];
				break;
			case 4:
				x = values[0];
				y = values[1];
				z = values[2];
				w = values[3];
				break;
			default:
				break;
		}
		return this;
	}

	@Override
	public void store(FloatBuffer storage) {
		try {
			storage.put(x);
			storage.put(y);
			storage.put(z);
			storage.put(w);
		}catch (BufferOverflowException e) {
			System.err.println("Insufficient space in buffer for storing this vector");
			e.printStackTrace();
		}
	}
	
	@Override
	public void store(ByteBuffer storage){
		try {
			storage.putFloat(x);
			storage.putFloat(y);
			storage.putFloat(z);
			storage.putFloat(w);
		} catch (BufferOverflowException e) {
			System.err.println("Insufficient space in buffer for storing this vector");
			e.printStackTrace();
		}
	}

	@Override
	public Vector swizzle(String type){
		type = type.trim();
		switch(type.length()){
			case 2:
				switch(type.charAt(0)){
					case 'x':
						return getPosType(type, 2);
					case 'y':
						return getPosType(type, 2);
					case 'z':
						return getPosType(type, 2);
					case 'w':
						return getPosType(type, 2);
						
					case 'r':
						return getColorType(type, 2);
					case 'g':
						return getColorType(type, 2);
					case 'b':
						return getColorType(type, 2);
					case 'a':
						return getColorType(type, 2);
						
					case 's':
						return getTextureType(type, 2);
					case 't':
						return getTextureType(type, 2);
					case 'p':
						return getTextureType(type, 2);
					case 'q':
						return getTextureType(type, 2);
					default:
						System.err.println("Swizzle string must begin with either x, y, z, w, r, g, b, a, s, t, p, or q");
						return null;
				}
			case 3:
				switch(type.charAt(0)){
					case 'x':
						return getPosType(type, 3);
					case 'y':
						return getPosType(type, 3);
					case 'z':
						return getPosType(type, 3);
					case 'w':
						return getPosType(type, 3);
						
					case 'r':
						return getColorType(type, 3);
					case 'g':
						return getColorType(type, 3);
					case 'b':
						return getColorType(type, 3);
					case 'a':
						return getColorType(type, 3);
						
					case 's':
						return getTextureType(type, 3);
					case 't':
						return getTextureType(type, 3);
					case 'p':
						return getTextureType(type, 3);
					case 'q':
						return getTextureType(type, 3);
					default:
						System.err.println("Swizzle string must begin with either x, y, z, w, r, g, b, a, s, t, p, or q");
						return null;
				}
			case 4:
				switch(type.charAt(0)){
					case 'x':
						return getPosType(type, 4);
					case 'y':
						return getPosType(type, 4);
					case 'z':
						return getPosType(type, 4);
					case 'w':
						return getPosType(type, 4);
						
					case 'r':
						return getColorType(type, 4);
					case 'g':
						return getColorType(type, 4);
					case 'b':
						return getColorType(type, 4);
					case 'a':
						return getColorType(type, 4);
						
					case 's':
						return getTextureType(type, 4);
					case 't':
						return getTextureType(type, 4);
					case 'p':
						return getTextureType(type, 4);
					case 'q':
						return getTextureType(type, 4);
					default:
						System.err.println("Swizzle string must begin with either x, y, z, w, r, g, b, a, s, t, p, or q");
						return null;
				}
			default:
				System.err.println("Swizzle string must be of an appropriate length between 2 and 4");
				return null;
		}
	}
	
	private Vector getPosType(String type, int size){
		Vector result = null;
		switch(size){
			case 2:
				result = new Vec2();
				break;
			case 3:
				result = new Vec3();
				break;
			case 4:
				result = new Vec4();
				break;
		}
		
		for (int index = 0; index < size; index++) {
			char symbol = type.charAt(index);

			switch (symbol) {
				case 'x':
					result.set(index, x);
					break;
				case 'y':
					result.set(index, y);
					break;
				case 'z':
					result.set(index, z);
					break;
				case 'w':
					result.set(index, w);
					break;
				default:
					System.err.println("A given swizzle value is not applicable to this vector");
					return null;
			}
		}
		return result;
	}
	
	private Vector getColorType(String type, int size){
		Vector result = null;
		switch(size){
			case 2:
				result = new Vec2();
				break;
			case 3:
				result = new Vec3();
				break;
			case 4:
				result = new Vec4();
				break;
		}
		
		for (int index = 0; index < size; index++) {
			char symbol = type.charAt(index);

			switch (symbol) {
				case 'r':
					result.set(index, x);
					break;
				case 'g':
					result.set(index, y);
					break;
				case 'b':
					result.set(index, z);
					break;
				case 'a':
					result.set(index, w);
					break;
				default:
					System.err.println("A given swizzle value is not applicable to this vector");
					return null;
			}
		}
		return result;
	}
	
	private Vector getTextureType(String type, int size){
		Vector result = null;
		switch(size){
			case 2:
				result = new Vec2();
				break;
			case 3:
				result = new Vec3();
				break;
			case 4:
				result = new Vec4();
				break;
		}
		
		for (int index = 0; index < size; index++) {
			char symbol = type.charAt(index);

			switch (symbol) {
				case 's':
					result.set(index, x);
					break;
				case 't':
					result.set(index, y);
					break;
				case 'p':
					result.set(index, z);
					break;
				case 'q':
					result.set(index, w);
					break;
				default:
					System.err.println("A given swizzle value is not applicable to this vector");
					return null;
			}
		}
		return result;
	}
	
	@Override
	public Vec4 proj(Vector onto){
		if(onto instanceof Vec4){
			Vec4 target = new Vec4((Vec4)onto);
			return target.scale(dot(target)/target.dot(target));
		}else{
			System.err.println("Error in vector projection, incompatible types");
			return null;
		}
	}
	
	@Override
	public float comp(Vector onto){
		if(onto instanceof Vec4){
			return dot(onto)/onto.length();
		}else{
			System.err.println("Error in vector projection, incompatible types");
			return 0;
		}
	}
	
	@Override
	public boolean equals(Object vect){
		if(vect instanceof Vec4){
			Vec4 vec = (Vec4)vect;
			return vec.x == x && vec.y == y && vec.z == z && vec.w == w;
		}else{
			return false;
		}
	}
	
	@Override
	public int hashCode(){
		int result = 0;
		result = 23*result+Float.floatToIntBits(this.x);
		result = 23*result+Float.floatToIntBits(this.y);
		result = 23*result+Float.floatToIntBits(this.z);
		result = 23*result+Float.floatToIntBits(this.w);
		return result;
	}
	
	@Override
	public FloatBuffer asBuffer(){
		FloatBuffer storage = BufferUtils.createFloatBuffer(SIZE_IN_FLOATS);
		this.store(storage);
		return (FloatBuffer)storage.flip();
	}
	
	@Override
	public String toString(){
		return x+" | "+y+" | "+z+" | "+w;
	}
	
	@Override
	public void print(){
		System.out.println(toString());
	}
	
	@Override
	public boolean isZero(){
		return x == 0 && y == 0 && z == 0 && w == 0;
	}
	
	@Override
	public Vec4 inverse(){
		return (new Vec4(this)).scale(-1);
	}
	
	@Override
	public void trunc(){
		if(x < ROUND_VALUE && x > -ROUND_VALUE){
			x = 0.0f;
		}
		
		if(y < ROUND_VALUE && y > -ROUND_VALUE){
			y = 0.0f;
		}
		
		if(z < ROUND_VALUE && z > -ROUND_VALUE){
			z = 0.0f;
		}
		
		if(w < ROUND_VALUE && w > -ROUND_VALUE){
			w = 0.0f;
		}
	}

	@Override
	public void write(DataOutputStream stream) throws IOException {
		stream.writeFloat(this.x);
		stream.writeFloat(this.y);
		stream.writeFloat(this.z);
		stream.writeFloat(this.w);
	}
}
