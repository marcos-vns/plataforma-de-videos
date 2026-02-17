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
	
	private Connection connection;

    public Channel save(String channelName){

        String sql = "INSERT INTO Channel (name) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){

            ps.setString(1, channelName);

            ps.executeUpdate();
            
            ResultSet rs = ps.getGeneratedKeys();
            
            if(rs.next()) {
            	return new Channel(
            			rs.getLong("id"),
            			rs.getString("name")
            		);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;

    }
    
    public void find(long id) {
    	
    	String sql = "SELECT name, subscribers FROM Channel WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)){
        	
        	ps.setLong(1, id);
        	
            ResultSet rs = ps.executeQuery();
            
            if(rs.next()) {
            	String name = rs.getString("name");
            	long subscribers = rs.getLong("subscribers");
            	
            	System.out.println("Nome: " + name + "\nInscritos: " + subscribers);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    	
    }
    
    public List<Channel> findByUser(long userId) {

        List<Channel> channels = new ArrayList<>();

        String sql = """
            SELECT c.id, c.name
            FROM Channel c
            JOIN UserChannel uc ON uc.channelid = c.id
            WHERE uc.userid = ?
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
