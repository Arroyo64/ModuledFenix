package org.ies.fenix.server.services;

import org.ies.fenix.controller.dto.game.GameResponseDTO;
import org.ies.fenix.controller.dto.game.GameSearchDTO;
import org.ies.fenix.server.models.Game;
import org.ies.fenix.server.models.Tag;
import org.ies.fenix.server.repositories.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ies.fenix.server.repositories.TagRepository;

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

    public GameResponseDTO getById(Integer id) {
        Game game = gameRepository.findById(id).orElse(null);
        if (game == null) {
            return null;
        }
        return toResponseDTO(game);
    }

    public List<GameResponseDTO> getByTitle(String title) {
        List<Game> games = gameRepository.findByTitleContainingIgnoreCase(title);
        List<GameResponseDTO> response = new ArrayList<>();

        for (Game game : games) {
            response.add(toResponseDTO(game));
        }

        return response;
    }

    public List<GameResponseDTO> getByDevId(Integer devId) {
        List<Game> games = gameRepository.findByDevId(devId);
        List<GameResponseDTO> response = new ArrayList<>();

        for (Game game : games) {
            response.add(toResponseDTO(game));
        }

        return response;
    }

    public List<GameResponseDTO> getByTagId(Integer tagId) {
        List<Game> games = gameRepository.findByTagsId(tagId);
        List<GameResponseDTO> response = new ArrayList<>();

        for (Game game : games) {
            response.add(toResponseDTO(game));
        }

        return response;
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

        List<String> tagNames = new ArrayList<>();
        if (game.getTags() != null) {
            for (Tag tag : game.getTags()) {
                tagNames.add(tag.getName());
            }
        }
        dto.setTags(tagNames);

        return dto;
    }

    public String formatSizeFromMB(BigDecimal mb) {
        if (mb.compareTo(BigDecimal.ZERO) == 0) return "0 MB";

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
        if (downloads == 0) return "0";

        final int CIFRAS = 3;

        String[] units = {"", "K", "M", "B", "T"};
        double value = downloads;
        int unitIndex = 0;

        // Escalar
        while (value >= 1000 && unitIndex < units.length - 1) {
            value /= 1000;
            unitIndex++;
        }

        // Cifras significativas
        double scale = Math.pow(10, Math.floor(Math.log10(value)) + 1);
        double rounded = Math.round(value / scale * Math.pow(10, CIFRAS))
                / Math.pow(10, CIFRAS) * scale;

        // Formato limpio
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

    public GameResponseDTO getGameById(Integer id) {
        return gameRepository.findById(id).map(this::toResponseDTO).orElse(null);
    }
}