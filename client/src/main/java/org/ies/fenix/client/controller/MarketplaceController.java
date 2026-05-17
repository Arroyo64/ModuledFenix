package org.ies.fenix.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import org.ies.fenix.client.api.SessionManager;
import org.ies.fenix.client.config.FxmlView;
import org.ies.fenix.client.config.StageManager;
import org.ies.fenix.controller.IClientController;
import org.ies.fenix.controller.IGameController;
import org.ies.fenix.controller.dto.client.ClientInfoDTO;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.http.ResponseEntity;

import java.net.URL;
import java.util.ResourceBundle;

import static org.ies.fenix.client.utils.ImageUtils.setAvatar;

public class MarketplaceController implements Initializable {

    @FXML
    private TextField searchField;

    private final StageManager stageManager;
    private final IClientController clientApiService;
    private final IGameController gameApiService;
    private final SessionManager sessionManager;

    public MarketplaceController(StageManager stageManager,
                                 IClientController clientApiService,
                                 IGameController gameApiService,
                                 SessionManager sessionManager) {
        this.stageManager = stageManager;
        this.clientApiService = clientApiService;
        this.gameApiService = gameApiService;
        this.sessionManager = sessionManager;
    }

    @FXML
    void switchProfileScene() {
        stageManager.switchScene(FxmlView.PROFILE);
    }

    @FXML
    void switchToLibraryScene() {
        stageManager.switchScene(FxmlView.LIBRARY);
    }

    @FXML
    void switchToUploadGameScene() {
        stageManager.switchScene(FxmlView.UPLOAD_GAME);
    }

    @FXML
    public void reloadView() {
        stageManager.reloadCurrentScene();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //todo setear las cartas de los videojuegos
    }
}