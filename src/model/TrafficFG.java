package model;


import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;
import simenv.SimulatorConstants;

public class TrafficFG implements Serializable {
	
	
	ArrayList<Double> epTraffic= null;
	//private ArrayList<Double> epTrafficEnvelope= null;
	//private ArrayList<Double> epTrafficEnvelopeAvg= null;
	int eps = SimulatorConstants.FG_EPS; //default endpoints 2
	double maxTraffic=0; 
	double cyclesPerPacket= SimulatorConstants.VNF_CPS; //default 1.9
	Random rand = new Random();
	int  n = rand.nextInt(400) + 100;
	MersenneTwister engine = new MersenneTwister(n);
	Uniform uni= new Uniform(SimulatorConstants.LOWER_TRAFFIC_FG,SimulatorConstants.UPPER_TRAFFIC_FG,engine);
	//private double envelopeTraffic=0;
	//private double envelopeTrafficAvg=0;
	
	public void generateTraffic(int eps, int req){ //initial
		this.eps=eps;
		//generate traffic from chain endpoints
		epTraffic= new ArrayList<Double> ();
		//epTrafficEnvelope= new ArrayList<Double> ();
		//epTrafficEnvelopeAvg= new ArrayList<Double> ();
		for (int i=0;i<eps;i++){
			this.maxTraffic=Math.floor(uni.nextDouble()*1000);
			//this.envelopeTraffic=SimulatorConstants.UPPER_TRAFFIC_FG*1000;
			//this.envelopeTrafficAvg=1000*(SimulatorConstants.UPPER_TRAFFIC_FG-SimulatorConstants.LOWER_TRAFFIC_FG)/2;
			//this.maxTraffic=SimulatorConstants.UPPER_TRAFFIC_FG *1000;
			this.epTraffic.add(this.maxTraffic); //Kbps
			//this.epTrafficEnvelope.add(envelopeTraffic); 
			//this.epTrafficEnvelopeAvg.add(envelopeTrafficAvg); 
			//this.epTraffic.add(1500.0); //Kbps
		}
		
	}
	
	public void generateTraffic(int eps,double max){
		this.eps=eps;
		//generate traffic from chain endpoints
		epTraffic= new ArrayList<Double> ();
		for (int i=0;i<eps;i++){
			//String epName= "ep"+i;
			Uniform uni= new Uniform(SimulatorConstants.LOWER_TRAFFIC_FG, 0.8*max/1000 ,engine); //tocheck
			if (0.8*max/1000 < SimulatorConstants.LOWER_TRAFFIC_FG) {
				System.out.println("ERORRRRRR");
			}
			
			// Uniform uni= new Uniform(SimulatorConstants.LOWER_TRAFFIC_FG, max, engine); //tocheck
			double trafficTMP =Math.floor(uni.nextDouble()*1000);
			System.out.println("max: " + max + "trafficTMP:" + trafficTMP );
			// System.exit(0);
			if (trafficTMP > 0.8 * max  || trafficTMP < SimulatorConstants.LOWER_TRAFFIC_FG * 1000) {
				System.out.println("ERORRRRRR");

			}
			if (trafficTMP>SimulatorConstants.UPPER_TRAFFIC_FG*1000) {
				System.out.println("ERORRRRRR");
				System.exit(0);
			}
				
			this.epTraffic.add(trafficTMP); //Kbps
			//this.epTraffic.add(1000.0); //Kbps
		}
		//System.out.println("EPupdates: " +this.epTraffic );
		

	}
	
	public void generateTraffic(int eps,String id){
		this.eps=eps;
		//generate traffic from chain endpoints
		epTraffic= new ArrayList<Double> ();
		for (int i=0;i<eps;i++){
			double trafficTMP = 0;
			if (id.contains("max")) {
				trafficTMP= SimulatorConstants.UPPER_TRAFFIC_FG*1000;
			}
			else {
				trafficTMP = 1000*(SimulatorConstants.UPPER_TRAFFIC_FG+SimulatorConstants.LOWER_TRAFFIC_FG)/2;
			}
			
			this.epTraffic.add(trafficTMP); //Kbps
			//this.epTraffic.add(1000.0); //Kbps
		}
		// System.out.println("EP: " +this.epTraffic);
		
	}
	public ArrayList<Double> getTraffic(){
		return this.epTraffic;
	}
	

   //OVERALOAD FUNCTION
	
