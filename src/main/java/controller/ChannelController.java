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
import javafx.scene.control.Alert;
import service.UserSession;
import service.UserChannelService;

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
    @FXML private Button subscribeButton;
    @FXML private Button studioBtn;
    @FXML private FlowPane contentGrid;
    
    @FXML private Button videosTabBtn;
    @FXML private Button postsTabBtn;
    
    @FXML private Button recentFilterBtn;
    @FXML private Button popularFilterBtn;
    @FXML private Button oldestFilterBtn;

    private PostService postService;
    private ChannelService channelService;
    private UserChannelService userChannelService;
    private Channel channel;
    private List<Post> allPosts = new ArrayList<>();
    private PostType currentTypeFilter = PostType.VIDEO;
    private String currentSortFilter = "RECENT";

    public void setServices(PostService postService, ChannelService channelService, UserChannelService userChannelService) {
        this.postService = postService;
        this.channelService = channelService;
        this.userChannelService = userChannelService;
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
        updateSubscribeButton();
    }

    private void loadChannelData() {
        if (channel == null) return;
        
        try {
            this.channel = channelService.getChannelById(this.channel.getId());
        } catch (SQLException e) {
            System.err.println("Erro ao recarregar dados do canal: " + e.getMessage());
        }

        channelNameLabel.setText(channel.getName());
        subscriberCountLabel.setText(channel.getSubscribers() + " inscritos");
        
        String picUrl = channel.getProfilePictureUrl();
        if (picUrl != null && !picUrl.isEmpty()) {
            File file = new File("storage", picUrl);
            if (file.exists()) {
                channelProfilePic.setImage(new Image(file.toURI().toString()));
            }
        }
        updateSubscribeButton();
    }

    @FXML
    private void enterStudio() {
        if (channel != null) {
            SceneManager.showStudioScene(channel);
        }
    }

    @FXML
    private void onSubscribeButtonClicked() {
        if (UserSession.getUser() == null) {
            new Alert(Alert.AlertType.WARNING, "Você precisa estar logado para se inscrever em um canal.").showAndWait();
            return;
        }
        if (channel == null) return;

        try {
            boolean isSubscribed = userChannelService.isUserSubscribed(UserSession.getUser().getId(), channel.getId());
            if (isSubscribed) {
                userChannelService.unsubscribe(UserSession.getUser().getId(), channel.getId());
            } else {
                userChannelService.subscribe(UserSession.getUser().getId(), channel.getId());
            }
            loadChannelData();
            updateSubscribeButton();
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erro ao alterar inscrição: " + e.getMessage()).showAndWait();
            e.printStackTrace();
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/view/post_card.fxml"));
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
        SceneManager.switchScene("/app/view/dashboard.fxml");
    }

    private void updateSubscribeButton() {
        if (UserSession.getUser() == null || channel == null) {
            subscribeButton.setVisible(false);
            subscribeButton.setManaged(false);
            studioBtn.setVisible(false);
            studioBtn.setManaged(false);
            return;
        }

        try {
            model.Role userRoleInChannel = userChannelService.getRole(UserSession.getUser().getId(), channel.getId());

            if (userRoleInChannel == model.Role.OWNER || userRoleInChannel == model.Role.EDITOR || userRoleInChannel == model.Role.MODERATOR) {
                subscribeButton.setVisible(false);
                subscribeButton.setManaged(false);
                studioBtn.setVisible(true);
                studioBtn.setManaged(true);
            } else {
                studioBtn.setVisible(false);
                studioBtn.setManaged(false);
                subscribeButton.setVisible(true);
                subscribeButton.setManaged(true);

                boolean isSubscribed = userChannelService.isUserSubscribed(UserSession.getUser().getId(), channel.getId());
                if (isSubscribed) {
                    subscribeButton.setText("Inscrito");
                    subscribeButton.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: black; -fx-background-radius: 20; -fx-padding: 8 15 8 15; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: black; -fx-border-width: 1;");
                } else {
                    subscribeButton.setText("Inscrever-se");
                    subscribeButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 15 8 15; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: black; -fx-border-width: 1;");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar status/permissões de inscrição: " + e.getMessage());
            subscribeButton.setVisible(false);
            subscribeButton.setManaged(false);
            studioBtn.setVisible(false);
            studioBtn.setManaged(false);
        }
    }
}
