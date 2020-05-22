package it.ff.Rover.MQTT;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import it.ff.Rover.Rover;
import it.ff.Rover.Utils.JSON;

public class MQTT implements MqttCallback
{
	private String brokerURL = null;
	private String clientId = null;
	private String username = null;
	private String password = null;
	private boolean mqttEnabled = true;

	private MqttClient mqttClient = null;

	public MQTT(String _brokerURL, String _clientId, String _username,
			String _password, boolean _mqttEnabled)
	{
		brokerURL = _brokerURL;
		clientId = _clientId;
		username = _username;
		password = _password;
		mqttEnabled = _mqttEnabled;
	}

	/**
	 * Connects to the MQTT broker
	 * 
	 * @return true if successful, false otherwise
	 */
	public boolean connect()
	{
		if (!mqttEnabled)
			return true;

		if (brokerURL == null || clientId == null || username == null
				|| password == null)
			return false;

		Rover.logger.info(
				"MQTT Connecting to [" + brokerURL + "] as [" + clientId + "]");

		try
		{
			mqttClient = new MqttClient(brokerURL, clientId,
					new MemoryPersistence());
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			connOpts.setUserName(username);
			connOpts.setPassword(password.toCharArray());

			mqttClient.connect(connOpts);
			Rover.logger.info("MQTT Connected to [" + brokerURL + "]");

			mqttClient.setCallback(this);
		} catch (MqttException e)
		{
			Rover.logger.error("Exception: " + e.getMessage());
			return false;
		}

		return true;
	}

	public boolean isConnected()
	{
		if (!mqttEnabled)
			return true;

		return mqttClient == null ? false : mqttClient.isConnected();
	}

	/**
	 * Disconnects from the MQTT broker
	 * 
	 * @return true if successful, false otherwise
	 */
	public boolean disconnect()
	{
		if (!mqttEnabled)
			return true;

		if (!isConnected())
			return false;

		try
		{
			mqttClient.disconnect();
		} catch (MqttException e)
		{
			Rover.logger.error("Exception: " + e.getMessage());
			return false;
		}

		return true;
	}

	/**
	 * Subscribe to the given MQTT topic
	 * 
	 * @param topic
	 *            the topic to subscribe to
	 * @return true if successful, false otherwise
	 */
	public boolean subscribe(String topic)
	{
		if (!mqttEnabled)
			return true;

		if (topic == null || !isConnected())
			return false;

		try
		{
			Rover.logger.trace("MQTT subscribing to [" + topic + "]");
			mqttClient.subscribe(topic);
		} catch (MqttException e)
		{
			Rover.logger.error("Exception: " + e.getMessage());
			return false;
		}

		return true;
	}

	/**
	 * Publishes a message to the given topic, using the specified QoS
	 * 
	 * @param topic
	 *            the topic to post to
	 * @param payload
	 *            the message to post
	 * @param qos
	 *            the QoS to use
	 * @return true if successful, false otherwise
	 */
	public boolean publish(String topic, String payload, int qos)
	{
		if (!mqttEnabled)
			return true;

		if (!isConnected())
			return false;

		try
		{
			Rover.logger.trace("MQTT publishing to [" + topic + "] = ["
					+ payload + "] qos [" + qos + "]");
			mqttClient.publish(topic, payload.getBytes(), qos, false);
		} catch (MqttException e)
		{
			Rover.logger.error("Exception: " + e.getMessage());
			return false;
		}

		return true;
	}

	/**
	 * Publishes the json representation of an object to the given topic, using
	 * the specified QoS
	 * 
	 * @param topic
	 *            the topic to post to
	 * @param o
	 *            the object to be serialized into JSON and published
	 * @param qos
	 *            the QoS to use
	 * @return true if successful, false otherwise
	 */
	public boolean publish(String topic, Object o, int qos)
	{
		if (!mqttEnabled)
			return true;

		if (!isConnected())
			return false;

		try
		{
			String json = JSON.Serialize(o);

			// Rover.logger.trace("MQTT publishing to [" + topic + "] = [" +
			// json + "] qos [" + qos + "]");
			mqttClient.publish(topic, json.getBytes(), qos, false);
		} catch (MqttException e)
		{
			Rover.logger.error("Exception: " + e.getMessage());
			return false;
		}

		return true;
	}

	public boolean isMqttEnabled()
	{
		return mqttEnabled;
	}

	public void setMqttEnabled(boolean mqttEnabled)
	{
		this.mqttEnabled = mqttEnabled;
	}

	@Override
	public void connectionLost(Throwable cause)
	{
		Rover.logger.trace("MQTT connection to broker lost");
		connect();
	}

	@Override
	public void messageArrived(String topic, MqttMessage message)
			throws Exception
	{
		Rover.logger
				.error("MQTT got message [" + topic + "] = [" + message + "]");
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token)
	{
		// TODO Auto-generated method stub
	}
}
