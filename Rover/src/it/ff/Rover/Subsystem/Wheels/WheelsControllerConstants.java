package it.ff.Rover.Subsystem.Wheels;

import com.pi4j.gpio.extension.pca.PCA9685Pin;
import com.pi4j.io.gpio.Pin;

public class WheelsControllerConstants
{
	// Wheels steering
	public static final Pin STEER_PIN_REAR_LEFT = PCA9685Pin.PWM_06;
	public static final Pin STEER_PIN_FRONT_LEFT = PCA9685Pin.PWM_07;
	public static final Pin STEER_PIN_FRONT_RIGHT = PCA9685Pin.PWM_08;
	public static final Pin STEER_PIN_REAR_RIGHT = PCA9685Pin.PWM_09;

	// Wheels steering positions
	public static int STEER_CENTER_RL = 0;
	public static int STEER_CENTER_FL = 0;
	public static int STEER_CENTER_FR = 0;
	public static int STEER_CENTER_RR = 0;

	public static int STEER_DELTA = 0;

	// Wheels direction
	public static final int WHEELS_DIRECTION_STOP = 0;
	public static final int WHEELS_DIRECTION_FORWARD = 1;
	public static final int WHEELS_DIRECTION_REVERSE = 2;
	public static final int WHEELS_DIRECTION_CLOCKWISE = 3;
	public static final int WHEELS_DIRECTION_COUNTERCLOCKWISE = 4;

	// Steering direction
	public static final int WHEELS_STEERING_NONE = 0;
	public static final int WHEELS_STEERING_LEFT = 1;
	public static final int WHEELS_STEERING_RIGHT = 2;
	public static final int WHEELS_STEERING_CLOCKWISE = 3;
	public static final int WHEELS_STEERING_COUNTERCLOCKWISE = 4;
}