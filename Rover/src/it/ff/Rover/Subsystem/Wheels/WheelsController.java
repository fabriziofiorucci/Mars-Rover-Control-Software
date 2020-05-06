package it.ff.Rover.Subsystem.Wheels;

import java.io.IOException;
import java.math.BigDecimal;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.gpio.extension.pca.PCA9685GpioProvider;
import com.pi4j.gpio.extension.pca.PCA9685Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinState;

import it.ff.Rover.Configuration;
import it.ff.Rover.Rover;
import it.ff.Rover.Hardware.IIC.IIC;

public class WheelsController
{
	private PCA9685GpioProvider pca9685Controller = null;
	private MCP23017GpioProvider mcp23017Controller = null;

	private GpioPinPwmOutput[] pwmOutputs = null;
	private GpioPinDigitalOutput[] directionOutputs = null;

	private int[] wheelsCurrentPWM;

	public WheelsController(IIC i2cbus, int pca9685_i2c_address,
			int mcp23017_i2c_address) throws IOException
	{
		Rover.logger.debug("Wheels controller init pca9685 at "
				+ pca9685_i2c_address + " mcp23017 at " + mcp23017_i2c_address);

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

		// Initializes MCP23017 controller
		mcp23017Controller = new MCP23017GpioProvider(i2cbus.getI2c(),
				mcp23017_i2c_address);
		directionOutputs = initMCP23017channels(mcp23017Controller);

		init();
	}

	public void init()
	{
		// Initializes variables from config file
		WheelsControllerConstants.STEER_CENTER_RL = Integer
				.parseInt(Configuration.get("steer_center_rl"));
		WheelsControllerConstants.STEER_CENTER_FL = Integer
				.parseInt(Configuration.get("steer_center_fl"));
		WheelsControllerConstants.STEER_CENTER_FR = Integer
				.parseInt(Configuration.get("steer_center_fr"));
		WheelsControllerConstants.STEER_CENTER_RR = Integer
				.parseInt(Configuration.get("steer_center_rr"));
		WheelsControllerConstants.STEER_DELTA = Integer
				.parseInt(Configuration.get("steer_delta"));

		// Set all pwm off
		setAllPWMOff();

		wheelsCurrentPWM = new int[16];
		for (int i = 0; i < 16; i++)
		{
			wheelsCurrentPWM[i] = 0;
		}

		wheelsCurrentPWM[WheelsControllerConstants.STEER_PIN_REAR_LEFT
				.getAddress()] = WheelsControllerConstants.STEER_CENTER_RL;
		wheelsCurrentPWM[WheelsControllerConstants.STEER_PIN_FRONT_LEFT
				.getAddress()] = WheelsControllerConstants.STEER_CENTER_FL;
		wheelsCurrentPWM[WheelsControllerConstants.STEER_PIN_FRONT_RIGHT
				.getAddress()] = WheelsControllerConstants.STEER_CENTER_FR;
		wheelsCurrentPWM[WheelsControllerConstants.STEER_PIN_REAR_RIGHT
				.getAddress()] = WheelsControllerConstants.STEER_CENTER_RR;

		// Set steering to neutral
		steerNeutral();

		// Set wheels to stopped
		setWheelsDirection(WheelsControllerConstants.WHEELS_DIRECTION_STOP);

		// Set wheels speed to 0
		setWheelsSpeed(1);
	}

	public void stop()
	{
		setAllPWMOff();
	}

	/**
	 * Steers left
	 */
	public void steerLeft(float percentage)
	{
		steer(-WheelsControllerConstants.STEER_DELTA, percentage);
	}

	/**
	 * Steers right
	 */
	public void steerRight(float percentage)
	{
		steer(WheelsControllerConstants.STEER_DELTA, percentage);
	}

	public void steerNeutral()
	{
		steer(0, 100);
	}

