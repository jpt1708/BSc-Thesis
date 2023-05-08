package tom;


import cern.jet.random.Exponential;
import cern.jet.random.engine.MersenneTwister;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.filters.FilterUtils;
import edu.uci.ics.jung.algorithms.generators.random.ErdosRenyiGenerator;
import edu.uci.ics.jung.graph.*;
import model.*;
import model.components.Link;
import model.components.Node;
import monitoring.Monitor;
import org.apache.commons.collections15.Factory;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import simenv.SimulatorConstants;
import tom.model.AlgorithmMILPmax;
import tom.model.AlgorithmRA;
import tom.model.AlgorithmRLb;
import tom.model.SimulationNFV;
import tom.MainWithoutGUITom.*;

import java.io.*;
import java.util.*;

public class MainWithoutGUITomDynamic {
    static ArrayList<Node> sRRHs = new ArrayList<Node>();
    static ArrayList<Node> sIXPs = null;
    static ArrayList<Node> NFs = null;
    static double req_wl = 0;
    static int sub_nodes = 0;
    static double nom_cap = 0;
    static boolean monitoring = true;
    static boolean dynamic = true;
    static boolean belief = true;

    private static String[] columns = {"Time", "Acceptance", "Cum Revenue", "Cum Cost",
            "Cum CPUCost", "Cum BWCos", "Avg Server Util", "Avg Link Util", "LBL Server", "LBL link",
            "Violations", "Avg Time PR", "Violation Ratio", "Rejection Ratio", "Non Collocated", "Collocated",
            "Accepted", "Rejected", "Violations Monitoring", "Monitoring Instances"};

    public static void main(String[] args) throws CloneNotSupportedException {
        // Number of experiments to execute
        int experiments = 10;

        for (int i = 0; i < experiments; i++) {
            try {
                int dc_no = 1;
                //Create an abstract graph where each node represent an InP
                Substrate InPs = new Substrate("InPs");
                ArrayList<Substrate> nfvi = MainWithoutGUITom.createSubGraph(dc_no);


                //Substrate nfvi1 = (Substrate)nfvi.getCopy(EdgeType.DIRECTED);
                //Create each InP
                List<Substrate> substrates = new ArrayList<Substrate>();
                List<Substrate> substrates1 = new ArrayList<Substrate>();
                List<Substrate> substrates2 = new ArrayList<Substrate>();
                List<Substrate> substrates3 = new ArrayList<Substrate>();
                List<Substrate> substrates4 = new ArrayList<Substrate>();
                substrates.add(nfvi.get(0));
                substrates1.add((Substrate) nfvi.get(0).clone());
                substrates2.add((Substrate) nfvi.get(0).clone());
                substrates3.add((Substrate) nfvi.get(0).clone());
                
                substrates4.add((Substrate) nfvi.get(0).clone());
                //Create the Requests
                List<Request> request_tab = new ArrayList<Request>();
                List<Request> request_tab1 = new ArrayList<Request>();
                List<Request> request_tab2 = new ArrayList<Request>();
                List<Request> request_tab3 = new ArrayList<Request>();
                List<Request> request_tab4 = new ArrayList<Request>();
                ArrayList<List<Request>> requests = new ArrayList<List<Request>>();
                requests = MainWithoutGUITom.createFG(10000, true, false);
                request_tab = requests.get(0);
                request_tab1 = requests.get(1);
                request_tab2 = requests.get(2);
                request_tab3 = requests.get(3);
                request_tab4 = requests.get(4);
/*			try {
		        System.in.read();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }
		*/
                System.out.println("The simulator is working");
                //System.exit(0);
                MainWithoutGUITom.dynamic = true;


//                tom.model.Algorithm algorithm = new AlgorithmRA();
//                //algorithm1.setStateReq(req_wl/nom_cap);
//                tom.model.SimulationNFV simulation = new tom.model.SimulationNFV(InPs, substrates, request_tab, algorithm);
//                try {
//                    MainWithoutGUITom.launchSimulation(simulation, i, "results_plasticity_final", false);
//                } catch (Exception e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//
//                tom.model.Algorithm algorithm1 = new AlgorithmRLb(substrates1.get(0));
//                tom.model.SimulationNFV simulation1 = new tom.model.SimulationNFV(InPs, substrates1, request_tab1, algorithm1);
//                try {
//                    MainWithoutGUITom.launchSimulation(simulation1, i, "results_plasticity_final", false);
//                } catch (Exception e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }

                tom.model.Algorithm algorithm2 = new AlgorithmMILPmax();
                tom.model.SimulationNFV simulation2 = new tom.model.SimulationNFV(InPs, substrates2, request_tab2, algorithm2);
                try {
                    MainWithoutGUITom.launchSimulation(simulation2, i, "results_plasticity_milp", false);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

                // MainWithoutGUITom.main(args);

    }

}
