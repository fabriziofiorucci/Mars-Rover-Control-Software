package it.ff.Rover.Subsystem.Arm;

import com.pi4j.gpio.extension.pca.PCA9685Pin;
import com.pi4j.io.gpio.Pin;

public class ArmControllerConstants
{
	// Arm servomotors
	public static final Pin ARM_PIN_BASE = PCA9685Pin.PWM_02;
	public static final Pin ARM_PIN_SHOULDER = PCA9685Pin.PWM_03;
	public static final Pin ARM_PIN_ELBOW = PCA9685Pin.PWM_04;
	public static final Pin ARM_PIN_HAND = PCA9685Pin.PWM_05;
	public static final Pin ARM_PIN_WRIST = PCA9685Pin.PWM_06;
	public static final Pin ARM_PIN_CLAMP = PCA9685Pin.PWM_07;

	// Arm positions
	public static int ARM_BASE_MIN = 0;
	public static int ARM_BASE_MID = 0;
	public static int ARM_BASE_MAX = 0;

	public static int ARM_SHOULDER_MIN = 0;
	public static int ARM_SHOULDER_MID = 0;
	public static int ARM_SHOULDER_MAX = 0;

	public static int ARM_ELBOW_MIN = 0;
	public static int ARM_ELBOW_MID = 0;
	public static int ARM_ELBOW_MAX = 0;

	public static int ARM_HAND_MIN = 0;
	public static int ARM_HAND_MID = 0;
	public static int ARM_HAND_MAX = 0;

	public static int ARM_WRIST_MIN = 0;
	public static int ARM_WRIST_MID = 0;
	public static int ARM_WRIST_MAX = 0;

	public static int ARM_CLAMP_MIN = 0;
	public static int ARM_CLAMP_MAX = 0;
}