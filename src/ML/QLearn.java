package ML;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import cern.jet.random.engine.MersenneTwister;

import java.util.Random;
import model.ResourceMappingNF;
import tools.ComparatorDouble;
import tools.Matrix;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;
import cern.jet.random.Uniform;

public class QLearn {
	private double[] rcc;
	private double[][] Q ;
	private double[][] U ; //replay memory
	private double futureReward=0; 
	private double reward=0;
	private int newState=-1;
	private int actions = 2;
	private int states;
	private boolean debug=false;
	private int episode=500;
	private Double penalty =0.0;
	private int counter=0;
	int  n = (int) Math.random();
	private double nom_cap;
	MersenneTwister generator = new MersenneTwister(n);
	Uniform myUniformDist = new Uniform(generator);
	private int prevAction = 1;
	private int[] lEpisodeDescision;
	
	double [][] initLinear() {
		double [] a={0.00000003, 0.00000005};
		double [] b= {0,0.0000001};

		
		 double[][]q_table= new double[actions][states] ;
		 for (int i=0; i<actions;i++) {
			 	for (int j=0;j<states;j++) {
			 		q_table[i][j]=b[i]-a[i]*j;
			 	}
		 }
		 //System.out.println(Arrays.deepToString(q_table));
		
		 return q_table;
	}
	
	double [][] initNonLinear() {
		double [] a={0.00000003, 0.0000002};
		double [] b= {0,0.0000001};
		double constant=5;

		 double[][]q_table= new double[actions][states] ;
		 for (int i=0; i<actions;i++) {
			 	for (int j=0;j<states;j++) {
			 		q_table[i][j]=(a[i]+b[i])/2*Math.exp(-1.0*j)+(b[i]-a[i])/2*Math.exp(-1.0*j/constant);
			 	}
		 }
		 //System.out.println(Arrays.deepToString(q_table));
		 //System.exit(0);
		 return q_table;
	}
	
	public QLearn(int servers) {
		 System.out.println("servers " + servers);
	     states = servers+1; //number of servers with residual capacity less than zero. All:16 none:0
		 Q =  initLinear(); //Q(s,a)-> Q[action][state] state space divided 
		 U = new double[actions][states]; //U(s,a)-> U[action][state] state space divided  //REPLAY MEMORY
		 this.lEpisodeDescision =   new int[states];
		 //into three areas /0 reject 1 prevent collocation 2  accept collocation. 
	}
	
