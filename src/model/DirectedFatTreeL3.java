package model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections15.multimap.MultiHashMap;
//import org.apache.commons.collections15.Transformer;
import tools.LinkComparator;
import tools.NodeComparator;
import model.components.Link;
import model.components.Node;
import simenv.SimulatorConstants;
/*import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.algorithms.transformation.FoldingTransformer;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedGraph;*/
import edu.uci.ics.jung.graph.DirectedSparseGraph;
//import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.SetHypergraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;


public class DirectedFatTreeL3 implements Serializable {
	
	private DirectedSparseGraph<Node, Link> graph = new DirectedSparseGraph<Node, Link>();
	//private Hypergraph<Node, Link> hGraph =   new DirectedSparseMultigraph<Node, Link>();
	private Hypergraph<Node, Link> hGraph=new SetHypergraph<Node, Link>();
//	private MultiHashMap<Pair<Node>,List<Pair<Node>>> pathlets =  null;
	private MultiHashMap<Pair<Integer>,List<Pair<Integer>>> pathlets1 =  null;
	private MultiHashMap<Pair<Integer>, HashMap<List<Pair<Integer>>,Integer>> pathletsUse=null;
	private ArrayList<DCpod> podsArray = new ArrayList<DCpod>();
	private ArrayList<Node> rootSwitches = new ArrayList<Node>();
	private Set<Node> aggSwitch = new HashSet<>();
	private ArrayList<Node> aggrSwitches; //= new ArrayList<Node>();
	private Node rootElement;
	private SubstrateNodeFactory nodeFactory = new SubstrateNodeFactory();
	private SubstrateLinkFactory linkFactory = new SubstrateLinkFactory();
	private SubstrateLinkFactory hlinkFactory = new SubstrateLinkFactory();
	private int pods = SimulatorConstants.PODS;
	AllPathsDetector<Node, Link> allp = new  AllPathsDetector<Node, Link>();

	
	
	class Path {
		Node start;
		Node end;
		List<Link> path;
	    public Path(Node start, Node end, List<Link> path) {
	       this.start = start;
	       this.end = end;
	       this.path = path;
	    }

	    public Node getStart() { return this.start; }
	    public Node getEnd() { return this.end; }

	}
	
/*public MultiHashMap<Pair<Integer>,List<Pair<Integer>>> getPaths() {
	return this.pathlets1;
}

public MultiHashMap<Pair<Integer>, HashMap<List<Pair<Integer>>,Integer>> getPathsUsage() {
	return pathletsUse;
}

public  void setPathsUsage(MultiHashMap<Pair<Integer>, HashMap<List<Pair<Integer>>,Integer>> pathsUseUp) {
	this.pathletsUse = pathsUseUp;
}*/

public SubstrateNodeFactory getNodeFactory(){
	return this.nodeFactory;
}

public SubstrateLinkFactory getLinkFactory(){
	return this.linkFactory;
}

/*public int[][][][][] getPathlets(){
	return this.subHyper;
}*/

public int podNum() {
    return this.pods;
}

public int getServersNum() {
  return this.graph.getVertexCount()-2*this.getAggrSwitches().size()-this.getRootSwitches().size();
}

public Node getRootElement() {
    return this.rootElement;
}

public ArrayList<Node> getRootSwitches(){
	return this.rootSwitches;
}

public ArrayList<Node> getAggrSwitches(){
	aggrSwitches =  new ArrayList<>(aggSwitch);
	return this.aggrSwitches;
}

public void setRootElement(Node root) {
     this.rootElement=root;
}


public DirectedSparseGraph<Node, Link> getFatTreeL3Graph() {
    return this.graph;
}

public void createFTGraph() {
	int roots=(int) (Math.pow(pods,2)/4);
	for (int j=0;j<roots;j++){ //create Root Switches
		Node rootSwitch = nodeFactory.create_switch("root");
	    this.graph.addVertex(rootSwitch);
	    rootSwitches.add(rootSwitch);
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
		// System.out.println(current.getGraph().getEdgeCount());
		 for (Link link:current.getGraph().getEdges()){
			 Pair<Node> endpoints = current.getGraph().getEndpoints(link);
			 this.graph.addEdge(link, endpoints,EdgeType.DIRECTED);
		 }
		// System.out.println("Pod: " );
		for (int ii=0;ii< aggSwitches.size();ii++){
			for (int jj=ii*(pods/2);jj<(ii+1)*(pods/2);jj++){
			createLink(rootSwitches.get(jj),aggSwitches.get(ii));
			aggSwitch.add(aggSwitches.get(ii));
			//System.out.println("Agg:  " +aggSwitches.get(ii)+ " CONNECTED TO " +rootSwitches.get(jj) );
			}
			
		}
		
	}
	
	System.out.println("Nodes: " +this.graph.getVertexCount());
	System.out.println("Links: " + this.graph.getEdgeCount());
	
}
@SuppressWarnings({ "unused", "unchecked" })
private void print(){
	ArrayList<Node> nodes = new ArrayList<Node>(this.graph.getVertices());
	Collections.sort(nodes,new NodeComparator());
	ArrayList<Link> links = new ArrayList<Link>(this.graph.getEdges());
	Collections.sort(links,new LinkComparator());
	
	for (Node current : nodes){
		System.out.println("["  +  current.getId() + "]: " + current.getName()+" " + current.getCpu());
	}
	for (Link current : links){
		Pair<Node> currentNodes =this.graph.getEndpoints(current);
		System.out.println("Link: " + current.getId()+ ": " +current.getBandwidth() +":" +currentNodes.getFirst() + "->"+currentNodes.getSecond());
	}
	
}


public ArrayList<Node> getNodes(List<Link> path){
	ArrayList<Node> coll =  new ArrayList<Node> ();
		for (Link link : path){
		Pair<Node> ep= this.graph.getEndpoints(link);
		//System.out.print(" " +ep.getFirst().getId()+" => "+ ep.getSecond().getId());
		if (!coll.contains(ep.getFirst()))
			coll.add(ep.getFirst());
		if (!coll.contains(ep.getSecond()))
			coll.add(ep.getSecond());
		}
	
	return coll;
	
	
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


}
