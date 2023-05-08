package ML;

public class EmbeddingActionSpaceDiscrete extends EmbeddingActionSpace{
   /**
     * Construct an actions space from an array of action strings
     * @param actions Array of action strings
     */
    public EmbeddingActionSpaceDiscrete(String... actions) {
        super(actions.length);
        this.actions = actions;
    }
}