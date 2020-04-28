package it.ff.Rover.Beans;

public class TelemetryGPSBean extends TelemetryBaseBean
{
	private double latitude = 0.0d;
	private double longitude = 0.0d;

	public TelemetryGPSBean(String _status, double _latitude, double _longitude)
	{
		super(_status);
		latitude = _latitude;
		longitude = _longitude;
	}

	public double getLatitude()
	{
		return latitude;
	}

	public double getLongitude()
	{
		return longitude;
	}
}