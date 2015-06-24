package collision;

import java.util.ArrayList;

import glMath.Vec3;
import glMath.VecUtil;

public abstract class CollisionDetector {
	
	protected static final float MAX_THRESHOLD = .001f;
	
	public static CollisionData gjkIntersect(CollisionMesh objA, CollisionMesh objB){
		Polytope.PolytopePoint start = new Polytope.PolytopePoint(objA.support(objA.getCenter()), objB.support(objA.getCenter().inverse()));
		ArrayList<Polytope.PolytopePoint> simplex = new ArrayList<Polytope.PolytopePoint>();
		simplex.add(start);
		Vec3 direction = start.supportFinal.inverse();
		while(!computeSimplex(simplex, direction)){
			Polytope.PolytopePoint newPoint = new Polytope.PolytopePoint(objA.support(direction), objB.support(direction.inverse()));
			//this early out is what is making it impossible to get accurate collision results
			if(newPoint.supportFinal.dot(direction) < 0){
				return computeDistance(simplex);
			}
			simplex.add(newPoint);
		}
		return expandingPolytope(new Polytope(simplex.get(3), simplex.get(2), simplex.get(1), simplex.get(0)), objA, objB);
	}
	
	private static boolean computeSimplex(ArrayList<Polytope.PolytopePoint> simplex, Vec3 direction){
		switch(simplex.size()){
			case 1:
				return false;
			case 2:
				//vectors from the recently added point to the simplex to the origin
				Vec3 ao = simplex.get(1).supportFinal.inverse();
				//vector from the recently added point in the simplex to the previous point
				Vec3 ab = (Vec3)VecUtil.subtract(simplex.get(0).supportFinal, simplex.get(1).supportFinal);
				/*since we know that the origin can't be behind B, since we just came from that
				direction, and we know it can't be in front of A, since A is that farthest point 
				on the sum and it would have failed the early exit test, we know then that the
				origin is only in the direction perpendicular to the edge AB
				*/				
				direction.set(VecUtil.cross(ab,ao,ab));
				return false;
			case 3:
				ab = (Vec3)VecUtil.subtract( simplex.get(1).supportFinal, simplex.get(2).supportFinal );
				Vec3 ac = (Vec3)VecUtil.subtract( simplex.get(0).supportFinal, simplex.get(2).supportFinal );
				ao = simplex.get(2).supportFinal.inverse();
				Vec3 abc = ab.cross(ac);
				/*
				 * check if the edge AC is closest to the origin*/
				if(abc.cross(ac).dot(ao) > 0){
					simplex.remove(1);
					direction.set(VecUtil.cross(ac,ao,ac));
				}else if(ab.cross(abc).dot(ao) > 0){
					simplex.remove(0);
					direction.set(VecUtil.cross(ab,ao,ab));
				}else{
					if(abc.dot(ao) > 0){
						direction.set(abc);
					}else{
						direction.set(abc.inverse());
						Polytope.PolytopePoint b = simplex.get(0);
						simplex.set(0, simplex.get(1));
						simplex.set(1, b);
					}
				}
				return false;
			case 4:
				ab = (Vec3)VecUtil.subtract( simplex.get(2).supportFinal, simplex.get(3).supportFinal );
				ac = (Vec3)VecUtil.subtract( simplex.get(1).supportFinal, simplex.get(3).supportFinal );
				Vec3 ad = (Vec3)VecUtil.subtract( simplex.get(0).supportFinal, simplex.get(3).supportFinal );
				abc = ab.cross(ac);
				Vec3 acd = ac.cross(ad);
				Vec3 adb = ad.cross(ab);
				ao = simplex.get(3).supportFinal.inverse();
				float abcDOTao = abc.dot(ao);
				float acdDOTao = acd.dot(ao);
				float adbDOTao = adb.dot(ao);
				
				//test what face the origin might be located
				if(abcDOTao > 0){
					simplex.remove(0);
				}else if(acdDOTao > 0){
					simplex.remove(2);
				}else if(adbDOTao > 0){
					simplex.remove(1);//remove C
					Polytope.PolytopePoint b = simplex.get(1);
					//swap B and D to maintain winding order
					simplex.set(1, simplex.get(0));
					simplex.set(0, b);
				}else{
					return true;
				}
				//check the new triangle for edge cases, only updating the direction vector
				//after, this was where I was messing up last time, passing a new direction
				//and letting the loop run again
				ab = (Vec3)VecUtil.subtract( simplex.get(1).supportFinal, simplex.get(2).supportFinal );
				ac = (Vec3)VecUtil.subtract( simplex.get(0).supportFinal, simplex.get(2).supportFinal );
				abc = ab.cross(ac);
				if(abc.cross(ac).dot(ao) > 0){
					simplex.remove(1);
					direction.set(VecUtil.cross(ac,ao,ac));
				}else if(ab.cross(abc).dot(ao) > 0){
					simplex.remove(0);
					direction.set(VecUtil.cross(ab,ao,ab));
				}else{
					direction.set(abc);
				}
				return false;
			default:
				return false;
		}
	}
	
