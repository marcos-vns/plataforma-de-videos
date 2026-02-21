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
    		
            config.setMaximumPoolSize(10);        // Máximo 10 conexões
            config.setMinimumIdle(2);             // Mínimo 2 conexões ociosas
            config.setConnectionTimeout(30000);   // 30 segundos para timeout
            config.setIdleTimeout(600000);        // 10 minutos idle
            config.setMaxLifetime(1800000);       // 30 minutos vida máxima
            
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
