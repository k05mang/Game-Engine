package glMath;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public interface Vector {
	public static final float ROUND_VALUE = 1e-10f;
	/**
	 * Computes the dot product of this vector with the given one if they are of the same type
	 * 
	 * @param vector Vector to dot with
	 * @return The dot product of this vector with the given one, 0 if their types do not match
	 */
	public float dot(Vector vector);
	
	/**
	 * Computes the length of this vector
	 * 
	 * @return The length of this vector
	 */
	public float length();
	
	/**
	 * Normalizes this vector, making the length 1
	 * 
	 * @return This vector post operation
	 */
	public float normalize();
	
	/**
	 * Adds the given vector to this vector
	 * 
	 * @param vector Vector to add to this vector
	 * @return This vector post operation
	 */
	public Vector add(Vector vector);
	
	/**
	 * Subtracts the given vector from this vector
	 * 
	 * @param vector Vector to subtract with
	 * @return This vector post operation
	 */
	public Vector subtract(Vector vector);
	
	/**
	 * Scales this vector by a given scalar factor
	 * 
	 * @param factor Amount to scale this vector by
	 * @return This vector post operation
	 */
	public Vector scale(float factor);
	
	/**
	 * Sets the value at the given index to the given value
	 * 
	 * @param index Index of for which part of the vector to change
	 * @param value Value to change at index to
	 */
	public Vector set(int index, float value);
	
	/**
	 * Sets the value of this vector to the given vectors values
	 * 
	 * @param vec Vector type matching the type of vector being passed to
	 */
	public Vector set(Vector vec);
	
	/**
	 * Sets the value of this vector to the values provided. The values provided are read
	 * sequentially and sets the values of the vector in the order x, y, z, w.
	 * 
	 * @param values Float values to be read into the vector
	 */
	public Vector set(float... values);
	
	/**
	 * Stores this vector in a float buffer that contains sufficient space to do so
	 * 
	 * @param storage FloatBuffer to store into 
	 */
	public void store(FloatBuffer storage);
	
	/**
	 * Stores this vector in a byte buffer that contains sufficient space to do so
	 * 
	 * @param storage ByteBuffer to store into 
	 */
	public void store(ByteBuffer storage);
	
	/**
	 * Creates a vector of a specific type that is no greater than a Vec4 and no less than a 
	 * Vec2, the given parameter is a string that represents the swizzling order of the desired vector\n
	 * \n
	 * For instance given the string "xxxx" this method would return the Vec4 composed of this vectors x values
	 * a string of "yzx" would return a Vec3 with the components of this vector specified by the swizzle string
	 * thus this Vec3 would be (this.y, this.z, this.x)
	 * 
	 * @param type A string representing the swizzle order, this string can be consist of the variables, xyzw, rgba, or stpq
	 * this string should be within reason of the current vector type, thus a Vec3 should not have a w, a, or q in its swizzling order
	 * 
	 * @return A vector composed of the given strings swizzle order
	 */
	public Vector swizzle(String type);
	
	/**
	 * Finds the projection of this vector onto the given vector
	 * 
	 * @param onto Vector to be projected onto
	 * @return Projection vector 
	 */
	public Vector proj(Vector onto);
	
	/**
	 * Computes the scalar projection of this vector onto the given vector
	 * 
	 * @param onto Vector to project onto
	 * @return Float value representing the scalar projection of this onto the given vector
	 */
	public float comp(Vector onto);
	
	/**
	 * Gets this vector as a float buffer
	 * 
	 * @return This vector as a float buffer
	 */
	public FloatBuffer asBuffer();
	
	/**
	 * Prints this vector to the terminal window
	 */
	public void print();
	
	/**
	 * Determines whether this vector is equal to the zero vector, it is recommended that trunc() be called before this is called
	 * trunc will round down any small near zero values
	 * 
	 * @return True if all this vectors components are equal to 0, false otherwise
	 */
	public boolean isZero();
	
	/**
	 * Returns a vector representing the "inverse" of this vector, that is, a vector scaled by -1
	 * 
	 * @return A copy of this vector scaled by -1
	 */
	public Vector inverse();
	
	/**
	 * Rounds near zero values in this vector to 0
	 */
	public void trunc();
	
	/**
	 * Writes a Vector out to a data stream
	 * 
	 * @param stream Data stream to be written out to
	 * @throws IOException
	 */
	public void write(DataOutputStream stream) throws IOException;
}
