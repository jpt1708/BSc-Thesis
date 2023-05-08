package model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import tools.NodeComparator;
import edu.uci.ics.jung.graph.Graph;
import model.components.Link;
import model.components.Node;

public class NFmappingRounding {
	  private LinkedHashMap<Node,LinkedHashMap<Node, Integer>> nfMap = new LinkedHashMap<Node, LinkedHashMap<Node, Integer>> ();
	  private ArrayList<LinkedHashMap<Node, ArrayList<Node>>> nodeMap = new ArrayList<LinkedHashMap<Node, ArrayList<Node>>> ();
	  private Substrate updatedSub;
	  private double[][][][][] xVarFinal;
	  private double[][][][][] xVarFinalInt;
	
	  public void instanceMapping(Request req, Substrate sub, double[][][][][] xVar, double[][][][][] fVar, int paths ){
		    int subNodesNum = sub.getGraph().getVertexCount();
		    this.updatedSub=sub;
		    int nodesSFC = req.getGraph().getVertexCount();
		    int subNodesNumAug=nodesSFC+subNodesNum;
		    xVarFinal = new  double [paths][subNodesNumAug][subNodesNumAug][nodesSFC][nodesSFC];
		    xVarFinalInt= new  double [paths][subNodesNumAug][subNodesNumAug][nodesSFC][nodesSFC];
		   
		    HashMap<Integer, Double> capacities= new  HashMap<Integer, Double>();
		    for (Node node: req.getGraph().getVertices()) {
		    	capacities.put(node.getId()+subNodesNum, (double) node.getAvailableCpu());
		    }

		   // System.out.println("x: "+Arrays.deepToString(xVar));
			for(int k=0; k< nodesSFC; k++){
				for(int m=0; m<nodesSFC; m++){
					for (int p=0;p<paths;p++){
						for (int u=0;u<subNodesNumAug;u++){
							for (int v=0;v<subNodesNumAug;v++){	
								if ((u-subNodesNum==k) || (v-subNodesNum==m)) {
									if (xVar[p][u][v][k][m]>0.00000000001){
										double flow= fVar[p][u][v][k][m]*xVar[p][u][v][k][m];
										xVarFinal[p][u][v][k][m] = flow;
/*										System.out.println(p + " " + u +" "+  v +"  "+ k +" " +m);
										 System.out.println(xVar[p][u][v][k][m] + "  " + fVar[p][u][v][k][m]+" " +xVarFinal[p][u][v][k][m]);
										try {
									        System.in.read();
									    } catch (IOException e) {
									        // TODO Auto-generated catch block
									        e.printStackTrace();
									    }*/
									}	
								}///check only possible mappings
								
							}
						}
					}///max
				}
			}
			

			 Multimap<Integer,Integer> myMultimap = ArrayListMultimap.create(); //real - virtual
			// System.out.println("xVarFinal: "+Arrays.deepToString(xVarFinal));	
			
				for(int k=0; k< nodesSFC; k++) {
					for(int m=0; m<nodesSFC; m++){
						double max=0;
						int indexReal =0;
						int indexVirt=0;
						int indexPath=0;
						boolean visited=false;
							for (int v=0;v<subNodesNumAug;v++){	
								for (int p=0;p<paths;p++){
		/*							if (k+subNodesNum==38) {
										System.out.println("first: " + p + " " + (k+subNodesNum) +" "+ v +"  "+ k +" " +m);
										System.out.println(xVarFinal[p][k+subNodesNum][v][k][m]);
									}*/
									if (xVarFinal[p][k+subNodesNum][v][k][m]>max){
										if ((!(myMultimap.containsKey(v)))&& (!(myMultimap.containsValue(k+subNodesNum))) ){
											if (checkFeasibilityNode(k+subNodesNum,sub,capacities)) {
												max=xVarFinal[p][k+subNodesNum][v][k][m];
												indexPath=p;
												indexVirt=k+subNodesNum;
												indexReal=v;
												visited=true;
												myMultimap.put(v, k+subNodesNum);
											}
/*											System.out.println("first: " + p + " " + (k+subNodesNum) +" "+ v +"  "+ k +" " +m);
											try {
										        System.in.read();
										    } catch (IOException e) {
										        // TODO Auto-generated catch block
										        e.printStackTrace();
										    }*/
										}
									}
							}
						}
						if (visited) {
							xVarFinalInt[indexPath][indexVirt][indexReal][k][m]=1;
						}
					}
				}
				
				
				for(int m=0; m<nodesSFC; m++){
					for(int k=0; k< nodesSFC; k++) {
						double max=0;
						int indexReal =0;
						int indexVirt=0;
						int indexPath=0;
						boolean visited=false;
							for (int u=0;u<subNodesNumAug;u++){	
								for (int p=0;p<paths;p++){
/*								if (m+subNodesNum==38) {
										System.out.println("sec: " + p + " " + u+" "+ (m+subNodesNum)  +"  "+ k +" " +m);
										System.out.println(xVarFinal[p][u][m+subNodesNum][k][m]);
										System.out.println(max);
									}*/
									
									if (xVarFinal[p][u][m+subNodesNum][k][m]>max){
										if ((!(myMultimap.containsKey(u))) && (!(myMultimap.containsValue(m+subNodesNum))) ) {
											if (checkFeasibilityNode(m+subNodesNum,sub,capacities)) {
											max=xVarFinal[p][u][m+subNodesNum][k][m];
											indexPath=p;
											indexVirt=m+subNodesNum;
											indexReal=u;
											visited=true;
											}
/*											System.out.println("second-A: " + p + " " + u +" "+  (m+subNodesNum) +"  "+ k +" " +m);
											try {
										        System.in.read();
										    } catch (IOException e) {
										        // TODO Auto-generated catch block
										        e.printStackTrace();
										    }*/
											myMultimap.put(u, m+subNodesNum);
										}
										else if (myMultimap.containsEntry(u,(m+subNodesNum))) {
											max=xVarFinal[p][u][m+subNodesNum][k][m];
											indexPath=p;
											indexVirt=m+subNodesNum;
											indexReal=u;
											visited=true;
/*											System.out.println("second-B: " + p + " " + u +" "+  (m+subNodesNum) +"  "+ k +" " +m);
											try {
										        System.in.read();
										    } catch (IOException e) {
										        // TODO Auto-generated catch block
										        e.printStackTrace();
										    }*/
											break;
										}
									}
							}
						}
						if (visited) {
							xVarFinalInt[indexPath][indexReal][indexVirt][k][m]=1;
						}
					}
				}
				
		
	
	   }
	  public boolean checkFeasibilityNode(int id, Substrate sub, HashMap<Integer, Double> capacities) {
		  for(Node node: sub.getGraph().getVertices()) {
			  if (node.getId()==id) {
				  if (capacities.get(id)>node.getAvailableCpu())
					  return false;
			  }
		  }
		  
		return true;
		  
	  }
	  
	  
/*	  public boolean checkFeasibility (double[][][][][] xVarFinalInt, Request req) {
	
	  for (Link link: req.getGraph().getEdges()) {
		  int ep1  =link.getEndpoint1().getId();
		  int ep2  =link.getEndpoint2().getId();
		  
	  }
		  
	  return true;
	  }*/
	  
