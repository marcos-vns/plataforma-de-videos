package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import model.Channel;
import model.Post;
import model.Video;
import model.Role;
import service.AuthenticationService;
import service.ChannelService;
import service.UserSession;
import service.UserChannelService;
import service.PostService;
import service.UserService;
import controller.PostCardController;
import controller.ChannelCardController;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DashboardController {

    @FXML private TextField searchField;
    @FXML private MenuButton userMenuButton;
    @FXML private Menu channelsSubMenu;
    @FXML private Button loginButton; // Added
    @FXML private MenuButton createOrAddMemberMenuButton; // Added
    @FXML private FlowPane postsGrid;
    @FXML private VBox channelsSection;
    @FXML private FlowPane channelsGrid;
    @FXML private Label sectionTitle;
    @FXML private ChoiceBox<String> searchFilterChoiceBox;
    @FXML private javafx.scene.image.ImageView userProfilePic;

    private ChannelService channelService;
    private UserChannelService userChannelService;
    private AuthenticationService authenticationService;
    private PostService postService;
    private UserService userService; // New field
    
    public void setServices(ChannelService channel,
            UserChannelService userChannel,
            AuthenticationService auth,
            PostService postService,
            UserService userService) { // Updated method signature
		this.channelService = channel;
		this.userChannelService = userChannel;
		this.authenticationService = auth;
        this.postService = postService;
        this.userService = userService; // New assignment
    }

    @FXML
    public void initialize() {
        searchFilterChoiceBox.setValue("Todos");
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
            userMenuButton.setVisible(true);
            userMenuButton.setManaged(true);
            userProfilePic.setVisible(true);
            userProfilePic.setManaged(true);
            createOrAddMemberMenuButton.setVisible(true);
            createOrAddMemberMenuButton.setManaged(true);

            loginButton.setVisible(false);
            loginButton.setManaged(false);
        } else {
            userMenuButton.setVisible(false);
            userMenuButton.setManaged(false);
            userProfilePic.setVisible(false);
            userProfilePic.setManaged(false);
            createOrAddMemberMenuButton.setVisible(false);
            createOrAddMemberMenuButton.setManaged(false);

            loginButton.setVisible(true);
            loginButton.setManaged(true);
        }
        loadAllPosts();
    }

    @FXML
    private void handleLogin() {
        SceneManager.switchScene("/app/view/login.fxml");
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
                Channel channel = null;
                try {
                    channel = channelService.getChannelById(post.getChannelId());
                } catch (SQLException e) {
                    System.err.println("Erro ao obter canal para post " + post.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
                controller.setData(post, channel);
                
                postsGrid.getChildren().add(card);
            } catch (Exception e) {
                System.err.println("DashboardController: Erro ao carregar card para post " + post.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void displayChannels(List<Channel> channels) {
        channelsGrid.getChildren().clear();
        for (Channel channel : channels) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/view/channel_card.fxml"));
                VBox card = loader.load();

                ChannelCardController controller = loader.getController();
                if (controller == null) {
                    System.err.println("DashboardController: Falha ao obter controller do ChannelCard!");
                    continue;
                }
                controller.setData(channel);
                
                channelsGrid.getChildren().add(card);
            } catch (Exception e) {
                System.err.println("DashboardController: Erro ao carregar card para canal " + channel.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void loadUserChannelsMenu() {
        channelsSubMenu.getItems().clear();
        try {
            long userId = UserSession.getUser().getId();
            var allAssociatedChannels = channelService.findChannelsByUser(userId);
            
            // Filter channels to only include those where the user is an owner, editor, or moderator
            var managedChannels = allAssociatedChannels.stream()
                .filter(channel -> {
                    Role userRole = channel.getCurrentUserRole();
                    return userRole == Role.OWNER || userRole == Role.EDITOR || userRole == Role.MODERATOR;
                })
                .toList();

            for (Channel channel : managedChannels) {
                String label = channel.getName();
                if (channel.getCurrentUserRole() != null) {
                    label += " (" + channel.getCurrentUserRole().name() + ")";
                }
                MenuItem item = new MenuItem(label);
                item.setOnAction(e -> SceneManager.showStudioScene(channel));
                channelsSubMenu.getItems().add(item);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao carregar canais do usuário: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase();
        String filter = searchFilterChoiceBox.getValue();
        
        postsGrid.getChildren().clear();
        channelsGrid.getChildren().clear();
        channelsSection.setVisible(false);
        channelsSection.setManaged(false);
        postsGrid.setVisible(false);
        postsGrid.setManaged(false);

        if (query.isEmpty()) {
            loadAllPosts();
            sectionTitle.setText("Recomendados");
            return;
        }

        try {
            boolean foundResults = false;
            if ("Todos".equals(filter) || "Canais".equals(filter)) {
                List<Channel> foundChannels = null;
                try {
                    foundChannels = channelService.searchChannels(query);
                } catch (SQLException e) {
                    System.err.println("Erro ao buscar canais: " + e.getMessage());
                    e.printStackTrace();
                    sectionTitle.setText("Erro na pesquisa de canais.");
                    return;
                }
                if (!foundChannels.isEmpty()) {
                    displayChannels(foundChannels);
                    channelsSection.setVisible(true);
                    channelsSection.setManaged(true);
                    foundResults = true;
                }
            }

            if ("Todos".equals(filter) || "Posts".equals(filter)) {
                List<Post> allPosts = postService.getAllPosts();
                List<Post> filteredPosts = allPosts.stream()
                        .filter(p -> {
                            String postDescription = "";
                            if (p instanceof model.Video videoPost) {
                                postDescription = videoPost.getDescription() != null ? videoPost.getDescription() : "";
                            }
                            return p.getTitle().toLowerCase().contains(query) || postDescription.toLowerCase().contains(query);
                        })
                        .toList();
                if (!filteredPosts.isEmpty()) {
                    displayPosts(filteredPosts);
                    postsGrid.setVisible(true);
                    postsGrid.setManaged(true);
                    foundResults = true;
                }
            }

            if (!foundResults) {
                sectionTitle.setText("Nenhum resultado encontrado para: " + query);
                postsGrid.setVisible(false);
                postsGrid.setManaged(false);
                channelsSection.setVisible(false);
                channelsSection.setManaged(false);
            } else {
                 sectionTitle.setText("Resultados da Pesquisa para: " + query);
            }
           
        } catch (Exception e) {
            System.err.println("Erro ao realizar pesquisa: " + e.getMessage());
            e.printStackTrace();
            sectionTitle.setText("Erro na pesquisa.");
        }
    }

    @FXML
    private void handleAddMember() {
        if (UserSession.getUser() == null) return;
        
        List<Channel> channels = null;
        try {
            channels = channelService.findChannelsByUser(UserSession.getUser().getId());
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erro ao carregar canais: " + e.getMessage()).showAndWait();
            return;
        }

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
        if (UserSession.getUser() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Você precisa estar logado para ver o histórico.");
            alert.showAndWait();
            return;
        }
        SceneManager.showHistoryScene();
    }

    @FXML
    private void enterStudio() {
        List<Channel> channels = null;
        try {
            channels = channelService.findChannelsByUser(UserSession.getUser().getId());
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erro ao carregar canais: " + e.getMessage()).showAndWait();
            return;
        }

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
        SceneManager.showCreateChannelDialog();
        loadUserChannelsMenu();
    }

    @FXML
    private void logout() {
    	authenticationService.logout();
    	if(UserSession.getUser() == null) {
    		SceneManager.switchScene("/app/view/login.fxml");
    	}
    }

    @FXML
    private void handleDeleteAccount() {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmar Exclusão de Conta");
        confirmationAlert.setHeaderText("Você está prestes a excluir sua conta permanentemente.");
        confirmationAlert.setContentText("Esta ação não pode ser desfeita. Tem certeza que deseja continuar?");

        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    long userId = UserSession.getUser().getId();
                    userService.deleteUser(userId);
                    authenticationService.logout();
                    SceneManager.switchScene("/app/view/login.fxml");
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Sucesso");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Sua conta foi excluída com sucesso.");
                    successAlert.showAndWait();
                } catch (Exception e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Erro");
                    errorAlert.setHeaderText("Erro ao excluir conta.");
                    errorAlert.setContentText("Ocorreu um erro ao tentar excluir sua conta: " + e.getMessage());
                    errorAlert.showAndWait();
                    e.printStackTrace();
                }
            }
        });
    }

}
