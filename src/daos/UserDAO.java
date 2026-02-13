package daos;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import model.User;

import java.sql.Connection;
import java.sql.DriverManager;

public class UserDAO {

	public void save(Connection conn, User user) {

		String sql = "INSERT INTO `User` (name, username, password, email) VALUES (? , ? , ?, ?)";
		
		try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);){

			ps.setString(1, user.getName());
			ps.setString(2, user.getUsername());
			ps.setString(3, user.getEmail());
			ps.setString(4, user.getPassword());
			
			ps.executeUpdate();
			
			ResultSet rs = ps.getGeneratedKeys();
			
			if(rs.next()) {
				user.setId(rs.getLong("id"));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	public void find(Connection conn, int id) {
		
		String sql = "SELECT name FROM `User` WHERE id = ?";
		
		try (PreparedStatement ps = conn.prepareStatement(sql);){

			ps.setLong(1, id);
			
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()) {
				String name = rs.getString("name");
				System.out.println(name);
			}
			

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

}
