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
import org.ies.fenix.controller.dto.client.ClientInfoDTO;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.http.ResponseEntity;

import java.net.URL;
import java.util.ResourceBundle;

import static org.ies.fenix.client.utils.ImageUtils.setAvatar;

public class MarketplaceController implements Initializable {
    @FXML
    public FontIcon topProfileIcon;
    @FXML
    public ImageView topProfileImage;
    @FXML
    private TextField searchField;

    @FXML
    private Hyperlink username;

    @FXML
    private Hyperlink marketplace;

    @FXML
    private Hyperlink library;

    private final StageManager stageManager;
    private final IClientController clientApiService;
    private final SessionManager sessionManager;


    public MarketplaceController(StageManager stageManager, IClientController clientApiService, SessionManager sessionManager) {
        this.stageManager = stageManager;
        this.clientApiService = clientApiService;
        this.sessionManager = sessionManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            ResponseEntity<ClientInfoDTO> response = clientApiService.getClientInfo(sessionManager.getAuthorizationHeader());
            if (response.getStatusCode().value() == 200 && response.getBody() != null) {
                username.setText(response.getBody().getUsername().toUpperCase());
            }
            ResponseEntity<byte[]> image = clientApiService.getProfileImage(sessionManager.getAuthorizationHeader());
            if (image.getStatusCode().value() == 200) {
                setAvatar(image.getBody(), topProfileImage, topProfileIcon, 40);
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
    void switchToLibraryScene() {
        stageManager.switchToNextScene(FxmlView.LIBRARY);
    }

    @FXML
    public void reloadView() {
        stageManager.reloadCurrentScene();
    }


}
