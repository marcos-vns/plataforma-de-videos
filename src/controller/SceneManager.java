package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import service.AuthenticationService;
import service.ChannelService;
import service.UserChannelService;

import java.io.IOException;
import java.net.URL;

public class SceneManager {

    private static Stage stage;

    // services compartilhados durante toda a sessão
    private static AuthenticationService authenticationService;
    private static ChannelService channelService;
    private static UserChannelService userChannelService;

    public static void init(Stage primaryStage,
                            AuthenticationService authService,
                            ChannelService chService,
                            UserChannelService ucService) {

        stage = primaryStage;
        authenticationService = authService;
        channelService = chService;
        userChannelService = ucService;
    }

    public static void switchScene(String fxml) {

        try {
            URL url = SceneManager.class.getResource(fxml);

            if (url == null) {
                throw new RuntimeException("FXML não encontrado: " + fxml);
            }

            FXMLLoader loader = new FXMLLoader(url);

            // ⭐ ponto crítico
            loader.setControllerFactory(clazz -> {
                try {
                    Object controller =
                            clazz.getDeclaredConstructor().newInstance();

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
            throw new RuntimeException(e);
        }
    }

    private static void injectServices(Object controller) {

        if (controller instanceof DashboardController dc) {
            dc.setServices(
            		channelService,
            		userChannelService,
                    authenticationService
            );
        }

        if (controller instanceof LoginController lc) {
            lc.setAuthenticationService(authenticationService);
        }
    }
}
