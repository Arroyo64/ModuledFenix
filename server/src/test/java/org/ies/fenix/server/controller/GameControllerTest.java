package org.ies.fenix.server.controller;

import org.ies.fenix.controller.dto.game.GameResponseDTO;
import org.ies.fenix.controller.dto.game.GameSearchDTO;
import org.ies.fenix.server.services.GameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameControllerTest {

    @Mock
    private GameService gameService;

    @InjectMocks
    private GameController gameController;

    @Test
    void getManyGames_whenDtoIsValid_returnsOkWithGames() {
        GameSearchDTO request = new GameSearchDTO("Fenix", null, null, 10);
        List<GameResponseDTO> games = List.of(aGameResponse());
        when(gameService.getGames(request)).thenReturn(games);

        ResponseEntity<List<GameResponseDTO>> response = gameController.getManyGames(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(games, response.getBody());
        verify(gameService).getGames(request);
    }

    @Test
    void getManyGames_whenDtoIsNull_returnsBadRequestAndDoesNotCallService() {
        ResponseEntity<List<GameResponseDTO>> response = gameController.getManyGames(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verifyNoInteractions(gameService);
    }

    @Test
    void getManyGames_whenServiceThrowsException_returnsBadRequest() {
        GameSearchDTO request = new GameSearchDTO("Fenix", null, null, 10);
        when(gameService.getGames(request)).thenThrow(new RuntimeException("unexpected error"));

        ResponseEntity<List<GameResponseDTO>> response = gameController.getManyGames(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(gameService).getGames(request);
    }

    @Test
    void getById_whenIdExists_returnsOkWithGame() {
        GameResponseDTO game = aGameResponse();
        when(gameService.getGameById(1)).thenReturn(game);

        ResponseEntity<GameResponseDTO> response = gameController.getById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(game, response.getBody());
        verify(gameService).getGameById(1);
    }

    @Test
    void getById_whenIdIsNull_returnsBadRequestAndDoesNotCallService() {
        ResponseEntity<GameResponseDTO> response = gameController.getById(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verifyNoInteractions(gameService);
    }

    @Test
    void getById_whenServiceThrowsException_returnsBadRequest() {
        when(gameService.getGameById(99)).thenThrow(new RuntimeException("not found"));

        ResponseEntity<GameResponseDTO> response = gameController.getById(99);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(gameService).getGameById(99);
    }

    private GameResponseDTO aGameResponse() {
        return new GameResponseDTO(
                1,
                "Fenix",
                "Visual novel",
                "512 MB",
                "1K",
                BigDecimal.TEN,
                "devUser",
                List.of("fantasy")
        );
    }
}
