package org.ies.fenix.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.ies.fenix.client.api.SessionManager;
import org.ies.fenix.client.config.FxmlView;
import org.ies.fenix.client.config.StageManager;
import org.ies.fenix.controller.IClientController;
import org.ies.fenix.controller.dto.ServerResponseDTO;
import org.ies.fenix.controller.dto.client.ClientInfoDTO;
import org.ies.fenix.controller.dto.client.FileUploadDTO;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    @FXML
    public TextField nameField;

    @FXML
    public TextField emailField;

    @FXML
    public PasswordField passwordField;

    @FXML
    private Hyperlink username;

    @FXML
    private Hyperlink marketplace;

    @FXML
    private Hyperlink library;

    @FXML
    private TextArea bio;

    @FXML
    private ImageView profileImage;

    @FXML
    private FontIcon profileIcon;

    private final StageManager stageManager;
    private final IClientController clientApiService;
    private final SessionManager sessionManager;
    private static final String SERVER_URL = "http://localhost:8080";

    public ProfileController(StageManager stageManager, IClientController clientApiService, SessionManager sessionManager) {
        this.stageManager = stageManager;
        this.clientApiService = clientApiService;
        this.sessionManager = sessionManager;


    }

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        try {
            ResponseEntity<ClientInfoDTO> response = clientApiService.getClientInfo(buildHeader()); //tokens en todos lados para peticiones de las interfaces Ike
            if (response.getStatusCode().value() == 200 && response.getBody() != null) {
                username.setText(response.getBody().getUsername().toUpperCase());
                nameField.setText(response.getBody().getUsername());
                emailField.setText(response.getBody().getEmail());
                passwordField.setText(buildStingWithCharsof(response.getBody().getPasswordCharacter()));
            }
            ResponseEntity<String> loadedBio = clientApiService.getBio(buildHeader());
            if (loadedBio.getStatusCode().value() != 404) {
                bio.setText(loadedBio.getBody());
            }
            ResponseEntity<byte[]> image = clientApiService.getProfileImage(sessionManager.getAuthorizationHeader());

            byte[] imageBytes = image.getBody();

            if (imageBytes != null && imageBytes.length > 0) {
                profileImage.setImage(new Image(new ByteArrayInputStream(imageBytes)));
                profileImage.setVisible(true);
                profileIcon.setVisible(false);
            } else {
                profileImage.setVisible(false);
                profileIcon.setVisible(true);
            }

        } catch (RuntimeException e) {
            e.printStackTrace(); //needs to be handled
        }
    }

    private String buildStingWithCharsof(int passwordCharacter) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < passwordCharacter; i++) {
            sb.append("*");
        }
        return sb.toString();
    }

    private String buildHeader() {
        return sessionManager.getAuthorizationHeader();
    }

    @FXML
    void switchLibraryScene() {
        stageManager.switchToNextScene(FxmlView.LIBRARY);
    }

    @FXML
    void switchToMarketplaceScene() {
        stageManager.switchToNextScene(FxmlView.MARKETPLACE);
    }

    @FXML
    public void reloadView() {
        stageManager.reloadCurrentScene();
    }

    @FXML
    public void updateProfileBio() {
        ResponseEntity<ServerResponseDTO> serverResponse = clientApiService.updateBio("Bearer " + sessionManager.getToken(), bio.getText());
        if (serverResponse.getStatusCode().value() == 200) {
            System.out.println("Bio updated");
            stageManager.reloadCurrentScene();
        }
        else {
            assert serverResponse.getBody() != null;
            System.out.println(serverResponse.getBody().getMessage());
        }
        stageManager.reloadCurrentScene();
    }

    @FXML
    public void uploadProfilePicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile == null) return;

        try {
            String mimeType = Files.probeContentType(selectedFile.toPath());
            byte[] bytes = Files.readAllBytes(selectedFile.toPath());
            FileUploadDTO dto = new FileUploadDTO(
                    selectedFile.getName(),
                    mimeType,
                    bytes
            );
            ResponseEntity<ServerResponseDTO> response =
                    clientApiService.uploadProfilePicture(
                            buildHeader(),
                            dto
                    );
            if (response.getStatusCode().value() == 200) {
                System.out.println("Profile picture updated");
                profileImage.setImage(new Image(selectedFile.toURI().toString()));
                stageManager.reloadCurrentScene();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

