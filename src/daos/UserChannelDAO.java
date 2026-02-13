package daos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import model.Role;
import model.UserChannel;

public class UserChannelDAO {
	
	public void save(Connection conn, UserChannel participation) {

			String sql = "INSERT INTO UserChannel (userid, channelid, role) VALUES (? , ? , ?)";
			
			long userId = (participation.getUser()).getId();
			long channelId = participation.getChannel().getId();
			String role = participation.getRole().toString();
			
			
			try (PreparedStatement ps = conn.prepareStatement(sql);){

				ps.setLong(1, userId);
				ps.setLong(2, channelId);
				ps.setString(3, role);
				
				ps.executeUpdate();

			} catch (SQLException e) {
				e.printStackTrace();
			}

	}
	
	public void addNewMember(Connection conn, User userRequestingId, String usernameToAdd) {
		
		if(userRequestingId.getRole != Role.OWNER) {
			System.out.println("sem permissao para essa acao");
			return;
		}
		
		String sql = "SELECT * FROM account WHERE account.username = ?";
		
		try (PreparedStatement ps = conn.prepareStatement(sql);){

			ps.setString(0, usernameToAdd);
			ps.executeQuery();
			
			User userToAdd = null;
			
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()) {
				
				String name = rs.getString("");
			}
			
			if(userToAdd.getRole() != null) {
				System.out.println("Usuario ja possui um cargo (" + userToAdd.getRole() + ")");
			}
			
			
			

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	// public void removerMembroDoCanal()
		
}
	
