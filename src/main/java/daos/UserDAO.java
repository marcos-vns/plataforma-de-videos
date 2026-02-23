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

		String sql = "INSERT INTO user_accounts (name, username, email, password, profile_picture_url) VALUES (? , ? , ?, ?, ?)";
		
		try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);){
			
			ps.setString(1, user.getName());
			ps.setString(2, user.getUsername());
			ps.setString(3, user.getEmail());
			ps.setString(4, user.getPasswordHash());
			ps.setString(5, user.getProfilePictureUrl());
			
			ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getLong(1));
                }
            }

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	public User findByUsername(String usernameToFind) {
		
		String sql = "SELECT * FROM user_accounts WHERE username = ?";
		
		try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);){

			ps.setString(1, usernameToFind);
			
			ResultSet rs = ps.executeQuery();
			
			if (rs.next()) {
                return new User(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("password"),
                        rs.getString("profile_picture_url")
                );
            }

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public User findByEmail(String emailToFind) {
		
		String sql = "SELECT * FROM user_accounts WHERE email = ?";
		
		try (java.sql.Connection conn = database.DatabaseConnection.getConnection(); java.sql.PreparedStatement ps = conn.prepareStatement(sql);){

			ps.setString(1, emailToFind);
			
			java.sql.ResultSet rs = ps.executeQuery();
			
			if (rs.next()) {
                return new model.User(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("password"),
                        rs.getString("profile_picture_url")
                	);
            }
		

		} catch (java.sql.SQLException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}

    public model.User findById(long id) throws java.sql.SQLException {
        String sql = "SELECT * FROM user_accounts WHERE id = ?";
        try (java.sql.Connection conn = database.DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new model.User(
                            rs.getLong("id"),
                            rs.getString("email"),
                            rs.getString("username"),
                            rs.getString("name"),
                            rs.getString("password"),
                            rs.getString("profile_picture_url")
                    );
                }
            }
        }
        return null;
    }

    public void delete(long id) throws java.sql.SQLException {
        String sql = "DELETE FROM user_accounts WHERE id = ?";
        try (java.sql.Connection conn = database.DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

}
