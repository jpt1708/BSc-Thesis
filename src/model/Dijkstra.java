package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.jung.graph.util.Pair;

import model.components.Link;
import model.components.Node;

public class Dijkstra {
	
	@SuppressWarnings("unchecked")
	public static double[][][]  GLM (Request req, Substrate sub, double[][] capTable, LinkedHashMap<Node, Node>  map ) {
		
		double[][][] flowReserved= new double[req.getGraph().getEdgeCount()][sub.getGraph().getVertexCount()]
		                                                                        [sub.getGraph().getVertexCount()];
		//Number of substrate nodes
		int initSubNodeNum=sub.getGraph().getVertexCount();
				
		//--------------------------------------------------------------------->
		//Denote the shortest path by maximizing the bw capacity of the path
		//--------------------------------------------------------------------->
		for (int i=0;i<initSubNodeNum;i++){
			for (int j=0;j<initSubNodeNum;j++){
				if (capTable[i][j]!=0){
					capTable[i][j]= 1/capTable[i][j];
				}
			}
		}
				
	
		//flag2 is false if link mapping cannot been done
		boolean flag=true;
		
		
		for (Link link:req.getGraph().getEdges()){
			Pair<Node> x =  req.getGraph().getEndpoints(link);
			//the source virtual node
			int virt_source=x.getFirst().getId();
			//the destination virtual node
			int virt_dest = x.getSecond().getId();
			
			//the source substrate node
			int source=0;
			//the destination substrate node
			int dest=0;
			
			//find which substrates nodes correspond to virtual source and destination
		    @SuppressWarnings("rawtypes")
			Set entries = map.entrySet();
		    @SuppressWarnings("rawtypes")
			Iterator iterator = entries.iterator();
		    while (iterator.hasNext()) {
		         @SuppressWarnings("rawtypes")
				Map.Entry entry = (Map.Entry)iterator.next();
		         if (virt_source==((Node)entry.getKey()).getId()){
		        	 source=((Node)entry.getValue()).getId();
		         }
		         if (virt_dest==((Node)entry.getKey()).getId()){
		        	 dest=((Node)entry.getValue()).getId();
		         }
		    }
		    
		    //The bandwidth demand of the current virtual link
		    double demand= (double)1/link.getBandwidth();
			//Run the Dijkstra
		    int [] pred= dijkstra(sub, source, capTable, demand);
			//Contain the substrate path
			ArrayList<Integer> path=new ArrayList<Integer>();
			//Check if Dijkstra returns a valid path if not denial
			if (pred==null){
				  System.out.println("does it break?");
				flag=false;
				break;
			}
			else{
				path =returnPath (sub, pred, source, dest);
			}
			
			if (path.get(path.size()-1)!=dest){
				flag=false;
				break;
			}

			//store the mapping and update the available capacities of the substrate links
			for (int g=0;g<path.size()-1;g++){
				flowReserved[link.getId()][path.get(g)][path.get(g+1)]=flowReserved[link.getId()][path.get(g)][path.get(g+1)]+link.getBandwidth();
							
				//update the initial remaining capacity
				capTable[path.get(g)][path.get(g+1)]=1/capTable[path.get(g)][path.get(g+1)]-1/Math.abs(demand);
				//reverse again the updated capacity
				capTable[path.get(g)][path.get(g+1)]=1/capTable[path.get(g)][path.get(g+1)];
				//take into consideration undirected flows
				capTable[path.get(g+1)][path.get(g)]=capTable[path.get(g)][path.get(g+1)];
				
			}
		}
		

		if (flag==false){
			return null;
		}
		else{
			return flowReserved;
		}
	}
	
	
	public static int[] dijkstra(final Substrate G, final int s, final double[][] capTable, final double demand){
		 double [] dist = new double [G.getGraph().getVertexCount()];  // shortest known distance from "s"
		 final int [] pred = new int [G.getGraph().getVertexCount()];  // preceeding node in path
		 final boolean [] visited = new boolean [G.getGraph().getVertexCount()]; // all false initially
		 
		 for (int i=0; i<dist.length; i++) {
			 dist[i] = Integer.MAX_VALUE;
		 }
		 dist[s] = 0;
		 //Check if the node mapping
		 boolean ctrl=true;
		
		 for (int i=0; i<dist.length; i++) {
			 int next = minVertex (dist, visited);
			 if (next==-1){
				 ctrl=false;
				 break;
			 }
			 visited[next] = true;
		 
			 // The shortest path to next is dist[next] and via pred[next].
			 ArrayList<Integer> n= new ArrayList<Integer>();
			//Find neighbors of next that satisfy the link requirements
			 for (int j=0;j<G.getGraph().getVertexCount();j++){
				if (capTable[next][j]!=0){
					if(capTable[next][j]<Math.abs(demand)){
						n.add(j);
					}	
				}
			}
			 
			 for (int j=0; j<n.size(); j++) {
		 
				 int v = n.get(j);		 
				 double d = dist[next] + capTable[next][v];
				 if (dist[v] > d) {
					 dist[v] = d;
					 pred[v] = next;
				 }
			 }
		 }
		  // System.out.println(ctrl);
      if (ctrl==true)
    	  return pred;
      else
    	  return null;
 
	}
	

	
	
	public static int minVertex (double [] dist, boolean [] v) {
		double x = Integer.MAX_VALUE;
		int y = -1;   // graph not connected, or no unvisited vertices
		//System.out.println( " minVertex :  " + dist.length);
		for (int i=0; i<dist.length; i++) {
		      if (!v[i] && dist[i]<x) {y=i; x=dist[i];}
		}
		return y;
	}
	
	@SuppressWarnings("rawtypes")
	public static  ArrayList returnPath (Substrate G, int [] pred, int s, int e) {
		ArrayList<Integer> path = new ArrayList<Integer>();
		int x = e;
		while (x!=s) {
			path.add (0, x);
			x = pred[x];
		}
		path.add (0, s);
	 
		return path;
	}


}