	//this seems to be producing the wrong results FIX IT
	private static CollisionData computeDistance(ArrayList<Polytope.PolytopePoint> points){
//		System.out.println(points.size());
		switch(points.size()){
			case 1:
				Polytope.PolytopePoint point = points.get(0);
				Vec3 normal = new Vec3(point.supportA);
				float distance = normal.normalize();
				return new CollisionData(normal, point.supportA, point.supportA, distance, false);
			case 2:
				Polytope.PolytopePoint start = points.get(1);
				Polytope.PolytopePoint end = points.get(0);
				Vec3 edge = (Vec3)VecUtil.subtract(start.supportFinal, end.supportFinal );
				Vec3 pointA = null, pointB = null;
				edge.trunc();
			if(!edge.isZero()){;
				float lambda1 = edge.dot(start.supportFinal)/edge.dot(edge);
				float lambda2 = 1 - lambda1;
				
				if(lambda1 < 0){
					pointA = end.supportA;
					pointB = end.supportB;
				}else if(lambda2 < 0){
					pointA = start.supportA;
					pointB = start.supportB;
				}else{
					pointA = start.supportA.scale(lambda1).add(start.supportB.scale(lambda2));
					pointB = end.supportA.scale(lambda1).add(end.supportB.scale(lambda2));
				}
			}else{
				pointA = start.supportA;
				pointB = start.supportB;
			}
			normal = (Vec3)VecUtil.subtract(pointB, pointA);
			distance = normal.length();
			normal.normalize();
			return new CollisionData(normal, pointA, pointB, distance, false);
		case 3:
			Polytope.PolytopeTriangle polytopeFace = new Polytope.PolytopeTriangle(points.get(2), points.get(1), points.get(0));
			 Vec3 coords = computeBarycentric(
						polytopeFace.s1.start.supportFinal,
						polytopeFace.s2.start.supportFinal,
						polytopeFace.s3.start.supportFinal,
						new Vec3(polytopeFace.normal).scale(polytopeFace.getDistance()));
			    
			    //compute the point on A using the barycentric coordinates
			    Vec3 aPoint = (Vec3)VecUtil.add(
			    		polytopeFace.s1.start.supportA.scale(coords.x),
			    		polytopeFace.s2.start.supportA.scale(coords.y),
			    		polytopeFace.s3.start.supportA.scale(coords.z));
			    
			    //compute the point on B using the barycentric coordinates
			    Vec3 bPoint = (Vec3)VecUtil.add(
			    		polytopeFace.s1.start.supportB.scale(coords.x),
			    		polytopeFace.s2.start.supportB.scale(coords.y),
			    		polytopeFace.s3.start.supportB.scale(coords.z));
			    
	    		//construct the collision data object 
			    return new CollisionData(
			    			polytopeFace.normal.inverse(), aPoint, bPoint, polytopeFace.getDistance(), false
			    		);
			default:
				return new CollisionData();
		}
	}
	
