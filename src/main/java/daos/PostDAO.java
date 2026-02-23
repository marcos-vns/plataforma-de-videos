package daos;

import java.sql.*;

import database.DatabaseConnection;
import model.Post;
import model.TextPost;
import model.Video;

public class PostDAO {

    public void save(Post post) throws SQLException {
        Connection conn = null;
        PreparedStatement psPost = null;
        PreparedStatement psChild = null;

        String sql = "INSERT INTO posts (channel_id, title, thumbnail_url, post_type, created_at) VALUES (?, ?, ?, ?, NOW())";
        
        try {
            System.out.println("PostDAO: Iniciando salvamento do post...");
            // 1. Obter conexão e INICIAR TRANSAÇÃO
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Desliga o commit automático

            // 2. Inserir na tabela pai (POSTS)
            System.out.println("PostDAO: Inserindo na tabela 'posts'...");
            psPost = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            psPost.setLong(1, post.getChannelId());
            psPost.setString(2, post.getTitle());
            psPost.setString(3, post.getThumbnailUrl());
            psPost.setString(4, post.getPostType().name()); // Salva "VIDEO" ou "TEXT"
            
            int affectedRows = psPost.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Falha ao criar post, nenhuma linha afetada.");
            }

            // 3. Recuperar o ID gerado (Obrigatório para a FK)
            try (ResultSet generatedKeys = psPost.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getLong(1));
                    post.setCreatedAt(java.time.LocalDateTime.now());
                    System.out.println("PostDAO: ID gerado: " + post.getId());
                } else {
                    throw new SQLException("Falha ao criar post, nenhum ID obtido.");
                }
            }

            // 4. Inserir na tabela filha específica (Polimorfismo)
            if (post instanceof Video) {
                System.out.println("PostDAO: Inserindo na tabela 'videos'...");
                saveVideo((Video) post, conn);
            } else if (post instanceof TextPost) {
                System.out.println("PostDAO: Inserindo na tabela 'text_posts'...");
                saveText((TextPost) post, conn);
            }

            // 5. EFETIVAR TRANSAÇÃO (Se chegou aqui, tudo deu certo)
            conn.commit();
            System.out.println("PostDAO: Transação efetivada com sucesso.");

        } catch (SQLException e) {
            System.err.println("PostDAO: Erro detectado. Realizando rollback...");
            // 6. ROLLBACK (Se der erro, desfaz tudo)
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e; // Relança o erro para o Service saber que falhou
        } finally {
            // 7. Fechar recursos (Boas práticas)
            if (psPost != null) psPost.close();
            if (psChild != null) psChild.close();
            if (conn != null) {
                conn.setAutoCommit(true); // Restaura o estado padrão
                conn.close(); // Devolve para o pool do Hikari
            }
        }
    }

    public java.util.List<Post> findAll() throws SQLException {
        java.util.List<Post> posts = new java.util.ArrayList<>();
        String sql = "SELECT p.*, v.description, v.duration_seconds, v.views, v.video_url, v.video_category, pt.content " +
                     "FROM posts p " +
                     "LEFT JOIN videos v ON p.id = v.post_id " +
                     "LEFT JOIN text_posts pt ON p.id = pt.post_id " +
                     "ORDER BY p.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String type = rs.getString("post_type");
                Post post;
                if ("VIDEO".equals(type)) {
                    Video video = new Video();
                    video.setPostType(model.PostType.VIDEO);
                    video.setDescription(rs.getString("description"));
                    video.setDurationSeconds(rs.getInt("duration_seconds"));
                    video.setViews(rs.getLong("views"));
                    video.setVideoUrl(rs.getString("video_url"));
                    String category = rs.getString("video_category");
                    if (category != null) {
                        video.setCategory(model.VideoCategory.valueOf(category));
                    }
                    post = video;
                } else {
                    TextPost textPost = new TextPost();
                    textPost.setPostType(model.PostType.TEXT);
                    textPost.setContent(rs.getString("content"));
                    post = textPost;
                }
                post.setId(rs.getLong("id"));
                post.setChannelId(rs.getLong("channel_id"));
                post.setTitle(rs.getString("title"));
                post.setThumbnailUrl(rs.getString("thumbnail_url"));
                post.setLikes(rs.getInt("likes"));
                post.setDislikes(rs.getInt("dislikes"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) {
                    post.setCreatedAt(ts.toLocalDateTime());
                }
                posts.add(post);
            }
        }
        return posts;
    }

    public java.util.List<model.Post> findAllByChannelId(long channelId) throws java.sql.SQLException {
        java.util.List<model.Post> posts = new java.util.ArrayList<>();
        String sql = "SELECT p.*, v.description, v.duration_seconds, v.views, v.video_url, v.video_category, pt.content " +
                     "FROM posts p " +
                     "LEFT JOIN videos v ON p.id = v.post_id " +
                     "LEFT JOIN text_posts pt ON p.id = pt.post_id " +
                     "WHERE p.channel_id = ? " +
                     "ORDER BY p.created_at DESC";

        try (java.sql.Connection conn = database.DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, channelId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String type = rs.getString("post_type");
                    model.Post post;
                    if ("VIDEO".equals(type)) {
                        model.Video video = new model.Video();
                        video.setPostType(model.PostType.VIDEO);
                        video.setDescription(rs.getString("description"));
                        video.setDurationSeconds(rs.getInt("duration_seconds"));
                        video.setViews(rs.getLong("views"));
                        video.setVideoUrl(rs.getString("video_url"));
                        String category = rs.getString("video_category");
                        if (category != null) {
                            video.setCategory(model.VideoCategory.valueOf(category));
                        }
                        post = video;
                    } else {
                        model.TextPost textPost = new model.TextPost();
                        textPost.setPostType(model.PostType.TEXT);
                        textPost.setContent(rs.getString("content"));
                        post = textPost;
                    }
                    post.setId(id);
                    post.setChannelId(rs.getLong("channel_id"));
                    post.setTitle(rs.getString("title"));
                    post.setThumbnailUrl(rs.getString("thumbnail_url"));
                    post.setLikes(rs.getInt("likes"));
                    post.setDislikes(rs.getInt("dislikes"));
                    java.sql.Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        post.setCreatedAt(ts.toLocalDateTime());
                    }
                    posts.add(post);
                }
            }
        }
        
        return posts;
    }

    public java.util.List<model.Post> findAllByAuthorId(long authorId) throws java.sql.SQLException {
        java.util.List<model.Post> posts = new java.util.ArrayList<>();
        String sql = "SELECT p.*, v.description, v.duration_seconds, v.views, v.video_url, v.video_category, pt.content " +
                     "FROM posts p " +
                     "LEFT JOIN videos v ON p.id = v.post_id " +
                     "LEFT JOIN text_posts pt ON p.id = pt.post_id " +
                     "WHERE p.author_id = ? " + // Assuming posts table has an author_id column
                     "ORDER BY p.created_at DESC";

        try (java.sql.Connection conn = database.DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, authorId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String type = rs.getString("post_type");
                    model.Post post;
                    if ("VIDEO".equals(type)) {
                        model.Video video = new model.Video();
                        video.setPostType(model.PostType.VIDEO);
                        video.setDescription(rs.getString("description"));
                        video.setDurationSeconds(rs.getInt("duration_seconds"));
                        video.setViews(rs.getLong("views"));
                        video.setVideoUrl(rs.getString("video_url"));
                        String category = rs.getString("video_category");
                        if (category != null) {
                            video.setCategory(model.VideoCategory.valueOf(category));
                        }
                        post = video;
                    } else {
                        model.TextPost textPost = new model.TextPost();
                        textPost.setPostType(model.PostType.TEXT);
                        textPost.setContent(rs.getString("content"));
                        post = textPost;
                    }
                    post.setId(id);
                    // post.setChannelId(rs.getLong("channel_id")); // Assuming posts can be author-centric rather than strictly channel-centric
                    post.setTitle(rs.getString("title"));
                    post.setThumbnailUrl(rs.getString("thumbnail_url"));
                    post.setLikes(rs.getInt("likes"));
                    post.setDislikes(rs.getInt("dislikes"));
                    java.sql.Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        post.setCreatedAt(ts.toLocalDateTime());
                    }
                    posts.add(post);
                }
            }
        }
        
        return posts;
    }

    public Post findById(long id) throws SQLException {
        String sql = "SELECT p.*, v.description, v.duration_seconds, v.views, v.video_url, v.video_category, pt.content " +
                     "FROM posts p " +
                     "LEFT JOIN videos v ON p.id = v.post_id " +
                     "LEFT JOIN text_posts pt ON p.id = pt.post_id " +
                     "WHERE p.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String type = rs.getString("post_type");
                    Post post;
                    if ("VIDEO".equals(type)) {
                        Video video = new Video();
                        video.setPostType(model.PostType.VIDEO);
                        video.setDescription(rs.getString("description"));
                        video.setDurationSeconds(rs.getInt("duration_seconds"));
                        video.setViews(rs.getLong("views"));
                        video.setVideoUrl(rs.getString("video_url"));
                        String category = rs.getString("video_category");
                        if (category != null) {
                            video.setCategory(model.VideoCategory.valueOf(category));
                        }
                        post = video;
                    } else {
                        TextPost textPost = new TextPost();
                        textPost.setPostType(model.PostType.TEXT);
                        textPost.setContent(rs.getString("content"));
                        post = textPost;
                    }
                    post.setId(rs.getLong("id"));
                    post.setChannelId(rs.getLong("channel_id"));
                    post.setTitle(rs.getString("title"));
                    post.setThumbnailUrl(rs.getString("thumbnail_url"));
                    post.setLikes(rs.getInt("likes"));
                    post.setDislikes(rs.getInt("dislikes"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        post.setCreatedAt(ts.toLocalDateTime());
                    }
                    return post;
                }
            }
        }
        return null;
    }

    public void incrementViews(long postId) throws SQLException {
        String sql = "UPDATE videos SET views = views + 1 WHERE post_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, postId);
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM posts WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public void toggleLike(long userId, long postId, boolean isLike) throws SQLException {
        String checkSql = "SELECT is_like FROM post_likes WHERE user_id = ? AND post_id = ?";
        String deleteSql = "DELETE FROM post_likes WHERE user_id = ? AND post_id = ?";
        String insertSql = "INSERT INTO post_likes (user_id, post_id, is_like) VALUES (?, ?, ?)";
        String updateSql = "UPDATE post_likes SET is_like = ? WHERE user_id = ? AND post_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Boolean currentReaction = null;
                try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    ps.setLong(1, userId);
                    ps.setLong(2, postId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            currentReaction = rs.getBoolean("is_like");
                        }
                    }
                }

                if (currentReaction == null) {
                    // Novo like/dislike
                    try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                        ps.setLong(1, userId);
                        ps.setLong(2, postId);
                        ps.setBoolean(3, isLike);
                        ps.executeUpdate();
                    }
                } else if (currentReaction == isLike) {
                    // Remover like/dislike existente (toggle off)
                    try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                        ps.setLong(1, userId);
                        ps.setLong(2, postId);
                        ps.executeUpdate();
                    }
                } else {
                    // Mudar de like para dislike ou vice-versa
                    try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                        ps.setBoolean(1, isLike);
                        ps.setLong(2, userId);
                        ps.setLong(3, postId);
                        ps.executeUpdate();
                    }
                }

                // Atualizar contadores na tabela posts
                syncCounters(postId, conn);
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void syncCounters(long postId, Connection conn) throws SQLException {
        String syncSql = "UPDATE posts p SET " +
                         "likes = (SELECT COUNT(*) FROM post_likes WHERE post_id = p.id AND is_like = TRUE), " +
                         "dislikes = (SELECT COUNT(*) FROM post_likes WHERE post_id = p.id AND is_like = FALSE) " +
                         "WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(syncSql)) {
            ps.setLong(1, postId);
            ps.executeUpdate();
        }
    }

    public Boolean getUserReaction(long userId, long postId) throws SQLException {
        String sql = "SELECT is_like FROM post_likes WHERE user_id = ? AND post_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, postId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("is_like");
                }
            }
        }
        return null;
    }

    // Métodos auxiliares privados recebendo a conexão ABERTA
    private void saveVideo(Video video, Connection conn) throws SQLException {
        String sql = "INSERT INTO videos (post_id, description, duration_seconds, video_url, video_category) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, video.getId()); // FK é o ID do pai
            ps.setString(2, video.getDescription());
            ps.setInt(3, video.getDurationSeconds());
            ps.setString(4, video.getVideoUrl());
            String category = (video.getCategory() != null) ? video.getCategory().name() : "LONG";
            ps.setString(5, category);
            
            ps.executeUpdate();
        }
    }

    private void saveText(TextPost text, Connection conn) throws SQLException {
        String sql = "INSERT INTO text_posts (post_id, content) VALUES (?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, text.getId()); // FK é o ID do pai
            ps.setString(2, text.getContent());
            
            ps.executeUpdate();
        }
    }
}
