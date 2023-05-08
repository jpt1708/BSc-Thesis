package model.components;



/**
 * RequestRouter Class. Subclass of Node.
 */
public class RequestRRH extends Node {
	
	private SubstrateRRH physicalRRH;
	private int instances = 1;
	
	public RequestRRH(int id) {
		super(id);
		name = "requestRRH"+id;
		physicalRRH = null;
	}

	public SubstrateRRH getPhysicalRRH() {
		return physicalRRH;
	}

	public void setPhysicalRRH(SubstrateRRH physicalRRH) {
		this.physicalRRH = physicalRRH;
	}
	public int  getInstances() {
		return this.instances;
	}
	
	public void  setInstances(int inst) {
		this.instances=inst;
	}

	
	public Object getCopy() {
		RequestRRH r = new RequestRRH(this.getId());
		r.name = this.name;
		r.cpu = this.cpu;
		r.coordinates = this.coordinates;
		r.instances=this.instances;
		return r;
	}
	
}
