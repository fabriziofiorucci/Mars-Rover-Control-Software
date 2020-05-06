package it.ff.Rover.Beans.Telemetry;

public class TelemetryGPSBean extends TelemetryBaseBean
{
	private String latitude = null;
	private String longitude = null;
	private String gpsTimestamp = null;

	public TelemetryGPSBean(String _status, double _latitude, double _longitude,
			String _timestamp)
	{
		super(_status);
		latitude = String.valueOf(_latitude);
		longitude = String.valueOf(_longitude);
		gpsTimestamp = _timestamp;
	}

	public String getLatitude()
	{
		return latitude;
	}

	public String getLongitude()
	{
		return longitude;
	}

	public String getGpsTimestamp()
	{
		return gpsTimestamp;
	}
}