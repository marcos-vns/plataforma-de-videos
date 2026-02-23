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

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "root")) {
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
	    daos.PostDAO postDao = new daos.PostDAO();
        daos.CommentDAO commentDao = new daos.CommentDAO(); // New DAO instantiation
        daos.WatchHistoryDAO watchHistoryDao = new daos.WatchHistoryDAO();

	    AuthenticationService authenticationService =
	            new AuthenticationService(userDao);

	    ChannelService channelService =
	            new ChannelService(channelDao, postService, commentService, fileService);

	    UserChannelService userChannelService =
	            new UserChannelService(userChannelDao, userDao, channelService);

	    FileService fileService = new FileService();

	    service.PostService postService = new service.PostService(postDao, commentService, fileService);

	    service.CommentService commentService = new service.CommentService(commentDao);

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
	    
	            stage.setTitle("Streamly");
	            stage.setWidth(1280);
	            stage.setHeight(720);
	            stage.centerOnScreen();
	    
	            stage.show();	}

	public static void main(String[] args) {
		Application.launch(Main.class, args);
	}


}
