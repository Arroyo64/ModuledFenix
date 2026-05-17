package org.ies.fenix.client.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.ies.fenix.client.controller.BaseLayoutController;
import org.ies.fenix.client.controller.NavbarController;
import org.ies.fenix.client.listener.SceneResizeListener;
import org.ies.fenix.controller.IClientController;
import org.ies.fenix.controller.dto.client.ClientInfoDTO;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Objects;

public class StageManager {

    private final Stage primaryStage;
    private final FxmlLoader fxmlLoader;
    private final String applicationTitle;
    private final SceneResizeListener sceneResizeListener;
    private FxmlView currentView;
    private FxmlView previousView;
    private BaseLayoutController baseLayoutController;

    public StageManager(FxmlLoader fxmlLoader,
                        Stage primaryStage,
                        String applicationTitle,
                        SceneResizeListener sceneResizeListener) {
        this.primaryStage = primaryStage;
        this.fxmlLoader = fxmlLoader;
        this.applicationTitle = applicationTitle;
        this.sceneResizeListener = sceneResizeListener;
    }

    public void switchScene(final FxmlView view) {
        previousView = currentView;
        currentView = view;

        Parent rootNode;

        try {
            if (view.usesBaseLayout()) {

                FXMLLoader baseLoader = fxmlLoader.createLoader("/fxml/base-layout.fxml");
                Parent baseRoot = baseLoader.load();
                this.baseLayoutController = baseLoader.getController();

                // 2. Guardar el controlador para acceso global
                this.baseLayoutController = baseLoader.getController();

                // 3. Cargar contenido dinámico
                FXMLLoader contentLoader = fxmlLoader.createLoader(view.getFxmlPath());
                Parent content = contentLoader.load();

                // 4. Insertar contenido
                baseLayoutController.setContent(content);

                // 5. Actualizar navbar dinámico
                baseLayoutController.setActiveView(view);

                rootNode = baseRoot;
            } else {
                // Cargar vista externa
                FXMLLoader loader = fxmlLoader.createLoader(view.getFxmlPath());
                rootNode = loader.load();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        createAndSetScene(rootNode);

        rootNode.applyCss();
        rootNode.autosize();

        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private void createAndSetScene(Parent rootNode) {
        Scene scene = new Scene(rootNode);

        String stylesheet = Objects.requireNonNull(getClass()
                .getResource("/styles/styles.css")).toExternalForm();

        scene.getStylesheets().add(stylesheet);

        scene.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> {
            if (sceneResizeListener != null) {
                sceneResizeListener.onSceneResized(newSceneWidth);
            }
        });

        primaryStage.setTitle(applicationTitle);
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        primaryStage.setScene(scene);
    }

    public void goBack() {
        if (previousView == null) {
            switchScene(FxmlView.MARKETPLACE);
            return;
        }
        switchScene(previousView);
    }

    public void reloadCurrentScene() {
        if (currentView == null) {
            throw new IllegalStateException("No hay vista cargada");
        }
        switchScene(currentView);
    }

    public BaseLayoutController getBaseLayoutController() {
        return baseLayoutController;
    }

    public void switchToFullScreenMode() {
        primaryStage.setFullScreen(true);
    }

    public void switchToWindowedMode() {
        primaryStage.setFullScreen(false);
    }

    public boolean isStageFullScreen() {
        return primaryStage.isFullScreen();
    }

    public void exit() {
        primaryStage.close();
    }
    public <T> T switchSceneAndGetController(final FxmlView view) {
        previousView = currentView;
        currentView = view;

        try {
            Parent rootNode;
            T controller;

            if (view.usesBaseLayout()) {

                // Cargar BaseLayout
                FXMLLoader baseLoader = fxmlLoader.createLoader("/fxml/base-layout.fxml");
                Parent baseRoot = baseLoader.load();
                this.baseLayoutController = baseLoader.getController();
                BaseLayoutController baseController = this.baseLayoutController;

                // Cargar contenido dinámico
                FXMLLoader contentLoader = fxmlLoader.createLoader(view.getFxmlPath());
                Parent content = contentLoader.load();

                // Insertar contenido
                baseController.setContent(content);
                baseController.setActiveView(view);

                // Obtener controlador REAL de la vista interna
                controller = contentLoader.getController();

                rootNode = baseRoot;

            } else {

                // Vista externa normal
                FXMLLoader loader = fxmlLoader.createLoader(view.getFxmlPath());
                rootNode = loader.load();
                controller = loader.getController();
            }

            createAndSetScene(rootNode);

            rootNode.applyCss();
            rootNode.autosize();

            primaryStage.sizeToScene();
            primaryStage.centerOnScreen();
            primaryStage.show();

            return controller;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
