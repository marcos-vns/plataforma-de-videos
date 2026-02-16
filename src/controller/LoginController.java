package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private Button loginButton;
    private Button registerButton;

    private TextField emailField;
    private TextField passwordField;
    
    @FXML
    private void login() {
    	System.out.println("login");
    }
    @FXML
    private void register() {
    	try {
    		System.out.println("tentando trocaaar");
			SceneManager.switchScene("/resources/app/view/register.fxml");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @FXML
    public void initialize() {
        
    	System.out.println("rodando");
    	
    }
}
