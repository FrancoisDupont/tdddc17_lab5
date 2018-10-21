import java.text.DecimalFormat;

public class StateAndReward {

    static DecimalFormat df = new DecimalFormat("0.0");

	/* State discretization function for the angle controller */
	public static String getStateAngle(double angle, double vx, double vy) {

	    String state;
	    // custom discretization
        if (angle >= -0.2 && angle <= 0.2){
            state = "front";
        } else if (angle > 0.2 && angle < 2.5){
            state = "right";
        }  else if (angle < -0.2 && angle > -2.5){
            state = "left";
        } else {
            state = "reverse";
        }

		return state;
	}

	/* Reward function for the angle controller */
	public static double getRewardAngle(double angle, double vx, double vy) {

		double reward = 0;

        String state = getStateAngle(angle, vx, vy);
        if (state.equals("front")){
            reward = 1;
        }

		return reward;
	}

	/* State discretization function for the full hover controller */
	public static String getStateHover(double angle, double vx, double vy) {

	    String state = "unstable";
	    if(vx < 0.5 && vx > -0.5){
            if (vy < 0.2 && vy > 0 && vx < 0.2 && vx > -0.2){
                state = "perfect";
            } else if (vy < 0.5 && vy >= 0.2){
	            state = "hover";
            } else if (vy >= 0.5 && vy < 5){
                state = "reach_hover";
            }
        }

		return state;
	}

	/* Reward function for the full hover controller */
	public static double getRewardHover(double angle, double vx, double vy) {

		double reward = 0;

		String state = getStateHover(angle, vx, vy);
        if (state.equals("perfect")){
            reward = 20;
        } else if (state.equals("hover")){
		    reward = 5;
        } else if (state.equals("reach_hover")){
            reward = 1;
        }

		return reward;
	}

    // ///////////////////////////////////////////////////////////
	// discretize() performs a uniform discretization of the
	// value parameter.
	// It returns an integer between 0 and nrValues-1.
	// The min and max parameters are used to specify the interval
	// for the discretization.
	// If the value is lower than min, 0 is returned
	// If the value is higher than min, nrValues-1 is returned
	// otherwise a value between 1 and nrValues-2 is returned.
	//
	// Use discretize2() if you want a discretization method that does
	// not handle values lower than min and higher than max.
	// ///////////////////////////////////////////////////////////
	public static int discretize(double value, int nrValues, double min,
			double max) {
		if (nrValues < 2) {
			return 0;
		}

		double diff = max - min;

		if (value < min) {
			return 0;
		}
		if (value > max) {
			return nrValues - 1;
		}

		double tempValue = value - min;
		double ratio = tempValue / diff;

		return (int) (ratio * (nrValues - 2)) + 1;
	}

	// ///////////////////////////////////////////////////////////
	// discretize2() performs a uniform discretization of the
	// value parameter.
	// It returns an integer between 0 and nrValues-1.
	// The min and max parameters are used to specify the interval
	// for the discretization.
	// If the value is lower than min, 0 is returned
	// If the value is higher than min, nrValues-1 is returned
	// otherwise a value between 0 and nrValues-1 is returned.
	// ///////////////////////////////////////////////////////////
	public static int discretize2(double value, int nrValues, double min,
			double max) {
		double diff = max - min;

		if (value < min) {
			return 0;
		}
		if (value > max) {
			return nrValues - 1;
		}

		double tempValue = value - min;
		double ratio = tempValue / diff;

		return (int) (ratio * nrValues);
	}

}
