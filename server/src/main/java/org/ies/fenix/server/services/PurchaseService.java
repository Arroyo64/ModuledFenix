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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PurchaseService {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private GameRepository gameRepository;

    public PurchaseResponseDTO createPurchase(PurchaseCreateDTO dto) {
        Client client = clientRepository.findById(dto.getClientId()).orElse(null);
        if (client == null) {
            return null;
        }

        Game game = gameRepository.findById(dto.getGameId()).orElse(null);
        if (game == null) {
            return null;
        }

        boolean alreadyPurchased = purchaseRepository.existsByClientIdAndGameId(dto.getClientId(), dto.getGameId());
        if (alreadyPurchased) {
            throw new AlreadyPurchasedException("El cliente ya compró este juego");
        }

        Purchase purchase = new Purchase();
        purchase.setClient(client);
        purchase.setGame(game);
        Purchase saved = purchaseRepository.save(purchase);
        return toResponseDTO(saved);
    }

    public List<PurchaseResponseDTO> getByClientId(Integer clientId) {
        List<Purchase> purchases = purchaseRepository.findByClientId(clientId);
        List<PurchaseResponseDTO> response = new ArrayList<>();

        for (Purchase purchase : purchases) {
            response.add(toResponseDTO(purchase));
        }

        return response;
    }

    public List<LibraryGameDTO> getLibraryByClientId(Integer clientId) {
        List<Purchase> purchases = purchaseRepository.findByClientId(clientId);
        List<LibraryGameDTO> library = new ArrayList<>();

        for (Purchase purchase : purchases) {
            Game game = purchase.getGame();

            LibraryGameDTO dto = new LibraryGameDTO();
            dto.setGameId(game.getId());
            dto.setTitle(game.getTitle());
            dto.setDescription(game.getDescription());
            dto.setTamanoMb(game.getSizeMb());
            dto.setDownloads(game.getDownloads());
            dto.setPrice(game.getPrice());

            library.add(dto);
        }

        return library;
    }

    public DownloadResponseDTO downloadGame(Integer clientId, Integer gameId) {
        boolean purchased = purchaseRepository.existsByClientIdAndGameId(clientId, gameId);
        if (!purchased) {
            return null;
        }

        Game game = gameRepository.findById(gameId).orElse(null);
        if (game == null) {
            return null;
        }

        Integer downloads = game.getDownloads();
        if (downloads == null) {
            downloads = 0;
        }

        game.setDownloads(downloads + 1);
        Game updatedGame = gameRepository.save(game);

        DownloadResponseDTO dto = new DownloadResponseDTO();
        dto.setGameId(updatedGame.getId());
        dto.setTitle(updatedGame.getTitle());
        dto.setDownloads(updatedGame.getDownloads());

        return dto;
    }

    public boolean hasPurchased(Integer clientId, Integer gameId) {
        return purchaseRepository.existsByClientIdAndGameId(clientId, gameId);
    }

    private PurchaseResponseDTO toResponseDTO(Purchase purchase) {
        PurchaseResponseDTO dto = new PurchaseResponseDTO();
        dto.setId(purchase.getId());
        dto.setClientId(purchase.getClient().getId());
        dto.setGameId(purchase.getGame().getId());
        dto.setGameTitle(purchase.getGame().getTitle());
        dto.setPrice(purchase.getGame().getPrice());
        return dto;
    }
}