	private static CollisionData expandingPolytope(Polytope simplex, CollisionMesh objA, CollisionMesh objB){
		//get the closest face on the polytope which is the face on the GJK termination simplex
		Polytope.PolytopeTriangle polytopeFace = simplex.findClosest();
		//get the distance this face is from the origin
		float prevDist = polytopeFace.getDistance();
		//get a new support point to add to the polytope in the direction of the closest face normal
		Polytope.PolytopePoint newSupport = new Polytope.PolytopePoint(
				objA.support(polytopeFace.normal),
				objB.support(polytopeFace.normal.inverse())
				);
		for(int curIter = 0; curIter < 50; curIter++){
			if(polytopeFace.normal.dot(newSupport.supportFinal)-prevDist < MAX_THRESHOLD){
				//compute the barycentric coordinates of the collision
			    
			    Vec3 coords = computeBarycentric(
						polytopeFace.s1.start.supportFinal,
						polytopeFace.s2.start.supportFinal,
						polytopeFace.s3.start.supportFinal,
						new Vec3(polytopeFace.normal).scale(prevDist));
			    
			    //compute the point on A using the barycentric coordinates
			    Vec3 aPoint = (Vec3)VecUtil.add(
			    		polytopeFace.s1.start.supportA.scale(coords.x),
			    		polytopeFace.s2.start.supportA.scale(coords.y),
			    		polytopeFace.s3.start.supportA.scale(coords.z));
			    
			    //compute the point on B using the barycentric coordinates
			    Vec3 bPoint = (Vec3)VecUtil.add(
			    		polytopeFace.s1.start.supportB.scale(coords.x),
			    		polytopeFace.s2.start.supportB.scale(coords.y),
			    		polytopeFace.s3.start.supportB.scale(coords.z));
			    
	    		//construct the collision data object 
			    return new CollisionData(
			    			polytopeFace.normal.inverse(), aPoint, bPoint, prevDist, true
			    		);
			}else{
				prevDist = polytopeFace.getDistance();
				//add the new Support point to the simplex and have it recompute the triangles
				//for the polytope
				simplex.add(newSupport);
				//get the new closest triangle on the new simplex 
				polytopeFace = simplex.findClosest();
				//compute a new support point in the direction of the new polytopeFace normal
				newSupport = new Polytope.PolytopePoint(
						objA.support(polytopeFace.normal),
						objB.support(polytopeFace.normal.inverse())
						);
			}
		}
		//compute the barycentric coordinates of the collision
	    
	    Vec3 coords = computeBarycentric(
				polytopeFace.s1.start.supportFinal,
				polytopeFace.s2.start.supportFinal,
				polytopeFace.s3.start.supportFinal,
				new Vec3(polytopeFace.normal).scale(prevDist));
	    
	    //compute the point on A using the barycentric coordinates
	    Vec3 aPoint = (Vec3)VecUtil.add(
	    		polytopeFace.s1.start.supportA.scale(coords.x),
	    		polytopeFace.s2.start.supportA.scale(coords.y),
	    		polytopeFace.s3.start.supportA.scale(coords.z));
	    
	    //compute the point on B using the barycentric coordinates
	    Vec3 bPoint = (Vec3)VecUtil.add(
	    		polytopeFace.s1.start.supportB.scale(coords.x),
	    		polytopeFace.s2.start.supportB.scale(coords.y),
	    		polytopeFace.s3.start.supportB.scale(coords.z));
	    
		//construct the collision data object 
	    return new CollisionData(
	    			polytopeFace.normal.inverse(), aPoint, bPoint, prevDist, true
	    		);
	}
	
	private static Vec3 computeBarycentric(Vec3 a, Vec3 b, Vec3 c, Vec3 p){
		// code from Christer Erickson's Real-Time Collision Detection
		
		Vec3 v0 = (Vec3)VecUtil.subtract(b,a);//b - a;
		Vec3 v1 = (Vec3)VecUtil.subtract(c,a);//c - a; 
		Vec3 v2 = (Vec3)VecUtil.subtract(p,a);//p - a;
		
	    float d00 = v0.dot(v0);
	    float d01 = v0.dot(v1);
	    float d11 = v1.dot(v1);
	    float d20 = v2.dot(v0);
	    float d21 = v2.dot(v1);
	    float denom = d00 * d11 - d01 * d01;
	     
	    Vec3 coords = new Vec3();
	    coords.y = (d11 * d20 - d01 * d21) / denom;
	    coords.z = (d00 * d21 - d01 * d20) / denom;
	    coords.x = 1.0f - coords.y - coords.z;
	    
	    return coords;
	}
}
