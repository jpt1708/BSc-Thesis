package model;


import java.util.ArrayList;
import java.util.List;

import model.components.Link;
import model.components.NF;
import model.components.Node;
import edu.uci.ics.jung.graph.DirectedSparseGraph;


public class DirectedFGLTE {
	
	private DirectedSparseGraph<Node, Link> graph = new DirectedSparseGraph<Node, Link>();
	private RequestNodeFactory nodeFactory = new RequestNodeFactory();
	private RequestLinkFactory linkFactory = new RequestLinkFactory();
	private TrafficLTE tr = new TrafficLTE();

public RequestNodeFactory getNodeFactory(){
	return this.nodeFactory;
}

public RequestLinkFactory getLinkFactory(){
	return this.linkFactory;
}

public TrafficLTE getTraffic(){
	return this.tr;
}


public DirectedSparseGraph<Node, Link> getDirectedFGLTE() {
    return this.graph;
}

public void createGraph(Node RRHsub, Node connIXP, int ind_node, int ind_link) {
	this.tr.generateTrafficLoad();
	this.nodeFactory.setNodeCount(ind_node);
	this.linkFactory.setLinkCount(ind_link);

	System.out.println("MME load: " + tr.getMMEload());
	System.out.println("MME mme traffic: " + tr.getNF("mme").get("trc_in_ul"));
	System.out.println("MME mme traffic: " + tr.getNF("mme").get("trc_out_dl"));
	System.out.println("MME mme traffic: " + tr.getNF("mme").get("trc_in_dl"));
	System.out.println("MME mme traffic: " + tr.getNF("mme").get("trc_out_ul"));
	System.out.println("MME mme traffic: " + tr.getNF("pgw").get("trc_in_ul"));
	System.out.println("MME mme traffic: " + tr.getNF("pgw").get("trc_out_dl"));
	
	Node rrh =nodeFactory.create("rrh",1);
	rrh.setCoords(RRHsub.getCoords());
	this.graph.addVertex(rrh); 
	System.out.println(" RRH: " + rrh.getId());
	Node enb= nodeFactory.create("enb",tr);
	this.graph.addVertex(enb);
	System.out.println("ENB: " + enb.getId());
	createLinks("rrh", "mme", rrh, enb);
	Node mme= nodeFactory.create("mme",tr);
	this.graph.addVertex(mme);
	System.out.println("MME: " + mme.getId());
	createLinks("enb", "mme", enb, mme);
	Node sgw= nodeFactory.create("sgw",tr);
	this.graph.addVertex(sgw);
	System.out.println("SGW: " + sgw.getId());
	createLinks("enb","sgw", enb, sgw);
	createLinks("mmew","sgw", mme, sgw);
	Node pgw= nodeFactory.create("pgw",tr);
	System.out.println("PGW: " + pgw.getId());
	createLinks("sgw","pgw",sgw, pgw);
	Node ixp = nodeFactory.create("IPX",1);
	ixp.setCoords(connIXP.getCoords());
	System.out.println(" IXP: " + ixp.getId());
	createLinks("pgw","ixp",pgw,ixp); 
	
}

private void createLinks(String ptype, String ctype, Node parent, Node child){
	Link link,rlink;
	double bw_ul=0;
	double bw_dl=0;
	
//	System.out.println("Parent" + parent.getId());
	if ((ctype.equalsIgnoreCase("sgw")) && (ptype.equalsIgnoreCase("mme"))){
			bw_ul = this.tr.getNF(ctype).get("trc_in_ul") ;
			bw_dl = this.tr.getNF(ctype).get("trc_out_dl");
	} else if ((ctype.equalsIgnoreCase("sgw")) && (ptype.equalsIgnoreCase("enb"))) {
			bw_ul = this.tr.getNF(ctype).get("tru_ul");
		    bw_dl = this.tr.getNF(ctype).get("tru_dl");;
	} else {
			bw_ul = this.tr.getNF(ctype).get("trc_in_ul") +this.tr.getNF(ctype).get("tru_ul");
            bw_dl = this.tr.getNF(ctype).get("trc_out_dl")+this.tr.getNF(ctype).get("tru_dl");
	}
	link=linkFactory.create(bw_ul );
	rlink=linkFactory.create(bw_dl );
//	System.out.println("Child" + newNode.getId());

    this.graph.addEdge(link, parent, child);
    this.graph.addEdge(rlink, child, parent);
}

public  ArrayList<Node>createNFlist(int rho) {
	 ArrayList<Node> nfList= new ArrayList<Node>();
	this.tr.generateTrafficLoad(rho);

	System.out.println("MME load: " + tr.getMMEload());
	System.out.println("MME mme traffic: " + tr.getNF("mme").get("trc_in_ul"));
	System.out.println("MME mme traffic: " + tr.getNF("mme").get("trc_out_dl"));
	System.out.println("MME mme traffic: " + tr.getNF("mme").get("trc_in_dl"));
	System.out.println("MME mme traffic: " + tr.getNF("mme").get("trc_out_ul"));
	System.out.println("MME mme traffic: " + tr.getNF("pgw").get("trc_in_ul"));
	System.out.println("MME mme traffic: " + tr.getNF("pgw").get("trc_out_dl"));
	
	int instances = 4;
	Node rrh =nodeFactory.create("rrh",instances);
	nfList.add(rrh);
	System.out.println("rrh: " + rrh.getId() + " " +rrh.getCpu());
	
	Node enb= nodeFactory.create("enb",tr, instances);
	nfList.add((NF) enb);
	System.out.println("ENB: " + enb.getId() + " " +enb.getCpu());
	
	Node mme= nodeFactory.create("mme",tr, instances);
	nfList.add((NF) mme);
	System.out.println("MME: " + mme.getId()+ " " +mme.getCpu());
	
	Node sgw= nodeFactory.create("sgw",tr,instances);
	nfList.add((NF) sgw);
	System.out.println("SGW: " + sgw.getId() + " " + sgw.getCpu());
	
	Node pgw= nodeFactory.create("pgw",tr,instances);
	nfList.add((NF) pgw);
	System.out.println("PGW: " + pgw.getId() + " " +pgw.getCpu());

	Node ixp = nodeFactory.create("IPX",instances);
	nfList.add(ixp);
	System.out.println("ixp: " + ixp.getId() + " " +ixp.getCpu());

	return nfList;
}

}