	public HashMap<String, Double>  generateTrafficLoad(String nfName, ArrayList<Double> traffic,boolean bidirectional){
		HashMap<String, Double> nat = new HashMap<String, Double>();
		HashMap<String, Double> fw = new HashMap<String, Double>();
		HashMap<String, Double> dpi = new HashMap<String, Double>();
		HashMap<String, Double> ip = new HashMap<String, Double>();
		HashMap<String, Double> lb = new HashMap<String, Double>();
		HashMap<String, Double> aes = new HashMap<String, Double>();
		double ul =0;
		double dl =0;
		
		if (bidirectional){ 
			dl=traffic.get(1);
		}
		ul= traffic.get(0);
		//System.out.println("traffic: " + traffic + "name : " +nfName + " " +bidirectional);
	
		if (nfName.startsWith("nat")) {
			cyclesPerPacket=2; 
			if (bidirectional){
				nat.put("load",(cyclesPerPacket*((ul+dl)/(1.5*8))));
				nat.put("tr_in_ul",ul);
				nat.put("tr_out_ul",ul);
				nat.put("tr_in_dl",dl);
				nat.put("tr_out_dl",dl);
			} else {
				nat.put("load",(cyclesPerPacket*(ul/(1.5*8))));
				nat.put("tr_in_ul",ul);
				nat.put("tr_out_ul",ul);
			}		
			return nat;
		} else if (nfName.startsWith("fw")){
			//System.out.println("Ul: " +ul+ "DL: "+dl);
			cyclesPerPacket=1.5; //kcycles
			if (bidirectional){
				fw.put("load",(cyclesPerPacket*((ul+dl)/(1.5*8))));
				fw.put("tr_in_ul",ul);
				fw.put("tr_out_ul",ul);
				fw.put("tr_in_dl",dl);
				fw.put("tr_out_dl",dl);
			} else {
				fw.put("load",(cyclesPerPacket*(ul/(1.5*8))));
				fw.put("tr_in_ul",ul);
				fw.put("tr_out_ul",ul);
			}
			return fw;
		} 
		else if (nfName.startsWith("dpi")){		
			cyclesPerPacket=53;
			if (bidirectional){
				dpi.put("load",(cyclesPerPacket*((ul+dl)/(1.5*8))));
				dpi.put("tr_in_ul",ul);
				dpi.put("tr_out_ul",ul);
				dpi.put("tr_in_dl",dl);
				dpi.put("tr_out_dl",dl);
			} else {
				dpi.put("load",(cyclesPerPacket*((ul)/(1.5*8))));
				dpi.put("tr_in_ul",ul);
				dpi.put("tr_out_ul",ul);
			}
			
			return dpi;
			
		} else if (nfName.startsWith("ip")){		
			cyclesPerPacket=1;//1.874;
			if (bidirectional){
				ip.put("load",(cyclesPerPacket*((ul+dl)/(1.5*8))));
				ip.put("tr_in_ul",ul);
				ip.put("tr_out_ul",ul);
				ip.put("tr_in_dl",dl);
				ip.put("tr_out_dl",dl);
			} else {
				ip.put("load",(cyclesPerPacket*((ul)/(1.5*8))));
				ip.put("tr_in_ul",ul);
				ip.put("tr_out_ul",ul);
			}
			
			return ip;
			
		}else if (nfName.startsWith("lb")){		
			cyclesPerPacket=1.5;
			if (bidirectional){
				lb.put("load",(cyclesPerPacket*((ul+dl)/(1.5*8))));
				lb.put("tr_in_ul",ul);
				lb.put("tr_out_ul",ul);
				lb.put("tr_in_dl",dl);
				lb.put("tr_out_dl",dl);
			} else {
				lb.put("load",(cyclesPerPacket*((ul)/(1.5*8))));
				lb.put("tr_in_ul",ul);
				lb.put("tr_out_ul",ul);
			}
			
			return lb;
			
		}else if (nfName.startsWith("aes")){		
			cyclesPerPacket=53;
			if (bidirectional){
				aes.put("load",(cyclesPerPacket*((ul+dl)/(1.5*8))));
				aes.put("tr_in_ul",ul);
				aes.put("tr_out_ul",ul);
				aes.put("tr_in_dl",dl);
				aes.put("tr_out_dl",dl);
			} else {
				aes.put("load",(cyclesPerPacket*((ul)/(1.5*8))));
				aes.put("tr_in_dl",ul);
				aes.put("tr_out_dl",ul);
			}
			return aes;
			
		}

		return null; 
			
	
	}
	
////////////// 
	
