package daos;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import database.DatabaseConnection;
import model.User;

import java.sql.Connection;
import java.sql.DriverManager;

public class UserDAO {
	
	public void save(User user) {

		String sql = "INSERT INTO UserAccount (name, username, email, password) VALUES (? , ? , ?, ?)";
		
		try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);){
			
			ps.setString(1, user.getName());
			ps.setString(2, user.getUsername());
			ps.setString(3, user.getEmail());
			ps.setString(4, user.getPasswordHash());
			
			ps.executeUpdate();
			
			conn.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	public User findByUsername(String usernameToFind) {
		
		String sql = "SELECT * FROM UserAccount WHERE username = ?";
		
		try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);){

			ps.setString(1, usernameToFind);
			
			ResultSet rs = ps.executeQuery();
			
			if (rs.next()) {
                return new User(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("password")
                );
            }

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public User findByEmail(String emailToFind) {
		
		String sql = "SELECT * FROM UserAccount WHERE email = ?";
		
		try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);){

			ps.setString(1, emailToFind);
			
			ResultSet rs = ps.executeQuery();
			
			if (rs.next()) {
                return new User(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("username"),
                        rs.getNString("name"),
                        rs.getString("password")
                	);
            }
		

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}

}
