package model.components;

import java.util.ArrayList;
import java.util.List;

/**
 * SubstrateRouter Class. Subclass of Node.
 */
public class SubstrateRRH extends Node {

	public static final int MAX_LOGICAL_RRH = 1;
	
//	private int maxLogicalIfaces;
	
	private int logicalInstances;
	
	private int availablelogicalInstances;
	
	private int cpu;
	
	private List<RequestRRH> virtualRRH;
	
	public SubstrateRRH(int id) {
		super(id);
		name = "substrateRRH"+id;
		virtualRRH = new ArrayList<RequestRRH>();
	}
	
	public int getLogicalInstances() {
		return logicalInstances;
	}

	public void setLogicalInstances(int logicalInstances) {
		this.logicalInstances = logicalInstances;
	}
	
	public List<RequestRRH> getVirtualRouters() {
		return virtualRRH;
	}

	public int getAvailableCpu() {
		return this.cpu;
	}
	
	public void setCPU(int cpu) {
		this.cpu = cpu;
	}
	
	public void setAvailableCpu(int cpu) {
		this.cpu = cpu;
	}
	
	public void setVirtualRouters(List<RequestRRH> virtualRRH) {
		this.virtualRRH = virtualRRH;
	}
	
	public void addVirtualRRH(RequestRRH rr) {
		this.virtualRRH.add(rr);
	}
	
	public int getAvailableLogicalInstances() {
		return availablelogicalInstances;
	}
	
	public void setAvailableLogicalInstances(int availablelogicalInstances){
		this.availablelogicalInstances=availablelogicalInstances;
	}


	public Object getCopy() {
		SubstrateRRH r = new SubstrateRRH(this.getId());
		r.name = this.name;
		r.cpu = this.cpu;
		r.coordinates= this.coordinates;
		for (RequestRRH rr : this.virtualRRH)
			r.virtualRRH.add((RequestRRH) rr.getCopy());
		return r;
	}
	
}
