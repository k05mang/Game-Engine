package core;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Random;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL33.*;

public class SierpinskiGasket {

	private int vao3D, vbo3D[], curIteration, num3DInstances, vAttrib, nAttrib, iAttrib;
	private float displayH, red, green , blue, scaleMatrix[][], baseY;
	private Vector3f topVert3D, leftFrontVert, rightFrontVert, midBackVert;
	
	/**
	 * Creates an instance of Sierpinskis' Gasket
	 * 
	 * @param iterations The number of iterations to render
	 * 
	 * @param height The height of the display window
	 * 
	 * @param vertAttrib The attribute that the vertex data will be sent to
	 * 
	 * @param normalAttrib The attribute that the normal data will be sent to
	 * 
	 * @param instanceAttrib The attribute that the instanced data will be sent to
	 */
	public SierpinskiGasket(int iterations, float height, int vertAttrib, int normalAttrib, int instanceAttrib){
		displayH = height;
		
		//store the passed attribute locations for later use
		vAttrib = vertAttrib;
		nAttrib = normalAttrib;
		iAttrib = instanceAttrib;
		
		//generate a random color from 3 float values
		Random rgb = new Random();
		red = rgb.nextFloat();
		green = rgb.nextFloat();
		blue = rgb.nextFloat();
		//store the starting number of iterations
		curIteration = iterations;
		//compute and store the number of instances that will be rendered of the gasket
		num3DInstances = 1 << 2*iterations;
		//compute the factor to which the main tetrahedron will be scaled for the different iterations
		scaleMatrix = MatrixUtil.scale(1f/(1 << iterations));
		
		//construct the data
		setup3D();
		generate3D();
	}
	
