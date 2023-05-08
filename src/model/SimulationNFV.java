package model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.uci.ics.jung.graph.util.Pair;
import model.components.Link;
import model.components.NF;
import model.components.Node;
import model.components.SubstrateLink;
import model.components.SubstrateSwitch;
import simenv.SimulatorConstants;

/**
 * This class is the instantiation of a simulation. It is defined by a set
 * of requests, a substrate and an algorithm
 */
public class SimulationNFV {
	
	private String id;
	private Substrate InPs;
	private List<Substrate> substrates;
	private List<Request> requests;
	private AlgorithmNF algorithm;
	private List<Node> NFs;
	
	/** Creates a new instance of Substrate */
        public SimulationNFV(Substrate InPs, List<Substrate> substrates, List<Request> requests,
        		AlgorithmNF algorithm2, List<Node> NFs) {
    	this.substrates = substrates;
    	this.requests = requests;
    	this.algorithm = algorithm2;
    	this.InPs = InPs;
    	this.NFs=NFs;
    }
        
        /** Creates a new instance of Substrate */
   public SimulationNFV(Substrate InPs, List<Substrate> substrates, List<Request> requests,
        		AlgorithmNF algorithm2) {
    	this.substrates = substrates;
    	this.requests = requests;
    	this.algorithm = algorithm2;
    	this.InPs = InPs;
    	
    	
/*    	System.out.println("id: " + substrates.get(0));
		try {
	        System.in.read();
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }*/
    	
    	
    }
    
    public Substrate getInPs(){
    	return this.InPs;
    }
    
    public List<Node> getNFs(){
    	return this.NFs;
    }
    
    public void setInPs(Substrate InPs){
    	this.InPs=InPs;
    }

	public List<Substrate> getSubstrates(){
		return this.substrates;
	}

	public void setSubstrates(List<Substrate> substrates){
		this.substrates=substrates;
	}
	
	public void setNFs(List<Node> NFs) {
		this.NFs = NFs;
	}
	
	public List<Request> getRequests() {
		return requests;
	}

	public void setRequests(List<Request> requests) {
		this.requests = requests;
	}
	
	public void addRequests(List<Request> selectedRequests) {
		for (Request req : selectedRequests) {
			this.requests.add(req);
			req.setState(SimulatorConstants.STATUS_READY);
		}
	}
	
	public void removeRequests(List<Request> selectedRequests) {
		for (Request req : selectedRequests) {
			this.requests.remove(req);
			req.setState(SimulatorConstants.STATUS_AVAILABLE);
		}
	}
	
	public void changeSubstrate(List<Substrate> newSubstrates) {
		for (Substrate sub: substrates){
			sub.setState(SimulatorConstants.STATUS_AVAILABLE);
		}
		this.substrates = newSubstrates;
		for (Substrate sub: substrates){
			sub.setState(SimulatorConstants.STATUS_READY);
		}
	}
	
	public AlgorithmNF getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(AlgorithmNF algorithm) {
		this.algorithm = algorithm;
	}
	
	public void changeAlgorithm(AlgorithmNF newAlgorithm) {
		this.algorithm.setState(SimulatorConstants.STATUS_AVAILABLE);
		this.algorithm = newAlgorithm;
		newAlgorithm.setState(SimulatorConstants.STATUS_READY);
		
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getEndDate() {
		int end = 0;
		for (Request req : requests)
			if (req.getEndDate()>end)
				end = req.getEndDate();
		return end;
	}

	public List<Request> getStartingRequests(int time) {
		List<Request> startingRequests = new ArrayList<Request>();
		for (Request req : requests)
			if (req.getStartDate()==time)
				startingRequests.add(req);
		return startingRequests;
	}
	
	public List<Request> getEndingRequests(int time) {
		List<Request> endingRequests = new ArrayList<Request>();
		for (Request req : requests){
			if (req.getEndDate()==time){
				endingRequests.add(req);
			}
		}
		return endingRequests;
	}

	public List<Request> getUpdatedRequests(int time) {
		List<Request> updatedRequests = new ArrayList<Request>();
		for (Request req : requests){
			if (req.getTS().contains((Integer)time)){
				//System.out.println("To update request " + req.id + " at time " + time + " " +req.getStartDate());
				updatedRequests.add(req);
				//System.out.println("To update request " + req.id + " at time " + time);
			}
		}
		return updatedRequests;
	}

	/** Release resources of the requests from the substrate **/
	public void releaseRequests(List<Request> endingRequests,int i) {

		for (Request req : endingRequests){
			//req.print();
					for (Substrate substrate: substrates){
						//if (subVN.getInP()==substrate.getId()){
							if (!(req.getRMapNF().isDenied())){
								req.getRMapNF().releaseNodes(substrate);
								req.getRMapNF().releaseLinks(substrate);
							//	req.getRMapNF().releaseTCAM(substrate);
								if (i==req.getEndDate())
									System.out.println("//////////Released///////// "+req.getId()+ " at time "+i +" with endTime " +req.getEndDate());
								else 
									System.out.println("//////////Released- Updated///////// "+req.getId()+ " at time "+i +" with endTime " +req.getEndDate());

								//substrate.print();
							}
							//System.out.println("id: " + substrates.get(0));
/*							try {
						        System.in.read();
						    } catch (IOException e) {
						        // TODO Auto-generated catch block
						        e.printStackTrace();
						    }*/
					    	
					}
				}
		}
	
	/** Update resources of the requests from the substrate **/
	public void updateRequests(List<Request> updatedRequests,int i) {
		for (Request req : updatedRequests){
					for (Substrate substrate: substrates){
						//substrate.print();
							if (!req.getRMapNF().isDenied()){
									req.getRMapNF().reserveNodes(substrate);
									req.getRMapNF().reserveLinks(substrate);
									System.out.println("//////////Updated///////// "+req.getId()+ " at time "+i);
									System.out.println();
							}
							//substrate.print();
					}
			}
	}
	
	
	public void initSubstrate(Substrate sub) {
		ArrayList<Node> tor_switch = new ArrayList<Node>();
		ArrayList<Node> dc_switch = new ArrayList<Node>();
		for (Link link : sub.getGraph().getEdges()) {
			Pair<Node> eps  =  sub.getGraph().getEndpoints(link);
			Node src = eps.getFirst();
			Node dst = eps.getSecond();
			 if (link.getLinkType().equalsIgnoreCase("torlink")) {
				if (src.getType().equalsIgnoreCase("switch")){
					if (!(tor_switch.contains(src))){
						src.setTCAM(src.getTCAM()-16);
						tor_switch.add(src);
					}
				}else {
					if (dst.getType().equalsIgnoreCase("switch")){
						if (!(tor_switch.contains(dst))){
							dst.setTCAM(dst.getTCAM()-16);
							tor_switch.add(dst);
						}
					}
				}
			}
			else
			if (link.getLinkType().equalsIgnoreCase("interracklink")) {
				if (!(((SubstrateSwitch)src).getTOR_switch())){
					if (!(dc_switch.contains(src))){
						src.setTCAM(src.getTCAM()-16);
						dc_switch.add(src);
					}	
				}
				if (!(((SubstrateSwitch)dst).getTOR_switch())){
					if (!(dc_switch.contains(dst))){
						dst.setTCAM(dst.getTCAM()-16);
						dc_switch.add(dst);
					}
					
				}
			}
			
		}//link iter
		
	}
}
