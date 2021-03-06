import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Random;

/* TODO: 
 * -Define state and reward functions (StateAndReward.java) suitable for your problem 
 * -Define actions
 * -Implement missing parts of Q-learning
 * -Tune state and reward function, and parameters below if the result is not satisfactory */

public class QLearningController extends Controller {

	/* These are the agents senses (inputs) */
	DoubleFeature x; /* Positions */
	DoubleFeature y;
	DoubleFeature vx; /* Velocities */
	DoubleFeature vy;
	DoubleFeature angle; /* Angle */

	/* These are the agents actuators (outputs)*/
	RocketEngine leftEngine;
	RocketEngine middleEngine;
	RocketEngine rightEngine;

	final static int NUM_ACTIONS = 7; /* The takeAction function must be changed if this is modified */
	
	/* Keep track of the previous state and action */
	String previous_state = null;
	double previous_vx = 0;
	double previous_vy = 0;
	double previous_angle = 0;
	int previous_action = 0; 
	
	/* The tables used by Q-learning */
	Hashtable<String, Double> Qtable = new Hashtable<String, Double>(); /* Contains the Q-values - the state-action utilities */
	Hashtable<String, Integer> Ntable = new Hashtable<String, Integer>(); /* Keeps track of how many times each state-action combination has been used */

	/* PARAMETERS OF THE LEARNING ALGORITHM - THESE MAY BE TUNED BUT THE DEFAULT VALUES OFTEN WORK REASONABLY WELL  */
	static final double GAMMA_DISCOUNT_FACTOR = 0.95; /* Must be < 1, small values make it very greedy */
    //static final double GAMMA_DISCOUNT_FACTOR = 0.5; /* Must be < 1, small values make it very greedy */
	static final double LEARNING_RATE_CONSTANT = 10; /* See alpha(), lower values are good for quick results in large and deterministic state spaces */
	double explore_chance = 0.5; /* The exploration chance during the exploration phase */
	final static int REPEAT_ACTION_MAX = 30; /* Repeat selected action at most this many times trying reach a new state, without a max it could loop forever if the action cannot lead to a new state */

	/* Some internal counters */
	int iteration = 0; /* Keeps track of how many iterations the agent has run */
	int action_counter = 0; /* Keeps track of how many times we have repeated the current action */
	int print_counter = 0; /* Makes printouts less spammy */ 

	/* These are just internal helper variables, you can ignore these */
	boolean paused = false;
	boolean explore = true; /* Will always do exploration by default */

	DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US); 
	public SpringObject object;
	ComposedSpringObject cso;
	long lastPressedExplore = 0;

	public void init() {
		cso = (ComposedSpringObject) object;
		x = (DoubleFeature) cso.getObjectById("x");
		y = (DoubleFeature) cso.getObjectById("y");
		vx = (DoubleFeature) cso.getObjectById("vx");
		vy = (DoubleFeature) cso.getObjectById("vy");
		angle = (DoubleFeature) cso.getObjectById("angle");
		
		previous_vy = vy.getValue();
		previous_vx = vx.getValue();
		previous_angle = angle.getValue();

		leftEngine = (RocketEngine) cso.getObjectById("rocket_engine_left");
		rightEngine = (RocketEngine) cso.getObjectById("rocket_engine_right");
		middleEngine = (RocketEngine) cso.getObjectById("rocket_engine_middle");
	}


    TestPairs pairs;
	public void writeToFile(String filename, String content) {
		try {
			FileOutputStream fos = new FileOutputStream(filename, true);
			fos.write(content.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/* Turn off all rockets */
	void resetRockets() {
		leftEngine.setBursting(false);
		rightEngine.setBursting(false);
		middleEngine.setBursting(false);
	}

	String get_key(String state, int action){
	    return state+'_'+action_str(action);
    }

	String action_str(int action){
	    String act = "";
        switch (action){
            case 0:
                act = "N";
                break;
            case 1:
                act = "L";
                break;
            case 2:
                act = "R";
                break;
            case 3:
                act = "M";
                break;
            case 4:
                act = "LM";
                break;
            case 5:
                act = "RM";
                break;
            case 6:
                act = "ALL";
                break;
        }
        return act;
    }

	/* Performs the chosen action */
	void performAction(int action) {
	    /*
	        0 --> NO_ACT
	        1 --> LEFT
	        2 --> RIGHT
	        3 --> MIDDLE
	        4 --> LEFT & MIDDLE
	        5 --> RIGHT & MIDDLE
	        6 --> ALL
	     */
	    switch (action){
            case 0:
                resetRockets();
                break;
            case 1:
                // left
                leftEngine.setBursting(true);
                rightEngine.setBursting(false);
                middleEngine.setBursting(false);
                break;
            case 2:
                // right
                leftEngine.setBursting(false);
                rightEngine.setBursting(true);
                middleEngine.setBursting(false);
                break;
            case 3:
                // middle
                leftEngine.setBursting(false);
                rightEngine.setBursting(false);
                middleEngine.setBursting(true);
                break;
            case 4:
                // left & middle
                leftEngine.setBursting(true);
                rightEngine.setBursting(false);
                middleEngine.setBursting(true);
                break;
            case 5:
                // right & middle
                leftEngine.setBursting(false);
                rightEngine.setBursting(true);
                middleEngine.setBursting(true);
                break;
            case 6:
                // all
                leftEngine.setBursting(true);
                rightEngine.setBursting(true);
                middleEngine.setBursting(true);
                break;
        }
	}

	/* Main decision loop. Called every iteration by the simulator */
	public void tick(int currentTime) {
		iteration++;

		// TODO : TO REMOVE + LES ATTRIBUTS
        if (iteration == 10000000) {
            String st;
            // pour chaque état de l'angle
            for (int i = 0; i < 4; i++) {
                pairs = new TestPairs();

                String angle;
                if (i == 0){
                    angle = "front";
                } else if (i == 1){
                    angle = "right";
                }  else if (i == 2){
                    angle = "left";
                } else {
                    angle = "reverse";
                }

                // pour chaque état du hover
                for (int k = 0; k < 4; k++){

                    String hover;
                    if (k == 0){
                        hover = "unstable";
                    } else if (k == 1){
                        hover = "perfect";
                    }  else if (k == 2){
                        hover = "hover";
                    } else {
                        hover = "reach_hover";
                    }

                    String state = angle + '_' + hover;
                    ArrayList<String> list = new ArrayList<>();
                    // pour chaque actions
                    for (int j = 0; j < 7; j++) {
                        String state_action;
                        state_action = get_key(state, j);
                        if (Qtable.containsKey(state_action)){
                            list.add(Qtable.get(state_action).toString());
                        } else {
                            list.add("0");
                        }
                    }
                    try {
                        String content = state + " = " + list.toString() + '\n';
                        writeToFile("output_qtables.m", content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
            System.exit(0);
        }

		if (!paused) {
			String new_state = StateAndReward.getStateAngle(angle.getValue(), vx.getValue(), vy.getValue());
			new_state += '_'+StateAndReward.getStateHover(angle.getValue(), vx.getValue(), vy.getValue());

			/* Repeat the chosen action for a while, hoping to reach a new state. This is a trick to speed up learning on this problem. */
			action_counter++;
			if (new_state.equals(previous_state) && action_counter < REPEAT_ACTION_MAX) {
				return;
			}
			double angle_reward = StateAndReward.getRewardAngle(previous_angle, previous_vx, previous_vy);
			double hover_reward = StateAndReward.getRewardHover(previous_angle, previous_vx, previous_vy);
			double reward = 0.2*angle_reward + 0.8*hover_reward;
			// promote reward if we have a good angle and hover state
			if(angle_reward == 1 && hover_reward >= 5){
			    reward = reward * 2;
            }
            double previous_reward = reward;

			action_counter = 0;

			/* The agent is in a new state, do learning and action selection */
			if (previous_state != null) {
				/* Create state-action key */
				String prev_stateaction = get_key(previous_state, previous_action);

				/* Increment state-action counter */
				if (Ntable.get(prev_stateaction) == null) {
					Ntable.put(prev_stateaction, 0);
				}
				Ntable.put(prev_stateaction, Ntable.get(prev_stateaction) + 1);

				/* Update Q value */
				if (Qtable.containsKey(prev_stateaction)) {
                    double curr = Qtable.get(prev_stateaction);
                    double value = curr + alpha(Ntable.get(prev_stateaction))*(previous_reward + (GAMMA_DISCOUNT_FACTOR*getMaxActionQValue(new_state)) - curr);
                    Qtable.put(prev_stateaction, value);
				} else {
                    Qtable.put(prev_stateaction, 0.0);
                }

				//System.out.println("Q-TABLE : "+ Qtable);

				int action = selectAction(new_state); /* Make sure you understand how it selects an action */
				performAction(action);
				
				/* Only print every 10th line to reduce spam */
				print_counter++;
				if (print_counter % 50 == 0) {
					System.out.println("ITERATION: " + iteration + " SENSORS: a=" + df.format(angle.getValue()) + " vx=" + df.format(vx.getValue()) + 
							" vy=" + df.format(vy.getValue()) + " P_STATE: " + previous_state + " P_ACTION: " + action_str(previous_action) +
							" P_REWARD: " + df.format(previous_reward) + " P_QVAL: " + df.format(Qtable.get(prev_stateaction)) + " Tested: "
							+ Ntable.get(prev_stateaction) + " times.");
				}
				
				previous_vy = vy.getValue();
				previous_vx = vx.getValue();
				previous_angle = angle.getValue();
				previous_action = action;
			}
			previous_state = new_state;
		}

	}

	/* Computes the learning rate parameter alpha based on the number of times the state-action combination has been tested */
	public double alpha(int num_tested) {
		/* Lower learning rate constants means that alpha will become small faster and therefore make the agent behavior converge to 
		 * to a solution faster, but if the state space is not properly explored at that point the resulting behavior may be poor.
		 * If your state-space is really huge you may need to increase it. */
		double alpha = (LEARNING_RATE_CONSTANT/(LEARNING_RATE_CONSTANT + num_tested));
		return alpha;
	}
	
	/* Finds the highest Qvalue of any action in the given state */
	public double getMaxActionQValue(String state) {
		double maxQval = Double.NEGATIVE_INFINITY;
		
		for (int action = 0; action < NUM_ACTIONS; action++) {
			Double Qval = Qtable.get(get_key(state, action));
			if (Qval != null && Qval > maxQval) {
				maxQval = Qval;
			} 
		}

		if (maxQval == Double.NEGATIVE_INFINITY) {
			/* Assign 0 as that corresponds to initializing the Qtable to 0. */
			maxQval = 0;
		}
		return maxQval;
	}
	
	/* Selects an action in a state based on the registered Q-values and the exploration chance */
	public int selectAction(String state) {
		Random rand = new Random();

		int action = 0;
		/* May do exploratory move if in exploration mode */
		if (explore && Math.abs(rand.nextDouble()) < explore_chance) {
			/* Taking random exploration action! */
			action = Math.abs(rand. nextInt()) % NUM_ACTIONS;
			return action;
		}

        //System.out.println("Q-TABLE : "+Qtable);
        //System.out.println("Finding action : " + state);
		/* Find action with highest Q-val (utility) in given state */
		double maxQval = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < NUM_ACTIONS; i++) {
		    String test_pair = get_key(state, i); /* Generate a state-action pair for all actions */
			double Qval = 0;
			if (Qtable.containsKey(test_pair)) {
				Qval = Qtable.get(test_pair);
			}
			if (Qval > maxQval) {
				maxQval = Qval;
				action = i;
			}
		}
        //System.out.println("maxQval "+ maxQval);
        //System.out.println("Action "+ action_str(action));
		return action;
	}

	
	
	/* The 'E' key will toggle the agents exploration mode. Turn this off to test its behavior */
	public void toggleExplore() {
		/* Make sure we don't toggle it multiple times */
		if (System.currentTimeMillis() - lastPressedExplore < 1000) {
			return;
		}
		if (explore) {
			System.out.println("Turning OFF exploration!");
			explore = false;
		} else {
			System.out.println("Turning ON exploration!");
			explore = true;
		}
		lastPressedExplore = System.currentTimeMillis(); 
	}

	/* Keys 1 and 2 can be customized for whatever purpose if you want to */
	public void toggleCustom1() {
        System.out.println("Q-TABLE : "+ Qtable);
	}

	/* Keys 1 and 2 can be customized for whatever purpose if you want to */
	public void toggleCustom2() {
		System.out.println("Custom key 2 pressed!");
	}
	
	public void pause() {
		paused = true;
		resetRockets();
	}

	public void run() {
		paused = false;
	}
}
