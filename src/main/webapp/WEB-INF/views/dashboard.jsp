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

                    // Color palette
                    const colors = {
                        primary: 'rgba(99, 102, 241, 0.8)',
                        secondary: 'rgba(168, 85, 247, 0.8)',
                        success: 'rgba(34, 197, 94, 0.8)',
                        warning: 'rgba(251, 191, 36, 0.8)',
                        danger: 'rgba(239, 68, 68, 0.8)',
                        info: 'rgba(59, 130, 246, 0.8)',
                        palette: [
                            'rgba(99, 102, 241, 0.8)',
                            'rgba(168, 85, 247, 0.8)',
                            'rgba(236, 72, 153, 0.8)',
                            'rgba(239, 68, 68, 0.8)',
                            'rgba(251, 191, 36, 0.8)',
                            'rgba(34, 197, 94, 0.8)',
                            'rgba(59, 130, 246, 0.8)',
                            'rgba(20, 184, 166, 0.8)'
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
                                    backgroundColor: [
                                        'rgba(239, 68, 68, 0.7)',   // 360p - Red (low)
                                        'rgba(251, 191, 36, 0.7)', // 480p - Yellow
                                        'rgba(34, 197, 94, 0.7)',  // 720p - Green
                                        'rgba(59, 130, 246, 0.7)', // 1080p - Blue
                                        'rgba(168, 85, 247, 0.7)'  // 4K - Purple (best)
                                    ]
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
                                    backgroundColor: [
                                        'rgba(34, 197, 94, 0.8)',   // WATCH - Green
                                        'rgba(251, 191, 36, 0.8)', // PAUSE - Yellow
                                        'rgba(239, 68, 68, 0.8)',  // STOP - Red
                                        'rgba(59, 130, 246, 0.8)', // RESUME - Blue
                                        'rgba(168, 85, 247, 0.8)'  // SEEK - Purple
                                    ],
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
                                    backgroundColor: 'rgba(16, 185, 129, 0.8)',
                                    borderColor: 'rgba(16, 185, 129, 1)',
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

                    // ========== SSE REAL-TIME EVENTS ==========
                    function connectEventStream() {
                        const eventDiv = document.getElementById('event-stream');
                        let eventCount = 0;

                        if (typeof (EventSource) !== "undefined") {
                            try {
                                const source = new EventSource('${pageContext.request.contextPath}/api/v1/analytics/realtime/stream');

                                source.addEventListener('connected', function (e) {
                                    eventDiv.innerHTML = '<p class="stream-status connected">✅ Connected to real-time stream</p>';
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

                                source.addEventListener('heartbeat', function (e) {
                                    console.log('SSE heartbeat received');
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
                        } else {
                            eventDiv.innerHTML = '<p class="stream-status">⚠️ Browser does not support SSE</p>';
                        }
                    }

                    connectEventStream();
                </script>
            </body>

            </html>