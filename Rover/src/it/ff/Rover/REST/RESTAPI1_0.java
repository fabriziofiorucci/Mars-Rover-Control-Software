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

import org.codehaus.jettison.json.JSONObject;

import it.ff.Rover.Rover;

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

	@POST
	@Path("/drive")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response drive(JSONObject jsonObject)
	{
		Rover.logger.trace("REST drive [" + jsonObject.toString() + "]");

		ResponseBuilder responseHeaders = responseHeaders(200);
		return responseHeaders.entity("").build();
	}

	private ResponseBuilder responseHeaders(int status)
	{
		return Response.status(status).header("Server", "Rover REST API");
	}
}
