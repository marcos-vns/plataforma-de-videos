package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.Post;
import model.Channel;
import java.io.File;

public class PostCardController {
    @FXML private ImageView thumbnail;
    @FXML private Label titleLabel;
    @FXML private Label channelLabel;
    @FXML private Label typeTag;
    @FXML private Label likesLabel;
    @FXML private Label viewsLabel;
    @FXML private ImageView channelProfilePic;

    private Post post;
    private Channel channel;

    public void setData(Post post, Channel channel) {
        this.post = post;
        this.channel = channel;
        titleLabel.setText(post.getTitle());
        channelLabel.setText(channel != null ? channel.getName() : "Canal Desconhecido");
        likesLabel.setText("👍 " + post.getLikes());
        
        if (post instanceof model.Video video) {
            viewsLabel.setText("👁 " + video.getViews());
            viewsLabel.setVisible(true);
            viewsLabel.setManaged(true);
        } else {
            viewsLabel.setVisible(false);
            viewsLabel.setManaged(false);
        }
        
        if (channel != null && channel.getProfilePictureUrl() != null && !channel.getProfilePictureUrl().isEmpty()) {
            File picFile = new File("storage", channel.getProfilePictureUrl());
            if (picFile.exists()) {
                channelProfilePic.setImage(new Image(picFile.toURI().toString()));
            }
        }
        
        // Set Tag
        if (post instanceof model.Video video) {
            typeTag.setText(video.getCategory().name()); // LONG or SHORT
            typeTag.setStyle(typeTag.getStyle() + (video.getCategory() == model.VideoCategory.SHORT ? "-fx-background-color: #f00;" : ""));
        } else {
            typeTag.setText("TEXTO");
            typeTag.setStyle(typeTag.getStyle() + "-fx-background-color: #007bff;");
        }
        
        if (post.getThumbnailUrl() != null && !post.getThumbnailUrl().isEmpty()) {
            // post.getThumbnailUrl() contains "thumbnails/uuid.png"
            File file = new File("storage", post.getThumbnailUrl());
            if (file.exists()) {
                thumbnail.setImage(new Image(file.toURI().toString()));
            } else {
                // Fallback for debugging if path is different
                System.err.println("Thumbnail not found at: " + file.getAbsolutePath());
            }
        }
    }

    @FXML
    private void onCardClicked(javafx.scene.input.MouseEvent event) {
        SceneManager.showPostScene(post.getId());
    }

    @FXML
    private void onChannelClicked(javafx.scene.input.MouseEvent event) {
        event.consume(); // Prevent card click
        if (channel != null) {
            SceneManager.showChannelScene(channel);
        }
    }
}
