package org.ies.fenix.server.services;

import org.ies.fenix.controller.dto.purchase.PurchaseCreateDTO;
import org.ies.fenix.server.exception.AlreadyPurchasedException;
import org.ies.fenix.server.models.Client;
import org.ies.fenix.server.models.Game;
import org.ies.fenix.server.models.Purchase;
import org.ies.fenix.server.repositories.ClientRepository;
import org.ies.fenix.server.repositories.GameRepository;
import org.ies.fenix.server.repositories.PurchaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceExceptionTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private PurchaseService purchaseService;

    @Test
    void createPurchase_whenAlreadyPurchased_throwsProjectExceptionWithMessage() {
        Client client = new Client();
        client.setId(1);

        Game game = new Game();
        game.setId(10);

        when(clientRepository.findById(1)).thenReturn(Optional.of(client));
        when(gameRepository.findById(10)).thenReturn(Optional.of(game));
        when(purchaseRepository.existsByClientIdAndGameId(1, 10)).thenReturn(true);

        AlreadyPurchasedException exception = assertThrows(
                AlreadyPurchasedException.class,
                () -> purchaseService.createPurchase(new PurchaseCreateDTO(1, 10))
        );

        assertEquals("El cliente ya compró este juego", exception.getMessage());
        verify(purchaseRepository).existsByClientIdAndGameId(1, 10);
        verify(purchaseRepository, never()).save(any(Purchase.class));
    }

    @Test
    void createPurchase_whenClientRepositoryRejectsInvalidId_propagatesIllegalArgumentException() {
        PurchaseCreateDTO dto = new PurchaseCreateDTO(null, 10);
        when(clientRepository.findById(null))
                .thenThrow(new IllegalArgumentException("clientId cannot be null"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.createPurchase(dto)
        );

        assertEquals("clientId cannot be null", exception.getMessage());
        verify(clientRepository).findById(null);
        verifyNoInteractions(gameRepository);
        verify(purchaseRepository, never()).save(any(Purchase.class));
    }
}
