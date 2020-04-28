package it.ff.Rover.Subsystem.GPS;

import it.ff.Rover.Configuration;
import it.ff.Rover.Rover;
import it.ff.Rover.RoverConstants;
import it.ff.Rover.Beans.TelemetryGPSBean;
import it.ff.Rover.Hardware.NEO6M.NEO6M;

public class GPSThread implements Runnable
{
	private Thread thisThread = null;
	private String workerName = null;

	private NEO6M neo6m = null;

	public GPSThread()
	{
		if (thisThread == null)
		{
			workerName = "gpsThread";
			neo6m = new NEO6M(Configuration.get("gps_ipv4_address"),
					Integer.parseInt(Configuration.get("gps_tcp_port")));
		}
	}

	public String getWorkerName()
	{
		return workerName;
	}

	public void start()
	{
		if (thisThread == null)
		{
			Rover.logger.debug("GPS thread: starting [" + workerName + "]");

			thisThread = new Thread(this, workerName);
			thisThread.start();
			neo6m.start();
			neo6m.enablePolling(true);

			Rover.logger.debug("GPS thread: started [" + workerName + "]");
		} else
		{
			Rover.logger.debug(
					"GPS thread: attempting to restart [" + workerName + "]");
		}
	}

	/**
	 * Triggers thread exit
	 */
	public void stop()
	{
		neo6m.enablePolling(false);
		neo6m.stop();
		thisThread = null;
	}

	/**
	 * Runs the thread
	 */
	public void run()
	{
		while (thisThread != null)
		{
			Rover.logger.trace("Running GPS thread");

			try
			{
				while (true)
				{
					neo6m.poll();
					double latitude = neo6m.getLatitude();
					double longitude = neo6m.getLongitude();

					Thread.sleep(Long
							.parseLong(Configuration.get("gps_polling_time")));

					Rover.logger.info(
							"GPS lat[" + latitude + "] lon[" + longitude + "]");

					// Publishes GPS MQTT update
					Rover.mqtt.publish(RoverConstants.MQTT_TELEMETRY_TOPIC_GPS,
							new TelemetryGPSBean("GPS beacon", latitude,
									longitude),
							2);
				}
			} catch (InterruptedException e)
			{
				Rover.logger.error("Exception: " + e.getMessage());
			}
		}
	}

	public String getLatLong()
	{
		return neo6m.getLatitude() + "/" + neo6m.getLongitude();
	}
}
