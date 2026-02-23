package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.*;
import service.*;

import java.io.File;
import java.sql.SQLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StudioController {

    @FXML private Label channelNameLabel;
    @FXML private Label roleLabel;
    @FXML private ImageView channelProfilePic;
    @FXML private TextField searchField;
    @FXML private Button createPostBtn;
    @FXML private ListView<Post> postsListView;
    
    @FXML private VBox mainDashboard;
    @FXML private VBox ownerPanel;
    @FXML private VBox commentModerationView;
    @FXML private VBox postEditArea;
    
    @FXML private Label moderatingPostTitle;
    @FXML private TextField editTitleField;
    @FXML private TextArea editDescriptionField;
    @FXML private VBox commentsContainer;

    private PostService postService;
    private CommentService commentService;
    private ChannelService channelService;
    private UserChannelService userChannelService;
    private Channel currentChannel;
    private Role userRole;
    private Post selectedPost;
    private List<Post> allPosts = new ArrayList<>();

    public void setPostService(PostService postService) { this.postService = postService; }
    public void setCommentService(CommentService commentService) { this.commentService = commentService; }
    public void setChannelService(ChannelService channelService) { this.channelService = channelService; }
    public void setUserChannelService(UserChannelService userChannelService) { this.userChannelService = userChannelService; }

    public void setChannel(Channel channel) {
        if (channelService != null) {
            try {
                this.currentChannel = channelService.getChannelById(channel.getId());
            } catch (SQLException e) {
                System.err.println("Erro ao carregar canal no StudioController: " + e.getMessage());
                e.printStackTrace();
                this.currentChannel = channel;
            }
        } else {
            this.currentChannel = channel;
        }
        
        ChannelSession.setChannel(this.currentChannel);
        loadUserRole();
        refreshChannelUI();
        loadPosts();
        applyRolePermissions();
    }

    private void loadUserRole() {
        if (currentChannel != null && UserSession.getUser() != null) {
            try {
                userRole = userChannelService.getRole(UserSession.getUser().getId(), currentChannel.getId());
            } catch (SQLException e) {
                System.err.println("Erro ao carregar função do usuário: " + e.getMessage());
                e.printStackTrace();
                userRole = Role.MODERATOR;
            }
            if (userRole == null) userRole = Role.MODERATOR;
        }
    }

    @FXML
    public void initialize() {
        postsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Post item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("[" + item.getPostType() + "] " + item.getTitle());
                }
            }
        });

        postsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                openModeration(newVal);
            }
        });
    }

    private void refreshChannelUI() {
        if (currentChannel != null) {
            channelNameLabel.setText(currentChannel.getName());
            roleLabel.setText("Função: " + userRole.name());
            if (currentChannel.getProfilePictureUrl() != null && !currentChannel.getProfilePictureUrl().isEmpty()) {
                File file = new File("storage", currentChannel.getProfilePictureUrl());
                if (file.exists()) {
                    channelProfilePic.setImage(new Image(file.toURI().toString()));
                }
            }
        }
    }

    private void applyRolePermissions() {
        boolean isOwner = userRole == Role.OWNER;
        boolean isEditor = userRole == Role.EDITOR || isOwner;
        
        ownerPanel.setVisible(isOwner);
        ownerPanel.setManaged(isOwner);
        
        createPostBtn.setVisible(isEditor);
        createPostBtn.setManaged(isEditor);
        
        postEditArea.setVisible(isEditor);
        postEditArea.setManaged(isEditor);
    }

    private void loadPosts() {
        if (currentChannel == null || postService == null) return;
        try {
            allPosts = postService.getPostsByChannel(currentChannel.getId());
            displayRecentPosts();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayRecentPosts() {
        List<Post> recent;
        if (userRole == Role.MODERATOR) {
            recent = allPosts.stream().limit(3).collect(Collectors.toList());
        } else {
            recent = allPosts;
        }
        postsListView.getItems().setAll(recent);
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase();
        List<Post> filtered = allPosts.stream()
                .filter(p -> p.getTitle().toLowerCase().contains(query))
                .collect(Collectors.toList());
        postsListView.getItems().setAll(filtered);
    }

    private void openModeration(Post post) {
        this.selectedPost = post;
        mainDashboard.setVisible(false);
        mainDashboard.setManaged(false);
        commentModerationView.setVisible(true);
        commentModerationView.setManaged(true);
        
        moderatingPostTitle.setText("Moderando: " + post.getTitle());
        editTitleField.setText(post.getTitle());
        
        if (post instanceof Video v) {
            editDescriptionField.setText(v.getDescription());
        } else if (post instanceof TextPost t) {
            editDescriptionField.setText(t.getContent());
        }
        
        loadComments(post.getId());
    }

    @FXML
    private void closeModeration() {
        commentModerationView.setVisible(false);
        commentModerationView.setManaged(false);
        mainDashboard.setVisible(true);
        mainDashboard.setManaged(true);
        postsListView.getSelectionModel().clearSelection();
    }

    private void loadComments(long postId) {
        commentsContainer.getChildren().clear();
        try {
            List<Comment> comments = commentService.getCommentsByPost(postId);
            for (Comment c : comments) {
                commentsContainer.getChildren().add(createCommentNode(c));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createCommentNode(Comment comment) {
        VBox mainBox = new VBox(5);
        mainBox.setStyle("-fx-padding: 10; -fx-border-color: #eee; -fx-border-width: 0 0 1 0;");
        
        HBox box = new HBox(10);
        box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        VBox texts = new VBox(2);
        Label userLabel = new Label(comment.getUsername() + (comment.getParentId() != null ? " (Resposta)" : ""));
        userLabel.setStyle("-fx-font-weight: bold;");
        Label contentLabel = new Label(comment.getText());
        contentLabel.setWrapText(true);
        texts.getChildren().addAll(userLabel, contentLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button replyBtn = new Button("Responder");
        replyBtn.setStyle("-fx-text-fill: #065fd4; -fx-background-color: transparent; -fx-cursor: hand;");
        
        Button deleteBtn = new Button("Excluir");
        deleteBtn.setStyle("-fx-text-fill: red; -fx-background-color: transparent; -fx-cursor: hand;");
        deleteBtn.setVisible(false);
        
        mainBox.setOnMouseEntered(e -> deleteBtn.setVisible(true));
        mainBox.setOnMouseExited(e -> deleteBtn.setVisible(false));
        
        VBox replyArea = new VBox(5);
        replyArea.setVisible(false);
        replyArea.setManaged(false);
        TextField replyInput = new TextField();
        replyInput.setPromptText("Sua resposta como canal...");
        Button btnSend = new Button("Enviar Resposta");
        btnSend.setOnAction(e -> {
            try {
                commentService.createReply(comment.getPostId(), comment.getId(), replyInput.getText());
                loadComments(comment.getPostId());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        replyArea.getChildren().addAll(replyInput, btnSend);
        
        replyBtn.setOnAction(e -> {
            replyArea.setVisible(!replyArea.isVisible());
            replyArea.setManaged(replyArea.isVisible());
        });

        deleteBtn.setOnAction(e -> {
            try {
                commentService.deleteComment(comment.getId());
                loadComments(comment.getPostId());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        
        box.getChildren().addAll(texts, spacer, replyBtn, deleteBtn);
        mainBox.getChildren().addAll(box, replyArea);
        
        HBox wrapper = new HBox(mainBox);
        HBox.setHgrow(mainBox, Priority.ALWAYS);
        return wrapper;
    }

    @FXML
    private void handleUpdatePost() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("Funcionalidade de edição de post será implementada em breve.");
        alert.show();
    }

    @FXML
    private void deleteSelectedPost() {
        if (selectedPost == null) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Excluir Post");
        alert.setHeaderText("Tem certeza que deseja excluir?");
        alert.setContentText(selectedPost.getTitle());
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                postService.deletePost(selectedPost.getId());
                closeModeration();
                loadPosts();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void createPost() {
        SceneManager.showCreatePostScene();
    }

    @FXML
    private void handleAddMember() {
        TextInputDialog userDialog = new TextInputDialog();
        userDialog.setTitle("Adicionar Membro");
        userDialog.setHeaderText("Adicionar membro ao canal: " + currentChannel.getName());
        userDialog.setContentText("Username do usuário:");
        
        userDialog.showAndWait().ifPresent(username -> {
            ChoiceDialog<Role> roleDialog = new ChoiceDialog<>(Role.MODERATOR, List.of(Role.OWNER, Role.EDITOR, Role.MODERATOR));
            roleDialog.setTitle("Adicionar Membro");
            roleDialog.setHeaderText("Selecione o cargo para " + username);
            roleDialog.setContentText("Cargo:");
            
            roleDialog.showAndWait().ifPresent(role -> {
                try {
                    userChannelService.addUserToChannel(username, currentChannel.getId(), role);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Usuário " + username + " adicionado como " + role + " com sucesso!");
                    alert.showAndWait();
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Erro: " + e.getMessage());
                    alert.showAndWait();
                }
            });
        });
    }

    @FXML
    private void handleEditChannel() {
        TextInputDialog dialog = new TextInputDialog(currentChannel.getName());
        dialog.setTitle("Editar Canal");
        dialog.setHeaderText("Alterar nome do canal");
        dialog.setContentText("Novo nome:");
        dialog.showAndWait().ifPresent(newName -> {
            System.out.println("Updating channel name to: " + newName);
        });
    }

    @FXML
    private void handleDeleteChannel() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Excluir Canal");
        alert.setHeaderText("CUIDADO: Isso excluirá o canal e todos os seus vídeos!");
        alert.setContentText("Deseja continuar?");
        if (alert.showAndWait().get() == ButtonType.OK) {
            System.out.println("Deleting channel...");
        }
    }

    @FXML
    private void backToDashboard() {
        SceneManager.switchScene("/app/view/dashboard.fxml");
    }
}
