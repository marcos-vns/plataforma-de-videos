package daos;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import database.DatabaseConnection;
import model.Channel;

public class ChannelDAO {

	public Channel save(String channelName, String profilePictureUrl) {

	    String sql = "INSERT INTO channels (name, profile_picture_url) VALUES (?, ?)";

	    try (Connection conn = DatabaseConnection.getConnection(); 
	         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

	        ps.setString(1, channelName);
	        ps.setString(2, profilePictureUrl);

	        int affectedRows = ps.executeUpdate();

	        if (affectedRows > 0) {
	            try (ResultSet rs = ps.getGeneratedKeys()) {
	                if (rs.next()) {
	                    // 1. Pegue o ID pelo índice 1 (primeira coluna do retorno)
	                    long generatedId = rs.getLong(1);
	                    
	                    // 2. Use o 'channelName' que veio do parâmetro, não do ResultSet
	                    return new Channel(generatedId, channelName, profilePictureUrl);
	                }
	            }
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return null;
	}
    
    public Channel findById(long id, long userId) {
        String sql = """
            SELECT c.name, c.subscribers, c.profile_picture_url, uc.role
            FROM channels c
            LEFT JOIN user_channel uc ON uc.channel_id = c.id AND uc.user_id = ?
            WHERE c.id = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setLong(1, userId);
            ps.setLong(2, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                Channel channel = new Channel(id, rs.getString("name"), rs.getString("profile_picture_url"));
                channel.setSubscribers(rs.getLong("subscribers"));
                String roleStr = rs.getString("role");
                if (roleStr != null) {
                    channel.setCurrentUserRole(model.Role.valueOf(roleStr.toUpperCase()));
                }
                return channel;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<Channel> findByUser(long userId) {

        List<Channel> channels = new ArrayList<>();

        String sql = """
            SELECT c.id, c.name, c.profile_picture_url, c.subscribers, uc.role
            FROM channels c
            JOIN user_channel uc ON uc.channel_id = c.id
            WHERE uc.user_id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Channel channel = new Channel(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("profile_picture_url")
                );
                channel.setSubscribers(rs.getLong("subscribers"));
                
                String roleStr = rs.getString("role");
                if (roleStr != null) {
                    channel.setCurrentUserRole(model.Role.valueOf(roleStr.toUpperCase()));
                }

                channels.add(channel);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return channels;
    }


}
