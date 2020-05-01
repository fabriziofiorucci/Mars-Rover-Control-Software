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
import it.ff.Rover.Subsystem.Arm.ArmControllerConstants;
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

	public static void main(String[] args) throws InterruptedException
	{
		init();

		// ---------------------------------------------------

		// Tests wheels steering, direction and arm
		try
		{
			WheelsController wheelsController = new WheelsController(i2c,
					Integer.decode(
							Configuration.get("i2c_address_pca9685_wheels")),
					Integer.decode(
							Configuration.get("i2c_address_mcp23017_wheels")));

			ArmController armController = new ArmController(i2c, Integer
					.decode(Configuration.get("i2c_address_pca9685_arm")));

			Scanner in = new Scanner(System.in);
			String s = "";

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
					armController.stop();
					break;
				case "l":
					System.out.println("- Amount 0-100");
					s = in.nextLine();
					wheelsController.steerLeft(Integer.valueOf(s));
					break;
				case "r":
					System.out.println("- Amount 0-100");
					s = in.nextLine();
					wheelsController.steerRight(Integer.valueOf(s));
					break;
				case "n":
					wheelsController.steerNeutral();
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
					wheelsController.setWheelsSpeed(Integer.valueOf(s));
					wheelsController.setWheelsDirection(
							WheelsControllerConstants.WHEELS_DIRECTION_CLOCKWISE);
					break;
				case "v":
					System.out.println("- Speed 1-100");
					s = in.nextLine();
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
					testArm(armController, in, i2c);
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
					armController.stop();
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

	private static void testArm(ArmController armController, Scanner in,
			IIC i2c) throws IOException
	{
		String s = "";

		while (!s.startsWith("q"))
		{
			System.out.println("Test arm:");
			System.out.println(
					"- (b)ase section(1) section(2) section(3) (w)rist (c)law (q)uit");
			s = in.nextLine();

			switch (s)
			{
			case "b":
				System.out.println("(l)eft (c)enter (r)ight");
				s = in.nextLine();
				switch (s)
				{
				case "l":
					armController.setPosition(
							ArmControllerConstants.ARM_PIN_BASE,
							ArmControllerConstants.ARM_BASE_MAX);
					break;
				case "c":
					armController.setPosition(
							ArmControllerConstants.ARM_PIN_BASE,
							ArmControllerConstants.ARM_BASE_MID);
					break;
				case "r":
					armController.setPosition(
							ArmControllerConstants.ARM_PIN_BASE,
							ArmControllerConstants.ARM_BASE_MIN);
					break;

				default:
					break;
				}
				break;
			case "1":
				System.out.println("(d)own (m)id (u)p");
				s = in.nextLine();
				switch (s)
				{
				case "d":
					armController.setPosition(
							ArmControllerConstants.ARM_PIN_SHOULDER,
							ArmControllerConstants.ARM_SHOULDER_MIN);
					break;
				case "m":
					armController.setPosition(
							ArmControllerConstants.ARM_PIN_SHOULDER,
							ArmControllerConstants.ARM_SHOULDER_MID);
					break;
				case "u":
					armController.setPosition(
							ArmControllerConstants.ARM_PIN_SHOULDER,
							ArmControllerConstants.ARM_SHOULDER_MAX);
					break;

				default:
					break;
				}
				break;
			case "2":
				System.out.println("(d)own (m)id (u)p");
				s = in.nextLine();
				switch (s)
				{
				case "d":
					armController.setPosition(
							ArmControllerConstants.ARM_PIN_ELBOW,
							ArmControllerConstants.ARM_ELBOW_MIN);
					break;
				case "m":
					armController.setPosition(
							ArmControllerConstants.ARM_PIN_ELBOW,
							ArmControllerConstants.ARM_ELBOW_MID);
					break;
				case "u":
					armController.setPosition(
							ArmControllerConstants.ARM_PIN_ELBOW,
							ArmControllerConstants.ARM_ELBOW_MAX);
					break;

				default:
					break;
				}
				break;
			case "3":
				System.out.println("(d)own (m)id (u)p");
				s = in.nextLine();
				switch (s)
				{
				case "d":
					armController.setPosition(
							ArmControllerConstants.ARM_PIN_HAND,
							ArmControllerConstants.ARM_HAND_MIN);
					break;
				case "m":
					armController.setPosition(
							ArmControllerConstants.ARM_PIN_HAND,
							ArmControllerConstants.ARM_HAND_MID);
					break;
				case "u":
					armController.setPosition(
							ArmControllerConstants.ARM_PIN_HAND,
							ArmControllerConstants.ARM_HAND_MAX);
					break;

				default:
					break;
				}
				break;
			case "w":
				System.out.println("(l)eft (c)enter (r)ight");
				s = in.nextLine();
				switch (s)
				{
				case "l":
					armController.setPosition(
							ArmControllerConstants.ARM_PIN_WRIST,
							ArmControllerConstants.ARM_WRIST_MIN);
					break;
				case "c":
					armController.setPosition(
							ArmControllerConstants.ARM_PIN_WRIST,
							ArmControllerConstants.ARM_WRIST_MID);
					break;
				case "r":
					armController.setPosition(
							ArmControllerConstants.ARM_PIN_WRIST,
							ArmControllerConstants.ARM_WRIST_MAX);
					break;

				default:
					break;
				}
				break;
			case "c":
				System.out.println("(o)pen (c)losed");
				s = in.nextLine();
				switch (s)
				{
				case "o":
					armController.setPosition(
							ArmControllerConstants.ARM_PIN_CLAMP,
							ArmControllerConstants.ARM_CLAMP_MIN);
					break;
				case "c":
					armController.setPosition(
							ArmControllerConstants.ARM_PIN_CLAMP,
							ArmControllerConstants.ARM_CLAMP_MAX);
					break;

				default:
					break;
				}
				break;

			default:
				break;
			}
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
				Configuration.get("mqtt_password"));
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

		try
		{
			System.in.read();
		} catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

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
			imuThread = new IMUThread(i2c,
					Integer.decode(Configuration.get("i2c_address_mpu6050")));
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
			distanceSensors = new DistanceSensors(i2c, Integer.decode(
					Configuration.get("i2c_address_ads1115_distancesensors")));
		} catch (IOException e)
		{
			Rover.logger.fatal("Exception: " + e.getMessage());
		}

		mqtt.publish(RoverConstants.MQTT_TELEMETRY_TOPIC_STATUS,
				new TelemetryStatusBean("Distance sensors initialized"), 2);
	}
}