<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

            <!DOCTYPE html>
            <html lang="en">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>📊 Streaming Analytics Dashboard</title>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
                <!-- Chart.js -->
                <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
            </head>

            <body>
                <div class="container">
                    <!-- Header -->
                    <header class="header">
                        <h1>📊 Streaming Analytics Dashboard</h1>
                        <p class="subtitle">Real-time Big Data Analytics Platform</p>

                        <!-- Continuous Generation Control -->
                        <div class="generator-control">
                            <div class="generator-info">
                                <span class="generator-label">📡 Continuous Generation</span>
                                <span id="generator-status" class="generator-status status-inactive">Inactive</span>
                            </div>

                            <!-- Rate Control Slider -->
                            <div class="rate-control">
                                <label for="rate-slider">⚡ Rate:</label>
                                <input type="range" id="rate-slider" min="1" max="50" value="5" class="rate-slider">
                                <span id="rate-value" class="rate-value">5 evt/s</span>
                            </div>

                            <div class="generator-stats">
                                <span id="events-count">0 events generated</span>
                            </div>

                            <!-- Live User Count -->
                            <div class="live-users">
                                <span class="live-indicator"></span>
                                <span id="live-user-count">0</span>
                                <span class="live-label">active users</span>
                            </div>

                            <button id="generator-toggle" class="generator-toggle" onclick="toggleGenerator()">
                                <div class="toggle-icon">
                                    <svg class="wifi-icon" viewBox="0 0 24 24" width="24" height="24">
                                        <path class="wifi-wave wave-3"
                                            d="M1 9l2 2c4.97-4.97 13.03-4.97 18 0l2-2C16.93 2.93 7.08 2.93 1 9z" />
                                        <path class="wifi-wave wave-2"
                                            d="M5 13l2 2c2.76-2.76 7.24-2.76 10 0l2-2C15.14 9.14 8.87 9.14 5 13z" />
                                        <path class="wifi-wave wave-1" d="M9 17l3 3 3-3c-1.65-1.66-4.34-1.66-6 0z" />
                                    </svg>
                                </div>
                                <span class="toggle-text">Start</span>
                            </button>

                            <!-- TMDB Continuous Generation Control -->
                            <div class="tmdb-control">
                                <div class="tmdb-info">
                                    <span class="tmdb-label">🎬 TMDB Mode</span>
                                    <span id="tmdb-status" class="generator-status status-inactive">Inactive</span>
                                </div>
                                <div class="tmdb-stats">
                                    <span id="tmdb-events-count">0 events</span>
                                    <span id="tmdb-movies-count">0 movies</span>
                                </div>
                                <button id="tmdb-toggle" class="btn-tmdb" onclick="toggleTmdb()">
                                    <span>🎬</span> <span class="tmdb-toggle-text">Start TMDB</span>
                                </button>
                            </div>

                            <!-- Export Buttons -->
                            <div class="export-buttons">
                                <button class="btn-export" onclick="exportData('json')" title="Export as JSON">
                                    <span>📄</span> JSON
                                </button>
                                <button class="btn-export" onclick="exportData('csv')" title="Export as CSV">
                                    <span>📊</span> CSV
                                </button>
                            </div>
                        </div>
                    </header>

                    <!-- Error Message -->
                    <c:if test="${not empty error}">
                        <div class="alert alert-error">
                            <strong>Error:</strong> ${error}
                        </div>
                    </c:if>

                    <!-- Summary Cards -->
                    <section class="summary-cards">
                        <div class="card summary-card">
                            <div class="card-icon">📈</div>
                            <div class="card-content">
                                <h3>Total Events</h3>
                                <p class="big-number">
                                    <fmt:formatNumber value="${totalEvents}" groupingUsed="true" />
                                </p>
                            </div>
                        </div>
                        <div class="card summary-card">
                            <div class="card-icon">🎬</div>
                            <div class="card-content">
                                <h3>Total Videos</h3>
                                <p class="big-number">
                                    <fmt:formatNumber value="${summary.totalVideos}" groupingUsed="true" />
                                </p>
                            </div>
                        </div>
                        <div class="card summary-card">
                            <div class="card-icon">📂</div>
                            <div class="card-content">
                                <h3>Categories</h3>
                                <p class="big-number">${categoryStats.size()}</p>
                            </div>
                        </div>
                        <div class="card summary-card">
                            <div class="card-icon">🔥</div>
                            <div class="card-content">
                                <h3>Trending</h3>
                                <p class="big-number">${trendingVideos.size()}</p>
                            </div>
                        </div>
                    </section>

                    <!-- Charts Section -->
                    <section class="section charts-section">
                        <h2>📊 Analytics Charts</h2>
                        <div class="charts-grid">
                            <!-- Category Views Bar Chart -->
                            <div class="card chart-card">
                                <h3>📈 Views by Category</h3>
                                <div class="chart-container">
                                    <canvas id="categoryChart"></canvas>
                                </div>
                            </div>

                            <!-- Device Distribution Doughnut Chart -->
                            <div class="card chart-card">
                                <h3>📱 Device Distribution</h3>
                                <div class="chart-container">
                                    <canvas id="deviceChart"></canvas>
                                </div>
                            </div>

                            <!-- Quality Distribution Chart -->
                            <div class="card chart-card">
                                <h3>📺 Video Quality Preference</h3>
                                <div class="chart-container">
                                    <canvas id="qualityChart"></canvas>
                                </div>
                            </div>

                            <!-- Action Breakdown Chart -->
                            <div class="card chart-card">
                                <h3>🎬 User Actions</h3>
                                <div class="chart-container">
                                    <canvas id="actionChart"></canvas>
                                </div>
                            </div>

                            <!-- Hourly Events Line Chart -->
                            <div class="card chart-card chart-wide">
                                <h3>⏰ Hourly Event Trend (Last 24h)</h3>
                                <div class="chart-container-wide">
                                    <canvas id="hourlyChart"></canvas>
                                </div>
                            </div>

                            <!-- Engagement by Category -->
                            <div class="card chart-card chart-wide">
                                <h3>⏱️ Average Watch Duration by Category (Engagement)</h3>
                                <div class="chart-container-wide">
                                    <canvas id="engagementChart"></canvas>
                                </div>
                            </div>
                        </div>
                    </section>

                    <!-- Top Videos Section -->
                    <section class="section">
                        <div class="card">
                            <h2>🔥 Top 10 Videos</h2>
                            <c:choose>
                                <c:when test="${not empty topVideos}">
                                    <table class="data-table">
                                        <thead>
                                            <tr>
                                                <th>#</th>
                                                <th>Video ID</th>
                                                <th>Total Views</th>
                                                <th>Avg Duration</th>
                                                <th>Last Updated</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach items="${topVideos}" var="video" varStatus="status">
                                                <tr>
                                                    <td><span class="rank">${status.index + 1}</span></td>
                                                    <td><code>${video.videoId}</code></td>
                                                    <td class="number">
                                                        <fmt:formatNumber value="${video.totalViews}"
                                                            groupingUsed="true" />
                                                    </td>
                                                    <td class="number">
                                                        <fmt:formatNumber value="${video.avgDuration}"
                                                            maxFractionDigits="1" />s
                                                    </td>
                                                    <td class="timestamp">${video.lastUpdated}</td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </c:when>
                                <c:otherwise>
                                    <p class="no-data">No video statistics available yet.</p>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </section>

                    <!-- Category Statistics -->
                    <section class="section">
                        <div class="card">
                            <h2>📂 Statistics by Category</h2>
                            <c:choose>
                                <c:when test="${not empty categoryStats}">
                                    <div class="category-grid">
                                        <c:forEach items="${categoryStats}" var="entry">
                                            <div class="category-card">
                                                <h4>${entry.key}</h4>
                                                <div class="category-stats">
                                                    <div class="stat">
                                                        <span class="stat-value">
                                                            <fmt:formatNumber value="${entry.value.totalViews}"
                                                                groupingUsed="true" />
                                                        </span>
                                                        <span class="stat-label">Views</span>
                                                    </div>
                                                    <div class="stat">
                                                        <span class="stat-value">${entry.value.videoCount}</span>
                                                        <span class="stat-label">Videos</span>
                                                    </div>
                                                    <div class="stat">
                                                        <span class="stat-value">
                                                            <fmt:formatNumber value="${entry.value.avgDuration}"
                                                                maxFractionDigits="0" />s
                                                        </span>
                                                        <span class="stat-label">Avg Duration</span>
                                                    </div>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <p class="no-data">No category statistics available yet.</p>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </section>

                    <!-- Trending Videos -->
                    <section class="section">
                        <div class="card">
                            <h2>📈 Trending Videos (24h)</h2>
                            <c:choose>
                                <c:when test="${not empty trendingVideos}">
                                    <table class="data-table">
                                        <thead>
                                            <tr>
                                                <th>Video</th>
                                                <th>Views (24h)</th>
                                                <th>Trend Score</th>
                                                <th>Total Views</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach items="${trendingVideos}" var="trend">
                                                <tr>
                                                    <td>
                                                        <strong>${trend.video.title}</strong>
                                                        <br><small>${trend.video.category}</small>
                                                    </td>
                                                    <td class="number">
                                                        <fmt:formatNumber value="${trend.views24h}"
                                                            groupingUsed="true" />
                                                    </td>
                                                    <td class="number trend-score">
                                                        <fmt:formatNumber value="${trend.trendScore}"
                                                            maxFractionDigits="2" />x
                                                    </td>
                                                    <td class="number">
                                                        <fmt:formatNumber value="${trend.totalViews}"
                                                            groupingUsed="true" />
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </c:when>
                                <c:otherwise>
                                    <p class="no-data">No trending videos detected.</p>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </section>

                    <!-- Real-time Section -->
                    <section class="section">
                        <div class="card">
                            <h2>⚡ Real-time Events</h2>
                            <div id="event-stream" class="event-stream">
                                <p class="stream-status">Connecting to event stream...</p>
                            </div>
                        </div>
                    </section>

                    <!-- Footer -->
                    <footer class="footer-actions">
                        <div>
                            <p>🚀 Streaming Analytics Platform - Big Data TP</p>
                            <p>Last refreshed: <span id="refresh-time"></span></p>
                        </div>
                        <button onclick="location.reload()" class="btn btn-refresh">🔄 Refresh Dashboard</button>
                    </footer>
                </div>

                <script>
                    // Display current time
                    document.getElementById('refresh-time').textContent = new Date().toLocaleString();

                    // Auto-refresh every 60 seconds
                    setTimeout(function () {
                        location.reload();
                    }, 60000);

                    // ========== CHART.JS CHARTS ==========

                    // Color palette - Blue to Purple Gradients
                    const colors = {
                        primary: 'rgba(59, 130, 246, 0.8)',      // Blue
                        secondary: 'rgba(139, 92, 246, 0.8)',    // Purple
                        // Function to create gradient for canvas charts
                        createGradient: function (ctx, startColor, endColor) {
                            const gradient = ctx.createLinearGradient(0, 0, 0, 400);
                            gradient.addColorStop(0, startColor);
                            gradient.addColorStop(1, endColor);
                            return gradient;
                        },
                        // Blue to purple gradient palette
                        palette: [
                            'rgba(59, 130, 246, 0.8)',   // Blue
                            'rgba(79, 120, 246, 0.8)',   // Blue-Indigo
                            'rgba(99, 110, 246, 0.8)',   // Indigo
                            'rgba(119, 100, 246, 0.8)', // Indigo-Violet
                            'rgba(139, 92, 246, 0.8)',  // Violet
                            'rgba(159, 82, 246, 0.8)',  // Violet-Purple
                            'rgba(168, 85, 247, 0.8)',  // Purple
                            'rgba(192, 75, 247, 0.8)'   // Purple-Magenta
                        ],
                        // Solid versions for borders
                        paletteSolid: [
                            'rgba(59, 130, 246, 1)',
                            'rgba(79, 120, 246, 1)',
                            'rgba(99, 110, 246, 1)',
                            'rgba(119, 100, 246, 1)',
                            'rgba(139, 92, 246, 1)',
                            'rgba(159, 82, 246, 1)',
                            'rgba(168, 85, 247, 1)',
                            'rgba(192, 75, 247, 1)'
                        ]
                    };

                    // 1. Category Views Bar Chart
                    const categoryLabels = [];
                    const categoryData = [];
                    <c:forEach items="${categoryStats}" var="entry">
                        categoryLabels.push('${entry.key}');
                        categoryData.push(${entry.value.totalViews});
                    </c:forEach>

                    if (categoryLabels.length > 0) {
                        new Chart(document.getElementById('categoryChart'), {
                            type: 'bar',
                            data: {
                                labels: categoryLabels,
                                datasets: [{
                                    label: 'Total Views',
                                    data: categoryData,
                                    backgroundColor: colors.palette,
                                    borderRadius: 8
                                }]
                            },
                            options: {
                                responsive: true,
                                maintainAspectRatio: false,
                                plugins: {
                                    legend: { display: false }
                                },
                                scales: {
                                    y: {
                                        beginAtZero: true,
                                        grid: { color: 'rgba(255,255,255,0.1)' },
                                        ticks: { color: '#94a3b8' }
                                    },
                                    x: {
                                        grid: { display: false },
                                        ticks: { color: '#94a3b8' }
                                    }
                                }
                            }
                        });
                    }

                    // 2. Device Distribution Doughnut Chart
                    const deviceLabels = [];
                    const deviceData = [];
                    <c:forEach items="${deviceStats}" var="entry">
                        deviceLabels.push('${entry.key}');
                        deviceData.push(${entry.value});
                    </c:forEach>

                    if (deviceLabels.length > 0) {
                        new Chart(document.getElementById('deviceChart'), {
                            type: 'doughnut',
                            data: {
                                labels: deviceLabels,
                                datasets: [{
                                    data: deviceData,
                                    backgroundColor: colors.palette,
                                    borderWidth: 2,
                                    borderColor: '#1e293b'
                                }]
                            },
                            options: {
                                responsive: true,
                                maintainAspectRatio: false,
                                plugins: {
                                    legend: {
                                        position: 'right',
                                        labels: { color: '#94a3b8', padding: 12 }
                                    }
                                }
                            }
                        });
                    }

                    // 3. Hourly Events Line Chart
                    const hourLabels = [];
                    const hourData = [];
                    <c:forEach items="${hourlyStats}" var="entry">
                        hourLabels.push('${entry.key}');
                        hourData.push(${entry.value});
                    </c:forEach>

                    if (hourLabels.length > 0) {
                        new Chart(document.getElementById('hourlyChart'), {
                            type: 'line',
                            data: {
                                labels: hourLabels,
                                datasets: [{
                                    label: 'Events per Hour',
                                    data: hourData,
                                    borderColor: colors.primary,
                                    backgroundColor: 'rgba(99, 102, 241, 0.1)',
                                    fill: true,
                                    tension: 0.4,
                                    pointRadius: 4,
                                    pointBackgroundColor: colors.primary
                                }]
                            },
                            options: {
                                responsive: true,
                                maintainAspectRatio: false,
                                plugins: {
                                    legend: { display: false }
                                },
                                scales: {
                                    y: {
                                        beginAtZero: true,
                                        grid: { color: 'rgba(255,255,255,0.1)' },
                                        ticks: { color: '#94a3b8' }
                                    },
                                    x: {
                                        grid: { display: false },
                                        ticks: { color: '#94a3b8', maxRotation: 45 }
                                    }
                                }
                            }
                        });
                    }

                    // 4. Quality Distribution Chart (Polar Area)
                    const qualityLabels = [];
                    const qualityData = [];
                    <c:forEach items="${qualityStats}" var="entry">
                        qualityLabels.push('${entry.key}');
                        qualityData.push(${entry.value});
                    </c:forEach>

                    if (qualityLabels.length > 0) {
                        new Chart(document.getElementById('qualityChart'), {
                            type: 'polarArea',
                            data: {
                                labels: qualityLabels,
                                datasets: [{
                                    data: qualityData,
                                    backgroundColor: colors.palette.slice(0, 5),
                                    borderColor: colors.paletteSolid.slice(0, 5),
                                    borderWidth: 2
                                }]
                            },
                            options: {
                                responsive: true,
                                maintainAspectRatio: false,
                                plugins: {
                                    legend: {
                                        position: 'right',
                                        labels: { color: '#94a3b8', padding: 10 }
                                    }
                                }
                            }
                        });
                    }

                    // 5. User Actions Chart (Horizontal Bar)
                    const actionLabels = [];
                    const actionData = [];
                    <c:forEach items="${actionStats}" var="entry">
                        actionLabels.push('${entry.key}');
                        actionData.push(${entry.value});
                    </c:forEach>

                    if (actionLabels.length > 0) {
                        new Chart(document.getElementById('actionChart'), {
                            type: 'bar',
                            data: {
                                labels: actionLabels,
                                datasets: [{
                                    label: 'Event Count',
                                    data: actionData,
                                    backgroundColor: colors.palette.slice(0, 5),
                                    borderColor: colors.paletteSolid.slice(0, 5),
                                    borderWidth: 1,
                                    borderRadius: 6
                                }]
                            },
                            options: {
                                indexAxis: 'y',
                                responsive: true,
                                maintainAspectRatio: false,
                                plugins: {
                                    legend: { display: false }
                                },
                                scales: {
                                    x: {
                                        beginAtZero: true,
                                        grid: { color: 'rgba(255,255,255,0.1)' },
                                        ticks: { color: '#94a3b8' }
                                    },
                                    y: {
                                        grid: { display: false },
                                        ticks: { color: '#94a3b8' }
                                    }
                                }
                            }
                        });
                    }

                    // 6. Engagement by Category Chart (Bar - Avg Duration in seconds)
                    const engagementLabels = [];
                    const engagementData = [];
                    <c:forEach items="${avgDurationStats}" var="entry">
                        engagementLabels.push('${entry.key}');
                        engagementData.push(${entry.value});
                    </c:forEach>

                    if (engagementLabels.length > 0) {
                        new Chart(document.getElementById('engagementChart'), {
                            type: 'bar',
                            data: {
                                labels: engagementLabels,
                                datasets: [{
                                    label: 'Avg Watch Duration (seconds)',
                                    data: engagementData,
                                    backgroundColor: colors.palette,
                                    borderColor: colors.paletteSolid,
                                    borderWidth: 1,
                                    borderRadius: 6
                                }]
                            },
                            options: {
                                responsive: true,
                                maintainAspectRatio: false,
                                plugins: {
                                    legend: { display: false }
                                },
                                scales: {
                                    y: {
                                        beginAtZero: true,
                                        grid: { color: 'rgba(255,255,255,0.1)' },
                                        ticks: {
                                            color: '#94a3b8',
                                            callback: function (value) { return value + 's'; }
                                        }
                                    },
                                    x: {
                                        grid: { display: false },
                                        ticks: { color: '#94a3b8' }
                                    }
                                }
                            }
                        });
                    }

                    // ========== WEBSOCKET REAL-TIME EVENTS ==========
                    let websocket = null;
                    let wsReconnectTimer = null;

                    function connectWebSocket() {
                        const eventDiv = document.getElementById('event-stream');
                        let eventCount = 0;

                        // Build WebSocket URL
                        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
                        const wsUrl = protocol + '//' + window.location.host + '${pageContext.request.contextPath}/ws/realtime';

                        try {
                            websocket = new WebSocket(wsUrl);

                            websocket.onopen = function (e) {
                                console.log('WebSocket connected');
                                eventDiv.innerHTML = '<p class="stream-status connected">🔌 WebSocket connected - Real-time mode</p>';

                                // Clear any pending reconnect timer
                                if (wsReconnectTimer) {
                                    clearTimeout(wsReconnectTimer);
                                    wsReconnectTimer = null;
                                }
                            };

                            websocket.onmessage = function (e) {
                                try {
                                    const message = JSON.parse(e.data);

                                    if (message.type === 'event') {
                                        const data = message.data;
                                        eventCount++;

                                        const eventHtml = '<div class="event-item">' +
                                            '<span class="event-time">' + new Date().toLocaleTimeString() + '</span> ' +
                                            '<span class="event-id">' + (data.eventId || 'N/A') + '</span> - ' +
                                            '<span class="event-action">' + (data.action || 'N/A') + '</span> - ' +
                                            'User: <span class="event-user">' + (data.userId || 'N/A') + '</span> - ' +
                                            'Video: <span class="event-video">' + (data.videoId || 'N/A') + '</span>' +
                                            '</div>';

                                        const status = eventDiv.querySelector('.stream-status');
                                        if (status) status.remove();

                                        const events = eventDiv.querySelectorAll('.event-item');
                                        if (events.length >= 10) {
                                            events[events.length - 1].remove();
                                        }
                                        eventDiv.insertAdjacentHTML('afterbegin', eventHtml);
                                    } else if (message.type === 'connected') {
                                        console.log('WebSocket welcome:', message.message);
                                    }
                                } catch (err) {
                                    console.log('Error parsing WebSocket message:', err);
                                }
                            };

                            websocket.onclose = function (e) {
                                console.log('WebSocket closed:', e.reason);
                                if (eventCount === 0) {
                                    eventDiv.innerHTML = '<p class="stream-status">⏸️ WebSocket disconnected - Reconnecting...</p>';
                                }

                                // Attempt to reconnect after 3 seconds
                                wsReconnectTimer = setTimeout(function () {
                                    console.log('Attempting WebSocket reconnection...');
                                    connectWebSocket();
                                }, 3000);
                            };

                            websocket.onerror = function (e) {
                                console.log('WebSocket error');
                            };

                        } catch (e) {
                            console.error('WebSocket connection error:', e);
                            eventDiv.innerHTML = '<p class="stream-status">⏸️ Could not connect to WebSocket</p>';

                            // Fallback to SSE
                            connectEventStreamSSE();
                        }
                    }

                    // SSE Fallback for browsers or when WebSocket fails
                    function connectEventStreamSSE() {
                        const eventDiv = document.getElementById('event-stream');
                        let eventCount = 0;

                        if (typeof (EventSource) !== "undefined") {
                            try {
                                const source = new EventSource('${pageContext.request.contextPath}/api/v1/analytics/realtime/stream');

                                source.addEventListener('connected', function (e) {
                                    eventDiv.innerHTML = '<p class="stream-status connected">✅ Connected (SSE fallback)</p>';
                                });

                                source.addEventListener('event', function (e) {
                                    const data = JSON.parse(e.data);
                                    eventCount++;

                                    const eventHtml = '<div class="event-item">' +
                                        '<span class="event-time">' + new Date().toLocaleTimeString() + '</span> ' +
                                        '<span class="event-id">' + (data.eventId || 'N/A') + '</span> - ' +
                                        '<span class="event-action">' + (data.action || 'N/A') + '</span> - ' +
                                        'User: <span class="event-user">' + (data.userId || 'N/A') + '</span> - ' +
                                        'Video: <span class="event-video">' + (data.videoId || 'N/A') + '</span>' +
                                        '</div>';

                                    const status = eventDiv.querySelector('.stream-status');
                                    if (status) status.remove();

                                    const events = eventDiv.querySelectorAll('.event-item');
                                    if (events.length >= 10) {
                                        events[events.length - 1].remove();
                                    }
                                    eventDiv.insertAdjacentHTML('afterbegin', eventHtml);
                                });

                                source.onerror = function () {
                                    if (eventCount === 0) {
                                        eventDiv.innerHTML = '<p class="stream-status">⏸️ Stream paused - Refresh to reconnect</p>';
                                    }
                                    source.close();
                                };
                            } catch (e) {
                                eventDiv.innerHTML = '<p class="stream-status">⏸️ Could not connect to stream</p>';
                            }
                        }
                    }

                    // Connect using WebSocket (primary) with SSE fallback
                    connectWebSocket();

                    // ========== CONTINUOUS GENERATOR CONTROL ==========
                    let generatorRunning = false;
                    let statusPollInterval = null;

                    // Check initial generator status
                    checkGeneratorStatus();

                    async function checkGeneratorStatus() {
                        try {
                            const response = await fetch('${pageContext.request.contextPath}/api/v1/generator/status');
                            if (response.ok) {
                                const data = await response.json();
                                updateGeneratorUI(data.running, data.eventsGenerated);
                            }
                        } catch (error) {
                            console.log('Could not check generator status:', error);
                        }
                    }

                    async function toggleGenerator() {
                        const button = document.getElementById('generator-toggle');
                        button.disabled = true;
                        button.classList.add('loading');

                        try {
                            const response = await fetch('${pageContext.request.contextPath}/api/v1/generator/toggle', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' }
                            });

                            if (response.ok) {
                                const data = await response.json();
                                updateGeneratorUI(data.running, data.eventsGenerated || 0);

                                // Start or stop polling based on state
                                if (data.running) {
                                    startStatusPolling();
                                } else {
                                    stopStatusPolling();
                                }
                            } else {
                                console.error('Failed to toggle generator');
                            }
                        } catch (error) {
                            console.error('Error toggling generator:', error);
                        } finally {
                            button.disabled = false;
                            button.classList.remove('loading');
                        }
                    }

                    function updateGeneratorUI(running, eventsCount) {
                        generatorRunning = running;

                        const button = document.getElementById('generator-toggle');
                        const statusSpan = document.getElementById('generator-status');
                        const toggleText = button.querySelector('.toggle-text');
                        const eventsSpan = document.getElementById('events-count');

                        if (running) {
                            button.classList.add('active');
                            statusSpan.textContent = 'Active';
                            statusSpan.className = 'generator-status status-active';
                            toggleText.textContent = 'Stop';
                        } else {
                            button.classList.remove('active');
                            statusSpan.textContent = 'Inactive';
                            statusSpan.className = 'generator-status status-inactive';
                            toggleText.textContent = 'Start';
                        }

                        eventsSpan.textContent = eventsCount.toLocaleString() + ' events generated';
                    }

                    function startStatusPolling() {
                        if (statusPollInterval) return;

                        statusPollInterval = setInterval(async () => {
                            try {
                                const response = await fetch('${pageContext.request.contextPath}/api/v1/generator/status');
                                if (response.ok) {
                                    const data = await response.json();
                                    updateGeneratorUI(data.running, data.eventsGenerated);

                                    if (!data.running) {
                                        stopStatusPolling();
                                    }
                                }
                            } catch (error) {
                                console.log('Error polling status:', error);
                            }
                        }, 2000);
                    }

                    function stopStatusPolling() {
                        if (statusPollInterval) {
                            clearInterval(statusPollInterval);
                            statusPollInterval = null;
                        }
                    }

                    // Initialize polling if already running
                    checkGeneratorStatus().then(() => {
                        if (generatorRunning) {
                            startStatusPolling();
                        }
                    });

                    // ========== RATE SLIDER CONTROL ==========
                    const rateSlider = document.getElementById('rate-slider');
                    const rateValue = document.getElementById('rate-value');

                    rateSlider.addEventListener('input', function () {
                        rateValue.textContent = this.value + ' evt/s';
                    });

                    // Get rate from slider for toggle
                    function getSelectedRate() {
                        return document.getElementById('rate-slider').value;
                    }

                    // Override toggle to include rate
                    async function toggleGenerator() {
                        const button = document.getElementById('generator-toggle');
                        button.disabled = true;
                        button.classList.add('loading');

                        const rate = getSelectedRate();

                        try {
                            const response = await fetch('${pageContext.request.contextPath}/api/v1/generator/toggle?rate=' + rate, {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' }
                            });

                            if (response.ok) {
                                const data = await response.json();
                                updateGeneratorUI(data.running, data.eventsGenerated || 0);

                                if (data.running) {
                                    startStatusPolling();
                                    startLiveUserPolling();
                                } else {
                                    stopStatusPolling();
                                }
                            } else {
                                console.error('Failed to toggle generator');
                            }
                        } catch (error) {
                            console.error('Error toggling generator:', error);
                        } finally {
                            button.disabled = false;
                            button.classList.remove('loading');
                        }
                    }

                    // ========== EXPORT DATA ==========
                    function exportData(format) {
                        const baseUrl = '${pageContext.request.contextPath}/api/v1/export/summary?format=' + format;
                        window.open(baseUrl, '_blank');
                    }

                    // ========== TMDB CONTINUOUS GENERATOR CONTROL ==========
                    let tmdbRunning = false;
                    let tmdbStatusPollInterval = null;

                    // Check initial TMDB status
                    checkTmdbStatus();

                    async function checkTmdbStatus() {
                        try {
                            const response = await fetch('${pageContext.request.contextPath}/api/v1/generator/tmdb/status');
                            if (response.ok) {
                                const data = await response.json();
                                updateTmdbUI(data.running, data.eventsGenerated, data.moviesLoaded);
                            }
                        } catch (error) {
                            console.log('Could not check TMDB status:', error);
                        }
                    }

                    async function toggleTmdb() {
                        const button = document.getElementById('tmdb-toggle');
                        button.disabled = true;
                        button.classList.add('loading');

                        const rate = getSelectedRate();

                        try {
                            const response = await fetch('${pageContext.request.contextPath}/api/v1/generator/tmdb/toggle?rate=' + rate, {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' }
                            });

                            if (response.ok) {
                                const data = await response.json();
                                updateTmdbUI(data.running, data.eventsGenerated || 0, data.moviesLoaded || 0);

                                if (data.running) {
                                    startTmdbStatusPolling();
                                } else {
                                    stopTmdbStatusPolling();
                                }
                            } else {
                                const error = await response.json();
                                alert('TMDB Error: ' + (error.error || 'Unknown error'));
                            }
                        } catch (error) {
                            console.error('Error toggling TMDB:', error);
                            alert('Failed to connect to TMDB API');
                        } finally {
                            button.disabled = false;
                            button.classList.remove('loading');
                        }
                    }

                    function updateTmdbUI(running, eventsCount, moviesCount) {
                        tmdbRunning = running;

                        const button = document.getElementById('tmdb-toggle');
                        const statusSpan = document.getElementById('tmdb-status');
                        const toggleText = button.querySelector('.tmdb-toggle-text');
                        const eventsSpan = document.getElementById('tmdb-events-count');
                        const moviesSpan = document.getElementById('tmdb-movies-count');

                        if (running) {
                            button.classList.add('active');
                            statusSpan.textContent = 'Active';
                            statusSpan.className = 'generator-status status-active';
                            toggleText.textContent = 'Stop TMDB';
                        } else {
                            button.classList.remove('active');
                            statusSpan.textContent = 'Inactive';
                            statusSpan.className = 'generator-status status-inactive';
                            toggleText.textContent = 'Start TMDB';
                        }

                        eventsSpan.textContent = (eventsCount || 0).toLocaleString() + ' events';
                        moviesSpan.textContent = (moviesCount || 0) + ' movies';
                    }

                    function startTmdbStatusPolling() {
                        if (tmdbStatusPollInterval) return;

                        tmdbStatusPollInterval = setInterval(async () => {
                            try {
                                const response = await fetch('${pageContext.request.contextPath}/api/v1/generator/tmdb/status');
                                if (response.ok) {
                                    const data = await response.json();
                                    updateTmdbUI(data.running, data.eventsGenerated, data.moviesLoaded);

                                    if (!data.running) {
                                        stopTmdbStatusPolling();
                                    }
                                }
                            } catch (error) {
                                console.log('Error polling TMDB status:', error);
                            }
                        }, 2000);
                    }

                    function stopTmdbStatusPolling() {
                        if (tmdbStatusPollInterval) {
                            clearInterval(tmdbStatusPollInterval);
                            tmdbStatusPollInterval = null;
                        }
                    }

                    // Initialize TMDB polling if already running
                    checkTmdbStatus().then(() => {
                        if (tmdbRunning) {
                            startTmdbStatusPolling();
                        }
                    });

                    // ========== LIVE USER COUNT ==========
                    let liveUserInterval = null;

                    async function fetchLiveUserCount() {
                        try {
                            const response = await fetch('${pageContext.request.contextPath}/api/v1/export/live-users');
                            if (response.ok) {
                                const data = await response.json();
                                document.getElementById('live-user-count').textContent = data.activeUsers.toLocaleString();
                            }
                        } catch (error) {
                            console.log('Error fetching live users:', error);
                        }
                    }

                    function startLiveUserPolling() {
                        if (liveUserInterval) return;
                        fetchLiveUserCount();
                        liveUserInterval = setInterval(fetchLiveUserCount, 5000);
                    }

                    function stopLiveUserPolling() {
                        if (liveUserInterval) {
                            clearInterval(liveUserInterval);
                            liveUserInterval = null;
                        }
                    }

                    // Start live user polling on page load
                    startLiveUserPolling();
                </script>
            </body>

            </html>