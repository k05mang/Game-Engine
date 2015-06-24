package glMath;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

public class Vec2 implements Vector {

	public float x, y;
	public static final int SIZE_IN_BYTES = 8;
	public static final int SIZE_IN_FLOATS = 2;
	
	/**
	 * Default construct which initializes all vector components to 0
	 */
	public Vec2(){
		this(0,0);
	}
	
	/**
	 * Constructs a Vec2 object with two floats
	 * 
	 * @param x X component of this vector
	 * @param y Y component of this vector
	 */
	public Vec2(float x, float y){
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Copy constructor for Vec2
	 * 
	 * @param copy Vec2 to copy from
	 */
	public Vec2(Vec2 copy){
		this(copy.x, copy.y);
	}
	
	/**
	 * Constructs a Vec2 object with a single value to initialize all the components of this vector
	 * 
	 * @param initializer Value to initialize all components with
	 */
	public Vec2(float initializer){
		this(initializer, initializer);
	}
	
	/**
	 * Constructs a Vec2 with the first 2 components of the array
	 * 
	 * @param init Float array to use in the initialization
	 */
	public Vec2(float[] init){
		if(init.length >= 2){
			x = init[0];
			y = init[1];
		}
	}
	
	@Override
	public float dot(Vector vector) {
		if(vector instanceof Vec2){
			Vec2 vect = (Vec2)vector;
			return x*vect.x+y*vect.y;
		}else{
			System.err.println("Type mismatch in dot product, vector is not of type Vec2");
			return 0;
		}
	}

	@Override
	public float length() {
		return (float)Math.sqrt(x*x+y*y);
	}

	@Override
	public float normalize() {
		float len = (float)Math.sqrt(x*x+y*y);
		x /= len;
		y /= len;
		return len;
	}

	@Override
	public Vec2 add(Vector vector) {
		if(vector instanceof Vec2){
			Vec2 vect = (Vec2)vector;
			x += vect.x;
			y += vect.y;
		}else{
			System.err.println("Type mismatch in vector addition, vector is not of type Vec2");
		}
		return this;
	}
	
	public Vec2 add(float x, float y) {
		this.x += x;
		this.y += y;
		return this;
	}

	@Override
	public Vec2 subtract(Vector vector) {
		if(vector instanceof Vec2){
			Vec2 vect = (Vec2)vector;
			x -= vect.x;
			y -= vect.y;
		}else{
			System.err.println("Type mismatch in vector subtraction, vector is not of type Vec2");
		}
		return this;
	}
	
	public Vec2 subtract(float x, float y) {
		this.x -= x;
		this.y -= y;
		return this;
	}

	@Override
	public Vec2 scale(float factor) {
		x *= factor;
		y *= factor;
		return this;
	}
	
	@Override
	public Vec2 set(int index, float value){
		switch(index){
			case 0:
				x = value;
				break;
			case 1:
				y = value;
				break;
			default:
				System.err.println("Index out of bounds for this vector");
				break;
		}
		return this;
	}
	
	@Override
	public Vec2 set(Vector vec){
		if(vec instanceof Vec2){
			Vec2 vector = (Vec2)vec;
			x = vector.x;
			y = vector.y;
		}else{
			System.err.println("Vector given is not of type Vec2, failed to set values");
		}
		return this;
	}
	
	@Override
	public Vec2 set(float... values){
		switch(values.length){
			case 1:
				x = values[0];
				break;
			case 2:
				x = values[0];
				y = values[1];
				break;
			default:
				break;
		}
		return this;
	}
	
	@Override
	public void store(FloatBuffer storage){
		try {
			storage.put(x);
			storage.put(y);
		} catch (BufferOverflowException e) {
			System.err.println("Insufficient space in buffer for storing this vector");
			e.printStackTrace();
		}
	}
	
	@Override
	public void store(ByteBuffer storage){
		try {
			storage.putFloat(x);
			storage.putFloat(y);
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
						
					case 'r':
						return getColorType(type, 2);
					case 'g':
						return getColorType(type, 2);
						
					case 's':
						return getTextureType(type, 2);
					case 't':
						return getTextureType(type, 2);
					default:
						System.err.println("Swizzle string must begin with either x, y, r, g, s, or t");
						return null;
				}
			case 3:
				switch(type.charAt(0)){
					case 'x':
						return getPosType(type, 3);
					case 'y':
						return getPosType(type, 3);
						
					case 'r':
						return getColorType(type, 3);
					case 'g':
						return getColorType(type, 3);
						
					case 's':
						return getTextureType(type, 3);
					case 't':
						return getTextureType(type, 3);
					default:
						System.err.println("Swizzle string must begin with either x, y, r, g, s, or t");
						return null;
				}
			case 4:
				switch(type.charAt(0)){
					case 'x':
						return getPosType(type, 4);
					case 'y':
						return getPosType(type, 4);
						
					case 'r':
						return getColorType(type, 4);
					case 'g':
						return getColorType(type, 4);
						
					case 's':
						return getTextureType(type, 4);
					case 't':
						return getTextureType(type, 4);
					default:
						System.err.println("Swizzle string must begin with either x, y, r, g, s, or t");
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
				default:
					System.err.println("A given swizzle value is not applicable to this vector");
					return null;
			}
		}
		return result;
	}
	
	@Override
	public Vec2 proj(Vector onto){
		if(onto instanceof Vec2){
			Vec2 target = new Vec2((Vec2)onto);
			return target.scale(dot(target)/target.dot(target));
		}else{
			System.err.println("Error in vector projection, incompatible types");
			return null;
		}
	}
	
	@Override
	public float comp(Vector onto){
		if(onto instanceof Vec2){
			return dot(onto)/onto.length();
		}else{
			System.err.println("Error in vector projection, incompatible types");
			return 0;
		}
	}
	
	@Override
	public boolean equals(Object vect){
		if(vect instanceof Vec2){
			Vec2 vec = (Vec2)vect;
			return vec.x == x && vec.y == y;
		}else{
			return false;
		}
	}
	
	@Override
	public int hashCode(){
		int result = 0;
		result = 23*result+Float.floatToIntBits(this.x);
		result = 23*result+Float.floatToIntBits(this.y);
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
		return x+" | "+y;
//		return "x="+x+" | "+"y="+y;
	}
	
	@Override
	public void print(){
		System.out.println(toString());
	}
	
	@Override
	public boolean isZero(){
		return x == 0 && y == 0;
	}
	
	@Override
	public Vec2 inverse(){
		return (new Vec2(this)).scale(-1);
	}
	
	@Override
	public void trunc(){
		if(x < ROUND_VALUE && x > -ROUND_VALUE){
			x = 0.0f;
		}
		
		if(y < ROUND_VALUE && y > -ROUND_VALUE){
			y = 0.0f;
		}
	}

	@Override
	public void write(DataOutputStream stream) throws IOException {
		stream.writeFloat(this.x);
		stream.writeFloat(this.y);
	}
}
