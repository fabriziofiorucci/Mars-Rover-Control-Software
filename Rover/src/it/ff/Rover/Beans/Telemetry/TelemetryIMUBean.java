package it.ff.Rover.Beans.Telemetry;

public class TelemetryIMUBean extends TelemetryBaseBean
{
	private int accelX = 0;
	private int accelY = 0;
	private int accelZ = 0;
	private int gyroX = 0;
	private int gyroY = 0;
	private int gyroZ = 0;

	public TelemetryIMUBean(String _status, int _accelX, int _accelY,
			int _accelZ, int _gyroX, int _gyroY, int _gyroZ)
	{
		super(_status);
		accelX = _accelX;
		accelY = _accelY;
		accelZ = _accelZ;
		gyroX = _gyroX;
		gyroY = _gyroY;
		gyroZ = _gyroZ;
	}

	public int getAccelX()
	{
		return accelX;
	}

	public int getAccelY()
	{
		return accelY;
	}

	public int getAccelZ()
	{
		return accelZ;
	}

	public int getGyroX()
	{
		return gyroX;
	}

	public int getGyroY()
	{
		return gyroY;
	}

	public int getGyroZ()
	{
		return gyroZ;
	}
}