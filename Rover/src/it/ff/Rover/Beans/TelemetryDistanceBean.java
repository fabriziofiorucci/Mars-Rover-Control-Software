package it.ff.Rover.Beans;

public class TelemetryDistanceBean extends TelemetryBaseBean
{
	private String sensorName = null;
	private double distanceCM = 0.0d;
	private double percentage = 0.0d;
	private double voltage = 0.0d;
	private double rawReading = 0.0d;

	public TelemetryDistanceBean(String _status, String _sensorName,
			double _distanceCM, double _percentage, double _voltage,
			double _rawReading)
	{
		super(_status);
		sensorName = _sensorName;
		distanceCM = _distanceCM;
		percentage = _percentage;
		voltage = _voltage;
		rawReading = _rawReading;
	}

	public String getSensorName()
	{
		return sensorName;
	}

	public double getDistanceCM()
	{
		return distanceCM;
	}

	public double getPercentage()
	{
		return percentage;
	}

	public double getVoltage()
	{
		return voltage;
	}

	public double getRawReading()
	{
		return rawReading;
	}
}