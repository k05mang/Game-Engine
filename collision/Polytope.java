package collision;

import java.util.ArrayList;
import java.util.Iterator;

import glMath.Vec3;
import glMath.VecUtil;

/**
 * 
 * @author Kevin
 *
 * Class for interacting with the polytope used in the Expanding Polytope Algorithm (EPA)
 * 
 * This class contains several sub classes that are used as containers for various information 
 * needed in the EPA
 */
public class Polytope {
	
	public ArrayList<PolytopeTriangle> faces;//stores all the current faces of the polytope
	
	public Polytope(PolytopePoint a, PolytopePoint b, PolytopePoint c, PolytopePoint d){
		faces = new ArrayList<PolytopeTriangle>();//construct array list to hold the faces of the polytope
		//construct the initial faces of the polytope using the given vertices that are from the simplex
		//GJK terminated with
		faces.add(new PolytopeTriangle(a,b,c));
		faces.add(new PolytopeTriangle(a,c,d));
		faces.add(new PolytopeTriangle(a,d,b));
		faces.add(new PolytopeTriangle(b,d,c));
	}
	
	/**
	 * Adds a new point to the polytope, when a new point is added the polytope will update
	 * all the faces facing that new point. This means that all the faces that can "see" the
	 * new vertex will be removed and the non adjacent edges of these triangles will be used
	 * in generating new triangles for the simplex. Essentially a hole is formed, then using the
	 * removed faces edges we construct new faces to fill the hole.
	 *	
	 * @param addition PolytopePoint to be added to the polytope
	 */
	public void add(PolytopePoint addition){
		//edges of the polytope hole
		ArrayList<PolytopeEdge> edges = new ArrayList<PolytopeEdge>();
		//iterator over the faces
		Iterator<PolytopeTriangle> loopControl = faces.iterator();
		
		//loop through the faces removing any that "see" the new point, and store their edges
		while(loopControl.hasNext()){
			PolytopeTriangle face = loopControl.next();//current face being processed
			//check if the face can see the new point
			if(VecUtil.subtract(addition.supportFinal, face.s1.start.supportFinal).dot(face.normal) > 0){
				
				//used to mark which edges of this face should be added to the edge array that forms the edges
				//of the hole that is being formed
				boolean add1 = true, add2 = true, add3 = true;
				
				Iterator<PolytopeEdge> edgeIterator = edges.iterator();
				//iterate over the edges currently stored
				while(edgeIterator.hasNext()){
					PolytopeEdge curEdge = edgeIterator.next();
					//test the individual edges of this face if any of them are in this edge list
					//remove them and flag that edge as do not add
					if(curEdge.equals(face.s1)){
						add1 = false;
						edgeIterator.remove();
					}else if(curEdge.equals(face.s2)){
						add2 = false;
						edgeIterator.remove();
					}else if(curEdge.equals(face.s3)){
						add3 = false;
						edgeIterator.remove();
					}
				}
				
				//check which edges have been flagged as do not add and add the ones that we have
				//determined to be valid
				if (add1) {
					edges.add(face.s1);
				}
				
				if (add2) {
					edges.add(face.s2);
				}
				
				if (add3) {
					edges.add(face.s3);
				}
				//remove the currently processed triangle face from the polytope
				loopControl.remove();
			}
//			System.out.println(faces.size());
//			System.out.println(edges.size());
		}
//		System.out.println("outside loop");
		//construct new faces with the new support point
		for(PolytopeEdge edge : edges){
			faces.add(new PolytopeTriangle(edge.start, edge.end, addition));
		}
	}
	
	/**
	 * Finds the new search direction based on the face of the polytope closest to 
	 * the origin
	 * 
	 * @return Face on the polytope closest to the origin
	 */
	public PolytopeTriangle findClosest(){
		//OPTIMIZATION MOVE THIS TO THE ADD FUNCTION AND HAVE IT BE THE RETURN VALUE
		float maxDist = faces.get(0).getDistance();
		int index = 0;
		for(int curIndex = 1; curIndex < faces.size(); curIndex++){
			float distance = faces.get(curIndex).getDistance();
			if(distance < maxDist){
				index = curIndex;
				maxDist = distance;
			}
		}
		
		return faces.get(index);
	}
	
	/**
	 * Class for storing information about a polytope point, including the final support point of the 
	 * Minkowski sum used in the EPA, as well as the support points of the two colliding objects that
	 * were used in forming the support point on the sum
	 * 
	 * @author Kevin
	 *
	 */
	public static class PolytopePoint{
		
		public Vec3 supportA, supportB, supportFinal;//support points
		
		public PolytopePoint(Vec3 sA, Vec3 sB){
			supportA = new Vec3(sA);
			supportB = new Vec3(sB);
			supportFinal = (Vec3)VecUtil.subtract(supportA, supportB);
		}
		
		@Override 
		public boolean equals(Object o){
			if(o instanceof PolytopePoint){
				PolytopePoint cast = (PolytopePoint) o;
				return supportA.equals(cast.supportA) 
						&& supportB.equals(cast.supportB) 
						&& supportFinal.equals(cast.supportFinal);
			}else{
				return false;
			}
		}
		
		@Override
		public String toString(){
			return supportA.toString()+"\n"+supportB.toString()+"\n"+supportFinal.toString()+"\n";
		}
	}
	
	/**
	 * Class for containing the information about a given edge for a face of the polytope triangle,
	 * this edge has a start point and an end point which are used in determining the winding of the
	 * polytope triangle faces
	 * 
	 * @author Kevin
	 *
	 */
	public static class PolytopeEdge{
		
		public PolytopePoint start, end;//start and end points as polytopePoints
		
		public PolytopeEdge(PolytopePoint s1, PolytopePoint s2){
			start = s1;
			end = s2;
		}
		
		@Override 
		public boolean equals(Object o){
			if(o instanceof PolytopeEdge){
				PolytopeEdge cast = (PolytopeEdge) o;
				/*test whether the two edges are equal based on opposite windings
				since the only edge this will be tested against is the one opposite
				to it with a different winding, we instead test equality based on 
				the this.start == that.end and this.end == that.start*/
				return start.equals(cast.end) && end.equals(cast.start);
			}else{
				return false;
			}
		}
	}
	
	/**
	 * Class for storing information about the different faces of the polytope being used in the 
	 * EPA. Each face has 3 edges that comprise them as well as a normal. The edges are stored 
	 * such that a winding order is retained in them which defines the direction of the normal
	 * that is also stored for this face.
	 * 
	 * @author Kevin
	 *
	 */
	public static class PolytopeTriangle{
		public PolytopeEdge s1, s2, s3;
		public Vec3 normal;
		
		public PolytopeTriangle(PolytopePoint a, PolytopePoint b, PolytopePoint c){
			//create edges based on winding of the triangle
			s1 = new PolytopeEdge(a,b);
			s2 = new PolytopeEdge(b,c);
			s3 = new PolytopeEdge(c,a);
			//compute the normal of this triangle
			Vec3 ab = (Vec3)VecUtil.subtract(b.supportFinal, a.supportFinal);
			Vec3 ac = (Vec3)VecUtil.subtract(c.supportFinal, a.supportFinal);
			normal = ab.cross(ac);
			normal.normalize();
		}
		
		/**
		 * Computes the distance of this face from the origin in the minkowski space
		 * 
		 * @return Distance of this face from the origin
		 */
		public float getDistance(){
			return s1.start.supportFinal.dot(normal);
		}
	}
}
