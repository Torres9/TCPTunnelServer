package com.yumcouver.tunnel.server.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("health")
public class HealthRest {
    private static final Logger LOGGER = LogManager.getLogger(HealthRest.class);

    public HealthRest() {
    }

    @GET
    @Produces("text/plain")
    public String healthCheck() {
        LOGGER.info("Health Checked");
        return "Health OK";
    }
}