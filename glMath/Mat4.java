package glMath;

import java.nio.BufferOverflowException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

public class Mat4 implements Matrix {
	private Vec4[] matrix;
	public static final int SIZE_IN_BYTES = 64;
	public static final int SIZE_IN_FLOATS = 16;
	
	public Mat4(){
		matrix = new Vec4[4];
		this.loadIdentity();
	}
	
	public Mat4(float diag){
		matrix = new Vec4[4];
		matrix[0] = new Vec4(diag,0,0,0);
		matrix[1] = new Vec4(0,diag,0,0);
		matrix[2] = new Vec4(0,0,diag,0);
		matrix[3] = new Vec4(0,0,0,diag);
	}
	
	public Mat4(Vec4 col1, Vec4 col2, Vec4 col3, Vec4 col4){
		matrix = new Vec4[4];
		matrix[0] = new Vec4(col1);
		matrix[1] = new Vec4(col2);
		matrix[2] = new Vec4(col3);
		matrix[3] = new Vec4(col4);
	}
	
	public Mat4(Mat3 upper, Vec3 rightCol){
		matrix = new Vec4[4];
		Vec3[] upCopy = upper.getMatrix();
		matrix[0] = new Vec4(upCopy[0], 0);
		matrix[1] = new Vec4(upCopy[1], 0);
		matrix[2] = new Vec4(upCopy[2], 0);
		matrix[3] = new Vec4(rightCol , 1);
	}
	
	public Mat4(Mat4 copy){
		matrix = new Vec4[4];
		Vec4[] copyFrom = copy.getMatrix();
		matrix[0] = new Vec4(copyFrom[0]);
		matrix[1] = new Vec4(copyFrom[1]);
		matrix[2] = new Vec4(copyFrom[2]);
		matrix[3] = new Vec4(copyFrom[3]);
	}
	
	public Mat3 getNormalMatrix(){
		return new Mat3( 
				(Vec3)matrix[0].swizzle("xyz"),
				(Vec3)matrix[1].swizzle("xyz"),
				(Vec3)matrix[2].swizzle("xyz") 
				);
	}

