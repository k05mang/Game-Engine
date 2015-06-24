package glMath;

import java.nio.BufferOverflowException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

public class Mat2 implements Matrix {
	
	private Vec2[] matrix;
	public static final int SIZE_IN_BYTES = 16;
	public static final int SIZE_IN_FLOATS = 4;

	/**
	 * Constructs a 2x2 matrix, where the default value is the identity matrix 
	 */
	public Mat2(){
		matrix = new Vec2[2];
		this.loadIdentity();
	}
	
	/**
	 * Constructs a 2x2 matrix with a single float value for the diagonal of this matrix
	 * 
	 * @param diag Value to set the diagonal to
	 */
	public Mat2(float diag){
		matrix = new Vec2[2];
		matrix[0] = new Vec2(diag,0);
		matrix[1] = new Vec2(0,diag);
	}
	
	public Mat2(Vec2 col1, Vec2 col2){
		matrix = new Vec2[2];
		matrix[0] = new Vec2(col1);
		matrix[1] = new Vec2(col2);
	}
	
	public Mat2(Mat2 copy){
		matrix = new Vec2[2];
		Vec2[] copyFrom = copy.getMatrix();
		matrix[0] = new Vec2(copyFrom[0]);
		matrix[1] = new Vec2(copyFrom[1]);
	}
	
	@Override
	public Mat2 invert() {
		float det = this.determinant();
		if(det != 0){
			Vec2 col1 = new Vec2(matrix[1].y, -matrix[0].y);
			Vec2 col2 = new Vec2(-matrix[1].x, matrix[0].x);
			matrix[0].set(col1);
			matrix[1].set(col2);
			this.multFactor(1.0f/det);
		}else{
			System.err.println("This matrix is not invertible");
		}
		return this;
	}
	
	@Override
	public Mat2 inverse() {
		return new Mat2(this).invert();
	}

	@Override
	public float determinant() {
		return matrix[0].x*matrix[1].y-matrix[1].x*matrix[0].y;
	}

	@Override
	public Mat2 transpose() {
		Vec2 col1 = new Vec2(matrix[0].x, matrix[1].x);
		Vec2 col2 = new Vec2(matrix[0].y, matrix[1].y);
		matrix[0].set(col1);
		matrix[1].set(col2);
		return this;
	}

	@Override
	public Mat2 add(Matrix rhs) {
		if(rhs instanceof Mat2){
			Mat2 toAdd = (Mat2)rhs;
			matrix[0].add(toAdd.col(0));
			matrix[1].add(toAdd.col(1));
		}else{
			System.err.println("Type mismatch in matrix addition\nrhs must be of type Mat2");
		}
		return this;
	}

	@Override
	public Mat2 subtract(Matrix rhs) {
		if(rhs instanceof Mat2){
			Mat2 sub = (Mat2)rhs;
			matrix[0].subtract(sub.col(0));
			matrix[1].subtract(sub.col(1));
		}else{
			System.err.println("Type mismatch in matrix subtraction\nrhs must be of type Mat2");
		}
		return this;
	}
	
	@Override
	public Mat2 loadIdentity(){
		matrix[0].set(1.0f,0.0f);
		matrix[1].set(0.0f,1.0f);
		return this;
	}
	
	@Override
	public Mat2 multFactor(float factor){
		matrix[0].scale(factor);
		matrix[1].scale(factor);
		return this;
	}
	
	@Override
	public Vec2 multVec(Vector vec){
		if(vec instanceof Vec2){
			Vec2 mult = (Vec2)vec;
			Vec2 row1 = new Vec2(matrix[0].x, matrix[1].x);
			Vec2 row2 = new Vec2(matrix[0].y, matrix[1].y);
			return new Vec2(row1.dot(mult), row2.dot(mult));
		}else{
			System.err.println("Type mismatch in matrix vector multiplication\nvec must be of type Vec2");
			return null;
		}
	}
	
