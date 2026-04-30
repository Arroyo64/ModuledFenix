package org.ies.fenix.client.config;


import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.ies.fenix.client.listener.SceneResizeListener;

import java.io.IOException;
import java.util.Objects;

public class StageManager {

    private final Stage primaryStage;
    private final FxmlLoader fxmlLoader;
    private final String applicationTitle;
    private final SceneResizeListener sceneResizeListener;
    private FxmlView currentView;

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
        primaryStage.setTitle(applicationTitle);

        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        Parent rootNode = loadRootNode(view.getFxmlPath());

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
                        .getResource("/styles/styles.css"))
                .toExternalForm();

        scene.getStylesheets().add(stylesheet);

        scene.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> {
            if (sceneResizeListener != null) {
                sceneResizeListener.onSceneResized(newSceneWidth);
            }
        });

        primaryStage.setScene(scene);
    }

    public void switchToNextScene(final FxmlView view) {
        this.currentView = view;
        Parent rootNode = loadRootNode(view.getFxmlPath());

        rootNode.applyCss();
        rootNode.autosize();

        createAndSetScene(rootNode);
        primaryStage.sizeToScene();
        primaryStage.show();
        primaryStage.centerOnScreen();
    }

    private Parent loadRootNode(String fxmlPath) {
        try {
            return fxmlLoader.load(fxmlPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reloadCurrentScene() {
        if (currentView == null) {
            throw new IllegalStateException("No hay vista cargada");
        }
        Parent rootNode = loadRootNode(currentView.getFxmlPath());
        primaryStage.getScene().setRoot(rootNode);
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
