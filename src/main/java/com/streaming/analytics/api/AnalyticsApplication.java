package com.streaming.analytics.api;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application configuration
 * Defines the base path for all REST endpoints
 */
@ApplicationPath("/api/v1/analytics")
public class AnalyticsApplication extends Application {
    // No additional configuration needed
    // JAX-RS will automatically discover all @Path annotated classes
}
