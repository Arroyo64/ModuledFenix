package org.ies.fenix.client.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.ies.fenix.client.controller.BaseLayoutController;
import org.ies.fenix.client.controller.GameController;
import org.ies.fenix.client.listener.SceneResizeListener;

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

    private Integer currentGameId;

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

                FXMLLoader contentLoader = fxmlLoader.createLoader(view.getFxmlPath());
                Parent content = contentLoader.load();

                Object controller = contentLoader.getController();

                if (view == FxmlView.GAME && controller instanceof GameController gameController) {
                    if (currentGameId != null) {
                        gameController.setSelectedGameId(currentGameId);
                    }
                }

                baseLayoutController.setContent(content);
                baseLayoutController.setActiveView(view);

                rootNode = baseRoot;

            } else {
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

    public <T> T switchSceneAndGetController(final FxmlView view) {
        previousView = currentView;
        currentView = view;

        try {
            Parent rootNode;
            T controller;

            if (view.usesBaseLayout()) {

                FXMLLoader baseLoader = fxmlLoader.createLoader("/fxml/base-layout.fxml");
                Parent baseRoot = baseLoader.load();
                this.baseLayoutController = baseLoader.getController();

                FXMLLoader contentLoader = fxmlLoader.createLoader(view.getFxmlPath());
                Parent content = contentLoader.load();

                baseLayoutController.setContent(content);
                baseLayoutController.setActiveView(view);

                controller = contentLoader.getController();

                if (view == FxmlView.GAME && controller instanceof GameController gameController) {
                    if (currentGameId != null) {
                        gameController.setSelectedGameId(currentGameId);
                    }
                }

                rootNode = baseRoot;

            } else {
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

    public void openGame(Integer gameId) {
        if (gameId == null) {
            return;
        }

        this.currentGameId = gameId;

        GameController gameController = switchSceneAndGetController(FxmlView.GAME);

        if (gameController != null) {
            gameController.setSelectedGameId(gameId);
        }
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

    public BaseLayoutController getBaseLayoutController() {
        return baseLayoutController;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
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
}