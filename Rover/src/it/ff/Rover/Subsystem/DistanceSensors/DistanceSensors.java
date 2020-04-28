package it.ff.Rover.Subsystem.DistanceSensors;

import java.io.IOException;

import com.pi4j.component.sensor.DistanceSensorChangeEvent;
import com.pi4j.component.sensor.DistanceSensorListener;
import com.pi4j.component.sensor.impl.DistanceSensorComponent;
import com.pi4j.gpio.extension.ads.ADS1115GpioProvider;
import com.pi4j.gpio.extension.ads.ADS1115Pin;
import com.pi4j.gpio.extension.ads.ADS1x15GpioProvider.ProgrammableGainAmplifierValue;
import com.pi4j.io.gpio.GpioPinAnalogInput;

import it.ff.Rover.Rover;
import it.ff.Rover.RoverConstants;
import it.ff.Rover.Beans.TelemetryDistanceBean;
import it.ff.Rover.Hardware.ADS1115.ADS1115;
import it.ff.Rover.Hardware.IIC.IIC;

public class DistanceSensors
{
	private ADS1115 ads1115 = null;
	private DistanceSensorComponent distanceRL = null;
	private DistanceSensorComponent distanceRR = null;
	private DistanceSensorComponent distanceFL = null;
	private DistanceSensorComponent distanceFR = null;

	public DistanceSensors(IIC i2cbus, int i2cAddress) throws IOException
	{
		ads1115 = new ADS1115(i2cbus, i2cAddress, 50);

		ads1115.setChannel(ADS1115Pin.INPUT_A0,
				ProgrammableGainAmplifierValue.PGA_4_096V, 50);
		ads1115.setChannel(ADS1115Pin.INPUT_A1,
				ProgrammableGainAmplifierValue.PGA_4_096V, 50);
		ads1115.setChannel(ADS1115Pin.INPUT_A2,
				ProgrammableGainAmplifierValue.PGA_4_096V, 50);
		ads1115.setChannel(ADS1115Pin.INPUT_A3,
				ProgrammableGainAmplifierValue.PGA_4_096V, 50);

		// Defines distance sensors
		distanceRL = createDistanceSensor(ads1115.getChannel0());
		distanceRR = createDistanceSensor(ads1115.getChannel1());
		distanceFL = createDistanceSensor(ads1115.getChannel2());
		distanceFR = createDistanceSensor(ads1115.getChannel3());

		distanceRL.setName("RearLeft");
		distanceRR.setName("RearRight");
		distanceFL.setName("FrontLeft");
		distanceFR.setName("FrontRight");
	}

	private DistanceSensorComponent createDistanceSensor(GpioPinAnalogInput pin)
	{
		DistanceSensorComponent d = new DistanceSensorComponent(pin);

		d.setName(pin.getName());
		d.addCalibrationCoordinate(21600, 13);
		d.addCalibrationCoordinate(21500, 14);
		d.addCalibrationCoordinate(21400, 15);
		d.addCalibrationCoordinate(21200, 16);
		d.addCalibrationCoordinate(21050, 17);
		d.addCalibrationCoordinate(20900, 18);
		d.addCalibrationCoordinate(20500, 19);
		d.addCalibrationCoordinate(20000, 20);
		d.addCalibrationCoordinate(15000, 30);
		d.addCalibrationCoordinate(12000, 40);
		d.addCalibrationCoordinate(9200, 50);
		d.addCalibrationCoordinate(8200, 60);
		d.addCalibrationCoordinate(6200, 70);
		d.addCalibrationCoordinate(4200, 80);

		d.addListener(new DistanceSensorListener() {
			@Override
			public void onDistanceChange(DistanceSensorChangeEvent event)
			{
				com.pi4j.component.sensor.DistanceSensor s = event.getSensor();

				// RAW value
				double value = event.getRawValue();

				// Estimated distance
				double distance = event.getDistance();

				// percentage
				double percent = ((value * 100)
						/ ADS1115GpioProvider.ADS1115_RANGE_MAX_VALUE);

				// approximate voltage ( *scaled based on PGA setting )
				double voltage = ProgrammableGainAmplifierValue.PGA_4_096V
						.getVoltage() * (percent / 100);

				String sensorName = s.getName();

				// display output
				Rover.logger.trace(sensorName + " Distance[" + distance
						+ "cm] Volts[" + voltage + "] Percent[" + percent
						+ "%] raw[" + value + "]");

				// Publishes distance MQTT update
				Rover.mqtt.publish(RoverConstants.MQTT_TELEMETRY_TOPIC_DISTANCE,
						new TelemetryDistanceBean("Distance sensor update",
								sensorName, distance, percent, voltage, value),
						2);
			}
		});

		return d;
	}

	public DistanceSensorComponent getDistanceRL()
	{
		return distanceRL;
	}

	public DistanceSensorComponent getDistanceRR()
	{
		return distanceRR;
	}

	public DistanceSensorComponent getDistanceFL()
	{
		return distanceFL;
	}

	public DistanceSensorComponent getDistanceFR()
	{
		return distanceFR;
	}
}