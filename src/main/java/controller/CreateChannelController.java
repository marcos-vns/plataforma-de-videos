package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import service.ChannelService;
import service.FileService;
import service.UserChannelService;
import service.UserSession;
import controller.SceneManager;
import model.Role;
import model.Channel;

import java.io.File;
import java.io.IOException;

public class CreateChannelController {

    @FXML
    private ImageView profileImageView;
    @FXML
    private TextField channelNameField;

    private ChannelService channelService;
    private FileService fileService;
    private UserChannelService userChannelService;
    private File selectedImageFile;

    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }

    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }

    public void setUserChannelService(UserChannelService userChannelService) {
        this.userChannelService = userChannelService;
    }

    @FXML
    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Escolha uma imagem de perfil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg")
        );
        selectedImageFile = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());
        if (selectedImageFile != null) {
            profileImageView.setImage(new Image(selectedImageFile.toURI().toString()));
        }
    }

    @FXML
    private void createChannel() {
        String channelName = channelNameField.getText();
        if (channelName == null || channelName.trim().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "O nome do canal não pode estar vazio.").showAndWait();
            return;
        }

        String profilePictureUrl = null;
        if (selectedImageFile != null) {
            try {
                profilePictureUrl = fileService.saveFile(selectedImageFile, "PROFILE");
            } catch (IOException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Erro ao salvar a imagem.").showAndWait();
                return;
            }
        }

        try {
            System.out.println("DEBUG: Creating channel with name: " + channelName + ", profile pic: " + profilePictureUrl);
            Channel newChannel = channelService.create(channelName, profilePictureUrl);
            System.out.println("DEBUG: ChannelService.create returned newChannel with ID: " + (newChannel != null ? newChannel.getId() : "null"));

            if (UserSession.getUser() != null && newChannel != null) {
                System.out.println("DEBUG: Adding user " + UserSession.getUser().getId() + " as OWNER to channel " + newChannel.getId());
                userChannelService.addUserToChannelCreation(UserSession.getUser().getId(), newChannel.getId(), model.Role.OWNER);
                System.out.println("DEBUG: User added to channel successfully.");
            } else if (newChannel == null) {
                System.err.println("DEBUG: Channel creation failed, newChannel is null.");
                new Alert(Alert.AlertType.ERROR, "Erro ao criar o canal: Objeto Channel não retornado.").showAndWait();
                return;
            } else {
                System.err.println("DEBUG: UserSession is null, cannot add owner to channel.");
            }
            new Alert(Alert.AlertType.INFORMATION, "Canal criado com sucesso!").show();
            cancel();
            SceneManager.switchScene("/app/view/dashboard.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erro ao criar o canal: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void cancel() {
        Stage stage = (Stage) channelNameField.getScene().getWindow();
        stage.close();
    }
}
