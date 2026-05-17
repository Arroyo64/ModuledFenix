package org.ies.fenix.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.ies.fenix.client.api.SessionManager;
import org.ies.fenix.client.config.FxmlView;
import org.ies.fenix.client.config.StageManager;
import org.ies.fenix.client.utils.ImageUtils;
import org.ies.fenix.controller.IClientController;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.ByteArrayInputStream;

public class NavbarController {



    @FXML
    private FontIcon topProfileIcon;

    @FXML
    private ImageView topProfileImage;

    @FXML
    private Hyperlink marketplace;

    @FXML
    private Hyperlink library;

    @FXML
    private Hyperlink username;

    private final StageManager stageManager;
    private final IClientController clientApiService;
    private final SessionManager sessionManager;

    public NavbarController(StageManager stageManager, IClientController clientApiService, SessionManager sessionManager) {
        this.stageManager = stageManager;
        this.clientApiService = clientApiService;
        this.sessionManager = sessionManager;
    }

    public void initialize() {
        ImageUtils.initialConfig(
                clientApiService,
                sessionManager,
                username,
                topProfileImage,
                topProfileIcon
        );
    }

    @FXML
    public void goMarketplace() {
        stageManager.switchScene(FxmlView.MARKETPLACE);
    }

    @FXML
    public void goLibrary() {
        stageManager.switchScene(FxmlView.LIBRARY);
    }

    @FXML
    public void goProfile() {
        stageManager.switchScene(FxmlView.PROFILE);
    }

    @FXML
    public void goUpload() {
        stageManager.switchScene(FxmlView.UPLOAD_GAME);
    }

    public void setActiveTab(FxmlView view) {

        // Limpia estilos previos
        marketplace.getStyleClass().removeAll("tab-active", "tab-inactive");
        library.getStyleClass().removeAll("tab-active", "tab-inactive");
        username.getStyleClass().removeAll("tab-active", "tab-inactive");

        // Activa el tab correcto
        switch (view) {
            case MARKETPLACE -> marketplace.getStyleClass().add("tab-active");
            case LIBRARY -> library.getStyleClass().add("tab-active");
            case PROFILE -> username.getStyleClass().add("tab-active");
        }

        // El resto quedan inactivos
        if (!marketplace.getStyleClass().contains("tab-active"))
            marketplace.getStyleClass().add("tab-inactive");

        if (!library.getStyleClass().contains("tab-active"))
            library.getStyleClass().add("tab-inactive");

        if (!username.getStyleClass().contains("tab-active"))
            username.getStyleClass().add("tab-inactive");
    }
}
