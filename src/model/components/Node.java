package model.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;

import org.bytedeco.javacpp.mkldnn.primitive;

import akka.japi.Pair;

/**
 * Node Class. Superclass of RequestSwitch, RequestRouter, VirtualMachine,
 * SubstrateSwitch, SubstrateRouter and Server
 */
public class Node implements Serializable {
	
	public enum NetStack {TCPIP, UDPIP, IPATM, IPEthernet}
	public enum Location {Europe, NorthAmerica, 
		SouthAmerica, Asia, Australia, Africa, Portugal, Spain, Ireland,
		Switzerland, Italy, Greece, Hungary, Germany, Czech_Republic,
		Poland, Denmark, Sweden}
	public enum LinkType {VLAN, SONET, EIGHT0211}
	public enum Connectivity {Broadcast, PointtoPoint, PointtoMultiPoint, Peering}
	
	protected Coords coordinates;
	protected Icon icon;
	protected Icon smallIcon;
	protected Icon pickedIcon;
	protected Icon yellowIcon;
	protected Icon redIcon;
	
	protected int id;
	/** name for visualization **/
	protected String name;
	protected int availablecpu;
	protected int allocated_cpu;
	protected int cpu;
	protected int nominal_cpu;
	protected int worst_cpu;
	protected int stress;
	protected int memory;
	protected int tcam;
	protected int available_tcam;
	protected int availablememory;
	protected double between;
	/** Number of VLANs **/
	protected int vlans;
	/** Operating System **/
	protected String os;
	/** Network Stack **/
	protected NetStack netStack;
	/** Geographical location **/
	protected Location location;
	/** Interfaces of the node **/
	protected List<Interface> interfaces;
	/** Link Type **/
	protected LinkType linkType;
	/** Virtual Environment Type **/
	protected String veType;
	/** Virtual Node Type **/
	protected String nType;
	/** Connectivity Type **/
	protected Connectivity connectivity;
	protected int instances;
	
	public Node(int id) {
		this.id = id;
/*		*//** Default SO **//*
		so = SO.Linux;
		*//** Default Network Stack **//*
		netStack = NetStack.TCPIP;
		*//** Default Location **//*
		location = Location.Europe;
*/		interfaces = new ArrayList<Interface>();
		
		
		int stack=(int) (Math.random()*4);
		
		switch (stack){
			case 0:
				netStack=NetStack.IPATM;
				break;
			case 1:
				netStack=NetStack.IPEthernet;
				break;
			case 2:
				netStack=NetStack.TCPIP;
				break;
			case 3:
				netStack=NetStack.UDPIP;
				break;
		}
		
		location = Location.Europe;
		
		int ltype=(int) (Math.random()*3);
		
		switch (ltype){
			case 0: 
				linkType=LinkType.VLAN;
				break;
			case 1:
				linkType=LinkType.SONET;
				break;
			case 2:
				linkType=LinkType.EIGHT0211;
				break;
		}
		
		
		int con=(int) (Math.random()*4);
		
		switch(con){
			case 0:
				connectivity=Connectivity.Broadcast;
				break;
			case 1:
				connectivity=Connectivity.PointtoPoint;
				break;
			case 2:
				connectivity=Connectivity.PointtoMultiPoint;
				break;
			case 3:
				connectivity=Connectivity.Peering;
				break;
		}
		

	}
	
	public Icon getIcon() {
		return icon;
	}
	
	public Coords getCoords(){
		return this.coordinates;
	}
	
	public void setCoords(Coords c){
		this.coordinates=c;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}
	
	public Icon getSmallIcon() {
		return smallIcon;
	}

	public void setSmallIcon(Icon smallIcon) {
		this.smallIcon = smallIcon;
	}

	public Icon getPickedIcon() {
		return pickedIcon;
	}

	public void setPickedIcon(Icon pickedIcon) {
		this.pickedIcon = pickedIcon;
	}
	
	
	public Icon getYellowIcon() {
		return yellowIcon;
	}

	public void setYellowIcon(Icon yellowIcon) {
		this.yellowIcon = yellowIcon;
	}

	public Icon getRedIcon() {
		return redIcon;
	}

	public void setRedIcon(Icon redIcon) {
		this.redIcon = redIcon;
	}

	public String getOS() {
		return os;
	}

	public void setOS(String os) {
		this.os = os;
	}

	public NetStack getNetStack() {
		return netStack;
	}

	public void setNetStack(NetStack netStack) {
		this.netStack = netStack;
	}

	public Location getLocation() {
		return location;
	}
	

	public void setLocation(Location location) {
		this.location = location;
	}

