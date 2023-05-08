package model.components;



/**
 * NF Class. Subclass of Node.
 */
public class NF extends Node {
	
	private Server server;
	private double in_tr_dl = 0;
	private double out_tr_dl= 0;
	private double in_tr_ul = 0;
	private double out_tr_ul= 0;
	private int instances = 1;
	public NF(int id, String nodeType) {
		super(id);
		name = nodeType+id;
		this.server = null;
	}
	
	public NF(int id, String nodeType, int instances) {
		super(id);
		name = nodeType+id;
		this.server = null;
		this.instances=instances;
	}
	
	public int  getInstances() {
		return this.instances;
	}


	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}
	
	public void setInTrafficUL(double tr) {
		this.in_tr_ul = tr;
	}
	public void setOutTrafficUL(double tr) {
		this.out_tr_ul = tr;
	}
	
	public double getInTrafficUL( ) {
		return this.in_tr_ul;
	}

	public double getOutTrafficUL( ) {
		return this.out_tr_ul;
	}
	
	public void setInTrafficDL(double tr) {
		this.in_tr_dl = tr;
	}
	public void setOutTrafficDL(double tr) {
		this.out_tr_dl = tr;
	}
	
	public double getInTrafficDL( ) {
		return this.in_tr_dl;
	}

	public double getOutTrafficDL( ) {
		return this.out_tr_dl;
	}

	public Object getCopy() {
		NF vm = new NF(this.getId(),this.name);
		vm.name = this.name;
		vm.cpu = this.cpu;
		vm.memory = this.memory;
		vm.vlans = this.vlans;
		vm.os = this.os;
		vm.veType=this.veType;
		vm.netStack = this.netStack;
		vm.location = this.location;
		vm.coordinates= this.coordinates;
		
		return vm;
	}
	
}
