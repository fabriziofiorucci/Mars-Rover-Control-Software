package it.ff.Rover.REST;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import it.ff.Rover.Rover;
import it.ff.Rover.Beans.REST.RESTRoverResponse;

@Path("/1.0")
public class RESTAPI1_0
{
	@GET
	@Path("/test/{string}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getIt(@Context HttpHeaders headers,
			@PathParam("string") String string)
	{
		Rover.logger.trace("REST API TEST [" + string + "]");

		ResponseBuilder responseHeaders = responseHeaders(200);

		return responseHeaders.entity(string).build();
	}

	/**
	 * Sets driving and steering mode
	 */
	@POST
	@Path("/drive")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response drive(String input)
	{
		Rover.logger.trace("REST drive [" + input + "]");

		JSONObject request = null;
		RESTRoverResponse response = new RESTRoverResponse();

		try
		{
			request = new JSONObject(input);

			// Json check
			if (request.has("drive") && request.has("steering"))
			{
				response.setStatus("done");
				response.setDescription("Operation successful");
			} else
			{
				response.setStatus("Invalid JSON");
				response.setDescription("drive and steering must be specified");
			}

		} catch (JSONException e)
		{
			response.setStatus("Error");
			response.setDescription(e.getMessage());
		}

		ResponseBuilder responseHeaders = responseHeaders(200);
		return responseHeaders.entity(response).build();
	}

	private ResponseBuilder responseHeaders(int status)
	{
		return Response.status(status).header("Server", "Rover REST API");
	}
}
