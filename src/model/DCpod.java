package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import model.components.Link;
import model.components.Node;
import model.components.SubstrateSwitch;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;


public class DCpod implements Serializable {
	
	private DirectedSparseGraph<Node, Link> graph = new DirectedSparseGraph<Node, Link>();
	private SubstrateNodeFactory nodeFactory = new SubstrateNodeFactory();
	private SubstrateLinkFactory linkFactory = new SubstrateLinkFactory();
	private int k=0;
	private int ServersPerRack =  16;//k*k/8; 
	private ArrayList<Node> torSwitches = new ArrayList<Node>();
	private ArrayList<Node> aggSwitches = new ArrayList<Node>();
	private ArrayList<Node> podServers = new ArrayList<Node>();


	public ArrayList<Node> getAggSwitches(){
		return this.aggSwitches;
	}
	
	public ArrayList<Node> getTORSwitches(){
		return this.torSwitches;
	}
	
	public ArrayList<Node> getpodServers(){
		return this.podServers;
	}
	
	public DirectedSparseGraph<Node, Link> getGraph(){
		return this.graph;
	}
	
	public SubstrateNodeFactory getNodeFactory(){
		return this.nodeFactory;
	}

	public SubstrateLinkFactory getLinkFactory(){
		return this.linkFactory;
	}

	public void setNodeFactory(SubstrateNodeFactory nodef){
		this.nodeFactory =nodef;
	}

	public void setLinkFactory(SubstrateLinkFactory linkf){
		this.linkFactory =linkf;
	}
	
	public void createPodGraph(int index) {
		this.k=index;
		this.ServersPerRack = k/2; 
		boolean linkOnlyTOR = false;
		
		for (int i=0;i<k/2;i++){
			Node aggSwitch = nodeFactory.create_switch("agg");
			//System.out.println("aggSwitch: " +aggSwitch.getId());
			aggSwitches.add(aggSwitch); //level 1
		    this.graph.addVertex(aggSwitch);
		    for (int j=0;j<k/2;j++){
		    	Node torSwitch = null;
		    	
		    	if (!linkOnlyTOR){ //create tor switches
		    		torSwitch = nodeFactory.create_switch("tor"); //level 2
		    		((SubstrateSwitch)torSwitch).setTOR_switch(true);
			    	this.torSwitches.add(torSwitch);
		    	//	System.out.println("torSwitch: " +torSwitch.getId() + " " +ServersPerRack);
		    	//	System.out.println(this.getTORSwitches().size());
			     	for (int l=0;l<ServersPerRack;l++){
			    		Node server = nodeFactory.create("rackserver");
			    		//System.out.println("Created SERVER: " + server.getId() );
			    		podServers.add(server);
			    	}
			    	for (int l=j*ServersPerRack;l<(j+1)*ServersPerRack;l++){ //create link to
			    		createLink(torSwitch,podServers.get(l));
			    	}
			    	if (j==k/2-1) linkOnlyTOR=true; 
		    	} else {
		    		torSwitch = torSwitches.get(j); //else get from pod list
		    	}
		    	createLink(aggSwitch,torSwitch); //create link agg to TOR
		    	
		    }
		}
		 
		//System.exit(0);
		
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
	


}
