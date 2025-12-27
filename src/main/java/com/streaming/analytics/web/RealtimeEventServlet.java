package com.streaming.analytics.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import jakarta.inject.Inject;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * SSE (Server-Sent Events) Servlet for real-time event streaming
 * Polls MongoDB and pushes new events to connected clients
 */
@WebServlet(urlPatterns = "/api/v1/analytics/realtime/stream", asyncSupported = true)
public class RealtimeEventServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(RealtimeEventServlet.class);
    private static final int POLL_INTERVAL_SECONDS = 3;
    private static final int MAX_EVENTS_PER_POLL = 5;

    @Inject
    private MongoDatabase database;

    private ObjectMapper objectMapper;
    private ScheduledExecutorService executor;

    @Override
    public void init() throws ServletException {
        super.init();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        executor = Executors.newScheduledThreadPool(2);
        logger.info("RealtimeEventServlet initialized");
    }

    @Override
    public void destroy() {
        if (executor != null) {
            executor.shutdown();
        }
        super.destroy();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Set SSE headers
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("Access-Control-Allow-Origin", "*");

        // Start async processing
        AsyncContext asyncContext = request.startAsync();
        asyncContext.setTimeout(0); // No timeout

        PrintWriter writer = response.getWriter();

        // Send initial connection message
        sendEvent(writer, "connected", "{\"status\":\"connected\",\"message\":\"Real-time stream active\"}");

        logger.info("SSE client connected");

        // Schedule periodic polling
        final String[] lastEventId = { null };

        executor.scheduleAtFixedRate(() -> {
            try {
                if (writer.checkError()) {
                    logger.info("SSE client disconnected");
                    asyncContext.complete();
                    return;
                }

                // Poll for recent events
                List<Document> recentEvents = getRecentEvents(lastEventId[0]);

                for (Document event : recentEvents) {
                    String eventId = event.get("_id") != null ? event.get("_id").toString() : null;
                    if (eventId != null) {
                        lastEventId[0] = eventId;
                    }

                    // Create simplified event for streaming
                    Document streamEvent = new Document()
                            .append("eventId", event.getString("eventId"))
                            .append("userId", event.getString("userId"))
                            .append("videoId", event.getString("videoId"))
                            .append("action", event.getString("action"))
                            .append("duration", event.get("duration"));

                    sendEvent(writer, "event", streamEvent.toJson());
                }

                // Send heartbeat to keep connection alive
                if (recentEvents.isEmpty()) {
                    sendEvent(writer, "heartbeat", "{\"type\":\"heartbeat\"}");
                }

            } catch (Exception e) {
                logger.error("Error polling events", e);
            }
        }, 0, POLL_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Gets recent events from MongoDB
     */
    private List<Document> getRecentEvents(String lastEventId) {
        List<Document> events = new ArrayList<>();

        try {
            MongoCollection<Document> collection = database.getCollection("events");

            // Get latest events, sorted by _id descending (most recent first)
            for (Document doc : collection.find()
                    .sort(Sorts.descending("_id"))
                    .limit(MAX_EVENTS_PER_POLL)) {
                events.add(doc);
            }

            // Reverse to get chronological order
            java.util.Collections.reverse(events);

        } catch (Exception e) {
            logger.error("Error fetching recent events", e);
        }

        return events;
    }

    /**
     * Sends an SSE event to the client
     */
    private void sendEvent(PrintWriter writer, String eventType, String data) {
        writer.write("event: " + eventType + "\n");
        writer.write("data: " + data + "\n\n");
        writer.flush();
    }
}