	/**
	 * Steers based on delta
	 */
	private void steer(int delta, float percentage)
	{
		if (percentage < 0 || percentage > 100)
			return;

		int deltaSteer = (int) (delta * percentage / 100);

		Rover.logger.trace("Steer [" + delta + "/" + percentage + "] => ["
				+ deltaSteer + "]");

		setPWM(WheelsControllerConstants.STEER_PIN_REAR_LEFT,
				WheelsControllerConstants.STEER_CENTER_RL - deltaSteer);
		setPWM(WheelsControllerConstants.STEER_PIN_FRONT_LEFT,
				WheelsControllerConstants.STEER_CENTER_FL + deltaSteer);
		setPWM(WheelsControllerConstants.STEER_PIN_FRONT_RIGHT,
				WheelsControllerConstants.STEER_CENTER_FR + deltaSteer);
		setPWM(WheelsControllerConstants.STEER_PIN_REAR_RIGHT,
				WheelsControllerConstants.STEER_CENTER_RR - deltaSteer);
	}

	/**
	 * Sets all wheels speed
	 * 
	 * @param percentage
	 *            0-100%
	 */
	public boolean setWheelsSpeed(int percentage)
	{
		if (percentage < 1 || percentage > 100)
			return false;

		// PCA9685: 12 bit PWM: 2^12=4096
		int speed = (int) 4095 / 100 * percentage;

		setWheelsSpeedLeft(speed);
		setWheelsSpeedRight(speed);

		return true;
	}

	/**
	 * Sets left wheels speed
	 * 
	 * @param speed
	 */
	private void setWheelsSpeedLeft(int speed)
	{
		pca9685Controller.setPwm(PCA9685Pin.PWM_00, 0, speed);
		pca9685Controller.setPwm(PCA9685Pin.PWM_02, 0, speed);
		pca9685Controller.setPwm(PCA9685Pin.PWM_04, 0, speed);
	}

	/**
	 * Sets right wheels speed
	 * 
	 * @param speed
	 */
	private void setWheelsSpeedRight(int speed)
	{
		pca9685Controller.setPwm(PCA9685Pin.PWM_01, 0, speed);
		pca9685Controller.setPwm(PCA9685Pin.PWM_03, 0, speed);
		pca9685Controller.setPwm(PCA9685Pin.PWM_05, 0, speed);
	}

