package model;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sourceforge.jdistlib.Uniform;
import simenv.SimulatorConstants;

public class TrafficLTE {
	
	Uniform uni= new Uniform(385,2300);
	double rho=2300; //default
	double MMEload=0; //default
	
	double session_duration_voice= 180.0; // 1/μ
	double inv_session_duration_voice = 1.0/session_duration_voice;
	double session_duration_streaming= 180.0;
	double inv_session_duration_streaming = 1/session_duration_streaming;
	double session_duration_bg= 10.0;
	double inv_session_duration_bg= 1/session_duration_bg;
	
	double voice_session_arr_rate = 0.67/3600.0; //α  session arrival rate  
	double streaming_session_arr_rate = 5.0/3600.0;
	double bg_session_arr_rate = 40.0/3600.0;
	
	double UE_prob_voice= 0.5; //χ probability that UE initiates session
	double UE_prob_streaming= 1.0;
	double UE_prob_bg=0.8;
	double t=10.0;
	
	//1/λ = 1/α-1/μ =>  λ = 1/(1/α - 1/μ)
	double radius = SimulatorConstants.OVERLAPPING_FACTOR* 
			(Math.sqrt(SimulatorConstants.AREA/(Math.PI* SimulatorConstants.MAX_DIS_RRH_BBU)));
	double cell_area= Math.PI*Math.pow(radius, 2);
	
	
	double voice_arr_rate = 1/((1/voice_session_arr_rate) - session_duration_voice);
	double streaming_arr_rate = 1/((1/streaming_session_arr_rate) - session_duration_streaming);
	double bg_arr_rate = 1/((1/bg_session_arr_rate) - session_duration_bg);
	
	double Apar = voice_arr_rate+streaming_arr_rate+bg_arr_rate; // (Σλ)
	double Bpar = (inv_session_duration_voice/(inv_session_duration_voice+voice_arr_rate))*
			(inv_session_duration_streaming/(inv_session_duration_streaming+streaming_arr_rate))*
			(inv_session_duration_bg/(inv_session_duration_bg+bg_arr_rate)); //Π(μ/μ+λ)
	double Cpar = Math.exp(-1*Apar*t);
	
	
	double ax_voice = UE_prob_voice*voice_session_arr_rate;
	double ax_streaming= UE_prob_streaming*streaming_session_arr_rate;
	double ax_bg=UE_prob_bg*bg_session_arr_rate;
	
	double Prob_UE_session= (ax_voice+ax_streaming+ax_bg)/(voice_session_arr_rate+streaming_session_arr_rate+bg_session_arr_rate);
	double Prob_nonUE_session= 1-Prob_UE_session;
	
	double Msr_ue = 3.0;
	double Msr_net= 4.0;
	double Mst_idle= 3.0;
	double Mst_enb_idle= 3.0;
	
	double sum_msg = Msr_ue*Prob_UE_session+Msr_net*Prob_nonUE_session+Mst_idle;
	
	double packet_att_enb_mme_ul = 218.0;  //bytes
	double packet_att_mme_sgw_ul=501.0; //bytes
	double packet_att_sgw_pgw_ul=402.0;
	
	double packet_sr_enb_mme_ul = 218.0;
	double packet_sr_mme_sgw_ul =169.0; 
	double packet_sr_sgw_pgw_ul =65.0; 
	
	double packet_srl_enb_mme_ul = 132.0;
	double packet_srl_mme_sgw_ul=65.0; 
	double packet_srl_sgw_pgw_ul=0;
	
	double packet_att_enb_mme_dl = 212.0;  //bytes
	double packet_att_mme_sgw_dl=388.0; //bytes
	double packet_att_sgw_pgw_dl=402.0;
	
	double packet_sr_enb_mme_dl =212.0;
	double packet_sr_mme_sgw_dl=172.0; 
	double packet_sr_sgw_pgw_dl=81.0; 
	
	double packet_srl_enb_mme_dl = 67.0;
	double packet_srl_mme_sgw_dl=66.0; 
	double packet_srl_sgw_pgw_dl=0;
	
	double trc_enb_mme_ul=0;
	double trc_enb_mme_dl=0;
	double trc_mme_sgw_ul=0;
	double trc_mme_sgw_ul_pps=0;
	double trc_mme_sgw_dl=0;
	double trc_mme_sgw_dl_pps=0;
	double trc_sgw_pgw_ul=0;
	double trc_sgw_pgw_ul_pps=0;
	double trc_sgw_pgw_dl=0;
	double trc_sgw_pgw_dl_pps=0;
	
