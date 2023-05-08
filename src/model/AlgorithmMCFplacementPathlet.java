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
import java.util.Set;

import model.components.Link;
import model.components.Node;
import model.components.Path;

import org.apache.commons.collections15.multimap.MultiHashMap;
import org.apache.commons.math3.ml.distance.EuclideanDistance;

import tools.LinkComparator;
import tools.NodeComparator;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

public class AlgorithmMCFplacementPathlet  {
	 private double[][][][][] xVar;
	 private Substrate substrate;
	 private Request req;
	 private LinkMapping lm = new LinkMapping();
	 private int[][][][][] pathlet;
	 private int paths_max;
	 private double[][][] subLinks;
	// private ArrayList<LinkMapSFC> linkMap =new  ArrayList<LinkMapSFC>();
	 private HashMap<Link, ArrayList<LinkedHashMap<Link, Double>>> linkMap= new HashMap<Link,ArrayList<LinkedHashMap<Link,Double>>>();
	 private LinkedHashMap<Node,Node> nodemap = new LinkedHashMap<Node,Node>();
	 private MultiHashMap<Pair<Integer>, HashMap<List<Pair<Integer>>,Integer>> pathletsUse= new MultiHashMap<Pair<Integer>, HashMap<List<Pair<Integer>>,Integer>>();
	
	public AlgorithmMCFplacementPathlet(double[][][][][] xVarFinal, Substrate sub,Request req,LinkedHashMap<Node,Node> nodemap, 
			int paths_max, int[][][][][] pathlets, double[][][] subLinks, MultiHashMap<Pair<Integer>, HashMap<List<Pair<Integer>>,Integer>> pathletsUse ){
		this.xVar=xVarFinal;
		this.substrate=sub;
		this.req=req;
		this.nodemap=nodemap;
		this.paths_max=paths_max;
		this.pathlet=pathlets;
		this.subLinks = subLinks;
		this.pathletsUse=pathletsUse;
	}
	
	



