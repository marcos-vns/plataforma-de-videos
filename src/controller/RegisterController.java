package controller;

import daos.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import service.FileService;
import service.UserService;

import java.io.File;
import java.io.IOException;

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
    @FXML
    private Label profilePicLabel;
    
    private UserService userService;
    private FileService fileService;
    private File profilePicFile;
    
    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }
    
    @FXML
    private void selectProfilePicture() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens", "*.jpg", "*.png", "*.jpeg"));
        File file = fc.showOpenDialog(null);
        if (file != null) {
            this.profilePicFile = file;
            profilePicLabel.setText(file.getName());
        }
    }
    
    @FXML
    private void send() {
        try {
            String profilePicUrl = null;
            if (profilePicFile != null && fileService != null) {
                profilePicUrl = fileService.saveFile(profilePicFile, "PROFILE");
            }
            
            userService.register(
                emailField.getText(), 
                passwordField.getText(), 
                usernameField.getText(), 
                nameField.getText(),
                profilePicUrl
            );
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Usuário cadastrado com sucesso!");
            alert.showAndWait();
            SceneManager.switchScene("/resources/app/view/login.fxml");
            
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Erro ao cadastrar: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    @FXML
    public void initialize() {
    	userService = new UserService(new UserDAO());
    }
}
