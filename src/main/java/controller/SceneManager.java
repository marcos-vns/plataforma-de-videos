package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.AuthenticationService;
import service.ChannelService;
import service.UserChannelService;
import service.FileService;
import service.PostService;
import service.CommentService;
import service.WatchHistoryService;
import service.ChannelSession;
import controller.PostController;
import controller.HistoryController;
import model.Channel;

import java.io.IOException;
import java.net.URL;

public class SceneManager {

    private static Stage stage;

    private static AuthenticationService authenticationService;
    private static ChannelService channelService;
    private static UserChannelService userChannelService;
    private static PostService postService;
    private static FileService fileService;
    private static CommentService commentService;
    private static WatchHistoryService watchHistoryService;
    private static service.UserService userService; // New static field

    public static void init(Stage primaryStage,
                            AuthenticationService authService,
                            ChannelService chService,
                            UserChannelService ucService,
                            PostService pService,
                            FileService fService,
                            CommentService cService,
                            WatchHistoryService whService,
                            service.UserService usService) { // New parameter

        stage = primaryStage;
        authenticationService = authService;
        channelService = chService;
        userChannelService = ucService;
        postService = pService;
        fileService = fService;
        commentService = cService;
        watchHistoryService = whService;
        userService = usService; // Assign the new service
    }

    private static URL getFXMLUrl(String fxmlPath) {
        URL url = SceneManager.class.getResource(fxmlPath);
        if (url == null && fxmlPath.startsWith("/")) {
            url = SceneManager.class.getResource(fxmlPath.substring(1));
        }
        return url;
    }

    public static void switchScene(String fxml) {
        try {
            URL url = getFXMLUrl(fxml);

            if (url == null) {
                throw new RuntimeException("FXML não encontrado: " + fxml);
            }

            FXMLLoader loader = new FXMLLoader(url);
            loader.setControllerFactory(clazz -> {
                try {
                    Object controller = clazz.getDeclaredConstructor().newInstance();
                    injectServices(controller);
                    return controller;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar cena: " + fxml, e);
        }
    }

    public static void showCommentScene() {
        switchScene("/app/view/comment.fxml");
    }

    public static void showCreatePostScene() {
        switchScene("/app/view/create_post.fxml");
    }

    public static void showStudioScene(Channel channel) {
        try {
            URL url = getFXMLUrl("/app/view/studio.fxml");

            if (url == null) {
                throw new RuntimeException("FXML não encontrado: /app/view/studio.fxml");
            }

            FXMLLoader loader = new FXMLLoader(url);
            loader.setControllerFactory(clazz -> {
                try {
                    Object controller = clazz.getDeclaredConstructor().newInstance();
                    injectServices(controller);
                    return controller;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();
            StudioController studioController = loader.getController();
            if (studioController != null) {
                ChannelSession.setChannel(channel);
                studioController.setChannel(channel);
            }

            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showPostScene(long postId) {
        try {
            URL url = getFXMLUrl("/app/view/post.fxml");

            if (url == null) {
                throw new RuntimeException("FXML não encontrado: /app/view/post.fxml");
            }

            FXMLLoader loader = new FXMLLoader(url);
            loader.setControllerFactory(clazz -> {
                try {
                    Object controller = clazz.getDeclaredConstructor().newInstance();
                    injectServices(controller);
                    return controller;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();
            PostController postController = loader.getController();
            if (postController != null) {
                postController.setPost(postId);
            }

            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showChannelScene(Channel channel) {
        try {
            URL url = getFXMLUrl("/app/view/channel.fxml");

            if (url == null) {
                throw new RuntimeException("FXML não encontrado: /app/view/channel.fxml");
            }

            FXMLLoader loader = new FXMLLoader(url);
            loader.setControllerFactory(clazz -> {
                try {
                    Object controller = clazz.getDeclaredConstructor().newInstance();
                    injectServices(controller);
                    return controller;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();
            ChannelController channelController = loader.getController();
            if (channelController != null) {
                channelController.setChannel(channel);
            }

            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showCreateChannelDialog() {
        try {
            URL url = getFXMLUrl("/app/view/create_channel.fxml");

            if (url == null) {
                throw new RuntimeException("FXML não encontrado: /app/view/create_channel.fxml");
            }

            FXMLLoader loader = new FXMLLoader(url);
            loader.setControllerFactory(clazz -> {
                try {
                    Object controller = clazz.getDeclaredConstructor().newInstance();
                    injectServices(controller);
                    return controller;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Criar Canal");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            dialogStage.showAndWait();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showHistoryScene() {
        switchScene("/app/view/history.fxml");
    }

    private static void injectServices(Object controller) {
        if (controller instanceof DashboardController dc) {
            dc.setServices(channelService, userChannelService, authenticationService, postService, userService);
        }
        if (controller instanceof LoginController lc) {
            lc.setAuthenticationService(authenticationService);
        }
        if (controller instanceof CreatePostController cpc) {
            cpc.setPostService(postService);
            cpc.setFileService(fileService);
        }
        if (controller instanceof PostController pco) {
            pco.setServices(postService, commentService, watchHistoryService);
        }
        if (controller instanceof CommentController cc) {
            cc.setCommentService(commentService);
        }
        if (controller instanceof StudioController sc) {
            sc.setPostService(postService);
            sc.setCommentService(commentService);
            sc.setChannelService(channelService);
            sc.setUserChannelService(userChannelService);
            sc.setFileService(fileService); // Added
        }
        if (controller instanceof RegisterController rc) {
            rc.setFileService(fileService);
            rc.setUserService(userService);
        }
        if (controller instanceof ChannelController cc) {
            cc.setServices(postService, channelService, userChannelService);
        }
        if (controller instanceof CreateChannelController ccc) {
            ccc.setChannelService(channelService);
            ccc.setFileService(fileService);
            ccc.setUserChannelService(userChannelService);
        }
        if (controller instanceof HistoryController hc) {
            hc.setWatchHistoryService(watchHistoryService);
            hc.setChannelService(channelService);
        }
    }
}
