package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.Post;
import java.io.File;

public class PostCardController {
    @FXML private ImageView thumbnail;
    @FXML private Label titleLabel;
    @FXML private Label channelLabel;
    @FXML private Label typeTag;
    @FXML private Label likesLabel;

    private Post post;

    public void setData(Post post, String channelName) {
        this.post = post;
        titleLabel.setText(post.getTitle());
        channelLabel.setText(channelName);
        likesLabel.setText("👍 " + post.getLikes());
        
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
    private void onCardClicked() {
        SceneManager.showPostScene(post.getId());
    }
}
