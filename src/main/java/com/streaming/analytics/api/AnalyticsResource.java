package com.streaming.analytics.api;

import com.streaming.analytics.model.Video;
import com.streaming.analytics.model.VideoStats;
import com.streaming.analytics.model.ViewEvent;
import com.streaming.analytics.service.AnalyticsService;
import com.streaming.analytics.service.EventProcessorService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API for analytics operations
 * Base path: /api/v1/analytics
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class AnalyticsResource {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsResource.class);

    @Inject
    private EventProcessorService eventProcessor;

    @Inject
    private AnalyticsService analyticsService;

    /**
     * Health check endpoint
     * GET /api/v1/analytics/health
     */
    @GET
    @Path("/health")
    public Response healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Streaming Analytics API");
        health.put("timestamp", Instant.now().toString());
        health.put("totalEvents", eventProcessor.getTotalEventCount());

        return Response.ok(health).build();
    }

    /**
     * Ingest a single event
     * POST /api/v1/analytics/events
     */
    @POST
    @Path("/events")
    public Response ingestEvent(ViewEvent event) {
        try {
            // Validate event
            if (event == null || event.getEventId() == null || event.getEventId().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorResponse("Invalid event: eventId is required"))
                        .build();
            }

            if (event.getUserId() == null || event.getUserId().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorResponse("Invalid event: userId is required"))
                        .build();
            }

            if (event.getVideoId() == null || event.getVideoId().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorResponse("Invalid event: videoId is required"))
                        .build();
            }

            // Process event
            eventProcessor.processEvent(event);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Event processed successfully");
            response.put("eventId", event.getEventId());

            return Response.status(Response.Status.CREATED).entity(response).build();

        } catch (Exception e) {
            logger.error("Error ingesting event", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse("Failed to process event: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Ingest a batch of events
     * POST /api/v1/analytics/events/batch
     */
    @POST
    @Path("/events/batch")
    public Response ingestBatch(List<ViewEvent> events) {
        try {
            if (events == null || events.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorResponse("No events provided"))
                        .build();
            }

            // Limit batch size for safety
            if (events.size() > 10000) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorResponse("Batch size exceeds maximum of 10000 events"))
                        .build();
            }

            long startTime = System.currentTimeMillis();
            int processed = eventProcessor.processBatch(events);
            long duration = System.currentTimeMillis() - startTime;

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Batch processed successfully");
            response.put("eventsProcessed", processed);
            response.put("processingTimeMs", duration);
            response.put("eventsPerSecond", processed * 1000.0 / duration);

            return Response.status(Response.Status.CREATED).entity(response).build();

        } catch (Exception e) {
            logger.error("Error ingesting batch", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse("Failed to process batch: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Get top videos
     * GET /api/v1/analytics/videos/top?limit=10
     */
    @GET
    @Path("/videos/top")
    public Response getTopVideos(@QueryParam("limit") @DefaultValue("10") int limit) {
        try {
            if (limit < 1 || limit > 100) {
                limit = 10;
            }

            List<VideoStats> topVideos = eventProcessor.getTopVideos(limit);

            Map<String, Object> response = new HashMap<>();
            response.put("count", topVideos.size());
            response.put("videos", topVideos);

            return Response.ok(response).build();

        } catch (Exception e) {
            logger.error("Error getting top videos", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse("Failed to get top videos: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Get video statistics
     * GET /api/v1/analytics/videos/{videoId}/stats
     */
    @GET
    @Path("/videos/{videoId}/stats")
    public Response getVideoStats(@PathParam("videoId") String videoId) {
        try {
            if (videoId == null || videoId.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorResponse("videoId is required"))
                        .build();
            }

            VideoStats stats = eventProcessor.getVideoStats(videoId);

            if (stats == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(errorResponse("No statistics found for video: " + videoId))
                        .build();
            }

            return Response.ok(stats).build();

        } catch (Exception e) {
            logger.error("Error getting video stats", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse("Failed to get video stats: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Get personalized recommendations
     * GET /api/v1/analytics/users/{userId}/recommendations?limit=5
     */
    @GET
    @Path("/users/{userId}/recommendations")
    public Response getRecommendations(
            @PathParam("userId") String userId,
            @QueryParam("limit") @DefaultValue("5") int limit) {
        try {
            if (userId == null || userId.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorResponse("userId is required"))
                        .build();
            }

            if (limit < 1 || limit > 50) {
                limit = 5;
            }

            List<Video> recommendations = eventProcessor.getRecommendations(userId, limit);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("count", recommendations.size());
            response.put("recommendations", recommendations);

            return Response.ok(response).build();

        } catch (Exception e) {
            logger.error("Error getting recommendations for user: {}", userId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse("Failed to get recommendations: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Get category statistics
     * GET /api/v1/analytics/categories
     */
    @GET
    @Path("/categories")
    public Response getCategoryStats() {
        try {
            Map<String, AnalyticsService.CategoryStats> categoryStats = analyticsService.aggregateByCategory();

            Map<String, Object> response = new HashMap<>();
            response.put("count", categoryStats.size());
            response.put("categories", categoryStats);

            return Response.ok(response).build();

        } catch (Exception e) {
            logger.error("Error getting category stats", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse("Failed to get category stats: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Get trending videos
     * GET /api/v1/analytics/videos/trending?limit=10
     */
    @GET
    @Path("/videos/trending")
    public Response getTrendingVideos(@QueryParam("limit") @DefaultValue("10") int limit) {
        try {
            if (limit < 1 || limit > 50) {
                limit = 10;
            }

            List<AnalyticsService.TrendingVideo> trending = analyticsService.detectTrending(limit);

            Map<String, Object> response = new HashMap<>();
            response.put("count", trending.size());
            response.put("trending", trending);

            return Response.ok(response).build();

        } catch (Exception e) {
            logger.error("Error getting trending videos", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse("Failed to get trending videos: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Get dashboard summary
     * GET /api/v1/analytics/dashboard
     */
    @GET
    @Path("/dashboard")
    public Response getDashboardSummary() {
        try {
            AnalyticsService.DashboardSummary summary = analyticsService.getDashboardSummary();
            return Response.ok(summary).build();

        } catch (Exception e) {
            logger.error("Error getting dashboard summary", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse("Failed to get dashboard summary: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Helper method to create error response
     */
    private Map<String, String> errorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}
