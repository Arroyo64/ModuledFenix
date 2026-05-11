package org.ies.fenix.server.services;

import org.ies.fenix.controller.dto.purchase.DownloadResponseDTO;
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
class PurchaseServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private PurchaseService purchaseService;

    @Test
    void createPurchase_whenClientAndGameExistAndNotPurchased_savesAndReturnsDto() {
        Client client = client(1, "Ana");
        Game game = game(10, "Fenix Quest", 3, "9.99");
        Purchase savedPurchase = purchase(100, client, game);

        when(clientRepository.findById(1)).thenReturn(Optional.of(client));
        when(gameRepository.findById(10)).thenReturn(Optional.of(game));
        when(purchaseRepository.existsByClientIdAndGameId(1, 10)).thenReturn(false);
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(savedPurchase);

        PurchaseResponseDTO response = purchaseService.createPurchase(new PurchaseCreateDTO(1, 10));

        assertNotNull(response);
        assertEquals(100, response.getId());
        assertEquals(1, response.getClientId());
        assertEquals(10, response.getGameId());
        assertEquals("Fenix Quest", response.getGameTitle());
        assertEquals(new BigDecimal("9.99"), response.getPrice());

        verify(clientRepository).findById(1);
        verify(gameRepository).findById(10);
        verify(purchaseRepository).existsByClientIdAndGameId(1, 10);
        verify(purchaseRepository).save(any(Purchase.class));
    }

    @Test
    void createPurchase_whenClientDoesNotExist_returnsNullAndDoesNotUseOtherRepositories() {
        when(clientRepository.findById(99)).thenReturn(Optional.empty());

        PurchaseResponseDTO response = purchaseService.createPurchase(new PurchaseCreateDTO(99, 10));

        assertNull(response);
        verify(clientRepository).findById(99);
        verifyNoInteractions(gameRepository, purchaseRepository);
    }

    @Test
    void createPurchase_whenGameDoesNotExist_returnsNullAndDoesNotSavePurchase() {
        Client client = client(1, "Ana");

        when(clientRepository.findById(1)).thenReturn(Optional.of(client));
        when(gameRepository.findById(404)).thenReturn(Optional.empty());

        PurchaseResponseDTO response = purchaseService.createPurchase(new PurchaseCreateDTO(1, 404));

        assertNull(response);
        verify(clientRepository).findById(1);
        verify(gameRepository).findById(404);
        verify(purchaseRepository, never()).existsByClientIdAndGameId(any(), any());
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void createPurchase_whenAlreadyPurchased_throwsExceptionAndDoesNotSave() {
        Client client = client(1, "Ana");
        Game game = game(10, "Fenix Quest", 3, "9.99");

        when(clientRepository.findById(1)).thenReturn(Optional.of(client));
        when(gameRepository.findById(10)).thenReturn(Optional.of(game));
        when(purchaseRepository.existsByClientIdAndGameId(1, 10)).thenReturn(true);

        AlreadyPurchasedException exception = assertThrows(
                AlreadyPurchasedException.class,
                () -> purchaseService.createPurchase(new PurchaseCreateDTO(1, 10))
        );

        assertEquals("El cliente ya compró este juego", exception.getMessage());
        verify(purchaseRepository).existsByClientIdAndGameId(1, 10);
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void getLibraryByClientId_mapsPurchasesToLibraryGames() {
        Client client = client(1, "Ana");
        Game game = game(10, "Fenix Quest", 3, "9.99");
        game.setDescription("A fantasy visual novel");
        game.setSizeMb(new BigDecimal("512"));

        when(purchaseRepository.findByClientId(1)).thenReturn(List.of(purchase(100, client, game)));

        List<LibraryGameDTO> library = purchaseService.getLibraryByClientId(1);

        assertEquals(1, library.size());
        assertEquals(10, library.get(0).getGameId());
        assertEquals("Fenix Quest", library.get(0).getTitle());
        assertEquals("A fantasy visual novel", library.get(0).getDescription());
        assertEquals(new BigDecimal("512"), library.get(0).getTamanoMb());
        assertEquals(3, library.get(0).getDownloads());
        assertEquals(new BigDecimal("9.99"), library.get(0).getPrice());

        verify(purchaseRepository).findByClientId(1);
    }

    @Test
    void downloadGame_whenPurchased_incrementsDownloadsAndReturnsDto() {
        Game game = game(10, "Fenix Quest", 3, "9.99");

        when(purchaseRepository.existsByClientIdAndGameId(1, 10)).thenReturn(true);
        when(gameRepository.findById(10)).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DownloadResponseDTO response = purchaseService.downloadGame(1, 10);

        assertNotNull(response);
        assertEquals(10, response.getGameId());
        assertEquals("Fenix Quest", response.getTitle());
        assertEquals(4, response.getDownloads());

        verify(purchaseRepository).existsByClientIdAndGameId(1, 10);
        verify(gameRepository).findById(10);
        verify(gameRepository).save(game);
    }

    @Test
    void downloadGame_whenDownloadsIsNull_startsCounterAtOne() {
        Game game = game(10, "Fenix Quest", null, "9.99");

        when(purchaseRepository.existsByClientIdAndGameId(1, 10)).thenReturn(true);
        when(gameRepository.findById(10)).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DownloadResponseDTO response = purchaseService.downloadGame(1, 10);

        assertNotNull(response);
        assertEquals(1, response.getDownloads());
        verify(gameRepository).save(game);
    }

    @Test
    void downloadGame_whenNotPurchased_returnsNullAndDoesNotLoadGame() {
        when(purchaseRepository.existsByClientIdAndGameId(1, 10)).thenReturn(false);

        DownloadResponseDTO response = purchaseService.downloadGame(1, 10);

        assertNull(response);
        verify(purchaseRepository).existsByClientIdAndGameId(1, 10);
        verifyNoInteractions(gameRepository);
    }

    @Test
    void hasPurchased_delegatesToRepository() {
        when(purchaseRepository.existsByClientIdAndGameId(1, 10)).thenReturn(true);

        boolean result = purchaseService.hasPurchased(1, 10);

        assertTrue(result);
        verify(purchaseRepository).existsByClientIdAndGameId(1, 10);
    }

    private Client client(Integer id, String username) {
        Client client = new Client();
        client.setId(id);
        client.setUsername(username);
        return client;
    }

    private Game game(Integer id, String title, Integer downloads, String price) {
        Client dev = client(2, "devUser");

        Game game = new Game();
        game.setId(id);
        game.setTitle(title);
        game.setDev(dev);
        game.setDescription("Description");
        game.setSizeMb(new BigDecimal("256"));
        game.setDownloads(downloads);
        game.setPrice(new BigDecimal(price));
        return game;
    }

    private Purchase purchase(Integer id, Client client, Game game) {
        Purchase purchase = new Purchase();
        purchase.setId(id);
        purchase.setClient(client);
        purchase.setGame(game);
        return purchase;
    }
}