	@Override
	public Mat2 multiply(Matrix rhs){
		if(rhs instanceof Mat2){
			Mat2 mult = (Mat2)rhs;
			Vec2 row1 = new Vec2(matrix[0].x, matrix[1].x);
			Vec2 row2 = new Vec2(matrix[0].y, matrix[1].y);
			matrix[0].set( row1.dot(mult.col(0)), row2.dot(mult.col(0)) );
			matrix[1].set( row1.dot(mult.col(1)), row2.dot(mult.col(1)) );
		}else{
			System.err.println("Type mismatch in matrix multiplication\nrhs must be of type Mat2");
		}
		return this;
	}
	
	@Override
	public Mat2 leftMult(Matrix lhs){
		if(lhs instanceof Mat2){
			Vec2[] lhsCols = ((Mat2)lhs).getMatrix();
			Vec2 row1 = new Vec2(lhsCols[0].x, lhsCols[1].x);
			Vec2 row2 = new Vec2(lhsCols[0].y, lhsCols[1].y);
			matrix[0].set( row1.dot(matrix[0]), row2.dot(matrix[0]) );
			matrix[1].set( row1.dot(matrix[1]), row2.dot(matrix[1]) );
		}else{
			System.err.println("Type mismatch in left matrix multiplication\nlhs must be of type Mat2");
		}
		return this;
	}
	
	@Override
	public Vec2 col(int index){
		return index < matrix.length ? matrix[index] : null;
	}
	
	@Override
	public void setColumn(int index, Vector column){
		if(index < matrix.length && column instanceof Vec2){
			matrix[index].set(column);
		}else{
			String outOfBounds = "The column being indexed is out of the bounds of this matrix type, the bounds are 0-1";
			String wrongType = "Type mismatch, the vector type used for inserting into this matrix is of type Vec2\n"
					+ "the paramater given does not match this type";
			System.err.println(index < matrix.length ? wrongType : outOfBounds);
		}
	}
	
	@Override
	public Vec2[] getMatrix(){
		return matrix;
	}
	
	@Override
	public void setMatrix(Matrix mat){
		if(mat instanceof Mat2){
			Mat2 copyFrom = (Mat2)mat;
			matrix[0].set(copyFrom.col(0));
			matrix[1].set(copyFrom.col(1));
		}else{
			System.err.println("Type mismatch in setting matrix\nmat must be of type Mat2");
		}
	}
	
	@Override
	public void setValueAt(int col, int row, float value){
		if(col < matrix.length && row < 2){
			matrix[col].set(row,  value);
		}else{
			System.err.println("The requested "+(col < matrix.length ? "row" : "column")+" is out of bounds for this matrix");
		}
	}
	
	@Override
	public void store(FloatBuffer storage){
		try {
			matrix[0].store(storage);
			matrix[1].store(storage);
		} catch (BufferOverflowException e) {
			System.err.println("Insufficient space in buffer to store matrix");
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean equals(Object compare){
		if(compare instanceof Mat2){
			Vec2[] mat = ((Mat2)compare).getMatrix();
			return matrix[0] == mat[0] && matrix[1] == mat[1];
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
		return matrix[0].x+" | "+matrix[1].x+"\n"+
				matrix[0].y+" | "+matrix[1].y+"\n";
	}
	
	@Override
	public void print(){
		System.out.println(toString());
	}
	
	@Override
	public float trace(){
		return matrix[0].x+matrix[1].y;
	}
	
	@Override
	public void trunc(){
		matrix[0].trunc();
		matrix[1].trunc();
	}

	@Override
	public void orthonormalize(){
//		matrix[1].subtract(matrix[0].proj(matrix[1]));//v2-proj1(v2)
//		//normalize
//		matrix[0].normalize();
//		matrix[1].normalize();

		for(int curVec = 0; curVec < 2; curVec++){
			matrix[curVec].normalize();
			for(int nextVec = curVec+1; nextVec < 2; nextVec++){
				matrix[nextVec].subtract(matrix[nextVec].proj(matrix[curVec]));
			}
		}
	}
}
