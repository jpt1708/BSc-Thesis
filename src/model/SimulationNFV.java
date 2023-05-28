package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import edu.uci.ics.jung.graph.util.Pair;
import model.components.Link;
import model.components.NF;
import model.components.Node;
import model.components.SubstrateLink;
import model.components.SubstrateSwitch;
import simenv.SimulatorConstants;

import org.apache.commons.io.output.NullWriter;
import org.apache.commons.collections15.Factory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.twelvemonkeys.io.NullOutputStream;

/**
 * This class is the instantiation of a simulation. It is defined by a set
 * of requests, a substrate and an algorithm
 */
public class SimulationNFV implements Cloneable, Runnable {
	
	private String id;
	private Substrate InPs;
	private List<Substrate> substrates;
	private ArrayList<Request> toEmbed;
	private ArrayList<Request> embedded;
	private AlgorithmNF algorithm;
	private List<Node> NFs;
	private int timestep;

	// \/ for data collection \/
	private double[] results=new double[3];
	private int denials = 0;
	private double cost=0;
	private double cpuCost=0;
	private double bwCost=0;
	private double revenue=0;
	private double cpu_util = 0;
	private double bw_util=0;
	private double sol_time=0;
	private double max_util_server=0;
	private double max_util_link=0;
	private int requested= 0;
	private int viol_cpu = 0;  //requests violating their SLAs
	private int viol_cpu_mon=0; //monitoring violations
	private int mon_instances=0; //cum  monitoring instances
	private int collocated=0;

	private static boolean belief = true; // from MainWithoutGUI

	private final Object lock;
	public boolean ready;

	private static String[] columns = {"Time", "Acceptance", "Cum Revenue", "Cum Cost",
            "Cum CPUCost", "Cum BWCos", "Avg Server Util", "Avg Link Util", "LBL Server", "LBL link",
            "Violations", "Avg Time PR", "Violation Ratio", "Rejection Ratio", "Non Collocated", "Collocated",
            "Accepted", "Rejected", "Violations Monitoring", "Monitoring Instances", "SFC Nodes", "SFC Links",
            "Total SFC Workload", "Total SFC Bandwidth"};

	
	/** Creates a new instance of Substrate */
    public SimulationNFV(Substrate InPs, List<Substrate> substrates,
        		AlgorithmNF algorithm2, String id, List<Node> NFs, Object lock) {
		this.lock = lock;
    	this.substrates = substrates;
    	this.algorithm = algorithm2;
		this.toEmbed = new ArrayList<Request>();
		this.embedded = new ArrayList<Request>();
    	this.InPs = InPs;
		this.id = id;
    	this.NFs=NFs;
		this.algorithm.addSubstrate(substrates);
		this.algorithm.addInPs(InPs);
		this.algorithm.addNFs(NFs);
		this.timestep = 0; // is set by orchestrator after every step in main loop
		this.ready = false;
		System.out.println("DC id: " + this.id);
    }

        /** Creates a new instance of Substrate */
    public SimulationNFV(Substrate InPs, List<Substrate> substrates,
        		AlgorithmNF algorithm2, String id, Object lock) {
		this.lock = lock;
    	this.substrates = substrates;
    	this.algorithm = algorithm2;
		this.toEmbed = new ArrayList<Request>();
		this.embedded = new ArrayList<Request>();
    	this.InPs = InPs;
		this.id = id;
		this.algorithm.addSubstrate(substrates);
		this.algorithm.addInPs(InPs);
		this.timestep = 0; // is set by orchestrator after every step in main loop
		this.ready = false;
		System.out.println("DC id: " + this.id);


/*    	System.out.println("id: " + substrates.get(0));
		try {
	        System.in.read();
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }*/


    }
	// Set ready variable to true to end waiting loop on lock
	public void go() {
		this.ready = true;
	}

	public List<Request> getEndingRequests(int time) {
		List<Request> endingRequests = new ArrayList<Request>();
		for (Request req : embedded){
			if (req.getEndDate()==time){
				endingRequests.add(req);
			}
		}
		return endingRequests;
	}

