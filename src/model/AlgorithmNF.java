package model;

import java.io.BufferedWriter;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;

import org.apache.commons.collections15.multimap.MultiHashMap;

import ML.QLearn;
import ML.QLearnBelief;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;

import org.apache.commons.io.output.NullWriter;
import tom.remote.RemoteAgent;
import tools.ComparatorDouble;
import tools.LinkComparator;
import tools.NodeComparator;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;
import model.components.Link;
import model.components.NF;
import model.components.Node;
import model.components.Server;
import monitoring.Monitor;

/**
 * DUMMY Algorithm Class.
 */

public class AlgorithmNF {


    private String id;
    private String state;
    private Substrate InPs;
    private static List<Substrate> substrates ;
    private List<Request> reqs;
    private List<Node> nfs;
    private  static int  MAX_TYPES = 3;
    private double thr_rl=0;
    private QLearnBelief ql6;
    private QLearn ql;
    private RemoteAgent ra;
    private double[] subNodesLife;
    private double[] subNodesHostedReq;
    private int ts = 0;
    ArrayList<Request> active = new ArrayList<Request>();
    private Monitor magent;
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    public AlgorithmNF(String id,Substrate sub) {
        this.id = id;
        this.state = "available";
        if  (this.id.contains("RLb")){
            this.ql6 =  new QLearnBelief(sub.getNumServers());
        } else if  (this.id.contains("RL")){
            this.ql =  new QLearn(sub.getNumServers());
        } else if (this.id.contains("RA")) {
            this.ra = new RemoteAgent();
        }

    }

    public AlgorithmNF(String id) {
        this.id = id;
        this.state = "available";
    }


    public void setStateReq(double thr_rl) {
        this.thr_rl = thr_rl;
    }

    public double getStateReq() {
        return this.thr_rl;
    }


    private QLearnBelief getQL6() {
        return this.ql6;
    }

    private QLearn getQL() {
        return this.ql;
    }

    private RemoteAgent getRA() {
        return this.ra;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }


    public void setState(String state) {
        this.state = state;
    }

    @SuppressWarnings("static-access")
    public void addSubstrate(List<Substrate> substrates) {
        this.substrates = substrates;
    }

    public void addNFs(List<Node> nfs) {
        this.nfs=nfs;
    }

    public void addRequests(List<Request> reqs) {
        this.reqs = reqs;
    }

    public void addInPs(Substrate InPs){
        this.InPs = InPs;
    }


    public boolean runAlgorithm (ArrayList<Request> active, int time){
        System.out.println("AlgorithmNF.java 151 runAlgorithm, " + id);
        boolean retres = false;
        this.active=active;
        this.ts = time;
        if  (this.id.contains("MILP")){
            retres=NFplacement_simple();
        }
        else if  (this.id.contains("ILP1")){
            //this.ql.setThreshold(this.thr_rl);
            retres=NFplacement_ILP1();

        }
        else if  (this.id.contains("ILP")){
            retres=NFplacement_ILP();

        }
        else if  (this.id.contains("RL") || this.id.contains("RA")) {
            //this.ql.setThreshold(this.thr_rl);
            retres=NFplacement_RL();

        }
        else if  (this.id.contains("Greedy")){
            //this.ql.setThreshold(this.thr_rl);
            retres=greedy();
        }

        return retres;
    }

    public boolean runAlgorithm (){
        boolean retres = false;

        if  (this.id.contentEquals("MILP")){
            retres=NFplacement_simple();
        }

        else if  (this.id.contentEquals("ILP")){
            retres=NFplacement_ILP();

        }
        else if  (this.id.contentEquals("RL") || this.id.contains("RA")){
            //this.ql.setThreshold(this.thr_rl);
            retres=NFplacement_RL();

        }
        else if  (this.id.contentEquals("RLdyn")){
            //this.ql.setThreshold(this.thr_rl);
            retres=NFplacement_RL();
        }
        else if  (this.id.contentEquals("ILP1")){
            //this.ql.setThreshold(this.thr_rl);
            retres=NFplacement_ILP1();

        }
        else if  (this.id.contentEquals("Greedy")){
            //this.ql.setThreshold(this.thr_rl);
            retres=greedy();

        }

        return retres;
    }

    public static void waiting (int n){

        long t0, t1;

        t0 =  System.currentTimeMillis();

        do{
            t1 = System.currentTimeMillis();
        }

        while (t1 - t0 < n);
    }


