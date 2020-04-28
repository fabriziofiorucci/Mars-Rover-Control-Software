package it.ff.Rover.Hardware.ADS1115;

import java.io.IOException;

import com.pi4j.gpio.extension.ads.ADS1115GpioProvider;
import com.pi4j.gpio.extension.ads.ADS1115Pin;
import com.pi4j.gpio.extension.ads.ADS1x15GpioProvider.ProgrammableGainAmplifierValue;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinAnalogInput;
import com.pi4j.io.gpio.Pin;

import it.ff.Rover.Hardware.IIC.IIC;

public class ADS1115
{
	private ADS1115GpioProvider ads1115 = null;
	private GpioPinAnalogInput channel0 = null;
	private GpioPinAnalogInput channel1 = null;
	private GpioPinAnalogInput channel2 = null;
	private GpioPinAnalogInput channel3 = null;

	// create gpio controller
	private final GpioController gpio = GpioFactory.getInstance();

	private IIC i2c_bus = null;
	private int i2c_address = 0;
	private int _monitorInterval = 0;

	public ADS1115(IIC i2cbus, int i2cAddress, int monitorInterval)
			throws IOException
	{
		i2c_bus = i2cbus;
		i2c_address = i2cAddress;
		_monitorInterval = monitorInterval;

		ads1115 = new ADS1115GpioProvider(i2c_bus.getI2c(), i2c_address);

		setMonitorInterval(_monitorInterval);
	}

	public GpioPinAnalogInput setChannel(Pin ads1115channel,
			ProgrammableGainAmplifierValue gain, double eventThreshold)
	{
		GpioPinAnalogInput p;

		p = gpio.provisionAnalogInputPin(ads1115, ads1115channel);
		ads1115.setProgrammableGainAmplifier(gain, ads1115channel);
		ads1115.setEventThreshold(eventThreshold, ads1115channel);

		if (ads1115channel == ADS1115Pin.INPUT_A0)
		{
			channel0 = p;
		} else if (ads1115channel == ADS1115Pin.INPUT_A1)
		{
			channel1 = p;
		} else if (ads1115channel == ADS1115Pin.INPUT_A2)
		{
			channel2 = p;
		} else if (ads1115channel == ADS1115Pin.INPUT_A3)
		{
			channel3 = p;
		}

		return p;
	}

	public ADS1115GpioProvider getAds1115()
	{
		return ads1115;
	}

	public int getMonitorInterval()
	{
		return _monitorInterval;
	}

	/**
	 * Define the monitoring thread refresh interval (in milliseconds). This
	 * governs the rate at which the monitoring thread will read input values
	 * from the ADC chip (a value less than 50 ms is not permitted)
	 */
	public void setMonitorInterval(int monitorInterval)
	{
		_monitorInterval = monitorInterval;

		ads1115.setMonitorInterval(_monitorInterval);
	}

	public GpioPinAnalogInput getChannel0()
	{
		return channel0;
	}

	public GpioPinAnalogInput getChannel1()
	{
		return channel1;
	}

	public GpioPinAnalogInput getChannel2()
	{
		return channel2;
	}

	public GpioPinAnalogInput getChannel3()
	{
		return channel3;
	}

	public GpioController getGpio()
	{
		return gpio;
	}

	public IIC getI2c_bus()
	{
		return i2c_bus;
	}

	public int getI2c_address()
	{
		return i2c_address;
	}
}
