CREATE DATABASE IF NOT EXISTS teste;
USE teste;

-- ================================
-- TABELA: accounts (user_accounts)
-- ================================
CREATE TABLE IF NOT EXISTS user_accounts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    profile_picture_url VARCHAR(255)
);

-- ================================
-- TABELA: channels
-- ================================
CREATE TABLE IF NOT EXISTS channels (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    subscribers BIGINT DEFAULT 0,
    profile_picture_url VARCHAR(255)
);

-- ================================
-- TABELA: user_channel
-- ================================
CREATE TABLE IF NOT EXISTS user_channel (
    user_id INT,
    channel_id INT,
    role ENUM('owner', 'editor', 'moderator', 'subscriber') NOT NULL,
    PRIMARY KEY (user_id, channel_id),
    
    CONSTRAINT fk_user_channel_user 
        FOREIGN KEY (user_id) 
        REFERENCES user_accounts(id) 
        ON DELETE CASCADE,
        
    CONSTRAINT fk_user_channel_channel 
        FOREIGN KEY (channel_id) 
        REFERENCES channels(id) 
        ON DELETE CASCADE
);

-- ================================
-- TABELA: posts
-- ================================
CREATE TABLE IF NOT EXISTS posts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    channel_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    thumbnail_url VARCHAR(255),
    likes INT DEFAULT 0,
    dislikes INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    post_type ENUM('VIDEO', 'TEXT') NOT NULL,
    
    CONSTRAINT fk_posts_channel 
        FOREIGN KEY (channel_id) 
        REFERENCES channels(id) 
        ON DELETE CASCADE
);

-- ================================
-- TABELA: videos
-- ================================
CREATE TABLE IF NOT EXISTS videos (
    post_id INT PRIMARY KEY,
    description TEXT,
    duration_seconds INT NOT NULL,
    views BIGINT DEFAULT 0,
    video_url VARCHAR(255) NOT NULL,
    video_category ENUM('LONG', 'SHORT') NOT NULL,
    
    CONSTRAINT fk_video_post
        FOREIGN KEY (post_id) 
        REFERENCES posts(id) 
        ON DELETE CASCADE
);

-- ================================
-- TABELA: text_posts
-- ================================
CREATE TABLE IF NOT EXISTS text_posts (
    post_id INT PRIMARY KEY,
    content TEXT NOT NULL,
    
    CONSTRAINT fk_text_post
        FOREIGN KEY (post_id) 
        REFERENCES posts(id) 
        ON DELETE CASCADE
);

-- ================================
-- TABELA: post_likes (Tracking)
-- ================================
CREATE TABLE IF NOT EXISTS post_likes (
    user_id INT,
    post_id INT,
    is_like BOOLEAN, -- TRUE for like, FALSE for dislike
    PRIMARY KEY (user_id, post_id),
    FOREIGN KEY (user_id) REFERENCES user_accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

-- ================================
-- TABELA: comments
-- ================================
CREATE TABLE IF NOT EXISTS comments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    text TEXT NOT NULL,
    parent_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user_accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE
);

-- ================================
-- TABELA: watch_history
-- ================================
CREATE TABLE IF NOT EXISTS watch_history (
    user_id INT NOT NULL,
    post_id INT NOT NULL,
    watch_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, post_id), -- Add this line
    FOREIGN KEY (user_id) REFERENCES user_accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);
