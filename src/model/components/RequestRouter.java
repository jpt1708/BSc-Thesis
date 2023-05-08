package model.components;



/**
 * RequestRouter Class. Subclass of Node.
 */
public class RequestRouter extends Node {
	
	private SubstrateRouter physicalRouter;
	private int instances = 1;
	
	public RequestRouter(int id) {
		super(id);
		name = "requestRouter"+id;
		physicalRouter = null;
	}

	public int getInstances() {
		return this.instances;
	}

	public void setInstances(int inst) {
		this.instances=inst;
	}

	public SubstrateRouter getPhysicalRouter() {
		return physicalRouter;
	}

	public void setPhysicalRouter(SubstrateRouter physicalRouter) {
		this.physicalRouter = physicalRouter;
	}
	
	public Object getCopy() {
		RequestRouter r = new RequestRouter(this.getId());
		r.name = this.name;
		r.cpu = this.cpu;
		r.memory = this.memory;
		r.vlans = this.vlans;
		r.os = this.os;
		r.veType=this.veType;
		r.netStack = this.netStack;
		r.location = this.location;
		r.instances=this.instances;
//		for (Interface i : this.interfaces)
//			r.interfaces.add((Interface) i.getCopy());
//		if (this.physicalRouter!=null)
//			r.physicalRouter = (SubstrateRouter) this.physicalRouter.getCopy();
		return r;
	}
	
}
