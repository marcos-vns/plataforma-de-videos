package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.Channel;

import java.io.File;

public class ChannelCardController {

    @FXML private ImageView channelProfilePic;
    @FXML private Label channelNameLabel;
    @FXML private Label subscriberCountLabel;

    private Channel channel;

    public void setData(Channel channel) {
        this.channel = channel;
        channelNameLabel.setText(channel.getName());
        subscriberCountLabel.setText(channel.getSubscribers() + " inscritos");

        if (channel.getProfilePictureUrl() != null && !channel.getProfilePictureUrl().isEmpty()) {
            File picFile = new File("storage", channel.getProfilePictureUrl());
            if (picFile.exists()) {
                channelProfilePic.setImage(new Image(picFile.toURI().toString()));
            }
        }
    }

    @FXML
    private void onChannelClicked() {
        if (channel != null) {
            SceneManager.showChannelScene(channel);
        }
    }
}
