package model.components;

import java.util.ArrayList;
import java.util.List;

/**
 * SubstrateLink Class. Subclass of Link.
 */
public class SubstrateLink extends Link {
	
	private List<RequestLink> virtualLinks;
	private double availablebw;
	private double nominal_bw;
	
	public SubstrateLink(int id, double bandwidth) {
		super(id, bandwidth);
		name = "substrateLink"+id;
		virtualLinks = new ArrayList<RequestLink>();
	}

	public List<RequestLink> getVirtualLinks() {
		return virtualLinks;
	}
	
	public void setVirtualLinks(List<RequestLink> virtualLinks) {
		this.virtualLinks = virtualLinks;
	}

	//The initial capacity of a substrate link
	public double getAvailableBandwidth(){
		return this.availablebw;
	}

	public double getNominalBandwidth(){
		return this.nominal_bw;
	}
	
	public void setAvailableBandwidth(int availablebw){
		this.availablebw = availablebw;
	}
	
	public void setNominalBandwidth(int nominal_bw){
		this.nominal_bw = nominal_bw;
	}
		
	public Object getCopy() {
		SubstrateLink l = new SubstrateLink(this.getId(),this.getBandwidth());
		l.name = this.name;
		l.delay = this.delay;
		l.availablebw=this.availablebw;
		l.bandwidth = this.bandwidth;
		return l;
	}
	
}
