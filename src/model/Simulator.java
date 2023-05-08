package model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class controls all resources added on the simulator:
 * requests, substrates and algorithms. It also has the simulation
 * running (if exists).
 */
public class Simulator {

	private List<Substrate> substrates;
	private List<Request> requests;
	private List<Algorithm> algorithms;
	private Simulation simulation = null;
	
	public Simulator() {
		substrates = new ArrayList<Substrate>();
		requests =  new ArrayList<Request>();
		algorithms = new ArrayList<Algorithm>();
		
	}
	
	public List<Substrate> getSubstrates() {
		return substrates;
	}
	
	public void setSubstrates(List<Substrate> substrates) {
		this.substrates = substrates;
	}
	
	/** returns substrate with id id **/
	public Substrate getSubstrate(String id) {
		for (Substrate s : this.substrates)
			if (s.getId().equals(id)) return s;
		return null;
	}
	
	/** Add a list of substrates to the simulator **/
	public void addSubstrates(List<Substrate> substrates) {
		for (Substrate substrate : substrates)
			this.substrates.add(substrate);
	}
	
	public void removeSubstrates(List<Substrate> selectedSubstrates) {
		for (Substrate subs : selectedSubstrates)
			this.substrates.remove(subs);
	}
	
	public boolean existSubstrateId(String id) {
		for (Substrate subs : this.substrates)
			if (subs.getId().equals(id))
				return true;
		return false;
	}
	public List<Request> getRequests() {
		return requests;
	}
	public void setRequests(List<Request> requests) {
		this.requests = requests;
	}
	public void addRequests(List<Request> requests) {
		for (Request request : requests)
			this.requests.add(request);
	}
	public void removeRequests(List<Request> selectedRequests) {
		for (Request req : selectedRequests)
			this.requests.remove(req);
	}
	public boolean existRequestId(String id) {
		for (Request req : this.requests)
			if (req.getId().equals(id))
				return true;
		return false;
	}
	public List<Algorithm> getAlgorithms() {
		return algorithms;
	}
	public void setAlgorithms(List<Algorithm> algorithms) {
		this.algorithms = algorithms;
	}
	/** Add a list of algorithms to the simulator **/
	public void addAlgorithms(List<Algorithm> algorithms) {
		for (Algorithm algorithm : algorithms)
			this.algorithms.add(algorithm);
	}
	public void removeAlgorithms(List<Algorithm> algorithms) {
		for (Algorithm alg : algorithms)
			this.algorithms.remove(alg);
	}
	public boolean existAlgorithmId(String id) {
		for (Algorithm alg : this.algorithms)
			if (alg.getId().equals(id))
				return true;
		return false;
	}
	public Simulation getSimulation() {
		return simulation;
	}

	public void setSimulation(Simulation simulation) {
		this.simulation = simulation;
	}

	/** returns request with id id **/
	public Request getRequest(String id) {
		for (Request p : this.requests)
			if (p.getId().equals(id)) return p;
		return null;
	}
	/** returns algorithm with id id **/
	public Algorithm getAlgorithm(String id) {
		for (Algorithm a : this.algorithms)
			if (a.getId().equals(id)) return a;
		return null;
	}
	
}
