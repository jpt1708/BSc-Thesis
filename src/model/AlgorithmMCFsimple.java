package model;


import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;

import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.components.Link;
import model.components.Node;

import org.apache.commons.collections15.multimap.MultiHashMap;

import tools.LinkComparator;
import tools.NodeComparator;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

public class AlgorithmMCFsimple  {
	 private double[][] xVar;
	 private Substrate substrate;
	 private Request req;
	 private LinkMapping lm = new LinkMapping();
	 private double bw_cost = 0;
	 private double hops_path =0;
	 
	 private MultiHashMap<HashMap<Pair<Integer>, Double>, List<Pair<Integer>>> linkMap;
	 					
	 private LinkedHashMap<Node,Node> nodemap = new LinkedHashMap<Node,Node>();
	
	public AlgorithmMCFsimple(double[][] xVarFinal, Substrate sub,Request req,LinkedHashMap<Node,Node> nodemap){
		this.xVar=xVarFinal;
		this.substrate=sub;
		this.req=req;
		this.nodemap=nodemap;
		this.linkMap = new  MultiHashMap<HashMap<Pair<Integer>, Double>, List<Pair<Integer>>>();
	}
	
	
	public boolean  RunMCF() {
		
		Graph<Node, Link> sub= substrate.getGraph();
		ArrayList<Node> subNodesList = (ArrayList<Node>) getNodes(sub);
		ArrayList<Link> subLinksList = (ArrayList<Link>) getLinks(sub);
		int subNodesNum = subNodesList.size();	
		//Adjacency Matrix Substrate
		double[][] subLinks = new double[subNodesNum][subNodesNum];
		for (Link y: subLinksList){
			Pair<Node> tmp = sub.getEndpoints(y);
			subLinks[tmp.getFirst().getId()][tmp.getSecond().getId()]  = y.getBandwidth();
		}
		for (int i=0;i<subNodesNum;i++){
			subLinks[i][i]=Integer.MAX_VALUE;
		}
		//System.out.println("subNodesNum: "+subNodesNum);	
		//System.out.println("subLinks: "+Arrays.deepToString(subLinks));
		
	
		
		ArrayList<Node> reqNodes = (ArrayList<Node>)getNodes(this.req.getGraph());
		ArrayList<Link> reqLinks = (ArrayList<Link>) getLinks(this.req.getGraph());
		double[] sfcNodes = new double[reqNodes.size()];
		String[] sfcType= new String [reqNodes.size()];
		int sizeOfSFC = sfcNodes.length;
		int nodesSFC = sizeOfSFC;
		double[][] sfcLinks = new double[sizeOfSFC][sizeOfSFC];
		
		//req.print();
		for (Node x: reqNodes){		
			//System.out.println(x.getId()+ " " + reqNodes.size());
			sfcNodes[x.getId()] = x.getAvailableCpu();
			sfcType[x.getId()] = "Server";// x.getType(); 
		}		
		
		for (Link y: reqLinks){
			Pair<Node> tmp = req.getGraph().getEndpoints(y);
			sfcLinks[tmp.getFirst().getId()][tmp.getSecond().getId()]  = y.getBandwidth();
		}	
		
		ArrayList<Integer> origin= new ArrayList<Integer>();
		ArrayList<Integer> dest= new ArrayList<Integer>();
		for (Link l: reqLinks){
			Pair<Node> eps = req.getGraph().getEndpoints(l);
			origin.add(eps.getFirst().getId());
			dest.add(eps.getSecond().getId());
		}	
	
	try {
	 	IloCplex cplex1 = new IloCplex();
		//cplex.setParam(IloCplex.DoubleParam.TiLim, 600);
		cplex1.setParam(IloCplex.DoubleParam.ObjULim,Double.MAX_VALUE);
		cplex1.setParam(IloCplex.DoubleParam.ObjLLim,-1*Double.MAX_VALUE);
		
				
		/*****************************System Variables **************************************************/

					//create f continuous variable, with bounds lb and ub
			//////////////////////////////////////////////////////////////////
			IloNumVar[][][][] f_mcf = new IloNumVar[subNodesNum][subNodesNum][][];
			for (int u=0;u<subNodesNum;u++){
				f_mcf[u]=new IloNumVar[subNodesNum][][];
				for (int v=0;v<subNodesNum;v++){
					f_mcf[u][v]=new IloNumVar[subNodesNum][subNodesNum];
						for(int k=0; k< nodesSFC; k++){
							f_mcf[u][v][k]=new IloNumVar[nodesSFC];
							for(int m=0; m<nodesSFC; m++){
								f_mcf[u][v][k]=cplex1.numVarArray(nodesSFC, 0,1000000000);
							}
						}
				}
			}

	/*****************************Objective Function **************************************************/
			IloLinearNumExpr flows = cplex1.linearNumExpr();
			for (int u=0;u<subNodesNum;u++){
				for (int v=0;v<subNodesNum;v++){
					for(int k=0; k< nodesSFC; k++){
						for(int m=0; m< nodesSFC; m++){
							flows.addTerm(1, f_mcf[u][v][k][m]);
						}
					}
				}
			}	
			cplex1.addMinimize(flows);
			
			
	/*****************************Capacity Constraints **************************************************/			
			int counter = 0;
			for (int u=0;u<subNodesNum;u++){
				for (int v=0;v<subNodesNum;v++){
					IloLinearNumExpr bwReq = cplex1.linearNumExpr();
						for(int k=0; k< nodesSFC; k++){
							for(int m=0; m<nodesSFC; m++){
							//	System.out.print(" ["+k+"]["+m+"]" + " SFC: " +  f);
								bwReq.addTerm(1,f_mcf[u][v][k][m]);
							}
						}
					double cap = subLinks[u][v];	
					//System.out.println("Cap:"+cap);
					cplex1.addLe(bwReq, cap);
					counter++;
				}
			}		
			
	/*****************************Flow Constraints **************************************************/		
			
		for(int k=0; k< nodesSFC;k++){
			Node tmp = this.nodemap.get(reqNodes.get(k));
			int id1 = tmp.getId();
			for(int m=0; m< nodesSFC; m++){
				Node tmp2 = this.nodemap.get(reqNodes.get(m));
				int id2 = tmp2.getId();
				double capVirt =sfcLinks[k][m];
				if(capVirt>0){ // if there is a requested link
					IloLinearNumExpr flowCon1 = cplex1.linearNumExpr();	
					if (id1!=id2) { //if there is no co-llocation
						for (int l=0;l<subNodesNum;l++){
							if (l!=id1){
							//	System.out.println("ff: " + id1+" " +l + " " +k + " " + m);
								flowCon1.addTerm(1,f_mcf[id1][l][k][m]); //incoming	
								flowCon1.addTerm(-1,f_mcf[k][id1][k][m]);//outgoing?
							}
						}
						
					}
					else {
						flowCon1.addTerm(1,f_mcf[id1][id1][k][m]);
						//System.out.println("xx: " + id1+" " +id1 + " " +k + " " + m);
					}
					//System.out.println("capVirt: " +capVirt ); 
					cplex1.addEq(flowCon1, capVirt);
				}
				}
			}
		//System.out.println("aaaaaaaaaaaaaaa");
			for(int k=0; k< nodesSFC;k++){
				Node tmp = this.nodemap.get(reqNodes.get(k));
				int id1 = tmp.getId();
				for(int m=0; m< nodesSFC; m++){
					Node tmp2 = this.nodemap.get(reqNodes.get(m));
					int id2 = tmp2.getId();
						double capVirt =sfcLinks[m][k];
						if(capVirt>0){
						IloLinearNumExpr flowCon1 = cplex1.linearNumExpr();	
							if (id1!=id2) {
								for (int l=0;l<subNodesNum;l++){
									if (l!=id1){
										flowCon1.addTerm(1,f_mcf[l][id1][m][k]); //incoming	
										flowCon1.addTerm(-1,f_mcf[id1][l][m][k]); //incoming	
									}
								}
							}
							else {
								flowCon1.addTerm(1,f_mcf[id1][id1][m][k]); //incoming
							}
							cplex1.addEq(flowCon1, capVirt);
						}
					}
			}
		

			for (int u=0;u<subNodesNum;u++){
				for(int k=0; k< nodesSFC;k++){
					for(int m=0; m< nodesSFC; m++){
						Node tmp1 = this.nodemap.get(reqNodes.get(k));
						int id1 = tmp1.getId();
						Node tmp2 = this.nodemap.get(reqNodes.get(m));
						int id2 = tmp2.getId();
						if (sfcLinks[k][m]!=0){
							if ((Integer.compare(id1,u)!=0) && (Integer.compare(id2,u)!=0)) { //not origin or destination of requested
							IloLinearNumExpr flowCon1 = cplex1.linearNumExpr();	
							IloLinearNumExpr flowCon2 = cplex1.linearNumExpr();
							for (int l=0;l<subNodesNum;l++){
								flowCon2.addTerm(1,f_mcf[l][u][k][m]); //incoming
							}
							for (int v=0;v<subNodesNum;v++){
								flowCon1.addTerm(1,f_mcf[u][v][k][m]); //outgoing
							}
							cplex1.addEq(flowCon1, flowCon2);
							}
						}

					}
				}
			}
	  // System.exit(0);
		//cplex1.exportModel("lpmcf.lp");
		long solveStartTime = System.nanoTime();
		boolean solvedOK = cplex1.solve();
		long solveEndTime = System.nanoTime();
		long solveTime = solveEndTime - solveStartTime;
		
		if (solvedOK) {
			System.out.println("###################################");
			System.out.println( "Found an answer! CPLEX status: " + cplex1.getStatus() + ", Time: " + ((double) solveTime / 1000000000.0));

		//	cplex.output().println("Prnting .... Solution status = " + cplex.getStatus());
			 cplex1.output().println("Solution value = " + cplex1.getObjValue());
			 
			 
			double[][][][] fVar =new double [subNodesNum][subNodesNum][nodesSFC][nodesSFC];
			for (int u=0;u<subNodesNum;u++){
					for (int v=0;v<subNodesNum;v++){
						for(int k=0; k<nodesSFC; k++){
							for(int m=0; m<nodesSFC; m++){
							fVar[u][v][k][m] = cplex1.getValue(f_mcf[u][v][k][m]);
								if (fVar[u][v][k][m]>0.00000000001){
										//System.out.println(k+" "+m + " to " + u+ " "+v +" "+ fVar[u][v][k][m]);
								}
							}		
						}
					}
			}
			
			linkMapping(fVar);
			cplex1.end();
			return true;
		}
		else {
			
//			try {
//	        System.in.read();
//	    } catch (IOException e) {
//	        // TODO Auto-generated catch block
//	        e.printStackTrace();
//	    }

			cplex1.end();
			return false;
		}
	
		
	} catch (IloException e) {
	System.err.println("Concert exception caught: " + e);
	}
	return false;

	}

