package it.ff.Rover.Hardware.NEO6M;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONArray;
import org.json.JSONObject;

import it.ff.Rover.Rover;

public class NEO6M
{
	// GPS TCP socket commands - http://catb.org/gpsd/gpsd_json.html
	private String GPS_GET_DEVICES = "?DEVICES;";
	private String GPS_WATCH_ENABLE = "?WATCH={\"enable\":true};";
	private String GPS_WATCH_DISABLE = "?WATCH={\"enable\":false};";
	private String GPS_POLL = "?POLL;";

	private String _ipAddress = null;
	private int _tcpPort = 0;

	// GPS socket
	private Socket gpsSocket = null;
	PrintWriter outToGPS = null;
	BufferedReader inFromGPS = null;

	// GPS data
	Double gpsLatitude = 0.0d;
	Double gpsLongitude = 0.0d;
	String gpsTimestamp = null;

	public NEO6M(String ipAddress, int tcpPort)
	{
		_ipAddress = ipAddress;
		_tcpPort = tcpPort;
	}

	/**
	 * Starts the GPS TCP socket
	 */
	public boolean start()
	{
		try
		{
			gpsSocket = new Socket(_ipAddress, _tcpPort);

			outToGPS = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(gpsSocket.getOutputStream())), true);
			inFromGPS = new BufferedReader(
					new InputStreamReader(gpsSocket.getInputStream()));

			String version = inFromGPS.readLine();

			Rover.logger.info("GPS version[" + version + "]");
		} catch (IOException e)
		{
			Rover.logger.error("Exception: " + e.getMessage());
			return false;
		}

		return true;
	}

	/**
	 * Closes the GPS TCP socket
	 * 
	 * @return
	 */
	public boolean stop()
	{
		try
		{
			if (gpsSocket != null)
				gpsSocket.close();
		} catch (IOException e)
		{
			Rover.logger.error("Exception: " + e.getMessage());
			return false;
		}

		gpsSocket = null;

		return true;
	}

	/**
	 * Sends a command to the GPS
	 * 
	 * @param cmd
	 *            the command to send
	 * @return the command reply
	 */
	private String sendCommand(String cmd)
	{
		if (gpsSocket != null)
		{

			try
			{
				Rover.logger.trace("GPS sending command[" + cmd + "]");
				outToGPS.println(cmd);
				outToGPS.flush();

				String output = "";

				while (inFromGPS.ready())
				{
					String reply = inFromGPS.readLine();

					Rover.logger.trace("GPS got reply[" + reply + "]");

					output += reply;
				}

				return output;
			} catch (IOException e)
			{
				Rover.logger.error("Exception: " + e.getMessage());
			}
		}

		return null;
	}

	/**
	 * Returns a json listing all GPS devices
	 * 
	 * @return
	 */
	public String getDevices()
	{
		return sendCommand(GPS_GET_DEVICES);
	}

	/**
	 * Sets the enabled/disabled state for GPS polling
	 * 
	 * ?WATCH={\"enable\":true};
	 * 
	 * {"class":"DEVICES","devices":[{"class":"DEVICE","path":"/dev/serial0","driver":"u-blox","subtype":"Unknown","activated":"2018-11-30T11:17:32.226Z","flags":1,"native":0,"bps":9600,"parity":"N","stopbits":1,"cycle":1.00,"mincycle":0.25}]}
	 * {"class":"WATCH","enable":true,"json":false,"nmea":false,"raw":0,"scaled":false,"timing":false,"split24":false,"pps":false}
	 * 
	 * @param enabled
	 * @return
	 */
	public String enablePolling(boolean enabled)
	{
		return sendCommand(enabled ? GPS_WATCH_ENABLE : GPS_WATCH_DISABLE);
	}

	/**
	 * Gets GPS data
	 * 
	 * ?POLL;
	 * 
	 * {"class":"POLL","time":"2018-11-30T11:17:50.162Z","active":1,"tpv":[{"class":"TPV","device":"/dev/serial0","mode":0,"time":"2018-11-30T11:17:49.000Z","ept":0.005}],"gst":[{"class":"GST","device":"/dev/serial0","time":"1970-01-02T07:02:29.000Z","rms":0.000,"lat":3750199.000,"lon":3750199.000,"alt":3750199.000}],"sky":[{"class":"SKY","device":"/dev/serial0","vdop":99.99,"tdop":99.99,"hdop":99.99,"gdop":99.99,"pdop":99.99}]}
	 * 
	 * @return
	 */
	public String poll()
	{
		String gpsData = sendCommand(GPS_POLL);

		// gpsData =
		// "{\"class\":\"POLL\",\"time\":\"2018-11-30T11:17:50.162Z\",\"active\":1,\"tpv\":[{\"class\":\"TPV\",\"device\":\"/dev/serial0\",\"mode\":0,\"time\":\"2018-11-30T11:17:49.000Z\",\"ept\":0.005,\"lat\":45.12,\"lon\":8.03}],\"gst\":[{\"class\":\"GST\",\"device\":\"/dev/serial0\",\"time\":\"1970-01-02T07:02:29.000Z\",\"rms\":0.000,\"lat\":3750199.000,\"lon\":3750199.000,\"alt\":3750199.000}],\"sky\":[{\"class\":\"SKY\",\"device\":\"/dev/serial0\",\"vdop\":99.99,\"tdop\":99.99,\"hdop\":99.99,\"gdop\":99.99,\"pdop\":99.99}]}";

		if (gpsData == null)
			return null;

		if (gpsData.isEmpty())
			return null;

		JSONObject obj = new JSONObject(gpsData);
		String time = (obj.isNull("time") ? null : obj.getString("time"));

		JSONArray tpvArray = null;
		JSONObject tpv = null;

		try
		{
			tpvArray = obj.getJSONArray("tpv");
			tpv = tpvArray.getJSONObject(0);
		} catch (Exception e)
		{
			Rover.logger.error("Exception: " + e.getMessage());
		}

		if (tpv != null)
		{
			Double latitude = (tpv.isNull("lat") ? Double.NaN
					: tpv.getDouble("lat"));
			Double longitude = (tpv.isNull("lon") ? Double.NaN
					: tpv.getDouble("lon"));

			// Sample date string from GPS: 2018-12-04T10:13:11.935Z
			gpsTimestamp = time;
			gpsLatitude = latitude;
			gpsLongitude = longitude;

			Rover.logger.debug("GPS time[" + gpsTimestamp + "] lat["
					+ gpsLatitude + "] lon[" + gpsLongitude + "]");
		}

		return gpsData;
	}

	/**
	 * Returns the current latitude
	 * 
	 * @return the current latitude, NaN if not available
	 */
	public double getLatitude()
	{
		return gpsLatitude;
	}

	/**
	 * Returns the current longitude
	 * 
	 * @return the current longitude, NaN if not available
	 */
	public double getLongitude()
	{
		return gpsLongitude;
	}

	/**
	 * Returns the GPS current time
	 * 
	 * @return the GPS current time
	 */
	public String getTimestamp()
	{
		return gpsTimestamp;
	}
}