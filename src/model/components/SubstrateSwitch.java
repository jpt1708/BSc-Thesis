package model.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import simenv.SimulatorConstants;

/**
 * SubstrateSwitch Class. Subclass of Node.
 */
public class SubstrateSwitch extends Node {

	private List<RequestSwitch> virtualSwitches;
	
	private int maxLogicalIfaces;
	private boolean tor_switch=false;
	public SubstrateSwitch(int id) {
		super(id);
		name = "substrateSwitch"+id;
		setMaxLogicalIfaces(SimulatorConstants.MAX_LOGICAl_IFACES_SWITCH);
		virtualSwitches = new ArrayList<RequestSwitch>();
	}

	public List<RequestSwitch> getVirtualSwitches() {
		return virtualSwitches;
	}

	public void setVirtualSwitches(List<RequestSwitch> virtualSwitches) {
		this.virtualSwitches = virtualSwitches;
	}
	
	public void addVirtualSwitch(RequestSwitch rs) {
		this.virtualSwitches.add(rs);
	}

	public int getAvailableCpu() {
		int returnValue = this.getCpu();
		for (RequestSwitch s : virtualSwitches)
			returnValue-=s.getCpu();
		return returnValue;
	}
	
	public int getAvailableMemory() {
		int returnValue = this.getMemory();
		for (RequestSwitch s : virtualSwitches)
			returnValue-=s.getMemory();
		return returnValue;
	}
	
	public int getAvailableVlans(Collection<Link> links) {
		int returnValue = this.vlans;
		for (RequestSwitch s : virtualSwitches)
			returnValue-=s.getVlans();
		for (Link l : links)
			returnValue -= ((SubstrateLink) l).getVirtualLinks().size();
		return returnValue;
	}

	public int getMaxLogicalIfaces() {
		return maxLogicalIfaces;
	}

	public void setMaxLogicalIfaces(int maxLogicalIfaces) {
		this.maxLogicalIfaces = maxLogicalIfaces;
	}
	
	public void setTCAM(int tcam) {
		this.tcam = tcam;
	}
	
	public int getTCAM(){
		return this.tcam;
	}

	public Object getCopy() {
		SubstrateSwitch s = new SubstrateSwitch(this.getId());
		s.name = this.name;
		s.cpu = this.cpu;
		s.memory = this.memory;
		s.tcam =this.tcam;
		s.vlans = this.vlans;
		s.os = this.os;
		s.netStack = this.netStack;
		s.location = this.location;
		s.maxLogicalIfaces = this.maxLogicalIfaces;
		s.coordinates= this.coordinates;
//		for (Interface i : this.interfaces)
//			s.interfaces.add((Interface) i.getCopy());
		for (RequestSwitch rs : this.virtualSwitches)
			s.virtualSwitches.add((RequestSwitch) rs.getCopy());
		return s;
	}
	
	public void setTOR_switch(boolean tor) {
		this.tor_switch=tor;
	}
	public boolean getTOR_switch() {
		return this.tor_switch;
	}
}