	  public void linkMapping (double[][][][] fVar ){
		  ArrayList<Node> subNodes = this.substrate.getNodes(this.substrate.getGraph());
		  ArrayList<Link> links = (ArrayList<Link>) getLinks(req
					.getGraph());
		 // updatedSubLinks.print();
		  //System.exit(0);
		  int subNodesNum = subNodes.size();
		  //MultiHashMap<HashMap<Pair<Integer>, Double>, List<Pair<Integer>>>()
		
		  int numNodesSFC= req.getGraph().getVertexCount();
		   //ArrayList<Link> reqLinks = this.req.getLinks(this.req.getGraph());
		   double avg_hops = 0;
		   for(int k=0; k< numNodesSFC; k++){
			   for(int m=0; m< numNodesSFC; m++){
				   int hops_link_path = 0;
				   for (int u=0;u<subNodesNum;u++){
						for (int v=0;v<subNodesNum;v++){
							if (fVar[u][v][k][m]>0.000000001){
								Pair<Integer> key = new Pair<Integer>(u, v);
								HashMap<Pair<Integer>, Double> key4lmap = new HashMap<Pair<Integer>, Double>();
								key4lmap.put(key, fVar[u][v][k][m]);
								List<Pair<Integer>> tmpPath = new ArrayList<Pair<Integer>>();
								tmpPath.add(key);
								this.linkMap.put(key4lmap, tmpPath);
								System.out.println("Link : " + k
										+ " " + m + " to " + u
										+ " " + v + " "
										+ fVar[u][v][k][m]);
								
								
								if(u!=v) {
									this.bw_cost += fVar[u][v][k][m];
									System.out.println("bw_cost " +bw_cost);
									if (!(updateSubLink(this.substrate,u, v, fVar[u][v][k][m])))
										throw new ArithmeticException("Substrate Link Capacity not updated");
									hops_link_path++;
								}
								
								
							}
						}		
					}
				   avg_hops = avg_hops + hops_link_path;
				   /////////
				}
		   } ///// end loop
		   
		   
		   this.hops_path= avg_hops / links.size();
		   
	  }

	
	  public double getBWcost() {
		  return this.bw_cost;
	  }
	  
