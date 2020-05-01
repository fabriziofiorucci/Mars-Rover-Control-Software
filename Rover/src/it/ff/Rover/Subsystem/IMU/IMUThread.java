package it.ff.Rover.Subsystem.IMU;

import java.io.IOException;

import it.ff.Rover.Configuration;
import it.ff.Rover.Rover;
import it.ff.Rover.RoverConstants;
import it.ff.Rover.Beans.Telemetry.TelemetryIMUBean;
import it.ff.Rover.Hardware.IIC.IIC;
import it.ff.Rover.Hardware.MPU6050.MPU6050;

public class IMUThread implements Runnable
{
	private Thread thisThread = null;
	private String workerName = null;

	private MPU6050 mpu6050 = null;

	public IMUThread(IIC i2cBus, int i2cAddress)
			throws NumberFormatException, IOException
	{
		if (thisThread == null)
		{
			workerName = "imuThread";
			mpu6050 = new MPU6050(i2cBus, i2cAddress);
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
			Rover.logger.debug("IMU thread: starting [" + workerName + "]");

			thisThread = new Thread(this, workerName);
			thisThread.start();
			try
			{
				mpu6050.init();
			} catch (NumberFormatException | IOException e)
			{
				Rover.logger.fatal("Exception: " + e.getMessage());
			}

			Rover.logger.debug("IMU thread: started [" + workerName + "]");
		} else
		{
			Rover.logger.debug(
					"IMU thread: attempting to restart [" + workerName + "]");
		}
	}

	/**
	 * Triggers thread exit
	 */
	public void stop()
	{
		mpu6050.stop();
		thisThread = null;
	}

	/**
	 * Runs the thread
	 */
	public void run()
	{
		while (thisThread != null)
		{
			Rover.logger.trace("Running IMU thread");

			try
			{
				while (true)
				{
					Rover.logger.trace("IMU Accel[" + mpu6050.getAccelX() + "/"
							+ mpu6050.getAccelY() + "/" + mpu6050.getAccelZ()
							+ "] Gyro[" + mpu6050.getGyroX() + "/"
							+ mpu6050.getGyroY() + "/" + mpu6050.getGyroZ()
							+ "]");

					// Publishes IMU MQTT update
					Rover.mqtt.publish(RoverConstants.MQTT_TELEMETRY_TOPIC_IMU,
							new TelemetryIMUBean("IMU Update",
									mpu6050.getAccelX(), mpu6050.getAccelY(),
									mpu6050.getAccelZ(), mpu6050.getGyroX(),
									mpu6050.getGyroY(), mpu6050.getGyroZ()),
							2);

					Thread.sleep(Long
							.parseLong(Configuration.get("imu_polling_time")));
				}
			} catch (IOException | InterruptedException e)
			{
				Rover.logger.error("Exception: " + e.getMessage());
			}
		}
	}
}