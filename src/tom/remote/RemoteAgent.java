package tom.remote;

import model.Request;
import model.ResourceMappingNF;
import model.Substrate;
import tools.ComparatorDouble;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class RemoteAgent {
    private double[] rcc;
    private double[] rlf_avg;
    private double[] rlf;
    private double[] hosted;
    private double reqLife;
    private boolean debug = false;
    private double nom_cap;
    private double avg_lifetime = 10000;
    private static RemoteAgentCommunicator remoteAgentCommunicator;

    public RemoteAgent() {
        if (remoteAgentCommunicator == null) {
            remoteAgentCommunicator = new RemoteAgentCommunicator();
            remoteAgentCommunicator.start();
        }
    }

    public double[][] placeloads(String id, double[] subNodesCap, String[] subNodesType,
                                 double[] reqNodesCap, ResourceMappingNF reqMap, double nom_cap, double[] resLife, double[] hostedReq,
                                 double avgLifetime, double reqLife, List<Substrate> substrates, Request request) {
        this.nom_cap = nom_cap;
        this.avg_lifetime = avgLifetime;
        this.reqLife = reqLife;
        HashMap<Integer, Integer> tmp = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> tmp1 = new HashMap<Integer, Integer>();  //inverse
        int ind = 0;
        for (int i = 0; i < subNodesCap.length; i++) {
            if (subNodesType[i].contentEquals("Server")) {
                tmp.put(ind, i);
                tmp1.put(i, ind);
                ind++;
            }
        }

        this.rcc = new double[tmp.size()];
        this.rlf = new double[tmp.size()];
        this.rlf_avg = new double[tmp.size()];
        this.hosted = new double[tmp.size()];

        if (debug) {
            System.out.println("+++++++++++++++++++++++++++++++");
            System.out.println("resLife: " + Arrays.toString(resLife));
            System.out.println("Req Lifetime: " + reqLife);
        }

        ind = 0;
        for (Entry<Integer, Integer> entry : tmp.entrySet()) {
            Integer nodeId = entry.getValue();
            this.rcc[ind] = subNodesCap[nodeId];
            this.rlf_avg[ind] = resLife[nodeId] / (hostedReq[nodeId] + Double.MIN_VALUE);
            this.rlf[ind] = resLife[nodeId];
            this.hosted[ind] = hostedReq[nodeId];
            ind++;
            //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
        }


        if (debug) {
            System.out.println("+++++++++++++++++++++++++++++++");
            System.out.println("this.rlf_avg: " + Arrays.toString(this.rlf_avg));
            System.out.println("rlf Lifetime: " + Arrays.toString(this.rlf));
            System.out.println("hosted: " + Arrays.toString(this.hosted));
            System.out.println("rcc: " + Arrays.toString(this.rcc));

            int stateCap = findStateCap(this.rcc);
            int stateLife = findStateLife(this.rlf_avg);

            System.out.println("cap state: " + stateCap + ", life state: " + stateLife);
        }


        List<NodeData> nodeData = new ArrayList<>();
        for (int i = 0; i < resLife.length; i++) {
            nodeData.add(new NodeData(resLife[i], hostedReq[i]));
        }

        Messages.Action action;
        try {
            action = this.remoteAgentCommunicator.sendAllocationRequest(0, substrates, nodeData, request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (debug) {
            if (action.getRequestAllocations(0).getSuccess()) {
                System.out.println("Take action: accept");
            } else {
                System.out.println("Take action: deny");
            }
        }

        if (action.getRequestAllocations(0).getSuccess()) {
            double[][] nodeMap = mapSolution(id, 1, subNodesCap, subNodesType, reqNodesCap, nom_cap, tmp1);
            return fixNodeMap(subNodesCap.length, reqNodesCap.length, nodeMap, tmp);
        }

        return new double[subNodesCap.length][reqNodesCap.length];
    }

    private double[][] fixNodeMap(int subLentgh, int reqLength, double[][] node_map, HashMap<Integer, Integer> compute) {
        double[][] nodemap = new double[subLentgh][reqLength];
        for (int i = 0; i < node_map.length; i++) {
            for (int j = 0; j < node_map[i].length; j++) {
                if (node_map[i][j] == 1) {
                    //System.out.println(j);
                    int index = compute.get(j);
                    nodemap[index][i] = 1;
                }
            }
        }

        return nodemap;
    }

    private double[][] mapSolution(String id, int action, double[] subNodesCap, String[] subNodesType,
                                   double[] reqNodesCap, double nom_cap, HashMap<Integer, Integer> map) {
        double[][] nodemap = null;
        double[][] nodemap_transformed = new double[reqNodesCap.length][this.rcc.length];

        if (debug)
            System.out.println("In Mapping: " + Arrays.toString(this.rcc));

        double[] tmp_rcc = Arrays.copyOf(this.rcc, rcc.length); // optNodeMapping1 anticollocation
        double[] tmp_rlf = Arrays.copyOf(this.rlf_avg, rlf_avg.length); // optNodeMapping1 anticollocation
/*		if (action==1)
			nodemap = optNodeMapping1(subNodesCap.length,reqNodesCap.length, subNodesCap, reqNodesCap,subNodesType,nom_cap);
		else  // optNodeMapping collocation
*/
        nodemap = greedy(subNodesCap.length, reqNodesCap.length, subNodesCap, reqNodesCap, subNodesType, nom_cap);
        //System.out.println(nodemap);
        //find new state
        for (int u = 0; u < subNodesCap.length; u++) {
            for (int i = 0; i < reqNodesCap.length; i++) {
                if (nodemap[u][i] > 0) {
                    int rcc_id = map.get(u);
                    tmp_rcc[rcc_id] -= reqNodesCap[i];
                    tmp_rlf[rcc_id] = (this.rlf[rcc_id] + reqLife) / (this.hosted[rcc_id] + 1);
                    nodemap_transformed[i][rcc_id] = 1;
                    if (debug) {
                        System.out.println("VFN : " + i + " mapped to: " + u + " withn newID: " + rcc_id);
                    }
                }
            }
        }

        if (debug) {
            System.out.println("In Mapping: new state " + findStateCap(tmp_rcc) + ":" + findStateLife(tmp_rlf));
        }


        return nodemap_transformed;
    }

    /////////////////////////////////////////////////////
    private int findStateCap(double[] rcc) {
        //double sum =  Arrays.stream(this.rcc).sum();
        //num_servers holds the number of servers with residual capacity less than the threshold
        int num_servers = checkResidualCap(rcc);

        return num_servers;
    }

    private int findStateLife(double[] rlf) {
        //double sum =  Arrays.stream(this.rcc).sum();
        //num_servers holds the number of servers with residual capacity less than the threshold
        int num_servers = checkResidualLifetime(rlf);

        return num_servers;
    }

    private int checkResidualCap(double[] tmp_rcc) {
        int num_servers = 0;


        for (int i = 0; i < this.rcc.length; i++) {
/*			if (debug) {
				System.out.println(tmp_rcc[i]+" " +nom_cap);

			}*/
            if (tmp_rcc[i] / (Double.MIN_VALUE + nom_cap) < 0.1)
                ++num_servers;
        }


        return num_servers;
    }


    private int checkResidualLifetime(double[] tmp_rlf) {
        int num_servers = 0;
        for (int i = 0; i < this.rlf_avg.length; i++) {
            //if (!(tmp_rlf[i]<0))
            if (tmp_rlf[i] > (10000)) {
                //System.out.println("aaaaaaaaaa: "+tmp_rlf[i] + " for " +i);
                ++num_servers;
            }
        }

        if (debug) {
            System.out.println("checkResidualLifetime: " + avg_lifetime + " " + num_servers);
            System.out.println("checkResidualLifetime: " + Arrays.toString(tmp_rlf));
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
                              String[] subNodesType, double nom_cap) {
        double[][] node_map = new double[subNodesNum][sizeOfSFC];
        for (int u = 0; u < node_map.length; u++)
            Arrays.fill(node_map[u], 0);
        HashMap<Integer, Double> hMap = new HashMap<Integer, Double>(); // cap id
        LinkedHashMap<Integer, Double> sortedMap = new LinkedHashMap<>();
        LinkedHashMap<Integer, Double> reSortedMap = new LinkedHashMap<>();

        ArrayList<Double> list = new ArrayList<>();


        //sort
        for (int i = 0; i < subNodes.length; i++) {
            if (!(subNodesType[i].equalsIgnoreCase("Switch"))) {
                hMap.put(i, subNodes[i]);
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

        for (int i = 0; i < sfcNodes.length; i++) {
            for (Entry<Integer, Double> sub_node : sortedMap.entrySet()) {
                if (sub_node.getValue() > sfcNodes[i]) {
                    int u = sub_node.getKey();
                    // System.out.println("[Key] : " + sub_node.getKey() + " [Value] : " + sub_node.getValue());
                    node_map[u][i] = 1.0;
                    sortedMap.put(sub_node.getKey(), (Double) (sub_node.getValue() - sfcNodes[i]));
                    break;
                }
            }
        }

        if (countNonZero(node_map) == sfcNodes.length) {
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
        for (int i = 0; i < sfcNodes.length; i++) {
            if (countNonZero(node_map, i) == 0) {
                for (Entry<Integer, Double> sub_node : reSortedMap.entrySet()) {
                    int u = sub_node.getKey();
                    //System.out.println(i + " to sub  " +u);
                    //System.out.println("[Key] : " + sub_node.getKey() + " [Value] : " + sub_node.getValue());
                    node_map[u][i] = 1.0;
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


        return node_map;

    }

    private int countNonZero(double[][] node_map) {
        int occurences = 0;
        for (int i = 0; i < node_map.length; i++) {
            for (int j = 0; j < node_map[i].length; j++) {
                if (node_map[i][j] > 0)
                    occurences++;
            }
        }
        return occurences;
    }

    private int countNonZero(double[][] node_map, int column) {
        int occurences = 0;
        for (int i = 0; i < node_map.length; i++) {
            if (node_map[i][column] > 0)
                return 1;
        }
        return occurences;
    }

    public void setCurrentPenalty(Double value) {
        remoteAgentCommunicator.sendPenalty(value);
    }

    public void stop() {
        remoteAgentCommunicator.stop();
    }

}


