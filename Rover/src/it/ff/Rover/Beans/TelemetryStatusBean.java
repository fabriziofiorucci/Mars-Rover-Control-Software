package it.ff.Rover.Beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TelemetryStatusBean extends TelemetryBaseBean
{
	public TelemetryStatusBean(String _status)
	{
		super(_status);
	}
}