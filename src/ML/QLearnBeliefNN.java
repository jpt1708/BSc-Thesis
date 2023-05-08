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
import java.util.Vector;

//import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.api.TrainingListener;
import org.deeplearning4j.rl4j.learning.Learning;
import org.deeplearning4j.rl4j.learning.sync.qlearning.QLearning;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.QLearningDiscreteDense;
import org.deeplearning4j.rl4j.network.dqn.DQNFactoryStdDense;
import org.deeplearning4j.rl4j.network.dqn.IDQN;
import org.deeplearning4j.rl4j.policy.DQNPolicy;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.util.DataManager;
//import org.deeplearning4j.ui.api.UIServer;
//import org.deeplearning4j.ui.stats.StatsListener;
//import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
//import org.deeplearning4j.ui.model.stats.StatsListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
//import org.tensorflow.Tensor;
import org.nd4j.linalg.learning.config.Adam;

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

public class QLearnBeliefNN {
	private double[] rcc;
	private double[] rlf_avg;
	private int servers;
	private final boolean debug=true;
	private Double penalty =0.0;
	private double nom_cap;
	private double avg_lifetime=10000;
	private int[][] lEpisodeDescision;
	//private UIServer uiServer = UIServer.getInstance();
	//Configure where the network information (gradients, score vs. time etc) is to be stored. Here: store in memory.
	//StatsStorage statsStorage = new InMemoryStatsStorage();         //Alternative: new FileStatsStorage(File), for saving and loading later

	//Attach the StatsStorage instance to the UI: this allows the contents of the StatsStorage to be visualized
	//StatsListener listeners = new StatsListener(statsStorage);
	private DataManager manager=null;


	public static QLearning.QLConfiguration QLearnND4J_QL =
			new QLearning.QLConfiguration(
					123,    //Random seed
					1,    //Max step By epoch
					1, //Max step
					50, //Max size of experience replay
					1,     //size of batches
					500,    //target update (hard)
					0,     //num step noop warmup
					1,   //reward scaling
					0.99,   //gamma
					10.0,    //td-error clipping
					0.1f,   //min epsilon
					10,   //num step for eps greedy anneal =>QLearnND4J_QL.getEpsilonNbStep()
					true    //double DQN
			);


	//private static TrainingListener[] listeners;
	public  DQNFactoryStdDense.Configuration RANET =
			DQNFactoryStdDense.Configuration.builder()
					//.listeners(listeners)
					.l2(0.01).updater(new Adam(1e-2))
					.numLayer(3).numHiddenNodes(16).build();
	
	
	
	public QLearnBeliefNN(int servers) throws IOException {
		this.servers=servers;
		// System.out.println("servers " + this.servers);
		manager = new DataManager(true);
		if (manager == null) {
			throw new IllegalArgumentException("object was null");
		}
		//Initialize the user interface backend
		//uiServer.attach(statsStorage);


	//	DQNFactoryStdDense net = new DQNFactoryStdDense(RANET);
		//net.
		//net.init();

		//net.setListeners(new StatsListener(ss), new ScoreIterationListener(1))


	}
	
