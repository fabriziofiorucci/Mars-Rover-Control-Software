package it.ff.Rover.Subsystem.Arm;

import java.io.IOException;
import java.math.BigDecimal;

import com.pi4j.gpio.extension.pca.PCA9685GpioProvider;
import com.pi4j.gpio.extension.pca.PCA9685Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.Pin;

import it.ff.Rover.Configuration;
import it.ff.Rover.Rover;
import it.ff.Rover.Hardware.IIC.IIC;

public class ArmController
{
	private PCA9685GpioProvider pca9685Controller = null;

	private GpioPinPwmOutput[] pwmOutputs = null;

	private int[] armCurrentPWM;

	public ArmController(IIC i2cbus, int pca9685_i2c_address) throws IOException
	{
		// This would theoretically lead into a resolution of 5 microseconds per
		// step:
		// 4096 Steps (12 Bit)
		// T = 4096 * 0.000005s = 0.02048s
		// f = 1 / T = 48.828125
		BigDecimal frequency = new BigDecimal("60.00");

		// Correction factor: actualFreq / targetFreq
		// e.g. measured actual frequency is: 60.00 Hz
		// Calculate correction factor: 60.00 / 48.828 = 1.2288
		// --> To measure actual frequency set frequency without correction
		// factor(or set to 1)
		BigDecimal frequencyCorrectionFactor = new BigDecimal("1.2288");

		// Create custom PCA9685 GPIO provider
		pca9685Controller = new PCA9685GpioProvider(i2cbus.getI2c(),
				pca9685_i2c_address, frequency, frequencyCorrectionFactor);

		pwmOutputs = initPWMchannels(pca9685Controller);

		// Reset outputs
		pca9685Controller.reset();

		init();
		setArmAtRest();
	}

	public void init()
	{
		// Initializes variables from config file
		ArmControllerConstants.ARM_BASE_MIN = Integer
				.parseInt(Configuration.get("arm_base_min"));
		ArmControllerConstants.ARM_BASE_MID = Integer
				.parseInt(Configuration.get("arm_base_mid"));
		ArmControllerConstants.ARM_BASE_MAX = Integer
				.parseInt(Configuration.get("arm_base_max"));

		ArmControllerConstants.ARM_SHOULDER_MIN = Integer
				.parseInt(Configuration.get("arm_shoulder_min"));
		ArmControllerConstants.ARM_SHOULDER_MID = Integer
				.parseInt(Configuration.get("arm_shoulder_mid"));
		ArmControllerConstants.ARM_SHOULDER_MAX = Integer
				.parseInt(Configuration.get("arm_shoulder_max"));

		ArmControllerConstants.ARM_ELBOW_MIN = Integer
				.parseInt(Configuration.get("arm_elbow_min"));
		ArmControllerConstants.ARM_ELBOW_MID = Integer
				.parseInt(Configuration.get("arm_elbow_mid"));
		ArmControllerConstants.ARM_ELBOW_MAX = Integer
				.parseInt(Configuration.get("arm_elbow_max"));

		ArmControllerConstants.ARM_HAND_MIN = Integer
				.parseInt(Configuration.get("arm_hand_min"));
		ArmControllerConstants.ARM_HAND_MID = Integer
				.parseInt(Configuration.get("arm_hand_mid"));
		ArmControllerConstants.ARM_HAND_MAX = Integer
				.parseInt(Configuration.get("arm_hand_max"));

		ArmControllerConstants.ARM_WRIST_MIN = Integer
				.parseInt(Configuration.get("arm_wrist_min"));
		ArmControllerConstants.ARM_WRIST_MID = Integer
				.parseInt(Configuration.get("arm_wrist_mid"));
		ArmControllerConstants.ARM_WRIST_MAX = Integer
				.parseInt(Configuration.get("arm_wrist_max"));

		ArmControllerConstants.ARM_CLAMP_MIN = Integer
				.parseInt(Configuration.get("arm_claw_min"));
		ArmControllerConstants.ARM_CLAMP_MAX = Integer
				.parseInt(Configuration.get("arm_claw_max"));

		// Set all pwm off
		setAllPWMOff();

		armCurrentPWM = new int[16];

		// Set arm to idle
		setArmAtRest();

		dumpCurrentPWM();
	}

	public void stop()
	{
		setAllPWMOff();
	}

