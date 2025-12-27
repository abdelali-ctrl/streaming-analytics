# Big Data Streaming Analytics Platform - Implementation Guide

## 📋 Project Overview

A real-time video streaming analytics platform built with **Jakarta EE 9.1**, **MongoDB**, and **Apache Tomcat**. The system processes viewing events, calculates statistics, and displays real-time analytics on a dashboard.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Browser                            │
│                     (Dashboard + SSE)                            │
└─────────────────────┬───────────────────────────────────────────┘
                      │ HTTP / SSE
┌─────────────────────▼───────────────────────────────────────────┐
│                    Tomcat 10 (Jakarta EE)                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐   │
│  │ Dashboard    │  │ REST API     │  │ RealtimeEventServlet │   │
│  │ Servlet      │  │ Resources    │  │ (SSE Stream)         │   │
│  └──────┬───────┘  └──────┬───────┘  └──────────┬───────────┘   │
│         │                  │                     │               │
│  ┌──────▼──────────────────▼─────────────────────▼──────────┐   │
│  │                    Services Layer                         │   │
│  │  (EventProcessorService, AnalyticsService)               │   │
│  └──────────────────────────┬───────────────────────────────┘   │
│                              │                                   │
│  ┌──────────────────────────▼───────────────────────────────┐   │
│  │                 Repositories (Document-Based)             │   │
│  │  (EventRepository, VideoRepository, VideoStatsRepository) │   │
│  └──────────────────────────┬───────────────────────────────┘   │
└──────────────────────────────┼───────────────────────────────────┘
                               │
┌──────────────────────────────▼───────────────────────────────────┐
│                         MongoDB 7.0                               │
│  ┌─────────┐  ┌─────────┐  ┌─────────────┐  ┌───────────────┐   │
│  │ events  │  │ videos  │  │ video_stats │  │ user_profiles │   │
│  └─────────┘  └─────────┘  └─────────────┘  └───────────────┘   │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                      DataGenerator                                │
│  Modes: batch (JSON files) | continuous (MongoDB insert)         │
└──────────────────────────────────────────────────────────────────┘
```

---

## 📁 Project Structure

```
streaming-analytics/
├── src/main/java/com/streaming/
│   ├── analytics/
│   │   ├── config/
│   │   │   ├── AnalyticsApplication.java    # JAX-RS application config
│   │   │   └── MongoClientProducer.java     # CDI MongoDB producer
│   │   ├── model/
│   │   │   ├── ViewEvent.java               # View event entity
│   │   │   ├── Video.java                   # Video metadata
│   │   │   ├── VideoStats.java              # Aggregated stats
│   │   │   └── UserProfile.java             # User profile
│   │   ├── repository/
│   │   │   ├── EventRepository.java
│   │   │   ├── VideoRepository.java
│   │   │   ├── VideoStatsRepository.java
│   │   │   └── UserProfileRepository.java
│   │   ├── service/
│   │   │   ├── EventProcessorService.java
│   │   │   └── AnalyticsService.java
│   │   ├── resource/
│   │   │   ├── EventResource.java           # REST /events endpoints
│   │   │   └── AnalyticsResource.java       # REST /analytics endpoints
│   │   └── web/
│   │       ├── DashboardServlet.java        # Dashboard controller
│   │       └── RealtimeEventServlet.java    # SSE streaming
│   └── datagenerator/
│       └── DataGenerator.java               # Test data generator
├── src/main/webapp/
│   ├── WEB-INF/views/dashboard.jsp          # Dashboard view
│   ├── META-INF/context.xml                 # Weld config for Tomcat
│   └── css/style.css                        # Dashboard styles
├── mongo-init/init.js                       # MongoDB initialization
├── docker-compose.yml
├── pom.xml
├── load-data-mongo.ps1                      # Load test data script
├── aggregate-stats.js                       # MongoDB aggregation script
└── run-generator.ps1                        # Run DataGenerator script
```

---

## 🔧 Key Components Explained

### 1. Data Models

| Model | MongoDB Collection | Purpose |
|-------|-------------------|---------|
| `ViewEvent` | `events` | User viewing events (WATCH, PAUSE, STOP, etc.) |
| `Video` | `videos` | Video metadata (title, category, duration) |
| `VideoStats` | `video_stats` | Aggregated stats per video (views, avg duration) |
| `UserProfile` | `user_profiles` | User preferences and watch history |

### 2. Repositories (Document-Based Queries)

All repositories use **Document-based MongoDB queries** instead of POJO codec:

```java
private VideoStats documentToVideoStats(Document doc) {
    VideoStats stats = new VideoStats();
    Number totalViews = (Number) doc.get("totalViews");
    stats.setTotalViews(totalViews != null ? totalViews.longValue() : 0L);
    // ... handles Integer, Long, Double automatically
    return stats;
}
```

**Why?** The POJO codec has issues with aggregated data types (Integer vs Long).

### 3. REST API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/events` | POST | Submit single event |
| `/api/v1/events/batch` | POST | Submit batch of events |
| `/api/v1/analytics/top-videos?limit=10` | GET | Top videos by views |
| `/api/v1/analytics/category-stats` | GET | Stats by category |
| `/api/v1/analytics/realtime/stream` | GET | SSE event stream |

