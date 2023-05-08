package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections15.MultiMap;

import tools.LinkComparator;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;
import model.components.Link;
import model.components.Node;

public class LinkMapping {
	  private ArrayList<LinkMapSFC> linkMapSFCs = new ArrayList<LinkMapSFC> ();
	  private Substrate updatedSub;
	  //substrate Link  - sfc/cap
	  
	  public void linkMapping1 (Substrate sub, ArrayList<Request> reqs, double[][][][][] fVar  ){

		  int numSFCs=reqs.size();
		  LinkMapSFC lmap =  new LinkMapSFC();
		   for (int f=0;f<numSFCs;f++){
				lmap.linkMapping( sub, reqs, fVar, f);
				this.linkMapSFCs.add(lmap);  						
		   }
		   
		   this.updatedSub=lmap.getUpdatedSub();
				
		  
	  }
	  

	  public  ArrayList<LinkMapSFC>  getLinkMappingAll(){
		  return this.linkMapSFCs;
	  }
	  
	  public Substrate getUpdatedSubstrate(){
		  return this.updatedSub;
	  }


}


