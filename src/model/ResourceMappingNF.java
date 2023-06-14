package model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.multimap.MultiHashMap;

import tools.LinkComparator;
import tools.NodeComparator;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;
import model.components.Link;
import model.components.Node;
import model.components.Server;
import model.components.SubstrateLink;
import model.components.SubstrateSwitch;
import simenv.SimulatorConstants;

public class ResourceMappingNF implements Serializable {
	@SuppressWarnings("unused")
	private Request request;
	private double cost=0;
	private double revenue=0;
	private double cpuCost=0;
	private double bwCost=0;
	private double avg_hops=0;
	private int rules=0;
	private int rulesTOR=0;
	private int rulesAgg=0;
	private int rulesRoot=0;
	private double solTime=0;
	private boolean overp_CPU=false;
	private double[][] ad;
	private double[][][] ad1;
	private double[][] U;
	private double[][][] U1;
	private int[] racks =  new int[8];
	private LinkedHashMap<Node, Node> nodeMap= new LinkedHashMap<Node, Node>(); //request-real
	private HashMap<Integer, Integer> tcamMap= new LinkedHashMap<Integer, Integer>();
	private HashMap<Integer, Integer> tcamMapTOR= new LinkedHashMap<Integer, Integer>();
	private HashMap<Integer, Integer> tcamMapAgg= new LinkedHashMap<Integer, Integer>();
	private HashMap<Integer, Integer> tcamMapRoot= new LinkedHashMap<Integer, Integer>();
	private HashMap<Link, ArrayList<LinkedHashMap<Link, Double>>> linkMap =
			new HashMap<Link, ArrayList<LinkedHashMap<Link, Double>>>();
	private HashMap<Pair<Integer>, ArrayList<HashMap<Pair<Integer>, Double>>> linkMapInt =
			new HashMap<Pair<Integer>, ArrayList<HashMap<Pair<Integer>, Double>>>();
	private MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>> flowMap=
			new MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>> ();;
	@SuppressWarnings("unused")
	private boolean denied=false;
	private int serversUsed =0;

	public ResourceMappingNF(Request req){
		this.request=req;
	}

	public void setOverpCPU(boolean violation){
		this.overp_CPU=violation;
	}

	public void setSolTime(double time) {
		this.solTime=time;
	}

	public void setQ(double[][] AD){
		this.ad=AD;
	}

	public void setQ(double[][][] AD){
		this.ad1=AD;
	}
	public void setU(double[][] memory){
		this.U=memory;
	}

	public void setU(double[][][] memory){
		this.U1=memory;
	}

	public double[][] getQ(){
		return this.ad;
	}

	public double[][] getU(){
		return this.U;
	}

	public double[][][] getQb(){
		return this.ad1;
	}

	public double[][][] getUb(){
		return this.U1;
	}

	public void setTCAMmapping(HashMap<Integer, Integer> tcamMap2){
		this.tcamMap=tcamMap2;
	}

	public void setTCAMmappingTOR(HashMap<Integer, Integer> tcamMapTor){
		this.tcamMapTOR=tcamMapTor;
		for (Integer value : tcamMapTor.values()) {
			rulesTOR =rulesTOR+value;
		}
		//System.out.println("rulesTOR: " +rulesTOR);
	}

	public void setTCAMmappingRoot(HashMap<Integer, Integer> tcamMapRoot){
		this.tcamMapRoot=tcamMapRoot;
		for (Integer value : tcamMapRoot.values()) {
			rulesRoot =rulesRoot+value;
		}
		//System.out.println("rulesRoot: " +rulesRoot);
	}

	public void setTCAMmappingAgg(HashMap<Integer, Integer> tcamMapAgg){
		this.tcamMapAgg=tcamMapAgg;
		for (Integer value : tcamMapAgg.values()) {
			rulesAgg =rulesAgg+value;
		}
		//System.out.println("rulesAgg: " +rulesAgg);
	}

	public void setNodeMapping(LinkedHashMap<Node, Node> map){
		this.nodeMap=map;
	}

	public boolean containsNodeInMap(Node node) {
		if (this.nodeMap.containsValue(node))
			return true;

		return false;
	}

	public void setLinkMapping(HashMap<Link, ArrayList<LinkedHashMap<Link, Double>>> f){
		this.linkMap=f;
	}

	public void setFlowMapping(MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>>  f){
		this.flowMap=f;
	}

