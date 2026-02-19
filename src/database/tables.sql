CREATE DATABASE IF NOT EXISTS teste;
USE teste;

-- ================================
-- TABELA: accounts (UserAccount)
-- ================================
CREATE TABLE IF NOT EXISTS UserAccount (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- ================================
-- TABELA: channels
-- ================================
CREATE TABLE IF NOT EXISTS channels (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    subscribers BIGINT DEFAULT 0
);

-- ================================
-- TABELA: user_channel
-- ================================
CREATE TABLE IF NOT EXISTS user_channel (
    user_id INT,
    channel_id INT,
    role ENUM('owner', 'editor', 'moderator') NOT NULL,
    PRIMARY KEY (user_id, channel_id),
    
    CONSTRAINT fk_user_channel_user 
        FOREIGN KEY (user_id) 
        REFERENCES UserAccount(id) 
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
    post_type ENUM('VIDEO', 'TEXTO') NOT NULL,
    
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
-- TABELA: posts_texto
-- ================================
CREATE TABLE IF NOT EXISTS posts_texto (
    post_id INT PRIMARY KEY,
    conteudo TEXT NOT NULL,
    
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
    FOREIGN KEY (user_id) REFERENCES UserAccount(id) ON DELETE CASCADE,
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES UserAccount(id) ON DELETE CASCADE
);
