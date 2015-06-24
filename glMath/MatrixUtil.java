package glMath;

public abstract class MatrixUtil {
	
	/**
	 * Gets the transpose of the given matrix as a separate matrix,
	 * the given matrix is unaltered
	 * 
	 * @param toTranspose Matrix whose transpose is requested
	 * @return A matrix that is the transpose of the given matrix
	 */
	public static Mat2 transpose(Mat2 toTranspose){
		return new Mat2(toTranspose).transpose();
	}

	/**
	 * Gets the transpose of the given matrix as a separate matrix,
	 * the given matrix is unaltered
	 * 
	 * @param toTranspose Matrix whose transpose is requested
	 * @return A matrix that is the transpose of the given matrix
	 */
	public static Mat3 transpose(Mat3 toTranspose){
		return new Mat3(toTranspose).transpose();
	}

	/**
	 * Gets the transpose of the given matrix as a separate matrix,
	 * the given matrix is unaltered
	 * 
	 * @param toTranspose Matrix whose transpose is requested
	 * @return A matrix that is the transpose of the given matrix
	 */
	public static Mat4 transpose(Mat4 toTranspose){
		return new Mat4(toTranspose).transpose();
	}
	
	/**
	 * Multiplies all the given matrices together and returns the result in a new matrix, the given
	 * matrices are multiplied from the farthest right matrix and are concatenated together moving
	 * left through the matrices
	 * 
	 * @param matrices Matrices to be multiplied together
	
	 * @return A new matrix of the same type as the ones given that contains the multiplication of 
	 * the given matrices
	 */
	public static Mat2 multiply(Mat2... matrices){
		//check if there are enough values in the array to multiply with
		if(matrices.length > 1){
			Mat2 result = new Mat2(1);
			for (int curMatrix = matrices.length - 1; curMatrix > -1; curMatrix--){
				result.leftMult(matrices[curMatrix]);
			}
			return result;
		}else{
			return matrices.length == 1 ? matrices[0] : null;
		}
	}
	
	/**
	 * Multiplies all the given matrices together and returns the result in a new matrix, the given
	 * matrices are multiplied from the farthest right matrix and are concatenated together moving
	 * left through the matrices
	 * 
	 * @param matrices Matrices to be multiplied together
	
	 * @return A new matrix of the same type as the ones given that contains the multiplication of 
	 * the given matrices
	 */
	public static Mat3 multiply(Mat3... matrices){
		//check if there are enough values in the array to multiply with
		if(matrices.length > 1){
			Mat3 result = new Mat3(1);
			for (int curMatrix = matrices.length - 1; curMatrix > -1; curMatrix--){
				result.leftMult(matrices[curMatrix]);
			}
			return result;
		}else{
			return matrices.length == 1 ? matrices[0] : null;
		}
	}
	
	/**
	 * Multiplies all the given matrices together and returns the result in a new matrix, the given
	 * matrices are multiplied from the farthest right matrix and are concatenated together moving
	 * left through the matrices
	 * 
	 * @param matrices Matrices to be multiplied together
	
	 * @return A new matrix of the same type as the ones given that contains the multiplication of 
	 * the given matrices
	 */
	public static Mat4 multiply(Mat4... matrices){
		//check if there are enough values in the array to multiply with
		if(matrices.length > 1){
			Mat4 result = new Mat4(1);
			for (int curMatrix = matrices.length - 1; curMatrix > -1; curMatrix--){
				result.leftMult(matrices[curMatrix]);
			}
			return result;
		}else{
			return matrices.length == 1 ? matrices[0] : null;
		}
	}

	/**
	 * Adds a list of matrices together and returns a new matrix that is the sum of the matrices
	 * the given matrices are read from left to right and added together
	 * 
	 * @param matrices Matrices to add together
	 * @return A new matrix containing the sum of the given matrices
	 */
	public static Mat2 add(Mat2... matrices){
		if(matrices.length > 1){
			Mat2 result = new Mat2(0);
			for (int curMatrix = 0; curMatrix < matrices.length; curMatrix++){
				result.add(matrices[curMatrix]);
			}
			return result;
		}else{
			return matrices.length == 1 ? matrices[0] : null;
		}
	}
	
	/**
	 * Adds a list of matrices together and returns a new matrix that is the sum of the matrices
	 * the given matrices are read from left to right and added together
	 * 
	 * @param matrices Matrices to add together
	 * @return A new matrix containing the sum of the given matrices
	 */
	public static Mat3 add(Mat3... matrices){
		if(matrices.length > 1){
			Mat3 result = new Mat3(0);
			for (int curMatrix = 0; curMatrix < matrices.length; curMatrix++){
				result.add(matrices[curMatrix]);
			}
			return result;
		}else{
			return matrices.length == 1 ? matrices[0] : null;
		}
	}
	
