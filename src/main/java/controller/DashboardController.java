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
import controller.PostCardController;
import controller.ChannelCardController;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DashboardController {

    @FXML private TextField searchField;
    @FXML private MenuButton userMenuButton;
    @FXML private Menu channelsSubMenu;
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
        // Set default search filter
        searchFilterChoiceBox.setValue("Todos");
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
            System.out.println("DEBUG: Loading channels for user ID: " + userId);
            var channels = channelService.findChannelsByUser(userId);
            System.out.println("DEBUG: Found " + channels.size() + " channels for user.");
            for (Channel channel : channels) {
                System.out.println("DEBUG: Adding channel to menu: " + channel.getName() + " (ID: " + channel.getId() + ")");
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
        
        // Clear previous results
        postsGrid.getChildren().clear();
        channelsGrid.getChildren().clear();
        channelsSection.setVisible(false);
        channelsSection.setManaged(false);
        postsGrid.setVisible(false); // Hide posts grid by default
        postsGrid.setManaged(false);

        if (query.isEmpty()) {
            loadAllPosts(); // This will load posts and set postsGrid visible
            sectionTitle.setText("Recomendados");
            return;
        }

        try {
            boolean foundResults = false;
            // Search Channels
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

            // Search Posts
            if ("Todos".equals(filter) || "Posts".equals(filter)) {
                List<Post> allPosts = postService.getAllPosts(); // Assuming this gets all posts to filter locally
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
        // By default, if they click "Studio" in the main menu, maybe show a channel picker or enter the first channel
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
    }

    @FXML
    private void logout() {
    	authenticationService.logout();
    	if(UserSession.getUser() == null) {
    		SceneManager.switchScene("/app/view/login.fxml");
    	}
    }
}
