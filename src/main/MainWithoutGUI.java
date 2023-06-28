package main;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Time;
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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;

import model.*;
import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.set.ListOrderedSet;
import org.apache.commons.io.output.NullWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sqlite.SQLiteConfig.LockingMode;

import com.twelvemonkeys.io.NullOutputStream;

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

public class MainWithoutGUI{
    static ArrayList<Node> sRRHs = new  ArrayList<Node>();
    static ArrayList<Node> sIXPs = null;
    static ArrayList<Node> NFs = null;
    static double req_wl =0;
    static int sub_nodes=0;
    static double nom_cap=0;
    static boolean monitoring=true;
    static boolean dynamic = true;
    static boolean belief=true;
    // Data collection variables \/
    static int totalRequested;
    static int totalRejected;
    static double totalRevenue;
    static double totalCost;
    static double totalCPUCost;
    static double totalBWCost;
    static double totalSolTime;
    static int totalViolationsCPU;
    static int totalMonInstances;
    static int totalViolationMon;
    static int totalCollocated;
    static long startTime;

    static long dcsruntime;
    static long orchestrationtime;

    //private static String[] columns = {"Time", "Acceptance", "Cum Revenue", "Cum Cost",
    //        "Cum CPUCost", "Cum BWCos", "Avg Server Util", "Avg Link Util", "LBL Server", "LBL link",
    //        "Violations", "Avg Time PR", "Violation Ratio", "Rejection Ratio", "Non Collocated", "Collocated",
    //        "Accepted", "Rejected", "Violations Monitoring", "Monitoring Instances", "SFC Nodes", "SFC Links",
    //        "Total SFC Workload", "Total SFC Bandwidth"};



