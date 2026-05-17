package org.ies.fenix.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;

import static org.ies.fenix.client.utils.ImageUtils.*;

public class ProfileController implements Initializable {

    @FXML
    public TextField nameField;

    @FXML
    public TextField emailField;

    @FXML
    public PasswordField passwordField;

    @FXML
    private TextArea bio;

    @FXML
    private ImageView profileImage;

    @FXML
    private FontIcon profileIcon;

    private final StageManager stageManager;
    private final IClientController clientApiService;
    private final SessionManager sessionManager;

    public ProfileController(StageManager stageManager, IClientController clientApiService, SessionManager sessionManager) {
        this.stageManager = stageManager;
        this.clientApiService = clientApiService;
        this.sessionManager = sessionManager;
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            ResponseEntity<ClientInfoDTO> response = clientApiService.getClientInfo(buildHeader());
            if (response.getStatusCode().value() == 200 && response.getBody() != null) {
                nameField.setText(response.getBody().getUsername());
                emailField.setText(response.getBody().getEmail());
                passwordField.setText(buildStingWithCharsOf(response.getBody().getPasswordCharacter()));
            }
            ResponseEntity<String> loadedBio = clientApiService.getBio(buildHeader());
            if (loadedBio.getStatusCode().value() != 404) {
                bio.setText(loadedBio.getBody());
            }
            ResponseEntity<byte[]> image = clientApiService.getProfileImage(sessionManager.getAuthorizationHeader());
            if (image.getStatusCode().value() == 200) {
                setCoverImage(image.getBody(), profileImage, 180);
                profileIcon.setVisible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String buildStingWithCharsOf(int passwordCharacter) {
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
        stageManager.switchScene(FxmlView.LIBRARY);
    }

    @FXML
    void switchToMarketplaceScene() {
        stageManager.switchScene(FxmlView.MARKETPLACE);
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
        } else {
            assert serverResponse.getBody() != null;
            System.out.println(serverResponse.getBody().getMessage());
        }
        stageManager.reloadCurrentScene();
    }

    @FXML
    public void uploadProfilePicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile == null) return;

        try {
            String mimeType = Files.probeContentType(selectedFile.toPath());
            byte[] bytes = Files.readAllBytes(selectedFile.toPath());

            FileUploadDTO dto = new FileUploadDTO(selectedFile.getName(), mimeType, bytes);

            ResponseEntity<ServerResponseDTO> response;

            try {
                response = clientApiService.uploadProfilePicture(buildHeader(), dto);

            } catch (org.springframework.web.client.HttpClientErrorException e) {
                showAlert("no se pudo subir la imagen", e.getResponseBodyAsString());
                return;
            }

            if (response.getStatusCode().value() != 200) {
                showAlert(
                        "no se pudo subir la imagen",
                        response.getBody() != null ? response.getBody().getMessage() : "error desconocido"
                );
                return;
            }

            ResponseEntity<byte[]> updatedImage =
                    clientApiService.getProfileImage(buildHeader());

            if (updatedImage.getStatusCode().value() == 200) {
                setCoverImage(updatedImage.getBody(), profileImage, 180);
                profileIcon.setVisible(false);

                NavbarController navbar = stageManager.getBaseLayoutController().getNavbarController();
                setAvatar(updatedImage.getBody(), navbar.getTopProfileImage(), navbar.getTopProfileIcon(), 40);

                System.out.println("profile picture updated");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showAlert(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);

        if (message == null || message.isBlank()) {
            message = "Ha ocurrido un error desconocido.";
        }

        // icono Ikonli
        FontIcon icon = new FontIcon("mdi2a-alert-circle");
        icon.setIconSize(48);
        icon.getStyleClass().add("alert-icon");

        Label title = new Label(header);
        title.getStyleClass().add("alert-title");

        Label text = new Label(message);
        text.setWrapText(true);
        text.getStyleClass().add("alert-message");

        VBox content = new VBox(12, icon, title, text);
        content.setAlignment(Pos.CENTER);
        content.getStyleClass().add("alert-content");

        alert.getDialogPane().setContent(content);

        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/alert.css").toExternalForm()
        );
        alert.setGraphic(null);
        alert.showAndWait();
    }



    @FXML
    void switchToUploadGameScene() {
        stageManager.switchScene(FxmlView.UPLOAD_GAME);
    }
}

