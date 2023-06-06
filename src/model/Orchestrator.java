package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.HashMap;

import model.*;
import org.apache.commons.collections15.Factory;
import org.apache.commons.io.output.NullWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.typesafe.config.ConfigException.Null;

import cern.jet.random.Exponential;
import cern.jet.random.engine.MersenneTwister;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.filters.FilterUtils;
//import edu.uci.ics.jung.algorithms.generators.random.BarabasiAlbertGenerator;
import edu.uci.ics.jung.algorithms.generators.random.ErdosRenyiGenerator;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
//import model.AlgorithmNFold;
import model.components.Link;
import model.components.Node;
import monitoring.Monitor;
import simenv.SimulatorConstants;


public class Orchestrator {
    private ArrayList<SimulationNFV> DCs;
    private List<Request> requests;
    private boolean dynamic; // taken from MainWithoutGUI
    private static int sub_nodes=0; // taken from MainWithoutGUI
    private static double nom_cap=0; // taken from MainWithoutGUI
    public int n_dcs;
    private boolean monitoring;

    public static final class Lock { }
    private List<Lock> locks;

    private XSSFWorkbook mainWorkbook;
    private XSSFSheet mapSheet;

    private static String[] maincolumns = {"Time", "Request ID", "DC ID"};

    public Orchestrator(int numRequests, int numDCs, boolean monitoring, boolean dynamic) throws CloneNotSupportedException {
        this.monitoring = monitoring;
        this.dynamic = dynamic;
        this.n_dcs = numDCs;
        ArrayList<Substrate> nfvi = createSubGraph(numDCs);
        Substrate InPs = new Substrate("InPs");
        this.DCs = new ArrayList<SimulationNFV>();
        this.locks = new ArrayList<Lock>();
        this.requests = createFG(numRequests);
        this.mainWorkbook = new XSSFWorkbook();
        this.mapSheet = this.mainWorkbook.createSheet("RequestMappings");
        for (int i = 0; i < numDCs; i++) {
            String algorithmID = "MILP_max";
            Lock cur_dc_lock = new Lock();
            locks.add(cur_dc_lock);
            AlgorithmNF algoi = new AlgorithmNF(algorithmID, nfvi.get(i));
            SimulationNFV DCi = new SimulationNFV(
                                InPs, nfvi, algoi,
                                "DC_" + i + "-" + algorithmID, cur_dc_lock,
                                this.monitoring, this.dynamic,
                                this.getEndDate(), this.mainWorkbook,
                                this.mapSheet);
            this.DCs.add(DCi);
        }
    }

    public List<Lock> getLocks() {
        return this.locks;
    }

    public int getEndDate() {
		int end = 0;
		for (Request req : requests)
			if (req.getEndDate()>end)
				end = req.getEndDate();
		return end;
	}

    public List<SimulationNFV> getDCs() {
        return this.DCs;
    }

    public void setDCs(ArrayList<SimulationNFV> DCs) {
        this.DCs = DCs;
    }

    public List<Request> getAllRequests() {
        return this.requests;
    }

    public XSSFWorkbook getMainWorkbook() {
        return this.mainWorkbook;
    }

    public XSSFSheet getMapSheet() {
        return this.mapSheet;
    }

    // Copied from SimulationNFV class, as requests are now handled in the Orchestrator
    public List<Request> getStartingRequests(int time) {
		List<Request> startingRequests = new ArrayList<Request>();
		for (Request req : requests)
			if (req.getStartDate()==time)
				startingRequests.add(req);
		return startingRequests;
	}

	public List<Request> getEndingRequests(int time) {
		List<Request> endingRequests = new ArrayList<Request>();
		for (Request req : requests){
			if (req.getEndDate()==time){
				endingRequests.add(req);
			}
		}
		return endingRequests;
	}