	public void setLinkMappingNew(HashMap<Pair<Integer>, ArrayList<HashMap<Pair<Integer>, Double>>> f){
		this.linkMapInt=f;
	}

	public void setRulesAdded(int rul){
		this.rules=rul;
	}
	public int getRules(){
		return this.rules;
	}

	public void setRulesAddedTOR(int rulT){
		this.rulesTOR=rulT;
	}
	public int getRulesTOR(){
		return this.rulesTOR;
	}

	public void setRulesAddedAgg(int rulA){
		this.rulesAgg=rulA;
	}
	public int getRulesAgg(){
		return this.rulesAgg;
	}

	public void setRackAllocation(int[] rack){
		this.racks=rack;
	}
	public int[] getRackAllocation(){
		return this.racks;
	}

	public void setRulesAddedRoot(int rulR){
		this.rulesRoot=rulR;
	}
	public int getRulesRoot(){
		return this.rulesRoot;
	}
	public void setEmbeddingCost(double ecost){
		this.cost=ecost;
	}

	public void setCPUCost(double cpuCost){
		this.cpuCost=cpuCost;
	}
	public double getCPUCost(){
		return this.cpuCost;
	}

	public void setBWCost(double bwCost){
		this.bwCost=bwCost;
	}

	public double getBWCost(){
		return this.bwCost;
	}


	public boolean getOverpCPU(){
		return this.overp_CPU;
	}


	public double getEmbeddingCost(){
		return this.cost;
	}
	public void setHops(double h){
		this.avg_hops=h;
	}

	public double getSolTime() {
		return this.solTime;
	}
	public double getHops(){
		return this.avg_hops;
	}
	public void setEmbeddingRevenue(double erev){
		this.revenue=erev;
	}

	public double getEmbeddingRevenue(){
		return this.revenue;
	}

	public void denied() { this.denied=true; }
	public void accepted() { this.denied=false; }

	public boolean isDenied() { return this.denied;}



	public void reserveNodes(Substrate sub){
		ArrayList<Node> reqNodes = (ArrayList<Node>) getNodes(this.request.getGraph());
//		System.out.println(this.nodeMap);
		for (Node reqNode: reqNodes){
			for (Node subNode: sub.getGraph().getVertices()){
				Node mappedNode=this.nodeMap.get(reqNode);
				int cap=reqNode.getAvailableCpu();
				if (subNode.getId()==mappedNode.getId()){
					//System.out.println(subNode.getId()+ " " +cap + " "+subNode.getAvailableCpu());
					double capNew= (subNode.getAvailableCpu()-cap);
					//System.out.println((int)capNew);
					subNode.setAvailableCpu((int) capNew);

			   	}
			}
		}

	}

	public void releaseAllocatedNodes(Substrate sub){
		ArrayList<Node> reqNodes = (ArrayList<Node>) getNodes(this.request.getGraph());
		if (!this.nodeMap.isEmpty()){
			for  (Node sfc: reqNodes){
				 for (Node x: sub.getGraph().getVertices()){
					 Node mappedNode=this.nodeMap.get(sfc);
					 int cap=sfc.getAvailableAllocatedCpu();
					   if (x.getId()==mappedNode.getId()){
						  //System.out.println("in release:  " +  sfc.getId()+ " " +cap + "  "+x.getId()+ " "+x.getAvailableCpu());
						   double capNew= (x.getAvailableAllocatedCpu()+cap);
						   //System.out.println((int)capNew);
						   x.setAvailableAllocatedCpu((int) capNew);
				   }
				 }
			}
		}
		else {
			System.out.println("Should not be here");
		}


	}

	public void releaseNodes(Substrate sub){
		ArrayList<Node> reqNodes = (ArrayList<Node>) getNodes(this.request.getGraph());
		if (!this.nodeMap.isEmpty()){
			for  (Node sfc: reqNodes){
				 for (Node x: sub.getGraph().getVertices()){
					 Node mappedNode=this.nodeMap.get(sfc);
					 int cap=sfc.getAvailableCpu();
					   if (x.getId()==mappedNode.getId()){
						  //System.out.println("in release:  " +  sfc.getId()+ " " +cap + "  "+x.getId()+ " "+x.getAvailableCpu());
						   double capNew= (x.getAvailableCpu()+cap);
						   //System.out.println((int)capNew);
						   x.setAvailableCpu((int) capNew);
				   }
				 }
			}
		}
		else {
			System.out.println("Should not be here");
		}


	}

