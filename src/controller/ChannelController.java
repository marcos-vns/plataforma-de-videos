package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import model.Channel;
import model.Post;
import model.PostType;
import model.Video;
import service.ChannelService;
import service.PostService;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ChannelController {

    @FXML private ImageView channelProfilePic;
    @FXML private Label channelNameLabel;
    @FXML private Label subscriberCountLabel;
    @FXML private Button studioBtn;
    @FXML private FlowPane contentGrid;
    
    @FXML private Button videosTabBtn;
    @FXML private Button postsTabBtn;
    
    @FXML private Button recentFilterBtn;
    @FXML private Button popularFilterBtn;
    @FXML private Button oldestFilterBtn;

    private PostService postService;
    private ChannelService channelService;
    private Channel channel;
    private List<Post> allPosts = new ArrayList<>();
    private PostType currentTypeFilter = PostType.VIDEO;
    private String currentSortFilter = "RECENT";

    public void setServices(PostService postService, ChannelService channelService) {
        this.postService = postService;
        this.channelService = channelService;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
        loadChannelData();
        loadPosts();
    }

    @FXML
    public void initialize() {
        updateTabStyles();
        updateFilterStyles();
    }

    private void loadChannelData() {
        if (channel == null) return;
        channelNameLabel.setText(channel.getName());
        subscriberCountLabel.setText(channel.getSubscribers() + " inscritos");
        
        // Role check
        if (channel.getCurrentUserRole() != null) {
            studioBtn.setVisible(true);
            studioBtn.setManaged(true);
        }

        String picUrl = channel.getProfilePictureUrl();
        if (picUrl != null && !picUrl.isEmpty()) {
            File file = new File("storage", picUrl);
            if (file.exists()) {
                channelProfilePic.setImage(new Image(file.toURI().toString()));
            }
        }
    }

    @FXML
    private void enterStudio() {
        if (channel != null) {
            SceneManager.showStudioScene(channel);
        }
    }

    private void loadPosts() {
        try {
            allPosts = postService.getPostsByChannel(channel.getId());
            applyFiltersAndDisplay();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void applyFiltersAndDisplay() {
        List<Post> filtered = allPosts.stream()
                .filter(p -> p.getPostType() == currentTypeFilter)
                .collect(Collectors.toList());

        switch (currentSortFilter) {
            case "RECENT":
                filtered.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
                break;
            case "OLDEST":
                filtered.sort((p1, p2) -> p1.getCreatedAt().compareTo(p2.getCreatedAt()));
                break;
            case "POPULAR":
                filtered.sort((p1, p2) -> {
                    long v1 = (p1 instanceof Video) ? ((Video) p1).getViews() : 0;
                    long v2 = (p2 instanceof Video) ? ((Video) p2).getViews() : 0;
                    return Long.compare(v2, v1);
                });
                break;
        }

        displayPosts(filtered);
    }

    private void displayPosts(List<Post> posts) {
        contentGrid.getChildren().clear();
        for (Post post : posts) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/app/view/post_card.fxml"));
                VBox card = loader.load();
                PostCardController controller = loader.getController();
                controller.setData(post, channel);
                contentGrid.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onVideosTabClicked() {
        currentTypeFilter = PostType.VIDEO;
        updateTabStyles();
        applyFiltersAndDisplay();
    }

    @FXML
    private void onPostsTabClicked() {
        currentTypeFilter = PostType.TEXT;
        updateTabStyles();
        applyFiltersAndDisplay();
    }

    @FXML
    private void onRecentClicked() {
        currentSortFilter = "RECENT";
        updateFilterStyles();
        applyFiltersAndDisplay();
    }

    @FXML
    private void onPopularClicked() {
        currentSortFilter = "POPULAR";
        updateFilterStyles();
        applyFiltersAndDisplay();
    }

    @FXML
    private void onOldestClicked() {
        currentSortFilter = "OLDEST";
        updateFilterStyles();
        applyFiltersAndDisplay();
    }

    private void updateTabStyles() {
        videosTabBtn.setStyle(currentTypeFilter == PostType.VIDEO ? "-fx-border-color: black; -fx-border-width: 0 0 2 0; -fx-background-color: transparent; -fx-font-weight: bold;" : "-fx-background-color: transparent;");
        postsTabBtn.setStyle(currentTypeFilter == PostType.TEXT ? "-fx-border-color: black; -fx-border-width: 0 0 2 0; -fx-background-color: transparent; -fx-font-weight: bold;" : "-fx-background-color: transparent;");
    }

    private void updateFilterStyles() {
        recentFilterBtn.setStyle(currentSortFilter.equals("RECENT") ? "-fx-background-color: black; -fx-text-fill: white; -fx-background-radius: 15;" : "-fx-background-color: #f0f0f0; -fx-background-radius: 15;");
        popularFilterBtn.setStyle(currentSortFilter.equals("POPULAR") ? "-fx-background-color: black; -fx-text-fill: white; -fx-background-radius: 15;" : "-fx-background-color: #f0f0f0; -fx-background-radius: 15;");
        oldestFilterBtn.setStyle(currentSortFilter.equals("OLDEST") ? "-fx-background-color: black; -fx-text-fill: white; -fx-background-radius: 15;" : "-fx-background-color: #f0f0f0; -fx-background-radius: 15;");
    }
    
    @FXML
    private void goBack() {
        SceneManager.switchScene("/resources/app/view/dashboard.fxml");
    }
}
