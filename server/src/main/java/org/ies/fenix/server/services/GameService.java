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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    @Transactional
    public GameResponseDTO createGame(String token,
                                      String title,
                                      String description,
                                      BigDecimal price,
                                      String tagsText,
                                      MultipartFile gameFile,
                                      MultipartFile logoFile,
                                      MultipartFile verticalImage,
                                      MultipartFile horizontalImageOne,
                                      MultipartFile horizontalImageTwo) {

        Client client = clientService.getClient(token);

        if (client == null) {
            throw new IllegalArgumentException("Token is not valid");
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }

        if (gameRepository.existsByTitleIgnoreCase(title)) {
            throw new IllegalArgumentException("A game with this title already exists");
        }

        if (gameFile == null || gameFile.isEmpty()) {
            throw new IllegalArgumentException("Game file is required");
        }

        if (logoFile == null || logoFile.isEmpty()) {
            throw new IllegalArgumentException("Logo image is required");
        }

        List<Tag> tags = parseExistingTags(tagsText);

        Game game = new Game();
        game.setTitle(title.trim());
        game.setDescription(description);
        game.setDev(client);
        game.setPrice(price != null ? price : BigDecimal.ZERO);
        game.setDownloads(0);
        game.setSizeMb(calculateSizeMb(gameFile));
        game.setTags(tags);

        game = gameRepository.save(game);

        String gameFileKey = fileStorageService.saveGameFile(game.getId(), gameFile);
        String logoKey = fileStorageService.saveGameLogo(game.getId(), logoFile);

        game.setGameFileKey(gameFileKey);
        game.setGameLogoKey(logoKey);

        game = gameRepository.save(game);

        saveTeaserIfPresent(game, verticalImage, "VERTICAL", "vertical");
        saveTeaserIfPresent(game, horizontalImageOne, "HORIZONTAL_1", "horizontal_1");
        saveTeaserIfPresent(game, horizontalImageTwo, "HORIZONTAL_2", "horizontal_2");

        return getGameById(game.getId());
    }

    private void saveTeaserIfPresent(Game game,
                                     MultipartFile file,
                                     String type,
                                     String fileBaseName) {
        if (file == null || file.isEmpty()) {
            return;
        }

        String objectKey = fileStorageService.saveTeaser(game.getId(), file, fileBaseName);

        Teaser teaser = new Teaser();
        teaser.setGame(game);
        teaser.setObjectKey(objectKey);
        teaser.setType(type);

        teaserRepository.save(teaser);
    }

    private BigDecimal calculateSizeMb(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return BigDecimal.ZERO;
        }

        double sizeMb = file.getSize() / 1024.0 / 1024.0;

        return BigDecimal.valueOf(sizeMb).setScale(2, RoundingMode.HALF_UP);
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
            games = gameRepository.findByAllTagNames(
                    dto.getTagNames(),
                    dto.getTagNames().size()
            );

        } else {
            games = gameRepository.findAll();
            Collections.shuffle(games);

            int limit = (dto.getLimit() != null) ? dto.getLimit() : 25;
            games = games.stream().limit(limit).toList();
        }

        return games.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public GameResponseDTO getGameById(Integer id) {
        return gameRepository.findById(id)
                .map(this::toResponseDTO)
                .orElse(null);
    }

    public List<GameResponseDTO> getByTitle(String title) {
        List<Game> games = gameRepository.findByTitleContainingIgnoreCase(title);

        return games.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<GameResponseDTO> getByDevId(Integer devId) {
        List<Game> games = gameRepository.findByDevId(devId);

        return games.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<GameResponseDTO> getByTagId(Integer tagId) {
        List<Game> games = gameRepository.findByTagsId(tagId);

        return games.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private GameResponseDTO toResponseDTO(Game game) {
        GameResponseDTO dto = new GameResponseDTO();

        dto.setId(game.getId());
        dto.setTitle(game.getTitle());
        dto.setDescription(game.getDescription());

        if (game.getSizeMb() != null) {
            dto.setSizeApproximation(formatSizeFromMB(game.getSizeMb()));
        } else {
            dto.setSizeApproximation("0 MB");
        }

        if (game.getDownloads() != null) {
            dto.setDownloadsApproximation(formatDownloads(game.getDownloads()));
        } else {
            dto.setDownloadsApproximation("0");
        }

        dto.setPrice(game.getPrice() != null ? game.getPrice() : BigDecimal.ZERO);

        if (game.getDev() != null) {
            dto.setDevUsername(game.getDev().getUsername());
        } else {
            dto.setDevUsername("Unknown");
        }

        dto.setGameLogoKey(game.getGameLogoKey());
        dto.setGameFileKey(game.getGameFileKey());

        List<String> tagNames = new ArrayList<>();
        if (game.getTags() != null) {
            for (Tag tag : game.getTags()) {
                tagNames.add(tag.getName());
            }
        }
        dto.setTags(tagNames);

        List<TeaserResponseDTO> teaserDtos = new ArrayList<>();
        if (game.getTeasers() != null) {
            for (Teaser teaser : game.getTeasers()) {
                teaserDtos.add(toTeaserResponseDTO(teaser));
            }
        }
        dto.setTeasers(teaserDtos);

        return dto;
    }

    private TeaserResponseDTO toTeaserResponseDTO(Teaser teaser) {
        TeaserResponseDTO dto = new TeaserResponseDTO();

        dto.setId(teaser.getId());

        if (teaser.getGame() != null) {
            dto.setGameId(teaser.getGame().getId());
        }

        dto.setObjectKey(teaser.getObjectKey());
        dto.setType(teaser.getType());

        return dto;
    }

    public String formatSizeFromMB(BigDecimal mb) {
        if (mb.compareTo(BigDecimal.ZERO) == 0) {
            return "0 MB";
        }

        final int CIFRAS = 3;

        String[] units = {"MB", "GB", "TB", "PB"};
        int unitIndex = 0;

        BigDecimal base = BigDecimal.valueOf(1024);

        while (mb.compareTo(base) >= 0 && unitIndex < units.length - 1) {
            mb = mb.divide(base, MathContext.DECIMAL128);
            unitIndex++;
        }

        MathContext mc = new MathContext(CIFRAS, RoundingMode.HALF_UP);
        BigDecimal rounded = mb.round(mc).stripTrailingZeros();

        return rounded.toPlainString() + " " + units[unitIndex];
    }

    public String formatDownloads(long downloads) {
        if (downloads == 0) {
            return "0";
        }

        final int CIFRAS = 3;

        String[] units = {"", "K", "M", "B", "T"};
        double value = downloads;
        int unitIndex = 0;

        while (value >= 1000 && unitIndex < units.length - 1) {
            value /= 1000;
            unitIndex++;
        }

        double scale = Math.pow(10, Math.floor(Math.log10(value)) + 1);
        double rounded = Math.round(value / scale * Math.pow(10, CIFRAS))
                / Math.pow(10, CIFRAS) * scale;

        if (rounded % 1 == 0) {
            return String.format("%.0f%s", rounded, units[unitIndex]);
        }

        return rounded + units[unitIndex];
    }

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
}