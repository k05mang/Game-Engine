package collision;

import glMath.Mat3;
import glMath.MatrixUtil;
import glMath.Quaternion;
import glMath.Vec3;
import glMath.VecUtil;
import core.GameObject;

import java.util.ArrayList;

public class ContactPair {
	private ArrayList<CollisionData> contactData;
	private GameObject contactA, contactB;
	
	public ContactPair(GameObject a, GameObject b){
		contactData = new ArrayList<CollisionData>();
		contactA = a;
		contactB = b;
	}
	
	public void addContact(CollisionData newContact){
		contactData.add(newContact);
	}
	
	public ArrayList<CollisionData> getContacts(){
		return contactData;
	}
	
	public void resolve(int iterations){
		for(int curIter = 0; curIter < iterations; curIter++){
			for(CollisionData curContact : contactData){
				//velocity relative to object A (this)
				Vec3 aRelB = VecUtil.subtract(contactB.velocity, contactA.velocity);
				//the magnitude of the end velocity
				float velAlongNormal = aRelB.dot(curContact.normal);
//				curContact.normal.print();
				if (velAlongNormal > 0) {
					float sumInv = contactA.massInv+contactB.massInv;
					//calculation of impulses of a collision
					float elasticCoeff = (contactA.restitution > contactB.restitution ? contactB.restitution : contactA.restitution);
					
					Vec3 pointA = (Vec3)VecUtil.subtract(curContact.contactA, contactA.position);
					Vec3 pointB = (Vec3)VecUtil.subtract(curContact.contactB, contactB.position);
					
					Vec3 raCrossN = pointA.cross(curContact.normal);
					Vec3 rbCrossN = pointB.cross(curContact.normal);
					
					//map the inertia tensor to be oriented to the objects current orientation, since it was computed when the object was at rest
					Mat3 aFinalInertia = (Mat3)MatrixUtil.multiply(contactA.mesh.getOrientation().asRotMatrix(), contactA.invInertiaTensor);
					Mat3 bFinalInertia = (Mat3)MatrixUtil.multiply(contactB.mesh.getOrientation().asRotMatrix(), contactB.invInertiaTensor);
					
					
					/* raCrossN = torque(force being applied to the angular component of the object)
					 * aFinalInertia.multVec(raCrossN) = rotation (amount of movement per unit of torque)
					 */
					//the angular resistance to change
					float angularInertiaA = raCrossN.dot(aFinalInertia.multVec(raCrossN));
					float angularInertiaB = rbCrossN.dot(bFinalInertia.multVec(rbCrossN));
					
					//the total resistance of the collision to change
					float totalInertia = sumInv + angularInertiaA + angularInertiaB;
					
					//the total change in the velocity of the collision in this instant
					float impulse = (1+elasticCoeff)*(velAlongNormal+raCrossN.dot(contactA.angVel)+rbCrossN.dot(contactB.angVel))
							/totalInertia;
					
					//the vector representing the impulse applied in the direction of the contact normal
					Vec3 impulseVec = new Vec3(curContact.normal).scale(impulse);
					
					contactA.velocity.add(new Vec3(curContact.normal).scale(contactA.massInv * impulse));
					contactB.velocity.add(new Vec3(curContact.normal).scale(contactB.massInv * -impulse));
					
					//the torque being applied to the bodies by the impulse
					Vec3 angularImpulseA = contactA.invInertiaTensor.multVec(impulseVec.cross(pointA));
					Vec3 angularImpulseB = contactB.invInertiaTensor.multVec(impulseVec.scale(-1).cross(pointB));
					
					contactA.angVel.add(angularImpulseA);
					contactB.angVel.add(angularImpulseB);
					
					//frictional tangent vector 
					Vec3 frictionVec = VecUtil.subtract(aRelB, new Vec3(curContact.normal).scale(velAlongNormal));
					frictionVec.trunc();
					
					if(!frictionVec.isZero()){
						frictionVec.normalize();
						float fImpulse = aRelB.dot(frictionVec)/sumInv;
//						float fImpulse = aRelB.dot(frictionVec)/totalInertia;
						float coeffFric = (contactA.sFriction+contactB.sFriction)/2.0f;
						
						if(fImpulse < impulse*coeffFric){
							contactA.velocity.add(new Vec3(frictionVec).scale(contactA.massInv*fImpulse));
							contactB.velocity.add(new Vec3(frictionVec).scale(contactB.massInv*-fImpulse));
							
//							contactA.angVel.add(contactA.inertiaTensor.multVec(new Vec3(frictionVec).scale(fImpulse)));
//							contactB.angVel.add(contactB.inertiaTensor.multVec(new Vec3(frictionVec).scale(-fImpulse)));
						}else{
							coeffFric = (contactA.dFriction+contactB.dFriction)/2.0f;
							contactA.velocity.add(new Vec3(frictionVec).scale(contactA.massInv*impulse*coeffFric));
							contactB.velocity.add(new Vec3(frictionVec).scale(contactB.massInv*-impulse*coeffFric));
							
//							contactA.angVel.add(contactA.inertiaTensor.multVec(new Vec3(frictionVec).scale(impulse*coeffFric)));
//							contactB.angVel.add(contactB.inertiaTensor.multVec(new Vec3(frictionVec).scale(-impulse*coeffFric)));
						}
					}
					
					float correction = (curContact.depth > 0 ? curContact.depth : 0)/sumInv;
//					float correction = (curContact.depth > 0 ? curContact.depth : 0)/totalInertia;
					Vec3 restCorrection = new Vec3(curContact.normal).scale(contactA.massInv*correction);
					contactA.mesh.translate(restCorrection);
//					contactA.collider.translate(restCorrection);
					contactA.position.add(restCorrection);
					//rotational correction
//					contactA.mesh.setOrientation(contactA.mesh.getOrientation().addVector(
//							contactA.invInertiaTensor.multVec(new Vec3(curContact.normal).scale(correction))));
					
					//resolve for object B
					Vec3 bRestCorr = new Vec3(curContact.normal).scale(contactB.massInv*-correction);
					contactB.mesh.translate(bRestCorr);
//					contactB.collider.translate(bRestCorr);
					contactB.position.add(bRestCorr);
					//adding a negative component currently causes infinite looping somewhere
//					contactB.mesh.setOrientation(contactB.mesh.getOrientation().addVector(
//							contactB.invInertiaTensor.multVec(new Vec3(curContact.normal).scale(-correction))));
				}
			}
		}
//		CollisionData curContact = contactData.get(0);
//		//velocity relative to object A (this)
//		Vec3 aRelB = VecUtil.subtract(contactB.velocity, contactA.velocity);
//		//the magnitude of the end velocity
//		float velAlongNormal = aRelB.dot(curContact.normal);
////		curContact.normal.print();
//		if (velAlongNormal > 0) {
//			float sumInv = contactA.massInv+contactB.massInv;
//			//calculation of impulses of a collision
//			float elasticCoeff = (contactA.restitution > contactB.restitution ? contactB.restitution : contactA.restitution);
//			
//			Vec3 pointA = (Vec3)VecUtil.subtract(curContact.contactA, contactA.position);
//			Vec3 pointB = (Vec3)VecUtil.subtract(curContact.contactB, contactB.position);
//			
//			Vec3 raCrossN = pointA.cross(curContact.normal);
//			Vec3 rbCrossN = pointB.cross(curContact.normal);
//			
//			//map the inertia tensor to be oriented to the objects current orientation, since it was computed when the object was at rest
//			Mat3 aFinalInertia = (Mat3)MatrixUtil.multiply(contactA.mesh.getOrientation().asRotMatrix(), contactA.invInertiaTensor);
//			Mat3 bFinalInertia = (Mat3)MatrixUtil.multiply(contactB.mesh.getOrientation().asRotMatrix(), contactB.invInertiaTensor);
//			
//			
//			/* raCrossN = torque(force being applied to the angular component of the object)
//			 * aFinalInertia.multVec(raCrossN) = rotation (amount of movement per unit of torque)
//			 */
//			//the angular resistance to change
//			float angularInertiaA = raCrossN.dot(aFinalInertia.multVec(raCrossN));
//			float angularInertiaB = rbCrossN.dot(bFinalInertia.multVec(rbCrossN));
//			
//			//the total resistance of the collision to change
//			float totalInertia = sumInv + angularInertiaA + angularInertiaB;
//			
//			//the total change in the velocity of the collision in this instant
//			float impulse = (1+elasticCoeff)*(velAlongNormal+raCrossN.dot(contactA.angVel)+rbCrossN.dot(contactB.angVel))
//					/totalInertia;
//			
////			System.out.println(velAlongNormal);
////			System.out.println(raCrossN.dot(contactA.angVel));
////			System.out.println(rbCrossN.dot(contactB.angVel));
////			System.out.println(totalInertia);
////			System.out.println(impulse);
////			System.out.println();
//			
//			//the vector representing the impulse applied in the direction of the contact normal
//			Vec3 impulseVec = new Vec3(curContact.normal).scale(impulse);
//			
//			contactA.velocity.add(new Vec3(curContact.normal).scale(contactA.massInv * impulse));
//			contactB.velocity.add(new Vec3(curContact.normal).scale(contactB.massInv * -impulse));
//			
//			//the torque being applied to the bodies by the impulse
//			Vec3 angularImpulseA = contactA.invInertiaTensor.multVec(impulseVec.cross(pointA));
//			Vec3 angularImpulseB = contactB.invInertiaTensor.multVec(impulseVec.scale(-1).cross(pointB));
//			
//			contactA.angVel.add(angularImpulseA);
//			contactB.angVel.add(angularImpulseB);
//			
//			//frictional tangent vector 
//			Vec3 frictionVec = VecUtil.subtract(aRelB, new Vec3(curContact.normal).scale(velAlongNormal));
//			frictionVec.trunc();
//			
//			if(!frictionVec.isZero()){
//				frictionVec.normalize();
//				float fImpulse = aRelB.dot(frictionVec)/sumInv;
////				float fImpulse = aRelB.dot(frictionVec)/totalInertia;
//				float coeffFric = (contactA.sFriction+contactB.sFriction)/2.0f;
//				
//				if(fImpulse < impulse*coeffFric){
//					contactA.velocity.add(new Vec3(frictionVec).scale(contactA.massInv*fImpulse));
//					contactB.velocity.add(new Vec3(frictionVec).scale(contactB.massInv*-fImpulse));
//					
////					contactA.angVel.add(contactA.inertiaTensor.multVec(new Vec3(frictionVec).scale(fImpulse)));
////					contactB.angVel.add(contactB.inertiaTensor.multVec(new Vec3(frictionVec).scale(-fImpulse)));
//				}else{
//					coeffFric = (contactA.dFriction+contactB.dFriction)/2.0f;
//					contactA.velocity.add(new Vec3(frictionVec).scale(contactA.massInv*impulse*coeffFric));
//					contactB.velocity.add(new Vec3(frictionVec).scale(contactB.massInv*-impulse*coeffFric));
//					
////					contactA.angVel.add(contactA.inertiaTensor.multVec(new Vec3(frictionVec).scale(impulse*coeffFric)));
////					contactB.angVel.add(contactB.inertiaTensor.multVec(new Vec3(frictionVec).scale(-impulse*coeffFric)));
//				}
//			}
//			
//			float correction = (curContact.depth > 0 ? curContact.depth : 0)/(totalInertia);
//			Vec3 restCorrection = new Vec3(curContact.normal).scale(contactA.massInv*correction);
//			contactA.mesh.translate(restCorrection);
//	//		contactA.collider.translate(restCorrection);
//			contactA.position.add(restCorrection);
//			//rotational correction
//			contactA.mesh.setOrientation(contactA.mesh.getOrientation().addVector(
//					contactA.invInertiaTensor.multVec(new Vec3(curContact.normal).scale(correction))));
//			
//			//resolve for object B
//			Vec3 bRestCorr = new Vec3(curContact.normal).scale(contactB.massInv*-correction);
//			contactB.mesh.translate(bRestCorr);
//	//		contactB.collider.translate(bRestCorr);
//			contactB.position.add(bRestCorr);
//			//adding a negative component currently causes infinite looping somewhere
//			contactB.mesh.setOrientation(contactB.mesh.getOrientation().addVector(
//					contactB.invInertiaTensor.multVec(new Vec3(curContact.normal).scale(-correction))));//			contactB.mesh.setOrientation(contactB.mesh.getOrientation().addVector(contactB.invInertiaTensor.multVec(new Vec3(curContact.normal).scale(-correction))));
//		}
	}
	
