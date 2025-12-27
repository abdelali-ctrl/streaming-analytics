package com.streaming.analytics.web;

import com.streaming.analytics.model.VideoStats;
import com.streaming.analytics.service.AnalyticsService;
import com.streaming.analytics.service.EventProcessorService;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Servlet controller for the analytics dashboard
 * Implements MVC pattern - fetches data and forwards to JSP view
 */
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(DashboardServlet.class);

    @Inject
    private EventProcessorService eventProcessor;

    @Inject
    private AnalyticsService analyticsService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            logger.info("Loading dashboard data...");

            // 1. Get top videos
            List<VideoStats> topVideos = eventProcessor.getTopVideos(10);
            request.setAttribute("topVideos", topVideos);

            // 2. Get category statistics
            Map<String, AnalyticsService.CategoryStats> categoryStats = analyticsService.aggregateByCategory();
            request.setAttribute("categoryStats", categoryStats);

            // 3. Get trending videos
            List<AnalyticsService.TrendingVideo> trendingVideos = analyticsService.detectTrending(5);
            request.setAttribute("trendingVideos", trendingVideos);

            // 4. Get dashboard summary
            AnalyticsService.DashboardSummary summary = analyticsService.getDashboardSummary();
            request.setAttribute("summary", summary);

            // 5. Get total event count
            long totalEvents = eventProcessor.getTotalEventCount();
            request.setAttribute("totalEvents", totalEvents);

            // 6. Get chart data - Device distribution
            Map<String, Long> deviceStats = analyticsService.aggregateByDevice();
            request.setAttribute("deviceStats", deviceStats);

            // 7. Get chart data - Hourly event counts
            Map<String, Long> hourlyStats = analyticsService.getHourlyStats();
            request.setAttribute("hourlyStats", hourlyStats);

            // 8. Get chart data - Quality distribution
            Map<String, Long> qualityStats = analyticsService.aggregateByQuality();
            request.setAttribute("qualityStats", qualityStats);

            // 9. Get chart data - Action breakdown
            Map<String, Long> actionStats = analyticsService.aggregateByAction();
            request.setAttribute("actionStats", actionStats);

            // 10. Get chart data - Avg duration by category
            Map<String, Double> avgDurationStats = analyticsService.getAvgDurationByCategory();
            request.setAttribute("avgDurationStats", avgDurationStats);

            logger.info("Dashboard data loaded: {} top videos, {} categories",
                    topVideos.size(), categoryStats.size());

            // Forward to JSP view
            request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp")
                    .forward(request, response);

        } catch (Exception e) {
            logger.error("Error loading dashboard", e);
            request.setAttribute("error", "Failed to load dashboard: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp")
                    .forward(request, response);
        }
    }
}
