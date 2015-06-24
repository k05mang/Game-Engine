package collision;

import glMath.Vec3;

public class CollisionData {
	public Vec3 normal, contactA, contactB;
	public float depth;
	public boolean areColliding;
	public int timeStamp;
	private final float DISTANCE_THRESHOLD = .1f;
	
	public CollisionData(Vec3 normal, Vec3 pA, Vec3 pB, float depth, boolean isColliding){
		this.normal = new Vec3(normal);
		contactA = new Vec3(pA);
		contactB = new Vec3(pB);
		this.depth = depth;
		areColliding = isColliding;
	}
	
	public CollisionData(){
		normal = new Vec3();
		contactA = new Vec3();
		contactB = new Vec3();
		depth = 0;
		areColliding = false;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof CollisionData){
			CollisionData cast = (CollisionData)o;
			return cast.normal.equals(normal) &&
					cast.contactA.equals(contactA) &&
					cast.contactB.equals(contactB) &&
					cast.depth == depth &&
					cast.timeStamp == timeStamp;
					
		}else{
			return false;
		}
	}
	
	/**
	 * Determines whether or not this contact point should be replaced with the given contact point
	 * if it should then this contact point will replace its data with the new contact point
	 * 
	 * @param newData New contact point to be checked against
	 * @return True if this contact point is to be replaced by the given contact point, false if not
	 */
//	public boolean compareToReplace(CollisionData newData){
//		Vec3 distanceGap = (Vec3)VecUtil.subtract(newData.contactPoint, contactPoint);
//		
//		if(distanceGap.dot(distanceGap) < DISTANCE_THRESHOLD){
//			this.contactPoint.set(newData.contactPoint);
//			this.normal.set(newData.normal);
//			this.depth = newData.depth;
//			this.areColliding = newData.areColliding;
//			this.timeStamp = newData.timeStamp;
//			return true;
//		}
//		return false;
//	}
	
	public void print(){
		System.out.println("Depth: "+depth);
		normal.print();
		contactA.print();
		contactB.print();
	}
}
