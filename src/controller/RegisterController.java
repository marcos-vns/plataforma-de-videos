package controller;

import daos.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import service.UserService;

public class RegisterController {

    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;
    @FXML
    private TextField emailField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField nameField;
    
    private UserService userService;
    
    @FXML
    private void send() {
    	userService.register(
    			emailField.getText(), 
    			passwordField.getText(), 
    			usernameField.getText(), 
    			nameField.getText()
    		);
    }
    
    @FXML
    public void initialize() {
    	userService = new UserService(new UserDAO());
    }
}
