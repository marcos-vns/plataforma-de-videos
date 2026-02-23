package controller;

import daos.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import service.AuthenticationService;
import service.UserService;

public class LoginController {

    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;
    @FXML
    private TextField emailField;
    @FXML
    private TextField passwordField;
    
    private AuthenticationService authenticationService;
    
    public void setAuthenticationService(AuthenticationService auth) {
    	this.authenticationService = auth;
    }
    
    @FXML
    private void login() {
    	boolean isLogged = authenticationService.login(
    			emailField.getText(), 
    			passwordField.getText(
    		));
    	
    	if(isLogged) {
    		try {
    			SceneManager.switchScene("/app/view/dashboard.fxml");
    		} catch (Exception e) {
    			System.out.println("peeee");
    			e.printStackTrace();
    		}
    	}
    }
    @FXML
    private void register() {
    	try {
			SceneManager.switchScene("/app/view/register.fxml");
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    @FXML
    public void initialize() {
    }

}