	  public void updateNFintances(){
		 LinkedHashMap<Node,LinkedHashMap<Node, Integer>> nfMapNew = new LinkedHashMap<Node, LinkedHashMap<Node, Integer>> ();

		  
		  
	  }
	   
	
	   private boolean updateSubstrate(Node node, double cap){
		   for (Node x: this.updatedSub.getGraph().getVertices()){
			   if (x.getId()==node.getId()){
				   //System.out.println(x.getId()+ " " +cap + " "+x.getAvailableCpu());	   
				   double capNew= (x.getAvailableCpu()-cap);
				   //System.out.println((int)capNew);
				   x.setAvailableCpu((int) capNew);
				   //this.updatedSub.getNodes(this.updatedSub.getGraph()).get(x.getId()).
				   	//						setAvailableCpu((int) capNew);
				  // this.updatedSub.getNodes(this.updatedSub.getGraph()).get(x.getId()).
						//setCpu((int) capNew);
				   return true;
			   }
		   }
		   return false;
	   }
	
	   public Substrate getUpdatedSub(){
		   return this.updatedSub;
	   }
	   @SuppressWarnings("unchecked")
	   public ArrayList<Node> getNodes(Graph<Node,Link> t) {
	   	ArrayList<Node> reqNodes =new ArrayList<Node>();

	   	for (Node x: t.getVertices())
	   		reqNodes.add(x);

	   	Collections.sort(reqNodes,new NodeComparator());

	   	return reqNodes;
	   }
	   
	   public LinkedHashMap<Node,LinkedHashMap<Node, Integer>>  getNFMap(){
		   return this.nfMap;
	   }
	   public ArrayList<LinkedHashMap<Node, ArrayList<Node>>> getNodeMap(){
		   return this.nodeMap;
	   }
	   

	   public double[][][][][] getNodeMappingTable(){
		   return this.xVarFinalInt;
	   }
/*	   public LinkedHashMap<Node, Integer> getInstanceMap(){
		   return this.instanceNum;
	   }*/
	   
}
