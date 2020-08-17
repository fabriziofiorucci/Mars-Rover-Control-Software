package it.ff.Rover.Subsystem.Arm;

import com.pi4j.io.gpio.Pin;

public class ArmServoPWMBean
{
	private Pin servoPin;
	private int minPwm = 0;
	private int midPwm = 0;
	private int maxPwm = 0;
	private int curPwm = 0;

	public Pin getServoPin()
	{
		return servoPin;
	}

	public void setServoPin(Pin servoPin)
	{
		this.servoPin = servoPin;
	}

	public int getMinPwm()
	{
		return minPwm;
	}

	public void setMinPwm(int minPwm)
	{
		this.minPwm = minPwm;
	}

	public int getMidPwm()
	{
		return midPwm;
	}

	public void setMidPwm(int midPwm)
	{
		this.midPwm = midPwm;
	}

	public int getMaxPwm()
	{
		return maxPwm;
	}

	public void setMaxPwm(int maxPwm)
	{
		this.maxPwm = maxPwm;
	}

	public int getCurPwm()
	{
		return curPwm;
	}

	public void setCurPwm(int curPwm)
	{
		this.curPwm = curPwm;
	}

	/**
	 * Sets the current pwm based on percentage
	 * 
	 * @param percentage
	 * @return the actual pwm computed based on percentage, -1 if percentage is
	 *         invalid
	 */
	public int setCurPercentage(int percentage)
	{
		if (percentage < 0 || percentage > 100)
			return -1;

		double pwm2set = 0;

		if (percentage == 50)
			pwm2set = getMidPwm();
		else if (percentage < 50)
		{
			pwm2set = getMinPwm()
					+ (getMidPwm() - getMinPwm()) * percentage / 50d;
		} else
		{
			pwm2set = getMidPwm()
					+ (getMaxPwm() - getMidPwm()) * (percentage - 50d) / 50d;
		}

		setCurPwm((int) pwm2set);

		return (int) pwm2set;
	}
}