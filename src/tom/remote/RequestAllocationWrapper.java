package tom.remote;

import model.Request;
import model.Substrate;
import model.components.Link;
import model.components.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class RequestAllocationWrapper {

    private final Messages.RequestAllocation requestAllocation;
    private final Request request;

    public RequestAllocationWrapper(Request request, Messages.RequestAllocation requestAllocation) {
        this.requestAllocation = requestAllocation;
        this.request = request;
    }

    public LinkedHashMap<Node, Node> getNodeMapping(Substrate substrate) {
        LinkedHashMap<Node, Node> nodeMap = new LinkedHashMap<>();
        List<Node> requestNodes = new ArrayList<>(request.getGraph().getVertices());
        List<Node> substrateNodes = new ArrayList<>(substrate.getGraph().getVertices());

        for (Messages.NodeAllocation nodeAllocation : requestAllocation.getNodeAllocationsList()) {
            Node requestNode = requestNodes.get(nodeAllocation.getSourceId());
            Node substrateNode = substrateNodes.get(nodeAllocation.getDestinationId());

            nodeMap.put(requestNode, substrateNode);
        }

        return nodeMap;
    }

    public HashMap<Link, ArrayList<LinkedHashMap<Link, Double>>> getLinkMapping(Substrate substrate) {
        HashMap<Link, ArrayList<LinkedHashMap<Link, Double>>> linkMap = new HashMap<>();
        List<Link> requestLinks = new ArrayList<>(request.getGraph().getEdges());
        List<Link> substrateLinks = new ArrayList<>(substrate.getGraph().getEdges());

        for (Messages.RequestLinkAllocation requestLinkAllocation : requestAllocation.getLinkAllocationsList()) {
            Link requestLink = requestLinks.get(requestLinkAllocation.getId());
            ArrayList<LinkedHashMap<Link, Double>> allocatedSubstrateLinks = new ArrayList<>();

            for (Messages.SubstrateLinkAllocation substrateLinkAllocation : requestLinkAllocation.getAllocationsList()) {
                Link substrateLink = substrateLinks.get(substrateLinkAllocation.getId());
                LinkedHashMap<Link, Double> mappedSubstrateLink = new LinkedHashMap<>();
                mappedSubstrateLink.put(substrateLink, substrateLink.getBandwidth());

                allocatedSubstrateLinks.add(mappedSubstrateLink);
            }

            linkMap.put(requestLink, allocatedSubstrateLinks);
        }

        return linkMap;
    }
}