    public static void main(String[] args) throws CloneNotSupportedException {
        startTime = (long) new Date().getTime();
        int n_dcs = 3; // # of datacenters to simulate
        int numRequests = 60; // # of requests to simulate

        try {
            n_dcs = Integer.parseInt(args[0]);
            numRequests = Integer.parseInt(args[1]);
        } catch (Exception e) {
            n_dcs = 3;
            numRequests = 60;
            System.err.println("No valid arguments, defaulting to " + n_dcs + " DCs and " + numRequests + " requests.");
        }

        // Number of experiments to execute
        // //int experiments=1; ////wasnt used?
        // SubstrateNodeFactory.MIN_CPU_RACK = 0.1;
        // SubstrateNodeFactory.MAX_CPU_RACK = 0.2; // Shoudl not be here

        boolean monitoring = true;
        boolean dynamic = false;
        //Create an abstract graph where each node represent an InP
        Orchestrator orchestrator = new Orchestrator(numRequests, n_dcs, monitoring, dynamic);

        //Substrate InPs = new Substrate("InPs"); moved to Orchestrator
        //ArrayList<Substrate> nfvi = createSubGraph(n_dcs); moved to Orchestrator


        //Substrate nfvi1 = (Substrate)nfvi.getCopy(EdgeType.DIRECTED);
        //Create each InP
        //List<Substrate> substrates = new ArrayList<Substrate>();
        //List<Substrate> substrates1= new ArrayList<Substrate>();
        //List<Substrate> substrates2= new ArrayList<Substrate>();
        //List<Substrate> substrates3= new ArrayList<Substrate>();
        //List<Substrate> substrates4= new ArrayList<Substrate>();
        //substrates.add(nfvi.get(0));
        //substrates1.add((Substrate)nfvi.get(0).clone());
        //substrates2.add((Substrate)nfvi.get(0).clone());
        //substrates3.add((Substrate)nfvi.get(0).clone());
        //substrates4.add((Substrate)nfvi.get(0).clone());
        //Create the Requests
        ////List<Request> request_tab1 = new ArrayList<Request>();
        ////List<Request> request_tab2 = new ArrayList<Request>();
        ////List<Request> request_tab3 = new ArrayList<Request>();
        ////List<Request> request_tab4 = new ArrayList<Request>();
        ////request_tab=requests.get(0);
        ////request_tab1=requests.get(1);
        ////request_tab2=requests.get(2);
        ////request_tab3=requests.get(3);
        ////request_tab4=requests.get(4);
/*			try {
            System.in.read();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    */
        System.out.println("The simulator is working");
        //System.exit(0);

//            dynamic = false;
        //AlgorithmNF algorithm = new AlgorithmNF("MILP_max",substrates.get(0));//// First substrate taken into algorithm
        //algorithm1.setStateReq(req_wl/nom_cap);
        //SimulationNFV simulation = new SimulationNFV(InPs, substrates, request_tab, algorithm);//// All substrates into simulation?
        { // bunch of commented out code, brackets allow collapsing it

//			AlgorithmNF algorithm1 = new AlgorithmNF("RLb",substrates1.get(0));
//			simulation = new SimulationNFV(InPs, substrates1, request_tab1, algorithm1);
//			try {
//				launchSimulation(simulation, i);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

            //substrates.get(0).print();
            //substrates1.get(0).print();



	/*		System.out.println("monitoring: " +monitoring);
			System.out.println("dynamic: " +dynamic);
			AlgorithmNF algorithm2 = new AlgorithmNF("MILP_max");
		    simulation = new SimulationNFV(InPs, substrates2, request_tab2, algorithm2);
            
//			AlgorithmNF algorithm1 = new AlgorithmNF("RLb",substrates1.get(0));
//			simulation = new SimulationNFV(InPs, substrates1, request_tab1, algorithm1);
//			try {
//				launchSimulation(simulation, i);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

            //substrates.get(0).print();
            //substrates1.get(0).print();



	/*		System.out.println("monitoring: " +monitoring);
			System.out.println("dynamic: " +dynamic);
			AlgorithmNF algorithm2 = new AlgorithmNF("MILP_max");
		    simulation = new SimulationNFV(InPs, substrates2, request_tab2, algorithm2);
            try {
                launchSimulation(orchestrator);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

/*

			AlgorithmNF algorithm1 = new AlgorithmNF("RLb",substrates1.get(0));
			
			AlgorithmNF algorithm2 = new AlgorithmNF("MILP_max");
		   
			AlgorithmNF algorithm3 = new AlgorithmNF("MILP_avg");
			
			AlgorithmNF algorithm4 = new AlgorithmNF("Greedy_max",substrates4.get(0));


            /////////////////////////////////////////////////////nonsense
	/*		substrates2.get(0).print();
			try {
		        System.in.read();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }

			//////////////////////
			
            /////////////////////////////////////////////////////nonsense
	/*		substrates2.get(0).print();
			try {
		        System.in.read();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }

			//////////////////////
			AlgorithmNF algorithm2 = new AlgorithmNF("ILP");
			SimulationNFV simulation2 = new SimulationNFV(InPs, substrates2, request_tab2, algorithm2);
	
			AlgorithmNF algorithm3 = new AlgorithmNF("ILP1");
*/
        }
        try {
            launchSimulation(orchestrator);
        } catch (Exception e) {
            System.out.println("error launching simulation:");
            e.printStackTrace();
        }
    }

    private static void waitForDCs(Orchestrator orchestrator) throws InterruptedException {
        for (int i = 0; i < orchestrator.n_dcs; i++) {
            synchronized (orchestrator.getLocks().get(i)) {
                while (orchestrator.getDCs().get(i).ready)
                    orchestrator.getLocks().get(i).wait();
            }
        }
    }

    private static void setTimeStepInDCs(Orchestrator orchestrator, int time) {
        for (SimulationNFV dc : orchestrator.getDCs()) {
            dc.setOrchestratorTimestep(time);
        }
    }



