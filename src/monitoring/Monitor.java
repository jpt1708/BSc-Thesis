package monitoring;

import java.util.ArrayList;
import java.util.Random;

import cern.jet.random.Exponential;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;

public class Monitor {
	ArrayList<Integer> m_ts = new ArrayList<Integer>();
	String requestID = "empty";
	Double penalty = 0.0;
	
	public void generateMInstances(int simTime) {
		
		Random rand = new Random();
		int  n = rand.nextInt(400) + 100;
		MersenneTwister generator = new MersenneTwister(n);
		Exponential exp_mon = new Exponential(0.1,generator);
		int m_time=0;
		while (m_time< simTime) {
			int m_interval = exp_mon.nextInt();
			m_time+=m_interval;
			System.out.println("interval: " +m_interval + " time " +m_time);
			if(!m_ts.contains(m_time)){			    
				m_ts.add(m_time);
			 }
			
		}
	}
	
	public ArrayList<Integer>  getTS(){
		return m_ts;
	}
	
	public void setRequestID(String rid) {
		requestID=rid;
	}
	
	public String getRequestID() {
		return requestID;
	}
	
	public void setPenalty(Double value) {
		penalty=value;
	}
	
	public Double getPernalty() {
		return penalty;
	}

}