	@Override
	public Mat4 invert() {
		float det = this.determinant();
		if( det != 0){
			Vec4 col1 = new Vec4(
					 matrix[1].y*(matrix[2].z*matrix[3].w-matrix[3].z*matrix[2].w)
					 -matrix[2].y*(matrix[1].z*matrix[3].w-matrix[3].z*matrix[1].w)
					 +matrix[3].y*(matrix[1].z*matrix[2].w-matrix[2].z*matrix[1].w),
		
					-(matrix[0].y*(matrix[2].z*matrix[3].w-matrix[3].z*matrix[2].w)
					 -matrix[2].y*(matrix[0].z*matrix[3].w-matrix[3].z*matrix[0].w)
					 +matrix[3].y*(matrix[0].z*matrix[2].w-matrix[2].z*matrix[0].w)),
				
					  matrix[0].y*(matrix[1].z*matrix[3].w-matrix[3].z*matrix[1].w)
					 -matrix[1].y*(matrix[0].z*matrix[3].w-matrix[3].z*matrix[0].w)
					 +matrix[3].y*(matrix[0].z*matrix[1].w-matrix[1].z*matrix[0].w),
				
					-(matrix[0].y*(matrix[1].z*matrix[2].w-matrix[2].z*matrix[1].w)
					 -matrix[1].y*(matrix[0].z*matrix[2].w-matrix[2].z*matrix[0].w)
					 +matrix[2].y*(matrix[0].z*matrix[1].w-matrix[1].z*matrix[0].w))
					  );
			
			Vec4 col2 = new Vec4(
					 -(matrix[1].x*(matrix[2].z*matrix[3].w-matrix[3].z*matrix[2].w)
					 -matrix[2].x*(matrix[1].z*matrix[3].w-matrix[3].z*matrix[1].w)
					 +matrix[3].x*(matrix[1].z*matrix[2].w-matrix[2].z*matrix[1].w)),
		
					  matrix[0].x*(matrix[2].z*matrix[3].w-matrix[3].z*matrix[2].w)
					 -matrix[2].x*(matrix[0].z*matrix[3].w-matrix[3].z*matrix[0].w)
					 +matrix[3].x*(matrix[0].z*matrix[2].w-matrix[2].z*matrix[0].w),
				
					  matrix[0].x*(matrix[1].z*matrix[3].w-matrix[3].z*matrix[1].w)
					 -matrix[1].x*(matrix[0].z*matrix[3].w-matrix[3].z*matrix[0].w)
					 +matrix[3].x*(matrix[0].z*matrix[1].w-matrix[1].z*matrix[0].w),
				
					 -(matrix[0].x*(matrix[1].z*matrix[2].w-matrix[2].z*matrix[1].w)
					 -matrix[1].x*(matrix[0].z*matrix[2].w-matrix[2].z*matrix[0].w)
					 +matrix[2].x*(matrix[0].z*matrix[1].w-matrix[1].z*matrix[0].w))
					);
			
			Vec4 col3 = new Vec4(
					 matrix[1].x*(matrix[2].y*matrix[3].w-matrix[3].y*matrix[2].w)
					 -matrix[2].x*(matrix[1].y*matrix[3].w-matrix[3].y*matrix[1].w)
					 +matrix[3].x*(matrix[1].y*matrix[2].w-matrix[2].y*matrix[1].w),
		
					-(matrix[0].x*(matrix[2].y*matrix[3].w-matrix[3].y*matrix[2].w)
					 -matrix[2].x*(matrix[0].y*matrix[3].w-matrix[3].y*matrix[0].w)
					 +matrix[3].x*(matrix[0].y*matrix[2].w-matrix[2].y*matrix[0].w)),
				
					  matrix[0].x*(matrix[1].y*matrix[3].w-matrix[3].y*matrix[1].w)
					 -matrix[1].x*(matrix[0].y*matrix[3].w-matrix[3].y*matrix[0].w)
					 +matrix[3].x*(matrix[0].y*matrix[1].w-matrix[1].y*matrix[0].w),
				
					-(matrix[0].x*(matrix[1].y*matrix[2].w-matrix[2].y*matrix[1].w)
					 -matrix[1].x*(matrix[0].y*matrix[2].w-matrix[2].y*matrix[0].w)
					 +matrix[2].x*(matrix[0].y*matrix[1].w-matrix[1].y*matrix[0].w))
					  );
			
			Vec4 col4 = new Vec4(
					 -(matrix[1].x*(matrix[2].y*matrix[3].z-matrix[3].y*matrix[2].z)
					 -matrix[2].x*(matrix[1].y*matrix[3].z-matrix[3].y*matrix[1].z)
					 +matrix[3].x*(matrix[1].y*matrix[2].z-matrix[2].y*matrix[1].z)),
		
					  matrix[0].x*(matrix[2].y*matrix[3].z-matrix[3].y*matrix[2].z)
					 -matrix[2].x*(matrix[0].y*matrix[3].z-matrix[3].y*matrix[0].z)
					 +matrix[3].x*(matrix[0].y*matrix[2].z-matrix[2].y*matrix[0].z),
				
					  matrix[0].x*(matrix[1].y*matrix[3].z-matrix[3].y*matrix[1].z)
					 -matrix[1].x*(matrix[0].y*matrix[3].z-matrix[3].y*matrix[0].z)
					 +matrix[3].x*(matrix[0].y*matrix[1].z-matrix[1].y*matrix[0].z),
				
					 -(matrix[0].x*(matrix[1].y*matrix[2].z-matrix[2].y*matrix[1].z)
					 -matrix[1].x*(matrix[0].y*matrix[2].z-matrix[2].y*matrix[0].z)
					 +matrix[2].x*(matrix[0].y*matrix[1].z-matrix[1].y*matrix[0].z))
					);
			
			matrix[0].set(col1);
			matrix[1].set(col2);
			matrix[2].set(col3);
			matrix[3].set(col4);
			this.multFactor(1/det);
		}else{
			System.err.println("This matrix is not invertible");
		}
		return this;
	}
	
	@Override
	public Mat4 inverse() {
		return new Mat4(this).invert();
	}

