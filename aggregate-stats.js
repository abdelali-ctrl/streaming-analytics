// MongoDB script to aggregate video stats from events
// Run with: docker exec streaming-mongodb mongosh streaming_analytics --eval "load('/tmp/aggregate-stats.js')"

// Connect to the database
db = db.getSiblingDB('streaming_analytics');

print("=== AGGREGATING VIDEO STATISTICS ===");
print("");

// 1. Drop existing video_stats and rebuild from events
print("Step 1: Building video_stats from events...");

db.video_stats.drop();

db.events.aggregate([
    {
        $match: { action: "WATCH" }
    },
    {
        $group: {
            _id: "$videoId",
            totalViews: { $sum: 1 },
            avgDuration: { $avg: "$duration" },
            uniqueViewers: { $addToSet: "$userId" },
            lastUpdated: { $max: "$timestamp" }
        }
    },
    {
        $project: {
            videoId: "$_id",
            totalViews: 1,
            avgDuration: 1,
            uniqueViewers: { $size: "$uniqueViewers" },
            lastUpdated: { $ifNull: ["$lastUpdated", new Date()] }
        }
    },
    {
        $out: "video_stats"
    }
]);

var statsCount = db.video_stats.countDocuments();
print("  Created " + statsCount + " video stats entries");

// 2. Create indexes on video_stats
print("");
print("Step 2: Creating indexes...");
db.video_stats.createIndex({ "videoId": 1 });
db.video_stats.createIndex({ "totalViews": -1 });
print("  Indexes created");

// 3. Show top 5 videos
print("");
print("Step 3: Top 5 videos by views:");
db.video_stats.find().sort({ totalViews: -1 }).limit(5).forEach(function (doc) {
    print("  - " + doc.videoId + ": " + doc.totalViews + " views, avg " + Math.round(doc.avgDuration) + "s");
});

// 4. Show category breakdown
print("");
print("Step 4: Category breakdown:");
db.videos.aggregate([
    {
        $group: {
            _id: "$category",
            count: { $sum: 1 },
            totalViews: { $sum: "$views" }
        }
    },
    { $sort: { totalViews: -1 } }
]).forEach(function (doc) {
    print("  - " + doc._id + ": " + doc.count + " videos, " + doc.totalViews + " views");
});

print("");
print("=== AGGREGATION COMPLETE ===");
print("Refresh the dashboard to see updated stats.");
