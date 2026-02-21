package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import model.Channel;
import model.Post;
import model.Role;
import service.AuthenticationService;
import service.ChannelService;
import service.UserSession;
import service.UserChannelService;
import service.PostService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DashboardController {

    @FXML private TextField searchField;
    @FXML private MenuButton userMenuButton;
    @FXML private Menu channelsSubMenu;
    @FXML private FlowPane postsGrid;
    @FXML private Label sectionTitle;
    @FXML private javafx.scene.image.ImageView userProfilePic;

    private ChannelService channelService;
    private UserChannelService userChannelService;
    private AuthenticationService authenticationService;
    private PostService postService;
    
    public void setServices(ChannelService channel,
            UserChannelService userChannel,
            AuthenticationService auth,
            PostService postService) {
		this.channelService = channel;
		this.userChannelService = userChannel;
		this.authenticationService = auth;
        this.postService = postService;
    }

    @FXML
    public void initialize() {
        // We wait for services to be injected before loading data
        // However, in our SceneManager, services are injected in the factory
        // so they should be ready here, along with @FXML fields.
        loadInitialData();
    }

    private void loadInitialData() {
        if (UserSession.getUser() != null) {
            userMenuButton.setText(UserSession.getUser().getUsername());
            
            String picUrl = UserSession.getUser().getProfilePictureUrl();
            if (picUrl != null && !picUrl.isEmpty()) {
                java.io.File file = new java.io.File("storage", picUrl);
                if (file.exists()) {
                    userProfilePic.setImage(new javafx.scene.image.Image(file.toURI().toString()));
                }
            }
            
            loadUserChannelsMenu();
        }
        loadAllPosts();
    }

    private void loadAllPosts() {
        try {
            if (postService == null) {
                System.err.println("DashboardController: postService is NULL!");
                return;
            }
            List<Post> posts = postService.getAllPosts();
            System.out.println("DashboardController: Carregados " + posts.size() + " posts.");
            displayPosts(posts);
        } catch (SQLException e) {
            System.err.println("DashboardController: Erro ao buscar posts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayPosts(List<Post> posts) {
        postsGrid.getChildren().clear();
        for (Post post : posts) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/view/post_card.fxml"));
                VBox card = loader.load();
                
                PostCardController controller = loader.getController();
                if (controller == null) {
                    System.err.println("DashboardController: Falha ao obter controller do PostCard!");
                    continue;
                }
                Channel channel = channelService.getChannelById(post.getChannelId());
                controller.setData(post, channel);
                
                postsGrid.getChildren().add(card);
            } catch (Exception e) {
                System.err.println("DashboardController: Erro ao carregar card para post " + post.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void loadUserChannelsMenu() {
        channelsSubMenu.getItems().clear();
        var channels = channelService.findChannelsByUser(UserSession.getUser().getId());
        for (Channel channel : channels) {
            String label = channel.getName();
            if (channel.getCurrentUserRole() != null) {
                label += " (" + channel.getCurrentUserRole().name() + ")";
            }
            MenuItem item = new MenuItem(label);
            item.setOnAction(e -> SceneManager.showStudioScene(channel));
            channelsSubMenu.getItems().add(item);
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase();
        if (query.isEmpty()) {
            loadAllPosts();
            sectionTitle.setText("Recomendados");
            return;
        }

        try {
            List<Post> allPosts = postService.getAllPosts();
            List<Post> filtered = allPosts.stream()
                    .filter(p -> p.getTitle().toLowerCase().contains(query))
                    .toList();
            displayPosts(filtered);
            sectionTitle.setText("Resultados para: " + query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddMember() {
        if (UserSession.getUser() == null) return;
        
        var channels = channelService.findChannelsByUser(UserSession.getUser().getId());
        if (channels.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Você não possui canais para gerenciar.");
            alert.showAndWait();
            return;
        }

        ChoiceDialog<Channel> channelDialog = new ChoiceDialog<>(channels.get(0), channels);
        channelDialog.setTitle("Adicionar Membro");
        channelDialog.setHeaderText("Selecione o canal:");
        channelDialog.setContentText("Canal:");
        
        channelDialog.showAndWait().ifPresent(channel -> {
            TextInputDialog userDialog = new TextInputDialog();
            userDialog.setTitle("Adicionar Membro");
            userDialog.setHeaderText("Adicionar membro ao canal: " + channel.getName());
            userDialog.setContentText("Username do usuário:");
            
            userDialog.showAndWait().ifPresent(username -> {
                ChoiceDialog<Role> roleDialog = new ChoiceDialog<>(Role.EDITOR, List.of(Role.OWNER, Role.EDITOR, Role.MODERATOR));
                roleDialog.setTitle("Adicionar Membro");
                roleDialog.setHeaderText("Selecione o cargo para " + username);
                roleDialog.setContentText("Cargo:");
                
                roleDialog.showAndWait().ifPresent(role -> {
                    try {
                        userChannelService.addUserToChannel(username, channel.getId(), role);
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
        });
    }

    @FXML
    private void viewHistory() {
        // Placeholder for history logic
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Histórico");
        alert.setHeaderText(null);
        alert.setContentText("Funcionalidade de histórico será implementada em breve.");
        alert.showAndWait();
    }

    @FXML
    private void enterStudio() {
        // By default, if they click "Studio" in the main menu, maybe show a channel picker or enter the first channel
        var channels = channelService.findChannelsByUser(UserSession.getUser().getId());
        if (!channels.isEmpty()) {
            SceneManager.showStudioScene(channels.get(0));
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Você não possui canais. Crie um no estúdio.");
            alert.showAndWait();
        }
    }
    
    @FXML
    private void handleCreateChannel() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Criar Canal");
        dialog.setHeaderText("Crie seu novo canal");
        dialog.setContentText("Nome do canal:");

        dialog.showAndWait().ifPresent(name -> {
            if (name.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("O nome do canal não pode estar vazio.");
                alert.showAndWait();
                return;
            }

            try {
                channelService.create(name, null); // For now, no profile pic during creation
                loadUserChannelsMenu(); // Refresh menu
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Canal '" + name + "' criado com sucesso!");
                alert.showAndWait();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Erro ao criar canal: " + e.getMessage());
                alert.showAndWait();
            }
        });
    }

    @FXML
    private void logout() {
    	authenticationService.logout();
    	if(UserSession.getUser() == null) {
    		SceneManager.switchScene("/app/view/login.fxml");
    	}
    }
}
