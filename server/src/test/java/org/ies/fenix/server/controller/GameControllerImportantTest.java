package org.ies.fenix.server.controller;

import org.ies.fenix.controller.dto.game.GameResponseDTO;
import org.ies.fenix.controller.dto.game.GameSearchDTO;
import org.ies.fenix.server.services.GameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameControllerImportantTest {

    @Mock
    private GameService gameService;

    @InjectMocks
    private GameController gameController;

    @Test
    void getAllGames_whenServiceReturnsGames_returnsOk() {
        GameResponseDTO game = new GameResponseDTO();
        game.setId(1);
        game.setTitle("Fenix Quest");
        game.setTags(List.of("RPG", "Fantasy"));

        when(gameService.getAllGames()).thenReturn(List.of(game));

        ResponseEntity<List<GameResponseDTO>> response =
                gameController.getAllGames("Bearer token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Fenix Quest", response.getBody().getFirst().getTitle());
        assertEquals(List.of("RPG", "Fantasy"), response.getBody().getFirst().getTags());

        verify(gameService).getAllGames();
    }

    @Test
    void getManyGames_whenDtoIsNull_returnsBadRequest() {
        ResponseEntity<List<GameResponseDTO>> response =
                gameController.getManyGames(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(gameService);
    }

    @Test
    void getById_whenGameExists_returnsOk() {
        GameResponseDTO game = new GameResponseDTO();
        game.setId(1);
        game.setTitle("Fenix Quest");

        when(gameService.getGameById(1)).thenReturn(game);

        ResponseEntity<GameResponseDTO> response =
                gameController.getById("Bearer token", 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(game, response.getBody());

        verify(gameService).getGameById(1);
    }

    @Test
    void getHorizontal1_whenImageExists_returnsPngBytes() {
        byte[] imageBytes = new byte[]{1, 2, 3};

        when(gameService.getHorizontal1Image(1)).thenReturn(imageBytes);

        ResponseEntity<byte[]> response =
                gameController.getHorizontal1("Bearer token", 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(imageBytes, response.getBody());
        assertEquals("image/png", response.getHeaders().getContentType().toString());

        verify(gameService).getHorizontal1Image(1);
    }

    @Test
    void getHorizontal1_whenImageDoesNotExist_returnsBadRequestWithMessage() {
        when(gameService.getHorizontal1Image(1))
                .thenThrow(new IllegalStateException("Teaser not found: HORIZONTAL_1"));

        ResponseEntity<byte[]> response =
                gameController.getHorizontal1("Bearer token", 1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Teaser not found: HORIZONTAL_1", new String(response.getBody()));

        verify(gameService).getHorizontal1Image(1);
    }

    @Test
    void downloadGame_whenFileExists_returnsOctetStream() {
        ByteArrayResource resource = new ByteArrayResource(new byte[]{1, 2, 3}) {
            @Override
            public String getFilename() {
                return "game.zip";
            }
        };

        when(gameService.downloadGame(1)).thenReturn(resource);

        ResponseEntity<?> response =
                gameController.downloadGame("Bearer token", 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resource, response.getBody());
        assertEquals("application/octet-stream", response.getHeaders().getContentType().toString());
        assertEquals("attachment; filename=\"game.zip\"",
                response.getHeaders().getFirst("Content-Disposition"));

        verify(gameService).downloadGame(1);
    }
}