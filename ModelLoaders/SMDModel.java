package ModelLoaders;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.lwjgl.BufferUtils;

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
import primitives.Vertex;
import primitives.Triangle;
import glMath.*;
import renderers.Texture;
import renderers.Renderable;

public class SMDModel extends Renderable{

	private Armature skeleton;
	private ArrayList<Triangle> faces;
	private ArrayList<Vertex> vertices;
	private ArrayList<Material> mats;
	private HashMap<String, Material> material;
	private String name;
	private final int AUXILIARY_DATA_SIZE = 4;
	
	
	private SMDModel(File file, int vAttrib, boolean bufferAdj, ArrayList<Material> material, String matsFile){
		super(vAttrib, vAttrib+1, bufferAdj);
		//create the materials hashmap
		this.material = new HashMap<String, Material>();
		//temporary hashmap to determine the index of the material from the list of materials for this object
		HashMap<Material, Integer> materialIndex = new HashMap<Material, Integer>();
		//if the materials list is null then the create the materail list from the given file name
		//otherwise use the given list for creating the data about the materials
		if(matsFile == null && material == null){
			this.material = new HashMap<String, Material>();
		}else if(material == null){
			//generate the materials from the file
			mats = Material.genMaterials(matsFile);
//			NUMTEXTURES = MATS.SIZE();//store the number of textures for this object
			//loop through each of the materials and store them while also creating an index map
			for(int curMat = 0; curMat < mats.size(); curMat++){
				this.material.put(mats.get(curMat).getDiffuseName(), mats.get(curMat));
				materialIndex.put(mats.get(curMat),  curMat+1);
			}
		}else{
//			NUMTEXTURES = MATERIALS.SIZE();
			mats = material;
			//loop through each of the materials and store them while also creating an index map
			for(int curMat = 0; curMat < material.size(); curMat++){
				this.material.put(material.get(curMat).getDiffuseName(), material.get(curMat));
				materialIndex.put(material.get(curMat),  curMat+1);
			}
		}
		ArrayList<Integer> materialIds = new ArrayList<Integer>();
		//begin parsing the file
		try {
			Scanner smdParser = new Scanner(file);
			faces = new ArrayList<Triangle>();
			vertices = new ArrayList<Vertex>();
			String line, split[];
			smdParser.nextLine();
			smdParser.nextLine();//skip over header
			line = smdParser.nextLine().trim();//skip over "node" header and begin processing root bone
			split = line.split("\\s+");
			line = split[1].replace('"', ' ');
			boolean isImplicit = line.contains("blender_implicit");
			
			if(!isImplicit){
				//skeleton = new Armature(line);
			}else{
				line = smdParser.nextLine().trim();
				/*split = line.split("\\s+");
				skeleton = new Armature(split[1].replace('"', ' '));*/
			}
			
			line = smdParser.nextLine().trim();//point to the second bone
			//add bones to the armature
			while(!line.equals("end")){
				/*split = line.split("\\s+");
				int id = Integer.parseInt(split[0]);
				String name = split[1].replace('"', ' ');
				int parent = Integer.parseInt(split[2]);
				skeleton.addBone((isImplicit ? id-1:id),(isImplicit ? parent-1:parent), name);*/
				line = smdParser.nextLine().trim();
			}
			//skeleton.setUpHierarchy();//construct the hierarchy of bones
			
			/*---create the bone matrices---*/
			smdParser.nextLine();//skip the end for previous section
			smdParser.nextLine();//skip over skeleton header
			line = smdParser.nextLine().trim();//skip over the single frame time stamp
			//start constructing at rest bones
			while(!line.equals("end")){
				line = smdParser.nextLine().trim();
			}
			
			/*---process the vertices---*/
			line = smdParser.nextLine();//skip to the triangles header
			line = smdParser.nextLine();//skip to first triangle material
			HashMap<Vertex, Integer> hashVertex = new HashMap<Vertex, Integer>();
			HashMap<Triangle.Edge, Triangle.HalfEdge> edgesMap = new HashMap<Triangle.Edge, Triangle.HalfEdge>();
			//loop through each block of triangles
			float x, y, z, normX, normY, normZ, u, v, weights[];
			int boneIDs[], links, curIndices[] = new int[3];
			while(!line.equals("end")){
				/*---process the texture---*/
				String texture = line.trim();//get this faces diffuse texture
				//using the diffuse texture name use the materials hashmap to determine if there is a material for it
				//if not set this faces material to the default, default being 0
				Material mat = this.material.get(texture);
				
				//loop through each of the vertices for a triangle
				for(int curVert = 0; curVert < 3; curVert++){
					line = smdParser.nextLine().trim();
					split = line.split("\\s+");
					x = Float.parseFloat(split[1]);
					y = Float.parseFloat(split[2]);
					z = Float.parseFloat(split[3]);
					normX = Float.parseFloat(split[4]);
					normY = Float.parseFloat(split[5]);
					normZ = Float.parseFloat(split[6]);
					u = Float.parseFloat(split[7]);
					v = Float.parseFloat(split[8]);
					links = Integer.parseInt(split[9]);
					boneIDs = new int[links];
					weights = new float[links];
					//gather bone weights and IDs
					for(int boneCount = 0;boneCount < links;boneCount++){
						boneIDs[boneCount] = Integer.parseInt(split[10+2*boneCount]);
						weights[boneCount] = Float.parseFloat(split[11+2*boneCount]);
					}
					//create the vertex
					Vertex vertex = new Vertex(x,y,z, normX,normY,normZ, u,v, boneIDs,weights);
					//using the vertex look up if the vertex has already been processed and retrieve its index
					//otherwise generate a new index for it and mark it in the hashmap
					Integer hashIndex = hashVertex.get(vertex);
					if(hashIndex != null){
						curIndices[curVert] = hashIndex.intValue();
					}
					else{
						vertices.add(vertex);
						hashVertex.put(vertex, vertices.size()-1);
						curIndices[curVert] = vertices.size()-1;
						if (mat != null) {
							materialIds.add(materialIndex.get(mat));
						}else{
							materialIds.add(0);
						}
					}
				}
				Triangle face = new Triangle(
						Integer.valueOf(curIndices[0]),
						Integer.valueOf(curIndices[1]),
						Integer.valueOf(curIndices[2])
						);
				super.setUpTriangle(face, edgesMap);
				faces.add(face);
				line = smdParser.nextLine().trim();
			}
			smdParser.close();
		} catch (IOException e) {
			System.out.println("Failed to load SMD");
			e.printStackTrace();
		}
		name = file.getName().replace(".smd", "");
		
		int vertexSize = AUXILIARY_DATA_SIZE+Vertex.SIZE_OF_BONEDATA+Vertex.SIZE_IN_BYTES;
		ByteBuffer vertexData = BufferUtils.createByteBuffer(vertices.size()*vertexSize);
		IntBuffer indices = BufferUtils.createIntBuffer(faces.size()*(bufferAdj ? Triangle.INDEX_ADJ : Triangle.INDEX_NOADJ));
		
		//since we can't predict the number of vertices that will be part of this mesh we need to store the vertices after we are done 
		//reading them in
		for(int curVert = 0; curVert < vertices.size(); curVert++){
			Vertex curVertex = vertices.get(curVert);
			curVertex.store(vertexData);
			int[] boneIds = curVertex.getIds();
			float[] weights = curVertex.getWeights();
			
			for(int boneData = 0; boneData < 8; boneData++){
				vertexData.putInt(boneIds[boneData]);
			}
			
			for(int boneData = 0; boneData < 8; boneData++){
				vertexData.putFloat(weights[boneData]);
			}
			
			vertexData.putInt(materialIds.get(curVert).intValue());
		}
		
		for(Triangle face : faces){
			face.initAdjacent();
			if(bufferAdj){
				face.storeAllIndices(indices);
			}else{
				face.storePrimitiveIndices(indices);
			}
		}
		
		vertexData.flip();
		indices.flip();
		
		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);
		glVertexAttribPointer(vAttrib, 3, GL_FLOAT, false, vertexSize, 0);
		glVertexAttribPointer(vAttrib+1, 3, GL_FLOAT, false, vertexSize, 12);
		glVertexAttribPointer(vAttrib+2, 2, GL_FLOAT, false, vertexSize, 24);
		
