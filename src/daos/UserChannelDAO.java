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

			String sql = "INSERT INTO UserChannel (userid, channelid, role) VALUES (?, ?, ?)";
			
			try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);){

				ps.setLong(1, userId);
				ps.setLong(2, channelId);
				ps.setString(3, role.name());
				
				ps.executeUpdate();

			} catch (SQLException e) {
				e.printStackTrace();
			}

	}
	
	public boolean exists(long userId, long channelId) {
		
		String sql = "SELECT userid, channelid from UserChannel WHERE userid = ? AND channelid = ?";
		
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
		
		String sql = "SELECT * from UserChannel WHERE userid = ? AND channelid = ?";
		
		try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);){

			ps.setLong(1, userId);
			ps.setLong(2, channelId);
			ps.setString(3, role.name());
			
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()) {
				boolean isOwner = rs.getString("role") == "OWNER";
				if(isOwner) { return true; };
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
		
	}
		
}
	
