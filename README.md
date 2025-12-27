# 📊 Streaming Analytics Platform

<div align="center">

![Java](https://img.shields.io/badge/Java-11+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Jakarta EE](https://img.shields.io/badge/Jakarta_EE-9.1-orange?style=for-the-badge)
![MongoDB](https://img.shields.io/badge/MongoDB-7.0-47A248?style=for-the-badge&logo=mongodb&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

**A real-time Big Data analytics platform for video streaming services**

[Features](#-features) • [Architecture](#️-architecture) • [Quick Start](#-quick-start) • [API Reference](#-rest-api-endpoints) • [Dashboard](#-dashboard)

</div>

---

## 🎯 Overview

This platform provides **real-time analytics** for video streaming services, featuring event ingestion, trend detection, user recommendations, and an interactive dashboard with **Chart.js** visualizations. Built with **Jakarta EE 9.1**, **MongoDB**, and containerized with **Docker**.

### Key Highlights
- ⚡ **Real-time event streaming** with Server-Sent Events (SSE)
- 📈 **MongoDB aggregation pipelines** for MapReduce-like operations  
- 🔥 **Trending video detection** algorithm
- 🎨 **Interactive dashboard** with 6 Chart.js visualizations
- 🐳 **Fully containerized** with Docker Compose

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| **Event Ingestion** | REST API for single and batch event processing (>500 events/sec) |
| **Analytics Dashboard** | Real-time JSP dashboard with auto-refresh |
| **Category Analytics** | Views, engagement, and duration by content category |
| **Device Analytics** | Breakdown by mobile, desktop, tablet, smart TV |
| **Trending Detection** | Algorithm comparing 24h views vs 7-day average |
| **User Recommendations** | Personalized video suggestions based on watch history |
| **Quality Metrics** | Video quality preference distribution (360p to 4K) |
| **Hourly Trends** | Time-series analysis of event patterns |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        STREAMING ANALYTICS PLATFORM                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   ┌──────────────┐     ┌──────────────┐     ┌──────────────────────────┐   │
│   │    Client    │     │   REST API   │     │       MongoDB 7.0        │   │
│   │   Browser    │────▶│  (JAX-RS)    │────▶│  ┌─────────────────────┐ │   │
│   └──────────────┘     └──────────────┘     │  │ events collection   │ │   │
│                               │              │  │ videos collection   │ │   │
│   ┌──────────────┐           │              │  │ video_stats         │ │   │
│   │    Data      │           ▼              │  │ user_profiles       │ │   │
│   │  Generator   │───▶ ┌──────────────┐     │  └─────────────────────┘ │   │
│   └──────────────┘     │   Services   │     └──────────────────────────┘   │
│                        │ ┌──────────┐ │              ▲                      │
│   ┌──────────────┐     │ │Analytics │ │              │                      │
│   │  Dashboard   │◀────│ │Processor │─┼──────────────┘                      │
│   │    (JSP)     │     │ └──────────┘ │                                     │
│   └──────────────┘     └──────────────┘                                     │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Technology Stack

| Layer | Technology |
|-------|------------|
| **Backend** | Jakarta EE 9.1 (CDI, JAX-RS, Servlet) |
| **Database** | MongoDB 7.0 with aggregation pipelines |
| **Frontend** | JSP + JSTL + Chart.js 4.4 |
| **Server** | Apache Tomcat 10 |
| **Container** | Docker & Docker Compose |
| **Build** | Maven 3.8+ |

---

## 🚀 Quick Start

### Prerequisites

- ☕ **Java 11** or higher
- 📦 **Maven 3.8+**
- 🐳 **Docker & Docker Compose**

### Installation

1️⃣ **Clone the repository**
```bash
git clone https://github.com/yourusername/streaming-analytics.git
cd streaming-analytics
```

2️⃣ **Start the infrastructure** (MongoDB + Tomcat + Mongo Express)
```bash
docker-compose up -d
```

3️⃣ **Build and deploy**
```bash
mvn clean package
```
> The WAR file is automatically deployed to Tomcat via Docker volume mapping.

4️⃣ **Generate sample data** (optional)
```bash
mvn exec:java -Dexec.mainClass="com.streaming.analytics.generator.DataGenerator"
```

### 🌐 Access Points

| Service | URL |
|---------|-----|
| **Analytics Dashboard** | http://localhost:8080/streaming-analytics/dashboard |
| **API Health Check** | http://localhost:8080/streaming-analytics/api/v1/analytics/health |
| **Mongo Express** | http://localhost:8081 |

---

## 📡 REST API Endpoints

### Event Ingestion

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/analytics/events` | Ingest a single event |
| `POST` | `/api/v1/analytics/events/batch` | Ingest batch of events |

### Analytics & Statistics

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/analytics/dashboard` | Get dashboard summary |
| `GET` | `/api/v1/analytics/videos/top?limit=10` | Get top N videos by views |
| `GET` | `/api/v1/analytics/videos/trending` | Get trending videos (24h) |
| `GET` | `/api/v1/analytics/videos/{id}/stats` | Get specific video stats |
| `GET` | `/api/v1/analytics/categories` | Get category breakdown |
| `GET` | `/api/v1/analytics/users/{id}/recommendations` | Get user recommendations |

### Health & Monitoring

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/analytics/health` | API health check |

### Example: Ingest an Event

```bash
curl -X POST http://localhost:8080/streaming-analytics/api/v1/analytics/events \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "evt_12345",
    "userId": "user_1",
    "videoId": "video_42",
    "action": "WATCH",
    "duration": 245,
    "quality": "1080p",
    "deviceType": "mobile"
  }'
```

### Example: Get Top Videos

```bash
curl http://localhost:8080/streaming-analytics/api/v1/analytics/videos/top?limit=5
```

---

## 📊 Dashboard

The interactive dashboard provides real-time insights through multiple visualizations:

| Chart | Type | Description |
|-------|------|-------------|
| **Views by Category** | Bar Chart | Total views across content categories |
| **Device Distribution** | Doughnut | Mobile vs Desktop vs Tablet vs Smart TV |
| **Video Quality** | Polar Area | Quality preference distribution |
| **User Actions** | Horizontal Bar | Watch, Pause, Stop, Resume, Seek breakdown |
| **Hourly Trends** | Line Chart | Event activity over last 24 hours |
| **Engagement** | Bar Chart | Average watch duration by category |

### Dashboard Sections

- 📈 **Summary Cards** - Total events, videos, categories, trending count
- 🔥 **Top 10 Videos** - Ranked by total views
- 📂 **Category Statistics** - Detailed breakdown per category
- 📈 **Trending Videos** - Videos with abnormal growth (>50% above average)
- ⚡ **Real-time Events** - Live SSE event stream

---

## 📁 Project Structure

```
streaming-analytics/
├── 📁 src/main/java/com/streaming/analytics/
│   ├── 📁 api/                      # REST API (JAX-RS)
│   │   ├── AnalyticsApplication.java
│   │   └── AnalyticsResource.java
│   ├── 📁 config/                   # Configuration
│   │   └── MongoClientProducer.java
│   ├── 📁 model/                    # Domain entities
│   │   ├── ViewEvent.java
│   │   ├── Video.java
│   │   ├── VideoStats.java
│   │   └── UserProfile.java
│   ├── 📁 repository/               # Data access layer (CDI)
│   │   ├── EventRepository.java
│   │   ├── VideoRepository.java
│   │   ├── VideoStatsRepository.java
│   │   └── UserProfileRepository.java
│   ├── 📁 service/                  # Business logic
│   │   ├── EventProcessorService.java
│   │   └── AnalyticsService.java
│   ├── 📁 web/                      # MVC Controllers
│   │   └── DashboardServlet.java
│   └── 📁 generator/                # Test data generator
│       └── DataGenerator.java
├── 📁 src/main/webapp/
│   ├── 📁 WEB-INF/
│   │   ├── beans.xml                # CDI configuration
│   │   └── 📁 views/
│   │       └── dashboard.jsp        # Dashboard template
│   └── 📁 css/
│       └── style.css                # Dashboard styles
├── 📁 mongo-init/
│   └── init.js                      # MongoDB initialization script
├── docker-compose.yml               # Container orchestration
├── pom.xml                          # Maven build configuration
└── README.md
```

---

## 📈 Performance Benchmarks

| Metric | Target | Status |
|--------|--------|--------|
| Event Ingestion Rate | >500/sec | ✅ Achieved |
| Top Videos Query | <100ms | ✅ Achieved |
| Recommendations API | <200ms | ✅ Achieved |
| Dashboard Load Time | <2s | ✅ Achieved |

---

## 🐳 Docker Services

| Service | Image | Port | Description |
|---------|-------|------|-------------|
| `mongodb` | mongo:7.0 | 27017 | Primary database |
| `mongo-express` | mongo-express:latest | 8081 | Database admin UI |
| `tomcat` | tomcat:10.0-jdk11 | 8080 | Application server |

### Environment Variables

```yaml
MONGODB_HOST: mongodb
MONGODB_PORT: 27017
MONGODB_DATABASE: streaming_analytics
MONGODB_USERNAME: admin
MONGODB_PASSWORD: admin123
```

---

## 🧪 Testing

### Run Unit Tests
```bash
mvn test
```

### Test API Endpoints
```bash
# Health check
curl http://localhost:8080/streaming-analytics/api/v1/analytics/health

# Get recommendations for a user
curl http://localhost:8080/streaming-analytics/api/v1/analytics/users/user_1/recommendations
```

---

## 🛠️ Development

### Hot Reload
After making changes, rebuild and the WAR will be automatically deployed:
```bash
mvn clean package
```

### View Logs
```bash
docker logs -f streaming-tomcat
```

### MongoDB Shell
```bash
docker exec -it streaming-mongodb mongosh -u admin -p admin123
```

---

## 📝 License

This project was developed as part of a **Big Data Practical Work (TP)** exercise for educational purposes.

---

<div align="center">

**Built with ❤️ using Jakarta EE, MongoDB, and Docker**

</div>
