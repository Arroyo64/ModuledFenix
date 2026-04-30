package org.ies.fenix.server.services;

import org.ies.fenix.controller.dto.game.GameResponseDTO;
import org.ies.fenix.controller.dto.game.GameSearchDTO;
import org.ies.fenix.server.models.Client;
import org.ies.fenix.server.models.Game;
import org.ies.fenix.server.models.Tag;
import org.ies.fenix.server.repositories.GameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameService gameService;

    @Test
    void getGames_whenTitleIsProvided_usesTitleSearchAndMapsResponse() {
        Game game = game(1, "Fenix Quest", "devUser", new BigDecimal("512"), 2000,
                List.of(tag("RPG"), tag("Fantasy")));
        GameSearchDTO search = new GameSearchDTO("fenix", null, null, null);

        when(gameRepository.findByTitleContainingIgnoreCase("fenix")).thenReturn(List.of(game));

        List<GameResponseDTO> response = gameService.getGames(search);

        assertEquals(1, response.size());
        assertEquals(1, response.get(0).getId());
        assertEquals("Fenix Quest", response.get(0).getTitle());
        assertEquals("512 MB", response.get(0).getSizeApproximation());
        assertEquals("2K", response.get(0).getDownloadsApproximation());
        assertEquals("devUser", response.get(0).getDevUsername());
        assertEquals(List.of("RPG", "Fantasy"), response.get(0).getTags());

        verify(gameRepository).findByTitleContainingIgnoreCase("fenix");
        verify(gameRepository, never()).findAll();
    }

    @Test
    void getGames_whenDeveloperNameIsProvided_usesDeveloperSearch() {
        GameSearchDTO search = new GameSearchDTO(null, "devUser", null, null);
        Game game = game(1, "Fenix Quest", "devUser", new BigDecimal("512"), 100, List.of());

        when(gameRepository.findByDev_Username("devUser")).thenReturn(List.of(game));

        List<GameResponseDTO> response = gameService.getGames(search);

        assertEquals(1, response.size());
        assertEquals("devUser", response.get(0).getDevUsername());
        verify(gameRepository).findByDev_Username("devUser");
        verify(gameRepository, never()).findByTitleContainingIgnoreCase(anyString());
    }

    @Test
    void getGames_whenTagNamesAreProvided_usesAllTagsSearch() {
        List<String> tags = List.of("RPG", "Fantasy");
        GameSearchDTO search = new GameSearchDTO(null, null, tags, null);
        Game game = game(1, "Fenix Quest", "devUser", new BigDecimal("512"), 100, List.of(tag("RPG")));

        when(gameRepository.findByAllTagNames(tags, tags.size())).thenReturn(List.of(game));

        List<GameResponseDTO> response = gameService.getGames(search);

        assertEquals(1, response.size());
        assertEquals("Fenix Quest", response.get(0).getTitle());
        verify(gameRepository).findByAllTagNames(tags, 2);
    }

    @Test
    void getGames_whenNoFilters_usesFindAllAndDefaultLimitOf25() {
        List<Game> games = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            games.add(game(i, "Game " + i, "devUser", new BigDecimal("100"), i, List.of()));
        }
        GameSearchDTO search = new GameSearchDTO(null, null, null, null);

        when(gameRepository.findAll()).thenReturn(games);

        List<GameResponseDTO> response = gameService.getGames(search);

        assertEquals(25, response.size());
        verify(gameRepository).findAll();
    }

    @Test
    void getById_whenGameExists_returnsMappedDto() {
        Game game = game(1, "Fenix Quest", "devUser", new BigDecimal("1024"), 0, List.of());
        when(gameRepository.findById(1)).thenReturn(Optional.of(game));

        GameResponseDTO response = gameService.getById(1);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("Fenix Quest", response.getTitle());
        assertEquals("1 GB", response.getSizeApproximation());
        assertEquals("0", response.getDownloadsApproximation());
        verify(gameRepository).findById(1);
    }

    @Test
    void getById_whenGameDoesNotExist_returnsNull() {
        when(gameRepository.findById(404)).thenReturn(Optional.empty());

        GameResponseDTO response = gameService.getById(404);

        assertNull(response);
        verify(gameRepository).findById(404);
    }

    @Test
    void formatSizeFromMB_coversZeroMegabytesAndGigabytes() {
        assertEquals("0 MB", gameService.formatSizeFromMB(BigDecimal.ZERO));
        assertEquals("1 GB", gameService.formatSizeFromMB(new BigDecimal("1024")));
        assertEquals("1.5 GB", gameService.formatSizeFromMB(new BigDecimal("1536")));
    }

    @Test
    void formatDownloads_coversZeroThousandsAndMillions() {
        assertEquals("0", gameService.formatDownloads(0));
        assertEquals("999", gameService.formatDownloads(999));
        assertEquals("1.5K", gameService.formatDownloads(1500));
        assertEquals("1.5M", gameService.formatDownloads(1_500_000));
    }

    private Game game(Integer id, String title, String devUsername, BigDecimal sizeMb,
                      Integer downloads, List<Tag> tags) {
        Client dev = new Client();
        dev.setId(2);
        dev.setUsername(devUsername);

        Game game = new Game();
        game.setId(id);
        game.setTitle(title);
        game.setDescription("Description");
        game.setDev(dev);
        game.setTamanoMb(sizeMb);
        game.setDownloads(downloads);
        game.setPrice(new BigDecimal("9.99"));
        game.setTags(tags);
        return game;
    }

    private Tag tag(String name) {
        Tag tag = new Tag();
        tag.setName(name);
        return tag;
    }
}
