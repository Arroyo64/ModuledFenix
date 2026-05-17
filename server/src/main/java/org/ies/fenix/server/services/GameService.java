package org.ies.fenix.server.services;

import org.ies.fenix.controller.dto.game.GameResponseDTO;
import org.ies.fenix.controller.dto.game.GameSearchDTO;
import org.ies.fenix.controller.dto.teaser.TeaserResponseDTO;
import org.ies.fenix.server.models.Client;
import org.ies.fenix.server.models.Game;
import org.ies.fenix.server.models.Tag;
import org.ies.fenix.server.models.Teaser;
import org.ies.fenix.server.repositories.GameRepository;
import org.ies.fenix.server.repositories.TagRepository;
import org.ies.fenix.server.repositories.TeaserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private TeaserRepository teaserRepository;
    @Autowired
    private ClientService clientService;
    @Autowired
    private FileStorageService fileStorageService;

    // ============================================================
    //                      PUBLIC API
    // ============================================================

    @Transactional
    public GameResponseDTO createGame(
            String token,
            String title,
            String description,
            BigDecimal price,
            String tagsText,
            MultipartFile gameFile,
            MultipartFile logoFile,
            MultipartFile verticalImage,
            MultipartFile horizontalImageOne,
            MultipartFile horizontalImageTwo
    ) {

        Client client = validateAndGetClient(token);
        validateGameInput(title, gameFile, logoFile);
        ensureTitleIsUnique(title);

        List<Tag> tags = parseExistingTags(tagsText);

        Game game = buildGameEntity(client, title, description, price, gameFile, tags);
        game = gameRepository.save(game);

        saveMainFiles(game, gameFile, logoFile);
        saveTeasers(game, verticalImage, horizontalImageOne, horizontalImageTwo);

        return getGameById(game.getId());
    }

    public Resource downloadGame(Integer gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        if (game.getGameFileKey() == null) {
            throw new IllegalStateException("Game file not available");
        }

        // Ruta donde guardas los juegos
        String folder = "uploads/games/" + game.getDev().getId();

        // El archivo real: key + extensión original
        File file = findFileByKey(folder, game.getGameFileKey());

        if (!file.exists()) {
            throw new IllegalStateException("Stored game file not found");
        }

        return new FileSystemResource(file);
    }

    // ============================================================
    //                      VALIDATION
    // ============================================================

    private Client validateAndGetClient(String token) {
        Client client = clientService.getClient(token);
        if (client == null) {
            throw new IllegalArgumentException("Token is not valid");
        }
        return client;
    }

    private void validateGameInput(String title, MultipartFile gameFile, MultipartFile logoFile) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (gameFile == null || gameFile.isEmpty()) {
            throw new IllegalArgumentException("Game file is required");
        }
        if (logoFile == null || logoFile.isEmpty()) {
            throw new IllegalArgumentException("Logo image is required");
        }
    }

    private void ensureTitleIsUnique(String title) {
        if (gameRepository.existsByTitleIgnoreCase(title)) {
            throw new IllegalArgumentException("A game with this title already exists");
        }
    }

    // ============================================================
    //                      GAME CREATION
    // ============================================================

    private Game buildGameEntity(
            Client client,
            String title,
            String description,
            BigDecimal price,
            MultipartFile gameFile,
            List<Tag> tags
    ) {
        Game game = new Game();
        game.setTitle(title.trim());
        game.setDescription(description);
        game.setDev(client);
        game.setPrice(price != null ? price : BigDecimal.ZERO);
        game.setDownloads(0);
        game.setSizeMb(calculateSizeMb(gameFile));
        game.setTags(tags);
        return game;
    }

    private void saveMainFiles(Game game, MultipartFile gameFile, MultipartFile logoFile) {
        String gameFileKey = fileStorageService.saveGameFile(game.getId(), gameFile);
        String logoKey = fileStorageService.saveGameLogo(game.getId(), logoFile);

        game.setGameFileKey(gameFileKey);
        game.setGameLogoKey(logoKey);

        gameRepository.save(game);
    }

    private void saveTeasers(Game game,
                             MultipartFile vertical,
                             MultipartFile horizontal1,
                             MultipartFile horizontal2) {

        saveTeaserIfPresent(game, vertical, "VERTICAL", "vertical");
        saveTeaserIfPresent(game, horizontal1, "HORIZONTAL_1", "horizontal_1");
        saveTeaserIfPresent(game, horizontal2, "HORIZONTAL_2", "horizontal_2");
    }

    // ============================================================
    //                      TEASERS
    // ============================================================

    private void saveTeaserIfPresent(Game game,
                                     MultipartFile file,
                                     String type,
                                     String fileBaseName) {

        if (file == null || file.isEmpty()) return;

        String objectKey = fileStorageService.saveTeaser(game.getId(), file, fileBaseName);

        Teaser teaser = new Teaser();
        teaser.setGame(game);
        teaser.setObjectKey(objectKey);
        teaser.setType(type);

        teaserRepository.save(teaser);
    }

    // ============================================================
    //                      TAGS
    // ============================================================

    private List<Tag> parseExistingTags(String tagsText) {
        if (tagsText == null || tagsText.isBlank()) {
            return List.of();
        }

        return Arrays.stream(tagsText.split(","))
                .map(String::trim)
                .filter(tagName -> !tagName.isBlank())
                .map(tagName -> tagRepository.findByNameIgnoreCase(tagName)
                        .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagName)))
                .toList();
    }

    // ============================================================
    //                      UTILITIES
    // ============================================================

    private BigDecimal calculateSizeMb(MultipartFile file) {
        if (file == null || file.isEmpty()) return BigDecimal.ZERO;

        double sizeMb = file.getSize() / 1024.0 / 1024.0;
        return BigDecimal.valueOf(sizeMb).setScale(2, RoundingMode.HALF_UP);
    }

    // ============================================================
    //                      QUERY METHODS
    // ============================================================

    public GameResponseDTO getGameById(Integer id) {
        return gameRepository.findById(id)
                .map(this::toResponseDTO)
                .orElse(null);
    }

    public List<GameResponseDTO> getAllGames() {
        return gameRepository.findAllByOrderByIdDesc()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<GameResponseDTO> getGames(GameSearchDTO dto) {
        List<Game> games;

        if (dto.getTitle() != null && !dto.getTitle().isEmpty()) {
            games = gameRepository.findByTitleContainingIgnoreCase(dto.getTitle());
        } else if (dto.getDeveloperName() != null && !dto.getDeveloperName().isEmpty()) {
            games = gameRepository.findByDev_Username(dto.getDeveloperName());
        } else if (dto.getTagNames() != null && !dto.getTagNames().isEmpty()) {
            games = gameRepository.findByAllTagNames(dto.getTagNames(), dto.getTagNames().size());
        } else {
            games = gameRepository.findAll();
            Collections.shuffle(games);
            int limit = dto.getLimit() != null ? dto.getLimit() : 25;
            games = games.stream().limit(limit).toList();
        }

        return games.stream().map(this::toResponseDTO).toList();
    }

    public List<GameResponseDTO> getByTitle(String title) {
        return gameRepository.findByTitleContainingIgnoreCase(title)
                .stream().map(this::toResponseDTO).toList();
    }

    public List<GameResponseDTO> getByDevId(Integer devId) {
        return gameRepository.findByDevId(devId)
                .stream().map(this::toResponseDTO).toList();
    }

    public List<GameResponseDTO> getByTagId(Integer tagId) {
        return gameRepository.findByTagsId(tagId)
                .stream().map(this::toResponseDTO).toList();
    }

    private File findFileByKey(String folder, String key) {
        File dir = new File(folder);
        File[] matches = dir.listFiles((d, name) -> name.startsWith(key + "."));
        if (matches == null || matches.length == 0) {
            throw new IllegalStateException("File with key " + key + " not found");
        }
        return matches[0];
    }

    // ============================================================
    //                      DTO MAPPING
    // ============================================================

    private GameResponseDTO toResponseDTO(Game game) {
        GameResponseDTO dto = new GameResponseDTO();

        dto.setId(game.getId());
        dto.setTitle(game.getTitle());
        dto.setDescription(game.getDescription());
        dto.setSizeApproximation(formatSizeFromMB(game.getSizeMb()));
        dto.setDownloadsApproximation(formatDownloads(game.getDownloads()));
        dto.setPrice(game.getPrice());
        dto.setDevUsername(game.getDev() != null ? game.getDev().getUsername() : "Unknown");
        dto.setGameLogoKey(game.getGameLogoKey());
        dto.setGameFileKey(game.getGameFileKey());

        dto.setTags(game.getTags().stream().map(Tag::getName).toList());
        dto.setTeasers(game.getTeasers().stream().map(this::toTeaserResponseDTO).toList());

        return dto;
    }

    private TeaserResponseDTO toTeaserResponseDTO(Teaser teaser) {
        TeaserResponseDTO dto = new TeaserResponseDTO();
        dto.setId(teaser.getId());
        dto.setGameId(teaser.getGame() != null ? teaser.getGame().getId() : null);
        dto.setObjectKey(teaser.getObjectKey());
        dto.setType(teaser.getType());
        return dto;
    }

    // ============================================================
    //                      FORMATTERS
    // ============================================================

    public String formatSizeFromMB(BigDecimal mb) {
        if (mb == null || mb.compareTo(BigDecimal.ZERO) == 0) return "0 MB";

        final int FIGURE = 3;
        String[] units = {"MB", "GB", "TB", "PB"};
        int unitIndex = 0;

        BigDecimal base = BigDecimal.valueOf(1024);

        while (mb.compareTo(base) >= 0 && unitIndex < units.length - 1) {
            mb = mb.divide(base, MathContext.DECIMAL128);
            unitIndex++;
        }

        BigDecimal rounded = mb.round(new MathContext(FIGURE, RoundingMode.HALF_UP))
                .stripTrailingZeros();

        return rounded.toPlainString() + " " + units[unitIndex];
    }

    public String formatDownloads(long downloads) {
        if (downloads == 0) return "0";

        final int FIGURE = 3;
        String[] units = {"", "K", "M", "B", "T"};
        double value = downloads;
        int unitIndex = 0;

        while (value >= 1000 && unitIndex < units.length - 1) {
            value /= 1000;
            unitIndex++;
        }

        double scale = Math.pow(10, Math.floor(Math.log10(value)) + 1);
        double rounded = Math.round(value / scale * Math.pow(10, FIGURE))
                / Math.pow(10, FIGURE) * scale;

        return (rounded % 1 == 0)
                ? String.format("%.0f%s", rounded, units[unitIndex])
                : rounded + units[unitIndex];
    }
}
