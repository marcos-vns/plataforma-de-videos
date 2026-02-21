package daos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.DatabaseConnection;
import model.Role;
import model.User;
import model.UserChannel;

public class UserChannelDAO {
	
	public void save(long userId, long channelId, Role role) {

			String sql = "INSERT INTO user_channel (user_id, channel_id, role) VALUES (?, ?, ?)";
			
			try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);){
				ps.setLong(1, userId);
				ps.setLong(2, channelId);
				ps.setString(3, role.name().toLowerCase());
				
				ps.executeUpdate();

			} catch (SQLException e) {
				e.printStackTrace();
			}

	}
	
	public boolean exists(long userId, long channelId) {
		
		String sql = "SELECT user_id, channel_id from user_channel WHERE user_id = ? AND channel_id = ?";
		
		try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);){

			ps.setLong(1, userId);
			ps.setLong(2, channelId);
			
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()) { return true; };

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
		
	}
	
	public boolean hasPermission(long userId, long channelId, Role role) {
		
		System.out.println(role.name());
		
		String sql = "SELECT role from user_channel WHERE user_id = ? AND channel_id = ?";
		
		try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);){
			
			ps.setLong(1, userId);
			ps.setLong(2, channelId);
			
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()) {
				
				if (rs.getString(1).equalsIgnoreCase(role.name())) {
					return true;
				}
				
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
		
	}

    public Role getRole(long userId, long channelId) {
        String sql = "SELECT role FROM user_channel WHERE user_id = ? AND channel_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, channelId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Role.valueOf(rs.getString("role").toUpperCase());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
		
}
	
