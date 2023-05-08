package model;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.components.Link;
import model.components.Node;
import model.components.Path;

import org.apache.commons.math3.ml.distance.EuclideanDistance;

import tools.LinkComparator;
import tools.NodeComparator;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

public class AlgorithmMCFplacement  {
	 private double[][][][] xVar;
	 private Substrate substrate;
	 private Request req;
	 private LinkMapping lm = new LinkMapping();
	// private ArrayList<LinkMapSFC> linkMap =new  ArrayList<LinkMapSFC>();
	 private HashMap<Link, ArrayList<LinkedHashMap<Link, Double>>> linkMap= new HashMap<Link,ArrayList<LinkedHashMap<Link,Double>>>();
	 private LinkedHashMap<Node,Node> nodemap = new LinkedHashMap<Node,Node>();
	
	public AlgorithmMCFplacement(double[][][][] xVarFinal, Substrate sub,Request req,LinkedHashMap<Node,Node> nodemap){
		this.xVar=xVarFinal;
		this.substrate=sub;
		this.req=req;
		this.nodemap=nodemap;
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
/*		int[] subCap = new int[]{0,0,0,100,0,0,100}; /// testing
		int subNodesNum = subCap.length;
		int[][] subLinks = new int[][] {
				{Integer.MAX_VALUE,50,0,0,50,0,0},
				{60,Integer.MAX_VALUE, 50,0,0,0,0}, 
				{0, 60, Integer.MAX_VALUE,50,0,0,0},
				{0,0,60,Integer.MAX_VALUE,0,0,0},
				{60,0,0,0,Integer.MAX_VALUE,50,0},  
				{0,0,0,0,60,Integer.MAX_VALUE,50},  
				{0,0,0,0,0,60,Integer.MAX_VALUE}};
		
		
		int[] sfcNodes = new int[] {50,50,50};
		int sizeOfSFC = sfcNodes.length;
		int[][]sfcLinks = new int[][]{{0,10,0},{20,0,10},{0,20,0}};
		int nodesSFC = sizeOfSFC;	
		LinkedHashMap<Integer,Integer> map = new LinkedHashMap<Integer,Integer>();
		map.put(0,3);
		map.put(1,6);
		map.put(2,3);*/
	/*	int[] sfcNodes = new int[] {50,50};
		int sizeOfSFC = sfcNodes.length;
		int[][]sfcLinks = new int[][]{{0,10},{20,0}};
		int nodesSFC = sizeOfSFC;	
		LinkedHashMap<Integer,Integer> map = new LinkedHashMap<Integer,Integer>();
		map.put(0,3);
		map.put(1,6);*/
		
/*		int[] subCap = new int[]{100,0,30,110};
		int subNodesNum = subCap.length;
		int[][] subLinks = new int[][] {{Integer.MAX_VALUE,50,0,0},
				{60,Integer.MAX_VALUE, 50,0}, 
				{0, 60, Integer.MAX_VALUE,50},
				{0,0,60,Integer.MAX_VALUE}};
		
		int[] sfcNodes = new int[] {50,50};
		int sizeOfSFC = sfcNodes.length;
		int[][]sfcLinks = new int[][]{{0,10},{20,0}};
		int nodesSFC = sizeOfSFC;	
		LinkedHashMap<Integer,Integer> map = new LinkedHashMap<Integer,Integer>();
		map.put(0,0);
		map.put(1,3);*/
	//	System.out.println ("sfcNodes: " + Arrays.toString(sfcNodes));
	//	System.out.println("sfcLinks: "+Arrays.deepToString(sfcLinks));

	//	System.out.println("sfcLinks: "+Arrays.deepToString(sfcLinks[0]));

		
	
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
/*			for(int k=0; k< nodesSFC;k++){
				Node tmp = this.nodemap.get(reqNodes.get(k));
				int id = tmp.getId();
				for(int m=0; m< nodesSFC; m++){
				IloLinearNumExpr capReq = cplex1.linearNumExpr();
				for (int v=0;v<subNodesNum;v++){
					capReq.addTerm(1,f_mcf[id][v][k][m]);
				//	capReq.addTerm(-1,f_mcf[v][id][k][m]);
				}
				double capVirt =sfcLinks[k][m];
				cplex1.addEq(capReq,capVirt);
				}
			}
			
			for(int k=0; k< nodesSFC;k++){
				Node tmp = this.nodemap.get(reqNodes.get(k));
				int id = tmp.getId();
				for(int m=0; m< nodesSFC; m++){
				IloLinearNumExpr capReq = cplex1.linearNumExpr();
				for (int v=0;v<subNodesNum;v++){
					capReq.addTerm(1,f_mcf[v][id][m][k]);
				//	capReq.addTerm(-1,f_mcf[id][v][m][k]);
				}
				double capVirt =sfcLinks[m][k];
				cplex1.addEq(capReq,capVirt);
				}
			}*/
			
			
		for(int k=0; k< nodesSFC;k++){
				//int id1=map.get(k);
				Node tmp = this.nodemap.get(reqNodes.get(k));
				int id1 = tmp.getId();
				for(int m=0; m< nodesSFC; m++){
					//int id2=map.get(m);
					Node tmp2 = this.nodemap.get(reqNodes.get(m));
					int id2 = tmp2.getId();
						double capVirt =sfcLinks[k][m];
						if(capVirt>0){
						//for (int u=0;u<subNodesNum;u++){
						IloLinearNumExpr flowCon1 = cplex1.linearNumExpr();	
							if (id1!=id2) {
								for (int l=0;l<subNodesNum;l++){
									if (l!=id1){
									//	System.out.println("ff: " + id1+" " +l + " " +k + " " + m);
										flowCon1.addTerm(1,f_mcf[id1][l][k][m]); //incoming	
										flowCon1.addTerm(-1,f_mcf[k][id1][k][m]);
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
				//int id1=map.get(k);
				Node tmp = this.nodemap.get(reqNodes.get(k));
				int id1 = tmp.getId();
				for(int m=0; m< nodesSFC; m++){
				//	int id2=map.get(m);
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
			
			//ayto de douleyei
			for (int u=0;u<subNodesNum;u++){
				for(int k=0; k< nodesSFC;k++){
					for(int m=0; m< nodesSFC; m++){
						//int id1=map.get(k);
						//int id2=map.get(m);
						Node tmp1 = this.nodemap.get(reqNodes.get(k));
						int id1 = tmp1.getId();
						Node tmp2 = this.nodemap.get(reqNodes.get(m));
						int id2 = tmp2.getId();
						if (sfcLinks[k][m]!=0){
							if ((Integer.compare(id1,u)!=0) && (Integer.compare(id2,u)!=0)) { //not origin or destination of requested
							IloLinearNumExpr flowCon1 = cplex1.linearNumExpr();	
							IloLinearNumExpr flowCon2 = cplex1.linearNumExpr();
						//	IloLinearNumExpr flowCon3 = cplex1.linearNumExpr();	
						//	IloLinearNumExpr flowCon4 = cplex1.linearNumExpr();
							for (int l=0;l<subNodesNum;l++){
							//	if ((Integer.compare(id1,l)!=0) || (Integer.compare(id2,l)!=0)){
								flowCon2.addTerm(1,f_mcf[l][u][k][m]); //incoming
							//	flowCon3.addTerm(1,f_mcf[u][l][m][k]); //outgoing
							//	}
							}
							for (int v=0;v<subNodesNum;v++){
							//	if ((Integer.compare(id1,v)!=0) || (Integer.compare(id2,v)!=0)){
								flowCon1.addTerm(1,f_mcf[u][v][k][m]); //outgoing
							//	flowCon4.addTerm(1,f_mcf[v][u][m][k]); //incoming
							//	}
							}
							cplex1.addEq(flowCon1, flowCon2);
							//cplex1.addEq(flowCon3, flowCon4);
							}
						}

					}
				}
			}
				
			
			//IloLinearNumExpr capReq1 = cplex1.linearNumExpr();
		/*	if ((id1!=u) && (id2!=u)) { //not origin or destination of requested
				for (int v=0;v<subNodesNum;v++){
					System.out.println("ff: " + v +" " +u + " " +k + " " + m);
					capReq.addTerm(1,f_mcf[v][u][k][m]);
				  //  capReq1.addTerm(1,f_mcf[v][u][m][k]);
					for (int l=0;l<subNodesNum;l++){
						if (l!=v){
						capReq.addTerm(-1,f_mcf[u][l][k][m]);
						System.out.println("xx: " + u +" " +l + " " +k + " " + m);
						//capReq1.addTerm(-1,f_mcf[u][l][m][k]);
						}
					}
					
					System.out.println("eq");
					cplex1.addEq(capReq,0);
					
				}
			}*/
			
		//}

	  // System.exit(0);
		cplex1.exportModel("lpmcf.lp");
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
										System.out.println(k+" "+m + " to " + u+ " "+v +" "+ fVar[u][v][k][m]);
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
		  ArrayList<Link> subLinks = this.substrate.getLinks(this.substrate.getGraph());
		 // updatedSubLinks.print();
		  //System.exit(0);
		  int subNodesNum = subNodes.size();
		  
		   for (int u=0;u<subNodesNum;u++){
				for (int v=0;v<subNodesNum;v++){
				int numNodesSFC= req.getGraph().getVertexCount();
				ArrayList<Link> reqLinks = this.req.getLinks(this.req.getGraph());
					for(int k=0; k< numNodesSFC; k++){
						for(int m=0; m< numNodesSFC; m++){
							if (fVar[u][v][k][m]>0.000000001){
								LinkedHashMap<Link, Double> tmp = new LinkedHashMap<Link, Double>();
								if (u!=v){
									/*Link dummy= substrate.getLinkFactory().create("dummy");
									//tmp.put(dummy, fVar[u][v][k][m]);
								}*/
								//else {
									Link subsrateLink=getLink(u,v,subLinks, this.substrate.getGraph());
									tmp.put(subsrateLink, fVar[u][v][k][m]);
								}
								Link sfcLink=getLink(k,m,reqLinks, this.req.getGraph());
								ArrayList<LinkedHashMap<Link,Double>> linkMappings =  this.linkMap.get(sfcLink);
								if (linkMappings==null){
									linkMappings = new ArrayList<LinkedHashMap<Link,Double>>();
								}
								linkMappings.add(tmp);
								this.linkMap.put(sfcLink, linkMappings);	
							}
						}		
					}
				}
		   }
		   
		   
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
	
public  HashMap<Link, ArrayList<LinkedHashMap<Link, Double>>> getLinkMapping(){
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
}
