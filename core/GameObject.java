package core;

import java.util.ArrayList;
import java.util.HashMap;

import primitives.Vertex;
import collision.AABB;
import collision.CollisionData;
import collision.CollisionDetector;
import collision.ContactPair;
import glMath.Mat3;
import glMath.Quaternion;
import glMath.Vec3;
import glMath.VecUtil;
import renderers.Renderable;

public class GameObject{

	public Renderable mesh;
	public Vec3 velocity, angVel, position, prevVel, prevPos, origPos, origVel, origAngVel;
	public float restitution, mass, massInv, gravity, sFriction, dFriction;
	private boolean isFixed;
	public Mat3 invInertiaTensor;
	public Quaternion origOrientation, prevOrient;
	private HashMap<ContactPair, ContactPair> contacts;
	
	public GameObject(Renderable mesh, float staticF, float dynamicF){
		this(mesh, 0,0,0, staticF, dynamicF);
	}
	
	public GameObject(Renderable mesh, Vec3 position, float staticF, float dynamicF){
		this(mesh, position.x, position.y, position.z, staticF, dynamicF);
	}
	
	public GameObject(Renderable mesh, float cX, float cY, float cZ, float staticF, float dynamicF){
		this(mesh, cX, cY, cZ, 0,0,0, 0, 0, 0, staticF, dynamicF, false);
	}
	
	public GameObject(Renderable mesh,  Vec3 position, Vec3 velocity,  
			float elasticity, float mass, float gravity, float staticF, float dynamicF, boolean isFixed){
		this(mesh, position.x, position.y, position.z, 
				velocity.x, velocity.y, velocity.z,
				elasticity, mass, gravity, staticF, dynamicF, isFixed);
	}
	
	public GameObject(Renderable mesh,  Vec3 position, float vX, float vY, float vZ, 
			float elasticity, float mass, float gravity, float staticF, float dynamicF, boolean isFixed){
		this(mesh, position.x, position.y, position.z, 
				vX, vY, vZ, 
				elasticity, mass, gravity, staticF, dynamicF, isFixed);
	}
	
	public GameObject(Renderable mesh, float cX, float cY, float cZ, Vec3 velocity,
			float elasticity, float mass, float gravity, float staticF, float dynamicF, boolean isFixed){
		this(mesh, cX, cY, cZ, 
				velocity.x, velocity.y, velocity.z,
				elasticity, mass, gravity, staticF, dynamicF, isFixed);
	} 
	
	public GameObject(Renderable mesh,  float cX, float cY, float cZ, float vX, float vY, float vZ,
			float elasticity, float mass, float gravity, float staticF, float dynamicF, boolean isFixed){
		this.mesh = mesh.copy();
		this.mesh.translate(cX, cY, cZ);
		
		this.position = new Vec3(cX, cY, cZ);
		this.origPos = new Vec3(cX, cY, cZ);
		this.prevPos = new Vec3(cX, cY, cZ);
		
		this.origOrientation = new Quaternion(this.mesh.getOrientation());
		
		this.restitution = elasticity;
		this.mass = mass;
		this.massInv = mass == 0 ? 0 : 1/mass;
		this.gravity = gravity;
		
		contacts = new HashMap<ContactPair, ContactPair>();
		
		this.velocity = new Vec3(vX, vY, vZ);
		this.origVel = new Vec3(vX, vY, vZ);
		this.prevVel = new Vec3(vX, vY, vZ);
		this.angVel = new Vec3();
		this.origAngVel = new Vec3();
		
		this.isFixed = isFixed;
		
		this.sFriction = staticF;
		this.dFriction = dynamicF;
//		invInertiaTensor = mesh.computeTensor(mass);//results are not the same check the contact pair code, the bug 
//		invInertiaTensor.print();
		computeTensor();
//		invInertiaTensor.print();
	}
	
