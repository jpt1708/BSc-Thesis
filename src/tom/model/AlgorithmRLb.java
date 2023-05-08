package tom.model;

import ML.QLearnBelief;
import model.Request;
import model.ResourceMappingNF;
import model.Substrate;
import model.components.Node;

import java.util.*;

public class AlgorithmRLb extends Algorithm {

    private static final int AVG_LIFETIME = 10000;
    private final QLearnBelief ql;

    public AlgorithmRLb(Substrate substrate) {
        super("RLb");
        this.ql = new QLearnBelief(substrate.getNumServers());
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
        this.ql.setCurrentPenalty(getMonitor().getPernalty());
        return this.ql.placeloads(request.getId(), subNodesCap, subNodesType, reqNodesCap, reqMap, nomCap,
                getSubNodesLife(), getSubNodesHostedReq(), AVG_LIFETIME, request.getEndDate() - this.getTs());
    }
}
