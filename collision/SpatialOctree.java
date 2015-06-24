package collision;

import java.util.ArrayList;

import core.GameObject;
import glMath.Vec3;
import glMath.VecUtil;

public class SpatialOctree {
	//first half of nodes will represent the upper portion of space
	//the nodes will read in counter clockwise from the -z, +x quadrant
	private OctreeNode root;
	private Vec3 halfDim, center;
	private ArrayList<GameObject> gameObjects;
	private int leafCap, maxDepth;
	
//	public SpatialOctree(int leafCapacity, int depthCap){
//		root = new OctreeNode();
//		center = new Vec3();
//		halfDim = new Vec3();
//		leafCap = leafCapacity;
//		gameObjects = new ArrayList<GameObject>();
//	}
//	
//	public SpatialOctree(int leafCapacity, int depthCap, ArrayList<GameObject> initializer){
//		root = new OctreeNode();
//		center = new Vec3();
//		halfDim = new Vec3();
//		leafCap = leafCapacity;
//		gameObjects = new ArrayList<GameObject>();
//		for(GameObject obj : initializer){
//			add(obj);
//		}
//	}
	
	public SpatialOctree(int leafCapacity, int depthCap, Vec3 center, Vec3 dimensions){
		this(leafCapacity, depthCap, center.x, center.y, center.z, dimensions.x, dimensions.y, dimensions.z);
	}
	
	public SpatialOctree(int leafCapacity, int depthCap, float cX, float cY, float cZ, Vec3 dimensions){
		this(leafCapacity, depthCap, cX, cY, cZ, dimensions.x, dimensions.y, dimensions.z);
	}
	
	public SpatialOctree(int leafCapacity, int depthCap, Vec3 center, float dX, float dY, float dZ){
		this(leafCapacity, depthCap, center.x, center.y, center.z, dX, dY, dZ);
	}
	
	public SpatialOctree(int leafCapacity, int depthCap, float cX, float cY, float cZ, float dX, float dY, float dZ){
		center = new Vec3(cX, cY, cZ);
		halfDim = new Vec3(dX/2.0f, dY/2.0f, dZ/2.0f);
		root = new OctreeNode(null, this.center, this.halfDim, 0);
		leafCap = leafCapacity;
		maxDepth = depthCap;
		gameObjects = new ArrayList<GameObject>();
	}
	
	public SpatialOctree(int leafCapacity, int depthCap, Vec3 center, Vec3 dimensions, ArrayList<GameObject> initializer){
		this(leafCapacity, depthCap, center.x, center.y, center.z, dimensions.x, dimensions.y, dimensions.z, initializer);
	}
	
	public SpatialOctree(int leafCapacity, int depthCap, Vec3 center, float dX, float dY, float dZ, ArrayList<GameObject> initializer){
		this(leafCapacity, depthCap, center.x, center.y, center.z, dX, dY, dZ, initializer);
	}
	
	public SpatialOctree(int leafCapacity, int depthCap, float cX, float cY, float cZ, Vec3 dimensions, ArrayList<GameObject> initializer){
		this(leafCapacity, depthCap, cX, cY, cZ, dimensions.x, dimensions.y, dimensions.z, initializer);
	}
	
	public SpatialOctree(int leafCapacity, int depthCap, float cX, float cY, float cZ, float dX, float dY, float dZ, ArrayList<GameObject> initializer){
		center = new Vec3(cX, cY, cZ);
		halfDim = new Vec3(dX/2.0f, dY/2.0f, dZ/2.0f);
		root = new OctreeNode(null, this.center, this.halfDim, 0);
		leafCap = leafCapacity;
		maxDepth = depthCap;
		gameObjects = new ArrayList<GameObject>();
		for(GameObject obj : initializer){
			add(obj);
		}
	}
	
	/**
	 * Adds a game object to the tree, if the object cannot fit into the trees primary bounds then it will not be added
	 * 
	 * @param object Game object to be added
	 * @return True if the object was added, false if it wasn't due to out of bounds volumes
	 */
	public boolean add(GameObject object){
		//alternative might be to have the tree dynamically resize as items are added and removed
		Vec3 relObjCenter = (Vec3)VecUtil.subtract(object.getPosition(), root.volume.getCenter());
		AABB objVolume = object.getBoundingVolume();
		Vec3 objHalfDim = objVolume.getHalfDimensions();

		boolean x = relObjCenter.x+objHalfDim.x < halfDim.x && relObjCenter.x-objHalfDim.x > -halfDim.x;
		boolean y = relObjCenter.y+objHalfDim.y < halfDim.y && relObjCenter.y-objHalfDim.y > -halfDim.y;
		boolean z = relObjCenter.z+objHalfDim.z < halfDim.z && relObjCenter.z-objHalfDim.z > -halfDim.z;
		
		if(x && y && z){
			gameObjects.add(object);
			root.add(gameObjects.size()-1, object, 0);
		}else{
			return false;
		}
		return true;
	}
	
	public void remove(GameObject object){
		//traverse the tree then iterate through the indices of the leaf node to find the array list element
	}
	
	public void update(){
		
	}
	
