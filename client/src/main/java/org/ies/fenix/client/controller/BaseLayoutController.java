package org.ies.fenix.client.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.ies.fenix.client.config.FxmlView;

public class BaseLayoutController {
    @FXML
    private HBox navbar;
    @FXML
    private StackPane contentArea;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private BorderPane root;
    @FXML
    private NavbarController navbarController;

    public void setActiveView(FxmlView view) {
        navbarController.setActiveTab(view);
    }

    public void setContent(Node node) {
        contentArea.getChildren().setAll(node);
    }

    public void showProgress() {
        progressBar.setVisible(true);
        progressBar.setProgress(-1); // modo indeterminado
    }

    public void hideProgress() {
        progressBar.setVisible(false);
        progressBar.setProgress(0);
    }

    public void setProgress(double value) {
        progressBar.setVisible(true);
        progressBar.setProgress(value);
    }

    public ProgressBar getGlobalProgressBar() {
        return progressBar;
    }

    public NavbarController getNavbarController() {
        return navbarController;
    }
}