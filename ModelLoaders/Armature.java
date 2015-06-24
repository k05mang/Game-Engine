package ModelLoaders;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL20.*;

public class Armature {

	private ArrayList<Bone> skeleton;
	private HashMap<String, Integer> iDs;
	/**
	 * Construct an armature with a single bone as the root bone
	 * with the given name, parent id will automatically be set to -1
	 * and bone id to 0
	 *  
	 * @param rootName name of the root bone of the skeletal armature
	 */
	public Armature(String rootName){
		iDs = new HashMap<String, Integer>();
		skeleton = new ArrayList<Bone>();
		skeleton.add(new Bone(0,-1,rootName));
		iDs.put(rootName, Integer.valueOf(0));
	}

	/**
	 * Adds a bone to the armature 
	 * 
	 * @param id
	 * @param parentID
	 * @param name
	 */
	public void addBone(int id, int parentID, String name){
		skeleton.add(new Bone(id, parentID,name));
		iDs.put(name,  Integer.valueOf(id));
	}

	/**
	 * Creates the bone hierarchy 
	 */
	public void setUpHierarchy(){
		for(Bone structure: skeleton){
			if(structure.parentID != -1){
				skeleton.get(structure.parentID).children.add(structure);
			}
		}
	}

	public void setMatrices(ArrayList<float[][]> mods, boolean animate){
		Bone root = skeleton.get(0);
		if(animate){
			root.setAnimate(mods.get(0));
		}
		else{
			root.setRest(mods.get(0));//set the rest matrix for the root bone
		}
		//iterate over the root bones children and set their matrices
		for(Bone child:root.children){
			modBone(child,mods.get(child.id),mods, animate);
		}
		if(!animate)
			invertRest();
	}
	
	/**
	 * Recursively go through the bone tree and modify the rest matrices for
	 * all the bones in the tree based on the values retrieved from the data file
	 * given as an array list of float matrices
	 * 
	 * @param start root of the subtree to recurse from 
	 * @param modMatrix matrix to modify
	 * @param mods values to modify matrices after applying parent matrices
	 * @param animated indicates whether the method is being used for the rest matrices 
	 * or the animation matrices
	 */
	protected void modBone(Bone start, float[][] modMatrix, ArrayList<float[][]> mods, boolean animated){
		if(animated){
			start.setAnimate(modMatrix);//modify the animation matrix for the starting bone
		}
		else{
			start.setRest(modMatrix);//modify the rest matrix for the starting bone
		}
		//test if the bone is a leaf node in the skeletal tree and end the recursion
		if(start.children.size() != 0){
			//iterate over the starting bones children and modify their matrices
			for(Bone child:start.children){
				modBone(child,mods.get(child.id),mods, animated);//recurse
			}
		}
	}

	/**
	 * 
	 * @param gl
	 * @param shaderProgram
	 */
	public void animate(int shaderProgram){
		int varyLoc = 0, normLoc = 0;
		float[][] zero = new float[4][4];
		FloatBuffer normalBuffer = BufferUtils.createFloatBuffer(9);
		
		FloatBuffer identiity = MatrixUtil.store(MatrixUtil.loadIdentity());
		FloatBuffer identiityNorm = BufferUtils.createFloatBuffer(9).put(new float[]{1,0,0, 0,1,0, 0,0,1});
		identiity.flip();
		identiityNorm.flip();
		for(int i = 0; i < skeleton.size(); i++){
			varyLoc = glGetUniformLocation(shaderProgram, "bones["+i+"]");
			normLoc = glGetUniformLocation(shaderProgram, "normalMatrices["+i+"]");
			//System.out.println(glGetError());
			float[][] anim = skeleton.get(i).getMatrix();
			if(!MatrixUtil.compare(anim, zero)){
				
				normalBuffer.put(MatrixUtil.getNormalMatrix(anim));
				normalBuffer.flip();
				glUniformMatrix4(varyLoc, false, MatrixUtil.store(anim));
				glUniformMatrix3(normLoc, false, normalBuffer);
			}
			else{
				glUniformMatrix4(varyLoc, false, identiity);
				glUniformMatrix3(normLoc, false, identiityNorm);
			}
		}
	}
	
	/**
	 * Inverts the atRest matrix for each bone in the armature
	 */
	public void invertRest(){
		for(Bone invert: skeleton){
			MatrixUtil.invert(invert.atRest);
		}
	}
	
	/**
	 * Gets the size of this armature, which is represented by the number of
	 * bones contained in this armature
	 * 
	 * @return The number of bones contained in this armature
	 */
	public int size(){
		return skeleton.size();
	}
	
	/**
	 * Gets the bone transformation matrix for the bone
	 * specified by the param bone
	 * 
	 * @param bone Name of the bone to retrieve
	 * @return 2D array representing the transformation
	 * 		   matrix of the bone found by the param bone
	 */
	public float[][] getBoneTransMatrix(String bone){
		return skeleton.get(iDs.get(bone).intValue()).animation;
	}
	
	/**
	 * Gets the bone specified by name
	 * 
	 * @param name the name of the bone to retrieve
	 * @return the bone found by the param name, if no bone is
	 * 		   found null is returned
	 */
	public Bone getBone(String name){
		return skeleton.get(iDs.get(name).intValue());
	}
	
	/**
	 * Gets the root bone of this armature
	 * 
	 * @return The root bone of this armature
	 */
	public Bone getRoot(){
		return skeleton.get(0);
	}
	
	/**
	 * Gets the collection of Bone objects that comprise this armature
	 * 
	 * @return Collection of Bones representing this armature
	 */
	public ArrayList<Bone> getBones(){
		return skeleton;
	}

	protected class Bone{

		public int parentID, id;
		public String name;
		public ArrayList<Bone> children;
		public float[][] atRest, animation;

		public Bone(int id, int parentID, String name){
			this.id = id;
			this.parentID = parentID;
			this.name = name;
			children = new ArrayList<Bone>();
		}
		
		public void setRest(float[][] multMatrix){
			if(parentID != -1){
				atRest = MatrixUtil.multMatrices(skeleton.get(parentID).atRest, multMatrix);
				animation = MatrixUtil.multMatrices(skeleton.get(parentID).animation, multMatrix);
			}
			else{
				atRest = multMatrix;
				animation = multMatrix;
			}
		}
		
		public void setAnimate(float[][] animated){
			if(parentID != -1){
				animation = MatrixUtil.multMatrices(skeleton.get(parentID).animation, animated);
			}
			else{
				animation = animated;
			}
		}
		
		public float[][] getMatrix(){
			return MatrixUtil.multMatrices(animation, atRest);
		}
	}
}
