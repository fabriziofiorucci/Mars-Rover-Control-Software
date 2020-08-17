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

	private ArmServoPWMBean armServos[] = null;

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
		armServos = new ArmServoPWMBean[6];

		for (int i = 0; i < armServos.length; i++)
		{
			armServos[i] = new ArmServoPWMBean();
		}

		armServos[ArmControllerConstants.ARM_SERVO_BASE]
				.setServoPin(PCA9685Pin.PWM_02);
		armServos[ArmControllerConstants.ARM_SERVO_BASE].setMinPwm(
				Integer.parseInt(Configuration.get("arm_base_right")));
		armServos[ArmControllerConstants.ARM_SERVO_BASE]
				.setMidPwm(Integer.parseInt(Configuration.get("arm_base_mid")));
		armServos[ArmControllerConstants.ARM_SERVO_BASE].setMaxPwm(
				Integer.parseInt(Configuration.get("arm_base_left")));

		armServos[ArmControllerConstants.ARM_SERVO_SHOULDER]
				.setServoPin(PCA9685Pin.PWM_03);
		armServos[ArmControllerConstants.ARM_SERVO_SHOULDER].setMinPwm(
				Integer.parseInt(Configuration.get("arm_shoulder_up")));
		armServos[ArmControllerConstants.ARM_SERVO_SHOULDER].setMidPwm(
				Integer.parseInt(Configuration.get("arm_shoulder_mid")));
		armServos[ArmControllerConstants.ARM_SERVO_SHOULDER].setMaxPwm(
				Integer.parseInt(Configuration.get("arm_shoulder_down")));

		armServos[ArmControllerConstants.ARM_SERVO_ELBOW]
				.setServoPin(PCA9685Pin.PWM_04);
		armServos[ArmControllerConstants.ARM_SERVO_ELBOW].setMinPwm(
				Integer.parseInt(Configuration.get("arm_elbow_down")));
		armServos[ArmControllerConstants.ARM_SERVO_ELBOW].setMidPwm(
				Integer.parseInt(Configuration.get("arm_elbow_mid")));
		armServos[ArmControllerConstants.ARM_SERVO_ELBOW]
				.setMaxPwm(Integer.parseInt(Configuration.get("arm_elbow_up")));

		armServos[ArmControllerConstants.ARM_SERVO_HAND]
				.setServoPin(PCA9685Pin.PWM_05);
		armServos[ArmControllerConstants.ARM_SERVO_HAND]
				.setMinPwm(Integer.parseInt(Configuration.get("arm_hand_up")));
		armServos[ArmControllerConstants.ARM_SERVO_HAND]
				.setMidPwm(Integer.parseInt(Configuration.get("arm_hand_mid")));
		armServos[ArmControllerConstants.ARM_SERVO_HAND].setMaxPwm(
				Integer.parseInt(Configuration.get("arm_hand_down")));

		armServos[ArmControllerConstants.ARM_SERVO_WRIST]
				.setServoPin(PCA9685Pin.PWM_06);
		armServos[ArmControllerConstants.ARM_SERVO_WRIST].setMinPwm(
				Integer.parseInt(Configuration.get("arm_wrist_left")));
		armServos[ArmControllerConstants.ARM_SERVO_WRIST].setMidPwm(
				Integer.parseInt(Configuration.get("arm_wrist_mid")));
		armServos[ArmControllerConstants.ARM_SERVO_WRIST].setMaxPwm(
				Integer.parseInt(Configuration.get("arm_wrist_right")));

		armServos[ArmControllerConstants.ARM_SERVO_CLAMP]
				.setServoPin(PCA9685Pin.PWM_07);
		armServos[ArmControllerConstants.ARM_SERVO_CLAMP].setMinPwm(
				Integer.parseInt(Configuration.get("arm_clamp_closed")));
		armServos[ArmControllerConstants.ARM_SERVO_CLAMP].setMidPwm(
				Integer.parseInt(Configuration.get("arm_clamp_mid")));
		armServos[ArmControllerConstants.ARM_SERVO_CLAMP].setMaxPwm(
				Integer.parseInt(Configuration.get("arm_clamp_open")));

		// Set all pwm off
		setAllPWMOff();

		// Set arm to idle
		setArmAtRest();
	}

	public void stop()
	{
		setAllPWMOff();
	}

	public void setArmAtRest()
	{
		armServos[ArmControllerConstants.ARM_SERVO_BASE].setCurPercentage(0);
		armServos[ArmControllerConstants.ARM_SERVO_SHOULDER]
				.setCurPercentage(70);
		armServos[ArmControllerConstants.ARM_SERVO_ELBOW].setCurPercentage(0);
		armServos[ArmControllerConstants.ARM_SERVO_HAND].setCurPercentage(0);
		armServos[ArmControllerConstants.ARM_SERVO_WRIST].setCurPercentage(50);
		armServos[ArmControllerConstants.ARM_SERVO_CLAMP].setCurPercentage(50);
	}

	public void setServo(int pwmChannel, int percentage)
	{
		int currentPwmValue = armServos[pwmChannel].getCurPwm();
		int targetPwmValue = armServos[pwmChannel].setCurPercentage(percentage);

		Rover.logger.trace("Setting arm PWM channel[" + pwmChannel
				+ "] current [" + currentPwmValue + "] to value["
				+ targetPwmValue + "]");

		if (currentPwmValue == 0)
		{
			pca9685Controller.setPwm(armServos[pwmChannel].getServoPin(), 0,
					targetPwmValue);
		} else
		{
			if (targetPwmValue != currentPwmValue)
			{
				try
				{
					if (targetPwmValue > currentPwmValue)
					{
						for (int i = currentPwmValue; i < targetPwmValue; i++)
						{
							Rover.logger.trace("PreviousPWM[" + currentPwmValue
									+ "] => TargetPWM[" + i + "]");
							pca9685Controller.setPwm(
									armServos[pwmChannel].getServoPin(), 0, i);
							Thread.sleep(20);
						}
					} else
					{
						for (int i = currentPwmValue; i > targetPwmValue; i--)
						{
							Rover.logger.trace("PreviousPWM[" + currentPwmValue
									+ "] => TargetPWM[" + i + "]");
							pca9685Controller.setPwm(
									armServos[pwmChannel].getServoPin(), 0, i);

							Thread.sleep(20);
						}
					}
				} catch (InterruptedException e)
				{
					Rover.logger.error("Exception: " + e.getMessage());
				}
			}
		}
	}

	private void setAllPWMOff()
	{
		for (int i = 0; i < armServos.length; i++)
		{
			pca9685Controller.setAlwaysOff(armServos[i].getServoPin());
		}
	}

	private void setAllPWMOn()
	{
		for (int i = 0; i < armServos.length; i++)
		{
			pca9685Controller.setAlwaysOn(armServos[i].getServoPin());
		}
	}

	public void dump()
	{
		dumpPWMstatus();
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

	private GpioPinPwmOutput[] initPWMchannels(
			final PCA9685GpioProvider gpioProvider)
	{
		GpioController gpio = GpioFactory.getInstance();
		GpioPinPwmOutput myOutputs[] = {
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_00,
						"Head pan"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_01,
						"Head tilt"),
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
						"Arm clamp"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_08,
						"unused"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_09,
						"unused"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_10,
						"unused"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_11,
						"unused"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_12,
						"unused"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_13,
						"unused"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_14,
						"unused"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_15,
						"unused") };

		return myOutputs;
	}
}