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
import org.ies.fenix.client.utils.ImageUtils;
import org.ies.fenix.controller.IClientController;
import org.ies.fenix.controller.IGameController;
import org.ies.fenix.controller.IPurchaseController;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;

import java.io.ByteArrayInputStream;

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
                             IPurchaseController purchaseApiService) {
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

            var response = purchaseApiService.getLibraryByClientId(
                    sessionManager.getAuthorizationHeader(),
                    clientId
            );

            var games = response.getBody();

            leftGamesList.getChildren().clear();
            libraryGrid.getChildren().clear();

            if (games == null || games.isEmpty()) {
                showEmptyLibraryMessage();
                return;
            }

            int col = 0;
            int row = 0;

            for (var game : games) {

                // ============================
                // PANEL IZQUIERDO
                // ============================
                HBox rowBox = new HBox(16);
                rowBox.setAlignment(Pos.CENTER_LEFT);
                rowBox.getStyleClass().add("library-left-game-row");

                ImageView icon = new ImageView();
                icon.setFitWidth(36);
                icon.setFitHeight(36);
                icon.setPreserveRatio(true);

                try {
                    try {
                        byte[] bytes = gameApiService.getLogo(
                                sessionManager.getAuthorizationHeader(),
                                game.getGameId()
                        ).getBody();

                        ImageUtils.setAvatar(
                                bytes,
                                icon,          // ImageView
                                36             // tamaño del avatar
                        );

                    } catch (Exception ignored) {
                    }


                } catch (Exception ignored) {
                }

                Hyperlink title = new Hyperlink(game.getTitle());
                title.getStyleClass().add("library-left-game-title");
                title.setOnAction(e -> openGame(game.getGameId()));

                HBox iconWrapper = new HBox(icon);
                iconWrapper.setAlignment(Pos.CENTER);
                iconWrapper.getStyleClass().add("library-left-game-icon-wrapper");

                rowBox.getChildren().addAll(iconWrapper, title);
                leftGamesList.getChildren().add(rowBox);

                // ============================
                // PANEL DERECHO
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
                cover.setPreserveRatio(false);

                try {
                    byte[] bytes = gameApiService.getVertical(
                            sessionManager.getAuthorizationHeader(),
                            game.getGameId()
                    ).getBody();

                    if (bytes != null && bytes.length > 0) {
                        cover.setImage(new Image(new ByteArrayInputStream(bytes)));
                    }

                } catch (Exception ignored) {
                }

                coverWrapper.getChildren().add(cover);
                card.getChildren().add(coverWrapper);

                // ============================
                // BOTÓN PLAY
                // ============================
                Button playButton = new Button("  PLAY");
                playButton.setGraphic(new FontIcon(MaterialDesignP.PLAY));
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

                playButton.setOnMousePressed(ev -> playButton.setStyle("""
                        -fx-background-color: #27ae60;
                        -fx-text-fill: white;
                        -fx-font-size: 18px;
                        -fx-background-radius: 8;
                        -fx-cursor: hand;
                        """));

                playButton.setOnMouseReleased(ev -> playButton.setStyle("""
                        -fx-background-color: #2ecc71;
                        -fx-text-fill: white;
                        -fx-font-size: 18px;
                        -fx-background-radius: 8;
                        -fx-cursor: hand;
                        """));

                playButton.setOnAction(ev -> {
                    System.out.println("Launching game " + game.getGameId());
                    // TODO ejecutar .exe
                });

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

                cardWrapper.getChildren().addAll(card, playButton);

                libraryGrid.add(cardWrapper, col, row);

                col++;
                if (col == 4) {
                    col = 0;
                    row++;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            leftGamesList.getChildren().clear();
            libraryGrid.getChildren().clear();
            showEmptyLibraryMessage();
        }
    }

    private void showEmptyLibraryMessage() {
        Label leftMessage = new Label("You don't have any games yet.");
        leftMessage.setStyle("""
                -fx-font-size: 16px;
                -fx-text-fill: #777777;
                -fx-font-weight: bold;
                -fx-padding: 20 0 20 0;
                """);

        leftGamesList.getChildren().add(leftMessage);

        Label gridMessage = new Label("Your library is empty.");
        gridMessage.setStyle("""
                -fx-font-size: 22px;
                -fx-text-fill: #777777;
                -fx-font-weight: bold;
                -fx-padding: 20 0 0 0;
                """);

        libraryGrid.add(gridMessage, 0, 0);
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

        if (controller != null) {
            controller.setSelectedGameId(gameId);
        }
    }
}