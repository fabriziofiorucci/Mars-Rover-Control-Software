package it.ff.Rover;

import java.io.IOException;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.pi4j.system.SystemInfo;

import it.ff.Rover.Beans.Telemetry.TelemetryStatusBean;
import it.ff.Rover.Hardware.IIC.IIC;
import it.ff.Rover.MQTT.MQTT;
import it.ff.Rover.REST.RESTServer;
import it.ff.Rover.Subsystem.Arm.ArmController;
import it.ff.Rover.Subsystem.DistanceSensors.DistanceSensors;
import it.ff.Rover.Subsystem.GPS.GPSThread;
import it.ff.Rover.Subsystem.IMU.IMUThread;
import it.ff.Rover.Subsystem.Wheels.WheelsController;
import it.ff.Rover.Subsystem.Wheels.WheelsControllerConstants;

public class Rover
{
	public static Logger logger = null;

	public static RESTServer restServer = null;
	public static MQTT mqtt = null;
	public static IIC i2c = null;
	public static GPSThread gpsThread = null;
	public static IMUThread imuThread = null;
	public static DistanceSensors distanceSensors = null;

	public static WheelsController wheelsController = null;
	public static ArmController armController = null;

	public static void main(String[] args) throws InterruptedException
	{
		init();

		// ---------------------------------------------------

		// Tests wheels steering, direction and arm
		try
		{
			wheelsController = new WheelsController(i2c, Integer.parseInt(
					Configuration.get("i2c_address_pca9685_wheels"), 16),
					Integer.parseInt(
							Configuration.get("i2c_address_mcp23017_wheels"),
							16));

			armController = new ArmController(i2c, Integer.parseInt(
					Configuration.get("i2c_address_pca9685_arm"), 16));

			Scanner in = new Scanner(System.in);
			String s = "";
			String s2 = "";

			while (!s.startsWith("quit"))
			{
				System.out.println("Test: (quit) to exit (o)ff all PWM");
				System.out.println("- Steer (l)eft (r)ight (n)eutral");
				System.out.println(
						"- Move  (f)wd  (b)ack  (c)w      (v)ccw    (s)top");
				System.out.println("- Arm (arm)");
				System.out.println("- (d)ump distance");
				s = in.nextLine();

				switch (s)
				{
				case "off":
					wheelsController.stop();
					// armController.stop();
					break;
				case "l":
					System.out.println("- Amount 0-100");
					s = in.nextLine();
					wheelsController.setSteerDirection(
							WheelsControllerConstants.WHEELS_STEERING_LEFT,
							Integer.valueOf(s));
					break;
				case "r":
					System.out.println("- Amount 0-100");
					s = in.nextLine();
					wheelsController.setSteerDirection(
							WheelsControllerConstants.WHEELS_STEERING_RIGHT,
							Integer.valueOf(s));
					break;
				case "n":
					wheelsController.setSteerDirection(
							WheelsControllerConstants.WHEELS_STEERING_NONE, 0);
					break;
				case "f":
					System.out.println("- Speed 1-100");
					s = in.nextLine();
					wheelsController.setWheelsSpeed(Integer.valueOf(s));
					wheelsController.setWheelsDirection(
							WheelsControllerConstants.WHEELS_DIRECTION_FORWARD);
					break;
				case "b":
					System.out.println("- Speed 1-100");
					s = in.nextLine();
					wheelsController.setWheelsSpeed(Integer.valueOf(s));
					wheelsController.setWheelsDirection(
							WheelsControllerConstants.WHEELS_DIRECTION_REVERSE);
					break;
				case "c":
					System.out.println("- Speed 1-100");
					s = in.nextLine();
					System.out.println("- Steering amount 1-100");
					s2 = in.nextLine();
					wheelsController.setSteerDirection(
							WheelsControllerConstants.WHEELS_STEERING_CLOCKWISE,
							Integer.valueOf(s2));
					wheelsController.setWheelsSpeed(Integer.valueOf(s));
					wheelsController.setWheelsDirection(
							WheelsControllerConstants.WHEELS_DIRECTION_CLOCKWISE);
					break;
				case "v":
					System.out.println("- Speed 1-100");
					s = in.nextLine();
					System.out.println("- Steering amount 1-100");
					s2 = in.nextLine();
					wheelsController.setSteerDirection(
							WheelsControllerConstants.WHEELS_STEERING_COUNTERCLOCKWISE,
							Integer.valueOf(s2));
					wheelsController.setWheelsSpeed(Integer.valueOf(s));
					wheelsController.setWheelsDirection(
							WheelsControllerConstants.WHEELS_DIRECTION_COUNTERCLOCKWISE);
					break;
				case "s":
					wheelsController.setWheelsSpeed(1);
					wheelsController.setWheelsDirection(
							WheelsControllerConstants.WHEELS_DIRECTION_STOP);
					break;

				case "arm":
					testArm(armController, in);
					break;

				case "d":
					System.out.println("Distances: FL["
							+ distanceSensors.getDistanceFL().getDistance()
							+ "] FR["
							+ distanceSensors.getDistanceFR().getDistance()
							+ "] RL["
							+ distanceSensors.getDistanceRL().getDistance()
							+ "] RR["
							+ distanceSensors.getDistanceRR().getDistance()
							+ "]");
					break;

				case "quit":
					wheelsController.stop();
					// armController.stop();
					break;

				default:
					break;
				}

				wheelsController.dump();
			}

			in.close();
		} catch (IOException e)
		{
			Rover.logger.fatal("Exception: " + e.getMessage());
		}

		gpsThread.stop();
		System.exit(0);
	}