	/*public void resolve(){
		//matrix transforming from world coords to contact coords
		Mat3 worldToContact = new Mat3(contactData.get(0).normal, VecUtil.yAxis, VecUtil.zAxis);
		worldToContact.orthonormalize();
		//matrix transforming from contact coords to world coords
		Mat3 contactToWorld = MatrixUtil.transpose(worldToContact);

		Vec3 pointA = (Vec3)VecUtil.subtract(contactData.get(0).point_on_A, contactA.position);
		Vec3 pointB = (Vec3)VecUtil.subtract(contactData.get(0).point_on_B, contactB.position);
		

		Mat3 aFinalInertia = (Mat3)MatrixUtil.multiply(contactA.mesh.getOrientation().asMatrix().getNormalMatrix(), contactA.inertiaTensor);
		Vec3 torquePerUnitImpulseA = pointA.cross(contactData.get(0).normal);
		Vec3 rotationPerUnitImpulseA = aFinalInertia.multVec(torquePerUnitImpulseA);
		Vec3 velocityPerUnitImpulseA = rotationPerUnitImpulseA.cross(pointA);
		
		float deltaV = velocityPerUnitImpulseA.dot(contactData.get(0).normal);
		deltaV += contactA.massInv;
		
		Vec3 velImpulseRelContactA = worldToContact.multVec(velocityPerUnitImpulseA);
		float angularCompA = velImpulseRelContactA.x;//angular component of the current change in velocity
		
		Vec3 velocity = contactA.angVel.cross(pointA);//add rotational velocity for A
		velocity.add(contactA.velocity);//add linear velocity for A

		if(contactB.mass != 0){
			Mat3 bFinalInertia = (Mat3)MatrixUtil.multiply(contactB.mesh.getOrientation().asMatrix().getNormalMatrix(), contactB.inertiaTensor);
			Vec3 torquePerUnitImpulseB = pointB.cross(contactData.get(0).normal);
			Vec3 rotationPerUnitImpulseB = bFinalInertia.multVec(torquePerUnitImpulseB);
			Vec3 velocityPerUnitImpulseB = rotationPerUnitImpulseB.cross(pointB);
			
			deltaV += velocityPerUnitImpulseB.dot(contactData.get(0).normal);
			deltaV += contactB.massInv;
			
			Vec3 velImpulseRelContactB = worldToContact.multVec(velocityPerUnitImpulseB);
			float angularCompB = velImpulseRelContactB.x;//angular component of the current change in velocity
			
			velocity.add(contactB.angVel.cross(pointB));//add rotational velocity for B
			velocity.add(contactB.velocity);//add linear velocity for B
		}
		
		Vec3 contactVelocity = worldToContact.multVec(velocity);
//		velocity.print();
//		worldToContact.print();
		float restitution = (contactA.restitution > contactB.restitution ? contactB.restitution : contactA.restitution);
		float desiredDeltaV = -(1 + restitution)*contactVelocity.x;
		Vec3 impulse = contactToWorld.multVec(new Vec3(desiredDeltaV/deltaV, 0, 0));
		
//		System.out.println(desiredDeltaV);
//		System.out.println(deltaV);
		
//		contactA.velocity.add(new Vec3(impulse).scale(contactA.massInv));
		contactA.velocity.add(impulse.inverse().scale(contactA.massInv));
//		contactA.angVel.add(contactA.inertiaTensor.multVec(pointA.cross(impulse)));
		contactA.angVel.add(contactA.inertiaTensor.multVec(pointA.cross(impulse.inverse())));
		
		if(contactB.mass != 0){
			contactB.velocity.add(new Vec3(impulse).scale(contactB.massInv));
//			contactB.velocity.add(impulse.inverse().scale(contactB.massInv));
			contactB.angVel.add(contactB.inertiaTensor.multVec(pointB.cross(impulse)));
//			contactB.angVel.add(contactB.inertiaTensor.multVec(pointB.cross(impulse.inverse())));
		}
	}*/
}
