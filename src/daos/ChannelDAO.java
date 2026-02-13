package daos;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import model.Channel;

public class ChannelDAO {
	
	private Connection connection;

    public Channel save(Connection conn, String channelName){

    	Channel channel = null;
        String sql = "INSERT INTO Channel (name) VALUES (?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){

            ps.setString(1, channelName);

            ps.executeUpdate();
            
            ResultSet rs = ps.getGeneratedKeys();
            
            if(rs.next()) {
            	channel = new Channel();
            	channel.setId(rs.getLong("id"));
            	channel.setName(rs.getString("name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return channel;

    }
    
    public void find(Connection conn, int id) {
    	
    	String sql = "SELECT name, subscribers FROM Channel WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)){
        	
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

    public void publishVideo() {
    	
    	
    	
    }

}