	/**
	 * Sets up the initial data for rendering
	 */
	private void setup3D(){
		vbo3D = new int[3];
		float screenHMid = displayH/2f;
		
		//create the first vertex at the top of the tetrahedron that will be used to generate the other vertices
		Vector3f normalTop = new Vector3f( 0, 1, 0);
		topVert3D = new Vector3f(0, screenHMid, 0);
		
		//first rotate the initial point down by 120 degrees this creates the first base vertex
		float[] rotated = MatrixUtil.multMatrices(MatrixUtil.rotateX(120), new float[][]{{0,screenHMid,0,1}})[0];
		
		//store that new vertex and it's normal
		Vector3f normalMB = new Vector3f();
		midBackVert = new Vector3f(rotated[0],rotated[1],rotated[2]);
		baseY = rotated[1];
		midBackVert.normalise(normalMB);
		
		//now repeat the process for the other 2 vertices rotating around the y axis by 120 degrees
		rotated = MatrixUtil.multMatrices(MatrixUtil.rotateY(120), new float[][]{rotated})[0];
		
		Vector3f normalLF = new Vector3f();
		leftFrontVert = new Vector3f(rotated[0],rotated[1],rotated[2]);
		leftFrontVert.normalise(normalLF);
		
		rotated = MatrixUtil.multMatrices(MatrixUtil.rotateY(120), new float[][]{rotated})[0];
		
		Vector3f normalRF = new Vector3f();
		rightFrontVert = new Vector3f(rotated[0],rotated[1],rotated[2]);
		rightFrontVert.normalise(normalRF);
		
		//create the buffer to send the data to the GPU
		FloatBuffer verts = BufferUtils.createFloatBuffer(24);
		
		//store all the vertices and normals in consecutive order in the buffer
		topVert3D.store(verts);
		normalTop.store(verts);
		
		rightFrontVert.store(verts);
		normalRF.store(verts);
		
		leftFrontVert.store(verts);
		normalLF.store(verts);
		
		midBackVert.store(verts);
		normalMB.store(verts);
		
		verts.flip();
		
		//create a buffer for sending the data for element rendering
		ByteBuffer indices = BufferUtils.createByteBuffer(12);
		
		indices.put(new byte[]{0,1,2, 0,2,3, 0,3,1, 1,3,2});
		indices.flip();
		
		//generate the VAO and VBO's
		vao3D = glGenVertexArrays();
		vbo3D[0] = glGenBuffers();
		vbo3D[1] = glGenBuffers();
		
		//setup the VAO and VBO data
		glBindVertexArray(vao3D);
		glBindBuffer(GL_ARRAY_BUFFER,vbo3D[0]);
		glBufferData(GL_ARRAY_BUFFER, verts, GL_STATIC_DRAW);
		glVertexAttribPointer(vAttrib,3, GL_FLOAT, false, 24, 0);
		glVertexAttribPointer(nAttrib,3, GL_FLOAT, false, 24, 12);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
		
		//send the instance data to the right buffer independent of the VAO
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo3D[1]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	/**
	 * Creates the data for the instance rendering
	 */
	public void generate3D(){
		//create the buffer to store the transformation matrices
		FloatBuffer transforms = BufferUtils.createFloatBuffer(16*num3DInstances);
		
		//start the recursion to generate the transform matrices starting with no previous transforms, zero vector
		recurse3D(new Vector3f[]{topVert3D, rightFrontVert, leftFrontVert, midBackVert},
				new Vector3f[]{new Vector3f(0,0,0)},
				transforms, 0);
		
		transforms.flip();
		
		//if we have already created the VBO to store the matrices delete the old one 
		if(vbo3D[2] != 0){
			glBindVertexArray(vao3D);
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glDeleteBuffers(vbo3D[2]);
			glBindVertexArray(0);
		}
		
		//create the VBO for storing the matrices that were computed
		vbo3D[2] = glGenBuffers();
		glBindVertexArray(vao3D);
		glBindBuffer(GL_ARRAY_BUFFER,  vbo3D[2]);
		glBufferData(GL_ARRAY_BUFFER, transforms, GL_STATIC_DRAW);
		glVertexAttribPointer(iAttrib, 4, GL_FLOAT, false, 64, 0);
		glVertexAttribPointer(iAttrib+1, 4, GL_FLOAT, false, 64, 16);
		glVertexAttribPointer(iAttrib+2, 4, GL_FLOAT, false, 64, 32);
		glVertexAttribPointer(iAttrib+3, 4, GL_FLOAT, false, 64, 48);
		//specify how often the instance data will update in the rendering process
		glVertexAttribDivisor(iAttrib, 1);
		glVertexAttribDivisor(iAttrib+1, 1);
		glVertexAttribDivisor(iAttrib+2, 1);
		glVertexAttribDivisor(iAttrib+3, 1);
		glBindBuffer(GL_ARRAY_BUFFER,  0);
		glBindVertexArray(0);
	}
	
	/**
	 * Recurses to create the transformation matrices in the creation of N iterations of the Sierpinski gasket
	 * 
	 * @param tetra Array of Vector3f specifying the vertex data for the current tetrahedron
	 * 
	 * @param prevSum The summed vectors of translation produced through previous iterations of the recursion
	 * 
	 * @param matrixStorage The float buffer that will store all the matrix data
	 * 
	 * @param curIteration The current iteration that the recursion is on
	 */
	public void recurse3D(Vector3f[] tetra, Vector3f[] prevSum, FloatBuffer matrixStorage, int curIteration){
		//check if we are at the iteration N, in which case we stop the recursion and compute and store the matrices 
		if(curIteration == this.curIteration){
			//compute the model matrix of a scale by the computed scale matrix and the translation by the passed in vector
			float[][] result;
			
			//loop through and compute the matrices for each of the tetrahedrons
			for(int i = 0;i < prevSum.length; i++){
				result = MatrixUtil.multMatrices(MatrixUtil.translate(prevSum[i].getX(), prevSum[i].getY(), prevSum[i].getZ()), scaleMatrix);
				
				matrixStorage.put(result[0]);
				matrixStorage.put(result[1]);
				matrixStorage.put(result[2]);
				matrixStorage.put(result[3]);
			}
			
		}else{
			//copy the passed in tetrahedron into separate variables
			Vector3f scaledTop = new Vector3f(tetra[0]);
			Vector3f scaledRF = new Vector3f(tetra[1]);
			Vector3f scaledLF = new Vector3f(tetra[2]);
			Vector3f scaledMB = new Vector3f(tetra[3]);
			
			//scale them to half their size giving use the translation vectors to the center points of where the smaller
			//tetrahedrons will be rendered in the next iteration
			scaledTop.scale(.5f);
			scaledRF.scale(.5f);
			scaledLF.scale(.5f);
			scaledMB.scale(.5f);
			
			//loop through and recurse adding the previous vector sums to the new scaled values 
			for(int i = 0;i < prevSum.length; i++){
				recurse3D(new Vector3f[]{scaledTop, scaledRF, scaledLF, scaledMB},
						new Vector3f[]{Vector3f.add(scaledTop, prevSum[i],null),
						Vector3f.add(scaledRF, prevSum[i],null),
						Vector3f.add(scaledLF, prevSum[i],null),
						Vector3f.add(scaledMB, prevSum[i],null)},
						matrixStorage,curIteration+1);
			}
		}
	}
	
	/**
	 * Set the diffuse of the tetrahedron, the diffuse used is the one computed in the construction of the object
	 * 
	 * @param program Shader program to use, it is expected the shader will have a uniform variable of type vec4 called
	 * diffuse
	 */
	public void setDiffuse(int program){
		glUseProgram(program);
		
		//setup the value for diffuse lighting
		int diffuse = glGetUniformLocation(program,"diffuse");
		glUniform4f(diffuse, red,green,blue,1f);
		
		glUseProgram(0);
	}
	
	/**
	 * Decreases the number of iterations to render
	 */
	public void decrement(){
		if(curIteration != 0){
			curIteration--;
			num3DInstances = 1 << 2*curIteration;
			scaleMatrix = MatrixUtil.scale(1f/(1 << curIteration));
			
			generate3D();
		}
	}
	
	/**
	 * Increases the number of iterations to render
	 */
	public void increment(){
		curIteration++;
		num3DInstances = 1 << 2*curIteration;
		scaleMatrix = MatrixUtil.scale(1f/(1 << curIteration));
		
		generate3D();
	}
	
	public float getBaseY(){
		return baseY;
	}
	
	/**
	 * Renders the Sierpinski tetrahedron
	 * 
	 * @param program The shader program to use in rendering
	 */
	public void render(){
			glBindVertexArray(vao3D);
			glEnableVertexAttribArray(vAttrib);
			glEnableVertexAttribArray(nAttrib);
			for(int i = 0;i < 4; i++){
				glEnableVertexAttribArray(iAttrib+i);
			}
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo3D[1]);
			glDrawElementsInstanced(GL_TRIANGLES, 12, GL_UNSIGNED_BYTE, 0, num3DInstances);
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
			glDisableVertexAttribArray(vAttrib);
			glDisableVertexAttribArray(nAttrib);
			for(int i = 0;i < 4; i++){
				glDisableVertexAttribArray(iAttrib+i);
			}
			glBindVertexArray(0);
	}
	
	/**
	 * Cleans up the GPU memory
	 */
	public void cleanUp(){
		glBindVertexArray(vao3D);
		glDisableVertexAttribArray(vAttrib);
		glDisableVertexAttribArray(nAttrib);
		for(int i = 0;i < 4; i++){
			glDisableVertexAttribArray(iAttrib+i);
		}
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		glDeleteBuffers(vbo3D[0]);
		glDeleteBuffers(vbo3D[1]);
		glDeleteBuffers(vbo3D[2]);
		glDeleteVertexArrays(vao3D);
	}
}
