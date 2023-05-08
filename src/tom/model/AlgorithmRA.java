package tom.model;

import model.Request;
import model.ResourceMappingNF;
import model.components.Node;
import tom.remote.RemoteAgent;

import java.util.ArrayList;

public class AlgorithmRA extends Algorithm {

    private static final int AVG_LIFETIME = 10000;
    private final RemoteAgent ra;

    public AlgorithmRA() {
        super("RA");
        this.ra = new RemoteAgent();
    }

    @Override
    protected double[][] allocateRequest(Request request, ResourceMappingNF reqMap) {
        ArrayList<Node> subNodesList = getNodes(getSubstrates().get(0).getGraph());

        double nomCap = 0;
        double[] subNodesCap = new double[subNodesList.size()];
        String[] subNodesType = new String[subNodesCap.length];

        for (Node node : subNodesList) {
            subNodesCap[node.getId()] = node.getAvailableCpu();
            subNodesType[node.getId()] = node.getType();

            if (node.getType().equalsIgnoreCase("Server")) {
                nomCap = node.getNominalCpu();
            }
        }

        double[] reqNodesCap = new double[request.getGraph().getVertexCount()];
        for (Node node : getNodes(request.getGraph())) {
            reqNodesCap[node.getId()] = node.getAvailableCpu();
        }

        getResLifetime(subNodesList);
        this.ra.setCurrentPenalty(getMonitor().getPernalty());
        return this.ra.placeloads(request.getId(), subNodesCap, subNodesType, reqNodesCap, reqMap, nomCap,
                getSubNodesLife(), getSubNodesHostedReq(), AVG_LIFETIME, request.getEndDate() - this.getTs(),
                getSubstrates(), request);
    }

    @Override
    public void clean() {
        this.ra.stop();
    }
}