	public double[][] placeloads(String id, double[] subNodesCap, String[] subNodesType, 
			double[] reqNodesCap, ResourceMappingNF reqMap , double nom_cap) {

		
/////////////////////////////////////////////////////////////
	//init variables -> newId - subNodeId
	this.nom_cap=nom_cap;
	HashMap<Integer,Integer> tmp = new HashMap<Integer,Integer> (); 
	HashMap<Integer,Integer> tmp1 = new HashMap<Integer,Integer> ();  //inverse
	int ind =0;
	for (int i=0;i<subNodesCap.length;i++) {
		if (subNodesType[i].contentEquals("Server")) {
			tmp.put(ind,i);
			tmp1.put(i,ind);
			ind++;
		}
	}

	this.rcc = new double[tmp.size()];

	ind =0;
	for (Entry<Integer, Integer> entry : tmp.entrySet()) { 
		this.rcc[ind]= subNodesCap[entry.getValue()];
		ind++;
		//System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue()); 
	}

/////////////////////////////////////////////////////////////
	
	//STEP 1: find segment - Observe the current state, s. 
	int state = findState(this.rcc);
	if (debug)
	System.out.println("Initial state: " + state + "  for " +id);

	//STEP 2: chose action a from state s using policy derived from Q and store for future reference
	////and get current value of the state-action value function Q(s,a) => Q_current
	int action = findMaxQH(state);
	
	if (debug) {
		String actionString="";
		if (action==0) actionString="deny"; else if (action==1) actionString="accept";
		System.out.println("Take action: " + actionString);
	}
	
	
	//STEP 3: Now we have selected an action - if action is not deny embed  -  find new state  and get reward for new state
	double[][] node_map = null;
	if (action!=0) {
		node_map = mapSolution(id,action,subNodesCap,subNodesType, reqNodesCap,nom_cap, tmp1);
		this.reward=1;
	}
	else {
		this.newState = state;
		this.reward=0;
	}
	

	if (debug) {
		System.out.println("Reward: " + this.reward);
		System.out.println("New state: " + this.newState);
	}

	
	//STEP 4: estimate the best value of the (current state) V*(s)=maxQ(s,prev_action)
	double max= RLconstants.MIN_VALUE;
	for(int i=0; i<actions; i++) {
        if (this.Q[i][state]>max) {
        	max=(this.Q[i][state]);
	     }
	}
	this.futureReward= max;

	//step 5: update replay memory U(s,a)
	replayMemory(action,state);
	
	if (debug) {
		System.out.println("MaxQ: " + this.futureReward);
		System.out.println(Arrays.deepToString(Q));
	}
	
	//step6:  if episode ends update Q values
	if (counter==episode)
		Qlearning();
	

/*	 try {
	        System.in.read();
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }*/
	 
	//////////////////////////////////////////////////

	double[][] nodemap =new double [subNodesCap.length][reqNodesCap.length];
	if (action!=0)  
		nodemap = fixNodeMap(subNodesCap.length,reqNodesCap.length,node_map,tmp );
	
	reqMap.setQ(Q);
	reqMap.setU(U);
	return nodemap;
	}
	
	
	/////////////////////////////////////////////////////
	private void replayMemory(int action, int state) { //append to qData
		//steop 5:  U(s,a)=U(s,a)+𝛼(𝑟(𝑠,𝑎)+𝛾max𝑎′𝑄(𝑠′,𝑎′)−𝑄(𝑠,𝑎))
		if (debug)
			System.out.println("Update replay memory with penalties: "+this.penalty);
		double Delta=(this.reward+this.penalty)+RLconstants.GAMMA*this.futureReward-Q[action][state];
		this.U[action][state]+=Delta;
		counter+=1;
	}
	
	/////////////////////////////////////////////////////
	private void Qlearning() { //append to qData
		
		for (int i=0;i<states;i++) {
				if (!(Q[1][i]<Q[0][i]))
						this.lEpisodeDescision[i]=1;
		}
		
		//steop 5:  Q(s,a)=𝑄(𝑠,𝑎)+𝛼(𝑟(𝑠,𝑎)+𝛾max𝑎′𝑄(𝑠′,𝑎′)−𝑄(𝑠,𝑎)) \\for all s, a
		double[][] Delta = Matrix.multiplyNum(RLconstants.ALPHA, U);
		Q=Matrix.add(Q, Delta);
		U = new double[actions][states]; //re-init U
		counter=0;
	}
	
	/////////////////////////////////////////////////////
	private double[][] fixNodeMap (int subLentgh, int reqLength, double[][] node_map,HashMap<Integer,Integer> compute) {
		
		double[][] nodemap =new double [subLentgh][reqLength];
		for(int i=0; i<node_map.length; i++) {
			for(int j=0; j<node_map[i].length; j++) {
				if (node_map[i][j]==1) {
					//System.out.println(j);
					int index = compute.get(j);
					nodemap[index][i]=1;
				}
			}
		}
		
		return nodemap;
	}
		
