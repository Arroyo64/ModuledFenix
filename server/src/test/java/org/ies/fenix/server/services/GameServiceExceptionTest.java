package org.ies.fenix.server.services;

import org.ies.fenix.server.repositories.GameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceExceptionTest {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameService gameService;

    @Test
    void getById_whenRepositoryRejectsInvalidId_propagatesIllegalArgumentException() {
        when(gameRepository.findById(null))
                .thenThrow(new IllegalArgumentException("gameId cannot be null"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.getById(null)
        );

        assertEquals("gameId cannot be null", exception.getMessage());
        verify(gameRepository).findById(null);
    }
}
