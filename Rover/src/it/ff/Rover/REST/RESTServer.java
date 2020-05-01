package it.ff.Rover.REST;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import it.ff.Rover.Rover;

import java.io.IOException;
import java.net.URI;

public class RESTServer
{
	private HttpServer httpServer = null;

	/**
	 * Starts Grizzly HTTP server exposing JAX-RS resources defined in this
	 * application.
	 * 
	 * @return Grizzly HTTP server.
	 */
	public RESTServer(String baseUri)
	{
		// create a resource config that scans for JAX-RS resources and
		// providers in it.ff.Rover.REST
		final ResourceConfig rc = new ResourceConfig();
		rc.registerClasses(it.ff.Rover.REST.RESTAPI1_0.class,
				SecurityFilter.class, AuthenticationExceptionMapper.class);

		Rover.logger.debug("REST API server starting at [" + baseUri + "]");

		// create and start a new instance of grizzly http server
		// exposing the Jersey application at BASE_URI
		httpServer = GrizzlyHttpServerFactory
				.createHttpServer(URI.create(baseUri), rc);
	}

	public void start() throws IOException
	{
		httpServer.start();
	}
}