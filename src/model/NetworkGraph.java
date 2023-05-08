package model;

import java.util.ArrayList;
import java.util.List;

import model.components.Interface;
import model.components.Link;
import model.components.Node;
import model.components.Server;
import model.components.SubstrateLink;
import model.components.VirtualMachine;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

public class NetworkGraph extends SparseMultigraph<Node, Link> implements UndirectedGraph<Node, Link> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** add an edge and create interfaces involved **/
	@Override
	public boolean addEdge(Link l, Pair<? extends Node> nodes, EdgeType ep) {
		boolean result = super.addEdge(l,nodes,ep);
		if (result) {
			// Creating Interfaces
			Node node1 = nodes.getFirst();
			Node node2 = nodes.getSecond();
			Interface iface1 = new Interface(
					node1.getInterfaces().size(),node1,l);
			Interface iface2 = new Interface(
					node2.getInterfaces().size(),node2,l);
			node1.addInterface(iface1);
			node2.addInterface(iface2);
			l.setEndpoint1(iface1);
			l.setEndpoint2(iface2);
		}
		return result;
	}
	
	/** add an edge with no interfaces **/
	public boolean addEdge(Link l, Node n1,
			Node n2) {
		return super.addEdge(l,n1,n2);
	}
	
	/** add an edge and connect node/link with the given interfaces **/
	public boolean addEdgeWithInterfaces(SubstrateLink link, Pair<Node> nodes,
			Pair<Interface> ifaces, EdgeType edgeType) {
		boolean result = addEdge(link,nodes.getFirst(),nodes.getSecond(),edgeType);
		if (result) {
			// connecting interfaces
			ifaces.getFirst().setConnectedTo(link);
			ifaces.getSecond().setConnectedTo(link);
			link.setEndpoint1(ifaces.getFirst());
			link.setEndpoint2(ifaces.getSecond());
		}
		return result;
		
	}
	
	/** remove an edge and interfaces involved **/
	@Override
	public boolean removeEdge(Link link) {
		boolean result = super.removeEdge(link);
		if (result) {
			// Remove interfaces of the endpoint nodes
			link.getEndpoint1().getBelongsTo().removeInterface(link.getEndpoint1());
			link.getEndpoint2().getBelongsTo().removeInterface(link.getEndpoint2());
		}
		return result;
	}

	public Node getVertexByName(String name) {
		for (Node node : this.getVertices()) {
			if (node.getName().equals(name))
				return node;
		}
		return null;
	}
	
	public void removeUnconnectedNodes() {
		List<Node> removeNodes = new ArrayList<Node>();
		for (Node node : this.getVertices())
			if (this.degree(node)==0)
				removeNodes.add(node);
		for (Node node : removeNodes)
			this.removeVertex(node);
	}

	public Graph<Node, Link> getCopy() {
		NetworkGraph g = new NetworkGraph();
		for (Link link : this.getEdges()) {
			Link l = (Link) link.getCopy();
			Pair<Node> endpoints = this.getEndpoints(link);
			Node n1 = g.getVertexByName(endpoints.getFirst().getName());
			if (n1==null)
				n1 = (Node) endpoints.getFirst().getCopy();
			Node n2 = g.getVertexByName(endpoints.getSecond().getName());
			if (n2==null)
				n2 = (Node) endpoints.getSecond().getCopy();
			g.addEdge(l,n1,n2);
		}
		return g;
	}

	/** Check the topology of the graph
	 * it must have at least one Virtual Machine/Server
	 */
	public boolean hasCorrectTopology() {
		for (Node node : this.getVertices())
			if (node instanceof Server ||
					node instanceof VirtualMachine)
				return true;
		return false;
	}
	
}
