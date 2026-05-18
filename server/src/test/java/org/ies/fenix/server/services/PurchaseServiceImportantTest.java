package org.ies.fenix.server.services;

import org.ies.fenix.controller.dto.purchase.LibraryGameDTO;
import org.ies.fenix.controller.dto.purchase.PurchaseCreateDTO;
import org.ies.fenix.controller.dto.purchase.PurchaseResponseDTO;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceImportantTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private PurchaseService purchaseService;

    @Test
    void createPurchase_whenValid_savesPurchaseIncrementsDownloadsAndReturnsDto() {
        Client client = new Client();
        client.setId(1);
        client.setUsername("David");

        Game game = new Game();
        game.setId(10);
        game.setTitle("Fenix Quest");
        game.setDownloads(4);
        game.setPrice(BigDecimal.valueOf(9.99));

        PurchaseCreateDTO request = new PurchaseCreateDTO(1, 10);

        when(clientRepository.findById(1)).thenReturn(Optional.of(client));
        when(gameRepository.findById(10)).thenReturn(Optional.of(game));
        when(purchaseRepository.existsByClientIdAndGameId(1, 10)).thenReturn(false);
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> {
            Purchase purchase = invocation.getArgument(0);
            purchase.setId(99);
            return purchase;
        });

        PurchaseResponseDTO response = purchaseService.createPurchase(request);

        assertNotNull(response);
        assertEquals(99, response.getId());
        assertEquals(1, response.getClientId());
        assertEquals(10, response.getGameId());
        assertEquals("Fenix Quest", response.getGameTitle());
        assertEquals(BigDecimal.valueOf(9.99), response.getPrice());
        assertEquals(5, game.getDownloads());

        verify(purchaseRepository).save(any(Purchase.class));
        verify(gameRepository).save(game);
    }

    @Test
    void createPurchase_whenClientDoesNotExist_returnsNull() {
        PurchaseCreateDTO request = new PurchaseCreateDTO(99, 10);

        when(clientRepository.findById(99)).thenReturn(Optional.empty());

        PurchaseResponseDTO response = purchaseService.createPurchase(request);

        assertNull(response);
        verifyNoInteractions(purchaseRepository);
        verifyNoInteractions(gameRepository);
    }

    @Test
    void createPurchase_whenGameDoesNotExist_returnsNull() {
        Client client = new Client();
        client.setId(1);

        PurchaseCreateDTO request = new PurchaseCreateDTO(1, 99);

        when(clientRepository.findById(1)).thenReturn(Optional.of(client));
        when(gameRepository.findById(99)).thenReturn(Optional.empty());

        PurchaseResponseDTO response = purchaseService.createPurchase(request);

        assertNull(response);
        verify(purchaseRepository, never()).save(any(Purchase.class));
    }

    @Test
    void createPurchase_whenAlreadyPurchased_throwsException() {
        Client client = new Client();
        client.setId(1);

        Game game = new Game();
        game.setId(10);

        PurchaseCreateDTO request = new PurchaseCreateDTO(1, 10);

        when(clientRepository.findById(1)).thenReturn(Optional.of(client));
        when(gameRepository.findById(10)).thenReturn(Optional.of(game));
        when(purchaseRepository.existsByClientIdAndGameId(1, 10)).thenReturn(true);

        assertThrows(AlreadyPurchasedException.class,
                () -> purchaseService.createPurchase(request));

        verify(purchaseRepository, never()).save(any(Purchase.class));
        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    void getLibraryByClientId_returnsPurchasedGames() {
        Game game = new Game();
        game.setId(10);
        game.setTitle("Fenix Quest");

        Purchase purchase = new Purchase();
        purchase.setId(1);
        purchase.setGame(game);

        when(purchaseRepository.findByClientId(1))
                .thenReturn(List.of(purchase));

        List<LibraryGameDTO> response = purchaseService.getLibraryByClientId(1);

        assertEquals(1, response.size());
        assertEquals(10, response.getFirst().getGameId());
        assertEquals("Fenix Quest", response.getFirst().getTitle());

        verify(purchaseRepository).findByClientId(1);
    }

    @Test
    void hasPurchased_delegatesToRepository() {
        when(purchaseRepository.existsByClientIdAndGameId(1, 10))
                .thenReturn(true);

        boolean response = purchaseService.hasPurchased(1, 10);

        assertTrue(response);
        verify(purchaseRepository).existsByClientIdAndGameId(1, 10);
    }
}