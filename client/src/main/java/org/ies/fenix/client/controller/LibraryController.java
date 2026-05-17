package org.ies.fenix.client.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.ies.fenix.client.api.SessionManager;
import org.ies.fenix.client.config.FxmlView;
import org.ies.fenix.client.config.StageManager;
import org.ies.fenix.controller.IClientController;
import org.ies.fenix.controller.IGameController;
import org.ies.fenix.controller.IPurchaseController;
import org.ies.fenix.controller.dto.client.ClientInfoDTO;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;

import static org.ies.fenix.client.utils.ImageUtils.initialConfig;
import static org.ies.fenix.client.utils.ImageUtils.setAvatar;

public class LibraryController {

    @FXML
    private TextField searchField;

    @FXML
    private VBox leftGamesList;

    @FXML
    private GridPane libraryGrid;

    private final StageManager stageManager;
    private final IClientController clientApiService;
    private final IGameController gameApiService;
    private final SessionManager sessionManager;
    private final IPurchaseController purchaseApiService;


    public LibraryController(StageManager stageManager,
                             IClientController clientApiService,
                             IGameController gameApiService,
                             SessionManager sessionManager,
                             IPurchaseController purchaseApiService
    ) {
        this.stageManager = stageManager;
        this.clientApiService = clientApiService;
        this.purchaseApiService = purchaseApiService;
        this.gameApiService = gameApiService;
        this.sessionManager = sessionManager;
    }

    @FXML
    private void initialize() {
        loadLibrary();
    }

    private void loadLibrary() {
        try {
            Integer clientId = sessionManager.getClientId();

            // 1. Obtener juegos comprados
            var response = purchaseApiService.getLibraryByClientId(sessionManager.getAuthorizationHeader(),clientId);
            var games = response.getBody();
            if (games == null) return;

            leftGamesList.getChildren().clear();
            libraryGrid.getChildren().clear();

            int col = 0;
            int row = 0;

            for (var game : games) {

                // ============================
                // PANEL IZQUIERDO (LISTA)
                // ============================
                HBox rowBox = new HBox(16);
                rowBox.getStyleClass().add("library-left-game-row");

                ImageView icon = new ImageView();
                icon.setFitWidth(36);
                icon.setFitHeight(36);
                icon.setPreserveRatio(true);

                try {
                    byte[] bytes = gameApiService.getLogo(sessionManager.getAuthorizationHeader(),game.getGameId()).getBody();
                    icon.setImage(new Image(new ByteArrayInputStream(bytes)));
                } catch (Exception ignored) {
                }

                Hyperlink title = new Hyperlink(game.getTitle());
                title.getStyleClass().add("library-left-game-title");
                title.setOnAction(e -> openGame(game.getGameId()));


                HBox iconWrapper = new HBox(icon);
                iconWrapper.getStyleClass().add("library-left-game-icon-wrapper");

                rowBox.getChildren().addAll(iconWrapper, title);
                leftGamesList.getChildren().add(rowBox);

                // ============================
                // PANEL DERECHO (GRID)
                // ============================
                StackPane cardWrapper = new StackPane();
                cardWrapper.getStyleClass().add("library-card-click-wrapper");

                VBox card = new VBox();
                card.getStyleClass().add("library-card");

                HBox coverWrapper = new HBox();
                coverWrapper.setAlignment(Pos.CENTER);
                coverWrapper.getStyleClass().add("library-cover-wrapper");

                ImageView cover = new ImageView();
                cover.setFitWidth(170);
                cover.setFitHeight(245);
                cover.setPreserveRatio(true);

                try {
                    byte[] bytes = gameApiService.getVertical(sessionManager.getAuthorizationHeader(),game.getGameId()).getBody();
                    cover.setImage(new Image(new ByteArrayInputStream(bytes)));
                } catch (Exception ignored) {
                }

                coverWrapper.getChildren().add(cover);
                card.getChildren().add(coverWrapper);

                // ============================
                // BOTÓN PLAY (visible solo en hover)
                // ============================
                Button playButton = new Button("  PLAY");
                playButton.setGraphic(new FontIcon("mdi-play"));
                playButton.setStyle("""
                            -fx-background-color: #2ecc71;
                            -fx-text-fill: white;
                            -fx-font-size: 18px;
                            -fx-background-radius: 8;
                            -fx-cursor: hand;
                        """);
                playButton.setPrefWidth(160);
                playButton.setPrefHeight(40);
                playButton.setVisible(false);
                StackPane.setAlignment(playButton, Pos.CENTER);

                // Animación de click
                playButton.setOnMousePressed(ev -> playButton.setStyle("""
                            -fx-background-color: #27ae60;
                            -fx-text-fill: white;
                            -fx-font-size: 18px;
                            -fx-background-radius: 8;
                        """));
                playButton.setOnMouseReleased(ev -> playButton.setStyle("""
                            -fx-background-color: #2ecc71;
                            -fx-text-fill: white;
                            -fx-font-size: 18px;
                            -fx-background-radius: 8;
                        """));

                playButton.setOnAction(ev -> {
                    System.out.println("Launching game " + game.getGameId());
                    // TODO ejecutar .exe
                });

                // ============================
                // EFECTO HOVER (blur + botón)
                // ============================
                GaussianBlur blur = new GaussianBlur(0);

                cardWrapper.setOnMouseEntered(ev -> {
                    blur.setRadius(12);
                    cover.setEffect(blur);
                    playButton.setVisible(true);
                });

                cardWrapper.setOnMouseExited(ev -> {
                    blur.setRadius(0);
                    cover.setEffect(blur);
                    playButton.setVisible(false);
                });

                // Añadir elementos al wrapper
                cardWrapper.getChildren().addAll(card, playButton);

                libraryGrid.add(cardWrapper, col, row);


                try {
                    byte[] bytes = gameApiService.getVertical(sessionManager.getAuthorizationHeader(),game.getGameId()).getBody();
                    cover.setImage(new Image(new ByteArrayInputStream(bytes)));
                } catch (Exception ignored) {
                }

                coverWrapper.getChildren().add(cover);
                card.getChildren().add(coverWrapper);

                Button invisible = new Button();
                invisible.getStyleClass().add("invisible-game-button");
                invisible.setPrefSize(230, 285);


                cardWrapper.getChildren().addAll(card, invisible);

                libraryGrid.add(cardWrapper, col, row);

                col++;
                if (col == 4) {
                    col = 0;
                    row++;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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
    void switchToUploadGameScene() {
        stageManager.switchScene(FxmlView.UPLOAD_GAME);
    }

    @FXML
    public void reloadView() {
        stageManager.reloadCurrentScene();
    }

    private void openGame(Integer gameId) {
        GameController controller =
                stageManager.switchSceneAndGetController(FxmlView.GAME);
        controller.setSelectedGameId(gameId);
    }
}