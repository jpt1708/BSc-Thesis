package model;

import java.util.ArrayList;

import simenv.SimulatorConstants;
import cern.jet.random.Uniform;


public class PeriodicTrafficFG extends TrafficFG {

	@Override
    public void generateTraffic(int eps, int req){ //initial
		this.eps=eps;
		//generate traffic from chain endpoints

		epTraffic= new ArrayList<Double> ();


		for (int i=0;i<eps;i++){
			double periodicScale = ((Math.sin((req * 2 * Math.PI) / 10000) + 1) / 2) * 0.8 + 0.2;
			if (periodicScale > 1) {
				System.out.println("periodicScale > 1");
			}

			Uniform tempUni= new Uniform(50, SimulatorConstants.UPPER_TRAFFIC_FG, engine);
			double tempMaxTraffic = Math.floor(tempUni.nextDouble() * 1000 * periodicScale);

			if (tempMaxTraffic < SimulatorConstants.LOWER_TRAFFIC_FG * 1000) {
				System.out.println("tempMaxTraffic < LOWER_TRAFFIC_FG");
			}

			this.epTraffic.add(tempMaxTraffic); //Kbps
		}

		this.maxTraffic = this.epTraffic.get(0);


		//System.out.println("EP: " +this.epTraffic );
	}

}
