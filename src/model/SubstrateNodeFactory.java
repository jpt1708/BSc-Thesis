package model;

import java.io.Serializable;
import java.util.Date;
import java.util.Random;

import model.components.Node;
import model.components.Server;
import model.components.SubstrateRRH;
import model.components.SubstrateRouter;
import model.components.SubstrateSwitch;
import simenv.SimulatorConstants;

import org.apache.commons.collections15.Factory;

import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;

/**
 * This class is a factory of SubstrateNode. It generates the elements
 * with random parameters. Ranges for randomness can be found on 
 * SimulatorConstants class
 */
public class SubstrateNodeFactory implements Factory<Node>, Serializable {

	public static double MIN_CPU_RACK = 0.0;
	public static double MAX_CPU_RACK = 1.0;


	private int nodeCount;
	Random rand = new Random();
	int  n = rand.nextInt(400) + 100;
	MersenneTwister engine = new MersenneTwister(n);
	Uniform myUniformDist = new Uniform(engine);
	
	public SubstrateNodeFactory() {
		super();
		nodeCount = 0;

		
	}
	
	/** Generate a random node **/
	public Node create() {
		Node node = null;
		int cpu;
		int memory;
		String os=null;
		String veType=null;

		int nodeType = (int) (Math.random()*10);
		if ( (nodeType>=0) && (nodeType<2) ){
				node = new SubstrateRouter(nodeCount);
				node.setVlans(SimulatorConstants.MAX_ROUTER_VLANS);
				((SubstrateRouter)node).setLogicalInstances(SimulatorConstants.MAX_LOGICAL_INSTANCES);
				((SubstrateRouter)node).setAvailableLogicalInstances(SimulatorConstants.MAX_LOGICAL_INSTANCES);
		}
		else{
				node = new Server(nodeCount);
				// Random diskSpace generation
				int diskSpace = SimulatorConstants.MIN_DISK 
							+ (int)(Math.random()*((SimulatorConstants.MAX_DISK 
							- SimulatorConstants.MIN_DISK) + 1));
				((Server) node).setDiskSpace(diskSpace);
				((Server) node).setAvailableDiskSpace(diskSpace);
				node.setVlans(SimulatorConstants.MAX_SERVER_VLANS);

				// Random cpu generation
				cpu = SimulatorConstants.MIN_CPU 
							+ (int)(Math.random()*((SimulatorConstants.MAX_CPU 
							- SimulatorConstants.MIN_CPU) + 1));
				node.setCpu(cpu);
				node.setAvailableCpu(cpu);
				node.setAvailableAllocatedCpu(cpu);
				
				// Random ram generation
				memory = SimulatorConstants.MIN_MEMORY 
							+ (int)(Math.random()*((SimulatorConstants.MAX_MEMORY 
							- SimulatorConstants.MIN_MEMORY) + 1));
				node.setMemory(memory);
				node.setAvailableMemory(memory);
				System.exit(0);
			}

		
		
	int operation=(int) (Math.random()*4);
		
		switch (operation){
			case 0:
				os="Linux";
				break;
			case 1:
				os="Windows";
				break;
			case 2:
				os="Solaris";
				break;
			case 3:
				os="Android";
				break;
		}
		
		//node.setOS(os);
		node.setOS("Linux");
		
		
		int vtype=(int) (Math.random()*4);
		
		switch (vtype) {
			case 0:
				veType="VMWare";
				break;
			case 1:
				veType="XEN";
				break;
			case 2:
				veType="KVM";
				break;
			case 3:
				veType="uml";
				break;
		}
		
		//node.setVEType(veType);
		node.setVEType("VMWare");


		nodeCount++;
		return node;
	}
	
