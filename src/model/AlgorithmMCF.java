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
import java.util.LinkedHashMap;

import model.components.Link;
import model.components.Node;

import org.apache.commons.math3.ml.distance.EuclideanDistance;

import tools.LinkComparator;
import tools.NodeComparator;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

public class AlgorithmMCF  {
	 private double[][][]zVar;
	 private Substrate substrate;
	 private ArrayList<Request> reqs;
	 private LinkMapping lm = new LinkMapping();
	 private ArrayList<LinkMapSFC> linkMap =new  ArrayList<LinkMapSFC>();
	
	public AlgorithmMCF(double[][][]zVarFinal, Substrate sub,ArrayList<Request> reqs){
		this.zVar=zVarFinal;
		this.substrate=sub;
		this.reqs=reqs;
	}
	
	



	public AlgorithmMCF(double[][][][] retval, Substrate substrateCopy,
			Request requestCopy, LinkedHashMap<Node, Node> nodeMap) {
		// TODO Auto-generated constructor stub
	}





	public void RunMCF() {
		
		Graph<Node, Link> sub= substrate.getGraph();
		ArrayList<Node> subNodesList = (ArrayList<Node>) getNodes(sub);
		ArrayList<Link> subLinksList = (ArrayList<Link>) getLinks(sub);
		int subNodesNum = subNodesList.size();	
		int numSFCs=reqs.size();
		//Adjacency Matrix Substrate
		double[][] subLinks = new double[subNodesNum][subNodesNum];
		for (Link y: subLinksList){
			Pair<Node> tmp = sub.getEndpoints(y);
			subLinks[tmp.getFirst().getId()][tmp.getSecond().getId()]  = y.getBandwidth();
		}
		//System.out.println("subNodesNum: "+subNodesNum);	
		//System.out.println("subLinks: "+Arrays.deepToString(subLinks));
	
		int max_num = 0;
		for (Request req: reqs){
			max_num =  Math.max(max_num, req.getGraph().getVertexCount());
		}
		
		double[][][] sfcLinks = new double[numSFCs][max_num][max_num]; //replace witn max	
		int counter=0;
		for (Request req: reqs){
			ArrayList<Link> links =(ArrayList<Link>) getLinks(req.getGraph());
			for (Link y: links){
			Pair<Node> tmp = req.getGraph().getEndpoints(y);
			sfcLinks[counter][tmp.getFirst().getId()][tmp.getSecond().getId()]  = y.getBandwidth();
			}
			counter++;
		}
		
	//	System.out.println("sfcLinks: "+Arrays.deepToString(sfcLinks[0]));

		
	
	try {
	 	IloCplex cplex1 = new IloCplex();
		//cplex.setParam(IloCplex.DoubleParam.TiLim, 600);
		cplex1.setParam(IloCplex.DoubleParam.ObjULim,Double.MAX_VALUE);
		cplex1.setParam(IloCplex.DoubleParam.ObjLLim,-1*Double.MAX_VALUE);
		
				
		/*****************************System Variables **************************************************/

					//create f continuous variable, with bounds lb and ub
			//////////////////////////////////////////////////////////////////

			IloNumVar[][][][][] f_mcf = new IloNumVar[subNodesNum][subNodesNum][numSFCs][][];
			counter=0;
			for (int u=0;u<subNodesNum;u++){
				f_mcf[u]=new IloNumVar[subNodesNum][][][];
				for (int v=0;v<subNodesNum;v++){
					f_mcf[u][v]=new IloNumVar[numSFCs][][];
					for (int f=0;f<numSFCs;f++){
						Request req = reqs.get(f);
						ArrayList<Node> sfc= req.getNodes(req.getGraph());
						int numNodesSFC=sfc.size();
						f_mcf[u][v][f]=new IloNumVar[numNodesSFC][];
						for(int k=0; k< numNodesSFC; k++){
							f_mcf[u][v][f][k]=new IloNumVar[numNodesSFC];
							for(int m=0; m<numNodesSFC; m++){
								f_mcf[u][v][f][k]=cplex1.numVarArray(numNodesSFC, 0,1000000);
							}
						}
					}
				}
			}

			/*****************************Objective Function **************************************************/
			IloLinearNumExpr flows = cplex1.linearNumExpr();
			for (int u=0;u<subNodesNum;u++){
				for (int v=0;v<subNodesNum;v++){
					for (int f=0;f<numSFCs;f++){
						//int numNodesSFC= sfcNodes[f].length;
						Request req = reqs.get(f);
						ArrayList<Node> sfc= req.getNodes(req.getGraph());
						int numNodesSFC=sfc.size();
							for(int k=0; k< numNodesSFC; k++){
								for(int m=0; m<numNodesSFC; m++){
									//System.out.println(u + " " +v + " " +f + " " + k + " " +m);
									flows.addTerm(1, f_mcf[u][v][f][k][m]);
							}
						}
					}
				}
			}	
			cplex1.addMinimize(flows);
			
			
	/*****************************Capacity Constraints **************************************************/			
			for (int u=0;u<subNodesNum;u++){
				for (int v=0;v<subNodesNum;v++){
					IloLinearNumExpr bwReq = cplex1.linearNumExpr();
					for (int f=0;f<numSFCs;f++){
						Request req = reqs.get(f);
						ArrayList<Node> sfc= req.getNodes(req.getGraph());
						int numNodesSFC=sfc.size();
						for(int k=0; k< numNodesSFC; k++){
							for(int m=0; m<numNodesSFC; m++){
							//	System.out.print(" ["+k+"]["+m+"]" + " SFC: " +  f);
								bwReq.addTerm(1,f_mcf[u][v][f][k][m]);
							}
						}
					}
					double cap = subLinks[u][v];	
					//System.out.println("Cap:"+cap);
					cplex1.addLe(bwReq, cap);
				}
			}		
			
			/*****************************Flow Constraints **************************************************/
			   
			   for (int f=0;f<numSFCs;f++){
			    int numNodesSFC= reqs.get(f).getGraph().getVertexCount();
				for (int u=0;u<subNodesNum;u++){
						for(int k=0; k< numNodesSFC; k++){
							for(int m=0; m<numNodesSFC; m++){
								IloLinearNumExpr flow2 = cplex1.linearNumExpr();
								double cap = sfcLinks[f][k][m];
							//	double inv_cap = sfcLinks[m][k];
								double capCon=cap*(zVar[u][f][k]-zVar[u][f][m]);
								//System.out.println(zVar[u][f][k]+ " "+ zVar[u][f][m]+ " " +cap+" " +capCon);
								for (int v=0;v<subNodesNum;v++){
									flow2.addTerm(1, f_mcf[u][v][f][k][m]);	
									flow2.addTerm(-1, f_mcf[v][u][f][k][m]);
								}
								cplex1.addEq(flow2, capCon);
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
			 
			 
				double[][][][][] fVar =new double [subNodesNum][subNodesNum][numSFCs][max_num][max_num];
				for (int u=0;u<subNodesNum;u++){
					for (int v=0;v<subNodesNum;v++){
							for (int f=0;f<numSFCs;f++){
								int numNodesSFC=reqs.get(f).getGraph().getVertexCount();
								//int numNodesSFC= sfcNodes[f].length;
								for(int k=0; k< numNodesSFC; k++){
									for(int m=0; m< numNodesSFC; m++){
										fVar[u][v][f][k][m] = cplex1.getValue(f_mcf[u][v][f][k][m]);
										if (fVar[u][v][f][k][m]>0){
											// if ((f==0) && (k==2))
											 System.out.println("SFC : "+f+ " " +k+" "+m + " to " + u+ " "+v +" "+ fVar[u][v][f][k][m]);
										}
									}		
								}
							}
					}
				}
				
				lm.linkMapping1(substrate, (ArrayList<Request>) reqs, fVar);
				this.linkMap =  lm.getLinkMappingAll();

			System.out.println("###################################");
		}
		
		cplex1.end();
	} catch (IloException e) {
	System.err.println("Concert exception caught: " + e);
	}

	}

public  ArrayList<LinkMapSFC> getLinkMapping(){
	return this.linkMap;
}

public Substrate getUpdatedSub(){
	return lm.getUpdatedSubstrate();
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
