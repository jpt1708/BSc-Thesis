package tom.remote;

import model.Request;
import model.Substrate;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RemoteAgentCommunicator {

    private final String protocol;
    private final String host;
    private final int port;
    private final ZMQ.Socket socket;

    public RemoteAgentCommunicator() {
        this("tcp", "localhost", 5555);
    }

    public RemoteAgentCommunicator(String protocol, String host, int port) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;

        ZContext context = new ZContext();
        socket = context.createSocket(SocketType.PAIR);
    }

    public void start() {
        String uri = String.format("%s://%s:%d", protocol, host, port);
        if (!socket.bind(uri)) {
            throw new RuntimeException("could not bind to socket at :" + uri);
        }
    }

    public void stop() {
        sendReset();
//        socket.close();
    }

    public void sendPenalty(double penalty) {
        Messages.MonitorPenalty monitorPenalty = Messages.MonitorPenalty.newBuilder()
                .setPenalty(penalty)
                .build();

        socket.send("penalty", ZMQ.SNDMORE);
        socket.send(monitorPenalty.toByteArray(), 0);
    }

    public void sendReset() {
        socket.send("reset", 0);
    }

    public Messages.Action sendAllocationRequest(int step, Substrate substrate, List<NodeData> nodeData, Request request) throws IOException {
        List<Substrate> substrates = new ArrayList<>(1);
        substrates.add(substrate);

        return sendAllocationRequest(step, substrates, nodeData, request);
    }

    public Messages.Action sendAllocationRequest(int step, List<Substrate> substrates, List<NodeData> nodeData, Request request) throws IOException {
        List<Request> requests = new ArrayList<>(1);
        requests.add(request);

        return sendAllocationRequest(step, substrates, nodeData, requests);
    }

    public Messages.Action sendAllocationRequest(int step, List<Substrate> substrates, List<NodeData> nodeData, List<Request> requests) throws IOException {
        Messages.AllocationRequest allocationRequest = MessagesUtils.createAllocationRequest(step, substrates, nodeData, requests);
        socket.send("allocation_request", ZMQ.SNDMORE);
        socket.send(allocationRequest.toByteArray(), 0);

        return Messages.Action.parseFrom(socket.recv(0));
    }

}
