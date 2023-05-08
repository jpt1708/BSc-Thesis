package model;

import java.io.Serializable;
import java.util.Random;

import model.components.Link;
import model.components.SubstrateLink;
import simenv.SimulatorConstants;

import org.apache.commons.collections15.Factory;

import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;

/**
 * This class is a factory of SubstrateLink. It generates the elements
 * with random parameters. Ranges for randomness can be found on 
 * SimulatorConstants class
 */
public class SubstrateLinkFactory implements Factory<Link>, Serializable {
	
	private int linkCount = 0;
	Random rand = new Random();
	int  n = rand.nextInt(400) + 100;
	MersenneTwister generator = new MersenneTwister(n);
	Uniform myUniformDist = new Uniform(generator);
	
	public SubstrateLinkFactory() {
		super();
		linkCount = 0;
	}
	
	public SubstrateLink create() {
		int bandwidth = SimulatorConstants.INTER_DC_BW;
		SubstrateLink link = new SubstrateLink(linkCount,bandwidth);
		link.setAvailableBandwidth(bandwidth);
		link.setNominalBandwidth(bandwidth);
		linkCount++;
		return link;
	}
	
	public SubstrateLink create(String linkType) {
		// Random bandwidth
		int bandwidth = SimulatorConstants.MIN_BW 
				+ (int)(Math.random()*((SimulatorConstants.MAX_BW 
				- SimulatorConstants.MIN_BW) + 1));
		int bw_nominal = 0;
		
		if (linkType.equalsIgnoreCase("torlink")) {
			double util = myUniformDist.nextDoubleFromTo(0.4,0.7);
		//double util=1;
		bandwidth = (int) (util*SimulatorConstants.TOR_BW);
		bw_nominal = (int) SimulatorConstants.TOR_BW;
		}
		else if (linkType.equalsIgnoreCase("interracklink")) {
		double util = myUniformDist.nextDoubleFromTo(0.4,0.7);
		//double util =1;
		bandwidth = (int) (util* SimulatorConstants.INTER_RACK_BW);
		bw_nominal = (int)  SimulatorConstants.INTER_RACK_BW;
		}
		else if (linkType.equalsIgnoreCase("interdclink")) {
			bandwidth = SimulatorConstants.INTER_DC_BW;
			bw_nominal = (int)   SimulatorConstants.INTER_DC_BW;
		}else if (linkType.equalsIgnoreCase("dummy")) {
			bandwidth = Integer.MAX_VALUE;
		}
		SubstrateLink link = new SubstrateLink(linkCount,bandwidth);
		link.setAvailableBandwidth(bandwidth);
		link.setNominalBandwidth(bw_nominal);
		link.setLinkType(linkType);
		
		linkCount++;
		return link;
	}
	  
	public Object getCopy() {
		SubstrateLinkFactory f = new SubstrateLinkFactory();
		f.linkCount = this.linkCount;
		return f;
	}

	public int getLinkCount() {
		return linkCount;
	}

	public void setLinkCount(int linkCount) {
		this.linkCount = linkCount;
	}
	
	
	
}