	double streaming_nrate = 256000.0;//256kbps
	double streaming_rate_perUE = 64000.0; 
	///5 sessions/h for 180s duration → all sessions of a UE 
	//active for 900s (=0.25% active time)  * nominal traffic (256kbps) 
	double voice_nrate = 12560.0;  //12.56 kbps
	double voice_rate_perUE = 1525.59; //bps
	//0.67 sessions/h for 180s duration → all sessions of a UE active 
	//for 120.6 s (=0.0335% active time) * nominal traffic (12.65kbps) →  kbps 
	double bg_nrate= 550000.0;
	double bg_rate_perUE = 61111.1;
     //40 sessions/h for 10s duration → all sessions of a UE active for 400s (=0.111% active time) 
	//* nominal traffic (550kbps)
	double tru_streaming_dl=0;
	double tru_voice_dl=0;
	double tru_bg_dl=0;
	
	double tru_streaming_dl_pps=0;
	double tru_voice_dl_pps=0;
	double tru_bg_dl_pps=0;
	
	double tru_streaming_ul=0;
	double tru_voice_ul=0;
	double tru_bg_ul=0;
	
	double tru_streaming_ul_pps=0;
	double tru_voice_ul_pps=0;
	double tru_bg_ul_pps=0;
	
	HashMap<String, Double> mme = new HashMap<String, Double>();
	HashMap<String, Double> sgw = new HashMap<String, Double>();
	HashMap<String, Double> pgw = new HashMap<String, Double>();
	HashMap<String, Double> enb = new HashMap<String, Double>();
	
	public void setUEdensity(double rho){
		this.rho=rho;
	}
	
