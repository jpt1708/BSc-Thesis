package ML;

import org.deeplearning4j.rl4j.space.Encodable;

import cern.colt.Arrays;
import org.nd4j.linalg.api.ndarray.INDArray;

//import org.apache.commons.lang3.ArrayUtils;

public class ObservedState implements Encodable  {
	private int resCapacity;
	private int resLife;
	private boolean over_subscription =false;
	private boolean long_requests=false;
	private double nom_cap;
	private double avg_rlf=10000;
	
	ObservedState(double[] rcc, double[] rlf_avg, double nom_cap, double avg_rlf){
		this.nom_cap=nom_cap;
		this.avg_rlf=avg_rlf;
		this.resCapacity=findStateCap(rcc);
		this.resLife=findStateLife(rlf_avg);
		System.out.println("Initial ObservedState: " + this.resCapacity +":" +this.resLife );

	}
	
	private int findStateCap(double[] rcc){
		int num_servers =0;
		System.out.println("in ObservedState rcc: " + Arrays.toString(rcc));
		System.out.println("Function findStateCap");
		for (int i=0;i<rcc.length;i++) {
			if (rcc[i]/(Double.MIN_VALUE+nom_cap)<0.1)
				++num_servers;
		}
		if (num_servers>rcc.length/2){
			over_subscription=true;
		}
	   	return num_servers;
    }

	private int findStateLife(double[] rlf){
		int num_servers =0;
		for (int i=0;i<rlf.length;i++) {
			if (rlf[i]>avg_rlf) {
				++num_servers;
				}
		}
		if (num_servers>rlf.length/2){
			long_requests=true;
		}
		return num_servers;
    }
	
	void print() {
		System.out.println(this.resCapacity +" " + this.resLife);
	}
	
    public  double[] toArray() {
    	double[] state = new double[] {this.resCapacity,this.resLife}; 
    	System.out.println("state: :" + Arrays.toString(state));  
        return state;
        //= ArrayUtils.addAll
    }

//	@Override
//	public boolean isSkipped() {
//		return false;
//	}
//
//	@Override
//	public INDArray getData() {
//		return null;
//	}
//
//	@Override
//	public Encodable dup() {
//		return null;
//	}

	public boolean isOver_subscription() {
		System.out.println("Oversubscibed: " + over_subscription);
		return over_subscription;
	}

	public boolean isLong_requests() {
		System.out.println("Long requests: " + long_requests);
		return long_requests;
	}
}
