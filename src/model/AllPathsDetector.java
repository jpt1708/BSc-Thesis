package model;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;
import model.components.Link;
import model.components.Node;

public class AllPathsDetector<Node, Link extends Comparable> implements Serializable
{

private List<List<Link>> allPaths;
private List<List<Link>> allPathsFinal = new ArrayList<List<Link>>();;

public void addPaths(List<List<Link>> Paths){
	this.allPathsFinal.addAll(Paths);
}

public List<List<Link>>  getPaths(){
 return this.allPathsFinal;
}

public List<List<Link>> getAllPathsBetweenNodes(DirectedGraph<Node, Link> graph,
        Node startNode, Node endNode, int maxDepth)
{
    allPaths = new ArrayList<List<Link>>();

    List<Link> currentPath = new ArrayList<Link>();

    findAllPaths(startNode, startNode, endNode, currentPath, graph, maxDepth, 0);

    return allPaths;
}

public List<List<Link>> getAllUniqePathsBetweenNodes(DirectedGraph<Node, Link> graph,
        Node startNode, Node endNode, int maxDepth)
{
    allPaths = new ArrayList<List<Link>>();

    List<Link> currentPath = new ArrayList<Link>();

    findAllUniquePaths(startNode, startNode, endNode, currentPath, graph, maxDepth, 0);

    return allPaths;
}

private void findAllPaths(Node currentNode, Node startNode, Node endNode,
        List<Link> currentPath, DirectedGraph<Node, Link> graph,
        int maxDepth, int currentDepth)
{
    Collection<Link> outgoingEdges = graph.getOutEdges(currentNode);

    if (currentDepth < maxDepth)
    {
        for (Link outEdge : outgoingEdges)
        {
            Node outNode = graph.getDest(outEdge);
            //String outNode = outEdge.getSupertype();

            if (outNode.equals(startNode))
            {
                List<Link> cyclePath = new ArrayList<Link>(currentPath);
                cyclePath.add(outEdge);
               // System.out.println("Found cycle provoked by path " + cyclePath);
                continue;
            }

            List<Link> newPath = new ArrayList<Link>(currentPath);
            newPath.add(outEdge);

            if (outNode.equals(endNode))
            {
                allPaths.add(newPath);
                continue;
            }

            findAllPaths(outNode, startNode, endNode, newPath, graph, maxDepth, currentDepth + 1);
        }
    }
}

private void findAllUniquePaths(Node currentNode, Node startNode, Node endNode,
        List<Link> currentPath, DirectedGraph<Node, Link> graph,
        int maxDepth, int currentDepth)
{
    Collection<Link> outgoingEdges = graph.getOutEdges(currentNode);

    if (currentDepth < maxDepth)
    {
        for (Link outEdge : outgoingEdges)
        {
            Node outNode = graph.getDest(outEdge);
            //String outNode = outEdge.getSupertype();

            if (outNode.equals(startNode))
            {
                List<Link> cyclePath = new ArrayList<Link>(currentPath);
                cyclePath.add(outEdge);
                //System.out.println("Found cycle provoked by path " + cyclePath);
                continue;
            }

            List<Link> newPath = new ArrayList<Link>(currentPath);
            newPath.add(outEdge);

            if (outNode.equals(endNode))
            {
                //Check for unique-ness before adding.
                boolean unique = true;
                //Check each existing path.
                for (int pathItr = 0; pathItr < allPaths.size(); pathItr++)
                {
                    //Compare the edges of the paths.
                    for (int edgeItr = 0; edgeItr < allPaths.get(pathItr).size(); edgeItr++)
                    {
                        //If the edges are the same, this path is not unique.
                        if (allPaths.get(pathItr).get(edgeItr).compareTo(newPath.get(edgeItr)) == 0)
                        {
                            unique = false;
                        }
                    }

                }
                //After we check all the edges, in all the paths,
                //if it is still unique, we add it.
                if (unique)
                {
                    allPaths.add(newPath);
                }
                continue;
            }
            findAllUniquePaths(outNode, startNode, endNode, newPath, graph, maxDepth, currentDepth + 1);
        }
    }
}
}