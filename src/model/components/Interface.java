package model.components;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface Class. Representation of an interface that will
 * be part of a Node and connected to a Link. It could be
 * part of requests and substrates.
 */
public class Interface {

	public enum InterfaceType {Ethernet, Optical, Radio};
	
	/** id of the interface **/
	private int id;
	/** name for visualization **/
	private String name;
	/** Operating System **/
	private InterfaceType interfaceType;
	/** It belongs to that Node **/
	private Node belongsTo;
	/** It is connected to that set of Link **/
	private Link connectedTo;
	/** Virtual links of the interface 
	 * in case it is a substrate interface **/
	private List<Link> virtualLinks;
	
	
	public Interface(int id, Node node, Link link) {
		this.id = id;
		/** Default InterfaceType **/
		this.interfaceType = InterfaceType.Ethernet;
		this.name = "iface"+id;
		this.belongsTo = node;
		this.connectedTo = link;
		this.virtualLinks = new ArrayList<Link>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public InterfaceType getInterfaceType() {
		return interfaceType;
	}

	public void setInterfaceType(InterfaceType interfaceType) {
		this.interfaceType = interfaceType;
	}

	public Node getBelongsTo() {
		return belongsTo;
	}

	public void setBelongsTo(Node belongsTo) {
		this.belongsTo = belongsTo;
	}
	
	public Link getConnectedTo() {
		return connectedTo;
	}

	public void setConnectedTo(Link connectedTo) {
		this.connectedTo = connectedTo;
	}

	public List<Link> getVirtualLinks() {
		return virtualLinks;
	}

	public void setVirtualLinks(List<Link> virtualLinks) {
		this.virtualLinks = virtualLinks;
	}

	public void addVirtualLink(Link link) {
		this.virtualLinks.add(link);
	}
	
}