	public void releaseTCAM(Substrate sub){
		if (!this.tcamMap.isEmpty()){
			 for (Node x: sub.getGraph().getVertices()){
				 int cap=x.getTCAM();
				   if (this.tcamMap.containsKey(x.getId())){
					   //System.out.println(x.getId()+ " " +cap + " "+x.getAvailableCpu());
					   x.setTCAM(cap+this.tcamMap.get(x.getId()));
				   }
			 }
		}
	}


	public void reserveLinks(Substrate sub){
/*		ArrayList<Link> reqLinks = (ArrayList<Link>) getLinks(this.request.getGraph());
		int reqLinkNum=reqLinks.size();
		ArrayList<Link> subLinks = (ArrayList<Link>) getLinks(sub.getGraph());
*/

		if(!flowMap.isEmpty()) {
			 for (HashMap<Pair<Integer>, Double> mapping: flowMap.keySet() ){ //for all mappings
				 if (!mapping.isEmpty()){
				        Set<Pair<Integer>> keys = mapping.keySet();
				        for(Pair<Integer> key: keys){
				        	Double cap = mapping.get(key);
				        	Collection<List<Pair<Integer>>> collPaths = flowMap.get(mapping);
				        	if (collPaths!=null){
								ArrayList<List<Pair<Integer>>>copy = new ArrayList<List<Pair<Integer>>>(collPaths);
								for (List<Pair<Integer>> list_links: copy){
									for (Pair<Integer> tmplink: list_links) {
										reserveSubLink(sub,tmplink.getFirst(), tmplink.getSecond(),cap);

									}
								}

				        	}
				        }
				 }
			 }
		}
		/*for (int i=0;i<reqLinkNum;i++ ){
			 ArrayList<LinkedHashMap<Link, Double>> tmp = new  ArrayList<LinkedHashMap<Link, Double>>();
			 tmp = this.linkMap.get(reqLinks.get(i));
			 for (LinkedHashMap<Link, Double> x: tmp ){ //for all mappings
				 //System.out.println(tmp);
				 if (!x.isEmpty()){
					 for (Map.Entry<Link, Double> entry : x.entrySet()) {
						 if (!(entry==null)){
						    Link mappedLink = entry.getKey();
						    //sub.print();
						   // System.out.println(mappedLink);
						    Double cap = entry.getValue();
						    double newCap = mappedLink.getBandwidth()-cap;
						    //System.out.println("newCap "+newCap);
						    Pair<Node> eps = sub.getGraph().getEndpoints(subLinks.get(mappedLink.getId()));
						    //System.out.println(eps.getFirst().getId()+ " " + eps.getSecond().getId());
							Link tmplink = sub.getGraph().findEdge(eps.getFirst(), eps.getSecond());
							tmplink.setBandwidth(newCap);
						 }
					 }
				 }
			 }
		}//for all requested links
*/

	}



	public void releaseLinks(Substrate sub){

		if(!flowMap.isEmpty()) {
			 for (HashMap<Pair<Integer>, Double> mapping: flowMap.keySet() ){ //for all mappings
				 if (!mapping.isEmpty()){
				        Set<Pair<Integer>> keys = mapping.keySet();
				        for(Pair<Integer> key: keys){
				        	Double cap = mapping.get(key);
				        	Collection<List<Pair<Integer>>> collPaths = flowMap.get(mapping);
				        	if (collPaths!=null){
								ArrayList<List<Pair<Integer>>>copy = new ArrayList<List<Pair<Integer>>>(collPaths);
								for (List<Pair<Integer>> list_links: copy){
									for (Pair<Integer> tmplink: list_links) {
										releaseSubLink(sub,tmplink.getFirst(), tmplink.getSecond(),cap);

									}
								}

				        	}
				        }
				 }
			 }
		}
	}

	 private boolean releaseSubLink(Substrate sub,int src, int dst, double cap){
		  for (Link x: sub.getGraph().getEdges()){
			  Pair<Node> eps = sub.getGraph().getEndpoints(x);
			  if ((eps.getFirst().getId()==src) && (eps.getSecond().getId()==dst)){
				// System.out.println("Found edge: " + src+ " -> " +dst + " for " + eps.getFirst().getId()+" -> "+eps.getSecond().getId());
				  double newCap = x.getBandwidth()+cap;
				  x.setBandwidth(newCap);
				//  System.out.println(newCap);
				  return true;
			  }
		  }

		  return false;

	}