	public List<Interface> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(List<Interface> interfaces) {
		this.interfaces = interfaces;
	}

	public void addInterface(Interface iface) {
		this.interfaces.add(iface);
	}
	
	public void removeInterface(Interface iface) {
		this.interfaces.remove(iface);
	}
	
	public LinkType getLinkType() {
		return linkType;
	}

	public void setLinkType(LinkType linkType) {
		this.linkType = linkType;
	}
	
	public String getVEType() {
		return veType;
	}

	public void setVEType(String veType) {
		this.veType = veType;
	}
	
	public Connectivity getConnectivity() {
		return connectivity;
	}

	public void setConnectivity(Connectivity connectivity) {
		this.connectivity = connectivity;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCpu() {
		return cpu;
	}
	
	public void setCpu(int cpu) {
		this.cpu = cpu;
	}
	
	public void setNominalCpu(int ncpu) {
		this.nominal_cpu=ncpu;
		
	}
	
	public int getNominalCpu() {
		return this.nominal_cpu;
		
	}
	
	public int getStress(){
		return stress;
	}
	
	public void setStress(int stress){
		this.stress=stress;
	}
	
	public double getBetweenness(){
		return between;
	}
	
	public void setBetweenness(double between){
		this.between=between;
	}

	public int getVlans() {
		return vlans;
	}

	public void setVlans(int vlans) {
		this.vlans = vlans;
	}
		
	
	public int getMemory() {
		return memory;
	}

	public void setMemory(int memory) {
		this.memory = memory;
	}
	
	public void setTCAM(int tcam) {
		this.tcam = tcam;
	}
	
	public void setAvailableTCAM(int tcam) {
		this.available_tcam = tcam;
	}
	public int getTCAM(){
		return this.tcam;
	}
	
	public int getAvailableTCAM(){
		return this.available_tcam;
	}

	/** set the initial capacity of the cpu **/
	public int getAvailableAllocatedCpu() {
		return allocated_cpu;
	}

	// List<Pair<Double, Double>> allocated_cpus = new ArrayList<>();
	
	public void setAvailableAllocatedCpu(int new_allocated_cpu){
		this.allocated_cpu=new_allocated_cpu;
		// this.allocated_cpus.add(new Pair(new_allocated_cpu, Thread.currentThread().getId()));

	}

	List<Node> nfs = new ArrayList<>();


	public void adNF(Node nf){
		this.nfs.add(nf);
	}

	/** set the initial capacity of the cpu **/
	public int getAvailableCpu() {
		return availablecpu;
	}
	
	// List<Pair<Double, Double>> availablecpus = new ArrayList<>();


	public void setAvailableCpu(int new_availablecpu){
		if ((new_availablecpu < this.allocated_cpu && this instanceof Server) || (this.availablecpu != 0 && new_availablecpu > this.allocated_cpu && this instanceof NF)) {
		// if ((new_availablecpu < this.allocated_cpu && this instanceof Server)) {
			System.out.println("invalid setAvailableCpu update");
		}



		this.availablecpu=new_availablecpu;
		// this.availablecpus.add(new Pair(new_availablecpu, Thread.currentThread().getId()));


	}
	
	/** set the initial capacity of the cpu **/
	public int getAvailableMemory() {
		return availablememory;
	}
	
	public void setAvailableMemory(int availablememory){
		this.availablememory = availablememory;
	}

	/** empty function rewrite on subclasses 
	 * @param links **/
	public int getAvailableVlans(Collection<Link> links) {
		return 0;
	}
	
	public String toString() {
		return Integer.toString(id);
	}

	public Interface getInterfaceByName(String ifaceName) {
		for (Interface iface : interfaces)
			if (iface.getName().equals(ifaceName))
				return iface;
		return null;
	}
	
	/** dummy getCopy. Subclass getCopy will be called **/
	public Object getCopy() {
		return null;
	}
	
	public String getType(){
		  if (this instanceof SubstrateRouter) return "Router";
		  else if (this instanceof RequestRouter) return "Router";
		  else if (this instanceof SubstrateSwitch) return "Switch";
		  else if (this instanceof RequestSwitch) return "Switch";
		  else if (this instanceof Server) return "Server";
		  else if (this instanceof VirtualMachine) return "Server";
		  else if (this instanceof RequestRRH) return "RRH";
		  else if (this instanceof SubstrateRRH) return "RRH";
		  else if (this instanceof NF) return "NF";
		  return null;
		 }

	public int getInstances() {
		// TODO Auto-generated method stub
		return this.instances;
	}
	
	
	public void setInstances(int num){
	 this.instances = num;
	}

	
}