	public double[][] placeloads(String id, double[] subNodesCap, String[] subNodesType, 
			double[] reqNodesCap, ResourceMappingNF reqMap , double nom_cap, double[] resLife, double[] hostedReq, 
			double avgLifetime, double reqLife) throws IOException {

		
/////////////////////////////////////////////////////////////
	//init variables -> newId - subNodeId
		//update substrate
		this.nom_cap=nom_cap;
		this.avg_lifetime=avgLifetime;
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
		double[] rlf = new double[tmp.size()];
		this.rlf_avg = new double[tmp.size()];
		double[] hosted = new double[tmp.size()];

		if (debug) {
			System.out.println("+++++++++++++++++++++++++++++++");
			System.out.println("resLife: "+Arrays.toString(resLife));
			System.out.println("Req Lifetime: "+reqLife);
		}

		ind =0;
		for (Entry<Integer, Integer> entry : tmp.entrySet()) {
			Integer nodeId=entry.getValue();
			this.rcc[ind]= subNodesCap[nodeId];
			this.rlf_avg[ind] = resLife[nodeId]/(hostedReq[nodeId]+Double.MIN_VALUE);
			rlf[ind] = resLife[nodeId];
			hosted[ind]=hostedReq[nodeId];
			ind++;
			//System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
		}


		if (debug) {
			System.out.println("+++++++++++++++++++++++++++++++");
			System.out.println("Request: " + id);
			System.out.println("this.rlf_avg: "+Arrays.toString(this.rlf_avg));
			System.out.println("rlf Lifetime: "+Arrays.toString(rlf));
			System.out.println(" hosted: "+Arrays.toString(hosted));
		}

		EmbeddingProblem mdp = new EmbeddingProblem();
		mdp.setSubstrate(rcc, rlf, Double.MAX_VALUE, 0, servers);
		if (mdp == null) {
			throw new IllegalArgumentException("object was null");
		}


		Learning<ObservedState, Integer, DiscreteSpace, IDQN> dql =
				new QLearningDiscreteDense<>(mdp, RANET, QLearnND4J_QL, this.manager);
		//this.mdp.setFetchable(dql);
		System.out.println("QLearnBeliefNN: Fixed NN: OK");

		mdp.setSubstrate(this.rcc, this.rlf_avg, this.nom_cap, this.avg_lifetime, this.servers);
		mdp.setInfo(reqNodesCap, rlf, subNodesCap, subNodesType, hosted, tmp1, reqLife);
 		//RANET.getListeners().setListeners(new StatsListener(statsStorage));
/////////////////////////////////////////////////////////////
		int stateCap = findStateCap(this.rcc);
		int stateLife = findStateLife(this.rlf_avg);
	
		if (debug) {
		System.out.println("Initial state before train: " + stateCap +":" +stateLife + "  for " +id );
		}
		dql.train();
		// get the final policy
		DQNPolicy<ObservedState> pol = (DQNPolicy<ObservedState>) dql.getPolicy();
		// serialize and save (serialization showcase, but not required)
		pol.save("dqn.policy");
		//double reward = pol.play(mdp);
		//System.out.println(manager.getStat() + " reward " +reward);
		double[][] node_map = mdp.getNodeMap();
			System.out.println("Pausing program");
			try {
		        System.in.read();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }
		double[][] nodemap =new double [subNodesCap.length][reqNodesCap.length];
		if (node_map!=null) {
			nodemap = fixNodeMap(subNodesCap.length, reqNodesCap.length, node_map, tmp);
	}

	return nodemap;
	}
	
	
/////////////////////////////////////////////////////
	
	
	
	/////////////////////////////////////////////////////
	private double[][] fixNodeMap (int subLentgh, int reqLength, 
			double[][] node_map,HashMap<Integer,Integer> compute) {
		
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

	private int findStateCap(double[] rcc){
	   	//double sum =  Arrays.stream(this.rcc).sum(); 
		//num_servers holds the number of servers with residual capacity less than the threshold 
	   	int num_servers = checkResidualCap(rcc);
	   	
	   	return num_servers;
    }

	private int findStateLife(double[] rlf){
	   	//double sum =  Arrays.stream(this.rcc).sum(); 
		//num_servers holds the number of servers with residual capacity less than the threshold 
	   	int num_servers = checkResidualLifetime(rlf);
	   	
	   	return num_servers;
    }
	/////////////////////////////////////////////////////FIND ACTION /////////////////////////////////////////////////////	
	//apply policy and find action that maximizes  

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
	
	
	private int checkResidualLifetime(double[] tmp_rlf) {
		int num_servers =0;
		for (int i=0;i<this.rlf_avg.length;i++) {
			//if (!(tmp_rlf[i]<0))
			if (tmp_rlf[i]>(10000)) {
				//System.out.println("aaaaaaaaaa: "+tmp_rlf[i] + " for " +i);
				++num_servers;
				}
		}
		
		if (debug) {
			System.out.println("checkResidualLifetime: "+ avg_lifetime+ " " +num_servers);
			System.out.println("checkResidualLifetime: "+Arrays.toString(tmp_rlf));
		}
/*		 try {
		        System.in.read();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }*/
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


