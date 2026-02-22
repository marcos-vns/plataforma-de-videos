package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import model.Channel;
import model.Post;
import service.ChannelService;
import service.UserSession;
import service.WatchHistoryService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class HistoryController {

    @FXML
    private FlowPane postsGrid;

    private WatchHistoryService watchHistoryService;
    private ChannelService channelService;

    public void setWatchHistoryService(WatchHistoryService watchHistoryService) {
        this.watchHistoryService = watchHistoryService;
    }
    
    public void setChannelService(ChannelService channelService) {
    	this.channelService = channelService;
    }

    @FXML
    public void initialize() {
        loadHistory();
    }

    private void loadHistory() {
        if (UserSession.getUser() != null && watchHistoryService != null) {
            List<Post> history = watchHistoryService.getWatchHistoryByUser(UserSession.getUser().getId());
            displayPosts(history);
        }
    }

    private void displayPosts(List<Post> posts) {
        postsGrid.getChildren().clear();
        for (Post post : posts) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/view/post_card.fxml"));
                VBox card = loader.load();
                PostCardController controller = loader.getController();
                if (controller != null) {
                    Channel channel = null;
                    try {
                        channel = channelService.getChannelById(post.getChannelId());
                    } catch (SQLException e) {
                        System.err.println("Erro ao obter canal para post " + post.getId() + " no histórico: " + e.getMessage());
                        e.printStackTrace();
                    }
                    controller.setData(post, channel);
                }
                postsGrid.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void goBack() {
        SceneManager.switchScene("/app/view/dashboard.fxml");
    }
}
