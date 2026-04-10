CREATE SCHEMA IF NOT EXISTS yt;

CREATE TABLE IF NOT EXISTS yt.videos (
    id BIGSERIAL PRIMARY KEY,
    youtube_url VARCHAR(255) NOT NULL,
    title VARCHAR(500),
    file_path VARCHAR(500) NOT NULL,
    file_size_bytes BIGINT,
    duration_seconds INTEGER,
    quality VARCHAR(50),
    is_audio_only BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_accessed_at TIMESTAMP
);

CREATE INDEX idx_videos_created_at ON yt.videos(created_at DESC);