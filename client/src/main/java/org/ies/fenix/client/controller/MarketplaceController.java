package org.ies.fenix.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.ies.fenix.client.api.SessionManager;
import org.ies.fenix.client.config.FxmlView;
import org.ies.fenix.client.config.StageManager;
import org.ies.fenix.controller.IClientController;
import org.ies.fenix.controller.IGameController;
import org.ies.fenix.controller.dto.game.GameResponseDTO;
import org.springframework.http.ResponseEntity;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.ies.fenix.client.utils.ImageUtils.setCoverImage;

public class MarketplaceController implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private HBox latestReleasedContainer;

    @FXML
    private GridPane recommendationsContainer;

    private final StageManager stageManager;
    private final IClientController clientApiService;
    private final IGameController gameApiService;
    private final SessionManager sessionManager;

    private List<GameResponseDTO> loadedGames = new ArrayList<>();

    public MarketplaceController(StageManager stageManager,
                                 IClientController clientApiService,
                                 IGameController gameApiService,
                                 SessionManager sessionManager) {
        this.stageManager = stageManager;
        this.clientApiService = clientApiService;
        this.gameApiService = gameApiService;
        this.sessionManager = sessionManager;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadMarketplaceGames();
        configureSearch();
    }

    private void loadMarketplaceGames() {
        try {
            ResponseEntity<List<GameResponseDTO>> response =
                    gameApiService.getAllGames(sessionManager.getAuthorizationHeader());

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                clearCarousels();
                return;
            }

            loadedGames = response.getBody();

            renderLatestReleased(loadedGames);
            renderRecommendations(loadedGames);

        } catch (Exception e) {
            e.printStackTrace();
            clearCarousels();
        }
    }

    private void configureSearch() {
        if (searchField == null) {
            return;
        }

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String searchText = newValue == null ? "" : newValue.trim().toLowerCase();

            if (searchText.isBlank()) {
                renderLatestReleased(loadedGames);
                renderRecommendations(loadedGames);
                return;
            }

            List<GameResponseDTO> filteredGames = loadedGames.stream()
                    .filter(game -> game.getTitle() != null
                            && game.getTitle().toLowerCase().contains(searchText))
                    .toList();

            renderLatestReleased(filteredGames);
            renderRecommendations(filteredGames);
        });
    }

    private void renderLatestReleased(List<GameResponseDTO> games) {
        latestReleasedContainer.getChildren().clear();

        if (games == null || games.isEmpty()) {
            latestReleasedContainer.getChildren().add(createEmptyGamesMessage("There are no games yet."));
            return;
        }

        games.stream()
                .sorted((g1, g2) -> Integer.compare(g2.getId(), g1.getId()))
                .limit(10)
                .forEach(game -> latestReleasedContainer.getChildren().add(createGameCard(game)));
    }

    private void renderRecommendations(List<GameResponseDTO> games) {
        recommendationsContainer.getChildren().clear();

        if (games == null || games.isEmpty()) {
            recommendationsContainer.add(
                    createEmptyGamesMessage("There are no games to recommend yet."),
                    0,
                    0
            );
            return;
        }

        List<GameResponseDTO> shuffledGames = new ArrayList<>(games);
        java.util.Collections.shuffle(shuffledGames);

        int index = 0;

        for (GameResponseDTO game : shuffledGames) {
            int row = index % 3;
            int col = index / 3;

            recommendationsContainer.add(createGameCard(game), col, row);

            index++;
        }
    }

    private Label createEmptyGamesMessage(String text) {
        Label emptyLabel = new Label(text);
        emptyLabel.setStyle("""
                -fx-font-size: 18px;
                -fx-text-fill: #777777;
                -fx-font-weight: bold;
                -fx-padding: 20 0 20 0;
                """);
        return emptyLabel;
    }

    private StackPane createGameCard(GameResponseDTO game) {
        StackPane wrapper = new StackPane();
        wrapper.getStyleClass().add("card-click-wrapper");

        VBox card = new VBox();
        card.getStyleClass().add("card");

        HBox imageWrapper = new HBox();
        imageWrapper.setAlignment(Pos.CENTER);
        imageWrapper.setPrefHeight(170.0);
        imageWrapper.setPrefWidth(280.0);
        imageWrapper.getStyleClass().add("card-image-wrapper");

        ImageView imageView = new ImageView();
        imageView.setFitHeight(150.0);
        imageView.setFitWidth(260.0);
        imageView.setPreserveRatio(false);
        imageView.getStyleClass().add("card-image");

        loadHorizontalOneIntoImageView(game, imageView);

        imageWrapper.getChildren().add(imageView);

        Label titleLabel = new Label(getSafeText(game.getTitle(), "Untitled"));
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(110.0);

        System.out.println("Marketplace game: " + game.getTitle() + " tags: " + game.getTags());

        GridPane tagsGrid = createMarketplaceTagsGrid(game.getTags());

        HBox infoRow = new HBox(10.0);
        infoRow.setAlignment(Pos.TOP_LEFT);
        infoRow.setPrefWidth(280.0);
        infoRow.setMaxWidth(280.0);

        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        infoRow.getChildren().addAll(titleLabel, tagsGrid);

        card.getChildren().addAll(imageWrapper, infoRow);

        wrapper.getChildren().add(card);

        wrapper.setOnMouseClicked(event -> openGame(game));
        wrapper.setStyle("-fx-cursor: hand;");

        return wrapper;
    }

    private GridPane createMarketplaceTagsGrid(List<String> tags) {
        GridPane tagsGrid = new GridPane();
        tagsGrid.setHgap(4.0);
        tagsGrid.setVgap(4.0);
        tagsGrid.setAlignment(Pos.TOP_RIGHT);

        if (tags == null || tags.isEmpty()) {
            return tagsGrid;
        }

        List<String> visibleTags = tags.stream()
                .limit(6)
                .toList();

        for (int i = 0; i < visibleTags.size(); i++) {
            Label tagLabel = new Label(visibleTags.get(i));
            tagLabel.getStyleClass().add("tag");

            tagLabel.setStyle("""
                    -fx-font-size: 9px;
                    -fx-padding: 2 6 2 6;
                    -fx-background-radius: 5px;
                    """);

            int col = i % 3;
            int row = i / 3;

            tagsGrid.add(tagLabel, col, row);
        }

        return tagsGrid;
    }

    private void loadHorizontalOneIntoImageView(GameResponseDTO game, ImageView imageView) {
        if (game == null || game.getId() == null) {
            return;
        }

        try {
            System.out.println("Loading horizontal 1 for game id: " + game.getId());

            ResponseEntity<byte[]> response = gameApiService.getHorizontal1(
                    sessionManager.getAuthorizationHeader(),
                    game.getId()
            );

            System.out.println("Horizontal 1 status: " + response.getStatusCode());

            if (response.getBody() == null) {
                System.out.println("Horizontal 1 body is null");
                return;
            }

            System.out.println("Horizontal 1 bytes: " + response.getBody().length);

            if (!response.getStatusCode().is2xxSuccessful()
                    || response.getBody().length == 0) {
                return;
            }

            setCoverImage(response.getBody(), imageView, 260.0, 150.0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openGame(GameResponseDTO game) {
        if (game == null || game.getId() == null) {
            return;
        }

        System.out.println("Opening game with id: " + game.getId());

        GameController gameController = stageManager.switchSceneAndGetController(FxmlView.GAME);

        if (gameController != null) {
            gameController.setSelectedGameId(game.getId());
        }
    }

    private void clearCarousels() {
        if (latestReleasedContainer != null) {
            latestReleasedContainer.getChildren().clear();
        }

        if (recommendationsContainer != null) {
            recommendationsContainer.getChildren().clear();
        }
    }

    private String getSafeText(String text, String fallback) {
        if (text == null || text.isBlank()) {
            return fallback;
        }

        return text;
    }

    @FXML
    void switchProfileScene() {
        stageManager.switchScene(FxmlView.PROFILE);
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