	@Override
	public float determinant() {
		return matrix[0].x*(matrix[1].y*(matrix[2].z*matrix[3].w-matrix[3].z*matrix[2].w)
							-matrix[2].y*(matrix[1].z*matrix[3].w-matrix[3].z*matrix[1].w)
							+matrix[3].y*(matrix[1].z*matrix[2].w-matrix[2].z*matrix[1].w))
				
				-matrix[1].x*(matrix[0].y*(matrix[2].z*matrix[3].w-matrix[3].z*matrix[2].w)
							  -matrix[2].y*(matrix[0].z*matrix[3].w-matrix[3].z*matrix[0].w)
							  +matrix[3].y*(matrix[0].z*matrix[2].w-matrix[2].z*matrix[0].w))
						
				+matrix[2].x*(matrix[0].y*(matrix[1].z*matrix[3].w-matrix[3].z*matrix[1].w)
							  -matrix[1].y*(matrix[0].z*matrix[3].w-matrix[3].z*matrix[0].w)
							  +matrix[3].y*(matrix[0].z*matrix[1].w-matrix[1].z*matrix[0].w))
						
				-matrix[3].x*(matrix[0].y*(matrix[1].z*matrix[2].w-matrix[2].z*matrix[1].w)
							  -matrix[1].y*(matrix[0].z*matrix[2].w-matrix[2].z*matrix[0].w)
							  +matrix[2].y*(matrix[0].z*matrix[1].w-matrix[1].z*matrix[0].w));
	}

	@Override
	public Mat4 transpose() {
		Vec4 col1 = new Vec4(matrix[0].x, matrix[1].x,  matrix[2].x, matrix[3].x);
		Vec4 col2 = new Vec4(matrix[0].y, matrix[1].y,  matrix[2].y, matrix[3].y);
		Vec4 col3 = new Vec4(matrix[0].z, matrix[1].z,  matrix[2].z, matrix[3].z);
		Vec4 col4 = new Vec4(matrix[0].w, matrix[1].w,  matrix[2].w, matrix[3].w);
		matrix[0].set(col1);
		matrix[1].set(col2);
		matrix[2].set(col3);
		matrix[3].set(col4);
		return this;
	}

	@Override
	public Mat4 add(Matrix rhs) {
		if(rhs instanceof Mat4){
			Mat4 toAdd = (Mat4)rhs;
			matrix[0].add(toAdd.col(0));
			matrix[1].add(toAdd.col(1));
			matrix[2].add(toAdd.col(2));
			matrix[3].add(toAdd.col(3));
		}else{
			System.err.println("Type mismatch in matrix addition\nrhs must be of type Mat4");
		}
		return this;
	}

	@Override
	public Mat4 subtract(Matrix rhs) {
		if(rhs instanceof Mat4){
			Mat4 toAdd = (Mat4)rhs;
			matrix[0].subtract(toAdd.col(0));
			matrix[1].subtract(toAdd.col(1));
			matrix[2].subtract(toAdd.col(2));
			matrix[3].subtract(toAdd.col(3));
		}else{
			System.err.println("Type mismatch in matrix subtraction\nrhs must be of type Mat4");
		}
		return this;
	}

	@Override
	public Mat4 loadIdentity() {
		matrix[0].set(1.0f, 0.0f, 0.0f, 0.0f);
		matrix[1].set(0.0f, 1.0f, 0.0f, 0.0f);
		matrix[2].set(0.0f, 0.0f, 1.0f, 0.0f);
		matrix[3].set(0.0f, 0.0f, 0.0f, 1.0f);
		return this;
	}

	@Override
	public Mat4 multFactor(float factor) {
		matrix[0].scale(factor);
		matrix[1].scale(factor);
		matrix[2].scale(factor);
		matrix[3].scale(factor);
		return this;
	}