	public List<Request> getUpdatedRequests(int time) {
		List<Request> updatedRequests = new ArrayList<Request>();
		for (Request req : requests) {
            ArrayList<Integer> reqTS = req.getTS();
            if (reqTS == null) continue;
            if (req.getTS().contains((Integer)time)) {
			//System.out.println("To update request " + req.id + " at time " + time + " " +req.getStartDate());
			updatedRequests.add(req);
			//System.out.println("To update request " + req.id + " at time " + time);
			}
		}
		return updatedRequests;
	}

    // Pick the datacenters on which to embed incoming requests.
    public HashMap<Integer, ArrayList<Request>> orchestrate(int time) {
        List<Request> startingRequests = getStartingRequests(time);
        Random rand = new Random();
        // initialize mapping
        HashMap<Integer, ArrayList<Request>> dc_map = new HashMap<Integer, ArrayList<Request>>();
        for (int i = 0; i < n_dcs; i++) {
            ArrayList<Request> l = new ArrayList<Request>();
            dc_map.put(i, l);
        }
        // create mapping
        for (Request req : startingRequests) {
            dc_map.get(rand.nextInt(n_dcs)).add(req);
        }
        return dc_map;
    }

    // Copied from MainWithoutGUI to move everything to orchestrator.
    public static ArrayList<Substrate> createSubGraph(int pop_num) throws CloneNotSupportedException{
        Substrate finalSubstrate = new Substrate("substrate");
        final ArrayList<Substrate> substrates = new ArrayList<Substrate>();

        for (int k=0; k<pop_num; k++) {
            Graph<Node, Link> sub= new DirectedSparseGraph<Node, Link>();
            DirectedFatTreeL3 fl3= new DirectedFatTreeL3();
            fl3.createFTGraph();


            Substrate substrate = new Substrate("sub"+k);
            sub = fl3.getFatTreeL3Graph();
            SubstrateNodeFactory nodeFactory = fl3.getNodeFactory();
            SubstrateLinkFactory linkFactory = fl3.getLinkFactory();

            substrate.setNodeFactory(nodeFactory);
            substrate.setLinkFactory(linkFactory);
            substrate.setGraph(sub);
            substrate.setFTL3(fl3);
            //substrate.print();


            //	Substrate substrate1 = (Substrate)substrate.clone();
		/*new Substrate("sub"+k);
		//fl3.createPaths();
		substrate1.setNodeFactory(nodeFactory);
		substrate1.setLinkFactory(linkFactory);
		substrate1.setGraph(sub);
		substrate1.print();
		substrate1.setFTL3(fl3);*/


/*		System.out.println(fl3.getPaths());
		System.exit(0);*/
            //substrate.addPods(fl3.podNum());


            //fl3.createHyperGraph();


            substrates.add(k, substrate);
            //	substrates.add(k+1, substrate1);

        }

        finalSubstrate= substrates.get(0);
        sub_nodes =finalSubstrate.getGraph().getVertices().size();
        nom_cap = finalSubstrate.getAvgWl();

        //finalSubstrate.setHGraph((Hypergraph<Node, Link>) substrates.get(0).getGraph());

        return substrates;
    }


