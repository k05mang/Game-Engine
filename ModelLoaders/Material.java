package ModelLoaders;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL13.*;
import renderers.Texture;

/**
 * Class for storing and using material data for a mesh.
 * 
 * @author Kevin Mango
 *
 */
public class Material {
	private String diffuseName;
	private Texture textures;
	private int numTextures;
	private float specPower, specInten;
	
	/**
	 * Constructs textures using the given data and stores them in this class to be called for future rendering
	 * Data that is passed in as null will be ignored however loc and diffuse cannot be null and must have a value.
	 * The texture created is a 2D array texture, thus all files provided must have the same format and dimensions
	 *  
	 * @param loc Directory where the textures are being kept in memory
	 * @param diffuse File name with extension of the diffuse diffuse texture
	 * @param normal Normal texture file name with extension
	 * @param bump Bump map texture file name with extension
	 * @param specular Specular map texture file name with extension
	 * @param occlusion Occlusion map texture file name with extension
	 */
	public Material(String loc, String diffuse, String normal, String bump, String specular, String occlusion, float specPower, float specIntensity){
		numTextures = 1;
		diffuseName = diffuse;
		ArrayList<String> textureNames = new ArrayList<String>();
		textureNames.add(loc+diffuse);
		
		if(normal != null){
			textureNames.add(loc+normal);
			numTextures++;
		}
		
		if(bump != null){
			textureNames.add(loc+bump);
			numTextures++;
		}
		
		if(specular != null){
			textureNames.add(loc+specular);
			numTextures++;
		}
		
		if(occlusion != null){
			textureNames.add(loc+occlusion);
			numTextures++;
		}
		
		this.specPower = specPower;
		this.specInten = specIntensity;
		
//		textures = Texture.load(Texture._2D_ARRAY, textureNames.toArray(new String[]{}));
	}
	
	/**
	 * Constructs textures using the given data and stores them in this class to be called for future rendering
	 * Data that is passed in as null will be ignored however loc and diffuse cannot be null and must have a value.
	 * The texture created is a 2D array texture, thus all files provided must have the same format and dimensions
	 *  
	 * @param loc Location where the textures are being kept in memory
	 * @param diffuse File name with extension of the diffuse diffuse texture
	 * @param normal Normal texture file name with extension
	 * @param bump Bump map texture file name with extension
	 * @param specular Specular map texture file name with extension
	 * @param specInt Intensity of the specular reflection for this material
	 * @param specPower Power of the specular reflection for this material
	 */
//	public Materials(String loc, String diffuse, String normal, String bump, String specular){
//		this(loc, diffuse, normal, bump, specular, null);
//	}
//	
//	/**
//	 * Constructs textures using the given data and stores them in this class to be called for future rendering
//	 * Data that is passed in as null will be ignored however loc and diffuse cannot be null and must have a value.
//	 * The texture created is a 2D array texture, thus all files provided must have the same format and dimensions
//	 *  
//	 * @param loc Location where the textures are being kept in memory
//	 * @param diffuse File name with extension of the diffuse diffuse texture
//	 * @param normal Normal texture file name with extension
//	 * @param bump Bump map texture file name with extension
//	 * @param specInt Intensity of the specular reflection for this material
//	 * @param specPower Power of the specular reflection for this material
//	 */
//	public Materials(String loc, String diffuse, String normal, String bump){
//		this(loc, diffuse, normal, bump, null, null);
//	}
//
//	/**
//	 * Constructs textures using the given data and stores them in this class to be called for future rendering
//	 * Data that is passed in as null will be ignored however loc and diffuse cannot be null and must have a value.
//	 * The texture created is a 2D array texture, thus all files provided must have the same format and dimensions
//	 *  
//	 * @param loc Location where the textures are being kept in memory
//	 * @param diffuse File name with extension of the diffuse diffuse texture
//	 * @param normal Normal texture file name with extension
//	 * @param specInt Intensity of the specular reflection for this material
//	 * @param specPower Power of the specular reflection for this material
//	 */
//	public Materials(String loc, String diffuse, String normal){
//		this(loc, diffuse, normal, null, null, null);
//	}
//
//	/**
//	 * Constructs textures using the given data and stores them in this class to be called for future rendering
//	 * Data that is passed in as null will be ignored however loc and diffuse cannot be null and must have a value.
//	 * The texture created is a 2D array texture, thus all files provided must have the same format and dimensions
//	 *  
//	 * @param loc Location where the textures are being kept in memory
//	 * @param diffuse File name with extension of the diffuse diffuse texture
//	 * @param specInt Intensity of the specular reflection for this material
//	 * @param specPower Power of the specular reflection for this material
//	 */
//	public Materials(String loc, String diffuse){
//		this(loc, diffuse, null, null, null, null);
//	}
	
