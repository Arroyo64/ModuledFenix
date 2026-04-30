package org.ies.fenix.client;

import javafx.application.Application;
import javafx.stage.Stage;
import org.ies.fenix.client.api.SessionManager;
import org.ies.fenix.client.config.FxmlLoader;
import org.ies.fenix.client.config.FxmlView;
import org.ies.fenix.client.config.StageManager;
import org.ies.fenix.client.controller.*;
import org.ies.fenix.client.listener.SceneResizeListener;
import org.ies.fenix.controller.IClientController;
import org.ies.fenix.controller.IGameController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

public class Main extends Application {

    private static Stage stage;

    private StageManager stageManager;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;

        SceneResizeListener resizeListener = newWidth -> {
            System.out.println("Scene resized: " + newWidth);
        };

        FxmlLoader fxmlLoader = new FxmlLoader();
        String applicationTitle = "Fenix";

        RestClient rc = RestClient.create("http://localhost:8080");
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(rc))
                .build();
        var  clientApiService = factory.createClient(IClientController.class);
        var  gamesApiService = factory.createClient(IGameController.class);
        //ClientApiService clientApiService = new ClientApiService();
        SessionManager sessionManager = new SessionManager();

        stageManager = new StageManager(
                fxmlLoader,
                primaryStage,
                applicationTitle,
                resizeListener
        );

        fxmlLoader.setControllerFactory(clazz -> {
            if (clazz == ClientController.class) {
                return new ClientController(stageManager, clientApiService, sessionManager);
            }

            if (clazz == EmailFormController.class) {
                return new EmailFormController(stageManager);
            }

            if (clazz == HomeController.class) {
                return new HomeController(stageManager);
            }

            if (clazz == MarketplaceController.class) {
                return new MarketplaceController(stageManager, clientApiService, sessionManager);
            }

            if (clazz == LibraryController.class) {
                return new LibraryController(stageManager, clientApiService, sessionManager);
            }
            if (clazz == ProfileController.class) {
                return new ProfileController(stageManager, clientApiService, sessionManager);
            }
            if (clazz == GameController.class) {
                return new GameController(stageManager, clientApiService, sessionManager);
            }

            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Cannot instantiate controller: " + clazz.getName(), e);
            }
        });

        showLoginScene();
    }

    @Override
    public void stop() {
        if (stage != null) {
            stage.close();
        }
    }

    private void showLoginScene() {
        stageManager.switchScene(FxmlView.LOGIN);
    }
}