    public List<Request>  createFG(int numRequests) throws CloneNotSupportedException{
        final String prefix ="req";
        final String timeDistribution = SimulatorConstants.POISSON_DISTRIBUTION;
        final int fixStart=0;
        final int uniformMin=0;
        final int uniformMax=0;
        final int normalMean=0;
        final int normalVariance=0;

        ArrayList<List<Request>> tmp = new ArrayList<List<Request>> ();
        final List<Request> requests = new ArrayList<Request>();
        ////final List<Request> requests1 = new ArrayList<Request>();
        ////final List<Request> requests2 = new ArrayList<Request>();
        ////final List<Request> requests3 = new ArrayList<Request>();
        ////final List<Request> requests4 = new ArrayList<Request>();
        double avg_wl = 0;

        int startDate = 0;
        int lifetime = 0;
        int sum=0;


        Exponential exp_arr = null;
        Exponential exp = null;
        Exponential exp_upd = null;

        if (timeDistribution.equals(SimulatorConstants.POISSON_DISTRIBUTION)) {
            MersenneTwister engine = new MersenneTwister(new Date());
            exp_arr = new Exponential(0.04,engine);
            exp= new Exponential(0.0001,engine); // INSTEAD OF 0.001XS
            exp_upd= new Exponential(0.1,engine);
        }

        for (int i=0; i<numRequests; i++) {
            Request request = new Request(prefix+i);
            DirectedFG requestSG = new DirectedFG(new TrafficFG());
            requestSG.createGraph(i);
            request.setDFG(requestSG);
            request.setGraph(requestSG.getDirectedFG()); // to be removed
            request.setNodeFactory(requestSG.getNodeFactory()); // to be removed
            request.setLinkFactory(requestSG.getLinkFactory()); // to be removed

            // Duration of the request
            lifetime= exp.nextInt();

/*			System.out.println("lifetime: " +lifetime);
			try {
		        System.in.read();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }*/

            // All requests start at month fixStart
            if (timeDistribution.equals(SimulatorConstants.FIXED_DISTRIBUTION)) {
                request.setStartDate(fixStart);
                request.setEndDate(fixStart+lifetime);
            }

            // Random: Uniform distribution
            if (timeDistribution.equals(SimulatorConstants.UNIFORM_DISTRIBUTION)) {
                startDate = uniformMin + (int)(Math.random()*((uniformMax - uniformMin) + 1));
                request.setStartDate(startDate);
                request.setEndDate(startDate+lifetime);
            }

            // Random: Normal distribution
            if (timeDistribution.equals(SimulatorConstants.NORMAL_DISTRIBUTION)) {
                Random random = new Random();
                startDate = (int) (normalMean + random.nextGaussian() * normalVariance);
                if (startDate<0)
                    startDate*=-1;
                request.setStartDate(startDate);
                request.setEndDate(startDate+lifetime);
            }

            // Random: Poisson distribution
            if (timeDistribution.equals(SimulatorConstants.POISSON_DISTRIBUTION)) {
                startDate = exp_arr.nextInt();
                sum=sum+startDate;
                if (lifetime==0)
                    lifetime=lifetime+1;
                request.setStartDate(sum);
                request.setEndDate(sum+lifetime);


                //request.setEndDate(lifetime_max );
            }

            if (dynamic) {
                int numTS = exp_upd.nextInt();
                ArrayList<Integer> reqTS = new ArrayList<Integer>();
                System.out.println(numTS + "  " +request.getStartDate() + " " +request.getEndDate());
                for (int k=0;k<numTS;k++) {
                    Random randn = new Random();
                    int  offsetTS = randn.nextInt(request.getEndDate()) + request.getStartDate();
                    if (!reqTS.contains((Integer)k +offsetTS) && ((Integer)k +offsetTS)<=request.getEndDate()){
                        reqTS.add((Integer)k +offsetTS) ;
                    } /*else {
							k--;
						}*/

                }
                request.setTS(reqTS);

                ///System.out.println(al + ": " +al.size());

            }
/*			System.out.println("request starttime:" +request.getStartDate());
		    System.out.println("request lifetime:" +request.getEndDate());*/

            //request.print();
            // System.out.println(request.getEndDate()-request.getStartDate());

/*		    System.exit(0);
			try {
			        System.in.read();
			    } catch (IOException e) {
			        // TODO Auto-generated catch block
			        e.printStackTrace();
			    }*/

            requests.add(request);
            ////requests1.add((Request)request.clone());
            ////requests2.add((Request)request.clone());
            ////requests3.add((Request)request.clone());
            ////requests4.add((Request)request.clone());
            //avg_wl +=request.getWl();

        }

        tmp.add(requests);
        ////tmp.add(requests1);
        ////tmp.add(requests2);
        ////tmp.add(requests3);
        ////tmp.add(requests4);
        double req_wl = avg_wl;
        //   System.exit(0);
        return requests;

    }
}