	public HashMap<String, Double>  generateTrafficLoad(String nfName,boolean bidirectional){
		HashMap<String, Double> nat = new HashMap<String, Double>();
		HashMap<String, Double> fw = new HashMap<String, Double>();
		HashMap<String, Double> dpi = new HashMap<String, Double>();
		HashMap<String, Double> ip = new HashMap<String, Double>();
		HashMap<String, Double> lb = new HashMap<String, Double>();
		HashMap<String, Double> aes = new HashMap<String, Double>();
		double ul =0;
		double dl =0;
		if (bidirectional){ 
			dl=this.epTraffic.get(1);
		}
		
		
		
		ul= this.epTraffic.get(0);
	
		if (nfName.startsWith("nat")) {
			cyclesPerPacket=2; 
			if (bidirectional){
				nat.put("load",(cyclesPerPacket*((ul+dl)/(1.5*8))));
				nat.put("tr_in_ul",ul);
				nat.put("tr_out_ul",ul);
				nat.put("tr_in_dl",dl);
				nat.put("tr_out_dl",dl);
			} else {
				nat.put("load",(cyclesPerPacket*(ul/(1.5*8))));
				nat.put("tr_in_ul",ul);
				nat.put("tr_out_ul",ul);
			}		
			return nat;
		} else if (nfName.startsWith("fw")){
			//System.out.println("Ul: " +ul+ "DL: "+dl);
			cyclesPerPacket=1.5; //kcycles
			if (bidirectional){
				fw.put("load",(cyclesPerPacket*((ul+dl)/(1.5*8))));
				fw.put("tr_in_ul",ul);
				fw.put("tr_out_ul",ul);
				fw.put("tr_in_dl",dl);
				fw.put("tr_out_dl",dl);
			} else {
				fw.put("load",(cyclesPerPacket*(ul/(1.5*8))));
				fw.put("tr_in_ul",ul);
				fw.put("tr_out_ul",ul);
			}
			return fw;
		} 
		else if (nfName.startsWith("dpi")){		
			cyclesPerPacket=53;
			if (bidirectional){
				dpi.put("load",(cyclesPerPacket*((ul+dl)/(1.5*8))));
				dpi.put("tr_in_ul",ul);
				dpi.put("tr_out_ul",ul);
				dpi.put("tr_in_dl",dl);
				dpi.put("tr_out_dl",dl);
			} else {
				dpi.put("load",(cyclesPerPacket*((ul)/(1.5*8))));
				dpi.put("tr_in_ul",ul);
				dpi.put("tr_out_ul",ul);
			}
			
			return dpi;
			
		} else if (nfName.startsWith("ip")){		
			cyclesPerPacket=1;//1.874;
			if (bidirectional){
				ip.put("load",(cyclesPerPacket*((ul+dl)/(1.5*8))));
				ip.put("tr_in_ul",ul);
				ip.put("tr_out_ul",ul);
				ip.put("tr_in_dl",dl);
				ip.put("tr_out_dl",dl);
			} else {
				ip.put("load",(cyclesPerPacket*((ul)/(1.5*8))));
				ip.put("tr_in_ul",ul);
				ip.put("tr_out_ul",ul);
			}
			
			return ip;
			
		}else if (nfName.startsWith("lb")){		
			cyclesPerPacket=1.5;
			if (bidirectional){
				if (eps>2) 
					dl=this.epTraffic.get(1)+this.epTraffic.get(2);
				lb.put("load",(cyclesPerPacket*((ul+dl)/(1.5*8))));
				lb.put("tr_in_ul",ul);
				lb.put("tr_out_ul",ul);
				lb.put("tr_in_dl",dl);
				lb.put("tr_out_dl",dl);
			} else {
				lb.put("load",(cyclesPerPacket*((ul)/(1.5*8))));
				lb.put("tr_in_ul",ul);
				lb.put("tr_out_ul",ul);
			}
			
			return lb;
			
		}else if (nfName.startsWith("aes")){		
			cyclesPerPacket=53;
			if (bidirectional){
				aes.put("load",(cyclesPerPacket*((ul+dl)/(1.5*8))));
				aes.put("tr_in_ul",ul);
				aes.put("tr_out_ul",ul);
				aes.put("tr_in_dl",dl);
				aes.put("tr_out_dl",dl);
			} else {
				aes.put("load",(cyclesPerPacket*((ul)/(1.5*8))));
				aes.put("tr_in_ul",ul);
				aes.put("tr_out_ul",ul);
			}
			return aes;
			
		}
	
	
		return null; 
			
	
	}

	public double getTrafficBound() {
		// TODO Auto-generated method stub
		return this.maxTraffic;
	}
	
}