	 private boolean reserveSubLink(Substrate sub,int src, int dst, double cap){
		  for (Link x: sub.getGraph().getEdges()){
			  Pair<Node> eps = sub.getGraph().getEndpoints(x);
			  if ((eps.getFirst().getId()==src) && (eps.getSecond().getId()==dst)){
				// System.out.println("Found edge: " + src+ " -> " +dst + " for " + eps.getFirst().getId()+" -> "+eps.getSecond().getId());
				  double newCap = x.getBandwidth()-cap;
				  x.setBandwidth(newCap);
				//  System.out.println(newCap);
				  return true;
			  }
		  }

		  return false;

	}

/*	@SuppressWarnings("rawtypes")
	public double computeCost(Substrate sub){

		double cost=0;
		for (Link key: flows.keySet()){
			for (Path path: (List<Path>) flows.get(key)){
				for (Link link: path.getSubstrateLinks()){
					for (Link edge: sub.getGraph().getEdges()){
						if (link.getName()==edge.getName()){
							cost += path.getBandwidth();
						}
					}
				}
			}
		}

		Collection v = sub.getGraph().getVertices();
		Iterator itr_sub = v.iterator();
		while(itr_sub.hasNext()){
			Node subNode =  (Node) itr_sub.next();
			int aug_ID=  subNode.getId();

			Collection c = this.nodeMap.entrySet();
			Iterator itr = c.iterator();

			while(itr.hasNext()){
				 Map.Entry entry = (Map.Entry)itr.next();
				 if(aug_ID == ((Node)entry.getValue()).getId()) {
				 	if (((Node)entry.getKey()) instanceof VirtualMachine)  {
				 		cost+=((VirtualMachine) entry.getKey()).getCpu()+((VirtualMachine)entry.getKey()).getMemory()+((VirtualMachine)entry.getKey()).getDiskSpace();
			   	 	}
				    else if ((((Node)entry.getKey()) instanceof RequestRouter)){
				    	cost+= ((SubstrateRouter) subNode).getStress();
		    	 	}
				    else if ((((Node)entry.getKey()) instanceof RequestSwitch)) {
				    	cost+= ((SubstrateSwitch) subNode).getStress();
				    }
				 }
			}
		}
		return cost;
	}
	*/
	 public double max_util_server(Substrate substrate) {
		 double max_cpu_util= (-1)*Double.MAX_VALUE;

		 for (Node node: substrate.getGraph().getVertices()){
			 if (node instanceof Server){
				 double div  = (double) ((Server) node).getAvailableCpu()/(double) ((Server) node).getNominalCpu();
				 if ((1-div)>max_cpu_util) max_cpu_util =1-div;
			 }
		 }

		 return max_cpu_util;
	 }


	 public  double Node_utilization_Server_Cpu(Substrate substrate){

		 double cpu_util= 0;

			 double servers=0;
			 double cpu_util_sub=0;
			 for (Node node: substrate.getGraph().getVertices()){
				 if (node instanceof Server){
					 servers++;
					 double div = (double) ((Server) node).getAvailableCpu()/(double) ((Server) node).getNominalCpu();//(double) ((Server) node).getCpu();
					 cpu_util_sub += (1-div);
				 }
			 }
			 cpu_util= cpu_util_sub/servers;

		 return cpu_util;
		}

	 public  double max_link_utilization(Substrate substrate){
		 double max_link_util= (-1)*Double.MAX_VALUE;
		 for (Link link: substrate.getGraph().getEdges()){
			 double div = (double) ((SubstrateLink) link).getBandwidth()/(double) ((SubstrateLink) link).getNominalBandwidth();
			 if ((1-div)>max_link_util) max_link_util=1-div;
		 }

		 return max_link_util;
	 }