	@Override
	public boolean equals(Object o){
		if(o!= null && o instanceof Material){
			Material equality = (Material) o;
			return diffuseName.equals(equality.getDiffuseName());
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		return textures.hashCode()+diffuseName.hashCode();
	}
	
	public int getNumTextures(){
		return numTextures;
	}
	
	/**
	 * Gets the file name of the diffuse texture
	 * 
	 * @return Name of the diffuse texture
	 */
	public String getDiffuseName(){
		return diffuseName;
	}
	
	/**
	 * Binds this materials texture array for use with the given texture unit
	 * If the texture unit given is negative then GL_TEXTURE0 will be used instead
	 * 
	 * @param texUnits Texture unit to bind this materials texture to
	 */
	public void bind(int texUnit){
		if(texUnit < 0){
			textures.bind(texUnit);
		}else{
			textures.bind(GL_TEXTURE0);
		}
	}
	
	/**
	 * Binds this materials texture array for use
	 */
	public void bind(){
		textures.bind();
	}
	
	/**
	 * Unbinds this materials texture array
	 */
	public void unbind(){
		textures.unbind();
	}
	
	/**
	 * Deletes the textures associated with this material object from the graphics card
	 */
	public void delete(){
		textures.delete();
	}
	
	/**
	 * Generates a list of materials from a materials file using the string file as the path to the 
	 * files location
	 * 
	 * @param file File path to the files location, as well as the file name
	 * @return ArrayList of Material objects as read from the material file
	 */
	public static ArrayList<Material> genMaterials(String file){
		return genMaterials(new File(file));
	}
	
	/**
	 * Generates a list of materials from a materials file using the string file as the path to the 
	 * files location
	 * 
	 * @param file File object representing the file to be parsed
	 * @return ArrayList of Material objects as read from the material file
	 */
	public static ArrayList<Material> genMaterials(File matFile){
		ArrayList<Material> mats = null;
		try{
			Scanner parser = new Scanner(matFile);
			mats = new ArrayList<Material>();
			while(parser.hasNextLine()){
				//strings for each of the various textures listed in the file
				String diffuse = null,
				normal = null,
				bump = null,
				specular = null,
				occlusion = null;
				float specPower = 0, specIntensity = 0;
				//the first line of the file will indicate where all the textures are being kept for that material
				String imageLoc = parser.nextLine().trim();
				String line = "";
				while(parser.hasNextLine() && !line.equals("&")){
					line = parser.nextLine();
					String[] data = line.split("(\\s|\\t)*;(\\s|\\t)*");
					for(int group = 0; group < data.length; group++){
						String[] pair = data[group].split("(\\s|\\t)*:(\\s|\\t)*");
						
						if(pair.length > 1){
							switch (pair[0].trim().toLowerCase()) {
								case "diffuse":
									diffuse = pair[1].trim();
									break;
								case "normal":
									normal = pair[1].trim();
									break;
								case "bump":
									bump = pair[1].trim();
									break;
								case "specular":
									specular = pair[1].trim();
									break;
								case "occlusion":
									occlusion = pair[1].trim();
									break;
								case "specular power":
									specPower = Float.parseFloat(pair[1].trim());
									break;
								case "specular intensity":
									specIntensity = Float.parseFloat(pair[1].trim());
									break;
								default:
									break;
							}
						}
					}
				}
				
				if(diffuse != null){
					mats.add(new Material(imageLoc, diffuse, normal, bump,
							specular, occlusion, specPower, specIntensity));
				}
			}
			parser.close();
		}catch(IOException e){
			System.err.println("Failed to load materials file");
			e.printStackTrace();
			return null;
		}
		return mats;
	}
}