	public void setWheelsDirection(int direction)
	{
		switch (direction)
		{
		case WheelsControllerConstants.WHEELS_DIRECTION_STOP:
			mcp23017Controller.setState(MCP23017Pin.GPIO_A0, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A1, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A2, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A3, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A4, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A5, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A6, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A7, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_B0, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_B1, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_B2, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_B3, PinState.LOW);
			break;

		case WheelsControllerConstants.WHEELS_DIRECTION_FORWARD:
			mcp23017Controller.setState(MCP23017Pin.GPIO_A0, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A1, PinState.HIGH);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A2, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A3, PinState.HIGH);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A4, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A5, PinState.HIGH);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A6, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A7, PinState.HIGH);
			mcp23017Controller.setState(MCP23017Pin.GPIO_B0, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_B1, PinState.HIGH);
			mcp23017Controller.setState(MCP23017Pin.GPIO_B2, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_B3, PinState.HIGH);
			break;

		case WheelsControllerConstants.WHEELS_DIRECTION_REVERSE:
			mcp23017Controller.setState(MCP23017Pin.GPIO_A0, PinState.HIGH);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A1, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A2, PinState.HIGH);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A3, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A4, PinState.HIGH);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A5, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A6, PinState.HIGH);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A7, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_B0, PinState.HIGH);
			mcp23017Controller.setState(MCP23017Pin.GPIO_B1, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_B2, PinState.HIGH);
			mcp23017Controller.setState(MCP23017Pin.GPIO_B3, PinState.LOW);
			break;

		case WheelsControllerConstants.WHEELS_DIRECTION_CLOCKWISE:
			mcp23017Controller.setState(MCP23017Pin.GPIO_A0, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A1, PinState.HIGH);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A2, PinState.HIGH);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A3, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A4, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A5, PinState.HIGH);

			mcp23017Controller.setState(MCP23017Pin.GPIO_A6, PinState.HIGH);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A7, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_B0, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_B1, PinState.HIGH);
			mcp23017Controller.setState(MCP23017Pin.GPIO_B2, PinState.HIGH);
			mcp23017Controller.setState(MCP23017Pin.GPIO_B3, PinState.LOW);
			break;

		case WheelsControllerConstants.WHEELS_DIRECTION_COUNTERCLOCKWISE:
			mcp23017Controller.setState(MCP23017Pin.GPIO_A0, PinState.HIGH);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A1, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A2, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A3, PinState.HIGH);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A4, PinState.HIGH);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A5, PinState.LOW);

			mcp23017Controller.setState(MCP23017Pin.GPIO_A6, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_A7, PinState.HIGH);
			mcp23017Controller.setState(MCP23017Pin.GPIO_B0, PinState.HIGH);
			mcp23017Controller.setState(MCP23017Pin.GPIO_B1, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_B2, PinState.LOW);
			mcp23017Controller.setState(MCP23017Pin.GPIO_B3, PinState.HIGH);
			break;

		default:
			break;
		}
	}

	private void setPWM(Pin pwmChannel, int pwmValue)
	{
		pca9685Controller.setPwm(pwmChannel, 0, pwmValue);

		wheelsCurrentPWM[pwmChannel.getAddress()] = pwmValue;
	}

	private void setAllPWMOff()
	{
		pca9685Controller.setAlwaysOff(PCA9685Pin.PWM_00);
		pca9685Controller.setAlwaysOff(PCA9685Pin.PWM_01);
		pca9685Controller.setAlwaysOff(PCA9685Pin.PWM_02);
		pca9685Controller.setAlwaysOff(PCA9685Pin.PWM_03);
		pca9685Controller.setAlwaysOff(PCA9685Pin.PWM_04);
		pca9685Controller.setAlwaysOff(PCA9685Pin.PWM_05);
		pca9685Controller.setAlwaysOff(PCA9685Pin.PWM_06);
		pca9685Controller.setAlwaysOff(PCA9685Pin.PWM_07);
		pca9685Controller.setAlwaysOff(PCA9685Pin.PWM_08);
		pca9685Controller.setAlwaysOff(PCA9685Pin.PWM_09);
		pca9685Controller.setAlwaysOff(PCA9685Pin.PWM_10);
		pca9685Controller.setAlwaysOff(PCA9685Pin.PWM_11);
		pca9685Controller.setAlwaysOff(PCA9685Pin.PWM_12);
		pca9685Controller.setAlwaysOff(PCA9685Pin.PWM_13);
		pca9685Controller.setAlwaysOff(PCA9685Pin.PWM_14);
		pca9685Controller.setAlwaysOff(PCA9685Pin.PWM_15);
	}

	private void setAllPWMOn()
	{
		pca9685Controller.setAlwaysOn(PCA9685Pin.PWM_00);
		pca9685Controller.setAlwaysOn(PCA9685Pin.PWM_01);
		pca9685Controller.setAlwaysOn(PCA9685Pin.PWM_02);
		pca9685Controller.setAlwaysOn(PCA9685Pin.PWM_03);
		pca9685Controller.setAlwaysOn(PCA9685Pin.PWM_04);
		pca9685Controller.setAlwaysOn(PCA9685Pin.PWM_05);
		pca9685Controller.setAlwaysOn(PCA9685Pin.PWM_06);
		pca9685Controller.setAlwaysOn(PCA9685Pin.PWM_07);
		pca9685Controller.setAlwaysOn(PCA9685Pin.PWM_08);
		pca9685Controller.setAlwaysOn(PCA9685Pin.PWM_09);
		pca9685Controller.setAlwaysOn(PCA9685Pin.PWM_10);
		pca9685Controller.setAlwaysOn(PCA9685Pin.PWM_11);
		pca9685Controller.setAlwaysOn(PCA9685Pin.PWM_12);
		pca9685Controller.setAlwaysOn(PCA9685Pin.PWM_13);
		pca9685Controller.setAlwaysOn(PCA9685Pin.PWM_14);
		pca9685Controller.setAlwaysOn(PCA9685Pin.PWM_15);
	}

	public void dump()
	{
		dumpPWMstatus();
		dumpDirectionStatus();
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

	private void dumpDirectionStatus()
	{
		// Show PWM values for outputs
		for (GpioPinDigitalOutput output : directionOutputs)
		{
			PinState pinState = mcp23017Controller.getState(output.getPin());

			Rover.logger
					.trace(output.getPin().getName() + " [" + output.getName()
							+ "] on/off[" + pinState.getValue() + "]");
		}
	}

	private GpioPinPwmOutput[] initPWMchannels(
			final PCA9685GpioProvider gpioProvider)
	{
		GpioController gpio = GpioFactory.getInstance();
		GpioPinPwmOutput myOutputs[] = {
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_00,
						"Speed Front Left"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_01,
						"Speed Front Right"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_02,
						"Speed Center Left"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_03,
						"Speed Center Right"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_04,
						"Speed Rear Left"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_05,
						"Speed Rear Right"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_06,
						"Steering Rear Left"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_07,
						"Steering Front Left"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_08,
						"Steering Front Right"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_09,
						"Steering Rear Right"),
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

	private GpioPinDigitalOutput[] initMCP23017channels(
			MCP23017GpioProvider provider)
	{
		provider.setMode(MCP23017Pin.GPIO_A0, PinMode.DIGITAL_OUTPUT);
		provider.setMode(MCP23017Pin.GPIO_A1, PinMode.DIGITAL_OUTPUT);
		provider.setMode(MCP23017Pin.GPIO_A2, PinMode.DIGITAL_OUTPUT);
		provider.setMode(MCP23017Pin.GPIO_A3, PinMode.DIGITAL_OUTPUT);
		provider.setMode(MCP23017Pin.GPIO_A4, PinMode.DIGITAL_OUTPUT);
		provider.setMode(MCP23017Pin.GPIO_A5, PinMode.DIGITAL_OUTPUT);
		provider.setMode(MCP23017Pin.GPIO_A6, PinMode.DIGITAL_OUTPUT);
		provider.setMode(MCP23017Pin.GPIO_A7, PinMode.DIGITAL_OUTPUT);
		provider.setMode(MCP23017Pin.GPIO_B0, PinMode.DIGITAL_OUTPUT);
		provider.setMode(MCP23017Pin.GPIO_B1, PinMode.DIGITAL_OUTPUT);
		provider.setMode(MCP23017Pin.GPIO_B2, PinMode.DIGITAL_OUTPUT);
		provider.setMode(MCP23017Pin.GPIO_B3, PinMode.DIGITAL_OUTPUT);

		GpioController gpio = GpioFactory.getInstance();
		GpioPinDigitalOutput myOutputs[] = {
				gpio.provisionDigitalOutputPin(provider, MCP23017Pin.GPIO_A0,
						"GPIO_A0 Center Left", PinState.LOW),
				gpio.provisionDigitalOutputPin(provider, MCP23017Pin.GPIO_A1,
						"GPIO_A1 Center Left", PinState.LOW),
				gpio.provisionDigitalOutputPin(provider, MCP23017Pin.GPIO_A2,
						"GPIO_A2 Center Right", PinState.LOW),
				gpio.provisionDigitalOutputPin(provider, MCP23017Pin.GPIO_A3,
						"GPIO_A3 Center Right", PinState.LOW),
				gpio.provisionDigitalOutputPin(provider, MCP23017Pin.GPIO_A4,
						"GPIO_A4 Rear Left", PinState.LOW),
				gpio.provisionDigitalOutputPin(provider, MCP23017Pin.GPIO_A5,
						"GPIO_A5 Rear Left", PinState.LOW),
				gpio.provisionDigitalOutputPin(provider, MCP23017Pin.GPIO_A6,
						"GPIO_A6 Rear Right", PinState.LOW),
				gpio.provisionDigitalOutputPin(provider, MCP23017Pin.GPIO_A7,
						"GPIO_A7 Rear Right", PinState.LOW),
				gpio.provisionDigitalOutputPin(provider, MCP23017Pin.GPIO_B0,
						"GPIO_B0 Front Left", PinState.LOW),
				gpio.provisionDigitalOutputPin(provider, MCP23017Pin.GPIO_B1,
						"GPIO_B1 Front Left", PinState.LOW),
				gpio.provisionDigitalOutputPin(provider, MCP23017Pin.GPIO_B2,
						"GPIO_B2 Front Right", PinState.LOW),
				gpio.provisionDigitalOutputPin(provider, MCP23017Pin.GPIO_B3,
						"GPIO_B3 Front Right", PinState.LOW) };

		return myOutputs;
	}
}