	public  double Link_utilization(Substrate substrate){
			double link_util= 0;
			double link_util_sub=0;

			 for (Link link: substrate.getGraph().getEdges()){
					 double div = (double) ((SubstrateLink) link).getBandwidth()/(double) ((SubstrateLink) link).getNominalBandwidth();
					 link_util_sub += (1-div);
			 }
				 link_util= link_util_sub/substrate.getGraph().getEdgeCount();

				 return link_util;
	}
	public  double Switch_utilizationAgg(Substrate substrate){
		 double switch_util= 0;
		 ArrayList<Node> rootSwitches = substrate.getFTL3().getRootSwitches();
		 ArrayList<Node> aggrSwitches = substrate.getFTL3().getAggrSwitches();

		 double switches=0;
		 double switch_util_sub=0;
		 for (Node node: substrate.getGraph().getVertices()){
			 if (node instanceof SubstrateSwitch){
				// if (!(rootSwitches.contains(node)) && !(aggrSwitches.contains(node))) {
				 if ((aggrSwitches.contains(node))) {
				 switches++;
				 double div = (double) ((SubstrateSwitch) node).getTCAM()/SimulatorConstants.MAX_SWITCH_TCAM;
				 switch_util_sub = switch_util_sub + (1-div);
				 }
			 }
		 }
		 switch_util= switch_util_sub/switches;
		/* System.out.println("switch_util: " +switch_util);
		 try {
		        System.in.read();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }*/
	 return switch_util;
	}
	public  double Switch_utilizationRoot(Substrate substrate){
		 double switch_util= 0;
		 ArrayList<Node> rootSwitches = substrate.getFTL3().getRootSwitches();
		 ArrayList<Node> aggrSwitches = substrate.getFTL3().getAggrSwitches();

		 double switches=0;
		 double switch_util_sub=0;
		 for (Node node: substrate.getGraph().getVertices()){
			 if (node instanceof SubstrateSwitch){
				// if (!(rootSwitches.contains(node)) && !(aggrSwitches.contains(node))) {
				 if ((rootSwitches.contains(node))) {
				 switches++;
				 double div = (double) ((SubstrateSwitch) node).getTCAM()/SimulatorConstants.MAX_SWITCH_TCAM;
				 switch_util_sub = switch_util_sub + (1-div);
				 }
			 }
		 }
		 switch_util= switch_util_sub/switches;
		/* System.out.println("switch_util: " +switch_util);
		 try {
		        System.in.read();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }*/
	 return switch_util;
	}
	public  double Switch_utilizationTor(Substrate substrate){
		 double switch_util= 0;
		 ArrayList<Node> rootSwitches = substrate.getFTL3().getRootSwitches();
		 ArrayList<Node> aggrSwitches = substrate.getFTL3().getAggrSwitches();

		 double switches=0;
		 double switch_util_sub=0;
		 for (Node node: substrate.getGraph().getVertices()){
			 if (node instanceof SubstrateSwitch){
				 if (!(rootSwitches.contains(node)) && !(aggrSwitches.contains(node))) {
				 switches++;
				 double div = (double) ((SubstrateSwitch) node).getTCAM()/SimulatorConstants.MAX_SWITCH_TCAM;
				 switch_util_sub = switch_util_sub + (1-div);
				 }
			 }
		 }
		 switch_util= switch_util_sub/switches;
		/* System.out.println("switch_util: " +switch_util);
		 try {
		        System.in.read();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }*/
	 return switch_util;
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

	@SuppressWarnings("rawtypes")
	void printNodeMapping(){
		Collection c = this.nodeMap.entrySet();
		Iterator itr = c.iterator();
		while(itr.hasNext()){
			 Map.Entry entry = (Map.Entry)itr.next();

			 System.out.println("Virtual " +((Node)entry.getKey()).getId()+  " cpu  " +((Node)entry.getKey()).getCpu() +
					 " Real " + ((Node) entry.getValue()).getId() + " cpu "  + ((Node) entry.getValue()).getCpu());
		}
	}

	void printFlowMapping(Substrate sub){

		for (HashMap<Pair<Integer>,Double> key: this.flowMap.keySet()){
			System.out.println("For Virtual Link "+ this.flowMap.keySet().toArray()[0]);
			for (List<Pair<Integer>> paths:  this.flowMap.get(key)){
				Iterator<Pair<Integer>> iterator = paths.iterator();
				while (iterator.hasNext()) {
				Pair<Integer> tmp_pair= iterator.next();
				 System.out.println(tmp_pair.getFirst()+ " -> " + tmp_pair.getSecond() +" : bw " +this.flowMap.keySet().toArray()[1]);
				}
			}
		}

	}

	public void setServersUsed(int servers) {
		this.serversUsed=servers;

	}
	public int getServersUsed() {
		return this.serversUsed;
	}
}