### 4. Real-time SSE Streaming

`RealtimeEventServlet` provides Server-Sent Events:
- Polls MongoDB every 3 seconds
- Pushes new events to connected browsers
- Heartbeat keeps connection alive
- **No Kafka required!**

---

## 📊 DataGenerator Modes

### Batch Mode (Default)
Generates JSON files for bulk import:
```powershell
mvn exec:java "-Dexec.args=batch"
# Creates: events_100k.json, videos_catalog.json
```

### Continuous Mode (Real-time Dashboard)
Inserts events directly into MongoDB:
```powershell
mvn exec:java "-Dexec.args=continuous 10 60"
# 10 events/second for 60 seconds = 600 events
```

### Console Mode (Debug)
Outputs JSON to console:
```powershell
mvn exec:java "-Dexec.args=console 10 60"
```

---

## 🚀 Quick Start Guide

### 1. Start Docker Containers
```powershell
docker-compose up -d
```

### 2. Build and Deploy
```powershell
mvn clean package -DskipTests
```

### 3. Load Test Data
```powershell
# Load 100K events + 10K videos into MongoDB
powershell -ExecutionPolicy Bypass -File .\load-data-mongo.ps1

# Aggregate video stats
docker cp aggregate-stats.js streaming-mongodb:/tmp/
docker exec streaming-mongodb mongosh streaming_analytics --username admin --password admin123 --authenticationDatabase admin --eval "load('/tmp/aggregate-stats.js')"
```

### 4. Open Dashboard
```
http://localhost:8080/streaming-analytics/dashboard
```

### 5. Start Real-time Data Generation (Optional)
```powershell
# Open new terminal
mvn exec:java "-Dexec.args=continuous 10 60"
```

---

## 🎨 Dashboard Features

| Section | Description |
|---------|-------------|
| **Summary Cards** | Total events, videos, categories, trending count |
| **Top 10 Videos** | Sorted by view count with avg duration |
| **Category Stats** | Views and video count per category |
| **Trending Videos** | Videos trending in last 24h (needs historical data) |
| **Real-time Events** | SSE streaming of latest events |

---

## 🔄 Technologies Used

| Technology | Version | Purpose |
|-----------|---------|---------|
| Java | 11 | Runtime |
| Jakarta EE | 9.1 | API specification |
| MongoDB | 7.0 | Document database |
| Weld | 4.0.3 | CDI implementation |
| Jersey | 3.0.8 | JAX-RS implementation |
| Jackson | 2.15.2 | JSON processing |
| Tomcat | 10.0.27 | Servlet container |
| Docker Compose | - | Container orchestration |

---

## � Detailed File Explanations

### Configuration Files

| File | Role |
|------|------|
| **`pom.xml`** | Maven build configuration. Defines dependencies (Jakarta EE, MongoDB, Weld, Jersey, Jackson), plugins, and Java version (11). |
| **`docker-compose.yml`** | Orchestrates MongoDB and Tomcat containers. Maps ports, volumes, and sets environment variables. |
| **`beans.xml`** | CDI (Contexts and Dependency Injection) configuration. Enables CDI bean discovery. |
| **`context.xml`** | Tomcat-specific config that enables Weld CDI integration via BeanManager. |

---

### Java Configuration (`config/`)

| File | Role |
|------|------|
| **`AnalyticsApplication.java`** | JAX-RS application entry point. Registers REST API path prefix `/api/v1` and scans for resource classes. |
| **`MongoClientProducer.java`** | CDI producer that creates `MongoClient` and `MongoDatabase` beans. Reads connection settings from environment variables. |

---