    @SuppressWarnings("unchecked")
    public ArrayList<Node> getNodes(Graph<Node,Link> t) {
        ArrayList<Node> reqNodes =new ArrayList<Node>();

        for (Node x: t.getVertices())
            reqNodes.add(x);

        Collections.sort(reqNodes,new NodeComparator());

        return reqNodes;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<Link> getLinks(Graph<Node, Link> t) {
        ArrayList<Link> links =new ArrayList<Link>();
        Collection<Link> edges =  t.getEdges();

        for (Link current : edges){
            links.add(current);
        }

        Collections.sort(links,new LinkComparator());

        return links;
    }

    public List<Node> getNFs(Graph<Node,Link> t) {
        ArrayList<Node> reqNFs =new ArrayList<Node>();

        for (Node x: t.getVertices()){
            if (((x) instanceof Server  )  || ((x) instanceof NF  ) )
                reqNFs.add(x);
        }

        return reqNFs;
    }

    public Node getSimilarNF(Node vnf, ArrayList<Node> nfList) {
        System.out.println("Checking vnf: " +vnf.getName().replaceAll("\\d",""));
        for (Node x: nfList){
            System.out.println("nf " + x.getName().replaceAll("\\d","")  );
            if (x.getName().replaceAll("\\d","").equalsIgnoreCase(vnf.getName().replaceAll("\\d","")))
                return x;
        }

        return null;
    }

    public boolean exists(Collection<Link> coll, Node node, Graph<Node,Link> g){
        //System.out.println("checking existance: " + node.getId());
        Iterator<Link> iter = coll.iterator();
        while (iter.hasNext()) {
            Link elem = iter.next();
            Pair<Node> currentNodes = g.getEndpoints(elem);
            //System.out.println("checking currentNodes: " + currentNodes);
            if (currentNodes.contains(node)){
                // System.out.println("Found");
                return true;
            }
        }
        return false;
    }

    public Link getLink(Node a, Node b, Collection<Link> coll,HashMap<Integer, Pair<Node>> linkEPs){
        Link l = null;
//	System.out.println("checking Link: " + a.getId()+ " _  " + b.getId());
        Iterator<Link> iter = coll.iterator();
        while (iter.hasNext()) {
            Link elem = iter.next();
            //  System.out.println("checking Link: " + elem.getId());
            Pair<Node> currentNodes = linkEPs.get(elem.getId());
            //System.out.println(currentNodes);
            if (currentNodes.getFirst().getId()==a.getId()){
                //	 System.out.println("checking Node F: " + currentNodes.getFirst().getId());
                if(currentNodes.getSecond().getId()==b.getId()){
                    //	System.out.println("checking Node S: " + currentNodes.getSecond().getId());
                    return elem;
                }
            }
        }
        return l;
    }

    public Link getLink(int a,int  b, Collection<Link> coll,HashMap<Integer, Pair<Node>> linkEPs){
        Link l = null;
        Iterator<Link> iter = coll.iterator();
        //System.out.println(a + "  " +b );
        while (iter.hasNext()) {
            Link elem = iter.next();
            Pair<Node> currentNodes = linkEPs.get(elem.getId());
            // System.out.println("currentNodes " +currentNodes );
            if (currentNodes.getFirst().getId()==a){
                if(currentNodes.getSecond().getId()==b){
                    return elem;
                }
            }
        }
        return l;
    }

    public Node getSimilarVNF(Node nf, ArrayList<Node> vnfList) {

        for (Node x: vnfList){
            if (x.getName().replaceAll("\\d","").equalsIgnoreCase(nf.getName().replaceAll("\\d","")))
                return x;
        }

        return null;
    }

    public boolean checkType(Node node, Node vnf){
        if (node.getType().equalsIgnoreCase(vnf.getType())){
            return true;
        } else if (node.getType().equalsIgnoreCase("Server") && vnf.getType().equalsIgnoreCase("NF") ){
            return true;
        }
        return false;
    }

    private boolean updateSubLinkPathlets(Substrate sub,int src, int dst, double cap){
        for (Link x: sub.getGraph().getEdges()){
            Pair<Node> eps = sub.getGraph().getEndpoints(x);
            if ((eps.getFirst().getId()==src) && (eps.getSecond().getId()==dst)){
                //System.out.println("Found edge: " + src+ " -> " +dst + " for " + eps.getFirst().getId()+" -> "+eps.getSecond().getId());
                double newCap = x.getBandwidth()-cap;
                if ((newCap<0.1) && (newCap>0)){
                    System.out.println("Found edge: " + src+ " -> " +dst + " for " + eps.getFirst().getId()+" -> "+eps.getSecond().getId());
                    System.out.println(x.getBandwidth() + " cap: " +cap +" "+newCap);
                }
                x.setBandwidth((int)newCap);
                //System.out.println(newCap);
                return true;
            }
        }

        return false;
    }

    private boolean updateSubLink(Substrate sub,int src, int dst, double cap){
        for (Link x: sub.getGraph().getEdges()){
            Pair<Node> eps = sub.getGraph().getEndpoints(x);
            if ((eps.getFirst().getId()==src) && (eps.getSecond().getId()==dst)){
                //System.out.println("Found edge: " + src+ " -> " +dst + " for " + eps.getFirst().getId()+" -> "+eps.getSecond().getId());
                double newCap = x.getBandwidth()-cap;
                if ((newCap<0.1) && (newCap>0)){
                    System.out.println("Found edge: " + src+ " -> " +dst + " for " + eps.getFirst().getId()+" -> "+eps.getSecond().getId());
                    System.out.println(x.getBandwidth() + " cap: " +cap +" "+newCap);
                }
                //  if (eps.getFirst().getType().equalsIgnoreCase("switch") ){
                // 		updateSubstrateTCAM(sub, eps.getFirst());
                // }
                x.setBandwidth((int)newCap);
                //System.out.println(newCap);
                return true;
            }
        }

        return false;

    }
    private boolean updateSubLinkRegular(Substrate sub,int src, int dst, double cap){
        for (Link x: sub.getGraph().getEdges()){
            Pair<Node> eps = sub.getGraph().getEndpoints(x);
            if ((eps.getFirst().getId()==src) && (eps.getSecond().getId()==dst)){
                //System.out.println("Found edge: " + src+ " -> " +dst + " for " + eps.getFirst().getId()+" -> "+eps.getSecond().getId());
                double newCap = x.getBandwidth()-cap;
                if ((newCap<0.1) && (newCap>0)){
                    System.out.println("Found edge: " + src+ " -> " +dst + " for " + eps.getFirst().getId()+" -> "+eps.getSecond().getId());
                    System.out.println(x.getBandwidth() + " cap: " +cap +" "+newCap);
                }
                x.setBandwidth((int)newCap);
                //System.out.println(newCap);
                return true;
            }
        }

        return false;

    }

    private boolean updateSubstrate(Substrate sub, Node node, double cap){
        for (Node x: sub.getGraph().getVertices()){
            if (x.getId()==node.getId()){
                // System.out.println("In reserve: " + x.getId()+ " "+x.getAvailableCpu() +" " +cap );
                double capNew= (x.getAvailableCpu()-cap);
                System.out.println("updating Node " + x.getId() + ": old cap: " + x.getAvailableCpu() + ", subtracting " + cap + ", new cap: " + capNew);
                //  System.out.println((int)capNew);
                x.setAvailableCpu((int) capNew);
                //  if (capNew<0) System.exit(0);
                return true;
            }

        }
        return false;
    }

    private double updateSubstrateNode(Substrate sub, Node node, double cap){
        for (Node x: sub.getGraph().getVertices()){
            if (x.getId()==node.getId()){
                //System.out.println(x.getId()+ " " +cap + " "+x.getAvailableCpu());
                double capNew= (x.getAvailableCpu()-cap);
                //System.out.println((int)capNew);
                x.setAvailableCpu((int) capNew);
                return capNew;
            }

        }
        return Double.MAX_VALUE;
    }

    private boolean updateSubstrateTCAMid(Substrate sub, int node){
        for (Node x: sub.getGraph().getVertices()){
            if (x.getId()==node){
                System.out.println(x.getId()+ "  "+x.getTCAM());
                if (x.getTCAM()>0) {
                    int capNew= x.getTCAM()-1;
                    //System.out.println((int)capNew);
                    x.setTCAM(capNew);
                    return true;
                }
                else
                    return false;
            }

        }
        return false;
    }

    private boolean updateSubstrateTCAM(Substrate sub, Node node){
        for (Node x: sub.getGraph().getVertices()){
            if (x.getId()==node.getId()){
                // System.out.println(x.getId()+ " " +cap + " "+x.getAvailableCpu());
                int capNew= x.getTCAM()-1;
                //System.out.println((int)capNew);
                x.setTCAM(capNew);
                return true;
            }

        }
        return false;
    }

    private boolean updateSubstrateTCAMRegular(Substrate sub, int node){
        for (Node x: sub.getGraph().getVertices()){
            if (x.getId()==node){
                if (x.getType().equalsIgnoreCase("switch")) {
                    // System.out.println(x.getId()+ " " +cap + " "+x.getAvailableCpu());
                    int capNew= x.getTCAM()-1;
                    //System.out.println((int)capNew);
                    x.setTCAM(capNew);
                    return true;
                }
            }
        }
        return false;
    }




    public boolean checkFeasibility(double[][][][] xVarFinal, int subNodesNum, int subNodesNumAug, int nodesSFC,
                                    double[][] sfcLinks, double[]subCapAug, double[] sfcNodes){
        //placement 1
        System.out.println(subNodesNum+ " "+subNodesNumAug + " "+ nodesSFC );

        for (int p=subNodesNum;p<subNodesNumAug;p++){
            for(int m=0; m<nodesSFC; m++){
                double sum =0;
                if (sfcLinks[p-subNodesNum][m]!=0) {
                    for (int w=0;w<subNodesNum;w++){
                        sum=sum+xVarFinal[p][w][p-subNodesNum][m];
                    }
                    if (sum!=1) return false;
                }
            }
        }

        //node capacity constraints (2)
        for (int v=0;v<subNodesNum;v++){
            double cpuCap = subCapAug[v];
            double cpuReq=0;
            ArrayList<Integer> visited= new ArrayList<Integer> ();
            ArrayList<Integer> visited_rev= new ArrayList<Integer> ();
            for(int m=0; m<nodesSFC; m++){
                for (int u=subNodesNum;u<subNodesNumAug;u++){
                    if (sfcLinks[u-subNodesNum][m]!=0){
                        if (!(visited.contains(u-subNodesNum))){
                            //System.out.println("x" + u + "  " +v + " " + (u-subNodesNum) + " " +m);
                            cpuReq = sfcNodes[u-subNodesNum]*xVarFinal[u][v][u-subNodesNum][m];
                            visited.add(u-subNodesNum);
                        }
                    }
                    else if (sfcLinks[m][u-subNodesNum]!=0){
                        if (!(visited.contains(u-subNodesNum))){
                            //System.out.println("x1 " + v + "  " +u + " " + m + " " +(u-subNodesNum));
                            cpuReq = sfcNodes[u-subNodesNum]*xVarFinal[v][u][m][u-subNodesNum];
                            visited.add(u-subNodesNum);
                        }
                    }
                }
            }
            //System.out.println("lEQ "+cpuCap);
            if (Double.compare(cpuReq, cpuCap)>0) return false;
        }


/*	for (int p=subNodesNum;p<subNodesNumAug;p++){
		for(int m=0; m< nodesSFC; m++){
			if(m!=p-subNodesNum){
				List<Integer> tmp = new ArrayList<Integer> ();
				List<Integer> tmp1 = new ArrayList<Integer> ();
				tmp.addAll(Arrays.asList(p-subNodesNum,m));
				tmp1.addAll(Arrays.asList(p-subNodesNum,m));
				if (requestedLinks.containsKey(tmp) && requestedLinks.containsKey(tmp1)){
					for (int w=0;w<subNodesNum;w++){
						double sum = xVarFinal[p][w][p-subNodesNum][m] -xVarFinal[w][p][m][p-subNodesNum];
						if (sum!=0)  return false;
					}
				}
			}
		}
}*/



/*	for (int p=subNodesNum;p<subNodesNumAug;p++){
		for (int w=0;w<subNodesNum;w++){
			for(int m=0; m< nodesSFC; m++){
				for(int l=0; l< nodesSFC; l++){
				double sum =0;
				sum=xVarFinal[p][w][p-subNodesNum][m]-xVarFinal[p][w][p-subNodesNum][l];
				if (sum!=0) return false;
				}
			}
		}
	}

	//
	for (int p=subNodesNum;p<subNodesNumAug;p++){
		for (int w=0;w<subNodesNum;w++){
			for(int m=0; m< nodesSFC; m++){
				for(int l=0; l< nodesSFC; l++){
					double sum =0;
					sum = xVarFinal[p][w][p-subNodesNum][m] - xVarFinal[w][p][l][p-subNodesNum];
					if (sum!=0) return false;
				}
			}
		}
	}*/

        return true;
    }


    public Node getNodeById (ArrayList<Node> nodes, int id){
        for (Node x: nodes){
            if (x.getId()==id)
                return x;
        }
        return null;
    }

    public static void printMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Node v = (Node) pair.getKey();
            Node s= (Node) pair.getValue();
            System.out.println(v.getId()+":"+v.getName() + " = " +  s.getId()+":"+ s.getName());
            if ((v.getType().contentEquals("RRH")) || v.getType().contentEquals("Router")){
                System.out.println(v.getType()+ " " + v.getCoords().getX()+ " " +v.getCoords().getY());
                System.out.println(s.getType()+ " " + s.getCoords().getX()+ " " +s.getCoords().getY());
            }
        }
    }


    private boolean NFplacementPathletsAlt1() {
        double[][] retres=new double[reqs.size()][1];
        double[] subCapAug=null;
        double[][] subLinksAug = null;
        int[][][][][] pathlet = null;
        int [] flowCap=null;

        Writer writer1=null;
        Writer writer=null;

        String path = "results/pathlets_alt";
        // String filename = "substratePaths" +System.currentTimeMillis() + ".txt";
        String filename = System.currentTimeMillis() + "subalt.txt";
        //String filename1 = "requestsPath" + System.currentTimeMillis() +".txt";
        String filename1 = System.currentTimeMillis() +"reqalt.txt";
        String debugFile =  path+File.separator+ System.currentTimeMillis()+"msgalt.txt";
        OutputStream output;
        try {
            output = new FileOutputStream(debugFile);
            try {
                writer = new BufferedWriter(new FileWriter(path+File.separator+filename));
                writer1 = new BufferedWriter(new FileWriter(path+File.separator+filename1));

                Substrate substrateCopy =  substrates.get(0);
                ArrayList<Node> rootSwitches = substrateCopy.getFTL3().getRootSwitches();
                ArrayList<Node> aggrSwitches = substrateCopy.getFTL3().getAggrSwitches();

                for (Request req: reqs){

                    ResourceMappingNF reqMap = new ResourceMappingNF(req);
                    ArrayList<Node> reqNodes = (ArrayList<Node>) getNodes(req.getGraph());
                    ArrayList<Link> reqLinks = (ArrayList<Link>) getLinks(req.getGraph());

                    ArrayList<Node> subNodes = (ArrayList<Node>) getNodes(substrateCopy.getGraph());
                    ArrayList<Link> subLinks = (ArrayList<Link>) getLinks(substrateCopy.getGraph());

                    //Substrate substrateCopy =  (Substrate) substrates.get(0).getCopy();

                    writer.write("Request: " +req.getId()+ " " +req.getStartDate() +  "\n");
                    writer1.write("Request: " +req.getId()+ " " +req.getStartDate() +  "\n");
                    //this.results.addReq();

                    ///////////////////////////////////////
                    double[] sfcNodes = new double[reqNodes.size()];
                    String[] sfcType= new String [reqNodes.size()];
                    int sizeOfSFC = sfcNodes.length;
                    int nodesSFC = sizeOfSFC;
                    double[][] sfcLinks = new double[sizeOfSFC][sizeOfSFC];
                    double embedding_rev=0;

                    ///////////////////////////////////////

                    int subNodesNum=substrateCopy.getGraph().getVertexCount();
                    int subNodesNumAug = subNodesNum+reqNodes.size();
                    String[] subTypeAug =  new String[subNodesNumAug];


                    subCapAug =  new double [subNodesNumAug];
                    flowCap =  new int [subNodesNumAug];
                    for (Node sn: subNodes){
                        subCapAug[sn.getId()]=sn.getAvailableCpu();

				/*if (sn.getId()==6) subCapAug[sn.getId()]=66802;
				else if (sn.getId()==7) subCapAug[sn.getId()]=400000;
				else if (sn.getId()==9) subCapAug[sn.getId()]=216668;
				else if (sn.getId()==10) subCapAug[sn.getId()]=233334;
				else if (sn.getId()==14) subCapAug[sn.getId()]=108401;
				else if (sn.getId()==15) subCapAug[sn.getId()]=233334;
				else if (sn.getId()==17) subCapAug[sn.getId()]=250134;
				else if (sn.getId()==18) subCapAug[sn.getId()]=216668;
				else if (sn.getId()==22) subCapAug[sn.getId()]=108401;
				else if (sn.getId()==23) subCapAug[sn.getId()]=250134;
				else if (sn.getId()==25) subCapAug[sn.getId()]=91735;
				else if (sn.getId()==26) subCapAug[sn.getId()]=366667;
				else if (sn.getId()==30) subCapAug[sn.getId()]=125068;
				else if (sn.getId()==31) subCapAug[sn.getId()]=91735;
				else if (sn.getId()==33) subCapAug[sn.getId()]=500000;
				else if (sn.getId()==34) subCapAug[sn.getId()]=200001;
				*/
                        subTypeAug[sn.getId()]=sn.getType();
                        if (sn.getType().equalsIgnoreCase("switch")){
                            flowCap[sn.getId()]=sn.getTCAM();
                        }
                        writer.write("Node: " + sn.getId() + " Type: " + sn.getType() +" CPU: " + sn.getAvailableCpu()
                                + " TCAM: " + sn.getTCAM() +  "\n");
                        //System.out.println(sn.getId() +" " + sn.getType() + " " +sn.getTCAM() + " "+sn.getAvailableCpu() );
                    }

                    for (Node rn: reqNodes){
                        embedding_rev += rn.getAvailableCpu();
                        subCapAug[subNodesNum+rn.getId()] = rn.getAvailableCpu();
                        subTypeAug[subNodesNum+rn.getId()] = "Server";
                        writer1.write("Node: " + rn.getId() + " Type: " + rn.getType() +" CPU: " + rn.getAvailableCpu()+"\n");
                    }

                    //create augmented links;
                    subLinksAug =  new double [subNodesNumAug][subNodesNumAug];
                    //add substrate
                    for (Link current : subLinks){
                        Pair<Node> x =  substrateCopy.getGraph().getEndpoints(current);
                        subLinksAug[x.getFirst().getId()][x.getSecond().getId()]= current.getBandwidth();
			/*	if (current.getId()==0) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==1) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==2) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==3) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 5.960464477539062E-7;
				else if (current.getId()==7) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==8) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==17) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 5.960464477539062E-7;
				else if (current.getId()==18) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 5.960464477539062E-7;
				else if (current.getId()==22) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==25) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==33) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==34) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==39) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==40) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==48) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==49) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==56) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==53) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 7600000.0;
				else if (current.getId()==58) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 7600000.0;
				else if (current.getId()==61) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 7600000.0;
				else if (current.getId()==62) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 7600000.0*/;
                        writer.write("Link " + current.getId()+ " : " + x.getFirst().getId() + " -> " + x.getSecond().getId()+" BW: " +
                                current.getBandwidth()+"\n");
                    }
                    //add requested
                    for (Link y: reqLinks){
                        embedding_rev+=y.getBandwidth();
                        Pair<Node> tmp = req.getGraph().getEndpoints(y);
                        //System.out.println("bw: "+y.getBandwidth());
                        sfcLinks[tmp.getFirst().getId()][tmp.getSecond().getId()]  = y.getBandwidth();
                        subLinksAug[subNodesNum+tmp.getFirst().getId()][subNodesNum+ tmp.getSecond().getId()]= 0;
                        writer1.write("Link " + y.getId()+ " : "+  tmp.getFirst().getId() + " -> " + tmp.getSecond().getId()+" BW: " +
                                y.getBandwidth()+"\n");
                    }

                    //	System.out.println("subLinksAug: "+Arrays.deepToString(subLinksAug[0]));
                    //add requested to AUG substrate
                    for (Node x: reqNodes){
                        //System.out.println(x.getId()+ " " + reqNodes.size());
                        sfcNodes[x.getId()] = x.getAvailableCpu();
                        sfcType[x.getId()] = "Server";// x.getType();
                        int new_id = subNodesNum+ x.getId();
                        //	for (int i=0;i<subNodesNum;i++){
                        for (Node sn: subNodes) {
                            if (sn.getType().equalsIgnoreCase("server")){
                                int i = sn.getId();
                                subLinksAug[new_id][i] = Double.MAX_VALUE;
                                subLinksAug[i][new_id] = Double.MAX_VALUE;
                            }
                        }
                    }
                    //System.out.println("subLinksAug: "+Arrays.deepToString(subLinksAug));

                    System.out.println("subNodesNum: " +subNodesNum);
                    System.out.println("subNodesNumAug: " +subNodesNumAug);
                    reqMap.setEmbeddingRevenue(embedding_rev);


                    System.out.println("sfcNodes: "+Arrays.toString(sfcNodes));
                    System.out.println("sizeOfSFC: "+sizeOfSFC);
                    nodesSFC = sizeOfSFC;
                    System.out.println("flowCap: "+Arrays.toString(sfcLinks));

                    System.out.println("subCapAug: "+Arrays.toString(subCapAug));
                    System.out.println("subNodesNumAug: "+subNodesNumAug);
                    System.out.println("subLinksAug: "+Arrays.deepToString(subLinksAug));
                    //	System.exit(0);


                    ///////////////////////////////////////

                    req.print();
                    try {
                        System.out.println("Pathlets: Starting opt:" + req.getId());
                        IloCplex cplex = new IloCplex();
                        cplex.setParam(IloCplex.DoubleParam.TiLim, 600);
                        cplex.setParam(IloCplex.DoubleParam.ObjULim,Double.MAX_VALUE);
                        cplex.setParam(IloCplex.DoubleParam.ObjLLim,-1*Double.MAX_VALUE);
                        cplex.setOut(output);
                        int denial=0;

                        int count = 0;
                        IloNumVar[][][][] x = new IloNumVar[subNodesNumAug][subNodesNumAug][][];

                        for (int u=0;u<subNodesNumAug;u++){
                            x[u]=new IloNumVar[subNodesNumAug][][];
                            for (int v=0;v<subNodesNumAug;v++){
                                x[u][v]=new IloNumVar[nodesSFC][];
                                for(int k=0; k< nodesSFC; k++){
                                    x[u][v][k]=new IloNumVar[nodesSFC];
                                    for(int m=0; m<nodesSFC; m++){
                                        x[u][v][k]=cplex.numVarArray(nodesSFC, 0, 1,IloNumVarType.Int);
                                        count++;
                                    }
                                }
                            }
                        }


                        count=0;
                        //System.out.println("nodesSFC: " +nodesSFC );
                        IloNumVar[][][][] fl = new IloNumVar [subNodesNumAug][subNodesNumAug][][];


                        for (int u=0;u<subNodesNumAug;u++){
                            fl[u]=new IloNumVar[subNodesNumAug][][];
                            for (int v=0;v<subNodesNumAug;v++){
                                fl[u][v]=new IloNumVar[nodesSFC][];
                                for(int k=0; k<nodesSFC; k++){
                                    fl[u][v][k]= new IloNumVar[nodesSFC];
                                    for(int m=0; m<nodesSFC; m++){
                                        fl[u][v][k][m]=cplex.numVar(0, 1000000000);
                                        //System.out.println(u + " " +v + " " +k + " " + m + " " +p);
                                        count++;
                                    }
                                }
                            }
                        }





                        /*****************************Objective Function **************************************************/

                        IloLinearNumExpr cost = cplex.linearNumExpr();
                        ////////////////////////////////////////////////////////////////////////
                        //It builds the first summation of the objective function//////////////
                        //////////////////////////////////////////////////////////////////////

                        for (int u=0;u<subNodesNumAug;u++){
                            for (int v=0;v<subNodesNumAug;v++){
                                for(int k=0; k< nodesSFC; k++){
                                    for(int m=0; m< nodesSFC; m++){
                                        cost.addTerm(1, x[u][v][k][m]);
                                    }
                                }
                            }
                        }


                        ////////////////////////////////////////////////////////////////////////
                        //It builds the second summation of the objective function//////////////
                        //////////////////////////////////////////////////////////////////////
                        count = 0;
                        IloLinearNumExpr flows = cplex.linearNumExpr();
                        double dmd =0;
                        //calculate normalization factor
                        for(int i=0;i< sizeOfSFC; i++){
                            for(int j=0; j<sizeOfSFC; j++){
                                dmd = dmd+sfcLinks[i][j];
                            }
                        }
                        //	System.out.println(dmd);

                        for(int i=0; i< sizeOfSFC; i++){
                            for(int j=0; j<sizeOfSFC; j++){
                                for (int u=0;u<subNodesNum;u++){
                                    for (int v=0;v<subNodesNum;v++){
                                        flows.addTerm(1/(dmd+Double.MIN_VALUE +Double.MIN_VALUE), fl[u][v][i][j]);
                                    }
                                }

                            }

                        }


                        IloNumExpr expr = cplex.sum(flows,cost);
                        cplex.addMinimize(expr);


                        /*****************************Capacity Constraints **************************************************/
                        //node capacity constraints
                        count=0;
                        for (int v=0;v<subNodesNum;v++){ //for every substrate
                            double cpuCap = subCapAug[v];
                            IloLinearNumExpr cpuReq = cplex.linearNumExpr();
                            ArrayList<Integer> visited= new ArrayList<Integer> (); //visited v-nodes
                            for(int m=0; m<nodesSFC; m++){
                                for (int u=subNodesNum;u<subNodesNumAug;u++){ //for every pseudo
                                    if (sfcLinks[u-subNodesNum][m]!=0){ //if requested link exists
                                        if (!(visited.contains(u-subNodesNum))){ //have not visited v-node before
                                            //if (v==6) System.out.println("x" + u + "  " +v + " " + (u-subNodesNum) + " " +m);

                                            cpuReq.addTerm(sfcNodes[u-subNodesNum], x[u][v][u-subNodesNum][m]);

                                            visited.add(u-subNodesNum);
                                        }
                                    }
                                    else if (sfcLinks[m][u-subNodesNum]!=0){ //if reverse exists
                                        if (!(visited.contains(u-subNodesNum))){
                                            //if (v==6)System.out.println("x1 " + v + "  " +u + " " + m + " " +(u-subNodesNum));

                                            cpuReq.addTerm(sfcNodes[u-subNodesNum], x[v][u][m][u-subNodesNum]);

                                            visited.add(u-subNodesNum);
                                        }
                                    }
                                }
                            }
                            //System.out.println("lEQ "+cpuCap);
                            cplex.addLe(cpuReq, cpuCap);
                            count++;
                        }
                        output.write(("1 - node cap " +count + "\n").getBytes());

                        //link capacity constraints (1)

                        for (int u=0;u<subNodesNumAug;u++){
                            for (int v=0;v<subNodesNumAug;v++){
                                //	System.out.println("subLinksAug" + u + "  " +v + " " + p);
                                double cap =subLinksAug[u][v];
                                IloLinearNumExpr bwReq = cplex.linearNumExpr();;
                                for(int k=0; k< nodesSFC; k++){
                                    for(int m=0; m< nodesSFC; m++){
                                        bwReq.addTerm(1,fl[u][v][k][m]);
                                    }
                                }
                                cplex.addLe(bwReq, cap);
                                count++;
                            }
                        }

                        output.write(("2 - link capacity " +count+ "\n").getBytes());

                        //link capacity constraints (2)

                        for (int u=0;u<subNodesNumAug;u++){
                            for (int v=0;v<subNodesNumAug;v++){
                                //	double cap = subLinksAug[u][v];
                                for(int k=0; k< nodesSFC; k++){
                                    for(int m=0; m< nodesSFC; m++){
                                        double cap = sfcLinks[k][m];
                                        IloLinearNumExpr bwReq1 = cplex.linearNumExpr();
                                        IloLinearNumExpr bwReq2 = cplex.linearNumExpr();
                                        bwReq1.addTerm(1,fl[u][v][k][m]);
                                        bwReq2.addTerm(cap,x[u][v][k][m]);
                                        cplex.addLe(bwReq1, bwReq2);
                                        count++;
                                    }
                                }
                            }
                        }

                        output.write(("3 - xf association " +count+ "\n").getBytes());

                        //flow capacity constraints
                        for (int v=0;v<subNodesNum;v++){
                            if (subTypeAug[v].equalsIgnoreCase("switch")) {
                                double flowC = flowCap[v];
                                IloLinearNumExpr fcReq = cplex.linearNumExpr();
                                for (int u=0;u<subNodesNum;u++){
                                    for(int i=0; i< sizeOfSFC; i++){
                                        for(int j=0; j<sizeOfSFC; j++){
                                            fcReq.addTerm(1,x[u][v][i][j]);
                                            //if (v==1) System.out.println("x" + u + "  " +v + " " + i + " " +j);
                                        }
                                    }
                                }
                                cplex.addLe(fcReq, flowC);
                                //System.out.println("flowC " + flowC);
                                count++;
                            }
                        }

                        //System.exit(0);
                        output.write(("4 - flow cap: " +count+ "\n").getBytes());


                        /*****************************Placement and Assignment Constraints **************************************************/


                        ///CON   (20)
/*			for (int p=subNodesNum;p<subNodesNumAug;p++){
				//System.out.println("x" + p);
					for(int m=0; m< nodesSFC; m++){
						if (sfcLinks[p-subNodesNum][m]!=0) {
							IloLinearNumExpr assignments1 = cplex.linearNumExpr();
							for (int w=0;w<subNodesNum;w++){
							if (p==40) System.out.println("x" + p + "  " +w + " " + (p-subNodesNum) + " " +m);
							assignments1.addTerm(1, x[p][w][p-subNodesNum][m]);
							}
							System.out.println("EQ "+1);
							cplex.addEq(assignments1, 1);
							//System.out.println(count+1);
							count++;
					}
				}

			}

			for (int p=subNodesNum;p<subNodesNumAug;p++){
				//System.out.println("x" + p);
					for(int m=0; m< nodesSFC; m++){
						if (sfcLinks[m][p-subNodesNum]!=0) {
							IloLinearNumExpr assignments1 = cplex.linearNumExpr();
							for (int w=0;w<subNodesNum;w++){
							if (p==40)  System.out.println("x" + p + "  " +w + " " + (p-subNodesNum) + " " +m);
							assignments1.addTerm(1, x[w][p][m][p-subNodesNum]);
							}
							//System.out.println("EQ "+1);
							cplex.addEq(assignments1, 1);
							//System.out.println(count+1);
							count++;
					}
				}

			}*/

                        for (int p=subNodesNum;p<subNodesNumAug;p++){
                            //System.out.println("x" + p);
                            for(int m=0; m< nodesSFC; m++){
                                IloLinearNumExpr assignments1 = cplex.linearNumExpr();
                                ArrayList<Integer> visited= new ArrayList<Integer> (); //visited s-node
                                for (int w=0;w<subNodesNum;w++){
                                    if (sfcLinks[p-subNodesNum][m]!=0) {
                                        if (!(visited.contains(w))){ //have not visited s-node before
                                            assignments1.addTerm(1, x[p][w][p-subNodesNum][m]);
                                            //if (w==1) System.out.println("x" + p + "  " +w + " " + (p-subNodesNum) + " " +m);
                                            visited.add(w);
                                        }
                                    }
                                    else if (sfcLinks[m][p-subNodesNum]!=0){ //if reverse exists
                                        if (!(visited.contains(w))){ //have not visited s-node before
                                            assignments1.addTerm(1, x[w][p][m][p-subNodesNum]);
                                            //if (w==1)  System.out.println("x" + w + "  " +p + " " + m+" "+ (p-subNodesNum));
                                            visited.add(w);
                                        }
                                    }
                                }
                                if (visited.size()>0){
                                    //System.out.println("EQ "+1);
                                    cplex.addEq(assignments1, 1);
                                    count++;
                                }
                                //System.out.println("aaaaaaaaaaaaaaaaa");
                                //if ((p==41) && (m==4)) System.exit(0);

                            }
                        }

                        output.write(("5 - single placement : " +count + "\n").getBytes());
                        //	System.exit(0);


                        //anticollocation
                        for (int w=0;w<subNodesNum;w++){
                            if (!subTypeAug[w].equalsIgnoreCase("switch")) {
                                //   System.out.println("w: " + w);
                                IloLinearNumExpr assignments1 = cplex.linearNumExpr();
                                for (int p=subNodesNum;p<subNodesNumAug;p++){
                                    ArrayList<Integer> visited_new= new ArrayList<Integer> ();
                                    //System.out.println("p: " + p);
                                    for(int m=0; m< nodesSFC; m++){
                                        //                      System.out.println("m: " + m);
                                        if (!((sfcLinks[p-subNodesNum][m]==0)&&(sfcLinks[m][p-subNodesNum]==0)))  {
                                            if (!(visited_new.contains(p))){
                                                visited_new.add(p);
                                                //System.out.println("x" + p + "  " +w + " " + (p-subNodesNum) + " " +m );
                                                //System.out.println("x" + w + "  " +p + " " + m + " " +(p-subNodesNum));
                                                assignments1.addTerm(1, x[p][w][p-subNodesNum][m]);
                                                assignments1.addTerm(1, x[w][p][m][p-subNodesNum]);
                                            }
                                        }
                                    }

                                }
                                //System.out.println("lEQ "+1);
                                cplex.addLe(assignments1, 1);
                                //System.out.println(count+1);
                                count++;
                            }
                        }

                        output.write(("6 - anticollocation : " +count+ "\n").getBytes());

                        ///26
                        for (int p=subNodesNum;p<subNodesNumAug;p++){
                            for (int w=0;w<subNodesNum;w++){
                                //IloLinearNumExpr assignments1 = cplex.linearNumExpr();
                                for(int m=0; m< nodesSFC; m++){
                                    if (sfcLinks[p-subNodesNum][m]!=0) {
                                        for(int l=0; l< nodesSFC; l++){
                                            if (l!=m){
                                                if (sfcLinks[p-subNodesNum][l]!=0) {
                                                    IloLinearNumExpr assignments1 = cplex.linearNumExpr();
                                                    //	if (p==37) System.out.println("x" + p + "  " +w + " " + (p-subNodesNum) + " " +m);
                                                    //	if (p==37) System.out.println("x" + p + "  " +w + " " + (p-subNodesNum) + " " +l);
                                                    assignments1.addTerm(1, x[p][w][p-subNodesNum][m]);
                                                    assignments1.addTerm(-1, x[p][w][p-subNodesNum][l]);
                                                    cplex.addEq(assignments1, 0);
                                                    //	if (p==37) System.out.println(" eq 0");
                                                    count++;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        output.write(("7 - connected graph1 : " +count+ "\n").getBytes());

                        //reverse
                        for (int p=subNodesNum;p<subNodesNumAug;p++){
                            for (int w=0;w<subNodesNum;w++){
                                //IloLinearNumExpr assignments1 = cplex.linearNumExpr();
                                for(int m=0; m< nodesSFC; m++){
                                    if (sfcLinks[m][p-subNodesNum]!=0) {
                                        for(int l=0; l< nodesSFC; l++){
                                            if (l!=m){
                                                if (sfcLinks[l][p-subNodesNum]!=0) {
                                                    IloLinearNumExpr assignments1 = cplex.linearNumExpr();
                                                    //if (p==40) System.out.println("x" + w + "  " +p + " " + m + "  " +(p-subNodesNum));
                                                    //if (p==40) System.out.println("x" + w + "  " +p + " " + l + "  " +(p-subNodesNum));
                                                    assignments1.addTerm(1, x[w][p][m][p-subNodesNum]);
                                                    assignments1.addTerm(-1, x[w][p][l][p-subNodesNum]);
                                                    //if (p==40) System.out.println(" eq 0");
                                                    cplex.addEq(assignments1, 0);
                                                    count++;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        output.write(("8 - connected graph2 : " +count+ "\n").getBytes());

                        //october (27)
                        for (int p=subNodesNum;p<subNodesNumAug;p++){
                            for (int w=0;w<subNodesNum;w++){
                                for(int m=0; m< nodesSFC; m++){
                                    if (sfcLinks[p-subNodesNum][m]!=0) {
                                        for(int l=0; l< nodesSFC; l++){
                                            if (sfcLinks[l][p-subNodesNum]!=0) {
                                                IloLinearNumExpr assignments1 = cplex.linearNumExpr();
                                                //if (p==36) System.out.println("x" + p + "  " +w + " " + (p-subNodesNum) + " " +m);
                                                ///if (p==36) System.out.println("x" + w + "  " +p + " " + l + "  " +(p-subNodesNum));
                                                assignments1.addTerm(1, x[p][w][p-subNodesNum][m]);
                                                assignments1.addTerm(-1, x[w][p][l][p-subNodesNum]);
                                                //if (p==36) System.out.println(" eq 0");
                                                cplex.addEq(assignments1, 0);
                                                count++;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        output.write(("9 - connected graph3 : " +count+ "\n").getBytes());



                        //System.out.println("7 : " +count);

                        //last added //october ( 25)
                        for (int p=subNodesNum;p<subNodesNumAug;p++){
                            //for (int w=0;w<subNodesNum;w++){
                            IloLinearNumExpr assignments1 = cplex.linearNumExpr();
                            for(int k=0; k< nodesSFC; k++){
                                for(int m=0; m< nodesSFC; m++){
                                    for (int w=0;w<subNodesNum;w++){

                                        if (k!=(p-subNodesNum)){
                                            //if (p==37) System.out.println("x" + p + "  " +w + " " + k+ " " +m);
                                            assignments1.addTerm(1, x[p][w][k][m]);
                                            //assignments1.addTerm(1, x[w][p][m][k]);
                                            //cplex.addEq(assignments1, 0);
                                        }
                                        if (m!=(p-subNodesNum)){
                                            // if (p==36) System.out.println("x" + w + "  " +p + " " + k+ " " +m);
                                            assignments1.addTerm(1, x[w][p][k][m]);
                                            //cplex.addEq(assignments1, 0);
                                        }


                                    }
                                }
                            }
                            cplex.addEq(assignments1, 0);
                            count++;
                        }

                        output.write(("10 - matching type : " +count+ "\n").getBytes());


		/*
			//System.out.println  bidirectional
		for (int p=subNodesNum;p<subNodesNumAug;p++){
			for(int m=0; m< nodesSFC; m++){
				if(m!=(p-subNodesNum)){
				if ((sfcLinks[p-subNodesNum][m]!=0) && (sfcLinks[m][p-subNodesNum]!=0)){
				for (int w=0;w<subNodesNum;w++){
					IloLinearNumExpr assignments1 = cplex.linearNumExpr();
					IloLinearNumExpr assignments2 = cplex.linearNumExpr();
					//System.out.println("x" + p + "  " +w + " " + (p-subNodesNum) + " " +m);
					assignments1.addTerm(1, x[p][w][p-subNodesNum][m]);
					assignments2.addTerm(1, x[w][p][m][p-subNodesNum]);
					//System.out.println("x" + w + "  " +p + " " +m + " " +(p-subNodesNum));
					//System.out.println("EQ 1");
					cplex.addEq(assignments1, assignments2);
					count++;
				}
				}
				}
			}
		}
		output.write(("11 - bidirectional : " +count).getBytes());
*/

                        /*****************************Flow Constraints **************************************************/

                        //oti mpainei se kombo bgainei gia normal substrate
                        for (int u=0;u<subNodesNum;u++){
                            for(int k=0; k< nodesSFC; k++){
                                for(int m=0; m< nodesSFC; m++){
                                    IloLinearNumExpr flowCon2 = cplex.linearNumExpr();
                                    IloLinearNumExpr flowCon3 = cplex.linearNumExpr();
                                    for (int l=0;l<subNodesNumAug;l++){
                                        flowCon2.addTerm(1,fl[l][u][k][m]); //incoming
                                        flowCon3.addTerm(1,fl[u][l][k][m]);
                                    }
                                    cplex.addEq(flowCon2, flowCon3);
                                    count++;
                                }
                            }
                        }

                        output.write(("12 - flow 1 : " +count+ "\n").getBytes());
                        //System.out.println("11 : " +count);
                        for (int u=subNodesNum;u<subNodesNumAug;u++){
                            for(int m=0; m< nodesSFC; m++){
                                double capVirt =sfcLinks[u-subNodesNum][m];
                                IloLinearNumExpr flowCon1 = cplex.linearNumExpr();
                                for (int l=0;l<subNodesNum;l++){
                                    flowCon1.addTerm(1,fl[u][l][u-subNodesNum][m]); //outgoing
                                }
                                cplex.addEq(flowCon1, capVirt);
                                count++;
                            }
                        }
                        output.write(("13 - flow 2 : " +count+ "\n").getBytes());
                        for (int u=subNodesNum;u<subNodesNumAug;u++){
                            for(int m=0; m< nodesSFC; m++){
                                double capVirt =sfcLinks[m][u-subNodesNum];
                                IloLinearNumExpr flowCon1 = cplex.linearNumExpr();
                                for (int l=0;l<subNodesNum;l++){
                                    flowCon1.addTerm(1,fl[l][u][m][u-subNodesNum]); //incoming
                                }
                                cplex.addEq(flowCon1, capVirt);
                                count++;
                            }
                        }
                        output.write(("14 - flow 3 : " +count+ "\n").getBytes());
                        //System.out.println("13 : " +count);


                        cplex.exportModel("LPsimple_alt1.lp");

                        long solveStartTime = System.nanoTime();
                        boolean solvedOK = cplex.solve();
                        long solveEndTime = System.nanoTime();
                        long solveTime = solveEndTime - solveStartTime;

                        req.print();
                        if (solvedOK) {
                            System.out.println("###################################");
                            System.out.println( "Found an answer! CPLEX status: " + cplex.getStatus() + ", Time: " + ((double) solveTime / 1000000000.0));

                            cplex.output().println("Solution value = " + cplex.getObjValue());
                            System.out.println("###################################");
                            double cpuCost=0;
                            int counterA=0;
                            //nodeMapping
                            LinkedHashMap<Node, Node> nodeMap = new LinkedHashMap<Node, Node> ();//requested-real
                            //ArrayList<Node> subNodes = getNodes(substrateCopy.getGraph());
                            int rules_added=0;
                            double embedding_cost=0;
                            ArrayList<Integer> fvisited = new ArrayList<Integer>();
                            double[][][][] xVar =new double [subNodesNumAug][subNodesNumAug][nodesSFC][nodesSFC];
                            for (int u=0;u<subNodesNumAug;u++){
                                for (int v=0;v<subNodesNumAug;v++){
                                    for(int k=0; k< nodesSFC; k++){
                                        for(int m=0; m<nodesSFC; m++){
                                            xVar[u][v][k][m]=cplex.getValue(x[u][v][k][m]);
                                            if (xVar[u][v][k][m]>0.00001)
                                            {
                                                counterA++;
                                                System.out.println(" Vlink: " + k +"->"+m +" mapped to: " + u +"->"+v + " " );
                                                if ((u==(subNodesNum+k)) && !fvisited.contains(k)) {
                                                    //System.out.println(subCapAug[u]);
                                                    embedding_cost +=reqNodes.get(k).getAvailableCpu();
                                                    cpuCost+=reqNodes.get(k).getAvailableCpu();
                                                    nodeMap.put(reqNodes.get(k),((List<Node>) subNodes).get(v));
                                                    if (!(updateSubstrate(substrateCopy,subNodes.get(v),reqNodes.get(k).getAvailableCpu())))
                                                        throw new ArithmeticException("Substrate Node Capacity not updated");
                                                    fvisited.add(k);
                                                }
                                                if ((v==(subNodesNum+m)) && !fvisited.contains(m)) {
                                                    //	System.out.println(subCapAug[v]);
                                                    embedding_cost +=reqNodes.get(m).getAvailableCpu();
                                                    cpuCost+=reqNodes.get(m).getAvailableCpu();
                                                    nodeMap.put(reqNodes.get(m),((List<Node>) subNodes).get(u));
                                                    if (!(updateSubstrate(substrateCopy,subNodes.get(u),reqNodes.get(m).getAvailableCpu())))
                                                        throw new ArithmeticException("Substrate Node Capacity not updated");
                                                    fvisited.add(m);
                                                }
                                                //if ((u!=(subNodesNum+k)) && (v!=(subNodesNum+m))  && (u!=(subNodesNum+m)) && (v!=(subNodesNum+k))) {
                                                if(subTypeAug[u].equalsIgnoreCase("switch")) {
                                                    System.out.println("added rule break 1");
                                                    rules_added+=1;
                                                } else if (subTypeAug[v].equalsIgnoreCase("switch")) {
                                                    System.out.println("added rule break 2");
                                                    rules_added+=1;
                                                }
                                                else if ( (subTypeAug[v].equalsIgnoreCase("server")) && (v!=(subNodesNum+k)) && (v!=(subNodesNum+m))){
                                                    System.out.println("added rule 3 " + subTypeAug[u]);
                                                    rules_added+=1;
                                                }
                                                //}
                                            }
                                        }
                                    }
                                }
                            }


                            //System.out.println("subNodesNumAug:" +subNodesNumAug);
                            MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>> lmap = new
                                    MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>>();
                            // HashMap<Pair<Integer>, ArrayList<HashMap<Pair<Integer>, Double>>> lmapint =
                            //new HashMap<Pair<Integer>, ArrayList<HashMap<Pair<Integer>, Double>>>();
                            double avg_hops=0;
                            double bwCost=0;
                            HashMap<Integer,Integer> tcamMap =  new HashMap<Integer,Integer>(); //all
                            HashMap<Integer,Integer> tcamMapRoot =  new HashMap<Integer,Integer>();
                            HashMap<Integer,Integer> tcamMapAgg =  new HashMap<Integer,Integer>();
                            HashMap<Integer,Integer> tcamMapTor =  new HashMap<Integer,Integer>();
                            int counterB=0;
                            double[][][][] fVar =new double [subNodesNumAug][subNodesNumAug][nodesSFC][nodesSFC];
                            for(int k=0; k< nodesSFC; k++){
                                for(int m=0; m<nodesSFC; m++){
                                    //System.out.println("checking link: " +k+" -> "+m);
                                    int paths_used=0;
                                    int hops_link_path=0;
                                    //Set<Integer> visitedSol = new HashSet<Integer>();
                                    for (int u=0;u<subNodesNumAug;u++){
                                        for (int v=0;v<subNodesNumAug;v++){

                                            fVar[u][v][k][m]=cplex.getValue(fl[u][v][k][m]);
                                            if (fVar[u][v][k][m]>0.1) {
                                                counterB++;
	/*								if (visitedSol.contains(u)){
										paths_used++;
									} else{
										visitedSol.add(u);
									}*/
                                                System.out.println(" Vlink: " + k +"->"+m +" mapped to: " + u +"->"+v +
                                                        "has flow" + fVar[u][v][k][m]);
                                                //update substrate
                                                //System.out.println(collPaths);

                                                Pair<Integer> key= new Pair<Integer>(u,v);
                                                HashMap<Pair<Integer>,Double> key4lmap=new HashMap<Pair<Integer>,Double>();
                                                key4lmap.put(key, fVar[u][v][k][m]);
                                                List<Pair<Integer>> tmpPath = new ArrayList<Pair<Integer>> ();
                                                tmpPath.add(key);
                                                lmap.put(key4lmap,tmpPath);
                                                //System.out.println("adding: " +key4lmap+ " " +tmpPath);
                                                //System.out.println(subNodesNum);
                                                //	System.out.println(u+ " " +v + " " + (k+subNodesNum)+ " "+ k +" " +(m+subNodesNum)+" " +m);
                                                if ((u!=(k+subNodesNum)) && (v!=(m+subNodesNum))){
                                                    if ((v!=(k+subNodesNum)) && (u!=(m+subNodesNum))){
                                                        embedding_cost=embedding_cost+fVar[u][v][k][m];
                                                        bwCost=bwCost+fVar[u][v][k][m];
                                                        if (!(updateSubLinkRegular(substrateCopy,u,v, fVar[u][v][k][m])))
                                                            throw new ArithmeticException("Substrate Link Capacity not updated");
                                                        if ((subTypeAug[u].equalsIgnoreCase("switch")) ||
                                                                (subTypeAug[u].equalsIgnoreCase("server"))){
                                                            //works only with anti-collocation
                                                            if (subTypeAug[v].equalsIgnoreCase("switch")){

                                                                updateSubstrateTCAMRegular(substrateCopy,v);
                                                                if (tcamMap.containsKey(v)){
                                                                    int tmp= tcamMap.get(v)+1;
                                                                    //tcamMap.replace(v, tmp);
                                                                }else {
                                                                    tcamMap.put(v, 1);
                                                                }
                                                                System.out.println("ADDED ONE RULE 1 for switch " + v);

                                                                Node current = getNodeById(subNodes,v);
                                                                if (rootSwitches.contains(current)) {
                                                                    if (tcamMapRoot.containsKey(v)){
                                                                        int tmp= tcamMapRoot.get(v)+1;
                                                                        //tcamMapRoot.replace(v, tmp);
                                                                    }else {
                                                                        tcamMapRoot.put(v, 1);
                                                                    }
                                                                } else if (aggrSwitches.contains(current)) {
                                                                    if (tcamMapAgg.containsKey(v)){
                                                                        int tmp= tcamMapAgg.get(v)+1;
                                                                        //tcamMapAgg.replace(v, tmp);
                                                                    }else {
                                                                        tcamMapAgg.put(v, 1);
                                                                    }
                                                                } else {
                                                                    if (tcamMapTor.containsKey(v)){
                                                                        int tmp= tcamMapTor.get(v)+1;
                                                                        //tcamMapTor.replace(v, tmp);
                                                                    }else {
                                                                        tcamMapTor.put(v, 1);
                                                                    }

                                                                }

                                                            }

                                                        }
						/*			 if(updateSubstrateTCAMRegular(substrateCopy,u)) {
												if (tcamMap.containsKey(u)){
				 									int tmp= tcamMap.get(u)+1;
				 									tcamMap.replace(u, tmp);
					 							} else {
					 								tcamMap.put(u, 1);
					 							}
										 }
											*/
                                                        hops_link_path++;
                                                        //System.out.println("hops_link_path");
                                                    }
                                                }


                                            }

                                        }

                                    }


                                    //System.out.println ( "hops_link_path: " +hops_link_path);
                                    avg_hops = avg_hops+hops_link_path;
	/*					if (hops_link_path>0){
							paths_used++;
							avg_hops+=(double)(hops_link_path/(paths_used));
							System.out.println ( " " +paths_used+ " " + avg_hops);
						}*/
                                }
                            }



                            avg_hops = 	avg_hops / reqLinks.size();
                            //substrateCopy.print();
                            //System.out.println ( " avg_hops " + avg_hops  +"  and rules  " + rules_added +"  for links" + reqLinks.size());
                            //	System.out.println (counterA+ " " +counterB);
                            //System.out.println(tcamMapTor);
                            //		System.out.println(rules_added);
                            //		System.exit(0);
                            //	System.exit(0);
                            //substrateCopy.print();
                            reqMap.setTCAMmapping(tcamMap);
                            reqMap.setTCAMmappingTOR(tcamMapTor);
                            reqMap.setTCAMmappingAgg(tcamMapAgg);
                            reqMap.setTCAMmappingRoot(tcamMapRoot);
                            reqMap.setNodeMapping(nodeMap);
                            reqMap.setFlowMapping(lmap);
                            reqMap.setRulesAdded(rules_added);
                            reqMap.setHops(avg_hops);
                            //System.out.println("Rules added: " +rules_added);

                            System.out.println("###############################################");
                            System.out.println(nodeMap);
                            System.out.println(lmap);

                            int[] racks = new int[8];
                            Set<Node> sub_mapping = nodeMap.keySet();
                            for(Node r:sub_mapping){
                                Node m = nodeMap.get(r);
                                if (m.getId()<8) {
                                    racks[0]++;
                                } else if (m.getId()<13) {
                                    racks[1]++;
                                } else if (m.getId()<16) {
                                    racks[2]++;
                                } else if (m.getId()<21) {
                                    racks[3]++;
                                } else if (m.getId()<24) {
                                    racks[4]++;
                                } else if (m.getId()<29) {
                                    racks[5]++;
                                } else if (m.getId()<32) {
                                    racks[6]++;
                                } else {
                                    racks[7]++;
                                }
                            }
                            reqMap.setRackAllocation(racks);
                            //System.out.println("racks:" +Arrays.toString(racks));
                            //System.exit(0);

	/*			System.out.println("TCAM Mem:" + tcamMap);
			    System.out.println("Root Switches" + rootSwitches);
			    System.out.println("Aggr Switches" + aggrSwitches);
			    System.out.println("TCAM Mem TOR:" + tcamMapTor);
			    System.out.println("TCAM Mem Agg:" + tcamMapAgg);
			    System.out.println("TCAM Mem Root:" + tcamMapRoot);*/



                            reqMap.accepted();
                            reqMap.setEmbeddingCost(embedding_cost);
                            reqMap.setBWCost(bwCost);
                            reqMap.setCPUCost(cpuCost);
                            req.setRMapNF(reqMap);
                            //if (counterA>counterB){


/*				try {
			        System.in.read();
			    } catch (IOException e) {
			        // TODO Auto-generated catch block
			        e.printStackTrace();
			    }*/

                            //System.exit(0);
                            writer1.write("Node Mapping: "+ nodeMap+ "\n");
                            writer1.write("Link Mapping: "+ lmap+ "\n");

                        }else{
                            System.out.println("Did not found an answer for Mapping");
                            reqMap.denied();
                            req.setRMapNF(reqMap);
                            substrateCopy.print();
                            req.print();

/*				try {
			        System.in.read();
			    } catch (IOException e) {
			        // TODO Auto-generated catch block
			        e.printStackTrace();
			    }*/
                        }
                        cplex.end();
                        //System.exit(0);
                    } catch (IloException e) {
                        System.err.println("Concert exception caught: " + e);
                    }

                    output.close();
                }//all requests



            } catch (IOException ex) {
                // report
            } finally {
                try {writer.close(); writer1.close();} catch (Exception ex) {/*ignore*/}
            }
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        return true;
    }


    private void getResLifetime(ArrayList<Node> subNodesList) {
        subNodesLife =new double[subNodesList.size()];
        subNodesHostedReq =new double[subNodesList.size()];
        for (Node curr: subNodesList) {
            double life=0;
            double reqHosted = 0;
            for (Request act: active) {
                if (act.getRMapNF().containsNodeInMap(curr)){
                    life=life+(act.getEndDate()-this.ts);
                    reqHosted++;
                }
            }
            subNodesLife[curr.getId()]=life;
            subNodesHostedReq[curr.getId()]=reqHosted;
        }
        System.out.println("AlgorithmNF: " +Arrays.toString(subNodesLife));
        System.out.println("AlgorithmNF: " +Arrays.toString(subNodesHostedReq));
        return;
    }
    private boolean NFplacement_RL() {
        Writer writer1=null;
        Writer writer=null;

        try {
            //log all results - substrate snapshot + request/mapping after running the algorithm
//            String path = "results/"+this.id;
//            new File(path).mkdirs();

//            String filename = "substrate_"+this.id+"_" +System.currentTimeMillis() + ".txt";
//            long name=System.currentTimeMillis();
//            writer = new BufferedWriter(new FileWriter(path+File.separator+filename));
//            String filename1 = "requests_"+this.id + "_" + name +".txt";
//            writer1 = new BufferedWriter(new FileWriter(path+File.separator+filename1));

            writer = new NullWriter();
            writer1 = new NullWriter();

            //for every request attempt mapping
            for (Request req: reqs){
                long solveStartTime = System.nanoTime();
                ResourceMappingNF reqMap = new ResourceMappingNF(req);
                //analyze substrate into arrays for cplex
                Substrate substrateCopy =  substrates.get(0);
                Graph<Node, Link> sub= substrateCopy.getGraph();
                ArrayList<Node> subNodesList = (ArrayList<Node>) getNodes(substrateCopy.getGraph());
                ArrayList<Link> subLinksList = (ArrayList<Link>) getLinks(substrateCopy.getGraph());
                writer.write("Request: " +req.getId()+ " \n");
                int subNodesNum = subNodesList.size();
                double[] subNodes =  new double[subNodesNum];

                String[] subNodesType =  new String[subNodesNum];

                double nom_cap =0;

                for (Node x: subNodesList){
                    subNodes[x.getId()] = x.getAvailableCpu();
                    subNodesType[x.getId()]= x.getType();
                    if (x.getType().equalsIgnoreCase("Server"))
                        nom_cap=x.getNominalCpu();
                    writer.write("Node: " + x.getId() + " Type: " + x.getType() +" CPU: " + x.getAvailableCpu()  +"\n");


                }
                //Adjacency Matrix Substrate
                double[][] subLinks = new double[subNodesNum][subNodesNum];
                //double [] resLinks = new double [subLinksList.size()];
                for (Link y: subLinksList){
                    Pair<Node> tmp = sub.getEndpoints(y);
                    subLinks[tmp.getFirst().getId()][tmp.getSecond().getId()]  = y.getBandwidth();
                    //resLinks[y.getId()]  = y.getBandwidth();
                    writer.write("Link " + y.getId()+ " : "+ tmp.getFirst().getId() + " -> " + tmp.getSecond().getId()+" BW: " +
                            y.getBandwidth()+"\n");
                }








	/*		System.out.println("subNodesNum: "+subNodesNum);
			System.out.println("subCap: "+Arrays.toString(subNodes));
			System.out.println("subLinks: "+Arrays.deepToString(subLinks));*/

                //analyze request into arrays for cplex

                int sfcNodes_num =  req.getGraph().getVertexCount();
                double[] sfcNodes =new double[sfcNodes_num];
                String[] sfcNodeFunctions =new String[sfcNodes_num];
                //estimate revenue for request
                double proc_revenue=0;

                writer1.write("Request: " +req.getId()+ " \n");
                ArrayList<Node> req_n = (ArrayList<Node>) getNodes(req.getGraph());
                for (Node node: req_n){
                    sfcNodes[node.getId()]=node.getAvailableCpu();
                    sfcNodeFunctions[node.getId()]=node.getName();
                    writer1.write("Node: " + node.getId() +" CPU: " + node.getAvailableCpu()+"\n");
                    proc_revenue += node.getAvailableCpu();
                }


                double[][] sfcLinks = new double[sfcNodes_num][sfcNodes_num]; //replace witn max

                double bw_revenue=0;
                ArrayList<Link> links =(ArrayList<Link>) getLinks(req.getGraph());
                //double [] reqLinks = new double [links.size()];
                for (Link y: links){
                    Pair<Node> tmp = req.getGraph().getEndpoints(y);
                    sfcLinks[tmp.getFirst().getId()][tmp.getSecond().getId()]  = y.getBandwidth();
                    //reqLinks[y.getId()]  = y.getBandwidth();
                    writer1.write("Link " + y.getId()+ " : "+  y.getId()+ " : "+  tmp.getFirst().getId() + " -> " + tmp.getSecond().getId()+" BW: " +
                            y.getBandwidth()+"\n");
                    bw_revenue+=y.getBandwidth();
                }

                ArrayList<Node> sfc= req.getNodes(req.getGraph());
                //redundant double naming... to lazy to change
                int sizeOfSFC=sfc.size();
                //set revenue to reqMap
                reqMap.setEmbeddingRevenue(bw_revenue+proc_revenue);

                System.out.println("Request: " +req.getId());
                System.out.println("sfcNodes: "+Arrays.toString(sfcNodes));
                System.out.println("sfcLinks: "+Arrays.deepToString(sfcLinks));

                double[][] node_map =new double [subNodesNum][sizeOfSFC];
                getResLifetime(subNodesList);
                if (this.id.contains("RLb")){
                    this.getQL6().setCurrentPenalty(magent.getPernalty());
                    node_map= this.getQL6().placeloads(req.getId(),subNodes,subNodesType, sfcNodes, reqMap, nom_cap, subNodesLife,
                            subNodesHostedReq, 10000, (req.getEndDate()-this.ts));
                } else if (this.id.contains("RA")){
                    this.getRA().setCurrentPenalty(magent.getPernalty());
                    node_map= this.getRA().placeloads(req.getId(),subNodes,subNodesType, sfcNodes, reqMap, nom_cap, subNodesLife,
                            subNodesHostedReq, 10000, (req.getEndDate()-this.ts), substrates, req);
                } else {
                    this.getQL().setCurrentPenalty(magent.getPernalty());
                    node_map= this.getQL().placeloads(req.getId(),subNodes,subNodesType, sfcNodes, reqMap, nom_cap);
                }
                //node_map = optNodeMapping1(subNodesNum,sizeOfSFC, subNodes,sfcNodes,subNodesType, nom_cap);
                //node_map = optNodeMapping(subNodesNum,sizeOfSFC, subNodes,sfcNodes,subNodesType, nom_cap);
		/*	try {
		        System.in.read();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }*/
                //double[][] nodemap =new double [subNodesNum][sizeOfSFC];
                //nodemap = Matrix.transpose(node_map);
                //System.out.println(Arrays.deepToString(node_map));
                //System.out.println(Arrays.deepToString(nodemap));
                LinkedHashMap<Node, Node> nodeMap = new LinkedHashMap<Node, Node> ();
                double embedding_cost=0;
                double cpu_cost=0;
                boolean violation=false;
                System.out.println("IN RL MAPPING for req " +  req.getId());
                for (int u=0;u<subNodesNum;u++){
                    for (int i=0;i<sizeOfSFC;i++){
                        if (node_map[u][i] > 0){
                            if (countNonZero(node_map)==sfcNodes.length) {
                                cpu_cost +=req_n.get(i).getAvailableCpu();
                                nodeMap.put(req_n.get(i),subNodesList.get(u));
                                double updatedValue = (updateSubstrateNode(substrateCopy,subNodesList.get(u),req_n.get(i).getAvailableCpu()));
                                if (updatedValue==Double.MAX_VALUE)
                                    throw new ArithmeticException("Substrate Node Capacity not updated");
                                else if  (updatedValue<0) violation=true;
                            }
                            // System.out.println("VNF  " + i + " to node" + u +"  " +node_map[u][i] + " " + updatedValue + " " + violation);
                        }
                    }
                }
                embedding_cost +=cpu_cost;

                boolean denied=false;
                if (!nodeMap.isEmpty()) {
                    //link mapping requested link - substrate path as list of links
                    MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>> lmap = new
                            MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>>();


                    AlgorithmMCFsimple mcf=new AlgorithmMCFsimple(node_map,substrateCopy,req,nodeMap);
                    mcf.RunMCF();
                    lmap = mcf.getLinkMapping();
                    if (!lmap.isEmpty()) {
                        double bw_cost = mcf.getBWcost();
                        System.out.println("bw_cosL " +bw_cost+ " cpu_cost " +cpu_cost);
                        embedding_cost +=bw_cost;


                        reqMap.setNodeMapping(nodeMap);
                        reqMap.setFlowMapping(lmap);
                        reqMap.accepted();
                        reqMap.setEmbeddingCost(embedding_cost);
                        reqMap.setCPUCost(cpu_cost);
                        reqMap.setBWCost(bw_cost);
                        reqMap.setOverpCPU(violation);
                        if (violation) {
                            System.out.println(ANSI_RED + "VIOLATION!!!!!!!!" +ANSI_RESET );
                        }
                        reqMap.setServersUsed(checkCollocation(nodeMap,subNodesList));
                        //req.setRMapNF(reqMap);
                        System.out.println("###############################################");
                        System.out.println(embedding_cost+ " " + (bw_revenue+proc_revenue) );
                        System.out.println("Node Mapping: " + nodeMap);
                        System.out.println(lmap);
                        System.out.println("###############################################");
                        writer1.write("Node Mapping: "+ nodeMap+ "\n");
                        writer1.write("Link Mapping: "+ lmap+ "\n");


/*			    try {
			        System.in.read();
			    } catch (IOException e) {
			        // TODO Auto-generated catch block
			        e.printStackTrace();
			    }*/

                    }else denied=true;

                }else denied=true;

                if (denied) {
                    System.out.println("Did not found an answer for Mapping");
                    reqMap.denied();
                    //	req.setRMapNF(reqMap);
                }

                long solveEndTime = System.nanoTime();
                long solveTime = solveEndTime - solveStartTime;
                double solTime =  (double) solveTime / 1000000.0;
                reqMap.setSolTime(solTime);
                req.setRMapNF(reqMap);

/*			substrateCopy.print();
			try {
		        System.in.read();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }
		*/
            }//all requests

        } catch (IOException ex) {
            // report
        } finally {
            try {writer.close(); writer1.close();} catch (Exception ex) {/*ignore*/}
        }

        return true;
    }

    int checkCollocation(LinkedHashMap<Node, Node>  nodeMap,ArrayList<Node> subNodesList) {
        int servers=0;
        for (Node current: subNodesList) {
            if (nodeMap.containsValue(current))
                servers++;
        }
        return servers;
    }


    private boolean NFplacement_ILP() {
        Writer writer1=null;
        Writer writer=null;

        try {
            //log all results - substrate snapshot + request/mapping after running the algorithm
            String path = "results/ILP";
            String filename = "substrate_ILP_" +System.currentTimeMillis() + ".txt";
            long name=System.currentTimeMillis();
            writer = new BufferedWriter(new FileWriter(path+File.separator+filename));
            String filename1 = "requests_ILP_" + + name +".txt";
            writer1 = new BufferedWriter(new FileWriter(path+File.separator+filename1));

            //for every request attempt mapping
            int numSFCs=reqs.size();
            for (Request req: reqs){

                long solveStartTime = System.nanoTime();
                ResourceMappingNF reqMap = new ResourceMappingNF(req);
                //analyze substrate into arrays for cplex
                Substrate substrateCopy =  substrates.get(0);
                Graph<Node, Link> sub= substrateCopy.getGraph();
                ArrayList<Node> subNodesList = (ArrayList<Node>) getNodes(substrateCopy.getGraph());
                ArrayList<Link> subLinksList = (ArrayList<Link>) getLinks(substrateCopy.getGraph());
                writer.write("Request: " +req.getId()+ " \n");
                int subNodesNum = subNodesList.size();
                double[] subNodes =  new double[subNodesNum];
                String[] subNodesType =  new String[subNodesNum];

                double nom_cap =0;

                for (Node x: subNodesList){
                    subNodes[x.getId()] = x.getAvailableCpu();
                    subNodesType[x.getId()]= x.getType();
                    if (x.getType().equalsIgnoreCase("Server"))
                        nom_cap=x.getNominalCpu();
                    writer.write("Node: " + x.getId() + " Type: " + x.getType() +" CPU: " + x.getAvailableCpu()  +"\n");


                }
                //Adjacency Matrix Substrate
                double[][] subLinks = new double[subNodesNum][subNodesNum];
                //double [] resLinks = new double [subLinksList.size()];
                for (Link y: subLinksList){
                    Pair<Node> tmp = sub.getEndpoints(y);
                    subLinks[tmp.getFirst().getId()][tmp.getSecond().getId()]  = y.getBandwidth();
                    //resLinks[y.getId()]  = y.getBandwidth();
                    writer.write("Link " + y.getId()+ " : "+ tmp.getFirst().getId() + " -> " + tmp.getSecond().getId()+" BW: " +
                            y.getBandwidth()+"\n");
                }



                //analyze request into arrays for cplex

                int sfcNodes_num =  req.getGraph().getVertexCount();
                double[] sfcNodes =new double[sfcNodes_num];
                String[] sfcNodeFunctions =new String[sfcNodes_num];
                //estimate revenue for request
                double proc_revenue=0;

                writer1.write("Request: " +req.getId()+ " \n");
                ArrayList<Node> req_n = (ArrayList<Node>) getNodes(req.getGraph());
                for (Node node: req_n){
                    sfcNodes[node.getId()]=node.getAvailableCpu();
                    sfcNodeFunctions[node.getId()]=node.getName();
                    writer1.write("Node: " + node.getId() +" CPU: " + node.getAvailableCpu()+"\n");
                    proc_revenue += node.getAvailableCpu();
                }


                double[][] sfcLinks = new double[sfcNodes_num][sfcNodes_num]; //replace witn max

                double bw_revenue=0;
                ArrayList<Link> links =(ArrayList<Link>) getLinks(req.getGraph());
                //double [] reqLinks = new double [links.size()];
                for (Link y: links){
                    Pair<Node> tmp = req.getGraph().getEndpoints(y);
                    sfcLinks[tmp.getFirst().getId()][tmp.getSecond().getId()]  = y.getBandwidth();
                    //reqLinks[y.getId()]  = y.getBandwidth();
                    writer1.write("Link " + y.getId()+ " : "+  y.getId()+ " : "+  tmp.getFirst().getId() + " -> " + tmp.getSecond().getId()+" BW: " +
                            y.getBandwidth()+"\n");
                    bw_revenue+=y.getBandwidth();
                }

                ArrayList<Node> sfc= req.getNodes(req.getGraph());
                //redundant double naming... to lazy to change
                int sizeOfSFC=sfc.size();
                int numNodesSFC = sfc.size();
                //set revenue to reqMap
                reqMap.setEmbeddingRevenue(bw_revenue+proc_revenue);

                System.out.println("Request: " +req.getId());
                System.out.println("sfcNodes: "+Arrays.toString(sfcNodes));
                System.out.println("sfcLinks: "+Arrays.toString(sfcLinks[0]));

                double[][] node_map =new double [subNodesNum][sizeOfSFC];
                node_map = optNodeMapping(subNodesNum,sizeOfSFC, subNodes,sfcNodes,subNodesType, nom_cap);
                //node_map = optNodeMapping(subNodesNum,sizeOfSFC, subNodes,sfcNodes,subNodesType, nom_cap);
		/*	try {
		        System.in.read();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }*/
                //double[][] nodemap =new double [subNodesNum][sizeOfSFC];
                //nodemap = Matrix.transpose(node_map);
                //System.out.println(Arrays.deepToString(node_map));
                //System.out.println(Arrays.deepToString(nodemap));
                LinkedHashMap<Node, Node> nodeMap = new LinkedHashMap<Node, Node> ();
                double embedding_cost=0;
                double cpu_cost=0;
                boolean violation=false;
                System.out.println("IN RL MAPPING");
                for (int u=0;u<subNodesNum;u++){
                    for (int i=0;i<sizeOfSFC;i++){
                        if (node_map[u][i] > 0){
                            cpu_cost +=req_n.get(i).getAvailableCpu();
                            nodeMap.put(req_n.get(i),subNodesList.get(u));
                            double updatedValue = (updateSubstrateNode(substrateCopy,subNodesList.get(u),req_n.get(i).getAvailableCpu()));
                            if (updatedValue==Double.MAX_VALUE)
                                throw new ArithmeticException("Substrate Node Capacity not updated");
                            else if  (updatedValue<0) violation=true;
                            System.out.println("VNF  " + i + " to node" + u +"  " +node_map[u][i]);
                        }
                    }
                }
                embedding_cost +=cpu_cost;

                boolean denied=false;
                if (!nodeMap.isEmpty()) {
                    //link mapping requested link - substrate path as list of links
                    MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>> lmap = new
                            MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>>();


                    AlgorithmMCFsimple mcf=new AlgorithmMCFsimple(node_map,substrateCopy,req,nodeMap);
                    mcf.RunMCF();
                    lmap = mcf.getLinkMapping();
                    if (!lmap.isEmpty()) {
                        double bw_cost = mcf.getBWcost();
                        embedding_cost +=bw_cost;


                        reqMap.setNodeMapping(nodeMap);
                        reqMap.setFlowMapping(lmap);
                        reqMap.accepted();
                        reqMap.setEmbeddingCost(embedding_cost);
                        reqMap.setCPUCost(cpu_cost);
                        reqMap.setBWCost(bw_cost);
                        reqMap.setOverpCPU(violation);
                        System.out.println("###############################################");
                        System.out.println(embedding_cost+ " " + (bw_revenue+proc_revenue) );
                        System.out.println("Node Mapping: " + nodeMap);
                        System.out.println(lmap);
                        System.out.println("###############################################");
                        writer1.write("Node Mapping: "+ nodeMap+ "\n");
                        writer1.write("Link Mapping: "+ lmap+ "\n");


                        try {
                            System.in.read();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }else denied=true;

                }else denied=true;

                if (denied) {
                    System.out.println("Did not found an answer for Mapping");
                    reqMap.denied();

                }


                long solveEndTime = System.nanoTime();
                long solveTime = solveEndTime - solveStartTime;
                double solTime =  (double) solveTime / 1000000.0;
                reqMap.setSolTime(solTime);
                req.setRMapNF(reqMap);

                //substrateCopy.print();
/*			try {
		        System.in.read();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }*/

            }//all requests

        } catch (IOException ex) {
            // report
        } finally {
            try {writer.close(); writer1.close();} catch (Exception ex) {/*ignore*/}
        }

        return true;
    }


    private boolean greedy() {
        Writer writer1=null;
        Writer writer=null;

        try {
            //log all results - substrate snapshot + request/mapping after running the algorithm
            String path = "results/Greedy_max";
            String filename = "substrate_greedy_" +System.currentTimeMillis() + ".txt";
            long name=System.currentTimeMillis();
            writer = new BufferedWriter(new FileWriter(path+File.separator+filename));
            String filename1 = "requests_greedy_" + + name +".txt";
            writer1 = new BufferedWriter(new FileWriter(path+File.separator+filename1));

            //for every request attempt mapping
            for (Request req: reqs){

                long solveStartTime = System.nanoTime();
                ResourceMappingNF reqMap = new ResourceMappingNF(req);
                //analyze substrate into arrays for cplex
                Substrate substrateCopy =  substrates.get(0);
                Graph<Node, Link> sub= substrateCopy.getGraph();
                ArrayList<Node> subNodesList = (ArrayList<Node>) getNodes(substrateCopy.getGraph());
                ArrayList<Link> subLinksList = (ArrayList<Link>) getLinks(substrateCopy.getGraph());
                writer.write("Request: " +req.getId()+ " \n");
                int subNodesNum = subNodesList.size();
                double[] subNodes =  new double[subNodesNum];
                String[] subNodesType =  new String[subNodesNum];

                double nom_cap =0;

                for (Node x: subNodesList){
                    subNodes[x.getId()] = x.getAvailableCpu();
                    subNodesType[x.getId()]= x.getType();
                    if (x.getType().equalsIgnoreCase("Server"))
                        nom_cap=x.getNominalCpu();
                    writer.write("Node: " + x.getId() + " Type: " + x.getType() +" CPU: " + x.getAvailableCpu()  +"\n");


                }
                //Adjacency Matrix Substrate
                double[][] subLinks = new double[subNodesNum][subNodesNum];
                //double [] resLinks = new double [subLinksList.size()];
                for (Link y: subLinksList){
                    Pair<Node> tmp = sub.getEndpoints(y);
                    subLinks[tmp.getFirst().getId()][tmp.getSecond().getId()]  = y.getBandwidth();
                    //resLinks[y.getId()]  = y.getBandwidth();
                    writer.write("Link " + y.getId()+ " : "+ tmp.getFirst().getId() + " -> " + tmp.getSecond().getId()+" BW: " +
                            y.getBandwidth()+"\n");
                }



                //analyze request into arrays for cplex

                int sfcNodes_num =  req.getGraph().getVertexCount();
                double[] sfcNodes =new double[sfcNodes_num];
                String[] sfcNodeFunctions =new String[sfcNodes_num];
                //estimate revenue for request
                double proc_revenue=0;

                writer1.write("Request: " +req.getId()+ " \n");
                ArrayList<Node> req_n = (ArrayList<Node>) getNodes(req.getGraph());
                for (Node node: req_n){
                    sfcNodes[node.getId()]=node.getAvailableCpu();
                    sfcNodeFunctions[node.getId()]=node.getName();
                    writer1.write("Node: " + node.getId() +" CPU: " + node.getAvailableCpu()+"\n");
                    proc_revenue += node.getAvailableCpu();
                }


                double[][] sfcLinks = new double[sfcNodes_num][sfcNodes_num]; //replace witn max

                double bw_revenue=0;
                ArrayList<Link> links =(ArrayList<Link>) getLinks(req.getGraph());
                //double [] reqLinks = new double [links.size()];
                for (Link y: links){
                    Pair<Node> tmp = req.getGraph().getEndpoints(y);
                    sfcLinks[tmp.getFirst().getId()][tmp.getSecond().getId()]  = y.getBandwidth();
                    //reqLinks[y.getId()]  = y.getBandwidth();
                    writer1.write("Link " + y.getId()+ " : "+  y.getId()+ " : "+  tmp.getFirst().getId() + " -> " + tmp.getSecond().getId()+" BW: " +
                            y.getBandwidth()+"\n");
                    bw_revenue+=y.getBandwidth();
                }

                ArrayList<Node> sfc= req.getNodes(req.getGraph());
                //redundant double naming... to lazy to change
                int sizeOfSFC=sfc.size();
                //set revenue to reqMap
                reqMap.setEmbeddingRevenue(bw_revenue+proc_revenue);

                System.out.println("Request: " +req.getId());
                System.out.println("sfcNodes: "+Arrays.toString(sfcNodes));
                System.out.println("sfcLinks: "+Arrays.toString(sfcLinks[0]));

                double[][] node_map =new double [subNodesNum][sizeOfSFC];
                node_map = greedy(subNodesNum,sizeOfSFC, subNodes,sfcNodes,subNodesType, nom_cap);
                //node_map = optNodeMapping(subNodesNum,sizeOfSFC, subNodes,sfcNodes,subNodesType, nom_cap);
		/*	try {
		        System.in.read();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }*/
                //double[][] nodemap =new double [subNodesNum][sizeOfSFC];
                //nodemap = Matrix.transpose(node_map);
                //System.out.println(Arrays.deepToString(node_map));
                //System.out.println(Arrays.deepToString(nodemap));

                LinkedHashMap<Node, Node> nodeMap = new LinkedHashMap<Node, Node> ();
                double embedding_cost=0;
                double cpu_cost=0;
                boolean violation=false;
                System.out.println("IN greedy MAPPING: Request " +req.getId()+ " mapped" );
                for (int u=0;u<subNodesNum;u++){
                    for (int i=0;i<sizeOfSFC;i++){
                        if (node_map[u][i] > 0){
                            cpu_cost +=req_n.get(i).getAvailableCpu();
                            nodeMap.put(req_n.get(i),subNodesList.get(u));
                            if (countNonZero(node_map)==sfcNodes.length) {
                                double updatedValue = (updateSubstrateNode(substrateCopy,subNodesList.get(u),req_n.get(i).getAvailableCpu()));
                                if (updatedValue==Double.MAX_VALUE)
                                    throw new ArithmeticException("Substrate Node Capacity not updated");
                                else if  (updatedValue<0) violation=true;
                            }
                            System.out.println("VNF  " + i + " to node" + u +"  " +node_map[u][i]);
                        }
                    }
                }
                embedding_cost +=cpu_cost;

                boolean denied=false;
                //req.print();
                if (countNonZero(node_map)==sfcNodes.length) {
                    //if (!nodeMap.isEmpty()) {
                    //link mapping requested link - substrate path as list of links
                    MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>> lmap = new
                            MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>>();


                    AlgorithmMCFsimple mcf=new AlgorithmMCFsimple(node_map,substrateCopy,req,nodeMap);
                    mcf.RunMCF();
                    lmap = mcf.getLinkMapping();
                    if (!lmap.isEmpty()) {
                        double bw_cost = mcf.getBWcost();
                        embedding_cost +=bw_cost;


                        reqMap.setNodeMapping(nodeMap);
                        reqMap.setFlowMapping(lmap);
                        reqMap.accepted();
                        reqMap.setEmbeddingCost(embedding_cost);
                        reqMap.setCPUCost(cpu_cost);
                        reqMap.setBWCost(bw_cost);
                        reqMap.setOverpCPU(violation);
                        reqMap.setServersUsed(checkCollocation(nodeMap,subNodesList));
                        System.out.println("###############################################");
                        System.out.println(embedding_cost+ " " + (bw_revenue+proc_revenue) );
                        System.out.println("Node Mapping: " + nodeMap);
                        System.out.println(lmap);
                        System.out.println("###############################################");
                        writer1.write("Node Mapping: "+ nodeMap+ "\n");
                        writer1.write("Link Mapping: "+ lmap+ "\n");


/*			    try {
			        System.in.read();
			    } catch (IOException e) {
			        // TODO Auto-generated catch block
			        e.printStackTrace();
			    }*/

                    }else denied=true;

                }else denied=true;

                if (denied) {
                    System.out.println("Did not found an answer for Mapping");
                    reqMap.denied();

                }


                long solveEndTime = System.nanoTime();
                long solveTime = solveEndTime - solveStartTime;
                double solTime =  (double) solveTime / 1000000.0;
                reqMap.setSolTime(solTime);
                req.setRMapNF(reqMap);

                //substrateCopy.print();

            }//all requests

        } catch (IOException ex) {
            // report
        } finally {
            try {writer.close(); writer1.close();} catch (Exception ex) {/*ignore*/}
        }

        return true;
    }

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
                System.out.println("subNodes: " +subNodes[i] + " " +i);
            }

        }


        for (Entry<Integer, Double> entry : hMap.entrySet()) {
            list.add(entry.getValue());
        }
        System.out.println(list);
        Collections.sort(list, new ComparatorDouble());
        Collections.reverse(list);
        for (Double str : list) {
            for (Entry<Integer, Double> entry : hMap.entrySet()) {
                if (entry.getValue().equals(str)) {
                    sortedMap.put(entry.getKey(), str);
                }
            }
        }
        System.out.println(sortedMap);

        for (int i=0;i<sfcNodes.length;i++) {
            for (Entry<Integer, Double> sub_node : sortedMap.entrySet()) {
                if (!(sfcNodes[i]>sub_node.getValue())) {
                    int u=sub_node.getKey();
                    System.out.println("[Key] : " + sub_node.getKey() + " [Value] : " + sub_node.getValue() + " " +sfcNodes[i]);
                    node_map[u][i]=1.0;
                    sortedMap.put(sub_node.getKey(),(Double)(sub_node.getValue()-sfcNodes[i]));
                    break;
                }
            }
        }

/*     if (countNonZero(node_map)==sfcNodes.length) {
    	 return node_map;
     }

     //resort
     list = new ArrayList<>();
     for (Entry<Integer, Double> entry : sortedMap.entrySet()) {
         list.add(entry.getValue());
      }

     Collections.sort(list, new ComparatorDouble());
     Collections.reverse(list);
     System.out.println(list);
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
    			 System.out.println(i + " to sub  " +u);
    			 System.out.println("[Key] : " + sub_node.getKey() + " [Value] : " + sub_node.getValue());
    			 node_map[u][i]=1.0;
    			 break;
    		 }
    	 }
     }*/
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

    private boolean NFplacement_ILP1() {
        Writer writer1=null;
        Writer writer=null;

        try {
            //log all results - substrate snapshot + request/mapping after running the algorithm
            String path = "results/ILP1";
            String filename = "substrate_ILP1_" +System.currentTimeMillis() + ".txt";
            long name=System.currentTimeMillis();
            writer = new BufferedWriter(new FileWriter(path+File.separator+filename));
            String filename1 = "requests_ILP1_" + + name +".txt";
            writer1 = new BufferedWriter(new FileWriter(path+File.separator+filename1));

            //for every request attempt mapping
            int numSFCs=reqs.size();
            for (Request req: reqs){

                long solveStartTime = System.nanoTime();
                ResourceMappingNF reqMap = new ResourceMappingNF(req);
                //analyze substrate into arrays for cplex
                Substrate substrateCopy =  substrates.get(0);
                Graph<Node, Link> sub= substrateCopy.getGraph();
                ArrayList<Node> subNodesList = (ArrayList<Node>) getNodes(substrateCopy.getGraph());
                ArrayList<Link> subLinksList = (ArrayList<Link>) getLinks(substrateCopy.getGraph());
                writer.write("Request: " +req.getId()+ " \n");
                int subNodesNum = subNodesList.size();
                double[] subNodes =  new double[subNodesNum];
                String[] subNodesType =  new String[subNodesNum];

                double nom_cap =0;

                for (Node x: subNodesList){
                    subNodes[x.getId()] = x.getAvailableCpu();
                    subNodesType[x.getId()]= x.getType();
                    if (x.getType().equalsIgnoreCase("Server"))
                        nom_cap=x.getNominalCpu();
                    writer.write("Node: " + x.getId() + " Type: " + x.getType() +" CPU: " + x.getAvailableCpu()  +"\n");


                }
                //Adjacency Matrix Substrate
                double[][] subLinks = new double[subNodesNum][subNodesNum];
                //double [] resLinks = new double [subLinksList.size()];
                for (Link y: subLinksList){
                    Pair<Node> tmp = sub.getEndpoints(y);
                    subLinks[tmp.getFirst().getId()][tmp.getSecond().getId()]  = y.getBandwidth();
                    //resLinks[y.getId()]  = y.getBandwidth();
                    writer.write("Link " + y.getId()+ " : "+ tmp.getFirst().getId() + " -> " + tmp.getSecond().getId()+" BW: " +
                            y.getBandwidth()+"\n");
                }



                //analyze request into arrays for cplex

                int sfcNodes_num =  req.getGraph().getVertexCount();
                double[] sfcNodes =new double[sfcNodes_num];
                String[] sfcNodeFunctions =new String[sfcNodes_num];
                //estimate revenue for request
                double proc_revenue=0;

                writer1.write("Request: " +req.getId()+ " \n");
                ArrayList<Node> req_n = (ArrayList<Node>) getNodes(req.getGraph());
                for (Node node: req_n){
                    sfcNodes[node.getId()]=node.getAvailableCpu();
                    sfcNodeFunctions[node.getId()]=node.getName();
                    writer1.write("Node: " + node.getId() +" CPU: " + node.getAvailableCpu()+"\n");
                    proc_revenue += node.getAvailableCpu();
                }


                double[][] sfcLinks = new double[sfcNodes_num][sfcNodes_num]; //replace witn max

                double bw_revenue=0;
                ArrayList<Link> links =(ArrayList<Link>) getLinks(req.getGraph());
                //double [] reqLinks = new double [links.size()];
                for (Link y: links){
                    Pair<Node> tmp = req.getGraph().getEndpoints(y);
                    sfcLinks[tmp.getFirst().getId()][tmp.getSecond().getId()]  = y.getBandwidth();
                    //reqLinks[y.getId()]  = y.getBandwidth();
                    writer1.write("Link " + y.getId()+ " : "+  y.getId()+ " : "+  tmp.getFirst().getId() + " -> " + tmp.getSecond().getId()+" BW: " +
                            y.getBandwidth()+"\n");
                    bw_revenue+=y.getBandwidth();
                }

                ArrayList<Node> sfc= req.getNodes(req.getGraph());
                //redundant double naming... to lazy to change
                int sizeOfSFC=sfc.size();
                int numNodesSFC = sfc.size();
                //set revenue to reqMap
                reqMap.setEmbeddingRevenue(bw_revenue+proc_revenue);

                System.out.println("Request: " +req.getId());
                System.out.println("sfcNodes: "+Arrays.toString(sfcNodes));
                System.out.println("sfcLinks: "+Arrays.toString(sfcLinks[0]));

                double[][] node_map =new double [subNodesNum][sizeOfSFC];
                node_map = optNodeMapping2(subNodesNum,sizeOfSFC, subNodes,sfcNodes,subNodesType, nom_cap);
                //node_map = optNodeMapping(subNodesNum,sizeOfSFC, subNodes,sfcNodes,subNodesType, nom_cap);
		/*	try {
		        System.in.read();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }*/
                //double[][] nodemap =new double [subNodesNum][sizeOfSFC];
                //nodemap = Matrix.transpose(node_map);
                //System.out.println(Arrays.deepToString(node_map));
                //System.out.println(Arrays.deepToString(nodemap));
                LinkedHashMap<Node, Node> nodeMap = new LinkedHashMap<Node, Node> ();
                double embedding_cost=0;
                double cpu_cost=0;
                boolean violation=false;
                System.out.println("IN RL MAPPING");
                for (int u=0;u<subNodesNum;u++){
                    for (int i=0;i<sizeOfSFC;i++){
                        if (node_map[u][i] > 0){
                            cpu_cost +=req_n.get(i).getAvailableCpu();
                            nodeMap.put(req_n.get(i),subNodesList.get(u));
                            double updatedValue = (updateSubstrateNode(substrateCopy,subNodesList.get(u),req_n.get(i).getAvailableCpu()));
                            if (updatedValue==Double.MAX_VALUE)
                                throw new ArithmeticException("Substrate Node Capacity not updated");
                            else if  (updatedValue<0) violation=true;
                            System.out.println("VNF  " + i + " to node" + u +"  " +node_map[u][i]);
                        }
                    }
                }
                embedding_cost +=cpu_cost;

                boolean denied=false;
                if (!nodeMap.isEmpty()) {
                    //link mapping requested link - substrate path as list of links
                    MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>> lmap = new
                            MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>>();


                    AlgorithmMCFsimple mcf=new AlgorithmMCFsimple(node_map,substrateCopy,req,nodeMap);
                    mcf.RunMCF();
                    lmap = mcf.getLinkMapping();
                    if (!lmap.isEmpty()) {
                        double bw_cost = mcf.getBWcost();
                        embedding_cost +=bw_cost;


                        reqMap.setNodeMapping(nodeMap);
                        reqMap.setFlowMapping(lmap);
                        reqMap.accepted();
                        reqMap.setEmbeddingCost(embedding_cost);
                        reqMap.setCPUCost(cpu_cost);
                        reqMap.setBWCost(bw_cost);
                        reqMap.setOverpCPU(violation);
                        System.out.println("###############################################");
                        System.out.println(embedding_cost+ " " + (bw_revenue+proc_revenue) );
                        System.out.println("Node Mapping: " + nodeMap);
                        System.out.println(lmap);
                        System.out.println("###############################################");
                        writer1.write("Node Mapping: "+ nodeMap+ "\n");
                        writer1.write("Link Mapping: "+ lmap+ "\n");

/*
			    try {
			        System.in.read();
			    } catch (IOException e) {
			        // TODO Auto-generated catch block
			        e.printStackTrace();
			    }*/

                    }else denied=true;

                }else denied=true;

                if (denied) {
                    System.out.println("Did not found an answer for Mapping");
                    reqMap.denied();

                }


                long solveEndTime = System.nanoTime();
                long solveTime = solveEndTime - solveStartTime;
                double solTime =  (double) solveTime / 1000000.0;
                reqMap.setSolTime(solTime);
                req.setRMapNF(reqMap);

                //substrateCopy.print();
/*			try {
		        System.in.read();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }*/

            }//all requests

        } catch (IOException ex) {
            // report
        } finally {
            try {writer.close(); writer1.close();} catch (Exception ex) {/*ignore*/}
        }

        return true;
    }

    private double[][] optNodeMapping1(int subNodesNum, int sizeOfSFC, double[] subNodes, double[] sfcNodes,
                                       String[] subNodesType, double nom_cap){
        double[][] node_map = new double [subNodesNum][sizeOfSFC];

        try {
            IloCplex cplex = new IloCplex();
            cplex.setParam(IloCplex.DoubleParam.TiLim, 60);
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

            // x^i_u if instance j of NF i is installed on susbtrate node u then x^i_u=1	\
            IloNumVar[][][] y = new IloNumVar[subNodesNum][][];
            for (int u=0;u<subNodesNum;u++){
                y[u]=new IloNumVar[sizeOfSFC][];
                for(int i=0; i< sizeOfSFC; i++){
                    y[u][i]=new IloNumVar[sizeOfSFC];
                    for(int j=0; j< sizeOfSFC; j++){
                        y[u][i]=cplex.numVarArray(sizeOfSFC, 0, 1, IloNumVarType.Int);
                    }
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
                    //	double mult = sfcNodes[i] *cpu;
                    cost.addTerm(cpu, x[u][i]);
                }
            }

            IloLinearNumExpr cost2 = cplex.linearNumExpr();
            for (int u=0;u<subNodesNum;u++){
                //double cpu = 1-subNodes[u];
                for(int i=0; i< sizeOfSFC; i++){
                    for(int j=0; j< sizeOfSFC; j++){
                        cost2.addTerm(1, y[u][i][j]);
                    }
                }
            }


            IloNumExpr expr = cplex.sum(cost2,cost);
            cplex.addMinimize(expr);



            /*****************************Placement Constraints **************************************************/


            // nf instance can be mapped to at most 1 substrate node
            for (int i=0;i<sizeOfSFC;i++){
                IloLinearNumExpr assignment1 = cplex.linearNumExpr();
                for (int u=0;u<subNodesNum;u++){
                    assignment1.addTerm(1,x[u][i]);
                }
                cplex.addEq(assignment1, 1);
            }

            //deny mapping to switches

            for (int u=0;u<subNodesNum;u++){
                IloLinearNumExpr assignment4 = cplex.linearNumExpr();
                if (subNodesType[u].equalsIgnoreCase("Switch")) {
                    for (int i=0;i<sizeOfSFC;i++){
                        assignment4.addTerm(1,x[u][i]);
                    }
                    cplex.addEq(assignment4, 0);
                }
            }



            for (int u=0;u<subNodesNum;u++){
                for(int i=0; i< sizeOfSFC; i++){
                    for(int j=0; j< sizeOfSFC; j++){
                        IloLinearNumExpr assignment2 = cplex.linearNumExpr();
                        assignment2.addTerm(1,x[u][i]);
                        assignment2.addTerm(1,x[u][j]);
                        assignment2.addTerm(-1,y[u][i][j]);
                        cplex.addLe(assignment2, 1);
                    }
                }
            }


            for (int u=0;u<subNodesNum;u++){
                for(int i=0; i< sizeOfSFC; i++){
                    IloLinearNumExpr assignment3 = cplex.linearNumExpr();
                    assignment3.addTerm(-1,x[u][i]);
                    for(int j=0; j< sizeOfSFC; j++){
                        assignment3.addTerm(1,y[u][i][j]);
                    }
                    cplex.addEq(assignment3, 0);
                }
            }


            /*****************************Solve **************************************************/
            cplex.exportModel("lpex_nodemap_ILP.lp");
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


    private double[][] optNodeMapping2(int subNodesNum, int sizeOfSFC, double[] subNodes, double[] sfcNodes,
                                       String[] subNodesType, double nom_cap){
        double[][] node_map = new double [subNodesNum][sizeOfSFC];

        try {
            IloCplex cplex = new IloCplex();
            cplex.setParam(IloCplex.DoubleParam.TiLim, 60);
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
                double cpu = subNodes[u]/nom_cap;
                for(int i=0; i< sizeOfSFC; i++){
                    //double mult =sfcNodes[i]* cpu;
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


            //node capacity
            for (int u=0;u<subNodesNum;u++){
                IloLinearNumExpr cpuReq = cplex.linearNumExpr();
                for(int i=0; i<sizeOfSFC; i++){
                    double cpuNF = sfcNodes[i];
                    cpuReq.addTerm(cpuNF,x[u][i]);
                }
                double cpu = subNodes[u];
                cplex.addLe(cpuReq, cpu);
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





    private double[][] optNodeMapping(int subNodesNum, int sizeOfSFC, double[] subNodes, double[] sfcNodes,
                                      String[] subNodesType, double nom_cap){
        double[][] node_map = new double [subNodesNum][sizeOfSFC];

        try {
            IloCplex cplex = new IloCplex();
            cplex.setParam(IloCplex.DoubleParam.TiLim, 60);
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
                double cpu = subNodes[u]/nom_cap;
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


/*			for (int u=0;u<subNodesNum;u++){
				IloLinearNumExpr assignment2 = cplex.linearNumExpr();
				if (subNodesType[u].equalsIgnoreCase("Switch")) {
					for (int i=0;i<sizeOfSFC;i++){
						assignment2.addTerm(1,x[u][i]);
					}
				cplex.addEq(assignment2, 0);
				}
			}*/


            //node capacity
            for (int u=0;u<subNodesNum;u++){
                IloLinearNumExpr cpuReq = cplex.linearNumExpr();
                for(int i=0; i<sizeOfSFC; i++){
                    double cpuNF = sfcNodes[i];
                    cpuReq.addTerm(cpuNF,x[u][i]);
                }
                double cpu = subNodes[u];
                cplex.addLe(cpuReq, cpu);
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

    private boolean NFplacement_simple() {
        System.out.println("AlgorithmNF.java 2916 NFplacement_simple, " + id);
        Writer writer1=null;
        Writer writer=null;
        try {
            //log all results - substrate snapshot + request/mapping after running the algorithm
            String path = "results/"+this.id;
            new File(path).mkdirs();
            String filename = "substrate_"+this.id+"_" +System.currentTimeMillis() + ".txt";
            long name=System.currentTimeMillis();
            writer = new BufferedWriter(new FileWriter(path+File.separator+filename));
            String filename1 = "requests_"+this.id+"_"+ + name +".txt";
            writer1 = new BufferedWriter(new FileWriter(path+File.separator+filename1));
            //System.out.println("Filenames:");
            //System.out.println(path+File.separator+filename);
            //System.out.println(path+File.separator+filename1);



            //for every request attempt mapping
//	/int numSFCs=reqs.size();
            for (Request req: reqs){
                long solveStartTime = System.nanoTime();
                ResourceMappingNF reqMap = new ResourceMappingNF(req);
                //analyze substrate into arrays for cplex
                Substrate substrateCopy =  substrates.get(0);
                Graph<Node, Link> sub= substrateCopy.getGraph();
                ArrayList<Node> subNodesList = (ArrayList<Node>) getNodes(substrateCopy.getGraph());
                ArrayList<Link> subLinksList = (ArrayList<Link>) getLinks(substrateCopy.getGraph());
                writer.write("Request: " +req.getId()+ " \n");
                int subNodesNum = subNodesList.size();
                double[] subNodes =  new double[subNodesNum];

                for (Node x: subNodesList){
                    subNodes[x.getId()] = x.getAvailableCpu();
                    writer.write("Node: " + x.getId() + " Type: " + x.getType() +" CPU: " + x.getAvailableCpu() + "\n");
                }
                //Adjacency Matrix Substrate
                double[][] subLinks = new double[subNodesNum][subNodesNum];
                for (Link y: subLinksList){
                    Pair<Node> tmp = sub.getEndpoints(y);
                    subLinks[tmp.getFirst().getId()][tmp.getSecond().getId()]  = y.getBandwidth();
                    writer.write("Link " + y.getId()+ " : "+ tmp.getFirst().getId() + " -> " + tmp.getSecond().getId()+" BW: " +
                            y.getBandwidth()+"\n");
                }

/*		System.out.println("subNodesNum: "+subNodesNum);
		System.out.println("subCap: "+Arrays.toString(subNodes));
		System.out.println("subLinks: "+Arrays.deepToString(subLinks));*/

                //analyze request into arrays for cplex

                int sfcNodes_num =  req.getGraph().getVertexCount();
                double[] sfcNodes =new double[sfcNodes_num];
                String[] sfcNodeFunctions =new String[sfcNodes_num];
                //estimate revenue for request
                double proc_revenue=0;

                writer1.write("Request: " +req.getId()+ " \n");
                ArrayList<Node> req_n = (ArrayList<Node>) getNodes(req.getGraph());
                for (Node node: req_n){
                    sfcNodes[node.getId()]=node.getAvailableCpu();
                    sfcNodeFunctions[node.getId()]=node.getName();
                    writer1.write("Node: " + node.getId() +" CPU: " + node.getAvailableCpu()+"\n");
                    proc_revenue += node.getAvailableCpu();
                }


                double[][] sfcLinks = new double[sfcNodes_num][sfcNodes_num]; //replace witn max

                double bw_revenue=0;
                ArrayList<Link> links =(ArrayList<Link>) getLinks(req.getGraph());
                for (Link y: links){
                    Pair<Node> tmp = req.getGraph().getEndpoints(y);
                    sfcLinks[tmp.getFirst().getId()][tmp.getSecond().getId()]  = y.getBandwidth();
                    writer1.write("Link " + y.getId()+ " : "+  y.getId()+ " : "+  tmp.getFirst().getId() + " -> " + tmp.getSecond().getId()+" BW: " +
                            y.getBandwidth()+"\n");
                    bw_revenue+=y.getBandwidth();
                }

                ArrayList<Node> sfc= req.getNodes(req.getGraph());
                //redundant double naming... to lazy to change
                int sizeOfSFC=sfc.size();
                int numNodesSFC = sfc.size();
                //set revenue to reqMap
                reqMap.setEmbeddingRevenue(bw_revenue+proc_revenue);

                //System.out.println("sfcNodes: "+Arrays.toString(sfcNodes));
                //System.out.println("sfcLinks: "+Arrays.toString(sfcLinks[0]));



                try {
                    IloCplex cplex = new IloCplex();
                    cplex.setParam(IloCplex.DoubleParam.TiLim, 600); //10 mins
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

                    //f^ij_uv
                    IloNumVar[][][][] fl = new IloNumVar[subNodesNum][subNodesNum][][];
                    for (int u=0;u<subNodesNum;u++){
                        fl[u]=new IloNumVar[subNodesNum][][];
                        for (int v=0;v<subNodesNum;v++){
                            fl[u][v]=new IloNumVar[sizeOfSFC][];
                            for(int i=0; i< sizeOfSFC; i++){
                                fl[u][v][i]=new IloNumVar[sizeOfSFC];
                                for(int j=0; j<sizeOfSFC; j++){
                                    fl[u][v][i]=cplex.numVarArray(sizeOfSFC, 0, 1000000000);
                                }
                            }
                        }
                    }

/*****************************Objective Function **************************************************/

                    IloLinearNumExpr cost = cplex.linearNumExpr();
                    ////////////////////////////////////////////////////////////////////////
                    //It builds the first summation of the objective function//////////////
                    //////////////////////////////////////////////////////////////////////
/*		for (int u=0;u<subNodesNum;u++){
			for(int i=0; i< sizeOfSFC; i++){
				double cpu = sfcNodes[i];
				cost.addTerm(cpu, x[u][i]);
			}
		}*/

                    ////////////////////////////////////////////////////////////////////////
                    //It builds the second summation of the objective function//////////////
                    /////////////////////////////////////////////////////////////////////\
                    IloLinearNumExpr flows = cplex.linearNumExpr();

//			double demand =0;
//			double cpu_demand =0;
//			for(int j=0; j<numNodesSFC; j++){
//				cpu_demand  += sfcNodes[j];
//			}
//
//			//calculate normalization factor
//			for(int i=0;i< numNodesSFC; i++){
//				for(int j=0; j<numNodesSFC; j++){
//					demand +=sfcLinks[i][j];
//				}
//			}
                    for(int i=0; i< numNodesSFC; i++){
                        for(int j=0; j<numNodesSFC; j++){
                            //demand = demand+sfcLinks[f][k][m];
                            for (int u=0;u<subNodesNum;u++){
                                for (int v=0;v<subNodesNum;v++){
                                    flows.addTerm(1, fl[u][v][i][j]);
                                    //flows.addTerm(cpu_demand/demand, fl[u][v][i][j]);
                                    //System.out.println(u + " " +v + " " +f + " " + k + " " +m);
                                }
                            }

                        }
                    }


                    //create objective minimization
                    IloNumExpr expr = cplex.sum(flows,cost);
                    cplex.addMinimize(expr);


/*****************************Capacity Constraints **************************************************/

                    //node capacity
                    for (int u=0;u<subNodesNum;u++){
                        IloLinearNumExpr cpuReq = cplex.linearNumExpr();
                        for(int i=0; i<numNodesSFC; i++){
                            double cpuNF = sfcNodes[i];
                            cpuReq.addTerm(cpuNF,x[u][i]);
                        }
                        double cpu = subNodes[u];
                        cplex.addLe(cpuReq, cpu);
                    }

                    //link capacity
                    for (int u=0;u<subNodesNum;u++){
                        for (int v=0;v<subNodesNum;v++){
                            IloLinearNumExpr bwReq = cplex.linearNumExpr();
                            for(int i=0; i< numNodesSFC; i++){
                                for(int j=0; j<numNodesSFC; j++){
                                    bwReq.addTerm(1,fl[u][v][i][j]);
                                }
                            }
                            double cap = subLinks[u][v];
                            cplex.addLe(bwReq, cap);
                        }
                    }

/*****************************Placement and Assignment Constraints **************************************************/


                    // nf instance can be mapped to at most 1 substrate node
                    for (int i=0;i<numNodesSFC;i++){
                        IloLinearNumExpr assignment1 = cplex.linearNumExpr();
                        for (int u=0;u<subNodesNum;u++){
                            assignment1.addTerm(1,x[u][i]);
                        }
                        cplex.addEq(assignment1, 1);
                    }


/*****************************Flow Constraints **************************************************/
                    for (int u=0;u<subNodesNum;u++){
                        for(int i=0; i< numNodesSFC; i++){
                            for(int j=0; j<numNodesSFC; j++){
                                IloLinearNumExpr flow2 = cplex.linearNumExpr();
                                IloLinearNumExpr x_var = cplex.linearNumExpr();
                                IloLinearNumExpr x_var1 = cplex.linearNumExpr();
                                IloNumExpr expr1=cplex.numExpr();
                                double cap = sfcLinks[i][j];
                                x_var.addTerm(cap, x[u][i]);
                                x_var.addTerm(-1*cap, x[u][j]);
                                // expr1 = cplex.sum(x_var,x_var1);
                                for (int v=0;v<subNodesNum;v++){
                                    flow2.addTerm(1, fl[u][v][i][j]);
                                    flow2.addTerm(-1, fl[v][u][i][j]);
                                }
                                cplex.addEq(flow2, x_var);
                            }
                        }

                    }


/*******************************************************************************/


                    cplex.exportModel("lpex_MILP.lp");
                    //long solveStartTime = System.nanoTime();
                    boolean solvedOK = cplex.solve();
                    //long solveEndTime = System.nanoTime();
                    //long solveTime = solveEndTime - solveStartTime;
                    //System.out.println("solvedOK: " + solvedOK);
                    System.out.println("Printing Request " + req.getId());
                    req.print();
                    System.out.println("cplex solvedOK = " + solvedOK);
                    if (solvedOK) {

                        //System.out.println("###################################");
                        //	System.out.println( "Found an answer! CPLEX status: " + cplex.getStatus() + ", Time (msec): " + ((double) solveTime / 1000000.0));
                        //System.out.println( "Found an answer! CPLEX status: " + cplex.getStatus() );
                        cplex.output().println("Solution value = " + cplex.getObjValue());
                        System.out.println("###################################");
                        substrateCopy.print();

                        //nodeMapping requested-real
                        LinkedHashMap<Node, Node> nodeMap = new LinkedHashMap<Node, Node> ();
                        double embedding_cost=0;
                        double cpu_cost=0;
                        double[][] xVar =new double [subNodesNum][sizeOfSFC];
                        for (int u=0;u<subNodesNum;u++){
                            for (int i=0;i<sizeOfSFC;i++){
                                xVar[u][i] = cplex.getValue(x[u][i]);
                                if (xVar[u][i] > 0.01){
                                    cpu_cost +=req_n.get(i).getAvailableCpu();
                                    nodeMap.put(req_n.get(i),subNodesList.get(u));
                                    System.out.println("updating substrate AlgorithmNF NFplacement_simple() 3183");
                                    if (!(updateSubstrate(substrateCopy,subNodesList.get(u),req_n.get(i).getAvailableCpu())))
                                        throw new ArithmeticException("Substrate Node Capacity not updated");
                                    //System.out.println("VNF  " + i + " to node" + u +"  " +xVar[u][i]);
                                }
                            }
                        }

                        //link mapping requested link - substrate path as list of links
                        MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>> lmap = new
                                MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>>();

                        //double avg_hops=0;
                        embedding_cost+=cpu_cost;
                        double bw_cost=0;

                        double[][][][] fVar1 =new double [subNodesNum][subNodesNum][sizeOfSFC][sizeOfSFC];
                        for(int k=0; k< numNodesSFC; k++){
                            for(int m=0; m< numNodesSFC; m++){
                                //	int hops_link_path=0;
                                //	HashMap<Integer,List<Pair<Integer>>> paths_used= new HashMap<Integer,List<Pair<Integer>>>();
                                for (int u=0;u<subNodesNum;u++){
                                    for (int v=0;v<subNodesNum;v++){
                                        fVar1[u][v][k][m] = cplex.getValue(fl[u][v][k][m]);
                                        if (fVar1[u][v][k][m]>0.00000001){
                                            bw_cost+=fVar1[u][v][k][m];
                                            Pair<Integer> key= new Pair<Integer>(u,v);
                                            HashMap<Pair<Integer>,Double> key4lmap=new HashMap<Pair<Integer>,Double>();
                                            key4lmap.put(key, fVar1[u][v][k][m]);
                                            List<Pair<Integer>> tmpPath = new ArrayList<Pair<Integer>> ();
                                            tmpPath.add(key);
                                            lmap.put(key4lmap,tmpPath);
                                            //System.out.println("Link : " +k+" "+m + " to " + u+ " "+v +" "+ fVar1[u][v][k][m]);
                                            if (!(updateSubLink(substrateCopy,u,v, fVar1[u][v][k][m])))
                                                throw new ArithmeticException("Substrate Link Capacity not updated");

                                        }
                                    }
                                }
                            }
                        }

                        embedding_cost+=bw_cost;

                        reqMap.setNodeMapping(nodeMap);
                        reqMap.setFlowMapping(lmap);
                        reqMap.accepted();
                        reqMap.setEmbeddingCost(embedding_cost);
                        reqMap.setCPUCost(cpu_cost);
                        reqMap.setBWCost(bw_cost);
                        //req.setRMapNF(reqMap);
                        reqMap.setServersUsed(checkCollocation(nodeMap,subNodesList));
                        System.out.println("###############################################");
                        System.out.println(embedding_cost+ " " + (bw_revenue+proc_revenue) );
                        System.out.println("nodeMap:\n" + nodeMap);
                        System.out.println("linkMap:\n" + lmap);
                        System.out.println("###############################################");


                        writer1.write("Node Mapping: "+ nodeMap+ "\n");
                        writer1.write("Link Mapping: "+ lmap+ "\n");
                        //System.out.println("//////////Accepted///////// "+req.getId());
                    }else{
                        System.out.println("Did not found an answer for Mapping");
                        reqMap.denied();
                        //req.setRMapNF(reqMap);
                    }
                    cplex.end();

/*			try {
		        System.in.read();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }*/
                    long solveEndTime = System.nanoTime();
                    long solveTime = solveEndTime - solveStartTime;
                    double solTime =  (double) solveTime / 1000000.0;
                    reqMap.setSolTime(solTime);
                    req.setRMapNF(reqMap);


                    //System.exit(0);

                } catch (IloException e) {
                    System.err.println("Concert exception caught: " + e);
                }


            }//all requests



        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {writer.close(); writer1.close();}
            catch (Exception ex) {
                //System.out.println("Cannot open results file: " + ex);
                System.exit(0);
            }
        }

        return true;
    }




    private List<Pair<Integer>> clearPath (List<Pair<Integer>> path, ArrayList<Node> subNodesList){
        List<Pair<Integer>> old_path= new ArrayList<Pair<Integer>>(path);
        List<Pair<Integer>> new_path= new ArrayList<Pair<Integer>>(path);
        for (Pair<Integer> ppairs: new_path) {
            boolean inPath1=false;
            boolean inPath2=false;
            boolean edge1 =getNodeById(subNodesList,ppairs.getFirst()).getType().equals("Server");
            boolean edge2 =getNodeById(subNodesList,ppairs.getSecond()).getType().equals("Server");
            if (!(edge1 || edge2)) {
                for (Pair<Integer> ppairs1: old_path) {
                    if (ppairs1.getSecond().equals(ppairs.getFirst())) {
                        inPath1=true;
                    } else if (ppairs1.getFirst().equals(ppairs.getSecond())) {
                        inPath2=true;
                    }
                }
                if (!(inPath1 && inPath2)) {
                    old_path.remove(ppairs);
                }
            }
        }


        return old_path;

    }

    public void addMAgent(Monitor monAgent) {
        // TODO Auto-generated method stub
        this.magent  =  monAgent;

    }

    public void clean() {
        if (this.ra != null) {
            this.ra.stop();
        }
    }

    public int getTs() {
        return this.ts;
    }
}

	
