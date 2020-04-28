package it.ff.Rover.Hardware.IIC;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class IIC
{
	private I2CBus i2c = null;

	public IIC(int busNumber) throws UnsupportedBusNumberException, IOException
	{
		init(busNumber);
	}

	public void init(int busNumber)
			throws UnsupportedBusNumberException, IOException
	{
		i2c = I2CFactory.getInstance(busNumber);
	}

	public int read(int deviceAddress, int register) throws IOException
	{
		I2CDevice device = i2c.getDevice(deviceAddress);
		return device.read(register);
	}

	public int read(int deviceAddress, int register, byte[] buffer, int offset,
			int size) throws IOException
	{
		I2CDevice device = i2c.getDevice(deviceAddress);
		return device.read(register, buffer, offset, size);
	}

	public void write(int deviceAddress, byte value) throws IOException
	{
		I2CDevice device = i2c.getDevice(deviceAddress);
		device.write(value);
	}

	public void write(int deviceAddress, int register, byte value)
			throws IOException
	{
		I2CDevice device = i2c.getDevice(deviceAddress);
		device.write(register, value);
	}

	public I2CBus getI2c()
	{
		return i2c;
	}
}