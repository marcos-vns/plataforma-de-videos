package controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import model.Channel;
import model.Role;
import service.AuthenticationService;
import service.ChannelService;
import service.Session;
import service.UserChannelService;

public class DashboardController {

    @FXML private TextField channelNameField;
    @FXML private TextField usernameField;

    @FXML private ComboBox<Channel> channelComboBox;
    @FXML private ComboBox<Role> roleComboBox;

    private ChannelService channelService;
    private UserChannelService userChannelService;
    private AuthenticationService authenticationService;
    
    public void setServices(ChannelService channel,
            UserChannelService userChannel,
            AuthenticationService auth) {

		this.channelService = channel;
		this.userChannelService = userChannel;
		this.authenticationService = auth;
		
    }

    @FXML
    public void initialize() {
        roleComboBox.getItems().setAll(Role.values());
        loadUserChannels();
    }

    @FXML
    private void createChannel() {
        String name = channelNameField.getText();
        channelService.create(name);
        loadUserChannels();
    }

    @FXML
    private void addUserToChannel() {
        Channel channel = channelComboBox.getValue();
        Role role = roleComboBox.getValue();
        String username = usernameField.getText();

        userChannelService.addUserToChannel(
                username,
                channel.getId(),
                role
        );
    }

    @FXML
    private void loadUserChannels() {
        channelComboBox.getItems().setAll(
            channelService.findChannelsByUser(Session.getUser().getId())
        );
    }
    
    @FXML
    private void logout() {
    	authenticationService.logout();
    }

}