	private void computeTensor(){
		//possibly put a check in for different primitive shapes for faster computation
		ArrayList<Vertex> verts = mesh.getVertices();
		float momentX = 0;
		float momentY = 0;
		float momentZ = 0;
		float productXY = 0;
		float productXZ = 0;
		float productYZ = 0;
		float pointMass = mass/verts.size();
		for(Vertex vert : verts){
			Vec3 vertexPos = vert.getPos();
			/*get the vector representing the distance of the point from the axis
			this can be found by projecting the point onto the given axis then subtracting
			from this point that vector to give us the orthogonal vector in the direction
			of the point*/
			Vec3 vecFromX = (Vec3)VecUtil.subtract(vertexPos, vertexPos.proj(VecUtil.xAxis));
			Vec3 vecFromY = (Vec3)VecUtil.subtract(vertexPos, vertexPos.proj(VecUtil.yAxis));
			Vec3 vecFromZ = (Vec3)VecUtil.subtract(vertexPos, vertexPos.proj(VecUtil.zAxis));
			//moment of inertia is computed as the sum of the point mass times the squared distance from the 
			//axis in question
			momentX += pointMass*vecFromX.dot(vecFromX);
			momentY += pointMass*vecFromY.dot(vecFromY);
			momentZ += pointMass*vecFromZ.dot(vecFromZ);
			productXY += pointMass*vertexPos.x*vertexPos.y;
			productXZ += pointMass*vertexPos.x*vertexPos.z;
			productYZ += pointMass*vertexPos.y*vertexPos.z;
		}
		invInertiaTensor = new Mat3(
				new Vec3(momentX, -productXY, -productXZ),
				new Vec3(-productXY, momentY, -productYZ),
				new Vec3(-productXZ, -productYZ, momentZ)
				);
//		inertiaTensor.print();
		if(mass > 0){
			invInertiaTensor.invert();
		}
	}
	
	public void checkCollision(GameObject collision){
		CollisionData data = CollisionDetector.gjkIntersect(mesh.getCollisionMesh(), collision.mesh.getCollisionMesh());
//		data.print();
		if(data.areColliding){
//			data.print();
			ContactPair resolver = new ContactPair(this, collision);
			resolver.addContact(data);
			resolver.resolve(1);
		}
	}
	
	protected void updateContacts(Vec3 linearMove, Quaternion rotMove){
		
	}
	
//	public ContactPair checkCollision(GameObject collision){
//		CollisionData data = CollisionDetector.gjkIntersect(collider, collision.collider);
////		data.print();
//		if(data.areColliding){
////			data.print();
//			return new ContactPair(this, collision, data);
//		}
//		return null;
//	}
	
	public void update(){
		velocity.add(0, gravity, 0);
		mesh.translate(velocity);
		mesh.setOrientation(mesh.getOrientation().addVector(angVel));
		angVel.scale(.85f);
		position.add(velocity);
	}
	
	public void render(){
		mesh.render();
	}
	
	public void render(ShaderProgram shader, String model, String normalMat){
		shader.setUniform(model, mesh.getMatrixBuffer());
		shader.setUniform(normalMat, mesh.getNMatrixBuffer());
		mesh.render();
	}
	
	public void render(ShaderProgram shader, String model){
		shader.setUniform(model, mesh.getMatrixBuffer());
		mesh.render();
	}
	
	public void orient(float x, float y, float z, float theta){
		mesh.orient(x,y,z,theta);
	}
	
	public void orient(Vec3 axis, float theta){
		mesh.orient(axis,theta);
	}
	
	public void delete(){
		mesh.delete();
	}
	
	public float getElasticity() {
		return restitution;
	}

	public void setElasticity(float elasticity) {
		this.restitution = elasticity;
	}

	public float getMass() {
		return mass;
	}

	public void setMass(float mass) {
		this.mass = mass;
	}

	public Vec3 getVelocity() {
		return velocity;
	}

	public void setVelocity(Vec3 velocity) {
		this.velocity.set(velocity);
	}
	
	public void setVelocity(float x, float y, float z) {
		this.velocity.set(x,y,z);
	}
	
	public void accelerate(float x, float y, float z){
		velocity.add(x,y,z);
	}
	
	public void accelerate(Vec3 acceleration){
		velocity.add(acceleration);
	}
	
	public void decelerate(float x, float y, float z){
		velocity.subtract(x,y,z);
	}

	public void decelerate(Vec3 acceleration){
		velocity.subtract(acceleration);
	}
	
	public float getGravity() {
		return gravity;
	}

	public void setGravity(float gravity) {
		this.gravity = gravity;
	}
	
	public Vec3 getMomentum(){
		return (new Vec3(velocity)).scale(mass);
	}
	
	public boolean isFixed(){
		return isFixed;
	}
	
	public void setFixed(boolean isFixed){
		this.isFixed = isFixed;
	}
	
	public Vec3 getPosition(){
		return mesh.getCollisionMesh().getCenter();
	}
	
	public AABB getBoundingVolume(){
		return mesh.getBoundingVolume();
	}
	
	public void reset(){
		this.position.set(origPos);
		this.velocity.set(origVel);
		this.angVel.set(origAngVel);
		
		mesh.resetModel();
		mesh.setOrientation(origOrientation);
		mesh.translate(origPos);
	}
}
