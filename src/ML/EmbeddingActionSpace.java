package ML;
import org.deeplearning4j.rl4j.space.DiscreteSpace;

import java.util.Random;

public abstract class EmbeddingActionSpace extends DiscreteSpace {
    /**
     * Array of action strings that will be sent to embedding
     */
    protected String[] actions;
    protected  Random rd;
    /**
     * Protected constructor
     *
     * @param size number of discrete actions in this space
     */
    protected EmbeddingActionSpace(int size) {
        super(size);
    }

    @Override
    public Object encode(Integer action) {
        return actions[action];
    }

    @Override
    public Integer noOp() {
        return -1;
    }

    /**
     * Sets the seed used for random generation of actions
     *
     * @param seed random number generator seed
     */
    public void setRandomSeed(long seed) {
        rd.setSeed(seed);
    }
}


