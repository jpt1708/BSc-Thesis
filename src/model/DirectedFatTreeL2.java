package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections15.Transformer;

import model.components.Link;
import model.components.Node;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;


public class DirectedFatTreeL2 {
	
	private DirectedSparseGraph<Node, Link> graph = new DirectedSparseGraph<Node, Link>();
	private Hypergraph<Node, Link> hgraph =   new DirectedSparseGraph<Node, Link>();
	private ArrayList<DCpod> podsArray = new ArrayList<DCpod>();
	private ArrayList<Node> torSwitches = new ArrayList<Node>();
	private ArrayList<Node> rootSwitches = new ArrayList<Node>();
	private Node rootElement;
	private boolean exists=false;
	private LinkedList<Node> current_parent = new LinkedList<Node>();
	private SubstrateNodeFactory nodeFactory = new SubstrateNodeFactory();
	private SubstrateLinkFactory linkFactory = new SubstrateLinkFactory();
	private HashMap<HashMap<Integer,Integer>, Node> podNodes =  new HashMap<HashMap<Integer,Integer>, Node>();
	private int pods = 4;
	private int ServersPerRack = 1;
	private int depth=3; //depth 2, k=2
	private int k_index=pods/2; //not used
	AllPathsDetector<Node, Link> allp = new  AllPathsDetector<Node, Link>();
	
public SubstrateNodeFactory getNodeFactory(){
	return this.nodeFactory;
}

public SubstrateLinkFactory getLinkFactory(){
	return this.linkFactory;
}


public Node getRootElement() {
    return this.rootElement;
}

public void setRootElement(Node root) {
     this.rootElement=root;
}


public DirectedSparseGraph<Node, Link> getFatTreeL2Graph() {
    return this.graph;
}

public void createFTGraph() {
	
	for (int j=0;j<pods;j++){ //create Root Switches
		Node rootSwitch = nodeFactory.create("switch");
	    this.graph.addVertex(rootSwitch);
	    rootSwitches.add(rootSwitch);
	    System.out.println("aaaaaaaa" + rootSwitch.getId());
	}
	
	for (int i=0;i<pods;i++){ //create pods
		DCpod pod = new DCpod();
		if (i!=0){
			pod.setNodeFactory(podsArray.get(i-1).getNodeFactory());
			pod.setLinkFactory(podsArray.get(i-1).getLinkFactory());
		}else{
			pod.setNodeFactory(nodeFactory);
			pod.setLinkFactory(linkFactory);
		}
		pod.createPodGraph(pods);
		//DirectedSparseGraph<Node, Link> cpod = pod.getGraph();
		//System.out.println(cpod);
		podsArray.add(pod);
	}
	nodeFactory = podsArray.get(pods-1).getNodeFactory();
	linkFactory =podsArray.get(pods-1).getLinkFactory();
	
	for (DCpod current: podsArray){
		ArrayList<Node> aggSwitches = current.getAggSwitches();
		 for (Node node:current.getGraph().getVertices()){
			 this.graph.addVertex(node);
		 }
		 for (Link link:current.getGraph().getEdges()){
			 Pair<Node> endpoints = current.getGraph().getEndpoints(link);
			 this.graph.addEdge(link, endpoints,EdgeType.DIRECTED);
		 }
		 
		for (int ii=0;ii< aggSwitches.size();ii++){
			System.out.println(rootSwitches.size());
			System.out.println(aggSwitches.size());
			createLink(rootSwitches.get(2*ii),aggSwitches.get(ii));
			createLink(rootSwitches.get(2*ii+1),aggSwitches.get(ii));
		}
	}
	System.out.println(this.graph);
	System.out.println(this.graph.getEdgeCount());
	System.out.println(this.graph.getVertexCount());
	
	
	
/*	findPathsRootTor();
	findPathsInTor();
	findPathsOutTor();
	findPathsTorVM();
	System.out.println("All paths"); //one direction taken though they are bidirectional.
	System.out.println(this.allp.getPaths());
	createHyperGraph();*/
}

private void createHyperGraph(){
	for (Node node: this.graph.getVertices()){
		this.hgraph.addVertex(node);
	}
	for (List<Link> llink: this.allp.getPaths()){
		System.out.println(llink);
		for (Link link: llink){
			 Pair<Node> endpoints = this.graph.getEndpoints(link);
		}
		//this.hgraph.addVertex(node);
	}
}

private void findPathsRootTor(){
	AllPathsDetector<Node, Link> allp_tmp = new  AllPathsDetector<Node, Link>();
	
	for (Node root: rootSwitches){
		for (DCpod current: podsArray){
			for (Node TORswitch: current.getTORSwitches()){
				int hops=0;
				List<List<Link>> allPaths= new ArrayList<List<Link>>();
				while (allPaths.isEmpty()){
				System.out.println(hops + " " + root.getId()+" " +TORswitch.getId());
				allPaths =allp_tmp.getAllPathsBetweenNodes(this.graph, 
			    		root, TORswitch, hops);
					if (!allPaths.isEmpty()){
						System.out.println(allPaths);
						this.allp.addPaths(allPaths);
					}
				hops++;
				}
			}
		}
	}
	
}

private void findPathsInTor(){

	for (DCpod current: podsArray){
		for (int i=0; i<current.getTORSwitches().size()-1;i++){
			for (int j=1;j< current.getTORSwitches().size(); j++){
				int hops=0;
				List<List<Link>> allPaths= new ArrayList<List<Link>>();
				Node start=current.getTORSwitches().get(i);
				Node end=current.getTORSwitches().get(j);
				while (allPaths.isEmpty()){
				System.out.println(hops + " " + start.getId()+" " +end.getId());
				allPaths =allp.getAllPathsBetweenNodes(this.graph, 
			    		start, end, hops);
					if (!allPaths.isEmpty()){
						System.out.println(allPaths);
						this.allp.addPaths(allPaths);
					}
				hops++;
				}
			}
		}
	}

}

private void findPathsOutTor(){

	for (DCpod current: podsArray){
		for (int i=0; i<current.getTORSwitches().size()-1;i++){
			for (DCpod current1: podsArray){
				if (!current.equals(current1)){
					for (int j=1;j< current1.getTORSwitches().size(); j++){
						int hops=0;
						List<List<Link>> allPaths= new ArrayList<List<Link>>();
						Node start=current.getTORSwitches().get(i);
						Node end=current1.getTORSwitches().get(j);
						while (allPaths.isEmpty()){
						System.out.println(hops + " " + start.getId()+" " +end.getId());
						allPaths =allp.getAllPathsBetweenNodes(this.graph, 
					    		start, end, hops);
							if (!allPaths.isEmpty()){
								System.out.println(allPaths);
								this.allp.addPaths(allPaths);
							}
						hops++;
						}
					}
				}
			}
		}
	}

}

private void findPathsTorVM(){

	for (DCpod current: podsArray){
		for (Node TORswitch: current.getTORSwitches()){
			for (Node server: current.getpodServers()){
				int hops=1;
				List<List<Link>> allPaths= new ArrayList<List<Link>>();
				while (allPaths.isEmpty()){
				System.out.println(hops + " " + TORswitch.getId()+" " +server.getId());
				allPaths =allp.getAllPathsBetweenNodes(this.graph, 
						TORswitch, server, hops);
					if (!allPaths.isEmpty()){
						System.out.println(allPaths);
						this.allp.addPaths(allPaths);
					}
				hops++;
				if (hops>1);break;
				}
			}
		}
	}

}

private void createLink(Node parent, Node child){
	Link link,rlink;
	String type = child.getType();
	//System.out.println(type);
	
	if (type.equalsIgnoreCase("Server")){
		link=linkFactory.create("torlink");	
		rlink=linkFactory.create("torlink");	
	}
	else {
		link=linkFactory.create("interracklink");
		rlink=linkFactory.create("interracklink");
	}
    this.graph.addEdge(link, parent, child,EdgeType.DIRECTED);
    this.graph.addEdge(rlink, child, parent,EdgeType.DIRECTED);
}
///////////////

public void createFT1Graph() {

	for (int i=0;i<pods;i++){
		Node rootSwitch = nodeFactory.create("switch");
	    this.graph.addVertex(rootSwitch); 
	    int linkedPodSwitch = 0;
	    for (int j=0;j<pods;j++){
	    	int current_depth = 1;
	    	LinkedList<Node> parent = new LinkedList<Node>();
	    	parent.push(rootSwitch);
	    	exists=false;
	    	while (current_depth <= this.depth){
	    		HashMap<Integer,Integer> tmp =new HashMap<Integer,Integer>();
	    		tmp.put(j,linkedPodSwitch);
	        	if (current_depth!=this.depth) {
	        		int iter = parent.size();
	        		for (int m=0;m<iter;m++){
	        			Node tmp_parent=parent.pop();
	    	    		System.out.println("Parent: " + tmp_parent);
	        			if (current_depth==1){k_index=1;}
	        			else {k_index=2;}
	        		    System.out.println("K" + k_index);
	        		    if (!exists){
		    		    	for (int l=0; l<k_index; l++){
		    		    		this.addChild(tmp_parent,current_depth, tmp);
		    		    		//System.out.println("Adding child for parent: "+ tmp_parent);
		    		    	}
	        		    }
	        		}
	        	} else {
	        		while (!parent.isEmpty()){
	        			this.addLeaf(parent.pop());
	        		}
	        	}
	        	current_depth++;
	        	while (!this.current_parent.isEmpty()){
	        		parent.push(this.current_parent.pop());
	        	}
	        }
	    	if (linkedPodSwitch==0) linkedPodSwitch = 1;
        	else linkedPodSwitch = 0;
	    }
	}
	System.out.println(this.graph);
	System.out.println(this.torSwitches.get(0) + " " + this.torSwitches.get(2));
	DijkstraShortestPath<Node, Link> alg = new DijkstraShortestPath(this.graph);
	 List<Link> path = alg.getPath(this.torSwitches.get(0), this.torSwitches.get(2));
	 System.out.println(path);
	 System.out.println("++++++++++");
	AllPathsDetector<Node, Link> allp = new  AllPathsDetector<Node, Link>();
	   // DijkstraShortestPath SPalgo=new DijkstraShortestPath
	
	List<List<Link>> allPaths =allp.getAllPathsBetweenNodes(this.graph, 
	    		this.torSwitches.get(0), this.torSwitches.get(2), 5);
	    		
	System.out.println(allPaths);
}
////////////////////////////////
public void createGraph() {
	int current_depth = 1;
	LinkedList<Node> parent = new LinkedList<Node>();
    Node treeRootNode = nodeFactory.create("switch");
    this.graph.addVertex(treeRootNode);
    this.setRootElement(treeRootNode);
    parent.push(treeRootNode);
    
    while (current_depth <= this.depth){
    //	System.out.println(current_depth);
    	if (current_depth!=this.depth) {
    		int iter = parent.size();
    		for (int j=0;j<iter;j++){
    			Node tmp_parent=parent.pop();
	    		//System.out.println("Parent: " + tmp_parent);
		    	for (int i=0; i<k_index; i++){
		    		this.addChild(tmp_parent,current_depth);
		    		//System.out.println("Adding child for parent: "+ tmp_parent);
		    	}
    		}
    	} else {
    		while (!parent.isEmpty()){
    			this.addLeaf(parent.pop());
    		}
    	}
    	current_depth++;
    	while (!this.current_parent.isEmpty()){
    		parent.push(this.current_parent.pop());
    		}
    //	System.out.println("parent" +parent);
    }
    
    //AllPaths allp = new AllPaths();
    System.out.println(this.graph);
    AllPathsDetector allp = new  AllPathsDetector();
   // DijkstraShortestPath SPalgo=new DijkstraShortestPath
    System.out.println( this.torSwitches.get(0) + " " + this.torSwitches.get(1));
    List<List<Link>> allPaths =allp.getAllUniqePathsBetweenNodes(this.graph, 
    		this.torSwitches.get(0), this.torSwitches.get(1), 10);
    		
    //  List<List<Link>> allPaths = allp.getAllPathsBetweenNodes( this.graph, this.torSwitches.get(0), this.torSwitches.get(1));
   System.out.println(allPaths);
    System.exit(0);
}



public void createHGraph() {
	this.hgraph= (Hypergraph<Node, Link>)this.graph;
	
	
}
private void addChild(Node parent, int current_depth ){	

	this.createNode("switch",parent,current_depth);
	//System.out.println("added switch");
	return;
}

private void addChild(Node parent, int current_depth,HashMap<Integer,Integer> tmp ){	

	this.createNode("switch",parent,current_depth, tmp);
	//System.out.println("added switch");
	return;
}

private void addLeaf(Node parent ){	
	for (int i=0;i<ServersPerRack;i++){
			this.createNode("rackserver",parent,this.depth);
			//System.out.println("added server");
	}
	
	return;
}

private void createNode(String type, Node parent){
	Node newNode;
	Link link,rlink;
//	System.out.println("Parent" + parent.getId());
	newNode = nodeFactory.create(type);
//	System.out.println("Child" + newNode.getId());
	this.current_parent.push(newNode);
	if (type.equalsIgnoreCase("rackserver")){
		link=linkFactory.create("torlink");	
		rlink=linkFactory.create("torlink");	
	}
	else {
		link=linkFactory.create("interracklink");
		rlink=linkFactory.create("interracklink");
	}
    this.graph.addEdge(link, parent, newNode);
    this.graph.addEdge(rlink, newNode, parent);
}


private void createNode(String type, Node parent, int current_depth){
	Node newNode;
	Link link,rlink;
//	System.out.println("Parent" + parent.getId());
	newNode = nodeFactory.create(type);
	if (current_depth==(this.depth-1)) {
		this.torSwitches.add(newNode);
	}
//	System.out.println("Child" + newNode.getId());
	this.current_parent.push(newNode);
	//System.out.println("current_parent: " + this.current_parent);
	if (type.equalsIgnoreCase("rackserver")){
		link=linkFactory.create("torlink");	
		rlink=linkFactory.create("torlink");	
	}
	else {
		link=linkFactory.create("interracklink");
		rlink=linkFactory.create("interracklink");
	}
    this.graph.addEdge(link, parent, newNode);
    this.graph.addEdge(rlink, newNode, parent);
}

private void createNode(String type, Node parent, 
		int current_depth, HashMap<Integer,Integer> tmp ){
	Node newNode = null;
	Link link,rlink;
//	System.out.println("Parent" + parent.getId());

	
	if (current_depth==1) {
		if (!(podNodes.containsKey(tmp))) {
			System.out.println("bp 1");
			newNode = nodeFactory.create(type);
			this.podNodes.put(tmp, newNode);
		} else {
		newNode = podNodes.get(tmp);
		exists=true;
		System.out.println("bp 2");
		}
	}else{
		newNode = nodeFactory.create(type);
		System.out.println("bp 3");
	}
	
	if (current_depth==(this.depth-1)) {
		this.torSwitches.add(newNode);
	}
	System.out.println("Child" + newNode.getId());
	this.current_parent.push(newNode);
	//System.out.println("current_parent: " + this.current_parent);
	if (type.equalsIgnoreCase("rackserver")){
		link=linkFactory.create("torlink");	
		rlink=linkFactory.create("torlink");	
	}
	else {
		link=linkFactory.create("interracklink");
		rlink=linkFactory.create("interracklink");
	}
    this.graph.addEdge(link, parent, newNode);
    this.graph.addEdge(rlink, newNode, parent);
}

}
