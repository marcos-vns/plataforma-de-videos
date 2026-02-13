import java.sql.Connection;
import java.sql.SQLException;

import daos.UserDAO;
import database.DatabaseConnection;
import model.User;

public class Main {
	public static void main(String[] args) {
		
		try {
			Connection conn = DatabaseConnection.getConnection();
		
			UserDAO userDAO = new UserDAO();
			
			User novoUsuario = new User();
			novoUsuario.setEmail("marcos1@hotmail.com");
			novoUsuario.setName("Marcos Vinicius");
			novoUsuario.setPassword("senhasegura123");
			novoUsuario.setUsername("cdrzinho");
			
			userDAO.save(conn, novoUsuario);
			
			novoUsuario.createChannel(conn, "Canal do Marcola");
			
			conn.close();
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DatabaseConnection.closePool();
		}
		
	}
}
