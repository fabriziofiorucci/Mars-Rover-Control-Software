package it.ff.Rover.Beans.REST;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RESTRoverResponse
{
	private String status = null;
	private String description = null;

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}
}
