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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceFlowTest {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameService gameService;

    @Test
    void getGames_whenFilteringByTags_transformsGamesIntoDtos() {
        List<String> tags = List.of("RPG", "Fantasy");
        GameSearchDTO search = new GameSearchDTO(null, null, tags, null);
        Game game = gameWithTags();

        when(gameRepository.findByAllTagNames(tags, tags.size())).thenReturn(List.of(game));

        List<GameResponseDTO> response = gameService.getGames(search);

        assertEquals(1, response.size());
        GameResponseDTO dto = response.get(0);
        assertEquals(10, dto.getId());
        assertEquals("Fenix Quest", dto.getTitle());
        assertEquals("Adventure game", dto.getDescription());
        assertEquals("1 GB", dto.getSizeApproximation());
        assertEquals("1.5K", dto.getDownloadsApproximation());
        assertEquals(BigDecimal.valueOf(19.99), dto.getPrice());
        assertEquals("devUser", dto.getDevUsername());
        assertEquals(List.of("RPG", "Fantasy"), dto.getTags());
        verify(gameRepository).findByAllTagNames(tags, 2);
    }

    @Test
    void getGameById_whenGameExists_fetchesEntityAndReturnsDto() {
        Game game = gameWithTags();
        when(gameRepository.findById(10)).thenReturn(Optional.of(game));

        GameResponseDTO dto = gameService.getGameById(10);

        assertNotNull(dto);
        assertEquals(10, dto.getId());
        assertEquals("Fenix Quest", dto.getTitle());
        assertEquals(List.of("RPG", "Fantasy"), dto.getTags());
        verify(gameRepository).findById(10);
    }

    private Game gameWithTags() {
        Client dev = new Client();
        dev.setId(2);
        dev.setUsername("devUser");

        Tag rpg = new Tag();
        rpg.setId(1);
        rpg.setName("RPG");
        Tag fantasy = new Tag();
        fantasy.setId(2);
        fantasy.setName("Fantasy");

        Game game = new Game();
        game.setId(10);
        game.setTitle("Fenix Quest");
        game.setDescription("Adventure game");
        game.setTamanoMb(BigDecimal.valueOf(1024));
        game.setDownloads(1500);
        game.setPrice(BigDecimal.valueOf(19.99));
        game.setDev(dev);
        game.setTags(List.of(rpg, fantasy));
        return game;
    }
}
