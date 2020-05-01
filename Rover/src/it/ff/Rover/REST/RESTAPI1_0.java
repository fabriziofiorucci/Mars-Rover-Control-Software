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
import javax.ws.rs.core.Response.Status;

import org.json.JSONException;
import org.json.JSONObject;

import it.ff.Rover.Rover;
import it.ff.Rover.Beans.REST.RESTRoverResponse;
import it.ff.Rover.Subsystem.Wheels.WheelsControllerConstants;

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
			if (!request.has("drive") || !request.has("steering"))

			{
				response.setStatus(Status.BAD_REQUEST);
				response.setDescription("drive and steering must be specified");
			} else
			{
				// Drive & steering sections check
				JSONObject drive = request.getJSONObject("drive");
				JSONObject steering = request.getJSONObject("steering");

				if (!drive.has("mode") || !drive.has("speed")
						|| !steering.has("mode") || !steering.has("amount"))
				{
					response.setStatus(Status.BAD_REQUEST);
					response.setDescription(
							"drive[mode,speed] and steering[mode,amount] must be specified");
				} else if (drive.getInt("speed") < 0
						|| drive.getInt("speed") > 100)
				{
					response.setStatus(Status.BAD_REQUEST);
					response.setDescription(
							"Driving speed must be between 0 and 100");
				} else if (steering.getInt("amount") < 0
						|| steering.getInt("amount") > 100)
				{
					response.setStatus(Status.BAD_REQUEST);
					response.setDescription(
							"Steering amount must be between 0 and 100");
				} else if (!drive.getString("mode").equals("forward")
						&& !drive.getString("mode").equals("reverse")
						&& !drive.getString("mode").equals("stop")
						&& !drive.getString("mode").equals("clockwise")
						&& !drive.getString("mode").equals("counterclockwise"))
				{
					response.setStatus(Status.BAD_REQUEST);
					response.setDescription(
							"Drive mode must be forward, reverse, clockwise, counterclockwise or stop");
				} else if (!steering.getString("mode").equals("left")
						&& !steering.getString("mode").equals("right")
						&& !steering.getString("mode").equals("none"))
				{
					response.setStatus(Status.BAD_REQUEST);
					response.setDescription(
							"Steering mode must be left, right or none");
				} else
				{
					switch (steering.getString("mode"))
					{
					case "left":
						Rover.wheelsController
								.steerLeft(steering.getInt("amount"));
						break;

					case "right":
						Rover.wheelsController
								.steerRight(steering.getInt("amount"));
						break;

					case "none":
						Rover.wheelsController.steerNeutral();
						break;
					}

					switch (drive.getString("mode"))
					{
					case "forward":
						Rover.wheelsController
								.setWheelsSpeed(drive.getInt("speed"));
						Rover.wheelsController.setWheelsDirection(
								WheelsControllerConstants.WHEELS_DIRECTION_FORWARD);
						break;

					case "reverse":
						Rover.wheelsController
								.setWheelsSpeed(drive.getInt("speed"));
						Rover.wheelsController.setWheelsDirection(
								WheelsControllerConstants.WHEELS_DIRECTION_REVERSE);
						break;

					case "clockwise":
						Rover.wheelsController
								.setWheelsSpeed(drive.getInt("speed"));
						Rover.wheelsController.setWheelsDirection(
								WheelsControllerConstants.WHEELS_DIRECTION_CLOCKWISE);
						break;

					case "counterclockwise":
						Rover.wheelsController
								.setWheelsSpeed(drive.getInt("speed"));
						Rover.wheelsController.setWheelsDirection(
								WheelsControllerConstants.WHEELS_DIRECTION_COUNTERCLOCKWISE);
						break;

					case "stop":
						Rover.wheelsController.setWheelsSpeed(1);
						Rover.wheelsController.setWheelsDirection(
								WheelsControllerConstants.WHEELS_DIRECTION_STOP);
						break;

					default:
						break;
					}

					response.setStatus(Status.ACCEPTED);
					response.setDescription("Operation successful");
				}
			}
		} catch (JSONException e)
		{
			response.setStatus(Status.INTERNAL_SERVER_ERROR);
			response.setDescription(e.getMessage());
		}

		ResponseBuilder responseHeaders = responseHeaders(
				response.getStatus().getStatusCode());

		return responseHeaders.entity(response).build();
	}

	private ResponseBuilder responseHeaders(int status)
	{
		return Response.status(status).header("Server", "Rover REST API");
	}
}
