package renderers;
import glMath.*;

import java.util.ArrayList;
import java.util.Iterator;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.GL_TRIANGLES_ADJACENCY;
import static org.lwjgl.opengl.GL33.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import primitives.Triangle;

public class InstanceRenderer {
	private Renderable mesh;
	private ArrayList<Mat4> instanceMatrices;
	private int iVbo, iNVbo, numIndices, numInstances, iAttrib, iNAttrib, spaceInBuffer;
	private static final int BASE_INSTANCE_BUFFER_SIZE = 50;
	
	public InstanceRenderer(Renderable mesh, int iAttrib, int iNAttrib){
		this.mesh = mesh;
		this.iAttrib = iAttrib;
		this.iNAttrib = iNAttrib;
		instanceMatrices = new ArrayList<Mat4>(BASE_INSTANCE_BUFFER_SIZE);
		numIndices = mesh.getNumIndices();
		numInstances = 0;
		spaceInBuffer = BASE_INSTANCE_BUFFER_SIZE;
		
		FloatBuffer instanceBuffer = BufferUtils.createFloatBuffer(BASE_INSTANCE_BUFFER_SIZE*Mat4.SIZE_IN_FLOATS);
		FloatBuffer normalMatrices = BufferUtils.createFloatBuffer(BASE_INSTANCE_BUFFER_SIZE*Mat3.SIZE_IN_FLOATS);
		
		glBindVertexArray(mesh.vao);
		iVbo = glGenBuffers();
		iNVbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, iVbo);
		glBufferData(GL_ARRAY_BUFFER, instanceBuffer, GL_DYNAMIC_DRAW);
		for(int attr = 0; attr < 4; attr++){
			glVertexAttribPointer(iAttrib+attr, Vec4.SIZE_IN_FLOATS, GL_FLOAT, false, Mat4.SIZE_IN_BYTES, attr*Vec4.SIZE_IN_BYTES);
			glVertexAttribDivisor(iAttrib+attr, 1);
		}
		glBindBuffer(GL_ARRAY_BUFFER, iNVbo);
		glBufferData(GL_ARRAY_BUFFER, normalMatrices, GL_DYNAMIC_DRAW);
		for(int attr = 0; attr < 4; attr++){
			glVertexAttribPointer(iNAttrib+attr, Vec3.SIZE_IN_FLOATS, GL_FLOAT, false, Mat3.SIZE_IN_BYTES, attr*Vec3.SIZE_IN_BYTES);
			glVertexAttribDivisor(iNAttrib+attr, 1);
		}
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}
	
	public InstanceRenderer(Renderable mesh, ArrayList<Mat4> data, int iAttrib, int iNAttrib){
		this.mesh = mesh;
		this.iAttrib = iAttrib;
		this.iNAttrib = iNAttrib;
		instanceMatrices = data;
		numIndices = mesh.getNumIndices();
		numInstances = data.size();
		spaceInBuffer = 0;
		
		FloatBuffer instanceBuffer = BufferUtils.createFloatBuffer(data.size()*Mat4.SIZE_IN_FLOATS);
		FloatBuffer normalMatrices = BufferUtils.createFloatBuffer(data.size()*Mat3.SIZE_IN_FLOATS);
		for(Mat4 matrix : data){
			matrix.store(instanceBuffer);
			(matrix.getNormalMatrix()).store(normalMatrices);
		}
		instanceBuffer.flip();
		normalMatrices.flip();
		
		glBindVertexArray(mesh.vao);
		iVbo = glGenBuffers();
		iNVbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, iVbo);
		glBufferData(GL_ARRAY_BUFFER, instanceBuffer, GL_DYNAMIC_DRAW);
		for(int attr = 0; attr < 4; attr++){
			glVertexAttribPointer(iAttrib+attr, Vec4.SIZE_IN_FLOATS, GL_FLOAT, false, Mat4.SIZE_IN_BYTES, attr*Vec4.SIZE_IN_BYTES);
			glVertexAttribDivisor(iAttrib+attr, 1);
		}
		glBindBuffer(GL_ARRAY_BUFFER, iNVbo);
		glBufferData(GL_ARRAY_BUFFER, normalMatrices, GL_DYNAMIC_DRAW);
		for(int attr = 0; attr < 4; attr++){
			glVertexAttribPointer(iNAttrib+attr, Vec3.SIZE_IN_FLOATS, GL_FLOAT, false, Mat3.SIZE_IN_BYTES, attr*Vec3.SIZE_IN_BYTES);
			glVertexAttribDivisor(iNAttrib+attr, 1);
		}
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}
	
	public void addInstance(Mat4 matrix){
		addInstances(matrix);
	}
	
	public void addInstances(Mat4... matrices){
		if(matrices.length > 0){
			numInstances += matrices.length;
			if (matrices.length > spaceInBuffer) {
				for(Mat4 matrix : matrices){
					instanceMatrices.add(matrix);
				}
				
				FloatBuffer instanceBuffer = BufferUtils.createFloatBuffer(instanceMatrices.size()*Mat4.SIZE_IN_FLOATS);
				FloatBuffer normalMatrices = BufferUtils.createFloatBuffer(instanceMatrices.size()*Mat3.SIZE_IN_FLOATS);
				for(Mat4 mat : instanceMatrices){
					mat.store(instanceBuffer);
					(mat.getNormalMatrix()).store(normalMatrices);
				}
				instanceBuffer.flip();
				normalMatrices.flip();
				
				glBindVertexArray(mesh.vao);
				glBindBuffer(GL_ARRAY_BUFFER, iVbo);
				glBufferData(GL_ARRAY_BUFFER, instanceBuffer, GL_DYNAMIC_DRAW);
				glBindBuffer(GL_ARRAY_BUFFER, iNVbo);
				glBufferData(GL_ARRAY_BUFFER, normalMatrices, GL_DYNAMIC_DRAW);
				glBindBuffer(GL_ARRAY_BUFFER, 0);
				glBindVertexArray(0);
				
				spaceInBuffer = 0;
			}else{
				//possibly change this to have additional empty storage so future calls would use buffersubdata
				FloatBuffer newData = BufferUtils.createFloatBuffer(matrices.length*Mat4.SIZE_IN_FLOATS);
				FloatBuffer newNMatrices = BufferUtils.createFloatBuffer(matrices.length*Mat3.SIZE_IN_FLOATS);
				for(int curIndex = 0; curIndex < matrices.length; curIndex++){
					matrices[curIndex].store(newData);
					(matrices[curIndex].getNormalMatrix()).store(newNMatrices);
					instanceMatrices.add(matrices[curIndex]);
				}
				newData.flip();
				newNMatrices.flip();
				
				glBindVertexArray(mesh.vao);
				glBindBuffer(GL_ARRAY_BUFFER, iVbo);
				glBufferSubData(GL_ARRAY_BUFFER, (instanceMatrices.size()-matrices.length)*Mat4.SIZE_IN_BYTES, newData);
				glBindBuffer(GL_ARRAY_BUFFER, iNVbo);
				glBufferSubData(GL_ARRAY_BUFFER, (instanceMatrices.size()-matrices.length)*Mat3.SIZE_IN_BYTES, newNMatrices);
				glBindBuffer(GL_ARRAY_BUFFER, 0);
				glBindVertexArray(0);
				spaceInBuffer -= matrices.length;
			}
		}
	}
	
	public Mat4 removeInstance(int index){
		ArrayList<Mat4> removed = removeInstances(index);
		return removed.get(0);
	}
	
	public ArrayList<Mat4> removeInstances(int... indices){
		if(indices.length > 0){
			ArrayList<Mat4> removed = new ArrayList<Mat4>();
			int lowestIndex = indices[0], ensuredCpacity = instanceMatrices.size()+spaceInBuffer;
			for(int index : indices){
				if(index > -1 && numInstances > 0 &&  index < instanceMatrices.size()){
					if(index < lowestIndex)
						lowestIndex = index;
					Mat4 remove = instanceMatrices.get(index);
					if(remove != null){
						removed.add(remove);
						instanceMatrices.set(index, null);
						numInstances--;
						spaceInBuffer++;
					}
				}
			}
			
			Iterator<Mat4> removal = instanceMatrices.iterator();
			while(removal.hasNext()){
				if(removal.next() == null){
					removal.remove();
				}
			}
			instanceMatrices.ensureCapacity(ensuredCpacity);
			
			if(!removed.isEmpty()){
				FloatBuffer shiftedData = BufferUtils.createFloatBuffer((instanceMatrices.size() - lowestIndex)*Mat4.SIZE_IN_FLOATS);
				FloatBuffer shiftedNormals = BufferUtils.createFloatBuffer((instanceMatrices.size() - lowestIndex)*Mat3.SIZE_IN_FLOATS);
				for (int curIndex = lowestIndex; curIndex < instanceMatrices.size(); curIndex++) {
					instanceMatrices.get(curIndex).store(shiftedData);
					(instanceMatrices.get(curIndex).getNormalMatrix()).store(shiftedNormals);
				}
				shiftedData.flip();
				shiftedNormals.flip();
				
				glBindVertexArray(mesh.vao);
				glBindBuffer(GL_ARRAY_BUFFER, iVbo);
				glBufferSubData(GL_ARRAY_BUFFER, lowestIndex*Mat4.SIZE_IN_BYTES, shiftedData);
				glBindBuffer(GL_ARRAY_BUFFER, iNVbo);
				glBufferSubData(GL_ARRAY_BUFFER, lowestIndex*Mat3.SIZE_IN_BYTES, shiftedNormals);
				glBindBuffer(GL_ARRAY_BUFFER, 0);
				glBindVertexArray(0);
			}
			return removed;
		}else{
			return null;
		}
	}
	
	public void setData(int index, Mat4... matrices){
		if(index > -1 && index+matrices.length-1 < instanceMatrices.size() && matrices.length > 0){
			FloatBuffer newData = BufferUtils.createFloatBuffer(matrices.length*Mat4.SIZE_IN_FLOATS);
			FloatBuffer newNormals = BufferUtils.createFloatBuffer(matrices.length*Mat3.SIZE_IN_FLOATS);
			for(int curIndex = 0; curIndex < matrices.length; curIndex++){
				matrices[curIndex].store(newData);
				(matrices[curIndex].getNormalMatrix()).store(newNormals);
				instanceMatrices.set(index+curIndex, matrices[curIndex]);
			}
			newData.flip();
			newNormals.flip();
			
			glBindVertexArray(mesh.vao);
			glBindBuffer(GL_ARRAY_BUFFER, iVbo);
			glBufferSubData(GL_ARRAY_BUFFER, index*Mat4.SIZE_IN_BYTES, newData);
			glBindBuffer(GL_ARRAY_BUFFER, iNVbo);
			glBufferSubData(GL_ARRAY_BUFFER, index*Mat3.SIZE_IN_BYTES, newNormals);
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glBindVertexArray(0);
		}
	}
	
	public ArrayList<Mat4> getData(){
		return instanceMatrices;
	}
	
	public Mat4 getMatrix(int index){
		if(index > -1 && index < instanceMatrices.size())
			return instanceMatrices.get(index);
		else
			return null;
	}
	
	public int numInstance(){
		return numInstances;
	}
	
	public void render(int numInstances){
		glBindVertexArray(mesh.vao);
		glEnableVertexAttribArray(mesh.vAttrib);
		glEnableVertexAttribArray(mesh.nAttrib);
		for(int attr = 0; attr < 4; attr++){
			glEnableVertexAttribArray(iAttrib+attr);
		}
		for(int attr = 0; attr < 4; attr++){
			glEnableVertexAttribArray(iNAttrib+attr);
		}
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mesh.ibo);
		glDrawElementsInstanced((mesh.isAdjBuffered ? GL_TRIANGLES_ADJACENCY : GL_TRIANGLES), numIndices, 
				GL_UNSIGNED_INT, 0, (numInstances < 1 ? this.numInstances : numInstances));
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		glDisableVertexAttribArray(mesh.vAttrib);
		glDisableVertexAttribArray(mesh.nAttrib);
		for(int attr = 0; attr < 4; attr++){
			glDisableVertexAttribArray(iAttrib+attr);
		}for(int attr = 0; attr < 4; attr++){
			glDisableVertexAttribArray(iNAttrib+attr);
		}
		glBindVertexArray(0);
	}
	
	public void render(){
		this.render(numInstances);
	}
	
	public void delete(){
		mesh.delete();
		glDeleteBuffers(iVbo);
		glDeleteBuffers(iNVbo);
	}
}
