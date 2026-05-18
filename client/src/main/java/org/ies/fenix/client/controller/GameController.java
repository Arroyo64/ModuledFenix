package org.ies.fenix.client.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.ies.fenix.client.api.SessionManager;
import org.ies.fenix.client.config.FxmlView;
import org.ies.fenix.client.config.StageManager;
import org.ies.fenix.controller.IClientController;
import org.ies.fenix.controller.IGameController;
import org.ies.fenix.controller.IPurchaseController;
import org.ies.fenix.controller.dto.game.GameResponseDTO;
import org.ies.fenix.controller.dto.purchase.PurchaseCreateDTO;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static org.ies.fenix.client.utils.ImageUtils.initialConfig;
import static org.ies.fenix.client.utils.ImageUtils.setCoverImage;

public class GameController {

    @FXML
    public FontIcon topProfileIcon;

    @FXML
    public ImageView topProfileImage;

    @FXML
    public Label selectedGameDeveloper;

    @FXML
    public Label selectedGameTitle;

    @FXML
    public Label selectedGameTitle2;

    @FXML
    public Label selectedGameDescription1;

    @FXML
    public Label selectedGameDescription2;

    @FXML
    public Label selectedGameDescription3;

    @FXML
    public Label selectedGameMainQuote;

    @FXML
    public ImageView selectedGameBannerImage;

    @FXML
    public VBox tagContainerFather;

    @FXML
    private Hyperlink username;

    private final StageManager stageManager;
    private final IClientController clientApiService;
    private final IGameController gameApiService;
    private final SessionManager sessionManager;
    private final RestClient restClient;
    private final IPurchaseController purchaseApiService;

    private Integer selectedGameId;

    public GameController(StageManager stageManager,
                          IClientController clientApiService,
                          IGameController gameApiService,
                          SessionManager sessionManager,
                          RestClient restClient,
                          IPurchaseController purchaseApiService) {
        this.stageManager = stageManager;
        this.clientApiService = clientApiService;
        this.gameApiService = gameApiService;
        this.sessionManager = sessionManager;
        this.restClient = restClient;
        this.purchaseApiService = purchaseApiService;
    }

    @FXML
    private void initialize() {
        initialConfig(clientApiService, sessionManager, username, topProfileImage, topProfileIcon);
    }

    public void setSelectedGameId(Integer selectedGameId) {
        this.selectedGameId = selectedGameId;
        loadSelectedGame();
    }

