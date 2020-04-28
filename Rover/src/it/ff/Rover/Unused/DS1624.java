package it.ff.Rover.Unused;

import java.io.IOException;

import it.ff.Rover.Rover;
import it.ff.Rover.Hardware.IIC.IIC;

public class DS1624
{
	private byte DS1624_READ_TEMP = (byte) 0xAA;
	private byte DS1624_START = (byte) 0xEE;
	private byte DS1624_STOP = (byte) 0x22;

	private IIC i2c_bus = null;
	private int i2cAddress = 0;

	public DS1624(IIC i2cbus, int i2c_address)
	{
		i2c_bus = i2cbus;
		i2cAddress = i2c_address;
	}

	public boolean start()
	{
		try
		{
			i2c_bus.write(i2cAddress, DS1624_START);
		} catch (IOException e)
		{
			Rover.logger.fatal("Exception: " + e.getMessage());
			return false;
		}

		return true;
	}

	public boolean stop()
	{
		try
		{
			i2c_bus.write(i2cAddress, DS1624_STOP);
		} catch (IOException e)
		{
			Rover.logger.fatal("Exception: " + e.getMessage());
			return false;
		}

		return true;
	}

	public double read() throws IOException
	{
		byte tempBytes[] = new byte[2];
		double temp = 0.0d;

		int readBytes = i2c_bus.read(i2cAddress, DS1624_READ_TEMP, tempBytes, 0,
				2);

		if (readBytes != 2)
		{
			Rover.logger.fatal("Error reading temperature from DS1624: read ["
					+ readBytes + "] bytes");

			throw new IOException();
		}

		byte msb = tempBytes[0];
		byte lsb = tempBytes[1];

		double sensorReading = (double) msb * 256 + (double) lsb;

		if (sensorReading > 32768)
		{
			temp = -(65536 - sensorReading) / 256;
		} else
		{
			temp = sensorReading / 256;
		}

		/*
		 * if (msb >= 128) temp = -(((double) (((255 - msb) << 8) | ((255 - lsb)
		 * & 0xf8)) + 8) / 8) * 0.03125; else temp = ((double) ((double)msb *
		 * 256 + lsb) / 8 * 0.03125);
		 */

		Rover.logger.trace("Reading [" + sensorReading + "] MSB["
				+ Byte.valueOf(msb) + "] LSB[" + Byte.valueOf(lsb) + "] temp["
				+ temp + "]");

		return temp;
	}
}