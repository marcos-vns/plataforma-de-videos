import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import controller.SceneManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import daos.UserDAO;
import database.DatabaseConnection;
import model.User;

public class Main extends Application {
	
	// aqui vai ser a pagina inicial depois
	
	@Override
    public void start(Stage stage) throws Exception {

		DatabaseConnection.init();
		
        FXMLLoader loader =
            new FXMLLoader(getClass().getResource("/resources/app/view/login.fxml"));

        Scene scene = new Scene(loader.load());
        
        SceneManager.setStage(stage);
        
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}
