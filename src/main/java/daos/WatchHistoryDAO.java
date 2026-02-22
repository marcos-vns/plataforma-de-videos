package daos;

import database.DatabaseConnection;
import model.Post;
import model.Video;
import model.VideoCategory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WatchHistoryDAO {

    public void addPostToHistory(long userId, long postId) {
        String sql = "INSERT INTO watch_history (user_id, post_id, watch_timestamp) VALUES (?, ?, NOW()) " +
                     "ON DUPLICATE KEY UPDATE watch_timestamp = NOW()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, postId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Post> getWatchHistoryByUser(long userId) {
        List<Post> history = new ArrayList<>();
        String sql = "SELECT DISTINCT p.*, v.description, v.duration_seconds, v.views, v.video_url, v.video_category " +
                     "FROM posts p " +
                     "JOIN watch_history wh ON p.id = wh.post_id " +
                     "LEFT JOIN videos v ON p.id = v.post_id " +
                     "WHERE wh.user_id = ? " +
                     "GROUP BY p.id " +
                     "ORDER BY MAX(wh.watch_timestamp) DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Video post = new Video();
                post.setId(rs.getLong("id"));
                post.setTitle(rs.getString("title"));
                post.setThumbnailUrl(rs.getString("thumbnail_url"));
                post.setChannelId(rs.getLong("channel_id"));
                post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                post.setLikes(rs.getInt("likes"));
                post.setDislikes(rs.getInt("dislikes"));
                post.setDescription(rs.getString("description"));
                post.setDurationSeconds(rs.getInt("duration_seconds"));
                post.setViews(rs.getLong("views"));
                post.setVideoUrl(rs.getString("video_url"));
                post.setCategory(model.VideoCategory.valueOf(rs.getString("video_category")));
                history.add(post);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
}