	public ArrayList<Request> getToEmbed() {
		return this.toEmbed;
	}

	public void setToEmbed(ArrayList<Request> toEmbed) {
		this.toEmbed = toEmbed;
	}

	public void run() {
		// Initialize data collecting classes
		int counter2=0;
		Writer writer=null;
		Writer writer2=null;
		String path = "results";

		new File(path).mkdirs();
		//  String filename = "input"+ orchestrator.getDCs().get(0).getAlgorithm().getId()+ "-" + "TMP" + ".xlsx";
		String filename = "outputData-" + this.id + ".xlsx";
		System.out.println(filename);
		if (this.id.contains("RL")) {
			String filename1 = this.id + "_AD.txt";
			try {
				writer = new BufferedWriter(new FileWriter(path+File.separator+filename1));
			} catch (IOException e) {
				//TODO: maybe handle exception
			}
			writer = new NullWriter();
			String filename2 = this.id + "_Memory.txt";
			try {
				writer2 = new BufferedWriter(new FileWriter(path+File.separator+filename2));
			} catch (IOException e) {
				writer2 = new NullWriter();
				//TODO: maybe handle exception
			}
			writer2 = new NullWriter();
		}
		OutputStream fileOut;
		try {
			fileOut = new FileOutputStream(path+File.separator+filename);
		} catch (Exception e) {
			fileOut = new NullOutputStream();
			System.out.println("[ERROR] Excel sheet writer for " + this.id + " is null");
			// TODO: handle exception
		}
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet dataSheet = workbook.createSheet("Sheet1");
		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeightInPoints((short) 14);
		headerFont.setColor(IndexedColors.RED.getIndex());

		CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setFont(headerFont);

		Row headerRow = dataSheet.createRow(0);
		for (int i = 0; i < columns.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(columns[i]);
			cell.setCellStyle(headerCellStyle);
		}
		int headerCellIndex = columns.length;

		for (Node current : this.substrates.get(0).getGraph().getVertices()) {
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

		// Data collecting set up finished

		for (int timestep = 0; ; timestep++) { // Break when toEmbed set to null by orchestrator
			// free ending requests, updated?
			// One iteration of this loop is one timestep in the simulation
			// Sleep until Orchestrator has set requests and timestep, then wake up and do an iteration
			synchronized (lock) {
				try {
					while (!ready) {
						lock.wait();
					}
				} catch (InterruptedException e) {
					System.out.println("DC " + this.id + " interrupted");
				}
			}
			// wake up -- toEmbed should be set by orchestrator
			
			if (this.toEmbed == null) { // if set to null by orchestrator, simulation is over
				System.out.println("DC " + this.id + " toEmbed null, break");
				break;
			}
			
			//if (this.toEmbed.size() > 0) {
			System.out.println(this.id + " timestep = " + timestep + ", " + this.toEmbed.size() + " requests");
			//}

			List<Request> endingReqs = getEndingRequests(timestep);
			releaseRequests(endingReqs, timestep);
			embedded.removeAll(endingReqs);

			this.algorithm.addRequests(toEmbed);
			//TODO: add monitoring agent? -- this was done in old main sim loop
			boolean ret = this.algorithm.runAlgorithm(embedded, timestep);
			if (!ret) {
				System.out.println("[ERROR] Algorithm in DC " + this.id + " failed.");
			}

			for (Request cur_req : toEmbed) {
				//System.out.println("DC " + this.id + " Req " + cur_req_id + " results");
				requested++;
				if (cur_req.getRMapNF().isDenied()) {
					denials++;
				} else {
					embedded.add(cur_req);
					revenue += cur_req.getRMapNF().getEmbeddingRevenue();
					cost += cur_req.getRMapNF().getEmbeddingCost();
					cpuCost+= cur_req.getRMapNF().getCPUCost();
					bwCost += cur_req.getRMapNF().getBWCost();
					sol_time += cur_req.getRMapNF().getSolTime();
					//avg_hops += req.getRMapNF().getHops();

					if (cur_req.getRMapNF().getOverpCPU()) {
						viol_cpu++;
					}
					if (!(cur_req.getRMapNF().getServersUsed()>1)) {
						collocated++;
					}

					///////////////////////////////////
					cpu_util = cur_req.getRMapNF().Node_utilization_Server_Cpu(this.substrates.get(0));
					bw_util = cur_req.getRMapNF().Link_utilization(this.substrates.get(0));
					max_util_server = cur_req.getRMapNF().max_util_server(this.substrates.get(0));
					max_util_link =  cur_req.getRMapNF().max_link_utilization(this.substrates.get(0));
				}
				counter2++;
				Row row = dataSheet.createRow(counter2);
				row.createCell(0).setCellValue(timestep);
				System.out.println("Writing data in " + this.id + " at timestep " + timestep);
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
				row.createCell(20).setCellValue(cur_req.getGraph().getVertexCount());
				row.createCell(21).setCellValue(cur_req.getGraph().getEdgeCount());
				row.createCell(22).setCellValue((double) cur_req.getWl());
				row.createCell(23).setCellValue((double) cur_req.getTotalBw());

				int cellIndex = 24; //TODO: should prolly not be hard coded
				for (Node current : this.substrates.get(0).getGraph().getVertices()) {
					if (!(current.getType().equalsIgnoreCase("Switch"))) {
						row.createCell(cellIndex).setCellValue(current.getAvailableCpu() / (double) current.getCpu());

						double life = 0;
						double reqHosted = 0;

						for (Request act : embedded) {
							if (act.getRMapNF().containsNodeInMap(current)) {
								life = life + (act.getEndDate() - this.algorithm.getTs());
								reqHosted++;
							}
						}

						row.createCell(cellIndex + 1).setCellValue(life / (double) reqHosted);

						cellIndex += 2;
					}
				}
				if (this.algorithm.getId().contains("RL")) {
					try {
						if (belief) {
							writer.write(cur_req.getId() + "	"+ Arrays.deepToString(cur_req.getRMapNF().getQb())+"\n");
							writer2.write(cur_req.getId() + "	"+ Arrays.deepToString(cur_req.getRMapNF().getUb())+"\n");
						} else {
							writer.write(cur_req.getId() + "	"+ Arrays.deepToString(cur_req.getRMapNF().getQ())+"\n");
							writer2.write(cur_req.getId() + "	"+ Arrays.deepToString(cur_req.getRMapNF().getU())+"\n");
						}
					} catch (Exception e) {
						// TODO: handle exception
					}

				}
			}
			toEmbed.clear();
			this.ready = false;
			synchronized (this.lock) {
				this.lock.notify();
			}
		} // End of main sim loop -- for (int timestep = 0; ; timestep++) { // Break when toEmbed set to null by orchestrator
		try {
			System.out.println("Writing workbook of " + this.id + " to " + path+File.separator+filename);
			System.out.println(dataSheet.getPhysicalNumberOfRows() + "rows in the sheet");
			workbook.write(fileOut);
			workbook.close();
			fileOut.close();
		} catch (IOException e) {
			System.out.println("Error writing workbook of " + this.id + " to file");
			e.printStackTrace();
		}
	}

	public Object getCopy(int n) {
		SimulationNFV s = new SimulationNFV(this.InPs, this.substrates, this.algorithm, this.id, this.NFs, this.lock);
		s.setId(this.getId() + "_copy_" + Integer.toString(n));
		return s;
	}
    
    public Substrate getInPs(){
    	return this.InPs;
    }
    
    public List<Node> getNFs(){
    	return this.NFs;
    }
    
    public void setInPs(Substrate InPs){
    	this.InPs=InPs;
    }

	public List<Substrate> getSubstrates(){
		return this.substrates;
	}

	public void setSubstrates(List<Substrate> substrates){
		this.substrates=substrates;
	}
	
	public void setNFs(List<Node> NFs) {
		this.NFs = NFs;
	}
	
	public void changeSubstrate(List<Substrate> newSubstrates) {
		for (Substrate sub: substrates){
			sub.setState(SimulatorConstants.STATUS_AVAILABLE);
		}
		this.substrates = newSubstrates;
		for (Substrate sub: substrates){
			sub.setState(SimulatorConstants.STATUS_READY);
		}
	}
	
	public AlgorithmNF getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(AlgorithmNF algorithm) {
		this.algorithm = algorithm;
	}
	
	public void changeAlgorithm(AlgorithmNF newAlgorithm) {
		this.algorithm.setState(SimulatorConstants.STATUS_AVAILABLE);
		this.algorithm = newAlgorithm;
		newAlgorithm.setState(SimulatorConstants.STATUS_READY);
		
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/** Release resources of the requests from the substrate **/
	public void releaseRequests(List<Request> endingRequests,int i) {

		for (Request req : endingRequests){
			//req.print();
					for (Substrate substrate: substrates){
						//if (subVN.getInP()==substrate.getId()){
							if (!(req.getRMapNF().isDenied())){
								req.getRMapNF().releaseNodes(substrate);
								req.getRMapNF().releaseLinks(substrate);
							//	req.getRMapNF().releaseTCAM(substrate);
								if (i==req.getEndDate())
									System.out.println("//////////Released///////// "+req.getId()+ " at time "+i +" with endTime " +req.getEndDate());
								else 
									System.out.println("//////////Released- Updated///////// "+req.getId()+ " at time "+i +" with endTime " +req.getEndDate());

								//substrate.print();
							}
							//System.out.println("id: " + substrates.get(0));
/*							try {
						        System.in.read();
						    } catch (IOException e) {
						        // TODO Auto-generated catch block
						        e.printStackTrace();
						    }*/
					    	
					}
				}
		}
	
	/** Update resources of the requests from the substrate **/
	public void updateRequests(List<Request> updatedRequests,int i) {
		for (Request req : updatedRequests){
					for (Substrate substrate: substrates){
						//substrate.print();
							if (!req.getRMapNF().isDenied()){
									req.getRMapNF().reserveNodes(substrate);
									req.getRMapNF().reserveLinks(substrate);
									System.out.println("//////////Updated///////// "+req.getId()+ " at time "+i);
									System.out.println();
							}
							//substrate.print();
					}
			}
	}
	
	
	public void initSubstrate(Substrate sub) {
		ArrayList<Node> tor_switch = new ArrayList<Node>();
		ArrayList<Node> dc_switch = new ArrayList<Node>();
		for (Link link : sub.getGraph().getEdges()) {
			Pair<Node> eps  =  sub.getGraph().getEndpoints(link);
			Node src = eps.getFirst();
			Node dst = eps.getSecond();
			 if (link.getLinkType().equalsIgnoreCase("torlink")) {
				if (src.getType().equalsIgnoreCase("switch")){
					if (!(tor_switch.contains(src))){
						src.setTCAM(src.getTCAM()-16);
						tor_switch.add(src);
					}
				}else {
					if (dst.getType().equalsIgnoreCase("switch")){
						if (!(tor_switch.contains(dst))){
							dst.setTCAM(dst.getTCAM()-16);
							tor_switch.add(dst);
						}
					}
				}
			}
			else
			if (link.getLinkType().equalsIgnoreCase("interracklink")) {
				if (!(((SubstrateSwitch)src).getTOR_switch())){
					if (!(dc_switch.contains(src))){
						src.setTCAM(src.getTCAM()-16);
						dc_switch.add(src);
					}	
				}
				if (!(((SubstrateSwitch)dst).getTOR_switch())){
					if (!(dc_switch.contains(dst))){
						dst.setTCAM(dst.getTCAM()-16);
						dc_switch.add(dst);
					}
					
				}
			}
			
		}//link iter
		
	}

	public Object clone() throws CloneNotSupportedException{
		return super.clone();
	}
}
