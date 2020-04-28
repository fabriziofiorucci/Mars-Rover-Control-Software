package it.ff.Rover.Hardware.MPU6050;

import java.io.IOException;

import it.ff.Rover.Hardware.IIC.IIC;

public class MPU6050
{
	private IIC i2c_bus = null;
	private int i2cAddress = 0;

	public MPU6050(IIC _i2cBus, int _i2cAddress)
			throws NumberFormatException, IOException
	{
		i2c_bus = _i2cBus;
		i2cAddress = _i2cAddress;
		init();
	}

	public void init() throws NumberFormatException, IOException
	{
		// Starts the MPU6050 writing 0 to the power management register
		i2c_bus.write(i2cAddress, MPU6050Registers.PWR_MGMT_1,
				Byte.parseByte("0"));
	}

	public void stop()
	{

	}

	private int getAccelReading(int hiRegister, int loRegister)
			throws IOException
	{
		int reading_h = i2c_bus.read(i2cAddress, hiRegister);
		int reading_l = i2c_bus.read(i2cAddress, loRegister);
		int fullReading = ((reading_h << 8) & 0xff00 | (reading_l & 0x00ff))
				/ 16384;

		return fullReading;
	}

	private int getGyroReading(int hiRegister, int loRegister)
			throws IOException
	{
		int reading_h = i2c_bus.read(i2cAddress, hiRegister);
		int reading_l = i2c_bus.read(i2cAddress, loRegister);
		int fullReading = ((reading_h << 8) & 0xff00 | (reading_l & 0x00ff))
				/ 131;

		return fullReading;
	}

	public int getAccelX() throws IOException
	{
		return getAccelReading(MPU6050Registers.ACCEL_X_OUT_H,
				MPU6050Registers.ACCEL_X_OUT_L);
	}

	public int getAccelY() throws IOException
	{
		return getAccelReading(MPU6050Registers.ACCEL_Y_OUT_H,
				MPU6050Registers.ACCEL_Y_OUT_L);
	}

	public int getAccelZ() throws IOException
	{
		return getAccelReading(MPU6050Registers.ACCEL_Z_OUT_H,
				MPU6050Registers.ACCEL_Z_OUT_L);
	}

	public int getGyroX() throws IOException
	{
		return getGyroReading(MPU6050Registers.GYRO_X_OUT_H,
				MPU6050Registers.GYRO_X_OUT_L);
	}

	public int getGyroY() throws IOException
	{
		return getGyroReading(MPU6050Registers.GYRO_Y_OUT_H,
				MPU6050Registers.GYRO_Y_OUT_L);
	}

	public int getGyroZ() throws IOException
	{
		return getGyroReading(MPU6050Registers.GYRO_Z_OUT_H,
				MPU6050Registers.GYRO_Z_OUT_L);
	}
}
