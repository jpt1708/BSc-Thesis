package ML;

import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.rl4j.learning.NeuralNetFetchable;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.network.dqn.IDQN;
import org.deeplearning4j.rl4j.space.ArrayObservationSpace;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.ObservationSpace;
import tools.ComparatorDouble;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;



public class EmbeddingProblem  implements
	MDP<ObservedState, Integer, DiscreteSpace>  {
	
	private double[] rcc;
	private double[] rlf;
	private double[] lifetime;
	private double[] hosted;
	private double[] reqNodesCap;
	private double[] subNodesCap;
	private double reqLife;
	private String[] subNodesType;
	double[][] node_map = null;
	private HashMap<Integer,Integer> map = new HashMap<Integer,Integer> ();
	private double nom_cap;
	private double avg_rlf=10000;
	private int index=0;
 
    private DiscreteSpace actionSpace = new DiscreteSpace(2); // new EmbeddingActionSpaceDiscrete("accept", "deny");// new DiscreteSpace(2);
    private ObservedState subState;
    private ObservationSpace<ObservedState> observationSpace;
  //  private ObservationSpace<ObservedState> observationSpace  = new DiscreteSpace(2);
   // private ObservedState subState=reset();
    private NeuralNetFetchable<IDQN> fetchable;
	private int hosts;


	public void setSubstrate(double[] rcc, double[] lf, double nomCap, double avRlf, int servers) {
		this.nom_cap=nomCap;
		this.avg_rlf=avRlf;
		this.rcc=rcc;
		this.lifetime=lf;
		this.hosts=servers;
		this.subState = new ObservedState(rcc, lifetime,nom_cap,avg_rlf);
		this.observationSpace= new ArrayObservationSpace(new int[] {2});

		System.out.println("In setSubstrate");
	}
	
	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	public void setFetchable(NeuralNetFetchable<IDQN> fetchable) {
		this.fetchable = fetchable;
	}

	//@Override
	public DiscreteSpace getActionSpace() {
		// TODO Auto-generated method stub;
		System.out.println("In getActionSpace");
		return actionSpace;
	}

	/*
	 * @Override public ObservationSpace<ObservedState> getObservationSpace() {
	 * 
	 * System.out.println("In getObservationSpace"); return null; }
	 */
	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		System.out.println("In isdone");
		return false;
	}

	@Override
	public MDP<ObservedState, Integer, DiscreteSpace> newInstance() {
		// TODO Auto-generated method stub
		System.out.println("In newInstance");

		return null;
	}

	@Override
	public ObservedState reset() {
		System.out.println("Find observed state for substrate");
		ObservedState subState = new ObservedState(rcc, lifetime,nom_cap,avg_rlf);
		System.out.println("In reset observed state");
        return subState;
	}

	@Override
	// StepReply is the container for the data returned after each step(action).
	public StepReply<ObservedState> step(Integer arg0) {
		System.out.println("In step reply  time: " + index+ " " +arg0);
		this.node_map = null;
		double reward=0;
		if (arg0!=0) { //accept
			this.node_map = mapSolution();
			if (!subState.isOver_subscription()){
				reward=1.2;
			} else if (subState.isOver_subscription() && (!subState.isLong_requests())){
				reward=-5;
			} else if(subState.isOver_subscription() && subState.isLong_requests()){
				reward=-10;
			}
		}
		else {
			if (!subState.isOver_subscription()){
				reward=-1;
			} else if (subState.isOver_subscription()){
				reward=1;
			}
			System.out.println("reward: " + reward);
		}

		index++;
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if 	(subState.isOver_subscription()) {
			System.out.println("Pausing program -  reward " + reward );
			try {
				System.in.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new StepReply<>(subState, reward, isDone(),null);
	}

	@Override
	public ObservationSpace<ObservedState> getObservationSpace() {
		System.out.println("In getObservationSpace");
		return observationSpace; //current observation space
	}


	private double[][]  mapSolution() {
		
			double[][] nodemap = null;
			double[][] nodemap_transformed = new double[reqNodesCap.length][this.rcc.length];
	
			System.out.println("In Mapping: "+ Arrays.toString(this.rcc));
			
			double[] tmp_rcc =  Arrays.copyOf(this.rcc, this.rcc.length); // optNodeMapping1 anticollocation
			double[] tmp_rlf =  Arrays.copyOf(this.lifetime, this.lifetime.length); // optNodeMapping1 anticollocation
	/*		if (action==1)
				nodemap = optNodeMapping1(subNodesCap.length,reqNodesCap.length, subNodesCap, reqNodesCap,subNodesType,nom_cap);
			else  // optNodeMapping collocation
	*/		nodemap = greedy(subNodesCap.length,reqNodesCap.length, subNodesCap, reqNodesCap,subNodesType,nom_cap);
			//System.out.println(nodemap);
			//find new state
			for (int u=0;u<subNodesCap.length;u++){
				for (int i=0;i<reqNodesCap.length;i++){
						if (nodemap[u][i] > 0){
							int rcc_id = map.get(u);
							tmp_rcc[rcc_id] -=reqNodesCap[i];
							tmp_rlf[rcc_id] = (this.rlf[rcc_id]+reqLife)/(this.hosted[rcc_id]+1);
							nodemap_transformed[i][rcc_id]=1;
								System.out.println("VFN : " + i + " mapped to: " +  u + " withn newID: " +rcc_id);
						}
				}
			}	
			setSubstrate(tmp_rcc,tmp_rlf,this.nom_cap,this.avg_rlf,this.hosts);

			return nodemap_transformed;
		}

	public void setInfo(double[] reqNodesCap , double[] rlf, double[] subNodesCap,
			String[] subNodesType, double[] hosted, HashMap<Integer,Integer> map, double reqLife) {
		this.reqNodesCap=reqNodesCap;
		this.rlf=rlf;
		this.subNodesType=subNodesType;
		this.subNodesCap=subNodesCap;
		this.hosted=hosted;
		this.map=map;
		this.reqLife=reqLife;
		
	}
	
	//anti collocation
	@SuppressWarnings("unchecked")
	private double[][] greedy(int subNodesNum, int sizeOfSFC, double[] subNodes, double[] sfcNodes, 
			String[] subNodesType, double nom_cap){
		double[][] node_map = new double [subNodesNum][sizeOfSFC];
		for (int u=0;u<node_map.length;u++)
			Arrays.fill(node_map[u],0);
		HashMap<Integer,Double> hMap = new HashMap<Integer,Double>(); // cap id
		 LinkedHashMap<Integer,Double> sortedMap = new LinkedHashMap<>();
		 LinkedHashMap<Integer,Double> reSortedMap = new LinkedHashMap<>();

	     ArrayList<Double> list = new ArrayList<>();
	     
		
		//sort
		for (int i=0;i<subNodes.length;i++) {
			if (!(subNodesType[i].equalsIgnoreCase("Switch"))) {
				hMap.put(i,subNodes[i]);
			//System.out.println("subNodes: " +subNodes[i]);
			}
			
		}
		for (Entry<Integer, Double> entry : hMap.entrySet()) {
	        list.add(entry.getValue());
	     }
		
	     Collections.sort(list, new ComparatorDouble());
	     Collections.reverse(list);  
	     for (Double str : list) {
	        for (Entry<Integer, Double> entry : hMap.entrySet()) {
	           if (entry.getValue().equals(str)) {
	              sortedMap.put(entry.getKey(), str);
	           }
	        }
	     }
	     //System.out.println(sortedMap);
		
	     for (int i=0;i<sfcNodes.length;i++) {
	    	 for (Entry<Integer, Double> sub_node : sortedMap.entrySet()) {
	    		 if (sub_node.getValue()>sfcNodes[i]) {
	    			 int u=sub_node.getKey();
	    			// System.out.println("[Key] : " + sub_node.getKey() + " [Value] : " + sub_node.getValue());
	    			 node_map[u][i]=1.0;
	    			 sortedMap.put(sub_node.getKey(),(Double)(sub_node.getValue()-sfcNodes[i]));
	    			 break;
	    		 }
	    	 }
	     }
	    
	     if (countNonZero(node_map)==sfcNodes.length) {
	    	 return node_map;
	     }
	     
	     //resort
	     list = new ArrayList<>();
	     for (Entry<Integer, Double> entry : sortedMap.entrySet()) {
	         list.add(entry.getValue());
	      }
	     
	     Collections.sort(list, new ComparatorDouble());
	     Collections.reverse(list);  
	     for (Double str : list) {
	        for (Entry<Integer, Double> entry : sortedMap.entrySet()) {
	           if (entry.getValue().equals(str)) {
	        	   reSortedMap.put(entry.getKey(), str);
	           }
	        }
	     }
	     System.out.println(reSortedMap);
	     System.out.println(Arrays.deepToString(node_map));
	     //oversubscribe the rest to the less loaded server
	     for (int i=0;i<sfcNodes.length;i++) {
	    	 if (countNonZero(node_map,i)==0) {
	    	 for (Entry<Integer, Double> sub_node : reSortedMap.entrySet()) {
	    			 int u=sub_node.getKey();
	    			 //System.out.println(i + " to sub  " +u);
	    			 //System.out.println("[Key] : " + sub_node.getKey() + " [Value] : " + sub_node.getValue());
	    			 node_map[u][i]=1.0;	 
	    			 break;
	    		 }
	    	 }
	     }

		
		return 	node_map;
		
	}
	
    private int countNonZero(double[][] node_map) {
  	  int occurences=0;
  	  for (int i = 0; i<node_map.length; i++){
  		    for (int j = 0; j<node_map[i].length; j++){
  		    	 if (node_map[i][j]>0)
  		    		 occurences++;
  		}
  	  }
  	  return occurences;
  	}

    private int countNonZero(double[][] node_map,int column) {
     	  int occurences=0;
     	  for (int i = 0; i<node_map.length; i++){
     		    if (node_map[i][column]>0)
     		    			return 1;
     	  }
     	  return occurences;
     	}
    
    public double[][] getNodeMap(){
    	return this.node_map;
    }
}
