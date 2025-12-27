// MongoDB initialization script
// Automatically executed when MongoDB starts

// Create database
db = db.getSiblingDB('streaming_analytics');

// Create collections
db.createCollection('events');
db.createCollection('video_stats');
db.createCollection('user_profiles');
db.createCollection('videos');

// Create indexes for performance
print('📊 Creating indexes...');

// Indexes on events
db.events.createIndex({ "userId": 1 });
db.events.createIndex({ "videoId": 1 });
db.events.createIndex({ "timestamp": -1 });
db.events.createIndex({ "userId": 1, "timestamp": -1 });
db.events.createIndex({ "videoId": 1, "timestamp": -1 });

// Indexes on video_stats
db.video_stats.createIndex({ "videoId": 1 }, { unique: true });
db.video_stats.createIndex({ "totalViews": -1 });
db.video_stats.createIndex({ "lastUpdated": -1 });

// Indexes on user_profiles
db.user_profiles.createIndex({ "userId": 1 }, { unique: true });

// Indexes on videos
db.videos.createIndex({ "videoId": 1 }, { unique: true });
db.videos.createIndex({ "category": 1 });
db.videos.createIndex({ "views": -1 });

print('✅ Indexes created successfully');

// Insert test data
print('📝 Inserting test data...');

db.videos.insertMany([
    {
        videoId: "video_1",
        title: "Introduction to Big Data",
        category: "Documentary",
        duration: 1800,
        uploadDate: new Date(),
        views: 15000,
        likes: 1200
    },
    {
        videoId: "video_2",
        title: "Jakarta EE in Action",
        category: "Educational",
        duration: 2400,
        uploadDate: new Date(),
        views: 8500,
        likes: 750
    },
    {
        videoId: "video_3",
        title: "MongoDB for Beginners",
        category: "Educational",
        duration: 1500,
        uploadDate: new Date(),
        views: 12000,
        likes: 950
    }
]);

db.video_stats.insertMany([
    {
        videoId: "video_1",
        totalViews: 15000,
        avgDuration: 1350.5,
        uniqueViewers: 12500,
        lastUpdated: new Date()
    },
    {
        videoId: "video_2",
        totalViews: 8500,
        avgDuration: 1800.2,
        uniqueViewers: 7200,
        lastUpdated: new Date()
    },
    {
        videoId: "video_3",
        totalViews: 12000,
        avgDuration: 980.7,
        uniqueViewers: 9800,
        lastUpdated: new Date()
    }
]);

print('✅ Test data inserted');

// Create application user
db.createUser({
    user: "streaming_app",
    pwd: "app_password_123",
    roles: [
        { role: "readWrite", db: "streaming_analytics" }
    ]
});

print('✅ Application user created');
print('✅ MongoDB initialization complete!');