	private double[][]  mapSolution(String id, int action, double[] subNodesCap,String[] subNodesType,
		double[] reqNodesCap,double nom_cap, HashMap<Integer,Integer> map) {
		double[][] nodemap = null;
		double[][] nodemap_transformed = new double[reqNodesCap.length][this.rcc.length];

		if (debug)
			System.out.println("In Mapping: "+ Arrays.toString(this.rcc));
		
		double[] tmp_rcc =  Arrays.copyOf(this.rcc, rcc.length); // optNodeMapping1 anticollocation
/*		if (action==1)
			nodemap = optNodeMapping1(subNodesCap.length,reqNodesCap.length, subNodesCap, reqNodesCap,subNodesType,nom_cap);
		else  // optNodeMapping collocation
*/			nodemap = greedy(subNodesCap.length,reqNodesCap.length, subNodesCap, reqNodesCap,subNodesType,nom_cap);
		//System.out.println(nodemap);
		//find new state
		for (int u=0;u<subNodesCap.length;u++){
			for (int i=0;i<reqNodesCap.length;i++){
					if (nodemap[u][i] > 0){
						int rcc_id = map.get(u);
						tmp_rcc[rcc_id] -=reqNodesCap[i];
						nodemap_transformed[i][rcc_id]=1;
						if (debug) {
							System.out.println("VFN : " + i + " mapped to: " +  u + " withn newID: " +rcc_id);
						}
					}
			}
		}	
		
		this.newState = findState(tmp_rcc);
		
		if (debug) {
			System.out.println("In Mapping: new state " + this.newState);
		}
		

		return nodemap_transformed;
	}
	/////////////////////////////////////////////////////
	private int findState(double[] rcc){
	   	//double sum =  Arrays.stream(this.rcc).sum(); 
		//num_servers holds the number of servers with residual capacity less than the threshold 
	   	int num_servers = checkResidualCap(rcc);
	   	return num_servers;
    }

	/////////////////////////////////////////////////////FIND ACTION /////////////////////////////////////////////////////	
	//apply policy and find action that maximizes  
	private int findMaxQ(int state) {
		
		System.out.println("Q: " + Arrays.deepToString(this.Q));
		double maxQ = RLconstants.MIN_VALUE;
		int action=-1;
		double sel_prob=RLconstants.SIGMA;
		
		
		for(int i=0;i<(actions);++i) {	
			@SuppressWarnings("static-access")
			double rand =  myUniformDist.staticNextDoubleFromTo(0, 1);
			//double rv = rand.nextGaussian()*RLconstants.SIGMA;
			if(debug)
				System.out.println("In findMaxQ:checking state " + state +  " action " +i + " exploration probability: "+ rand );
			double q_val = this.Q[i][state];
			if(!(q_val<maxQ)) {
				maxQ = this.Q[i][state];
				action=i;
				if(debug)
					System.out.println("In findMaxQ:replacing " + this.Q[i][state] + " previous max " + maxQ + " action selected   " +action);
			} 
			if (rand<sel_prob) { //exploration
				action=i;
				break;
			}	
		}

		return action;
	}
	

	private int findMaxQH(int state) {
		
		System.out.println("Q: " + Arrays.deepToString(this.Q));
		double maxQ = RLconstants.MIN_VALUE;
		int action=-1;
		double sel_prob=RLconstants.SIGMA;
		
		
		for(int i=0;i<(actions);++i) {	
			@SuppressWarnings("static-access")
			double rand =  myUniformDist.staticNextDoubleFromTo(0, 1);
			//double rv = rand.nextGaussian()*RLconstants.SIGMA;
			if(debug)
				System.out.println("In findMaxQ:checking state " + state +  " action " +i + " exploration probability: "+ rand );
			double q_val = this.Q[i][state];
			if(!(q_val<maxQ)) {
				maxQ = this.Q[i][state];
				action=i;
				if(debug)
					System.out.println("In findMaxQ:replacing " + this.Q[i][state] + " previous max " + maxQ + " action selected   " +action);
			} 
			if (rand<sel_prob) { //exploration
				action=i;
				break;
			}	
		}
		
		int prevEpisodeAction=this.lEpisodeDescision[state];
		if (prevEpisodeAction!=action) {
			double hysterisis = Math.abs(this.Q[action][state]-this.Q[prevAction][state]);
			if (hysterisis<0.01) {
				action = prevAction;
			}
		
		}
		
/*		if (prevAction!=action) {
			double hysterisis = Math.abs(this.Q[action][state]-this.Q[prevAction][state]);
			if (hysterisis<0.01) {
				action = prevAction;
			}
		}*/
		
		prevAction = action;
		return action;
	}
	
