package org.ies.fenix.server.services;

import org.ies.fenix.controller.dto.purchase.DownloadResponseDTO;
import org.ies.fenix.controller.dto.purchase.LibraryGameDTO;
import org.ies.fenix.controller.dto.purchase.PurchaseCreateDTO;
import org.ies.fenix.controller.dto.purchase.PurchaseResponseDTO;
import org.ies.fenix.server.models.Client;
import org.ies.fenix.server.models.Game;
import org.ies.fenix.server.models.Purchase;
import org.ies.fenix.server.repositories.ClientRepository;
import org.ies.fenix.server.repositories.GameRepository;
import org.ies.fenix.server.repositories.PurchaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceFlowTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private PurchaseService purchaseService;

    @Test
    void createPurchase_whenClientAndGameExist_savesPurchaseAndReturnsDto() {
        Client client = client(1, "ana");
        Game game = game(10, "Fenix Quest", 7, BigDecimal.valueOf(12.99));
        PurchaseCreateDTO request = new PurchaseCreateDTO(1, 10);

        when(clientRepository.findById(1)).thenReturn(Optional.of(client));
        when(gameRepository.findById(10)).thenReturn(Optional.of(game));
        when(purchaseRepository.existsByClientIdAndGameId(1, 10)).thenReturn(false);
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> {
            Purchase saved = invocation.getArgument(0);
            saved.setId(99);
            return saved;
        });

        PurchaseResponseDTO response = purchaseService.createPurchase(request);

        assertNotNull(response);
        assertEquals(99, response.getId());
        assertEquals(1, response.getClientId());
        assertEquals(10, response.getGameId());
        assertEquals("Fenix Quest", response.getGameTitle());
        assertEquals(BigDecimal.valueOf(12.99), response.getPrice());

        ArgumentCaptor<Purchase> purchaseCaptor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseRepository).save(purchaseCaptor.capture());
        assertSame(client, purchaseCaptor.getValue().getClient());
        assertSame(game, purchaseCaptor.getValue().getGame());
        verify(clientRepository).findById(1);
        verify(gameRepository).findById(10);
        verify(purchaseRepository).existsByClientIdAndGameId(1, 10);
    }

    @Test
    void downloadGame_whenAlreadyPurchased_incrementsDownloadsAndReturnsDto() {
        Game game = game(10, "Fenix Quest", 4, BigDecimal.ZERO);

        when(purchaseRepository.existsByClientIdAndGameId(1, 10)).thenReturn(true);
        when(gameRepository.findById(10)).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DownloadResponseDTO response = purchaseService.downloadGame(1, 10);

        assertNotNull(response);
        assertEquals(10, response.getGameId());
        assertEquals("Fenix Quest", response.getTitle());
        assertEquals(5, response.getDownloads());

        ArgumentCaptor<Game> gameCaptor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository).save(gameCaptor.capture());
        assertEquals(5, gameCaptor.getValue().getDownloads());
        verify(purchaseRepository).existsByClientIdAndGameId(1, 10);
        verify(gameRepository).findById(10);
    }

    @Test
    void getLibraryByClientId_transformsPurchasesIntoLibraryDtos() {
        Client client = client(1, "ana");
        Game game = game(10, "Fenix Quest", 20, BigDecimal.valueOf(9.99));
        game.setDescription("Adventure game");
        game.setSizeMb(BigDecimal.valueOf(2048));
        Purchase purchase = new Purchase(5, client, game);

        when(purchaseRepository.findByClientId(1)).thenReturn(List.of(purchase));

        List<LibraryGameDTO> library = purchaseService.getLibraryByClientId(1);

        assertEquals(1, library.size());
        LibraryGameDTO dto = library.get(0);
        assertEquals(10, dto.getGameId());
        assertEquals("Fenix Quest", dto.getTitle());
        assertEquals("Adventure game", dto.getDescription());
        assertEquals(BigDecimal.valueOf(2048), dto.getTamanoMb());
        assertEquals(20, dto.getDownloads());
        assertEquals(BigDecimal.valueOf(9.99), dto.getPrice());
        verify(purchaseRepository).findByClientId(1);
    }

    private Client client(Integer id, String username) {
        Client client = new Client();
        client.setId(id);
        client.setUsername(username);
        return client;
    }

    private Game game(Integer id, String title, Integer downloads, BigDecimal price) {
        Game game = new Game();
        game.setId(id);
        game.setTitle(title);
        game.setDownloads(downloads);
        game.setPrice(price);
        return game;
    }
}