		glVertexAttribPointer(vAttrib+3, 4, GL_FLOAT, false, vertexSize, 32);
		glVertexAttribPointer(vAttrib+4, 4, GL_FLOAT, false, vertexSize, 48);
		
		glVertexAttribPointer(vAttrib+5, 4, GL_INT, false, vertexSize, 64);
		glVertexAttribPointer(vAttrib+6, 4, GL_INT, false, vertexSize, 80);
		
		glVertexAttribPointer(vAttrib+7, 1, GL_INT, false, vertexSize, 96);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}
	
	public SMDModel(File file, int vAttrib, boolean bufferAdj, ArrayList<Material> material){
		this(file, vAttrib, bufferAdj, material, null);
	}
	
	public SMDModel(File file, int vAttrib, boolean bufferAdj, String matsFile){
		this(file, vAttrib, bufferAdj, null, matsFile);
	}
	
	public SMDModel(File file, int vAttrib, boolean bufferAdj){
		this(file, vAttrib, bufferAdj, null, null);
	}
	
	public SMDModel(String filePath, int vAttrib, boolean bufferAdj, ArrayList<Material> material){
		this(new File(filePath), vAttrib, bufferAdj, material, null);
	}
	
	public SMDModel(String filePath, int vAttrib, boolean bufferAdj, String matsFile){
		this(new File(filePath), vAttrib, bufferAdj, null, matsFile);
	}
	
	public SMDModel(String filePath, int vAttrib, boolean bufferAdj){
		this(new File(filePath), vAttrib, bufferAdj, null, null);
	}
	
	public SMDModel(SMDModel copy){
		super(copy);
		//add copy for armature class, we want the armature to be independent for each mesh, thus a copy is needed
		faces = copy.faces;
		vertices = copy.vertices;
		mats = copy.mats;
		material = copy.material;
		name = copy.name;
	}
	
	@Override
	public SMDModel copy(){
		return new SMDModel(this);
	}
	
	@Override
	public ArrayList<Vertex> getVertices(){
		return vertices;
	}
	
	@Override
	public ArrayList<Triangle> getFaces(){
		return faces;
	}

	/**
	 * Gets this objects armature which contains the skeletal hierarchy for this mesh
	 * 
	 * @return Armature containing bone information for this mesh
	 */
	public Armature getSkeleton() {
		return skeleton;
	}

	/**
	 * Loads an animation file from memory for use with this 3D model, the animation file must
	 * be of the .smd format 
	 * 
	 * @param filePath String containing the file path where the .smd animation file is stored
	 */
	public void loadAnimation(String filePath){
		
	}
	
	public void setAnimation(String fileName){
		
	}
	
	/*public void getAnimation(String name){
		
	}*/
	
	
	@Override
	public void render(){
		for(int curMat = 0; curMat < mats.size(); curMat++){
			mats.get(curMat).bind(GL_TEXTURE0+curMat);
		}
		
		glBindVertexArray(vao);
		for(int vertexAttrib = 0; vertexAttrib < 8; vertexAttrib++){
			glEnableVertexAttribArray(vAttrib+vertexAttrib);
		}
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
		glDrawElements((isAdjBuffered ? GL_TRIANGLES_ADJACENCY : GL_TRIANGLES), 
				faces.size()*(isAdjBuffered ? Triangle.INDEX_ADJ : Triangle.INDEX_NOADJ), 
				GL_UNSIGNED_INT, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		for(int vertexAttrib = 0; vertexAttrib < 8; vertexAttrib++){
			glDisableVertexAttribArray(vAttrib+vertexAttrib);
		}
		glBindVertexArray(0);
		
		for(Material mat : mats){
			mat.unbind();
		}
	}
	
	public String getName(){
		return name;
	}
	
	@Override
	public int getNumIndices(){
		return faces.size()*(isAdjBuffered ? Triangle.INDEX_ADJ : Triangle.INDEX_NOADJ);
	}
	
	@Override 
	public void delete(){
		super.delete();
		for(Material mat : mats){
			mat.unbind();
			mat.delete();
		}
	}

	@Override
	public Mat3 computeTensor(float mass) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void write(File file){
		try {
			DataOutputStream stream = new DataOutputStream(new FileOutputStream(file));
			stream.writeShort(vertices.size());
			for(Vertex vert : vertices){
				vert.write(stream);
			}
			stream.writeShort(faces.size()*(this.isAdjBuffered ? Triangle.INDEX_ADJ : Triangle.INDEX_NOADJ));
			for(Triangle face : faces){
				if(this.isAdjBuffered){
					face.writeAll(stream);
				}else{
					face.write(stream);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch(IOException e){
			e.printStackTrace();
			return;
		}
	}
}
