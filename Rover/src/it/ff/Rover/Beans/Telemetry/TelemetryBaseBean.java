package it.ff.Rover.Beans.Telemetry;

import java.time.LocalDateTime;

import it.ff.Rover.Rover;

public class TelemetryBaseBean
{
	private LocalDateTime timestamp = null;
	private String status = null;
	/*
	 * private float cpuTemperature = 0.0f; private long memoryUsed = 0; private
	 * long memoryFree = 0;
	 */

	public TelemetryBaseBean(String _status)
	{
		status = _status;
		try
		{
			timestamp = java.time.LocalDateTime.now();
			/*
			 * cpuTemperature = SystemInfo.getCpuTemperature(); memoryUsed =
			 * SystemInfo.getMemoryUsed(); memoryFree =
			 * SystemInfo.getMemoryFree();
			 */
		} catch (UnsupportedOperationException e)
		{
			Rover.logger.error("Exception: " + e.getMessage());
		}
	}

	public LocalDateTime getTimestamp()
	{
		return timestamp;
	}

	public String getStatus()
	{
		return status;
	}

	/*
	 * public Float getCpuTemperature() { return cpuTemperature; }
	 * 
	 * public long getMemoryUsed() { return memoryUsed; }
	 * 
	 * public long getMemoryFree() { return memoryFree; }
	 */
}