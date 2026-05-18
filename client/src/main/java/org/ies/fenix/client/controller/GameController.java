package org.ies.fenix.client.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
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
import org.springframework.web.client.RestClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static org.ies.fenix.client.utils.ImageUtils.initialConfig;

public class GameController {

    @FXML
    public FontIcon topProfileIcon;

    @FXML
    public ImageView topProfileImage;

    //todo lo que dice selected son los datos que se muestran del juego seleccionado, por lo que tiene que ser "SETEADOS"

    @FXML
    public Label selectedGameGenre;
    public Label selectedGameDeveloper;
    public Label selectedGameTitle;
    public Label selectedGameTitle2; //es un duplicado necesario en el fxml creo

    // TODO Estos tags deberian ser auto creados dependiendo del los tag recibidos de la base de datos
    public Label selectedGameTag1;
    public Label selectedGameTag2;
    public Label selectedGameTag12;
    public Label selectedGameTag13;
    public Label selectedGameTag22;
    // Estan numerados asi porque los q empiezan en 1 estan en una fila y los 2 en otra, consideraciona al crear esto tags
    // Estos de abajo encontre 3 bloques label en vez de un textArea o algo asi no entiendo porque lo haces asi pero igual le asigno id
    public Label selectedGameDescription1;
    public Label selectedGameDescription2;
    public Label selectedGameDescription3;
    public Label selectedGameMainQuote;
    public ImageView selectedGameBannerImage;
    public VBox tagContainerFather; // por si te sirve para crear los tags automaticamente como punto de anglaje
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

    @FXML
    private ProgressBar progressBar;

    private final StageManager stageManager;
    private final IClientController clientApiService;
    private final IGameController gameApiService;
    private final SessionManager sessionManager;
    private final RestClient restClient;
    private final IPurchaseController purchaseApiService;

    private Integer selectedGameId;

    public void setSelectedGameId(Integer selectedGameId) {
        this.selectedGameId = selectedGameId;
        loadSelectedGame();
    }

    public GameController(StageManager stageManager,
                          IClientController clientApiService,
                          IGameController gameApiService,
                          SessionManager sessionManager, RestClient restClient, IPurchaseController purchaseApiService) {
        this.stageManager = stageManager;
        this.clientApiService = clientApiService;
        this.gameApiService = gameApiService;
        this.sessionManager = sessionManager;
        this.restClient = restClient;
        this.purchaseApiService = purchaseApiService;
        BaseLayoutController base = stageManager.getBaseLayoutController();
    }

    @FXML
    private void initialize() {
        initialConfig(clientApiService, sessionManager, username, topProfileImage, topProfileIcon);
        //todo setear el contenido del juego dependiendo de la opcion que haya clickado el cliente y poner el ID del
        //del juego en el atributo selectedGameId para que se puede ejecutar el onDonwload
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

    @FXML
    private void onDownload() {

        try {
            if (selectedGameId == null) {
                showError("No game selected", "Please select a game to download.");
                return;
            }

            // 1. Comprobar si ya lo tiene comprado
            boolean purchased = hasPurchased(selectedGameId);

            if (!purchased) {
                // 2. Mostrar confirmación
                boolean confirmed = showPurchaseConfirmation();

                if (!confirmed) {
                    return; // usuario canceló
                }

                // 3. Ejecutar compra
                boolean success = performPurchase(selectedGameId);

                if (!success) {
                    return; // no continuar si la compra falló
                }
            }
            if (selectedGameId == null) {
                showError("No game selected", "Please select a game to download.");
                return;
            }

            //  Obtener barra global
            BaseLayoutController base = stageManager.getBaseLayoutController();
            base.showProgress(); // mostrar barra global en modo indeterminado

            progressBar.setProgress(0); // si quieres mantener la barra local

            // 1. Llamar al servidor
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

            // 2. Obtener nombre del archivo
            String filename = resource.getFilename();
            if (filename == null || filename.isBlank()) {
                filename = "game_" + selectedGameId;
            }

            // 3. Elegir dónde guardar el archivo
            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName(filename);
            File target = chooser.showSaveDialog(progressBar.getScene().getWindow());

            if (target == null) {
                base.hideProgress();
                return;
            }

            // 4. Crear tarea con barra de progreso
            Task<Void> downloadTask = new Task<>() {
                @Override
                protected Void call() throws Exception {

                    long fileSize = resource.contentLength();
                    InputStream in = resource.getInputStream();

                    try (OutputStream out = new FileOutputStream(target)) {

                        byte[] buffer = new byte[8192];
                        long totalRead = 0;
                        int read;

                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                            totalRead += read;

                            updateProgress(totalRead, fileSize);

                            double progress = (double) totalRead / fileSize;
                            Platform.runLater(() -> base.setProgress(progress));
                        }
                    }

                    return null;
                }
            };

            // 5. Enlazar barra local
            progressBar.progressProperty().bind(downloadTask.progressProperty());

            // 6. Ocultar barra global al terminar
            downloadTask.setOnSucceeded(e -> base.hideProgress());
            downloadTask.setOnFailed(e -> base.hideProgress());
            downloadTask.setOnCancelled(e -> base.hideProgress());

            // 7. Ejecutar en background
            new Thread(downloadTask).start();

        } catch (Exception e) {
            e.printStackTrace();
            stageManager.getBaseLayoutController().hideProgress();
            showError("Download failed", "There was an error downloading the game.");
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private boolean hasPurchased(Integer gameId) {
        Integer clientId = sessionManager.getClientId();

        try {
            ResponseEntity<Boolean> response =
                    purchaseApiService.hasPurchased(sessionManager.getAuthorizationHeader(),clientId, gameId);

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

            ResponseEntity<?> response = purchaseApiService.createPurchase(sessionManager.getAuthorizationHeader(),dto);

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            showError("Purchase failed", "Could not complete the purchase.");
            return false;
        }
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

            if (game.getTags() != null && !game.getTags().isEmpty()) {
                selectedGameGenre.setText("Genre: " + game.getTags().get(0));
            } else {
                selectedGameGenre.setText("Genre: Unknown");
            }

            selectedGameDescription1.setText(description);
            selectedGameDescription2.setText("");
            selectedGameDescription3.setText("");

            selectedGameMainQuote.setText(title);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}