	/**
	 * Adds a list of matrices together and returns a new matrix that is the sum of the matrices
	 * the given matrices are read from left to right and added together
	 * 
	 * @param matrices Matrices to add together
	 * @return A new matrix containing the sum of the given matrices
	 */
	public static Mat4 add(Mat4... matrices){
		if(matrices.length > 1){
			Mat4 result = new Mat4(0);
			for (int curMatrix = 0; curMatrix < matrices.length; curMatrix++){
				result.add(matrices[curMatrix]);
			}
			return result;
		}else{
			return matrices.length == 1 ? matrices[0] : null;
		}
	}
	
	/**
	 * Subtracts a list of matrices and returns the result in a new matrix, values are read from left to right
	 * and subtracted
	 * 
	 * @param matrices Matrices to subtract
	 * @return A new matrix containing the difference between the given matrices
	 */
	public static Mat2 subtract(Mat2... matrices){
		if(matrices.length > 1){
			Mat2 result = new Mat2((Mat2)matrices[0]);
			for (int curMatrix = 1; curMatrix < matrices.length; curMatrix++){
				result.subtract(matrices[curMatrix]);
			}
			return result;
		}else{
			return matrices.length == 1 ? matrices[0] : null;
		}
	}
	
	/**
	 * Subtracts a list of matrices and returns the result in a new matrix, values are read from left to right
	 * and subtracted
	 * 
	 * @param matrices Matrices to subtract
	 * @return A new matrix containing the difference between the given matrices
	 */
	public static Mat3 subtract(Mat3... matrices){
		if(matrices.length > 1){
			Mat3 result = new Mat3((Mat3)matrices[0]);
			for (int curMatrix = 1; curMatrix < matrices.length; curMatrix++){
				result.subtract(matrices[curMatrix]);
			}
			return result;
		}else{
			return matrices.length == 1 ? matrices[0] : null;
		}
	}
	
	/**
	 * Subtracts a list of matrices and returns the result in a new matrix, values are read from left to right
	 * and subtracted
	 * 
	 * @param matrices Matrices to subtract
	 * @return A new matrix containing the difference between the given matrices
	 */
	public static Mat4 subtract(Mat4... matrices){
		if(matrices.length > 1){
			Mat4 result = new Mat4((Mat4)matrices[0]);
			for (int curMatrix = 1; curMatrix < matrices.length; curMatrix++){
				result.subtract(matrices[curMatrix]);
			}
			return result;
		}else{
			return matrices.length == 1 ? matrices[0] : null;
		}
	}
	
	/**
	 * Generates a scaling matrix of type Mat4 based on the given scalars
	 * 
	 * @param x X scale
	 * @param y Y scale
	 * @param z Z scale
	 * @return A 4x4 matrix of type Mat4 representing a scaling matrix using the given scaling components
	 */
	public static Mat4 scale(float x, float y, float z){
		return new Mat4(
				new Vec4(x,0,0,0),
				new Vec4(0,y,0,0),
				new Vec4(0,0,z,0),
				new Vec4(0,0,0,1)
				);
	}
	
	/**
	 * Generates a scaling matrix of type Mat4 based on the given scalars that are stored in the vector
	 *  
	 * @param scalars Vector containing the scalars for the matrix
	 * @return A 4x4 matrix of type Mat4 representing a scaling matrix using the given scaling components
	 */
	public static Mat4 scale(Vec3 scalars){
		return new Mat4(
				new Vec4(scalars.x,0,0,0),
				new Vec4(0,scalars.y,0,0),
				new Vec4(0,0,scalars.z,0),
				new Vec4(0,0,0,1)
				);
	}
	
	/**
	 * Generates a scaling matrix of type Mat3 based on the given scalars
	 * 
	 * @param x X scale
	 * @param y Y scale
	 * @return A 3x3 matrix of type Mat3 representing a scaling matrix using the given scaling components
	 */
	public static Mat3 scale(float x, float y){
		return new Mat3(
				new Vec3(x,0,0),
				new Vec3(0,y,0),
				new Vec3(0,0,1)
				);
	}
	
	/**
	 * Generates a scaling matrix of type Mat3 based on the given scalars that are stored in the vector
	 * 
	 * @param scalars Vector containing the scalars for this matrix
	 * @return A 3x3 matrix of type Mat3 representing a scaling matrix using the given scaling components
	 */
	public static Mat3 scale(Vec2 scalars){
		return new Mat3(
				new Vec3(scalars.x,0,0),
				new Vec3(0,scalars.y,0),
				new Vec3(0,0,1)
				);
	}
	
	/**
	 * Generates a 4x4 translation matrix
	 * 
	 * @param x X component to translate by
	 * @param y Y component to translate by
	 * @param z Z component to translate by
	 * @return A 4x4 matrix representing a translation by the given components 
	 */
	public static Mat4 translate(float x, float y, float z){
		return new Mat4(
				new Mat3(1),
				new Vec3(x, y, z)
				);
	}
	
