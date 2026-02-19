package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import model.Channel;
import model.Post;
import model.PostType;
import service.ChannelSession;
import service.PostService;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class StudioController {

    @FXML
    private Label channelNameLabel;

    @FXML
    private ListView<Post> videosListView;
    
    @FXML
    private ListView<Post> textPostsListView;

    private PostService postService;
    private Channel currentChannel;

    @FXML
    public void initialize() {
        if (ChannelSession.hasSelectedChannel()) {
            this.currentChannel = ChannelSession.getChannel();
            channelNameLabel.setText("Estúdio do Canal: " + currentChannel.getName());
            // Tentamos carregar, mas pode falhar se o postService ainda não foi injetado
            loadPosts();
        }
    }

    public void setPostService(PostService postService) {
        this.postService = postService;
        System.out.println("Studio: PostService injetado.");
        loadPosts();
    }

    public void setChannel(Channel channel) {
        this.currentChannel = channel;
        ChannelSession.setChannel(channel);
        channelNameLabel.setText("Estúdio do Canal: " + channel.getName());
        loadPosts();
    }

    @FXML
    private void refresh() {
        loadPosts();
    }

    private void loadPosts() {
        if (currentChannel == null || postService == null) {
            return;
        }

        // Garantimos que a atualização ocorra na Thread do JavaFX
        Platform.runLater(() -> {
            try {
                System.out.println("Studio: Buscando posts para canal ID " + currentChannel.getId());
                List<Post> posts = postService.getPostsByChannel(currentChannel.getId());
                System.out.println("Studio: Encontrados " + posts.size() + " posts.");

                // Filtragem baseada no Enum (mais seguro que instanceof em alguns contextos de build)
                List<Post> videos = posts.stream()
                        .filter(p -> p.getTipoPost() == PostType.VIDEO)
                        .collect(Collectors.toList());
                
                List<Post> textPosts = posts.stream()
                        .filter(p -> p.getTipoPost() == PostType.TEXTO)
                        .collect(Collectors.toList());

                System.out.println("Studio: Vídeos: " + videos.size() + " | Textos: " + textPosts.size());

                videosListView.getItems().setAll(videos);
                textPostsListView.getItems().setAll(textPosts);
                
            } catch (SQLException e) {
                System.err.println("Studio: Erro ao carregar posts: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void createPost() {
        SceneManager.showCreatePostScene();
    }

    @FXML
    private void viewSelectedPost() {
        Post selectedPost = getSelectedPost();
        if (selectedPost != null) {
            SceneManager.showPostScene(selectedPost.getId());
        }
    }

    @FXML
    private void deleteSelectedPost() {
        Post selectedPost = getSelectedPost();
        if (selectedPost != null) {
            try {
                postService.deletePost(selectedPost.getId());
                loadPosts(); 
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private Post getSelectedPost() {
        Post video = videosListView.getSelectionModel().getSelectedItem();
        if (video != null) return video;
        
        return textPostsListView.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void backToDashboard() {
        ChannelSession.close();
        SceneManager.switchScene("/resources/app/view/dashboard.fxml");
    }
}