	private class OctreeNode{
		protected ArrayList<Integer> gameIndices;
		protected OctreeNode[] nodes;
		protected OctreeNode parent;
		protected AABB volume;
		
		/**
		 * Constructs an octree node
		 * 
		 * @param parent Parent node of this tree node, if null then this node is the root node
		 * @param parentCenter Center point of the parent node
		 * @param parentHalfDim Half dimensions of the parent node
		 * @param index Index that represents what quadrant of the parent this node is meant to represent
		 */
		public OctreeNode(OctreeNode parent, Vec3 parentCenter, Vec3 parentHalfDim, int index){
			gameIndices = new ArrayList<Integer>(leafCap);
			if(parent == null){
				this.parent = null;
				volume = new AABB(parentHalfDim.x*2.0f, parentHalfDim.y*2.0f, parentHalfDim.z*2.0f);
				volume.translate(parentCenter);
			}else{
				this.parent = parent;
				volume = new AABB(parentHalfDim.x*.5f, parentHalfDim.y*.5f, parentHalfDim.z*.5f);
				switch(index){
					//first 4 are upper half of node
					case 0:
						volume.translate(parentCenter.x-halfDim.x, parentCenter.y-halfDim.y, parentCenter.z+halfDim.z);
						break;
					case 1:
						volume.translate(parentCenter.x+halfDim.x, parentCenter.y-halfDim.y, parentCenter.z+halfDim.z);
						break;
					case 2:
						volume.translate(parentCenter.x+halfDim.x, parentCenter.y-halfDim.y, parentCenter.z-halfDim.z);
						break;
					case 3:
						volume.translate(parentCenter.x-halfDim.x, parentCenter.y-halfDim.y, parentCenter.z-halfDim.z);
						break;
					case 4:
						volume.translate(parentCenter.x-halfDim.x, parentCenter.y+halfDim.y, parentCenter.z+halfDim.z);
						break;
					case 5:
						volume.translate(parentCenter.x+halfDim.x, parentCenter.y+halfDim.y, parentCenter.z+halfDim.z);
						break;
					case 6:
						volume.translate(parentCenter.x+halfDim.x, parentCenter.y+halfDim.y, parentCenter.z-halfDim.z);
						break;
					case 7:
						volume.translate(parentCenter.x-halfDim.x, parentCenter.y+halfDim.y, parentCenter.z-halfDim.z);
						break;
					default:
						volume.translate(parentCenter);
						break;
				}
			}
		}
		
		/**
		 * Adds a Game Object to the tree, passing the object down the tree finding the proper node to place it in
		 * 
		 * @param index Index into the primary game objects array where the game object is stored
		 * @param object Actual game object to be added, this is used to check bounds and position against the node
		 * @param curDepth The current traversal depth of the tree
		 */
		public void add(int index, GameObject object, int curDepth){
			//check if we have reached the depth cap for the tree, at this point the object can only be added to the 
			//current node regardless of the leaf capacity set by the tree
			if(curDepth == maxDepth){
				gameIndices.add(index);
			}else{
				//check if this node has been split yet, if it has the continuing moving down the tree to find 
				//the leaf for the game object that is to be added
				if(nodes != null){
					//check whether the add object intersects the bounding volumes of the nodes and add it to the
					//node that contains it
					for(int curNode = 0; curNode < 8; curNode++){
						if(nodes[curNode].volume.colliding(object.getBoundingVolume())){
							nodes[curNode].add(index, object, curDepth+1);
						}
					}
				}else{
					//if there is room for this object at this level then add it
					if(gameIndices.size() < leafCap){
						gameIndices.add(index);
					}else{
						//break this node up
						split();
						//parse the objects that are contained in this node and distribute them to the sub nodes
						//that were made
						for(Integer objIndex : gameIndices){
							GameObject curObject = gameObjects.get(objIndex.intValue());
							//check which sub space to put the object in
							for(int curNode = 0; curNode < 8; curNode++){
								if(nodes[curNode].volume.colliding(curObject.getBoundingVolume())){
									nodes[curNode].add(objIndex.intValue(), curObject, curDepth+1);
								}
							}
						}
						//after splitting this node into the sub nodes and distributing the current nodes objects among them
						//put the current object into the nodes it belongs
						for(int curNode = 0; curNode < 8; curNode++){
							if(nodes[curNode].volume.colliding(object.getBoundingVolume())){
								nodes[curNode].add(index, object, curDepth+1);
							}
						}
						//clear the indices stored at this level since this node is no longer a leaf
						gameIndices.clear();
					}
				}
			}
		}
		
		public void split(){
			Vec3 center = volume.getCenter();
			Vec3 hlafDimensions = volume.getHalfDimensions();
			nodes = new OctreeNode[]{
					new OctreeNode(this, center, hlafDimensions, 0),
					new OctreeNode(this, center, hlafDimensions, 1),
					new OctreeNode(this, center, hlafDimensions, 2),
					new OctreeNode(this, center, hlafDimensions, 3),
					new OctreeNode(this, center, hlafDimensions, 4),
					new OctreeNode(this, center, hlafDimensions, 5),
					new OctreeNode(this, center, hlafDimensions, 6),
					new OctreeNode(this, center, hlafDimensions, 7)
			};
		}
	}
}
