package org.ies.fenix.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.ies.fenix.client.api.SessionManager;
import org.ies.fenix.client.config.FxmlView;
import org.ies.fenix.client.config.StageManager;
import org.ies.fenix.controller.IClientController;
import org.ies.fenix.controller.IGameController;
import org.ies.fenix.controller.dto.client.ClientInfoDTO;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.http.ResponseEntity;

import static org.ies.fenix.client.utils.ImageUtils.setAvatar;

public class GameController {

    @FXML
    public FontIcon topProfileIcon;

    @FXML
    public ImageView topProfileImage;

    @FXML
    private TextField searchField;

    @FXML
    private VBox leftGamesList;

    @FXML
    private GridPane libraryGrid;

    @FXML
    private Hyperlink username;

    @FXML
    private Hyperlink marketplace;

    @FXML
    private Hyperlink library;

    private final StageManager stageManager;
    private final IClientController clientApiService;
    private final IGameController gameApiService;
    private final SessionManager sessionManager;

    public GameController(StageManager stageManager,
                          IClientController clientApiService,
                          IGameController gameApiService,
                          SessionManager sessionManager) {
        this.stageManager = stageManager;
        this.clientApiService = clientApiService;
        this.gameApiService = gameApiService;
        this.sessionManager = sessionManager;
    }

    @FXML
    private void initialize() {
        try {
            ResponseEntity<ClientInfoDTO> response =
                    clientApiService.getClientInfo(sessionManager.getAuthorizationHeader());

            if (response.getStatusCode().value() == 200 && response.getBody() != null) {
                username.setText(response.getBody().getUsername().toUpperCase());
            }

            ResponseEntity<byte[]> image =
                    clientApiService.getProfileImage(sessionManager.getAuthorizationHeader());

            if (image.getStatusCode().value() == 200) {
                setAvatar(image.getBody(), topProfileImage, topProfileIcon, 40);
            }

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void switchProfileScene() {
        stageManager.switchToNextScene(FxmlView.PROFILE);
    }

    @FXML
    void switchToMarketplaceScene() {
        stageManager.switchToNextScene(FxmlView.MARKETPLACE);
    }

    @FXML
    void switchToLibraryScene() {
        stageManager.switchToNextScene(FxmlView.LIBRARY);
    }

    @FXML
    void switchToUploadGameScene() {
        stageManager.switchToNextScene(FxmlView.UPLOAD_GAME);
    }

    @FXML
    public void reloadView() {
        stageManager.reloadCurrentScene();
    }
}