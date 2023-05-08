package model;

import java.io.Serializable;

import org.apache.commons.collections15.Factory;

import model.components.Link;
import model.components.Node;
import simenv.SimulatorConstants;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;

/**
 * Network Class. Superclass of Request and Substrate.
 */
public class Network implements Serializable {
	protected String id;
	protected Graph<Node, Link> graph;
	// protected Layout<Node,Link> graphLayout;
	@SuppressWarnings("rawtypes")
	protected Factory nodeFactory;
	@SuppressWarnings("rawtypes")
	protected Factory linkFactory;
	protected String state;
	protected Hypergraph<Node, Link> hgraph;

	
	
	public Network() {
	}

	public Network(String id) {
		this.id = id;
		this.state = SimulatorConstants.STATUS_AVAILABLE;
		graph = new NetworkGraph();
		// graphLayout = new FRLayout2<Node, Link>(graph);
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Graph<Node, Link> getGraph() {
		return graph;
	}

	public void setGraph(Graph<Node, Link> graph) {
		this.graph = graph;
	}

	public void setHGraph(Hypergraph<Node, Link> hgraph) {
		this.hgraph = hgraph;
	}

	
	// public Layout<Node, Link> getGraphLayout() {
	// 	return graphLayout;
	// }

	// public void setGraphLayout(Layout<Node, Link> graphLayout) {
	// 	this.graphLayout = graphLayout;
	// }

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@SuppressWarnings("rawtypes")
	public Factory getNodeFactory() {
		return nodeFactory;
	}

	public void setNodeFactory(@SuppressWarnings("rawtypes") Factory nodeFactory) {
		this.nodeFactory = nodeFactory;
	}

	@SuppressWarnings("rawtypes")
	public Factory getLinkFactory() {
		return linkFactory;
	}

	@SuppressWarnings("rawtypes")
	public void setLinkFactory(Factory linkFactory) {
		this.linkFactory = linkFactory;
	}

}