	/**
	 * Generates a 4x4 translation matrix
	 * 
	 * @param vector Vector representing a direction and magnitude of translation in a 3 dimensional space
	 * @return A 4x4 matrix representing a translation by the given vector
	 */
	public static Mat4 translate(Vec3 vector){
		return new Mat4(
				new Mat3(1),
				vector
				);
	}
	
	/**
	 * Generates a 3x3 translation matrix
	 * 
	 * @param x X component to translate by
	 * @param y Y component to translate by
	 * @return A 3x3 matrix representing a translation by the given components
	 */
	public static Mat3 translate(float x, float y){
		return new Mat3(
				new Vec3(1,0,0),
				new Vec3(0,1,0),
				new Vec3(x, y, 1)
				);
	}
	
	/**
	 * Generates a 3x3 translation matrix
	 * 
	 * @param vector Vector representing a direction and magnitude of translation in a 2 dimensional space
	 * @return A 3x3 matrix representing a translation by the given vector
	 */
	public static Mat3 translate(Vec2 vector){
		return new Mat3(
				new Vec3(1,0,0),
				new Vec3(0,1,0),
				new Vec3(vector.x, vector.y, 1)
				);
	}
	
	/**
	 * Generates a 4x4 matrix representing a rotation around the axis given by the components x, y, z, by the angle theta
	 * 
	 * @param x X component of the axis of rotation
	 * @param y Y component of the axis of rotation
	 * @param z Z component of the axis of rotation
	 * @param theta Angle of rotation in degrees
	 * @return A 4x4 matrix representing a rotation of theta degrees around the given axis
	 */
	public static Mat4 rotate(float x, float y, float z, float theta){
		Vec3 axis = new Vec3(x, y, z);
		axis.normalize();
		return Quaternion.fromAxisAngle(axis, theta).asMatrix();
	}
	
	/**
	 * Generates a 4x4 matrix representing a rotation around the axis given by the vector, by the angle theta
	 * 
	 * @param axis Vector representing the axis of rotation
	 * @param theta Angle to rotate about the axis in degrees
	 * @return A 4x4 matrix representing a rotation of theta degrees around the given axis
	 */
	public static Mat4 rotate(Vec3 axis, float theta){
		Vec3 nAxis = new Vec3(axis);
		nAxis.normalize();
		return Quaternion.fromAxisAngle(nAxis, theta).asMatrix();
	}
	
	/**
	 * Generates a 3x3 matrix representing a rotation around the z axis 
	 * 
	 * @param theta Angle of rotation, in degrees,  around the z axis
	 * @return A 3x3 matrix representing a rotation of theta degrees around the z axis
	 */
	public static Mat3 rotate(float theta){
		float cos = (float)Math.cos(theta*Math.PI/180.0f);
		float sin = (float)Math.sin(theta*Math.PI/180.0f);
		
		return new Mat3(
				new Vec3(cos, sin, 0),
				new Vec3(-sin, cos, 0),
				new Vec3(0,0,1)
				);
	}
	
	/**
	 * Generates a perspective projection matrix as a 1-dimensional array that can be passed 
	 * into OpenGL shader uniforms, after being stored in a buffer, with calls to glUniformMatrix4* 
	 * 
	 * @param fovy Field of View angle for the y direction 
	 * @param aspect Aspect ratio of the area being rendered to
	 * @param zNear The nearest z value to the view before being clipped
	 * @param zFar The farthest z value from the view before being clipped
	 * @return Mat4 that is the perspective projection matrix formed using the given data
	 */
	public static Mat4 getPerspective(float fovy, float aspect, float zNear, float zFar){
		float f = 1/(float)Math.tan(Math.toRadians(fovy/2f));
		 return new Mat4(
				new Vec4(f/aspect,0,0,0),
				new Vec4(0,f,0,0),
				new Vec4(0,0,(zFar+zNear)/(zNear-zFar),-1),
				new Vec4(0,0,(2*zFar*zNear)/(zNear-zFar),0)
		);
	}
	
	/**
	 * Generates an orthographic projection matrix as a 1-dimensional array that can be passed 
	 * into OpenGL shader uniforms, after being stored in a buffer, with calls to glUniformMatrix4* 
	 * 
	 * @param left The minimum x clipping value
	 * @param right The maximum x clipping value
	 * @param bottom The minimum y clipping value
	 * @param top The maximum y clipping value
	 * @param zNear The nearest z value to the view before being clipped
	 * @param zFar The farthest z value from the view before being clipped
	 * @return Mat4 that is the Orthographic projection matrix formed using the given values
	 */
	public static Mat4 getOrtho(float left, float right, float bottom, float top, float zNear, float zFar){
		
		return new Mat4(
				new Vec4(2/(right-left),0,0,0),
				new Vec4(0,2/(top-bottom),0,0),
				new Vec4(0,0,1/(zFar-zNear),0),
				new Vec4(-((right+left)/(right-left)), -((top+bottom)/(top-bottom)), -(zNear/(zFar-zNear)), 1)
				);
	}
}
