package org.ies.fenix.server.services;

import org.ies.fenix.controller.dto.game.GameResponseDTO;
import org.ies.fenix.controller.dto.game.GameSearchDTO;
import org.ies.fenix.controller.dto.teaser.TeaserResponseDTO;
import org.ies.fenix.server.models.*;
import org.ies.fenix.server.repositories.*;
import org.ies.fenix.server.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.util.*;

@Service
public class GameService {

    @Autowired private GameRepository gameRepository;
    @Autowired private TagRepository tagRepository;
    @Autowired private TeaserRepository teaserRepository;
    @Autowired private ClientService clientService;

    private static final String BASE_UPLOAD_DIR =
            System.getProperty("user.home") + "/fenix/uploads/games/";

    // ============================================================
    //                      CREATE GAME
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

        Game game = buildGameEntity(client, title, description, price, gameFile, parseTags(tagsText));
        game = gameRepository.save(game);

        saveMainFiles(game, gameFile, logoFile);
        saveTeasers(game, verticalImage, horizontalImageOne, horizontalImageTwo);

        return getGameById(game.getId());
    }

    // ============================================================
    //                      DOWNLOAD GAME
    // ============================================================

    public Resource downloadGame(Integer gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        String key = game.getGameFileKey();
        if (key == null) throw new IllegalStateException("Game file not available");

        File file = findFile(BASE_UPLOAD_DIR + game.getId() + "/files", key);
        return new FileSystemResource(file);
    }

    // ============================================================
    //                      VALIDATION
    // ============================================================

    private Client validateAndGetClient(String token) {
        Client client = clientService.getClient(token);
        if (client == null) throw new IllegalArgumentException("Token is not valid");
        return client;
    }

    private void validateGameInput(String title, MultipartFile gameFile, MultipartFile logoFile) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Title is required");
        if (gameFile == null || gameFile.isEmpty()) throw new IllegalArgumentException("Game file is required");
        if (logoFile == null || logoFile.isEmpty()) throw new IllegalArgumentException("Logo image is required");
    }

    private void ensureTitleIsUnique(String title) {
        if (gameRepository.existsByTitleIgnoreCase(title))
            throw new IllegalArgumentException("A game with this title already exists");
    }

    // ============================================================
    //                      GAME ENTITY
    // ============================================================

    private Game buildGameEntity(Client client, String title, String description, BigDecimal price,
                                 MultipartFile gameFile, List<Tag> tags) {

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

    private List<Tag> parseTags(String tagsText) {
        if (tagsText == null || tagsText.isBlank()) return List.of();

        return Arrays.stream(tagsText.split(","))
                .map(String::trim)
                .filter(t -> !t.isBlank())
                .map(t -> tagRepository.findByNameIgnoreCase(t)
                        .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + t)))
                .toList();
    }

    // ============================================================
    //                      FILE SAVE
    // ============================================================

    private void saveMainFiles(Game game, MultipartFile gameFile, MultipartFile logoFile) {
        game.setGameFileKey(saveGameFile(game, gameFile));
        game.setGameLogoKey(saveImage(game, logoFile, "logo"));
        gameRepository.save(game);
    }

    private void saveTeasers(Game game, MultipartFile... teasers) {
        String[] types = {"VERTICAL", "HORIZONTAL_1", "HORIZONTAL_2"};

        for (int i = 0; i < teasers.length; i++) {
            MultipartFile file = teasers[i];
            if (file == null || file.isEmpty()) continue;

            String key = saveImage(game, file, "teasers");

            Teaser teaser = new Teaser();
            teaser.setGame(game);
            teaser.setObjectKey(key);
            teaser.setType(types[i]);

            teaserRepository.save(teaser);
        }
    }

    private String saveGameFile(Game game, MultipartFile file) {
        validateMime(file,
                "application/zip",
                "application/x-zip-compressed",
                "application/octet-stream"
        );

        return saveFile(
                BASE_UPLOAD_DIR + game.getId() + "/files",
                file,
                "zip"
        );
    }

    private String saveImage(Game game, MultipartFile file, String folder) {
        validateMimeStartsWith(file, "image/");
        return saveFile(
                BASE_UPLOAD_DIR + game.getId() + "/" + folder,
                file,
                "png"
        );
    }

    private String saveFile(String folderPath, MultipartFile file, String defaultExt) {
        try {
            File folder = new File(folderPath);
            if (!folder.exists()) folder.mkdirs();

            String key = UUID.randomUUID().toString().replace("-", "");
            String ext = FileUtils.getExtension(file.getOriginalFilename());
            if (ext.isEmpty()) ext = defaultExt;

            File target = new File(folder, key + "." + ext);
            file.transferTo(target);

            return key;

        } catch (IOException e) {
            throw new RuntimeException("Error saving file: " + e.getMessage(), e);
        }
    }

    private void validateMime(MultipartFile file, String... allowed) {
        try {
            String detected = FileUtils.getContentType(file.getBytes(), file.getOriginalFilename());
            if (Arrays.stream(allowed).noneMatch(detected::equals))
                throw new IllegalArgumentException("Invalid file type: " + detected);
        } catch (IOException e) {
            throw new RuntimeException("Error validating file type", e);
        }
    }

    private void validateMimeStartsWith(MultipartFile file, String prefix) {
        try {
            String detected = FileUtils.getContentType(file.getBytes(), file.getOriginalFilename());
            if (!detected.startsWith(prefix))
                throw new IllegalArgumentException("Invalid image type: " + detected);
        } catch (IOException e) {
            throw new RuntimeException("Error validating image type", e);
        }
    }

    // ============================================================
    //                      FILE LOAD
    // ============================================================

    public byte[] loadLogo(Integer id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        File file = findFile(BASE_UPLOAD_DIR + game.getId() + "/logo", game.getGameLogoKey());

        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Error loading logo", e);
        }
    }

    public byte[] getVerticalImage(Integer id) {
        return loadTeaser(id, "VERTICAL");
    }

    public byte[] getHorizontal1Image(Integer id) {
        return loadTeaser(id, "HORIZONTAL_1");
    }

    public byte[] getHorizontal2Image(Integer id) {
        return loadTeaser(id, "HORIZONTAL_2");
    }

    private byte[] loadTeaser(Integer gameId, String type) {
        Teaser teaser = teaserRepository.findByGameIdAndType(gameId, type)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Teaser not found: " + type));

        File file = findFile(BASE_UPLOAD_DIR + gameId + "/teasers", teaser.getObjectKey());

        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Error loading teaser: " + type, e);
        }
    }

    private File findFile(String folder, String key) {
        File dir = new File(folder);
        File[] matches = dir.listFiles((d, name) -> name.startsWith(key + "."));
        if (matches == null || matches.length == 0)
            throw new IllegalStateException("File with key " + key + " not found");
        return matches[0];
    }

    // ============================================================
    //                      QUERY METHODS
    // ============================================================

    public GameResponseDTO getGameById(Integer id) {
        return gameRepository.findByIdWithDevAndTags(id)
                .map(this::toGameDetailResponseDTO)
                .orElse(null);
    }

    public List<GameResponseDTO> getAllGames() {
        return gameRepository.findAllWithDevAndTagsOrderByIdDesc()
                .stream()
                .map(this::toMarketplaceResponseDTO)
                .toList();
    }

    public java.util.List<GameResponseDTO> getGames(GameSearchDTO dto) {
        java.util.List<Game> games;

        if (dto.getTitle() != null && !dto.getTitle().isEmpty())
            games = gameRepository.findByTitleContainingIgnoreCase(dto.getTitle());
        else if (dto.getDeveloperName() != null && !dto.getDeveloperName().isEmpty())
            games = gameRepository.findByDev_Username(dto.getDeveloperName());
        else if (dto.getTagNames() != null && !dto.getTagNames().isEmpty())
            games = gameRepository.findByAllTagNames(dto.getTagNames(), dto.getTagNames().size());
        else {
            games = gameRepository.findAll();
            Collections.shuffle(games);
            games = games.stream().limit(dto.getLimit() != null ? dto.getLimit() : 25).toList();
        }

        return games.stream().map(this::toResponseDTO).toList();
    }

    // ============================================================
    //                      DTO MAPPING
    // ============================================================

    private GameResponseDTO toGameDetailResponseDTO(Game game) {
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

        if (game.getTags() != null) {
            dto.setTags(game.getTags().stream().map(Tag::getName).toList());
        } else {
            dto.setTags(List.of());
        }

        dto.setTeasers(java.util.List.of());

        return dto;
    }

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

        if (game.getTags() != null) {
            dto.setTags(game.getTags().stream().map(Tag::getName).toList());
        } else {
            dto.setTags(List.of());
        }

        if (game.getTeasers() != null) {
            dto.setTeasers(game.getTeasers().stream().map(this::toTeaserResponseDTO).toList());
        } else {
            dto.setTeasers(List.of());
        }

        return dto;
    }

    private GameResponseDTO toMarketplaceResponseDTO(Game game) {
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

        if (game.getTags() != null) {
            dto.setTags(game.getTags()
                    .stream()
                    .map(Tag::getName)
                    .toList());
        } else {
            dto.setTags(List.of());
        }

        dto.setTeasers(List.of());

        return dto;
    }

    private TeaserResponseDTO toTeaserResponseDTO(Teaser teaser) {
        TeaserResponseDTO dto = new TeaserResponseDTO();
        dto.setId(teaser.getId());
        dto.setGameId(teaser.getGame().getId());
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

    private BigDecimal calculateSizeMb(MultipartFile file) {
        if (file == null || file.isEmpty()) return BigDecimal.ZERO;

        double sizeMb = file.getSize() / 1024.0 / 1024.0;
        return BigDecimal.valueOf(sizeMb).setScale(2, RoundingMode.HALF_UP);
    }
}