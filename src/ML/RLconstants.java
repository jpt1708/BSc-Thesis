package ML;

public class RLconstants {
	
	private RLconstants() {

	}
	
	public static final double REWARD_ACCEPT = 1;
	public static final double REWARD_OVERLOAD = -5;
	public static final double REWARD_PENALTY = 5;
	public static final double PENALTY_DENY = -1;
	// gamma discount factor: how to deal with future rewards [0,1]
	public static final double GAMMA = 0.7;
	// learning rate
	public static final double ALPHA = 0.005;
	//exploration factor
	public static final double SIGMA = 0.01;
	public static final double MIN_VALUE = -1e5;
	// segments the state-space such that Q(s,a) can be specified by a finite number of values
	//public static final double SUM_RCC_THR =0.3;


/*	public static final double MIN_VALUE = -1e5;
	// number of states
	public static final int NUM_OF_STATES = 6;
	// reward in non-terminal states (used to initialize R[][])
	public static final double STANDARD_REWARD = -0.1;
	public static final double EXIT_REWARD = 100;
	// gamma discount factor: how to deal with future rewards [0,1]
	// number of iterations
	public static final int NUM_OF_EPISODES = 100000;*/
    


}