	@Override
	public Vec4 multVec(Vector vec) {
		if(vec instanceof Vec4){
			Vec4 mult = (Vec4)vec;
			Vec4 row1 = new Vec4(matrix[0].x, matrix[1].x,  matrix[2].x, matrix[3].x);
			Vec4 row2 = new Vec4(matrix[0].y, matrix[1].y,  matrix[2].y, matrix[3].y);
			Vec4 row3 = new Vec4(matrix[0].z, matrix[1].z,  matrix[2].z, matrix[3].z);
			Vec4 row4 = new Vec4(matrix[0].w, matrix[1].w,  matrix[2].w, matrix[3].w);
			return new Vec4(row1.dot(mult), row2.dot(mult), row3.dot(mult), row4.dot(mult));
		}else if(vec instanceof Vec3){
			Vec4 mult = new Vec4((Vec3)vec, 1);
			Vec4 row1 = new Vec4(matrix[0].x, matrix[1].x,  matrix[2].x, matrix[3].x);
			Vec4 row2 = new Vec4(matrix[0].y, matrix[1].y,  matrix[2].y, matrix[3].y);
			Vec4 row3 = new Vec4(matrix[0].z, matrix[1].z,  matrix[2].z, matrix[3].z);
			Vec4 row4 = new Vec4(matrix[0].w, matrix[1].w,  matrix[2].w, matrix[3].w);
			return new Vec4(row1.dot(mult), row2.dot(mult), row3.dot(mult), row4.dot(mult));
		}else{
			System.err.println("Type mismatch in matrix vector multiplication\nvec must be of type Vec4 or Vec3");
		}
		return null;
	}

	@Override
	public Mat4 multiply(Matrix rhs) {
		if(rhs instanceof Mat4){
			Mat4 multMat = (Mat4)rhs;
			Vec4 row1 = new Vec4(matrix[0].x, matrix[1].x,  matrix[2].x, matrix[3].x);
			Vec4 row2 = new Vec4(matrix[0].y, matrix[1].y,  matrix[2].y, matrix[3].y);
			Vec4 row3 = new Vec4(matrix[0].z, matrix[1].z,  matrix[2].z, matrix[3].z);
			Vec4 row4 = new Vec4(matrix[0].w, matrix[1].w,  matrix[2].w, matrix[3].w);
			matrix[0].set( row1.dot(multMat.col(0)), row2.dot(multMat.col(0)), row3.dot(multMat.col(0)), row4.dot(multMat.col(0)) );
			matrix[1].set( row1.dot(multMat.col(1)), row2.dot(multMat.col(1)), row3.dot(multMat.col(1)), row4.dot(multMat.col(1)) );
			matrix[2].set( row1.dot(multMat.col(2)), row2.dot(multMat.col(2)), row3.dot(multMat.col(2)), row4.dot(multMat.col(2)) );
			matrix[3].set( row1.dot(multMat.col(3)), row2.dot(multMat.col(3)), row3.dot(multMat.col(3)), row4.dot(multMat.col(3)) );
		}else{
			System.err.println("Type mismatch in matrix multiplication\nrhs must be of type Mat4");
		}
		return this;
	}
	
	@Override
	public Mat4 leftMult(Matrix lhs){
		if(lhs instanceof Mat4){
			Vec4[] lhsCols = ((Mat4)lhs).getMatrix();
			Vec4 row1 = new Vec4(lhsCols[0].x, lhsCols[1].x,  lhsCols[2].x, lhsCols[3].x);
			Vec4 row2 = new Vec4(lhsCols[0].y, lhsCols[1].y,  lhsCols[2].y, lhsCols[3].y);
			Vec4 row3 = new Vec4(lhsCols[0].z, lhsCols[1].z,  lhsCols[2].z, lhsCols[3].z);
			Vec4 row4 = new Vec4(lhsCols[0].w, lhsCols[1].w,  lhsCols[2].w, lhsCols[3].w);
			matrix[0].set( row1.dot(matrix[0]), row2.dot(matrix[0]), row3.dot(matrix[0]), row4.dot(matrix[0]) );
			matrix[1].set( row1.dot(matrix[1]), row2.dot(matrix[1]), row3.dot(matrix[1]), row4.dot(matrix[1]) );
			matrix[2].set( row1.dot(matrix[2]), row2.dot(matrix[2]), row3.dot(matrix[2]), row4.dot(matrix[2]) );
			matrix[3].set( row1.dot(matrix[3]), row2.dot(matrix[3]), row3.dot(matrix[3]), row4.dot(matrix[3]) );
		}else{
			System.err.println("Type mismatch in left matrix multiplication\nlhs must be of type Mat4");
		}
		return this;
	}

	@Override
	public Vec4 col(int index) {
		return index < matrix.length ? matrix[index] : null;
	}

