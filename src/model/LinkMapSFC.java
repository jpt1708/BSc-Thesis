package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

import model.components.Link;
import model.components.Node;
import tools.LinkComparator;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

public class LinkMapSFC {
      private HashMap<Link, ArrayList<LinkedHashMap<Link, Double>>> linkMap= new HashMap<Link,ArrayList<LinkedHashMap<Link,Double>>>();
	  private Substrate updatedSubLinks;
      
	  public void linkMapping (Substrate sub, ArrayList<Request> reqs, double[][][][][] fVar, int f  ){
		  ArrayList<Node> subNodes = sub.getNodes(sub.getGraph());
		  ArrayList<Link> subLinks = sub.getLinks(sub.getGraph());
		  this.updatedSubLinks=sub;
		 // updatedSubLinks.print();
		  //System.exit(0);
		  int subNodesNum = subNodes.size();
		  int numSFCs=reqs.size();
		  
				for (int u=0;u<subNodesNum;u++){
					for (int v=0;v<subNodesNum;v++){
						//	for (int f=0;f<numSFCs;f++){
								int numNodesSFC= reqs.get(f).getGraph().getVertexCount();
								ArrayList<Link> reqLinks = reqs.get(f).getLinks(reqs.get(f).getGraph());
								for(int k=0; k< numNodesSFC; k++){
									for(int m=0; m< numNodesSFC; m++){
										if (fVar[u][v][f][k][m]>0.00000001){
											Link subsrateLink=getLink(u,v,subLinks, sub.getGraph());
											Link sfcLink=getLink(k,m,reqLinks, reqs.get(f).getGraph());
											 LinkedHashMap<Link, Double> tmp = new LinkedHashMap<Link, Double>();
											 tmp.put(subsrateLink, fVar[u][v][f][k][m]);
											 if (!(updateSubLink(subsrateLink,fVar[u][v][f][k][m])))
													 throw new ArithmeticException("Substrate Link Capacity not updated"); 
											 ArrayList<LinkedHashMap<Link,Double>> linkMappings =  this.linkMap.get(sfcLink);
											 if (linkMappings==null){
												 linkMappings = new ArrayList<LinkedHashMap<Link,Double>>();
											 }
											 linkMappings.add(tmp);
											 this.linkMap.put(sfcLink, linkMappings);	
										}
									}		
								}
						//	}
					}
				}
		  
	  }
	  
	  

	  
	  private boolean updateSubLink(Link link, double cap){
		  ArrayList<Link> subLinks = this.updatedSubLinks.getLinks(this.updatedSubLinks.getGraph());
		  for (Link x:subLinks ){
			  if (x.getId()==link.getId()){
				  double newCap = x.getBandwidth()-cap;
				  Pair<Node> eps = this.updatedSubLinks.getGraph().getEndpoints(x);
				  this.updatedSubLinks.getGraph().findEdge(eps.getFirst(), eps.getSecond()).setBandwidth(newCap);
				  return true;
			  }
		  }
		  return false;
		  
	  }
	  
	  public HashMap<Link, ArrayList<LinkedHashMap<Link, Double>>> getLinkMapping(){
		  return this.linkMap;
	  }
	  
	  public Link getLink(int a,int  b, ArrayList<Link> links, Graph<Node,Link> graph){ 
			Link l = null;
			for(Link link: links){
			    Pair<Node> currentNodes = graph.getEndpoints(link);
			   // System.out.println("currentNodes " +currentNodes );
			    if (currentNodes.getFirst().getId()==a){
			    	if(currentNodes.getSecond().getId()==b){
			    		return link;
			    	}
			    }
			}
			return l;
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




	public Substrate getUpdatedSub() {
		// TODO Auto-generated method stub
		return null;
	}
}
