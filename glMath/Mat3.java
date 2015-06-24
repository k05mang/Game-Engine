package glMath;

import java.nio.BufferOverflowException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

public class Mat3 implements Matrix {
	private Vec3[] matrix;
	public static final int SIZE_IN_BYTES = 36;
	public static final int SIZE_IN_FLOATS = 9;

	public Mat3(){
		matrix = new Vec3[3];
		this.loadIdentity();
	}
	
	public Mat3(float diag){
		matrix = new Vec3[3];
		matrix[0] = new Vec3(diag,0,0);
		matrix[1] = new Vec3(0,diag,0);
		matrix[2] = new Vec3(0,0,diag);
	}
	
	public Mat3(Vec3 col1, Vec3 col2, Vec3 col3){
		matrix = new Vec3[3];
		matrix[0] = new Vec3(col1);
		matrix[1] = new Vec3(col2);
		matrix[2] = new Vec3(col3);
	}
	
	public Mat3(Mat3 copy){
		matrix = new Vec3[3];
		Vec3[] copyFrom = copy.getMatrix();
		matrix[0] = new Vec3(copyFrom[0]);
		matrix[1] = new Vec3(copyFrom[1]);
		matrix[2] = new Vec3(copyFrom[2]);
	}
	
	public Mat2 getUpperMatrix(){
		return new Mat2((Vec2)matrix[0].swizzle("xy"), (Vec2)matrix[1].swizzle("xy"));
	}
	
	@Override
	public Mat3 invert() {
		float det = this.determinant();
		if(det != 0){
			Vec3 col1 = new Vec3(
					matrix[1].y*matrix[2].z-matrix[2].y*matrix[1].z,
					matrix[2].y*matrix[0].z-matrix[0].y*matrix[2].z,
					matrix[0].y*matrix[1].z-matrix[1].y*matrix[0].z
					);
			Vec3 col2 =  new Vec3(
					matrix[2].x*matrix[1].z-matrix[1].x*matrix[2].z,
					matrix[0].x*matrix[2].z-matrix[2].x*matrix[0].z,
					matrix[1].x*matrix[0].z-matrix[0].x*matrix[1].z
					);
			Vec3 col3 = new Vec3(
					matrix[1].x*matrix[2].y-matrix[2].x*matrix[1].y,
					matrix[2].x*matrix[0].y-matrix[0].x*matrix[2].y,
					matrix[0].x*matrix[1].y-matrix[1].x*matrix[0].y
					);
			matrix[0].set(col1);
			matrix[1].set(col2);
			matrix[2].set(col3);
			this.multFactor(1.0f/det);
		}else{
			System.err.println("This matrix is not invertible");
		}
		return this;
	}
	
	@Override
	public Mat3 inverse() {
		return new Mat3(this).invert();
	}

	@Override
	public float determinant() {
		return matrix[0].x*(matrix[1].y*matrix[2].z-matrix[2].y*matrix[1].z)
				-matrix[1].x*(matrix[0].y*matrix[2].z-matrix[2].y*matrix[0].z)
				+matrix[2].x*(matrix[0].y*matrix[1].z-matrix[1].y*matrix[0].z);
	}

	@Override
	public Mat3 transpose() {
		Vec3 col1 = new Vec3(matrix[0].x, matrix[1].x,  matrix[2].x);
		Vec3 col2 = new Vec3(matrix[0].y, matrix[1].y,  matrix[2].y);
		Vec3 col3 = new Vec3(matrix[0].z, matrix[1].z,  matrix[2].z);
		matrix[0].set(col1);
		matrix[1].set(col2);
		matrix[2].set(col3);
		return this;
	}

	@Override
	public Mat3 add(Matrix rhs) {
		if(rhs instanceof Mat3){
			Mat3 toAdd = (Mat3)rhs;
			matrix[0].add(toAdd.col(0));
			matrix[1].add(toAdd.col(1));
			matrix[2].add(toAdd.col(2));
		}else{
			System.err.println("Type mismatch in matrix addition\nrhs must be of type Mat3");
		}
		return this;
	}

	@Override
	public Mat3 subtract(Matrix rhs) {
		if(rhs instanceof Mat3){
			Mat3 toAdd = (Mat3)rhs;
			matrix[0].subtract(toAdd.col(0));
			matrix[1].subtract(toAdd.col(1));
			matrix[2].subtract(toAdd.col(2));
		}else{
			System.err.println("Type mismatch in matrix subtraction\nrhs must be of type Mat3");
		}
		return this;
	}

	@Override
	public Mat3 loadIdentity() {
		matrix[0].set(1.0f,0.0f,0.0f);
		matrix[1].set(0.0f,1.0f,0.0f);
		matrix[2].set(0.0f,0.0f,1.0f);
		return this;
	}

	@Override
	public Mat3 multFactor(float factor) {
		matrix[0].scale(factor);
		matrix[1].scale(factor);
		matrix[2].scale(factor);
		return this;
	}

	@Override
	public Vec3 multVec(Vector vec) {
		if(vec instanceof Vec3){
			Vec3 mult = (Vec3)vec;
			Vec3 row1 = new Vec3(matrix[0].x, matrix[1].x, matrix[2].x);
			Vec3 row2 = new Vec3(matrix[0].y, matrix[1].y, matrix[2].y);
			Vec3 row3 = new Vec3(matrix[0].z, matrix[1].z, matrix[2].z);
			return new Vec3(row1.dot(mult), row2.dot(mult), row3.dot(mult));
		}else{
			System.err.println("Type mismatch in matrix vector multiplication\nvec must be of type Vec3");
		}
		return null;
	}