	@Override
	public void setColumn(int index, Vector column) {
		if(index < matrix.length && column instanceof Vec4){
			matrix[index].set(column);
		}else{
			String outOfBounds = "The column being indexed is out of the bounds of this matrix type, the bounds are 0-3";
			String wrongType = "Type mismatch, the vector type used for inserting into this matrix is of type Vec4\n"
					+ "the paramater given does not match this type";
			System.err.println(index < matrix.length ? wrongType : outOfBounds);
		}
	}

	@Override
	public Vec4[] getMatrix() {
		return matrix;
	}

	@Override
	public void setMatrix(Matrix mat) {
		if(mat instanceof Mat4){
			Mat4 copyFrom = (Mat4)mat;
			matrix[0].set(copyFrom.col(0));
			matrix[1].set(copyFrom.col(1));
			matrix[2].set(copyFrom.col(2));
			matrix[3].set(copyFrom.col(3));
		}else{
			System.err.println("Type mismatch in setting matrix\nmat must be of type Mat4");
		}
	}

	@Override
	public void setValueAt(int col, int row, float value) {
		if(col < matrix.length && row < 4){
			matrix[col].set(row,  value);
		}else{
			System.err.println("The requested "+(col < matrix.length ? "row" : "column")+" is out of bounds for this matrix");
		}
	}

	@Override
	public void store(FloatBuffer storage) {
		try {
			matrix[0].store(storage);
			matrix[1].store(storage);
			matrix[2].store(storage);
			matrix[3].store(storage);
		} catch (BufferOverflowException e) {
			System.err.println("Insufficient space in buffer to store matrix");
			e.printStackTrace();
		}
	}

	@Override
	public boolean equals(Object compare){
		if(compare instanceof Mat4){
			Vec4[] mat = ((Mat4)compare).getMatrix();
			return matrix[0] == mat[0] && matrix[1] == mat[1] && matrix[2] == mat[2] && matrix[3] == mat[3];
		}else{
			return false;
		}
	}
	
	@Override
	public FloatBuffer asBuffer(){
		FloatBuffer storage = BufferUtils.createFloatBuffer(SIZE_IN_FLOATS);
		this.store(storage);
		return (FloatBuffer)storage.flip();
	}
	
	@Override
	public String toString(){
		return matrix[0].x+" | "+matrix[1].x+" | "+matrix[2].x+" | "+matrix[3].x+"\n"+
				matrix[0].y+" | "+matrix[1].y+" | "+matrix[2].y+" | "+matrix[3].y+"\n"+
				matrix[0].z+" | "+matrix[1].z+" | "+matrix[2].z+" | "+matrix[3].z+"\n"+
				matrix[0].w+" | "+matrix[1].w+" | "+matrix[2].w+" | "+matrix[3].w+"\n";
	}
	
	@Override
	public void print(){
		System.out.println(toString());
	}
	
	@Override
	public float trace(){
		return matrix[0].x+matrix[1].y+matrix[2].z+matrix[3].w;
	}
	
	@Override
	public void trunc(){
		matrix[0].trunc();
		matrix[1].trunc();
		matrix[2].trunc();
		matrix[3].trunc();
	}

	@Override
	public void orthonormalize(){
		//classical gram-schmidt numerically unstable
//		matrix[1].subtract(matrix[0].proj(matrix[1]));//v2-proj1(v2)
//		matrix[2].subtract(matrix[0].proj(matrix[2])).subtract(matrix[1].proj(matrix[2]));//v3-proj1(v3)-proj2(v3)
//		matrix[3].subtract(matrix[0].proj(matrix[3])).subtract(matrix[1].proj(matrix[3])).subtract(matrix[2].proj(matrix[3]));//v4-proj1(v4)-proj2(v4)-proj3(v4)
//		//normalize
//		matrix[0].normalize();
//		matrix[1].normalize();
//		matrix[2].normalize();
//		matrix[3].normalize();
		
		for(int curVec = 0; curVec < 4; curVec++){
			matrix[curVec].normalize();
			for(int nextVec = curVec+1; nextVec < 4; nextVec++){
				matrix[nextVec].subtract(matrix[nextVec].proj(matrix[curVec]));
			}
		}
	}
}