	/** Generate a Node specified by its type **/
	public Node create(String nodeType) {
		Node node = null;
		int cpu;
		int memory;
			
		if (nodeType.equalsIgnoreCase("router")) {
			node = new SubstrateRouter(nodeCount);
			node.setVlans(SimulatorConstants.MAX_ROUTER_VLANS);
			((SubstrateRouter)node).setLogicalInstances(SimulatorConstants.MAX_LOGICAL_INSTANCES);
/*			cpu = SimulatorConstants.MIN_CPU 
					+ (int)(Math.random()*((SimulatorConstants.MAX_CPU 
					- SimulatorConstants.MIN_CPU) + 1));
			node.setCpu(cpu);
			memory = SimulatorConstants.MIN_MEMORY 
					+ (int)(Math.random()*((SimulatorConstants.MAX_MEMORY 
					- SimulatorConstants.MIN_MEMORY) + 1));
		    node.setMemory(memory);*/
		}
		else if (nodeType.equalsIgnoreCase("switch")) {
			//double util = myUniformDist.nextDoubleFromTo(0.4,0.7);
			//double util =1;
			node = new SubstrateSwitch(nodeCount); 
			node.setVlans(SimulatorConstants.MAX_SWITCH_VLANS);
			//int tcam = (int) (util*SimulatorConstants.MAX_SWITCH_TCAM);
			//node.setTCAM(tcam);
			//node.setAvailableTCAM(SimulatorConstants.MAX_SWITCH_TCAM);
		/*	cpu = SimulatorConstants.MIN_CPU 
					+ (int)(Math.random()*((SimulatorConstants.MAX_CPU 
					- SimulatorConstants.MIN_CPU) + 1));
			node.setCpu(cpu);
			memory = SimulatorConstants.MIN_MEMORY 
					+ (int)(Math.random()*((SimulatorConstants.MAX_MEMORY 
					- SimulatorConstants.MIN_MEMORY) + 1));
		    node.setMemory(memory);*/
		}
		else if (nodeType.equalsIgnoreCase("RRH_agg")) {
			node = new SubstrateRRH(nodeCount); 
			node.setVlans(SimulatorConstants.MAX_SWITCH_VLANS);
			//node.setCpu(60);
			//node.setAvailableCpu(60);
/*			cpu = SimulatorConstants.MIN_CPU 
					+ (int)(Math.random()*((SimulatorConstants.MAX_CPU 
					- SimulatorConstants.MIN_CPU) + 1));
			node.setCpu(cpu);
			memory = SimulatorConstants.MIN_MEMORY 
					+ (int)(Math.random()*((SimulatorConstants.MAX_MEMORY 
					- SimulatorConstants.MIN_MEMORY) + 1));
		    node.setMemory(memory);*/
		}
		else if (nodeType.equalsIgnoreCase("IXP")) {
			node = new SubstrateRouter(nodeCount); 
			node.setVlans(SimulatorConstants.MAX_SWITCH_VLANS);
			//node.setCpu(60);
/*			cpu = SimulatorConstants.MIN_CPU 
					+ (int)(Math.random()*((SimulatorConstants.MAX_CPU 
					- SimulatorConstants.MIN_CPU) + 1));
			node.setCpu(cpu);
			memory = SimulatorConstants.MIN_MEMORY 
					+ (int)(Math.random()*((SimulatorConstants.MAX_MEMORY 
					- SimulatorConstants.MIN_MEMORY) + 1));
		    node.setMemory(memory);*/
		}
		else if (nodeType.equalsIgnoreCase("rackserver")) {
			double util = myUniformDist.nextDoubleFromTo(0.8,0.9);
			//	double util=1;
			node = new Server(nodeCount);
			// Random diskSpace generation
			int diskSpace = SimulatorConstants.RACK_SERVER_DISK;
			((Server) node).setDiskSpace(diskSpace);
			// Random cpu generation
			cpu = (int) (SimulatorConstants.RACK_SERVER_CPU);
			((Server) node).setCpu(cpu);
			((Server) node).setNominalCpu((int)SimulatorConstants.RACK_SERVER_CPU);
			((Server) node).setAvailableCpu(cpu);
			((Server) node).setAvailableAllocatedCpu(cpu);
			// Random ram generation
			memory = SimulatorConstants.RACK_SERVER_MEMORY;
			((Server) node).setMemory(memory);
		}
		else if (nodeType.equalsIgnoreCase("server")) {
			node = new Server(nodeCount);
			// Random diskSpace generation
			int diskSpace = SimulatorConstants.MIN_DISK 
						+ (int)(Math.random()*((SimulatorConstants.MAX_DISK 
						- SimulatorConstants.MIN_DISK) + 1));
			((Server) node).setDiskSpace(diskSpace);
			node.setVlans(SimulatorConstants.MAX_SERVER_VLANS);
			// Random cpu generation
			cpu = SimulatorConstants.MIN_CPU 
						+ (int)(Math.random()*((SimulatorConstants.MAX_CPU 
						- SimulatorConstants.MIN_CPU) + 1));
			((Server) node).setCpu(cpu);
			// Random ram generation
			memory = SimulatorConstants.MIN_MEMORY 
						+ (int)(Math.random()*((SimulatorConstants.MAX_MEMORY 
						- SimulatorConstants.MIN_MEMORY) + 1));
			((Server) node).setMemory(memory);
		}
		
 

		nodeCount++;
		return node;
	}
	
	
	public Node create_switch(String nodeType) {
		Node node = null;
		int cpu;
		int memory;
			
		if (nodeType.equalsIgnoreCase("tor")) {
			//double util = myUniformDist.nextDoubleFromTo(0.4,0.7);
			//double util =1;
			node = new SubstrateSwitch(nodeCount); 
			node.setVlans(SimulatorConstants.MAX_SWITCH_VLANS);
			//int tcam = (int) (util*SimulatorConstants.MAX_SWITCH_TORTCAM);
			//node.setTCAM(tcam);
			//node.setAvailableTCAM(SimulatorConstants.MAX_SWITCH_TORTCAM);
		}
		else {
			//double util = myUniformDist.nextDoubleFromTo(0.4,0.7);
			//double util =1;
			node = new SubstrateSwitch(nodeCount); 
			node.setVlans(SimulatorConstants.MAX_SWITCH_VLANS);
			//int tcam = (int) (util*SimulatorConstants.MAX_SWITCH_TCAM);
			//node.setTCAM(tcam);
			//node.setAvailableTCAM(SimulatorConstants.MAX_SWITCH_TCAM);
		}
		


		nodeCount++;
		return node;
	}
	
	public Object getCopy() {
		SubstrateNodeFactory f = new SubstrateNodeFactory();
		f.nodeCount = this.nodeCount;
		return f;
	}

	public int getNodeCount() {
		return nodeCount;
	}

	public void setNodeCount(int nodeCount) {
		this.nodeCount = nodeCount;
	}

}
