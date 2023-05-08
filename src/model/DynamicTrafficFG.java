package model;

import java.util.ArrayList;

import cern.jet.random.Uniform;

public class DynamicTrafficFG extends TrafficFG {
	
	Uniform uni1= new Uniform(50,120,engine);

	@Override
    public void generateTraffic(int eps,int req){ //initial
		this.eps=eps;
		epTraffic= new ArrayList<Double>();

		for (int i=0;i<eps;i++){
			if (req<5000)
				this.maxTraffic=Math.floor(uni.nextDouble()*1000);
			else 
				this.maxTraffic=Math.floor(uni1.nextDouble()*1000);

			this.epTraffic.add(maxTraffic); //Kbps
		}
		//System.out.println("EP: " +this.epTraffic );
	}
	
}