    private static double[] launchSimulation(Orchestrator orchestrator) throws Exception {

        String[] cumDataSheetColumns = {"Time", "Requests Total", "Denials Total", "Revenue Total", "Cost Total",
            "CPU Cost Total", "BW Cost Total", "AVG Sol Time", "CPU Violations Total", "CPU Violation Ratio", "Non Collocated Total",
            "Collocated Total", "Accepted Total", "Mon Instances Total", "Denial Ratio", "Acceptance Ratio"};

        String workbookOutputFileName = "results/simulation_" + new Date().getTime() + "_" + orchestrator.getAllRequests().size() + "R_" + orchestrator.n_dcs + "DCs.xlsx";
        OutputStream workbookFileStream;
        try {
            workbookFileStream = new FileOutputStream(workbookOutputFileName);
        } catch (Exception e) {
            workbookFileStream = new NullOutputStream();
            System.err.println("[ERROR] Excel sheet writer for orchestrator is null");
            e.printStackTrace(System.err);
        }
        //results, 0 cost, 1 time, 2, denial
        double[] results=new double[3];

        XSSFWorkbook w = orchestrator.getMainWorkbook();
        XSSFSheet cumDataSheet = w.createSheet("CumulativeDataSheet");
        TreeSet<Integer> reqStartTimes = new TreeSet<Integer>();
        for (Request req : orchestrator.getAllRequests()) {
            reqStartTimes.add(req.getStartDate());
        }

        List<Thread> DC_threads = new ArrayList<Thread>();
        for (SimulationNFV cur_dc : orchestrator.getDCs()) {
            Thread cur_dc_thread = new Thread(cur_dc);
            DC_threads.add(cur_dc_thread);
            System.out.println("------ Starting thread for DC " + cur_dc.getId());
            cur_dc_thread.start();
        }
        dcsruntime = 0;
        orchestrationtime = 0;
        long threadsDispatchedTime = new Date().getTime();
        long threadsSyncedTime = new Date().getTime();

        int rowCounter = 0;
        int prevSimTime = -1;

        int simulationEndTime = (int)orchestrator.getEndDate() + 10000;
        System.out.println("simulationEndTime: " + simulationEndTime);
        //// MAIN SIMULATION LOOP
        for (int curSimTime : reqStartTimes) {
            System.out.println("Orchestrator time: " + curSimTime);
            totalRequested = 0;
            totalRejected = 0;
            totalRevenue = 0;
            totalCost = 0;
            totalCPUCost = 0;
            totalBWCost = 0;
            totalSolTime = 0;
            totalViolationsCPU = 0;
            totalMonInstances = 0;
            totalViolationMon = 0;
            totalCollocated = 0;
            setTimeStepInDCs(orchestrator, curSimTime);
            orchestrationtime += (new Date().getTime() - threadsSyncedTime);
            waitForDCs(orchestrator); // All DCs are now waiting, safe to pull data from them
            threadsSyncedTime = new Date().getTime();
            dcsruntime += (threadsSyncedTime - threadsDispatchedTime);
            for (SimulationNFV cur_dc : orchestrator.getDCs()) {
                totalRequested += cur_dc.requested;
                totalRejected += cur_dc.denials;
                totalRevenue += cur_dc.revenue;
                totalCost += cur_dc.cost;
                totalCPUCost += cur_dc.cpuCost;
                totalBWCost += cur_dc.bwCost;
                totalSolTime += cur_dc.sol_time;
                totalViolationsCPU += cur_dc.viol_cpu;
                totalMonInstances += cur_dc.mon_instances;
                totalViolationMon += cur_dc.viol_cpu_mon;
                totalCollocated += cur_dc.collocated;
            }
            HashMap<Integer, ArrayList<Request>> req_mapping = orchestrator.orchestrate(curSimTime);
            for (int i = 0; i < orchestrator.n_dcs; i++) {
                synchronized (orchestrator.getLocks().get(i)) {
                    orchestrator.getDCs().get(i).setToEmbed(req_mapping.get(i));
                    orchestrator.getDCs().get(i).go();
                    orchestrator.getLocks().get(i).notify();
                }
            }
            threadsDispatchedTime = new Date().getTime();
            // write data while DCs running
            if (reqStartTimes.contains(curSimTime)) {
                double denialRatio = (double) totalRejected / (double) totalRequested;
                Row row = cumDataSheet.createRow(rowCounter++);
                row.createCell(0).setCellValue(prevSimTime); //time
                row.createCell(1).setCellValue(totalRequested); //Requests
                row.createCell(2).setCellValue(totalRejected); //Rejected
                row.createCell(3).setCellValue(totalRevenue); //Cum Revenue
                row.createCell(4).setCellValue(totalCost); //Cum cost
                row.createCell(5).setCellValue(totalCPUCost); //Cum CPU cost
                row.createCell(6).setCellValue(totalBWCost); //Cum BW Cost
                row.createCell(7).setCellValue(totalSolTime / (double) totalRequested); //AVG Sol Time
                row.createCell(8).setCellValue(totalViolationsCPU); //Cum violations
                row.createCell(9).setCellValue(totalViolationsCPU / (double) totalRequested); //Violation ratio
                row.createCell(10).setCellValue((double)(totalRequested-totalRejected-totalCollocated)); // non-collocated
                row.createCell(11).setCellValue((double)(totalCollocated)); //Cum collocated
                row.createCell(12).setCellValue((double)(totalRequested-totalRejected)); // accepted
                row.createCell(13).setCellValue(totalMonInstances); //Total Mon Instances
                row.createCell(14).setCellValue(denialRatio); //Denial ratio
                row.createCell(15).setCellValue(1 - denialRatio); //Acceptance ratio
            }

            prevSimTime = curSimTime;
        }

        System.out.println("Orchestrator done w/ requests");

        Font headerFont = w.createFont(); // Setting headers for new sheet here to overwrite obsolete first row
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.RED.getIndex());

