package model;

import java.util.LinkedList;

import model.components.Link;
import model.components.Node;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;


public class DirectedLTEreq {
	
	private DirectedSparseGraph<Node, Link> graph = new DirectedSparseGraph<Node, Link>();
	private Node rootElement;
	private LinkedList<Node> current_parent = new LinkedList<Node>();
	private SubstrateNodeFactory nodeFactory = new SubstrateNodeFactory();
	private SubstrateLinkFactory linkFactory = new SubstrateLinkFactory();
	private int ServersPerRack = 3;
	private int depth=2;
	private int k=2;

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
    		for (int j=0;j<parent.size();j++){
    			Node tmp_parent=parent.pop();
	    		//System.out.println("Parent" + tmp_parent);
		    	for (int i=0; i<k; i++){
		    			this.addChild(tmp_parent,current_depth);
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
    
}

private void addChild(Node parent, int current_depth ){	

	this.createNode("switch",parent);
	//System.out.println("added switch");
	return;
}

private void addLeaf(Node parent ){	
	for (int i=0;i<ServersPerRack;i++){
			this.createNode("rackserver",parent);
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
}
