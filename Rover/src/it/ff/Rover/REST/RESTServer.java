package it.ff.Rover.REST;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import it.ff.Rover.Configuration;
import it.ff.Rover.Rover;

import java.net.URI;

public class RESTServer
{
	/**
	 * Starts Grizzly HTTP server exposing JAX-RS resources defined in this
	 * application.
	 * 
	 * @return Grizzly HTTP server.
	 */
	public HttpServer startServer()
	{
		// create a resource config that scans for JAX-RS resources and
		// providers in it.ff.Rover.REST
		final ResourceConfig rc = new ResourceConfig()
				.packages("it.ff.Rover.REST.RESTResources");

		Rover.logger.debug("REST API server starting at ["
				+ Configuration.get("rest_base_uri") + "]");

		// create and start a new instance of grizzly http server
		// exposing the Jersey application at BASE_URI
		return GrizzlyHttpServerFactory.createHttpServer(
				URI.create(Configuration.get("rest_base_uri")), rc);
	}
}