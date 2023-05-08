package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;
import model.components.Link;
import model.components.Node;

public class AllPaths {

    private  List<List<Link>> allPaths;
    private  ArrayList<Node> visited = new  ArrayList<Node> ();

    public  List<List<Link>> getAllPathsBetweenNodes(DirectedGraph<Node, Link> graph, 
    		Node startNode, Node endNode) {
        allPaths = new ArrayList<List<Link>>();

        List<Link> currentPath = new ArrayList<Link>();

        findAllPaths(startNode, startNode, endNode, currentPath, graph);

        return allPaths;
    }

    private  void findAllPaths(Node currentNode, Node startNode, Node endNode, 
    		List<Link> currentPath, DirectedGraph<Node, Link> graph) {
        Collection<Link> outgoingEdges = graph.getOutEdges(currentNode);
       
        Node outNode =null;
        //System.out.println("currentNode: " +currentNode);
        //System.out.println(outgoingEdges);
        //System.exit(0);
        for (Link outEdge : outgoingEdges) {
        	Pair<Node> currentNodes =graph.getEndpoints(outEdge);	
        	if (currentNode.equals(currentNodes.getFirst())){
        		outNode = currentNodes.getSecond();
        	} else if (currentNode.equals(currentNodes.getSecond())){
        		outNode = currentNodes.getFirst();
            }
        	System.out.println("outNode " + outNode);
        	
        	printPath(currentPath,graph);
        	if (!visited.contains(outNode)){
	        	this.visited.add(outNode);
	            if (outNode.equals(startNode)) {
	                List<Link> cyclePath = new ArrayList<Link>(currentPath);
	                cyclePath.add(outEdge);
	                System.out.println("Found cycle provoked by path " + cyclePath);
	                continue;
	            }
	
	            List<Link> newPath = new ArrayList<Link>(currentPath);
	            newPath.add(outEdge);
	
	            if (outNode.equals(endNode)) {
	                allPaths.add(newPath);
	                continue;
	            }
	
	            findAllPaths(outNode, startNode, endNode, newPath, graph);
        	}
        }
    }
    
    void printPath(List<Link> path, DirectedGraph<Node, Link> graph){
    	ArrayList<Node> nodes= new ArrayList<Node>();
    	Set<Node> nd=new HashSet<>();
    	for (Link  x:path){
    		Pair<Node> currentPathNodes =graph.getEndpoints(x);
    		nd.add(currentPathNodes.getFirst());
    		nd.add(currentPathNodes.getSecond());
    	}
    	System.out.println();
    	for (Node temp : nd) {
			System.out.print(temp.getId()+" ");
		}
    	System.out.println();
    }
}
