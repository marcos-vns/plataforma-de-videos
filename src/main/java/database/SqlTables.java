package database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;

public class SqlTables {

    public void createTables() throws SQLException {
        
        if(DatabaseConnection.isDataSourceNull()) {
            throw new SQLException("Conexão não foi inicializada ainda");
        }
        
        String[] tableNames = {"UserAccount", "channels", "user_channel", "posts", "videos", "text_posts"};
        String[] sql = {
            """
            CREATE TABLE IF NOT EXISTS UserAccount(
                id INT PRIMARY KEY AUTO_INCREMENT,
                name VARCHAR(100) NOT NULL,
                username VARCHAR(50) NOT NULL UNIQUE,
                email VARCHAR(100) NOT NULL UNIQUE,
                password VARCHAR(255) NOT NULL,
                profile_picture_url VARCHAR(255)
            );
            """, 
            
            """
            CREATE TABLE IF NOT EXISTS channels(
                id INT PRIMARY KEY AUTO_INCREMENT,
                name VARCHAR(100) NOT NULL,
                subscribers BIGINT DEFAULT 0,
                profile_picture_url VARCHAR(255)
            );
            """, 
            
            """
            CREATE TABLE IF NOT EXISTS user_channel(
                user_id INT,
                channel_id INT,
                role ENUM('owner', 'editor', 'moderator', 'subscriber') NOT NULL,
                PRIMARY KEY (user_id, channel_id),
                FOREIGN KEY (user_id) REFERENCES UserAccount(id) ON DELETE CASCADE,
                FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE
            );
            """,

                        """
                        CREATE TABLE IF NOT EXISTS posts(
                            id INT PRIMARY KEY AUTO_INCREMENT,
                            channel_id INT NOT NULL,
                            title VARCHAR(255) NOT NULL,
                            thumbnail_url VARCHAR(255),
                            likes INT DEFAULT 0,
                            dislikes INT DEFAULT 0,
                            post_type ENUM('VIDEO', 'TEXT') NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE
                        );
                        """, 
                        """
            CREATE TABLE IF NOT EXISTS videos(
                post_id INT PRIMARY KEY,
                description TEXT,
                duration_seconds INT NOT NULL,
                views BIGINT DEFAULT 0,
                video_url VARCHAR(255) NOT NULL,
                video_category ENUM('LONG', 'SHORT') NOT NULL,
                FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
            );
            """,

            """
            CREATE TABLE IF NOT EXISTS text_posts(
                post_id INT PRIMARY KEY,
                content TEXT NOT NULL,
                FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
            );
            """,
            
            """
            CREATE TABLE IF NOT EXISTS post_likes (
                user_id INT,
                post_id INT,
                is_like BOOLEAN,
                PRIMARY KEY (user_id, post_id),
                FOREIGN KEY (user_id) REFERENCES UserAccount(id) ON DELETE CASCADE,
                FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
            );
            """,

            """
            CREATE TABLE IF NOT EXISTS comments (
                id INT AUTO_INCREMENT PRIMARY KEY,
                post_id INT NOT NULL,
                user_id INT NOT NULL,
                text TEXT NOT NULL,
                parent_id INT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
                FOREIGN KEY (user_id) REFERENCES UserAccount(id) ON DELETE CASCADE,
                FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS watch_history (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                post_id INT NOT NULL,
                watch_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES UserAccount(id) ON DELETE CASCADE,
                FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
            );
            """
        };

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Drop comments table before creation to apply unique constraint changes
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS comments;");
            }
            // Drop watch_history table before creation to apply unique constraint changes
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS watch_history;");
            }
            // Drop user_channel table before creation to apply ENUM changes
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS user_channel;");
            }
            // Migração rápida para colunas que podem estar faltando
            try (Statement stmt = conn.createStatement()) {
                try { stmt.execute("ALTER TABLE posts ADD COLUMN likes INT DEFAULT 0"); } catch (SQLException ignored) {}
                try { stmt.execute("ALTER TABLE posts ADD COLUMN dislikes INT DEFAULT 0"); } catch (SQLException ignored) {}
                try { stmt.execute("ALTER TABLE posts ADD COLUMN description TEXT"); } catch (SQLException ignored) {}
                try { stmt.execute("ALTER TABLE UserAccount ADD COLUMN profile_picture_url VARCHAR(255)"); } catch (SQLException ignored) {}
                try { stmt.execute("ALTER TABLE channels ADD COLUMN profile_picture_url VARCHAR(255)"); } catch (SQLException ignored) {}
                try { stmt.execute("ALTER TABLE videos ADD COLUMN views BIGINT DEFAULT 0"); } catch (SQLException ignored) {}
            }

            String[] tableNamesWithComments = {"UserAccount", "channels", "user_channel", "posts", "videos", "text_posts", "post_likes", "comments", "watch_history"};
            for(int i = 0; i < sql.length; i++) {
                System.out.println("Verificando/Criando tabela: " + tableNamesWithComments[i]);
                try (PreparedStatement ps = conn.prepareStatement(sql[i])) {
                    ps.execute();
                }
            }
            System.out.println("Todas as tabelas foram processadas com sucesso.");
        } catch (SQLException e) {
            System.err.println("Erro crítico ao criar tabelas: " + e.getMessage());
            throw e;
        }
    }
}
