package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


import tools.LinkComparator;
import tools.NodeComparator;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import model.components.*;

/**
 * Substrate Class. Subclass of Network.
 */
public class Substrate extends Network implements Cloneable {
	private int[][][][][] pathlets = null;
	private int pods;
	private int numOfServers;
	private DirectedFatTreeL3  fl3;
	
    /** Creates a new instance of Substrate */
    public Substrate(String id) {
    	super(id);
    	nodeFactory = new SubstrateNodeFactory();
    	linkFactory = new SubstrateLinkFactory();
    }
    
    
    public Object getCopy() {
    	Substrate s = new Substrate(this.getId());
    	s.state = this.state;
    	s.nodeFactory = (SubstrateNodeFactory) ((SubstrateNodeFactory) this.nodeFactory).getCopy();
    	s.linkFactory = (SubstrateLinkFactory) ((SubstrateLinkFactory) this.linkFactory).getCopy();
    	s.graph =  getCopyGraph();
    	//	s.graph = ((NetworkGraph) this.graph).getCopy();
    	// s.graphLayout = this.graphLayout;
    	return s;
    }
    
    public Object getCopy(EdgeType ep) {
    	Substrate s = new Substrate(this.getId());
    	s.state = this.state;
    	s.nodeFactory = (SubstrateNodeFactory) ((SubstrateNodeFactory) this.nodeFactory).getCopy();
    	s.linkFactory = (SubstrateLinkFactory) ((SubstrateLinkFactory) this.linkFactory).getCopy();
    	s.graph =  getCopyGraph(ep);
    	//	s.graph = ((NetworkGraph) this.graph).getCopy();
    	// s.graphLayout = this.graphLayout;
    	return s;
    }
    
	public Graph<Node, Link> getCopyGraph(EdgeType ep) {
		NetworkGraphDirected g = new NetworkGraphDirected();
		for (Link link : this.graph.getEdges()) {
			Link l = (Link) link.getCopy();
			Pair<Node> endpoints = this.graph.getEndpoints(link);
			Node n1 = g.getVertexByName(endpoints.getFirst().getName());
		//	Interface if1 = this.graph.);
			if (n1==null)
				n1 = (Node) endpoints.getFirst().getCopy();
			Node n2 = g.getVertexByName(endpoints.getSecond().getName());
			if (n2==null)
				n2 = (Node) endpoints.getSecond().getCopy();
				g.addEdge(l,n1,n2, ep);
		}
		return g;
	}
	
    
	public Graph<Node, Link> getCopyGraph() {
		NetworkGraph g = new NetworkGraph();
		for (Link link : this.graph.getEdges()) {
			Link l = (Link) link.getCopy();
			Pair<Node> endpoints = this.graph.getEndpoints(link);
			Node n1 = g.getVertexByName(endpoints.getFirst().getName());
			if (n1==null)
				n1 = (Node) endpoints.getFirst().getCopy();
			Node n2 = g.getVertexByName(endpoints.getSecond().getName());
			if (n2==null)
				n2 = (Node) endpoints.getSecond().getCopy();
			g.addEdge(l,n1,n2);
		}
		return g;
	}
	
    public SubstrateNodeFactory getNodeFactory(){
    	return (SubstrateNodeFactory) ((SubstrateNodeFactory) this.nodeFactory).getCopy();
    }
    
    public SubstrateLinkFactory getLinkFactory(){
    	return (SubstrateLinkFactory) ((SubstrateLinkFactory) this.linkFactory).getCopy();
    }
    
    
    @SuppressWarnings("unchecked")
	public void print(){
		ArrayList<Node> nodes =(ArrayList<Node>)getNodes(this.getGraph());
		ArrayList<Link> links =(ArrayList<Link>) getLinks(this.getGraph());
		Collections.sort(nodes,new NodeComparator());
		Collections.sort(links,new LinkComparator());
		
		
		System.out.println("Id: " + this.id);
		System.out.println("****************************Substrate Nodes**************************");
		
		
		for (Node current : nodes){
			System.out.print("["  +  current.getId() + ": ");
		if ((current) instanceof Server  )  
			System.out.println(((Server)current).getAvailableCpu()+" " +((Server)current).getCpu()+ " "+ ((Server)current).getMemory()+" "+((Server)current).getDiskSpace()+"]");	
		else if (((current) instanceof SubstrateRRH) ||((current) instanceof SubstrateRouter))
		 	System.out.println( (current).getCoords().getX()+ " " + (current).getCoords().getY()+"  ]");
		else if((current) instanceof NF  )  
			System.out.println(((NF)current).getAvailableCpu());
		else if((current) instanceof SubstrateSwitch  )  
			System.out.println(((SubstrateSwitch)current).getTCAM()+"]");
		else
			System.out.println("]");
	  }
		System.out.println("****************************Substrate Links**************************");
		for (Link current : links){
			Pair<Node> currentNodes =this.getGraph().getEndpoints(current);
			System.out.println("Link: " + current.getId()+ ": " + ((SubstrateLink)current).getAvailableBandwidth() +":" +current.getBandwidth() + " : " 
			+currentNodes.getFirst() + "->"+currentNodes.getSecond());
		}
		
	}
    public ArrayList<Link> getLinks(Graph<Node,Link> t) {
		ArrayList<Link> reqLink =new ArrayList<Link>();
		Collection<Link> edges =  t.getEdges();

		for (Link current : edges)
			reqLink.add(current);
		
		return reqLink;
	}
	
	public ArrayList<Node> getNodes(Graph<Node,Link> t) {
		ArrayList<Node> reqNodes =new ArrayList<Node>();
		Collection<Link> edges =  t.getEdges();

		for (Link current : edges){
			Pair<Node> currentNodes =t.getEndpoints(current);
			if (reqNodes.contains(currentNodes.getFirst())==false)
				reqNodes.add(currentNodes.getFirst());
			if (reqNodes.contains(currentNodes.getSecond())==false)
				reqNodes.add(currentNodes.getSecond());
		}


		return reqNodes;
	}
	
	public void addPathlets (int[][][][][] paths){
		this.pathlets=paths;
	}
	
	public void addPods (int pods){
		this.pods=pods;
	}
	public int getPods (){
		return this.pods;
	}
	
	public int[][][][][] getPathlets(){
		return this.pathlets;
	}
	
	public void setFTL3 (DirectedFatTreeL3 fl3){
		this.fl3=fl3;
		this.numOfServers=fl3.getServersNum();
	}
	
	public  double getAvgWl() {
		Collection<Node> nodes =  this.getGraph().getVertices();
		int numNodes=0;
		double cpu = 0;
		
		for (Node current : nodes){
			if (current.getType().contentEquals("Server")) {
				numNodes++;
				cpu += current.getAvailableCpu();
			}
		}
	    if (numNodes==0) numNodes=1;

		return cpu;
	}
	
	
	public DirectedFatTreeL3 getFTL3(){
		return this.fl3;
	}
	
	public Object clone()throws CloneNotSupportedException{  
		return super.clone();  
		}


	public int getNumServers() {
		return this.numOfServers;
	}  
}
