package it.ff.Rover.REST.API1_0;

import javax.ws.rs.core.Response.Status;

import org.json.JSONException;
import org.json.JSONObject;

import it.ff.Rover.Rover;
import it.ff.Rover.Beans.REST.RESTRoverResponse;
import it.ff.Rover.Subsystem.Wheels.WheelsControllerConstants;

public class Methods
{
	/**
	 * Handles the "drive" REST method
	 * 
	 * @param request
	 *            the request json
	 * @return the rest response
	 */
	public static RESTRoverResponse drive(JSONObject request)
	{
		int driveDirection = WheelsControllerConstants.WHEELS_DIRECTION_STOP;
		int steeringDirection = WheelsControllerConstants.WHEELS_STEERING_NONE;
		int driveSpeed = 0;
		int steeringAmount = 0;

		Rover.logger.trace("REST drive [" + request.toString() + "]");

		RESTRoverResponse response = new RESTRoverResponse();

		try
		{
			// Json request must include either drive or steering
			if (!request.has("drive") && !request.has("steering"))
			{
				response.setStatus(Status.BAD_REQUEST);
				response.setDescription("drive or steering must be specified");

				return response;
			}

			if (request.has("drive"))
			{
				JSONObject drive = request.getJSONObject("drive");

				if (!drive.has("mode"))
				{
					response.setStatus(Status.BAD_REQUEST);
					response.setDescription("drive mode must be specified");

					return response;
				}

				// driving speed defaults to 0%
				if (drive.has("speed"))
				{
					driveSpeed = drive.getInt("speed");

					if (driveSpeed < 0 || driveSpeed > 100)
					{
						response.setStatus(Status.BAD_REQUEST);
						response.setDescription(
								"Driving speed must be between 0 and 100");

						return response;
					}
				}

				switch (drive.getString("mode").toLowerCase())
				{
				case "forward":
					driveDirection = WheelsControllerConstants.WHEELS_DIRECTION_FORWARD;
					break;

				case "reverse":
					driveDirection = WheelsControllerConstants.WHEELS_DIRECTION_REVERSE;
					break;

				case "clockwise":
					driveDirection = WheelsControllerConstants.WHEELS_DIRECTION_CLOCKWISE;
					break;

				case "counterclockwise":
					driveDirection = WheelsControllerConstants.WHEELS_DIRECTION_COUNTERCLOCKWISE;
					break;

				case "stop":
					driveDirection = WheelsControllerConstants.WHEELS_DIRECTION_STOP;
					break;

				default:
					response.setStatus(Status.BAD_REQUEST);
					response.setDescription(
							"Invalid drive mode. Allowed modes are: forward, reverse, clockwise, counterclockwise and stop");
					return response;
				}
			}

			if (request.has("steering"))
			{
				JSONObject steering = request.getJSONObject("steering");

				if (!steering.has("mode"))
				{
					response.setStatus(Status.BAD_REQUEST);
					response.setDescription("steering mode must be specified");

					return response;
				}

				// steering amount defaults to 0%
				if (steering.has("amount"))
				{
					steeringAmount = steering.getInt("amount");

					if (steeringAmount < 0 || steeringAmount > 100)
					{
						response.setStatus(Status.BAD_REQUEST);
						response.setDescription(
								"Steering amount must be between 0 and 100");

						return response;
					}
				}

				switch (steering.getString("mode").toLowerCase())
				{
				case "left":
					steeringDirection = WheelsControllerConstants.WHEELS_STEERING_LEFT;
					break;

				case "right":
					steeringDirection = WheelsControllerConstants.WHEELS_STEERING_RIGHT;
					break;

				case "clockwise":
					steeringDirection = WheelsControllerConstants.WHEELS_STEERING_CLOCKWISE;
					break;

				case "counterclockwise":
					steeringDirection = WheelsControllerConstants.WHEELS_STEERING_COUNTERCLOCKWISE;
					break;

				case "none":
					steeringDirection = WheelsControllerConstants.WHEELS_STEERING_NONE;
					break;
				default:
					response.setStatus(Status.BAD_REQUEST);
					response.setDescription(
							"Invalid steering mode mode. Allowed modes are: left, right, clockwise, counterclockwise and none");
					return response;
				}
			}
		} catch (JSONException e)
		{
			response.setStatus(Status.INTERNAL_SERVER_ERROR);
			response.setDescription(e.getMessage());
		}

		if (request.has("steering"))
		{
			Rover.logger.info("REST Steering [" + steeringDirection + "/"
					+ steeringAmount + "%]");

			Rover.wheelsController.setSteerDirection(steeringDirection,
					steeringAmount);
		}

		if (request.has("drive"))
		{
			Rover.logger.info("REST Driving [" + driveDirection + "/"
					+ driveSpeed + "%]");

			Rover.wheelsController.setWheelsDirection(driveDirection);
			Rover.wheelsController.setWheelsSpeed(driveSpeed);
		}

		response.setStatus(Status.ACCEPTED);
		response.setDescription("Operation successful");

		return response;

	}

	/**
	 * Handles the "arm" REST method
	 * 
	 * @param request
	 *            the request json
	 * @return the rest response
	 */
	public static RESTRoverResponse arm(JSONObject request)
	{
		Rover.logger.trace("REST arm [" + request.toString() + "]");

		RESTRoverResponse response = new RESTRoverResponse();

		response.setStatus(Status.NOT_IMPLEMENTED);
		response.setDescription("To be implemented");

		return response;
	}
}