        CellStyle headerCellStyle = w.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = cumDataSheet.createRow(0);
        for (int i = 0; i < cumDataSheetColumns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(cumDataSheetColumns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        waitForDCs(orchestrator);
        setTimeStepInDCs(orchestrator, simulationEndTime);


        for (int i = 0; i < orchestrator.n_dcs; i++) {
            synchronized (orchestrator.getLocks().get(i)) {
                orchestrator.getDCs().get(i).setToEmbed(null);
                orchestrator.getDCs().get(i).go();
                orchestrator.getLocks().get(i).notify();
            }
        }
        try {
            System.out.println("Simulation over");
            for (int i = 0; i < orchestrator.n_dcs; i++) {
                System.out.println("Joining thread " + i);
                DC_threads.get(i).join();
            }
        } catch (InterruptedException e) {
            System.err.println("Error joining threads:");
            e.printStackTrace(System.err);
        }
        // Separate runtime sheet start
        XSSFSheet runTimeSheet = w.createSheet("Running times");
        int idx = 0;
        Row runTimeSheetHeaders = runTimeSheet.createRow(0);
        runTimeSheetHeaders.createCell(idx++).setCellValue("Orchestrator only runtime");
        runTimeSheetHeaders.createCell(idx++).setCellValue("At least 1 DC running");
        for (SimulationNFV cur_dc : orchestrator.getDCs()) {
            runTimeSheetHeaders.createCell(idx++).setCellValue("Runtime of " + cur_dc.getId());
        }
        runTimeSheetHeaders.createCell(idx++).setCellValue("Sum of DC runtimes");
        Row runTimeSheetValues = runTimeSheet.createRow(1);
        idx = 0;
        runTimeSheetValues.createCell(idx++).setCellValue(orchestrationtime);
        runTimeSheetValues.createCell(idx++).setCellValue(dcsruntime);
        // Separate runtime sheet end


        Row cur_row = cumDataSheet.createRow(rowCounter + 2);
        cur_row.createCell(1).setCellValue("Total time in which at least one dc was running");
        cur_row.createCell(2).setCellValue(dcsruntime);
        cur_row = cumDataSheet.createRow(rowCounter + 3);
        cur_row.createCell(1).setCellValue("Total time in which only orchestrator was running");
        cur_row.createCell(2).setCellValue(orchestrationtime);
        long sumofDCtimes = 0;
        for (int i = 0; i < orchestrator.n_dcs; i++) {
            cur_row = cumDataSheet.createRow(rowCounter + 7 + i);
            SimulationNFV cur_dc = orchestrator.getDCs().get(i);
            cur_row.createCell(1).setCellValue("Running time of " + cur_dc.getId());
            cur_row.createCell(2).setCellValue(cur_dc.getTotalDCRuntime());
            runTimeSheetValues.createCell(idx++).setCellValue(cur_dc.getTotalDCRuntime()); // Separate runtime sheet
            sumofDCtimes += cur_dc.getTotalDCRuntime();
        }
        cur_row = cumDataSheet.createRow(rowCounter + 8 + orchestrator.n_dcs);
        cur_row.createCell(1).setCellValue("Sum of DC runtimes:");
        cur_row.createCell(2).setCellValue(sumofDCtimes);

        runTimeSheetValues.createCell(idx++).setCellValue(sumofDCtimes); // Separate runtime sheet



		
        String[] mapSheetColumns = {"Timestep", "Request ID", "Mapped to", "Amt Embedded", "SFC Nodes", "SFC Links", "Total SFC Workload", "Total SFC Bandwidth", "End Time", "Update Times ->"};
        Row reqMapSheetRow = orchestrator.getMapSheet().createRow(0);
        for (int i = 0; i < mapSheetColumns.length; i++) {
            Cell c = reqMapSheetRow.createCell(i);
            c.setCellStyle(headerCellStyle);
            c.setCellValue(mapSheetColumns[i]);
        }
        // Write total runtime to sheet
        cur_row = cumDataSheet.createRow(reqStartTimes.size() + 5);
        cur_row.createCell(1).setCellValue("Sim Runtime:");
        cur_row.createCell(2).setCellValue(new Date().getTime() - startTime);

        // Set headers in DC dataSheets
        String[] dataSheetColumns = {"Time", "Request ID", "Currently Embedded", "Acceptance", "Cum Revenue", "Cum Cost",
            "Cum CPUCost", "Cum BWCos", "Avg Server Util", "Avg Link Util", "LBL Server", "LBL link",
            "Violations", "Avg Time PR", "Violation Ratio", "Rejection Ratio", "Non Collocated", "Collocated",
            "Accepted", "Rejected", "Violations Monitoring", "Monitoring Instances", "SFC Nodes", "SFC Links",
            "Total SFC Workload", "Total SFC Bandwidth"};
        for (SimulationNFV cur_dc : orchestrator.getDCs()) {
            XSSFSheet curDataSheet = cur_dc.getDataSheet();
            headerRow = curDataSheet.createRow(0);
		    for (int i = 0; i < dataSheetColumns.length; i++) {
		    	Cell cell = headerRow.createCell(i);
		    	cell.setCellValue(dataSheetColumns[i]);
		    	cell.setCellStyle(headerCellStyle);
		    }
		    int headerCellIndex = dataSheetColumns.length;

		    for (Node current : cur_dc.getSubstrates().get(0).getGraph().getVertices()) {
		    	if (!(current.getType().equalsIgnoreCase("Switch"))) {
		    		Cell cell = headerRow.createCell(headerCellIndex);
		    		cell.setCellValue("node_" + current.getId() + "_available_cpu");
		    		cell.setCellStyle(headerCellStyle);

		    		Cell cell2 = headerRow.createCell(headerCellIndex + 1);
		    		cell2.setCellValue("node_" + current.getId() + "_avg_life");
		    		cell2.setCellStyle(headerCellStyle);
		    		headerCellIndex += 2;
		    	}
		    }
        }
        try {
            System.out.println("Writing global workbook to " + workbookOutputFileName);
            w.write(workbookFileStream);
            w.close();
            workbookFileStream.close();
        } catch (IOException e) {
            System.err.println("Error writing global workbook to file");
            e.printStackTrace(System.err);
        }



  /* catch (WriteException e)
    {
      e.printStackTrace();
    }
  	*/
        for (SimulationNFV cur_dc : orchestrator.getDCs()) {
            cur_dc.getAlgorithm().clean();
        }

        System.out.println(reqStartTimes);

        return results;
    }



    public static List<Substrate> createSubstrateGraph(int inp_no){

        final String prefix ="sub";
        final List<Substrate> substrates = new ArrayList<Substrate>();

        for (int i=0;i<inp_no;i++){
            Substrate substrate = new Substrate(prefix+i);

            UndirectedSparseGraph<Node, Link> sub= new UndirectedSparseGraph<Node, Link>();
            FatTreeL2 fl2= new FatTreeL2();
            fl2.createGraph();
            sub = fl2.getFatTreeL2Graph();
            substrate.setGraph(sub);
            substrate.print();
            substrate.setNodeFactory(fl2.getNodeFactory());
            substrate.setLinkFactory(fl2.getLinkFactory());
            substrates.add(substrate);


        }

        return substrates;
    }


    //pathlets
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
    ////Creates an arraylist of 5 identical lists of requests -- to be just one list
    public static List<Request>  createFG(int numRequests) throws CloneNotSupportedException{
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
        req_wl=avg_wl;
        //   System.exit(0);
        return requests;

    }

    public static Substrate createInPGraph(int inp_no){


        final String prefix ="inps";
        final Substrate substrate = new Substrate(prefix);
        //Number of nodes in the Infrastructure Provider
        int numNodes = inp_no;
        //Probability of a connection
        double linkProbability = 0.5;
        SparseMultigraph<Node, Link> g = null;
        SubstrateNodeFactory nodeFactory = new SubstrateNodeFactory();
        SubstrateLinkFactory linkFactory = new SubstrateLinkFactory();

        //Random Graph Generation
        Factory<UndirectedGraph<Node, Link>> graphFactory = new Factory<UndirectedGraph<Node, Link>>() {
            public UndirectedGraph<Node, Link> create() {
                return new NetworkGraph();
            }
        };

        //ErdosRenyiGenerator generation
        ErdosRenyiGenerator<Node, Link> randomGraph =
                new ErdosRenyiGenerator<Node, Link>(graphFactory, nodeFactory,
                        linkFactory, numNodes, linkProbability );
        g = (SparseMultigraph<Node, Link>) randomGraph.create();
        //Remove unconnected nodes
        ((NetworkGraph) g).removeUnconnectedNodes();
        // TODO remove disconnected graphs
        WeakComponentClusterer<Node, Link> wcc = new WeakComponentClusterer<Node, Link>();
        Set<Set<Node>> nodeSets = wcc.transform(g);
        Collection<SparseMultigraph<Node, Link>> gs = FilterUtils.createAllInducedSubgraphs(nodeSets, g);
        if (gs.size()>1) {
            @SuppressWarnings("rawtypes")
            Iterator itr = gs.iterator();
            g = (NetworkGraph)itr.next();
            while (itr.hasNext()) {
                @SuppressWarnings("unchecked")
                SparseMultigraph<Node, Link> nextGraph = (SparseMultigraph<Node, Link>) itr.next();

                if (nextGraph.getVertexCount()>g.getVertexCount())
                    g = (NetworkGraph)nextGraph;
            }
        }

        if (g.getVertexCount()>0){
            // Change id of nodes to consecutive int (0,1,2,3...)
            @SuppressWarnings("rawtypes")
            Iterator itr = g.getVertices().iterator();
            int id = 0;
            while (itr.hasNext()) {
                ((Node) itr.next()).setId(id);
                id++;
            }
            // refresh nodeFactory's nodeCount
            nodeFactory.setNodeCount(id);
            // Change id of edges to consecutive int (0,1,2,3...)
            itr = g.getEdges().iterator();
            id = 0;
            while (itr.hasNext()) {
                ((Link) itr.next()).setId(id);
                id++;
            }
            // refresh linkFactory's linkCount
            linkFactory.setLinkCount(id);
        }

        if  ((g.getVertexCount()==inp_no)){
            substrate.setGraph(g);
            substrate.setNodeFactory(nodeFactory);
            substrate.setLinkFactory(linkFactory);
        }
        else{
            createInPGraph(inp_no);
        }

        final String pre_fix ="sub";
        int i=0;
        for (Node node: substrate.getGraph().getVertices()){
            node.setName(pre_fix+i);
            i++;
        }

        return substrate;
    }










}