	@Override
	public Mat3 multiply(Matrix rhs) {
		if(rhs instanceof Mat3){
			Mat3 multMat = (Mat3)rhs;
			Vec3 row1 = new Vec3(matrix[0].x, matrix[1].x, matrix[2].x);
			Vec3 row2 = new Vec3(matrix[0].y, matrix[1].y, matrix[2].y);
			Vec3 row3 = new Vec3(matrix[0].z, matrix[1].z, matrix[2].z);
			matrix[0].set( row1.dot(multMat.col(0)), row2.dot(multMat.col(0)), row3.dot(multMat.col(0)) );
			matrix[1].set( row1.dot(multMat.col(1)), row2.dot(multMat.col(1)), row3.dot(multMat.col(1)) );
			matrix[2].set( row1.dot(multMat.col(2)), row2.dot(multMat.col(2)), row3.dot(multMat.col(2)) );
		}else{
			System.err.println("Type mismatch in matrix multiplication\nrhs must be of type Mat3");
		}
		return this;
	}
	
	@Override
	public Mat3 leftMult(Matrix lhs){
		if(lhs instanceof Mat3){
			Vec3[] lhsCols = ((Mat3)lhs).getMatrix();
			Vec3 row1 = new Vec3(lhsCols[0].x, lhsCols[1].x, lhsCols[2].x);
			Vec3 row2 = new Vec3(lhsCols[0].y, lhsCols[1].y, lhsCols[2].y);
			Vec3 row3 = new Vec3(lhsCols[0].z, lhsCols[1].z, lhsCols[2].z);
			matrix[0].set( row1.dot(matrix[0]), row2.dot(matrix[0]), row3.dot(matrix[0]) );
			matrix[1].set( row1.dot(matrix[1]), row2.dot(matrix[1]), row3.dot(matrix[1]) );
			matrix[2].set( row1.dot(matrix[2]), row2.dot(matrix[2]), row3.dot(matrix[2]) );
		}else{
			System.err.println("Type mismatch in left matrix multiplication\nlhs must be of type Mat3");
		}
		return this;
	}

	@Override
	public Vec3 col(int index) {
		return index < matrix.length ? matrix[index] : null;
	}

	@Override
	public void setColumn(int index, Vector column) {
		if(index < matrix.length && column instanceof Vec3){
			matrix[index].set(column);
		}else{
			String outOfBounds = "The column being indexed is out of the bounds of this matrix type, the bounds are 0-2";
			String wrongType = "Type mismatch, the vector type used for inserting into this matrix is of type Vec3\n"
					+ "the paramater given does not match this type";
			System.err.println(index < matrix.length ? wrongType : outOfBounds);
		}
	}

	@Override
	public Vec3[] getMatrix() {
		return matrix;
	}

	@Override
	public void setMatrix(Matrix mat) {
		if(mat instanceof Mat3){
			Mat3 copyFrom = (Mat3)mat;
			matrix[0].set(copyFrom.col(0));
			matrix[1].set(copyFrom.col(1));
			matrix[2].set(copyFrom.col(2));
		}else{
			System.err.println("Type mismatch in setting matrix\nmat must be of type Mat3");
		}
	}

	@Override
	public void setValueAt(int col, int row, float value) {
		if(col < matrix.length && row < 3){
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
		} catch (BufferOverflowException e) {
			System.err.println("Insufficient space in buffer to store matrix");
			e.printStackTrace();
		}
	}

	@Override
	public boolean equals(Object compare){
		if(compare instanceof Mat3){
			Vec3[] mat = ((Mat3)compare).getMatrix();
			return matrix[0] == mat[0] && matrix[1] == mat[1] && matrix[2] == mat[2];
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
		return matrix[0].x+" | "+matrix[1].x+" | "+matrix[2].x+"\n"+
				matrix[0].y+" | "+matrix[1].y+" | "+matrix[2].y+"\n"+
				matrix[0].z+" | "+matrix[1].z+" | "+matrix[2].z+"\n";
	}
	
	@Override
	public void print(){
		System.out.println(toString());
	}
	
	@Override
	public float trace(){
		return matrix[0].x+matrix[1].y+matrix[2].z;
	}
	
	@Override
	public void trunc(){
		matrix[0].trunc();
		matrix[1].trunc();
		matrix[2].trunc();
	}

	@Override
	public void orthonormalize(){
//		matrix[1].subtract(matrix[0].proj(matrix[1]));//v2-proj1(v2)
//		matrix[2].subtract(matrix[0].proj(matrix[2])).subtract(matrix[1].proj(matrix[2]));//v3-proj1(v3)-proj2(v3)
//		//normalize
//		matrix[0].normalize();
//		matrix[1].normalize();
//		matrix[2].normalize();

		for(int curVec = 0; curVec < 3; curVec++){
			matrix[curVec].normalize();
			for(int nextVec = curVec+1; nextVec < 3; nextVec++){
				matrix[nextVec].subtract(matrix[nextVec].proj(matrix[curVec]));
			}
		}
		
		if(matrix[0].isZero()){
			matrix[0].set(matrix[1].cross(matrix[2]));
		}else if(matrix[1].isZero()){
			matrix[1].set(matrix[0].cross(matrix[2]));
		}else if(matrix[2].isZero()){
			matrix[2].set(matrix[0].cross(matrix[1]));
		}
	}
}
