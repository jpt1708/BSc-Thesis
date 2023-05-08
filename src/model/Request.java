package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import tools.LinkComparator;
import tools.NodeComparator;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import model.components.Link;
import model.components.NF;
import model.components.Node;
import model.components.RequestLink;
import model.components.RequestRouter;
import model.components.RequestSwitch;
import model.components.Server;
import model.components.VirtualMachine;


/**
 * Request Class. Subclass of Network.
 */
public class Request extends Network implements Serializable {
	
	private String domain; 
	private String InP;
	private int startDate;
	private int endDate;
	private List<Request> subReq;
	
	/** Not used yet **/
	private float resiliency;
	/** Not used yet **/
	private boolean splittable;
	private ResourceMapping rmap;
	private ResourceMappingNF rmapNF;
	private ArrayList<Integer> ts;
	private DirectedFG dfg=null;
	private DirectedFG dfg_avg=null;


	/** Creates a new instance of Substrate */
    public Request(String id) {
    	super(id);
    	nodeFactory = new RequestNodeFactory();
    	linkFactory = new RequestLinkFactory();
    	/** Setting default values **/
    	resiliency = 0;
    	splittable = false;
    	this.rmap= new ResourceMapping(this);
    	this.rmapNF= new ResourceMappingNF(this);
    }

    public Object clone()throws CloneNotSupportedException{  
    	return super.clone();  
    }  
    
    public void setTS(ArrayList<Integer> reqTS) {
    	this.ts = reqTS;    	
    }
    
	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	public String getInP() {
		return InP;
	}

	public void setInP(String InP) {
		this.InP = InP;
	}

	public int getStartDate() {
		return startDate;
	}

	public void setStartDate(int startDate) {
		this.startDate = startDate;
	}

	public int getEndDate() {
		return endDate;
	}

	public void setEndDate(int endDate) {
		this.endDate = endDate;
	}
	
	public List<Request> getSubReq(){
		return subReq;
	}
	
	public void setSubReq(List<Request> subReq){
		this.subReq=subReq;
	}
	

	
	public Object getCopy() {
    	Request r = new Request(this.getId());
    	r.state = this.state;
    	r.nodeFactory = (RequestNodeFactory) ((RequestNodeFactory) this.nodeFactory).getCopy();
    	r.linkFactory = (RequestLinkFactory) ((RequestLinkFactory) this.linkFactory).getCopy();
    	r.graph = getCopyGraph();
    	//r.graph = ((NetworkGraph) this.graph).getCopy();
    	// r.graphLayout = this.graphLayout;
    	r.domain = this.domain;
    	r.resiliency = this.resiliency;
    	r.splittable = this.splittable;
    	r.startDate = this.startDate;
    	r.endDate = this.endDate;
    	return r;
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
	
	
	@SuppressWarnings("unchecked")
	public void print(){
		ArrayList<Node> nodes = new ArrayList<Node> (this.getGraph().getVertices());
		ArrayList<Link> links = new ArrayList<Link> (this.getGraph().getEdges());
		//ArrayList<Node> nodes =(ArrayList<Node>)getNodes(this.getGraph());
		//ArrayList<Link> links =(ArrayList<Link>) getLinks(this.getGraph());
		
		Collections.sort(nodes,new NodeComparator());
		Collections.sort(links,new LinkComparator());	
		
		System.out.println("Id: " + this.id + " " +this.ts + " " + this);
		System.out.println("****************************Request Nodes**************************");
		
		
		for (Node current : nodes){
			System.out.print("["  +  current.getId() + ": " + current.getName()+" " );
		if ((current) instanceof Server  )  
			System.out.println(((Server)current).getAvailableCpu()+" "+ ((Server)current).getMemory()+" "+((Server)current).getDiskSpace()+"]");	
		else if  ((current) instanceof NF  )  
			System.out.println(((NF)current).getAvailableCpu()+"  ]");
		else
			System.out.println( (current).getCoords().getX()+ " " + (current).getCoords().getY()+"  ]");
	  }
	//	System.out.println("****************************Request Links**************************");
		for (Link current : links){
			Pair<Node> currentNodes =this.getGraph().getEndpoints(current);
			System.out.println("Link: " + current.getId()+ ": " +current.getBandwidth() +":" +currentNodes.getFirst() + "->"+currentNodes.getSecond());
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
	
   public ArrayList<Integer>  getTS() {
    	return this.ts;	
    }

	public double getTotalBw() {
		double bw = 0;
		
		for (Link link : this.getGraph().getEdges()) {
			bw += link.getBandwidth();
		}

		return bw;
	}

	public double getAvgBw() {
		return getTotalBw() / this.getGraph().getVertices().size();
	}
	   
	   
	public  double getAvgWl() {
		Collection<Node> nodes =  this.getGraph().getVertices();
		int numNodes=this.getGraph().getVertices().size();
		double cpu = 0;
		
		for (Node current : nodes){
			cpu += current.getAvailableCpu();
			}
	    if (numNodes==0) numNodes=1;

		return cpu/numNodes;
	}
	
	public  double getWl() {
		Collection<Node> nodes =  this.getGraph().getVertices();
		double cpu = 0;
		
		for (Node current : nodes){
			cpu += current.getAvailableCpu();
			}

		return cpu;
	}
	
	 public void setRMap (ResourceMapping r){ this.rmap=r; }
	 public ResourceMapping getRMap (){ return  this.rmap; }
	 
	 public void setRMapNF (ResourceMappingNF r){ this.rmapNF=r; }
	 public ResourceMappingNF getRMapNF (){ return  this.rmapNF; }

	
	public void setDFG(DirectedFG dfg) {
		this.dfg=dfg;
		
	}
	
	public DirectedFG getDFG() {
		return this.dfg;
	}
	
	///milp avg
	public void setDFG_avg(DirectedFG dfg) {
		this.dfg_avg=dfg;
		
	}
	
	public DirectedFG getDFG_avg() {
		return this.dfg_avg;
	}
	
	
}