	  public double getHopsPathAVG() {
		  return this.hops_path;
	  }
	 
	  public Link getLink(int a,int  b, ArrayList<Link> links, Graph<Node,Link> graph){ 
			Link l = null;
			for(Link link: links){
			    Pair<Node> currentNodes = graph.getEndpoints(link);
			    if (currentNodes.getFirst().getId()==a){
			    	if(currentNodes.getSecond().getId()==b){
			    		return link;
			    	}
			    }
			}
			return l;
		}
	
	  
	  public Node getKeyByValue(Map<Node, Node> map, Node value) {
		  for (Map.Entry<Node, Node> entry : map.entrySet()) {
		      if (value.equals(entry.getValue())) {
		          return entry.getKey();
		      }
		  }
		  return null;
		}
	
	public ArrayList<Node> getKeysByValue(Map<Node, Node> map, Node value) {
		ArrayList<Node> tmp = new ArrayList<Node>();
		  for (Map.Entry<Node, Node> entry : map.entrySet()) {
		      if (value.equals(entry.getValue())) {
		          tmp.add(entry.getKey());
		      }
		  }
		  return tmp;
		}
	
public  MultiHashMap<HashMap<Pair<Integer>, Double>, List<Pair<Integer>>> getLinkMapping(){
	return this.linkMap;
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

private boolean updateSubLink(Substrate sub, int src, int dst, double cap) {
	for (Link x : sub.getGraph().getEdges()) {
		Pair<Node> eps = sub.getGraph().getEndpoints(x);
		if ((eps.getFirst().getId() == src)
				&& (eps.getSecond().getId() == dst)) {
			// System.out.println("Found edge: " + src+ " -> " +dst +
			// " for " +
			// eps.getFirst().getId()+" -> "+eps.getSecond().getId());
			double newCap = x.getBandwidth() - cap;
			if ((newCap < 0.1) && (newCap > 0)) {
				System.out.println("Found edge: " + src + " -> " + dst
						+ " for " + eps.getFirst().getId() + " -> "
						+ eps.getSecond().getId());
				System.out.println(x.getBandwidth() + " cap: " + cap + " "
						+ newCap);
			}
			/*if (eps.getFirst().getType().equalsIgnoreCase("switch")) {
				updateSubstrateTCAM(sub, eps.getFirst());
			}*/
			x.setBandwidth((int) newCap);
		   // System.out.println("newCap1 "+newCap);
			if (newCap<0)
			{
				System.out.println("newCap "+newCap);
				System.exit(0);
			}
		//    System.exit(0);
			// System.out.println(newCap);
			return true;
		}
	}

	return false;

}

private boolean updateSubstrateTCAM(Substrate sub, Node node) {
	for (Node x : sub.getGraph().getVertices()) {
		if (x.getId() == node.getId()) {
			// System.out.println(x.getId()+ " " +cap +
			// " "+x.getAvailableCpu());
			int capNew = x.getTCAM() - 1;
			// System.out.println((int)capNew);
			x.setTCAM(capNew);
			return true;
		}

	}
	return false;
}
}
