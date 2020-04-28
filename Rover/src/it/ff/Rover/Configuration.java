package it.ff.Rover;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration
{
	private static Properties properties = new Properties();

	/**
	 * Fetches a configuration file
	 * 
	 * @param cfgFileName
	 *            the configuration file name
	 * @throws IOException
	 */
	public static void init(String cfgFileName) throws IOException
	{
		Rover.logger.info("Fetching configuration file [" + cfgFileName + "]");

		InputStream is = new FileInputStream(cfgFileName);

		properties.load(is);

		RoverConstants.MQTT_CONTROL_TOPIC = get("mqtt_control_topic");
		RoverConstants.MQTT_TELEMETRY_TOPIC_STATUS = get(
				"mqtt_telemetry_topic_status");
		RoverConstants.MQTT_TELEMETRY_TOPIC_IMU = get(
				"mqtt_telemetry_topic_imu");
		RoverConstants.MQTT_TELEMETRY_TOPIC_GPS = get(
				"mqtt_telemetry_topic_gps");
		RoverConstants.MQTT_TELEMETRY_TOPIC_DISTANCE = get(
				"mqtt_telemetry_topic_distance");
	}

	/**
	 * Returns the given parameter value
	 * 
	 * @param parameterName
	 *            the parameter name
	 * @return the parameter value
	 */
	public static String get(String parameterName)
	{
		String parameterValue = properties.getProperty(parameterName);

		Rover.logger.trace("Configuration parameter [" + parameterName + "] = ["
				+ parameterValue + "]");

		return parameterValue;
	}
}