	private static void testArm(ArmController armController, Scanner in)
			throws IOException
	{
		String s = "";
		String p = "";

		while (!s.startsWith("q"))
		{
			System.out.println("Test arm:");
			System.out.println(
					"- (0)base (1)shoulder (2)elbow (3)hand (4)wrist (5)clamp (q)uit");
			s = in.nextLine();

			System.out.println("Percentage?");
			p = in.nextLine();

			armController.setServo(Integer.valueOf(s), Integer.valueOf(p));
		}
	}

	private static void init()
	{
		// Main logger setup
		logger = LogManager.getLogger(Rover.class.getName());
		Rover.logger.info("Rover starting");

		// Configuration file fetch
		try
		{
			Configuration.init("etc/rover.conf");
		} catch (IOException e)
		{
			Rover.logger.fatal("Exception: " + e.getMessage());
			System.exit(1);
		}

		// MQTT initialization
		mqtt = new MQTT(Configuration.get("mqtt_broker"),
				Configuration.get("mqtt_clientid"),
				Configuration.get("mqtt_username"),
				Configuration.get("mqtt_password"),
				Boolean.valueOf(Configuration.get("mqtt_enabled")));
		if (!mqtt.connect())
		{
			Rover.logger.fatal("Can't connect to MQTT");
			return;
		}

		// MQTT subscription to control topic
		mqtt.subscribe(Configuration.get("mqtt_control_topic"));

		// REST Server initialization
		Rover.restServer = new RESTServer(Configuration.get("rest_base_uri"));
		try
		{
			Rover.restServer.start();
		} catch (IOException e)
		{
			Rover.logger.fatal("Exception: " + e.getMessage());
			mqtt.publish(RoverConstants.MQTT_TELEMETRY_TOPIC_STATUS,
					new TelemetryStatusBean("Can't start REST API Server"), 2);

			return;
		}
		Rover.logger.info("REST API Server started");
		mqtt.publish(RoverConstants.MQTT_TELEMETRY_TOPIC_STATUS,
				new TelemetryStatusBean("REST API Server started"), 2);

		/*
		 * try { System.in.read(); } catch (IOException e1) { // TODO
		 * Auto-generated catch block e1.printStackTrace(); }
		 */

		// I2C bus initialization
		try
		{
			Rover.logger.info("Running on [" + SystemInfo.getModelName() + " / "
					+ SystemInfo.getHardware() + "]");
			Rover.logger.info(
					"CPU temperature [" + SystemInfo.getCpuTemperature() + "]");

			mqtt.publish(RoverConstants.MQTT_TELEMETRY_TOPIC_STATUS,
					new TelemetryStatusBean("MQTT connected"), 2);

			Rover.logger.info("I2C bus: initializing");
			i2c = new IIC(I2CBus.BUS_1);
			Rover.logger.info("I2C bus: initialized");

			mqtt.publish(RoverConstants.MQTT_TELEMETRY_TOPIC_STATUS,
					new TelemetryStatusBean("I2C bus initialized"), 2);
		} catch (UnsupportedBusNumberException | UnsupportedOperationException
				| InterruptedException |

				IOException e)
		{
			Rover.logger.fatal("Exception: " + e.getMessage());
			mqtt.disconnect();

			System.exit(0);
		}

		// Starts GPS thread
		try
		{
			gpsThread = new GPSThread();
			gpsThread.start();
		} catch (Exception e)
		{
			Rover.logger.fatal("GPS thread: start failed");
			Rover.logger.fatal("Exception: " + e.getMessage());
		}

		mqtt.publish(RoverConstants.MQTT_TELEMETRY_TOPIC_STATUS,
				new TelemetryStatusBean("GPS initialized"), 2);

		// Starts IMU thread
		try
		{
			imuThread = new IMUThread(i2c, Integer
					.parseInt(Configuration.get("i2c_address_mpu6050"), 16));
			imuThread.start();
		} catch (Exception e)
		{
			Rover.logger.fatal("IMU thread: start failed");
			Rover.logger.fatal("Exception: " + e.getMessage());
		}

		mqtt.publish(RoverConstants.MQTT_TELEMETRY_TOPIC_STATUS,
				new TelemetryStatusBean("IMU initialized"), 2);

		// Starts distance sensors polling
		try
		{
			distanceSensors = new DistanceSensors(i2c,
					Integer.parseInt(
							Configuration
									.get("i2c_address_ads1115_distancesensors"),
							16));
		} catch (IOException e)
		{
			Rover.logger.fatal("Exception: " + e.getMessage());
		}

		mqtt.publish(RoverConstants.MQTT_TELEMETRY_TOPIC_STATUS,
				new TelemetryStatusBean("Distance sensors initialized"), 2);
	}
}