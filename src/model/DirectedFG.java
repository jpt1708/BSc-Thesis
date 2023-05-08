package model;


import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;



import java.util.Random;
//import jdistlib.Uniform;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

import tools.LinkComparator;
import tools.NodeComparator;
import model.components.Link;
import model.components.NF;
import model.components.Node;
import simenv.SimulatorConstants;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;


public class DirectedFG implements Cloneable, Serializable {
	DirectedSparseGraph<Node, Link> graph = new DirectedSparseGraph<Node, Link>();
	RequestNodeFactory nodeFactory = new RequestNodeFactory();
    RequestLinkFactory linkFactory = new RequestLinkFactory();
	HashMap<Node, Integer> branches= new HashMap<Node, Integer> ();
	double maxTraffic=0;
    boolean bidirectional =SimulatorConstants.BIDIRECTIONAL_FG;
	int eps = SimulatorConstants.FG_EPS; //default
	TrafficFG tr = new TrafficFG();
	Random rand = new Random();
	int  n = rand.nextInt(400) + 100;
	MersenneTwister engine = new MersenneTwister(n);
	Uniform uni= new Uniform(5,10,engine);
    Uniform uni1= new Uniform(0,1,engine);

	public DirectedFG(TrafficFG tr) {
		this.tr = tr;
	}  

public RequestNodeFactory getNodeFactory(){
	return this.nodeFactory;
}

public RequestLinkFactory getLinkFactory(){
	return this.linkFactory;
}

public TrafficFG getTR(){
	return this.tr;
}

public int getEPs() {
	return this.eps;
}

public void setEPs(int eps) {
	 this.eps=eps;
}

public boolean getDir() {
	return this.bidirectional;
}

public DirectedSparseGraph<Node, Link> getDirectedFG() {
    return this.graph;
}

public void resetGraph(Graph<Node, Link> reqGraph, String id) {

	this.tr = new TrafficFG();
	this.tr.generateTraffic(this.eps, id);
	//this.tr.generateTraffic(this.eps, true);
	//System.out.println(this.maxTraffic+" " + this.tr.getTrafficBound());
	ArrayList<Node> req_n = (ArrayList<Node>) getNodes(reqGraph);
	ArrayList<Link> req_l = (ArrayList<Link>) getLinks(reqGraph);
	//System.out.println(" reqGraph: " + reqGraph + "  ");
	boolean lbExists=false;
	for (Node node: req_n){
	HashMap<String, Double> nfIns = this.tr.generateTrafficLoad(node.getName(),this.getDir());
	//update cpu
	double cpu = nfIns.get("load");
    int temp_val = (int) Math.floor(cpu);
    node.setAvailableCpu(temp_val);
   //update links
   if ((branches.get(node)==0) && (node.getName().startsWith("lb")) && (!(lbExists))){
	   lbExists=true;
    	for (Link currLink:req_l) {
   		 Pair<Node> epsLink = reqGraph.getEndpoints(currLink);
   		if (node.getId()==epsLink.getFirst().getId()) {
   				if (epsLink.getSecond().getId()>node.getId()) {//uplink
   					currLink.setBandwidth(nfIns.get("tr_out_ul")/2);
   	    			//System.out.println("Update link0 " + currLink.getId()+ " with bw " + (nfIns.get("tr_out_ul")/2));
   				}else  {
   					currLink.setBandwidth(nfIns.get("tr_out_dl")); //downlink
   	    			//System.out.println("Update link2" + currLink.getId()+ " with bw " + nfIns);
   				}
	   		}
	   	}
    } else  if ((branches.get(node)==0) && (node.getName().startsWith("lb")) && (lbExists)){
    	lbExists=false;
    	for (Link currLink:req_l) {
      		 Pair<Node> epsLink = reqGraph.getEndpoints(currLink);
      		if (node.getId()==epsLink.getFirst().getId()) {
      				if (epsLink.getSecond().getId()<node.getId()) {//uplink
      					currLink.setBandwidth(nfIns.get("tr_out_dl")/2);
      	    			//System.out.println("Update link0 " + currLink.getId()+ " with bw " + (nfIns.get("tr_out_ul")));
      				}else  {
      					currLink.setBandwidth(nfIns.get("tr_out_ul")); //downlink
      	    			//System.out.println("Update link2" + currLink.getId()+ " with bw " +  (nfIns.get("tr_out_ul")/2));
      				}
   	   		}
   	   	}
	} else  if ((branches.get(node)==0) && (reqGraph.outDegree(node)<=2)){
    	for (Link currLink : req_l) {
    		Pair<Node> epsLink = reqGraph.getEndpoints(currLink);
    		if (node.getId()==epsLink.getFirst().getId()) {
    			if (epsLink.getSecond().getId()>node.getId()) {//uplink
    				currLink.setBandwidth(nfIns.get("tr_out_ul"));
    			//System.out.println("Update link1 " + currLink.getId()+ " with bw " + nfIns.get("tr_out_ul"));
    			}else  {
    				if ((bidirectional)&&(eps>2))
    						nfIns.put("tr_out_dl", (this.tr.getTraffic().get(1)+this.tr.getTraffic().get(2)));
  					currLink.setBandwidth(nfIns.get("tr_out_dl"));
  	    			//System.out.println("Update link4" + currLink.getId()+ " with bw " + nfIns);
  				}
    		}
    	}
    } else if (((branches.get(node)==1) || (branches.get(node)==2)) && (searchBigger(node.getId()))){
    	for (Link currLink:req_l) {
      		 Pair<Node> epsLink = reqGraph.getEndpoints(currLink);
      		if (node.getId()==epsLink.getFirst().getId()) {
      				if (epsLink.getSecond().getId()>node.getId()) {//uplink
      	    			//System.out.println("Update link3 " +nfIns);
    					currLink.setBandwidth(nfIns.get("tr_out_ul")/2);
      				}else  {
      					currLink.setBandwidth(nfIns.get("tr_out_dl")/2);
      	    			//System.out.println("Update link4" + currLink.getId()+ " with bw " + nfIns);
      				}
   	   		}
   	   	}
    } else {
    	for (Link currLink:req_l) {
     		 Pair<Node> epsLink = reqGraph.getEndpoints(currLink);
     		if (node.getId()==epsLink.getFirst().getId()) {
     				if (epsLink.getSecond().getId()>node.getId()) {//uplink
     					currLink.setBandwidth(nfIns.get("tr_out_ul")/2);
     	    			//ystem.out.println("Update link " + currLink.getId()+ " with bw " + (nfIns.get("tr_out_ul")/2));
     				}else  {
     					if (branches.get(node)==2)
     						nfIns.put("tr_out_dl", (this.tr.getTraffic().get(2)));
     					currLink.setBandwidth(nfIns.get("tr_out_dl")); //downlink
     	    			//System.out.println("Update link 2" + currLink.getId()+ " with bw " + nfIns);
     				}
  	   		}
  	   	}
    }
	}//for loop


}

public void updateGraph(Graph<Node, Link> reqGraph, int simulationTimeStep, int totalSimulationTime, boolean periodic) {
	this.tr = new TrafficFG();
	this.tr.generateTraffic(this.eps,this.maxTraffic);
	
	// this.tr.generateTraffic(this.eps, true);
	//System.out.println(this.maxTraffic+" " + this.tr.getTrafficBound());
	ArrayList<Node> req_n = (ArrayList<Node>) getNodes(reqGraph);
	ArrayList<Link> req_l = (ArrayList<Link>) getLinks(reqGraph);
	//System.out.println(" reqGraph: " + reqGraph + "  ");
	boolean lbExists=false;
	for (Node node: req_n){
	HashMap<String, Double> nfIns = this.tr.generateTrafficLoad(node.getName(),this.getDir());

	// if (periodic) {
	// 	double periodicScale = (Math.sin((simulationTimeStep * 2 * Math.PI) / totalSimulationTime) * 0.8 + 1) / 2;
	// 	System.out.println("scale: " + periodicScale);
	// 	nfIns.replaceAll((key, value) -> value * periodicScale);
	// }

	//update cpu
	// double cpu = nfIns.get("load");
    // int temp_val = (int) Math.floor(cpu);
    // if (temp_val >= node.getAvailableCpu()) {
		
	// 	System.out.println("should not happend");
	// }
	
	// node.setAvailableCpu(temp_val);

   //update links
   if ((branches.get(node)==0) && (node.getName().startsWith("lb")) && (!(lbExists))){
	   lbExists=true;
    	for (Link currLink:req_l) {
   		 Pair<Node> epsLink = reqGraph.getEndpoints(currLink);
   		if (node.getId()==epsLink.getFirst().getId()) {
   				if (epsLink.getSecond().getId()>node.getId()) {//uplink
   					currLink.setBandwidth(nfIns.get("tr_out_ul")/2);
   	    			//System.out.println("Update link0 " + currLink.getId()+ " with bw " + (nfIns.get("tr_out_ul")/2));
   				}else  {
   					currLink.setBandwidth(nfIns.get("tr_out_dl")); //downlink
   	    			//System.out.println("Update link2" + currLink.getId()+ " with bw " + nfIns);
   				}
	   		}
	   	}
    } else  if ((branches.get(node)==0) && (node.getName().startsWith("lb")) && (lbExists)){
    	lbExists=false;
    	for (Link currLink:req_l) {
      		 Pair<Node> epsLink = reqGraph.getEndpoints(currLink);
      		if (node.getId()==epsLink.getFirst().getId()) {
      				if (epsLink.getSecond().getId()<node.getId()) {//uplink
      					currLink.setBandwidth(nfIns.get("tr_out_dl")/2);
      	    			//System.out.println("Update link0 " + currLink.getId()+ " with bw " + (nfIns.get("tr_out_ul")));
      				}else  {
      					currLink.setBandwidth(nfIns.get("tr_out_ul")); //downlink
      	    			//System.out.println("Update link2" + currLink.getId()+ " with bw " +  (nfIns.get("tr_out_ul")/2));
      				}
   	   		}
   	   	}
	} else  if ((branches.get(node)==0) && (reqGraph.outDegree(node)<=2)){
    	for (Link currLink : req_l) {
    		Pair<Node> epsLink = reqGraph.getEndpoints(currLink);
    		if (node.getId()==epsLink.getFirst().getId()) {
    			if (epsLink.getSecond().getId()>node.getId()) {//uplink
    				currLink.setBandwidth(nfIns.get("tr_out_ul"));
    			//System.out.println("Update link1 " + currLink.getId()+ " with bw " + nfIns.get("tr_out_ul"));
    			}else  {
    				if ((bidirectional)&&(eps>2))
    						nfIns.put("tr_out_dl", (this.tr.getTraffic().get(1)+this.tr.getTraffic().get(2)));
  					currLink.setBandwidth(nfIns.get("tr_out_dl"));
  	    			//System.out.println("Update link4" + currLink.getId()+ " with bw " + nfIns);
  				}
    		}
    	}
    } else if (((branches.get(node)==1) || (branches.get(node)==2)) && (searchBigger(node.getId()))){
    	for (Link currLink:req_l) {
      		 Pair<Node> epsLink = reqGraph.getEndpoints(currLink);
      		if (node.getId()==epsLink.getFirst().getId()) {
      				if (epsLink.getSecond().getId()>node.getId()) {//uplink
      	    			//System.out.println("Update link3 " +nfIns);
    					currLink.setBandwidth(nfIns.get("tr_out_ul")/2);
      				}else  {
      					currLink.setBandwidth(nfIns.get("tr_out_dl")/2);
      	    			//System.out.println("Update link4" + currLink.getId()+ " with bw " + nfIns);
      				}
   	   		}
   	   	}
    } else {
    	for (Link currLink:req_l) {
     		 Pair<Node> epsLink = reqGraph.getEndpoints(currLink);
     		if (node.getId()==epsLink.getFirst().getId()) {
     				if (epsLink.getSecond().getId()>node.getId()) {//uplink
     					currLink.setBandwidth(nfIns.get("tr_out_ul")/2);
     	    			//ystem.out.println("Update link " + currLink.getId()+ " with bw " + (nfIns.get("tr_out_ul")/2));
     				}else  {
     					if (branches.get(node)==2)
     						nfIns.put("tr_out_dl", (this.tr.getTraffic().get(2)));
     					currLink.setBandwidth(nfIns.get("tr_out_dl")); //downlink
     	    			//System.out.println("Update link 2" + currLink.getId()+ " with bw " + nfIns);
     				}
  	   		}
  	   	}
    }


	double bandwidth = 0;
	for (Link currLink: reqGraph.getInEdges(node)) {
		bandwidth += currLink.getBandwidth();
	}

	if (bandwidth == 0) {
		for (Link currLink: reqGraph.getOutEdges(node)) {
			bandwidth += currLink.getBandwidth();
		}
		// bandwidth = this.maxTraffic;
	}

	ArrayList<Double> traffic = new ArrayList<Double>();
	traffic.add(bandwidth);

	double cpu = tr.generateTrafficLoad(node.getName(), traffic, this.getDir()).get("load");
	//System.out.println(" traffic: " + traffic + " load; " + cpu);
	int temp_val = (int) Math.floor(cpu);
	node.setAvailableCpu(temp_val);

	}//for loop

}

private boolean searchBigger(int id) {

	for (Entry<Node, Integer> entry : branches.entrySet()) {
	    if (entry.getValue()==0){
		    if (entry.getKey().getId()>id)
		    	return true;
	    }
	}

	return false;
}

public void createGraph(int req) {
    int nodenum =  (int) Math.floor(uni.nextInt());

    double probTemplate =uni1.nextDouble();

    if (probTemplate<0.4) {//0.4
    	tr.generateTraffic(eps, req);
    	chain(nodenum,tr, bidirectional);
    }else if (probTemplate<0.7){//0.7
    	eps=3;
    	tr.generateTraffic(eps, req);
    	split(nodenum,tr, bidirectional);
    } else {
    	tr.generateTraffic(eps, req);
    	agg(nodenum,tr,bidirectional);
    }
    this.maxTraffic=tr.getTrafficBound();

}

@SuppressWarnings("unchecked")
protected void chain(int nodenum,TrafficFG tr, boolean bidirectional){
	//Uniform uni= new Uniform(0,1);
	double ul= tr.getTraffic().get(0);
	double dl= tr.getTraffic().get(1);

	for (int i=0;i<nodenum;i++){
		double prob =uni1.nextDouble();
		String type="ip";

		if (prob>0.7){ //0.7
			type="nat";
		} else if (prob>0.3){
			type="dpi";
		}

		//System.out.println("type: "  + type);
		Node node = nodeFactory.create(type);
		double cpu = tr.generateTrafficLoad(type, tr.getTraffic(),bidirectional).get("load");
	   // System.out.println("cpu:  " +cpu +" for " +type);

	    int temp_val = (int) Math.floor(cpu);
		node.setAvailableAllocatedCpu(temp_val);
	    node.setAvailableCpu(temp_val);

	   if (bidirectional){
	   ((NF)node).setOutTrafficDL((int) Math.floor(dl));
	   ((NF)node).setInTrafficDL((int) Math.floor(dl));
	    }
	   ((NF)node).setOutTrafficUL((int) Math.floor(ul));
	   ((NF)node).setInTrafficUL((int) Math.floor(ul));

	    branches.put(node, 0);
		this.graph.addVertex(node);

	}

	ArrayList<Node> nodes = new ArrayList<Node>(graph.getVertices());
	Collections.sort(nodes,new NodeComparator());

	for (int i=0;i<nodenum-1;i++){
		createLinks(nodes.get(i),nodes.get(i+1),(int) Math.floor(ul),(int) Math.floor(dl),bidirectional);
	}

}

protected void split(int nodenum,TrafficFG tr, boolean bidirectional){
	//Uniform uni= new Uniform(0,1);
	Random r = new Random();
	int id = r.nextInt(nodenum-2);
	double ul= tr.getTraffic().get(0);
	double dl1= tr.getTraffic().get(1);
	double dl2= tr.getTraffic().get(2);

	//System.out.println("splitter id: "+id); // id of splitter
	//System.out.println("dl1: " + dl1+ " ul: " +ul +" dl2: " +dl2);

	for (int i=0;i<nodenum;i++){ // for every node

		double tmp_dl=0;
		double tmp_ul=0;
		double prob =uni1.nextDouble();
		String type="ip";

		if (i==id){
			type="lb";
		}
		else if (prob>0.7){
			type="nat";
		} else if (prob>0.3){
			type="dpi";
		}

		Node node = nodeFactory.create(type);   ///create NF
		//System.out.println("Node: " + i);
		int branch = r.nextInt(2)+1; //assign to branch
		if (i>id) {
			//System.out.println("current node: " + i + "in branch "+branch);
			if (i==(id+1)) branches.put(node, 1);
			else if (i==(id+2)) branches.put(node, 2);
			else branches.put(node, branch);
		}else{
			branches.put(node, 0);
		}

		if (branches.get(node)==0){
			if (bidirectional) tmp_dl=dl1+dl2;
			tmp_ul=ul;
			//System.out.println("node: " + node.getId()+"  dl: " + tmp_dl+ " ul: " + tmp_ul );
		} else if (branches.get(node)==1){
			if (bidirectional)  tmp_dl=dl1;
			tmp_ul=ul/2;
			//System.out.println("a node: " + node.getId()+ " dl: " + tmp_dl+ " ul: " +tmp_ul );
		} else {
			if (bidirectional)tmp_dl=dl2;
			tmp_ul=ul/2;
			//System.out.println("b node: " + node.getId()+ "  dl: " + tmp_dl+ " ul: " +tmp_ul );
		}

		ArrayList<Double> traffic = new ArrayList<Double>();
		traffic.add(tmp_ul);
		if (bidirectional) traffic.add(tmp_dl);


		double cpu = tr.generateTrafficLoad(type, traffic, bidirectional).get("load");
		//System.out.println(" traffic: " + traffic + " load; " + cpu);
	    int temp_val = (int) Math.floor(cpu);
		node.setAvailableAllocatedCpu(temp_val);
	    node.setAvailableCpu(temp_val);
	    if (bidirectional){
		((NF)node).setOutTrafficDL(tmp_dl);
		((NF)node).setInTrafficDL(tmp_dl);
	    }
		((NF)node).setOutTrafficUL(tmp_ul);
		((NF)node).setInTrafficUL(tmp_ul);


		this.graph.addVertex(node);
	}

	ArrayList<Node> nodes = getNodes(graph);
//	ArrayList<Node> nodes = new ArrayList<Node>(graph.getVertices());
//	Collections.sort(nodes,new NodeComparator());

	for (int i=0;i<id;i++){
		if (branches.get(nodes.get(i))==0) {
			createLinks(nodes.get(i),nodes.get(i+1),ul,(dl1+dl2),bidirectional);
			//System.out.println("Node " +nodes.get(i).getId()+ " branch "+ branches.get(nodes.get(i)));
		}
	}

	Node prev1 = nodes.get(id);
	Node prev2=  nodes.get(id);

	for (int i=id+1;i<nodenum;i++){
		if (branches.get(nodes.get(i))==1) {
			createLinks(prev1,nodes.get(i),ul/2,dl1,bidirectional);
			prev1=nodes.get(i);
			//System.out.println("Node " +nodes.get(i).getId()+ " branch "+ branches.get(nodes.get(i)));
		}else if (branches.get(nodes.get(i))==2) {
			createLinks(prev2,nodes.get(i),ul/2,dl2,bidirectional);
			prev2=nodes.get(i);
			//System.out.println("Node " +nodes.get(i).getId()+ " branch "+ branches.get(nodes.get(i)));
		}
	}

  //System.out.println(this.graph);


}

protected void agg(int nodenum,TrafficFG tr, boolean bidirectional){
	//Uniform uni= new Uniform(0,1);
	Random r = new Random();
	int randomNum1 = r.nextInt((nodenum/2-2) + 1);
	int randomNum2 = r.nextInt((nodenum-2 - nodenum/2) + 1) + nodenum/2;
	double ul= tr.getTraffic().get(0);
	double dl= tr.getTraffic().get(1);
//	System.out.println("Splitter: " + ul + "   " +dl);

	for (int i=0;i<nodenum;i++){
		double tmp_dl=0;
		double tmp_ul=0;
		double prob =uni1.nextDouble();

		String type="ip";
		//System.out.println("Node: " + i);
		if ((i==randomNum1) || (i==randomNum2)){
			type="lb";
		} else if (prob>0.7){
			type="nat";
		} else if (prob>0.3){
			type="dpi";
		}

		Node node = null;

		int branch = r.nextInt(2)+1;
		if ((i>randomNum1) && (i<randomNum2)){
			if (i==(randomNum1+1)) {
				type="aes";
				node = nodeFactory.create(type);
				branches.put(node, 1);
			}
			else if (i==(randomNum1+2)){
				type="fw";
				node = nodeFactory.create(type);
				branches.put(node, 2);
			}
			else{
				node = nodeFactory.create(type);
				branches.put(node, branch);
				tmp_dl=dl;
				tmp_ul=ul;
			}
		}else{
			node = nodeFactory.create(type);
			branches.put(node, 0);
			tmp_dl=dl;
			tmp_ul=ul;
		}

		if ((branches.get(node)==1) || (branches.get(node)==2)){
			tmp_dl=dl/2;
			tmp_ul=ul/2;
		}

		ArrayList<Double> traffic = new ArrayList<Double>();
		traffic.add(tmp_ul);
		traffic.add(tmp_dl);

	//	System.out.println("traffic : " +traffic );

		double cpu = tr.generateTrafficLoad(type, traffic, bidirectional).get("load");
	    int temp_val = (int) Math.floor(cpu);
		node.setAvailableAllocatedCpu(temp_val);
	    node.setAvailableCpu(temp_val);

	    // ((NF)node).setOutTrafficDL(tmp_dl);
	    // ((NF)node).setInTrafficDL(tmp_dl);
	    // if (bidirectional){
		//    ((NF)node).setOutTrafficUL(tmp_ul);
		//    ((NF)node).setInTrafficUL(tmp_ul);
	    // }

		if (bidirectional){
			((NF)node).setOutTrafficDL(tmp_dl);
			((NF)node).setInTrafficDL(tmp_dl);
			}
			((NF)node).setOutTrafficUL(tmp_ul);
			((NF)node).setInTrafficUL(tmp_ul);
		

		this.graph.addVertex(node);
	}


	ArrayList<Node> nodes = new ArrayList<Node>(graph.getVertices());
	Collections.sort(nodes,new NodeComparator());

	for (int i=0;i<randomNum1;i++){
		if (branches.get(nodes.get(i))==0) {
		createLinks(nodes.get(i),nodes.get(i+1),ul,dl,bidirectional);
		}
	}
	for (int i=randomNum2;i<nodenum-1;i++){
		if (branches.get(nodes.get(i))==0) {
		createLinks(nodes.get(i),nodes.get(i+1),ul,dl,bidirectional);
		}
	}


	Node prev1 = nodes.get(randomNum1);
	Node prev2=  nodes.get(randomNum1);

	for (int i=randomNum1+1;i<randomNum2+1;i++){
		if (branches.get(nodes.get(i))==1) {
			createLinks(prev1,nodes.get(i),ul/2,dl/2,bidirectional);
			prev1=nodes.get(i);
		}else if (branches.get(nodes.get(i))==2) {
			createLinks(prev2,nodes.get(i),ul/2,dl/2,bidirectional);
			prev2=nodes.get(i);
		}else{ //lb case
			createLinks(prev1,nodes.get(i),ul/2,dl/2,bidirectional);
			prev1=nodes.get(i);
			createLinks(prev2,nodes.get(i),ul/2,dl/2,bidirectional);
			prev2=nodes.get(i);
		}
	}

}



private void createLinks(Node parent, Node child,double ul, double dl){
	Link link,rlink;

	link=linkFactory.create(ul);
	rlink=linkFactory.create(dl);

	this.graph.addEdge(link, parent, child, EdgeType.DIRECTED);
	this.graph.addEdge(rlink, child, parent, EdgeType.DIRECTED);

}


protected void createLinks(Node parent, Node child,double ul, double dl,boolean bidirectional){
	Link link,rlink;

	link=linkFactory.create(ul);

	if (bidirectional){
	rlink=linkFactory.create(dl);
	this.graph.addEdge(rlink, child, parent, EdgeType.DIRECTED);
	}

	this.graph.addEdge(link, parent, child, EdgeType.DIRECTED);



}

@SuppressWarnings("unchecked")
public ArrayList<Node> getNodes(Graph<Node,Link> t) {
	ArrayList<Node> reqNodes =new ArrayList<Node>();

	for (Node x: t.getVertices())
		reqNodes.add(x);

	Collections.sort(reqNodes,new NodeComparator());

	return reqNodes;
}

@SuppressWarnings("unchecked")
private ArrayList<Link> getLinks(Graph<Node, Link> t) {
	ArrayList<Link> links =new ArrayList<Link>();
	Collection<Link> edges =  t.getEdges();

	for (Link current : edges){
		links.add(current);
	}

	Collections.sort(links,new LinkComparator());

	return links;
}



}