### Data Models (`model/`)

| File | Role |
|------|------|
| **`ViewEvent.java`** | Represents a user viewing event (WATCH, PAUSE, STOP, etc.). Fields: eventId, userId, videoId, timestamp, action, duration, quality, deviceType. |
| **`Video.java`** | Video metadata entity. Fields: videoId, title, category, duration, uploadDate, views, likes. |
| **`VideoStats.java`** | Aggregated statistics for a video. Fields: videoId, totalViews, avgDuration, uniqueViewers, lastUpdated. |
| **`UserProfile.java`** | User profile with watch history and preferences. Fields: userId, preferredCategories, totalWatchTime. |

---

### Repositories (`repository/`)

| File | Role |
|------|------|
| **`EventRepository.java`** | Data access for `events` collection. Methods: save, findByVideoId, findByUserId, findRecent, count. |
| **`VideoRepository.java`** | Data access for `videos` collection. Methods: findByVideoId, findByCategory, findMostPopular, getAllCategories. Uses Document-based queries. |
| **`VideoStatsRepository.java`** | Data access for `video_stats` collection. Methods: getStats, updateStats, getTopVideos, findRecentlyUpdated. Uses Document-based queries. |
| **`UserProfileRepository.java`** | Data access for `user_profiles` collection. Methods: findByUserId, updateWatchHistory. |

---

### Services (`service/`)

| File | Role |
|------|------|
| **`EventProcessorService.java`** | Business logic for processing events. When an event arrives: validates it, saves to DB, updates video stats, updates user profile. |
| **`AnalyticsService.java`** | Analytics and aggregation logic. Methods: aggregateByCategory (MapReduce-like), detectTrending (compares 24h vs 7d average), getDashboardSummary. Contains inner classes: CategoryStats, TrendingVideo, DashboardSummary. |

---

### REST API Resources (`resource/`)

| File | Role |
|------|------|
| **`EventResource.java`** | REST endpoints for events. `POST /events` - submit single event. `POST /events/batch` - submit multiple events. `GET /events/recent` - get recent events. |
| **`AnalyticsResource.java`** | REST endpoints for analytics. `GET /analytics/top-videos` - top videos by views. `GET /analytics/category-stats` - stats per category. `GET /analytics/health` - health check. |

---

### Web Layer (`web/`)

| File | Role |
|------|------|
| **`DashboardServlet.java`** | MVC controller for dashboard. Fetches data from services (top videos, category stats, trending, summary), sets request attributes, forwards to JSP view. |
| **`RealtimeEventServlet.java`** | SSE (Server-Sent Events) endpoint. Polls MongoDB every 3 seconds, pushes new events to connected browsers. Enables real-time dashboard updates without page refresh. |

---

### Views & Static Files (`webapp/`)

| File | Role |
|------|------|
| **`dashboard.jsp`** | Dashboard HTML view using JSTL. Displays summary cards, top videos table, category stats grid, trending videos, real-time event stream. Contains JavaScript for SSE connection. |
| **`style.css`** | Dashboard styling. Dark theme, card layouts, responsive grid, animation effects. |

---

### Data Generator (`datagenerator/`)

| File | Role |
|------|------|
| **`DataGenerator.java`** | Test data generator with 3 modes: **batch** (generates JSON files), **continuous** (inserts into MongoDB for real-time testing), **console** (outputs to stdout). Contains inner classes ViewEvent and Video (separate from main models). |

---

### Helper Scripts

| File | Role |
|------|------|
| **`load-data-mongo.ps1`** | PowerShell script that copies JSON files to MongoDB container and uses `mongoimport` to load them. |
| **`aggregate-stats.js`** | MongoDB script that rebuilds `video_stats` collection from events using aggregation pipeline. |
| **`run-generator.ps1`** | Helper to run DataGenerator in continuous mode with configurable parameters. |

---

### MongoDB Initialization

| File | Role |
|------|------|
| **`mongo-init/init.js`** | Runs when MongoDB container first starts. Creates database, collections, indexes, and inserts sample seed data. |

---

## �📝 Key Design Decisions

1. **Document-based repositories** instead of POJO codec to handle type mismatches
2. **SSE polling** instead of Kafka for simplicity
3. **Jakarta EE 9.1** (not 10) for Tomcat 10 compatibility
4. **Weld + Jersey bundled** in WAR since Tomcat doesn't provide them
5. **DataGenerator with MongoDB mode** for real-time dashboard testing