    private void loadSelectedGame() {
        if (selectedGameId == null) {
            return;
        }

        try {
            ResponseEntity<GameResponseDTO> response =
                    gameApiService.getById(sessionManager.getAuthorizationHeader(), selectedGameId);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return;
            }

            GameResponseDTO game = response.getBody();

            String title = game.getTitle() != null ? game.getTitle() : "Untitled";
            String developer = game.getDevUsername() != null ? game.getDevUsername() : "Unknown";
            String description = game.getDescription() != null ? game.getDescription() : "No description available.";

            selectedGameTitle.setText(title);
            selectedGameTitle2.setText("Title: " + title);
            selectedGameDeveloper.setText("Developer: " + developer);

            selectedGameDescription1.setText(description);
            selectedGameDescription2.setText("");
            selectedGameDescription3.setText("");

            selectedGameMainQuote.setText(title);

            renderTags(game.getTags());
            loadHorizontalTwoIntoBanner(selectedGameId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderTags(List<String> tags) {
        tagContainerFather.getChildren().clear();

        if (tags == null || tags.isEmpty()) {
            return;
        }

        HBox firstRow = new HBox(10.0);
        HBox secondRow = new HBox(10.0);

        firstRow.setAlignment(Pos.CENTER_LEFT);
        secondRow.setAlignment(Pos.CENTER_LEFT);

        List<String> visibleTags = tags.stream()
                .limit(6)
                .toList();

        for (int i = 0; i < visibleTags.size(); i++) {
            Label tagLabel = new Label(visibleTags.get(i));
            tagLabel.getStyleClass().add("tag");

            if (i < 3) {
                firstRow.getChildren().add(tagLabel);
            } else {
                secondRow.getChildren().add(tagLabel);
            }
        }

        tagContainerFather.getChildren().add(firstRow);

        if (!secondRow.getChildren().isEmpty()) {
            tagContainerFather.getChildren().add(secondRow);
        }
    }

    private void loadHorizontalTwoIntoBanner(Integer gameId) {
        if (gameId == null || selectedGameBannerImage == null) {
            return;
        }

        try {
            System.out.println("Loading horizontal 2 for game id: " + gameId);

            ResponseEntity<byte[]> response = gameApiService.getHorizontal2(
                    sessionManager.getAuthorizationHeader(),
                    gameId
            );

            System.out.println("Horizontal 2 status: " + response.getStatusCode());

            if (response.getBody() == null) {
                System.out.println("Horizontal 2 body is null");
                return;
            }

            System.out.println("Horizontal 2 bytes: " + response.getBody().length);

            if (!response.getStatusCode().is2xxSuccessful()
                    || response.getBody().length == 0) {
                return;
            }

            setCoverImage(response.getBody(), selectedGameBannerImage, 1200.0, 320.0);

            selectedGameBannerImage.setVisible(true);
            selectedGameBannerImage.setManaged(true);
            selectedGameBannerImage.setOpacity(1.0);
            selectedGameBannerImage.toFront();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onDownload() {
        try {
            if (selectedGameId == null) {
                showError("No game selected", "Please select a game to download.");
                return;
            }

            boolean purchased = hasPurchased(selectedGameId);

            if (!purchased) {
                boolean confirmed = showPurchaseConfirmation();

                if (!confirmed) {
                    return;
                }

                boolean success = performPurchase(selectedGameId);

                if (!success) {
                    return;
                }
            }

            BaseLayoutController base = stageManager.getBaseLayoutController();
            base.showProgress();

            ResponseEntity<Resource> response = restClient.get()
                    .uri("/api/games/download/" + selectedGameId)
                    .header("Authorization", sessionManager.getAuthorizationHeader())
                    .retrieve()
                    .toEntity(Resource.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                base.hideProgress();
                showError("Download failed", "The server returned an error.");
                return;
            }

            Resource resource = response.getBody();

            if (resource == null) {
                base.hideProgress();
                showError("Download failed", "Empty file received.");
                return;
            }

            String filename = resource.getFilename();

            if (filename == null || filename.isBlank()) {
                filename = "game_" + selectedGameId;
            }

            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName(filename);

            File target = chooser.showSaveDialog(stageManager.getPrimaryStage());

            if (target == null) {
                base.hideProgress();
                return;
            }

            Task<Void> downloadTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    long fileSize = resource.contentLength();

                    try (InputStream in = resource.getInputStream();
                         OutputStream out = new FileOutputStream(target)) {

                        byte[] buffer = new byte[8192];
                        long totalRead = 0;
                        int read;

                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                            totalRead += read;

                            updateProgress(totalRead, fileSize);
                        }
                    }

                    return null;
                }
            };

// BIND
            base.getGlobalProgressBar()
                    .progressProperty()
                    .bind(downloadTask.progressProperty());

// UNBIND + HIDE
            downloadTask.setOnSucceeded(e -> {
                base.getGlobalProgressBar().progressProperty().unbind();
                base.hideProgress();
            });
            downloadTask.setOnFailed(e -> {
                base.getGlobalProgressBar().progressProperty().unbind();
                base.hideProgress();
            });
            downloadTask.setOnCancelled(e -> {
                base.getGlobalProgressBar().progressProperty().unbind();
                base.hideProgress();
            });

// START
            new Thread(downloadTask).start();
        }  catch (HttpClientErrorException e) {
            String serverMessage = e.getResponseBodyAsString();
            stageManager.getBaseLayoutController().hideProgress();
            showError("Server error", serverMessage);
        } catch (Exception e) {
            stageManager.getBaseLayoutController().hideProgress();
            showError("Download failed", "Unexpected error");
        }
    }

    private boolean hasPurchased(Integer gameId) {
        Integer clientId = sessionManager.getClientId();

        try {
            ResponseEntity<Boolean> response =
                    purchaseApiService.hasPurchased(
                            sessionManager.getAuthorizationHeader(),
                            clientId,
                            gameId
                    );

            Boolean purchased = response.getBody();
            return purchased != null && purchased;

        } catch (Exception e) {
            return false;
        }
    }

    private boolean showPurchaseConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm purchase");
        alert.setHeaderText("You don't own this game");
        alert.setContentText("Do you want to acquire this game?");

        ButtonType yes = new ButtonType("Yes");
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(yes, no);

        return alert.showAndWait().orElse(no) == yes;
    }

    private boolean performPurchase(Integer gameId) {
        Integer clientId = sessionManager.getClientId();

        try {
            PurchaseCreateDTO dto = new PurchaseCreateDTO();
            dto.setClientId(clientId);
            dto.setGameId(gameId);

            ResponseEntity<?> response =
                    purchaseApiService.createPurchase(
                            sessionManager.getAuthorizationHeader(),
                            dto
                    );

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            showError("Purchase failed", "Could not complete the purchase.");
            return false;
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void switchProfileScene() {
        stageManager.switchScene(FxmlView.PROFILE);
    }

    @FXML
    void switchToMarketplaceScene() {
        stageManager.switchScene(FxmlView.MARKETPLACE);
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
}