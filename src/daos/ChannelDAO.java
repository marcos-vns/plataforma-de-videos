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

	public Channel save(String channelName) {

	    String sql = "INSERT INTO channels (name) VALUES (?)";

	    try (Connection conn = DatabaseConnection.getConnection(); 
	         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

	        ps.setString(1, channelName);

	        int affectedRows = ps.executeUpdate();

	        if (affectedRows > 0) {
	            try (ResultSet rs = ps.getGeneratedKeys()) {
	                if (rs.next()) {
	                    // 1. Pegue o ID pelo índice 1 (primeira coluna do retorno)
	                    long idGerado = rs.getLong(1);
	                    
	                    // 2. Use o 'channelName' que veio do parâmetro, não do ResultSet
	                    return new Channel(idGerado, channelName);
	                }
	            }
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return null;
	}
    
    public Channel findById(long id) {
    	String sql = "SELECT name, subscribers FROM channels WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)){
        	ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                Channel channel = new Channel(id, rs.getString("name"));
                // Note: Channel model might not have subscribers field yet, 
                // but we can at least return the name.
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
            SELECT c.id, c.name
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
                    rs.getString("name")
                );

                channels.add(channel);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return channels;
    }


}
