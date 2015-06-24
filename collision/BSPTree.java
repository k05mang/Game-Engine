package collision;
import java.util.ArrayList;

import glMath.Vec3;
import core.GameObject;

public class BSPTree {
	
	private BSPNode root;
	
	public BSPTree(){
		
	}
	
	public BSPTree(Vec3 center, Vec3 planeNormal){
		
	}
	
	public BSPTree(Vec3 center, Vec3 planeNormal, ArrayList<GameObject> starterObjects){
		
	}
	
	protected class BSPNode{
		protected BSPNode left, right;
		
		public BSPNode(){
			left = null;
			right = null;
		}
		
		public boolean isLeaf(){
			return left == null && right == null;
		}
	}
}
