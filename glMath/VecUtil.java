package glMath;

public abstract class VecUtil {
	public static final Vec3 
	xAxis = new Vec3(1,0,0),
	yAxis = new Vec3(0,1,0),
	zAxis = new Vec3(0,0,1);
	/**
	 * Computes the dot product of two vectors of the same type
	 * 
	 * @param vec1 Vector to start from
	 * @param vec2 Vector to dot with
	 * @return Float representing the dot product of the given vectors, or 0 in the event of a 
	 * mismatch
	 */
	public static float dot(Vector vec1, Vector vec2){
		//check if the given vectors of a matching type and compute the result if they are 
		if(vec1 instanceof Vec2 && vec2 instanceof Vec2){
			Vec2 vect1 = (Vec2)vec1;
			Vec2 vect2 = (Vec2)vec2;
			return vect1.dot(vect2);
			
		}else if(vec1 instanceof Vec3 && vec2 instanceof Vec3){
			Vec3 vect1 = (Vec3)vec1;
			Vec3 vect2 = (Vec3)vec2;
			return vect1.dot(vect2);
			
		}else if(vec1 instanceof Vec4 && vec2 instanceof Vec4){
			Vec4 vect1 = (Vec4)vec1;
			Vec4 vect2 = (Vec4)vec2;
			return vect1.dot(vect2);
		}else{
			System.err.println("Type mismatch, the vectors given are not of the same type");
			return 0;
		}
	}
	
	/**
	 * Generates the cross product of the given vectors.
	 * The cross product produced by this function takes the first 2 arguments crosses them
	 * and uses the result of that cross product as the left hand value for the next value in
	 * the given set, and continues this for each element in the given arguments. 
	 * 
	 * So given the vectors {A, B, C, D}, this would return ( (A x B) x C) x D.
	 * 
	 * @param vectors Vectors of type Vec3 to be used in determining the cross product
	 * @return The vector representing the chained cross product of the given arguments
	 */
	public static Vec3 cross(Vec3... vectors){
		if(vectors.length > 1){
			Vec3 result = vectors[0].cross(vectors[1]);
			for(int curVec = 2; curVec < vectors.length; curVec++){
				result.cross(vectors[curVec]);
			}
			return result;
		}else{
			return null;
		}
	}
	
	/**
	 * Adds vectors together, returning the result of their sum
	 * 
	 * @param vectors Vectors to be added together from left to right
	 * @return The sum of the given vectors
	 */
	public static Vec2 add(Vec2... vectors){
		if(vectors.length > 1){
			Vec2 result = new Vec2(0);
			for (int curVector = 0; curVector < vectors.length; curVector++){
				result.add(vectors[curVector]);
			}
			return result;
		}else{
			return vectors.length == 1 ? vectors[0] : null;
		}
	}
	
	/**
	 * Adds vectors together, returning the result of their sum
	 * 
	 * @param vectors Vectors to be added together from left to right
	 * @return The sum of the given vectors
	 */
	public static Vec3 add(Vec3... vectors){
		if(vectors.length > 1){
			Vec3 result = new Vec3(0);
			for (int curVector = 0; curVector < vectors.length; curVector++){
				result.add(vectors[curVector]);
			}
			return result;
		}else{
			return vectors.length == 1 ? vectors[0] : null;
		}
	}

	/**
	 * Adds vectors together, returning the result of their sum
	 * 
	 * @param vectors Vectors to be added together from left to right
	 * @return The sum of the given vectors
	 */
	public static Vec4 add(Vec4... vectors){
		if(vectors.length > 1){
			Vec4 result = new Vec4(0);
			for (int curVector = 0; curVector < vectors.length; curVector++){
				result.add(vectors[curVector]);
			}
			return result;
		}else{
			return vectors.length == 1 ? vectors[0] : null;
		}
	}
	
	/**
	 * Subtracts vectors, returning the result of their difference
	 * 
	 * @param vectors Vectors to be subtracted from left to right
	 * @return The difference of the given vectors
	 */
	public static Vec2 subtract(Vec2... vectors){
		if(vectors.length > 1){
			Vec2 result = new Vec2((Vec2)vectors[0]);
			for (int curVector = 1; curVector < vectors.length; curVector++){
				result.subtract(vectors[curVector]);
			}
			return result;
		}else{
			return vectors.length == 1 ? vectors[0] : null;
		}
	}

	/**
	 * Subtracts vectors, returning the result of their difference
	 * 
	 * @param vectors Vectors to be subtracted from left to right
	 * @return The difference of the given vectors
	 */
	public static Vec3 subtract(Vec3... vectors){
		if(vectors.length > 1){
			Vec3 result = new Vec3((Vec3)vectors[0]);
			for (int curVector = 1; curVector < vectors.length; curVector++){
				result.subtract(vectors[curVector]);
			}
			return result;
		}else{
			return vectors.length == 1 ? vectors[0] : null;
		}
	}

	/**
	 * Subtracts vectors, returning the result of their difference
	 * 
	 * @param vectors Vectors to be subtracted from left to right
	 * @return The difference of the given vectors
	 */
	public static Vec4 subtract(Vec4... vectors){
		if(vectors.length > 1){
			Vec4 result = new Vec4((Vec4)vectors[0]);
			for (int curVector = 1; curVector < vectors.length; curVector++){
				result.subtract(vectors[curVector]);
			}
			return result;
		}else{
			return vectors.length == 1 ? vectors[0] : null;
		}
	}
	
	/**
	 * Computes the reflection of a vector over a given incident vector
	 * 
	 * @param incident Vector being reflected
	 * @param normal Vector to be reflected over
	 * @return The reflection of the incident vector over the given normal vector
	 */
	public static Vec2 reflect(Vec2 incident, Vec2 normal){
		Vec2 i = new Vec2(incident);
		Vec2 n = new Vec2(incident);
		n.normalize();
		return i.subtract(n.scale(2*(i.dot(n))));
	}
	
	/**
	 * Computes the reflection of a vector over a given incident vector
	 * 
	 * @param incident Vector being reflected
	 * @param normal Vector to be reflected over
	 * @return The reflection of the incident vector over the given normal vector
	 */
	public static Vec3 reflect(Vec3 incident, Vec3 normal){
		Vec3 i = new Vec3(incident);
		Vec3 n = new Vec3(incident);
		n.normalize();
		return i.subtract(n.scale(2*(i.dot(n))));
	}
	
	/**
	 * Computes the reflection of a vector over a given incident vector
	 * 
	 * @param incident Vector being reflected
	 * @param normal Vector to be reflected over
	 * @return The reflection of the incident vector over the given normal vector
	 */
	public static Vec4 reflect(Vec4 incident, Vec4 normal){
		Vec4 i = new Vec4(incident);
		Vec4 n = new Vec4(incident);
		n.normalize();
		return i.subtract(n.scale(2*(i.dot(n))));
	}
	
	public static Vec2 scale(Vec2 vector, float scalar){
		return new Vec2(vector).scale(scalar);
	}
	
	public static Vec3 scale(Vec3 vector, float scalar){
		return new Vec3(vector).scale(scalar);
	}
	
	public static Vec4 scale(Vec4 vector, float scalar){
		return new Vec4(vector).scale(scalar);
	}
}
