package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import model.components.NF;
import model.components.Node;
import model.components.RequestRRH;
import model.components.RequestRouter;
import model.components.RequestSwitch;
import model.components.VirtualMachine;
import simenv.SimulatorConstants;

import org.apache.commons.collections15.Factory;

/**
 * This class is a factory of RequestNode. It generates the elements
 * with random parameters. Ranges for randomness can be found on 
 * SimulatorConstants class
 */
public class RequestNodeFactory implements Factory<Node>, Serializable {

	private int nodeCount;
	
	public RequestNodeFactory() {
		super();
		nodeCount = 0;
	}
	
	public Node create() {
		Node node = null;
		int cpu;
		int memory;
		String os=null;
		String veType=null;
		int nodeType = (int) (Math.random()*10);
		if ( (nodeType>=0) && (nodeType<1) ){
				node = new RequestRouter(nodeCount);
		}
		else if ( (nodeType>=11) && (nodeType<12) ){
				node = new RequestSwitch(nodeCount); 
				// Random VLANs generation
				int vlans = SimulatorConstants.MIN_VLANS_REQUEST 
							+ (int)(Math.random()*((SimulatorConstants.MAX_VLANS_REQUEST 
							- SimulatorConstants.MIN_VLANS_REQUEST) + 1));
				((RequestSwitch) node).setVlans(vlans);
			}
		else{
				node = new VirtualMachine(nodeCount);
				
				// Random cpu generation
				cpu = SimulatorConstants.MIN_CPU_REQUEST 
							+ (int)(Math.random()*((SimulatorConstants.MAX_CPU_REQUEST 
							- SimulatorConstants.MIN_CPU_REQUEST) + 1));
				node.setCpu(cpu);
				// Random ram generation
				memory = SimulatorConstants.MIN_MEMORY_REQUEST 
							+ (int)(Math.random()*((SimulatorConstants.MAX_MEMORY_REQUEST 
							- SimulatorConstants.MIN_MEMORY_REQUEST) + 1));
				node.setMemory(memory);
				
				// Random diskSpace generation
				int diskSpace = SimulatorConstants.MIN_DISK_REQUEST 
							+ (int)(Math.random()*((SimulatorConstants.MAX_DISK_REQUEST 
							- SimulatorConstants.MIN_DISK_REQUEST) + 1));
				((VirtualMachine) node).setDiskSpace(diskSpace);
				
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
			node = new RequestRouter(nodeCount);
		}
		else if (nodeType.equalsIgnoreCase("RRH")) {
			node = new RequestRRH(nodeCount);
			((RequestRRH)node).setInstances(2);
			//node.setCpu(50);
		}
		else if (nodeType.equalsIgnoreCase("IPX")) {
			node = new RequestRouter(nodeCount);
			((RequestRouter)node).setInstances(2);
			//node.setCpu(50);
		}
		else if (nodeType.equalsIgnoreCase("switch")) {
			node = new RequestSwitch(nodeCount); 
			// Random VLANs generation
			int vlans = SimulatorConstants.MIN_VLANS_REQUEST 
						+ (int)(Math.random()*((SimulatorConstants.MAX_VLANS_REQUEST 
						- SimulatorConstants.MIN_VLANS_REQUEST) + 1));
			((RequestSwitch) node).setVlans(vlans);
		}
		else if (nodeType.equalsIgnoreCase("virtualMachine")) {
			node = new VirtualMachine(nodeCount);
			// Random diskSpace generation
			int diskSpace = SimulatorConstants.MIN_DISK_REQUEST 
						+ (int)(Math.random()*((SimulatorConstants.MAX_DISK_REQUEST 
						- SimulatorConstants.MIN_DISK_REQUEST) + 1));
			((VirtualMachine) node).setDiskSpace(diskSpace);
		}else {
			node = new NF(nodeCount,nodeType);
		}
		
		
/*		// Random cpu generation
		cpu = SimulatorConstants.MIN_CPU_REQUEST 
					+ (int)(Math.random()*((SimulatorConstants.MAX_CPU_REQUEST 
					- SimulatorConstants.MIN_CPU_REQUEST) + 1));
		node.setCpu(cpu);
		// Random ram generation
		memory = SimulatorConstants.MIN_MEMORY_REQUEST 
					+ (int)(Math.random()*((SimulatorConstants.MAX_MEMORY_REQUEST 
					- SimulatorConstants.MIN_MEMORY_REQUEST) + 1));
		node.setMemory(memory);*/

		nodeCount++;
		return node;
		
	}
	