	public void generateTrafficLoad(){
		System.out.println("Prob_UE_session : "+Prob_UE_session);
		//this.rho  = Math.floor(uni.random());
		this.rho=615;
//		this.rho =2308.0;
		double UEs_per_area = rho*cell_area;
		//System.out.println("UE_prob_voice+UE_prob_streaming+UE_prob_bg: " + (UE_prob_voice+UE_prob_streaming+UE_prob_bg));
		//load
		this.MMEload = Apar*Bpar*Cpar*UEs_per_area*sum_msg;
		//control traffic
		this.trc_enb_mme_ul = Apar*Bpar*Cpar*UEs_per_area*(packet_sr_enb_mme_ul +packet_srl_enb_mme_ul)*8;
		this.trc_enb_mme_dl = Apar*Bpar*Cpar*UEs_per_area*(packet_sr_enb_mme_dl +packet_srl_enb_mme_dl)*8;
		
		this.trc_mme_sgw_ul = Apar*Bpar*Cpar*UEs_per_area*(packet_sr_mme_sgw_ul+packet_srl_mme_sgw_ul)*8;
		this.trc_mme_sgw_dl = Apar*Bpar*Cpar*UEs_per_area*(packet_sr_mme_sgw_dl+packet_srl_mme_sgw_dl)*8;
		this.trc_mme_sgw_ul_pps = Apar*Bpar*Cpar*UEs_per_area*2;
		this.trc_mme_sgw_dl_pps= Apar*Bpar*Cpar*UEs_per_area*2;
		
		this.trc_sgw_pgw_ul = Apar*Bpar*Cpar*UEs_per_area*(packet_sr_sgw_pgw_ul+packet_srl_sgw_pgw_ul)*8;
		this.trc_sgw_pgw_dl = Apar*Bpar*Cpar*UEs_per_area*(packet_sr_sgw_pgw_dl+packet_srl_sgw_pgw_dl)*8;
		this.trc_sgw_pgw_ul_pps=Apar*Bpar*Cpar*UEs_per_area*2;
		this.trc_sgw_pgw_dl_pps=Apar*Bpar*Cpar*UEs_per_area*2;
		
		
		this.tru_streaming_dl=(streaming_rate_perUE*streaming_session_arr_rate*UEs_per_area) / (1293*8);//https://www.idc.ac.il/en/schools/cs/research/Documents/online%20classification%20of%20vod%202013.pdf
		this.tru_voice_dl=voice_rate_perUE*voice_session_arr_rate*UEs_per_area /253; //http://lteuniversity.com/get_trained/expert_opinion1/b/donhanley/archive/2013/09/11/how-big-is-a-voice-call.aspx
		this.tru_bg_dl=bg_rate_perUE*bg_session_arr_rate*UEs_per_area / 8000; //1kB
		
		this.tru_streaming_dl_pps=tru_streaming_dl/ (1293*8);//https://www.idc.ac.il/en/schools/cs/research/Documents/online%20classification%20of%20vod%202013.pdf
		this.tru_voice_dl_pps=tru_voice_dl/253; //http://lteuniversity.com/get_trained/expert_opinion1/b/donhanley/archive/2013/09/11/how-big-is-a-voice-call.aspx
		this.tru_bg_dl_pps=tru_bg_dl/ 8000; //1kB
		
		
		this.tru_voice_ul=tru_voice_dl;
		this.tru_bg_ul=tru_bg_dl;
		
		this.tru_voice_ul_pps=tru_voice_dl_pps;
		this.tru_bg_ul_pps=tru_bg_dl_pps;
		
		mme.put("load",this.MMEload);
		mme.put("trc_in_ul",this.trc_enb_mme_ul);
		mme.put("trc_out_ul",this.trc_mme_sgw_ul);
		mme.put("trc_in_dl",this.trc_mme_sgw_dl);
		mme.put("trc_out_dl",this.trc_enb_mme_dl);
		mme.put("tru_ul",0.0);
		mme.put("tru_dl",0.0);
/*		mme.put("tru_ul_pps",0.0);
		mme.put("tru_dl_pps",0.0);*/
		
		double data_sg_dl=this.tru_streaming_dl+this.tru_voice_dl+this.tru_bg_dl;
		double data_sg_dl_pps=this.tru_streaming_dl_pps+this.tru_voice_dl_pps+this.tru_bg_dl_pps;
		double data_sg_ul=this.tru_streaming_ul+this.tru_voice_ul+this.tru_bg_ul;
		double data_sg_ul_pps=this.tru_streaming_ul_pps+this.tru_voice_ul_pps+this.tru_bg_ul_pps;
		//missing loads for components
		sgw.put("load", 1874* (this.trc_mme_sgw_ul_pps+this.trc_mme_sgw_dl_pps+data_sg_dl_pps+data_sg_ul_pps));
		sgw.put("trc_in_ul",this.trc_mme_sgw_ul);
		sgw.put("trc_out_ul",this.trc_sgw_pgw_ul);
		sgw.put("trc_in_dl",this.trc_sgw_pgw_dl);
		sgw.put("trc_out_dl",this.trc_mme_sgw_dl);
		sgw.put("tru_ul",data_sg_ul);
		sgw.put("tru_dl",data_sg_dl);
/*		sgw.put("tru_ul_pps",(tru_streaming_ul_pps+tru_bg_ul_pps));
		sgw.put("tru_dl_pps",(tru_streaming_dl_pps+tru_bg_dl_pps));*/
		
		double data_pg_dl=this.tru_streaming_dl+this.tru_voice_dl+this.tru_bg_dl;
		double data_pg_dl_pps=this.tru_streaming_dl_pps+this.tru_voice_dl_pps+this.tru_bg_dl_pps;
		double data_pg_ul=this.tru_streaming_ul+this.tru_voice_ul+this.tru_bg_ul;
		double data_pg_ul_pps=this.tru_streaming_ul_pps+this.tru_voice_ul_pps+this.tru_bg_ul_pps;
		pgw.put("load", 1874* (this.trc_sgw_pgw_ul_pps+this.trc_sgw_pgw_dl_pps+data_pg_dl_pps+data_pg_ul_pps));
		pgw.put("trc_in_ul",this.trc_sgw_pgw_ul);
		pgw.put("trc_out_ul",0.0);
		pgw.put("trc_in_dl",0.0);
		pgw.put("trc_out_dl",this.trc_sgw_pgw_dl);
		pgw.put("tru_ul",data_pg_ul);
		pgw.put("tru_dl",data_pg_dl);
/*		pgw.put("tru_ul_pps",(tru_streaming_ul_pps+tru_bg_ul_pps));
		pgw.put("tru_dl_pps",(tru_streaming_dl_pps+tru_bg_dl_pps));*/
		
		///find for enb
		enb.put("load",this.MMEload);
		enb.put("trc_in_ul",0.0);
		enb.put("trc_out_ul",this.trc_mme_sgw_ul);
		enb.put("trc_in_dl",this.trc_mme_sgw_dl);
		enb.put("trc_out_dl",0.0);
		enb.put("tru_ul",(tru_streaming_ul+tru_bg_ul));
		enb.put("tru_dl",(tru_streaming_dl+tru_bg_dl));
/*		enb.put("tru_ul_pps",(tru_streaming_ul_pps+tru_bg_ul_pps));
		enb.put("tru_dl_pps",(tru_streaming_dl_pps+tru_bg_dl_pps));*/
	}
	
	
	
