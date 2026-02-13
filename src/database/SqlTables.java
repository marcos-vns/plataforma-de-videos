package database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SqlTables {

public void createTables() throws SQLException{
    	
    	if(DatabaseConnection.isDataSourceNull()) {
    		throw new SQLException("Conaxao não foi inicializada ainda");
    	}
    	
    	String[] sql = {"CREATE TABLE IF NOT EXISTS UserAccount(\n"
                + "\tid INT PRIMARY KEY AUTO_INCREMENT,\n"
                + "\tname VARCHAR(100) NOT NULL,\n"
                + "\tusername VARCHAR(50) NOT NULL UNIQUE,\n"
                + "\temail VARCHAR(100) NOT NULL UNIQUE,\n"
                + "\tpassword VARCHAR(255) NOT NULL\n"
                + ");\n", 
                
                "CREATE TABLE IF NOT EXISTS Channel(\n"
                + "\tid INT PRIMARY KEY AUTO_INCREMENT,\n"
                + "\tname VARCHAR(100) NOT NULL,\n"
                + "\tsubscribers BIGINT DEFAULT 0\n"
                + ");\n", 
                
                "CREATE TABLE IF NOT EXISTS UserChannel(\n"
                + "\tuserid INT,\n"
                + "\tchannelid INT,\n"
                + "\trole ENUM('owner', 'editor', 'moderator') NOT NULL,\n"
                + "\tPRIMARY KEY (userid, channelid),\n"
                + "\tFOREIGN KEY (userid) ADD CONSTRAINT fk_useraccountid REFERENCES UserAccount(id) ON DELETE CASCADE,\n"
                + "\tFOREIGN KEY (channelid) ADD CONSTRAINT fk_channelid REFERENCES Channel(id) ON DELETE CASCADE\n"
                + ");"};

        for(String statement : sql) {
        	Connection conn = DatabaseConnection.getConnection();
        	PreparedStatement ps = conn.prepareStatement(statement);
            ps.execute();
        }
    }
	
}
