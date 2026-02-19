package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import service.CommentService;
import service.PostService;
import service.ChannelSession;
import service.UserSession;
import model.Post;
import model.Comment;
import model.Video;
import model.TextPost;
import java.io.File;
import java.sql.SQLException;

public class PostController {

    @FXML private Label postTitle;
    @FXML private MediaView mediaView;
    @FXML private TextArea txtContent;
    @FXML private TextArea postDescription;
    @FXML private ListView<Comment> commentListView;
    @FXML private VBox videoControls;
    @FXML private Button btnPlayPause;
    @FXML private Slider progressBar;
    @FXML private Slider volumeSlider;
    @FXML private Label currentTimeLabel;
    @FXML private Label totalTimeLabel;
    @FXML private StackPane contentPane;
    @FXML private VBox descriptionContainer;
    @FXML private Label likesLabel;
    @FXML private Label dislikesLabel;
    @FXML private Button btnLike;
    @FXML private Button btnDislike;
    @FXML private TextField commentInput;

    private PostService postService;
    private CommentService commentService;
    private MediaPlayer mediaPlayer;
    private long currentPostId;

    public void setServices(PostService postService, CommentService commentService) {
        this.postService = postService;
        this.commentService = commentService;
    }

    public void setPost(long postId) {
        this.currentPostId = postId;
        loadPostData();
        refreshReactionUI();
        loadComments();
    }

    private void loadPostData() {
        try {
            Post post = postService.getPostById(currentPostId);
            if (post != null) {
                postTitle.setText(post.getTitle());
                likesLabel.setText(String.valueOf(post.getLikes()));
                dislikesLabel.setText(String.valueOf(post.getDislikes()));
                
                if (post instanceof Video video) {
                    setupVideo(video);
                } else if (post instanceof TextPost textPost) {
                    setupText(textPost);
                }
            }
        } catch (Throwable t) {
            System.err.println("Erro crítico ao carregar dados do post: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private void refreshReactionUI() {
        try {
            if (UserSession.getUser() == null) return;
            
            Boolean reaction = postService.getUserReaction(UserSession.getUser().getId(), currentPostId);
            
            // Update labels from DB
            Post post = postService.getPostById(currentPostId);
            likesLabel.setText(String.valueOf(post.getLikes()));
            dislikesLabel.setText(String.valueOf(post.getDislikes()));

            // Update button styles
            if (reaction == null) {
                btnLike.setStyle("-fx-background-color: transparent; -fx-border-color: #ccc; -fx-border-radius: 5;");
                btnDislike.setStyle("-fx-background-color: transparent; -fx-border-color: #ccc; -fx-border-radius: 5;");
            } else if (reaction) {
                btnLike.setStyle("-fx-background-color: #e1f5fe; -fx-border-color: #03a9f4; -fx-border-radius: 5; -fx-text-fill: #03a9f4;");
                btnDislike.setStyle("-fx-background-color: transparent; -fx-border-color: #ccc; -fx-border-radius: 5;");
            } else {
                btnLike.setStyle("-fx-background-color: transparent; -fx-border-color: #ccc; -fx-border-radius: 5;");
                btnDislike.setStyle("-fx-background-color: #ffebee; -fx-border-color: #f44336; -fx-border-radius: 5; -fx-text-fill: #f44336;");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLike() {
        try {
            if (UserSession.getUser() == null) return;
            postService.toggleLike(UserSession.getUser().getId(), currentPostId, true);
            refreshReactionUI();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDislike() {
        try {
            if (UserSession.getUser() == null) return;
            postService.toggleLike(UserSession.getUser().getId(), currentPostId, false);
            refreshReactionUI();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupVideo(Video video) {
        descriptionContainer.setVisible(true);
        descriptionContainer.setManaged(true);
        postDescription.setText(video.getDescription());
        mediaView.setVisible(true);
        txtContent.setVisible(false);
        videoControls.setVisible(true);

        if (video.getVideoUrl() != null) {
            try {
                File videoFile = new File("storage", video.getVideoUrl());
                if (videoFile.exists()) {
                    Media media = new Media(videoFile.toURI().toString());
                    mediaPlayer = new MediaPlayer(media);
                    mediaView.setMediaPlayer(mediaPlayer);

                    mediaPlayer.setOnReady(() -> {
                        // Obter dimensões originais do vídeo
                        double videoWidth = media.getWidth();
                        double videoHeight = media.getHeight();
                        
                        // Limitar largura máxima a 900px, mantendo proporção
                        double maxWidth = 900;
                        double displayWidth = Math.min(videoWidth, maxWidth);
                        double displayHeight = (displayWidth / videoWidth) * videoHeight;

                        // Ajustar MediaView
                        mediaView.setFitWidth(displayWidth);
                        mediaView.setFitHeight(displayHeight);

                        // Ajustar Container para não sobrar fundo preto
                        contentPane.setPrefSize(displayWidth, displayHeight);
                        contentPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

                        totalTimeLabel.setText(formatTime(mediaPlayer.getTotalDuration()));
                        progressBar.setMax(mediaPlayer.getTotalDuration().toSeconds());
                    });

                    mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                        if (!progressBar.isValueChanging()) {
                            progressBar.setValue(newTime.toSeconds());
                        }
                        currentTimeLabel.setText(formatTime(newTime));
                    });

                    progressBar.valueProperty().addListener((obs, oldVal, newVal) -> {
                        if (progressBar.isValueChanging()) {
                            mediaPlayer.seek(javafx.util.Duration.seconds(newVal.doubleValue()));
                        }
                    });

                    // Permite clicar na barra para buscar
                    progressBar.setOnMousePressed(e -> {
                        mediaPlayer.seek(javafx.util.Duration.seconds(progressBar.getValue()));
                    });

                    volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                        mediaPlayer.setVolume(newVal.doubleValue());
                    });
                    mediaPlayer.setVolume(volumeSlider.getValue());

                    mediaPlayer.setOnEndOfMedia(() -> {
                        mediaPlayer.seek(mediaPlayer.getStartTime());
                        mediaPlayer.pause();
                        btnPlayPause.setText("▶");
                    });

                    mediaPlayer.play();
                    btnPlayPause.setText("⏸");
                } else {
                    postDescription.setText("Erro: Arquivo não encontrado.");
                }
            } catch (Exception e) {
                postDescription.setText("Erro ao carregar vídeo.");
            }
        }
    }

    private String formatTime(javafx.util.Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) duration.toSeconds() % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void setupText(TextPost text) {
        descriptionContainer.setVisible(false);
        descriptionContainer.setManaged(false);
        mediaView.setVisible(false);
        txtContent.setVisible(true);
        videoControls.setVisible(false);
        txtContent.setText(text.getContent());
    }

    @FXML
    private void togglePlay() {
        if (mediaPlayer != null) {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                btnPlayPause.setText("▶");
            } else {
                mediaPlayer.play();
                btnPlayPause.setText("⏸");
            }
        }
    }

    @FXML
    private void back() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        
        if (ChannelSession.hasSelectedChannel()) {
            SceneManager.showStudioScene(ChannelSession.getChannel());
        } else {
            SceneManager.switchScene("/resources/app/view/dashboard.fxml");
        }
    }

    private void loadComments() {
        try {
            java.util.List<Comment> comments = commentService.getCommentsByPost(currentPostId);
            commentListView.getItems().setAll(comments);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void addComment() {
        String text = commentInput.getText();
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        try {
            commentService.createComment(currentPostId, text);
            commentInput.clear();
            loadComments(); // Atualiza a lista
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Erro ao postar comentário: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