	private int checkResidualCap(double[] tmp_rcc) {
		int num_servers =0;


		for (int i=0;i<this.rcc.length;i++) {
/*			if (debug) {
				System.out.println(tmp_rcc[i]+" " +nom_cap);

			}*/
			if (tmp_rcc[i]/(Double.MIN_VALUE+nom_cap)<0.1)
				++num_servers;
		}
	
		

		return num_servers;
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
/*	    try {
	        System.in.read();
	    	} catch (IOException e) {
	    	    // TODO Auto-generated catch block
	    	    e.printStackTrace();
	    	}*/
     

	
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
    	
private double[][] optNodeMapping(int subNodesNum, int sizeOfSFC, double[] subNodes, double[] sfcNodes, 
		String[] subNodesType, double nom_cap){
	double[][] node_map = new double [subNodesNum][sizeOfSFC];
	
	try {
	 	IloCplex cplex = new IloCplex();
		cplex.setParam(IloCplex.DoubleParam.TiLim, 600);
		//cplex.setParam(IloCplex.DoubleParam.ObjULim,Double.MAX_VALUE);
		//cplex.setParam(IloCplex.DoubleParam.ObjLLim,-1*Double.MAX_VALUE);
			

		/*****************************System Variables **************************************************/
		// x^i_u if instance j of NF i is installed on susbtrate node u then x^i_u=1	\
		IloNumVar[][] x = new IloNumVar[subNodesNum][];
		for (int u=0;u<subNodesNum;u++){
			x[u]=new IloNumVar[sizeOfSFC];
			for(int i=0; i< sizeOfSFC; i++){
				x[u]=cplex.numVarArray(sizeOfSFC, 0, 1, IloNumVarType.Int);
			}
		}
		
		/*****************************Objective Function **************************************************/
		IloLinearNumExpr cost = cplex.linearNumExpr();
		////////////////////////////////////////////////////////////////////////
		//It builds the first summation of the objective function//////////////
		//////////////////////////////////////////////////////////////////////
		for (int u=0;u<subNodesNum;u++){
			double cpu = 1-subNodes[u]/nom_cap;
			for(int i=0; i< sizeOfSFC; i++){
				//double mult =sfcNodes[i] -cpu;
				cost.addTerm(cpu, x[u][i]);
			}
		}
		cplex.addMinimize(cost);
		
		/*****************************Placement Constraints **************************************************/				


		// nf instance can be mapped to at most 1 substrate node 
			for (int i=0;i<sizeOfSFC;i++){
				IloLinearNumExpr assignment1 = cplex.linearNumExpr();
				for (int u=0;u<subNodesNum;u++){			
						assignment1.addTerm(1,x[u][i]);
				}
				cplex.addEq(assignment1, 1);
			}
			
			
			for (int u=0;u<subNodesNum;u++){
				IloLinearNumExpr assignment2 = cplex.linearNumExpr();
				if (subNodesType[u].equalsIgnoreCase("Switch")) {
					for (int i=0;i<sizeOfSFC;i++){
						assignment2.addTerm(1,x[u][i]);
					}
				cplex.addEq(assignment2, 0);
				}
			}
			
			/*****************************Solve **************************************************/		
			cplex.exportModel("lpex_nodemap.lp");
			long solveStartTime = System.nanoTime();
			boolean solvedOK = cplex.solve();
			long solveEndTime = System.nanoTime();
			long solveTime = solveEndTime - solveStartTime;
			System.out.println("solvedOK: " + solvedOK);

			if (solvedOK) {

					System.out.println("###################################");
					System.out.println( "Found an answer! CPLEX status: " + cplex.getStatus() + ", Time (msec): " + ((double) solveTime / 1000000.0));
					cplex.output().println("Solution value = " + cplex.getObjValue());
					System.out.println("###################################");
					

					for (int u=0;u<subNodesNum;u++){
						for (int i=0;i<sizeOfSFC;i++){
								node_map[u][i] = cplex.getValue(x[u][i]);
						}
					}	
					
			} else System.out.println("Cannot find solution");
		
		 cplex.end();
		
	} catch (IloException e) {
		System.err.println("Concert exception caught: " + e);
	}
	
		
	return 	node_map;
}
	 public void setCurrentPenalty(Double value) {
		 this.penalty = value;
	 }
	 


	
}