	public boolean  RunMCF() {
		
		Graph<Node, Link> sub= substrate.getGraph();
		ArrayList<Node> subNodesList = (ArrayList<Node>) getNodes(sub);
		ArrayList<Link> subLinksList = (ArrayList<Link>) getLinks(sub);
		int subNodesNum = subNodesList.size();	
		
/*		//Adjacency Matrix Substrate
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
*/		String[] subType =  new String[subNodesNum];
	    int[] flowCap =  new int [subNodesNum];
		for (Node x: subNodesList){		
			//System.out.println(x.getId()+ " " + reqNodes.size());
			subType[x.getId()] = x.getType();
			flowCap[x.getId()]=  x.getTCAM();
		}	
		
		
			
		
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

		IloNumVar[][][][][] f_mcf = new IloNumVar [paths_max][subNodesNum][subNodesNum][][];
		for (int p=0;p<paths_max;p++){
			f_mcf[p]=new IloNumVar[subNodesNum][][][];
			for (int u=0;u<subNodesNum;u++){
				f_mcf[p][u]=new IloNumVar[subNodesNum][][];
				for (int v=0;v<subNodesNum;v++){
					f_mcf[p][u][v]=new IloNumVar[nodesSFC][];
						for(int k=0; k<nodesSFC; k++){
							f_mcf[p][u][v][k]= new IloNumVar[nodesSFC];
							for(int m=0; m<nodesSFC; m++){
								f_mcf[p][u][v][k][m]=cplex1.numVar(0, 1000000000);
							}
					}
				}
			}
		}

	/*****************************Objective Function **************************************************/
		IloLinearNumExpr flows = cplex1.linearNumExpr();
		for(int i=0; i< sizeOfSFC; i++){
			for(int j=0; j<sizeOfSFC; j++){
				for (int p=0;p<paths_max;p++){
					for (int u=0;u<subNodesNum;u++){
						for (int v=0;v<subNodesNum;v++){
							double path_w = 1;
							for (int k=0;k<subNodesNum;k++){
								for (int m=0;m<subNodesNum;m++){
									//System.out.println(pathlet[p][u][v][k][m] + " " +k + " " +m);
									path_w= path_w+ pathlet[p][u][v][k][m];
								}
							}
							if (path_w>1) path_w--; //correction for non_pathlets
							//flows.addTerm(path_w, fl[p][u][v][i][j]);
							//System.out.println(u + " " +v + " " +i + " " + j + " " +p + " " +path_w);	
							flows.addTerm(path_w, f_mcf[p][u][v][i][j]);														
						}
					}
				}
				
			}
			
		}
			cplex1.addMinimize(flows);
			
			
	/*****************************Capacity Constraints **************************************************/			
			int counter = 0;
			for (int p=0;p<paths_max;p++){ 
				for (int u=0;u<subNodesNum;u++){
					for (int v=0;v<subNodesNum;v++){
					//	System.out.println("subLinksAug" + u + "  " +v + " " + p);
						double cap =subLinks[p][u][v];
						IloLinearNumExpr bwReq = cplex1.linearNumExpr();
							for(int k=0; k< nodesSFC; k++){
								for(int m=0; m< nodesSFC; m++){
									bwReq.addTerm(1,f_mcf[p][u][v][k][m]);
								}
							}
					cplex1.addLe(bwReq, cap);
					counter++;
					}
				}	
			}
						
	/*****************************Flow Constraints **************************************************/	
	
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

						IloLinearNumExpr flowCon1 = cplex1.linearNumExpr();	
							if (id1!=id2) {
								for (int l=0;l<subNodesNum;l++){
									if (l!=id1){
										for (int p=0;p<paths_max;p++){
										flowCon1.addTerm(1,f_mcf[p][id1][l][k][m]); //incoming	
										flowCon1.addTerm(-1,f_mcf[p][l][id1][k][m]);
										}
									}
								}
								
							}
							cplex1.addEq(flowCon1, capVirt);
						}
					}
			}

	
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

							for (int l=0;l<subNodesNum;l++){
								for (int p=0;p<paths_max;p++){
								flowCon2.addTerm(1,f_mcf[p][l][u][k][m]); //incoming
								}
							}
							for (int v=0;v<subNodesNum;v++){
								for (int p=0;p<paths_max;p++){
								flowCon1.addTerm(1,f_mcf[p][u][v][k][m]); //outgoing
								}
							}
							cplex1.addEq(flowCon1, flowCon2);
							//cplex1.addEq(flowCon3, flowCon4);
							}
						}

					}
				}
			}
				
			


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
			 
			 
			double[][][][][] fVar =new double [paths_max][subNodesNum][subNodesNum][nodesSFC][nodesSFC];
			for (int p=0;p<paths_max;p++){
				for (int u=0;u<subNodesNum;u++){
						for (int v=0;v<subNodesNum;v++){
							for(int k=0; k<nodesSFC; k++){
								for(int m=0; m<nodesSFC; m++){
								fVar[p][u][v][k][m] = cplex1.getValue(f_mcf[p][u][v][k][m]);
									if (fVar[p][u][v][k][m]>0.00000000001){
											System.out.println(p+ ": " + k+" "+m + " to " + u+ " "+v +" "+ fVar[p][u][v][k][m]);
									}
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

	  public void linkMapping (double[][][][][] fVar ){
		  ArrayList<Node> subNodes = this.substrate.getNodes(this.substrate.getGraph());
		  ArrayList<Link> subLinks = this.substrate.getLinks(this.substrate.getGraph());
		 // updatedSubLinks.print();
		  //System.exit(0);
		  int subNodesNum = subNodes.size();
		  
		  for (int p=0;p<paths_max;p++){
		   for (int u=0;u<subNodesNum;u++){
				for (int v=0;v<subNodesNum;v++){
				int numNodesSFC= req.getGraph().getVertexCount();
				ArrayList<Link> reqLinks = this.req.getLinks(this.req.getGraph());
					for(int k=0; k< numNodesSFC; k++){
						for(int m=0; m< numNodesSFC; m++){
							if (fVar[p][u][v][k][m]>0.000000001){
								LinkedHashMap<Link, Double> tmp = new LinkedHashMap<Link, Double>();
								if (u!=v){
									/*Link dummy= substrate.getLinkFactory().create("dummy");
									//tmp.put(dummy, fVar[u][v][k][m]);
								}*/
								//else {
									Link subsrateLink=getLink(u,v,subLinks, this.substrate.getGraph());
									tmp.put(subsrateLink, fVar[p][u][v][k][m]);
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
