package tom.remote;

import model.Request;
import model.Substrate;
import model.components.Link;
import model.components.Node;
import model.components.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MessagesUtils {

    /**
     * Creates a new parameter.
     *
     * @param key   The parameter key.
     * @param value The parameter value.
     * @return Protobuf parameter.
     */
    private static Messages.Parameter createParameter(String key, Object value) {
        return Messages.Parameter.newBuilder()
                .setKey(key)
                .setValue(value.toString())
                .build();
    }

    /**
     * Extracts all the parameters from a node and returns it in a Protobuf compatible message.
     *
     * @param node The node.
     * @return List of parameters.
     */
    private static List<Messages.Parameter> createNodeParameters(Node node) {
        List<Messages.Parameter> entries = new ArrayList<>();

        if (node instanceof Server) {
            Server server = (Server) node;
            entries.add(createParameter("available_cpu", server.getAvailableCpu()));
            entries.add(createParameter("cpu", server.getCpu()));
            entries.add(createParameter("memory", server.getMemory()));
            entries.add(createParameter("disk_space", server.getDiskSpace()));
            entries.add(createParameter("nom_cpu", server.getNominalCpu()));
        } else {
            entries.add(createParameter("available_cpu", node.getAvailableCpu()));
            entries.add(createParameter("cpu", node.getCpu()));
        }

        return entries;
    }

    private static List<Messages.Parameter> createLinkParameters(Link link) {
        List<Messages.Parameter> entries = new ArrayList<>();

        entries.add(createParameter("bandwidth", link.getBandwidth()));

        return entries;
    }

    /**
     * Converts a {@link Substrate} to a Protobuf compatible message.
     *
     * @param substrate The {@link Substrate} to be converted.
     * @return Protobuf compatible message.
     */
    private static Messages.Substrate convertSubstrate(Substrate substrate, List<NodeData> nodeData) {
        List<Messages.Node> messageNodes = new ArrayList<>(substrate.getGraph().getVertexCount());
        for (Node node : substrate.getGraph().getVertices()) {
            NodeData data = nodeData.get(node.getId());

            List<Messages.Parameter> parameters = createNodeParameters(node);
            parameters.add(createParameter("life", data.getLife()));
            parameters.add(createParameter("req_hosted", data.getReqHosted()));

            Messages.Node messageNode = Messages.Node.newBuilder()
                    .setId(node.getId())
                    .addAllParameters(parameters)
                    .build();

            messageNodes.add(messageNode);
        }

        List<Messages.Link> messageLinks = new ArrayList<>(substrate.getGraph().getEdgeCount());
        for (Link link : substrate.getGraph().getEdges()) {
            Messages.Link messageLink = Messages.Link.newBuilder()
                    .setId(link.getId())
                    .addAllParameters(createLinkParameters(link))
                    .setSourceNodeId(substrate.getGraph().getSource(link).getId())
                    .setDestinationNodeId(substrate.getGraph().getDest(link).getId())
                    .build();

            messageLinks.add(messageLink);
        }

        return Messages.Substrate.newBuilder()
                .setId(substrate.getId())
                .addAllNodes(messageNodes)
                .addAllLinks(messageLinks)
                .build();
    }

    /**
     * Converts a {@link Request} to a Protobuf compatible message.
     *
     * @param request The {@link Request} to be converted.
     * @return Protobuf compatible message.
     */
    private static Messages.ServiceRequest convertRequest(Request request) {
        List<Messages.Node> messageNodes = new ArrayList<>(request.getGraph().getVertexCount());
        for (Node node : request.getGraph().getVertices()) {
            Messages.Node messageNode = Messages.Node.newBuilder()
                    .setId(node.getId())
                    .addAllParameters(createNodeParameters(node))
                    .build();

            messageNodes.add(messageNode);
        }

        List<Messages.Link> messageLinks = new ArrayList<>(request.getGraph().getEdgeCount());
        for (Link link : request.getGraph().getEdges()) {
            Messages.Link messageLink = Messages.Link.newBuilder()
                    .setId(link.getId())
                    .addAllParameters(createLinkParameters(link))
                    .setSourceNodeId(request.getGraph().getSource(link).getId())
                    .setDestinationNodeId(request.getGraph().getDest(link).getId())
                    .build();

            messageLinks.add(messageLink);
        }

        return Messages.ServiceRequest.newBuilder()
                .setId(request.getId())
                .addAllNodes(messageNodes)
                .addAllLinks(messageLinks)
                .build();
    }

    /**
     * Convert the simulation state into a Protobuf compatible message.
     *
     * @param step       The simulation time step.
     * @param substrates The substrate networks in the simulation.
     * @param requests   The new service requests of the simulation step.
     * @return Protobuf compatible message, representing the complete state.
     */
    public static Messages.AllocationRequest createAllocationRequest(int step, List<Substrate> substrates,
                                                                     List<NodeData> nodeData,
                                                                     List<Request> requests) {
        List<Messages.Substrate> messageSubstrates = substrates.stream()
                .map(substrate -> MessagesUtils.convertSubstrate(substrate, nodeData))
                .collect(Collectors.toList());
        List<Messages.ServiceRequest> messageRequests = requests.stream()
                .map(MessagesUtils::convertRequest)
                .collect(Collectors.toList());

        return Messages.AllocationRequest.newBuilder()
                .setStep(step)
                .addAllSubstrates(messageSubstrates)
                .addAllRequests(messageRequests)
                .build();
    }

}
