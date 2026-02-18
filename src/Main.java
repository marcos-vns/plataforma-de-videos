import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

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
import service.FileService;
import service.UserChannelService;

public class Main extends Application {
	
	@Override
	public void start(Stage stage) throws Exception {

	    DatabaseConnection.init();

	    // DAOs
	    UserDAO userDao = new UserDAO();
	    ChannelDAO channelDao = new ChannelDAO();
	    UserChannelDAO userChannelDao = new UserChannelDAO();
	    PostDAO postDao = new PostDAO();

	    // Services
	    AuthenticationService authenticationService =
	            new AuthenticationService(userDao);

	    UserChannelService userChannelService =
	            new UserChannelService(userChannelDao, userDao);
	    
	    ChannelService channelService =
	            new ChannelService(channelDao, userChannelService);

	    FileService fileService = 
	    		new FileService();
	    
	    SceneManager.init(
	            stage,
	            authenticationService,
	            channelService,
	            userChannelService,
	            fileService,
	    );

	    SceneManager.switchScene("/resources/app/view/login.fxml");

	    stage.show();
	}


}
