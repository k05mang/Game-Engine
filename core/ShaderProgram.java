package core;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class ShaderProgram {
	private HashMap<String, Uniform> uniforms;
	private int programId;
	private String space1plus = "(\\s+|\\t+)+";
	private String space0plus = "(\\s*|\\t*)*";
	private String initializers = "(\\((\\d+|\\d+\\.\\d+f?|\\w|\\-|\\*|/|\\+|,|\\(|\\)|"+space0plus+")*\\)";

	public ShaderProgram(String[] shaderNames, int[] shaderTypes){
		uniforms = new HashMap<String, Uniform>();
		
		if(shaderNames.length == shaderTypes.length){
			int[] programObjects = new int[shaderNames.length];
			programId = glCreateProgram();
			
			for (int shaders = 0; shaders < programObjects.length; shaders++) {
				programObjects[shaders] = createShader(
						shaderNames[shaders], shaderTypes[shaders]);
				glAttachShader(programId, programObjects[shaders]);
			}
			glLinkProgram(programId);
			
			//check for faults in the shader program
			if(glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE){
				String infoLog = glGetProgramInfoLog(programId, GL_INFO_LOG_LENGTH);
				
				for(int shader = 0; shader < programObjects.length;shader++){
					glDetachShader(programId, programObjects[shader]);
					glDeleteShader(programObjects[shader]);
				}
				glDeleteProgram(programId);
				
				System.err.println("Failed to link program");
				System.err.println(infoLog);
			}else{
				for (int detach = 0; detach < programObjects.length; detach++) {
					glDetachShader(programId, programObjects[detach]);
					glDeleteShader(programObjects[detach]);
				}
				
				for(String varName: uniforms.keySet()){
					uniforms.get(varName).setLoc(glGetUniformLocation(programId, varName));
				}
			}
		}
	}
	
	public int get(String retrieve){
		return uniforms.containsKey(retrieve) ? uniforms.get(retrieve).getLoc():-1;
	}
	
	public void bind(){
		glUseProgram(programId);
	}
	
	public void unbind(){
		glUseProgram(0);
	}
	
	/**
	 * Generates a shader object, reads the source code for a shader, and compiles the shader to be used
	 * in the rendering pipeline
	 * 
	 * @param fileName String specifying the file path to the file containing the shader code
	 * 
	 * @param shaderType The type of shader to be generated, this must be one of the types specified
	 * in the openGL specification, that is GL_VERTEX_SHADER or GL_FRAGMENT_SHADER
	 * 
	 * @param gl OpenGL graphics context with which to work with shaders
	 * 
	 * @return Returns an int representing the handle for the generated shader object
	 */
	protected int createShader(String fileName, int shaderType){
		int shaderID = 0;
		StringBuilder source = new StringBuilder();
		
		/*scan the file taking each line and adding it to the source array list and the lengths of each line to the 
		Integer array list*/
		try{
			Scanner shaderParse = new Scanner(new File(fileName));
			HashMap<String, ShaderStruct> structs = new HashMap<String, ShaderStruct>();
			String line;
			//set of regular expressions to use in determining information about the shader
			String uniform = "uniform";//due to constant usage, make a universal object for it
			String struct = "struct";
			/*String glslTypes = "float|int|uint|bool|mat2|mat2x2|mat2x3|mat2x4|mat3|mat3x3|mat3x2|mat3x4|mat4|mat4x4|mat4x2|mat4x3|"+
			"vec2|uvec2|ivec2|bvec2|vec3|uvec3|ivec3|bvec3|vec4|uvec4|ivec4|bvec4|\\w*sampler\\w*";*/
			int numAnon = 0;
			String anon = "anonStruct";
			
			while(shaderParse.hasNextLine()){
				line = shaderParse.nextLine();
				//add source code line to stringBuilder
				source.append(line+"\n");
				//process potential uniforms
				if(line.contains(uniform)){
					if(line.contains(struct)){
						StringBuilder structInfo = new StringBuilder(line);
						//continue reading the shader storing additional information about the structure in the string builder
						//while also updating the line variable and the source string builder
						while(shaderParse.hasNextLine() && !line.contains("}")){
							line = shaderParse.nextLine();
							structInfo.append(line);
							source.append(line+"\n");
						}
						//convert the string builder to a string for processing
						String structBody = structInfo.toString();
						int left = structBody.indexOf(struct);
						int right = structBody.indexOf('}');
						//get the substring starting after the keyword struct and before the } brace
						//then split on { dividing the data into the name and the member list of the struct
						String[] structData = structBody.substring(left+struct.length(), right).split(space0plus+"\\{"+space0plus);
						//store the name of the structure
						String structName = structData[0].trim();
						if(structName.length() == 0){
							structName = anon+numAnon;
							numAnon++;
						}
						//generate a structure for use
						genStruct(structName, structData[1], structs);
						//determine if potential instance names have already been gathered into the current string
						if(!line.contains(";")){//if they haven't
							//then continue processing the shader file until the end delimited is found
							//updating the proper variables as necessary
							while(shaderParse.hasNextLine() && !line.contains(";")){
								line = shaderParse.nextLine();
								structInfo.append(line);
								source.append(line+"\n");
							}
							//store the new string 
							structBody = structInfo.toString();
							//get the instance names between the } brace and the ; 
							String instances = structBody.substring(right+1, structBody.lastIndexOf(';')).trim();
							//check if there are any intance names to actually process
							if(instances.length() != 0){
								//generate uniforms based on the structures fields and types and create uniforms with those values and the variable name
								genUniformStructs(structName, instances, structs);
							}
						}else{//if they have
							//get the instance names between the } brace and the ; 
							String instances = structBody.substring(right+1, structBody.lastIndexOf(';')).trim();
							//check if there are any intance names to actually process
							if(instances.length() != 0){
								//generate uniforms based on the structures fields and types and create uniforms with those values and the variable name
								genUniformStructs(structName, instances, structs);
							}
						}
					}else{
						int left = line.indexOf(uniform);
						int right = line.indexOf(';');
						//use the @ to replace the spacing between the uniform type and the names of the uniforms then split them around that
						String[] typeNames = line.substring(left+uniform.length(), right).trim().replaceFirst(space1plus, "@").split("\\@");
						//determine is the uniform type is a struct type or basic type
						if(!structs.containsKey(typeNames[0])){
							genUniforms(typeNames[0], typeNames[1]);
						}else{
							genUniformStructs(typeNames[0], typeNames[1], structs);
						}
					}
				}else if(line.contains(struct)){
					StringBuilder structInfo = new StringBuilder(line);
					//continue reading the shader storing additional information about the structure in the string builder
					//while also updating the line variable and the source string builder
					while(shaderParse.hasNextLine() && !line.contains("}")){
						line = shaderParse.nextLine();
						structInfo.append(line);
						source.append(line+"\n");
					}
					//convert the string builder to a string for processing
					String structBody = structInfo.toString();
					int left = structBody.indexOf(struct);
					int right = structBody.indexOf('}');
					//get the substring starting after the keyword struct and before the } brace
					//then split on {  dividing the data into the name and the member list of the struct
					String[] structData = structBody.substring(left+struct.length(), right).split(space0plus+"\\{"+space0plus);
					//store the name of the structure
					String structName = structData[0].trim();
					if(structName.length() != 0){
						//generate a structure for use
						genStruct(structName, structData[1], structs);
					}
				}
			}
			shaderParse.close();
		}catch(IOException e){
			e.printStackTrace();
			System.exit(0);
		}
		//System.out.println(source.toString());
		//create a shader object and store it in a handler for the application to use
		shaderID = glCreateShader(shaderType);
		//send the source code read from the file to the shader object
		glShaderSource(shaderID, source.toString());
		//compile the source code stored on the specified shader object
		glCompileShader(shaderID);
		
		return shaderID;
	}
	
	public void delete(){
		glDeleteProgram(programId);
	}
	
	private void genUniforms(String type, String names){
		String[] varNames = names.trim().replaceAll("="+space0plus+"("+type+initializers+")|true|false|\\d+|\\d+\\.\\d+f?)"+space0plus+",?", ",").split(space0plus+","+space0plus);
		
		for(int vars = 0; vars < varNames.length; vars++){
			if(!varNames[vars].contains("[")){
				uniforms.put(varNames[vars], new Uniform(varNames[vars], type));
			}
			else{
				int indexOfBrace1 = varNames[vars].indexOf("[");
				int indexOfBrace2 = varNames[vars].indexOf("]");
				int arraySize = Integer.parseInt(varNames[vars].substring(indexOfBrace1+1, indexOfBrace2));
				String uniformName = varNames[vars].substring(0, indexOfBrace1);
				
				for(int array = 0; array < arraySize; array++){
					uniforms.put(uniformName+"["+array+"]", new Uniform(uniformName+"["+array+"]", type));
				}
			}
		}
	}
	
	private void genStruct(String name, String memberList, HashMap<String, ShaderStruct> structMap){
		ShaderStruct struct = new ShaderStruct(name);
		String[] members = memberList.trim().split(space0plus+";");
		//iterate over the different groups of variables for the current structure
		for(int curGroup = 0; curGroup < members.length; curGroup++){
			//separate the type from the names
			String[] type_names = members[curGroup].trim().replaceFirst(space1plus, "@").split("\\@");
			String type = type_names[0];//store type
			String[] names = type_names[1].split(space0plus+","+space0plus);//separate the names
			//iterate over the names of the structure variables
			for(int curName = 0; curName < names.length; curName++){
				String curVar = names[curName].trim();
				//determine if the name is an array type or not and store the names
				if(!curVar.contains("[")){
					struct.addField(type, curVar);
				}
				else{
					int indexOfBrace1 = curVar.indexOf("[");
					int indexOfBrace2 = curVar.indexOf("]");
					int arraySize = Integer.parseInt(curVar.substring(indexOfBrace1+1, indexOfBrace2));
					String varName = curVar.substring(0, indexOfBrace1);
					//create the different names for each element of the array
					for(int array = 0; array < arraySize; array++){
						struct.addField(type, varName+"["+array+"]");
					}
				}
			}
		}
		//store in the structures map passed to the function using the provided name
		structMap.put(name, struct);
	}
	/**
	 * 
	 * @param structName
	 * @param uniformNames Names of the instances of the uniform structure to be formed
	 * @param structMap
	 */
	private void genUniformStructs(String structName, String uniformNames, HashMap<String, ShaderStruct> structMap){
		//split up the instance names
		String[] instanceNames = uniformNames.replaceAll("="+space0plus+"("+structName+initializers+")|true|false|\\d+|\\d+\\.\\d+f?)"+space0plus+",?", ",").split(space0plus+","+space0plus);
		//iterate over every instance name
		for(int curInstance = 0; curInstance < instanceNames.length; curInstance++){
			//get the name of the instance
			String curVar = instanceNames[curInstance];
			//check if it is an array type
			if(!curVar.contains("[")){
				genStructUniform(curVar, structName, structMap);
			}
			else{
				int indexOfBrace1 = curVar.indexOf("[");
				int indexOfBrace2 = curVar.indexOf("]");
				int arraySize = Integer.parseInt(curVar.substring(indexOfBrace1+1, indexOfBrace2));
				String varName = curVar.substring(0, indexOfBrace1);
				//create the different names for each element of the array
				for(int array = 0; array < arraySize; array++){
					genStructUniform(varName+"["+array+"]", structName, structMap);
				}
			}
		}
	}
	
	private void genStructUniform(String name, String curType, HashMap<String, ShaderStruct> structMap){
		String glslTypes = "float|int|uint|bool|mat2|mat2x2|mat2x3|mat2x4|mat3|mat3x3|mat3x2|mat3x4|mat4|mat4x4|mat4x2|mat4x3|"+
				"vec2|uvec2|ivec2|bvec2|vec3|uvec3|ivec3|bvec3|vec4|uvec4|ivec4|bvec4";
		if(curType.matches(glslTypes)){
			uniforms.put(name, new Uniform(name, curType));
		}else{
			ShaderStruct current = structMap.get(curType);
			for(int structVar = 0; structVar < current.fields.size(); structVar++){
				String curName = name+"."+current.fields.get(structVar);
				genStructUniform(curName, current.fieldTypes.get(structVar), structMap);
			}
		}
	}
	
	public void setUniform(String uniformName, Buffer data){
		uniforms.get(uniformName).set(data);
	}
	
	public void setUniform(String uniformName, float... variables){
		uniforms.get(uniformName).set(variables);
	}
	
	public void setUniform(String uniformName, int... variables){
		uniforms.get(uniformName).set(variables);
	}
	
	public void setUniform(String uniformName, boolean... variables){
		uniforms.get(uniformName).set(variables);
	}
	
	public void setUniform(String uniformName, boolean transpose, FloatBuffer data){
		uniforms.get(uniformName).setMat(transpose, data);
	}
	
	protected class ShaderStruct{
		public String name;
		public ArrayList<String> fields;
		public ArrayList<String> fieldTypes;
		
		public ShaderStruct(String name){
			this.name = name;
			fields = new ArrayList<String>();
			fieldTypes = new ArrayList<String>();
		}
		
		public void addField(String fieldType, String field){
			fieldTypes.add(fieldType.trim());
			fields.add(field.trim());
		}
	}
}