	public void setArmAtRest()
	{
		setPosition(ArmControllerConstants.ARM_PIN_BASE,
				ArmControllerConstants.ARM_BASE_MIN);
		setPosition(ArmControllerConstants.ARM_PIN_SHOULDER,
				ArmControllerConstants.ARM_SHOULDER_MID);
		setPosition(ArmControllerConstants.ARM_PIN_ELBOW,
				ArmControllerConstants.ARM_ELBOW_MAX);
		setPosition(ArmControllerConstants.ARM_PIN_HAND,
				ArmControllerConstants.ARM_HAND_MAX);
		setPosition(ArmControllerConstants.ARM_PIN_WRIST,
				ArmControllerConstants.ARM_WRIST_MID);
		setPosition(ArmControllerConstants.ARM_PIN_HAND,
				ArmControllerConstants.ARM_CLAMP_MIN);
	}

	/**
	 * Returns the percentage amount of PWM value in the range between
	 * currentValue and targetValue
	 * 
	 * @param currentValue
	 *            the current PWM value
	 * @param targetValue
	 *            the target PWM value
	 * @param percentage
	 *            the requested PWM value percentage
	 * @return
	 */
	private int getPercentagePWMvalue(int currentValue, int targetValue,
			int percentage)
	{
		if (percentage < 0 || percentage > 100)
			return currentValue;

		double deltaPWM = targetValue - currentValue;
		double deltaValue = deltaPWM / 100 * percentage;
		double newValue = currentValue + deltaValue;

		Rover.logger.trace("PERCENTAGE: cur[" + currentValue + "] target["
				+ targetValue + "] %[" + percentage + "] = delta[" + deltaValue
				+ "] cur+delta[" + newValue + "/" + (int) newValue + "]");

		return (int) newValue;
	}

	public void setPosition(Pin pwmChannel, int pwmValue)
	{
		int currentPwmValue = armCurrentPWM[pwmChannel.getAddress()];

		Rover.logger.trace("Setting arm PWM channel[" + pwmChannel.getAddress()
				+ "] current [" + currentPwmValue + "] to value[" + pwmValue
				+ "]");

		if (pwmValue != currentPwmValue)
		{
			if (pwmValue > currentPwmValue)
			{
				for (int i = currentPwmValue; i < pwmValue; i++)
				{
					Rover.logger.trace("PWM [" + i + "]");
					pca9685Controller.setPwm(pwmChannel, 0, i);
				}
			} else
			{
				for (int i = currentPwmValue; i > pwmValue; i--)
				{
					Rover.logger.trace("PWM [" + i + "]");
					pca9685Controller.setPwm(pwmChannel, 0, i);
				}
			}
		}

		armCurrentPWM[pwmChannel.getAddress()] = pwmValue;
	}

	private void setAllPWMOff()
	{
		pca9685Controller.setAlwaysOff(PCA9685Pin.PWM_02);
		pca9685Controller.setAlwaysOff(PCA9685Pin.PWM_03);
		pca9685Controller.setAlwaysOff(PCA9685Pin.PWM_04);
		pca9685Controller.setAlwaysOff(PCA9685Pin.PWM_05);
		pca9685Controller.setAlwaysOff(PCA9685Pin.PWM_06);
		pca9685Controller.setAlwaysOff(PCA9685Pin.PWM_07);
	}

	private void setAllPWMOn()
	{
		pca9685Controller.setAlwaysOn(PCA9685Pin.PWM_02);
		pca9685Controller.setAlwaysOn(PCA9685Pin.PWM_03);
		pca9685Controller.setAlwaysOn(PCA9685Pin.PWM_04);
		pca9685Controller.setAlwaysOn(PCA9685Pin.PWM_05);
		pca9685Controller.setAlwaysOn(PCA9685Pin.PWM_06);
		pca9685Controller.setAlwaysOn(PCA9685Pin.PWM_07);
	}

	public void dump()
	{
		dumpPWMstatus();
		dumpCurrentPWM();
	}

	private void dumpPWMstatus()
	{
		// Show PWM values for outputs
		for (GpioPinPwmOutput output : pwmOutputs)
		{
			int[] onOffValues = pca9685Controller
					.getPwmOnOffValues(output.getPin());

			Rover.logger.trace(output.getPin().getName() + " ["
					+ output.getName() + "] on/off[" + onOffValues[0] + "/"
					+ onOffValues[1] + "]");
		}
	}

	private void dumpCurrentPWM()
	{
		for (int i = 0; i < 16; i++)
			Rover.logger.trace(
					"ARM PWM current [" + i + "] = [" + armCurrentPWM[i] + "]");
	}

	private GpioPinPwmOutput[] initPWMchannels(
			final PCA9685GpioProvider gpioProvider)
	{
		GpioController gpio = GpioFactory.getInstance();
		GpioPinPwmOutput myOutputs[] = {
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_02,
						"Arm base"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_03,
						"Arm shoulder"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_04,
						"Arm elbow"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_05,
						"Arm hand"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_06,
						"Arm wrist"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_07,
						"Arm clamp") };

		return myOutputs;
	}
}