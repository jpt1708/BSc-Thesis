package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

import tools.NodeComparator;
import edu.uci.ics.jung.graph.Graph;
import model.components.Link;
import model.components.Node;

public class NFmapping {
	  private LinkedHashMap<Node,LinkedHashMap<Node, Integer>> nfMap = new LinkedHashMap<Node, LinkedHashMap<Node, Integer>> ();
	  private ArrayList<LinkedHashMap<Node, ArrayList<Node>>> nodeMap = new ArrayList<LinkedHashMap<Node, ArrayList<Node>>> ();
	  private Substrate updatedSub;
	
	  public void instanceMapping(ArrayList<Node> nfList,ArrayList<Node> subNodes, Substrate sub, double[][][] xVar ){
		    int subNodesNum = subNodes.size();
		    this.updatedSub=sub;
		    
			for (int u=0;u<subNodesNum;u++){
				for (int i=0;i<nfList.size();i++){
				    int inst =  nfList.get(i).getInstances();
					for(int j=0; j<inst; j++){
						if (xVar[u][i][j]>0){
							LinkedHashMap<Node, Integer> tmp = new LinkedHashMap<Node, Integer>();
							if (!(updateSubstrate(subNodes.get(u),nfList.get(i).getCpu())))
								 throw new ArithmeticException("Substrate Node Capacity not updated"); 
							if (this.nfMap.get(subNodes.get(u))!=null){
								tmp=this.nfMap.get(subNodes.get(u));
								if (tmp.get(nfList.get(i))!=null){
									int instances=tmp.get(nfList.get(i))+1;
									tmp.put(nfList.get(i),instances);
								}
								else{
									 tmp.put(nfList.get(i),1);
								}
							}
							else {
							 tmp.put(nfList.get(i),1);
							 
							}
							this.nfMap.put(subNodes.get(u), tmp);
							
						}
					}
				}
			}	
			//this.updatedSub.print();
			
	   }
	   
	   public void nodeMapping(ArrayList<Request> req,ArrayList<Node> subNodes,
			  Substrate sub, double[][][]zVar){
		    int subNodesNum = subNodes.size();
		    int numSFCs = req.size();
		    
		    
			
				for (int f=0;f<numSFCs;f++){
					ArrayList<Node> req_n = (ArrayList<Node>) getNodes(req.get(f).getGraph());
					int sizeOfSFC= req.get(f).getGraph().getVertexCount();
					LinkedHashMap<Node, ArrayList<Node>> nodeMapSFC =  new  LinkedHashMap<Node, ArrayList<Node>>();
					//System.out.println(sizeOfSFC);
					for (int u=0;u<subNodesNum;u++){
						for(int k=0; k< sizeOfSFC; k++){
							if (zVar[u][f][k]>0.000000001){
								ArrayList<Node> tmp = new ArrayList<Node>();
								if (nodeMapSFC.containsKey(req_n.get(k))){
									tmp = nodeMapSFC.get(req_n.get(k));
								}
								tmp.add(subNodes.get(u));
								nodeMapSFC.put(req_n.get(k),tmp);
							}
						}
					}
					if (nodeMapSFC.size()>0){
						//System.out.println(nodeMapSFC);
						this.nodeMap.add(f,nodeMapSFC);
					}
					
				}
		
		   
	   }
	   private boolean updateSubstrate(Node node, double cap){
		   for (Node x: this.updatedSub.getGraph().getVertices()){
			   if (x.getId()==node.getId()){
				   System.out.println(x.getId()+ " " +cap + " "+x.getAvailableCpu());	   
				   double capNew= (x.getAvailableCpu()-cap);
				   System.out.println((int)capNew);
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
	   
	   
/*	   public LinkedHashMap<Node, Integer> getInstanceMap(){
		   return this.instanceNum;
	   }*/
	   
}
