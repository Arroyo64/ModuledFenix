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
    private ProgressBar globalProgressBar;
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

    public ProgressBar getGlobalProgressBar() {
        return globalProgressBar;
    }

    public NavbarController getNavbarController() {
        return navbarController;
    }
}
