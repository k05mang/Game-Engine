package primitives;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;

public class Triangle {
	
	public int[] primitive, adjacent;
	public Edge[] edges;
	public ArrayList<HalfEdge> halfEdges;
	//public boolean isStored;
	public static final int INDEX_ADJ = 6, INDEX_NOADJ = 3;
	
	/* normal - normal of the triangle face
	 * 
	 * primitive - Array list of integers referring to the indices of the
	 * 
	 * vertices that comprise the primitive face
	 * 
	 * adjacent - Array list of integers referring to the indices of the 
	 * vertices that are adjacent to the edges of this triangle and create
	 * the adjacent triangles to this one
	 * 
	 * INDEX_COUNT - int referring to the number of indices in this triangle
	 */
	
	/**
	 * Constructs a triangle primitive from a set of given vertices
	 * the vertices are assumed to be given in a counter clockwise ordering
	 * 
	 * @param v0 First vertex of the triangle 
	 * @param v1 Second vertex of the triangle 
	 * @param v2 Third vertex of the triangle 
	 */
	public Triangle(Integer v0, Integer v1, Integer v2/*, boolean hasAdjacent*/){
		//isStored = false;
		
		primitive = new int[]{v0.intValue(), v1.intValue(), v2.intValue()};
		adjacent = new int[]{-1, -1, -1};
		
		edges = new Edge[]{
				new Edge(v0,v1),
				new Edge(v1,v2),
				new Edge(v2,v0)};
		
		halfEdges = new ArrayList<HalfEdge>(3);
	}
	
	/**
	 * Stores the indices of the vertices representing this triangles primitive in a buffer
	 * 
	 * @param storage Integer buffer to store the indices in
	 */
	public void storePrimitiveIndices(IntBuffer storage){
		storage.put(primitive[0]);
		storage.put(primitive[1]);
		storage.put(primitive[2]);
	}
	
	/**
	 * Stores all the index information for this triangle, that is, the primitive indices
	 * as well as the adjacent indices, the indices are store in a manner coherent with the
	 * OpenGL recommendation for storing adjacency information, to allow for rendering without
	 * the need for a geometry shader
	 * 
	 * @param storage Integer buffer to store the indices
	 */
	public void storeAllIndices(IntBuffer storage){
//		hasAdjacent canbe used to determine whether to store the primitve or all
//		this makes for a single function to be called 
		storage.put(primitive[0]);
		storage.put(adjacent[0]);
		
		storage.put(primitive[1]);
		storage.put(adjacent[1]);
		
		storage.put(primitive[2]);
		storage.put(adjacent[2]);
	}

	public void initAdjacent(){
		adjacent[0] = halfEdges.get(0).opposite.next.next.sourceVert;
		adjacent[1] = halfEdges.get(1).opposite.next.next.sourceVert;
		adjacent[2] = halfEdges.get(2).opposite.next.next.sourceVert;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Triangle){
			Triangle cast = (Triangle)o;
			return cast.primitive[0] == primitive[0] && 
				   cast.primitive[1] == primitive[1] && 
				   cast.primitive[2] == primitive[2] &&
				   edges[0].equals(cast.edges[0]) &&
				   edges[1].equals(cast.edges[1]) && 
				   edges[2].equals(cast.edges[2]);
		}else{
			return false;
		}
	}
	
	public void write(DataOutputStream stream) throws IOException{
		stream.writeShort(primitive[0]);
		stream.writeShort(primitive[1]);
		stream.writeShort(primitive[2]);
	}
	
	public void writeAll(DataOutputStream stream) throws IOException{
		stream.writeShort(primitive[0]);
		stream.writeShort(adjacent[0]);
		
		stream.writeShort(primitive[1]);
		stream.writeShort(adjacent[1]);
		
		stream.writeShort(primitive[2]);
		stream.writeShort(adjacent[2]);
	}
	
	public static class  Edge {
		public Integer start, end;
		private int hashCode;
		
		public Edge(Integer start, Integer end){
			this.start = start;
			this.end = end;
		    hashCode = (1 << 25)*(start+1)+end;
		}
		
		@Override
		public int hashCode(){
			return hashCode;
		}
		
		@Override
		public boolean equals(Object obj){
			if(obj instanceof Edge)
				return ((Edge)obj).start.equals(this.start) && ((Edge)obj).end.equals(this.end);
			else
				return false;
		}
	}
	
	public static class HalfEdge {
		public Triangle parent;
		public HalfEdge opposite, next;
		public Integer sourceVert;
		
		public HalfEdge(Integer emenatingVert){
			sourceVert = emenatingVert;
		}
		
		public HalfEdge(HalfEdge copy){
			parent = copy.parent;
			opposite = copy.opposite;
			next = copy.next;
			sourceVert = copy.sourceVert;
		}
		
		@Override
		public boolean equals(Object o){
			if(o instanceof HalfEdge){
				HalfEdge cast = (HalfEdge)o;
				return sourceVert == cast.sourceVert && parent.equals(cast.parent);
			}else{
				return false;
			}
		}
		
		public void set(HalfEdge setter){
			parent = setter.parent;
			opposite = setter.opposite;
			next = setter.next;
			sourceVert = setter.sourceVert;
		}
	}
}