	/** Generate a Node specified by its type and instances **/
	public Node create(String nodeType, int instances) {
		Node node = null;
		int cpu;
		int memory;
		if (nodeType.equalsIgnoreCase("router")) {
			node = new RequestRouter(nodeCount);
		}
		else if (nodeType.equalsIgnoreCase("RRH")) {
			node = new RequestRRH(nodeCount);
			((RequestRRH)node).setInstances(instances);
			//node.setCpu(50);
		}
		else if (nodeType.equalsIgnoreCase("IPX")) {
			node = new RequestRouter(nodeCount);
			((RequestRouter)node).setInstances(instances);
			//node.setCpu(50);
		}
		else if (nodeType.equalsIgnoreCase("switch")) {
			node = new RequestSwitch(nodeCount); 
			// Random VLANs generation
			int vlans = SimulatorConstants.MIN_VLANS_REQUEST 
						+ (int)(Math.random()*((SimulatorConstants.MAX_VLANS_REQUEST 
						- SimulatorConstants.MIN_VLANS_REQUEST) + 1));
			((RequestSwitch) node).setVlans(vlans);
		}
		else if (nodeType.equalsIgnoreCase("virtualMachine")) {
			node = new VirtualMachine(nodeCount);
			// Random diskSpace generation
			int diskSpace = SimulatorConstants.MIN_DISK_REQUEST 
						+ (int)(Math.random()*((SimulatorConstants.MAX_DISK_REQUEST 
						- SimulatorConstants.MIN_DISK_REQUEST) + 1));
			((VirtualMachine) node).setDiskSpace(diskSpace);
		}
		
		
/*		// Random cpu generation
		cpu = SimulatorConstants.MIN_CPU_REQUEST 
					+ (int)(Math.random()*((SimulatorConstants.MAX_CPU_REQUEST 
					- SimulatorConstants.MIN_CPU_REQUEST) + 1));
		node.setCpu(cpu);
		// Random ram generation
		memory = SimulatorConstants.MIN_MEMORY_REQUEST 
					+ (int)(Math.random()*((SimulatorConstants.MAX_MEMORY_REQUEST 
					- SimulatorConstants.MIN_MEMORY_REQUEST) + 1));
		node.setMemory(memory);*/

		nodeCount++;
		return node;
		
	}
	
	/** Generate a Node specified by its type NF **/
	public Node create(String nodeType, TrafficLTE tr) {
		NF vnf = null;
		double cpu;
		int temp_val=0;
		
		vnf = new NF(nodeCount,nodeType);
	    cpu = tr.getNF(nodeType).get("load");
	    System.out.println("cpu:  " +cpu +" for " +nodeType);
	    temp_val = (int) Math.floor(cpu);
	    vnf.setCpu(temp_val);
/*	    double in  = tr.getNF(nodeType).get("trc_in_ul")+tr.getNF(nodeType).get("trc_in_dl")+
	    		 tr.getNF(nodeType).get("tru_ul")+tr.getNF(nodeType).get("tru_dl");
	    vnf.setInTraffic(in);
	    double out  = tr.getNF(nodeType).get("trc_out_ul")+tr.getNF(nodeType).get("trc_out_dl")+
	    		 tr.getNF(nodeType).get("tru_ul")+tr.getNF(nodeType).get("tru_dl");*/
	    
	    nodeCount++;
		return vnf;
		
	}
	
/*	public Node create(String nodeType, TrafficLTE tr) {
		NF vnf = null;
		double cpu;
		int temp_val=0;
		
		vnf = new NF(nodeCount,nodeType);
	    cpu = tr.getNF(nodeType).get("load");
	    System.out.println("cpu:  " +cpu +" for " +nodeType);
	    temp_val = (int) Math.floor(cpu);
	    vnf.setCpu(temp_val);
	    double in  = tr.getNF(nodeType).get("trc_in_ul")+tr.getNF(nodeType).get("trc_in_dl")+
	    		 tr.getNF(nodeType).get("tru_ul")+tr.getNF(nodeType).get("tru_dl");
	    vnf.setInTraffic(in);
	    double out  = tr.getNF(nodeType).get("trc_out_ul")+tr.getNF(nodeType).get("trc_out_dl")+
	    		 tr.getNF(nodeType).get("tru_ul")+tr.getNF(nodeType).get("tru_dl");
	    
	    nodeCount++;
		return vnf;
		
	}*/
	
	public Node create(String nodeType, TrafficFG tr, ArrayList<Double> traffic) {
		NF vnf = null;
		double cpu;
		int temp_val=0;
		
		vnf = new NF(nodeCount,nodeType);
	   // cpu = tr.generateTrafficLoad(nodeType,traffic).get("load");
	   // System.out.println("cpu:  " +cpu +" for " +nodeType);
	    //temp_val = (int) Math.floor(cpu);
	   // vnf.setCpu(temp_val);

	    
	    
/*	    double in  = tr.getNF(nodeType).get("trc_in_ul")+tr.getNF(nodeType).get("trc_in_dl")+
	    		 tr.getNF(nodeType).get("tru_ul")+tr.getNF(nodeType).get("tru_dl");
	    vnf.setInTraffic(in);
	    double out  = tr.getNF(nodeType).get("trc_out_ul")+tr.getNF(nodeType).get("trc_out_dl")+
	    		 tr.getNF(nodeType).get("tru_ul")+tr.getNF(nodeType).get("tru_dl");*/
	    
	    nodeCount++;
		return vnf;
		
	}
	
	/** Generate a Node specified by its type NF **/
	public Node create(String nodeType, TrafficLTE tr, int instances) {
		NF vnf = null;
		double cpu;
		int temp_val=0;
		
		vnf = new NF(nodeCount, nodeType,instances);
	    cpu = tr.getNF(nodeType).get("load");
	    System.out.println("cpu:  " +cpu +" for " +nodeType);
	    temp_val = (int) Math.floor(cpu);
	    vnf.setCpu(temp_val);
/*	    double in  = tr.getNF(nodeType).get("trc_in_ul")+tr.getNF(nodeType).get("trc_in_dl")+
	    		 tr.getNF(nodeType).get("tru_ul")+tr.getNF(nodeType).get("tru_dl");
	    vnf.setInTraffic(in);
	    double out  = tr.getNF(nodeType).get("trc_out_ul")+tr.getNF(nodeType).get("trc_out_dl")+
	    		 tr.getNF(nodeType).get("tru_ul")+tr.getNF(nodeType).get("tru_dl");*/
	    
	    nodeCount++;
		return vnf;
		
	}

	public Object getCopy() {
		RequestNodeFactory f = new RequestNodeFactory();
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
