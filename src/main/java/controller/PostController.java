package controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import service.WatchHistoryService;
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
    @FXML private HBox textContentArea;
    @FXML private ImageView textThumbnail;
    @FXML private TextArea postDescription;
    @FXML private VBox commentsContainer;
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
    @FXML private Label viewsLabel;
    @FXML private Button btnLike;
    @FXML private Button btnDislike;
    @FXML private TextField commentInput;

    private PostService postService;
    private CommentService commentService;
    private WatchHistoryService watchHistoryService;
    private MediaPlayer mediaPlayer;
    private long currentPostId;

    public void setServices(PostService postService, CommentService commentService, WatchHistoryService watchHistoryService) {
        this.postService = postService;
        this.commentService = commentService;
        this.watchHistoryService = watchHistoryService;
    }

    public void setPost(long postId) {
        this.currentPostId = postId;
        System.out.println("PostController.setPost called with postId: " + postId);
        if (UserSession.getUser() != null) {
            System.out.println("UserSession.getUser() is not null. User ID: " + UserSession.getUser().getId());
            watchHistoryService.addPostToHistory(UserSession.getUser().getId(), postId);
        } else {
            System.out.println("UserSession.getUser() is null. Cannot add to watch history.");
        }
        loadPostData();
        refreshReactionUI();
        loadComments();
    }

    private void loadPostData() {
        try {
            postService.incrementViews(currentPostId);

            Post post = postService.getPostById(currentPostId);
            if (post != null) {
                postTitle.setText(post.getTitle());
                likesLabel.setText(String.valueOf(post.getLikes()));
                dislikesLabel.setText(String.valueOf(post.getDislikes()));
                
                if (post instanceof Video video) {
                    viewsLabel.setText("👁 " + video.getViews() + " visualizações");
                    viewsLabel.setVisible(true);
                    viewsLabel.setManaged(true);
                    setupVideo(video);
                } else if (post instanceof TextPost textPost) {
                    viewsLabel.setVisible(false);
                    viewsLabel.setManaged(false);
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
            
            Post post = postService.getPostById(currentPostId);
            likesLabel.setText(String.valueOf(post.getLikes()));
            dislikesLabel.setText(String.valueOf(post.getDislikes()));

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
        textContentArea.setVisible(false);
        textContentArea.setManaged(false);
        videoControls.setVisible(true);

        if (video.getVideoUrl() != null) {
            System.out.println("DEBUG: Video URL: " + video.getVideoUrl());
            try {
                File videoFile = new File("storage", video.getVideoUrl());
                System.out.println("DEBUG: Video file path: " + videoFile.getAbsolutePath());
                System.out.println("DEBUG: Video file exists: " + videoFile.exists());
                if (videoFile.exists()) {
                    Media media = new Media(videoFile.toURI().toString());
                    mediaPlayer = new MediaPlayer(media);
                    mediaView.setMediaPlayer(mediaPlayer);

                    mediaPlayer.setOnReady(() -> {
                        double videoWidth = media.getWidth();
                        double videoHeight = media.getHeight();
                        
                        double maxWidth = 900;
                        double displayWidth = Math.min(videoWidth, maxWidth);
                        double displayHeight = (displayWidth / videoWidth) * videoHeight;

                        mediaView.setFitWidth(displayWidth);
                        mediaView.setFitHeight(displayHeight);

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
                System.err.println("DEBUG: Exceção ao carregar vídeo: " + e.getMessage());
                e.printStackTrace();
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
        textContentArea.setVisible(true);
        textContentArea.setManaged(true);
        videoControls.setVisible(false);
        txtContent.setText(text.getContent());

        if (text.getThumbnailUrl() != null && !text.getThumbnailUrl().isEmpty()) {
            File file = new File("storage", text.getThumbnailUrl());
            if (file.exists()) {
                textThumbnail.setImage(new Image(file.toURI().toString()));
                textThumbnail.setVisible(true);
                textThumbnail.setManaged(true);
            } else {
                textThumbnail.setVisible(false);
                textThumbnail.setManaged(false);
            }
        } else {
            textThumbnail.setVisible(false);
            textThumbnail.setManaged(false);
        }
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
        textThumbnail.setImage(null);
        
        if (ChannelSession.hasSelectedChannel()) {
            SceneManager.showStudioScene(ChannelSession.getChannel());
        } else {
            SceneManager.switchScene("/app/view/dashboard.fxml");
        }
    }

    private void loadComments() {
        try {
            commentsContainer.getChildren().clear();
            java.util.List<Comment> allComments = commentService.getCommentsByPost(currentPostId);
            
            java.util.Map<Long, java.util.List<Comment>> childrenMap = new java.util.HashMap<>();
            java.util.List<Comment> rootComments = new java.util.ArrayList<>();
            
            for (Comment c : allComments) {
                if (c.getParentId() == null || c.getParentId() == 0) {
                    rootComments.add(c);
                } else {
                    childrenMap.computeIfAbsent(c.getParentId(), k -> new java.util.ArrayList<>()).add(c);
                }
            }
            
            for (Comment root : rootComments) {
                renderComment(root, childrenMap, 0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void renderComment(Comment comment, java.util.Map<Long, java.util.List<Comment>> childrenMap, int depth) {
        VBox commentBox = new VBox(5);
        commentBox.setPadding(new Insets(10, 0, 10, depth * 20));
        commentBox.setStyle("-fx-border-color: #eee; -fx-border-width: 0 0 1 0;");
        
        Label userLabel = new Label(comment.getUsername());
        userLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        Label contentLabel = new Label(comment.getText());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 14px;");
        
        Button btnReply = new Button("Responder");
        btnReply.setStyle("-fx-background-color: transparent; -fx-text-fill: #065fd4; -fx-cursor: hand; -fx-padding: 2 0 2 0; -fx-font-weight: bold;");
        
        VBox replyArea = new VBox(5);
        replyArea.setVisible(false);
        replyArea.setManaged(false);
        
        TextField replyInput = new TextField();
        replyInput.setPromptText("Adicione uma resposta...");
        Button btnSendReply = new Button("Responder");
        btnSendReply.setStyle("-fx-background-color: #065fd4; -fx-text-fill: white;");
        
        btnSendReply.setOnAction(e -> {
            String text = replyInput.getText();
            if (text != null && !text.trim().isEmpty()) {
                try {
                    commentService.createReply(currentPostId, comment.getId(), text);
                    loadComments();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        replyArea.getChildren().addAll(replyInput, btnSendReply);
        
        btnReply.setOnAction(e -> {
            replyArea.setVisible(!replyArea.isVisible());
            replyArea.setManaged(replyArea.isVisible());
        });
        
        commentBox.getChildren().addAll(userLabel, contentLabel, btnReply, replyArea);
        commentsContainer.getChildren().add(commentBox);
        
        if (childrenMap.containsKey(comment.getId())) {
            for (Comment child : childrenMap.get(comment.getId())) {
                renderComment(child, childrenMap, depth + 1);
            }
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
            loadComments();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Erro ao postar comentário: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
