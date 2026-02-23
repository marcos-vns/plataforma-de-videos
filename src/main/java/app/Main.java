package app;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import controller.SceneManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import daos.ChannelDAO;
import daos.PostDAO;
import daos.UserChannelDAO;
import daos.UserDAO;
import database.DatabaseConnection;
import model.User;
import service.AuthenticationService;
import service.ChannelService;
import service.CommentService;
import service.FileService;
import service.PostService;
import service.UserChannelService;
import service.WatchHistoryService;
import daos.WatchHistoryDAO;

public class Main extends Application {
	
	@Override
	public void start(Stage stage) throws Exception {

        // Ensure the database exists before trying to connect to it
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "root")) { // Replace with actual credentials if different
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE DATABASE IF NOT EXISTS teste;");
            System.out.println("Database 'teste' checked/created.");
        } catch (SQLException e) {
            System.err.println("Erro ao criar/verificar database: " + e.getMessage());
        }
        
	    DatabaseConnection.init();

	    UserDAO userDao = new UserDAO();
	    ChannelDAO channelDao = new ChannelDAO();
	    UserChannelDAO userChannelDao = new UserChannelDAO();
	    PostDAO postDao = new PostDAO();
        WatchHistoryDAO watchHistoryDao = new WatchHistoryDAO();

	    AuthenticationService authenticationService =
	            new AuthenticationService(userDao);

	    // Instantiate ChannelService first as UserChannelService now depends on it
	    ChannelService channelService =
	            new ChannelService(channelDao); // No longer takes userChannelService

	    UserChannelService userChannelService =
	            new UserChannelService(userChannelDao, userDao, channelService); // Pass channelService here

	    FileService fileService = new FileService();

	    PostService postService = new PostService();

	    CommentService commentService = new CommentService();

        WatchHistoryService watchHistoryService = new WatchHistoryService(watchHistoryDao);

	    SceneManager.init(
	            stage,
	            authenticationService,
	            channelService,
	            userChannelService,
	            postService,
	            fileService,
	            commentService,
                watchHistoryService
	    );

	            SceneManager.switchScene("/app/view/login.fxml");
	    
	            stage.setTitle("Streamly"); // Set the window title
	            stage.setWidth(1280);      // Set the window width
	            stage.setHeight(720);     // Set the window height
	            stage.centerOnScreen();   // Center the window
	    
	            stage.show();	}

	public static void main(String[] args) {
		Application.launch(Main.class, args);
	}


}
