package org.ies.fenix.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.ies.fenix.client.api.SessionManager;
import org.ies.fenix.client.config.FxmlView;
import org.ies.fenix.client.config.StageManager;
import org.ies.fenix.controller.IGameController;
import org.ies.fenix.controller.dto.game.GameResponseDTO;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.File;

public class UploadGameController {

    @FXML
    private TextField titleField;

    @FXML
    private TextField developerField;

    @FXML
    private TextField tagsField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private Label gameFileNameLabel;

    @FXML
    private Label logoFileNameLabel;

    @FXML
    private Label verticalFileNameLabel;

    @FXML
    private Label horizontalOneFileNameLabel;

    @FXML
    private Label horizontalTwoFileNameLabel;

    @FXML
    private ImageView logoImageView;

    @FXML
    private ImageView verticalImageView;

    @FXML
    private ImageView horizontalImageOneView;

    @FXML
    private ImageView horizontalImageTwoView;

    private final StageManager stageManager;
    private final IGameController gameApiService;
    private final SessionManager sessionManager;
    private final RestClient restClient;

    private File selectedGameFile;
    private File selectedLogoImage;
    private File selectedVerticalImage;
    private File selectedHorizontalImageOne;
    private File selectedHorizontalImageTwo;

    public UploadGameController(StageManager stageManager,
                                IGameController gameApiService,
                                SessionManager sessionManager,
                                RestClient restClient) {
        this.stageManager = stageManager;
        this.gameApiService = gameApiService;
        this.sessionManager = sessionManager;
        this.restClient = restClient;
    }

    @FXML
    private void initialize() {
        if (developerField != null) {
            developerField.setEditable(false);
            developerField.setPromptText("Your username will be used automatically");
        }
    }

    @FXML
    private void clearForm() {
        stageManager.goBack();
    }

    @FXML
    private void submitGame() {
        try {
            if (!validateForm()) {
                return;
            }

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            body.add("title", titleField.getText().trim());
            body.add("description", descriptionArea.getText() != null ? descriptionArea.getText().trim() : "");
            body.add("tags", tagsField.getText() != null ? tagsField.getText().trim() : "");
            body.add("price", "0");

            body.add("gameFile", new FileSystemResource(selectedGameFile));
            body.add("logoFile", new FileSystemResource(selectedLogoImage));

            if (selectedVerticalImage != null) {
                body.add("verticalImage", new FileSystemResource(selectedVerticalImage));
            }

            if (selectedHorizontalImageOne != null) {
                body.add("horizontalImageOne", new FileSystemResource(selectedHorizontalImageOne));
            }

            if (selectedHorizontalImageTwo != null) {
                body.add("horizontalImageTwo", new FileSystemResource(selectedHorizontalImageTwo));
            }

            ResponseEntity<GameResponseDTO> response = restClient.post()
                    .uri("/api/games/create/upload")
                    .header("Authorization", sessionManager.getAuthorizationHeader())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .toEntity(GameResponseDTO.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                showInfo("Game published", "Your game has been published successfully.");
                stageManager.switchToNextScene(FxmlView.LIBRARY);
            } else {
                showError("Upload failed", "The server could not publish the game.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Upload failed", "There was an error publishing the game.");
        }
    }

    private boolean validateForm() {
        if (titleField.getText() == null || titleField.getText().isBlank()) {
            showError("Missing title", "You must write a title for the game.");
            return false;
        }

        if (selectedGameFile == null) {
            showError("Missing game file", "You must choose a game file.");
            return false;
        }

        if (selectedLogoImage == null) {
            showError("Missing logo", "You must choose a logo image.");
            return false;
        }

        return true;
    }

    @FXML
    private void chooseGameFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose game file");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Game files", "*.zip", "*.rar", "*.7z", "*.jar", "*.exe"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile == null) {
            return;
        }

        selectedGameFile = selectedFile;
        gameFileNameLabel.setText(selectedFile.getName());
    }

    @FXML
    private void chooseLogoImage() {
        File selectedFile = chooseImageFile("Choose logo image");

        if (selectedFile == null) {
            return;
        }

        selectedLogoImage = selectedFile;
        logoFileNameLabel.setText(selectedFile.getName());
        logoImageView.setImage(new Image(selectedFile.toURI().toString()));
    }

    @FXML
    private void chooseVerticalImage() {
        File selectedFile = chooseImageFile("Choose vertical image");

        if (selectedFile == null) {
            return;
        }

        selectedVerticalImage = selectedFile;
        verticalFileNameLabel.setText(selectedFile.getName());
        verticalImageView.setImage(new Image(selectedFile.toURI().toString()));
    }

    @FXML
    private void chooseHorizontalImageOne() {
        File selectedFile = chooseImageFile("Choose horizontal image 1");

        if (selectedFile == null) {
            return;
        }

        selectedHorizontalImageOne = selectedFile;
        horizontalOneFileNameLabel.setText(selectedFile.getName());
        horizontalImageOneView.setImage(new Image(selectedFile.toURI().toString()));
    }

    @FXML
    private void chooseHorizontalImageTwo() {
        File selectedFile = chooseImageFile("Choose horizontal image 2");

        if (selectedFile == null) {
            return;
        }

        selectedHorizontalImageTwo = selectedFile;
        horizontalTwoFileNameLabel.setText(selectedFile.getName());
        horizontalImageTwoView.setImage(new Image(selectedFile.toURI().toString()));
    }

    private File chooseImageFile(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Image files",
                        "*.png",
                        "*.jpg",
                        "*.jpeg",
                        "*.webp"
                )
        );

        return fileChooser.showOpenDialog(null);
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}