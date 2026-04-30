package org.ies.fenix.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.ies.fenix.client.api.SessionManager;
import org.ies.fenix.client.config.FxmlView;
import org.ies.fenix.client.config.StageManager;
import org.ies.fenix.controller.IClientController;
import org.ies.fenix.controller.dto.client.ClientNameDTO;
import org.springframework.http.ResponseEntity;

public class GameController {

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
    private final SessionManager sessionManager;

    public GameController(StageManager stageManager, IClientController clientApiService, SessionManager sessionManager) {
        this.stageManager = stageManager;
        this.clientApiService = clientApiService;
        this.sessionManager = sessionManager;
    }

    @FXML
    private void initialize() {
        try {
            ResponseEntity<ClientNameDTO> response = clientApiService.getUsername("Bearer " + sessionManager.getToken()); //tokens en todos lados para peticiones de las interfaces Ike

            if (response.getStatusCode().value() == 200 && response.getBody() != null) {
                username.setText(response.getBody().getUsername().toUpperCase());
            }
        } catch (RuntimeException e) {
            e.printStackTrace(); //needs to be handled
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
    public void reloadView() {
        stageManager.reloadCurrentScene();
    }
}