	public void generateTrafficLoad(int rho){
		System.out.println("Prob_UE_session : "+Prob_UE_session);
		this.rho  = rho;
		
		double UEs_per_area = rho*cell_area;
		//System.out.println("UE_prob_voice+UE_prob_streaming+UE_prob_bg: " + (UE_prob_voice+UE_prob_streaming+UE_prob_bg));
		//load
		this.MMEload = Apar*Bpar*Cpar*UEs_per_area*sum_msg;
		//control traffic
		this.trc_enb_mme_ul = Apar*Bpar*Cpar*UEs_per_area*(packet_sr_enb_mme_ul +packet_srl_enb_mme_ul)*8;
		this.trc_enb_mme_dl = Apar*Bpar*Cpar*UEs_per_area*(packet_sr_enb_mme_dl +packet_srl_enb_mme_dl)*8;
		
		this.trc_mme_sgw_ul = Apar*Bpar*Cpar*UEs_per_area*(packet_sr_mme_sgw_ul+packet_srl_mme_sgw_ul)*8;
		this.trc_mme_sgw_dl = Apar*Bpar*Cpar*UEs_per_area*(packet_sr_mme_sgw_dl+packet_srl_mme_sgw_dl)*8;
		this.trc_mme_sgw_ul_pps = Apar*Bpar*Cpar*UEs_per_area*2;
		this.trc_mme_sgw_dl_pps= Apar*Bpar*Cpar*UEs_per_area*2;
		
		this.trc_sgw_pgw_ul = Apar*Bpar*Cpar*UEs_per_area*(packet_sr_sgw_pgw_ul+packet_srl_sgw_pgw_ul)*8;
		this.trc_sgw_pgw_dl = Apar*Bpar*Cpar*UEs_per_area*(packet_sr_sgw_pgw_dl+packet_srl_sgw_pgw_dl)*8;
		this.trc_sgw_pgw_ul_pps=Apar*Bpar*Cpar*UEs_per_area*2;
		this.trc_sgw_pgw_dl_pps=Apar*Bpar*Cpar*UEs_per_area*2;
		
		
		this.tru_streaming_dl=(streaming_rate_perUE*streaming_session_arr_rate*UEs_per_area) / (1293*8);//https://www.idc.ac.il/en/schools/cs/research/Documents/online%20classification%20of%20vod%202013.pdf
		this.tru_voice_dl=voice_rate_perUE*voice_session_arr_rate*UEs_per_area /253; //http://lteuniversity.com/get_trained/expert_opinion1/b/donhanley/archive/2013/09/11/how-big-is-a-voice-call.aspx
		this.tru_bg_dl=bg_rate_perUE*bg_session_arr_rate*UEs_per_area / 8000; //1kB
		
		this.tru_streaming_dl_pps=tru_streaming_dl/ (1293*8);//https://www.idc.ac.il/en/schools/cs/research/Documents/online%20classification%20of%20vod%202013.pdf
		this.tru_voice_dl_pps=tru_voice_dl/253; //http://lteuniversity.com/get_trained/expert_opinion1/b/donhanley/archive/2013/09/11/how-big-is-a-voice-call.aspx
		this.tru_bg_dl_pps=tru_bg_dl/ 8000; //1kB
		
		
		this.tru_voice_ul=tru_voice_dl;
		this.tru_bg_ul=tru_bg_dl;
		
		this.tru_voice_ul_pps=tru_voice_dl_pps;
		this.tru_bg_ul_pps=tru_bg_dl_pps;
		
		mme.put("load",this.MMEload);
		mme.put("trc_in_ul",this.trc_enb_mme_ul);
		mme.put("trc_out_ul",this.trc_mme_sgw_ul);
		mme.put("trc_in_dl",this.trc_mme_sgw_dl);
		mme.put("trc_out_dl",this.trc_enb_mme_dl);
		mme.put("tru_ul",0.0);
		mme.put("tru_dl",0.0);

		
		double data_sg_dl=this.tru_streaming_dl+this.tru_voice_dl+this.tru_bg_dl;
		double data_sg_dl_pps=this.tru_streaming_dl_pps+this.tru_voice_dl_pps+this.tru_bg_dl_pps;
		double data_sg_ul=this.tru_streaming_ul+this.tru_voice_ul+this.tru_bg_ul;
		double data_sg_ul_pps=this.tru_streaming_ul_pps+this.tru_voice_ul_pps+this.tru_bg_ul_pps;
		//missing loads for components
		sgw.put("load", 1874* (this.trc_mme_sgw_ul_pps+this.trc_mme_sgw_dl_pps+data_sg_dl_pps+data_sg_ul_pps));
		sgw.put("trc_in_ul",this.trc_mme_sgw_ul);
		sgw.put("trc_out_ul",this.trc_sgw_pgw_ul);
		sgw.put("trc_in_dl",this.trc_sgw_pgw_dl);
		sgw.put("trc_out_dl",this.trc_mme_sgw_dl);
		sgw.put("tru_ul",data_sg_ul);
		sgw.put("tru_dl",data_sg_dl);
/*		sgw.put("tru_ul_pps",(tru_streaming_ul_pps+tru_bg_ul_pps));
		sgw.put("tru_dl_pps",(tru_streaming_dl_pps+tru_bg_dl_pps));*/
		
		double data_pg_dl=this.tru_streaming_dl+this.tru_voice_dl+this.tru_bg_dl;
		double data_pg_dl_pps=this.tru_streaming_dl_pps+this.tru_voice_dl_pps+this.tru_bg_dl_pps;
		double data_pg_ul=this.tru_streaming_ul+this.tru_voice_ul+this.tru_bg_ul;
		double data_pg_ul_pps=this.tru_streaming_ul_pps+this.tru_voice_ul_pps+this.tru_bg_ul_pps;
		pgw.put("load", 1874* (this.trc_sgw_pgw_ul_pps+this.trc_sgw_pgw_dl_pps+data_pg_dl_pps+data_pg_ul_pps));
		pgw.put("trc_in_ul",this.trc_sgw_pgw_ul);
		pgw.put("trc_out_ul",0.0);
		pgw.put("trc_in_dl",0.0);
		pgw.put("trc_out_dl",this.trc_sgw_pgw_dl);
		pgw.put("tru_ul",data_pg_ul);
		pgw.put("tru_dl",data_pg_dl);
/*		pgw.put("tru_ul_pps",(tru_streaming_ul_pps+tru_bg_ul_pps));
		pgw.put("tru_dl_pps",(tru_streaming_dl_pps+tru_bg_dl_pps));*/
		
		///find for enb
		enb.put("load",this.MMEload);
		enb.put("trc_in_ul",0.0);
		enb.put("trc_out_ul",this.trc_mme_sgw_ul);
		enb.put("trc_in_dl",this.trc_mme_sgw_dl);
		enb.put("trc_out_dl",0.0);
		enb.put("tru_ul",(tru_streaming_ul+tru_bg_ul));
		enb.put("tru_dl",(tru_streaming_dl+tru_bg_dl));
/*		enb.put("tru_ul_pps",(tru_streaming_ul_pps+tru_bg_ul_pps));
		enb.put("tru_dl_pps",(tru_streaming_dl_pps+tru_bg_dl_pps));*/
	}
	
	public HashMap<String, Double> getNF(String nfName){
		if (nfName.equalsIgnoreCase("mme")) {
			return this.mme;
		} else if (nfName.equalsIgnoreCase("enb")){
			return this.enb;
		} else if (nfName.equalsIgnoreCase("sgw")){
			return this.sgw;
		} else if (nfName.equalsIgnoreCase("pgw")){
			return this.pgw;
		}
		return mme;
	}
	
	public void printModel(){
		System.out.println("voice_arr_rate: "+voice_session_arr_rate + "streaming_arr_rate: "+ streaming_session_arr_rate + 
				" bg_arr_rate: " +bg_session_arr_rate);
		System.out.println("UEs_per_area: "+ rho*cell_area);
		System.out.println("Σα : " +  (voice_session_arr_rate+streaming_session_arr_rate+bg_session_arr_rate));
		System.out.println("Σαχ : " + ( ax_voice+  ax_streaming +ax_bg));
		System.out.println("sum_msg " +sum_msg);
		System.out.println("Beta params: " + Apar + " " + Bpar + " "+ Cpar);
	}
	
	
	public double getMMEload(){
		return this.MMEload; 
	}
	

}
