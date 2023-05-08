package tom.model;


import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;
import model.AlgorithmMCFsimple;
import model.Request;
import model.ResourceMappingNF;
import model.Substrate;
import model.components.Link;
import model.components.NF;
import model.components.Node;
import model.components.Server;
import monitoring.Monitor;
import org.apache.commons.collections15.multimap.MultiHashMap;
import org.apache.commons.io.output.NullWriter;
import tools.ComparatorDouble;
import tools.LinkComparator;
import tools.NodeComparator;

import java.io.*;
import java.util.*;

public abstract class Algorithm {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    private final String id;
    private String state;
    private Substrate InPs;
    private List<Substrate> substrates;
    private List<Request> requests;
    private List<Node> nfs;
    private int ts = 0;
    private double[] subNodesLife;
    private double[] subNodesHostedReq;
    private List<Request> active = new ArrayList<>();
    private Monitor monitor;

    public Algorithm(String id) {
        this.id = id;
        this.state = "available";
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setInPs(Substrate inPs) {
        InPs = inPs;
    }

    public void setRequests(List<Request> requests) {
        this.requests = requests;
    }

    public void setNfs(List<Node> nfs) {
        this.nfs = nfs;
    }

    public void setTs(int ts) {
        this.ts = ts;
    }

    public void setSubNodesLife(double[] subNodesLife) {
        this.subNodesLife = subNodesLife;
    }

    public void setSubNodesHostedReq(double[] subNodesHostedReq) {
        this.subNodesHostedReq = subNodesHostedReq;
    }

    public void setActive(List<Request> active) {
        this.active = active;
    }

    public void setMonitor(Monitor monitor) {
        this.monitor = monitor;
    }

    public String getId() {
        return id;
    }

    public String getState() {
        return state;
    }

    public Substrate getInPs() {
        return InPs;
    }

    public List<Substrate> getSubstrates() {
        return substrates;
    }

    public List<Request> getRequests() {
        return requests;
    }

    public List<Node> getNfs() {
        return nfs;
    }

    public int getTs() {
        return ts;
    }

    public double[] getSubNodesLife() {
        return subNodesLife;
    }

    public double[] getSubNodesHostedReq() {
        return subNodesHostedReq;
    }

    public List<Request> getActive() {
        return active;
    }

    public Monitor getMonitor() {
        return monitor;
    }

    public boolean runAlgorithm(ArrayList<Request> active, int time) {
        this.active = active;
        this.ts = time;

        return runAlgorithm();
    }

    public boolean runAlgorithm() {
        // String path = "results/" + this.id;
        // File filePath = new File(path);
        // if (!filePath.exists() && !filePath.mkdirs()) {
        //     throw new RuntimeException("Could not create results directory");
        // }

        // long now = System.currentTimeMillis();
        // String substrateFilePath = path + File.separator + "substrate_" + this.id + "_" + now + ".txt";
        // String requestsFilePath = path + File.separator + "requests_" + this.id + "_" + now + ".txt";

//        try (Writer subWriter = new BufferedWriter(new FileWriter(substrateFilePath));
//             Writer reqWriter = new BufferedWriter(new FileWriter(requestsFilePath))) {
        try (Writer subWriter = new NullWriter();
             Writer reqWriter = new NullWriter()) {
            //log all results - substrate snapshot + request/mapping after running the algorithm
            //for every request attempt mapping

            for (Request request : requests) {
                long solveStartTime = System.nanoTime();
                ResourceMappingNF reqMap = new ResourceMappingNF(request);
                //analyze substrate into arrays for cplex
                Substrate substrateCopy = substrates.get(0);
                Graph<Node, Link> sub = substrateCopy.getGraph();
                ArrayList<Node> subNodesList = getNodes(substrateCopy.getGraph());
                ArrayList<Link> subLinksList = getLinks(substrateCopy.getGraph());
                subWriter.write("Request: " + request.getId() + " \n");

                for (Node x : subNodesList) {
                    subWriter.write("Node: " + x.getId() + " Type: " + x.getType() + " CPU: " + x.getAvailableCpu() + "\n");
                }

                //Adjacency Matrix Substrate
                for (Link y : subLinksList) {
                    Pair<Node> tmp = sub.getEndpoints(y);
                    subWriter.write("Link " + y.getId() + " : " + tmp.getFirst().getId() + " -> " + tmp.getSecond().getId() + " BW: " +
                            y.getBandwidth() + "\n");
                }
                //analyze request into arrays for cplex

                int sfcNodes_num = request.getGraph().getVertexCount();
                double[] sfcNodes = new double[sfcNodes_num];
                //estimate revenue for request
                double proc_revenue = 0;

                reqWriter.write("Request: " + request.getId() + " \n");
                ArrayList<Node> req_n = getNodes(request.getGraph());
                for (Node node : req_n) {
                    sfcNodes[node.getId()] = node.getAvailableCpu();
                    reqWriter.write("Node: " + node.getId() + " CPU: " + node.getAvailableCpu() + "\n");
                    proc_revenue += node.getAvailableCpu();
                }


                double[][] sfcLinks = new double[sfcNodes_num][sfcNodes_num]; //replace witn max

                double bw_revenue = 0;
                ArrayList<Link> links = getLinks(request.getGraph());
                //double [] reqLinks = new double [links.size()];
                for (Link y : links) {
                    Pair<Node> tmp = request.getGraph().getEndpoints(y);
                    sfcLinks[tmp.getFirst().getId()][tmp.getSecond().getId()] = y.getBandwidth();
                    //reqLinks[y.getId()]  = y.getBandwidth();
                    reqWriter.write("Link " + y.getId() + " : " + y.getId() + " : " + tmp.getFirst().getId() + " -> " + tmp.getSecond().getId() + " BW: " +
                            y.getBandwidth() + "\n");
                    bw_revenue += y.getBandwidth();
                }

                ArrayList<Node> sfc = request.getNodes(request.getGraph());
                //redundant double naming... to lazy to change
                //set revenue to reqMap
                reqMap.setEmbeddingRevenue(bw_revenue + proc_revenue);

                System.out.println("Request: " + request.getId());
                System.out.println("sfcNodes: " + Arrays.toString(sfcNodes));
                System.out.println("sfcLinks: " + Arrays.deepToString(sfcLinks));

                double[][] node_map = allocateRequest(request, reqMap);

                LinkedHashMap<Node, Node> nodeMap = new LinkedHashMap<>();
                double embedding_cost = 0;
                double cpu_cost = 0;
                boolean violation = false;
                System.out.println("IN RL MAPPING for req " + request.getId());
                for (int u = 0; u < subNodesList.size(); u++) {
                    for (int i = 0; i < sfc.size(); i++) {
                        if (node_map[u][i] > 0) {
                            if (countNonZero(node_map) == sfcNodes.length) {
                                cpu_cost += req_n.get(i).getAvailableCpu();
                                nodeMap.put(req_n.get(i), subNodesList.get(u));
                                req_n.get(i).setAvailableAllocatedCpu(req_n.get(i).getAvailableCpu());
                                double updatedValue = (updateSubstrateNode(substrateCopy, subNodesList.get(u), req_n.get(i).getAvailableCpu(), req_n.get(i)));
                                if (updatedValue == Double.MAX_VALUE)
                                    throw new ArithmeticException("Substrate Node Capacity not updated");
                                else if (updatedValue < 0) {
                                    System.out.println("Violations not allowed");
                                    // System.exit(0);
                                    violation = true;
                                }
                            }
                            // System.out.println("VNF  " + i + " to node" + u +"  " +node_map[u][i] + " " + updatedValue + " " + violation);
                        }
                    }
                }

                embedding_cost += cpu_cost;

                boolean denied = true;
                if (!nodeMap.isEmpty()) {
                    //link mapping requested link - substrate path as list of links
                    MultiHashMap<HashMap<Pair<Integer>, Double>, List<Pair<Integer>>> lmap;
                    AlgorithmMCFsimple mcf = new AlgorithmMCFsimple(node_map, substrateCopy, request, nodeMap);
                    mcf.RunMCF();

                    lmap = mcf.getLinkMapping();
                    if (!lmap.isEmpty()) {
                        denied = false;

                        double bw_cost = mcf.getBWcost();
                        System.out.println("bw_cosL " + bw_cost + " cpu_cost " + cpu_cost);
                        embedding_cost += bw_cost;

                        reqMap.setNodeMapping(nodeMap);
                        reqMap.setFlowMapping(lmap);
                        reqMap.accepted();
                        reqMap.setEmbeddingCost(embedding_cost);
                        reqMap.setCPUCost(cpu_cost);
                        reqMap.setBWCost(bw_cost);
                        reqMap.setOverpCPU(violation);
                        if (violation) {
                            System.out.println(ANSI_RED + "VIOLATION!!!!!!!!" + ANSI_RESET);
                        }
                        reqMap.setServersUsed(checkCollocation(nodeMap, subNodesList));
                        //req.setRMapNF(reqMap);
                        System.out.println("###############################################");
                        System.out.println(embedding_cost + " " + (bw_revenue + proc_revenue));
                        System.out.println("Node Mapping: " + nodeMap);
                        System.out.println(lmap);
                        System.out.println("###############################################");
                        reqWriter.write("Node Mapping: " + nodeMap + "\n");
                        reqWriter.write("Link Mapping: " + lmap + "\n");
                    }
                }

                if (denied) {
                    System.out.println("Did not found an answer for Mapping");
                    reqMap.denied();
                    //	req.setRMapNF(reqMap);
                }

                long solveEndTime = System.nanoTime();
                long solveTime = solveEndTime - solveStartTime;
                double solTime = (double) solveTime / 1000000.0;
                reqMap.setSolTime(solTime);
                request.setRMapNF(reqMap);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }


    public ArrayList<Node> getNodes(Graph<Node, Link> t) {
        ArrayList<Node> reqNodes = new ArrayList<>(t.getVertices());
        reqNodes.sort(new NodeComparator());

        return reqNodes;
    }

    protected ArrayList<Link> getLinks(Graph<Node, Link> t) {
        ArrayList<Link> links = new ArrayList<>(t.getEdges());
        links.sort(new LinkComparator());

        return links;
    }

    public List<Node> getNFs(Graph<Node, Link> t) {
        ArrayList<Node> reqNFs = new ArrayList<>();

        for (Node x : t.getVertices()) {
            if (((x) instanceof Server) || ((x) instanceof NF))
                reqNFs.add(x);
        }

        return reqNFs;
    }

    public Node getSimilarNF(Node vnf, ArrayList<Node> nfList) {
        System.out.println("Checking vnf: " + vnf.getName().replaceAll("\\d", ""));
        for (Node x : nfList) {
            System.out.println("nf " + x.getName().replaceAll("\\d", ""));
            if (x.getName().replaceAll("\\d", "").equalsIgnoreCase(vnf.getName().replaceAll("\\d", "")))
                return x;
        }

        return null;
    }

    public boolean exists(Collection<Link> coll, Node node, Graph<Node, Link> g) {
        //System.out.println("checking existance: " + node.getId());
        for (Link elem : coll) {
            Pair<Node> currentNodes = g.getEndpoints(elem);
            //System.out.println("checking currentNodes: " + currentNodes);
            if (currentNodes.contains(node)) {
                // System.out.println("Found");
                return true;
            }
        }
        return false;
    }

    public Link getLink(Node a, Node b, Collection<Link> coll, HashMap<Integer, Pair<Node>> linkEPs) {
        //	System.out.println("checking Link: " + a.getId()+ " _  " + b.getId());
        for (Link elem : coll) {
            //  System.out.println("checking Link: " + elem.getId());
            Pair<Node> currentNodes = linkEPs.get(elem.getId());
            //System.out.println(currentNodes);
            if (currentNodes.getFirst().getId() == a.getId()) {
                //	 System.out.println("checking Node F: " + currentNodes.getFirst().getId());
                if (currentNodes.getSecond().getId() == b.getId()) {
                    //	System.out.println("checking Node S: " + currentNodes.getSecond().getId());
                    return elem;
                }
            }
        }

        return null;
    }

    public Link getLink(int a, int b, Collection<Link> coll, HashMap<Integer, Pair<Node>> linkEPs) {
        //System.out.println(a + "  " +b );
        for (Link elem : coll) {
            Pair<Node> currentNodes = linkEPs.get(elem.getId());
            // System.out.println("currentNodes " +currentNodes );
            if (currentNodes.getFirst().getId() == a) {
                if (currentNodes.getSecond().getId() == b) {
                    return elem;
                }
            }
        }

        return null;
    }

    public Node getSimilarVNF(Node nf, ArrayList<Node> vnfList) {
        for (Node x : vnfList) {
            if (x.getName().replaceAll("\\d", "").equalsIgnoreCase(nf.getName().replaceAll("\\d", "")))
                return x;
        }

        return null;
    }

    public boolean checkType(Node node, Node vnf) {
        if (node.getType().equalsIgnoreCase(vnf.getType())) {
            return true;
        } else if (node.getType().equalsIgnoreCase("Server") && vnf.getType().equalsIgnoreCase("NF")) {
            return true;
        }
        return false;
    }

    private boolean updateSubLinkPathlets(Substrate sub, int src, int dst, double cap) {
        for (Link x : sub.getGraph().getEdges()) {
            Pair<Node> eps = sub.getGraph().getEndpoints(x);
            if ((eps.getFirst().getId() == src) && (eps.getSecond().getId() == dst)) {
                //System.out.println("Found edge: " + src+ " -> " +dst + " for " + eps.getFirst().getId()+" -> "+eps.getSecond().getId());
                double newCap = x.getBandwidth() - cap;
                if ((newCap < 0.1) && (newCap > 0)) {
                    System.out.println("Found edge: " + src + " -> " + dst + " for " + eps.getFirst().getId() + " -> " + eps.getSecond().getId());
                    System.out.println(x.getBandwidth() + " cap: " + cap + " " + newCap);
                }
                x.setBandwidth((int) newCap);
                //System.out.println(newCap);
                return true;
            }
        }

        return false;
    }

    protected boolean updateSubLink(Substrate sub, int src, int dst, double cap) {
        for (Link x : sub.getGraph().getEdges()) {
            Pair<Node> eps = sub.getGraph().getEndpoints(x);
            if ((eps.getFirst().getId() == src) && (eps.getSecond().getId() == dst)) {
                //System.out.println("Found edge: " + src+ " -> " +dst + " for " + eps.getFirst().getId()+" -> "+eps.getSecond().getId());
                double newCap = x.getBandwidth() - cap;
                if ((newCap < 0.1) && (newCap > 0)) {
                    System.out.println("Found edge: " + src + " -> " + dst + " for " + eps.getFirst().getId() + " -> " + eps.getSecond().getId());
                    System.out.println(x.getBandwidth() + " cap: " + cap + " " + newCap);
                }
                //  if (eps.getFirst().getType().equalsIgnoreCase("switch") ){
                // 		updateSubstrateTCAM(sub, eps.getFirst());
                // }
                x.setBandwidth((int) newCap);
                //System.out.println(newCap);
                return true;
            }
        }

        return false;

    }

    private boolean updateSubLinkRegular(Substrate sub, int src, int dst, double cap) {
        for (Link x : sub.getGraph().getEdges()) {
            Pair<Node> eps = sub.getGraph().getEndpoints(x);
            if ((eps.getFirst().getId() == src) && (eps.getSecond().getId() == dst)) {
                //System.out.println("Found edge: " + src+ " -> " +dst + " for " + eps.getFirst().getId()+" -> "+eps.getSecond().getId());
                double newCap = x.getBandwidth() - cap;
                if ((newCap < 0.1) && (newCap > 0)) {
                    System.out.println("Found edge: " + src + " -> " + dst + " for " + eps.getFirst().getId() + " -> " + eps.getSecond().getId());
                    System.out.println(x.getBandwidth() + " cap: " + cap + " " + newCap);
                }
                x.setBandwidth((int) newCap);
                //System.out.println(newCap);
                return true;
            }
        }

        return false;

    }

    protected boolean updateSubstrate(Substrate sub, Node node, double cap) {
        for (Node x : sub.getGraph().getVertices()) {
            if (x.getId() == node.getId()) {
                // System.out.println("In reserve: " + x.getId()+ " "+x.getAvailableCpu() +" " +cap );
                double capNew = (x.getAvailableCpu() - cap);
                //  System.out.println((int)capNew);
                x.setAvailableCpu((int) capNew);
                //  if (capNew<0) System.exit(0);
                return true;
            }

        }
        return false;
    }

    protected double updateSubstrateNode(Substrate sub, Node node, double cap, Node nf) {
        for (Node x : sub.getGraph().getVertices()) {
            if (x.getId() == node.getId()) {
                //System.out.println(x.getId()+ " " +cap + " "+x.getAvailableCpu());
                double capNew = (x.getAvailableCpu() - cap);
                double capNew2 = (x.getAvailableAllocatedCpu() - cap);
                //System.out.println((int)capNew);
                x.setAvailableAllocatedCpu((int) capNew2);
                x.setAvailableCpu((int) capNew);
                x.adNF(nf);

                return capNew;
            }

        }
        return Double.MAX_VALUE;
    }

    private boolean updateSubstrateTCAMid(Substrate sub, int node) {
        for (Node x : sub.getGraph().getVertices()) {
            if (x.getId() == node) {
                System.out.println(x.getId() + "  " + x.getTCAM());
                if (x.getTCAM() > 0) {
                    int capNew = x.getTCAM() - 1;
                    //System.out.println((int)capNew);
                    x.setTCAM(capNew);
                    return true;
                } else
                    return false;
            }

        }
        return false;
    }

    private boolean updateSubstrateTCAM(Substrate sub, Node node) {
        for (Node x : sub.getGraph().getVertices()) {
            if (x.getId() == node.getId()) {
                // System.out.println(x.getId()+ " " +cap + " "+x.getAvailableCpu());
                int capNew = x.getTCAM() - 1;
                //System.out.println((int)capNew);
                x.setTCAM(capNew);
                return true;
            }

        }
        return false;
    }

    private boolean updateSubstrateTCAMRegular(Substrate sub, int node) {
        for (Node x : sub.getGraph().getVertices()) {
            if (x.getId() == node) {
                if (x.getType().equalsIgnoreCase("switch")) {
                    // System.out.println(x.getId()+ " " +cap + " "+x.getAvailableCpu());
                    int capNew = x.getTCAM() - 1;
                    //System.out.println((int)capNew);
                    x.setTCAM(capNew);
                    return true;
                }
            }
        }
        return false;
    }


    public boolean checkFeasibility(double[][][][] xVarFinal, int subNodesNum, int subNodesNumAug, int nodesSFC,
                                    double[][] sfcLinks, double[] subCapAug, double[] sfcNodes) {
        //placement 1
        System.out.println(subNodesNum + " " + subNodesNumAug + " " + nodesSFC);

        for (int p = subNodesNum; p < subNodesNumAug; p++) {
            for (int m = 0; m < nodesSFC; m++) {
                double sum = 0;
                if (sfcLinks[p - subNodesNum][m] != 0) {
                    for (int w = 0; w < subNodesNum; w++) {
                        sum = sum + xVarFinal[p][w][p - subNodesNum][m];
                    }
                    if (sum != 1) return false;
                }
            }
        }

        //node capacity constraints (2)
        for (int v = 0; v < subNodesNum; v++) {
            double cpuCap = subCapAug[v];
            double cpuReq = 0;
            ArrayList<Integer> visited = new ArrayList<Integer>();
            ArrayList<Integer> visited_rev = new ArrayList<Integer>();
            for (int m = 0; m < nodesSFC; m++) {
                for (int u = subNodesNum; u < subNodesNumAug; u++) {
                    if (sfcLinks[u - subNodesNum][m] != 0) {
                        if (!(visited.contains(u - subNodesNum))) {
                            //System.out.println("x" + u + "  " +v + " " + (u-subNodesNum) + " " +m);
                            cpuReq = sfcNodes[u - subNodesNum] * xVarFinal[u][v][u - subNodesNum][m];
                            visited.add(u - subNodesNum);
                        }
                    } else if (sfcLinks[m][u - subNodesNum] != 0) {
                        if (!(visited.contains(u - subNodesNum))) {
                            //System.out.println("x1 " + v + "  " +u + " " + m + " " +(u-subNodesNum));
                            cpuReq = sfcNodes[u - subNodesNum] * xVarFinal[v][u][m][u - subNodesNum];
                            visited.add(u - subNodesNum);
                        }
                    }
                }
            }
            //System.out.println("lEQ "+cpuCap);
            if (Double.compare(cpuReq, cpuCap) > 0) return false;
        }

        return true;
    }


    public Node getNodeById(ArrayList<Node> nodes, int id) {
        for (Node x : nodes) {
            if (x.getId() == id)
                return x;
        }

        return null;
    }

    public static void printMap(Map mp) {
        for (Object o : mp.entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            Node v = (Node) pair.getKey();
            Node s = (Node) pair.getValue();
            System.out.println(v.getId() + ":" + v.getName() + " = " + s.getId() + ":" + s.getName());
            if ((v.getType().contentEquals("RRH")) || v.getType().contentEquals("Router")) {
                System.out.println(v.getType() + " " + v.getCoords().getX() + " " + v.getCoords().getY());
                System.out.println(s.getType() + " " + s.getCoords().getX() + " " + s.getCoords().getY());
            }
        }
    }

    protected void getResLifetime(ArrayList<Node> subNodesList) {
        subNodesLife = new double[subNodesList.size()];
        subNodesHostedReq = new double[subNodesList.size()];
        for (Node curr : subNodesList) {
            double life = 0;
            double reqHosted = 0;
            for (Request act : active) {
                if (act.getRMapNF().containsNodeInMap(curr)) {
                    life = life + (act.getEndDate() - this.ts);
                    reqHosted++;
                }
            }
            subNodesLife[curr.getId()] = life;
            subNodesHostedReq[curr.getId()] = reqHosted;
        }
        System.out.println("AlgorithmNF: " + Arrays.toString(subNodesLife));
        System.out.println("AlgorithmNF: " + Arrays.toString(subNodesHostedReq));
    }

    protected int checkCollocation(LinkedHashMap<Node, Node> nodeMap, ArrayList<Node> subNodesList) {
        int servers = 0;
        for (Node current : subNodesList) {
            if (nodeMap.containsValue(current))
                servers++;
        }
        return servers;
    }

    private double[][] greedy(int subNodesNum, int sizeOfSFC, double[] subNodes, double[] sfcNodes,
                              String[] subNodesType, double nom_cap) {
        double[][] node_map = new double[subNodesNum][sizeOfSFC];
        for (double[] doubles : node_map) Arrays.fill(doubles, 0);
        HashMap<Integer, Double> hMap = new HashMap<Integer, Double>(); // cap id
        LinkedHashMap<Integer, Double> sortedMap = new LinkedHashMap<>();
        LinkedHashMap<Integer, Double> reSortedMap = new LinkedHashMap<>();
        ArrayList<Double> list = new ArrayList<>();


        //sort
        for (int i = 0; i < subNodes.length; i++) {
            if (!(subNodesType[i].equalsIgnoreCase("Switch"))) {
                hMap.put(i, subNodes[i]);
                System.out.println("subNodes: " + subNodes[i] + " " + i);
            }

        }


        for (Map.Entry<Integer, Double> entry : hMap.entrySet()) {
            list.add(entry.getValue());
        }
        System.out.println(list);
        Collections.sort(list, new ComparatorDouble());
        Collections.reverse(list);
        for (Double str : list) {
            for (Map.Entry<Integer, Double> entry : hMap.entrySet()) {
                if (entry.getValue().equals(str)) {
                    sortedMap.put(entry.getKey(), str);
                }
            }
        }
        System.out.println(sortedMap);

        for (int i = 0; i < sfcNodes.length; i++) {
            for (Map.Entry<Integer, Double> sub_node : sortedMap.entrySet()) {
                if (!(sfcNodes[i] > sub_node.getValue())) {
                    int u = sub_node.getKey();
                    System.out.println("[Key] : " + sub_node.getKey() + " [Value] : " + sub_node.getValue() + " " + sfcNodes[i]);
                    node_map[u][i] = 1.0;
                    sortedMap.put(sub_node.getKey(), (Double) (sub_node.getValue() - sfcNodes[i]));
                    break;
                }
            }
        }

        return node_map;

    }

    protected int countNonZero(double[][] node_map) {
        int occurences = 0;
        for (double[] doubles : node_map) {
            for (double aDouble : doubles) {
                if (aDouble > 0)
                    occurences++;
            }
        }
        return occurences;
    }

    protected abstract double[][] allocateRequest(Request request, ResourceMappingNF reqMap);

    public void clean() {

    }

    public void setSubstrates(List<Substrate> substrates) {
        this.substrates = substrates;
    }
}


