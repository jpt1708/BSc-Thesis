package tom.remote;

public class NodeData {

    private final double life;
    private final double reqHosted;

    public NodeData(double life, double reqHosted) {
        this.life = life;
        this.reqHosted = reqHosted;
    }

    public double getLife() {
        return life;
    }

    public double getReqHosted() {
        return reqHosted;
    }
}
