package main;


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

    private static String[] columns = {"Time", "Acceptance", "Cum Revenue", "Cum Cost",
            "Cum CPUCost", "Cum BWCos", "Avg Server Util", "Avg Link Util", "LBL Server", "LBL link",
            "Violations", "Avg Time PR", "Violation Ratio", "Rejection Ratio", "Non Collocated", "Collocated",
            "Accepted", "Rejected", "Violations Monitoring", "Monitoring Instances", "SFC Nodes", "SFC Links",
            "Total SFC Workload", "Total SFC Bandwidth"};
    public static void main(String[] args) throws CloneNotSupportedException {


        // Number of experiments to execute
        int experiments=1;
        SubstrateNodeFactory.MIN_CPU_RACK = 0.1;
        SubstrateNodeFactory.MAX_CPU_RACK = 0.2;
        for (int i=0;i<experiments;i++){

            int dc_no=1;
            //Create an abstract graph where each node represent an InP
            Substrate InPs=new Substrate("InPs");
            ArrayList<Substrate> nfvi = createSubGraph(dc_no);


            //Substrate nfvi1 = (Substrate)nfvi.getCopy(EdgeType.DIRECTED);
            //Create each InP
            List<Substrate> substrates = new ArrayList<Substrate>();
            List<Substrate> substrates1= new ArrayList<Substrate>();
            List<Substrate> substrates2= new ArrayList<Substrate>();
            List<Substrate> substrates3= new ArrayList<Substrate>();
            List<Substrate> substrates4= new ArrayList<Substrate>();
            substrates.add(nfvi.get(0));
            substrates1.add((Substrate)nfvi.get(0).clone());
            substrates2.add((Substrate)nfvi.get(0).clone());
            substrates3.add((Substrate)nfvi.get(0).clone());
            substrates4.add((Substrate)nfvi.get(0).clone());
            //Create the Requests
            List<Request> request_tab = new ArrayList<Request>();
            List<Request> request_tab1 = new ArrayList<Request>();
            List<Request> request_tab2 = new ArrayList<Request>();
            List<Request> request_tab3 = new ArrayList<Request>();
            List<Request> request_tab4 = new ArrayList<Request>();
            ArrayList<List<Request>> requests =  new ArrayList<List<Request>> ();
            requests= createFG(10000);
            request_tab=requests.get(0);
            request_tab1=requests.get(1);
            request_tab2=requests.get(2);
            request_tab3=requests.get(3);
            request_tab4=requests.get(4);
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
            AlgorithmNF algorithm = new AlgorithmNF("MILP_max",substrates.get(0));
            //algorithm1.setStateReq(req_wl/nom_cap);
            SimulationNFV simulation = new SimulationNFV(InPs, substrates, request_tab, algorithm);
            try {
                launchSimulation(simulation, i);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


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
				launchSimulation(simulation, i);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			AlgorithmNF algorithm3 = new AlgorithmNF("MILP_avg");
			simulation = new SimulationNFV(InPs, substrates3, request_tab3, algorithm3);
			try {
				launchSimulation(simulation, i);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			AlgorithmNF algorithm4 = new AlgorithmNF("Greedy_max",substrates4.get(0));
			//algorithm1.setStateReq(req_wl/nom_cap);
			simulation = new SimulationNFV(InPs, substrates4, request_tab4, algorithm4);
			try {
				launchSimulation(simulation, i);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			substrates4.get(0).print();
			substrates2.get(0).print();
			substrates1.get(0).print();
			substrates3.get(0).print();
			substrates.get(0).print();
			*/

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
			try {
				launchSimulation(simulation2, i);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
/*			substrates3.get(0).print();
			try {
		        System.in.read();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }

			AlgorithmNF algorithm3 = new AlgorithmNF("ILP1");
			SimulationNFV simulation3 = new SimulationNFV(InPs, substrates3, request_tab3, algorithm3);
			try {
				launchSimulation(simulation3, i);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/




        }
    }



    private static double[] launchSimulation(SimulationNFV simulation,int inD) throws Exception{
        //results, 0 cost, 1 time, 2, denial
        double[] results=new double[3];
        int denials = 0;
        double cost=0;
        double cpuCost=0;
        double bwCost=0;
        double revenue=0;
        double cpu_util = 0;
        double bw_util=0;
        double sol_time=0;
        double max_util_server=0;
        double max_util_link=0;
        int requested= 0;
        int viol_cpu = 0;  //requests violating their SLAs
        int viol_cpu_mon=0; //monitoring violations
        int mon_instances=0; //cum  monitoring instances
        int collocated=0;

        try {
            Writer writer=null;
            Writer writer2=null;
            String path = "results";

            new File(path).mkdirs();

            String filename = "input"+ simulation.getAlgorithm().getId()+ "-" + inD + ".xlsx";
            System.out.println(filename);
            if (simulation.getAlgorithm().getId().contains("RL")) {
                String filename1 = simulation.getAlgorithm().getId()+"_AD.txt";
                writer = new BufferedWriter(new FileWriter(path+File.separator+filename1));
                writer = new NullWriter();
                String filename2 = simulation.getAlgorithm().getId()+"_Memory.txt";
                writer2 = new BufferedWriter(new FileWriter(path+File.separator+filename2));
                writer2 = new NullWriter();
            }


            FileOutputStream fileOut = new FileOutputStream(path+File.separator+filename);
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet s = workbook.createSheet("Sheet1");
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);
            headerFont.setColor(IndexedColors.RED.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            Row headerRow = s.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }




            int simulationTime = (int)simulation.getEndDate() +10000;
            System.out.println(simulationTime);
            // System.exit(0);
            List<Substrate> substrates = simulation.getSubstrates();

            // add server columns
            int headerCellIndex = columns.length;
            for (Node current : substrates.get(0).getGraph().getVertices()) {
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

            Substrate InPs = simulation.getInPs();
            AlgorithmNF algorithm = simulation.getAlgorithm();
            List<Request> endingRequests;
            List<Request> startingRequests;
            List<Request> updatedRequests;
            String currentReq = "none";
            Double reward =  0.0;
            ArrayList<Integer> m_ts  = new ArrayList<Integer>();
            algorithm.addSubstrate(substrates);
            algorithm.addInPs(InPs);
            algorithm.addNFs(NFs);
            Monitor monAgent =  new Monitor();
            ArrayList<Request> embedded = new ArrayList<Request>();

	  /*    if (monitoring) {
	      monAgent.generateMInstances(simulationTime);
	      m_ts  = monAgent.getTS();
	      }*/

            int counter2=0;
            for (int i=0; i<(simulationTime+10); i++) {
                //System.out.println("Monitoring Moment: " + i );
                // Release ended simulations in the moment "i"
                endingRequests = simulation.getEndingRequests(i);
                simulation.releaseRequests(endingRequests,i);
                for (Request endReq:endingRequests){  ///remove from embedded
                    embedded.remove(endReq);
                }
                // Allocate arriving requests in the moment "i"
                startingRequests = simulation.getStartingRequests(i);
                algorithm.addRequests(startingRequests);
                algorithm.addMAgent(monAgent);
//                if (algorithm.getId().contains("max") || algorithm.getId().contains("avg") ) {
//                    for(Request upReq: startingRequests){
//                        upReq.getDFG().resetGraph(upReq.getGraph(),algorithm.getId());
//                    }
//                }
                if (dynamic) {
                    updatedRequests = simulation.getUpdatedRequests(i);

                    if (updatedRequests.size()>0){
                        //System.out.println("updatedRequests: " +updatedRequests.size());

                        simulation.releaseRequests(updatedRequests,i);
                        for(Request upReq: updatedRequests){
                            //upReq.print();
                            if (!(upReq.getRMapNF().isDenied())) {
                                upReq.getDFG().updateGraph(upReq.getGraph(), i, simulationTime, false);
                            }
                            //upReq.print();

                        }
                        simulation.updateRequests(updatedRequests,i);
                    }

                }
                //////////////////////////////////
                if (monitoring) {
                    //if (m_ts.contains(i)) {
                    mon_instances++;
                    //System.out.println("Monitoring Moment: " + i );
                    if (!(currentReq.equalsIgnoreCase(monAgent.getRequestID()))) {
                        reward=0.0;
                    }
                    boolean overSubscription = false;
                    Collection<Node> tmp_nodes = substrates.get(0).getGraph().getVertices();
                    Iterator<Node> iterator = tmp_nodes.iterator();

                    // while loop
                    while (iterator.hasNext()) {
                        Node current = iterator.next();
                        if (!(current.getType().equalsIgnoreCase("Switch"))) {
                            //	double util = current.getAvailableCpu()/(current.getCpu()+Double.MIN_VALUE);
                            //System.out.println("Monitoring util: " + util );
                            //	if (util<0.1) {
                            if (current.getAvailableCpu()<0) {
                                overSubscription=true;
                                break;
                            }
                        }
                    }

                    if (overSubscription) {
                        reward=reward-0.1;
                        viol_cpu_mon++;
                        //System.out.println(currentReq + " " + monAgent.getRequestID() + " " +reward);
                    }

                    monAgent.setRequestID(currentReq);
                    monAgent.setPenalty(reward);
                    //System.out.println(currentReq + " " + monAgent.getRequestID() + " " +monAgent.getPernalty());

                    //}
                }


                if (startingRequests.size()>0){
                    //System.out.println("startingRequests: " +startingRequests.size());

                    boolean ret = algorithm.runAlgorithm(embedded,i);
                    if (!ret)
                        throw new Exception("Algorithm error");


                    for (Request req:startingRequests) {
                        currentReq = req.getId();


                        System.out.println("Taking results");
                        requested++;
                        if (req.getRMapNF().isDenied() ){
                            denials++;
                        }else{
                            embedded.add(req);
                            revenue += req.getRMapNF().getEmbeddingRevenue();
                            cost += req.getRMapNF().getEmbeddingCost();
                            cpuCost+= req.getRMapNF().getCPUCost();
                            bwCost += req.getRMapNF().getBWCost();
                            sol_time += req.getRMapNF().getSolTime();
                            //avg_hops += req.getRMapNF().getHops();

                            if (req.getRMapNF().getOverpCPU()) {
                                viol_cpu++;
                            }
                            if (!(req.getRMapNF().getServersUsed()>1)) {
                                collocated++;
                            }

                            ///////////////////////////////////
                            cpu_util = req.getRMapNF().Node_utilization_Server_Cpu(substrates.get(0));
                            bw_util = req.getRMapNF().Link_utilization(substrates.get(0));
                            max_util_server = req.getRMapNF().max_util_server(substrates.get(0));
                            max_util_link =  req.getRMapNF().max_link_utilization(substrates.get(0));

                            /*	System.out.println(revenue+ " " +cost+ " " +avg_hops+" "+rules + " "+requested+ " "+denials);
                             */
                        }
                        counter2++;
                        Row row = s.createRow(counter2);
                        row.createCell(0).setCellValue(i);
                        row.createCell(1).setCellValue((double)(requested-denials)/(double)requested);
                        row.createCell(2).setCellValue(revenue);
                        row.createCell(3).setCellValue(cost);
                        row.createCell(4).setCellValue(cpuCost);
                        row.createCell(5).setCellValue(bwCost);
                        row.createCell(6).setCellValue(cpu_util);
                        row.createCell(7).setCellValue(bw_util);
                        row.createCell(8).setCellValue(max_util_server/cpu_util);
                        row.createCell(9).setCellValue(max_util_link/bw_util);
                        row.createCell(10).setCellValue(viol_cpu);
                        row.createCell(11).setCellValue(sol_time/(double)requested);
                        row.createCell(12).setCellValue(viol_cpu/(double)requested);
                        row.createCell(13).setCellValue((double)(denials)/(double)requested);
                        row.createCell(14).setCellValue((double)(requested-denials-collocated));
                        row.createCell(15).setCellValue((double)(collocated));
                        row.createCell(16).setCellValue((double)(requested-denials));
                        row.createCell(17).setCellValue((double)(denials));
                        row.createCell(18).setCellValue((double)viol_cpu_mon);
                        row.createCell(19).setCellValue((double)mon_instances);
                        row.createCell(20).setCellValue(req.getGraph().getVertexCount());
                        row.createCell(21).setCellValue(req.getGraph().getEdgeCount());
                        row.createCell(22).setCellValue((double) req.getWl());
                        row.createCell(23).setCellValue((double) req.getTotalBw());

                        int cellIndex = 24;
                        for (Node current : substrates.get(0).getGraph().getVertices()) {
                            if (!(current.getType().equalsIgnoreCase("Switch"))) {
                                row.createCell(cellIndex).setCellValue(current.getAvailableCpu() / (double) current.getCpu());

                                double life = 0;
                                double reqHosted = 0;

                                for (Request act : embedded) {
                                    if (act.getRMapNF().containsNodeInMap(current)) {
                                        life = life + (act.getEndDate() - algorithm.getTs());
                                        reqHosted++;
                                    }
                                }

                                row.createCell(cellIndex + 1).setCellValue(life / (double) reqHosted);

                                cellIndex += 2;
                            }
                        }
                        if ( simulation.getAlgorithm().getId().contains("RL")) {
                            if (belief) {
                                writer.write( req.getId() + "	"+ Arrays.deepToString(req.getRMapNF().getQb())+"\n");
                                writer2.write( req.getId() + "	"+ Arrays.deepToString(req.getRMapNF().getUb())+"\n");
                            }else {
                                writer.write( req.getId() + "	"+ Arrays.deepToString(req.getRMapNF().getQ())+"\n");
                                writer2.write( req.getId() + "	"+ Arrays.deepToString(req.getRMapNF().getU())+"\n");
                            }
                        }

                    } //starting iterate
                }   //  starting


            }//end of simulation

            workbook.write(fileOut);
            workbook.close();
            fileOut.close();
            if ( simulation.getAlgorithm().getId().contains("RL")) {
                writer.close();
                writer2.close();
            }


        }
        catch (IOException e)
        {
            e.printStackTrace();

        }

  /* catch (WriteException e)
    {
      e.printStackTrace();
    }
  	*/

        simulation.getAlgorithm().clean();

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

    public static ArrayList<List<Request>>  createFG(int numRequests) throws CloneNotSupportedException{
        final String prefix ="req";
        final String timeDistribution = SimulatorConstants.POISSON_DISTRIBUTION;
        final int fixStart=0;
        final int uniformMin=0;
        final int uniformMax=0;
        final int normalMean=0;
        final int normalVariance=0;

        ArrayList<List<Request>> tmp = new ArrayList<List<Request>> ();
        final List<Request> requests = new ArrayList<Request>();
        final List<Request> requests1 = new ArrayList<Request>();
        final List<Request> requests2 = new ArrayList<Request>();
        final List<Request> requests3 = new ArrayList<Request>();
        final List<Request> requests4 = new ArrayList<Request>();
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
            requests1.add((Request)request.clone());
            requests2.add((Request)request.clone());
            requests3.add((Request)request.clone());
            requests4.add((Request)request.clone());
            //avg_wl +=request.getWl();

        }

        tmp.add(requests);
        tmp.add(requests1);
        tmp.add(requests2);
        tmp.add(requests3);
        tmp.add(requests4);
        req_wl=avg_wl;
        //   System.exit(0);
        return tmp;

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
