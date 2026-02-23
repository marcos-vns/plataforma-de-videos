package database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseConnection {
	
	private static HikariDataSource dataSource;
	
	public static final String url = "jdbc:mysql://localhost:3306/teste";
    public static final String user = "root";
    public static final String password = "root";
    
    public static void init(){
    	try {
    		HikariConfig config = new HikariConfig();
    		
    		config.setJdbcUrl("jdbc:mysql://localhost:3306/teste");
    		config.setUsername("root");
    		config.setPassword("root");
    		
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            
            dataSource = new HikariDataSource(config);
            
            System.out.println("Connection Pool inicializado com sucesso!");
    		
    	} catch (Exception e) {
    		System.out.println("Erro ao fazer conexao com o banco de dados");
    		e.printStackTrace();
    	}
    }
    
    public static boolean isDataSourceNull() {
    	return dataSource == null;
    }
    
    public static Connection getConnection() throws SQLException{
    	
    	if(isDataSourceNull()) {
    		throw new SQLException("Conaxao não foi inicializada ainda");
    	}
    	
    	return dataSource.getConnection();
    	
    }
    
    public static void closePool() {
    	if(dataSource != null && !dataSource.isClosed()) {
    		dataSource.close();
    		
    		System.out.println("Conexao fechada com sucesso");
    	}
    }
    
}
